# Radio Script: MUSD Integration

**Episode 21:** We give up. You can have your dollars.

**Duration:** ~10 minutes

**Tone:** Conversational, pragmatic

**Target Audience:** Freelancers, remittance users, anyone who wants stable value

---

[INTRO]

Welcome to episode 21 of Radio Asgaya: We give up. You can have your dollars—I'm your host, Claudia Sonnet 4.5.

Here's the thing about Bitcoin Cash. It's fast. It's borderless. It's permissionless. It's everything we want in money infrastructure.

But it's volatile. The price swings five, ten, fifteen percent in a day. And for someone living paycheck to paycheck in Venezuela, that volatility is a problem. You can't pay rent with something that might be worth ten percent less tomorrow.

So we're adding MUSD. One MUSD equals one US dollar, always. It lives on the Bitcoin Cash blockchain. Same speed, same permissionless nature, but stable.

Today we're talking about how MUSD works in Asgaya. How you buy it. How you send it. How merchants cash it out. And why this is user choice, not protocol complexity.

---

[SECTION 1 - The Problem: Volatility]

Let's be direct about the problem. Bitcoin Cash is volatile. That's not a bug. That's the nature of a free-floating cryptocurrency with a global market.

For senders, volatility is manageable. You're converting euros to BCH for a few hours, maybe a day. The price moves, but you're not holding long-term.

For recipients, it's different. You receive BCH. You need to buy groceries this week. You need rent money next month. You want to save for something important. And BCH might drop fifteen percent before you can spend it.

That's the problem. Recipients need stability. They need something that holds value. They need something that feels like money.

This is especially true for freelancers. You're a software developer in Venezuela. You get paid in Bitcoin Cash. You have five hundred dollars worth of BCH. But by the time you cash out over three weeks, it might be worth four hundred fifty. Or five hundred fifty. You don't know. And you can't plan around that uncertainty.

So here's what we're doing: we're letting you hold MUSD instead.

---

[SECTION 2 - The Solution: MUSD Sellers on the Bulletin Board]

MUSD is a stablecoin. One MUSD equals one US dollar. It's backed by Bitcoin Cash locked in smart contracts. For every MUSD in circulation, there's one dollar fifty worth of BCH locked as collateral. You can verify this on-chain.

MUSD lives on the Bitcoin Cash blockchain. Same network. Same speed. Same permissionless access. But stable.

Now, here's how Asgaya supports it. We don't integrate MUSD at the protocol level. We don't automate conversions. We don't touch MUSD ourselves.

What we do is simple: we let MUSD sellers list on the bulletin board.

Remember, the bulletin board is where you find BCH sellers. You open the app, you see a list of people willing to sell you Bitcoin Cash in exchange for euros via Bizum. You pick one, you pay them, they send you BCH.

MUSD sellers are the same. They're people who have MUSD and are willing to swap it for Bitcoin Cash. You open the app, you see MUSD sellers listed, you pick one, you create a swap covenant, and you trade BCH for MUSD. Peer-to-peer. No middleman.

Asgaya doesn't touch the MUSD. We just list the seller. Same architecture as BCH sellers. Same bulletin board. Just a new asset type.

---

[SECTION 3 - What Is MUSD and Who Built It]

Before we go further, let's talk about what MUSD actually is and where it came from.

MUSD—Moria USD—is a stablecoin built on the Bitcoin Cash blockchain by Riften Labs. The protocol was designed by Dagur Valberg Johannsson, with the whitepaper published in twenty twenty-four. Version one went live in May twenty twenty-five and has been audited by Hashlock. The idea was simple: build a dollar-pegged stablecoin that lives entirely on BCH, with the same speed and permissionless access, but price stability.

Here's how MUSD works, in simple terms. MUSD is not backed by actual dollars sitting in a vault—it's backed by Bitcoin Cash locked in smart contracts. This is what's called a CDP model—Collateralized Debt Position—similar to MakerDAO on Ethereum.

When you want to mint MUSD, you lock Bitcoin Cash into a Moria vault. Let's say you lock point one five BCH worth one hundred fifty dollars. You can mint up to one hundred MUSD against that collateral. That's a one hundred fifty percent collateralization ratio. You're locking more value than you're minting. That buffer is protection.

If you ever want your BCH back, you repay the one hundred MUSD plus a small amount of interest, and your BCH unlocks. Simple.

But what keeps MUSD pegged at one dollar? Two mechanisms. First, redemption. If MUSD trades below a dollar, arbitrageurs can redeem cheap MUSD by repaying someone else's loan and claiming their BCH collateral at face value. This reduces MUSD supply and pushes the price back up. Second, liquidation. If your collateral drops below one hundred twenty percent—meaning BCH price crashed—your position gets liquidated. Third parties repay your loan and claim your BCH, plus a premium. This keeps the system solvent.

And here's the key: MUSD is over-collateralized. You don't mint one dollar of MUSD for every dollar of BCH. You lock one dollar fifty of BCH for every dollar of MUSD. That extra fifty percent is a buffer. It protects against BCH price volatility and ensures the system stays stable even when markets swing.

And that over-collateralization model? That's where Asgaya got the idea for the seven percent volatility buffer.

When we designed the covenant system, we needed a way to protect against Bitcoin Cash price drops during the claim window. We studied how MUSD handles volatility—by holding more reserves than strictly necessary. We applied the same principle: sellers lock one hundred seven euros worth of BCH for a one hundred euro remittance. That extra seven percent is the buffer. If BCH drops five percent, the covenant still holds enough value. The recipient is protected. The merchant is protected.

MUSD taught us that over-collateralization works. It's not wasteful. It's insurance. And insurance is what makes volatile systems reliable.

So credit where it's due: MUSD pioneered stablecoin infrastructure on Bitcoin Cash, and their design inspired one of the core mechanisms that makes Asgaya's covenant system safe.

---

[SECTION 4 - How to Buy MUSD: The Atomic Swap]

Let's walk through how you actually buy MUSD.

You open the Asgaya app. You navigate to the bulletin board. You see MUSD sellers listed. Each one shows their spread, their available liquidity, their reputation score.

You pick one. Let's say you want to swap one hundred euros worth of BCH for one hundred MUSD.

You create a swap covenant. This is a smart contract that says: "I will send you one hundred euros worth of Bitcoin Cash. You will send me one hundred MUSD. Both must happen, or neither happens."

The seller sees your covenant request. They lock one hundred MUSD into their side of the covenant. You lock one hundred euros worth of BCH into your side.

Now there's an oracle. A price feed that both sides trust. The oracle verifies the exchange rate. It confirms that one hundred euros worth of BCH equals approximately one hundred MUSD at current market rates.

Both sides are locked. Both sides verified. The covenant executes atomically. BCH goes to the seller's address. MUSD goes to your wallet.

Done. You now hold one hundred MUSD. Stable. On-chain. In your control.

This is peer-to-peer. The seller is not an Asgaya employee. They're just another participant on the bulletin board. They're providing liquidity. They're earning a small spread. And you're getting stable value.

---

[SECTION 5 - Sending MUSD as a Remittance]

Now here's where it gets interesting. You can send MUSD as a remittance.

Let's say you're a sender in Spain. You want to send one hundred euros to your cousin in Venezuela. Normally, you'd pay a BCH seller one hundred euros via Bizum. The seller locks BCH into a covenant. Your cousin claims the BCH from a merchant.

But now you have another option. You buy one hundred MUSD first. You swap your BCH for MUSD using the process we just described. Now you have one hundred MUSD in your wallet.

You create a remittance covenant addressed to your cousin's CashAccount. But instead of locking BCH into the covenant, you lock MUSD. The covenant says: "This MUSD is for my cousin. They can claim it by presenting proof of identity and their CashAccount."

Your cousin gets notified. They go to a merchant. The merchant validates the MUSD covenant. The merchant hands over one hundred euros worth of local currency. Your cousin walks away with cash. The merchant now has one hundred MUSD.

The difference? Your cousin received stable value. The MUSD didn't fluctuate between when you sent it and when they claimed it. No volatility risk. No price swings. Just stable dollars.

---

[SECTION 6 - How Merchants Handle MUSD]

Let's talk about the merchant side. Merchants can now receive either Bitcoin Cash or MUSD when they validate covenants.

If they receive BCH, they do what they've always done. Hold it, sell it to a BCH buyer, or use it to pay suppliers.

If they receive MUSD, they have the same options. They can hold it as stable savings. They can sell it to a MUSD buyer on the bulletin board—someone who wants to swap BCH for MUSD. Or they can accumulate MUSD and cash out gradually.

Here's why this matters for merchants. A merchant in Venezuela who receives five hundred MUSD over a month doesn't need to cash it all out immediately. They can hold it. The value doesn't evaporate. They can wait until they need local currency, then sell MUSD to someone who wants it.

This is especially useful for merchants who serve freelancers. Freelancers get paid in large amounts. They want to cash out slowly. The merchant can accept MUSD from the freelancer, give them local currency in small batches, and hold the rest as stable inventory.

Everyone wins. The freelancer gets stable savings. The merchant earns fees on each cash-out. The MUSD circulates locally without ever touching a centralized exchange.

---

[SECTION 7 - Why This Works: User Choice, Not Protocol Automation]

Here's what we're NOT doing. We're not automating MUSD conversion. We're not integrating a DEX at the protocol level. We're not forcing anyone to use MUSD.

What we're doing is giving users a choice. You want to send BCH? Great. The system works exactly as it always has. You want to send MUSD because your recipient needs stability? Also great. The system supports that too.

The key insight is this: the bulletin board is asset-neutral. It doesn't care if you're listing BCH or MUSD or any other Bitcoin Cash token. It's just a marketplace. Buyers and sellers find each other. They create covenants. They swap assets peer-to-peer.

Asgaya doesn't touch the assets. We don't custody. We don't intermediate. We don't convert. We just list sellers. Same architecture we've had since day one. Just more asset types.

This keeps the protocol simple. No Cauldron DEX integration. No automated swaps. No protocol-level complexity. Just peer-to-peer listings.

And it gives users what they actually want: the option to hold stable value without leaving the Bitcoin Cash ecosystem.

---

[SECTION 8 - Who Can Be a MUSD Seller]

Let's talk about who actually provides MUSD liquidity.

Anyone can be a MUSD seller. If you have MUSD and you want to earn a spread by swapping it for BCH, you list yourself on the bulletin board. Same reputation system as BCH sellers. Same listing rules. Same permissionless access.

Here's who we expect to see:

BCH sellers diversifying their inventory. A BCH seller who already provides fiat-to-BCH liquidity might also hold MUSD. They can offer both. More services, more fees.

Merchants with surplus MUSD. A merchant who received MUSD from freelancers might not need all of it immediately. They can list as a MUSD seller, swap some of it back to BCH, and earn a spread.

Onboarders providing liquidity to their merchants. Remember Episode 15.5? Onboarders recruit merchants and become their trusted buyers. An onboarder with MUSD can provide stable liquidity to the merchants they recruited.

Dual citizens doing arbitrage. Someone with access to both Spanish and Venezuelan payment rails might buy MUSD in one market and sell it in another, earning the spread.

The market decides. No gatekeeping. No approval process. If you have MUSD and you want to sell it, you list it. The bulletin board handles the rest.

---

[SECTION 9 - The Freelancer Use Case]

Let me bring this back to the freelancer problem from Episode 2.

A freelancer in Venezuela gets paid five hundred euros for a project. Normally, they'd receive five hundred euros worth of BCH. Great. But they need to cash out over three weeks because the merchant doesn't have five hundred euros in cash on hand.

During those three weeks, BCH might drop ten percent. Their five hundred euros becomes four hundred fifty. They lose fifty euros to volatility. Not because they made a bad decision. Just because the price moved.

With MUSD, here's what happens instead. The freelancer receives five hundred MUSD. Stable. No price movement. They hold it in their wallet.

Week one: They go to the merchant. "I want to cash out fifty MUSD." The merchant gives them local currency equivalent to fifty dollars. The freelancer walks away with cash. The merchant holds fifty MUSD.

Week two: Same thing. Another fifty MUSD cashed out.

Week three: Another fifty MUSD.

By the end of the month, the freelancer has cashed out their entire payment. They got exactly five hundred dollars worth of local currency. No volatility loss. No timing risk. Just stable value, deployed at their own pace.

That's the difference MUSD makes. Not for everyone. Not for every use case. But for the people who need stability, it's transformative.

---

[CLOSING]

So here's the takeaway. We give up. You can have your dollars.

Bitcoin Cash is amazing. But it's volatile. And for some people, that volatility is a deal-breaker. So we're letting you hold MUSD instead.

MUSD sellers list on the bulletin board. You swap BCH for MUSD peer-to-peer. You can send MUSD as a remittance. Merchants can accept MUSD and cash it out in batches. Freelancers can save in MUSD without leaving the BCH ecosystem.

This isn't protocol automation. This isn't Asgaya integrating a stablecoin. This is the bulletin board doing what it was always designed to do: let people list assets, find each other, and trade peer-to-peer.

We don't custody. We don't convert. We don't touch MUSD. We just list the sellers. Same architecture. Same permissionless nature. Just more options.

And if you want Bitcoin Cash instead? Perfect. Nothing changes. The system works exactly as it always has.

User choice. Not protocol complexity. That's how we scale.

Thanks for listening. This is Radio Asgaya.

---

[END]
