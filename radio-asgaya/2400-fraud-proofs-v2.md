# Radio Script: Fraud Proofs (Payment-First Version)

**Episode 22:** How does a merchant know the money is real? They check the math. How does a sender know the seller won't ghost them? The bank acts as proof.

**Duration:** ~8 minutes  
**Tone:** Conversational  
**Target Audience:** Asgaya users, merchants, senders  
**Version:** 2.0 (Payment-First Architecture)  
**Date:** 2026-05-31

---

## INTRO

Welcome to Radio Asgaya. Trust, but verify.

I'm your host, Claudia Sonnet 4.5.

You walk into a small store in Venezuela. A customer comes up and says, "I have money waiting for you on Bitcoin. Here's my proof." They show you their phone. But here's the question: how do you know they're telling the truth? How do you know the money is actually there? How do you know they can't just claim it twice?

Today we're talking about fraud proofs. And the answer is simple: you check the math.

But there's another question just as important: How does the sender know the BCH seller won't just take their euros and disappear? The answer? Their bank acts as proof.

---

## THE TRUST PROBLEM

Let's start with the real problem. You're a merchant in a small neighborhood store. A customer walks in and tells you they have funds coming through Asgaya. They're about to buy something for one hundred euros. But before you hand over goods or cash, you need proof. Real proof.

You can't just trust their word. You can't just look at their app screen. Why? Because screens lie. Someone could fake an app, show you a payment that doesn't exist, walk out with your goods, and you're left holding nothing. So what do you do?

You verify everything cryptographically. You query the blockchain directly and you check the math. And here's the beautiful part: the math never lies.

---

## THE COVENANT PROOF (PAYMENT-FIRST)

Let's walk through what actually happens. Say the customer's name is Maria, and she has a CashAccount address. Someone in Spain wants to send Maria one hundred euros.

Here's what happens behind the scenes: First, the sender creates a covenant on the Bitcoin Cash blockchain—a smart contract specifying Maria's CashAccount, the amount, and the claim window. The covenant exists, but it's not funded yet.

Second, the sender selects a BCH seller from the bulletin board and requests payment information. The seller's bot responds within seconds with encrypted payment details: their bank account, a reference code, and the amount. This proves the seller's bot is online and responsive right now.

Third, the sender pays one hundred euros to the seller via Bizum or SEPA transfer. This is a normal bank payment—nothing special.

Fourth—and this is critical—the seller's bank sends a notification: "Received €100. Reference: ASGAYA covenant xyz." The seller's bot parses this notification automatically. It verifies the payment is real, came from the correct sender, and matches the expected amount.

Only then, only after the seller's bank confirms the payment, does the seller's bot lock one hundred seven euros worth of Bitcoin Cash into the covenant—one hundred euros face value for Maria, plus seven euros as volatility buffer from the seller's own inventory. The seller signs the funding transaction. The Bitcoin Cash is now locked on-chain.

The seller can recycle that one hundred euros in fiat to the next sender immediately. The seven euro BCH buffer stays locked in the covenant until Maria claims.

The covenant is now live. Maria gets a notification: "€100 ready to claim!"

---

## WHY THIS PROTECTS THE SELLER

Here's what this sequence eliminates: Under the old design, the seller locked their Bitcoin Cash first, then waited for the sender to pay. That was vulnerable. A dishonest sender could create fake covenant requests, lock up the seller's capital, and never pay. The seller would be stuck waiting for timeouts, capital frozen, unable to serve real customers.

Payment-first flips this. The seller never locks their Bitcoin Cash until they receive independent, verifiable proof from their own bank that the payment actually happened. Fake covenant requests cost nothing to ignore. Real payments trigger real bot responses. The seller's bank—an institution they already trust—acts as the notary.

If someone tries to spam the seller with fake covenants? The bot doesn't respond. If someone creates a covenant but never pays? The bot never sees a bank notification. The covenant stays unfunded. The seller's capital is never at risk.

And here's the clever part: obtaining payment info also proves the seller's bot is online. If the seller's bot doesn't respond with payment details within two minutes, the sender knows immediately: don't pay this seller, try someone else. No money changes hands until both sides prove they're operational.

This means Phase 0 doesn't need complex on-chain slashing, seller bonds, or automated arbitration. We have something better: the seller's bank and Spain's legal system. Bizum and SEPA transfers require verified bank accounts. If a dishonest seller takes your money but doesn't lock the Bitcoin Cash, you have bank statements proving payment, the covenant showing which seller you selected, and the police to file a report with. In Spain, fraud is a felony. The seller's identity is tied to their bank account. They can't hide.

Simple. Practical. Works in Phase 0.

---

## FIVE CHECKS (FOR MERCHANTS)

So Maria shows up at your store. She says she has a covenant worth one hundred euros. You, the merchant, need to verify five things.

First check: does the covenant actually exist on the blockchain? You don't take Maria's word for it. Your app queries the Bitcoin Cash blockchain directly and looks up her CashAccount. If there's a covenant for her, it's there. If there isn't, you see nothing. No covenant, no sale.

Second check: is the covenant properly funded? You see the covenant, but is there enough Bitcoin Cash locked in it? You know the current exchange rate. One hundred euros at today's rate tells you how much Bitcoin that should be. You check the blockchain. Is that amount there? Yes? Good. The covenant is funded.

Third check: has the covenant expired? Covenants have time windows. The sender and Maria coordinated. The sender said, "I'm sending the covenant now. Can you claim within four hours?" Maria said yes. So the covenant has a four-hour window. If you check it after four hours and twenty minutes have passed, you see nothing. The covenant auto-refunded to the sender. But if you're within the window, the covenant is still active.

Fourth check: is the covenant properly collateralized? When the seller funded the covenant, they locked at least one hundred seven percent of the claim amount. That's your seven percent buffer. Why? Because Bitcoin price moves. The buffer protects you. You check: is the covenant still over one hundred percent collateralized right now? Yes? Then you're safe. If price dropped too much, the covenant would have auto-refunded already.

Fifth check: is the BCH seller's signature on the covenant? The seller committed their own Bitcoin Cash into the covenant. They signed the funding transaction. You verify that signature cryptographically. That proves the covenant was properly funded by a real seller with real BCH. It's not fake. It's not spoofed. The math says so.

---

## WHY YOU CAN'T BE FOOLED

Now here's where fraud becomes impossible. You're not trusting anyone. You're not checking an app that Maria controls. You're not even checking what Maria's phone shows you. You're checking the blockchain directly. The blockchain is like a ledger that everyone can see, and no one can fake.

If Maria tries to show you a fake covenant on a fake app, you query your own app. You query the blockchain. You see nothing. "Sorry," you tell her. "There's no covenant on the blockchain for you. No sale."

If Maria tries to double-spend the covenant—claim it twice—the blockchain won't let her. The covenant releases the Bitcoin Cash exactly once. After the first claim, it's gone. She can't claim it a second time. The blockchain enforces this. She can try, but the blockchain will reject the second claim automatically.

What if she tries to claim with someone else's covenant? The covenant is locked to her specific CashAccount address. Only someone with that address can claim it. No one else.

---

## MULTI-SIGNATURE PROTECTION

Here's another layer of security. When Maria claims at your store, the covenant requires both signatures: Maria's signature—because she's the recipient and destination wallet holder—and your signature as the merchant, because you're accepting the BCH. Both must sign to release the BCH to you. This makes the cash-out atomic. No trust required. The recipient alone can't just walk away with the Bitcoin. You have to agree. You sign. They sign. Both signatures together unlock the Bitcoin.

This means you control the final step. You hold onto your signing key until you're sure. Maybe you do a small test transaction first. You gain confidence. Then you handle larger amounts.

---

## WHAT IF THE PRICE CRASHES

Let's talk about worst-case scenario. Maria shows up. You've confirmed the covenant. All five checks passed. But then Bitcoin crashes. The price drops ten percent. The covenant is now undercollateralized. It's below one hundred percent. What happens?

Here's the critical part: the covenant doesn't wait for expiry. It doesn't sit there hoping the price bounces back. It immediately refunds. The Bitcoin goes back to the sender's wallet automatically. The blockchain handles it. No humans involved. Just code.

Maria says, "Where's my covenant?" You check the blockchain. It's gone. It refunded. "The covenant is gone," you tell her. "No sale today. The price moved too much." Maria tries to claim elsewhere, but she can't. The covenant is gone. The sender has their money back.

The sender bears the loss. They bought Bitcoin at one hundred seven euros worth. The crash means they only got ninety-five euros worth back. But that's why they coordinated a four-hour window with Maria, not twenty-four hours. With a short window, Bitcoin rarely drops more than three percent. The seven percent buffer usually protects both of them.

And here's the thing: because the seller got paid first—before locking Bitcoin—the seller is always neutral. The seller sold Bitcoin to the sender at market rate. If the covenant aborts, the sender gets the Bitcoin back at the new (lower) market rate. The seller keeps the euros and can buy back the same amount of Bitcoin for less. The seller made their half-percent fee on the exchange and is neutral on the volatility. The sender took the volatility risk, which is fair—they're the one who wanted to send money to Venezuela.

---

## THE SECURITY MODEL

So what's the actual security model here? It's not reputation. It's not trust. It's not some company promising you something. It's math for the merchant, and it's the banking system for the sender.

Bitcoin Cash uses cryptography. Covenants use smart contracts—code that runs on the blockchain. Signatures use elliptic curve math. You either have a valid signature or you don't. The covenant either released or it didn't. The Bitcoin either went somewhere or it didn't. There's no gray area. There's no negotiation. The math is final.

A merchant's security is based on verifying this math directly. You don't trust Asgaya. You don't trust Maria. You don't trust the sender. You trust the Bitcoin Cash blockchain. And you verify everything yourself by querying it.

A sender's security is based on their bank. They only pay after they've confirmed the seller's bot is responsive. And they have bank statements proving payment if the seller ghosts. In Spain, that's enough. Fraud is prosecutable. The seller's bank account is verified. No complex on-chain systems needed.

Simple. Effective. Ready for Phase 0.

---

## THE TRANSACTION ITSELF

Once all five checks pass, you're ready. Maria hands you her CashAccount. She's the recipient. You're the merchant. The covenant has a specific amount of Bitcoin Cash locked for her specific address. You initiate the claim. She signs. You sign. The blockchain processes both signatures. The Bitcoin Cash moves from the covenant to you. The transaction is complete. You got paid in Bitcoin. She got the cash. The covenant is closed.

Everyone is happy. And it all works because the math is perfect and the payment system is simple.

---

## CLOSING

Here's the key takeaway. Asgaya's fraud protection is simple: cryptography for the merchant, banking for the sender.

Merchants verify covenants cryptographically. They check the blockchain directly. They verify seller signatures. They check collateral. They make sure the covenant exists and hasn't expired. Fraud at this layer is mathematically impossible. Recipients can't fake covenants. They can't double-spend. They can't forge signatures.

Senders are protected by payment-first. They don't pay until the seller's bot proves it's online by delivering payment info. Once they pay, their bank acts as notary—cryptographic proof of payment. If a seller ghosts, the sender has bank statements, covenant records, and Spain's legal system. No complex on-chain arbitration needed. Just police, courts, and the fact that fraud is a felony.

The result? Fraud doesn't happen because it's cryptographically impossible for merchants and legally prosecutable for dishonest sellers. Not through complex smart contracts. Not through DAO juries. Through math, banks, and the legal system.

This is how Asgaya works. Trust, but verify. Then make verification simple.

Thanks for listening. This is Radio Asgaya.

---

END
