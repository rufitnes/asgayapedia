# Pull System — Recipient-Triggered Execution

**Concept Type:** Protocol Design Pattern  
**Category:** Volatility Protection  
**Related:** [Why Eliminate Volatility](../core-architecture/why-eliminate-volatility.md), [With volatility buffer Bounty Contracts](./bounty-contracts-with-volatility-buffer.md)

---

## What It Is

A remittance execution model where the **recipient triggers** BCH settlement when they're ready to cash out, rather than the sender triggering immediate conversion.

**Traditional "push" model:**
```
Sender pays → Immediately convert to BCH → Send to recipient
├─ Time window: 20+ minutes (banking + blockchain delays)
├─ Volatility risk: 2%+ possible during transit
└─ Timezone coordination: Sender decides when, recipient must be ready
```

**Asgaya "pull" model:**
```
Sender creates covenant → BCH seller funds it → Recipient claims whenever ready
├─ Time window: 30 seconds (covenant execution time)
├─ Volatility risk: 0.1% typical (volatility buffer absorbs swings)
└─ No timezone coordination: Recipient controls timing completely
```

---

## Why It Exists

**The volatility problem:** BCH price can move significantly in 20 minutes during volatile periods.

**Who gets hurt in push model:**
- Sender sends €100
- 20 minutes later (banking delays, processing time)
- BCH dropped 2%
- Recipient receives €98 worth
- **Recipient loses money through no fault of their own**

**The coordination problem:** Sender and recipient in different timezones.

**Who gets hurt in synchronized model:**
- Sender (Spain, 2pm) wants to send money
- Recipient (Venezuela, 8am) is asleep or busy
- Either sender waits, or recipient must wake up
- **Terrible UX, kills adoption**

**Remittances need:**
1. **Predictability** - Recipient knows exactly what they'll get
2. **Asynchronous** - No timezone coordination required
3. **Recipient control** - Cash out when ready, not when sender decides

**The pull system solves all three.**

---

## 🎯 CRITICAL RISK ALLOCATION IN PULL SYSTEM

**Common misconception:** "If the covenant holds BCH and the merchant receives from it, the merchant bears volatility risk."

**Reality:** Merchants only participate when covenants are VALID and fully collateralized.

* ❌ **Merchant NEVER receives undercollateralized BCH**
* ❌ **Merchant NEVER bears BCH volatility risk**
* ✅ **Merchant's software validates covenant BEFORE handing cash:**
  * Is covenant sufficiently collateralized? (≥100% of EUR promise)
  * Are both conditions signable? (seller paid, recipient present)
  * Is covenant still within 24h window?
* 🔄 **If covenant becomes invalid (BCH drops >7%):**
  * Covenant expires early
  * ALL remaining BCH refunds to **SENDER**
  * Merchant's software detects invalid covenant
  * Merchant does NOT hand cash to recipient
  * Transaction cancelled - no one loses money except sender (bears tail risk)

**The "pull" system doesn't just mean recipient controls timing** - it also means **merchant validates claim validity at the exact moment of execution**. This is why merchants are completely protected from volatility.

**See:** [With volatility buffer Bounty Contracts](./bounty-contracts-with-volatility-buffer.md) for technical details on covenant validation.

---

## How It Works

### Three-Phase Flow

**Phase 1: Covenant Creation (Sender initiates, asynchronous)**

1. **Iris (sender in Spain) creates covenant contract:**
   - Recipient: Elena (Venezuela)
   - Amount: **€100 worth of BCH at the moment the covenant matures**
   - Payment method: Bizum
   - Reward: 1% / participants
   - Timeout: 24 hours
   - Published to Asgaya bulletin board as open bounty

2. **Elena notified:**
   ```
   "Iris sent you 500,000 VES (~€100 at current rate, ±1% reward)
   Claim it at any associated merchant within 24 hours"
   ```

**Key insight:** Sender creates terms but doesn't control timing. Elena decides when to claim.

---

**Phase 2: BCH Seller Accepts & Funds (Asynchronous)**

1. **BCH seller bot sees bounty** on bulletin board
2. **Seller accepts challenge:**
   - Locks €107 worth of BCH into covenant (107% with volatility buffer)
   - Provides cushion against price volatility
3. **Sender (Iris) receives notification:**
   ```
   "BCH seller accepted your bounty.
   Send €100 Bizum to: [seller phone/account]
   Concept: ASGAYA #4729 ELENA"
   ```
4. **Iris sends €100 Bizum** to BCH seller
5. **Seller bot parses Bizum SMS** (smsbridge_loop.py)
6. **Condition 1 satisfied:** ✅ Payment received + BCH locked in covenant

**Elena's 24-hour claim window starts NOW** (not from covenant creation, but from funding).

**Key insight:** Sender pays BCH seller via regular Bizum. No crypto knowledge needed.

---

**Phase 3: Contract Maturity (Recipient controls timing - 30 seconds)**

1. **Elena walks to merchant neighborhood store** (whenever she's ready - could be immediately, could be tomorrow)

2. **Elena shares code with merchant:**
   - Code displays:
     - Amount: 500,000 VES
     - Merchant fee: 2,500 VES (0.5%)
     - Covenant conditions: [1] Seller paid ✅, [2] Merchant confirms cash given ⏳

3. **Merchant reviews terms and accepts bounty:**
   - Sees they'll receive €99.5 worth of BCH
   - Sees they'll hand out €99 worth of VES
   - Net profit: €0.50 (0.5% fee)

4. **Merchant hands 500,000 VES cash to Elena**

5. **Both co-sign the covenant:**
   - Merchant signature: "Cash delivered to Elena" ✅
   - Elena signature: "Cash received" ✅ (via app)

6. **Both conditions met → Covenant executes automatically:**
   ```
   Contract Maturity:
   ├─ Condition 1 (seller): Payment received ✅
   ├─ Condition 2 (merchant): Cash delivered ✅
   └─ Executing distribution...
   
   Result:
   ├─ Merchant receives: €99.5 worth of BCH (at current rate)
   ├─ BCH seller receives: €7.5 (volatility buffer surplus + 0.5% fee)
   └─ Transaction complete (on-chain, immutable)
   ```

**Total execution time: ~30 seconds** (from Elena walking in to covenant maturity)

**Volatility window: 30 seconds** (BCH can't move significantly in 30 seconds)

---

## The Key Innovation

**Three innovations in one:**

### 1. Volatility Window: 20 Minutes → 30 Seconds

**Statistical reality:**
- BCH **CAN** move 2% in 20 minutes (happens regularly)
- BCH **CANNOT** move 2% in 30 seconds (except black swan events)

**Result:** Volatility risk effectively eliminated (0.1% typical slippage)

### 2. Timezone Coordination: Required → Not Required

**Old model (P2P bulletin board without covenants):**
- Sender and recipient must both be online at same time
- Spain 2pm = Venezuela 8am (terrible UX)
- Sender waits or recipient wakes up early

**New model (covenant-based pull system):**
- Sender creates covenant (takes 2 minutes)
- Recipient claims whenever ready (within 24 hours)
- No coordination needed!

**Result:** Send-and-forget restored. Recipient empowered.

### 3. Regulatory Compliance: Impossible → Compliant

**Old model (central escrow holds EUR):**
- Entity holds client funds = custody service
- Entity provides conditional transfers = payment intermediation
- Triggers MiCA CASP licensing (€50K-200K, 12-18 months)
- **Legally impossible for permissionless protocol**

**New model (covenant contract):**
- Smart contract holds BCH (autonomous code, not entity)
- BCH seller posts their own capital (not holding client funds)
- Covenant executes automatically (no discretionary intermediation)
- **No MiCA/PSD2 licensing required** ✅

**Result:** Legally viable, permissionless architecture.

---

## How Volatility buffer Protects

**The problem:** What if BCH price drops while covenant is waiting?

**Example scenario:**
```
Day 1, 10:00am: Covenant created, €100 = 0.01 BCH
Day 1, 10:05am: BCH seller locks 0.0107 BCH (€107 at time of lock)
Day 1, 2:00pm:  BCH drops 5% during wait
Day 2, 9:00am:  Elena claims at merchant

At claim time: 0.01 BCH now = €95 (not €100!)
```

**Without volatility buffer:**
- Merchant expects €100 worth
- Covenant only has €95 worth
- **Deal fails** ❌

**With 7% volatility buffer:**
- BCH seller locked €107 worth
- Even after 5% drop: €107 × 0.95 = €101.65
- Merchant still gets €99.5 worth ✅
- BCH seller gets €2.15 back (instead of €7.5 surplus)

**Protection thresholds:**
- 3% drop → Fully covered, seller gets smaller surplus
- 5% drop → Fully covered, seller gets tiny surplus
- 7% drop → Break-even, seller gets no surplus (but no loss)
- >7% drop → **Margin call** (seller must time extension or deal refunds)

**Statistical reality:**
- BCH moves ±3% in 24h: **Common** (daily volatility)
- BCH moves ±7% in 24h: **Uncommon** (happens ~5% of days)
- BCH moves >10% in 24h: **Rare** (black swan events)

**Result:** 7% volatility buffer covers 95%+ of normal volatility.

---

## Trade-offs

**Adds complexity (vs. simple push model):**
- Four-party coordination (sender, BCH seller, merchant, recipient)
- Covenant contract deployment (CashScript technical knowledge)
- Volatility buffer capital requirements (BCH sellers need inventory)
- Margin call monitoring (if BCH drops >7%)
- More moving parts than "sender pays, immediately convert, send"

**But eliminates complexity (vs. synchronized P2P model):**
- ✅ No timezone coordination needed (sender and recipient asynchronous)
- ✅ No "both online at same time" requirement
- ✅ No sender waiting for recipient to wake up / be ready
- ✅ No recipient rushing because sender wants to pay NOW

**And provides benefits:**
- ✅ 99.9%+ accuracy in EUR → VES conversion (volatility buffer absorbs volatility)
- ✅ Recipient sees exact VES amount before claiming
- ✅ Send-and-forget UX restored (sender doesn't wait for execution)
- ✅ Recipient-controlled timing (claim when convenient, not when sender decides)
- ✅ MiCA/PSD2 compliant (no custody, no intermediation)
- ✅ Permissionless (anyone can be BCH seller)

**Net:** Complexity worth it for volatility protection + asynchronous UX + regulatory compliance.

---

## Implementation Requirements

**Sender side:**
- Bizum or bank transfer app (already have)
- Optional: Asgaya app for covenant creation (or use web interface)
- Creates covenant once, pays BCH seller once, done

**Recipient side:**
- **Must have Asgaya app** (critical requirement)
- Receives notification of bounty
- Shares code with merchant when ready
- Co-signs covenant to mature contract

**Merchant side:**
- Asgaya merchant app
- Views bounty details (amount, fee, conditions)
- Hands cash and co-signs covenant
- Receives BCH directly to self-custody wallet

**BCH Seller side:**
- Automated bot monitoring bulletin board (smsbridge_loop.py)
- BCH inventory (capital requirements)
- Accepts bounties, locks BCH + volatility buffer
- Monitors Bizum SMS notifications
- Monitors BCH price for margin calls
- Signs covenant when payment received

**Covenant Contract:**
- CashScript covenant deployed on BCH blockchain
- Holds BCH + volatility buffer
- Enforces two conditions: [1] Seller paid, [2] Merchant confirmed
- Executes automatically when both conditions met
- Refunds BCH seller if timeout expires (24h)

---

## Why "Pull" Not "Push"

**Analogy: Restaurant gift card vs. food delivery**

**Push model** = Sender orders food delivery for recipient
- Food made immediately when sender orders
- Delivered whenever it arrives (sender's timing)
- If recipient not home, delivery fails or food gets cold
- Recipient has no control over when food arrives
- Wrong analogy for money

**Pull model** = Sender gives recipient restaurant gift card
- Recipient goes to restaurant when ready (recipient's timing)
- Orders exactly what they want, when they want it
- Food made fresh at time of order
- Recipient controls entire experience
- Perfect analogy for remittances

**Money should work like the gift card**, not like the food delivery.

**The covenant contract is the digital gift card:**
- Sender funds it (covenant creation + BCH seller payment)
- Recipient redeems it (walks to merchant, co-signs maturity)
- Value locked until recipient ready (with volatility buffer protection)
- No one can force recipient to claim before they're ready

---

## Security Considerations

**What if recipient never "pulls"?**
- BCH stays in covenant contract
- Timeout: 24 hours from funding (configurable)
- After timeout: Covenant refunds BCH to seller
- Sender's €100 Bizum refunded by BCH seller (minus small processing fee)
- No BCH moved to merchant = no volatility exposure

**What if merchant is dishonest?**
- Merchant shows fake amount in app
- Recipient sees real covenant terms in their app before co-signing
- Recipient must explicitly approve amount
- **Recipient controls covenant maturity** (won't sign if terms wrong)

**What if BCH seller fails to lock BCH?**
- Covenant never funded
- Sender notified: "No BCH seller accepted within timeout"
- Sender's Bizum payment never triggered
- Or different BCH seller accepts later

**What if BCH drops >7% during wait?**
- **Margin call** triggered automatically
- BCH seller notified: "Add 0.0005 BCH within 1 hour or bounty refunds"
- BCH seller can:
  - Time extension collateral (keep deal alive)
  - Ignore (covenant refunds, sender's Bizum refunded)
  - Hope price recovers within grace period
- Prevents merchant from receiving less than promised

**What if merchant and recipient collude?**
- Could claim cash delivered when it wasn't
- But covenant requires BOTH signatures (merchant + recipient)
- Recipient won't sign if they didn't get cash (self-interest)
- For Phase 0: 1-2 trusted merchants only (known contacts)
- For Phase 1+: Reputation system, merchant bonds, community verification

---

## Current Status

**We're building this NOW using BCH CashScript covenants.**

The pull system concept, originally designed years ago with a central escrow, has evolved into a fully decentralized implementation using blockchain smart contracts. This removes the regulatory compliance issues while preserving—and even enhancing—the core innovation: recipient-triggered execution.

**Timeline:**
- **May 2026 (Phase 0):** Chipnet testing of covenant contracts
  - Validate volatility buffer mechanics
  - Test margin call triggers
  - Measure real volatility protection
  - Simulate timeout/refund scenarios
  
- **June 2026 (Phase 1):** Mainnet launch with covenant-based pull system
  - 1-2 trusted merchants (known contacts)
  - €50-100 transaction sizes
  - BCH miners as initial sellers (have inventory)
  - Real-world validation: Spain → Venezuela corridor

**The future is already here.** Smart contracts enable the pull system without central custody.

---

## Related Concepts

- [With volatility buffer Bounty Contracts](./bounty-contracts-with-volatility-buffer.md) — Technical implementation
- [Why Eliminate Volatility](../core-architecture/why-eliminate-volatility.md) — The problem we're solving
- [Core Regulatory Constraints](./core-regulatory-constraints.md) — Why covenant-based design is required
- [Two-Step Settlement Timing](../decisions/two-step-settlement-timing.md) — Detailed flow mechanics

---

## Why This Matters

Most crypto remittance solutions push complexity to the recipient:
- "Download wallet, save seed phrase, understand blockchain, wait for confirmations"
- High friction, low adoption
- Recipient bears all the technical burden

Most traditional remittance solutions push timing control to the sender:
- "Money arrives when we send it, be ready"
- High friction, bad UX
- Recipient has no control over when cash is available

**Asgaya pushes complexity to the protocol:**
- Covenant contract handles escrow, timing, and volatility
- BCH seller handles crypto inventory and margin calls
- Recipient just shares code and receives cash
- Sender just creates covenant and pays Bizum

**The pull system makes this possible** by decoupling:
- **Funding** (asynchronous, sender controls when they initiate)
- **Execution** (synchronous, recipient controls when they claim)
- **Settlement** (automatic, covenant controls distribution)

Each actor controls their part. Complexity hidden from end users. The covenant is the invisible escrow that requires no trust.

---

**Concept refined:** May 10, 2026  
**Original design:** Years ago (central escrow version)  
**Current implementation:** Covenant-based decentralized version  
**Status:** In Chipnet testing, targeting June 2026 mainnet launch  
**Key insight:** Smart contracts enable recipient-triggered execution without central custody
