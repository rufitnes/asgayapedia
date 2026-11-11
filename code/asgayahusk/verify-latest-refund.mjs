#!/usr/bin/env node
import { ElectrumNetworkProvider } from 'cashscript';
import pkg from 'bitcoincashjs-lib';
const { Transaction } = pkg;

const provider = new ElectrumNetworkProvider('testnet3');
const txid = '2b397368e462e1a1105ddb3e1b69aaaf0cc03f303735985f18b31610b6d079fa';
const senderAddress = 'bchtest:qrw5nukh5jqend8922tf8zhxwyku6wfpxu9nl79hxf';

console.log('🔍 Verifying Refund Transaction');
console.log('═'.repeat(60));
console.log(`TXID: ${txid}`);
console.log();

// Get raw transaction
const rawTx = await provider.getRawTransaction(txid);
const tx = Transaction.fromHex(rawTx);

console.log('📤 Transaction Outputs:');
tx.outs.forEach((out, i) => {
    const value = out.value;
    console.log(`  Output ${i}: ${value} sats (${(value / 100000000).toFixed(8)} BCH)`);
});
console.log();

// Check sender's UTXOs
const senderUtxos = await provider.getUtxos(senderAddress);
const refundUtxos = senderUtxos.filter(u => u.txid === txid);

console.log(`✅ Sender received ${refundUtxos.length} outputs from this transaction:`);
refundUtxos.forEach(u => {
    const sats = Number(u.satoshis);
    console.log(`   Output ${u.vout}: ${sats} sats (${(sats / 100000000).toFixed(8)} BCH)`);
});
console.log();

if (refundUtxos.length === 2) {
    const output0 = refundUtxos.find(u => u.vout === 0);
    const output1 = refundUtxos.find(u => u.vout === 1);
    
    if (output0 && output1) {
        console.log('✅ BOTH OUTPUTS WENT TO SENDER!');
        console.log(`   EUR payment: ${Number(output0.satoshis)} sats`);
        console.log(`   Buffer:      ${Number(output1.satoshis)} sats`);
    }
} else {
    console.log('❌ Not all outputs went to sender!');
}

process.exit(0);
