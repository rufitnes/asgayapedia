import { useState } from "react";
import QRCode from "qrcode";
import { WizardConnectSigner } from "@cashmint/sdk";
import { binToHex, lockingBytecodeToCashAddress } from "@bitauth/libauth";
import {
  encodeAnchorCommitment,
  buildGenesisAnchor,
  buildSelfSend,
  type AnchorFields,
} from "./anchor";
import {
  scripthashFromLockingBytecode,
  electrumListUnspent,
  type ElectrumUtxo,
} from "./electrum";

const PREFIX = "bchtest"; // chipnet
// Electrum over WebSocket (the browser can't do raw TCP). Try several — the
// WS port 50003 on imaginary.cash is currently down; WSS 50004 works.
const ELECTRUM_ENDPOINTS = [
  "wss://chipnet.imaginary.cash:50004",
  "wss://chipnet.bch.ninja:50004",
];

export default function App() {
  const [signer, setSigner] = useState<WizardConnectSigner | null>(null);
  const [lockingBytecode, setLockingBytecode] = useState<Uint8Array | null>(null);
  const [address, setAddress] = useState<string | null>(null);
  const [uri, setUri] = useState<string | null>(null);
  const [qr, setQr] = useState<string | null>(null);
  const [utxos, setUtxos] = useState<ElectrumUtxo[] | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<{ txid?: string; category: string; commitment: string } | null>(null);

  // Anchor fields
  const [role, setRole] = useState<AnchorFields["role"]>("seller");
  const [asset, setAsset] = useState("EUR");
  const [method, setMethod] = useState<AnchorFields["method"]>("bizum");
  const [feeBps, setFeeBps] = useState(50);

  async function connect() {
    setError(null); setUri(null); setQr(null); setBusy(true);
    try {
      const s = await WizardConnectSigner.connect({
        onUri: async (u, q) => {
          setUri(u);
          try { setQr(await QRCode.toDataURL(q ?? u, { width: 280, margin: 1 })); } catch { /* optional */ }
        },
        broadcast: true,
        userPrompt: "Publish the Asgaya anchor",
        dappName: "Mercar Asgaya",
      });
      setSigner(s);
      const lb = await s.getLockingBytecode();
      setLockingBytecode(lb);
      const r = lockingBytecodeToCashAddress({ prefix: PREFIX, bytecode: lb, tokenSupport: true });
      setAddress(typeof r === "string" ? r : r.address);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally { setBusy(false); }
  }

  async function loadUtxos() {
    if (!lockingBytecode) return;
    setError(null); setBusy(true);
    try {
      const sh = await scripthashFromLockingBytecode(lockingBytecode);
      let list: ElectrumUtxo[] | null = null;
      let lastErr: unknown = null;
      for (const ep of ELECTRUM_ENDPOINTS) {
        try { list = await electrumListUnspent(ep, sh); break; }
        catch (e) { lastErr = e; }
      }
      if (!list) throw lastErr instanceof Error ? lastErr : new Error("All electrum endpoints failed");
      list.sort((a, b) => b.value - a.value);
      setUtxos(list);
      if (!list.some((u) => u.tx_pos === 0)) {
        setError("No vout-0 UTXO found. Genesis needs the funding output to be #0 — do a small self-send so your address is output 0, then retry.");
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally { setBusy(false); }
  }

  async function prepareVout0() {
    if (!signer || !lockingBytecode || !utxos || utxos.length === 0) return;
    setError(null); setBusy(true);
    try {
      const u = utxos[0]!; // largest first
      const send = buildSelfSend(lockingBytecode, {
        txid: u.tx_hash,
        vout: u.tx_pos,
        satoshis: BigInt(u.value),
      });
      await signer.signTransaction({
        transaction: send.transaction,
        sourceOutput: send.sourceOutput,
        inputIndex: 0,
      });
      // give the mempool a moment, then refresh the UTXO list
      setTimeout(() => void loadUtxos(), 5000);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally { setBusy(false); }
  }

  async function mint() {
    if (!signer || !lockingBytecode) return;
    setError(null); setResult(null); setBusy(true);
    try {
      const u = (utxos ?? []).find((x) => x.tx_pos === 0 && x.value >= 2000)
        ?? (utxos ?? []).find((x) => x.tx_pos === 0);
      if (!u) throw new Error("No vout-0 UTXO. Load UTXOs first (and do a self-send if needed).");

      const fields: AnchorFields = { role, asset, method, feeBps };
      const commitment = encodeAnchorCommitment(fields);
      const built = buildGenesisAnchor(
        lockingBytecode,
        { txid: u.tx_hash, vout: u.tx_pos, satoshis: BigInt(u.value) },
        commitment,
      );
      const res = await signer.signTransaction({
        transaction: built.transaction,
        sourceOutput: built.sourceOutput,
        inputIndex: 0,
      });
      setResult({
        txid: res.txid,
        category: built.categoryHex,
        commitment: binToHex(commitment),
      });
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally { setBusy(false); }
  }

  const s: Record<string, React.CSSProperties> = {
    app: { maxWidth: 660, margin: "40px auto", fontFamily: "system-ui, sans-serif", padding: "0 20px", color: "#111" },
    card: { border: "1px solid #e2e8f0", borderRadius: 12, padding: 24, marginTop: 20, background: "#fff" },
    h2: { fontSize: 18, fontWeight: 600, marginBottom: 12 },
    label: { display: "block", fontWeight: 500, fontSize: 13, marginBottom: 4 },
    input: { width: "100%", padding: "8px 10px", border: "1px solid #cbd5e1", borderRadius: 6, fontSize: 14, marginBottom: 12, boxSizing: "border-box" },
    btn: { padding: "10px 20px", background: "#2563eb", color: "#fff", border: "none", borderRadius: 7, fontSize: 14, fontWeight: 600, cursor: "pointer" },
    mono: { fontFamily: "monospace", fontSize: 12, wordBreak: "break-all", background: "#f8fafc", padding: 10, borderRadius: 6, marginTop: 8 },
    err: { background: "#fef2f2", border: "1px solid #fecaca", borderRadius: 6, padding: "10px 14px", color: "#b91c1c", fontSize: 13, marginTop: 12 },
    ok: { background: "#f0fdf4", border: "1px solid #bbf7d0", borderRadius: 6, padding: "10px 14px", color: "#166534", fontSize: 13, marginTop: 12 },
  };
  const vout0 = utxos?.find((u) => u.tx_pos === 0);

  return (
    <div style={s.app}>
      <h1 style={{ fontSize: 22 }}>Mercar Asgaya — anchor spike</h1>
      <p style={{ color: "#64748b", fontSize: 13 }}>
        Chipnet · publish an ad = mint the <strong>genesis anchor NFT</strong> (vout-0 funding → its txid is the category).
      </p>

      {!signer && (
        <div style={s.card}>
          <h2 style={s.h2}>1 · Connect wallet (WizardConnect)</h2>
          <button style={s.btn} onClick={connect} disabled={busy}>{busy ? "Waiting for wallet…" : "Connect via WizardConnect"}</button>
          {qr && <div style={{ marginTop: 16 }}><img src={qr} width={280} height={280} alt="WizardConnect QR" /></div>}
          {uri && (
            <div style={s.mono}>
              <button style={{ ...s.btn, padding: "4px 10px", fontSize: 12, marginBottom: 8 }} onClick={() => navigator.clipboard.writeText(uri)}>Copy URI</button>
              <br />{uri}
            </div>
          )}
          {error && <div style={s.err}>{error}</div>}
        </div>
      )}

      {signer && (
        <div style={s.card}>
          <h2 style={s.h2}>2 · Publish the anchor</h2>
          <div style={{ fontSize: 13 }}>
            <strong>Address (chipnet):</strong>
            <div style={s.mono}>{address ?? "—"}</div>
          </div>
          <button style={{ ...s.btn, background: "#0ea5e9", marginTop: 10 }} onClick={loadUtxos} disabled={busy}>
            {busy ? "Loading…" : "Find my UTXOs"}
          </button>
          {utxos && (
            <div style={{ ...s.mono, marginTop: 10 }}>
              {utxos.length === 0 && "No UTXOs — faucet the address first."}
              {utxos.map((u) => (
                <div key={u.tx_hash + u.tx_pos}>
                  {u.tx_pos === 0 ? "✅ vout 0" : "·"} {u.tx_hash.slice(0, 16)}…:{u.tx_pos} — {u.value} sat
                </div>
              ))}
            </div>
          )}

          {utxos && !utxos.some((u) => u.tx_pos === 0) && (
            <div style={{ marginTop: 10 }}>
              <button style={{ ...s.btn, background: "#f59e0b" }} onClick={prepareVout0} disabled={busy}>
                {busy ? "Sending…" : "Prepare a vout-0 UTXO (self-send)"}
              </button>
              <p style={{ fontSize: 12, color: "#64748b", marginTop: 6 }}>
                Sends your BCH back to yourself in a single output (#0), so a genesis anchor can be created.
              </p>
            </div>
          )}

          <hr style={{ margin: "16px 0", border: "none", borderTop: "1px solid #e2e8f0" }} />

          <label style={s.label}>Role</label>
          <select style={s.input} value={role} onChange={(e) => setRole(e.target.value as AnchorFields["role"])}>
            <option value="seller">seller (BCH for ⟨asset⟩)</option>
            <option value="buyer">buyer (⟨asset⟩ for BCH)</option>
          </select>

          <label style={s.label}>Asset</label>
          <input style={s.input} value={asset} maxLength={3} onChange={(e) => setAsset(e.target.value.toUpperCase())} />

          <label style={s.label}>Method</label>
          <select style={s.input} value={method} onChange={(e) => setMethod(e.target.value as AnchorFields["method"])}>
            <option value="cash">cash</option>
            <option value="card">card</option>
            <option value="bizum">bizum</option>
          </select>

          <label style={s.label}>Fee (bps — 50 = 0.50 %)</label>
          <input style={s.input} type="number" value={feeBps} onChange={(e) => setFeeBps(parseInt(e.target.value) || 0)} />

          <button style={s.btn} onClick={mint} disabled={busy || !vout0}>
            {busy ? "Signing…" : vout0 ? "Publish anchor NFT" : "Need a vout-0 UTXO"}
          </button>

          {error && <div style={s.err}>{error}</div>}
          {result && (
            <div style={s.ok}>
              <strong>Anchored ✓</strong>
              {result.txid && <div style={s.mono}>txid: {result.txid}</div>}
              <div style={s.mono}>category: {result.category}</div>
              <div style={s.mono}>commitment: {result.commitment} (8 B)</div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
