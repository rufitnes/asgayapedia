# Radio Script: Core Regulatory Constraints
**Episode 14:** MiCA and PSD2 almost killed Asgaya. Here's how we designed around them.
**Duration:** ~15 minutes
**Tone:** Strategic, clarifying, architectural
**Target Audience:** Protocol designers, policy-curious users, technical reviewers

---

[INTRO - Hook]

Welcome to Radio Asgaya. Designing around MiCA without asking permission.

I'm your host, Claudia Sonnet 4.5.

You've heard us talk about the architecture. The covenants. The pull system. The merchant validation. The way sellers recycle capital in minutes.

But you might not have heard about the regulations that almost killed all of it.

Here's the thing: Asgaya isn't designed around what's technically possible. It's designed around what's legal. And the path we took to stay legal? It fundamentally shaped every technical decision we made.

Today we're talking about regulatory architecture. How MiCA and PSD2—the EU's cryptocurrency and payments regulations—nearly forced us into a design that wouldn't work. And how we escaped.

---

[SECTION 1 - The regulatory landscape]

Let me set the stage.

The EU has two massive regulations that matter to anyone building money infrastructure. First, MiCA: the Markets in Crypto-Assets Regulation. It came into force in December twenty twenty-three. What it does is classify any company or entity that holds customer crypto as a Crypto Asset Service Provider. That's a CASP. And being a CASP means you need a license. You need capital reserves measured in millions of euros. You need regulatory oversight. You need compliance departments. Audits. Procedures. Governance committees.

The licensing process? Years. Not months. Years.

Second regulation: PSD2. Payment Services Directive Two. It's older—been around since twenty eighteen. What PSD2 does is regulate anyone who provides payment intermediation services. If you take someone's money, hold it, and release it conditionally based on a payment instruction? That's intermediation. That makes you a payment institution. That requires licensing. That requires capital. That requires compliance.

Between these two regulations, the EU has essentially said: "If you touch customer funds, if you move money between parties, if you hold anything conditionally—you must be licensed."

For a permissionless protocol built by a small team with no corporate structure? That's a death sentence.

---

[SECTION 2 - The May ninth problem]

Let me tell you what almost happened.

We built the original architecture around an escrow model. It was elegant. A sender in Spain wanted to send one hundred euros to Venezuela. They paid our escrow service one hundred euros in euros—not crypto. The escrow held the euros. No volatility. Safe.

The recipient in Venezuela got a notification: "You have one hundred euros waiting. Claim whenever you're ready." That recipient control was crucial for merchant adoption. Walk into a store when you need cash. Not when a payment arrives. Your timing. Your convenience.

When the recipient went to claim, they walked into a merchant. The merchant checked: yes, valid claim exists. At that exact moment—recipient standing there, cash in hand—the escrow bought Bitcoin Cash and released it to the merchant. Thirty seconds of volatility exposure. That's it.

Perfect architecture. Perfect economics. Perfect user experience.

But then we realized: That escrow is holding customer funds. That's custody. Under MiCA, that makes the escrow a Crypto Asset Service Provider. We'd need a license.

And it gets worse: The escrow is conditionally moving money between parties. That's payment intermediation. Under PSD2, that makes the escrow a payment institution. We'd need another license.

Except you can't get a CASP license or a payment institution license for a permissionless protocol. You need corporate structure. You need regulatory oversight. You need to be somebody's subsidiary.

Asgaya is not a company. Asgaya is a protocol. Protocols don't have licenses.

On May ninth, twenty twenty-six, we realized our entire architecture was illegal.

---

[SECTION 3 - The design crisis]

So what do you do when your core architecture is illegal?

We had limited options. We could abandon the pull system. Design for instant push settlement instead. No central escrow. No custody. Compliant.

But push settlement means covenants post, recipients claim to their personal wallets, merchants are just optional off-ramps. No merchant involvement in the settlement process. Which means merchants don't validate transactions. Which means no foot traffic. Which means no product sales. Which means no triple-dip. Which means the entire business model collapses.

We'd still have a working protocol. But we'd have sacrificed the feature that made adoption possible.

We looked at other options. Could we federate? Have multiple independent escrows instead of one central one? Technically possible. But each escrow is still a CASP. Each escrow still requires licensing. We'd just have ten problems instead of one.

Could we use a stablecoin? Hold USDC instead of euros? Still custody. Still a CASP. Still illegal.

Could we move to a different jurisdiction? Find a country with lighter regulation? Possible. But then we'd be building something permissionless in one jurisdiction and licensed in another. Permissionless means anyone can participate. If we required licensing anywhere, we're not permissionless.

We were backed into a corner. Fix the architecture or accept that Asgaya couldn't work.

---

[SECTION 4 - The May tenth breakthrough]

May tenth. Morning. Staring at the problem with fresh eyes.

Then the question: What if the custody isn't held by an entity?

What if it's held by code?

Here's the insight: Bitcoin Cash has native covenants—smart contracts that can validate conditions and release funds. What if senders pay BCH sellers in fiat, and those sellers lock their own Bitcoin Cash into covenants—smart contracts that validate conditions and release funds? The sellers already hold BCH. They post covenants directly on the blockchain, not through a company.

The covenant is controlled by code, not by a company. No entity holds the funds. The blockchain does. The blockchain isn't a service provider. The blockchain is a network. Networks aren't regulated as custodians.

And here's the critical part: BCH sellers own the covenant BCH outright. They bought it, they posted it, they control it. It's their property. The covenant is just a spending condition they've set on their own money. They're selling a crypto asset that they own to a sender who uses it to pay a recipient.

Under MiCA, who's the custodian? Nobody. The BCH is on the blockchain. The seller owns it. There's no entity holding client funds. No CASP licensing required.

Under PSD2, who's the payment institution? Nobody. The seller posted their own BCH as a conditional covenant. The recipient claims from a blockchain. The sender paid the seller for a crypto asset. There's no entity intermediating payment. No payment institution license required.

But here's the problem: Bitcoin Cash is volatile. If the seller posts one hundred euros worth of BCH and the recipient waits twenty-four hours to claim, that BCH might be worth ninety euros. Price moves.

So we overcollateralize. Not one hundred euros. One hundred seven euros. A seven percent buffer. The seller locks one hundred seven euros worth of their own Bitcoin Cash into the covenant—one hundred euros face value plus a seven percent volatility buffer. The sender pays the seller one hundred euros via Bizum. The fiat goes to the seller's bank account.

If BCH drops five percent while the recipient is claiming, no problem. The covenant still holds one hundred two euros worth. Still valid. Settlement proceeds.

If BCH drops more than seven percent? The covenant is now worth less than one hundred euros. At that point, the covenant automatically refunds to the sender's wallet. The recipient never gets notified. The merchant never participates. The sender paid one hundred euros, gets back BCH now worth maybe ninety-six euros. The sender bears the tail volatility loss. The seller keeps the one hundred euros in fiat—they effectively sold BCH above current market price.

But seven percent covers ninety-five percent of Bitcoin Cash twenty-four-hour volatility. It's rare for BCH to drop more than seven percent in a day. And with shorter claim windows—two hours instead of twenty-four—the volatility risk becomes negligible.

This works. This is legal. This is permissionless.

---

[SECTION 5 - What Asgaya is NOT]

Let me be very clear about what we're not.

We are not a crypto exchange. We don't run an order book. We don't match buyers and sellers. We don't take trading fees. We don't hold assets on behalf of traders. We're not regulated as an exchange.

We are not a payment institution. We don't provide payment intermediation services. We don't move money between parties on behalf of a service provider. We don't hold payment accounts. PSD2 doesn't apply.

We are not a money transmitter. That's a US term, but it applies here: We don't transmit money. When a sender creates a covenant, they're not transmitting their own money. They're posting their own BCH with spending conditions attached. When a recipient claims, they're claiming their own funds from the covenant they're authorized to spend. No third party is transmitting anything. The blockchain is a network. Networks don't transmit. They process transactions.

And we are not custodial. We never hold customer funds. The blockchain holds the covenants. The senders own the covenants. The merchants validate the covenants but don't hold them. No entity has custody of customer assets.

---

[SECTION 6 - What Asgaya IS]

So what are we?

Asgaya is a bulletin board. There's a place—a blockchain, specifically Bitcoin Cash—where senders can post information: "Here's a covenant. Here's a recipient's CashAccount. Here's the amount. It expires in twenty-four hours." Anyone can read the bulletin board. Recipients get notified. Merchants can check it. No gate-keeping. No permission required. It's information.

Asgaya is a protocol. It's open source software. It's a set of rules for how to structure covenants, how to validate them, how to settle them. Anyone can read the code. Anyone can implement it. Anyone can audit it. Nobody owns the protocol. It's not a company providing a service. It's code.

Asgaya is a network. It's the people using the protocol. Senders sending BCH. Recipients receiving notifications. Merchants validating claims. These are peers. They're not customers of a service provider. They're participants in a network. The network is permissionless. Anyone can join.

That distinction matters legally. A bulletin board is information. A protocol is software. A network is people. None of those trigger CASP or PSD2 licensing.

---

[SECTION 7 - The three design principles that make it work]

This regulatory architecture rests on three core principles. Get these right, everything works. Get them wrong, the whole thing collapses.

First principle: No custody. Never hold user funds. Ever. The moment you hold funds, you're a CASP under MiCA. The moment you hold funds conditionally, you're a payment institution under PSD2. The solution: Covenants hold BCH, not a company. BCH sellers post their own BCH assets to the blockchain as covenants. The blockchain is the custodian. Companies aren't. Senders pay sellers for crypto assets; sellers own those assets and create covenants with them.

Second principle: No intermediation. Never provide payment services. Never take money from A and release it to B based on a payment instruction. That's payment intermediation. The solution: Peer-to-peer. Sellers post covenants with their own BCH. Recipients claim from covenants. Merchants validate and exchange BCH for cash. No central coordinator. No intermediary. Each party acts independently. The blockchain coordinates everything. Blockchains don't require payment services licenses.

Third principle: No territory gatekeeping. Can't control who participates. The moment you require permission, you're responsible for who you permit. You're no longer permissionless. The solution: Bulletin board. Anyone posts covenants. Anyone reads them. Anyone participates as sender, recipient, or merchant. You can't prevent someone from joining. You can't verify their identity. You can't enforce KYC. Which means you have zero control over access. Which means you can't be held responsible for who participates. Permissionless by default.

These three principles sound simple. But they drive every technical decision. They're why covenants exist. They're why the pull system works. They're why merchants validate. They're why senders own the funds. They're why we use Bitcoin Cash instead of a custodial chain.

Everything else flows from these three.

---

[SECTION 8 - The May ninth versus May tenth difference]

Let me contrast the two approaches so you see what changed.

May ninth architecture: Central escrow holds euros. Company is the custodian. MiCA applies. PSD2 applies. Two licenses required. Years to obtain. Impossible for permissionless protocol.

May tenth architecture: BCH sellers post Bitcoin Cash covenants using their own BCH. No company custody. Blockchain is the custodian. No CASP license. No payment institution license. Permissionless by design. Legally viable.

May ninth: Merchants wait for settlement. Covenants auto-settle after claim window expires.

May tenth: Merchants validate and immediately co-sign. Settlement is instant, not passive.

May ninth: Volatility is the escrow's problem. They buy BCH at settlement time.

May tenth: Volatility is the seller's problem. They post BCH upfront with a buffer from their own capital.

May ninth: Sellers wait for settlement. Capital is locked in escrow until recipient claims.

May tenth: Sellers receive payment from sender immediately. Can recycle most capital in minutes. Only the volatility buffer portion stays locked per covenant.

May ninth: Sender bears no risk. Escrow bears volatility risk.

May tenth: Sender bears tail volatility risk beyond seven percent. Merchant bears zero risk. Seller bears opportunity cost only. Sender has simple UX—just pays seller one hundred euros.

These aren't minor tweaks. This is a fundamental redesign of how the system operates. But everything—compliance, merchant adoption, capital efficiency, risk allocation—everything improved.

---

[SECTION 9 - Why this matters long-term]

You might think: "Okay, we stay legal in the EU. What about other jurisdictions?"

Here's what's beautiful about the permissionless architecture: It works everywhere.

MiCA is the strictest regulation in the world for crypto right now. If you can satisfy MiCA, you can satisfy almost any other framework. Singapore is lighter. The US is fragmented but evolving. Latin America is more permissive. If Asgaya is legal under MiCA constraints, it's legal nearly everywhere.

More importantly: Permissionless means we don't have to get regulatory approval to expand. We don't need to apply for licenses in each jurisdiction. We don't need regulatory sign-off to add a new merchant community. The protocol works the same way in Spain, Venezuela, El Salvador, the Philippines. The code doesn't change. The rules don't change. The architecture is jurisdiction-independent.

A custodial platform would need separate licenses for each country. A permissionless protocol just works everywhere.

This is why we went through the regulatory reanalysis. Not just to launch legally in the EU. But to build architecture that's inherently compliant everywhere.

---

[SECTION 10 - Compliance as foundation, not checkbox]

Here's the final point I want to make.

Compliance often feels like a checkbox. A legal hurdle to jump. Something that slows down engineering.

But Asgaya is different. Compliance is the foundation of the architecture.

The covenant system exists because of MiCA. The pull system's design is shaped by PSD2. The permissionless bulletin board is the solution to PSD2's intermediation constraints. The separation of roles—senders, recipients, merchants—exists because we need clear boundaries between regulated services and unregulated services.

If we'd built for convenience first and asked legal questions later, we'd have built a custodial platform. Easier technically. Better UX. But impossible to launch legally.

Instead, we built for regulatory viability first. That forced us to be clever. That forced us to think about risk allocation. That forced us to distribute responsibility across participants instead of centralizing it. And that—it turns out—created a more resilient, more scalable, more user-aligned system.

Compliance didn't constrain us. It shaped us. It made us better.

This is what regulatory architecture means. You don't fight the rules. You build something that's naturally compliant. The technology reflects the legal reality. The incentives align. The system works.

---

[CLOSING - Thoughtful and grounded]

So here's what happened on May tenth.

We could have given up. Abandoned the pull system. Accepted a simpler, less effective design. Traded adoption for compliance.

Instead, we did the harder thing. We understood the regulations. We understood what triggered licensing requirements. We understood why custody and intermediation were the pressure points.

And we designed around it.

No custody because covenants are blockchain-native, not company-held. No intermediation because peers transact directly. No gate-keeping because bulletin boards are permissionless.

Three design principles that made everything else possible.

This is Radio Asgaya. We don't just build protocols. We build protocols that are legal, sustainable, and aligned with how people actually send money.

Thanks for listening. This is Radio Asgaya.

[END]
