# Decision: Two-Step Settlement - Covenant-Based Pull System

**Decision Date:** April 2026 (Updated May 2026 for covenant architecture)  
**Status:** Implemented (Chipnet testing)  
**Related Requirement:** [Cheaper Than Legacy](../core-architecture/why-cheaper-than-legacy.md) (Volatility Protection)

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
- No central entity holding funds (MiCA/PSD2 compliance)

---

## The Constraint (Economic Reality)

**Challenge:** When should BCH move in the process, and who provides the liquidity?

**Critical insight:** BCH moving too early exposes someone to volatility risk. BCH held by a central entity triggers custody regulations.

**Participants who could bear risk:**
1. **Sender:** Pays EUR upfront, expects recipient to get exact VES amount
2. **BCH Seller:** Posts overcollateralized BCH to covenant, receives EUR from sender
3. **Merchant:** Hands cash to recipient, receives BCH from covenant
4. **Recipient:** Receives VES cash, doesn't hold BCH
5. **Smart Contract:** Holds BCH immutably until conditions met (no entity discretion)

**Questions:**
- Who should provide BCH liquidity?
- When should BCH be locked in covenant?
- Who bears volatility risk during the wait period?
- How do we avoid MiCA CASP licensing (no custody/intermediation)?

---

## The Decision

**Use two-step settlement with covenant-based pull execution.**

**Implementation:**

### Step 1: Covenant Creation & Funding (Asynchronous)

1. **Sender (Iris) creates covenant contract:**
   - Recipient: Elena (Venezuela)
   - Amount: €100 worth of BCH **at the moment the covenant matures**
   - Overcollateralization: 7% (to absorb volatility)
   - Timeout: 24 hours
   - Conditions: [1] BCH seller received payment, [2] Merchant handed cash to recipient

2. **Covenant published to Asgaya bulletin board** as open bounty

3. **BCH seller accepts bounty:**
   - Posts €107 worth of BCH to covenant (overcollateralized)
   - Covenant holds BCH immutably (autonomous code, not entity)

4. **Sender (Iris) pays €100 via Bizum** to BCH seller

5. **BCH seller bot parses Bizum notification**
   - Confirms €100 received
   - **Signs covenant: "Condition 1 satisfied - Payment received"**

6. **Recipient (Elena) notified:**
   ```
   "Iris sent you 500,000 VES (~€100)
   Claim it at any merchant within 24 hours"
   ```

**Volatility exposure during Step 1:**
- BCH seller bears risk (posted overcollateralized BCH)
- If BCH drops <7%: Overcollateralization absorbs it
- If BCH drops >7%: Margin call (seller must add BCH or bounty refunds)
- Recipient has ZERO exposure (not holding any assets yet)

---

### Step 2: Recipient-Triggered Execution (30 seconds)

**When recipient is ready (controls timing):**

1. **Elena walks to merchant pulpería** (whenever convenient - could be immediately, could be tomorrow)

2. **Elena shows code to merchant:**
   - Merchant sees: "500,000 VES to hand out, earn 2,500 VES fee (0.5%)"
   - Merchant sees covenant conditions: [1] Seller paid ✅, [2] Merchant confirms cash given ⏳

3. **Merchant hands 500,000 VES cash to Elena**

4. **Both co-sign covenant:**
   - Merchant signature: "Cash delivered to Elena" ✅
   - Elena signature: "Cash received" ✅ (via app)

5. **Both conditions met → Covenant executes automatically:**
   ```
   Contract Maturity:
   ├─ Condition 1 (BCH seller): Payment received ✅
   ├─ Condition 2 (Merchant): Cash delivered ✅
   └─ Executing distribution...
   
   Result:
   ├─ Merchant receives: €99.5 worth of BCH (at current rate)
   ├─ BCH seller receives: €7.5 (overcollateralization surplus + 0.5% fee)
   └─ Transaction complete (on-chain, immutable)
   ```

**Volatility exposure during Step 2:**
- **30 seconds** (from Elena walking in to covenant maturity)
- Overcollateralization absorbs any price movement in this window
- Merchant receives exactly €99.5 worth of BCH regardless of price changes

---

## How This Decision Meets Engineering Requirements

The covenant-based two-step settlement is not an arbitrary design choice—it's the **only architecture** that satisfies all core engineering requirements for permissionless operation in difficult infrastructure conditions **while remaining MiCA/PSD2 compliant**.

### 1. Minimal Hardware Requirement (Permissionless Core)

**Requirement:** Recipients need minimal hardware—no BCH wallet, no crypto knowledge, no exchange access.

**Phase 0: Smartphone with app required**
- Recipient needs Android smartphone with Asgaya app installed
- Internet connection required at time of cash-out (merchant location)
- **No BCH wallet needed** (merchant handles crypto)
- **No technical knowledge required** (just share code, receive cash, confirm)

**Future (Phase 1+): RFID card alternative**
- Recipient can use RFID card instead of smartphone (see [RFID Card Recipients](../concepts/rfid-card-recipients.md))
- Card costs $0.50-2 (vs $50-200 smartphone)
- Merchant helps provision card (merchant-assisted onboarding)
- Tap card on merchant device → covenant signature → transaction completes
- **True minimal hardware** (no phone ownership needed)

**Why this matters:**
- Phase 0 smartphone requirement is still MUCH lower barrier than traditional crypto (no wallet, no private keys, no exchange)
- Future RFID option makes Asgaya accessible to recipients without smartphones (elderly, low-income)
- Merchant handles all crypto complexity
- Covenant holds BCH immutably (no entity custody)

**Alternative (push model) would fail:** If BCH was purchased when sender initiated, recipient would need BCH wallet to receive funds. This excludes most potential users in target markets.

---

### 2. Fail-Safe Design (Nothing Happens Until Ready)

**Requirement:** Transaction must not execute until both parties are physically present and able to confirm.

**How covenant-based pull system delivers:**
- BCH locked in covenant contract (immutable, no entity can touch it)
- Nothing happens until recipient is physically at merchant
- Merchant hands cash only after seeing covenant status
- Both sides co-sign covenant while facing each other
- **Covenant executes automatically** when both signatures present (no human discretion)

**Why this matters:** Self-auditing UX. Recipient can't claim "merchant didn't pay" if they co-signed covenant in front of merchant. Merchant can't claim "recipient didn't show up" if covenant has no recipient signature.

**Alternative (push model) would fail:** If BCH was sent upfront, someone must hold it waiting for uncertain recipient action. Entity custody = MiCA CASP licensing. Covenant custody = autonomous code (compliant).

---

### 3. Safe in Unreliable Environments (Delay is a Feature, Not a Bug)

**Requirement:** System must work in Venezuela where power outages, poor connectivity, and infrastructure failures are routine.

**How covenant-based pull system delivers:**
- **Transaction waits** if recipient can't get to merchant (not a problem, covenant just waits immutably)
- **Transaction waits** if merchant loses power/connectivity (BCH safely locked in covenant)
- **Transaction waits** if it takes 15 minutes instead of 2 minutes (actually SAFER—more time to verify)
- BCH in covenant has **overcollateralization protection** during wait (seller bears 7% buffer)
- After 24 hours, **automatic refund** to BCH seller (no manual intervention)

**Why this matters:**
- Venezuela routinely has 6-12 hour power outages
- Rural areas have spotty mobile connectivity
- Merchant might be busy with other customers
- **Covenant-based pull system treats these as normal conditions, not failures**

**Real scenario:**
- Sender pays €100 via Bizum
- BCH seller locks €107 in covenant
- Recipient gets notification "funds ready"
- Power outage hits recipient's neighborhood for 8 hours
- Recipient waits until power restored, then goes to merchant next day
- Transaction completes successfully 22 hours later
- **Everyone gets paid correctly because covenant held BCH immutably the entire time**

**External reviewer concern (Gemini):** "If transaction takes 15 minutes due to Venezuela connectivity, merchant holds volatility risk."

**Reality:** Gemini had it backwards. The longer the delay, the SAFER covenant-based pull system becomes:
- Delay in Step 1 (covenant funding) → BCH seller bears risk via overcollateralization (not recipient)
- Delay in Step 2 (cash-out execution) → Covenant waits immutably, overcollateralization protects merchant
- **Covenant doesn't care if it waits 2 minutes or 22 hours** (autonomous code has no operational costs)

**15-minute transaction is safer than 2-minute transaction** because more time = more confirmation opportunities = fewer disputes.

---

### 4. No Orphaned Funds (Automatic Resolution)

**Requirement:** If recipient never claims, funds must return to sender automatically.

**How covenant-based pull system delivers:**
- BCH locked in covenant (no entity custody, no discretion)
- After 24 hours, **covenant timeout clause executes automatically**
- BCH returns to seller's wallet (immutable, on-chain)
- Sender's €100 Bizum refunded by BCH seller (minus €0.50 processing fee)
- No manual intervention, no customer support needed
- No "stuck" funds requiring investigation

**Why this matters:** In Phase 0, if recipient's phone breaks, they move, they forget—sender's money is not lost. BCH seller absorbs opportunity cost (24h capital lockup), but no one loses principal. System self-heals via code, not humans.

**Alternative (entity-based model) would fail:**
- Entity holding funds = custody service (MiCA CASP licensing)
- Entity deciding refund = intermediation (PSD2 payment service)
- Entity discretion = regulatory trigger

**Covenant:** Code executes predetermined logic. No entity custody. No intermediation. Compliant.

---

### 5. User Sovereignty (Recipient Controls Execution)

**Requirement:** Recipient decides when and where to claim funds, not the sender.

**How covenant-based pull system delivers:**
- Sender initiates transfer (creates covenant), but execution waits for recipient
- Recipient chooses which merchant to visit (convenience, trust, proximity)
- Recipient chooses when to claim (immediate, next day, next week—up to 24h)
- Recipient's physical presence + co-signature triggers covenant maturity (agency and control)
- **No entity can force execution** (covenant waits for both signatures)

**Why this matters:**
- Empowerment framing (not just technical)
- Recipient isn't passive—they control the process
- Builds trust (recipient verifies merchant hands cash before co-signing)
- Cultural fit (Latin America values face-to-face transactions)

**Alternative (push model) would fail:** Sender's action triggers immediate execution, recipient is just notified. Recipient becomes passive recipient of funds already in motion. Less control, less agency, less trust.

---

### 6. Regulatory Compliance (No Custody, No Intermediation)

**Requirement:** Avoid MiCA CASP and PSD2 payment service licensing.

**How covenant-based pull system delivers:**

**No custody service:**
- Covenant contract holds BCH (autonomous code, not entity)
- BCH seller posts **their own BCH as collateral** (not holding client funds)
- Sender never gives BCH to anyone (pays EUR via Bizum to seller)
- Recipient never holds BCH (receives VES cash from merchant)
- **No party provides "custody on behalf of clients"**

**No payment intermediation:**
- Each transaction is bilateral:
  - Sender → BCH seller (EUR for promise to lock BCH)
  - BCH seller → Covenant (posts own collateral)
  - Merchant → Recipient (VES cash for covenant signature)
  - Covenant → Merchant (BCH release, autonomous code)
- **No entity intermediates between sender and recipient**

**No discretionary control:**
- Covenant executes when both signatures present (deterministic)
- No entity decides who gets what (code enforces rules)
- No dispute resolution by freezing/reversing (covenant immutable once deployed)
- **No "provision of services to clients"**

**Precedent:** MakerDAO, Aave, Uniswap—users lock own capital in smart contracts for profit. Not regulated as custody/payment services because:
- Code executes autonomously
- Users own assets throughout
- No entity provides services

**Same principle applies to Asgaya BCH sellers.**

---

### 7. Volatility Protection (Bonus Benefit, Not Primary Driver)

**Common misconception:** Covenant-based pull system exists primarily to solve volatility.

**Reality:** Covenant-based pull system exists to meet permissionless engineering requirements **and regulatory compliance**. Volatility protection is a **valuable side effect**, not the core reason.

**Benefits:**
- Volatility window: 30 seconds (Step 2 execution only)
- Overcollateralization absorbs ±7% price swings during 24h wait
- Recipient gets exact VES amount expected
- Sender pays exact EUR amount committed

**But:** Even if BCH had zero volatility, we would still use covenant-based pull system because it's the only design that works with:
- Minimal hardware (no recipient wallet needed)
- Unreliable infrastructure (covenant waits safely)
- Fail-safe operation (automatic refunds)
- User sovereignty (recipient-controlled execution)
- **Regulatory compliance (no custody, no intermediation)**

**Volatility protection validates the choice, but regulatory compliance and engineering requirements drive it.**

---

## Summary: Why Covenant-Based Pull System is THE Solution

| Engineering Requirement | Push Model | Escrow Pull (Old) | Covenant Pull (New) |
|------------------------|------------|-------------------|---------------------|
| Minimal hardware (no recipient wallet) | ❌ Fails | ✅ Works | ✅ Works |
| Fail-safe (nothing until ready) | ❌ Fails | ✅ Works | ✅ Works |
| Unreliable infrastructure | ❌ Breaks | ✅ Waits safely | ✅ Waits immutably |
| No orphaned funds | ❌ Complex refunds | ✅ Auto-refund | ✅ Covenant timeout |
| User sovereignty | ❌ Sender-triggered | ✅ Recipient-triggered | ✅ Recipient co-signs |
| Volatility protection | ⚠️ High risk | ✅ 5-30 sec window | ✅ Overcollateralized |
| **No custody service** | ⚠️ Depends | ❌ Entity holds EUR | ✅ Code holds BCH |
| **No intermediation** | ⚠️ Depends | ❌ Entity coordinates | ✅ Autonomous code |
| **MiCA/PSD2 compliant** | ⚠️ Depends | ❌ CASP license needed | ✅ Compliant |

**Conclusion:** Covenant-based pull system isn't one of several options—it's the **only permissionless design** that works in difficult conditions with minimal infrastructure requirements **while remaining legally viable**.

---

## Covenant Contract Advantages Over Escrow Model

**Why we pivoted from escrow (April) to covenant (May):**

### Regulatory Compliance
- **Escrow model:** Entity holds EUR → Custody service → MiCA CASP licensing required (€50K-200K, 12-18 months)
- **Covenant model:** Code holds BCH → No entity custody → No licensing ✅

### Decentralization
- **Escrow model:** Single point of failure (one escrow per corridor)
- **Covenant model:** Multiple BCH sellers compete (permissionless participation)

### Trust Model
- **Escrow model:** Trust entity to buy BCH, send correctly, not misappropriate funds
- **Covenant model:** Trust code to execute correctly (open-source, auditable, immutable)

### Capital Efficiency
- **Escrow model:** Escrow needs EUR float (working capital locked)
- **Covenant model:** BCH seller uses own BCH inventory (miners have natural supply)

### Operational Risk
- **Escrow model:** Entity can be shut down, regulated, censored
- **Covenant model:** Covenant on blockchain (cannot be shut down)

**Result:** Same pull system UX + volatility protection, but permissionless and compliant.

---

## Rationale

### Why This Works

**1. Recipient gets VES immediately (Step 2 execution - 30 seconds)**
- No volatility risk on actual remittance (overcollateralization protects)
- Sender achieved their goal (sent money to recipient)
- Recipient can spend VES immediately

**2. BCH seller bears volatility risk via overcollateralization**
- Posts 107% of required BCH upfront
- If BCH drops <7%: Seller's buffer absorbs it, merchant still gets full amount
- If BCH rises: Seller gets larger surplus back (profit opportunity)
- If BCH drops >7%: Margin call (seller adds BCH or bounty refunds)

**3. Covenant executes deterministically**
- Both signatures present → Release BCH to merchant, surplus to seller
- Timeout expires (24h) → Refund BCH to seller
- **No human discretion** (code enforces rules)
- **No entity custody** (autonomous execution)

**4. BCH adoption happens organically**
- Merchants receive BCH as payment (0.5% fee)
- BCH sellers earn from fees + surplus (if BCH rises)
- Recipients see growing merchant network, more likely to use BCH

---

## Trade-offs Accepted

### Lost: Simplicity of Central Coordinator
- Covenant contracts more complex than escrow API calls
- Multiple BCH sellers instead of single escrow (coordination via bulletin board)
- Smart contract security risk (code bugs could lock funds)
- Overcollateralization capital requirement (107% vs 100%)

### Gained: Regulatory Compliance + Decentralization
- **No MiCA/PSD2 licensing required** (no custody, no intermediation)
- **Permissionless** (anyone can be BCH seller)
- **Cannot be shut down** (covenant on blockchain)
- **Trustless** (code executes, not entity)
- **Multiple liquidity providers** (no single point of failure)

### New Trust Requirements

**Covenant → Participants:**
- Trust smart contract code executes correctly (mitigation: open-source, audited, Chipnet tested)
- Trust BCH blockchain remains operational (mitigation: high hash rate, established network)

**Sender → BCH Seller:**
- Trust seller will lock BCH after receiving Bizum (mitigation: seller reputation on bulletin board)
- Trust seller won't disappear (mitigation: overcollateralization locked in covenant, not seller's wallet)

**Merchant → Covenant:**
- Trust covenant will release BCH after co-signing (mitigation: code is deterministic, testable)

**Recipient → Merchant:**
- Trust merchant will hand cash before asking for co-signature (mitigation: recipient controls final signature, won't sign without cash)

**Overall:** More participants, more trust relationships, but **less reliance on any single entity**. Distributed trust.

---

## Implementation Details

### Timing

**Step 1 (Covenant Creation & Funding):**
- Duration: 5-30 minutes (covenant deployment + BCH seller acceptance + Bizum payment)
- BCH seller holds: Overcollateralized BCH in covenant (7% buffer protects against volatility)

**Step 2 (Recipient-Triggered Execution):**
- Duration: 30 seconds (recipient at merchant → both co-sign → covenant executes)
- Covenant holds: BCH immutably until both signatures present

**Total time:** 10-45 minutes (comparable to Western Union, but recipient controls timing)

---

### Capital Requirements

**BCH Seller:**
- Must post €107 worth of BCH per €100 bounty (overcollateralized)
- Can lock for up to 24 hours (recipient controls timing)
- Example: 10 concurrent bounties = €1,070 locked

**Merchant:**
- Must have VES liquidity to hand out before covenant executes
- Receives BCH within seconds of co-signing
- No extended capital lockup

**Sender:**
- Pays €100 Bizum once (to BCH seller)
- No additional capital requirements

---

### Overcollateralization Mechanics

**How 7% buffer protects:**

| BCH Price Change | Impact | Result |
|------------------|--------|--------|
| Stable (0%) | Seller posted €107, merchant takes €99.5 | Seller gets €7.5 back (includes €0.5 fee + €7 buffer) |
| Rises 5% | €107 → €112.35 | Seller gets €12.85 back (larger surplus!) |
| Drops 3% | €107 → €103.79 | Seller gets €4.29 back (smaller surplus, still positive) |
| Drops 7% | €107 → €99.51 | Seller gets ~€0 back (break-even) |
| **Drops >7%** | **€107 → <€99.5** | **Margin call!** Seller must add BCH or bounty refunds |

**Margin call process:**
1. BCH price drops >5% during 24h window → Alert sent to seller
2. Seller has 1 hour grace period to top up covenant
3. If seller adds BCH → Covenant continues normally
4. If seller ignores → Covenant timeout triggers after 24h → Refunds to seller, sender's Bizum refunded

---

### Failure Modes

**Scenario 1: BCH seller doesn't accept bounty**
- Covenant never funded
- After timeout (e.g., 2 hours), sender notified: "No BCH seller accepted"
- Sender can cancel or wait for different seller
- No funds lost (sender hasn't paid Bizum yet)

**Scenario 2: Sender doesn't pay Bizum after seller locks BCH**
- Seller locked €107 in covenant
- Sender disappears (doesn't send Bizum)
- After 24h, covenant timeout → BCH returns to seller
- Seller lost: Opportunity cost (24h capital lockup)
- Mitigation: Reputation system penalizes senders who don't pay

**Scenario 3: Recipient doesn't cash out at merchant**
- BCH locked in covenant (no volatility risk for first 7%)
- After 24 hours, covenant timeout → BCH returns to seller
- Sender's €100 Bizum refunded by seller (minus €0.50 processing fee)
- BCH seller bears opportunity cost (24h lockup)

**Scenario 4: BCH drops >7% during wait (margin call)**
- Seller notified: "Add 0.0005 BCH within 1 hour or bounty refunds"
- Option A: Seller tops up → Covenant continues
- Option B: Seller ignores → Timeout after 24h → Refund
- Recipient not affected (hasn't received anything yet)

**Scenario 5: Covenant code bug**
- Rare (Chipnet testing + community audit should catch)
- Worst case: Funds locked permanently
- Mitigation: Extensive testing, formal verification, start with small amounts (€50-100)

**Scenario 6: BCH blockchain downtime**
- Extremely rare (BCH has 99.9%+ uptime)
- Covenant waits until blockchain operational
- No funds lost (covenant immutable)

---

## Validation

**How we verify this decision:**
- ⏳ **Pending:** Chipnet testing (May 2026)
  - Deploy test covenants
  - Simulate margin calls
  - Test timeout/refund logic
  - Measure overcollateralization effectiveness
- ⏳ **Pending:** Mainnet pilot (June 2026)
  - 1-2 trusted BCH sellers (Phase 0)
  - €50-100 transactions
  - Real Spain → Venezuela corridor
  - Validate real-world volatility protection

**Metrics to track:**
- Overcollateralization coverage (target: >95% of transactions within 7% buffer)
- Margin call frequency (target: <5% of transactions)
- Covenant execution success rate (target: >99%)
- Recipient claim rate (target: >90% within 24h)
- BCH seller profitability (target: Positive after fees + surplus)

---

## Related Decisions

- [Fee Splitting Model](./fee-splitting-model.md) — How 1% fee split between seller (0.5%) and merchant (0.5%)
- [Payment Timeout Window](./payment-timeout-window.md) — Why 24-hour timeout works for covenant
- [Dispute Resolution](./dispute-resolution.md) — How conflicts handled without central arbiter

---

## Related Concepts

- [Pull System](../concepts/pull-system.md) — Why recipient-triggered execution is critical
- [Overcollateralized Bounty Contracts](../concepts/overcollateralized-bounty-contracts.md) — Technical covenant implementation
- [Core Regulatory Constraints](../concepts/core-regulatory-constraints.md) — Why covenant-based design is required
- [BCH Sellers](../concepts/bch-sellers.md) — Who provides liquidity and why
- [Volatility Protection](../core-architecture/why-eliminate-volatility.md) — Architecture overview

---

## References

- **Architecture:** `/docs/core-architecture/why-eliminate-volatility.md`
- **Implementation:** `/docs/android-app/flows/sender-flows.md`, `/docs/android-app/flows/recipient-flows.md`
- **Smart Contracts:** CashScript covenant specification (to be documented)

---

**Decision made:** April 2026  
**Updated for covenant architecture:** May 10, 2026  
**Validated:** €3 test transfers successful (escrow model), Chipnet testing pending (covenant model)  
**Status:** Active design, undergoing implementation pivot from escrow to covenant
