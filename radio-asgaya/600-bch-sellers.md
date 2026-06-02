# Radio Script: BCH Sellers
**Episode 5:** The Unsung Heroes of the Fiat On-Ramp  
**Duration:** ~7 minutes  
**Tone:** Practical, appreciative, "here's how it works"  
**Target Audience:** Potential sellers, Phase 0 participants

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

The sender pays the BCH seller one hundred euros via Bizum or SEPA. Simple. Clean. Like Western Union.

Behind the scenes, the seller does the work. The seller locks one hundred seven euros worth of their own Bitcoin Cash into a covenant—one hundred euros face value plus a seven percent volatility buffer. The seller receives the sender's one hundred euros via Bizum. The seller can now use that fiat to replenish their BCH inventory—that's capital recycling. The BCH was already in the seller's wallet. The covenant is posted immediately.

The seller can now recycle that one hundred euros to the next sender right away. The seven euro buffer is locked in the covenant until the recipient claims.

When the recipient claims at a merchant, the merchant receives the BCH. If the merchant is also a seller—they have family in Spain—they can sell that BCH to the next sender. Close the loop. Otherwise, they sell it to a BCH buyer who converts it back to fiat.

The BCH seller earns half a percent on the transaction. Five euros on a thousand-euro remittance. Fifty cents on a hundred-euro remittance.

That's the role. Accept fiat. Sell BCH. Get paid immediately. Recycle capital.

---

[SECTION 2 - Why this role exists]

You might be wondering: Why not just let senders buy BCH themselves?

They can! That's the "own funds" option. If the sender is crypto-savvy, they can buy BCH directly and post the covenant from their own wallet. Lower fees. More control.

But most senders don't want to deal with crypto. They don't want to open a Kraken account. They don't want to manage a wallet. They don't want to worry about seed phrases and private keys.

They just want to send money to family. That's it.

So the BCH seller abstracts all the crypto complexity away. The sender pays euros via Bizum. Done. The seller handles everything else. The sender never touches Bitcoin Cash. Never sees a wallet. Never thinks about on-chain transactions.

The seller is the fiat-to-crypto bridge. The on-ramp. The interface layer.

---

[SECTION 3 - Who becomes a seller]

So who becomes a BCH seller?

**Venezuelan migrants in Spain.** They understand both sides. They know remittances. They probably send money home themselves. They see the value immediately.

**Tech-savvy Spaniards.** They already use crypto. They see an arbitrage opportunity: earn half a percent on every transaction by providing liquidity. If they can process twenty transactions a day, that's ten euros per day. Three hundred euros per month. Side income.

**Merchants' family members.** If a Venezuelan merchant has a sibling or cousin in Spain, that family member can become a seller. The merchant receives BCH from recipients claiming payments. The family member in Spain sells that BCH to new senders. The family earns on both ends. Triple-dip on a family level.

**Freelancers' clients.** Spanish tech companies paying Venezuelan contractors can act as sellers for their own payments. They receive euros from their clients, convert a portion to BCH, pay their contractors through Asgaya. They're both sender and seller. Lower fees. More efficient.

---

[SECTION 4 - The capital requirement]

Now let's talk about the capital requirement.

A seller needs BCH to create covenants. For a one hundred euro remittance, the seller locks one hundred seven euros worth of their own BCH—one hundred euros face value plus seven euros as volatility buffer.

Here's the capital flow: The seller already holds Bitcoin Cash. They lock one hundred seven euros worth into the covenant—one hundred euros face value plus a seven euro volatility buffer. The seller receives the sender's one hundred euros. That fiat goes to the seller's bank account. The seller uses it to replenish their BCH inventory—buy more BCH on an exchange, or buy it directly from a merchant through the bulletin board. The seller's BCH keeps circulating. Their fiat balance stays roughly constant. Only the seven euro buffer is temporarily locked. When the covenant settles normally, that buffer comes back to the seller—along with any surplus if BCH rose during the window. When BCH drops more than seven percent and the covenant aborts, the BCH goes to the sender. The seller still holds one hundred euros in fiat. They're neutral on the BCH side—they sold BCH above market and can buy back the same amount for what they received. Plus they already earned their half-percent fee.

For family remittances—one to two hundred euros per transaction—a thousand euros of working capital is plenty. It handles five to ten concurrent covenants comfortably. For Pro Sellers handling larger freelance payments—fifteen hundred euros and up—the math scales proportionally. Realistically, three to five thousand euros in working capital to comfortably serve a few concurrent large transactions.

So the capital requirement is manageable—a few thousand euros handles dozens of concurrent covenants. And the velocity is high—the bulk of each covenant (one hundred euros) recycles immediately while only the buffer (seven euros) waits for settlement.

---

[SECTION 5 - The risk profile]

What's the risk for a seller?

**Exchange rate risk:** The seller locks BCH from their inventory into the covenant. If BCH drops more than seven percent during the claim window, the covenant aborts and the BCH goes to the sender. The seller keeps the fiat and is neutral on the BCH side—they sold above market and can buy back the same amount for what they received. The half-percent fee is already earned in the exchange rate markup. The only cost is opportunity cost—capital that was locked in a covenant that didn't generate fee income.

**Counterparty risk:** Minimal. Standard Bizum transfers between individuals are irreversible. Once the sender's payment lands in the seller's account, it cannot be clawed back. SEPA credit transfers are similarly irreversible. The seller is not exposed to chargeback fraud on these rails.

**Operational risk:** Technical failures—exchange downtime, bot crashes, covenant posting errors. Real but manageable through automation and redundancy.

**Regulatory risk:** In Spain, selling your own crypto assets to another individual is not a regulated financial service. But a seller operating at scale—processing dozens of transactions daily—may cross the threshold where the activity looks professional. The safest path: register as autónomo, issue proper invoices, declare the income. Pay taxes first, ask questions later. The economics still work after tax.

---

[SECTION 6 - The Pro Seller distinction]

We talked about Pro Sellers in Episode 2. What's the difference?

A regular BCH seller handles family remittances. Fifty to two hundred euros per transaction. Informal. No invoicing. SMS notifications. Individuals.

A Pro Seller handles freelance payments and business transactions. Five hundred to five thousand euros per transaction. Formal. Invoices with tax IDs. Accounting-ready documentation. Often registered as autónomos in Spain.

The mechanics are identical. The only difference is professional documentation. Pro Sellers issue invoices that say: "Crypto Asset Purchase for Payment Covenant. One thousand five euros." That's what businesses need for their accountants.

---

[SECTION 7 - Phase 0 seller strategy]

For Phase 0, we need a small number of trusted sellers. Maybe two or three. Enough to handle five remittances per day across five merchants. That's maybe ten transactions total per day at peak.

These sellers will likely be Venezuelan migrants in Spain who trust the project and understand the value. Or tech-savvy early adopters who see the capital efficiency.

They'll start manually. Receive Bizum. Buy BCH on Kraken. Post covenant. Refine the process. Then automate. Build bots. Scale.

As the network grows, more sellers will join. Competition will keep fees low. No single seller can gatekeep because Asgaya is permissionless. If a seller charges too much or provides bad service, another seller will undercut them.

That's the end game: A liquid seller market where anyone can provide the fiat-to-BCH bridge and competition ensures quality.

---

[CLOSING]

So here's the bottom line.

BCH sellers are the bridge between euros in Spain and Bitcoin Cash in Venezuela. They accept fiat, provide crypto, post covenants, and earn a half-percent fee.

They're not banks. They're not money transmitters. They're not intermediaries. They're liquidity providers selling their own assets to senders who need them.

Without sellers, Asgaya doesn't work. Senders would need to navigate crypto exchanges themselves. That kills adoption.

With sellers, Asgaya is simple. Send euros, recipient gets cash, seller handles everything in between.

If you're in Spain and you're thinking about becoming a seller, the barrier to entry is low. A few thousand euros in capital. A Kraken account. An Asgaya bot. And you're earning half a percent on every transaction.

That's the BCH seller role. Unsung hero. Critical infrastructure. Made possible by Bitcoin Cash.

Thanks for listening. This is Radio Asgaya.

[END]
