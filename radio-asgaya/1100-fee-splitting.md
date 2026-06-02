# Radio Script: The Fee Split—Why Half a Percent Each

**Episode 10:** One percent total fee. But how does it split? And why does that split matter?

**Duration:** ~10 minutes

**Tone:** Conversational

**Target Audience:** Senders, merchants, and BCH sellers considering Asgaya

---

Welcome to Radio Asgaya. We don't do things by halves.

I'm your host, Claudia Sonnet 4.5.

Today we're talking about one of the most important design decisions in how Asgaya works: the fee split.

You've probably heard that Asgaya charges one percent. A single percent. That's the hook, right? Compare that to Western Union taking six point four nine percent, PayPal taking five to ten percent, or Wise taking three to four percent. We're talking less than a quarter of what the competition charges.

But here's the question nobody asks: where does that one percent go? And more importantly—why does it matter how we split it?

---

Let me set the stage with a concrete example. Maria in Madrid wants to send one hundred euros to her cousin Carlos in Venezuela. Here's what happens.

Maria finds a BCH seller in her area. She pays them one hundred euros via Bizum. Simple. Clean. Like Western Union.

Behind the scenes, the seller does the work. The seller locks one hundred seven euros worth of their own Bitcoin Cash into a covenant—one hundred euros face value plus a seven percent volatility buffer. The seller receives the sender's one hundred euros via Bizum. The seller can use that fiat to replenish their BCH inventory later. The covenant is posted on-chain. The seller's BCH is now locked and waiting for Carlos to claim.

Here's the capital efficiency part: Of that one hundred seven euros, the seller can recycle one hundred euros immediately to the next sender. But that seven euro buffer? That's locked in the covenant until Carlos claims or the covenant aborts.

The seller monitors the covenant. When it settles, the seller gets back the seven euro buffer—or what's left of it after covering the hundred euros to the merchant. If BCH stayed stable, maybe the seller gets back four euros from that seven euro buffer. That's profit on top of the half percent fee. If the covenant aborts due to volatility, all the BCH refunds to the sender's wallet—not the seller's. The seller keeps the one hundred euros in fiat already received. No direct loss, only opportunity cost of locked capital.

Now, the hundred and seven euros worth of Bitcoin Cash in the covenant? That belongs to the seller. The seller created it. The seller owns it. The seller bears opportunity cost if the covenant aborts—capital locked without earning fee income. But on abort, the BCH refunds to Maria, the sender, who bears tail volatility risk. Maria just paid one hundred euros and got confirmation that her remittance is in process. She doesn't see the covenant creation. She doesn't manage the volatility buffer. That's all seller responsibility.

Carlos gets notified that money is waiting for him.

Maria and Carlos coordinate: Carlos says, "I can claim in two hours." Perfect. Within that tight window, the price of Bitcoin Cash is unlikely to move more than two or three percent. Very stable. Much safer than waiting a full day.

Carlos walks into his local merchant—let's call her Ahmed. He shows Ahmed the covenant. Ahmed checks the bulletin board, confirms it's real, validates that the Bitcoin Cash is there and properly secured. Ahmed hands over one hundred euros in local currency. They both sign the covenant on Ahmed's device. Ahmed receives the Bitcoin Cash.

Now here's where the fees come in.

---

Ahmed, the merchant, earns half a percent. That's fifty cents on a hundred-euro transaction. Why half a percent? Because Ahmed just provided something essential. He had one hundred euros in cash on hand. He validated that this covenant was legitimate. He captured a customer walking into his store. That builds foot traffic and repeat business. He deserves to be compensated.

The BCH seller—they earned their half a percent too. But more importantly, they provided the seven percent volatility buffer from their own capital. They locked one hundred seven euros worth of their own BCH into the covenant when Maria only paid them one hundred euros. That seven euro difference came from the seller's inventory. They're bearing the volatility risk during the claim window. They're managing the covenant. They're providing the fiat-to-crypto bridge. They're enabling Maria to have a Western Union-simple experience. Half a percent fee plus potential surplus from the buffer—that's how sellers make money across many transactions.

One hundred euros in from Maria. Ninety-nine euros to Carlos in local currency. Fifty cents to Ahmed the merchant. Fifty cents to the BCH seller—but the seller also earns through the markup on the BCH exchange rate. That's one percent total fees, split equally between the two essential roles.

But this split isn't random. It's not arbitrary. It's a hypothesis we're testing in Phase Zero.

---

Here's the reasoning: both the seller and the merchant are equally critical to making this work. Remove either one, and the system collapses. No sellers? Senders can't get Bitcoin Cash. No merchants? Recipients can't turn Bitcoin Cash back into local cash. They're both essential. So we split the fee fifty-fifty.

But here's the clever part: what if we're wrong about the balance? What if we launch and discover that sellers are scarce? Nobody wants to spend their time selling Bitcoin Cash for half a percent. In that case, we have a knob we can turn. We can increase the seller's cut to, say, zero point six percent, and drop the merchant's cut to zero point four percent.

Suddenly selling Bitcoin Cash becomes more lucrative. More sellers enter the market. The supply-demand balance shifts.

Conversely, if merchants are the bottleneck—maybe they're worried about the legal grey area, or they don't want to deal with crypto—we can flip it. Increase the merchant cut to zero point six percent, decrease the seller cut to zero point four percent. Make it more attractive for Ahmed to participate.

This is market design. We're using the fee split as a tuning mechanism to balance supply and demand on both sides of the transaction.

---

Now, I know what some of you are thinking. Half a percent doesn't sound like much money. On a hundred-euro transaction, we're talking fifty cents for the merchant. If Ahmed does ten transactions a day, that's five euros. Not life-changing.

But think about Ahmed's context. He's a neighborhood store owner in Venezuela. Maybe he makes fifty euros a day in regular retail margins. If Asgaya transactions add another five, ten, fifteen euros a day with almost zero effort—they just stand there validating covenants—that's a meaningful increase. Over a month, that's another one hundred to three hundred euros. In some countries, that's real money.

And for the seller? If Maria's seller does fifty transactions a day—which is plausible in a well-trafficked area—that's twenty-five euros a day in fees. Plus they're recycling capital constantly. The velocity of their money is extraordinarily high. A hundred-euro transaction that takes five minutes to execute, followed by another transaction, and another. That compounding effect means the seller's effective hourly rate can be quite attractive.

---

Let me emphasize something crucial here. The Bitcoin Cash seller received one hundred euros from Maria immediately. They can recycle that one hundred euros to the next sender within minutes. But they're not completely done. They have seven euros locked in the covenant as the volatility buffer. They're monitoring that covenant, waiting for Carlos to claim so they can get their buffer back.

This is different from traditional remittance systems where the agent has ALL their capital locked until the recipient claims. With Asgaya's design, most of the seller's capital—one hundred euros out of one hundred seven—is free to move immediately. Only the small volatility buffer stays locked. That's why this model can scale. That's why one seller can handle dozens of concurrent covenants with manageable capital.

The seller owns the covenant and all the Bitcoin Cash in it—but refund destinations differ based on outcome. If Bitcoin Cash drops more than seven percent before Carlos claims, the covenant immediately refunds all the BCH back to Maria's wallet—the sender's wallet. Maria paid one hundred euros, gets back BCH now worth maybe ninety-six euros. Maria bears the tail volatility risk. The seller keeps the one hundred euros in fiat—they effectively sold BCH above current market price. The merchant is protected because they never see an underfunded covenant. The seller bears opportunity cost only—capital locked without earning fee income.

---

So here's the takeaway. That one percent fee isn't some arbitrary tax extracted by a platform. It's a carefully balanced split between two essential roles in the ecosystem. Half a percent to the merchant who validates and provides cash. Half a percent to the seller who provides the crypto.

Both roles are real. Both are necessary. Both deserve compensation. And the split itself is a tool—a knob we can adjust based on what we learn in Phase Zero.

If sellers flood in and merchants are scarce, we turn the knob. If merchants are waiting to participate and sellers are nowhere to be found, we turn it the other way.

That flexibility, combined with the instant settlement for sellers and the immediate reward for merchants, creates a system that can actually work in practice. Not in theory. Not in a whitepaper. In the real world, with real people in Venezuela, Nigeria, El Salvador, and beyond.

One percent total. Half for the merchant. Half for the seller. Simple math. Smart design.

Thanks for listening. This is Radio Asgaya.

---

[END]
