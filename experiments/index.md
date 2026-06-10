# Asgaya: A Bitcoin Cash Adoption Engine

**Send €100 to Venezuela for €0.50 instead of €5.**
No company. No custody. No KYC. Just four components working together.

---

## Mission: Make Ourselves Unnecessary

Asgaya is a cheap, user‑friendly fiat on/off‑ramp for people and businesses
underserved by both banks (5‑8% remittance fees) and crypto exchanges (complex
UX, KYC, unavailable in many countries).

But the real goal is **Bitcoin Cash merchant adoption**. Every remittance creates
demand for BCH‑accepting merchants, rewards them with a 0.5% spread, and offers
an escape hatch from hyperinflation. When merchants accept BCH directly, Asgaya
becomes unnecessary. **Success means we disappear.**

---

## The Four Gears

| Gear | What it does |
|------|--------------|
| ⚙️ **Wallet** | Where users hold BCH and establish identity (Cash Accounts replace phone numbers) |
| ⚙️ **Bulletin Board** | Where buyers and sellers find each other (NFTs on the BCH blockchain — anyone can read) |
| ⚙️ **Nostr** | How they coordinate payment details (encrypted P2P messaging) |
| ⚙️ **Notification Bot** | How it runs automatically (the killer feature — set and forget) |

---

## A Complete Transaction (€100 Spain → Venezuela)

1. **Wallet:** María opens her wallet, enters Elena’s Cash Account (`Elena#142`).
2. **Covenant:** María creates a covenant (smart contract) specifying the terms.
3. **Bulletin Board:** The app queries the blockchain and finds a seller accepting Bizum.
4. **Nostr:** María selects a seller and receives the payment instructions.
5. **Notification Bot:** The seller’s bot detects María’s Bizum payment and funds the covenant.
6. **Recipient:** Elena gets a notification and can claim BCH to her wallet or cash it out at a local merchant.

**Cost:** 0.5% (€0.50) vs 5% (€5) via Western Union.  
**Time:** 5 minutes–4 hours vs 1–2 days.  
**Trust:** The seller never locks BCH until paid. The sender has legal recourse if the seller ghosts.

---

## There Are Only Buyers and Sellers

| Role | What They Really Are |
|------|---------------------|
| Sender (María) | BCH buyer (buys from seller to send) |
| Recipient (Elena) | BCH seller (sells to merchant for cash) |
| Merchant | BCH buyer (buys from recipient) |
| Trader | Both (buys and sells for arbitrage) |

**The entire system is a bulletin board where buyers and sellers discover each other
and execute trades via covenants.**

---

## Why It Works

**Payment‑First Covenants.** The seller’s bank notification acts as a notary; BCH moves
only after payment is confirmed. Fraud is criminal, and personal payment information
is traceable.

**Volatility Buffer.** Seller locks 107% (€107 for €100 trade). The 7% buffer absorbs
typical daily swings. Unused buffer returns to the seller.

**On‑Chain Bulletin Board.** Listings are NFTs on the BCH blockchain. No central server
to shut down. Anyone can read, anyone can post.

**Notification Bot.** Once the seller posts a listing, the bot watches the blockchain
and bank notifications. No manual intervention needed — scale without effort.

---

## Why Bitcoin Cash?

Low fees (~€0.002/tx), fast settlement, native covenant support (CashTokens),
and a permissionless, global settlement layer. We chose BCH because it earned our
respect.

---

## Status: Phase 0 (Pre‑Launch)

**Launch corridor:** Spain → Venezuela (EUR → VES) — Q3 2026.  
**Current:** Documentation complete; Android app in development.  
**Validating:** Does the 7% buffer hold? Will merchants provide liquidity? Can bots handle volume?

**Start here:** [Bulletin Board](bulletin-board.md) — the core innovation.  
**Then:** [Buyers and Sellers](buyers-and-sellers.md) — the user perspective.  
**Deeper rationale:** [Why This Design](why-this-design/) — requirements, evidence, open questions.

---

**Authors:** Suso + Claude Sonnet 4.5 + DeepSeek  
**Updated:** 2026‑06‑05 · **Experimental structure — feedback welcomed**
