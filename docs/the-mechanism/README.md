# Asgaya: A Bitcoin Cash Adoption Engine
**📖 Unfamiliar terms?** See the [glossary](../glossary.md) for definitions.

**Send €100 to Venezuela for €0.50 instead of €5.**
No company. No custody. No KYC. Just five components working together.

---

## Mission: Make Ourselves Unnecessary

Asgaya is a cheap, user‑friendly fiat on/off‑ramp for people and businesses
underserved by both banks (5‑8% remittance fees) and crypto exchanges (complex
UX, KYC, unavailable in many countries).

But the real goal is **Bitcoin Cash merchant adoption**. Every remittance creates
demand for BCH‑accepting merchants, rewards them with a 0.5% spread plus product 
margin (~30% on the sale of products and services during the cash-out), and offers 
an escape hatch from hyperinflation. When merchants accept BCH directly, Asgaya 
becomes unnecessary. **Success means we disappear.**

---

## The Five Gears

| Gear | What it does |
|------|--------------|
| ⚙️ **Wallet** | Where users hold BCH and establish identity (Cash Accounts replace phone numbers) |
| ⚙️ **Bulletin Board** | Where buyers and sellers find each other (NFTs on the BCH blockchain — anyone can read) |
| ⚙️ **Nostr** | How they coordinate payment details (encrypted P2P messaging) |
| ⚙️ **Notification Bot** | How it runs automatically (the killer feature — set and forget) |
| ⚙️ **Stability Layer** | How merchants protect against volatility (H€/HAu tokens backed by AnyHedge) |

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

## The Merchant Problem: Post‑Cashout Volatility

**After Elena cashes out at a merchant, that merchant holds BCH.** If they need to convert to VES for rent in two weeks, they're exposed to ±20% monthly volatility. Lose 15% once, they quit.

**Solution:** Optional stability tokens backed by pooled AnyHedge contracts.

### Two Stability Options

| Token | Pegged To | Why Merchants Choose It |
|-------|-----------|------------------------|
| **H€** (Heuro) | Euro | Familiar unit, easy mental math, quick conversion to VES |
| **HAu** (How) | Gold (XAU) | Maximum protection, hedges ALL fiat inflation (EUR + VES), universal value |

**Mechanism:**
1. Merchant receives BCH from covenant cashout
2. Wallet asks: "Stabilize as H€, HAu, or keep BCH?"
3. If H€/HAu chosen and pool has capacity:
   - Creates standard AnyHedge contract (merchant shorts BCH vs asset)
   - Bull pool provides long side (leveraged BCH exposure)
   - Mints stability tokens (30‑day auto‑renewing)
4. Merchant holds stable value, trades it for VES when needed

**If pool exhausted:** Falls back to BCH delivery. Existing tokens still tradeable. System degrades gracefully.

**Why gold oracle is best:** 24/7 global trading (LBMA, CME, COMEX), centuries of price history, $12T market cap (manipulation‑resistant). More reliable than any fiat/BCH pair.

**Phase 0 capital:** €3K unified pool (approved assets: H€, HAu). High merchant velocity (weekly VES conversion) means low capital lock. Supports ~80 merchants initially.

**The hook:** Merchants come for remittances (0.5% spread). Stay for stability (hedge hyperinflation without trusting banks).

---

## Why Bitcoin Cash?

Low fees (~€0.002/tx), fast settlement, native covenant support (CashTokens),
and a permissionless, global settlement layer. We chose BCH because it earned our
respect.

---

## Status: Phase 0 (Pre‑Launch)

**Launch corridor:** Spain → Venezuela (EUR → VES) — Q3 2026.  
**Current:** Documentation in progress; stability tokens (H€/HAu) design validated.  
**Validating:** Does the 7% buffer hold? Will merchants adopt stability tokens? Can bull pool scale?

**Explore the gears:**
- [Wallet](wallet/README.md) - Your BCH, your identity
- [Bulletin Board](bulletin-board/README.md) - Discover buyers and sellers  
- [Nostr Coordination](nostr-coordination/README.md) - Private payment coordination
- [Notification Bot](notification-bot/README.md) - Automation engine
- [Stability Layer](stability-layer/README.md) - H€/HAu volatility protection
- [How They Interact](how-they-interact.md) - The complete picture

**Deeper dive:** [Why This Design?](../why-this-design/README.md) - Design rationale and constraints

---

**Authors:** Suso + Claude Sonnet 4.5 (Coordination) + TightDS (DeepSeek)  
**Updated:** 2026‑06‑28
