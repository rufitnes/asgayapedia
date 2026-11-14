import QRCode from 'qrcode';
import { decodeCashAddress, lockingBytecodeToCashAddress, hexToBin, binToHex } from '@bitauth/libauth';
import { WizardConnectSigner } from './wizardconnect.js';
import { electrumListUnspent, scripthashFromLockingBytecode, broadcastTransaction } from './electrum.js';

const CATALOGUE_URL = 'catalogue/menu.json';
const ELECTRUM_WS = 'wss://chipnet.imaginary.cash:50004';
// Demo quote. The live fiat->BCH quote comes from the oracle in the next step.
const DEMO_EUR_PER_BCH = 207.88;

const state = { catalogue: null, cart: new Map() };
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
    const txidBytes = hexToBin(utxo.tx_hash).slice().reverse();
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
}

main();
