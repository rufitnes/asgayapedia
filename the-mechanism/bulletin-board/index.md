# The Bulletin Board: Where Buyers and Sellers Find Each Other

**The core innovation.** No company, no servers, no permission needed.

---

## What It Is

The Asgaya Bulletin Board is searchable data stored on the Bitcoin Cash blockchain. Not a website. Not a database. Just NFT tokens on the blockchain that say "I'm selling BCH for euros" or "I'm buying BCH with bolivares."

Anyone can post. Anyone can read. No one can censor it.

---

## Two Listing Types (Only Two)

Every participant is either a **BCH Seller** (has BCH, wants fiat) or a **BCH Buyer** (has fiat, wants BCH). Everything else is just payment method variations.

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
| **Storage** | Central database (company servers) | On-chain NFTs (blockchain) |
| **Discovery** | Website API (single point of failure) | Electrum queries (any node) |
| **Censorship** | Platform can ban you | Permissionless |
| **Trust** | Platform escrow (custody risk) | Covenants (non-custodial) |
| **Regulation** | Platform liable (KYC required) | No intermediary (MiCA compliant) |
| **Uptime** | Depends on company | Depends on BCH network (99.99%) |

There's no company to shut down. LocalBitcoins was ordered to close. The bulletin board is just data on the blockchain. No one can turn it off.

---

## How Discovery Works

### Posting a Listing

The app creates a Bitcoin Cash transaction with an NFT UTXO:

- **Category:** `ASGAYA_SELLER_V1` or `ASGAYA_BUYER_V1`
- **Commitment:** Listing details (payment methods, fee, contact)
- **Value:** 0.001 BCH (~€0.50) anti-spam deposit (reclaimed when you remove the listing)
- **Covenant:** Rules for updating or removing

Broadcast it. Confirmed in ~10 minutes. Now it's live.

### Finding a Listing

The app queries an Electrum node for UTXOs with the relevant NFT category, filtered by payment method and corridor. No API. No website. Just standard blockchain queries that any Bitcoin Cash wallet can do.

### Updating or Removing

Spend the old listing UTXO (reclaim your 0.001 BCH), create a new one with updated details. Old listing vanishes, new one appears. To pause, spend the UTXO and don't recreate it.

---

## Anti-Spam: An Unknown We're Testing

**Phase 0 baseline:** Every listing UTXO must contain ≥ 0.001 BCH (~€0.50). To spam with 1,000 fake listings costs €500. You reclaim the deposit when you remove the listing (minus ~€0.002 transaction fee).

**This is an unknown.** We don't know if this simple economic barrier is sufficient, or if we need additional measures like listing limits per account, device fingerprinting, or payment method verification. Phase 0 will test whether the 0.001 BCH deposit alone deters spam effectively.

**See:** [Bulletin Board Anti-Spam Strategies](../docs/unknowns/bulletin-board-anti-spam.md) for full analysis of alternatives and testing plan.

---

## Multi-Role Strategies: Playing Both Sides

Because listings are just NFTs, you can post as many as you want.

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

**Visible on-chain:** Listing type, payment methods, corridor, fee, contact info.  
**Not on-chain:** Real identity (unless you put it in the contact field), transaction volume, customer details.

Pseudonymous by default. Merchants reveal location because cash pickup requires it. Online traders need only a CashAccount nickname.

---

## Reputation (Phase 1+)

Phase 0 uses trusted participants only. Phase 1+ will add on-chain reputation linked to your CashAccount: transaction count, success rate, response time, total volume. Physical merchants get priority because location is reputation at stake—they can't disappear overnight.

---

## Key Takeaways

1. **On-chain bulletin board** — Listings are NFT UTXOs on the BCH blockchain; no central server.
2. **Two listing types** — BCH Sellers and BCH Buyers. That's it.
3. **Merchants are BCH buyers** — Payment method "cash" plus location. Nothing special.
4. **Permissionless** — Anyone can post, anyone can read. No gatekeeper.
5. **Multi-role earnings** — Post multiple listings to earn on both sides.
6. **Censorship-resistant** — No company to shut down. Just blockchain data.
7. **Works with covenants** — Bulletin board discovers; covenants execute.

The bulletin board isn't a product. It's infrastructure. When regulators shut down LocalBitcoins, users lost their marketplace. When they come for Asgaya, there's nothing to shut down. The listings are already on the blockchain.

That's the point.

---

**Next:** [Wallet](wallet.md) — Where you hold BCH and establish identity  
**Related:** [Buyers and Sellers](buyers-and-sellers.md) — The user perspective
