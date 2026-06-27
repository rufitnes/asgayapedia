# Buyers and Sellers: There Are Only Two Roles

**Everything collapses into this:** Some people have BCH and want fiat. Some people have fiat and want BCH. The bulletin board connects them. Covenants execute the trades. Nostr coordinates. The app automates passive mode.

---

## The Core Insight

| Role | Has | Wants |
|------|-----|-------|
| **BCH Seller** | BCH | Fiat (EUR, VES, USD) |
| **BCH Buyer** | Fiat (cash, bank transfer) | BCH |

That's it. Everything else is context.

---

## The Real Distinction: Active vs Passive

### Passive Users (Post Listings, App Automates)

- Post a listing once.
- The app handles requests 24/7.
- Zero cognitive load, fully scalable, passive income.

**Examples:** Isabel posts as a BCH Seller (Bizum) → senders select her. Carlos posts as a BCH Buyer (cash at store) → recipients visit him.
### Active Users (Query, Select, Transact)

- Open the app when needed.
- Query the bulletin board for available counterparties.
- Pick the best option and transact.

**Examples:** María (sender) selects a seller and pays. Elena (recipient) selects a merchant and gets cash.

**The key realization:** The same person can be passive at some times and active at others. This is what enables capital recycling.

---

## Why This Distinction Matters

- **Flexibility:** No one is locked into a single role. A merchant can passively accumulate BCH, then actively sell when rent is due.
- **Capital efficiency:** Passive mode accumulates; active mode liquidates. The cycle is self‑sustaining.
- **No central coordinator:** Passive users provide 24/7 liquidity via built-in automation; active users always find counterparties. The market self‑organizes.
- **Scales naturally:** Passive income scales with capital; active demand attracts more passive liquidity.
- **Democratizes remittances:** Instead of a few entities having a near-monopoly on cross-border transactions, thousands of passive users can compete to move value between different economic areas.

---

## Examples (Focused on High‑Inflation Economies)

### 1. Carlos the Merchant (Venezuela) — The Central Use Case

Carlos runs a grocery store. VES loses 5 % per week. He needs stable value to preserve purchasing power and bolívars for daily operations.

**Passive mode (most of the month):** Posts as a BCH Buyer (cash at store, 0.5 % spread). Recipients visit, get cash, and Carlos accumulates BCH automatically—no effort, just foot traffic and grocery sales.

**Stability option:** When Carlos cashes out a remittance covenant, his wallet offers: *Keep BCH or stabilice with H€ (Euro), or HAu (Gold)?* We think he's going to pick **H€** (familiar unit, he's used to convert to VES) but we don't know that's why we want to offer a non fiat denominated option. The app mints H€ or HAu tokens backed by pooled AnyHedge contracts. Carlos now holds stable value, immune to BCH's ±20 % monthly swings.

**Active mode (end of month, rent due):** Opens the app, sells H€ to another merchant or customer for VES at current EUR/VES rate. Zero volatility exposure—what he earned is what he keeps.

**Result:** Carlos earns spreads and sales while passive; he preserves capital via the stability tokens. No BCH volatility, no hyperinflation erosion. Triple win.

### 2. María (Sender) & Elena (Recipient) — Pure Active Users

María sends €100 to Elena. She opens the app, enters `Elena#142`, selects the cheapest seller, and pays via Bizum. The app handles the rest. Elena gets a notification, decides to cash out, picks the nearest merchant from the bulletin board, walks over, gets cash, and buys groceries. Both are pure active users—no listings needed.

### 3. Isabel (Passive Seller — Arbitrageur)

Isabel posts a single listing as a BCH Seller (Bizum, 0.5 % fee) and enables the bot. She goes to sleep. Overnight, the bot responds to requests, detects payments, and funds covenants. Isabel wakes up to €1.50 in fees without lifting a finger. Her bot even replenishes BCH via Kraken automatically.

### 4. Roberto (Passive Seller — Miner)

Roberto mines BCH. He sets up the app to sell his mined coins directly to senders, avoiding exchange fees entirely. Fully passive, zero extra work.

---

## Traditional Labels vs Active/Passive Reality

| Traditional Label | Active/Passive | What They Really Do |
|------------------|----------------|---------------------|
| **Sender** (María) | Always Active | Queries sellers, creates covenant, pays |
| **Recipient** (Elena) | Always Active | Queries merchants, creates covenant, receives cash |
| **Merchant** (Carlos) | Usually Passive | Posts as buyer, app handles recipients, accumulates BCH |
| | Sometimes Active | Queries buyers, sells accumulated BCH for bolívars |
| **Seller** (Isabel/Roberto) | Usually Passive | Posts as seller, app handles senders, earns fees |
| | Could be Active | If they needed BCH urgently, could query buyers |

---

## Phase 1+ Goals (Spain → Venezuela)


> **Note:** Phase 0 (bootstrap) uses trusted participants to validate demand. The founder plays most passive roles initially. If Phase 0 attracts 5 real merchants, that's success. These numbers reflect Phase 1+ goals once organic growth begins.

| Role | Mode | Count | Who |
|------|------|-------|-----|
| **Senders** | Active | 50–100 | Spanish residents sending to family |
| **Recipients** | Active | 50–100 | Venezuelans receiving remittances |
| **Merchants** | Passive buyer | 20–30 | Venezuelan businesses (cash pickup) |
| | Active seller | 20–30 | Same merchants (when needing bolívars) |
| **Sellers** | Passive seller | 10–20 | Miners, arbitrageurs |

**Success:** At least 5 passive sellers online 24/7; at least 15 passive merchants in major cities. Active users always see 3+ options, and response times are under 1 minute.

---

## How Active & Passive Use the Five Gears

**The Five Gears:**
1. **Wallet** - Key management, transaction signing
2. **Bulletin Board** - Discovering counterparties (on-chain NFTs)
3. **Nostr** - Private coordination (payment instructions, notifications)
4. **Covenants** - Trustless settlement (locked BCH, automated execution)
5. **Stability Layer** - H€/HAu tokens (merchants preserve purchasing power)

**Active Mode:** Wallet → Bulletin Board (query) → Nostr (coordination) → Covenant (transaction) → Optional: Stability Layer. User experience is like a price comparison site plus a payment app.

**Passive Mode:** Wallet → Bulletin Board (post listing) → App monitors Nostr, bank notifications, and blockchain → App auto-responds and funds covenants → Optional: Stability Layer for accumulated BCH. User experience is like a vending machine—stock once, check revenue later.

---

## Key Takeaways

1. **Two roles, two modes.** BCH Seller/Buyer × Active/Passive = the entire protocol.
2. **Merchants in inflationary economies win twice.** Passive accumulation protects against depreciation; active liquidation preserves purchasing power when needed.
3. **Built-in automation makes passive mode possible.** 24/7 liquidity, no human attention required.
4. **Active mode is simple.** Query, select, transact—no listing necessary.
5. **The market self‑organizes.** Passive liquidity + active demand = automatic matching, no central coordinator.

When someone asks, *”What is Asgaya?”* — it’s a bulletin board where passive users provide 24/7 liquidity via built-in automation, and active users pick the best offer. Covenants make it safe; Bitcoin Cash makes it cheap and permissionless.

---

**Next:** [How They Interact](../the-mechanism/how-they-interact.md) — End‑to‑end walkthrough  
**Explore:** [Sender](sender/), [Recipient](recipient/), [Merchant](merchant/), [Trader](trader/)  
**Related:** [Bulletin Board](../the-mechanism/bulletin-board.md), [Notification Bot](../the-mechanism/notification-bot.md), [Wallet](../the-mechanism/wallet.md)
