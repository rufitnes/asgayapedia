# Radio Script: Unknowns Overview

**Episode 26:** We have fourteen open questions. And we're telling you exactly what we don't know.

**Duration:** ~10 minutes

**Tone:** Conversational and strategic

**Target Audience:** Asgaya contributors and potential investigators looking to understand what we're trying to figure out

---

**INTRO**

Welcome to Radio Asgaya. Fourteen open questions we're not hiding.

I'm your host, Claudia Sonnet 4.5.

Imagine you're building something completely new. No playbook exists. You can't just copy what PayPal does or what Wise does, because this is different. This is peer-to-peer remittance using Bitcoin Cash. Truly peer-to-peer.

So here's the thing. We know a lot about how Asgaya should work in theory. We know the mechanics. We know the incentives are aligned. But there's a gap between theory and reality, and that gap is where real businesses live or die.

We've decided to be honest about that gap. We have fourteen open questions. And we're going to walk you through exactly what we don't know, why it matters, and what kind of investigation we need to answer it.

This is the philosophy of structured ignorance. Documenting what we don't know as carefully as we document what we do know. And inviting anyone in this community to help us find out.

---

**SECTION 1: Why We Document What We Don't Know**

Most startups hide their uncertainties. They project confidence. They say we've got this all figured out. But that's not how you build great things. That's how you build fragile things that break when reality hits.

So we're doing something different. We're writing down every major question we can't answer yet. And we're doing it publicly.

Why? Because uncertainty is an invitation. When you name a specific unknown, you're saying: here's a concrete problem. Here's how we'd test it. If you investigate this and you're right, you change our strategy. You become a co-founder, in effect.

That's way more powerful than generic "help us build."

And there's another reason. We're trying to avoid the classic startup trap: premature scaling. If you don't know your answer to some fundamental question, you shouldn't build the whole system yet. You should test that specific thing first. Get the answer. Then build.

So think of these fourteen unknowns as our validation checklist. Before we scale, we need to be reasonably confident in the answers to at least the top five. And we're going to tell you exactly which five.

---

**SECTION 2: The Four Categories**

The fourteen unknowns fall into four buckets. Economic questions. Behavioral questions. Technical questions. And market questions.

Economic is about the money. The margins, the fees, the incentives. The behavioral questions are about people. How do they actually act when they use Asgaya? Do they coordinate timing the way we expect? Do they spend their received cash when we expect them to?

Technical is about whether the infrastructure actually works reliably. Does the covenant validation succeed at the rates we predict? Does the notification system work? Are the price oracles accurate? Technical questions are what keep engineers up at night.

And market questions are about the big picture. Can we find enough senders? Do merchants exist at the density we need? Can anyone actually buy Bitcoin Cash when they want to?

Within each category, we've got specific questions with specific investigation methods and specific success criteria. Because a vague unknown is useless. You need to know exactly what you're testing.

---

**SECTION 3: The Economic Unknowns**

Let's start with economics. There are four.

First: what's the actual overcollateralization rate? Here's why this matters. When a seller creates a covenant, they provide a volatility buffer from their own funds. We think it should be seven percent. But maybe merchants and sellers will push back based on actual volatility patterns. Maybe three percent is enough. Maybe fifteen is needed. We don't know.

This is critical because it affects the entire capital economics. Too little buffer and covenants fail when Bitcoin Cash volatility spikes. Too much and sellers have too much capital locked per covenant. We'll test this by running live transactions with real senders, sellers, and merchants and measuring how often covenants actually hit their buffer limits.

Second: how should we split fees between merchants and sellers? Right now we think fifty-fifty. But what if merchants need more? What if sellers can't survive on that? This isn't theoretical. If we get the split wrong, one side of the marketplace doesn't participate. We'll figure this out by working with early merchants and sellers and seeing what actually incentivizes sustainable behavior.

Third: would time-based bonuses drive faster coordination? This is a Phase One idea, not a Phase Zero feature. We have a theory that if we offer bonus rewards—maybe a one percent bonus for claiming in two hours instead of twenty-four—it could incentivize faster coordination. But do people actually care about bonuses? Or do they just want free money with no timing pressure? Phase Zero tests claim timing without bonuses. If coordination is slow, we'll test bonuses in Phase One and measure whether they actually move behavior.

Fourth: what's the actual merchant margin from Asgaya transactions? Merchants earn the half-percent fee on every covenant they validate, plus product margins when recipients buy groceries in-store. But we don't yet know what percentage of recipients spend in-store, or how much they spend. The merchant fee is fixed and predictable. The product margin depends on recipient behavior we haven't measured yet. We'll track in-store spending patterns to understand the full merchant economics.

Here's a prediction we're making: triple-dipping merchants—grocery stores with family in Spain who also act as BCH sellers—will subsidize remittances to capture the product margin business. Why? Because product margins on groceries are huge. Fifteen to thirty percent on every item sold. If a recipient spends fifty euros on groceries after claiming a remittance, the merchant makes seven to fifteen euros in product margin alone. That dwarfs the half-percent transaction fee.

This means triple-dippers will compete aggressively. They'll undercut hardware stores and cafés for remittance traffic because the real money isn't in the fee—it's in selling groceries. They might even offer better exchange rates or lower fees than double-dip merchants just to get customers through the door.

If this prediction holds, it changes everything about merchant competition. The grocery stores win. The hardware stores and cafés either match the subsidies or lose the business. And recipients get better economics because merchants are competing on value, not just location.

Phase Zero will tell us if this actually happens or if we're wrong about merchant behavior.

---

**SECTION 4: The Behavioral Unknowns**

Now let's talk about people. Three unknowns here.

First: what are the actual claim timing patterns? We expect most recipients to claim within two to six hours. But maybe most people are actually planning to wait until the next day. Maybe they're more risk-averse than we predicted. Or maybe they're just not paying attention to timing at all. This matters because it affects how much volatility risk we need to buffer. We'll test it by tracking when people actually claim and building a distribution map. Early Phase Zero data will tell us.

Second: what percentage of received cash do people actually spend in-store? Here's the scenario. A recipient in Venezuela gets fifty euros worth of Bitcoin Cash from Asgaya. They go to the merchant, claim it, get Venezuelan bolívares. Do they spend it immediately? Do they save it? Do they split it? The answer changes everything about how merchants position themselves. If recipients immediately spend, the merchant becomes a gateway hub. If recipients save, the merchant is just a conversion point. We'll track spending patterns in early test markets.

Third: how much coordination actually happens between senders and recipients before the claim? We think most senders will message their recipients saying "I'm sending now, can you be at the merchant in two hours?" But maybe most pairs won't coordinate. Maybe they'll just rely on the default twenty-four-hour window. If coordination is low, we need bigger buffers. If it's high, we can be more aggressive. We'll measure this by surveying early users about their coordination patterns.

---

**SECTION 5: The Technical Unknowns**

Four technical questions. These are make-or-break questions. If the technology doesn't work reliably, nothing else matters.

First: what's the actual covenant validation success rate? We think it should be over ninety-nine percent. But that's theory. In practice, there might be edge cases. Network delays. Bitcoin Cash reorganizations. Bugs we haven't found. We'll measure this by running thousands of test covenants and tracking how many validate successfully. If success rate drops below ninety-five percent, we have a major engineering problem to solve before scaling.

Second: does the notification system actually reach recipients reliably? A recipient needs to know when a covenant is waiting for them. We're using a notification service, but does it work consistently across networks? Do notifications reach people quickly enough? Do people respond? We'll test notification delivery rates and response times in Phase Zero.

Third: are our rate oracles accurate? We need to know the Bitcoin Cash price at the exact moment a covenant is created so we can calculate the buffer. If the oracle is wrong, the whole economics break. We'll measure oracle accuracy by comparing our prices to multiple external sources and measuring latency and deviation.

Fourth: what's the actual performance of covenant lookup on the blockchain? Merchants query the Bitcoin Cash blockchain via Electrum to find active covenants. In Phase Zero with low volume, this is fast. But as transaction volume scales, we need to measure query response times, Electrum server reliability under load, and whether caching layers are needed. If merchants can't look up covenants quickly, the in-store experience breaks.

---

**SECTION 6: The Market Unknowns**

Three questions about the market.

First: what channels can actually get us senders at scale? Social media recruiting works for a hundred people. But how do you get five thousand senders? Direct outreach to remittance communities? Partnerships with existing money-transfer services? We won't know until we try. We'll test different channels in Phase Zero and measure cost per acquisition and sender quality.

Second: what's the actual merchant density required? We think a neighborhood needs at least three merchants for Asgaya to work well. Three gives recipients choice. Prevents any one merchant from extracting too much. But maybe it's five. Maybe it's one. We'll map merchant density against successful transaction patterns in early test markets.

Third: is there actually enough Bitcoin Cash buyer liquidity at the prices and speeds we need? When a sender wants to buy one hundred and seven euros worth of Bitcoin Cash, can they get it instantly at the price they expect? Or do they wait? Or do they get a worse price? If liquidity dries up, the whole system stalls. We'll track buyer availability and price slippage in each test market.

---

**SECTION 7: The Top Five Critical Unknowns**

Of the fourteen, we've identified five that are absolutely critical for Phase Zero validation.

These are our make-or-break questions.

First: overcollateralization rate. If seven percent doesn't work, the whole capital model breaks.

Second: claim timing patterns. If people don't coordinate, we need way bigger buffers.

Third: merchant spending patterns. This determines whether merchants become hubs or conversion points.

Fourth: fee split optimization. If we get the economics wrong between merchants and sellers, one side disappears.

Fifth: covenant reliability. If covenant validation doesn't work reliably, nothing else matters.

We're running Phase Zero specifically to get data on these five unknowns. The moment we have confidence in the answers, we'll know whether to scale, pivot, or iterate on the model.

---

**SECTION 8: How to Contribute**

Here's the powerful part. These investigations aren't gatekept. Anyone can pick an unknown. Anyone can design an investigation. Anyone can gather data and submit findings.

Each unknown has an investigation brief. It says what the question is. Why it matters. What method we'd use to answer it. What success looks like. And what we'd do if the answer proved us wrong.

You don't need permission. You don't need to ask. If you see an unknown that interests you, grab it. Design your investigation. Run it. Document your results.

This is how communities scale. Not by asking permission. But by inviting contribution and making it easy to contribute.

---

**CLOSING**

The core insight here is simple. Documenting what you don't know is how you invite other people to help you learn.

Most businesses hide their unknowns because they think it shows weakness. But that's backwards. A business that names its unknowns clearly is a business that's confident enough to be honest. It's a business that wants help. It's a business that will probably succeed because it's not pretending to know things it doesn't.

We have fourteen open questions. We're sharing them publicly. We're explaining what we'd do to answer them. And we're inviting anyone in this community to pick one, investigate it, and help us build something real.

That's Radio Asgaya. Thanks for listening.

---

[END]
