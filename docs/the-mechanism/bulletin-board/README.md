# The Bulletin Board: Where Buyers and Sellers Find Each Other
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**The core innovation.** No company, no servers, no permission needed.

> **✅ Updated (2026-09-21) — Phase-0 discovery is Nostr.** The "query all listings on-chain" step assumed below is **not supported** by our Electrum/Fulcrum server (no category enumeration). So Phase 0 finds listings via **Nostr (NIP-99 — signed events, cached locally)**. The on-chain anchor is **planned (Phase 0+)**: it makes listings censorship-resistant and enumerable on-chain, but is **not required** to discover them today.

---

## What It Is

The Asgaya Bulletin Board is open discovery data. Not a website. Not a database. In Phase 0 it is **signed Nostr listings** — anyone can post, anyone can read, no one needs permission. *(A censorship-resistant on-chain copy — the anchor — is planned for Phase 0+.)*

Anyone can post. Anyone can read. No one can censor it.

---

## Two Listing Types (Only Two)

Every participant is either a **BCH Seller** (has BCH, wants fiat) or a **BCH Buyer** (has an asset, wants BCH). Everything else — the asset, the payment rail — is a field.

**These two "types" are an internal label, not two classes.** On the board **every poster is a seller** — of BCH, or of some other asset — and only the **asset** changes; the label is *derived*, never chosen.

**The board is asset-agnostic: the asset is a free field.** Anyone can list *any* asset — a fiat (`EUR`, `VES`), a token (`H€`, `HAu`, `PUSD`), or even a non-monetary good (e.g. `rice`). Asgaya does **not** curate which assets are listed; that neutrality *is* the point, and the legal posture: no intermediary, nothing for us to control.

### BCH Sellers: "I Have BCH, I'll Lock It for Your Remittance"

A seller posts a listing with their accepted payment methods (Bizum, SEPA), fee (0.5%), and volatility buffer (7%). When a sender selects them and pays €100 via Bizum, the seller's bot detects the payment and locks €107 worth of BCH into the covenant. The seller earns 0.5% (€0.50) plus any unused buffer if BCH appreciates during the window.

**Guarantee:** The seller never locks BCH until their bank confirms the fiat payment. No capital risk.

### BCH Buyers: "I Have Fiat, I'll Buy Your BCH"

A buyer posts a listing with their payment method. For a merchant, that method is "cash at my shop" plus a location. For an online buyer, it's "bank transfer" or "PagoMóvil."

**Merchants are BCH buyers.** Nothing more. A merchant is just a buyer whose payment method is cash and who has a physical address. The system doesn't need a separate "merchant" category.

When a recipient visits the merchant, they hand over the claim, receive cash, and both sign. The merchant earns 0.5% in BCH. The recipient is now in the shop—likely to buy groceries. The remittance fee is the door opener.

---

## Why This Design Beats Centralized P2P Markets

| Feature | LocalBitcoins / Binance P2P | Asgaya Bulletin Board |
|---------|----------------------------|----------------------|
| **Storage** | Central database (company servers) | Signed Nostr listings — no server; on-chain anchor (Phase 0+) |
| **Discovery** | Website API (single point of failure) | Any client reads the same open listings (any relay/index) |
| **Censorship** | Platform can ban you | Permissionless |
| **Trust** | Platform escrow (custody risk) | Covenants (non-custodial) |
| **Regulation** | Platform liable (KYC required) | No intermediary (MiCA compliant) |
| **Uptime** | Depends on company | Depends on BCH network (99.99%) |

There's no company to shut down. LocalBitcoins was ordered to close. The bulletin board is just open data — no one can turn it off.

---

## How Discovery Works

### Posting a Listing

The app publishes a **signed Nostr event** (NIP-99, `kind:30402`) — an addressable listing carrying the offer (ad type, asset, payment methods, fee, buffer, limits, location) and the poster's BCH pubkey + `npub`. It is **replaceable**: republishing with the same id updates it.

No transaction, no fee — live in seconds.

### Finding a Listing

Each device keeps a **local cache** of the listings it sees on its relays (one subscription, filtered by the `asgaya-board` tag), then **filters and ranks client-side**. No API, no website — any client can read the same open events.

### Updating or Removing

Republish the same listing id with new details → relays replace the old version. Set the listing's status to *paused* to hide it; delete to remove it.

> **On-chain anchor (planned, Phase 0+):** the *discovery-critical* fields (identity, asset, payment method) will also live in a per-seller BCH NFT, so a listing survives relay censorship and can one day be enumerated directly on-chain. Until then, **Nostr is the Phase-0 discovery layer**.

---

## Anti-Spam: An Unknown We're Testing

**Phase 0 (Nostr):** listings are free to publish, so spam is handled **client-side** (validity checks + pruning) and by relay policy. The **on-chain 0.001 BCH deposit** (~€0.50) returns with the **Phase-0+ anchor**, where faking a listing at scale gets expensive.

**This is an unknown.** We don't yet know whether the deposit (or relay policy / listing limits) is enough. Phase 0/0+ will test it.

**See:** [Bulletin Board Anti-Spam Strategies](../../unknowns/bulletin-board-anti-spam.md) for full analysis of alternatives and testing plan.

---

## Multi-Role Strategies: Playing Both Sides

Because listings are just signed events, you can post as many as you want.

### Double-Dip (Seller + Buyer)

You have bank accounts in Spain and Venezuela. Post as a BCH Seller (accepting Bizum) and as a BCH Buyer (paying via PagoMóvil). Receive €100 from a sender, lock BCH into a covenant. The merchant in Venezuela receives the BCH. Buy that same BCH back from the merchant via PagoMóvil. Your BCH returns to your wallet. Net: seller fee + buyer spread on the same capital.

### Triple-Dip (Merchant + Seller + Product Margin)

You're a merchant in Venezuela with family in Spain. Post as a BCH Buyer (cash at shop) and as a BCH Seller (via family's Bizum). A sender pays your family €100. You receive the BCH at your shop, hand cash to the recipient, and they buy €20 of groceries. Recycle the BCH through your family's seller listing. Net: merchant fee + seller fee + grocery margin. The remittance is the door opener.

### International Business (Multi-Currency)

You receive income in multiple currencies. Post as a BCH Buyer accepting all your incoming payment rails (Bizum, MB Way, SEPA, PayPal). Post as a BCH Seller paying out in your target currencies (WeChat, Alipay). Earn a small spread on each conversion.

---

## How It Connects to Covenants

**Bulletin board = discovery. Covenants = execution.**

**Seller flow:** Sender finds a seller on the bulletin board → creates a covenant → pays the seller via Bizum → seller's bot detects payment and locks BCH → covenant releases BCH to the recipient.

**Buyer flow:** Recipient finds a buyer (merchant) on the bulletin board → creates a covenant → visits the merchant and receives cash → both sign → covenant releases BCH to the merchant.

**Key:** The sender (or recipient) creates the covenant, not the seller or merchant. The counterparty co-signs after confirming payment or handing over cash.

---

## Privacy

**Published (Phase 0, on Nostr):** listing type, asset, payment methods, fee, and the poster's BCH pubkey + `npub`.  
**Not published:** real identity (unless you put it in the listing), transaction volume, customer details.

Pseudonymous by default. Merchants reveal location because cash pickup requires it. Online traders need only a nickname.

---

## Reputation

Reputation is **derived from the identity's settlement history** on-chain — the **settled covenants** that touch the identity address: number of trades, completion, average value, recency. It is a **continuity** signal (not self-reported truth). Phase 0 runs on **trusted participants** (weak signal); the full model and **local ranking** arrive with the anchor (Phase 0+). Physical merchants get priority because their location is reputation at stake—they can't disappear overnight.

---

## Key Takeaways

1. **Permissionless bulletin board** — Listings are signed **Nostr** events (NIP‑99); no central server. *(On-chain anchor planned, Phase 0+.)*
2. **Two listing types** — BCH Sellers and BCH Buyers. That's it.
3. **Merchants are BCH buyers** — Payment method "cash" plus location. Nothing special.
4. **Permissionless** — Anyone can post, anyone can read. No gatekeeper.
5. **Multi-role earnings** — Post multiple listings to earn on both sides.
6. **Censorship-resistant** — No company to shut down. Just open data.
7. **Works with covenants** — Bulletin board discovers; covenants execute.

The bulletin board isn't a product. It's infrastructure. When regulators shut down LocalBitcoins, users lost their marketplace. When they come for Asgaya, there's nothing to shut down. The listings are already published (Nostr today; a censorship-resistant on-chain anchor is planned).

That's the point.
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ The Mechanism](../README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Wallet](../wallet/README.md) · [Nostr](../nostr-coordination/README.md) · [Notification Bot](../notification-bot/README.md) · [Stability Layer](../stability-layer/README.md)
