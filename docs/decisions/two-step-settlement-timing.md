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

## Alternatives Considered

### Option 1: Immediate BCH Purchase (Push Model)

**Flow:**
1. Sender pays €101 to escrow (via Bizum)
2. **Escrow immediately buys BCH** (€100 worth at current rate)
3. Escrow waits for recipient to cash out at merchant (2-60 min)
4. Merchant hands VES to recipient
5. Escrow sends BCH to merchant or LP

**Pros:**
- Simple, linear flow
- Sender gets instant BCH price lock
- No coordination delay

**Cons:**
- **Escrow holds BCH for unknown duration** (waiting for recipient to cash out)
- If BCH drops 2% → Escrow loses €2 per €100 transfer
- If BCH rises 2% → Escrow profits €2 (but can't rely on this)
- **Unsustainable for escrow** (gambling on volatility)

**Verdict:** ❌ Exposes escrow to unacceptable risk

---

### Option 2: Delayed BCH Purchase After Recipient Gets VES

**Flow:**
1. Sender pays €101 to escrow (via Bizum)
2. Escrow holds EUR (no BCH purchased yet)
3. Escrow notifies recipient "funds ready for cash-out"
4. Recipient goes to merchant, receives VES
5. **Escrow buys BCH** after merchant confirms cash-out
6. Escrow sends BCH to merchant or LP

**Pros:**
- Escrow holds EUR during uncertainty (no volatility risk)
- BCH purchased only after recipient has VES
- Volatility window reduced to settlement phase only

**Cons:**
- Still has brief volatility exposure during BCH settlement (seconds to minutes)
- Requires merchant to send VES before receiving BCH (trust requirement)

**Verdict:** ✅ Good approach, but can we reduce volatility further?

---

### Option 3: Two-Step Settlement (Pull-Based BCH Purchase)

**Flow:**
1. **Step 1: Fiat transfer (EUR→VES)**
   - Sender pays €101 to escrow (via Bizum)
   - Escrow holds EUR (no BCH purchased yet)
   - Escrow notifies recipient "funds ready for cash-out"
   - Recipient goes to merchant, shows transaction ID
   - **Merchant hands VES to recipient** (BCH not involved yet)
   - Merchant confirms cash-out in app

2. **Step 2: BCH settlement (happens AFTER recipient has VES)**
   - **If merchant selected instant settlement:**
     - LP sends €100.247 fiat to merchant
     - Merchant app parses fiat notification from LP
     - **Escrow buys BCH** (€101 worth at current rate)
     - Escrow sends €100.247 worth of BCH to LP
     - Escrow sends €0.247 worth of BCH to merchant (fee)
     - Escrow keeps €0.247 worth of BCH (fee)

   - **If merchant holds BCH (no instant settlement):**
     - **Escrow buys BCH** (€101 worth at current rate)
     - Escrow sends €100.37 worth of BCH to merchant
     - Escrow keeps €0.37 worth of BCH (fee split 2 ways)

**Pros:**
- **Recipient has VES immediately** (no volatility risk on the actual remittance)
- **Escrow only buys BCH after LP sends fiat** (settlement happens in seconds)
- Volatility window: **5-30 seconds** (from BCH purchase to LP/merchant receipt)
- Clear separation: fiat transfer (minutes) + BCH settlement (seconds)

**Cons:**
- More complex flow (two distinct phases)
- Merchant must send VES before receiving BCH (trust-minimized via reputation)
- Requires merchant liquidity (hands out VES before settlement)

**Verdict:** ✅ Best risk mitigation, enables permissionless design

---

### Option 4: Stablecoin Intermediary (EUR-Token)

**Flow:**
1. Sender pays €100 to escrow
2. Escrow mints EUR-token on BCH (CashTokens)
3. Escrow sends EUR-token to recipient
4. Recipient redeems EUR-token for VES via merchant
5. Merchant redeems EUR-token for EUR via escrow

**Pros:**
- Zero volatility (EUR-token pegged to EUR)
- Instant settlement (no waiting for BCH purchase)
- Simple coordination

**Cons:**
- **Requires EUR-token infrastructure** (doesn't exist on BCH yet)
- **Centralization risk** (who backs the EUR-token?)
- **Regulatory risk** (stablecoins under heavy scrutiny)
- Defeats purpose of using BCH (just rebuilding fiat system on blockchain)

**Verdict:** ❌ Defeats permissionless goals, adds complexity

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
- Must have €100.247 fiat liquidity to send to merchant
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
- [Volatility Protection](core-architecture/volatility-protection.md) — Architecture overview

---

## Lessons Learned

### 1. Volatility Risk Cannot Be Ignored
- Every crypto remittance project that ignored volatility failed
- Users will not accept "surprise" losses from price swings
- Must design around volatility as foundational constraint

### 2. Separate Fiat from Crypto Timelines
- Fiat transfer can take minutes (notification delays, recipient going to merchant)
- BCH settlement happens in seconds (exchange purchase + network confirmation)
- Don't couple them into single atomic transaction

### 3. Trust the Escrow, Verify the Code
- Merchants and LPs trust escrow to coordinate fairly
- Open-source code + reputation system enables this trust
- Community oversight more reliable than attempting "trustless" smart contracts

### 4. Volatility Window Can Be Seconds, Not Minutes
- By purchasing BCH only after all confirmations, volatility exposure drops from 10+ minutes to 5-30 seconds
- **This is the key innovation** that makes BCH remittances viable

---

## References

- **Architecture:** `/docs/core-architecture/volatility-protection.md`
- **Implementation:** `/docs/android-app/flows/sender-flows.md`

---

*Decision made: April 2026*
*Validated: €3 test transfers successful*
*Status: Active, proven design*
