# Glossary

**Purpose:** Single source of truth for all Asgaya terminology.

---

## Core Concepts

### Asgaya Protocol
A permissionless peer-to-peer protocol built on Bitcoin Cash, delivered as a wallet designed to make remittances simple and affordable. Its mission is to promote merchant Bitcoin Cash adoption by turning every remittance into an opportunity for circular economy growth. It uses a decentralised bulletin board and covenant smart contracts to connect BCH sellers with senders and merchants with recipients, without any central server or custodian.

### Bulletin Board
The decentralised marketplace where BCH sellers and buyers discover each other. All listings (offers to sell or buy BCH) are stored on the Bitcoin Cash blockchain as NFT UTXOs. Anyone can query the bulletin board via Electrum servers; anyone can post a listing by broadcasting a transaction.

### Covenant
A Bitcoin Cash smart contract that locks BCH and releases it only when specific conditions are met. In Asgaya, covenants are used to coordinate multi‑party remittances: a seller funds a covenant after receiving fiat, and the BCH is released when the recipient (and optionally a merchant) co‑signs.

### Cash Account
A human‑readable name permanently registered on the BCH blockchain (e.g., `Elena#142`). Cash Accounts serve four simultaneous roles in Asgaya: user identifier, covenant recipient field, payment reference on bank transfers, and bot lookup key. They eliminate the need for phone numbers, email addresses, or long cryptographic addresses.

### Payment‑First Covenant
The architectural principle that a BCH seller never locks their BCH into a covenant until **after** their bank confirms receipt of the sender’s fiat payment. This eliminates the seller’s capital risk and avoids creating a custodial or money‑transmission relationship.

### Two‑Transaction Model
The compliance separation between the remittance (sender → recipient) and the subsequent commerce (recipient selling BCH to a merchant). The recipient owns the BCH after claiming it from the covenant, making the second transaction a voluntary peer‑to‑peer sale.

### Pull System
The design choice that lets the recipient decide when to claim a remittance, rather than having the sender push it automatically. The pull system compresses volatility exposure to the few seconds of the cash‑out event and creates merchant foot traffic.

### Volatility Buffer (7 %)
Extra BCH that a seller locks into a covenant (currently 7 % above face value) to protect the recipient and merchant from moderate price drops during the claim window. If BCH drops by more than 7 %, the covenant aborts and the BCH returns to the sender. The buffer is not a fee—it is the seller’s own capital that returns to them when the covenant settles normally.

### 3 % Early Warning
A notification sent to both sender and recipient when the BCH price drops by 3 % from the covenant’s starting price. It warns the recipient to claim soon before a potential 7 % abort. This is a Phase 0 hypothesis, not a guaranteed protection.

### Stability Layer
The set of mechanisms that allow merchants (and senders) to convert volatile BCH into value‑stable tokens (H€ or HAu). It uses pooled AnyHedge contracts and CashTokens to mint EUR‑pegged or gold‑pegged tokens.

### H€ (Hedged Euro)
A BCH‑native CashToken pegged to the Euro via pooled AnyHedge contracts. 1 H€ is always worth approximately €1. It can be minted at covenant lifecycle endpoints (abort or merchant cash‑out). Phase 0: founder‑provided long side; Phase 1+: open long‑side market.

### HAu (Hedged Gold)
A BCH‑native CashToken pegged to the price of gold (XAU) via pooled AnyHedge contracts. It serves as a universal stable value alternative, especially attractive to merchants who want to escape both BCH volatility and local‑currency depreciation.

### Bull Pool
The Phase 0 liquidity pool that takes the long (speculative) side of all AnyHedge contracts backing H€ and HAu tokens. Funded by the founder, it will be replaced in Phase 1+ by a crowdfunded, community‑operated long‑side pool.

### Triple‑Dip
The three revenue streams a merchant earns from a single remittance: (1) a 0.5 % spread on the cash‑out, (2) product margin on groceries the recipient buys during the visit, and (3) the BCH position itself, which can be stabilised into H€/HAu or recycled as a seller.

### Double‑Dip
When a participant operates simultaneously as a BCH seller and a BCH buyer, earning fees on both sides of the corridor. Common among dual‑citizens with bank accounts in both Spain and Venezuela.

### Onboarder
A local community member who recruits merchants, builds trust, and often becomes that merchant’s preferred BCH buyer. Onboarders are critical to the cold‑start strategy and may earn the right to become Phase 0 sellers or buyers.

---

## Roles

All roles collapse into two fundamental types: **BCH Seller** (has BCH, wants fiat) and **BCH Buyer** (has fiat, wants BCH). The traditional labels below are just context‑specific names for these two types.

### Sender
A BCH buyer who purchases BCH from a seller and sends it via covenant to a recipient. Typically someone sending remittances (e.g., María in Spain).

### Recipient
A BCH seller who receives BCH through a covenant and either keeps it or sells it to a local merchant for cash. Typically the family member receiving the remittance (e.g., Elena in Venezuela).

### Merchant
A BCH buyer whose payment method is “cash at a physical shop.” Merchants provide cash‑out services for recipients and earn spreads, product margins, and stability‑layer benefits.

### BCH Seller (Passive)
Someone who lists on the bulletin board as a seller, accepts fiat payments (via Bizum, SEPA, or cash), and uses the notification bot to automatically lock BCH into covenants. They earn a 0.5 % fee per transaction.

### BCH Buyer (Passive)
Someone who lists on the bulletin board as a buyer, provides fiat in exchange for BCH, and may stabilise received BCH into H€ or HAu. Merchants are the primary passive buyers.

### Trader
A professional liquidity provider who operates both as a passive seller and a passive buyer, often recycling the same BCH across multiple transactions. Traders earn fees on volume rather than capital size.

### Onboarder
(see Core Concepts)

### Pro Seller
A BCH seller who issues proper invoices for business clients, typically for larger freelance payments. Same architecture as a regular seller, but with professional documentation.

---

## The Five Gears (Components)

### Wallet
The non‑custodial BCH wallet where users hold BCH, register Cash Accounts, create covenants, and sign transactions. It also generates Nostr keys and serves as the identity foundation.

### Bulletin Board
(see Core Concepts)

### Nostr
The encrypted peer‑to‑peer messaging layer used to coordinate payment details between buyers and sellers. Messages are sent over public relays, but content is end‑to‑end encrypted (NIP‑04). Nostr also carries blacklist warnings and optional covenant‑funded notifications.

### Notification Bot
The automation engine running on a passive seller’s device. It intercepts bank/payment notifications, extracts Cash Account references, matches them to unfunded covenants via Electrum, and automatically locks the seller’s BCH into the covenant.

### Stability Layer
(see Core Concepts)

---

## Economics & Strategy

### Fee Split
The 1 % total system fee is split equally: 0.5 % goes to the BCH seller who provided the BCH, and 0.5 % goes to the merchant who provided cash‑out services. The protocol takes no fee.

### Capital Recycling
The ability of a passive seller to immediately reuse fiat received from a sender to replenish their BCH inventory (via exchange), while the original BCH is still locked in the covenant. Enables small capital to process high transaction volumes.

### Money Velocity
The number of times the same capital is turned over per day. High money velocity (enabled by automation and capital recycling) allows a small capital base to serve a large remittance market.

### Progressive Decentralisation
The phased approach from a curated Phase 0 (trusted sellers, coordinator‑vetted merchants) to a fully permissionless Phase 3 (bonded, reputation‑based, on‑chain enforcement). Decentralisation is delayed until the model is proven.

### Cold Start Strategy
The Phase 0 plan to bootstrap a two‑sided marketplace: recruit 150 senders and 5 merchants through personal networks, onboarders, and freelancers, and validate all economic and behavioural assumptions before scaling.

### Progressive Payment Rollout
The decision to support only cash‑in‑person and a few well‑documented digital payment methods (Bizum, SEPA) at launch, adding new methods only after they are thoroughly researched and tested by community pioneers.

### Pioneer Badge
A permanent reputation marker awarded to users who document a new payment method or corridor. Pioneers earn higher trust scores and are credited in the protocol’s release notes.

### Escape Hatch
The insight that Venezuelan merchants and recipients face a currency (VES) that is far more volatile than Bitcoin Cash. Holding BCH (or H€/HAu) is not a speculative gamble—it is a rational escape from guaranteed hyperinflation losses.

### Permissionless Accelerant
The mechanism by which permissionless access accelerates adoption: anyone can join, so the first merchant’s success is visible and competitors can replicate it without gatekeepers. Competitive pressure drives growth faster than any centralised onboarding programme.

---

## Bitcoin Cash / Technical

### Electrum Server
A lightweight Bitcoin Cash server that provides blockchain data (UTXOs, transaction history, covenant state) via a JSON‑RPC interface. Asgaya apps use public Electrum servers to query the bulletin board, monitor covenants, and verify on‑chain state.

### NFT UTXO
A Bitcoin Cash unspent transaction output that carries a non‑fungible token (CashTokens NFT). In Asgaya, bulletin board listings are NFT UTXOs whose commitment field contains the listing metadata (payment methods, limits, fee, contact info).

### OP_RETURN
A Bitcoin Cash script opcode that allows embedding arbitrary data (up to 223 bytes) in a transaction. Used for Cash Account registrations, payment‑info exchange (encrypted), and on‑chain fallback for Nostr messages.

### CashTokens
The native token standard on Bitcoin Cash, enabling both fungible tokens (like H€ and HAu) and non‑fungible tokens (like bulletin board listings and reputation credentials).

### AnyHedge
A mature (since 2020) Bitcoin Cash protocol for non‑custodial bilateral hedge contracts. Asgaya’s stability layer uses pooled AnyHedge contracts to back H€ and HAu tokens.

### MUSD
A BCH‑native overcollateralised stablecoin created by the Moria Protocol. MUSD was evaluated as a potential settlement asset but was found to be too immature for Phase 0 (it suffered a critical failure in April 2026).

### SPV (Simplified Payment Verification)
A method for lightweight clients to verify transactions without downloading the full blockchain. Asgaya’s mobile wallet can operate in SPV mode, though it primarily relies on Electrum servers for convenience.

### 0‑conf (Zero‑Confirmation)
Accepting a Bitcoin Cash transaction as valid before it is included in a block. Safe for the small amounts involved in Asgaya remittances due to BCH’s low double‑spend risk. Enables the sub‑minute settlement experience.

---

## Terms to Avoid

These terms have been deliberately replaced in the documentation to maintain regulatory clarity and architectural accuracy.

| ❌ Avoid | ✅ Use Instead | Reason |
|----------|---------------|--------|
| Escrow | Covenant | Escrow implies a central custodian holding funds |
| Liquidity Provider (LP) | BCH Seller or BCH Buyer | LP was a pre‑covenant role eliminated in May 2026 |
| Platform | Protocol or Bulletin Board | Asgaya is not a company‑run platform |
| Account | Cash Account or Wallet | There are no user accounts on a server |
| Order | Listing | No central order book exists |
| Trade | Transaction or Exchange | Avoids securities‑law connotations |
| Buy/Sell BCH | Trade BCH | Regulatory‑safe language for UI |
| Fee | Spread (for merchant), Fee (for seller) | Clarifies who charges what |
| Kraken | Exchange (generic) | Exchange dependency is not part of the protocol |

---

*Last updated: 2026‑06‑27 – reflects the covenant architecture, five‑gear model, and stability layer as of Phase 0.*
---

## Navigation

**[🏠 Home](index.md)**
