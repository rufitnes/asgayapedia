#!/usr/bin/env node
import { ElectrumNetworkProvider } from 'cashscript';
import pkg from 'bitcoincashjs-lib';
const { Transaction } = pkg;

const provider = new ElectrumNetworkProvider('testnet3');
const txid = '4a01498a212a835cfd4cc4573c8cef94e9024897895b7f062474518b54cc8a76';
const recipientAddress = 'bchtest:qq2uxg4cu9axyzd9gjnhxwrvealt44mcwunp7gzd0k';
const senderAddress = 'bchtest:qrw5nukh5jqend8922tf8zhxwyku6wfpxu9nl79hxf';

console.log('🔍 Verifying Claim Transaction');
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

// Check recipient's UTXOs
const recipientUtxos = await provider.getUtxos(recipientAddress);
const recipientClaimUtxos = recipientUtxos.filter(u => u.txid === txid);

console.log(`✅ Recipient (Isabel) received ${recipientClaimUtxos.length} output(s):`);
recipientClaimUtxos.forEach(u => {
    const sats = Number(u.satoshis);
    console.log(`   Output ${u.vout}: ${sats} sats (€7 payment)`);
});
console.log();

// Check sender's UTXOs (buffer)
const senderUtxos = await provider.getUtxos(senderAddress);
const senderBufferUtxos = senderUtxos.filter(u => u.txid === txid);

console.log(`✅ Sender received ${senderBufferUtxos.length} output(s):`);
senderBufferUtxos.forEach(u => {
    const sats = Number(u.satoshis);
    console.log(`   Output ${u.vout}: ${sats} sats (buffer back to funder)`);
});
console.log();

if (recipientClaimUtxos.length === 1 && senderBufferUtxos.length === 1) {
    console.log('✅ CLAIM SUCCESSFUL!');
    console.log('✅ Payment flow complete: Sender → Covenant → Recipient');
    console.log('✅ Buffer returned to funder (sender)');
} else {
    console.log('❌ Unexpected output distribution');
}

process.exit(0);
