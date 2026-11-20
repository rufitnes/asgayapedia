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

const state = { catalogue: null, cart: new Map(), adType: null, view: null, selectedAd: null, category: 'exchange', subcategory: null, payAddress: null, payAmount: null };
let wc = null;
let wcPubHex = null;
let wcReceiveAddress = null;
let wcLocking = null;
let wcAddress = null;

const eur = (cents) =>
  new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' }).format(cents / 100);
const sats = (cents, rate) => Math.round((cents / 100 / rate) * 100_000_000);
const $ = (id) => document.getElementById(id);
const cleanConcept = (s) => String(s || '').replace(/[^A-Za-z0-9]/g, '').slice(0, 40);

// The anchor page can target chipnet (the canary) or mainnet (the BLAZE artefact).
function selectedNetwork() {
  return 'mainnet';
}
function anchorWs() { return ELECTRUM_WS_BY_NETWORK[selectedNetwork()]; }
function addressPrefix() { return selectedNetwork() === 'mainnet' ? 'bitcoincash' : 'bchtest'; }

function renderMerchant(c) {
  $('merchant-name').textContent = c.merchant.name;
  $('merchant-desc').textContent = c.merchant.description ?? '';
  $('merchant-address').textContent = c.merchant.address;
  if (c.merchant.network) {
    $('network-badge').textContent = c.merchant.network;
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
  const total = state.payAmount ?? cartTotalCents();
  if (total === 0) return;
  $('co-total').textContent = eur(Math.round(total * 100));
  const rate = await fetchLiveRate();
  $('co-rate').textContent = rate
    ? `1 BCH ≈ ${eur(Math.round(rate * 100))} (reference only)`
    : 'Reference rate unavailable';
  $('co-bch').textContent = rate ? `≈ ${sats(Math.round(total * 100), rate).toLocaleString('en-GB')} sats` : '—';
  $('co-address').textContent = state.payAddress || state.catalogue.merchant.address;
  $('co-pay').disabled = false;
  $('co-pay').textContent = 'Pay with WizardConnect';
  $('checkout').showModal();
}

let cancelConnect = null;

function resetWallet() {
  wcPubHex = null;
  wcReceiveAddress = null;
  updateOwnAd();
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
  try { wcPubHex = binToHex(await wc.getPubkey()); } catch { wcPubHex = null; }
  updateOwnAd();
  wcLocking = await wc.getLockingBytecode();
  const r = lockingBytecodeToCashAddress({ prefix: addressPrefix(), bytecode: wcLocking, tokenSupport: false });
  wcAddress = typeof r === 'string' ? null : r.address;
  console.log('[mercar] wallet', wcAddress);
  wcReceiveAddress = wcAddress;
  $('connect-btn').textContent = wcAddress ? `Connected ${wcAddress.slice(0, 14)}…` : 'Connected';
  if (wcReceiveAddress) { const d = document.getElementById('buy-destination'); if (d && !d.value) d.value = wcReceiveAddress; }
  dlg.close();
}

async function payWithBch() {
  const total = state.payAmount ?? cartTotalCents() / 100;
  if (!total) return;
  $('co-pay').disabled = true;
  try {
    if (!wc) await connectWallet();
    if (!liveRate) throw new Error('Reference rate unavailable — try again in a moment');
    const amountSats = sats(Math.round(total * 100), liveRate);
    const dec = decodeCashAddress(state.payAddress || state.catalogue.merchant.address);
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

function buildCard(ad) {
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
  if (ad.anchor) {
    const chip = document.createElement('span');
    chip.className = 'ad-anchored';
    chip.title = 'Identity anchored on-chain';
    chip.textContent = '✓ on-chain';
    card.querySelector('.ad-id').appendChild(chip);
  }
  if (ad.title) {
    card.querySelector('.ad-type').style.display = 'none';
    card.querySelector('.ad-fee').style.display = 'none';
    card.querySelector('.accepts').style.display = 'none';
    card.querySelector('.methods').style.display = 'none';
    card.querySelector('.meta').style.display = 'none';
    const h = document.createElement('h3');
    h.textContent = ad.title;
    card.prepend(h);
    if (ad.description) {
      const d = document.createElement('p');
      d.className = 'muted';
      d.textContent = ad.description;
      card.insertBefore(d, h.nextSibling);
    }
    {
      const img = document.createElement('img');
      img.src = ad.image || '/img/placeholder.svg';
      img.loading = 'lazy';
      img.alt = ad.title;
      img.className = 'ad-image';
      card.insertBefore(img, card.firstChild);
    }
    if (ad.price != null) {
      const p = document.createElement('div');
      p.className = 'ad-price';
      p.textContent = `${ad.price} ${ad.assets[0] || ''}`;
      card.insertBefore(p, card.querySelector('.ad-row'));
    }
    if (ad.url) {
      const a = document.createElement('a');
      a.href = ad.url;
      a.target = '_blank';
      a.rel = 'noopener';
      a.className = 'ad-external';
      a.textContent = 'View in shop ↗';
      card.appendChild(a);
    }
  }
  card.title = `BCH pubkey: ${ad.publicKey}`;
  card.tabIndex = 0;
  card.setAttribute('role', 'button');
  card.addEventListener('click', () => openAd(ad));
  card.addEventListener('keydown', (e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); openAd(ad); } });
  return card;
}

function renderFeatured() {
  const list = document.getElementById('featured-list');
  const section = document.getElementById('featured');
  if (!list || !section) return;
  list.innerHTML = '';
  const withImage = exchangeAds.filter((a) => a.image);
  const picks = (withImage.length >= 4 ? withImage : exchangeAds).slice(0, 4);
  if (!picks.length) { section.hidden = true; return; }
  for (const ad of picks) list.appendChild(buildCard(ad));
  section.hidden = false;
}

function renderExchange() {
  const list = $('exchange-list');
  list.innerHTML = '';
  const fa = $('filter-asset').value;
  const fm = $('filter-method').value;
  const sort = $('sort-by').value;
  const q = ($('search')?.value || '').trim().toLowerCase();
  const cat = state.category || 'exchange';
  let ads = exchangeAds.filter((a) => ((a.category || 'exchange') === cat) && (!state.subcategory || String(a.subcategory || '').toLowerCase() === state.subcategory.toLowerCase()) && (!state.adType || a.adType === state.adType) && (!fa || a.assets.includes(fa)) && (!fm || a.methods.includes(fm)));
  if (q) {
    ads = ads.filter((a) =>
      [a.name, a.cashAccount, a.title, a.description, a.location, a.npub, ...a.assets, ...a.methods]
        .filter(Boolean).join(' ').toLowerCase().includes(q));
  }
  ads = sort === 'fee' ? [...ads].sort((a, b) => a.fee - b.fee) : [...ads].sort((a, b) => b.createdAt - a.createdAt);
  $('exchange-note').textContent = ads.length ? '' : `No ads in ${cat} yet. Publish the first one.`;
  for (const ad of ads) list.appendChild(buildCard(ad));
  $('featured-buy-bch').classList.toggle('active', state.adType === 'SELLER');
  const CAT_LABEL = { exchange: 'Currency exchange', goods: 'Goods', services: 'Services', restaurants: 'Restaurants' };
  $('exchange-title').textContent = cat === 'exchange'
    ? (state.adType === 'SELLER' ? 'Currency exchange · Buy BCH' : state.adType === 'BUYER' ? 'Currency exchange · Sell BCH' : 'Currency exchange')
    : (CAT_LABEL[cat] || 'Board') + (state.subcategory ? ` · ${state.subcategory}` : '');
}

function openAd(ad) {
  state.selectedAd = ad;
  showView('ad');
  const isSeller = ad.adType === 'SELLER';
  const generic = ad.category && ad.category !== 'exchange';
  const CAT_LABEL = { exchange: 'Currency exchange', goods: 'Goods', services: 'Services', restaurants: 'Restaurants' };
  $('back-ad').textContent = `← ${CAT_LABEL[generic ? ad.category : 'exchange'] || 'Board'}`;
  $('identity-title').textContent = `Identity: ${ad.name || ad.cashAccount || 'this merchant'}`;
  $('detail-product').hidden = !generic;
  $('detail-image').hidden = !generic;
  if (generic) $('detail-image').src = ad.image || '/img/placeholder.svg';
  $('detail-price').textContent = generic && ad.price != null ? `${ad.price} ${ad.assets[0] || ''}` : '';
  $('detail-cond').textContent = generic ? [ad.condition, ad.shipping].filter(Boolean).join(' · ') : '';
  $('detail-desc').textContent = generic ? (ad.description || '') : '';
  $('detail-type').textContent = `Sells ${isSeller ? 'BCH' : (ad.assets[0] ?? 'asset')}`;
  $('detail-type').className = `ad-type ${ad.adType.toLowerCase()}`;
  $('detail-type').style.display = generic ? 'none' : '';
  $('detail-name').textContent = generic ? (ad.title || ad.name || 'Item') : (ad.name || ad.cashAccount || 'Merchant');
  $('detail-sub').textContent = [ad.location, ad.hours].filter(Boolean).join(' · ');
  $('detail-fee').textContent = `${(ad.fee * 100).toFixed(2)}% fee`;
  $('detail-fee').style.display = generic ? 'none' : '';
  $('detail-assets-label').textContent = generic ? 'Payment BCH' : 'Offers';
  $('detail-assets').textContent = generic ? 'BCH' : (isSeller ? (ad.assets.join(', ') || '—') : 'BCH');
  $('detail-assets').closest('div').style.display = generic ? 'none' : '';
  $('detail-methods').textContent = ad.methods.join(' · ') || '—';
  $('detail-limits').textContent = `${ad.min ?? '—'} – ${ad.max ?? '—'} ${ad.assets[0] ?? ''}`;
  $('detail-limits').parentElement.style.display = generic ? 'none' : '';
  $('detail-where').textContent = [ad.location, ad.hours].filter(Boolean).join(' · ') || '—';
  $('buy-title').style.display = generic ? 'none' : '';
  $('buy-title').textContent = isSeller ? 'Buy BCH' : 'Sell BCH';
  $('buy-get').textContent = generic ? 'Buy this item' : (isSeller ? 'Buy BCH' : 'Sell BCH');
  $('buy-hint').textContent = generic
    ? `${ad.price ?? '—'} ${ad.assets[0] || 'EUR'} · ${ad.condition || ''}${ad.shipping ? ' · ' + ad.shipping : ''}`
    : `Fee ${(ad.fee * 100).toFixed(2)}%; limits ${ad.min ?? '—'} – ${ad.max ?? '—'} ${ad.assets[0] ?? ''}.`;
  $('buy-amount').value = generic ? (ad.price ?? '') : (ad.min ?? '');
  $('buy-amount').min = generic ? '' : (ad.min ?? '');
  $('buy-amount').max = generic ? '' : (ad.max ?? '');
  $('buy-amount').disabled = generic;
  $('buy-currency').disabled = generic;
  $('buy-method').closest('label').style.display = generic ? 'none' : '';
  $('buy-destination').closest('label').style.display = generic ? 'none' : '';
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
  updateOwnAd();
  const dest = document.getElementById('buy-destination');
  if (dest && !dest.value && wcReceiveAddress) dest.value = wcReceiveAddress;
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
  { title: 'Goods', items: ['Technology', 'Home', 'Fashion', 'Motor', 'Sport', 'Food', 'Books', 'Collectibles'].map((l) => ({ label: l, kind: 'category', value: 'goods', sub: l })) },
  { title: 'Services', items: ['Home', 'Professional', 'Classes', 'Repairs'].map((l) => ({ label: l, kind: 'category', value: 'services', sub: l })) },
];

function countFor(it) {
  const exch = (a) => (a.category || 'exchange') === 'exchange';
  if (it.kind === 'all') return exchangeAds.filter(exch).length;
  if (it.kind === 'category') return exchangeAds.filter((a) => (a.category || 'exchange') === it.value && (!it.sub || String(a.subcategory || '').toLowerCase() === it.sub.toLowerCase())).length;
  if (it.kind === 'buyBch') return exchangeAds.filter((a) => exch(a) && a.adType === 'SELLER').length;
  if (it.kind === 'sellBch') return exchangeAds.filter((a) => exch(a) && a.adType === 'BUYER').length;
  if (it.kind === 'asset') return exchangeAds.filter((a) => exch(a) && a.assets.includes(it.value)).length;
  if (it.kind === 'method') return exchangeAds.filter((a) => exch(a) && a.methods.includes(it.value)).length;
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
  document.body.classList.toggle('viewing', !!name);
  document.getElementById('exchange').hidden = name !== 'exchange';
  document.getElementById('restaurants-view').hidden = name !== 'restaurants';
  document.getElementById('menu-view').hidden = name !== 'menu';
  document.getElementById('anchor-view').hidden = name !== 'anchor';
  document.getElementById('ad-view').hidden = name !== 'ad';
  const target = name === 'exchange' ? 'exchange' : name === 'restaurants' ? 'restaurants-view' : name === 'menu' ? 'menu-view' : name === 'anchor' ? 'anchor-view' : name === 'ad' ? 'ad-view' : 'categories';
  const el = document.getElementById(target);
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function applyCategory(it) {
  if (it.kind === 'restaurants') {
    document.getElementById('categories-dialog').close();
    showView('restaurants');
    return;
  }
  if (it.kind === 'category') {
    state.category = it.value;
    state.subcategory = it.sub || null;
    state.adType = null;
    document.getElementById('filter-asset').value = '';
    document.getElementById('filter-method').value = '';
    renderExchange();
    showView('exchange');
    document.getElementById('categories-dialog').close();
    return;
  }
  state.category = 'exchange';
  state.subcategory = null;
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
function savedAnchorForNetwork() {
  const s = loadSavedAnchor();
  return s?.txid && (s.network || 'mainnet') === selectedNetwork() ? s.txid : null;
}

// The write-through anchor index: clients register their anchor when they mint it,
// and any browser can look it up by the wallet pubkey (the same key that signed it).
const ANCHOR_INDEX = 'https://oracle.asgaya.org/anchors';
async function lookupIndexedAnchor(pubHex) {
  if (!pubHex) return null;
  try {
    const r = await fetch(`${ANCHOR_INDEX}?pubkey=${encodeURIComponent(pubHex)}`, { cache: 'no-store' });
    if (!r.ok) return null;
    const j = await r.json();
    return (j.anchors && j.anchors[0]) || null;
  } catch { return null; }
}
function registerAnchor({ pubkey, address, anchor, commitment, name }) {
  fetch(ANCHOR_INDEX, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ pubkey, address, anchor, commitment, name: name || '', network: 'mainnet' }),
  }).catch(() => {});
}

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
  let fallback = null;
  for (const h of history) {
    try {
      const raw = await electrumGetRawTx(anchorWs(), h.tx_hash);
      if (typeof raw !== 'string') continue;
      const tx = decodeTransaction(hexToBin(raw));
      if (typeof tx === 'string') continue;
      const c = tx.outputs?.[0]?.token?.nft?.commitment;
      if (!c) continue;
      if (binToHex(c).toLowerCase() === want) return h.tx_hash;
      if (!fallback) fallback = h.tx_hash;
    } catch { /* skip */ }
  }
  if (fallback) return fallback;
  const idx = state.bchPub ? await lookupIndexedAnchor(state.bchPub) : null;
  return idx ? idx.anchor : null;
}

function refreshPublishButton(keepExisting = false) {
  const btn = document.getElementById('anchor-mint');
  const note = document.getElementById('anchor-note');
  if (!btn) return;
  if (!wc) { btn.disabled = true; note.textContent = 'Connect a BCH wallet to publish.'; return; }
  const want = currentCommitmentHex();
  if (!keepExisting) identityAnchor = matchAnchor(want) ?? savedAnchorForNetwork();
  const existing = want ? identityAnchor : null;
  btn.disabled = !want;
  document.getElementById('anchor-selfsend').disabled = anchorUtxos.length === 0;
  if (!want) note.textContent = 'Pick one currency and one payment method to continue.';
  else if (existing) note.textContent = `Ready — reusing your identity anchor ${existing.slice(0, 12)}…`;
  else if (anchorUtxos.length) note.textContent = 'Ready — you will approve the transaction in your wallet.';
  else note.textContent = `No mainnet UTXOs for ${wcAddress || 'this wallet'}. Check that the wallet is on mainnet and funded (the address is shown below the button).`;
}

async function identityNameFor(anchorTx) {
  if (!anchorTx) return null;
  const saved = loadSavedAnchor();
  if (saved?.txid === anchorTx && saved.name) return saved.name;
  if (state.bchPub) {
    const rec = await lookupIndexedAnchor(state.bchPub);
    if (rec?.anchor === anchorTx && rec.name) return rec.name;
  }
  return null;
}

function lockIdentityName(name) {
  const el = document.getElementById('ad-name');
  if (!el) return;
  if (name) {
    el.value = name; el.disabled = true;
    el.title = 'This name belongs to your identity. To change it, spend the anchor NFT and mint a successor that inherits the reputation.';
  } else {
    el.disabled = false; el.title = '';
  }
}

async function anchorLoad() {
  if (!wc) { lockIdentityName(null); document.getElementById('anchor-note').textContent = 'Connect a BCH wallet to publish.'; return; }
  document.getElementById('anchor-address').textContent = wcAddress ?? '';
  const note = document.getElementById('anchor-note');
  note.textContent = 'Looking for the wallet funds…';
  const tryPath = async (pathName, index) => {
    wc.pathName = pathName; wc.addressIndex = index;
    const lb = await wc.getLockingBytecode();
    const sh = await scripthashFromLockingBytecode(lb);
    const utxos = await electrumListUnspent(anchorWs(), sh);
    return { pathName, index, lb, utxos };
  };
  let found = null;
  const start = state.fundPath || null;
  if (start) { const t = await tryPath(start.pathName, start.index); if (t.utxos.length) found = t; }
  if (!found) {
    for (const pathName of ['receive', 'change']) {
      for (let index = 0; index < 20 && !found; index++) {
        if (start && start.pathName === pathName && start.index === index) continue;
        const t = await tryPath(pathName, index);
        if (t.utxos.length) found = t;
      }
    }
  }
  if (found) {
    state.fundPath = { pathName: found.pathName, index: found.index };
    wcLocking = found.lb;
    const r = lockingBytecodeToCashAddress({ prefix: addressPrefix(), bytecode: wcLocking, tokenSupport: false });
    wcAddress = typeof r === 'string' ? null : r.address;
    anchorUtxos = found.utxos;
    try { wcPubHex = binToHex(await wc.getPubkey()); } catch { /* keep */ }
    document.getElementById('anchor-address').textContent = `${wcAddress ?? ''} (${found.pathName}[${found.index}])`;
    note.textContent = 'Found the wallet funds. Ready to publish.';
  } else {
    wc.pathName = 'receive'; wc.addressIndex = 0;
    wcLocking = await wc.getLockingBytecode();
    const r = lockingBytecodeToCashAddress({ prefix: addressPrefix(), bytecode: wcLocking, tokenSupport: false });
    wcAddress = typeof r === 'string' ? null : r.address;
    anchorUtxos = [];
  }
  try { identityAnchor = await findIdentityAnchor(currentCommitmentHex()); } catch { identityAnchor = null; }
  lockIdentityName(identityAnchor ? await identityNameFor(identityAnchor) : null);
  refreshPublishButton(true);
}

function openAnchor() {
  showView('anchor');
  { const ab = $('anchor-connect'); if (ab) ab.textContent = wc ? 'Disconnect wallet' : '1 · Connect a BCH wallet'; }
  document.getElementById('ad-pub').textContent = '';
  document.getElementById('ad-npub').textContent = '';
  if (!wc) {
    document.getElementById('anchor-note').textContent = 'Connect a BCH wallet to publish.';
    return;
  }
  wc.getPubkey().then((pub) => {
    const k = keyFromPub(binToHex(pub));
    state.bchPub = k.bchPubHex;
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
    state.bchPub = k.bchPubHex;
    const role = document.getElementById('ad-role').value;
    const category = document.getElementById('ad-category').value;
    const generic = category !== 'exchange';
    const assets = generic
      ? [document.getElementById('ad-currency').value]
      : [...document.querySelectorAll('input[name=ad-asset]:checked')].map((x) => x.value);
    const methods = generic
      ? ['BCH']
      : [...document.querySelectorAll('input[name=ad-method]:checked')].map((x) => x.value);
    if (!generic && assets.length === 0) throw new Error('Pick at least one currency.');
    if (!generic && methods.length === 0) throw new Error('Pick at least one payment method.');
    const fee = generic ? 0 : Number(document.getElementById('ad-fee').value);
    const min = generic ? 0 : Number(document.getElementById('ad-min').value);
    const max = generic ? 0 : Number(document.getElementById('ad-max').value);
    const name = document.getElementById('ad-name').value;
    const title = document.getElementById('ad-title').value.trim();
    const description = document.getElementById('ad-description').value.trim();
    const price = Number(document.getElementById('ad-price').value);
    const image = document.getElementById('ad-image').value.trim();
    const externalUrl = document.getElementById('ad-url').value.trim();
    if (generic && !title) throw new Error('Enter a title.');
    if (generic && (!Number.isFinite(price) || price <= 0)) throw new Error('Enter a price.');
    const cash = '';
    const location = (generic ? document.getElementById('ad-location-product') : document.getElementById('ad-location')).value;
    const hours = generic ? '' : document.getElementById('ad-hours').value;
    const subcategory = generic ? document.getElementById('ad-subcategory').value : null;
    const condition = generic ? document.getElementById('ad-condition').value : null;
    const shipping = generic ? document.getElementById('ad-shipping').value : null;
    const notes = (generic ? document.getElementById('ad-notes-product') : document.getElementById('ad-notes')).value;
    await anchorLoad();
    const commitment = encodeAnchorCommitment({
      role,
      asset: assets[0],
      method: METHOD_KEY[methods[0]] ?? 'other',
      feeBps: Math.round(fee * 100),
    });
    const commitmentHex = binToHex(commitment);
    let anchorTx = identityAnchor ?? savedAnchorForNetwork() ?? undefined;
    if (anchorTx) {
      document.getElementById('anchor-note').textContent = `Reusing your identity anchor ${anchorTx.slice(0, 12)}…`;
    } else {
      let v0 = anchorUtxos
        .filter((u) => u.tx_pos === 0 && BigInt(u.value) >= 3000n)
        .sort((a, b) => Number(b.value) - Number(a.value))[0];
      if (!v0) {
        await anchorSelfSend();
        v0 = anchorUtxos
          .filter((u) => u.tx_pos === 0 && BigInt(u.value) >= 3000n)
          .sort((a, b) => Number(b.value) - Number(a.value))[0];
      }
      if (!v0) throw new Error('The wallet is not ready yet. Give it a moment and press Publish again.');
      const built = buildGenesisAnchor(wcLocking, { txid: v0.tx_hash, vout: v0.tx_pos, satoshis: BigInt(v0.value) }, commitment);
      walletPrompt('Check Cashonize: approve the transaction that creates your identity.');
      const res = await wc.signTransaction({ transaction: built.transaction, sourceOutput: built.sourceOutput, inputIndex: 0, userPrompt: 'Create your identity' });
      walletPrompt('');
      anchorTx = res.txid ?? (await broadcastTransaction(anchorWs(), binToHex(res.signedTxBytes)));
      saveSavedAnchor({ txid: anchorTx, commitmentHex, name: name, network: selectedNetwork() });
      registerAnchor({ pubkey: k.bchPubHex, address: wcAddress, anchor: anchorTx, commitment: commitmentHex, name });
      identityAnchor = anchorTx;
    }
    const adId = generic
      ? `${k.bchPubHex.slice(0, 16)}-${category}-${slug(title)}`
      : `${k.bchPubHex.slice(0, 16)}-${role === 'seller' ? 'SELLER' : 'BUYER'}-${assets.join('')}-${slug(methods[0])}`;
    const tags = [
      ['d', adId],
      ['t', 'asgaya-board'],
      ['t', generic ? `asgaya-${category}` : (role === 'seller' ? 'asgaya-seller' : 'asgaya-buyer')],
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
      ...(generic ? [['title', title], ['price', String(price)], ['t', `category:${category}`], ['t', `subcategory:${slug(subcategory || '')}`], ['condition', condition || ''], ['shipping', shipping || '']] : []),
      ...(image ? [['image', image]] : []),
      ...(externalUrl ? [['url', externalUrl]] : []),
      ['status', 'active'],
      ['anchor', `${anchorTx}:0`],
      ['network', selectedNetwork()],
    ];
    const content = JSON.stringify({
      adType: generic ? 'SELLER' : (role === 'seller' ? 'SELLER' : 'BUYER'),
      category,
      title: generic ? title : undefined,
      description: generic ? description : undefined,
      price: generic ? price : undefined,
      subcategory: generic ? subcategory : undefined,
      condition: generic ? condition : undefined,
      shipping: generic ? shipping : undefined,
      image: image || undefined,
      url: externalUrl || undefined,
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
      notes,
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
    renderFeatured();
    const created = exchangeAds.find((a) => a.id === event.id);
    if (created) openAd(created); else showView('exchange');
  } catch (e) {
    document.getElementById('anchor-note').textContent = `Publish failed: ${e.message}`;
    btn.disabled = false;
  }
}

function walletPrompt(text) {
  const p = document.getElementById('anchor-prompt');
  if (!p) return;
  if (!text) { p.hidden = true; p.textContent = ''; return; }
  p.hidden = false;
  p.textContent = text;
  document.getElementById('anchor-note').textContent = text;
}

async function anchorSelfSend() {
  try {
    if (!wc) await connectWallet();
    const u = anchorUtxos.slice().sort((a, b) => Number(b.value) - Number(a.value))[0];
    if (!u) throw new Error('No UTXOs');
    const built = buildSelfSend(wcLocking, { txid: u.tx_hash, vout: u.tx_pos, satoshis: BigInt(u.value) });
    walletPrompt('Check Cashonize: approve the small self-send (one-time).');
    const res = await wc.signTransaction({ transaction: built.transaction, sourceOutput: built.sourceOutput, inputIndex: 0, userPrompt: 'Prepare the wallet (one-time)' });
    const txid = res.txid ?? (await broadcastTransaction(anchorWs(), binToHex(res.signedTxBytes)));
    await anchorLoad();
    walletPrompt('');
    document.getElementById('anchor-note').textContent = `Wallet prepared (${txid.slice(0, 12)}…).`;
  } catch (e) {
    const msg = e.message || '';
    if (/already in block chain|already known|already-in-mempool|already in the mempool/i.test(msg)) {
      document.getElementById('anchor-note').textContent = 'The wallet is already prepared. Refreshing…';
      await anchorLoad().catch(() => {});
    } else {
      document.getElementById('anchor-note').textContent = `Self-send failed: ${msg}`;
    }
  }
}

async function openPay(ad) {
  if (isOwnAd(ad)) { alert('This is your own ad — you are the seller. Switch to a buyer wallet to try it.'); return; }
  const generic = ad.category && ad.category !== 'exchange';
  const method = $('buy-method').value;
  const dest = ($('buy-destination')?.value || wcAddress || '').trim();
  if (generic) {
    const amount = Number(ad.price || 0);
    if (!(amount > 0)) { alert('This ad has no price.'); return; }
    $('order-title').textContent = `Order: ${ad.title || ad.name || 'item'}`;
    $('order-sub').textContent = `${amount} ${ad.assets[0] || 'EUR'} · the merchant confirms the payment details in the chat.`;
    $('order-name').value = '';
    $('order-shipping').value = '';
    $('order-qty').value = '1';
    $('order-dialog').showModal();
    return;
  }
  const amount = Number($('buy-amount').value);
  if (!Number.isFinite(amount) || amount <= 0) { alert('Enter an amount.'); return; }
  if (ad.min != null && amount < ad.min) { alert(`Minimum is ${ad.min} ${ad.assets[0] ?? ''}.`); return; }
  if (ad.max != null && amount > ad.max) { alert(`Maximum is ${ad.max} ${ad.assets[0] ?? ''}.`); return; }
  if (!dest) { alert('Enter the BCH address where you want to receive the funds.'); return; }
  const params = new URLSearchParams({
    ad: ad.id, seller: ad.npub, name: ad.name || '', asset: ad.assets[0] || 'EUR',
    amount: String(amount), method, anchor: ad.anchor || '',
    dest,
  });
  state.payAddress = null;
  state.payAmount = null;
  history.pushState(null, '', `#pay?${params}`);
  startPay(params);
}

function isOwnAd(ad) {
  return !!(wcPubHex && ad && ad.publicKey && ad.publicKey.toLowerCase() === wcPubHex.toLowerCase());
}

function updateOwnAd() {
  const w = document.getElementById('own-warning');
  if (!w) return;
  const ad = state.selectedAd;
  const own = !!(ad && state.view === 'ad' && isOwnAd(ad));
  w.hidden = !own;
  const b = document.getElementById('buy-get');
  const c = document.getElementById('contact-seller');
  if (b) { b.disabled = own; b.title = own ? 'This is your own ad' : ''; }
  if (c) { c.disabled = own; c.title = own ? 'This is your own ad' : ''; }
}

function openChat(ad) {
  if (isOwnAd(ad)) { alert('This is your own ad — you are the seller. Switch to a buyer wallet to try it.'); return; }
  const params = new URLSearchParams({
    type: 'chat', ad: ad.id, seller: ad.npub,
    name: ad.name || ad.title || '', anchor: ad.anchor || '',
  });
  startPay(params);
}

function placeOrder(ad) {
  if (isOwnAd(ad)) { alert('This is your own ad — you are the seller.'); return; }
  const orderName = $('order-name').value.trim();
  const shipping = $('order-shipping').value.trim();
  const qty = Math.max(1, Number($('order-qty').value || 1));
  if (!orderName) { alert('Enter your name.'); return; }
  if (!shipping) { alert('Enter the shipping address or “pickup”.'); return; }
  const amount = Number(ad.price || 0) * qty;
  const params = new URLSearchParams({
    type: 'product', ad: ad.id, seller: ad.npub, name: ad.name || '',
    asset: ad.assets[0] || 'EUR', amount: String(amount), method: 'BCH',
    anchor: ad.anchor || '', dest: (wcAddress || ''), orderName, shipping,
    quantity: String(qty),
  });
  $('order-dialog').close();
  history.pushState(null, '', `#pay?${params}`);
  startPay(params);
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
  if (text.includes('[ORDER_INSTRUCTIONS]')) {
    const kv = {};
    for (const line of text.split('\n')) {
      const i = line.indexOf('=');
      if (i > 0) kv[line.slice(0, i).trim()] = line.slice(i + 1).trim();
    }
    return `✅ Order confirmed — send ${kv.amount || '?'} ${kv.currency || 'EUR'} in BCH to ${kv.address || '?'}. Order ${kv.orderId || '?'}.`;
  }
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
  { const d = $('pay-dialog'); if (!d.open) d.showModal(); }
  if (state.payUnsub) { try { state.payUnsub(); } catch { /* ignore */ } state.payUnsub = null; }
  $('pay-chat').innerHTML = '';
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
  const type = params.get('type') || 'exchange';
  $('pay-name').textContent = name;
  $('pay-sub').textContent = type === 'chat' ? 'Ask the merchant anything' : `${amount} ${asset} · ${method}`;
  $('pay-ids').textContent = `You (session): ${npubHex.slice(0, 16)}… · Merchant: ${seller.slice(0, 16)}…${anchor ? ` · anchor ${anchor}` : ''}`;
  $('pay-status').textContent = 'Waiting for the merchant…';
  $('pay-status').className = 'badge warn';
  chatAdd('sys', 'A private, end-to-end encrypted channel with the merchant. Mercar only showed you the ad; it cannot read or store these messages.');
  if (!seller) { chatAdd('sys', 'This ad has no merchant npub, so the channel cannot be opened.'); return; }
  const requestId = (crypto.randomUUID && crypto.randomUUID()) || String(Date.now());
  const request = type === 'product'
    ? [
        '[PRODUCT_ORDER]',
        `orderId=${requestId}`,
        `adId=${params.get('ad') || ''}`,
        `title=${params.get('name') || ''}`,
        `quantity=1`,
        `amount=${amount}`,
        `currency=${asset}`,
        `buyerAddress=${destination}`,
        `buyerName=${params.get('orderName') || ''}`,
        `shipping=${params.get('shipping') || ''}`,
        `timestamp=${Date.now()}`,
        '[/PRODUCT_ORDER]',
      ].join('\n')
    : [
        '[PAY_INSTRUCTIONS_REQUEST]',
        `adId=${params.get('ad') || ''}`,
        `amount=${amount}`,
        `currency=${asset}`,
        `buyerAddress=${destination}`,
        `requestId=${requestId}`,
        `timestamp=${Date.now()}`,
        '[/PAY_INSTRUCTIONS_REQUEST]',
      ].join('\n');
  if (type === 'chat') {
    chatAdd('sys', 'Ask the merchant anything. They answer from their own device — Mercar never reads or stores these messages.');
  } else {
  chatAdd('sys', type === 'product'
    ? `Order placed: ${amount} ${asset} · we asked the merchant for the payment details.`
    : `Requested payment instructions: ${amount} ${asset} (${method}).`);
  try {
    const content = await nip04Encrypt(privHex, seller, request);
    const event = makeEvent({ privHex, pubHex: npubHex, kind: 4, tags: [['p', seller]], content });
    await publishEvent(NOSTR_RELAY, event);
  } catch (e) { chatAdd('sys', `Could not send the request: ${e.message}`); }
  }
  const timer = type === 'chat' ? null : setTimeout(() => {
    if ($('pay-status').textContent.startsWith('Waiting')) {
      $('pay-status').textContent = 'Merchant not detected';
      chatAdd('sys', 'The merchant’s app has not replied. It must be open: the instructions come from their device (liveness).');
    }
  }, 45000);
  state.payUnsub = subscribeDMs(NOSTR_RELAY, { me: npubHex, author: seller, onEvent: async (ev) => {
    console.log('[pay] dm', String(ev.id || '').slice(0, 10), 'from', String(ev.pubkey || '').slice(0, 12), 'expected seller', String(seller || '').slice(0, 12));
    if (ev.pubkey !== seller) return;
    try {
      const pt = await nip04Decrypt(privHex, seller, ev.content);
      const reqId = (pt.match(/requestId=([^\n\]]+)/) || [])[1];
      const orderId = (pt.match(/orderId=([^\n\]]+)/) || [])[1];
      chatAdd('them', formatInstructions(pt), orderId ? `order-${orderId}` : (reqId ? `pi-${reqId}` : undefined));
      if (pt.includes('[ORDER_INSTRUCTIONS]')) {
        const kv = {};
        for (const line of pt.split('\n')) {
          const i = line.indexOf('=');
          if (i > 0) kv[line.slice(0, i).trim()] = line.slice(i + 1).trim();
        }
        state.payAddress = kv.address || null;
        state.payAmount = kv.amount ? Number(kv.amount) : null;
        if (state.payAddress) openCheckout().catch(() => {});
      }
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
  $('order-cancel').addEventListener('click', () => $('order-dialog').close());
  $('pay-close').addEventListener('click', () => $('pay-dialog').close());
  $('order-place').addEventListener('click', () => { if (state.selectedAd) placeOrder(state.selectedAd); });
  $('connect-btn').addEventListener('click', () => {
    if (wc) {
      if (confirm('Disconnect the wallet?')) { wc.disconnect(); resetWallet(); }
      return;
    }
    connectWallet().catch((e) => {
      if (e.message === 'Cancelled') return;
      const dlg = $('wc-dialog');
      if (!dlg.open) dlg.showModal();
      $('wc-uri').textContent = e.message;
    });
  });
  $('wc-cancel').addEventListener('click', () => {
    if (cancelConnect) { cancelConnect(); cancelConnect = null; }
    $('wc-dialog').close();
  });
  $('search').addEventListener('input', () => { if ($('search').value.trim()) showView('exchange'); renderExchange(); });
  $('browse-board').addEventListener('click', () => showView(null));
  $('brand-home').addEventListener('click', () => { if (location.hash) history.pushState(null, '', location.pathname); showView(null); window.scrollTo({ top: 0, behavior: 'smooth' }); });
  $('merchant-btn').addEventListener('click', openAnchor);
  $('hero-publish').addEventListener('click', openAnchor);
  $('back-anchor').addEventListener('click', () => showView(null));
  $('anchor-connect').addEventListener('click', () => {
    if (wc) { wc.disconnect(); resetWallet(); openAnchor(); return; }
    connectWallet().then(() => openAnchor()).catch((e) => { if (e.message !== 'Cancelled') document.getElementById('anchor-note').textContent = e.message; });
  });
  $('anchor-mint').addEventListener('click', publishAd);
  $('ad-category').addEventListener('change', () => {
    const generic = $('ad-category').value !== 'exchange';
    $('ad-exchange').hidden = generic;
    $('ad-product').hidden = !generic;
    $('ad-assets-field').hidden = generic;
    $('ad-methods-field').hidden = generic;
  });
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
  $('featured-exchange').addEventListener('click', () => applyCategory({ kind: 'all' }));
  $('featured-goods').addEventListener('click', () => applyCategory({ kind: 'category', value: 'goods' }));
  $('featured-services').addEventListener('click', () => applyCategory({ kind: 'category', value: 'services' }));
  $('casa-elena').addEventListener('click', () => showView('menu'));
  $('back-exchange').addEventListener('click', () => showView(null));
  $('back-restaurants').addEventListener('click', () => showView(null));
  $('back-menu').addEventListener('click', () => showView('restaurants'));
  $('back-ad').addEventListener('click', () => showView('exchange'));
  $('buy-get').addEventListener('click', () => { if (state.selectedAd) openPay(state.selectedAd); });
  $('buy-amount').addEventListener('input', () => { $('buy-instructions').hidden = true; });
  $('contact-seller').addEventListener('click', () => { if (state.selectedAd) openChat(state.selectedAd); });
  $('buy-method').addEventListener('change', () => { $('buy-instructions').hidden = true; });
  $('all-categories').addEventListener('click', openCategories);
  $('categories-close').addEventListener('click', () => $('categories-dialog').close());
  $('featured-buy-bch').addEventListener('click', () => applyCategory({ kind: state.adType === 'SELLER' ? 'all' : 'buyBch' }));
  $('filter-asset').addEventListener('change', renderExchange);
  $('filter-method').addEventListener('change', renderExchange);
  $('sort-by').addEventListener('change', renderExchange);
  fetchListings()
    .then((ads) => { exchangeAds = ads; populateFilters(); renderExchange(); renderFeatured(); applyLocationHash(); })
    .catch((e) => { $('exchange-note').textContent = `Could not load ads (${e.message}). LAN relay: ${NOSTR_RELAY}`; });
  window.addEventListener('hashchange', applyLocationHash);
}

// Shareable links: #exchange, #goods, #services, #restaurants, #buy-bch, #cat=<category>, #ad=<event id>.
function applyLocationHash() {
  const raw = decodeURIComponent(location.hash.slice(1));
  if (!raw) return;
  const [key, value] = raw.split('=');
  const cat = key === 'cat' ? value : (raw === 'exchange' || raw === 'goods' || raw === 'services' || raw === 'restaurants' ? raw : null);
  if (cat) { state.subcategory = null; applyCategory({ kind: 'category', value: cat }); return; }
  if (raw === 'buy-bch') { applyCategory({ kind: 'buyBch' }); return; }
  if (key === 'ad' && value) {
    const ad = exchangeAds.find((a) => a.id === value);
    if (ad) openAd(ad);
  }
}

main();

// The judges' banner: copy the CashTokens address (EMBER2026 votes).
const CASH_TOKENS_ADDRESS = 'bitcoincash:zr2zsrsy00ns7v2uce3esmrrsnvv38qswsvas3fyzq';
const cashBtn = $('cash-btn');
if (cashBtn) {
  cashBtn.addEventListener('click', async () => {
    try {
      await navigator.clipboard.writeText(CASH_TOKENS_ADDRESS);
      const label = cashBtn.querySelector('span');
      const prev = label.textContent;
      label.textContent = 'copied';
      cashBtn.classList.add('copied');
      setTimeout(() => { label.textContent = prev; cashBtn.classList.remove('copied'); }, 1400);
    } catch (e) {
      cashBtn.title = CASH_TOKENS_ADDRESS;
    }
  });
}
