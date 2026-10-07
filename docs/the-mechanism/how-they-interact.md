# How They Interact: A Complete End‑to‑End Flow

**One transaction. Four gears.** Wallet, Bulletin Board, Nostr, Notification Bot—working together.

---

## The Transaction

**María** (Madrid) sends €100 to **Elena** (Caracas).  
**Isabel** (Barcelona) provides BCH passively. **Carlos** (Maracaibo) cashes Elena out.

**Total elapsed:** ~4 h (most is Elena’s decision delay)  
**Total cost:** €1.00 (0.5 % seller + 0.5 % merchant — the fee is set by the counterparties; here the Phase-0 bootstrap default) vs. €5 (Western Union)

---

## Part 1: María Sends (Active Sender)

### Step 1 — Wallet & Bulletin Board

María opens the app, enters `Elena#142`, specifies €100. The app resolves Elena’s Cash Account on‑chain and creates a covenant template (€100 worth of BCH, 7 % buffer, 8‑h timeout).

The app then queries the bulletin board for BCH sellers accepting Bizum in the EUR→VES corridor. It finds three passive sellers. María chooses on fee and rating: Isabel (0.5 %, good rating, Bizum).

### Step 2 — Nostr & Bot (with Device Health Check)

María’s app sends an encrypted Nostr request (“Need payment details for covenant xyz789”) to Isabel’s bot. The bot validates the covenant, checks its device health (bank app installed ✅, enabled ✅, battery 67% and charging ✅), generates a unique reference (`Elena#142`), and replies with the bank details (Bizum phone number, concept, amount €100.50) **plus health status** in under a second.

María’s app shows: **”✅ Seller ready (67% battery, charging)”** — green light to proceed.

María sees the payment instructions, taps “Open Bizum,” and pays €100.50 instantly.

### Step 3 — Bot Funds Covenant

Isabel’s Android notification listener detects the BBVA message (“Recibido 100,50 € … Concepto: Elena#142”), parses it, and matches it to the pending covenant. The bot creates a funding transaction locking €107 of Isabel’s BCH, signs it with limited covenant keys, and broadcasts it. Total processing time: ~2 seconds. Isabel is at work, completely unaware.

María’s app confirms the covenant is funded. Elena receives a push notification: “María sent you €100 in BCH.”

---

## Part 2: Elena Receives (Active Recipient)

### Step 4 — Claim BCH to Wallet

Elena’s phone buzzed four hours ago; she now opens Asgaya and taps “Claim BCH to Wallet.” The covenant releases the BCH to Elena’s address; the unused buffer returns to Isabel. **Elena now owns the BCH.**

### Step 5 — Decide & Discover

Elena chooses “Cash Out” (she needs bolívars today). The app queries the bulletin board for **BCH buyers** near Caracas (the picker shows **buyer ads**). It finds Carlos’s Grocery (0.8 km, 0.5 % spread, open). Elena selects it.

### Step 6 — Walk, Exchange, Shop

Elena walks to the store. She sends her **BCH-for-sale request** to Carlos (over Nostr — or shows a QR if she has no data). Carlos's app fetches a **fresh price**, pre-signs an offer and returns it; Elena reviews and co-signs; Carlos's app **broadcasts (BCH moves first)**, then hands her 398,000 VES (400k VES less 0.5 %). The sale appears in Carlos's **Buy BCH** tab.

Elena then buys groceries worth 300,000 VES from Carlos’s store.

**Carlos’s triple‑dip from this one visit:**
- 2,000 VES spread
- ~60,000 VES product margin (20 % on 300k)
- 0.2564 BCH position (hedge against VES depreciation)

### Step 7 — Stabilize (Optional)

Carlos's wallet now asks: **"Stabilize this BCH?"**

- **Keep BCH:** Accept volatility, potential upside
- **H€ (Heuro):** Lock in €99.50 value (familiar unit, easy to convert to VES)
- **HAu (Gold):** Lock in ~0.05 oz gold value (hedge ALL fiat inflation)

Carlos chooses **H€** (he knows EUR/VES rates, easier mental math). The app:
1. Checks H€ pool availability (founder's long capital for phase 0)
2. Creates 30‑day AnyHedge contract (Carlos shorts BCH/EUR, pool goes long)
3. Mints 99.50 H€ tokens to Carlos's wallet
4. Auto‑renews monthly unless Carlos burns tokens for BCH

**If pool exhausted:** Carlos keeps BCH (graceful degradation). Existing H€ holders unaffected.

**Carlos now holds stable EUR value.** When he needs VES for rent next week, he sells H€ to another merchant or customer at current EUR/VES rate. Zero BCH volatility exposure.

---

## The Four Gears at Work

- **Wallet:** María creates covenant, Elena claims, Carlos receives. Cash Accounts provide identity.
- **Bulletin Board:** María queries sellers; Elena queries merchants. Passive listings discovered via **Nostr (NIP‑99)**.
- **Nostr:** María’s app requests payment details from Isabel’s bot, and Elena coordinates the sale with Carlos (request → quote → co-sign → signed tx). Encrypted, sub‑second, no phone number.
- **Bot:** Isabel’s bot detects the bank notification and funds the covenant automatically. Carlos’s bot notifies him of the claim.

---

## Two Transactions = Clear Compliance

1. **María → Elena (remittance):** Completed when Elena claims BCH to her wallet. A family transfer.
2. **Elena → Carlos (commerce):** Elena sells her BCH for cash. A separate, voluntary transaction.

**Elena could have kept the BCH or spent it directly at the store.** The end goal is that enough merchants accept BCH directly so recipients never need to cash out.

---

## Timeline

| Step | Duration |
|------|----------|
| María creates covenant & pays | ~2 min |
| Isabel’s bot funds covenant | ~1 min |
| Elena claims BCH to wallet | ~2 min (after 4 h delay) |
| Elena walks to Carlos’s store | ~10 min |
| Cash exchange & shopping | ~8 min |
| **Total active human time** | **~12 min** |

---

## What Could Go Wrong (And Didn’t)

- **Seller offline or unhealthy:** Nostr request times out after 2 min OR device health check shows critical issues (bank app disabled, battery dead). María sees warning, picks another seller. **No money at risk** (caught before payment).
- **BCH drops >7 %:** Covenant aborts to protect Isabel. Instead of sending BCH to María (exposing her to 7% loss she didn't sign up for), the covenant mints H€ tokens (if pool has capacity) and sends €100 H€ to María. She can still send to Elena using H€. If pool exhausted, María receives BCH (fallback). Isabel keeps the fiat and fee.
- **Elena never claims:** the covenant expires; the **funder recovers its buffer** and María can **refund the BCH (manual)**.

---

## Cost Breakdown

| Party | Out | In | Net |
|-------|-----|-----|-----|
| María | €100.50 | (remittance delivered) | –€100.50 |
| Isabel | €107 BCH locked | €100.50 fiat + €0.50 fee | +€0.50 |
| Elena | 2k VES spread | €100 worth of BCH → 398k VES | +€99.50 equiv |
| Carlos | 398k VES cash | 0.2564 BCH + 2k spread + 60k sales | +€12.4 equiv |

**Total fee:** 1 % vs. Western Union’s 5 %.
If Elena had spent BCH directly at the store (Option B), total fee would have been **0.5 %** (Isabel’s seller fee only).

---

## Key Takeaways

1. **Two transactions = clear compliance.** Remittance (María→Elena) and commerce (Elena→Carlos) are separate.
2. **Passive bots provide 24/7 liquidity.** Isabel and Carlos posted once; bots handled everything.
3. **The end goal is visible.** Elena could have spent BCH directly—that’s the destination.
4. **Privacy through segmentation.** María and Carlos never interact; only the blockchain sees the full flow.
5. **Hyperinflation makes this urgent.** VES loses 5 %/week; BCH volatility is the lesser evil.
6. **Merchants earn on multiple levels.** Spread + foot traffic + product sales = triple‑dip.
7. **The system self‑organizes.** No coordinator. Passive bots + active queries = automatic matching.

When recipients spend BCH directly at merchants, Asgaya has succeeded.

---

**Related:** [The Mechanism](README.md), [Fund-Request Lifecycle](fund-request-lifecycle.md), [Bulletin Board](bulletin-board/), [Wallet](wallet/), [Nostr](nostr-coordination/), [Device Health](nostr-coordination/device-health.md), [Notification Bot](notification-bot/)
---

## Navigation

**[🏠 Home](../index.md)** | **[↑ The Mechanism](README.md)** | **[📖 Glossary](../glossary.md)**
