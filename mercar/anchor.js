// Asgaya identity anchor (BLAZE): a genesis CashToken NFT whose category is the
// funding input's txid; the commitment carries the discovery fields. The
// identity is the key that signs the genesis (the npub derives from it).
// Ported from the pre-BLAZE anchor-mint spike.
import {
  encodeTransaction, hexToBin, binToHex, NonFungibleTokenCapability, verifyTransactionTokens,
} from '@bitauth/libauth';

const DUST_SATOSHIS = 1000n;
const METHOD_TO_BYTE = { cash: 0, card: 1, bizum: 2 };

export function encodeAnchorCommitment(a) {
  const out = new Uint8Array(8);
  out[0] = 0x01;
  out[1] = a.role === 'seller' ? 1 : 0;
  const asset = (a.asset.toUpperCase() + '   ').slice(0, 3);
  out[2] = asset.charCodeAt(0);
  out[3] = asset.charCodeAt(1);
  out[4] = asset.charCodeAt(2);
  out[5] = METHOD_TO_BYTE[a.method] ?? 0xff;
  new DataView(out.buffer).setUint16(6, a.feeBps & 0xffff, true);
  return out;
}

export function buildGenesisAnchor(lockingBytecode, utxo, commitment) {
  if (utxo.vout !== 0) throw new Error(`Genesis requires a vout-0 UTXO (got ${utxo.vout}).`);
  if (commitment.length > 128) throw new Error('Commitment exceeds 128 bytes.');
  const outpointHash = hexToBin(utxo.txid); // display order, libauth reverses on encode
  const category = outpointHash;
  const sourceOutput = {
    outpointTransactionHash: outpointHash,
    outpointIndex: utxo.vout,
    sequenceNumber: 0xfffffffe,
    unlockingBytecode: new Uint8Array(0),
    lockingBytecode,
    valueSatoshis: utxo.satoshis,
  };
  const build = (changeSatoshis) => ({
    version: 2,
    locktime: 0,
    inputs: [{ outpointTransactionHash: outpointHash, outpointIndex: utxo.vout, sequenceNumber: 0xfffffffe, unlockingBytecode: new Uint8Array(0) }],
    outputs: [
      {
        lockingBytecode,
        valueSatoshis: DUST_SATOSHIS,
        token: { amount: 0n, category, nft: { capability: NonFungibleTokenCapability.none, commitment } },
      },
      { lockingBytecode, valueSatoshis: changeSatoshis },
    ],
  });
  const dummy = build(0n);
  const withDummy = { ...dummy, inputs: [{ ...dummy.inputs[0], unlockingBytecode: new Uint8Array(140) }] };
  const fee = BigInt(encodeTransaction(withDummy).length);
  const change = utxo.satoshis - DUST_SATOSHIS - fee;
  if (change < 0n) throw new Error(`Insufficient sats: need ${DUST_SATOSHIS + fee}, have ${utxo.satoshis}.`);
  const transaction = build(change);
  if (commitment.length <= 40) {
    const verdict = verifyTransactionTokens(transaction, [sourceOutput]);
    if (verdict !== true) throw new Error(`Token validation failed: ${verdict}`);
  }
  return { transaction, sourceOutput, fee, categoryHex: binToHex(category) };
}

export function buildSelfSend(lockingBytecode, utxo) {
  const outpointHash = hexToBin(utxo.txid);
  const sourceOutput = {
    outpointTransactionHash: outpointHash,
    outpointIndex: utxo.vout,
    sequenceNumber: 0xfffffffe,
    unlockingBytecode: new Uint8Array(0),
    lockingBytecode,
    valueSatoshis: utxo.satoshis,
  };
  const build = (value) => ({
    version: 2,
    locktime: 0,
    inputs: [{ outpointTransactionHash: outpointHash, outpointIndex: utxo.vout, sequenceNumber: 0xfffffffe, unlockingBytecode: new Uint8Array(0) }],
    outputs: [{ lockingBytecode, valueSatoshis: value }],
  });
  const dummy = build(0n);
  const withDummy = { ...dummy, inputs: [{ ...dummy.inputs[0], unlockingBytecode: new Uint8Array(140) }] };
  const fee = BigInt(encodeTransaction(withDummy).length);
  const newValue = utxo.satoshis - fee;
  if (newValue <= 0n) throw new Error('UTXO too small for a self-send.');
  return { transaction: build(newValue), sourceOutput, fee, newValue };
}
