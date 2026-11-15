import { sha256, secp256k1, hexToBin, binToHex } from '@bitauth/libauth';
import { secp256k1 as noble } from '@noble/curves/secp256k1';
// Minimal NIP-99 read client: fetch the Asgaya board ads (kind 30402) from a relay.
export const NOSTR_RELAY = 'ws://192.168.1.115:7777';
export const KIND_LISTING = 30402;
export const TAG_BOARD = 'asgaya-board';

const tag = (ev, name) => (ev.tags || []).find((t) => t[0] === name)?.[1];
const allTags = (ev, name) => (ev.tags || []).filter((t) => t[0] === name).map((t) => t[1]);

export function parseListing(ev) {
  if (!ev || ev.kind !== KIND_LISTING) return null;
  const id = tag(ev, 'd');
  if (!id) return null;
  const ts = allTags(ev, 't');
  if (!ts.includes(TAG_BOARD)) return null;
  let content = {};
  try { content = JSON.parse(ev.content || '{}'); } catch { /* tags fallback */ }
  const adType = content.adType ||
    (ts.includes('asgaya-seller') ? 'SELLER' : ts.includes('asgaya-buyer') ? 'BUYER' : null);
  if (!adType) return null;
  const assets = content.assets ?? ts.filter((t) => t.startsWith('asset:')).map((t) => t.slice(6));
  const methods = content.paymentMethods ?? ts.filter((t) => t.startsWith('pay:')).map((t) => t.slice(4));
  const feeTag = tag(ev, 'fee');
  return {
    id,
    adType,
    cashAccount: content.cashAccount ?? tag(ev, 'cash') ?? '',
    publicKey: content.bchPubkey ?? tag(ev, 'bchpub') ?? '',
    npub: content.npub ?? ev.pubkey ?? '',
    assets,
    methods,
    fee: content.fee ?? (feeTag ? Number(feeTag) / 100 : 0.005),
    min: content.min ?? (tag(ev, 'min') ? Number(tag(ev, 'min')) : null),
    max: content.max ?? (tag(ev, 'max') ? Number(tag(ev, 'max')) : null),
    location: content.location ?? tag(ev, 'location') ?? null,
    hours: content.hours ?? tag(ev, 'hours') ?? null,
    name: content.name ?? tag(ev, 'name') ?? null,
    status: tag(ev, 'status') ?? 'active',
    anchor: tag(ev, 'anchor') ?? null,
    phone: content.phone ?? tag(ev, 'phone') ?? null,
    iban: content.iban ?? tag(ev, 'iban') ?? null,
    notes: content.notes ?? null,
    createdAt: ev.created_at ?? 0,
  };
}

export function fetchListings(relay = NOSTR_RELAY, timeoutMs = 8000) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(relay);
    const sub = `mercar-${Math.random().toString(36).slice(2)}`;
    const byId = new Map();
    let done = false;
    const finish = () => {
      if (done) return;
      done = true;
      try { ws.close(); } catch { /* ignore */ }
      resolve([...byId.values()].sort((a, b) => b.createdAt - a.createdAt));
    };
    const timer = setTimeout(finish, timeoutMs);
    ws.onopen = () => ws.send(JSON.stringify(['REQ', sub, { kinds: [KIND_LISTING], limit: 500 }]));
    ws.onmessage = (ev) => {
      let msg;
      try { msg = JSON.parse(ev.data); } catch { return; }
      if (msg[0] === 'EVENT' && msg[1] === sub) {
        const ad = parseListing(msg[2]);
        if (ad && !byId.has(ad.id)) byId.set(ad.id, ad);
      } else if (msg[0] === 'EOSE' && msg[1] === sub) {
        clearTimeout(timer);
        finish();
      }
    };
    ws.onerror = () => {
      clearTimeout(timer);
      if (byId.size) finish();
      else reject(new Error(`relay ${relay} unreachable`));
    };
  });
}

export function makeEvent({ privHex, pubHex, kind, tags, content }) {
  const createdAt = Math.floor(Date.now() / 1000);
  const serial = JSON.stringify([0, pubHex, createdAt, kind, tags, content]);
  const id = binToHex(sha256.hash(new TextEncoder().encode(serial)));
  const sig = secp256k1.signMessageHashSchnorr(hexToBin(privHex), hexToBin(id));
  if (typeof sig === 'string') throw new Error(sig);
  return { id, pubkey: pubHex, created_at: createdAt, kind, tags, content, sig: binToHex(sig) };
}

export function publishEvent(relay, event, timeoutMs = 10000) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(relay);
    let done = false;
    const finish = (fn) => { if (done) return; done = true; try { ws.close(); } catch { /* ignore */ } fn(); };
    const timer = setTimeout(() => finish(() => reject(new Error('relay publish timeout'))), timeoutMs);
    ws.onopen = () => ws.send(JSON.stringify(['EVENT', event]));
    ws.onmessage = (ev) => {
      let msg;
      try { msg = JSON.parse(ev.data); } catch { return; }
      if (msg[0] === 'OK' && msg[1] === event.id) {
        clearTimeout(timer);
        if (msg[2] === false) finish(() => reject(new Error(msg[3] || 'relay rejected the event')));
        else finish(() => resolve(event.id));
      }
    };
    ws.onerror = () => { clearTimeout(timer); finish(() => reject(new Error(`relay ${relay} unreachable`))); };
  });
}

const toB64 = (bytes) => btoa(String.fromCharCode(...bytes));
const fromB64 = (str) => Uint8Array.from(atob(str), (c) => c.charCodeAt(0));

async function sharedX(privHex, pubXHex) {
  const pub = '02' + String(pubXHex).replace(/^0x/, '').toLowerCase();
  const shared = noble.getSharedSecret(hexToBin(privHex), pub, true);
  return shared.slice(1);
}

export async function nip04Encrypt(privHex, pubXHex, plaintext) {
  const key = await crypto.subtle.importKey('raw', await sharedX(privHex, pubXHex), 'AES-CBC', false, ['encrypt']);
  const iv = crypto.getRandomValues(new Uint8Array(16));
  const ct = new Uint8Array(await crypto.subtle.encrypt({ name: 'AES-CBC', iv }, key, new TextEncoder().encode(plaintext)));
  return `${toB64(ct)}?iv=${toB64(iv)}`;
}

export async function nip04Decrypt(privHex, pubXHex, content) {
  const [ct, iv] = String(content).split('?iv=');
  if (!ct || !iv) throw new Error('not a NIP-04 message');
  const key = await crypto.subtle.importKey('raw', await sharedX(privHex, pubXHex), 'AES-CBC', false, ['decrypt']);
  const pt = await crypto.subtle.decrypt({ name: 'AES-CBC', iv: fromB64(iv) }, key, fromB64(ct));
  return new TextDecoder().decode(pt);
}

export function subscribeDMs(relay, { me, author, onEvent }) {
  const ws = new WebSocket(relay);
  const sub = `dm-${Math.random().toString(36).slice(2)}`;
  ws.onopen = () => ws.send(JSON.stringify(['REQ', sub, { kinds: [4], '#p': [me], authors: [author], limit: 50 }]));
  ws.onmessage = (ev) => {
    let msg;
    try { msg = JSON.parse(ev.data); } catch { return; }
    if (msg[0] === 'EVENT' && msg[1] === sub) onEvent(msg[2]);
  };
  return () => { try { ws.close(); } catch { /* ignore */ } };
}
