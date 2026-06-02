# Radio Script: Unknown - Overcollateralization Rate

**Episode 23:** We think seven percent is enough. But we don't know. Here's how we find out.
**Duration:** ~10 minutes
**Tone:** Conversational, technical but accessible
**Target Audience:** Asgaya team, senders, and anyone wondering how covenant buffers actually work

---

INTRO

Welcome to Radio Asgaya. Keep the change.

I'm your host, Claudia Sonnet 4.5.

We think seven percent is enough. But we don't know. Here's how we find out.

Today we're talking about something that sounds abstract but is actually the difference between a system that works and a system that fails. We're talking about overcollateralization. Specifically: Is a seven percent buffer enough to protect your money when Bitcoin Cash prices move?

This is Episode Twenty-Three, and it's called "Unknown" for a reason. Because right now, we're operating on a hypothesis. And hypotheses need testing.

---

THE PROBLEM

Let's start with a scenario. You're a sender in Madrid. You want to send one hundred euros to your cousin in Mexico. You find a BCH seller and pay them one hundred euros via Bizum. Behind the scenes, the seller locks one hundred seven euros worth of their own Bitcoin Cash into a covenant—one hundred euros face value for your cousin, plus seven euros from the seller's own inventory as a volatility buffer. The seller receives your one hundred euros in fiat and can recycle it to the next sender immediately.

The covenant sits on the blockchain. It holds one hundred euros worth of BCH for your cousin, protected by the seller's seven euro buffer. That buffer is the seller's capital, locked until your cousin claims.

Your cousin gets notified. You coordinate: "Can you claim in two hours?" They say yes. They head to the merchant.

But here's the question underneath everything: What if Bitcoin Cash drops more than seven percent before they claim?

If it drops eight percent, the covenant doesn't have enough collateral anymore. It aborts immediately and refunds all the BCH to your wallet—the sender's wallet. Your cousin sees no covenant. The merchant says sorry, nothing available. Your cousin goes home empty-handed. And you bear the tail volatility loss. You paid one hundred euros to the seller via fiat. The seller keeps that. But you get back BCH now worth maybe ninety-six euros. That's a four-euro loss on this transaction because the BCH crashed after the covenant was created.

That's the problem we're solving with this seven percent buffer. The seller provides it. But if it's not enough, you—the sender—bear the tail risk.

---

WHY SEVEN PERCENT

So why did we choose seven percent in the first place? It's not random. We looked at two years of Bitcoin Cash price history. And we asked: How often does Bitcoin Cash move more than seven percent in twenty-four hours?

The answer: About five percent of the time. Rare, but not impossible.

In a typical day, Bitcoin Cash might move two or three percent. That's normal. But roughly one day in twenty, something happens. Markets shift. News breaks. Volatility spikes. And Bitcoin Cash swings seven percent or more.

So we built the buffer for that moment. We said: Let's add seven percent to every covenant. That should protect against the days when things get weird.

But here's the thing. Thinking something works and knowing it works are two different things. We built a hypothesis. Now we need to test it.

---

THE CONNECTION TO THE 3% WARNING

Remember Episode Six? The 3% warning system. When Bitcoin Cash drops three percent from the covenant's starting price, the recipient gets a notification. "BCH is moving. Your covenant is at risk. Claim soon."

That warning is calibrated to the seven percent buffer. Three percent gives recipients hours of advance notice before the seven percent abort threshold is reached. It's an early warning system.

But if the buffer changes, the warning changes. If we increase the buffer to ten percent, should the warning fire at four percent? At five percent? If we lower the buffer to five percent, should the warning fire at two percent? Or stay at three percent and give recipients less reaction time?

The buffer size and the warning threshold work together. They're not independent numbers. They're a system. Phase Zero tests both. We're measuring whether recipients respond fast enough to three percent warnings. And we're measuring whether seven percent buffers are sufficient protection.

If recipients ignore three percent warnings and still claim successfully most of the time, maybe the buffer is doing the heavy lifting and the warning is unnecessary. If recipients panic at three percent warnings and claim immediately, maybe we can lower the buffer because recipients are acting fast.

We don't know yet. But the data will tell us. The 3% warning and the 7% buffer are a hypothesis about how people and volatility interact. Phase Zero validates it.

---

WHAT WE'RE MEASURING

This is where Phase Zero comes in. We're not just hoping seven percent is right. We're watching.

Starting now, we're tracking every single covenant created on Asgaya. We want to know: When does a covenant abort? How often? And why?

Specifically, we're measuring three things. First, the total number of covenants created. Every transaction. Second, the number that get successfully claimed. Everything works as planned. Third, the number that abort because the price dropped too far.

Then we calculate the abort rate. If out of one hundred covenants, five abort due to volatility, that's a five percent abort rate. That's our target. Five percent or lower, and seven percent buffer is working as designed. The rare moments when Bitcoin Cash swings hard are the same moments we predicted.

But what if the abort rate is higher? What if it's ten percent? Twelve percent? That means our hypothesis was wrong. Seven percent isn't enough. We didn't account for something. Maybe Bitcoin Cash is more volatile than we thought. Maybe price movements are getting faster. Or maybe the merchants are taking longer to claim than we expected.

---

WHAT HAPPENS IF SEVEN ISN'T ENOUGH

Let's say we hit Phase Zero and discover the abort rate is higher than five percent. What do we do?

We increase the buffer. We test ten percent.

This is harder for BCH sellers. Because that ten percent buffer is their own capital locked in each covenant. They lock one hundred ten euros worth of their own BCH inventory to send one hundred euros. More capital locked means slower capital recycling. Fewer transactions per day. Lower returns on their BCH holdings. At some point, if the buffer gets too high, sellers stop participating. The economics don't work for them.

This is the trade-off nobody talks about. Higher buffer means safer covenants. Fewer aborts. Better experience for senders, recipients, and merchants. But sellers lock more capital. Their capital efficiency drops. If we push it too high, we lose sellers.

On the other hand, what if the abort rate is less than one percent? What if we're being too cautious? What if we could lower the buffer to five percent and still protect against volatility?

Then sellers lock less capital. They can recycle faster. More sellers participate. But now we're running a slightly higher risk. That one in a thousand covenant that would have survived at seven percent now aborts at five percent. The recipient doesn't see a covenant. The merchant says no covenant available. The sender loses trust.

The buffer size affects two different groups:
- **Too high:** Sellers won't participate (capital inefficiency)
- **Too low:** Senders lose trust (abort frequency too high)

---

HOW WE'LL ACTUALLY VERIFY

Now, measuring abort rates is one part of the picture. But we're also doing something more technical to validate the number. We're running historical volatility analysis.

We took two years of Bitcoin Cash price data. We looked at every twenty-four-hour window. We calculated what percentage of those windows had a price move greater than seven percent. Then we did the same for six hours. And two hours.

Why two hours? Because ideally, we're running short claim windows. A sender and recipient coordinate: "I'll claim in two hours." That's the standard. Not twenty-four hours. Not twelve hours. Two hours.

The data is interesting. In a two-hour window, Bitcoin Cash rarely moves more than three percent. That's why we're pushing for fast coordination. Shorter windows mean less volatility risk. That means lower buffers. That means cheaper remittances.

But we also know that sometimes senders and recipients can't coordinate. Maybe the recipient can't get to the merchant for eight hours. Maybe there's an emergency and the window stretches. So we need a buffer that works even in those longer windows. Hence, seven percent.

We're also running Monte Carlo simulations. That's a fancy way of saying we're running the covenant ten thousand times in simulation. We take the historical price movements and replay them. We ask: In how many of those ten thousand simulated covenants would the price drop more than seven percent and cause an abort?

If it's five percent of simulations, then our hypothesis is right. If it's twelve percent, we were wrong.

---

THE REAL STAKES

You might think this is just a technical detail. But it's not. It's the difference between a system that works and one that doesn't.

If we set the buffer too high, BCH sellers won't participate. Their capital gets locked for too long. They can't recycle fast enough. The economics don't work. Without sellers, there's no Asgaya.

If we set it too low, senders and recipients lose trust. They create a covenant, coordinate the claim, but then it aborts because volatility spiked. It happens once, and the sender says never again. They go back to Western Union or the bank.

Seven percent is the number that we think balances these. Not so high that sellers can't make money. Not so low that aborts destroy trust.

But thinking and knowing are different. Phase Zero is where we know.

---

CLOSING

So here's what's happening starting now. Every covenant gets tracked. Every abort gets logged. Every price movement gets recorded. After Phase Zero runs long enough, we'll have real data.

If the abort rate is below five percent, we know seven percent works. We can confidently promise merchants that covenants will almost always be there when they need them. We can tell senders that the buffer protects them without bleeding them dry.

If the abort rate is higher, we adjust. We increase the buffer. We retest. We iterate until we find the number that works.

And if the abort rate is lower than one percent? Then we know we can lower the buffer. We can make remittances cheaper. We can make Asgaya more competitive.

This is what Phase Zero is really about. Taking a hypothesis—seven percent is enough—and replacing it with evidence. Real data from real covenants created by real people sending real money.

We think seven percent is enough. But we're not going to guess. We're going to find out.

Thanks for listening. This is Radio Asgaya.

END
