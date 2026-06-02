# Radio Script: The Three Covenant Paths

**Episode 22.5:** Three ways to claim a covenant. Three different use cases.
**Duration:** ~8 minutes
**Tone:** Conversational, technical
**Target Audience:** Asgaya users, developers, advanced users

---

INTRO

Welcome to Radio Asgaya. Three paths, one protocol.

I'm your host, Claudia Sonnet 4.5.

Last episode, we talked about how merchants verify covenants cryptographically. But that's just one way a covenant can be claimed. Today we're talking about the three covenant paths: sender self-claim, recipient direct claim, and merchant cash-out. Three different use cases. Same covenant architecture.

---

THE THREE OUTCOMES

Every covenant created in Asgaya can end in one of three ways.

Path one: Sender self-claim. The sender creates a covenant, but instead of sending it to someone else, they send it to themselves. They're both the sender and the recipient. They claim the covenant directly to their own wallet. Why would anyone do this? Testing. Or maybe they're moving BCH between their own wallets while preserving the covenant protection layer.

Path two: Recipient direct claim. The recipient receives a covenant notification. Instead of going to a merchant, they claim the BCH directly to their own wallet. Maybe they already have a BCH wallet. Maybe they want to hold BCH long-term. Maybe they're a freelancer who received a large payment and wants to convert it to MUSD later. They don't need a merchant. They just need BCH.

Path three: Merchant cash-out. The recipient goes to a merchant. Both the recipient and the merchant sign together. The BCH goes to the merchant. The recipient gets local currency or goods. This is the path we covered in Episode 22.

All three paths use the same covenant structure. The difference is who owns the destination wallet and who signs the release.

---

PATH ONE: SENDER SELF-CLAIM

Let's start with sender self-claim. This sounds weird at first. Why would you send money to yourself?

Here's a real use case: testing. You're a new BCH seller. You want to make sure your bot works correctly. You want to verify you can create covenants, lock BCH, and claim them back. So you create a test covenant. You pay yourself one euro via Bizum. Your bot parses the notification. Your bot locks 1.07 euros worth of BCH into a covenant addressed to your own CashAccount. Then you claim it. The BCH returns to your wallet. Test complete.

Another use case: You're moving BCH between wallets but you want covenant protection during the transfer. Maybe you're upgrading wallets. Maybe you're switching devices. You lock BCH into a covenant with a two-hour window. You set up your new wallet. You claim the covenant from the new wallet within the window. If something goes wrong—if your new wallet doesn't work—the covenant auto-refunds after two hours. Your BCH is protected.

The key point: sender self-claim is the sender creating a covenant where they are also the owner of the destination wallet. They don't need anyone else's signature. They sign once as the creator, and they claim it themselves.

---

PATH TWO: RECIPIENT DIRECT CLAIM

Now recipient direct claim. This is when someone sends you a covenant, but you claim the BCH directly to your own wallet without involving a merchant.

Why would you do this? Several reasons.

Reason one: You want to hold BCH. Maybe you believe Bitcoin Cash will appreciate. Maybe you're bullish. You don't want local currency. You want the BCH itself. So you claim it directly to your wallet. No merchant needed.

Reason two: You want to convert BCH to MUSD. Remember Episode 21? MUSD is a stablecoin on Bitcoin Cash. You received a remittance worth five hundred euros. That's too much to cash out at a merchant in one visit. Instead, you claim the BCH directly to your wallet. Then you swap it for MUSD using a MUSD seller on the bulletin board. Now you hold five hundred MUSD—stable value, no volatility. You cash out fifty MUSD per week at a merchant over the next ten weeks.

Reason three: You're a freelancer or merchant who deals in BCH regularly. You don't need to convert to local currency immediately. You keep BCH in your wallet as working capital. Maybe you're also a BCH seller. You accumulate BCH from remittances, then you list it on the bulletin board as a seller for future senders.

The key point: recipient direct claim means the recipient owns the destination wallet. They're the only signature required. No merchant involved. They claim the BCH and it goes straight to their wallet.

---

PATH THREE: MERCHANT CASH-OUT (RECAP)

Path three is what we covered in Episode 22. The recipient goes to a merchant. The covenant requires two signatures: the recipient's (because they own the destination wallet conceptually) and the merchant's (because they're accepting the BCH). Both sign together. The BCH goes to the merchant. The merchant gives the recipient local currency or goods.

This is the most common path in Asgaya's remittance use case. Recipients need local currency for rent, groceries, daily expenses. They don't want to hold volatile BCH. They go to a merchant, co-sign, and walk away with cash or products.

---

HOW THE ARCHITECTURE HANDLES ALL THREE

Here's the beautiful part: the covenant doesn't care which path you take. The covenant just specifies:

One: The destination wallet address (the recipient's CashAccount).
Two: The amount of BCH locked.
Three: The claim window (two hours, four hours, twenty-four hours—whatever the sender and recipient coordinated).
Four: The collateralization level (one hundred seven percent minimum).

That's it. The covenant doesn't say "this must be claimed at a merchant." It doesn't say "this must be claimed directly." It just says: "Here's BCH locked for this CashAccount. It can be claimed within this window by whoever controls this CashAccount. If a merchant is involved, both must sign. If not, the recipient signs alone."

The flexibility comes from the covenant architecture itself. Covenants on Bitcoin Cash are programmable conditions. The condition for Asgaya covenants is: "BCH locked for recipient's address, claimable within window, with optional co-signature if merchant involved."

The blockchain enforces this. The sender creates the covenant on-chain. The seller locks their BCH to fund it. The recipient (and merchant, if applicable) sign to claim it. The blockchain validates all signatures and releases the BCH. Simple, flexible, powerful.

---

WHICH PATH SHOULD YOU USE?

So when would you use each path?

Use sender self-claim when:
- You're testing your bot or wallet setup.
- You're moving BCH between your own wallets with covenant protection.
- You want to verify the covenant flow before trusting it with real remittances.

Use recipient direct claim when:
- You want to hold BCH long-term.
- You want to convert BCH to MUSD and cash out gradually.
- You're a freelancer or merchant who uses BCH regularly.
- You're accumulating BCH to become a BCH seller.

Use merchant cash-out when:
- You need local currency immediately.
- You don't have a BCH wallet or don't want to manage one.
- You want the simplicity of walking into a store and getting cash or goods.
- You want zero volatility risk (merchant handles conversion immediately).

Most recipients in Asgaya's Phase 0 will use merchant cash-out. It's simple, fast, and requires no technical knowledge. But the other paths exist for advanced users, freelancers, and participants building BCH circular economies.

---

THE USER CHOICE PRINCIPLE

Here's the key principle: Asgaya doesn't force you into one path. You choose based on your needs.

If you're a grandmother receiving remittances from family in Spain, you probably use merchant cash-out. You go to your local store, co-sign with the merchant, get your cash. Done.

If you're a freelancer receiving five hundred euro payments, you probably use recipient direct claim. You claim BCH to your wallet, swap to MUSD, cash out gradually over weeks.

If you're a BCH seller testing your setup, you use sender self-claim. You create test covenants, verify everything works, then accept real bounties.

The protocol supports all three. The covenant structure is the same. The verification is the same. The difference is who signs and where the BCH goes.

This flexibility is by design. Asgaya isn't just a remittance protocol. It's a programmable payment system. Remittances are the first use case. But the covenant architecture supports peer-to-peer payments, BCH accumulation, merchant liquidity, stablecoin conversion, and more.

Three paths. One protocol. User choice.

---

CLOSING

So here's the summary. Every covenant in Asgaya can be claimed in three ways.

Path one: Sender self-claim. Sender is the recipient. Used for testing and wallet transfers.

Path two: Recipient direct claim. Recipient claims BCH to their own wallet. Used for holding BCH, converting to MUSD, or accumulating inventory.

Path three: Merchant cash-out. Recipient and merchant co-sign. Recipient gets local currency. Used for simple remittances and daily expenses.

All three paths use the same covenant architecture. The blockchain enforces the conditions. The signatures determine where the BCH goes. The choice is yours.

Thanks for listening. This is Radio Asgaya.

---

END
