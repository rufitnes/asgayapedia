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

  static connect({ onUri, onCancel, pathName = 'receive', addressIndex = 0, broadcast = true, userPrompt = 'Confirm payment', dappName = 'Mercar Asgaya' }) {
    const mgr = new DappConnectionManager(dappName);
    const stored = mgr.loadStoredSession();
    if (stored && stored.walletPublicKey) {
      try {
        const relay = initiateDappRelay((payload) => mgr.updateConnection(payload.client, payload.status), {
          existingCredentials: { privateKey: stored.privateKey, secret: stored.secret, walletPublicKey: stored.walletPublicKey },
        });
        mgr.attachRelay(relay);
        return Promise.resolve(new WizardConnectSigner(mgr, pathName, addressIndex, broadcast, userPrompt));
      } catch {
        mgr.clearStoredSession();
      }
    }
    return new Promise((resolve, reject) => {
      const relay = initiateDappRelay((payload) => mgr.updateConnection(payload.client, payload.status));
      mgr.attachRelay(relay);
      if (onCancel) onCancel(() => {
        try { relay.cleanup(); } catch { /* ignore */ }
        try { mgr.clearStoredSession(); } catch { /* ignore */ }
        reject(new Error('Cancelled'));
      });
      onUri(relay.uri, relay.qrUri);
      mgr.once('walletready', () => resolve(new WizardConnectSigner(mgr, pathName, addressIndex, broadcast, userPrompt)));
      mgr.once('disconnect', (reason, msg) => reject(new Error(`WizardConnect disconnected: ${reason}${msg ? ` — ${msg}` : ''}`)));
    });
  }

  async getPubkey() {
    const child = childIndexOfPathName(this.pathName);
    let pubkey = this.mgr.getPubkey(child, BigInt(this.addressIndex));
    if (!pubkey) {
      // Some wallets do not publish the receive path. Use any path they did publish (index 0).
      for (let c = 0; c <= 8 && !pubkey; c++) {
        if (c === child || !this.mgr.hasPath(c)) continue;
        pubkey = this.mgr.getPubkey(c, 0n);
      }
    }
    if (!pubkey) {
      throw new Error('This wallet connected but did not share a receive address. Try again, or use Cashonize or Paytaca.');
    }
    return pubkey;
  }

  async getLockingBytecode() {
    return publicKeyToP2pkhLockingBytecode({ publicKey: await this.getPubkey() });
  }

  async signTransaction({ transaction, sourceOutput, inputIndex, userPrompt }) {
    const payload = {
      transaction: {
        transaction: transactionToHex(transaction.inputs, transaction.outputs, transaction.version, transaction.locktime),
        sourceOutputs: [sourceOutputToRelay(sourceOutput)],
        broadcast: this.broadcast,
        userPrompt: userPrompt ?? this.userPrompt,
      },
      inputPaths: [[inputIndex, this.pathName, this.addressIndex]],
    };
    const res = await this.mgr.signTransaction(payload);
    if (res.error) throw new Error(`WizardConnect signing failed: ${res.error}`);
    const signed = hexToBin(res.signedTransaction);
    const txid = this.broadcast ? binToHex(hash256(signed).slice().reverse()) : undefined;
    return { signedTxBytes: signed, txid };
  }

  onDisconnect(handler) {
    this.mgr.on('disconnect', (reason, msg) => {
      try { this.mgr.clearStoredSession(); } catch { /* ignore */ }
      handler(reason, msg);
    });
  }

  disconnect() {
    try { this.mgr.sendDisconnect(); } catch { /* ignore */ }
    try { this.mgr.clearStoredSession(); } catch { /* ignore */ }
  }
}
