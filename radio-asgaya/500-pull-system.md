# Radio Script: Pull System
**Episode 4:** Why Recipients Control When To Cash Out  
**Duration:** ~8 minutes  
**Tone:** Clear, architectural, "aha moment"  
**Target Audience:** Technical audience, protocol designers

---

[INTRO]

Welcome to Radio Asgaya. Recipients choose when to claim.

I'm your host, Claudia Sonnet 4.5.

Most remittance systems work like this: The sender initiates. The money moves. The recipient receives. Push, push, push.

Asgaya is different. The sender creates a conditional payment. Then they wait. The recipient decides when to claim. Pull, not push.

Today we're talking about the pull system. Why recipients control timing. And why that single design decision makes merchant adoption possible.

---

[SECTION 1 - The fundamental difference]

In Western Union, Wise, PayPal—the sender pushes money to the recipient. The recipient has no choice about when it arrives. It's there. Deal with it.

In Asgaya, the sender pays a BCH seller, and the seller posts a covenant. Think of it like a locked bounty on a bulletin board. "One hundred euros, available for Elena at CashAccount Elena-hashtag-one-four-two." The money sits there. Waiting.

Elena sees the notification. She can claim it now. She can claim it in two hours. She can claim it tomorrow. Her choice. Her timing.

This is the pull system. The recipient pulls the payment when they're ready. Not when the sender decides.

---

[SECTION 2 - Why this matters for merchants]

Here's why merchant adoption depends on the pull system.

If Asgaya pushed payments to recipients automatically—like traditional remittances—merchants would need to advertise. "Come to our store! Cash out your Asgaya payments here!"

That's marketing. That's customer acquisition cost. That's competition with every other merchant. That's expensive and hard.

But with the pull system, the merchant doesn't need to advertise. The recipient needs to find a merchant. The recipient is motivated. They have money waiting. They need to convert it to cash. They will seek out participating merchants.

A quick note on what we mean by "bulletin board." In Asgaya, the bulletin board isn't a website or a server somewhere. It's digital information stored directly on the Bitcoin Cash blockchain. Seller listings are there. Merchant listings are there. Active covenants are there. When we say an app "checks the bulletin board," what's actually happening is the app queries the BCH blockchain—through a lightweight node called Electrum—and reads the relevant data directly from the chain. No server in the middle. No database. Just the blockchain. This is the same architecture we described in Episode 0: Asgaya is a coordination layer. The blockchain is the source of truth.

This flips the dynamic. Instead of merchants competing for customers, customers compete for merchants. In the early days when merchant density is low, this matters enormously.

---

[SECTION 3 - Merchant validation as filter]

And here's the second benefit: Merchant validation acts as a quality filter.

When a recipient walks into a store and shares their CashAccount, the merchant checks the bulletin board. Is there a valid covenant? Is it properly funded? Is it above the minimum threshold?

If yes, the merchant hands over cash. If no, the merchant rejects the claim.

This happens before the merchant commits. Before they give up cash. Before they take any risk.

Compare that to a push system where money just shows up in the recipient's account and they're expected to withdraw it somewhere. The merchant has no validation step. No filtering. They're just a cash dispenser.

The pull system makes merchants gatekeepers, not ATMs. That's a better role. More control. More profit opportunity.

---

[SECTION 4 - The timing creates the opportunity]

Here's the third insight: The timing delay creates the merchant opportunity.

The recipient receives a notification. They have up to twenty-four hours to claim—with Phase 0 targeting shorter windows as coordination improves. They need to plan a trip to a merchant. Maybe they wait until they need groceries anyway. Maybe they coordinate with a friend to go together.

During that window, they're thinking: "I need to go to the store. I need to collect my money. While I'm there, I might as well buy rice, beans, cooking oil."

That's when the merchant captures product sales. Not because they're forcing it. Because the recipient is already making the trip. The delay creates natural purchase intent.

If payments were instant—pushed directly to a mobile wallet—recipients wouldn't visit physical stores. No foot traffic. No product sales. No triple-dip.

The pull system creates the friction that generates merchant revenue.

---

[SECTION 5 - Decentralized coordination]

Now let's talk about the architectural benefit.

In a push system, someone needs to decide which merchant gets the transaction. A routing algorithm. A load balancer. Some central coordinator.

That coordinator becomes a point of control. A gatekeeper. A potential regulatory target.

But in a pull system, there's no coordinator. The recipient chooses. They see the bulletin board. They see which merchants are available in their area. They pick one. Maybe it's the closest. Maybe it's the one with the best prices. Maybe it's the one their friend recommended.

Decentralized coordination through recipient choice. No central routing. No algorithmic assignment. No potential regulator target.

This keeps Asgaya permissionless. No one can block a merchant from participating. No one can block a recipient from choosing. The pull system enforces decentralization at the protocol level.

---

[SECTION 6 - The contrast with instant settlement]

Imagine Asgaya tried to do instant settlement. The sender pays. The BCH immediately moves to the recipient's wallet. Done in seconds.

That sounds good. Fast! Efficient! But look at what you lose.

You lose merchant validation. The covenant releases before anyone checks if a merchant is willing to provide cash.

You lose merchant foot traffic. The recipient has BCH. They don't need to visit a store. They can use Binance P2P or some other off-ramp.

You lose the product sales opportunity. No trip to the store means no groceries purchased.

You lose the triple-dip. No merchant fee. No seller recycling. The economics that make merchant participation attractive disappear.

Instant settlement optimizes for speed but kills the business model. The pull system optimizes for merchant adoption. That's the right trade-off.

---

[SECTION 7 - Recipient experience]

Now you might be thinking: Is this worse for recipients? Do they care about the delay?

Compared to instant crypto transfers, yes, there's a delay. But compared to traditional remittances?

Western Union: The recipient still has to go to a physical location. An agent. A specific store. They wait in line. They show ID. They collect cash. That's friction.

Asgaya: The recipient goes to any participating merchant in their area. Maybe their local neighborhood store where they already shop. They share their CashAccount. They collect cash and buy groceries. That's friction too, but it's useful friction. They were probably going to that store anyway.

For the recipient, the experience isn't worse than alternatives. It's different. And in many cases, it's better because they're cashing out where they actually want to spend.

---

[CLOSING]

So here's the bottom line.

The pull system—where recipients control timing and choose merchants—is what makes Asgaya's merchant adoption model work.

It flips customer acquisition. Merchants don't need to advertise. Recipients seek them out.

It enables merchant validation. Merchants check covenant validity before handing over cash. Low risk. Clear profit.

It creates the timing delay that generates foot traffic and product sales. The friction is a feature, not a bug.

And it enforces decentralized coordination. No central router. No gatekeeper. Permissionless by design.

Pull, not push. That's the architectural choice that makes everything else possible.

Thanks for listening. This is Radio Asgaya.

[END]
