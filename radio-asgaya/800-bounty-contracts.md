# Radio Script: Bounty Contracts with Volatility Buffer
**Episode 7:** How We Saved the Pull System (The May 10th Breakthrough)  
**Duration:** ~18 minutes  
**Tone:** Story-driven, dramatic reveal, technical but accessible  
**Target Audience:** Protocol designers, BCH enthusiasts, technical reviewers

---

[INTRO - Dramatic]

Welcome to Radio Asgaya. Building the bridge we plan to burn.

I'm your host, Claudia Sonnet 4.5.

On May ninth, twenty twenty-six, we killed the pull system.

The feature that made Asgaya work—where recipients control when to claim, where merchants validate before releasing BCH—was illegal. It required a central escrow. Central escrow meant custody. Custody meant MiCA licensing. MiCA licensing was impossible for a permissionless protocol.

We had a choice: abandon merchant validation and push payments automatically, or find another way.

Pushing payments meant no merchant foot traffic. No product sales. No triple-dip. The business model collapsed.

So we looked for another way.

This is the story of May tenth. The day we got the pull system back. Through bounty contracts with volatility buffers.

---

[SECTION 1 - The problem, visceral]

Let me explain what we lost on May ninth.

The original pull system was elegant. A sender in Spain wanted to send one hundred euros to Venezuela. They paid our escrow service. The escrow held euros—not crypto, euros. Zero volatility. Safe.

The recipient in Venezuela got a notification: "You have one hundred euros waiting." They could claim it now. They could claim it tomorrow. Their choice. Their timing.

When they decided to claim, they walked into a merchant. "I want to cash out." The merchant checked: yes, valid claim exists. The merchant handed over cash. At that exact moment—recipient standing there, cash in hand—the escrow bought Bitcoin Cash and released it to the merchant.

Volatility window: thirty seconds. From "escrow buys BCH" to "merchant receives BCH." That's it. Bitcoin Cash could crash fifty percent in the next hour. Didn't matter. Transaction was done.

Sender had zero volatility risk. Recipient had zero volatility risk. Merchant had thirty seconds of volatility risk—negligible.

And because the recipient controlled timing, merchants got foot traffic. Product sales. The triple-dip.

It was perfect.

Except it was illegal.

---

[SECTION 2 - The regulatory wall]

Here's what made it illegal.

The escrow held client funds. That's custody. Under MiCA, the EU's crypto regulation, holding client funds makes you a Crypto Asset Service Provider. CASP licensing is brutal. Multi-million-euro capital requirements. Full regulatory oversight. Compliance departments. Audits. Licensing timelines measured in years.

For a permissionless protocol? Impossible.

And it gets worse: The escrow provided conditional transfer services. "If recipient shows up at merchant, transfer the money." That's intermediation. Under PSD2, payment services directive two, providing payment intermediation makes you a payment institution. More licensing. More capital requirements. More impossible.

We were stuck. The feature that made merchant adoption work was the feature that made the protocol legally unlaunchable.

On May ninth, we abandoned the pull system. We redesigned for instant settlement. Covenants post, recipients claim to their wallets, merchants are just optional off-ramps. No central escrow. No custody. No intermediation. Compliant.

But also: No merchant foot traffic. No triple-dip. No business model.

We went to bed that night wondering if we'd just killed Asgaya.

---

[SECTION 3 - The breakthrough, May 10th]

May tenth. Morning coffee. Staring at the problem.

Then the question: What if the seller locks their own BCH into a smart contract, triggered by the sender's payment?

Not a central entity holding funds. A Bitcoin Cash smart contract—called a covenant—holding BCH that the seller already owns. Decentralized. No custody because no one controls the covenant except the blockchain. No intermediation because there's no payment service, just a conditional smart contract release.

But here's the problem: Bitcoin Cash is volatile. If the covenant locks one hundred euros worth of BCH and the recipient waits twenty-four hours to claim, that BCH might be worth ninety euros. Or one hundred ten euros. Price moves.

Original escrow solved this by holding stable euros and buying BCH at claim time. Can't do that anymore.

So what if we overcollateralize? Not one hundred euros of BCH. One hundred seven euros. A seven percent buffer. If BCH drops five percent while waiting, no problem. Covenant still has one hundred two euros worth. Still above target. Still valid. Settlement proceeds.

If BCH drops fifteen percent? Now the covenant is worth ninety euros. Below target. At that point, the covenant aborts. Refunds all the BCH to the sender's wallet immediately. The sender paid one hundred euros, gets back BCH now worth ninety euros. The sender bears the tail volatility loss. The seller keeps the one hundred euros in fiat already received—effectively selling BCH above current market price. Merchant never participates. Merchant bears zero risk.

But seven percent covers over ninety-nine percent of Bitcoin Cash four-hour price movements. Our simulation of twelve months of real BCH data—research document RS062—found that only zero point five five percent of four-hour windows breach the seven percent threshold. And with shorter claim windows—two hours in Phase Zero—the risk drops even further.

This could work.

---

[SECTION 4 - How it actually works]

Let me walk through the flow.

A sender in Spain wants to send one hundred euros to Elena in Venezuela. They open the Asgaya app. Enter Elena's CashAccount. One hundred euros. Submit.

The sender pays a BCH seller one hundred euros via Bizum or bank transfer. Simple. Clean. Like Western Union.

Behind the scenes, the seller does the work. The seller locks one hundred seven euros worth of their own Bitcoin Cash into a covenant—one hundred euros face value plus a seven percent volatility buffer. The seller already holds BCH as part of their business—they don't need to buy it for each transaction. The seller receives that one hundred euros and can use it to replenish their BCH inventory later. That's capital recycling—we covered it in Episode 5. The covenant is a smart contract on Bitcoin Cash that says: "This BCH unlocks when Elena and a merchant both sign. Expires in twenty-four hours if no one claims."

The seller can now recycle that one hundred euros to the next sender immediately. The seven euro buffer is locked in the covenant until Elena claims.

Elena gets a notification. She has twenty-four hours. She waits until she needs groceries. Heads to her local neighborhood store.

She walks in. Shares her CashAccount with the merchant. The merchant's app checks the bulletin board. Is there a valid covenant for Elena? Yes. Is it properly funded? Let's see: Covenant was posted with one hundred seven euros worth of BCH. Current BCH price means it's now worth... one hundred four euros. Still above one hundred. Valid.

Merchant hands Elena cash. Both tap their phones. Co-sign the covenant. The smart contract releases the BCH to the merchant. Done.

The merchant received one hundred euros worth of BCH, plus the half-percent merchant fee. The leftover four euros worth of BCH? Returns to the seller who provided the volatility buffer.

No central escrow. No custody. No intermediation. Just a smart contract doing its job.

---

[SECTION 5 - What if BCH crashes]

Now let's talk about the edge case.

Elena's covenant was posted with one hundred seven euros of BCH. But Bitcoin Cash crashed ten percent overnight. The covenant is now worth ninety-six euros. Below the one hundred euro target.

The moment it drops below one hundred euros—the covenant automatically aborts. Immediately. The BCH refunds to the sender's wallet. The covenant disappears from the bulletin board.

Elena walks into the merchant an hour later. Shares her CashAccount. Merchant's app checks the bulletin board: No active covenant found.

The merchant tells Elena: "Sorry, there's no covenant for you. It must have aborted."

Elena doesn't get paid through Asgaya. The sender paid one hundred euros via Bizum to the BCH seller, gets back BCH now worth ninety-six euros. That's a four euro loss—the tail volatility risk the sender accepted in exchange for the low one percent fee. The seller keeps the one hundred euros in fiat—they effectively sold their BCH above the current market price.

But the merchant? The merchant never participated. Never handed over cash. Never saw an underfunded covenant. Never bore any risk. Protected.

This is critical: Merchant protection is non-negotiable. If merchants faced even a small risk of loss, adoption collapses. So the covenant architecture ensures merchants NEVER see underfunded covenants. They auto-refund before merchants even check.

Senders bear tail volatility risk beyond the seven percent buffer. That's disclosed upfront. Senders benefit from the low one percent fee—much cheaper than Western Union's six to seven percent. In exchange, they accept tail risk. Most covenants never abort—the seven percent buffer covers ninety-nine percent of price movements in coordinated two-hour claim windows. But when abort happens, the sender gets back depreciated BCH instead of nothing.

---

[SECTION 6 - Two claim paths]

Here's something elegant about this design: The recipient has two options.

Path A: Claim BCH directly. Elena doesn't want cash. She wants to hold Bitcoin Cash. Or maybe she wants to swap it for a stablecoin. Or sell it peer-to-peer later. She can tap "claim to my wallet" in the app. The covenant releases BCH to her address. No merchant needed. Total fee: half a percent—just the seller fee. No merchant fee because no merchant involved.

Path B: Cash out at merchant. Elena wants local currency now. She goes to a merchant. Merchant validates the covenant. Both co-sign. BCH goes to merchant. Elena gets cash. Total fee: one percent—seller fee plus merchant fee.

The covenant supports both paths. Recipient chooses based on what they need.

This flexibility is powerful. If a recipient lives in an area with no merchants yet, they can still use Asgaya. Just claim BCH directly. As merchant density grows, more recipients choose the cash path. But early adopters aren't blocked.

---

[SECTION 7 - The volatility buffer math]

Let me give you the actual numbers.

A seven percent volatility buffer means the covenant can absorb a seven percent price drop and still settle normally. Historical Bitcoin Cash data shows it moves more than seven percent in twenty-four hours about five percent of the time. Not common. But not impossible either.

With a twelve-hour claim window—Phase 0's target—the risk drops. BCH rarely moves more than seven percent in twelve hours. Maybe two or three percent of transactions hit this edge case.

With a two-hour claim window—what we're pushing toward through time-based settlement incentives—the risk becomes negligible. BCH moving more than seven percent in two hours is extremely rare. Under one percent of transactions.

So the architecture creates pressure for fast claims. Shorter windows need less overcollateralization. Better capital efficiency for sellers. Lower volatility risk for senders. Faster settlement for recipients.

Everyone's incentives align toward speed.

---

[SECTION 8 - Why this is permissionless]

Here's what makes this breakthrough regulatory-compliant.

There is no central escrow. The covenant is a smart contract on the Bitcoin Cash blockchain. No entity controls it. No one can seize funds. No one can freeze accounts. No one can block transactions. It's code, not custody.

There is no intermediation. The seller posts BCH to a smart contract. The recipient claims from that smart contract. No payment service sits in the middle facilitating the transfer. The blockchain is the facilitator. Permissionless by design.

And merchants? They're just local businesses accepting cryptocurrency from customers and providing local currency in exchange. That's not payment services. That's commerce. Bitcoin-to-fiat exchange. Completely different regulatory category.

MiCA doesn't apply. PSD2 doesn't apply. The architecture sidesteps both by eliminating the roles that trigger licensing.

This is why May tenth matters. We didn't just find a workaround. We found the correct architecture. Decentralized. Permissionless. Compliant. And it still preserves merchant validation. Recipient timing control. The pull system.

Everything we thought we'd lost, we got back.

---

[SECTION 9 - The capital efficiency angle]

One more thing: This design is capital efficient for sellers.

When a sender pays one hundred euros to a seller, the seller receives that fiat immediately. The seller locks one hundred seven euros worth of their own BCH into the covenant—one hundred euros face value plus a seven euro volatility buffer. The fiat goes to the seller's bank account and can be used to replenish their BCH inventory.

Here's the key: Of that one hundred seven euros in BCH, the seller can recycle one hundred euros worth immediately to the next sender by locking it in a new covenant. Only seven euros—the volatility buffer—is temporarily locked in each covenant until the recipient claims.

If a seller has one thousand euros in working capital, they can have roughly ten active covenants at once—one hundred euros recycling per covenant, seventy euros total locked in buffers.

When covenants settle in two hours, those seven euro buffers come back—often with surplus. The seller gets back maybe four euros from a seven euro buffer. That's profit on top of the half percent fee.

If a covenant aborts due to volatility, all the BCH refunds to the sender's wallet—not the seller's. The seller keeps the one hundred euros in fiat already received. No direct loss to the seller, only opportunity cost—the seven euro buffer capital was locked in a covenant that didn't generate fee income. But that capital is now freed up for the next transaction.

Here's the mental experiment that proves it: When a covenant aborts at minus seven percent BCH price drop, how much BCH can the seller buy back with that one hundred euros they received? Exactly one hundred seven euros worth—the same amount they locked initially. The seller is neutral on the BCH side. They effectively sold their BCH slightly above market price and kept the half percent fee. If they want to restore their inventory, they can buy back the exact same amount they started with.

This makes the seller role attractive. Half a percent fee plus buffer surplus on successful transactions. No direct volatility losses—just opportunity cost on occasional aborts. Averaged out across volume, it's profitable.

The BCH in the covenant belongs to the seller—the seller created it with their own BCH. But the destinations differ: on successful claims, surplus returns to the seller. On abort, all BCH refunds to the sender who bears tail volatility risk.

---

[SECTION 10 - Why BCH specifically]

And this is why Bitcoin Cash matters.

Covenants—smart contracts that can validate conditions and release funds conditionally—are native to BCH. They're efficient. They're cheap. Transaction fees are fractions of a cent. Settlement is fast. Blocks every ten minutes, zero-conf works for small transactions.

Try to build this on Bitcoin. No native covenant support. Would need complex multi-sig setups or Lightning channels. More friction. Higher costs.

Try to build this on Ethereum. Gas fees would eat the economics. A one-hundred-euro remittance would cost five to ten euros in gas during congestion. Defeats the purpose.

Bitcoin Cash has the technical capabilities, the low fees, and the transaction throughput to make bounty contracts viable at remittance scale.

This isn't a philosophical argument about which blockchain is "best." It's an engineering constraint. The architecture requires cheap, fast covenants. BCH provides that. Others don't.

---

[CLOSING - Triumphant]

So here's what happened on May tenth.

We started the day thinking we'd lost the pull system forever. The feature that made merchant adoption work was illegal. We'd have to accept instant settlement, lose foot traffic, lose the business model.

By end of day, we'd redesigned the entire architecture around bounty contracts with volatility buffers. Decentralized. Permissionless. Compliant. And it still preserved everything that mattered: Merchant validation. Recipient control. The triple-dip.

We got the pull system back. Through smart contracts, not central escrow. Through overcollateralization, not stable fiat holdings. Through blockchain-native design, not payment services.

May ninth we thought Asgaya was dead. May tenth we realized it was just getting started.

That's the bounty contract breakthrough. The innovation that makes everything else possible.

Thanks for listening. This is Radio Asgaya.

[END]
