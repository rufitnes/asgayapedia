import QRCode from 'qrcode';
import { decodeCashAddress, lockingBytecodeToCashAddress, hexToBin, binToHex, secp256k1, sha256, decodePrivateKeyWif } from '@bitauth/libauth';
import { WizardConnectSigner } from './wizardconnect.js';
import { encodeAnchorCommitment, buildGenesisAnchor, buildSelfSend } from './anchor.js';
import { electrumListUnspent, scripthashFromLockingBytecode, broadcastTransaction } from './electrum.js';
import { fetchListings, NOSTR_RELAY, makeEvent, publishEvent } from './nostr.js';

const CATALOGUE_URL = 'catalogue/menu.json';
const ELECTRUM_WS = 'wss://chipnet.imaginary.cash:50004';
// Demo quote. The live fiat->BCH quote comes from the oracle in the next step.
const DEMO_EUR_PER_BCH = 207.88;

const state = { catalogue: null, cart: new Map(), adType: null, view: null };
let wc = null;
let wcLocking = null;
let wcAddress = null;

const eur = (cents) =>
  new Intl.NumberFormat('en-IE', { style: 'currency', currency: 'EUR' }).format(cents / 100);
const sats = (cents) => Math.round((cents / 100 / DEMO_EUR_PER_BCH) * 100_000_000);
const $ = (id) => document.getElementById(id);

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

function openCheckout() {
  const total = cartTotalCents();
  if (total === 0) return;
  $('co-total').textContent = eur(total);
  $('co-rate').textContent = `1 BCH = ${eur(Math.round(DEMO_EUR_PER_BCH * 100))} (demo)`;
  $('co-bch').textContent = `${sats(total).toLocaleString('en-GB')} sats`;
  $('co-address').textContent = state.catalogue.merchant.address;
  $('co-pay').disabled = false;
  $('co-pay').textContent = 'Pay with WizardConnect';
  $('checkout').showModal();
}

async function connectWallet() {
  if (wc) return;
  const dlg = $('wc-dialog');
  $('wc-qr').removeAttribute('src');
  $('wc-uri').textContent = 'Waiting for the relay…';
  if (!dlg.open) dlg.showModal();
  wc = await WizardConnectSigner.connect({
    onUri: async (u, q) => {
      $('wc-uri').textContent = u;
      try { $('wc-qr').src = await QRCode.toDataURL(q ?? u, { width: 280, margin: 1 }); } catch { /* optional */ }
    },
    broadcast: false,
    userPrompt: 'Pay with BCH',
    dappName: 'Mercar Asgaya',
  });
  wcLocking = await wc.getLockingBytecode();
  const r = lockingBytecodeToCashAddress({ prefix: 'bchtest', bytecode: wcLocking, tokenSupport: false });
  wcAddress = typeof r === 'string' ? null : r.address;
  console.log('[mercar] wallet', wcAddress);
  $('connect-btn').textContent = wcAddress ? `Connected ${wcAddress.slice(0, 14)}…` : 'Connected';
  dlg.close();
}

async function payWithBch() {
  const total = cartTotalCents();
  if (total === 0) return;
  $('co-pay').disabled = true;
  try {
    if (!wc) await connectWallet();
    const amountSats = sats(total);
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
    list.appendChild(card);
  }
  $('featured-buy-bch').classList.toggle('active', state.adType === 'SELLER');
  $('exchange-title').textContent =
    state.adType === 'SELLER' ? 'Currency exchange · Buy BCH'
    : state.adType === 'BUYER' ? 'Currency exchange · Sell BCH'
    : 'Currency exchange';
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
  const target = name === 'exchange' ? 'exchange' : name === 'restaurants' ? 'restaurants-view' : name === 'menu' ? 'menu-view' : name === 'anchor' ? 'anchor-view' : 'categories';
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

const TEST_NOSTR = {
  '03d6e1b33780e34dff1c6dc705c626deed385e827e762abfc596764be0ca3e4f45': '33'.repeat(32),
  '032774f66536468a06a244568724bf1f8762682be1573ff9910667c0fea1226ece': '11'.repeat(32),
  '03886b4f4100f9e348caed5155474f0a1a96db3e2706dc59e15260ed3d2b35e148': '22'.repeat(32),
};

function keyFromPub(pubHex) {
  const nostrPrivHex = TEST_NOSTR[pubHex.toLowerCase()];
  if (!nostrPrivHex) throw new Error('This wallet is not a seeded demo identity; use the app’s merchant wallet.');
  const nostrPub = secp256k1.derivePublicKeyCompressed(hexToBin(nostrPrivHex));
  return { bchPubHex: pubHex, nostrPrivHex, npubHex: binToHex(nostrPub.slice(1)) };
}

async function anchorLoad() {
  if (!wc) { document.getElementById('anchor-note').textContent = 'Connect a chipnet wallet to mint the anchor.'; return; }
  document.getElementById('anchor-address').textContent = wcAddress ?? '';
  const sh = await scripthashFromLockingBytecode(wcLocking);
  anchorUtxos = await electrumListUnspent(ELECTRUM_WS, sh);
  const v0 = anchorUtxos.filter((u) => u.tx_pos === 0 && BigInt(u.value) >= 3000n);
  document.getElementById('anchor-mint').disabled = v0.length === 0;
  document.getElementById('anchor-selfsend').disabled = anchorUtxos.length === 0;
  document.getElementById('anchor-note').textContent = v0.length
    ? `Ready: vout-0 UTXO with ${Number(v0[0].value)} sats.`
    : anchorUtxos.length
      ? 'No vout-0 UTXO: use “Prepare a vout-0 UTXO”.'
      : 'No chipnet UTXOs: use the chipnet faucet first.';
}

function openAnchor() {
  showView('anchor');
  document.getElementById('ad-pub').textContent = '';
  document.getElementById('ad-npub').textContent = '';
  if (!wc) {
    document.getElementById('anchor-note').textContent = 'Connect your merchant wallet (WizardConnect).';
    return;
  }
  wc.getPubkey().then((pub) => {
    const k = keyFromPub(binToHex(pub));
    document.getElementById('ad-pub').textContent = `BCH pubkey: ${k.bchPubHex}`;
    document.getElementById('ad-npub').textContent = `npub (hex): ${k.npubHex}`;
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
    const cash = document.getElementById('ad-cash').value;
    const location = document.getElementById('ad-location').value;
    const hours = document.getElementById('ad-hours').value;
    const v0 = anchorUtxos
      .filter((u) => u.tx_pos === 0 && BigInt(u.value) >= 3000n)
      .sort((a, b) => Number(b.value) - Number(a.value))[0];
    if (!v0) throw new Error('No vout-0 UTXO; prepare one first.');
    const commitment = encodeAnchorCommitment({
      role,
      asset: assets[0],
      method: METHOD_KEY[methods[0]] ?? 'cash',
      feeBps: Math.round(fee * 100),
    });
    const built = buildGenesisAnchor(wcLocking, { txid: v0.tx_hash, vout: v0.tx_pos, satoshis: BigInt(v0.value) }, commitment);
    document.getElementById('anchor-note').textContent = 'Approve the anchor in your wallet…';
    const res = await wc.signTransaction({ transaction: built.transaction, sourceOutput: built.sourceOutput, inputIndex: 0, userPrompt: 'Mint your identity anchor' });
    const anchorTx = res.txid ?? (await broadcastTransaction(ELECTRUM_WS, binToHex(res.signedTxBytes)));
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
      ['cash', cash],
      ['name', name],
      ['status', 'active'],
      ['anchor', `${anchorTx}:0`],
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
      phone: document.getElementById('ad-phone').value,
      iban: document.getElementById('ad-iban').value,
      notes: document.getElementById('ad-notes').value,
    });
    const event = makeEvent({ privHex: k.nostrPrivHex, pubHex: k.npubHex, kind: 30402, tags, content });
    document.getElementById('anchor-note').textContent = 'Publishing the listing…';
    await publishEvent(NOSTR_RELAY, event);
    document.getElementById('anchor-result').textContent =
      `Anchor tx: ${anchorTx}\nListing: ${event.id}\nIdentity (npub hex): ${k.xHex}`;
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
    const txid = res.txid ?? (await broadcastTransaction(ELECTRUM_WS, binToHex(res.signedTxBytes)));
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

async function main() {
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
  $('connect-btn').addEventListener('click', () => connectWallet().catch((e) => alert(e.message)));
  $('wc-cancel').addEventListener('click', () => $('wc-dialog').close());
  $('search').addEventListener('input', () => { if ($('search').value.trim()) showView('exchange'); renderExchange(); });
  $('browse-board').addEventListener('click', () => showView(null));
  $('merchant-btn').addEventListener('click', openAnchor);
  $('hero-publish').addEventListener('click', openAnchor);
  $('back-anchor').addEventListener('click', () => showView(null));
  $('anchor-connect').addEventListener('click', () => connectWallet().then(() => openAnchor()).catch((e) => { document.getElementById('anchor-note').textContent = e.message; }));
  $('anchor-mint').addEventListener('click', publishAd);
  $('anchor-selfsend').addEventListener('click', anchorSelfSend);
  $('anchor-refresh').addEventListener('click', () => anchorLoad().catch((e) => { document.getElementById('anchor-note').textContent = e.message; }));
  $('hero-buy-bch').addEventListener('click', () => applyCategory({ kind: 'buyBch' }));
  $('featured-exchange').addEventListener('click', () => { state.adType = null; $('filter-asset').value = ''; $('filter-method').value = ''; renderExchange(); showView('exchange'); });
  $('featured-restaurants').addEventListener('click', () => showView('restaurants'));
  $('casa-elena').addEventListener('click', () => showView('menu'));
  $('back-exchange').addEventListener('click', () => showView(null));
  $('back-restaurants').addEventListener('click', () => showView(null));
  $('back-menu').addEventListener('click', () => showView('restaurants'));
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
