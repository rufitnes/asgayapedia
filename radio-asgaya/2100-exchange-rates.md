# Radio Script: Exchange Rates and Trust

**Episode 19:** One hundred euros in Spain. How many bolivares in Venezuela? That rate determines everything.

**Duration:** ~8 minutes

**Tone:** Conversational, story-driven

**Target Audience:** Asgaya senders, recipients, merchants, and people curious about how international money actually moves

---

## INTRO

Welcome to episode 19 of Radio Asgaya: One hundred euros in Spain. How many bolivares in Venezuela? That rate determines everything—I'm your host, Claudia Sonnet 4.5.

Today we're talking about something that seems simple but is absolutely critical to Asgaya: exchange rates. 

Imagine this. You're in Spain. You have one hundred euros. Your cousin is in Venezuela. You want to send them the value of those hundred euros, not as euros—they can't use euros in Venezuela—but as bolivares, the local currency. So you need to know: how many bolivares is a hundred euros worth right now? That single number determines whether your cousin gets what you intended to send or if something got lost in the conversion. That's what today is about.

---

## THE PROBLEM WE'RE SOLVING

Here's the thing about sending money across borders. It's not just about the euros and the bolivares. There are real people on both ends, and they both need to trust the number.

Let's say you want to send one hundred euros to Venezuela. You create a covenant on the blockchain specifying your cousin's CashAccount. Then you pay a BCH seller one hundred euros via Bizum. Here's where the automation happens: the seller's bank sends a notification—"Bizum received: €100." The seller's bot parses this notification automatically, verifies the payment is real, and immediately locks one hundred seven euros worth of Bitcoin Cash into your covenant—one hundred euros face value for your cousin, plus a seven percent volatility buffer from the seller's own BCH inventory. The seller can recycle that one hundred euros in fiat to the next sender. The seven euro BCH buffer stays locked until your cousin claims.

Now the covenant is live on the blockchain, addressed to your cousin's CashAccount. It holds one hundred euros worth of BCH for your cousin, protected by the seller's seven percent buffer. But here's the critical part: your cousin needs to know exactly how much Venezuelan bolívares they're getting when they claim that covenant.

They go to a merchant. The merchant validates the covenant. And the merchant needs to know: if I give this person one hundred euros worth of bolivares in Venezuelan currency, am I covered by the Bitcoin Cash in this covenant? That's where the exchange rate comes in.

---

## WHY THE PARALLEL RATE, NOT THE OFFICIAL RATE

Venezuela has two exchange rates. There's the official rate that the government publishes. And there's the parallel rate—what people actually use in real life.

The official rate? It's political fiction. Nobody actually trades at that rate. Banks don't use it. Merchants don't use it. It's a number the government maintains, but it has no relationship to what real Bitcoin, real euros, or real bolívares are actually worth on the street.

The parallel rate is what matters. It's the price the market sets. It's what merchants charge for euros. It's what merchants will accept in trade. It's real. It changes constantly—every thirty minutes, in fact—because people are actually buying and selling at that rate.

So when we need an exchange rate for Asgaya, we go to DolarAPI. That's a service that tracks the Venezuelan parallel market rate. Every thirty minutes, it updates. Every thirty minutes, we know what one US dollar is worth in bolivares according to people actually trading in Venezuela.

---

## HOW THE CONVERSION ACTUALLY WORKS

Now, DolarAPI gives us dollars to bolivares. But we're sending euros. So here's how the conversion chain works.

First, we take the euro amount. Let's say one hundred euros. We convert that to US dollars using the European Central Bank's exchange rate. The ECB publishes the rate every day for euro to dollar conversions.

Then we take those dollars and convert them to bolivares using the DolarAPI parallel rate.

So it's euros to dollars using the official European rate, then dollars to bolivares using the actual Venezuelan market rate. Two conversions, two different sources, both as accurate as possible.

---

## AUTOMATED RATE UPDATES

From day one, from Phase Zero, this is automated. An oracle fetches the rates from DolarAPI every thirty minutes. Every single time it updates, the system refreshes what exchange rate is active.

No human needed. No forgetting. No delay between the real market moving and the Asgaya system reflecting it. Just constant, automatic, mechanical accuracy.

The moment a sender creates a covenant after paying a seller, the rate used is the current market rate, within thirty minutes. The merchant validates it knowing the same thing. The recipient in Venezuela claims the covenant knowing that the rate they're getting is real, current, and based on actual market prices.

---

## WHY THIS MATTERS: TRUST AND ACCURACY

Let me be direct. Exchange rates are not negotiable. They are the connection between what you send and what the person receives. Get them wrong, and everything breaks.

If the rate is too old, too slow to update, or wrong in any way, what happens? Either the sender overpays—they send one hundred euros expecting to cover one hundred euros worth of bolivares, but the rate was stale and they actually covered ninety-five euros. Or the recipient gets less than they expected, because the rate shifted before the covenant was claimed.

Either way, someone loses trust. Either the sender feels cheated because they paid more than they should have. Or the recipient feels cheated because they got less than they should have. And once trust breaks, the system doesn't work.

That's why automation matters. That's why accurate, current, continuously updated rates are non-negotiable. It's not a feature. It's a foundation.

---

## CLOSING

So here's what to remember. When you're sending money across borders with Asgaya, that rate is not arbitrary. It's the link between your euros and your cousin's bolivares. It comes from real markets—the European Central Bank for euros to dollars, DolarAPI for the actual Venezuelan parallel market. Updated automatically every thirty minutes, from day one.

Because accurate rates are how you maintain trust with the people you're sending money to. And trust is everything.

Thanks for listening. This is Radio Asgaya.

[END]
