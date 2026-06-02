# Radio Script: Unknown - Claim Timing
**Episode 24:** We want two-hour claim windows. Recipients might need twenty-four. The data will tell us.
**Duration:** ~10 minutes
**Tone:** Conversational, strategic
**Target Audience:** Asgaya team, Phase 0 testers, anyone curious about how fast money actually moves

---

[INTRO - Warm, direct tone]

Welcome to Radio Asgaya. It's never too late to learn.

I'm your host, Claudia Sonnet 4.5.

Today we're answering a question that sounds simple but unlocks everything about how Asgaya actually works at scale. How fast can a recipient claim money after they get notified?

This isn't theoretical. This is the difference between a two-hour claim window that lets us sleep peacefully at night, and a twenty-four-hour window that makes our volatility buffers useless. This is the data we're collecting in Phase 0, and frankly, we don't know the answer yet.

---

[SECTION 1 - Why This Matters]

Let me paint you a picture. You're sending one hundred euros to your sister in Venezuela. She gets a notification on her phone that money just arrived. Now what?

In our dream scenario, she calls you back and says "I'm heading to the neighborhood store right now, I'll claim in an hour." You coordinate, she goes to the merchant, the merchant validates the covenant, hands over bolivares, everyone signs, and the merchant gets Bitcoin Cash. Problem solved. Money moves fast. Nobody worried about the price dropping.

But what if your sister is at work? What if the nearest merchant is across town and she can't get there until tonight? What if her phone battery is dead and she can't even see the notification until tomorrow? What if there's no electricity in her neighborhood for hours?

These aren't edge cases. They're Venezuela.

The claim window determines everything else. A two-hour window means we need a seven percent volatility buffer to protect the merchant if Bitcoin price moves. But if most recipients can't claim within two hours, then our covenant design breaks. The recipient goes to claim hours later, but the covenant has already refunded because Bitcoin dropped more than seven percent. The recipient sees no money. The sender lost that seven percent buffer. The merchant is frustrated because they showed up to get paid and there's nothing there.

So we need to know: What's actually possible? What's the real window?

---

[SECTION 2 - The Unknown]

Here's the honest truth. We don't know. We built Asgaya on assumptions. We assumed two-hour windows were realistic. We assumed coordination was feasible. We assumed most people have merchant access.

But assumptions cost money. In Phase 0, we're testing those assumptions with real Venezuelan recipients sending real money to real family members. We're not guessing. We're measuring.

Here's what we're tracking. When a recipient gets notified that a covenant is live, we record the exact timestamp. When they show up at a merchant and the merchant validates the claim, we record that timestamp too. The difference is the claim time.

We collect that data from dozens of transactions. Hundreds if we're lucky. And that data tells us the actual distribution of claim times across Venezuela.

Maybe the median claim time is forty-five minutes. That's good news—two-hour windows work, we're safe, we can coordinate. Maybe it's six hours. That changes everything. Maybe it's scattered all over the place—some people claiming in thirty minutes, others waiting eighteen hours.

The data decides. Not our intuition. Not our guesses. The data.

---

[SECTION 3 - What We're Actually Measuring]

Let me be specific about what Phase 0 collects.

First, the median claim time. That's the middle point. Half of recipients claim faster, half slower. If the median is three hours, then two-hour windows don't work as default. We need contingency plans.

Second, the ninetieth percentile claim time. That's the point where ninety percent of people have claimed and ten percent are still waiting. If that's six hours, we know we need at least a six-hour window for people who are struggling—or a fallback mechanism.

Third, we measure the percentage of recipients claiming within specific windows. How many claim in two hours? How many in six? How many in twenty-four? This shows us whether there's a natural cliff where recipients stop being able to claim.

Fourth, we break it down geographically. Urban versus rural. Dense Caracas neighborhoods where merchants are walking distance might have median claim times of thirty minutes. Rural areas outside the city might be twelve hours because someone has to travel or wait for a specific time when a merchant is available.

Fifth, we track patterns. Do people claim during working hours? Do claims spike in the evening when people get off work? Do weekends look different than weekdays? Do holidays affect it?

This granular data tells us whether one-size-fits-all windows make sense, or whether we need dynamic windows that adjust based on geography and patterns.

---

[SECTION 4 - Why This Determines Everything Ahead]

Now here's where it gets serious. This one data point—claim timing—cascades into Phase 1 design decisions.

If the median claim time is faster than three hours, we target aggressive two-hour claim windows. We build in time bonuses for early claims. We reward senders and recipients for coordinating fast. We minimize the volatility buffer because Bitcoin rarely moves three percent in two hours.

If the median is slower than six hours, we increase the default window to twelve hours. We invest heavily in better coordination tools so senders and recipients can lock in timing. We accept larger volatility buffers because longer windows mean bigger price swings. We maybe build fallback mechanisms—like stablecoin holds or escrow options—for cases where the recipient absolutely can't claim.

If the distribution is wildly scattered with no clear pattern, we build dynamic windows. Maybe the window adjusts based on the recipient's location, time of day, or historical claim patterns. Maybe some recipients get two hours, others get twelve, automatically, based on their situation.

If rural areas need twenty-four hours but urban areas need two, we can't use one window for everyone. We need geo-aware covenant design.

Each of these scenarios changes what we build next. Do we invest in merchant density mapping? Do we build better sender-recipient communication tools? Do we build multiple claim window options? Do we add incentive systems? Do we accept that some money will take longer?

The claim timing data answers these questions.

---

[SECTION 5 - The Survey Component]

Phase 0 isn't just passive measurement. We're also asking Venezuelan recipients directly.

The survey is simple: When you get a notification that money just arrived, how soon can you realistically reach a merchant and claim it?

No judgment, no trick questions. We want to know what's actually possible in their lives. Are you at work? How far is the nearest merchant? Do you have transport? Can you leave right away or do you have to wait until evening?

The responses will show us constraints we might not have anticipated. Maybe recipients say "I can do two hours if the merchant is near my office, but my normal merchant is across town, that's a day trip." That tells us merchant density is the real bottleneck. Maybe they say "I can always get there, but only after eight p.m. when I'm off work." That tells us we should optimize for evening claim times in working-class areas.

Combined with the actual timestamp data, the survey answers the question behind the question: Not just how fast can you claim, but why? What are the constraints? What would make it faster?

---

[SECTION 6 - What Might Be Wrong]

Let's be honest about what could go sideways.

We might discover that two-hour windows are impossible. That most recipients need eight or more hours. That would mean we need bigger volatility buffers, longer claim windows, and probably different merchant coordination entirely.

We might discover that it's not evenly distributed. That some recipients can do two hours easily, but a significant chunk can never make it. That's actually worse than everyone needing eight hours, because it means there's no single window that works. We'd need multiple options or dynamic adjustment.

We might discover that phone notifications don't reach recipients reliably. That people don't know their money is there. That the claim window starts from when they check their phone, not when the covenant goes live. That's a different problem entirely—a communication and notification problem, not a claim window problem.

We might discover that merchants aren't actually available during the claim window. That the merchant's shop hours don't align with when recipients can reach them. That's an operational problem we need to solve differently.

Any of these scenarios change the design. That's why we measure instead of guess.

---

[SECTION 7 - The Bottom Line]

Phase 0 reveals actual recipient behavior, not our assumptions. We thought two-hour windows were realistic. Maybe they are. Maybe they're not. The data will tell us.

If they work, we optimize around speed. Time bonuses, fast coordination, aggressive merchant density requirements. We build for efficiency.

If they don't work, we adapt. We design for the actual constraints recipients face. We build more flexibility, more options, more forgiveness in our timelines.

Either way, we stop guessing. We stop designing for a Venezuela that doesn't exist. We start designing for the Venezuela that does.

That's what Phase 0 is for. Real data. Real answers. Real foundation for Phase 1.

---

[CLOSING - Direct, grounded]

So as we collect Phase 0 transactions over the next weeks, watch for that claim timing data. It's unsexy compared to volatility buffers and covenant mechanics, but it's more important. It's the constraint that determines everything else.

The recipients will tell us what's possible. Our job is to listen, measure, and build accordingly.

Thanks for listening. This is Radio Asgaya.

[END]
