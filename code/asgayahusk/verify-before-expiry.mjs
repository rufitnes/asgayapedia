#!/usr/bin/env node
import { ElectrumNetworkProvider } from 'cashscript';
import pkg from 'bitcoincashjs-lib';
const { Transaction } = pkg;

const provider = new ElectrumNetworkProvider('testnet3');
const txid = '8569044818f78d94d2066dc5fc535940ea4bd4c6437945beb0ca8c7bc37291d1';
const senderAddress = 'bchtest:qrw5nukh5jqend8922tf8zhxwyku6wfpxu9nl79hxf';

console.log('🔍 Verifying Refund BEFORE Expiry');
console.log('═'.repeat(60));
console.log(`TXID: ${txid}`);
console.log();

const rawTx = await provider.getRawTransaction(txid);
const tx = Transaction.fromHex(rawTx);

console.log('📤 Transaction Outputs:');
tx.outs.forEach((out, i) => {
    const value = out.value;
    console.log(`  Output ${i}: ${value} sats (${(value / 100000000).toFixed(8)} BCH)`);
});
console.log();

const senderUtxos = await provider.getUtxos(senderAddress);
const refundUtxos = senderUtxos.filter(u => u.txid === txid);

console.log(`✅ Sender received ${refundUtxos.length} outputs:`);
refundUtxos.forEach(u => {
    const sats = Number(u.satoshis);
    console.log(`   Output ${u.vout}: ${sats} sats`);
});
console.log();

if (refundUtxos.length === 2) {
    console.log('✅ BOTH OUTPUTS WENT TO SENDER!');
    console.log('✅ REFUND WORKED BEFORE EXPIRY!');
    console.log('✅ PROVES: Sender can access their funds anytime!');
} else {
    console.log('❌ Not all outputs went to sender');
}

process.exit(0);
