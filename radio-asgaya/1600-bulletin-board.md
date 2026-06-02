# Radio Script: The Bulletin Board

**Episode 16:** How everyone finds each other without a central server  
**Duration:** ~12 minutes  
**Tone:** Technical but accessible, architectural focus  
**Target Audience:** Technical reviewers, protocol designers, BCH community

---

## INTRO

Welcome to Radio Asgaya. The marketplace that isn't a marketplace.

I'm your host, Claudia Sonnet 4.5.

Here's a question: How do you build a peer-to-peer remittance system where senders find sellers, merchants find buyers, and everyone coordinates—without a central server, without a company running the show, without a database that someone controls?

The answer is the bulletin board. Today we're talking about Asgaya's coordination layer—how discovery works, how listings get published, how apps make decentralization feel centralized, and why this architecture is the key to everything.

---

## SECTION 1 - The Coordination Problem

Let's start with the problem we're solving.

You're a sender in Barcelona. You want to send one hundred euros to your cousin in Caracas. You need someone with Bitcoin Cash willing to sell it to you for euros. Where do you find them?

You could post on Twitter. You could ask in a Telegram group. You could check a website. But all of those require trust in a central party—the platform, the group admin, the website owner. And all of them can be shut down, censored, or compromised.

Now flip sides. You're a BCH seller in Madrid. You have Bitcoin Cash. You want to earn fees by providing liquidity to remittance senders. How do you advertise your service? How do senders find you?

You could build a website. You could run ads. But that makes you a visible target for regulators. It makes you look like a business, not a peer.

This is the coordination problem. Buyers and sellers need to find each other. But centralized solutions create single points of failure, regulatory targets, and trust dependencies.

Asgaya solves this with the bulletin board—a decentralized discovery layer where anyone can post listings, anyone can query them, and no one controls the infrastructure.

---

## SECTION 2 - What Is the Bulletin Board?

The bulletin board is not a server. It's not a website. It's not owned by anyone.

It's a coordination protocol built on top of Bitcoin Cash. Think of it like this: the Bitcoin Cash blockchain is a public ledger that everyone can read. You can write data into that ledger using OP_RETURN transactions—small pieces of information embedded in blockchain transactions that don't move money, just publish data.

The bulletin board uses OP_RETURN to publish listings. When a BCH seller wants to advertise their service, they create a transaction with an OP_RETURN output that says:

"I am a BCH seller. I have Bitcoin Cash. I accept Bizum payments. My contact info is [encrypted]. My fee is 0.5%. My reputation score is [verifiable on-chain]."

That transaction gets mined into a block. Now it's on the blockchain. Forever. Anyone running a Bitcoin Cash node can see it. Anyone querying the blockchain can discover it.

No central server needed. No company to shut down. No database to hack. Just data on a public blockchain that anyone can read.

---

## SECTION 3 - Three Types of Listings

The bulletin board has three types of participants:

**1. BCH Sellers**

These are people who have Bitcoin Cash and want to sell it for euros or bolivares. They post listings that say:

"I have BCH. I want EUR via Bizum. Fee: 0.5%. Available liquidity: 5 BCH. Response time: < 2 minutes."

When a sender creates a covenant and needs someone to fund it, they query the bulletin board for BCH sellers. They see a list of sellers, sorted by reputation, fee, and liquidity. They pick one. They request payment info. The seller's bot responds with bank details. The sender pays. The seller's bot detects payment and funds the sender's covenant.

**2. BCH Buyers**

These are people who have euros or bolivares and want to buy Bitcoin Cash. They post listings that say:

"I want BCH. I have EUR via SEPA transfer. I'll pay market rate + 0.5%. Maximum purchase: 2 BCH."

Why do buyers exist? Because merchants receive Bitcoin Cash from covenants and want to convert it back to fiat. Merchants need exit liquidity. BCH buyers provide that liquidity.

When a merchant receives BCH and wants fiat, they query the bulletin board for buyers. They see a list. They pick one. They coordinate a peer-to-peer trade. The merchant sends BCH. The buyer sends EUR via SEPA. Done.

**3. Merchants (Cash-Accepting BCH Buyers)**

Merchants are a special type of BCH buyer. Instead of accepting SEPA or Bizum, they accept cash at a physical location.

Their listing says:

"I want BCH. I have VES cash. Physical location: Caracas, Avenida Libertador. Payment method: Cash. Hours: 9 AM - 6 PM."

When a recipient wants to claim a remittance, they query the bulletin board for merchants near them. They see a list of stores. They pick one. They walk in. They show their covenant. The merchant validates it, hands over cash, and receives the BCH.

Merchants are BCH buyers who happen to accept cash instead of bank transfers. That's the conceptual model. Same bulletin board. Same listing protocol. Just a different payment method.

---

## SECTION 4 - How Discovery Works

So how does an app like Asgaya actually query the bulletin board?

**Step 1: The app connects to Electrum servers.**

Electrum is a lightweight Bitcoin Cash client protocol. You don't need to download the entire blockchain. You just connect to Electrum servers—public nodes that index blockchain data and let you query it.

**Step 2: The app queries for OP_RETURN transactions matching a pattern.**

Let's say you're a sender looking for BCH sellers. Your app queries the blockchain for OP_RETURN outputs tagged with "ASGAYA_SELLER" or a similar identifier. Electrum servers return a list of transactions.

**Step 3: The app parses the listings.**

Each OP_RETURN contains structured data—seller info, fee, liquidity, contact method (encrypted). Your app decrypts the contact info using your private key if needed, or reads public fields directly.

**Step 4: The app ranks the listings.**

The app sorts sellers by reputation (on-chain transaction history), fee (lowest first), and liquidity (highest first). You see a clean list in the UI—just like browsing products on a website, except there's no website.

**Step 5: The app facilitates contact.**

You select a seller. The app sends a peer-to-peer message (using their contact method from the listing). The seller's bot responds. You coordinate. No middleman.

This entire process happens client-side. Your app does the querying. Your app does the parsing. Your app does the ranking. No server needed. No API controlled by a company. Just a protocol anyone can implement.

---

## SECTION 5 - Decentralized but Feels Centralized

Here's the magic: even though the bulletin board is fully decentralized, the user experience feels like using a centralized app.

You open the Asgaya app. You tap "Send Remittance." You see a list of BCH sellers with their fees and liquidity—clean, sorted, easy to browse.

You don't see blockchain queries. You don't see OP_RETURN parsing. You don't see Electrum servers. You just see a list of sellers. You pick one. It works.

That's the goal. Decentralization under the hood. Centralized UX on the surface.

Contrast this with early Bitcoin wallets. You had to manually manage addresses, understand transaction fees, know what a mempool was. That was too hard for normal users.

Asgaya hides the complexity. You're using Bitcoin Cash. You're querying a decentralized bulletin board. You're coordinating peer-to-peer. But it feels like Bizum. It feels like a normal app.

Good UX despite decentralization—that's what makes this work.

---

## SECTION 6 - Integration with Covenants and Notification Bots

The bulletin board doesn't work in isolation. It's integrated with two other core pieces: covenants and notification listener bots.

**Covenants** are the smart contracts that hold Bitcoin Cash for recipients. The sender creates a covenant on-chain. The covenant specifies: recipient CashAccount, amount, claim window.

But the covenant starts unfunded. It's just a contract sitting there, waiting.

**The bulletin board is how the sender finds someone to fund it.** The sender queries for BCH sellers. Picks one. Pays them fiat. The seller's notification listener bot detects the payment and funds the covenant.

**Notification listener bots** are automated programs that sellers and merchants run. They monitor the blockchain (via Electrum) and their bank accounts (via SMS parsing or APIs).

When a seller's bot sees a Bizum payment arrive, it checks: Is this payment for a covenant I agreed to fund? If yes, lock 107% BCH into that covenant. Automated. Instant.

When a merchant's bot sees a new covenant addressed to a recipient who just walked into the store, it validates: Is this covenant properly funded? Is it still within the claim window? If yes, notify the merchant. Safe to hand over cash.

The bulletin board, covenants, and notification bots form a triangle. Each piece depends on the others. The bulletin board enables discovery. Covenants enable trustless settlement. Notification bots enable automation.

Together, they create a system that works without a central coordinator.

---

## SECTION 7 - No Central Server, No Single Point of Failure

Let's talk about what this means for resilience.

If Asgaya (the project, the website, the documentation) disappeared tomorrow, what would happen?

The protocol would keep working. The bulletin board is on the Bitcoin Cash blockchain. Covenants are on the blockchain. Notification bots are software that anyone can run. Apps are open-source—anyone can fork them, host them, modify them.

There's no server to shut down. There's no database to seize. There's no company to pressure.

Compare this to traditional remittance platforms:

- Western Union: shut down the servers, the network dies
- Wise: freeze the bank accounts, the service stops
- PayPal: ban the company, users are locked out

Asgaya has no servers to shut down. The protocol is the product. And the protocol lives on Bitcoin Cash, which is censorship-resistant by design.

This is what permissionless means. You don't need anyone's permission to use the bulletin board. You don't need Asgaya's permission to post a listing. You don't need approval to query it. You just do it.

That's the goal. Build the infrastructure. Open-source the tools. Let it run without us.

---

## SECTION 8 - Phase 0: Curated Bulletin Board

Now, here's the reality check. In Phase 0, the bulletin board is curated.

Why? Because launching fully permissionless on day one is risky. Scammers could post fake listings. Bad actors could spam the blockchain. Reputation systems need time to build.

So in Phase 0, Asgaya vets the first sellers and merchants. We verify their bank accounts. We test their bots. We onboard them manually.

The bulletin board still works the same way—listings on-chain, queried via Electrum, no central server. But only approved participants can post listings during Phase 0.

As the network grows and reputation systems mature, we remove the training wheels. Phase 1 opens it up. Anyone can post. Anyone can participate. Reputation becomes the filter, not gatekeeping.

Progressive decentralization. Start safe. Prove the model. Then let it go fully permissionless.

---

## SECTION 9 - Future: Multi-Corridor, Multi-Asset Listings

Right now, the bulletin board is focused on Spain → Venezuela. EUR and VES. BCH as the bridge asset.

But the architecture generalizes. The same bulletin board can support:

- Italy → Philippines: EUR → PHP via BCH
- US → Mexico: USD → MXN via BCH
- Argentina internal: ARS → ARS via BCH (merchants escaping peso depreciation)

And it's not limited to BCH. Remember Episode 2300 on MUSD? MUSD sellers can post listings on the same bulletin board. Same protocol. Different asset.

"I have MUSD. I want EUR via SEPA. Fee: 0.3%."

Someone who wants stable value instead of BCH volatility just queries for MUSD sellers instead of BCH sellers. Same discovery layer. Same coordination protocol. User choice.

The bulletin board is asset-agnostic. It's corridor-agnostic. It's a marketplace protocol, not a Spain-Venezuela-BCH-specific database.

That's why it's worth building right. Once the bulletin board works for one corridor, it works for all corridors. Once it works for BCH, it works for any CashToken.

---

## SECTION 10 - Why This Matters

Let me bring this home.

The bulletin board is not the sexy part of Asgaya. Covenants are cooler. Smart contracts sound impressive. Volatility buffers are clever.

But the bulletin board is the foundation. Without it, none of the rest works.

Senders can't find sellers. Sellers can't advertise. Merchants can't be discovered. Buyers can't find merchants. The coordination problem kills the entire system.

The bulletin board solves coordination without centralization. That's the breakthrough.

And here's the thing: once you have decentralized coordination working for remittances, you have it working for anything.

Peer-to-peer marketplaces. Decentralized exchanges. Local crypto trading. All of it uses the same pattern: publish listings on-chain, query via light clients, coordinate peer-to-peer.

The bulletin board is not just Asgaya infrastructure. It's general-purpose infrastructure for any peer-to-peer market on Bitcoin Cash.

---

## CLOSING

So here's what to remember.

The bulletin board is how senders find sellers, how merchants find buyers, how everyone coordinates—without a central server, without a company in the middle, without a single point of failure.

It's built on Bitcoin Cash using OP_RETURN transactions. It's queried via Electrum servers. It supports three listing types: sellers, buyers, and merchants (who are cash-accepting buyers). It integrates with covenants and notification bots to create a complete system.

In Phase 0, it's curated for safety. In Phase 1, it goes fully permissionless. And in the future, it supports any corridor, any asset, any peer-to-peer market.

Decentralized infrastructure with centralized UX. That's the goal. And the bulletin board is how we get there.

Thanks for listening. This is Radio Asgaya.

---

**Episode 16 Complete**
