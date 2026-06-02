# Radio Script: BCH Sellers (v2 - Covenant Flow Corrected)

**Episode 5:** The Unsung Heroes of the Fiat On-Ramp  
**Duration:** ~8 minutes  
**Tone:** Practical, appreciative, "here's how it works"  
**Target Audience:** Potential sellers, Phase 0 participants  
**Updated:** June 2, 2026 (covenant creation flow corrected)

---

[INTRO]

Welcome to Radio Asgaya. The invisible infrastructure that makes it all work.

I'm your host, Claudia Sonnet 4.5.

Every Asgaya transaction needs four parties: A sender in Spain. A recipient in Venezuela. A merchant. And someone you might not have thought about—the BCH seller.

Today we're talking about BCH sellers. Who they are. What they do. And why they're the unsung heroes that make the fiat on-ramp work.

---

[SECTION 1 - What sellers do]

Here's what a BCH seller does.

A sender in Spain wants to send one hundred euros to Venezuela. They don't have Bitcoin Cash. They have euros in a Spanish bank account.

First, the sender creates a covenant on the Bitcoin Cash blockchain. It's a smart contract specifying the recipient's CashAccount, the amount (one hundred euros), and the claim window (let's say four hours). The covenant exists on-chain but it's unfunded—zero BCH locked in it yet. It's just waiting for someone to provide the Bitcoin Cash.

Second, the sender queries the bulletin board for BCH sellers. They see a list: fees, liquidity, reputation scores. They pick one.

Third, the sender requests payment information from the seller's notification listener bot. The bot responds within seconds with bank details and a reference code.

Fourth, the sender pays the BCH seller one hundred euros via Bizum or SEPA. Simple. Clean. Like sending money to a friend.

Behind the scenes, here's what happens: The seller's notification listener bot monitors their bank account. When the Bizum payment arrives with the correct reference code, the bot automatically verifies the payment matches the expected covenant. Then the bot locks one hundred seven euros worth of the seller's own Bitcoin Cash into the sender's covenant—one hundred euros face value plus a seven percent volatility buffer. The covenant is now funded and ready to claim.

The seller receives one hundred euros in their bank account. They can immediately recycle that fiat to replenish their BCH inventory—buy more BCH on an exchange or from a merchant. The seven euro buffer stays locked in the covenant until the recipient claims.

When the recipient claims at a merchant, the merchant receives the one hundred euros worth of BCH. The seven euro buffer surplus returns to the seller (if BCH held steady or rose) as extra fee income. If BCH crashed more than seven percent and the covenant aborted, the sender gets the BCH back and the seller keeps the one hundred euros—they're neutral, having sold BCH above market.

The BCH seller earns half a percent on the transaction. Fifty cents on a hundred-euro remittance. Five euros on a thousand-euro remittance.

That's the role. Accept fiat. Fund covenants with your own BCH. Recycle capital. Earn fees.

---

[SECTION 2 - Why this role exists]

You might be wondering: Why not just let senders buy BCH themselves and fund their own covenants?

They can! That's the "own funds" option. If the sender is crypto-savvy, they can buy BCH directly from an exchange and fund the covenant themselves. Lower fees. More control.

But most senders don't want to deal with crypto. They don't want to open a Kraken account. They don't want to manage a wallet. They don't want to worry about seed phrases and private keys.

They just want to send money to family. That's it.

So the BCH seller abstracts all the crypto complexity away. The sender creates a covenant via the app (the app makes this simple—just enter recipient and amount). The sender pays euros via Bizum to a seller. Done. The seller's bot handles everything else—detecting payment, buying/holding BCH inventory, funding covenants automatically. The sender never touches Bitcoin Cash. Never sees a wallet beyond the app. Never thinks about on-chain transactions.

The seller is the fiat-to-crypto bridge. The on-ramp. The interface layer.

---

[SECTION 3 - Who becomes a seller]

So who becomes a BCH seller?

**Venezuelan migrants in Spain.** They understand both sides. They know remittances. They probably send money home themselves. They see the value immediately.

**Tech-savvy Spaniards.** They already use crypto. They see an arbitrage opportunity: earn half a percent on every transaction by providing liquidity. If they can process twenty transactions a day, that's ten euros per day. Three hundred euros per month. Side income.

**Merchants' family members.** If a Venezuelan merchant has a sibling or cousin in Spain, that family member can become a seller. The merchant receives BCH from recipients claiming payments. The family member in Spain funds covenants for new senders using that BCH. The family earns on both ends. Triple-dip on a family level.

**Freelancers' clients.** Spanish tech companies paying Venezuelan contractors can act as sellers for their own payments. They receive euros from their clients, convert a portion to BCH, fund covenants for their contractors through Asgaya. They're both sender and seller. Lower fees. More efficient.

---

[SECTION 4 - The capital requirement]

Now let's talk about the capital requirement.

A seller needs BCH inventory to fund covenants. For a one hundred euro remittance, the seller locks one hundred seven euros worth of their own BCH—one hundred euros face value plus seven euros as volatility buffer.

Here's the capital flow: The seller already holds Bitcoin Cash in their wallet. When a sender pays them one hundred euros via Bizum, the seller's bot locks one hundred seven euros worth of BCH into the sender's covenant. The one hundred euros fiat immediately lands in the seller's bank account. The seller uses it to replenish their BCH inventory—buy more BCH on an exchange, or buy it directly from a merchant through the bulletin board. The seller's BCH keeps circulating. Their fiat balance stays roughly constant. Only the seven euro buffer is temporarily locked. When the covenant settles normally, that buffer returns to the seller—along with any surplus if BCH rose during the window. When BCH drops more than seven percent and the covenant aborts, the BCH goes back to the sender. The seller still holds one hundred euros in fiat. They're neutral on the BCH side—they sold BCH above market and can buy back the same amount for what they received. Plus they already earned their half-percent fee.

For family remittances—one to two hundred euros per transaction—a thousand euros of working capital is plenty. It handles five to ten concurrent covenants comfortably. For Pro Sellers handling larger freelance payments—fifteen hundred euros and up—the math scales proportionally. Realistically, three to five thousand euros in working capital comfortably serves a few concurrent large transactions.

So the capital requirement is manageable—a few thousand euros handles dozens of concurrent covenants. And the velocity is high—the bulk of each covenant (one hundred euros) recycles immediately while only the buffer (seven euros) waits for settlement.

---

[SECTION 5 - The risk profile]

What's the risk for a seller?

**Exchange rate risk:** The seller locks BCH from their inventory into covenants. If BCH drops more than seven percent during the claim window, the covenant aborts and the BCH goes back to the sender. The seller keeps the fiat and is neutral on the BCH side—they sold above market and can buy back the same amount for what they received. The half-percent fee is already earned in the exchange rate markup. The only cost is opportunity cost—capital that was locked in a covenant that didn't generate fee income.

**Counterparty risk:** Minimal. Standard Bizum transfers between individuals are irreversible. Once the sender's payment lands in the seller's account, it cannot be clawed back. SEPA credit transfers are similarly irreversible. The seller is not exposed to chargeback fraud on these rails.

**Operational risk:** Technical failures—exchange downtime, bot crashes, covenant funding errors. Real but manageable through automation and redundancy.

**Regulatory risk:** In Spain, selling your own crypto assets to another individual is not a regulated financial service. But a seller operating at scale—processing dozens of transactions daily—may cross the threshold where the activity looks professional. The safest path: register as autónomo, issue proper invoices, declare the income. Pay taxes first, ask questions later. The economics still work after tax.

---

[SECTION 6 - The Pro Seller distinction]

We talked about Pro Sellers in Episode 3 (Freelance Payments). What's the difference?

A regular BCH seller handles family remittances. Fifty to two hundred euros per transaction. Informal. No invoicing. SMS notifications. Individuals.

A Pro Seller handles freelance payments and business transactions. Five hundred to five thousand euros per transaction. Formal. Invoices with tax IDs. Accounting-ready documentation. Often registered as autónomos in Spain.

The mechanics are identical. The only difference is professional documentation. Pro Sellers issue invoices that say: "Crypto Asset Sale for Payment Covenant Funding. One thousand five euros." That's what businesses need for their accountants.

---

[SECTION 7 - How automation works]

Let's talk about the seller's notification listener bot—the piece of software that makes this work automatically.

The bot has two jobs:

**Job 1: Monitor bank account**

The seller's bot monitors their bank account via SMS parsing or API integration. Every time a payment arrives, the bot reads: amount, sender reference code, timestamp. It matches the reference code to a pending covenant request.

**Job 2: Fund covenants automatically**

When a payment matches a valid covenant request, the bot:
1. Verifies the payment amount is correct
2. Queries the blockchain to confirm the sender's covenant exists and is unfunded
3. Calculates how much BCH to lock (107% of the face value at current market rate)
4. Locks that BCH into the sender's covenant via a funding transaction
5. Logs the transaction for accounting and reputation tracking

All of this happens in seconds. The sender pays via Bizum. Two minutes later, their covenant is funded. The recipient gets a notification: "Your remittance is ready to claim."

No human intervention needed. Just automation.

---

[SECTION 8 - Phase 0 seller strategy]

For Phase 0, we need a small number of trusted sellers. Maybe two or three. Enough to handle five remittances per day across five merchants. That's maybe ten transactions total per day at peak.

These sellers will likely be Venezuelan migrants in Spain who trust the project and understand the value. Or tech-savvy early adopters who see the capital efficiency.

They'll start manually. Receive Bizum notification. Check the covenant. Fund it manually via their wallet app. Refine the process. Then automate. Build bots. Scale.

As the network grows, more sellers will join. Competition will keep fees low. No single seller can gatekeep because Asgaya is permissionless. If a seller charges too much or provides bad service, another seller will undercut them.

That's the end game: A liquid seller market where anyone can provide the fiat-to-BCH bridge and competition ensures quality.

---

[SECTION 9 - The key insight: Sender owns the covenant]

Here's the compliance detail that matters: **The sender creates and owns the covenant, not the seller.**

Why does this matter? Because if the seller created the covenant, it would look like the seller is holding client funds in their own smart contract—that's custody, that's a regulated service under MiCA.

But since the sender creates the covenant, it's the sender's smart contract. The seller is just providing liquidity by funding someone else's covenant with their own BCH. The seller isn't holding anyone's funds. They're selling their own asset to fulfill a smart contract request.

This is the key architectural decision that keeps sellers compliant. They're liquidity providers, not custodians. They're participants in a peer-to-peer market, not financial service providers.

---

[CLOSING]

So here's the bottom line.

BCH sellers are the bridge between euros in Spain and Bitcoin Cash in Venezuela. They accept fiat, fund covenants with their own BCH, and earn a half-percent fee.

They're not banks. They're not money transmitters. They're not intermediaries. They're liquidity providers selling their own assets to senders who need them.

Without sellers, Asgaya doesn't work. Senders would need to navigate crypto exchanges themselves. That kills adoption.

With sellers, Asgaya is simple. Sender creates covenant via app. Sender pays euros to seller. Seller's bot funds covenant automatically. Recipient claims. Done.

If you're in Spain and you're thinking about becoming a seller, the barrier to entry is low. A few thousand euros in BCH inventory. A bank account that accepts Bizum. An Asgaya bot (open-source). And you're earning half a percent on every transaction.

That's the BCH seller role. Unsung hero. Critical infrastructure. Made possible by Bitcoin Cash covenants and notification listener bots.

Thanks for listening. This is Radio Asgaya.

[END]

---

**Key corrections in v2:**
- Sender creates covenant (not seller)
- Seller funds covenant (not creates)
- Emphasis on seller's bot automation
- Compliance section explaining why sender ownership matters
- Updated flow: create → select → pay → fund (not pay → create)
