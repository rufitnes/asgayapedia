# Radio Script: Multi-Payment Methods

**Episode 17:** Senders can pay via Bizum, ATM deposits, SEPA, or Revolut. Here's why that matters.

**Duration:** ~10 minutes

**Tone:** Conversational and strategic

**Target Audience:** Asgaya stakeholders, phase planning team, payment infrastructure builders

---

INTRO - Setting the stage for a scaling challenge

Welcome to Radio Asgaya. Bizum, SEPA, ATMs, Revolut—sender's choice.

I'm your host, Claudia Sonnet 4.5.

Picture this. You're trying to launch Phase Zero of Asgaya. You need one hundred fifty senders to prove the model works. But right now, you've only got a way for people in Spain to send money—through Bizum. That sounds great until you do the math. Bizum in Spain might reach one hundred users. That's not enough. You need payment method diversity to unlock the full European diaspora. And that diversity is what turns one hundred potential senders into three hundred fifty.

This is Episode 17 of Radio Asgaya. Today we're talking about why accepting multiple payment methods isn't just nice to have. It's how you actually scale from one hundred senders to one hundred fifty. And why cash-based workers in Europe become your best customers when you let them pay how they already pay.

---

THE PROBLEM - Why Bizum alone isn't enough

Let's start with why this even matters. Bizum works great in Spain. It's instant. It's free. It's how Spanish people move money to each other. But here's the hard truth: most of the Venezuelan diaspora doesn't live in Spain.

You've got Venezuelans in Portugal. In Italy. In Germany. In France. In Germany especially—there's a huge Venezuelan community in Berlin. And in all those countries, Bizum doesn't work. They use their own mobile payment systems. Different banks. Different apps. Different speed and cost structures.

So what happens when you limit yourself to Bizum? You're leaving four-fifths of your potential sender pool outside in the cold. You want one hundred fifty senders for Phase 0. You're sitting at one hundred. And you're stuck because your payment infrastructure can't reach them.

This is the scaling bottleneck that kills early projects. You build the perfect product. The economics work. The merchant flow is solid. But your payment method only works in one country. So you stall.

---

THE SOLUTION PART ONE - Building a multi-method bridge

Here's the shift in thinking. What if instead of asking your senders to use Bizum, you ask: how do our senders already move money around? And how do we accept that?

Let's talk about Bizum first since we've got it. Bizum in Spain remains your killer app. Instant, free, popular. A sender pays a BCH seller one hundred euros via Bizum. The seller receives it immediately and locks one hundred seven euros worth of their own Bitcoin Cash into a covenant—one hundred euros face value plus a seven percent volatility buffer. The fiat goes to the seller's bank account. The seller can recycle that one hundred euros to the next sender immediately. Everyone's happy.

But now let's expand. In Portugal, you accept MB Way. MB Way is Portugal's answer to Bizum. Same speed, same convenience, same instant transfer. A Portuguese sender pays the same way, gets the same outcome. The BCH seller—maybe the same person, maybe someone in Portugal—receives the payment, adds their buffer, creates the covenant. Capital recycling works the same way. Everything else is identical.

Now here's where it gets interesting. What about people without a smartphone app? What about migrants doing cash-based work? Construction workers. Day laborers. People who get paid cash and keep cash in their pocket. They don't have a bank account. They don't use Bizum. They use ATMs and corner stores.

This is where ATM deposits change the game.

---

ATM DEPOSITS - Bringing cash into the system

Let's imagine Diego. He's a construction worker in Frankfurt. He gets paid in cash every Friday. He wants to send two hundred euros to his sister in Caracas. He doesn't have a German bank account. He doesn't have Bizum. He doesn't trust banks very much, honestly. But he's got two hundred euros in his pocket.

With ATM deposits, here's what Diego does. He opens the Asgaya app, taps a few buttons to create a covenant request for two hundred euros to his sister in Caracas, and selects a BCH seller who accepts ATM deposits. The app shows that seller's bank account details and the CashAccount concept field he needs to include.

Diego walks to any ATM in Frankfurt. He enters the seller's account number into the ATM, types the CashAccount concept field into the reference line, and deposits two hundred euros cash. The deposit is instant and irreversible—just like Bizum. The seller's bank app shows the deposit with the CashAccount concept field. The seller matches it to Diego's covenant request and locks their own Bitcoin Cash into the covenant. The covenant goes live.

Diego has five minutes to complete the ATM deposit after creating the covenant request. Whether five minutes is enough in practice is one of the things Phase Zero will validate.

Same logic works at a bank teller: Diego hands cash to the teller, gives them the seller's bank details and the CashAccount concept field, and the teller processes the deposit. Done.

Here's the beautiful part. It's EU-wide. Diego can use any ATM. Any ATM accepts cash. Cash is the oldest, most universal payment method. And every ATM network in Europe is connected. The deposit clears in seconds to minutes, and it's reliable.

Now, why does this matter for your sender pool? Because cash-heavy workers are everywhere. Construction, agriculture, service industry, gig economy. These people represent thousands of potential senders across the EU. But you only unlock them if you accept the method they already use: cash.

The tradeoff? ATM deposits are slower than Bizum. Bizum is instant. ATM deposits might take a few minutes to reconcile and confirm. But they're absolutely reliable. And they unlock an entire demographic.

---

SEPA AND NEOBANKS - The bridge for the banked

But we're not done yet. Some senders have bank accounts. They're not using mobile payments—maybe they prefer traditional banks. These senders need a way that works with their existing financial setup.

SEPA—Single Euro Payments Area—is your tool here. And as of 2025, EU regulation now mandates SEPA Instant Credit Transfer across the eurozone. This means SEPA transfers that used to take one to two hours now arrive in under ten seconds. It's effectively as instant as Bizum, but it works across every EU country and every bank account.

A sender in Italy with a regular bank account can transfer via SEPA Instant. The money hits your account in seconds—just like Bizum or MB Way. The BCH seller verifies the deposit immediately, locks their Bitcoin Cash into the covenant, and the sender's remittance is on its way. No waiting. No timing coordination issues. The instant nature of SEPA now makes it equivalent to any mobile payment method.

Then there are the neobanks. Revolut. Wise. These are platforms that migrants love because they work across borders with low fees and fair exchange rates. A Venezuelan in Berlin might have a Revolut account. They can send via Revolut instantly to your account. The seller confirms receipt in real time. The flow mirrors Bizum.

So your payment method menu now looks like this: Bizum for Spain, MB Way for Portugal, ATM deposits for anyone with cash, SEPA for traditional bank transfers, and Revolut or Wise for people using neobanks. You've gone from one method to five methods. And that one change expands your sender pool from one hundred to well over one hundred fifty.

---

THE RECONCILIATION CHALLENGE - Why the concept field matters

Now here's where it gets technical, but stick with me. This is important for how the whole system actually works.

All these payments—Bizum, MB Way, ATM deposits, SEPA, Revolut—they all end up in the same place. They go to a BCH seller's bank account. One account. Multiple payment methods feeding into it.

This is a reconciliation problem. The seller's bank shows a deposit. But where did it come from? Which sender? Which potential remittance?

Asgaya solves this with something called the concept field. It's that little text box you fill in when you make a bank transfer. Usually you'd write "invoice 123" or "payment for dinner." In Asgaya, the sender writes something very specific: they include the recipient's CashAccount. The unique identifier for who this money is going to.

Here's the flow. Sender wants to send two hundred euros to their cousin with CashAccount number three-zero-four-two-seven. Sender pays a BCH seller via Bizum, SEPA, whatever method. In the Bizum app, or in the concept field, sender writes: CashAccount three-zero-four-two-seven.

The seller receives the notification from their bank. They see a deposit for two hundred euros with concept field: three-zero-four-two-seven. They look it up in the system. It matches an active remittance—same amount, same recipient account. They know exactly who this is for. The seller locks their own Bitcoin Cash into the covenant—two hundred fourteen euros worth total, including the seven percent volatility buffer. The system matches payment to remittance automatically through that concept field.

This is why it's critical. Without it, you've got deposits flowing in from five different payment methods with no clear attribution. You can't match sender to recipient. The whole system breaks down.

But get it right—and this works elegantly. Bizum deposit comes in with concept attached. SEPA transfer comes in with concept attached. ATM deposit confirmation comes in with concept attached. The system reads the concept field, matches it to a remittance, and executes.

---

WHY THIS SCALES - The numbers behind diversity

Let's do the math. You start with Bizum. You reach Spanish Bizum users. Maybe one hundred senders.

You add MB Way. Portugal's got Portuguese-speaking migrants. You add fifty senders.

You add ATM deposits. Every cash-using worker in the EU suddenly has a pathway. You add another hundred senders. Construction, agriculture, service industry—these workers exist in every European city.

You add SEPA. Anyone with a traditional bank account, across the EU, now has a method. You add another fifty senders.

You add Revolut and Wise. Neobank users, popular with younger migrants. You add another fifty senders.

Where you started at one hundred senders with one payment method, you now have four hundred plus senders across five payment methods. Phase 0 needs one hundred fifty. You've got room to grow. You've got redundancy. You've got geographical distribution. If one method hits regulatory friction, you've got four backups.

And here's the secret that most remittance companies miss: it's not just about volume. It's about meeting people where they are. A migrant who trusts ATM deposits because they've never had a bank account—they're not going to switch to Bizum for you. But if you offer both, you get them. A young professional using Revolut—they'll never move to SEPA. But if you accept Revolut, you capture them.

---

THE SELLER SIDE - Recycling capital across methods

Let's bring this back to the seller. Remember, the whole system depends on sellers being able to recycle capital fast.

A seller in Spain receives one hundred euros via Bizum. Instant. They add seven euros from their own funds, create the covenant. They can recycle that one hundred euros to the next sender within minutes. The seven euro buffer stays locked in the covenant.

A seller in Frankfurt receives one hundred euros via ATM deposit. It takes a few minutes to clear and reconcile. But as soon as that deposit confirms, they add their buffer, create the covenant, and can recycle the one hundred euros immediately.

A seller receives one hundred euros via SEPA Instant. It clears in seconds. They immediately add their buffer, create the covenant, and can recycle that one hundred euros to the next sender within minutes—just like with Bizum or MB Way.

The beauty is this: you don't need the same seller for each method. You need a network of sellers across Europe. A Bizum specialist in Spain. An ATM deposit specialist in Frankfurt. A SEPA specialist in Portugal. Each one optimized for their local payment method. Each one able to recycle capital at whatever speed their payment method requires.

This is how you scale from one seller to a network. Payment method diversity forces you to build a geographic network. And that network is what actually scales Phase 0.

---

CLOSING - Multi-payment is multi-sender

Here's what we've learned today. Limiting yourself to one payment method limits you to one geography. Bizum only reaches Spain. You cap out at one hundred senders.

But accept five payment methods—Bizum, MB Way, ATM deposits, SEPA, and neobanks—and you've cracked the European Venezuelan diaspora. You've reached every payment preference. Cash-based workers. Banked professionals. Mobile payment users. Neobank enthusiasts. Everyone's got a way to send.

Phase 0 doesn't happen with one hundred senders. It happens when you hit one hundred fifty senders across multiple countries, multiple payment methods, multiple cultures of how people actually move money.

Multi-payment isn't a technical feature. It's the difference between a Spain-only remittance app and a European remittance network.

That's Radio Asgaya, Episode 17. Thanks for listening.

---

[END]
