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
