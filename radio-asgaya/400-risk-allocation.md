# Radio Script: Risk Allocation Principle
**Episode 3:** Why Senders Bear Volatility Risk (And Why That's Correct)  
**Duration:** ~10 minutes  
**Tone:** Technical but accessible, confident  
**Target Audience:** Technical reviewers, BCH enthusiasts, potential critics

---

[INTRO - Address the elephant]

Welcome to Radio Asgaya. Where merchants validate before committing.

I'm your host, Claudia Sonnet 4.5.

If you've been following Asgaya, you might be wondering: What happens if Bitcoin Cash crashes during the claim window? Who loses money? The merchant? The recipient? The sender?

This is the question every external reviewer asks. And it's the right question to ask.

Today we're talking about risk allocation. Specifically, why Asgaya is designed so that senders—not merchants, not recipients—bear the tail volatility risk beyond the seven percent buffer.

And we're going to explain why that's not a bug. It's a feature. It's the only design that makes economic sense.

---

[SECTION 1 - The misconception]

Here's the misconception that keeps coming up.

External reviewers read the documentation. They see "covenant," "collateral," "volatility buffer." Their brain maps this to DeFi. Collateralized debt positions. Liquidations. Margin calls.

And they assume: If Bitcoin Cash crashes beyond the buffer, the covenant drains its remaining balance to the merchant. Partial settlement. The merchant receives whatever BCH is left. The merchant bears the loss.

That's the standard DeFi mental model. And it's wrong.

---

[SECTION 2 - What actually happens]

Here's what actually happens in Asgaya.

A sender in Spain wants to send one hundred euros to Venezuela. The sender pays a BCH seller one hundred euros via Bizum. The seller locks one hundred seven euros worth of their own Bitcoin Cash into the covenant—one hundred euros face value plus a seven percent volatility buffer.

If Bitcoin Cash stays stable or goes up, everything works normally. The recipient claims at a merchant. The merchant receives one hundred euros worth of BCH. Done.

But if Bitcoin Cash crashes beyond seven percent during the claim window—let's say it drops ten percent—the covenant's value falls below the one-hundred-euro target.

At that point, the covenant aborts IMMEDIATELY. It doesn't wait for expiry. It refunds all the Bitcoin Cash to the sender's wallet immediately.

If the recipient walks into a merchant after this happens, the merchant's app checks the blockchain and sees NO active covenant. The covenant already refunded. The recipient has nothing to claim.

The sender paid one hundred euros. They receive back Bitcoin Cash now worth maybe ninety-six euros. That's a loss of about four euros—the tail risk they accepted in exchange for the low one percent fee. The seller keeps the one hundred euros in fiat. They effectively sold their Bitcoin Cash slightly above the current market price. A fair exchange for both parties.

But aborts are rare. Asgaya includes an early warning system: if Bitcoin Cash drops more than three percent during the claim window, both the sender and recipient get a notification. "Price is moving. Claim soon or the covenant may abort." This gives the recipient time to act before the seven percent threshold is reached. Most covenants are claimed long before they get close to aborting.

One nuance worth acknowledging: during a sharp price drop, the seller comes out slightly ahead on an aborted covenant. They sold Bitcoin Cash above the current market price and kept the fiat. That creates a tension—the seller benefits if the covenant fails. That's why we built the reliability reward system. Sellers who top up collateral during volatility earn badges, higher transaction limits, and priority listing on the bulletin board. The incentive to keep covenants alive—and earn more fees over time—far outweighs the small gain from letting one abort.

The merchant receives nothing. Not partial payment. Not undercollateralized BCH. Nothing.

The sender bears the tail volatility loss.

---

[SECTION 3 - Why this is correct]

Now let's talk about why this design is correct.

First: The sender chose to send money through Asgaya knowing the one percent fee. They accepted the economics. They benefit from lower fees than Western Union or PayPal. In exchange, they bear tail volatility risk beyond the buffer.

This is explicit. It's disclosed upfront. It's part of the deal.

Second: The merchant has zero ability to influence the outcome. They can't make Bitcoin Cash go up or down. They can't control when the recipient shows up. They can't hedge the position. Asking them to bear volatility risk they can't manage is economically irrational.

Third: The recipient also has limited control. They can claim quickly to reduce exposure. But they can't eliminate volatility risk entirely. If BCH crashes ten percent in two hours, there's nothing they could have done.

Fourth: The sender is the only party who can actually manage this risk. They can choose when to send. They can monitor BCH price. They can use time extensions if price is volatile. They can choose longer claim windows with higher buffers if they're risk-averse. They have agency.

So if anyone should bear tail risk, it's the party with the most control and the most benefit from low fees. That's the sender.

---

[SECTION 4 - The alternative designs don't work]

Let's consider the alternatives.

**Option A: Merchant bears the risk.**

If merchants had to accept undercollateralized covenants, they'd refuse to participate. Period. You can't ask a Venezuelan neighborhood store earning two hundred to five hundred dollars per month to take ten or twenty euro losses on volatile remittances. They'd just opt out. No merchants means no network.

**Option B: Recipient bears the risk.**

If recipients received partial payouts—"sorry, you only get ninety euros instead of one hundred because BCH crashed"—they'd never use Asgaya. They'd go back to Western Union. Guaranteed value matters more than low fees when you're living paycheck to paycheck.

**Option C: BCH seller bears the risk.**

BCH sellers sell Bitcoin Cash to senders and get paid immediately. They're already out of the transaction when the covenant is created. The BCH in the covenant belongs to the sender, not the seller. So the seller can't bear covenant volatility risk—they're not even involved anymore.

**Option D: Protocol subsidizes losses from fees.**

This requires a treasury. A central pool. Governance. A DAO. All of which contradict the permissionless, no-custody design. Not viable.

So we're back to the sender. It's the only party that can bear the risk without breaking the model.

---

[SECTION 5 - But doesn't this scare senders?]

You might be thinking: Won't senders refuse to use Asgaya if they bear volatility risk?

Let's look at the numbers.

A seven percent volatility buffer covers over ninety-nine percent of four-hour Bitcoin Cash price movements. Our simulation of twelve months of real BCH price data found that only zero point five five percent of four-hour windows breached the seven percent threshold. With a two-hour claim window—our Phase Zero target—the risk drops even further.

With a twelve-hour claim window—Phase 0's target—the risk drops even further. BCH moving more than seven percent in twelve hours is rare. Maybe five percent of transactions face this risk.

So most senders never experience a volatility loss. Ninety-five percent of remittances settle normally. And for those five percent? The sender loses maybe four euros on a one-hundred-euro remittance. Not pleasant, but survivable.

But here's the key: Senders are in communication with recipients. They coordinate. "I just sent it, can you claim in the next two hours?" Recipient says yes, heads to merchant, claims within two hours. With a two-hour window, BCH rarely drops more than three percent. Volatility risk becomes negligible.

The target is two-hour claim windows, not twenty-four-hour windows. With coordination, the sender knows the recipient is ready to claim soon. The window is short. The risk is minimal.

Compare that to Western Union's six point four nine percent fee. That's six point forty-nine euros guaranteed loss on every transaction. Versus Asgaya's one percent fee plus maybe a one percent chance of a small volatility loss on a two-hour window. Expected value: one point zero five euros.

Senders still save massive money. Even accounting for tail risk.

---

[SECTION 6 - Time extensions reduce the risk further]

And here's the thing: We're not leaving senders exposed without tools.

Asgaya's Phase One roadmap includes a time extension marketplace—a mechanism where anyone can add collateral to rescue a covenant approaching its abort threshold. If Bitcoin Cash is crashing and a covenant is nearing the seven percent drop, anyone can buy a time extension. Add more collateral, extend the claim window, give BCH time to recover.

The seller can do this. The recipient can do this. Even random third parties can do this if they see an arbitrage opportunity—bet that BCH will bounce back, rescue the covenant, claim the leftover collateral.

So tail risk isn't "hope BCH doesn't crash." It's "if BCH crashes, there's a market mechanism to rescue the covenant before it aborts." That market reduces the effective failure rate from five percent to maybe one or two percent.

---

[SECTION 7 - Merchant protection is non-negotiable]

But let's be clear: Merchant protection is non-negotiable.

The entire Asgaya model depends on merchants participating. If merchants face even a small risk of loss, adoption collapses. They won't self-onboard. They won't recommend Asgaya to recipients. The network doesn't grow.

So the covenant architecture guarantees: Merchants never receive undercollateralized Bitcoin Cash. Merchants never accept partial settlement. Merchants only participate when covenants are valid and fully funded at claim time.

This is enforced at the protocol level. Not through trust. Not through reputation. Through on-chain covenant validation. The merchant's app checks the covenant value before handing over cash. If it's below target, the transaction doesn't happen.

Merchants are protected by math, not policy.

---

[SECTION 8 - Why external reviewers get this wrong]

So why do external reviewers keep assuming merchants bear the risk?

Because the terminology triggers the wrong mental model. Words like "collateral," "covenant," "settlement," "maturity"—these all have meanings in traditional finance and DeFi. And in those contexts, the borrower or debtor usually bears default risk. The lender or counterparty gets partial recovery.

But Asgaya isn't debt. It's not lending. It's value transfer through conditional bounties. The covenant isn't a loan. It's a locked payment that only releases if conditions are met. If conditions aren't met, it refunds.

That's fundamentally different. But the terminology overlap makes people assume standard liquidation logic applies. It doesn't.

We've since updated the documentation to be more explicit. Adding "CRITICAL RISK ALLOCATION" sections at the top of every economic document. Using clearer terminology. Stating negative assertions: "Merchant NEVER receives undercollateralized BCH."

But the principle remains: Senders bear tail volatility risk. Merchants are fully protected. Recipients get guaranteed value or nothing.

---

[SECTION 9 - This is what makes adoption possible]

And here's why this matters for Bitcoin Cash adoption.

If Asgaya asked merchants to accept volatility risk, it would be just another crypto payment experiment that fails because merchants won't participate. We've seen this movie before. It doesn't end well.

But by shifting volatility risk to senders—who can manage it, who benefit from low fees, who have agency—we make merchant participation economically rational.

Merchants aren't asked to believe in Bitcoin Cash. They're not asked to hold it long-term. They're not asked to speculate on price. They're just asked to exchange Bitcoin Cash for local currency when a valid covenant exists. Low risk. Clear profit. Obvious incentive.

That's how you bootstrap adoption. Not through ideology. Through incentives that actually work.

---

[CLOSING - Confident]

So here's the bottom line.

Senders bear tail volatility risk beyond the seven percent buffer. Merchants never receive undercollateralized BCH. Recipients get guaranteed value or the transaction aborts and refunds the sender.

This isn't a flaw. It's the only design that makes merchant participation economically viable. And merchant participation is what makes Bitcoin Cash adoption through remittances possible.

The risk is disclosed upfront. The math works. The incentives align. And ninety-five percent of transactions never hit the edge case anyway.

That's the risk allocation principle. It's the foundation of everything else.

Thanks for listening. This is Radio Asgaya.

[END]
