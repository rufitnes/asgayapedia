// WizardConnect dapp signer (adapted from the anchor-mint spike): connect,
// derive the wallet's P2PKH locking bytecode, sign a transaction, disconnect.
import { publicKeyToP2pkhLockingBytecode, hexToBin, hash256, binToHex } from '@bitauth/libauth';
import { initiateDappRelay, childIndexOfPathName } from '@wizardconnect/core';
import { transactionToHex, sourceOutputToRelay } from '@wizardconnect/core/hdwalletv1-serialize';
import { DappConnectionManager } from '@wizardconnect/dapp';

export class WizardConnectSigner {
  constructor(mgr, pathName, addressIndex, broadcast, userPrompt) {
    this.mgr = mgr;
    this.pathName = pathName;
    this.addressIndex = addressIndex;
    this.broadcast = broadcast;
    this.userPrompt = userPrompt;
  }

  static connect({ onUri, pathName = 'receive', addressIndex = 0, broadcast = true, userPrompt = 'Confirm payment', dappName = 'Mercar Asgaya' }) {
    const mgr = new DappConnectionManager(dappName);
    return new Promise((resolve, reject) => {
      const relay = initiateDappRelay((payload) => mgr.updateConnection(payload.client, payload.status));
      mgr.attachRelay(relay);
      onUri(relay.uri, relay.qrUri);
      mgr.once('walletready', () => resolve(new WizardConnectSigner(mgr, pathName, addressIndex, broadcast, userPrompt)));
      mgr.once('disconnect', (reason, msg) => reject(new Error(`WizardConnect disconnected: ${reason}${msg ? ` — ${msg}` : ''}`)));
    });
  }

  async getLockingBytecode() {
    const child = childIndexOfPathName(this.pathName);
    const pubkey = this.mgr.getPubkey(child, BigInt(this.addressIndex));
    if (!pubkey) throw new Error(`No pubkey for path '${this.pathName}[${this.addressIndex}]'`);
    return publicKeyToP2pkhLockingBytecode({ publicKey: pubkey });
  }

  async signTransaction({ transaction, sourceOutput, inputIndex }) {
    const payload = {
      transaction: {
        transaction: transactionToHex(transaction.inputs, transaction.outputs, transaction.version, transaction.locktime),
        sourceOutputs: [sourceOutputToRelay(sourceOutput)],
        broadcast: this.broadcast,
        userPrompt: this.userPrompt,
      },
      inputPaths: [[inputIndex, this.pathName, this.addressIndex]],
    };
    const res = await this.mgr.signTransaction(payload);
    if (res.error) throw new Error(`WizardConnect signing failed: ${res.error}`);
    const signed = hexToBin(res.signedTransaction);
    const txid = this.broadcast ? binToHex(hash256(signed).slice().reverse()) : undefined;
    return { signedTxBytes: signed, txid };
  }

  disconnect() {
    try { this.mgr.sendDisconnect(); } catch { /* ignore */ }
  }
}
