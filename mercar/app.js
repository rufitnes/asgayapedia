// Mercar Asgaya — static board (Day 1 scaffold)
// Reads a merchant catalogue (JSON), renders the menu, keeps a cart, and
// prepares the direct-checkout request. WizardConnect is the next step.

const CATALOGUE_URL = "catalogue/menu.json";

// Demo quote. The live fiat->BCH quote comes from the oracle in the next step.
const DEMO_EUR_PER_BCH = 207.88;

const state = {
  catalogue: null,
  cart: new Map(), // itemId -> qty
};

const eur = (cents) =>
  new Intl.NumberFormat("en-IE", { style: "currency", currency: "EUR" }).format(cents / 100);

const sats = (cents) =>
  Math.round((cents / 100 / DEMO_EUR_PER_BCH) * 100_000_000);

const $ = (id) => document.getElementById(id);

function renderMerchant(c) {
  $("merchant-name").textContent = c.merchant.name;
  $("merchant-desc").textContent = c.merchant.description ?? "";
  $("merchant-address").textContent = c.merchant.address;
  if (c.merchant.network) {
    $("network-badge").textContent = `${c.merchant.network} demo`;
    $("network-badge").dataset.network = c.merchant.network;
  }
}

function renderMenu(c) {
  const menu = $("menu");
  menu.innerHTML = "";
  const byCategory = new Map();
  for (const item of c.items) {
    if (!byCategory.has(item.category)) byCategory.set(item.category, []);
    byCategory.get(item.category).push(item);
  }
  for (const [category, items] of byCategory) {
    const h = document.createElement("h2");
    h.className = "category";
    h.textContent = category;
    menu.appendChild(h);

    const grid = document.createElement("div");
    grid.className = "items";
    for (const item of items) {
      const card = document.createElement("article");
      card.className = "item";
      card.innerHTML = `
        <h3></h3>
        <p></p>
        <div class="item-foot">
          <span class="price"></span>
          <button class="primary" type="button">Add</button>
        </div>`;
      card.querySelector("h3").textContent = item.name;
      card.querySelector("p").textContent = item.description ?? "";
      card.querySelector(".price").textContent = eur(item.price);
      card.querySelector("button").addEventListener("click", () => add(item.id));
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
  const box = $("cart-items");
  box.innerHTML = "";
  if (state.cart.size === 0) {
    box.innerHTML = '<p class="cart-empty">Your cart is empty.</p>';
  } else {
    for (const [id, qty] of state.cart) {
      const item = state.catalogue.items.find((i) => i.id === id);
      if (!item) continue;
      const row = document.createElement("div");
      row.className = "cart-row";
      row.innerHTML = `
        <span></span>
        <span class="qty">
          <button type="button" aria-label="Remove">−</button>
          <span>${qty}</span>
          <button type="button" aria-label="Add">+</button>
        </span>`;
      row.querySelector("span").textContent = item.name;
      const [minus, plus] = row.querySelectorAll("button");
      minus.addEventListener("click", () => remove(id));
      plus.addEventListener("click", () => add(id));
      box.appendChild(row);
    }
  }
  const total = cartTotalCents();
  $("cart-total").textContent = eur(total);
  $("pay-btn").disabled = total === 0;
  $("pay-note").textContent =
    total === 0 ? "Add something from the menu to start." : "Direct payment on chipnet (demo).";
}

function openCheckout() {
  const total = cartTotalCents();
  if (total === 0) return;
  $("co-total").textContent = eur(total);
  $("co-rate").textContent = `1 BCH = ${eur(Math.round(DEMO_EUR_PER_BCH * 100))} (demo)`;
  $("co-bch").textContent = `${sats(total).toLocaleString("en-IE")} sats`;
  $("co-address").textContent = state.catalogue.merchant.address;
  $("checkout").showModal();
}

async function main() {
  try {
    const res = await fetch(CATALOGUE_URL, { cache: "no-store" });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    state.catalogue = await res.json();
    renderMerchant(state.catalogue);
    renderMenu(state.catalogue);
    renderCart();
  } catch (err) {
    $("menu").innerHTML =
      `<p class="muted">Could not load the catalogue (${err.message}).</p>`;
  }

  $("pay-btn").addEventListener("click", openCheckout);
  $("co-close").addEventListener("click", () => $("checkout").close());
  $("connect-btn").addEventListener("click", () => {
    $("pay-note").textContent = "WizardConnect connects in the next step.";
  });
}

main();
