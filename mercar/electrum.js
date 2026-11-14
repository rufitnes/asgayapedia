// Minimal chipnet Electrum (WebSocket) helper: list the BCH UTXOs of an address.
export async function scripthashFromLockingBytecode(lb) {
  const digest = await crypto.subtle.digest('SHA-256', lb);
  const bytes = new Uint8Array(digest).slice().reverse();
  return Array.from(bytes, (b) => b.toString(16).padStart(2, '0')).join('');
}

export function electrumListUnspent(wsUrl, scripthash) {
  return new Promise((resolve, reject) => {
    let settled = false;
    let timer;
    const ws = new WebSocket(wsUrl);
    const done = (fn) => {
      if (settled) return;
      settled = true;
      if (timer) clearTimeout(timer);
      try { ws.close(); } catch { /* ignore */ }
      fn();
    };
    timer = setTimeout(() => done(() => reject(new Error(`Electrum timeout (${wsUrl})`))), 15000);
    ws.onopen = () => {
      ws.send(JSON.stringify({ jsonrpc: '2.0', id: 1, method: 'server.version', params: ['mercar-asgaya', '1.4'] }));
      ws.send(JSON.stringify({ jsonrpc: '2.0', id: 2, method: 'blockchain.scripthash.listunspent', params: [scripthash] }));
    };
    ws.onmessage = (ev) => {
      let msg;
      try { msg = JSON.parse(ev.data); } catch { return; }
      if (msg.id !== 2) return;
      if (msg.error) done(() => reject(new Error(msg.error.message ?? 'electrum error')));
      else done(() => resolve(msg.result ?? []));
    };
    ws.onerror = () => done(() => reject(new Error(`Could not connect to electrum ${wsUrl}`)));
  });
}
