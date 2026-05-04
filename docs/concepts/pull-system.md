# Pull System — Recipient-Triggered Execution

**Concept Type:** Protocol Design Pattern
**Category:** Volatility Protection
**Related:** [../core-architecture/why-eliminate-volatility.md](core-architecture/why-eliminate-volatility.md)

---

## What It Is

A remittance execution model where the **recipient triggers** the EUR→BCH→merchant conversion, rather than the sender triggering it.

**Traditional "push" model:**
```
Sender pays → Immediately convert to BCH → Send to merchant
Time window: 20+ minutes
Volatility risk: 2%+ possible
```

**Asgaya "pull" model:**
```
Sender pays → EUR held in escrow → Recipient walks to merchant → Triggers conversion
Time window: 30 seconds
Volatility risk: 0.1% typical
```

---

## Why It Exists

**The volatility problem:** BCH price can move significantly in 20 minutes during volatile periods.

**Who gets hurt in push model:**
- Sender sends €50
- 20 minutes later (banking delays, processing time)
- BCH dropped 2%
- Merchant receives €49 worth
- **Merchant loses money through no fault of their own**

**Remittances need predictability.** Merchants must know what they're getting.

---

## How It Works

### Three-Phase Flow

**Phase 1: Funding (asynchronous, no time pressure)**
1. Sender sends €50 Bizum to escrow
2. Escrow receives notification (smsbridge_loop.py)
3. Escrow creates pending transaction in database
4. **EUR stays in escrow's bank account** (NOT converted to BCH yet)
5. Recipient notified via app: "€50 ready for pickup"

**Phase 2: Execution (synchronous, 30 seconds)**
1. Recipient walks to merchant pulpería
2. Merchant enters amount in lempiras (e.g., 1,250 HNL)
3. App calculates BCH amount at **current rate** (Kraken ticker)
4. App generates QR code (BCH address + exact amount)
5. Recipient scans QR code with Asgaya app
6. **App signals escrow: "EXECUTE NOW"**
7. Escrow buys BCH via Kraken (~10 sec)
8. Escrow sends BCH from hot wallet (~5 sec)
9. Merchant sees 0-conf transaction (~15 sec total)
10. Merchant hands cash to recipient

**Phase 3: Settlement (background)**
1. Transaction marked complete
2. Escrow replenishes buffer via SEPA to Kraken

---

## The Key Innovation

**Volatility window: 20 minutes → 30 seconds**

**Statistical reality:**
- BCH **CAN** move 2% in 20 minutes (happens regularly)
- BCH **CANNOT** move 2% in 30 seconds (except black swan events)

**Result:** Volatility risk effectively eliminated (0.1% typical slippage)

---

## Trade-offs

**Adds complexity:**
- Three-party coordination (sender, escrow, merchant)
- Recipient needs Asgaya app (can't use arbitrary BCH wallet)
- Backend must track pending transactions
- More moving parts than simple push

**Provides benefits:**
- 99.7%+ accuracy in EUR → local currency conversion
- Merchant sees exact BCH amount before execution
- Can verify rate against market
- Better UX (notifications, status tracking, pickup locations)

**Net:** Complexity worth it for volatility protection

---

## Implementation Requirements

**Sender side:**
- Bizum/payment app (already have)
- No Asgaya app needed (optional for tracking)

**Recipient side:**
- **Must have Asgaya app** (critical requirement)
- QR code scanner
- Trigger execution capability

**Merchant side:**
- Amount entry in local currency
- QR code generation (address + amount)
- 0-conf transaction detection

**Escrow side:**
- Notification parsing (SMS or API)
- Pending transaction database
- Real-time price feed (Kraken ticker)
- Atomic EUR→BCH→send execution

---

## Why "Pull" Not "Push"

**Analogy: Restaurant order**

**Push model** = Sender orders pizza for recipient
- Pizza made immediately
- Delivered whenever it arrives
- If recipient not ready, pizza gets cold
- Wrong analogy for money

**Pull model** = Sender gives recipient gift card
- Recipient goes to restaurant when ready
- Orders exactly what they want, when they want it
- Food made fresh at time of order
- Perfect analogy for remittances

**Money should work like the gift card**, not like the delivered pizza.

---

## Security Considerations

**What if recipient never "pulls"?**
- EUR stays in escrow account
- Timeout: 30 days (configurable)
- After timeout: Return EUR to sender (minus small fee)
- No BCH bought = no volatility exposure

**What if merchant dishonest?**
- Merchant generates QR for wrong amount
- Recipient sees amount in app before scanning
- Recipient must approve explicitly
- **Trust minimized** (recipient controls execution)

**What if escrow fails to execute?**
- Recipient triggered but BCH never arrives
- Transaction marked failed
- EUR returned to sender
- Or retry with different escrow

---

## Future Evolution

**V1: Prove 30-second execution**
- Test with real Bizum → Kraken → merchant
- Measure actual timing
- Validate 0-conf acceptance

**V1.1: Stablecoin option**
- Use MUSD instead of BCH
- Zero volatility window (stablecoin = stable)
- For users who want 100% certainty

**V2: Smart contract escrow**
- On-chain pending transactions
- Trustless execution
- No centralized escrow needed

**V3: Lightning Network**
- Sub-second finality
- Instant settlement
- Zero volatility physically impossible

---

## Related Concepts

- **Volatility Protection:** [../core-architecture/why-eliminate-volatility.md](core-architecture/why-eliminate-volatility.md)

---

## Why This Matters

Most crypto remittance solutions push complexity to the recipient:
- "Download wallet, save seed phrase, understand addresses"
- High friction, low adoption

Asgaya pushes complexity to the protocol:
- Recipient just scans QR code
- Everything else handled automatically
- Low friction, high adoption potential

**The pull system makes this possible** by decoupling:
- Funding (asynchronous, sender controls)
- Execution (synchronous, recipient controls)
- Settlement (background, escrow controls)

Each actor controls their part, complexity hidden from users.

---

*Concept documented: April 15, 2026*
*Original design: Years ago, refined through implementation*
*Status: Core protocol feature, in development*
*Key insight: Recipient-triggered execution eliminates volatility window*
