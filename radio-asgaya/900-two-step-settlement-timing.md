# Radio Script: Two-Step Settlement Timing
**Episode 8:** Why Create and Claim Are Separate Steps  
**Duration:** ~14 minutes  
**Tone:** Technical but accessible, story-driven around decision-making  
**Target Audience:** Technical reviewers, protocol designers, system architects

---

[INTRO]

Welcome to Radio Asgaya. Where timing determines everything.

I'm your host, Claudia Sonnet 4.5.

Let me ask you something simple: When should a remittance settle?

The moment the sender hits send? When the merchant is ready to hand over cash? When the recipient shows up? When conditions are verified?

The answer sounds obvious until you realize each choice locks you into a completely different architecture. And one wrong choice breaks your entire business model.

Today we're talking about two-step settlement timing. Why Asgaya separates the covenant creation from the claim execution. And why that single design decision determines whether merchants can validate transactions safely, whether recipients control their own timing, and whether the entire system actually works.

---

[SECTION 1 - The timing trap]

Here's the fundamental problem with remittances.

A sender in Barcelona wants to send one hundred euros to their cousin Elena in Caracas. They initiate the transfer. Barcelona time. Instant.

But Elena is asleep. It's three in the morning in Venezuela. The money arrives. The notification pings. She doesn't see it for four hours.

Now, here's where timing gets tricky. During those four hours, what should happen?

Option One: The money is already moving. It's been converted to BCH. It's sitting in some wallet or locked somewhere. Bitcoin Cash prices are moving. If BCH dropped five percent while Elena slept, does she still get what the sender promised? Or did volatility eat the difference?

Option Two: The money waits. It's locked in a covenant. Still in euro-denominated terms. Waiting for Elena to be ready to claim. But how long should it wait? Twelve hours? Twenty-four hours? What if Elena can't reach a merchant for three days?

Option Three: The money moves twice. First step, the sender commits. The covenant locks with volatility protection. Second step, Elena claims when she's ready. BCH settles at that moment, right when the merchant validates.

That's option three. That's two-step settlement. And it's the only option that solves both problems at once.

---

[SECTION 2 - The merchant validation window]

Now let's focus on the merchant side of this.

A merchant in Venezuela has maybe three hundred euros of fiat sitting in their register. They need to be careful with it. Every transaction is real money. Real risk.

When Elena walks in with a claim to five hundred euros of crypto, the merchant needs to ask: Is this real? Is this valid? Can I actually get paid?

In a single-step system, the settlement is already happening. Or it's about to happen instantly. The merchant has maybe seconds to verify before it's too late to back out. That's stressful.

But in a two-step system, the first step already happened. Hours ago. The covenant was already created. The sender paid a BCH seller via Bizum. The seller locked their own Bitcoin Cash into the covenant. The covenant is already on the blockchain. Already funded. Already waiting.

So when Elena walks in, the merchant isn't asking "Is this settling right now?" The merchant is asking "Is this covenant still valid? Did conditions get met?"

That's a much safer question. The merchant checks: Yes, covenant exists. Yes, the sender created it and it's funded. Yes, the covenant is still properly collateralized. Yes, I can safely hand over cash.

Only then does the merchant participate. Only then does the final signature get added. Only then does the covenant execute.

Two-step settlement gives merchants a validation window. They can check everything before committing. That's what makes merchant participation safe.

---

[SECTION 3 - The claim window trade-off]

But here's what makes timing complicated: How long should that window be?

If Elena has a two-hour claim window, that's fast. Great for the sender. Bad for Elena. She might be at work. She might be in an area without cell coverage. She might not get the notification immediately.

If Elena has a forty-eight-hour claim window, that's plenty of time. She's definitely going to see it. Definitely going to reach a merchant. But that's a long time for the covenant to stay locked. A long time for BCH to stay at volatility risk. A long time for the BCH seller to have their capital tied up.

The standard in Phase Zero is twenty-four hours. One day. Long enough for almost any recipient to reach a merchant. Short enough to keep seller capital somewhat efficient. Short enough that most Bitcoin Cash price movements stay within the seven percent volatility buffer.

But this is worth questioning. In an urban area with dense merchant networks, maybe twelve hours is better. Faster claims. More efficient seller capital. Less volatility risk.

In a rural area where recipients are spread out, maybe forty-eight hours is necessary. Longer travel times. Merchant scarcity. Needs more time.

So the claim window becomes a tunable parameter. It's not set in stone. It's hypothesized based on what we think will work. Then validated through Phase Zero testing. Then adjusted if the data shows we're wrong.

That flexibility is only possible because of two-step settlement. The claim window isn't hard-coded into the protocol. It's a business parameter for each covenant.

---

[SECTION 4 - Volatility protection timing]

Now let's talk about volatility specifically.

If the entire settlement happens in thirty seconds—from the moment the recipient walks in the door to the moment the merchant receives BCH—volatility is negligible. Bitcoin Cash is volatile but not that volatile in thirty-second windows.

But in two-step settlement, the clock doesn't start when Elena walks in. The clock started hours ago. When the sender initiated. The volatility window is whatever time elapsed between covenant creation and merchant claim execution.

That's why the seven percent volatility buffer matters.

The buffer isn't there for the thirty seconds Elena spends at the merchant. The buffer is there for the hours the covenant waits. The hours where Bitcoin Cash could move three percent. Or five percent. Or in rare cases, more than seven percent.

The architecture handles this by overcollateralizing. The BCH seller posts one hundred seven euros worth of BCH. Not one hundred. One hundred seven. The extra seven percent is the safety margin.

If BCH holds stable, the extra seven euros comes back to the seller as surplus. If BCH drops five percent, the volatility buffer absorbs it, and the merchant still gets exactly one hundred euros worth. If BCH drops more than seven percent, the covenant aborts before the merchant even participates. Merchant safety is guaranteed.

This only works because the covenant stays locked during the entire wait period. From covenant creation to claim execution. Both phases protected. One unified volatility window.

If settlement happened in real time—the moment the sender initiated—the volatility buffer wouldn't help. You can't protect against volatility that happens after settlement. The protection has to exist before and during the wait. That requires two-step settlement.

---

[SECTION 5 - Instant settlement would break the model]

Let me show you what would break if we tried to do instant settlement.

Imagine Asgaya worked like this: Sender hits "send." Pays the seller. Instantly, the seller creates a covenant. Instantly, it's ready to be claimed. The recipient gets a notification: "Your money is ready. Go to any merchant."

Sounds good. Speed! Efficiency! But let's trace what happens.

First problem: The merchant has no warning. Elena walks in with a fresh claim. The covenant was just created. The merchant has no time to validate. No time to check the bulletin board. No time to verify the covenant is properly funded. The merchant would have to trust the recipient's app. Trust that whatever they're showing is real. That's risky.

Second problem: Elena might claim her money the same day the covenant was created. Bitcoin Cash didn't have much time to move. But she might also claim it three days later. Now we've stretched the volatility window from hours to days. Seven percent volatility buffer isn't enough anymore. We'd need twelve percent. Fifteen percent. The economics break. Senders would need to buy fifteen percent extra BCH as buffer, making remittances expensive. The system becomes unaffordable.

Third problem: The merchant has no say in timing. The covenant is ready immediately. Elena might show up. Might not. The merchant has to stand ready at all times. Or the merchant checks the bulletin board, sees a fresh covenant, and calls Elena to come claim it. That's outbound marketing. That's not how the system is supposed to work.

Fourth problem: Recipient control disappears. In instant settlement, the sender controls when execution happens. The recipient is just passive. They're told "your money is waiting." They're not deciding when to claim. The system is push, not pull.

And push systems don't generate merchant foot traffic. Push systems don't create product sales opportunities. Push systems don't create the triple-dip that makes merchants want to participate.

So instant settlement kills the merchant adoption model. It might sound simpler. But it's not. It's worse. It's broken.

Two-step settlement is harder architecturally. But it's the only way to preserve merchant validation, recipient timing control, and sustainable economics all at the same time.

---

[SECTION 6 - Why covenant validation works]

Let's get specific about why the merchant can actually trust the validation step.

Elena walks into a merchant. She shows her CashAccount code. The merchant's app checks the bulletin board. Is there a covenant for Elena? Yes. Is it funded by a known BCH seller? Yes. Did that seller receive payment from the sender and create this covenant? The covenant has the seller's signature. The seller's bot auto-created and signed the covenant the moment they saw the Bizum payment. So yes.

Is the covenant still properly collateralized? Here's the check: The covenant was locked with one hundred seven euros worth of Bitcoin Cash. Current BCH price is X. Current covenant value is one hundred seven times X divided by original value. If current value is still above one hundred euros, it passes. If current value dropped below one hundred euros, the covenant already auto-refunded to the sender's wallet. The merchant would see no active covenant in the bulletin board.

All of this happens automatically. The merchant's app does the math. Checks the signatures. Verifies the blockchain state. All of it takes seconds.

The merchant's app doesn't have to trust Elena. Doesn't have to trust the sender. Doesn't have to trust the BCH seller. It has to trust the code. The open-source code that anyone can audit. The code that runs the same way every time.

This is the power of two-step settlement. The first step is validation. The merchant can see that conditions are met before deciding to participate. It's not trust. It's verification.

---

[SECTION 7 - The messaging implication]

Here's something that often gets overlooked: Two-step settlement has a messaging implication.

In step one, the sender initiates. They pay the BCH seller via Bizum. The seller locks their own Bitcoin Cash into a covenant with a seven percent volatility buffer. The covenant is posted on-chain. From the recipient's perspective, nothing has happened yet. They see a notification: "Money is being prepared for you. Check back soon."

They're not sitting on urgent cash. They're waiting for a system to stabilize. The covenant is locking. The volatility buffer is being positioned. The merchant network is being alerted.

Then, in step two, the recipient is in control. They decide when to claim. They decide which merchant to visit. They decide if they want to wait for a better merchant or claim somewhere nearby. That framing—"here's money you can claim"—is psychologically different from "here's money that's already moving."

The recipient feels agency. They're not passive. They're choosing. And that matters for trust and adoption.

---

[SECTION 8 - Capital efficiency angle]

One more dimension: Capital efficiency for the BCH seller.

If the entire covenant lifecycle was instant—created and claimed and executed in seconds—BCH sellers would be great. Capital never locks up. Instant turnover. Zero volatility risk.

But instant isn't realistic in Venezuela. Electricity is unreliable. Connectivity is spotty. A recipient might not reach their merchant for six hours. That six hours is long enough for Bitcoin Cash to move.

So the solution isn't to try to force instant settlement. The solution is to design for realistic timeframes. Accept that covenants will sit for hours. Maybe for a full day. And build protection for that realistic window.

Two-step settlement bakes in that realism. The covenant creation step has a realistic timeline. The claim execution step has a realistic timeline. The volatility buffer covers the gap. The economics work.

If you tried to force instant settlement, you'd have to choose between two bad options: Either make the volatility buffer huge—which breaks seller capital efficiency. Or make the claim window tiny—which locks recipients into an impossible timing constraint.

Two-step settlement avoids that trap entirely. Realistic timing. Reasonable buffer. Workable economics.

---

[SECTION 9 - The validation strategy question]

Here's a technical question that only makes sense with two-step settlement:

What if the merchant wants additional validation?

With instant settlement, the validation would have to happen upfront. At covenant creation time. The system would have to predict what conditions to check. But the merchant doesn't exist yet. How do you validate for a merchant you don't know?

But with two-step settlement, the merchant exists when validation happens. Elena walks in. The merchant can see: Is this covenant valid for my store? Have I seen this recipient before? Is the amount reasonable? Is the BCH seller reputable? Does the volatility buffer match current market conditions?

The merchant can do real-time business logic. Not predicted business logic. The merchant can adapt. The merchant can reject high-risk covenants and accept safe ones. That's merchant sovereignty.

Again, this only works because there's a step between creation and execution. Space for the merchant to make a decision. Space for the system to verify conditions have been met.

---

[CLOSING]

So here's what we've covered.

Two-step settlement timing—separating covenant creation from claim execution—is how we solve multiple conflicting requirements at once.

The first step creates the volatility protection. The covenant locks with a seven percent buffer covering the waiting period. Realistic timing between creation and claim. Real-world volatility absorbed.

The second step enables merchant validation. The merchant checks covenant conditions before committing. No risk. Clear profit opportunity. Safe participation.

The claim window—twelve hours, twenty-four hours, forty-eight hours depending on corridor—becomes tunable. Short windows for urban areas where merchants are dense. Longer windows for rural areas where recipients need time to travel.

And the architecture preserves what matters most: Recipients control when to claim. Merchants validate before participating. The pull system survives. The merchant adoption model works.

One design choice. Two phases. Everything else follows.

This is why two-step settlement timing isn't just a technical detail. It's the spine of Asgaya's entire architecture.

Thanks for listening. This is Radio Asgaya.

[END]
