// WizardConnect dapp signer (adapted from the anchor-mint spike): connect,
// derive the wallet's P2PKH locking bytecode, sign a transaction, disconnect.
import { publicKeyToP2pkhLockingBytecode, hexToBin, hash256, binToHex, decodeHdPublicKey, deriveHdPublicNodeChild } from '@bitauth/libauth';
import { initiateDappRelay, childIndexOfPathName } from '@wizardconnect/core';
import { transactionToHex, sourceOutputToRelay } from '@wizardconnect/core/hdwalletv1-serialize';
import { DappConnectionManager } from '@wizardconnect/dapp';

// Wallets like Selene send a first `wallet_ready` with empty paths and a second one
// only after the user approves the connection. Wait for the paths, not the first event.
function waitForPaths(mgr, timeoutMs) {
  return new Promise((resolve, reject) => {
    if (mgr.getSessionPaths().length > 0) return resolve();
    const timer = setTimeout(() => {
      mgr.off('walletready', onReady);
      reject(new Error('The wallet did not approve the connection. Approve it in the wallet and try again.'));
    }, timeoutMs);
    const onReady = () => {
      if (mgr.getSessionPaths().length === 0) return;
      clearTimeout(timer);
      mgr.off('walletready', onReady);
      resolve();
    };
    mgr.on('walletready', onReady);
  });
}

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
    const signer = () => new WizardConnectSigner(mgr, pathName, addressIndex, broadcast, userPrompt);
    const stored = mgr.loadStoredSession();
    if (stored && stored.walletPublicKey) {
      try {
        const relay = initiateDappRelay((payload) => mgr.updateConnection(payload.client, payload.status), {
          existingCredentials: { privateKey: stored.privateKey, secret: stored.secret, walletPublicKey: stored.walletPublicKey },
        });
        mgr.attachRelay(relay);
        // The wallet may need to be opened; wait for the session paths before returning.
        return waitForPaths(mgr, 90000).then(signer);
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
      mgr.once('disconnect', (reason, msg) => reject(new Error(`WizardConnect disconnected: ${reason}${msg ? ` — ${msg}` : ''}`)));
      waitForPaths(mgr, 90000).then(() => resolve(signer())).catch(reject);
    });
  }

  async getPubkey() {
    const child = childIndexOfPathName(this.pathName);
    let pubkey = this.mgr.getPubkey(child, BigInt(this.addressIndex));
    if (!pubkey) {
      // Some wallets do not publish the receive path. Use any path they did publish (index 0)
      // and sign with that same path name, not the one we asked for.
      for (let c = 0; c <= 8 && !pubkey; c++) {
        if (c === child || !this.mgr.hasPath(c)) continue;
        pubkey = this.mgr.getPubkey(c, 0n);
        if (pubkey) {
          const path = this.mgr.getSessionPaths().find((p) => childIndexOfPathName(p.name) === c);
          if (path) { this.pathName = path.name; this.addressIndex = 0; }
        }
      }
    }
    if (!pubkey) {
      // Last resort: derive from any xpub the wallet published, whatever its path name.
      for (const path of this.mgr.getSessionPaths()) {
        const decoded = decodeHdPublicKey(path.xpub);
        if (typeof decoded === 'string') continue;
        const child = deriveHdPublicNodeChild(decoded.node, Number(this.addressIndex));
        if (typeof child === 'string') continue;
        pubkey = child.publicKey;
        this.pathName = path.name;
        break;
      }
    }
    if (!pubkey) {
      const names = this.mgr.getSessionPaths().map((p) => p.name).join(', ') || 'none';
      throw new Error(`This wallet connected but did not share a usable key (paths: ${names}). Try again, or use Cashonize, Paytaca or Selene.`);
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
