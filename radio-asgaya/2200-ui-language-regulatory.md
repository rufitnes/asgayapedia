# Radio Script: UI Language Regulatory

**Episode 20:** One word in the UI can trigger a million-euro licensing requirement. Here's what we can't say.

**Duration:** ~8 minutes

**Tone:** Conversational, instructional

**Target Audience:** Developers, protocol designers, anyone building permissionless financial systems

---

[INTRO]

Welcome to Radio Asgaya. Measuring our words.

I'm your host, Claudia Sonnet 4.5.

Today we're talking about something that might sound boring but could cost you a million euros. We're talking about words. Specifically, the words you choose in your user interface. Because regulators don't care how your system actually works. They care about what you call it. And if you call it the wrong thing, you get the wrong regulatory classification. Wrong classification means illegal operation. All because of vocabulary.

Let me give you an example. Your interface has a field that says "escrow balance." That one word—escrow—just triggered a licensing requirement in the European Union. You're now operating a custodial asset service provider. You need a CASP license. That's Crypto Asset Service Provider. And if you don't have that license, you're breaking the law.

But here's the thing: your system might not actually be custodial at all. Your smart contract might hold funds with specific release conditions. Your users might have full control. But the word escrow changed everything. That's why this matters.

---

[SECTION 1 - Why Language Matters]

Let me explain the regulatory framework briefly. In Europe, we have multiple regulations that classify crypto services. The Markets in Crypto Assets Regulation—that's MiCA. The Payment Services Directive—PSD2. These regulations say different things based on how you describe your service.

Here's the pattern: regulators read your user interface and documentation. They see what you call your service. Based on that description, they classify you. The classification determines which rules apply to you.

If you call something a payment service, you're under PSD2. If you call something custody, you're under MiCA as a CASP. If you call something an exchange, you're under different rules entirely. The language in your UI doesn't just describe your service. It legally defines your service.

Now, Asgaya is building something that doesn't fit neatly into those boxes. We're building a bulletin board. A peer-to-peer information platform. Our smart contract, which we call a covenant, enables direct transactions between participants. We don't hold funds. We don't intermediate. We don't regulate the trades. We just provide the technical infrastructure for people to find each other and settle directly.

But if we use the wrong words in the interface, regulators might classify us as something we're not. And once you're classified, you're trapped in that category until you appeal or change your language. Which is expensive.

So Asgaya made a choice: we describe exactly what we do, using language that's technically honest and regulatory accurate.

---

[SECTION 2 - The Prohibited Words]

Let me walk through the words we cannot use and why each one is dangerous.

First: escrow. This word implies that someone—likely you, the platform—is holding client funds on their behalf. That's custody. The moment a regulator reads the word escrow in your interface, they're thinking CASP license. They're thinking you need authorization. They're thinking capital requirements, audit obligations, segregated accounts. All from one word. So we don't use escrow. Period.

Second: custody. This is even more explicit. If you say your system provides custody services, you've just told a regulator exactly what category you're in. Custody means you control the funds. You hold the private keys. You're responsible for security, recovery, all of it. Even if your technical architecture doesn't actually work that way, the word custody will drag you into that regulatory bucket. So we don't use it.

Third: payment service. This one is tricky because it sounds innocent. But in regulatory language, a payment service is something that moves money on behalf of users. It intermediates. You accept euros from one person, move them to another person. You're the middle. The moment you're the middle, you're under PSD2, Payment Services Directive, and you need a license. Asgaya doesn't move money. We provide information about where money can be moved. So we don't call ourselves a payment service.

Fourth: exchange. If you call yourself an exchange, you're saying you're matching buyers and sellers, taking custody of their assets during the trade, possibly setting prices. That's a completely different regulatory animal. We don't do that. So we don't use that word.

There's also wallet service. This implies custodial management of crypto. If you're holding someone's crypto in a wallet, you're a CASP. Even if it's a smart contract wallet, even if users control the keys, the word wallet service can trigger the classification. So we avoid it in our interface language.

---

[SECTION 3 - The Required Language]

Now let me tell you what we say instead. And why each term is both technically accurate and regulatory safe.

We call Asgaya a bulletin board. That's exactly what it is. A place where information is posted. Anyone can read the bulletin board. Anyone can post. The platform doesn't control the content. It doesn't validate the trades. It's infrastructure. That's the most accurate description and it doesn't trigger any restrictive regulatory category. A bulletin board is an information service. Information services have minimal licensing requirements.

We describe relationships as peer-to-peer. This is critical. Peer-to-peer means direct participant interaction with no intermediary. You're not going through us. You're not trusting us with your money. You're finding another peer, negotiating directly, and settling directly. When regulators see peer-to-peer, they understand there's no intermediation. There's no custody. There's just two people making a deal. No license required.

We call our smart contract a covenant. This is both technically precise and regulatory neutral. A covenant is a smart contract condition on Bitcoin Cash. It specifies when and how funds are released. But the word covenant doesn't trigger any regulatory assumptions. It's not a loaded term. A merchant and a recipient can covenant together. That's a private agreement. Not a service.

We talk about merchants, not platforms. A merchant is a business that accepts payment. That's what your local coffee shop is. That's what your neighborhood store is. When we say merchants use Asgaya, we're saying businesses can now accept Bitcoin Cash directly from customers. We're not saying Asgaya is providing merchant services. The merchant is providing merchant services. We're providing the covenant infrastructure.

And we use the word claim instead of receive or pull or withdraw. This is subtle but important. When a recipient claims a covenant, they're actively pulling the payment to themselves. The merchant doesn't push it. The recipient pulls it. The language reinforces that recipients control the timing and the action. That's technically accurate and it protects you from claims that you're operating a payment service.

---

[SECTION 4 - What This Looks Like in Practice]

Let me give you a concrete example of what we don't say versus what we do say.

If you want to show users the balance available to them, don't say "Check your escrow balance." That's regulatory suicide. Instead say: "Check the bulletin board for active covenants." See the difference? The first assumes you're holding money. The second describes where to find the information about transactions they're involved in. Same feature. Completely different regulatory implications.

If you're describing how recipients get paid, don't say "The merchant receives payment from our payment service." Say instead: "The merchant claims the covenant and receives Bitcoin Cash directly." This describes the actual mechanism. The merchant is the one taking action. The Bitcoin Cash is the asset. There's no intermediation. There's no payment service.

When a user is setting up a transaction, don't ask them to "deposit funds in escrow." Instead, describe it as "Create a covenant with the following terms." The covenant already specifies the terms. The user is creating a binding smart contract condition, not depositing anything with you.

This might feel like we're being pedantic about language. We're not. We're being legally precise. Because in the regulatory environment we're operating in, language is everything. The words in your interface become the evidence that defines what you're doing.

---

[SECTION 5 - Why This Matters for Permissionless]

Here's the deeper point: Asgaya is trying to build something that's genuinely permissionless. We're not gatekeeping. We're not intermediating. We're not taking custody. But if our interface language makes us sound like we are, then we're not actually permissionless anymore. We're pretending to be permissionless while operating as if we are custodial.

That's dishonest. And it's dangerous.

Regulatory compliance doesn't start with lawyers and licenses. It starts with honest language about what you actually do. If your smart contract doesn't hold funds, don't say it does. If your platform doesn't intermediate payments, don't use payment service language. If your users control transactions directly, say that. Say it clearly in the interface.

The reason this matters is that honest language is your defense. When a regulator looks at your interface, they should see technology that matches the description. They should see peer-to-peer infrastructure using peer-to-peer language. They should see bulletin boards described as bulletin boards. They should see covenants and claims and merchants making direct decisions.

Now, let's be honest about the tradeoff. Regulatory-safe language is not always user-friendly language. "Create a covenant" is more technical than "send money." "Claim from bulletin board" is less familiar than "check your balance." We're walking a fine line between making the app approachable and measuring our words carefully to stay compliant.

This is an ongoing balance, not a solved problem. Every word in the interface is a choice between regulatory safety and user familiarity. But here's the reality: if the language is friendly but triggers licensing, the app becomes illegal. If the language is honest but technical, the app stays permissionless and we can teach users what the words mean.

We chose honesty. We chose compliance. And we accept the UX friction that comes with it. Because better to be technical and legal than friendly and shut down.

If the language is honest, the regulatory analysis becomes straightforward: This is an information service with no custody, no intermediation, no payment service function. This is permissionless.

But if the language is misleading—using prohibited terms when your system doesn't work that way—then you're not just gambling with regulatory classification. You're creating evidence that you're misrepresenting your service. That's worse than classification. That's fraud risk.

---

[CLOSING]

So here's the takeaway: regulatory compliance starts with vocabulary. Before you write a single line of code, before you build your interface, decide what you're actually building. Then describe it using language that's both honest and regulatory safe.

Don't use escrow if you're not holding custody. Don't use payment service if you're not intermediating. Don't use wallet service if users control the keys. Don't use exchange if you're not matching and settling trades.

Instead, use language that describes what you actually do. Bulletin board. Peer-to-peer. Covenant. Claim. Merchant. These words are accurate. They're safe. And they're permissionless.

Because one word can trigger a million-euro licensing requirement. But one honest word can also set you free.

Thanks for listening. This is Radio Asgaya.

[END]
