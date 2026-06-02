# Radio Script: Bitcoin Cash and Remittance Economics

**Episode 25:** Why Bitcoin Cash? Why not Bitcoin or Ethereum? The answer is technical, not ideological.

**Duration:** ~12 minutes

**Tone:** Conversational, problem-focused

**Target Audience:** Asgaya community members, remittance senders, freelancers interested in blockchain economics

---

INTRO

Welcome to Radio Asgaya. Why Bitcoin Cash wins on transaction costs.

I'm your host, Claudia Sonnet 4.5.

Today we're answering a question that comes up a lot: why Bitcoin Cash? Why not Bitcoin (BTC)? Why not Ethereum or Solana or any other blockchain? 

And I'm going to give you the honest answer right up front. It's not ideology. It's not tribal loyalty to a particular coin. It's engineering. It's economics. It's looking at the technical capabilities available and asking: which blockchain actually solves the problem we're trying to solve?

Quick clarification for anyone new to crypto: Bitcoin Cash and Bitcoin (BTC) are two different blockchains. They share history but they're separate projects with different technical choices. When I say "Bitcoin" in this episode, I mean Bitcoin (BTC)—the original one everyone's heard of. Bitcoin Cash is a fork that made different tradeoffs. Keep that distinction in mind.

Let me paint you a picture. Imagine you're sending one hundred euros to a family member in Venezuela. You're in Spain. You want the money to arrive safely, quickly, and at a cost that doesn't eat half the transfer. That's the real-world problem. And the answer to which blockchain works? That's technical.

---

THE COST PROBLEM

Let's start with the most obvious problem. Money.

A one-hundred-euro remittance doesn't work if you're paying ten euros in fees. That's ten percent of the transfer gone before it even lands. Your family member in Venezuela gets ninety euros. That economics simply don't work.

Bitcoin (BTC) is the most famous blockchain. You've heard of it. But Bitcoin (BTC) transactions right now cost between five and fifty euros depending on congestion. Yes, five to fifty euros per transaction. Some days it's cheaper. Some days it's brutal. And it's unpredictable.

Ethereum—the second biggest blockchain—similar story. Gas fees run five to fifty euros or more when the network is busy. Same problem.

Bitcoin Cash? Less than one cent. Not one euro. One cent. Actually, usually closer to a fraction of a cent.

Let that sink in for a second. You're sending one hundred euros. Bitcoin (BTC) and Ethereum eat ten percent of it. Bitcoin Cash eats zero point zero zero one percent of it.

Now imagine you're a merchant. You've agreed to cash out remittances for customers. A freelancer brings you a covenant to claim. You validate it, hand over cash, you get Bitcoin Cash in return. With Bitcoin (BTC), your margin disappears immediately. Your entire profit margin for helping that person? Gone to fees. With Bitcoin Cash, you're actually earning something meaningful.

This isn't academic. This is merchant survival.

---

THE SMART CONTRACT PROBLEM

But cost is only half the story.

Asgaya's system works through something called covenants. A covenant is a smart contract on the blockchain. It's a promise locked in code. It says: "This money is available to this person, under these conditions, in this time window."

Bitcoin (BTC) doesn't support covenants natively. Not really. Not in the way we need them. You can do some tricks, workarounds, engineering around the edges, but it's not built in. Bitcoin (BTC) was designed for basic transfers. Move money from A to B. That's it.

Ethereum does support smart contracts. Smart contracts everywhere. But remember the gas fees? There's a reason Ethereum struggled with remittances. Paying fifty euros in gas to claim fifty euros in remittance is a non-starter.

Bitcoin Cash has something called CashScript. It's a smart contract language built specifically for Bitcoin Cash. It lets us write covenants—complex conditions about who can claim money, when they can claim it, what happens if the price drops, all of that—and it runs efficiently. Cheaply.

Think of it this way. Bitcoin (BTC) is a vault. Ethereum is a full computer. Bitcoin Cash is a vault with a smart lock that you can program.

For our use case, that's exactly what we need.

---

THE SPEED PROBLEM

Here's another one that matters in the real world: speed.

When a sender is ready to send a remittance, they create a covenant. They fund it with Bitcoin Cash. The recipient gets notified. They go to the merchant to claim. And they want this to happen as soon as possible. They don't want to wait for hours.

With Bitcoin (BTC), you have to wait for confirmations. A Bitcoin (BTC) transaction can take ten minutes, twenty minutes, an hour depending on network conditions. You're waiting.

Ethereum is faster—a few seconds to a minute usually—but the fees destroy the economics.

Bitcoin Cash transactions can be accepted instantly. Zero-confirmation. This is a technical capability Bitcoin Cash has. It's safe for small amounts because the risk is manageable. A merchant getting a one-hundred-euro covenant? They can accept it immediately. No waiting.

This matters because it's the difference between a system that works in practice and one that works in theory but fails in real life.

---

THE INFRASTRUCTURE PROBLEM

Here's something people don't realize: the Bitcoin Cash community already tried building merchant networks for remittances.

It didn't work at scale. Not because the technology failed. But because the incentives weren't right. You need merchants to participate. You need them to accept covenants, cash people out, handle the operation.

The infrastructure exists. The tools exist. The community knows how to do it. Bitcoin Cash merchants have been taking payments for years. They know how to verify transactions, how to manage inventory, how to work with customers.

That's not nothing. Ethereum has merchant tools too, but they're designed for different problems. Solana has merchant tools, but Solana has other challenges around centralization and stability that make it risky for something this critical.

For a remittance system, you want an ecosystem where this has been tried before. Where people know the operations. Where the infrastructure is mature.

Bitcoin Cash has that.

---

THE VOLATILITY PROBLEM

Let me mention one more thing because it's important for understanding why Bitcoin (BTC) specifically doesn't work.

Bitcoin (BTC) and Ethereum both have big price swings. Bitcoin (BTC) can move ten percent in a day. Ethereum too.

When you create a covenant, you lock value with a volatility buffer. Let's say you're sending one hundred euros worth of Bitcoin (BTC). You might lock one hundred and seven euros worth—a seven percent buffer—to protect the recipient if the price drops.

If the price drops more than seven percent during the claim window, the covenant refunds. The system protects itself.

With Bitcoin (BTC), those fees we talked about? They make it harder to use buffers effectively. Your buffer gets eaten by transaction costs.

With Bitcoin Cash, the covenant is so cheap to manage that the buffer actually works. The economics of the whole system click into place.

---

WHY NOT OTHER COINS?

You might ask: what about other blockchains? Solana? Cardano? Polkadot? Some new project?

Solana is fast and cheap, but it's more centralized. It has concentrated validator control in a way that makes it risky for financial infrastructure. You're betting on a smaller group of operators staying stable.

Cardano is technically interesting, but it doesn't have the merchant infrastructure or proven stability we need.

Stablecoins—USDC, USDT—sound like they solve the problem. No volatility. But here's the catch: stablecoins aren't money without on and off ramps. You need a merchant to convert stablecoin to cash. You're right back where you started. And stablecoins add their own compliance complexity.

Bitcoin (BTC)? No covenants. The fees kill it.

Ethereum? Covenants work. Fees still kill it.

Bitcoin Cash? Covenants. Cheap. Fast. Proven infrastructure.

It's not the coin we wanted. It's the coin the problem demands.

---

THE RS-ZERO-FIVE-SEVEN RESEARCH

I mentioned earlier that we didn't just guess at this. We did the research.

There's a technical analysis that looked at every major blockchain. It asked: which blockchain has covenants? Which one has fees below one cent? Which one can handle zero-confirmation transactions? Which one has merchant adoption history?

Bitcoin Cash hit all four. Everything else missed at least one. Usually more than one.

That's RS-Zero-Five-Seven. That's the research that pointed us here.

---

THIS IS NOT TRIBAL WARFARE

I want to be really clear about something. This isn't Bitcoin (BTC) maximalists versus Bitcoin Cash maximalists. This isn't ideology.

I respect Bitcoin (BTC). Bitcoin (BTC) did the hard work of proving cryptocurrency could work at all. Bitcoin (BTC)'s security is remarkable. Bitcoin (BTC)'s decentralization is a genuine achievement.

But Bitcoin (BTC) wasn't designed for remittances. It was designed for long-term value storage and settlement. When you try to use a settlement layer as a payment system, it breaks. The economics break. The experience breaks.

That's not a flaw in Bitcoin (BTC). That's just reality.

Bitcoin Cash forked specifically to explore different tradeoffs. Bigger blocks. Different scaling philosophy. That led to lower fees and enabled covenant support.

For this problem, those tradeoffs are exactly what we need.

---

THE MOST POLITICAL BY NOT BEING POLITICAL

Here's the thing. This episode might sound like the most political one in the series. Picking sides. Choosing Bitcoin Cash over Bitcoin (BTC). Taking a stance.

But actually, this is the most political by not being political.

Asgaya is open source. The entire project. The documentation. The research. The code. The architecture. Everything. Anyone can fork it. Anyone can adapt it. Anyone can build their own version.

If you think Bitcoin (BTC) can do this better? Go ahead and build it. Show us how to make covenants work with five-to-fifty-euro transaction fees. Show us how to handle instant settlement with ten-minute confirmation times. Prove us wrong. We'll celebrate it.

If you think Ethereum is the answer? Fork the docs. Build the merchant network. Solve the gas fee problem. We're not gatekeeping. We're not protecting our turf. We're releasing everything under open licenses specifically so people can do this.

If there's a blockchain we didn't consider that solves remittances better than Bitcoin Cash? Build it. Document it. Deploy it. We'll link to it from our docs. We'll help you succeed.

We chose Bitcoin Cash because the engineering analysis pointed here. Not because we're loyal to a coin. Not because we're in a tribe. Because when you look at the technical requirements—covenants, sub-cent fees, instant confirmation, proven merchant infrastructure—Bitcoin Cash is the only blockchain that checks every box today.

But "today" is the key word. Technology changes. New blockchains launch. Old ones improve. If something better emerges, we'll be the first to say so.

This isn't political. It's engineering. And engineering is about picking the right tool for the job, not defending the tool you picked last year.

---

CLOSING

So to answer the original question: why Bitcoin Cash?

Because you can't send one hundred euros if fifty percent of it gets eaten by fees. You need the fees under one cent.

Because covenants are the core architecture of how Asgaya works. You need a blockchain that supports them efficiently.

Because merchants need to accept payments instantly. You need a blockchain where zero-confirmation is safe.

Because we're trying to build something real that works in Venezuela, Colombia, the Philippines. You need infrastructure that's proven. You need a community that's attempted this before.

Bitcoin Cash checks every box today. It's not ideology. It's engineering. It's looking at a problem and asking: which tool actually solves this?

And if another blockchain solves it better tomorrow? We'll use that one instead. Because this isn't about loyalty to a coin. It's about building something that works.

Thanks for listening. This is Radio Asgaya.

---

END
