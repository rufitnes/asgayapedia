import QRCode from 'qrcode';
import { decodeCashAddress, decodeTransaction, generatePrivateKey, lockingBytecodeToCashAddress, hexToBin, binToHex, secp256k1, sha256, decodePrivateKeyWif } from '@bitauth/libauth';
import { WizardConnectSigner } from './wizardconnect.js';
import { encodeAnchorCommitment, buildGenesisAnchor, buildSelfSend } from './anchor.js';
import { electrumListUnspent, electrumGetHistory, electrumGetRawTx, scripthashFromLockingBytecode, broadcastTransaction } from './electrum.js';
import { fetchListings, NOSTR_RELAY, makeEvent, publishEvent, nip04Encrypt, nip04Decrypt, subscribeDMs } from './nostr.js';

const CATALOGUE_URL = 'catalogue/menu.json';
const ELECTRUM_WS = 'wss://chipnet.imaginary.cash:50004';
// The identity badge verifies an anchor on the network that actually holds it: the app mints on
// testnet3, the web canary on chipnet. New ads declare `network`; older ones are probed.
const ELECTRUM_WS_BY_NETWORK = {
  chipnet: 'wss://chipnet.imaginary.cash:50004',
  testnet3: 'wss://testnet.imaginary.cash:50004',
  mainnet: 'wss://bch.imaginary.cash:50004',
};
const VERIFY_NETWORKS = ['chipnet', 'testnet3', 'mainnet'];
// Informational market reference. NOT an offer and NOT the binding quote: the merchant's
// payment instructions carry the final amount. Mercar must not intermediate.
const FEED_URL = 'https://oracle.asgaya.org/quote';
let liveRate = null; // EUR per BCH from the public feed

async function fetchLiveRate() {
  try {
    const r = await fetch(FEED_URL, { cache: 'no-store' });
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    const j = await r.json();
    if (!(j.priceEur > 0)) throw new Error('bad quote');
    liveRate = j.priceEur;
  } catch (e) {
    liveRate = null;
  }
  return liveRate;
}

const state = { catalogue: null, cart: new Map(), adType: null, view: null, selectedAd: null };
let wc = null;
let wcLocking = null;
let wcAddress = null;

const eur = (cents) =>
  new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' }).format(cents / 100);
const sats = (cents, rate) => Math.round((cents / 100 / rate) * 100_000_000);
const $ = (id) => document.getElementById(id);
const cleanConcept = (s) => String(s || '').replace(/[^A-Za-z0-9]/g, '').slice(0, 40);

// The anchor page can target chipnet (the canary) or mainnet (the BLAZE artefact).
function selectedNetwork() {
  const el = document.getElementById('anchor-network');
  return el && el.value === 'mainnet' ? 'mainnet' : 'chipnet';
}
function anchorWs() { return ELECTRUM_WS_BY_NETWORK[selectedNetwork()]; }
function addressPrefix() { return selectedNetwork() === 'mainnet' ? 'bitcoincash' : 'bchtest'; }

function renderMerchant(c) {
  $('merchant-name').textContent = c.merchant.name;
  $('merchant-desc').textContent = c.merchant.description ?? '';
  $('merchant-address').textContent = c.merchant.address;
  if (c.merchant.network) {
    $('network-badge').textContent = `${c.merchant.network} demo`;
    $('network-badge').dataset.network = c.merchant.network;
  }
}

function renderMenu(c) {
  const menu = $('menu');
  menu.innerHTML = '';
  const byCategory = new Map();
  for (const item of c.items) {
    if (!byCategory.has(item.category)) byCategory.set(item.category, []);
    byCategory.get(item.category).push(item);
  }
  for (const [category, items] of byCategory) {
    const h = document.createElement('h2');
    h.className = 'category';
    h.textContent = category;
    menu.appendChild(h);
    const grid = document.createElement('div');
    grid.className = 'items';
    for (const item of items) {
      const card = document.createElement('article');
      card.className = 'item';
      card.innerHTML = `<h3></h3><p></p><div class="item-foot"><span class="price"></span><button class="primary" type="button">Add</button></div>`;
      card.querySelector('h3').textContent = item.name;
      card.querySelector('p').textContent = item.description ?? '';
      card.querySelector('.price').textContent = eur(item.price);
      card.querySelector('button').addEventListener('click', () => add(item.id));
      grid.appendChild(card);
    }
    menu.appendChild(grid);
  }
}

function add(itemId) {
  state.cart.set(itemId, (state.cart.get(itemId) ?? 0) + 1);
  renderCart();
}

function remove(itemId) {
  const qty = (state.cart.get(itemId) ?? 0) - 1;
  if (qty <= 0) state.cart.delete(itemId);
  else state.cart.set(itemId, qty);
  renderCart();
}

function cartTotalCents() {
  let total = 0;
  for (const [id, qty] of state.cart) {
    const item = state.catalogue.items.find((i) => i.id === id);
    if (item) total += item.price * qty;
  }
  return total;
}

function renderCart() {
  const box = $('cart-items');
  box.innerHTML = '';
  if (state.cart.size === 0) {
    box.innerHTML = '<p class="cart-empty">Your cart is empty.</p>';
  } else {
    for (const [id, qty] of state.cart) {
      const item = state.catalogue.items.find((i) => i.id === id);
      if (!item) continue;
      const row = document.createElement('div');
      row.className = 'cart-row';
      row.innerHTML = `<span></span><span class="qty"><button type="button" aria-label="Remove">−</button><span>${qty}</span><button type="button" aria-label="Add">+</button></span>`;
      row.querySelector('span').textContent = item.name;
      const [minus, plus] = row.querySelectorAll('button');
      minus.addEventListener('click', () => remove(id));
      plus.addEventListener('click', () => add(id));
      box.appendChild(row);
    }
  }
  const total = cartTotalCents();
  $('cart-total').textContent = eur(total);
  $('pay-btn').disabled = total === 0;
  $('pay-note').textContent = total === 0 ? 'Add something from the menu to start.' : 'Direct payment on chipnet (demo).';
}

async function openCheckout() {
  const total = cartTotalCents();
  if (total === 0) return;
  $('co-total').textContent = eur(total);
  const rate = await fetchLiveRate();
  $('co-rate').textContent = rate
    ? `1 BCH ≈ ${eur(Math.round(rate * 100))} (reference only)`
    : 'Reference rate unavailable';
  $('co-bch').textContent = rate ? `≈ ${sats(total, rate).toLocaleString('en-GB')} sats` : '—';
  $('co-address').textContent = state.catalogue.merchant.address;
  $('co-pay').disabled = false;
  $('co-pay').textContent = 'Pay with WizardConnect';
  $('checkout').showModal();
}

let cancelConnect = null;

function resetWallet() {
  wc = null;
  wcLocking = null;
  wcAddress = null;
  $('connect-btn').textContent = 'Connect wallet';
  const ab = $('anchor-connect'); if (ab) ab.textContent = 'Connect anchor wallet';
  const pubEl = document.getElementById('ad-pub'); if (pubEl) pubEl.textContent = '';
  const npubEl = document.getElementById('ad-npub'); if (npubEl) npubEl.textContent = '';
}

async function connectWallet() {
  if (wc) return;
  const dlg = $('wc-dialog');
  $('wc-qr').removeAttribute('src');
  $('wc-uri').textContent = 'Waiting for the relay…';
  wc = await WizardConnectSigner.connect({
    onUri: async (u, q) => {
      if (!dlg.open) dlg.showModal();
      $('wc-uri').textContent = u;
      try { $('wc-qr').src = await QRCode.toDataURL(q ?? u, { width: 280, margin: 1 }); } catch { /* optional */ }
    },
    broadcast: false,
    userPrompt: 'Pay with BCH',
    dappName: 'Mercar Asgaya',
    onCancel: (fn) => { cancelConnect = fn; },
  });
  cancelConnect = null;
  wc.onDisconnect(() => { console.log('[mercar] wallet disconnected'); resetWallet(); });
  wcLocking = await wc.getLockingBytecode();
  const r = lockingBytecodeToCashAddress({ prefix: addressPrefix(), bytecode: wcLocking, tokenSupport: false });
  wcAddress = typeof r === 'string' ? null : r.address;
  console.log('[mercar] wallet', wcAddress);
  $('connect-btn').textContent = wcAddress ? `Connected ${wcAddress.slice(0, 14)}…` : 'Connected';
  if (wcAddress) { const d = document.getElementById('buy-destination'); if (d && !d.value) d.value = wcAddress; }
  dlg.close();
}

async function payWithBch() {
  const total = cartTotalCents();
  if (total === 0) return;
  $('co-pay').disabled = true;
  try {
    if (!wc) await connectWallet();
    if (!liveRate) throw new Error('Reference rate unavailable — try again in a moment');
    const amountSats = sats(total, liveRate);
    const dec = decodeCashAddress(state.catalogue.merchant.address);
    if (typeof dec === 'string') throw new Error(dec);
    const merchantLocking = new Uint8Array([0x76, 0xa9, 0x14, ...dec.payload, 0x88, 0xac]);
    console.log('[mercar] amount', amountSats, 'sats; wallet', wcAddress);
    const sh = await scripthashFromLockingBytecode(wcLocking);
    const list = await electrumListUnspent(ELECTRUM_WS, sh);
    console.log('[mercar] utxos', list.map((u) => u.value));
    const fee = 1500n;
    const need = BigInt(amountSats) + fee;
    const utxo = list
      .map((u) => ({ ...u, value: BigInt(u.value) }))
      .filter((u) => u.value >= need)
      .sort((a, b) => (b.value > a.value ? 1 : b.value < a.value ? -1 : 0))[0];
    if (!utxo) {
      const have = list.reduce((a, u) => a + BigInt(u.value), 0n);
      throw new Error(`Need ${need} sats on chipnet; the wallet has ${have}.`);
    }
    // libauth expects the outpoint in display order and reverses on encode (see the spike).
    const txidBytes = hexToBin(utxo.tx_hash);
    const transaction = {
      version: 2,
      inputs: [{
        outpointTransactionHash: txidBytes,
        outpointIndex: utxo.tx_pos,
        sequenceNumber: 0xffffffff,
        unlockingBytecode: new Uint8Array(),
      }],
      outputs: [
        { lockingBytecode: merchantLocking, valueSatoshis: BigInt(amountSats) },
        { lockingBytecode: wcLocking, valueSatoshis: utxo.value - need },
      ],
      locktime: 0,
    };
    const sourceOutput = {
      outpointTransactionHash: txidBytes,
      outpointIndex: utxo.tx_pos,
      sequenceNumber: 0xffffffff,
      unlockingBytecode: new Uint8Array(),
      lockingBytecode: wcLocking,
      valueSatoshis: utxo.value,
    };
    console.log('[mercar] signing…');
    $('co-pay').textContent = 'Approve in your wallet…';
    $('pay-note').textContent = 'Open your wallet (Cashonize, Paytaca, …) and approve the payment.';
    const res = await wc.signTransaction({ transaction, sourceOutput, inputIndex: 0 });
    console.log('[mercar] signed, broadcast:', res.txid ?? 'self');
    const txid = res.txid ?? (await broadcastTransaction(ELECTRUM_WS, binToHex(res.signedTxBytes)));
    console.log('[mercar] txid', txid);
    $('co-pay').textContent = 'Paid';
    $('pay-note').textContent = `Paid ${amountSats} sats. txid ${txid}`;
    $('checkout').close();
    alert(`Payment sent.\ntxid: ${txid}`);
  } catch (e) {
    alert(`Payment failed: ${e.message}`);
    $('co-pay').disabled = false;
  }
}

let exchangeAds = [];

function populateFilters() {
  const assets = [...new Set(exchangeAds.flatMap((a) => a.assets))].sort();
  const methods = [...new Set(exchangeAds.flatMap((a) => a.methods))].sort();
  const asset = $('filter-asset');
  const method = $('filter-method');
  asset.innerHTML = '<option value="">All currencies</option>';
  for (const x of assets) asset.add(new Option(x, x));
  method.innerHTML = '<option value="">All payment methods</option>';
  for (const x of methods) method.add(new Option(x, x));
}

function renderExchange() {
  const list = $('exchange-list');
  list.innerHTML = '';
  const fa = $('filter-asset').value;
  const fm = $('filter-method').value;
  const sort = $('sort-by').value;
  const q = ($('search')?.value || '').trim().toLowerCase();
  let ads = exchangeAds.filter((a) => (!state.adType || a.adType === state.adType) && (!fa || a.assets.includes(fa)) && (!fm || a.methods.includes(fm)));
  if (q) {
    ads = ads.filter((a) =>
      [a.name, a.cashAccount, a.location, a.npub, ...a.assets, ...a.methods]
        .filter(Boolean).join(' ').toLowerCase().includes(q));
  }
  ads = sort === 'fee' ? [...ads].sort((a, b) => a.fee - b.fee) : [...ads].sort((a, b) => b.createdAt - a.createdAt);
  $('exchange-note').textContent = ads.length ? '' : 'No ads match the filters.';
  for (const ad of ads) {
    const card = document.createElement('article');
    card.className = 'ad';
    const isSeller = ad.adType === 'SELLER';
    const sold = isSeller ? 'BCH' : (ad.assets[0] ?? 'asset');
    const accepts = isSeller ? (ad.assets.join(', ') || '—') : 'BCH';
    card.innerHTML = `
      <div class="ad-top"><span class="ad-type ${ad.adType.toLowerCase()}"></span><span class="ad-name"></span><span class="ad-fee"></span></div>
      <div class="ad-row accepts"></div>
      <div class="ad-row methods"></div>
      <div class="ad-row meta"></div>
      <div class="ad-row locality"></div>
      <div class="ad-id"></div>`;
    card.querySelector('.ad-type').textContent = `Sells ${sold}`;
    card.querySelector('.ad-name').textContent = ad.name || ad.cashAccount || 'Merchant';
    card.querySelector('.ad-fee').textContent = `${(ad.fee * 100).toFixed(2)}%`;
    card.querySelector('.accepts').textContent = `for ${accepts}`;
    card.querySelector('.methods').textContent = ad.methods.join(' · ');
    card.querySelector('.meta').textContent = `${ad.min ?? '—'} – ${ad.max ?? '—'} ${ad.assets[0] ?? ''}`;
    card.querySelector('.locality').textContent = [ad.location, ad.hours].filter(Boolean).join(' · ');
    card.querySelector('.ad-id').textContent = `${ad.npub.slice(0, 18)}…`;
    card.title = `BCH pubkey: ${ad.publicKey}`;
    card.tabIndex = 0;
    card.setAttribute('role', 'button');
    card.addEventListener('click', () => openAd(ad));
    card.addEventListener('keydown', (e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); openAd(ad); } });
    list.appendChild(card);
  }
  $('featured-buy-bch').classList.toggle('active', state.adType === 'SELLER');
  $('exchange-title').textContent =
    state.adType === 'SELLER' ? 'Currency exchange · Buy BCH'
    : state.adType === 'BUYER' ? 'Currency exchange · Sell BCH'
    : 'Currency exchange';
}

function openAd(ad) {
  state.selectedAd = ad;
  showView('ad');
  const isSeller = ad.adType === 'SELLER';
  $('detail-type').textContent = `Sells ${isSeller ? 'BCH' : (ad.assets[0] ?? 'asset')}`;
  $('detail-type').className = `ad-type ${ad.adType.toLowerCase()}`;
  $('detail-name').textContent = ad.name || ad.cashAccount || 'Merchant';
  $('detail-sub').textContent = [ad.location, ad.hours].filter(Boolean).join(' · ');
  $('detail-fee').textContent = `${(ad.fee * 100).toFixed(2)}% fee`;
  $('detail-assets').textContent = isSeller ? (ad.assets.join(', ') || '—') : 'BCH';
  $('detail-methods').textContent = ad.methods.join(' · ') || '—';
  $('detail-limits').textContent = `${ad.min ?? '—'} – ${ad.max ?? '—'} ${ad.assets[0] ?? ''}`;
  $('detail-where').textContent = [ad.location, ad.hours].filter(Boolean).join(' · ') || '—';
  $('buy-title').textContent = isSeller ? 'Buy BCH' : 'Sell BCH';
  $('buy-get').textContent = isSeller ? 'Buy BCH' : 'Sell BCH';
  $('buy-hint').textContent = `Fee ${(ad.fee * 100).toFixed(2)}%; limits ${ad.min ?? '—'} – ${ad.max ?? '—'} ${ad.assets[0] ?? ''}.`;
  const amount = $('buy-amount');
  amount.min = ad.min ?? '';
  amount.max = ad.max ?? '';
  amount.value = ad.min ?? '';
  const method = $('buy-method');
  method.innerHTML = '';
  for (const m of ad.methods) method.add(new Option(m, m));
  $('buy-instructions').hidden = true;
  $('buy-instructions').innerHTML = '';
  $('identity-badge').textContent = 'Checking…';
  $('identity-badge').className = 'badge';
  $('identity-note').textContent = 'Verifying the on-chain anchor…';
  $('identity-pub').textContent = ad.publicKey ? `BCH pubkey: ${ad.publicKey}` : '';
  $('identity-npub').textContent = ad.npub ? `npub (hex): ${ad.npub}` : '';
  $('identity-anchor').textContent = ad.anchor ? `Anchor: ${ad.anchor}` : '';
  verifyAnchor(ad);
}

async function verifyAnchor(ad) {
  const badge = $('identity-badge');
  const note = $('identity-note');
  if (!ad.anchor) {
    badge.textContent = 'No anchor';
    badge.className = 'badge warn';
    note.textContent = 'This ad has no on-chain identity anchor.';
    return;
  }
  if (!ad.publicKey) {
    badge.textContent = 'No key';
    badge.className = 'badge warn';
    note.textContent = 'This ad has no BCH pubkey to compare with the anchor.';
    return;
  }
  const txid = ad.anchor.split(':')[0];
  const candidates = ad.network && ELECTRUM_WS_BY_NETWORK[ad.network]
    ? [ad.network, ...VERIFY_NETWORKS.filter((n) => n !== ad.network)]
    : VERIFY_NETWORKS;
  let lastError = null;
  for (const network of candidates) {
    try {
      const raw = await electrumGetRawTx(ELECTRUM_WS_BY_NETWORK[network], txid);
      if (typeof raw !== 'string') throw new Error('transaction not found');
      const tx = decodeTransaction(hexToBin(raw));
      if (typeof tx === 'string') throw new Error(tx);
      const pub = (tx.inputs?.[0]?.unlockingBytecode ?? new Uint8Array()).slice(-33);
      if (pub.length !== 33) throw new Error('no signer pubkey in the genesis tx');
      if (binToHex(pub) !== ad.publicKey.toLowerCase()) {
        badge.textContent = `Mismatch · ${network}`;
        badge.className = 'badge warn';
        note.textContent = `The anchor on ${network} was not signed by this ad’s key.`;
        $('identity-anchor').textContent = `Anchor: ${ad.anchor} (${network})`;
        return;
      }
      badge.textContent = `Verified on-chain · ${network}`;
      badge.className = 'badge ok';
      note.textContent = `The genesis anchor on ${network} was signed by this ad’s key: same-key identity.`;
      $('identity-anchor').textContent = `Anchor: ${ad.anchor} (${network})`;
      return;
    } catch (e) {
      lastError = e;
    }
  }
  badge.textContent = 'Unverified';
  badge.className = 'badge warn';
  note.textContent = `Could not verify the anchor on ${candidates.join(' / ')}: ${lastError?.message ?? 'not found'}`;
}

function showInstructions() {
  const ad = state.selectedAd;
  if (!ad) return;
  const amount = Number($('buy-amount').value);
  if (!Number.isFinite(amount) || amount <= 0) { alert('Enter an amount.'); return; }
  if (ad.min != null && amount < ad.min) { alert(`Minimum is ${ad.min} ${ad.assets[0] ?? ''}.`); return; }
  if (ad.max != null && amount > ad.max) { alert(`Maximum is ${ad.max} ${ad.assets[0] ?? ''}.`); return; }
  const method = $('buy-method').value;
  const asset = $('buy-currency')?.value || ad.assets[0] || 'EUR';
  const isSeller = ad.adType === 'SELLER';
  const box = $('buy-instructions');
  box.innerHTML = '';
  const add = (text) => { const p = document.createElement('p'); p.textContent = text; box.appendChild(p); };
  add(`${isSeller ? 'Buy BCH' : 'Sell BCH'}: ${amount.toLocaleString('en-GB')} ${asset}.`);
  if (/cash|in person/i.test(method)) {
    add(`Visit ${ad.name || 'the merchant'} at ${ad.location || 'the merchant’s location'}${ad.hours ? ` (${ad.hours})` : ''} and complete the exchange in person.`);
  } else if (/bizum/i.test(method)) {
    add(`Send ${amount.toLocaleString('en-GB')} ${asset} by Bizum to ${ad.cashAccount || ad.phone || 'the merchant’s phone'}.`);
    add(`Concept: Asgaya ${ad.name || ''}. Name: ${ad.name || 'the merchant'}.`);
  } else if (/sepa|transfer/i.test(method)) {
    add(`Send ${amount.toLocaleString('en-GB')} ${asset} by SEPA${ad.iban ? ` to ${ad.iban}` : ' to the merchant’s account'}.`);
    add(`Concept: Asgaya ${ad.name || ''}. Name: ${ad.name || 'the merchant'}.`);
  } else {
    add(`Contact the merchant to complete the payment (${method}).`);
  }
  if (ad.phone) add(`Contact: ${ad.phone}`);
  if (ad.notes) add(ad.notes);
  const dest = $('buy-destination')?.value.trim();
  if (isSeller) add(dest ? `The merchant sends the BCH to: ${dest}` : 'Enter the BCH address where you want to receive the funds.');
  else add('The merchant pays you the fiat once your BCH is received.');
  box.hidden = false;
}

const CATEGORY_GROUPS = [
  { title: 'Currency exchange', items: [
    { label: 'All ads', kind: 'all' },
    { label: 'Buy BCH', kind: 'buyBch' },
    { label: 'Sell BCH', kind: 'sellBch' },
    { label: 'EUR', kind: 'asset', value: 'EUR' },
    { label: 'USD', kind: 'asset', value: 'USD' },
    { label: 'NGN', kind: 'asset', value: 'NGN' },
    { label: 'Cash in person', kind: 'method', value: 'Cash in person' },
    { label: 'Card in person', kind: 'method', value: 'Card in person' },
    { label: 'Bizum', kind: 'method', value: 'Bizum' },
  ] },
  { title: 'Restaurants', items: [{ label: 'View restaurants', kind: 'restaurants' }] },
  { title: 'Goods', soon: true, items: ['Technology', 'Home', 'Fashion', 'Motor', 'Sport'].map((l) => ({ label: l })) },
  { title: 'Services', soon: true, items: ['Home', 'Professional', 'Classes'].map((l) => ({ label: l })) },
];

function countFor(it) {
  if (it.kind === 'all') return exchangeAds.length;
  if (it.kind === 'buyBch') return exchangeAds.filter((a) => a.adType === 'SELLER').length;
  if (it.kind === 'sellBch') return exchangeAds.filter((a) => a.adType === 'BUYER').length;
  if (it.kind === 'asset') return exchangeAds.filter((a) => a.assets.includes(it.value)).length;
  if (it.kind === 'method') return exchangeAds.filter((a) => a.methods.includes(it.value)).length;
  if (it.kind === 'restaurants') return 1;
  return null;
}

function openCategories() {
  const body = $('categories-body');
  body.innerHTML = '';
  for (const g of CATEGORY_GROUPS) {
    const group = document.createElement('div');
    group.className = 'cat-group' + (g.soon ? ' soon' : '');
    const h = document.createElement('h3');
    h.textContent = g.title;
    group.appendChild(h);
    const items = document.createElement('div');
    items.className = 'cat-items';
    for (const it of g.items) {
      const b = document.createElement('button');
      b.type = 'button';
      b.className = 'cat-item';
      b.textContent = it.label;
      if (g.soon) {
        b.disabled = true;
        const s = document.createElement('span');
        s.className = 'count';
        s.textContent = 'soon';
        b.appendChild(s);
      } else {
        const c = countFor(it);
        if (c !== null) {
          const s = document.createElement('span');
          s.className = 'count';
          s.textContent = c;
          b.appendChild(s);
        }
        b.addEventListener('click', () => { applyCategory(it); $('categories-dialog').close(); });
      }
      items.appendChild(b);
    }
    group.appendChild(items);
    body.appendChild(group);
  }
  $('categories-dialog').showModal();
}

function showView(name) {
  state.view = name;
  document.getElementById('exchange').hidden = name !== 'exchange';
  document.getElementById('restaurants-view').hidden = name !== 'restaurants';
  document.getElementById('menu-view').hidden = name !== 'menu';
  document.getElementById('anchor-view').hidden = name !== 'anchor';
  document.getElementById('ad-view').hidden = name !== 'ad';
  document.getElementById('pay-view').hidden = name !== 'pay';
  const target = name === 'exchange' ? 'exchange' : name === 'restaurants' ? 'restaurants-view' : name === 'menu' ? 'menu-view' : name === 'anchor' ? 'anchor-view' : name === 'ad' ? 'ad-view' : name === 'pay' ? 'pay-view' : 'categories';
  const el = document.getElementById(target);
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function applyCategory(it) {
  if (it.kind === 'restaurants') {
    document.getElementById('categories-dialog').close();
    showView('restaurants');
    return;
  }
  state.adType = it.kind === 'buyBch' ? 'SELLER' : it.kind === 'sellBch' ? 'BUYER' : null;
  if (it.kind === 'asset') {
    document.getElementById('filter-asset').value = it.value;
    document.getElementById('filter-method').value = '';
  } else if (it.kind === 'method') {
    document.getElementById('filter-method').value = it.value;
    document.getElementById('filter-asset').value = '';
  } else {
    document.getElementById('filter-asset').value = '';
    document.getElementById('filter-method').value = '';
  }
  renderExchange();
  showView('exchange');
}

let anchorUtxos = [];
let identityAnchor = null;

const METHOD_KEY = {
  'Cash in person': 'cash',
  'Card in person': 'card',
  'Bizum': 'bizum',
  'SEPA': 'other',
  'Pago Móvil': 'other',
};

const TEST_NOSTR = {
  '03d6e1b33780e34dff1c6dc705c626deed385e827e762abfc596764be0ca3e4f45': '33'.repeat(32),
  '032774f66536468a06a244568724bf1f8762682be1573ff9910667c0fea1226ece': '11'.repeat(32),
  '03886b4f4100f9e348caed5155474f0a1a96db3e2706dc59e15260ed3d2b35e148': '22'.repeat(32),
};

function slug(value) {
  return String(value).toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '');
}

const ANCHOR_KEY = 'mercar-identity-anchor';
function loadSavedAnchor() { try { return JSON.parse(localStorage.getItem(ANCHOR_KEY) || 'null'); } catch { return null; } }
function saveSavedAnchor(a) { try { localStorage.setItem(ANCHOR_KEY, JSON.stringify(a)); } catch { /* ignore */ } }

function keyFromPub(pubHex) {
  const p = pubHex.toLowerCase();
  const seeded = TEST_NOSTR[p];
  const nostrPrivHex = seeded || binToHex(sha256.hash(new TextEncoder().encode('asgaya-web-test-nostr:' + p)));
  const nostrPub = secp256k1.derivePublicKeyCompressed(hexToBin(nostrPrivHex));
  return { bchPubHex: p, nostrPrivHex, npubHex: binToHex(nostrPub.slice(1)), seeded: !!seeded };
}

function currentCommitmentHex() {
  const role = document.getElementById('ad-role').value;
  const asset = document.querySelector('input[name=ad-asset]:checked')?.value;
  const method = document.querySelector('input[name=ad-method]:checked')?.value;
  const fee = Number(document.getElementById('ad-fee').value);
  if (!asset || !method || !Number.isFinite(fee)) return null;
  return binToHex(encodeAnchorCommitment({
    role,
    asset,
    method: METHOD_KEY[method] ?? 'other',
    feeBps: Math.round(fee * 100),
  }));
}

/**
 * Reuse is per on-chain tuple (`10 §2.1`): an anchor commits `(seller, asset, method, fee)`, so
 * ads that share that tuple share the anchor; a different tuple needs a new anchor. Matching the
 * full commitment (not just the `0x01` version byte) is what makes the reuse honest.
 */
function matchAnchor(commitmentHex) {
  const want = (commitmentHex || '').toLowerCase();
  if (!want) return null;
  const token = anchorUtxos.find((u) => u.tx_pos === 0 && u.token_data?.nft
    && String(u.token_data.nft.commitment ?? '').toLowerCase() === want);
  if (token) return token.tx_hash;
  const saved = loadSavedAnchor();
  if (saved?.txid && (saved.network || 'chipnet') === selectedNetwork()
    && String(saved.commitmentHex ?? '').toLowerCase() === want) return saved.txid;
  return null;
}

async function findIdentityAnchor(commitmentHex) {
  const sync = matchAnchor(commitmentHex);
  if (sync) return sync;
  const want = (commitmentHex || '').toLowerCase();
  if (!want || !wcLocking) return null;
  const sh = await scripthashFromLockingBytecode(wcLocking);
  const history = await electrumGetHistory(anchorWs(), sh);
  for (const h of history) {
    try {
      const raw = await electrumGetRawTx(anchorWs(), h.tx_hash);
      if (typeof raw !== 'string') continue;
      const tx = decodeTransaction(hexToBin(raw));
      if (typeof tx === 'string') continue;
      const c = tx.outputs?.[0]?.token?.nft?.commitment;
      if (c && binToHex(c).toLowerCase() === want) return h.tx_hash;
    } catch { /* skip */ }
  }
  return null;
}

function refreshPublishButton(keepExisting = false) {
  const btn = document.getElementById('anchor-mint');
  const note = document.getElementById('anchor-note');
  if (!btn) return;
  if (!wc) { btn.disabled = true; note.textContent = 'Connect a wallet to mint the anchor.'; return; }
  const want = currentCommitmentHex();
  if (!keepExisting) identityAnchor = matchAnchor(want);
  const existing = want ? identityAnchor : null;
  const hasV0 = anchorUtxos.some((u) => u.tx_pos === 0 && BigInt(u.value) >= 3000n);
  btn.disabled = !(want && (existing || hasV0));
  document.getElementById('anchor-selfsend').disabled = anchorUtxos.length === 0;
  if (!want) note.textContent = 'Pick one currency and one payment method to compute the anchor tuple.';
  else if (existing) note.textContent = `Ready: reusing the anchor ${existing} (same seller/asset/method/fee).`;
  else if (hasV0) note.textContent = 'Ready: a vout-0 UTXO is available; this tuple gets a new anchor.';
  else if (anchorUtxos.length) note.textContent = 'No vout-0 UTXO: use “Prepare a vout-0 UTXO”.';
  else note.textContent = `No ${selectedNetwork()} UTXOs: fund the wallet first.`;
}

async function anchorLoad() {
  if (!wc) { document.getElementById('anchor-note').textContent = 'Connect a wallet to mint the anchor.'; return; }
  document.getElementById('anchor-address').textContent = wcAddress ?? '';
  const sh = await scripthashFromLockingBytecode(wcLocking);
  anchorUtxos = await electrumListUnspent(anchorWs(), sh);
  try { identityAnchor = await findIdentityAnchor(currentCommitmentHex()); } catch { identityAnchor = null; }
  refreshPublishButton(true);
}

function openAnchor() {
  showView('anchor');
  { const ab = $('anchor-connect'); if (ab) ab.textContent = wc ? 'Disconnect wallet' : 'Connect anchor wallet'; }
  document.getElementById('ad-pub').textContent = '';
  document.getElementById('ad-npub').textContent = '';
  if (!wc) {
    document.getElementById('anchor-note').textContent = 'Connect your merchant wallet (WizardConnect).';
    return;
  }
  wc.getPubkey().then((pub) => {
    const k = keyFromPub(binToHex(pub));
    document.getElementById('ad-pub').textContent = `BCH pubkey: ${k.bchPubHex}`;
    document.getElementById('ad-npub').textContent = `npub (hex): ${k.npubHex} (${k.seeded ? 'app merchant identity' : 'web demo identity'})`;
    return anchorLoad();
  }).catch((e) => { document.getElementById('anchor-note').textContent = e.message; });
}

async function publishAd() {
  const btn = document.getElementById('anchor-mint');
  btn.disabled = true;
  try {
    if (!wc) await connectWallet();
    const walletPub = await wc.getPubkey();
    const k = keyFromPub(binToHex(walletPub));
    const role = document.getElementById('ad-role').value;
    const assets = [...document.querySelectorAll('input[name=ad-asset]:checked')].map((x) => x.value);
    const methods = [...document.querySelectorAll('input[name=ad-method]:checked')].map((x) => x.value);
    if (assets.length === 0) throw new Error('Pick at least one currency.');
    if (methods.length === 0) throw new Error('Pick at least one payment method.');
    const fee = Number(document.getElementById('ad-fee').value);
    const min = Number(document.getElementById('ad-min').value);
    const max = Number(document.getElementById('ad-max').value);
    const name = document.getElementById('ad-name').value;
    const cash = '';
    const location = document.getElementById('ad-location').value;
    const hours = document.getElementById('ad-hours').value;
    await anchorLoad();
    const commitment = encodeAnchorCommitment({
      role,
      asset: assets[0],
      method: METHOD_KEY[methods[0]] ?? 'other',
      feeBps: Math.round(fee * 100),
    });
    const commitmentHex = binToHex(commitment);
    let anchorTx = identityAnchor ?? undefined;
    if (anchorTx) {
      document.getElementById('anchor-note').textContent = `Reusing the identity anchor ${anchorTx}…`;
    } else {
      const v0 = anchorUtxos
        .filter((u) => u.tx_pos === 0 && BigInt(u.value) >= 3000n)
        .sort((a, b) => Number(b.value) - Number(a.value))[0];
      if (!v0) throw new Error('No vout-0 UTXO; prepare one first.');
      const built = buildGenesisAnchor(wcLocking, { txid: v0.tx_hash, vout: v0.tx_pos, satoshis: BigInt(v0.value) }, commitment);
      document.getElementById('anchor-note').textContent = 'Approve the anchor in your wallet…';
      const res = await wc.signTransaction({ transaction: built.transaction, sourceOutput: built.sourceOutput, inputIndex: 0, userPrompt: 'Mint your identity anchor' });
      anchorTx = res.txid ?? (await broadcastTransaction(anchorWs(), binToHex(res.signedTxBytes)));
      saveSavedAnchor({ txid: anchorTx, commitmentHex, network: selectedNetwork() });
      identityAnchor = anchorTx;
    }
    const adId = `${k.bchPubHex.slice(0, 16)}-${role === 'seller' ? 'SELLER' : 'BUYER'}-${assets.join('')}-${slug(methods[0])}`;
    const tags = [
      ['d', adId],
      ['t', 'asgaya-board'],
      ['t', role === 'seller' ? 'asgaya-seller' : 'asgaya-buyer'],
      ['bchpub', k.bchPubHex],
      ...assets.map((a) => ['t', `asset:${a.toLowerCase()}`]),
      ...methods.map((m) => ['t', `pay:${slug(m)}`]),
      ['fee', String(fee)],
      ['buffer', '93'],
      ['location', location],
      ['min', String(min)],
      ['max', String(max)],
      ['hours', hours],
      ['name', name],
      ['status', 'active'],
      ['anchor', `${anchorTx}:0`],
      ['network', selectedNetwork()],
    ];
    const content = JSON.stringify({
      adType: role === 'seller' ? 'SELLER' : 'BUYER',
      assets,
      paymentMethods: methods,
      fee: fee / 100,
      minPricePercent: 93,
      cashAccount: cash,
      bchPubkey: k.bchPubHex,
      npub: k.npubHex,
      location,
      min,
      max,
      hours,
      name,
      notes: document.getElementById('ad-notes').value,
    });
    const event = makeEvent({ privHex: k.nostrPrivHex, pubHex: k.npubHex, kind: 30402, tags, content });
    document.getElementById('anchor-note').textContent = 'Publishing the listing…';
    await publishEvent(NOSTR_RELAY, event);
    document.getElementById('anchor-result').textContent =
      `Anchor tx: ${anchorTx}\nListing: ${event.id}\nIdentity (npub hex): ${k.npubHex}`;
    document.getElementById('anchor-note').textContent = 'Published. The ad is on the board with its identity anchor.';
    exchangeAds = await fetchListings();
    populateFilters();
    renderExchange();
    showView('exchange');
  } catch (e) {
    document.getElementById('anchor-note').textContent = `Publish failed: ${e.message}`;
    btn.disabled = false;
  }
}

async function anchorSelfSend() {
  try {
    if (!wc) await connectWallet();
    const u = anchorUtxos.slice().sort((a, b) => Number(b.value) - Number(a.value))[0];
    if (!u) throw new Error('No UTXOs');
    const built = buildSelfSend(wcLocking, { txid: u.tx_hash, vout: u.tx_pos, satoshis: BigInt(u.value) });
    document.getElementById('anchor-note').textContent = 'Approve in your wallet…';
    const res = await wc.signTransaction({ transaction: built.transaction, sourceOutput: built.sourceOutput, inputIndex: 0, userPrompt: 'Prepare a vout-0 UTXO' });
    const txid = res.txid ?? (await broadcastTransaction(anchorWs(), binToHex(res.signedTxBytes)));
    await anchorLoad();
    document.getElementById('anchor-note').textContent = `Self-send sent (${txid}). If the vout-0 is not ready yet, press Refresh in a moment.`;
  } catch (e) {
    const msg = e.message || '';
    if (/already in block chain|already known|already-in-mempool|already in the mempool/i.test(msg)) {
      document.getElementById('anchor-note').textContent = 'The self-send is already on-chain. Refreshing the UTXOs…';
      await anchorLoad().catch(() => {});
    } else {
      document.getElementById('anchor-note').textContent = `Self-send failed: ${msg}`;
    }
  }
}

async function openPay(ad) {
  const amount = Number($('buy-amount').value);
  const method = $('buy-method').value;
  if (!Number.isFinite(amount) || amount <= 0) { alert('Enter an amount.'); return; }
  if (ad.min != null && amount < ad.min) { alert(`Minimum is ${ad.min} ${ad.assets[0] ?? ''}.`); return; }
  if (ad.max != null && amount > ad.max) { alert(`Maximum is ${ad.max} ${ad.assets[0] ?? ''}.`); return; }
  const dest = ($('buy-destination')?.value || '').trim();
  if (!dest) { alert('Enter the BCH address where you want to receive the funds.'); return; }
  const params = new URLSearchParams({
    ad: ad.id, seller: ad.npub, name: ad.name || '', asset: ad.assets[0] || 'EUR',
    amount: String(amount), method, anchor: ad.anchor || '',
    dest,
  });
  window.open(`${location.pathname}#pay?${params}`, '_blank', 'noopener');
}

function chatAdd(kind, text, id) {
  if (id) {
    const prev = $('pay-chat').querySelector(`.msg[data-id="${id}"]`);
    if (prev) { prev.textContent = text; prev.scrollIntoView({ block: 'nearest' }); return; }
  }
  const div = document.createElement('div');
  div.className = `msg ${kind}`;
  div.textContent = text;
  if (id) div.dataset.id = id;
  $('pay-chat').appendChild(div);
  div.scrollIntoView({ block: 'nearest' });
}

/** Render the app's `[PAY_INSTRUCTIONS]` reply like the app's own instructions dialog. */
function formatInstructions(message) {
  const text = String(message);
  if (text.includes('[PAY_RECEIVED]')) {
    const kv = {};
    for (const line of text.split('\n')) {
      const i = line.indexOf('=');
      if (i > 0) kv[line.slice(0, i).trim()] = line.slice(i + 1).trim();
    }
    return `✅ Payment received — ${kv.amountBch || '?'} BCH · tx ${kv.txid || '?'}`;
  }
  if (!text.includes('[PAY_INSTRUCTIONS]')) return text;
  const kv = {};
  for (const line of text.split('\n')) {
    const i = line.indexOf('=');
    if (i > 0) kv[line.slice(0, i).trim()] = line.slice(i + 1).trim();
  }
  const methods = { cash: 'Cash in person', card: 'Card in person', bizum: 'Bizum', sepa: 'SEPA', pago_movil: 'Pago Móvil' };
  const labels = { name: 'Name', location: 'Where', hours: 'Hours', phone: 'Phone', iban: 'IBAN', notes: 'Notes', concept: 'Concept' };
  const skip = new Set(['requestId', 'adId', 'method', 'timestamp', 'amount', 'asset']);
  if (kv.busy) {
    return `The merchant has a payment in progress${kv.activeConcept ? ` (${kv.activeConcept})` : ''}. Try again in a few minutes.`;
  }
  if (kv.method === 'bizum') {
    const amount = [kv.amount, kv.asset].filter(Boolean).join(' ');
    const rate = kv.rate ? `The merchant's BCH price is ${kv.rate} ${kv.asset || 'EUR'}/BCH.` : '';
    return `To finish your purchase, send ${amount ? amount + ' ' : ''}by Bizum to ${kv.phone || '?'} with concept ${kv.concept || '?'}. ${rate} The final price is set when the payment is received.`;
  }
  const out = [`💳 Method: ${methods[kv.method] || kv.method || '?'}`];
  for (const [k, v] of Object.entries(kv)) if (!skip.has(k) && v) out.push(`• ${labels[k] || k}: ${v}`);
  return out.join('\n');
}

async function startPay(params) {
  showView('pay');
  const seller = String(params.get('seller') || '').toLowerCase();
  const name = params.get('name') || 'the merchant';
  const asset = params.get('asset') || 'EUR';
  const amount = params.get('amount') || '';
  const method = params.get('method') || '';
  const destination = (params.get('dest') || wcAddress || '').trim();
  const anchor = params.get('anchor') || '';
  const privHex = binToHex(generatePrivateKey());
  const pub = secp256k1.derivePublicKeyCompressed(hexToBin(privHex));
  const npubHex = binToHex(pub.slice(1));
  $('pay-name').textContent = name;
  $('pay-sub').textContent = `${amount} ${asset} · ${method}`;
  $('pay-ids').textContent = `You (session): ${npubHex.slice(0, 16)}… · Merchant: ${seller.slice(0, 16)}…${anchor ? ` · anchor ${anchor}` : ''}`;
  $('pay-status').textContent = 'Waiting for the merchant…';
  $('pay-status').className = 'badge warn';
  chatAdd('sys', 'A private, end-to-end encrypted channel with the merchant. Mercar only showed you the ad; it cannot read or store these messages.');
  if (!seller) { chatAdd('sys', 'This ad has no merchant npub, so the channel cannot be opened.'); return; }
  const requestId = (crypto.randomUUID && crypto.randomUUID()) || String(Date.now());
  const request = [
    '[PAY_INSTRUCTIONS_REQUEST]',
    `adId=${params.get('ad') || ''}`,
    `amount=${amount}`,
    `currency=${asset}`,
    `buyerAddress=${destination}`,
    `requestId=${requestId}`,
    `timestamp=${Date.now()}`,
    '[/PAY_INSTRUCTIONS_REQUEST]',
  ].join('\n');
  chatAdd('sys', `Requested payment instructions: ${amount} ${asset} (${method}).`);
  try {
    const content = await nip04Encrypt(privHex, seller, request);
    const event = makeEvent({ privHex, pubHex: npubHex, kind: 4, tags: [['p', seller]], content });
    await publishEvent(NOSTR_RELAY, event);
  } catch (e) { chatAdd('sys', `Could not send the request: ${e.message}`); }
  const timer = setTimeout(() => {
    if ($('pay-status').textContent.startsWith('Waiting')) {
      $('pay-status').textContent = 'Merchant not detected';
      chatAdd('sys', 'The merchant’s app has not replied. It must be open: the instructions come from their device (liveness).');
    }
  }, 45000);
  subscribeDMs(NOSTR_RELAY, { me: npubHex, author: seller, onEvent: async (ev) => {
    console.log('[pay] dm', String(ev.id || '').slice(0, 10), 'from', String(ev.pubkey || '').slice(0, 12), 'expected seller', String(seller || '').slice(0, 12));
    if (ev.pubkey !== seller) return;
    try {
      const pt = await nip04Decrypt(privHex, seller, ev.content);
      const reqId = (pt.match(/requestId=([^\n\]]+)/) || [])[1];
      chatAdd('them', formatInstructions(pt), reqId ? `pi-${reqId}` : undefined);
      $('pay-status').textContent = 'Merchant replied (live)';
      $('pay-status').className = 'badge ok';
      clearTimeout(timer);
    } catch (e) { chatAdd('sys', `Could not decrypt a message: ${e.message}`); }
  } });
  $('pay-form').onsubmit = async (e) => {
    e.preventDefault();
    const text = $('pay-input').value.trim();
    if (!text) return;
    $('pay-input').value = '';
    chatAdd('me', text);
    try {
      const content = await nip04Encrypt(privHex, seller, text);
      const event = makeEvent({ privHex, pubHex: npubHex, kind: 4, tags: [['p', seller]], content });
      await publishEvent(NOSTR_RELAY, event);
    } catch (err) { chatAdd('sys', `Could not send: ${err.message}`); }
  };
}

async function main() {
  const payParams = location.hash.startsWith('#pay?') ? new URLSearchParams(location.hash.slice(5)) : null;
  if (payParams) { startPay(payParams).catch((e) => { $('pay-status').textContent = `Chat error: ${e.message}`; }); return; }
  try {
    const res = await fetch(CATALOGUE_URL, { cache: 'no-store' });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    state.catalogue = await res.json();
    renderMerchant(state.catalogue);
    renderMenu(state.catalogue);
    renderCart();
  } catch (err) {
    $('menu').innerHTML = `<p class="muted">Could not load the catalogue (${err.message}).</p>`;
  }
  $('pay-btn').addEventListener('click', openCheckout);
  $('co-close').addEventListener('click', () => $('checkout').close());
  $('co-pay').addEventListener('click', payWithBch);
  $('connect-btn').addEventListener('click', () => {
    if (wc) {
      if (confirm('Disconnect the wallet?')) { wc.disconnect(); resetWallet(); }
      return;
    }
    connectWallet().catch((e) => { if (e.message !== 'Cancelled') alert(e.message); });
  });
  $('wc-cancel').addEventListener('click', () => {
    if (cancelConnect) { cancelConnect(); cancelConnect = null; }
    $('wc-dialog').close();
  });
  $('search').addEventListener('input', () => { if ($('search').value.trim()) showView('exchange'); renderExchange(); });
  $('browse-board').addEventListener('click', () => showView(null));
  $('merchant-btn').addEventListener('click', openAnchor);
  $('hero-publish').addEventListener('click', openAnchor);
  $('back-anchor').addEventListener('click', () => showView(null));
  $('anchor-connect').addEventListener('click', () => {
    if (wc) { wc.disconnect(); resetWallet(); openAnchor(); return; }
    connectWallet().then(() => openAnchor()).catch((e) => { if (e.message !== 'Cancelled') document.getElementById('anchor-note').textContent = e.message; });
  });
  $('anchor-mint').addEventListener('click', publishAd);
  $('anchor-selfsend').addEventListener('click', anchorSelfSend);
  $('anchor-refresh').addEventListener('click', () => anchorLoad().catch((e) => { document.getElementById('anchor-note').textContent = e.message; }));
  const anchorForm = document.getElementById('anchor-view');
  if (anchorForm) {
    anchorForm.addEventListener('change', refreshPublishButton);
    anchorForm.addEventListener('input', refreshPublishButton);
  }
  const anchorNetwork = document.getElementById('anchor-network');
  if (anchorNetwork) {
    anchorNetwork.addEventListener('change', () => {
      anchorUtxos = []; identityAnchor = null;
      anchorLoad().catch((e) => { document.getElementById('anchor-note').textContent = e.message; });
    });
  }
  $('hero-buy-bch').addEventListener('click', () => applyCategory({ kind: 'buyBch' }));
  $('featured-exchange').addEventListener('click', () => { state.adType = null; $('filter-asset').value = ''; $('filter-method').value = ''; renderExchange(); showView('exchange'); });
  $('featured-restaurants').addEventListener('click', () => showView('restaurants'));
  $('casa-elena').addEventListener('click', () => showView('menu'));
  $('back-exchange').addEventListener('click', () => showView(null));
  $('back-restaurants').addEventListener('click', () => showView(null));
  $('back-menu').addEventListener('click', () => showView('restaurants'));
  $('back-ad').addEventListener('click', () => showView('exchange'));
  $('buy-get').addEventListener('click', () => { if (state.selectedAd) openPay(state.selectedAd); });
  $('buy-amount').addEventListener('input', () => { $('buy-instructions').hidden = true; });
  $('buy-method').addEventListener('change', () => { $('buy-instructions').hidden = true; });
  $('all-categories').addEventListener('click', openCategories);
  $('categories-close').addEventListener('click', () => $('categories-dialog').close());
  $('featured-buy-bch').addEventListener('click', () => applyCategory({ kind: state.adType === 'SELLER' ? 'all' : 'buyBch' }));
  $('filter-asset').addEventListener('change', renderExchange);
  $('filter-method').addEventListener('change', renderExchange);
  $('sort-by').addEventListener('change', renderExchange);
  fetchListings()
    .then((ads) => { exchangeAds = ads; populateFilters(); renderExchange(); })
    .catch((e) => { $('exchange-note').textContent = `Could not load ads (${e.message}). LAN relay: ${NOSTR_RELAY}`; });
}

main();
