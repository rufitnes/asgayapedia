// Asgaya anchor — the genesis-NFT builder (BCH BLAZE 2026 spike)
//
// Our anchor is a *genesis* CashToken NFT (a new category per mint): the anchor
// NFT carries a small commitment (≤ 40 B) describing a classified ad, and its
// category IS the txid of the funding input (libauth: `extractGenesisCategories`).
// The funding UTXO must therefore be vout 0.

import {
  encodeTransaction,
  hexToBin,
  binToHex,
  NonFungibleTokenCapability,
  verifyTransactionTokens,
  type Transaction,
} from "@bitauth/libauth";

// ── The commitment (≤ 40 B) ──────────────────────────────────────────────────
// 8 bytes: version(1) · role(1) · asset(3 ASCII) · method(1) · fee_bps(2, LE)
export interface AnchorFields {
  role: "seller" | "buyer";
  asset: string; // e.g. EUR, VES, USD, PUSD
  method: "cash" | "card" | "bizum";
  feeBps: number; // e.g. 50 = 0.50 %
}

const METHOD_TO_BYTE: Record<AnchorFields["method"], number> = { cash: 0, card: 1, bizum: 2 };
const BYTE_TO_METHOD: AnchorFields["method"][] = ["cash", "card", "bizum"];

export function encodeAnchorCommitment(a: AnchorFields): Uint8Array {
  const out = new Uint8Array(8);
  out[0] = 0x01; // version
  out[1] = a.role === "seller" ? 1 : 0;
  const asset = (a.asset.toUpperCase() + "   ").slice(0, 3);
  out[2] = asset.charCodeAt(0);
  out[3] = asset.charCodeAt(1);
  out[4] = asset.charCodeAt(2);
  out[5] = METHOD_TO_BYTE[a.method] ?? 0xff;
  new DataView(out.buffer).setUint16(6, a.feeBps & 0xffff, true);
  return out;
}

export function decodeAnchorCommitment(c: Uint8Array): AnchorFields | null {
  if (c.length < 8 || c[0] !== 0x01) return null;
  return {
    role: c[1] === 1 ? "seller" : "buyer",
    asset: String.fromCharCode(c[2], c[3], c[4]).trim(),
    method: BYTE_TO_METHOD[c[5]] ?? "cash",
    feeBps: new DataView(c.buffer, c.byteOffset, c.byteLength).getUint16(6, true),
  };
}

// ── The genesis tx ───────────────────────────────────────────────────────────

const DUST_SATOSHIS = 1000n; // CashTokens output minimum

export interface SourceUTXO {
  txid: string; // display order (as from Electrum)
  vout: number;
  satoshis: bigint;
}

/** The source-output shape `WizardConnectSigner.signTransaction` expects (SIGHASH_UTXOS). */
export interface AnchorSourceOutput {
  outpointTransactionHash: Uint8Array;
  outpointIndex: number;
  sequenceNumber: number;
  unlockingBytecode: Uint8Array;
  lockingBytecode: Uint8Array;
  valueSatoshis: bigint;
}

export interface BuiltAnchor {
  transaction: Transaction;
  sourceOutput: AnchorSourceOutput;
  fee: bigint;
  categoryHex: string;
}

/**
 * Build (and locally validate) a genesis-anchor transaction:
 *   input 0: the funding UTXO (MUST be vout 0 → its txid becomes the category)
 *   output 0: the anchor NFT (capability none, our commitment), 1000 sat dust
 *   output 1: BCH change back to the same address
 */
export function buildGenesisAnchor(
  lockingBytecode: Uint8Array,
  utxo: SourceUTXO,
  commitment: Uint8Array,
): BuiltAnchor {
  if (utxo.vout !== 0) {
    throw new Error(
      `Genesis requires the funding UTXO to be vout 0 (got ${utxo.vout}). ` +
        "Send yourself some sats so your address is output 0, then retry.",
    );
  }
  // The network allows NFT commitments up to 128 bytes (CHIP-2024-12 P2S, active 15 May 2026).
  if (commitment.length > 128) throw new Error("Commitment exceeds the 128-byte CashTokens limit (P2S, 2026).");

  const outpointHash = hexToBin(utxo.txid); // display order — libauth reverses on encode
  const category = outpointHash; // genesis: category = the (vout-0) input's txid

  const sourceOutput: AnchorSourceOutput = {
    outpointTransactionHash: outpointHash,
    outpointIndex: utxo.vout,
    sequenceNumber: 0xfffffffe,
    unlockingBytecode: new Uint8Array(0),
    lockingBytecode,
    valueSatoshis: utxo.satoshis,
  };

  const build = (changeSatoshis: bigint): Transaction => ({
    version: 2,
    locktime: 0,
    inputs: [
      {
        outpointTransactionHash: outpointHash,
        outpointIndex: utxo.vout,
        sequenceNumber: 0xfffffffe,
        unlockingBytecode: new Uint8Array(0),
      },
    ],
    outputs: [
      {
        lockingBytecode,
        valueSatoshis: DUST_SATOSHIS,
        token: {
          amount: 0n,
          category,
          nft: { capability: NonFungibleTokenCapability.none, commitment },
        },
      },
      { lockingBytecode, valueSatoshis: changeSatoshis },
    ],
  });

  // 1 sat/byte fee via a worst-case P2PKH unlocking script (uncompressed key)
  const dummy = build(0n);
  const withDummy: Transaction = {
    ...dummy,
    inputs: [{ ...dummy.inputs[0]!, unlockingBytecode: new Uint8Array(140) }],
  };
  const fee = BigInt(encodeTransaction(withDummy).length);

  const change = utxo.satoshis - DUST_SATOSHIS - fee;
  if (change < 0n) {
    throw new Error(
      `Insufficient sats: need ${DUST_SATOSHIS + fee} (dust + ${fee} fee), have ${utxo.satoshis}.`,
    );
  }

  const transaction = build(change);

  // Local token-aware validation. NOTE: libauth (this build) implements the
  // *2023* CashTokens consensus — commitment ≤ 40 B — it predates P2S/128. So we
  // only trust its verdict for commitments ≤ 40 B; above that, skip it (the
  // network allows 128 B since CHIP-2024-12 P2S, 15 May 2026).
  if (commitment.length <= 40) {
    const verdict = verifyTransactionTokens(transaction, [sourceOutput]);
    if (verdict !== true) throw new Error(`Token validation failed: ${verdict}`);
  }

  return { transaction, sourceOutput, fee, categoryHex: binToHex(category) };
}

export interface BuiltSend {
  transaction: Transaction;
  sourceOutput: AnchorSourceOutput;
  fee: bigint;
  newValue: bigint;
}

/**
 * A self-send: spend `utxo` and pay it all back to our own address in a single
 * output — so our address becomes **output 0** (a valid genesis funding UTXO).
 * Used when the wallet has no vout-0 UTXO to anchor from.
 */
export function buildSelfSend(lockingBytecode: Uint8Array, utxo: SourceUTXO): BuiltSend {
  const outpointHash = hexToBin(utxo.txid);
  const sourceOutput: AnchorSourceOutput = {
    outpointTransactionHash: outpointHash,
    outpointIndex: utxo.vout,
    sequenceNumber: 0xfffffffe,
    unlockingBytecode: new Uint8Array(0),
    lockingBytecode,
    valueSatoshis: utxo.satoshis,
  };

  const build = (value: bigint): Transaction => ({
    version: 2,
    locktime: 0,
    inputs: [
      {
        outpointTransactionHash: outpointHash,
        outpointIndex: utxo.vout,
        sequenceNumber: 0xfffffffe,
        unlockingBytecode: new Uint8Array(0),
      },
    ],
    outputs: [{ lockingBytecode, valueSatoshis: value }],
  });

  const dummy = build(0n);
  const withDummy: Transaction = {
    ...dummy,
    inputs: [{ ...dummy.inputs[0]!, unlockingBytecode: new Uint8Array(140) }],
  };
  const fee = BigInt(encodeTransaction(withDummy).length);
  const newValue = utxo.satoshis - fee;
  if (newValue <= 0n) throw new Error("UTXO too small for a self-send.");

  return { transaction: build(newValue), sourceOutput, fee, newValue };
}
