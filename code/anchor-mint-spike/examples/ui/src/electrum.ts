// Minimal chipnet Electrum (WebSocket) helper — list the BCH UTXOs of an address.
// We only need `listunspent`; broadcasting is done by the wallet (WizardConnect).

export interface ElectrumUtxo {
  height: number;
  tx_hash: string; // display order
  tx_pos: number; // vout
  value: number; // satoshis
}

/** Electrum scripthash = reverse(SHA-256(lockingBytecode)), hex. */
export async function scripthashFromLockingBytecode(lb: Uint8Array): Promise<string> {
  const digest = await crypto.subtle.digest("SHA-256", lb as unknown as BufferSource);
  const bytes = new Uint8Array(digest).slice().reverse();
  return Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
}

export function electrumListUnspent(wsUrl: string, scripthash: string): Promise<ElectrumUtxo[]> {
  return new Promise((resolve, reject) => {
    let settled = false;
    let timer: ReturnType<typeof setTimeout> | undefined;
    const ws = new WebSocket(wsUrl);

    const done = (fn: () => void) => {
      if (settled) return;
      settled = true;
      if (timer) clearTimeout(timer);
      try { ws.close(); } catch { /* ignore */ }
      fn();
    };

    timer = setTimeout(() => done(() => reject(new Error(`Electrum timeout (${wsUrl})`))), 15000);

    ws.onopen = () => {
      ws.send(JSON.stringify({ jsonrpc: "2.0", id: 1, method: "server.version", params: ["asgaya-spike", "1.4"] }));
      ws.send(JSON.stringify({ jsonrpc: "2.0", id: 2, method: "blockchain.scripthash.listunspent", params: [scripthash] }));
    };
    ws.onmessage = (ev) => {
      let msg: { id?: number; result?: unknown; error?: { message?: string } };
      try { msg = JSON.parse(ev.data as string); } catch { return; }
      if (msg.id !== 2) return;
      if (msg.error) done(() => reject(new Error(msg.error!.message ?? "electrum error")));
      else done(() => resolve((msg.result as ElectrumUtxo[]) ?? []));
    };
    ws.onerror = () => done(() => reject(new Error(`Could not connect to electrum ${wsUrl}`)));
  });
}
