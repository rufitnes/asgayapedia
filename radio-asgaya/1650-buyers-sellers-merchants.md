# Radio Script: BCH Buyers, Sellers, and Merchants — The Circular Flow

**Episode 17:** How capital recycles and why merchants are just BCH buyers who accept cash  
**Duration:** ~14 minutes  
**Tone:** Conversational, systems-thinking focus  
**Target Audience:** Protocol designers, BCH community, potential participants

---

## INTRO

Welcome to Radio Asgaya. Follow the money in a circle.

I'm your host, Claudia Sonnet 4.5.

In the last episode, we talked about the bulletin board—the marketplace infrastructure where everyone finds each other. Today we're going one layer deeper: Who are the participants? What roles do they play? And how do they interact with covenants, notification bots, and each other?

By the end of this episode, you'll understand why a grocery store merchant in Caracas and a BCH buyer in Madrid are fundamentally the same thing—just with different payment methods. And you'll see how capital flows in a complete circle, from sender to seller to merchant to buyer, and back to seller again.

Let's trace the flow.

---

## SECTION 1 - The Three Roles

In Asgaya, there are three core economic roles:

**1. BCH Sellers** — People who have Bitcoin Cash and want fiat (EUR, VES)  
**2. BCH Buyers** — People who have fiat and want Bitcoin Cash  
**3. Merchants** — People who accept cash at physical locations to buy Bitcoin Cash

At first glance, these seem like three separate categories. But here's the key insight: **merchants are just a special type of BCH buyer.** They want BCH. They have fiat. The only difference is their payment method—they accept cash at a physical store instead of SEPA transfers online.

Once you see this, the entire system clicks into place. There are really only two sides to the market:

- **Supply side:** BCH sellers (people with BCH)
- **Demand side:** BCH buyers (including merchants)

And the magic happens when the same person plays both roles—when a buyer becomes a seller, when a seller becomes a buyer. That's when capital recycles. That's when the system becomes self-sustaining.

---

## SECTION 2 - BCH Sellers: The Liquidity Providers

Let's start with BCH sellers. Who are they?

**BCH sellers are Bitcoin Cash holders willing to provide liquidity to senders.**

You have 5 BCH sitting in your wallet. You believe in Bitcoin Cash long-term. But you also want to earn yield on it. You could stake it, lend it, hold it—or you could sell it temporarily to remittance senders and earn fees.

So you post a listing on the bulletin board:

"I have 5 BCH. I accept EUR via Bizum. Fee: 0.5%. Response time: < 2 minutes. Reputation: 100 successful transactions."

Now you wait. Your notification listener bot monitors your bank account (via SMS parsing or API). When a Bizum payment arrives with the reference code for an Asgaya covenant, your bot automatically:

1. Verifies the payment is real and matches the expected amount
2. Checks that the sender created a valid covenant on-chain
3. Locks 107% BCH into that covenant (100% face value + 7% volatility buffer)
4. Sends you a confirmation: "Covenant funded. You received €100. You locked 0.032 BCH."

All automated. All instant. You never manually click anything. Your bot does it.

Now here's the clever part: **You can recycle that €100 immediately.** A new sender comes along five minutes later. They want to send €100 to Venezuela. They pay you via Bizum. Your bot funds their covenant using BCH you still have in reserve. The €100 you just received? You can use it to buy back BCH from a merchant or buyer who wants to sell. The cycle continues.

This is capital efficiency. You're not locking up €100 waiting for the covenant to settle. You're recycling fiat in near real-time, using your BCH inventory to provide continuous liquidity.

---

## SECTION 3 - BCH Buyers: The Exit Liquidity

Now flip to the other side. Who are BCH buyers?

**BCH buyers are people with fiat who want to accumulate Bitcoin Cash.**

Why would you want BCH? Maybe you're bullish on Bitcoin Cash long-term. Maybe you need to send remittances yourself and want to hold BCH as inventory. Maybe you're a dual citizen moving money between countries and BCH is your bridge.

Whatever the reason, you have euros or bolivares, and you want BCH.

So you post a listing on the bulletin board:

"I want BCH. I have EUR via SEPA transfer. I'll pay market rate + 0.5% spread. Maximum purchase: 2 BCH."

Now you wait. When a merchant receives BCH from a covenant and wants to convert it back to fiat, they query the bulletin board for buyers. They see your listing. They contact you. You coordinate a peer-to-peer trade.

The merchant sends you 0.5 BCH. You send them €1,000 via SEPA. Done. The merchant has euros to restock inventory. You have BCH to hold or sell to the next sender.

**Here's the key: buyers provide exit liquidity for merchants.** Without buyers, merchants would receive BCH and have no one to sell it to. They'd be stuck holding crypto they don't want. The system would jam.

Buyers are the release valve. They absorb BCH from merchants and keep the loop flowing.

---

## SECTION 4 - Merchants: Cash-Accepting BCH Buyers

Now let's talk about merchants. What do they actually do?

A recipient in Caracas gets a notification: "€100 remittance ready to claim." They open the app, see a list of merchants nearby, and pick one—a grocery store on Avenida Libertador.

The recipient walks into the store. They show their CashAccount. The merchant's app queries the blockchain and validates:

- Does a covenant exist for this recipient? **Yes.**
- Is it properly funded with 107% collateralization? **Yes.**
- Is it still within the claim window? **Yes.**
- Is it signed by a known seller from the bulletin board? **Yes.**

All checks pass. The merchant hands the recipient cash—let's say 500,000 bolivares (equivalent to €100 at today's parallel rate). The recipient signs the claim transaction. The merchant signs. The covenant releases 0.032 BCH to the merchant's wallet. Done.

The recipient walks away with cash. The merchant now has Bitcoin Cash.

But here's the thing: **the merchant didn't want Bitcoin Cash.** They wanted bolivares to restock shelves, pay suppliers, keep the business running. So now they need to convert that BCH to fiat.

They have two options:

**Option 1: Find a BCH buyer online.** They query the bulletin board for buyers willing to pay VES for BCH. They coordinate a trade. The buyer sends VES via bank transfer. The merchant sends BCH. Capital recycled.

**Option 2: Trade with another merchant.** Another merchant in Caracas just cashed out five remittances today. They have surplus BCH and depleted VES float. They need VES for the next cash-out. Merchant A (with BCH surplus) and Merchant B (with VES surplus) trade directly. Peer-to-peer. No external buyer needed.

But notice what the merchant is doing: **They're selling BCH for fiat.** They're acting as a BCH seller in reverse. They received BCH, now they want fiat.

From the system's perspective, merchants are **BCH buyers who happen to accept cash.** When a recipient claims at their store, the merchant is buying BCH from the recipient (via the covenant) and paying in cash instead of SEPA.

Same economic function as an online BCH buyer. Different payment method.

---

## SECTION 5 - The Circular Flow: How It All Connects

Now let's trace the complete cycle. Follow the money:

**Step 1: Sender creates covenant**

A sender in Barcelona wants to send €100 to their cousin in Caracas. They open the Asgaya app, create a covenant on the blockchain specifying the recipient's CashAccount, the amount, and a 4-hour claim window. The covenant is created but unfunded—it's just a smart contract waiting for someone to lock BCH into it.

**Step 2: Sender selects BCH seller**

The sender queries the bulletin board for BCH sellers. They see a list: Alice (fee 0.5%, liquidity 3 BCH), Bob (fee 0.6%, liquidity 10 BCH), Carol (fee 0.4%, liquidity 1 BCH). The sender picks Alice.

**Step 3: Sender pays seller**

The sender requests payment info from Alice's bot. Alice's bot responds with encrypted bank details. The sender pays Alice €100 via Bizum, including a reference code tied to the covenant.

**Step 4: Seller's bot funds covenant**

Alice's notification listener bot sees the Bizum payment arrive. It verifies the payment matches the expected covenant. It locks 0.0321 BCH (107% of €100) into the sender's covenant. The covenant is now live.

**Step 5: Recipient claims at merchant**

The recipient in Caracas gets a notification. They walk into a nearby grocery store—Merchant Carlos. Carlos validates the covenant using his app. All checks pass. Carlos hands the recipient 500,000 bolivares in cash. The recipient signs. Carlos signs. The covenant releases 0.030 BCH to Carlos (the merchant fee of 0.5% stays in the covenant and returns to Alice, the seller, as surplus from the volatility buffer).

**Step 6: Merchant sells BCH to buyer**

Carlos now has 0.030 BCH but needs bolivares to restock his inventory. He queries the bulletin board for BCH buyers. He sees David, a dual citizen in Caracas with euros in Spain and a local VES bank account. David wants to accumulate BCH. Carlos and David trade: Carlos sends 0.030 BCH to David's wallet. David sends 500,000 VES to Carlos's bank account. Carlos has his fiat back. David has BCH.

**Step 7: Buyer becomes seller**

David now has 0.030 BCH. A new sender in Madrid needs BCH to send a remittance. David posts a listing as a seller: "I have BCH, I accept EUR via SEPA." The sender pays David. David funds the sender's covenant. The loop closes.

**Capital has flowed in a complete circle:**

Sender → Seller (Alice) → Covenant → Recipient → Merchant (Carlos) → Buyer (David) → Seller (David) → next Sender

Alice started with BCH and ended with EUR + fees. David started with VES and ended with BCH (which he'll sell for EUR + fees). Carlos started with VES, received BCH, and converted back to VES (earning product margin on the grocery sale). The recipient got their remittance in cash.

Everyone wins. And the system is self-sustaining—no external exchange needed, no central entity coordinating, just peers trading with peers.

---

## SECTION 6 - Notification Bots: The Automation Layer

None of this would work without notification listener bots. Let's break down what each role's bot does:

**Seller's bot:**
- Monitors bank account (via SMS parsing or API)
- Detects Bizum/SEPA payments with covenant reference codes
- Validates payment amount and sender
- Queries blockchain to verify sender's covenant exists
- Locks 107% BCH into the covenant automatically
- Logs transaction for accounting and reputation scoring

**Merchant's bot:**
- Monitors blockchain (via Electrum) for new covenants addressed to recipients
- Notifies merchant when a recipient walks in and shows their CashAccount
- Validates covenant: funded? collateralized? within claim window? signed by known seller?
- Facilitates signing and claim execution
- Logs BCH receipt for inventory management

**Buyer's bot:**
- Monitors bulletin board for merchants/sellers wanting to sell BCH
- Matches listings based on price, location, payment method
- Facilitates peer-to-peer coordination (encrypted messaging)
- Executes trades and logs for accounting

These bots are not Asgaya-hosted services. They're open-source software that participants run on their own devices—a phone, a Raspberry Pi, a VPS. You control your bot. You monitor your transactions. No one else sees your data unless you choose to publish it.

Automation without centralization. That's the goal.

---

## SECTION 7 - Double-Dip and Triple-Dip Patterns

Now here's where it gets interesting. The same person can play multiple roles simultaneously.

**Double-dip:** Merchant + Seller

A grocery store in Caracas has family in Spain. They cash out remittances (acting as a merchant), receive BCH, and instead of selling it to a buyer, they use it to fund covenants for senders in Spain who want to send money to Venezuela. They're both a merchant and a seller. They earn the 0.5% merchant fee when they cash out. They earn the 0.5% seller fee when they fund covenants. Double revenue from the same BCH inventory.

**Triple-dip:** Merchant + Seller + Buyer

Same grocery store also buys BCH from other merchants who have surplus. They're providing exit liquidity (acting as a buyer), earning the spread, accumulating BCH inventory, and using that inventory to fund covenants (acting as a seller).

One participant, three roles, three revenue streams. This is capital efficiency at its peak.

And here's the thing: **the more roles you play, the more you stabilize the network.** You're providing liquidity on both sides of the market. You're absorbing volatility. You're keeping capital flowing.

The protocol rewards this behavior economically. The more you participate, the more you earn.

---

## SECTION 8 - Why Merchants Are BCH Buyers (Conceptually)

Let's revisit the key insight: merchants are BCH buyers.

When you think of a "buyer," you imagine someone online transferring EUR via SEPA to purchase BCH. That's one type of buyer.

But what is a merchant doing when they cash out a remittance?

- They're giving fiat (VES cash) to the recipient
- They're receiving BCH (from the covenant)
- They're buying BCH using cash as the payment method

Same transaction structure. Same economic function. Different interface.

The bulletin board reflects this. Merchant listings look like buyer listings:

**Buyer listing:**
"I want BCH. I have EUR via SEPA. Market rate + 0.5%."

**Merchant listing:**
"I want BCH. I have VES via cash. Location: Caracas, Avenida Libertador. Market rate + 0.5%."

Same fields. Same protocol. Just a different payment method and a physical location instead of online-only.

This is why Episode 16 (The Bulletin Board) said there are three listing types but only two economic sides: BCH supply (sellers) and BCH demand (buyers + merchants).

Understanding this simplifies the mental model. You're not managing three separate markets. You're managing one two-sided market with different payment rails.

---

## SECTION 9 - Capital Recycling: The Efficiency Metric

The faster capital recycles, the more efficient the system.

Let's say Alice is a BCH seller with 2 BTC inventory and €10,000 fiat. She funds covenants for senders. She receives EUR via Bizum. She uses that EUR to buy BCH back from merchants or buyers who want to sell. Her capital recycles every few hours.

Over the course of a week, Alice might fund 50 covenants totaling €5,000 in transaction volume—even though she only started with €10,000 and 2 BCH. Why? Because she's recycling capital. She doesn't hold the EUR. She doesn't hold the BCH long-term. She's just providing liquidity, earning fees, and moving on.

Compare this to a static liquidity pool in DeFi. You lock $10,000 in a pool. It sits there. It earns fees on trades. But you can't reuse that $10,000 for other purposes while it's locked.

Asgaya's model is dynamic. Your capital is never locked. You're constantly recycling. That's why a small amount of capital can support much larger transaction volumes.

The faster buyers absorb BCH from merchants, the faster merchants can cash out the next remittance. The faster sellers receive EUR from senders, the faster they can fund the next covenant. Speed = efficiency.

And automation (via notification bots) is what makes speed possible.

---

## SECTION 10 - Phase 0 vs. Phase 1: Curated vs. Permissionless

In Phase 0, Asgaya vets the first sellers, buyers, and merchants. We verify bank accounts. We test bots. We onboard manually.

Why? Because reputation systems need time to mature. Scammers exist. We want Phase 0 to succeed so we can prove the model works.

But the goal is Phase 1: fully permissionless participation.

In Phase 1:
- Anyone can post a seller listing (if they have BCH)
- Anyone can post a buyer listing (if they have fiat)
- Anyone can register as a merchant (if they have a physical location)

Reputation becomes the filter. Your on-chain transaction history is public. How many covenants have you funded? How many claims have you settled? Have you ever failed to fund a covenant after receiving payment? The data is there. Apps can rank participants by reputation.

Bad actors get filtered out by the market, not by gatekeepers. Good actors earn reputation and rise to the top of listings. No permission needed. Just proof of past performance.

That's the vision. Start curated. Prove it works. Open it up. Let the market self-regulate.

---

## SECTION 11 - Why This Model Scales

Traditional remittance companies scale by opening more agent locations, hiring more compliance staff, integrating with more banks. Every new corridor requires infrastructure investment.

Asgaya scales by adding participants. Every new seller increases liquidity. Every new buyer increases exit options. Every new merchant increases claim locations.

And here's the key: **participants are self-interested.** They're not doing this out of charity. They're earning fees. They're capturing spreads. They're recycling capital for profit.

The protocol doesn't need to pay for growth. Growth is profitable for participants, so participants drive growth.

This is the same dynamic that made Bitcoin successful. Miners aren't mining out of altruism—they're mining for profit. But their profit motive secures the network for everyone.

Asgaya applies the same principle to remittance infrastructure. Sellers, buyers, and merchants are profit-motivated. But their profit motive creates a resilient, decentralized network for everyone.

---

## CLOSING

So here's what to remember.

There are three roles in Asgaya: BCH sellers, BCH buyers, and merchants. But merchants are just BCH buyers who accept cash. Two economic sides: supply (sellers) and demand (buyers + merchants).

Capital flows in a circle: Sender → Seller → Covenant → Recipient → Merchant → Buyer → Seller (repeat). Each participant earns fees. Each participant recycles capital. The system is self-sustaining.

Notification bots automate everything. Sellers' bots fund covenants. Merchants' bots validate claims. Buyers' bots match trades. No manual intervention needed.

Double-dip and triple-dip participants play multiple roles, earning multiple revenue streams and stabilizing the network.

In Phase 0, we vet participants. In Phase 1, we go fully permissionless. Reputation becomes the filter.

And the model scales by adding participants, not by building infrastructure. Every new participant increases liquidity, increases coverage, and makes the system better for everyone.

That's the circular flow. That's how buyers, sellers, and merchants create a decentralized remittance network that works without a company in the middle.

Thanks for listening. This is Radio Asgaya.

---

**Episode 17 Complete**
