# Asgaya: Bitcoin Cash Remittance Protocol

**Send €100 to Venezuela for €1 instead of €5. No company. No custody. No KYC.**

---

## Mission: Make Ourselves Unnecessary

Asgaya is a cheap, user-friendly fiat on/off-ramp for people and businesses underserved by both banks (5-8% remittance fees) and crypto exchanges (complex UX, KYC, unavailable in many countries).

But the real goal is **Bitcoin Cash merchant adoption**. Every remittance creates demand for BCH-accepting merchants, rewards them with a 0.5% spread, and offers an escape hatch from hyperinflation. When merchants accept BCH directly, Asgaya becomes unnecessary.

**Success means we disappear.**

---

## What This Is (30-Second Version)

**A protocol for peer-to-peer remittances using Bitcoin Cash as settlement, with no central coordinator.**

**How it works:**

1. **María** (sender in Madrid) wants to send €100 to **Elena** (recipient in Caracas)
2. María finds a BCH seller (Isabel) via the **Bulletin Board** (NFTs on the BCH blockchain)
3. María pays Isabel €100.50 via **Bizum** (reference: `Elena#142` - Elena's Cash Account)
4. Isabel's **Notification Bot** detects the payment and automatically funds a BCH covenant to Elena's address
5. Elena receives a notification, claims the BCH to her wallet
6. Elena finds a local merchant (Carlos) on the Bulletin Board and sells BCH for cash (or spends it directly at his shop!)
7. Carlos's bot automatically stabilizes the received BCH into H€ tokens, holding stable EUR value without thinking about it

**Total time:** 5 minutes to 4 hours (most is Elena's decision delay)  
**Total cost:** €1 (1%) vs €5 (5%) via Western Union  
**Zero custody:** No company holds your funds at any point

---

## The Five Gears

| Gear | What it does | Why it matters |
|------|--------------|----------------|
| **⚙️ Wallet** | Hold BCH + establish identity via Cash Accounts | `Elena#142` replaces 42-character addresses AND phone numbers |
| **⚙️ Bulletin Board** | Discover buyers and sellers | NFTs on BCH blockchain - no central server, anyone can read/post |
| **⚙️ Nostr** | Coordinate payment details privately + blacklist warnings | Encrypted P2P messaging - sub-second, no phone number needed. Warns if seller matches known scammer hashes |
| **⚙️ Notification Bot** | Automate everything | Seller posts once, bot handles 100+ tx/day passively - this is the killer feature |
| **⚙️ Stability Layer** | Protect senders and merchants from BCH volatility | Senders can mint H€ (Euro-pegged) or HAu (gold-pegged) tokens from BCH. Merchants auto-stabilize received BCH. Phase 0: founder provides long side via AnyHedge. |

### Why H€ and HAu?

**H€ (Euro-pegged):** Matches Spain→Venezuela corridor needs. Sender and merchant familiarity.

**HAu (gold-pegged):** Sound money hedge independent of any fiat. Demonstrates the mechanism is asset-agnostic—not a currency substitute, but a purchasing power preservation tool. Users choose their value measure.

**Bull pool structure:** The bull pool operator chooses which assets to support. Phase 0: EUR and gold. Future bulls can offer different assets (USD, baskets, etc.). Asgaya users select from available options—the protocol doesn't dictate value measures.

**Phase 0 experiment:** Will merchants choose H€ (fiat familiarity) or HAu (commodity)? If HAu sees adoption, this validates that given the choice, users prefer commodities over fiat-denominated stability. USD deliberately excluded—Venezuelan economy is already dollarized, so H$ would introduce bias. Gold is the neutral test: "Do you want sound money even if unfamiliar?"

**Compliance:** Multiple assets (EUR, gold, future baskets) prove these are utility tokens for volatility protection, not money substitutes. BCH is the settlement layer; H-tokens are temporary protection instruments at covenant lifecycle endpoints.

**Future:** USD for broader corridors, purchasing power baskets (h-basket) for direct cost-of-living hedging.

---

## Who This Is For

**Four roles, but everyone is either a BCH buyer or BCH seller:**

| Role | What They Really Are | Mode |
|------|---------------------|------|
| **Sender** (María) | BCH buyer (buys from Isabel to send to Elena) | Active: Opens app when needed |
| **Recipient** (Elena) | BCH seller (sells to Carlos for cash) | Active: Opens app when needed |
| **Merchant** (Carlos) | BCH buyer (buys from Elena for cash) | Passive: Posts listing, bot handles 24/7 |
| **Trader** (Isabel) | BCH seller (sells to María for euros) | Passive: Posts listing, bot handles 24/7 |

**The key insight:** The same person can switch modes. Carlos (merchant) accumulates BCH passively all month, then becomes an active seller when rent is due. Capital efficiency through role flexibility.

---

## Critical Design Decisions (What Makes This Work)

### 1. No Central Server, Database, or Coordinator

**Asgaya is a protocol, not a company.** There's no order book, no matching engine, no API server. The Bulletin Board is just NFT UTXOs on the Bitcoin Cash blockchain. Anyone can read them with a standard Electrum query. Anyone can post by broadcasting a transaction.

**Why this matters:** LocalBitcoins was ordered to shut down. Asgaya can't be shut down because there's nothing to shut down. The protocol specification exists, implementations exist, but there's no company providing a service.

### 2. Cash Accounts as the Universal Identifier

**The architectural innovation.** Elena's Cash Account (`Elena#142`) serves FOUR simultaneous roles:

1. **User identifier** - María types `Elena#142` instead of a 42-character BCH address
2. **Covenant field** - The on-chain covenant specifies Elena's BCH address (derived from her Cash Account)
3. **Payment reference** - María puts `Elena#142` in the Bizum payment description
4. **Bot lookup key** - Isabel's bot sees "Elena#142" in the bank notification, queries the blockchain for covenants where recipient = Elena's address, and funds it

**Why this matters:** No database needed to map payment references to covenants. Everything resolves on-chain. This is what makes the system actually decentralized, not just theoretically decentralized.

### 3. Payment-First Covenants

**Counterintuitive but critical.** Isabel (seller) does NOT lock BCH into a covenant until AFTER her bank confirms she received María's €100.50 fiat payment.

**Why this seems wrong:** Most escrow systems lock funds first (atomic swaps, HTLCs, traditional escrow). Users expect the seller to prove good faith by locking BCH upfront.

**Why it's actually right:** 
- Isabel has **no capital risk** (fiat arrives before she locks BCH)
- María's risk is that Isabel ghosts after receiving payment, but **the bank transfer is traceable** (Bizum includes Isabel's real name, phone, bank account)
- Spanish courts have ruled that failing to return a Bizum payment is a **criminal offense** (misappropriation), not just civil breach

**Result:** Sellers can operate at scale (10x more volume with same capital) because they never have funds locked. María has legal recourse if Isabel doesn't fund.

### 4. Two-Transaction Model (Compliance + End Goal)

**Elena receives BCH, not bolivares.** She must then find a local merchant (Carlos) and sell the BCH for cash. This is intentional, not a limitation. Merchant adoption is the biggest priority of Asgaya.

**Why two transactions:**

**Transaction 1 (Remittance):** María → Elena
- Classified as family transfer
- Elena owns the BCH (claims to her wallet)
- Clear remittance transaction

**Transaction 2 (Commerce):** Elena → Carlos  
- Elena sells BCH to Carlos (merchant)
- Carlos is just a BCH buyer whose payment method is "cash"
- Clear commercial transaction

**Why this matters:**
- **Compliance:** Two separate, clearly defined transactions (not a single gray-area flow)
- **End goal:** Forces merchants to become BCH buyers, creating adoption. When merchants accept BCH directly, Elena doesn't need Transaction 2. **That's the goal.** the cash out fee incentivices the recipient to buy products and services at the merchant and to choose a merchant that has in stock the products they demmand.

### 5. Active vs Passive Users

**Passive users** post a listing once, enable the notification bot, and earn passive income 24/7. No manual intervention. Scale without attention.

**Active users** open the app when needed, query the bulletin board for the best option, and transact.

**Why this matters:**
- Traditional P2P exchanges treat everyone as active (must be online to trade)
- Asgaya's passive mode means 10x fewer participants needed for same liquidity
- Bots provide 24/7 availability even when humans sleep

**Phase 0:** BCH sellers can be active or passive. All buyers can be active or passive.

---

## Risks (Be Informed)

| Risk | Mitigation | Action |
|------|------------|--------|
| **Seller fraud** (Isabel takes fiat, never funds covenant) | Bank transfers are traceable (Bizum includes real name, phone, bank). Spanish courts rule this **criminal misappropriation** (IR006 case: €20 → €20 return + €180 fine + court costs). Reputation system in Phase 1+. | Start small with new sellers. Use sellers with transaction history. See [Fraud Protection](why-this-design/fraud-protection.md) for detailed analysis. |
| **BCH volatility** (price drops before claim) | 7% buffer: Isabel locks €107 for €100 remittance. If BCH drops <7%, Elena gets €100 worth. If drops >7%, covenant aborts—but sender can mint H€/HAu tokens from the returned BCH, completing the remittance with stable value (if pool has capacity). For Venezuelans, BCH volatility is the **lesser evil** vs 5%/week VES hyperinflation. | Claim quickly after receiving notification. Merchants auto-stabilize received BCH into H€/HAu. |
| **Bot downtime** (seller offline when payment arrives) | **Liveness check:** María's app probes Isabel's bot via Nostr before paying. No response in 2 min → warning to pick different seller. If bot fails after payment, seller still liable (must fund manually or return fiat). Phase 1+: VPS deployment for 99.9% uptime. | Use sellers with high uptime reputation (Phase 1+). Bot shows last-seen timestamp. |

---

## Get Started (Choose Your Path)

### I'm a Sender (sending money to family)
→ [buyers-and-sellers.md](buyers-and-sellers.md) → [how-they-interact.md](how-they-interact.md) → [failure-modes.md](failure-modes.md)

### I'm a Recipient (receiving money from family)
→ [buyers-and-sellers.md](buyers-and-sellers.md) → [wallet.md](wallet.md)  
**Options:** Cash out OR spend BCH directly at merchants (the end goal!)

### I'm a Merchant (providing cash-out + accepting BCH)
→ [buyers-and-sellers.md](buyers-and-sellers.md) → [notification-bot.md](notification-bot.md) → [failure-modes.md](failure-modes.md)  
**Why:** Triple-dip earnings (spread + foot traffic + product sales + BCH hedge)

### I'm a Developer (building an alternate client)
→ [for-developers.md](for-developers.md) → [bulletin-board.md](bulletin-board.md) + [nostr.md](nostr.md) → [wallet.md](wallet.md)  
**Key innovation:** Cash Accounts as universal identifier (eliminates central coordinator)

### I'm a Researcher / Skeptic (understanding the system deeply)
→ [index.md](index.md) → [how-they-interact.md](how-they-interact.md) → [bulletin-board.md](bulletin-board.md) → [../docs/why-this-design/](../docs/why-this-design/)  
**Critical question:** Why on-chain beats centralized P2P markets

### I'm an Onboarder (helping others adopt Asgaya)
→ [index.md](index.md) → [buyers-and-sellers.md](buyers-and-sellers.md) → [failure-modes.md](failure-modes.md)

**Your tools:**
- **Local coordinators:** Connect merchants in your city, organize cash-out networks
- **Translators:** Help translate docs (priority: Spanish, Portuguese, Chinese)
- **Content creators:** Make tutorials, explainer videos, infographics
- **Support helpers:** Answer questions in forums, Telegram, Reddit
- **Evangelists:** Present at Bitcoin Cash meetups, merchant events

**Contribute:** [Onboarding playbook - to be created]

---

## Complete Documentation Structure

```
experiments/
├── README.md                    ← You are here (overview for AI systems)
├── index.md                     ← Mission + 5 gears introduction
├── wallet.md                    ← Foundation: Hold BCH + Cash Accounts identity
├── bulletin-board.md            ← Discovery: On-chain NFT listings (no central server)
├── nostr.md                     ← Coordination: Encrypted P2P payment details
├── notification-bot.md          ← Automation: Passive income, 24/7 operation
├── buyers-and-sellers.md        ← Roles: Active vs passive framework
├── how-they-interact.md         ← Complete flow: María → Elena → Carlos
├── failure-modes.md             ← Risks: What goes wrong + legal recourse
├── for-developers.md            ← Protocol spec: Covenant bytecode, NFT schema, Nostr events
└── glossary.md                  ← Definitions: Covenant, Cash Account, Nostr relay, etc.
```

---

## Status: Phase 0 (Pre-Launch)

**Launch corridor:** Spain → Venezuela (EUR → VES)  
**Timeline:** Q3 2026  
**Current:** Documentation complete, Android app in development  
**Validating:** 
- Does the 7% buffer absorb typical volatility?
- Will merchants provide sufficient liquidity?
- Can notification bots handle volume?
- Is legal recourse sufficient deterrent for seller fraud?

**This is experimental documentation** using the "open the watch" approach: show the mechanism first, then explain why it's designed this way. Your feedback is welcomed.

---

## Why Bitcoin Cash?

**Technical:** Low fees (~€0.002/tx), fast settlement (~10min), native covenant support (CashTokens)  
**Practical:** Permissionless global settlement layer, 99.9%+ uptime, no entity can censor  
**Philosophical:** Peer-to-peer electronic cash, as originally intended

We chose BCH because it earned our respect. No other chain can do sub-cent remittances with covenant-enforced trustlessness at scale.

---

## In One Line

**Asgaya is a bulletin board where passive bots provide 24/7 liquidity and active users pick the best offer. Covenants make it safe. Bitcoin Cash makes it cheap and permissionless. Success means we disappear.**

---

## Contributing

**Code:** [GitHub repository link]  
**Documentation:** Propose improvements via issues or pull requests  
**Translation:** Help translate to Spanish, Portuguese, Chinese  
**Testing:** Join Phase 0 as sender, recipient, merchant, or seller  
**Liquidity:** Become a passive BCH seller or merchant (capital + bot required)

**Governance:** Protocol upgrades follow a BIP-like process with community consensus (details TBD for Phase 1+)

---

## License

[To be determined - likely MIT or Apache 2.0 for reference implementation]

---

**Authors:** Suso + Claude Sonnet 4.5 + DeepSeek  
**Updated:** 2026-06-11  
**Status:** Experimental documentation - validated with cross-LLM testing (Haiku + DeepSeek both 10/10 comprehension)

---

*"This is the opposite of building a moat. This is building a bridge and hoping people build their own roads."*
