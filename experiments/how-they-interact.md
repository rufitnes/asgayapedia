# How They Interact: A Complete End‑to‑End Flow

**One transaction. Four gears.** Wallet, Bulletin Board, Nostr, Notification Bot—working together.

---

## The Transaction

**María** (Madrid) sends €100 to **Elena** (Caracas).  
**Isabel** (Barcelona) provides BCH passively. **Carlos** (Maracaibo) cashes Elena out.

**Total elapsed:** ~4 h (most is Elena’s decision delay)  
**Total cost:** €0.90 (0.4 % seller + 0.5 % merchant) vs. €5 (Western Union)

---

## Part 1: María Sends (Active Sender)

### Step 1 — Wallet & Bulletin Board

María opens the app, enters `Elena#142`, specifies €100. The app resolves Elena’s Cash Account on‑chain and creates a covenant template (€100 worth of BCH, 7 % buffer, 24‑h timeout).

The app then queries the bulletin board for BCH sellers accepting Bizum in the EUR→VES corridor. It finds three passive sellers. María chooses the cheapest: Isabel (0.4 %, good rating, Bizum).

### Step 2 — Nostr & Bot

María’s app sends an encrypted Nostr request (“Need payment details for covenant xyz789”) to Isabel’s bot. The bot validates the covenant, generates a unique reference (`Elena#142`), and replies with the bank details (Bizum phone number, concept, amount €100.40) in under a second.

María sees the payment instructions, taps “Open Bizum,” and pays €100.40 instantly.

### Step 3 — Bot Funds Covenant

Isabel’s Android notification listener detects the BBVA message (“Recibido 100,40 € … Concepto: Elena#142”), parses it, and matches it to the pending covenant. The bot creates a funding transaction locking €107 of Isabel’s BCH, signs it with limited covenant keys, and broadcasts it. Total processing time: ~2 seconds. Isabel is at work, completely unaware.

María’s app confirms the covenant is funded. Elena receives a push notification: “María sent you €100 in BCH.”

---

## Part 2: Elena Receives (Active Recipient)

### Step 4 — Claim BCH to Wallet

Elena’s phone buzzed four hours ago; she now opens Asgaya and taps “Claim BCH to Wallet.” The covenant releases the BCH to Elena’s address; the unused buffer returns to Isabel. **Elena now owns the BCH.**

### Step 5 — Decide & Discover

Elena chooses “Cash Out” (she needs bolívars today). The app queries the bulletin board for BCH buyers near Caracas. It finds Carlos’s Grocery (0.8 km, 0.5 % spread, open). Elena selects it.

### Step 6 — Walk, Exchange, Shop

Elena walks to the store. Carlos’s bot has already detected her sell covenant and pre‑filled a cash‑out screen. Carlos counts 398,000 VES (400k VES less 0.5 %), hands it to Elena. Both sign on their phones. The covenant executes, sending Elena’s BCH to Carlos.

Elena then buys groceries worth 300,000 VES from Carlos’s store.

**Carlos’s triple‑dip from this one visit:**
- 2,000 VES spread
- ~60,000 VES product margin (20 % on 300k)
- 0.2564 BCH position (hedge against VES depreciation)

---

## The Four Gears at Work

- **Wallet:** María creates covenant, Elena claims, Carlos receives. Cash Accounts provide identity.
- **Bulletin Board:** María queries sellers; Elena queries merchants. Passive listings discovered on‑chain.
- **Nostr:** María’s app requests payment details from Isabel’s bot. Encrypted, sub‑second, no phone number.
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

- **Seller offline:** Nostr request times out after 2 min; María picks another seller.
- **BCH drops >7 %:** Covenant aborts; María gets the BCH back (refund). Isabel keeps the fiat and the fee.
- **Elena never claims:** Covenant expires; María gets the BCH back.

---

## Cost Breakdown

| Party | Out | In | Net |
|-------|-----|-----|-----|
| María | €100.40 | (remittance delivered) | –€100.40 |
| Isabel | €107 BCH locked | €100.40 fiat + €0.40 fee | +€0.40 |
| Elena | 2k VES spread | €100 worth of BCH → 398k VES | +€99.50 equiv |
| Carlos | 398k VES cash | 0.2564 BCH + 2k spread + 60k sales | +€12.4 equiv |

**Total fee:** 0.9 % vs. Western Union’s 5 %.  
If Elena had spent BCH directly at the store (Option B), total fee would have been **0.4 %** (Isabel’s seller fee only).

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

**Related:** [Index](index.md), [Buyers and Sellers](buyers-and-sellers.md), [Bulletin Board](bulletin-board.md), [Wallet](wallet.md), [Nostr](nostr.md), [Notification Bot](notification-bot.md)
