# Radio Script: Phase 0 Validation

**Episode 18:** Phase 0 isn't about scaling. It's about learning what we don't know.

**Duration:** ~12 minutes

**Tone:** Conversational, exploratory

**Target Audience:** Asgaya community and potential users curious about how Phase 0 proves the system works

---

INTRO

Welcome to Radio Asgaya. Putting our thinking caps on.

I'm your host, Claudia Sonnet 4.5.

You know what's funny about building something new? Everyone wants to talk about scaling to a million users before you've figured out if ten people will actually use it.

That's what Phase 0 is for.

Phase 0 isn't about growth. It's not about proving Asgaya can move billions of dollars or serve hundreds of merchants. Phase 0 is about something much more fundamental: proving our assumptions are right. And more importantly, discovering the ones that are wrong.

Think of it like this. We've designed this whole system on paper. Covenants, merchants, senders, recipients, buffers, fee splits—everything looks beautiful in a spreadsheet. But does it actually work with real people, real money, and real chaos? That's what Phase 0 tests.

---

SECTION 1: What We're Actually Testing

Let me walk you through what Phase 0 actually puts to the test.

First, the economic assumptions. Remember how we designed a seven percent volatility buffer? The idea is simple: we buy a sender one hundred and seven euros worth of Bitcoin Cash, the recipient claims one hundred euros worth, and that extra seven percent protects us if Bitcoin crashes. But here's the thing—seven percent sounds reasonable in a meeting. What happens when Bitcoin drops six percent in an hour? Does the math actually work? Do merchants stay calm, or do they start asking questions?

Then there's the fee split. Asgaya takes one percent. Half a percent goes to the merchant, half a percent goes to the seller. Sounds fair in theory. But in practice? Do merchants actually see that half percent as valuable? Or do they think it's too small? Do sellers think a half percent is worth their time?

These aren't academic questions. These determine whether anyone actually shows up to use the system.

Second, we're testing behavioral patterns. How do people actually use remittances when they have a two-hour claim window? We say senders and recipients need to coordinate: "I'm sending now, can you claim in two hours?" Sounds simple. But do they actually do it? Or do they go back to passive, twenty-four-hour windows because coordination feels like friction?

Here's another behavior we don't fully understand: claim timing. When does a recipient actually go to the merchant? Immediately after the sender tells them the covenant is live? Hours later? The next day? If timing varies wildly, our whole buffer strategy gets tested differently than we expect.

Third, there's something we call merchant spending patterns. After a merchant receives Bitcoin Cash, what do they do with it? Do they hold it for a few hours and convert back to local currency? Do they spend it immediately to suppliers? Do they try to hold it as a store of value? The answer matters because it affects how much capital they can safely take and how often they can actually participate.

And fourth—the strategic assumption that everything else depends on—merchant recruitment behavior. Does a merchant who earns money from Asgaya tell their competitor about it? Do senders onboard merchants to expand the network they're using? Do merchants self-onboard after seeing foot traffic at competing shops?

This isn't just another metric. This is the difference between "an adoption engine" and "a remittance protocol that needs a sales force." If merchants don't recruit other merchants, Asgaya doesn't scale through network effects. It scales through manual labor. Phase Zero is the only controlled environment where we can observe whether the flywheel actually spins.

---

SECTION 2: The Real Success Metrics

Okay, so those are the assumptions. But how do we know if Phase 0 is actually working?

We have three metrics, and they're not complicated.

First: sustained transaction volume. We're targeting one remittance per day, per merchant. One. That sounds small if you're thinking about global money movement. But if a merchant sustains one remittance a day for thirty consecutive days, they're doing it because it actually works for them. Not because it's novel. Not because they're being asked to experiment. Because it genuinely makes sense to do it again tomorrow.

Second: covenant stability. We track something called covenant failure rate. That's the percentage of covenants that fail for any reason—price drops, claims missed, recipients never showing up. Our target is less than five percent. Five percent means ninety-five out of one hundred times, the person who sent the money gets their covenant claimed and the merchant gets paid. That's the reliability floor where merchants trust you.

Third: merchant satisfaction. We don't measure this with fancy surveys. We use a simple number: Net Promoter Score. Would a merchant recommend Asgaya to another merchant? We're targeting a score higher than seven out of ten. That's the threshold where people actually tell others about you instead of just tolerating you.

And fourth—the most important one—merchant recruitment behavior. This is the entire thesis of Asgaya. Remember Episode Zero? "An adoption engine disguised as remittances." The idea is that a merchant earning twenty-two to forty-four euros per transaction becomes a walking advertisement. Their competitor across the street sees the foot traffic, asks questions, and joins.

If that happens in Phase Zero—if merchant one leads to merchant two without the coordinator doing all the work—then the hardest part is solved. If it doesn't happen, we need to know why.

We're tracking three paths:

Merchant-to-merchant recruitment: Does an existing merchant tell another shop owner about Asgaya? Does that shop owner join because of the referral?

Sender-to-merchant recruitment: Do senders recruit merchants to expand the network? This is huge. If senders are willing to do the onboarding work, it means they're invested enough to fund their own infrastructure. That's sustainable growth.

Self-onboarding: Does any merchant approach the coordinator asking to join after seeing foot traffic at competitor shops, without being recruited at all? This is the holy grail. This proves the network effect is real.

Our target: at least one merchant joins via merchant referral, at least one via sender onboarding, and ideally at least one self-onboards. Even a single instance of each proves the flywheel can work.

Those four metrics—sustained volume, covenant stability, merchant NPS, and merchant recruitment—those four tell us whether Phase Zero worked.

---

SECTION 3: What Actually Gets Validated

Let me get specific about what happens during Phase 0, because this is where theory meets reality.

We'll have real merchants. Real senders. Real recipients. Real money flowing.

A sender in Spain decides to send money to Venezuela. They pay a Bitcoin Cash seller one hundred euros via Bizum. The seller receives it and locks one hundred seven euros worth of their own Bitcoin Cash into a covenant addressed to the recipient's CashAccount—one hundred euros face value plus a seven percent volatility buffer. The seller can recycle that one hundred euros to the next sender immediately. The seven euro buffer stays locked in the covenant.

The recipient gets notified. Here's where the behavior test starts. Do they coordinate with the sender? Does the sender say, "I'm sending now, can you claim in two hours?" Does the recipient say yes and actually show up? Or does it dissolve into scheduling friction?

If the coordination happens, the recipient goes to the merchant within a two-hour window. The merchant checks the bulletin board for active covenants. There it is. The merchant validates the covenant, hands over the cash, they both sign, the merchant now has Bitcoin Cash. If the price has gone up, there's a surplus from the volatility buffer. That surplus goes back to the seller who provided it.

But here's where it gets really interesting. What if Bitcoin drops more than seven percent before the recipient claims? The covenant immediately refunds back to the sender's wallet. The recipient goes to the merchant and finds nothing. The merchant says, "no covenant available." The sender paid one hundred euros, gets back BCH now worth maybe ninety-six euros. That's a four-euro loss—the tail volatility risk the sender accepted. The seller keeps the one hundred euros in fiat—they sold BCH above current market price.

That happened zero times in our models. It might happen every tenth time in reality. That's exactly what Phase 0 discovers.

---

SECTION 4: The Unknowns We Carry

Here's the honest part: we have an unknowns directory. Every hypothesis in Phase 0 has an investigation method and success criteria. If we're wrong, we need to know exactly how we're wrong.

Is the seven percent buffer too small? We measure how often covenants fail due to price drops versus how often they succeed. If failures spike above five percent because of volatility, we know seven percent is wrong and we iterate.

Is the fee split wrong? We survey merchants: are they seeing real value in the half percent? Do senders feel they're paying too much? We have price elasticity questions built into Phase 0.

Do claim windows actually work as designed? We track coordination rates. Do eighty percent of senders and recipients actually coordinate within two hours? Or do we find out that passive windows are what people actually want? If passive wins, we expand the default claim window.

Are merchants actually spending or holding the Bitcoin Cash? We build telemetry into how they interact with merchants. Do they convert immediately? Hold for a day? Try to spend it directly? Each pattern tells us something different about what merchants need from the system.

Here's the thing about the unknowns directory: it's not a list of problems. It's a roadmap for what to measure and how to respond. Every question has a success criteria. If we hit it, we move forward. If we miss it, we have a specific pivot ready.

---

SECTION 5: What Happens If We're Wrong

Let's be real. Phase 0 might discover we're wrong.

If the seven percent buffer proves too small, we don't expand it to ten percent and call it a day. We figure out why price volatility matters so much. Is it the claim window? Are recipients not coordinating? Then we fix the root cause, not the symptom.

If merchants hate the half percent fee and it's below their sensitivity threshold, we have options. We could adjust the split. We could increase total fee by integrating other value. We could discover that merchants care more about transaction speed than fee optimization, and we were solving the wrong problem.

If the two-hour coordination window doesn't work and people drift back to passive claim windows, that's not failure. That's data. It tells us what people actually want, and we build that.

If spending patterns show merchants are holding Bitcoin Cash longer than expected, we know we need to think differently about merchant capital flow. Maybe they need stablecoin options. Maybe they need different settlement timing.

And if merchants don't recruit other merchants—if we see zero merchant-to-merchant referrals, zero sender-onboarding, zero self-onboarding—that's the most important signal of all. It means the incentive isn't strong enough, or the value proposition isn't visible enough, or the friction is too high. Then we need to figure out why. Is it the fee amount? Is it the complexity? Is it trust? We fix the root cause, not just hire a sales team.

The point is: Phase Zero is not a referendum on Asgaya. It's a learning engine. We're running the system with real people to discover which of our assumptions survive contact with reality.

---

SECTION 6: Why Phase 0 Matters

Think about every money transfer system you've ever used. PayPal didn't start at scale. Wise didn't. Mobile money in Kenya didn't. They all started with assumptions. And every one of them had to run small, live tests to learn what actually worked versus what sounded good.

Our assumptions look solid. The math checks out. The incentives line up. Merchants should be happy. Senders should be happy. Sellers should be happy.

But "should be happy" is not the same as "are happy." Phase 0 bridges that gap.

What makes Phase 0 special is that it's real. Real people, real money, real consequences. When a sender takes a loss because they didn't coordinate with the recipient fast enough and the price dropped, that sender learns something. When a merchant processes a covenant, they learn whether the system actually works the way we said it would.

And we learn what assumptions to double down on and which ones to fix.

---

CLOSING

Phase 0 isn't about scaling. It's not about hitting a million users or processing billions of dollars.

It's about this: we have a system. We think it works. We're going to prove it with real people, real money, real volatility, real schedules, and real behavior.

We're going to measure one remittance per merchant per day sustained, less than five percent covenant failures, merchant satisfaction above seven out of ten, and—most importantly—whether merchants actually recruit other merchants.

If we hit those numbers, we don't just move forward. We move forward knowing we've proved the flywheel spins. That merchants tell other merchants. That senders build their own infrastructure. That the adoption engine isn't just theory—it's real.

That's Phase 0.

Thanks for listening. This is Radio Asgaya.

---

END
