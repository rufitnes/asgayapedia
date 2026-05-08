# Decision: Two-Step Settlement - Pull-Based BCH Purchase

**Decision Date:** April 2026
**Status:** Implemented
**Related Requirement:** [Cheaper Than Legacy](core-architecture/why-cheaper-than-legacy.md) (Volatility Protection)

---

## The Goal (Architectural Ideal)

**Eliminate cryptocurrency volatility risk** from the remittance process.

**Problem to solve:**
- BCH price can fluctuate 5-10% in minutes
- If sender commits €100 but BCH drops 5% before settlement, recipient gets €95 worth of value
- Volatility risk is the #1 barrier to crypto remittances

**Ideal outcome:**
- Sender knows exact EUR amount at time of transfer
- Recipient receives exact VES amount expected
- No surprises from BCH price movements

---

## The Constraint (Economic Reality)

**Challenge:** When to buy BCH in the process?

**Critical insight:** Buying BCH too early exposes someone to volatility risk.

**Participants who could bear risk:**
1. **Sender:** Pays EUR upfront, expects recipient to get exact VES amount
2. **Escrow:** Receives EUR, must decide when to buy BCH
3. **Merchant:** Hands cash to recipient, receives BCH later
4. **LP:** Sends fiat to merchant, receives BCH as payment
5. **Recipient:** Receives VES, holds BCH until spending

**Question:** Who should bear the volatility risk, and when should BCH be purchased?

---

## The Decision

**Use two-step settlement with pull-based BCH purchase.**

**Implementation:**

### Step 1: Fiat Transfer (EUR→VES)

1. Sender pays €101 via Bizum to escrow
2. **Escrow holds EUR** (does not buy BCH)
3. Escrow receives notification (2-10 min typical)
4. Escrow notifies recipient "€100 transfer ready - go to merchant to cash out"
5. Recipient goes to participating merchant
6. Recipient shows transaction ID to merchant
7. Merchant enters code in Asgaya app
8. **Merchant hands VES cash to recipient**
9. Both confirm cash-out in app

**Volatility exposure: ZERO** (all fiat, no BCH involved yet)

---

### Step 2: BCH Settlement (After Recipient Has VES)

**If merchant selected instant settlement (3 participants):**

10. LP sends €100.247 fiat to merchant (bank transfer/mobile payment)
11. **Merchant app parses fiat settlement notification from LP**
12. **Escrow buys €101 worth of BCH** (e.g., 0.1007374 BCH lands in hot wallet)
13. Calculate fee: 0.1007374 - 0.1000000 = 0.0007374 BCH
14. Split 3 ways: 0.0007374 / 3 = 0.0002458 BCH each
15. Escrow sends to LP: 0.1002458 BCH (€100 principal + 0.0002458 BCH fee)
16. Escrow sends to merchant: 0.0002458 BCH (fee only, merchant has fiat from LP)
17. Escrow keeps: 0.0002458 BCH

**Volatility exposure:** 5-30 seconds (from BCH purchase to LP receipt)

---

**If merchant holds BCH (2 participants):**

10. **Escrow buys €101 worth of BCH** (e.g., 0.1007374 BCH)
11. Calculate fee: 0.0007374 BCH
12. Split 2 ways: 0.0007374 / 2 = 0.0003687 BCH each
13. Escrow sends to merchant: 0.1003687 BCH
14. Escrow keeps: 0.0003687 BCH

**Volatility exposure:** 5-30 seconds (from BCH purchase to merchant receipt)

---

## How This Decision Meets Engineering Requirements

The pull-based two-step settlement is not an arbitrary design choice—it's the **only architecture** that satisfies all core engineering requirements for permissionless operation in difficult infrastructure conditions.

### 1. Minimal Hardware Requirement (Permissionless Core)

**Requirement:** Recipients need minimal hardware—no BCH wallet, no crypto knowledge, no exchange access.

**Phase 0: Smartphone with app required**
- Recipient needs Android smartphone with Asgaya app installed
- Internet connection required at time of cash-out (merchant location)
- **No BCH wallet needed** (merchant handles crypto)
- **No technical knowledge required** (just enter a code)

**Future (Phase 1+): RFID card alternative**
- Recipient can use RFID card instead of smartphone (see [RFID Card Recipients](concepts/rfid-card-recipients.md))
- Card costs $0.50-2 (vs $50-200 smartphone)
- Merchant helps provision card (merchant-assisted onboarding)
- Tap card on merchant device → transaction completes
- **True minimal hardware** (no phone ownership needed)

**Why this matters:** 
- Phase 0 smartphone requirement is still MUCH lower barrier than traditional crypto (no wallet, no private keys, no exchange)
- Future RFID option makes Asgaya accessible to recipients without smartphones (elderly, low-income)
- Merchant handles all crypto complexity

**Alternative (push model) would fail:** If BCH was purchased when sender initiated, recipient would need BCH wallet to receive funds. This excludes most potential users in target markets.

---

### 2. Fail-Safe Design (Nothing Happens Until Ready)

**Requirement:** Transaction must not execute until both parties are physically present and able to confirm.

**How pull system delivers:**
- EUR sits in escrow (no BCH purchased yet)
- Nothing happens until recipient is physically at merchant
- Merchant hands cash only after seeing escrow confirmation
- Both sides confirm in app while facing each other
- BCH settlement triggered only after recipient has VES in hand

**Why this matters:** Self-auditing UX. Recipient can't claim "merchant didn't pay" if they confirmed in front of merchant. Merchant can't claim "recipient didn't show up" if escrow has no confirmation.

**Alternative (push model) would fail:** If BCH was purchased upfront, escrow holds BCH waiting for uncertain recipient action. Refunds become complex. Timing becomes unpredictable.

---

### 3. Safe in Unreliable Environments (Delay is a Feature, Not a Bug)

**Requirement:** System must work in Venezuela where power outages, poor connectivity, and infrastructure failures are routine.

**How pull system delivers:**
- **Transaction waits** if recipient can't get to merchant (not a problem, it just waits)
- **Transaction waits** if merchant loses power/connectivity (escrow holds EUR safely)
- **Transaction waits** if it takes 15 minutes instead of 2 minutes (actually SAFER—more time to verify)
- EUR in escrow has zero volatility risk while waiting
- After 24 hours, automatic refund to sender (no manual intervention needed)

**Why this matters:** 
- Venezuela routinely has 6-12 hour power outages
- Rural areas have spotty mobile connectivity
- Merchant might be busy with other customers
- **Pull system treats these as normal conditions, not failures**

**Real scenario:**
- Sender pays €100 via Bizum
- Recipient gets notification "funds ready"
- Power outage hits recipient's neighborhood for 8 hours
- Recipient waits until power restored, then goes to merchant next day
- Transaction completes successfully 22 hours later
- **Everyone gets paid correctly because EUR was safely in escrow the entire time**

**Alternative (push model) would fail:** If escrow bought BCH immediately and held it for 22 hours, volatility risk would be enormous. Escrow could lose significant money. The delay becomes a problem instead of being harmless.

**External reviewer concern (Gemini):** "If transaction takes 15 minutes due to Venezuela connectivity, merchant holds volatility risk."

**Reality:** Gemini had it backwards. The longer the delay, the SAFER pull system becomes:
- Delay in Step 1 (EUR→VES) → Zero risk, EUR just waits in escrow
- Delay in Step 2 (BCH settlement) → Merchant already has VES from LP, just waiting for BCH fee (seconds)

**15-minute transaction is safer than 2-minute transaction** because more time = more confirmation opportunities = fewer disputes.

---

### 4. No Orphaned Funds (Automatic Resolution)

**Requirement:** If recipient never claims, funds must return to sender automatically.

**How pull system delivers:**
- EUR sits in escrow (no BCH purchased, no volatility risk)
- After 24 hours, automatic refund to sender
- Sender gets €100.90 back (€0.10 processing fee for 24h liquidity lock)
- No manual intervention, no customer support needed
- No "stuck" funds requiring escrow investigation

**Why this matters:** In Phase 0, if recipient's phone breaks, they move, they forget—sender's money is not lost. Escrow doesn't absorb risk. System self-heals.

**Alternative (push model) would fail:** If BCH was purchased upfront, escrow would need to:
1. Hold BCH for 24 hours (volatility risk)
2. Sell BCH to refund EUR (exchange fees + slippage)
3. Sender might get less than €100 back if BCH dropped
4. Complex refund logic, potential for losses

**Pull system:** Refund is trivial—escrow just returns the EUR it's been holding. Clean, simple, fair.

---

### 5. User Sovereignty (Recipient Controls Execution)

**Requirement:** Recipient decides when and where to claim funds, not the sender.

**How pull system delivers:**
- Sender initiates transfer, but execution waits for recipient
- Recipient chooses which merchant to visit (convenience, trust, proximity)
- Recipient chooses when to claim (immediate, next day, next week)
- Recipient's physical presence triggers settlement (agency and control)

**Why this matters:** 
- Empowerment framing (not just technical)
- Recipient isn't passive—they control the process
- Builds trust (recipient verifies merchant hands cash before confirming)
- Cultural fit (Latin America values face-to-face transactions)

**Alternative (push model) would fail:** Sender's action triggers BCH purchase, recipient is just notified. Recipient becomes passive recipient of funds already in motion. Less control, less agency, less trust.

---

### 6. Volatility Protection (Bonus Benefit, Not Primary Driver)

**Common misconception:** Pull system exists primarily to solve volatility.

**Reality:** Pull system exists to meet permissionless engineering requirements. Volatility protection is a **valuable side effect**, not the core reason.

**Benefits:**
- Volatility window: 5-30 seconds (Step 2 settlement only)
- Escrow never holds BCH during uncertainty
- Recipient gets exact VES amount expected
- Sender pays exact EUR amount committed

**But:** Even if BCH had zero volatility, we would still use pull system because it's the only design that works with:
- Minimal hardware (no recipient wallet needed)
- Unreliable infrastructure (transaction waits safely)
- Fail-safe operation (automatic refunds)
- User sovereignty (recipient-controlled execution)

**Volatility protection validates the choice, but doesn't drive it.**

---

## Summary: Why Pull System is THE Solution

| Engineering Requirement | Push Model | Pull Model |
|------------------------|------------|------------|
| Minimal hardware (no recipient wallet) | ❌ Fails | ✅ Works |
| Fail-safe (nothing until ready) | ❌ Fails | ✅ Works |
| Unreliable infrastructure | ❌ Breaks | ✅ Waits safely |
| No orphaned funds | ❌ Complex refunds | ✅ Auto-refund |
| User sovereignty | ❌ Sender-triggered | ✅ Recipient-triggered |
| Volatility protection | ⚠️ Escrow bears risk | ✅ 5-30 second window |

**Conclusion:** Pull system isn't one of several options—it's the **only permissionless design** that works in difficult conditions with minimal infrastructure requirements.

---

## Rationale

### Why This Works

**1. Recipient gets VES immediately (Step 1)**
- No volatility risk on the actual remittance
- Sender achieved their goal (sent money to recipient)
- Recipient can spend VES while BCH settles in background

**2. Escrow only buys BCH after confirmations (Step 2)**
- Holds EUR during uncertainty (zero volatility)
- Only buys BCH after merchant confirms cash-out AND LP confirms fiat sent
- Volatility window reduced to seconds (settlement phase only)

**3. Merchant liquidity enables permissionless**
- Merchant hands VES before receiving BCH (trust-minimized via reputation)
- Merchant gets fiat from LP (if instant settlement) or BCH (if holding) within seconds
- If merchant doesn't get paid, community reputation system flags the issue

**4. BCH adoption happens organically**
- Merchants receive BCH fees
- LPs accumulate BCH from buying at market rate
- Recipients see growing merchant network, more likely to hold/spend BCH

---

## Trade-offs Accepted

### Lost: Instant BCH Settlement
- BCH settlement delayed until after recipient gets VES
- Two-phase process (not single atomic transaction)
- More coordination steps
- Multiple trust relationships (everyone trusts escrow to coordinate properly)

### Gained: Near-Zero Volatility Risk on Remittance
- Recipient gets VES regardless of BCH price
- Escrow protected from volatility during uncertainty (holds EUR)
- Sender knows exact EUR cost upfront
- **Volatility window: 5-30 seconds** (settlement phase only, not minutes)

### Trust Requirements
- **Merchant → Escrow:** Merchant trusts escrow will send BCH after merchant hands VES to recipient
- **LP → Escrow:** LP trusts escrow will send BCH after LP sends fiat to merchant
- **Mitigation:** Escrow reputation, open-source code, community oversight

---

## Implementation Details

### Timing

**Step 1 (Fiat - EUR to VES):**
- Duration: 2-10 minutes (notification delay + recipient going to merchant)
- Escrow holds: EUR (zero volatility)

**Step 2 (BCH Settlement):**
- Duration: 5-30 seconds (BCH purchase + confirmation)
- Escrow holds: BCH (only during settlement)

**Total time:** 3-15 minutes (comparable to Western Union)

---

### Capital Requirements

**Escrow:**
- Must hold €100 in EUR during Step 1 (no volatility risk)
- Must hold €100 in BCH during Step 2 (only 5-30 seconds)

**Merchant:**
- Must have VES liquidity to hand out before receiving BCH/fiat
- Recoups via BCH or fiat from LP within seconds

**LP (if instant settlement selected):**
- Must have €100 fiat liquidity to send to merchant
- Receives €100.247 worth of BCH within seconds

---

### Failure Modes

**Scenario 1: Escrow doesn't receive Bizum from sender**
- Timeout after 10 minutes
- Ask sender: "Did you send the payment?"
- If yes → Manual verification
- If no → Cancel or extend timeout
- No BCH purchased = no loss

**Scenario 2: Recipient doesn't cash out at merchant**
- Escrow holds EUR indefinitely (no volatility risk)
- After 24 hours, sender can request refund
- Escrow refunds €101 via Bizum
- No BCH purchased = no loss

**Scenario 3: LP doesn't send fiat to merchant (instant settlement)**
- Timeout after 10 minutes (starting from merchant cash-out confirmation)
- **Broadcast bounty again** to other LPs in corridor
- Next LP sends fiat → settlement proceeds
- If no LP accepts → Merchant receives BCH directly (fallback to non-instant settlement)

**Scenario 4: Escrow fails to buy BCH**
- Rare (exchange API failure)
- Escrow refunds €101 to sender
- Merchant already handed VES to recipient (merchant accepts loss or coordinates reversal with recipient)
- **Why rare:** Escrow can retry BCH purchase multiple times before giving up

---

## Validation

**How we verify this decision:**
- ✅ Zero volatility risk on fiat remittance (recipient always gets VES)
- ✅ Escrow never holds BCH during uncertainty (only during settlement)
- ✅ Volatility window reduced to 5-30 seconds (not minutes)
- ⏳ Pending: Real transfers to validate merchant trust model
- ⏳ Pending: Real transfers to validate LP timeout/bounty re-broadcast

**Metrics to track:**
- Escrow volatility losses (target: €0 - should never happen)
- Settlement success rate (target: >99%)
- LP response time (target: <5 minutes from cash-out to fiat sent)
- Merchant trust incidents (target: <1% bad actors)

---

## Future Considerations

### If CashTokens EUR-Stablecoin Emerges
- Could simplify to single-step settlement
- **Trade-off:** Adds centralization risk (stablecoin issuer)
- **Decision:** Stick with two-step unless stablecoin is proven decentralized

### If BCH Volatility Decreases Significantly
- If BCH stable for months (< 1% daily volatility) → could merge steps
- **For now:** Assume volatility is reality, design around it

### If Open Banking APIs Become Available
- Could automate sender payment (no manual Bizum)
- Would reduce Step 1 time from minutes to seconds
- Would enable instant EUR→VES settlement

---

## Related Decisions

- [Payment Timeout Window](decisions/payment-timeout-window.md) — Why 10-minute timeout works (escrow holds EUR, not BCH)
- [Fee Splitting Model](decisions/fee-splitting-model.md) — How fees are calculated after BCH lands in wallet

---

## Related Concepts

- [Pull System](concepts/pull-system.md) — Why pull-based BCH purchase is critical
- [Volatility Protection](core-architecture/why-eliminate-volatility.md) — Architecture overview

---

## References

- **Architecture:** `/docs/core-architecture/why-eliminate-volatility.md`
- **Implementation:** `/docs/android-app/flows/sender-flows.md`

---

*Decision made: April 2026*
*Validated: €3 test transfers successful*
*Status: Active, proven design*
