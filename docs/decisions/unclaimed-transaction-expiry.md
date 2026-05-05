← [Back to Decisions](README.md)

# Unclaimed Transaction Expiry Policy

**Status:** Active (MVP)  
**Category:** User Protection & Escrow Operations  
**Related:** [Two-Step Settlement](two-step-settlement-timing.md), [Transaction APIs](android-app/backend-apis/transaction-apis.md)

---

## The Goal

Prevent funds from being locked indefinitely in escrow when recipients never claim their remittances.

---

## The Problem

**Scenario:** Sender sends €100 to recipient Elena in Venezuela. Elena never goes to a merchant to claim. What happens?

**Without expiry policy:**
- EUR remains locked in escrow indefinitely
- Escrow has already bought BCH (to eliminate volatility window)
- No clear resolution process
- Sender's money is in limbo
- Escrow bears ongoing operational costs

**Critical gap identified by:** DeepSeek Review (May 4, 2026)

---

## The Constraint

**Escrow operational reality (Pull System):**
1. Escrow receives €100 via Bizum (instant)
2. Escrow **HOLDS EUR** (doesn't buy BCH yet - pull system!)
3. Claim code generated and sent to recipient
4. **If recipient claims:** Escrow buys BCH, settles transaction
5. **If recipient never claims:**
   - EUR sits in escrow account for 24h (liquidity locked)
   - No BCH purchased (pull system = on-demand purchase)
   - No revenue earned (transaction never completed)
   - Escrow must manually send Bizum refund (administrative work)

**Escrow costs for unclaimed transaction:**
- Buy BCH: €0.00 (never purchased - pull system!)
- Sell BCH: €0.00 (never purchased)
- Manual refund: ~10 min work
- Liquidity lock: €100 tied up for 24h (opportunity cost)
- **Total cost:** Time + opportunity cost (no hard exchange fees)

---

## Alternatives Considered

### Option 1: No Expiry (Indefinite Hold)
**How it works:** Funds stay in escrow until claimed (days, weeks, months)

**Pros:**
- ✅ Recipient can claim anytime (maximum flexibility)
- ✅ Simple logic (no expiry mechanism needed)

**Cons:**
- ❌ Escrow bears unlimited volatility risk
- ❌ Sender's money in limbo indefinitely
- ❌ Operational costs accumulate
- ❌ No resolution for forgotten/lost transactions

**Verdict:** ❌ **Rejected** — Transfers risk to escrow operator, terrible UX for sender

---

### Option 2: Aggressive Expiry (6-12 hours)
**How it works:** Short claim window to minimize escrow exposure

**Pros:**
- ✅ Minimal escrow risk
- ✅ Fast resolution
- ✅ Encourages immediate claiming

**Cons:**
- ❌ Too short for recipients in remote areas
- ❌ Doesn't account for connectivity issues
- ❌ Forces rushed claims (bad UX)
- ❌ Sender/recipient may need coordination time

**Verdict:** ❌ **Rejected** — Prioritizes escrow convenience over user success

---

### Option 3: 24-Hour Expiry with Refund
**How it works:** Recipients have 24 hours to claim, then auto-refund sender

**Pros:**
- ✅ Reasonable window for coordination
- ✅ Bounded escrow risk (24h max volatility exposure)
- ✅ Clear deadline for all parties
- ✅ Automatic resolution (no manual intervention)
- ✅ Sender gets most of their money back

**Cons:**
- ⚠️ 24h might be tight for rural/remote recipients
- ⚠️ Processing fee deducted from refund
- ⚠️ Requires notification infrastructure

**Verdict:** ✅ **SELECTED** — Best balance of user flexibility and escrow protection

---

### Option 4: Configurable Expiry (24-72h)
**How it works:** Sender chooses claim window at transaction creation

**Pros:**
- ✅ Maximum flexibility
- ✅ Sender controls their own risk tolerance

**Cons:**
- ❌ More complex UX (another decision point)
- ❌ Escrow can't predict exposure
- ❌ Longer windows = higher escrow costs

**Verdict:** ❌ **Rejected for MVP** — Can add in V1.1 if users request it

---

### Option 5: Extension Mechanism
**How it works:** 24h default, sender can grant 24h extension if needed

**Pros:**
- ✅ Handles edge cases (recipient traveling, sick, etc.)
- ✅ Sender retains control

**Cons:**
- ❌ Adds implementation complexity
- ❌ Most transactions won't need it (sender/recipient coordinate beforehand)
- ❌ Extends escrow exposure

**Verdict:** ❌ **Rejected for MVP** — Real usage data will show if needed

---

## The Decision

**24-hour claim window with automatic refund and proactive notifications**

**Timeline:**
```
Hour 0:  Transaction created → Claim code sent to recipient
Hour 12: Reminder to recipient ("Claim your money today")
Hour 18: Status update to sender ("Not claimed yet, auto-refund in 6h")
Hour 23: URGENT reminder to recipient ("Claim in 1 hour or funds refunded")
Hour 24: Auto-refund initiated → Both parties notified
```

**Refund calculation:**
```
Original amount:     €100.00
Processing fee:      -€0.10 (0.1%)
─────────────────────────────
Refund to sender:    €99.90
```

**What processing fee covers:**
- Manual refund work (escrow sends Bizum back to sender)
- 24h liquidity lock (escrow's EUR was tied up)
- Administrative overhead

**What it does NOT cover:**
- ❌ Exchange fees (no BCH was purchased - pull system!)
- ❌ Network fees (no blockchain transaction happened)

**Why 0.1%?**
- Start low, encourage legitimate use
- Can increase to 0.2-1.0% if abuse detected (discourage fake claims)
- Real escrow feedback will determine optimal rate

**Who gets notified:**
- ✅ **Recipient:** Action-oriented reminders (hours 12, 23)
- ✅ **Sender:** Status updates (hours 18, 24)
- ✅ **Both:** Final outcome notification (hour 24)

---

## Trade-offs

### What We Gained ✅
- **Clear deadline:** Everyone knows when funds will be refunded
- **Automatic resolution:** No manual escrow intervention needed
- **Bounded escrow risk:** 24h max BCH volatility exposure
- **Fair processing fee:** Only covers actual exchange costs
- **Sender protection:** Get money back if recipient can't claim
- **Recipient protection:** Multiple reminders prevent accidental expiry

### What We Lost ❌
- **Infinite flexibility:** Can't claim weeks/months later
- **Zero-fee refunds:** Sender loses €0.50 to exchange costs
- **Edge case handling:** No built-in extension for special circumstances

### What We're Unsure About 🤔
- **Is 24h enough for rural areas?** (Will monitor success rate)
- **Do we need corridor-specific timeouts?** (EUR→VES urban vs rural)
- **Should we allow extensions?** (Wait for user feedback)

---

## Implementation Details

### Transaction State Machine Updates

**New states:**
- `expiring_soon` — Claim window closing (<6h remaining)
- `expired_unclaimed` — 24h passed, no claim, refund initiated
- `refunded` — Funds returned to sender

**State transitions:**
```
pending → active (escrow funded)
active → claimed (recipient claims)
active → expiring_soon (18h elapsed)
expiring_soon → claimed (recipient claims before expiry)
expiring_soon → expired_unclaimed (24h elapsed, no claim)
expired_unclaimed → refunded (BCH sold, EUR returned)
```

### Notification Requirements

**Recipient notifications (app + SMS):**
- Hour 12: "Reminder: €100 waiting. Claim at [merchant list]"
- Hour 23: "URGENT: Claim in 1 hour or €100 will be refunded"
- Hour 24: "Transaction expired. Funds returned to sender."

**Sender notifications (app):**
- Hour 18: "Elena hasn't claimed yet. Auto-refund in 6h if unclaimed. Contact: 0412-XXX-5678"
- Hour 24: "Elena didn't claim. Refunding €99.50 (€0.50 processing fee)"

### API Updates

**GET /transaction/:txId endpoint:**
```json
{
  "status": "expiring_soon",
  "claim_deadline": "2026-05-06T18:30:00Z",
  "hours_remaining": 5.2,
  "refund_amount_eur": 99.50,
  "processing_fee_eur": 0.50
}
```

**Escrow automation:**
- Cron job checks transactions every 15 minutes
- At 24h mark: Send Bizum refund to sender (€99.90) → Update state to `refunded`
- No BCH to sell (pull system - BCH was never purchased)

---

## Validation

### Success Metrics
- **<5% expiry rate** — Most claims should succeed within 24h
- **<10% support requests** — Notifications should make process clear
- **Zero stuck funds** — No manual intervention needed

### What Would Prove Us Wrong
- **>15% expiry rate** — 24h is too short, need longer window
- **Many extension requests** — Users need flexibility we didn't provide
- **Corridor-specific patterns** — Rural areas need different timeouts

### Monitoring
- Track expiry rate by corridor (EUR→VES urban vs rural)
- Track time-to-claim distribution (most within 1h? 12h? 24h?)
- Survey recipients who expired: Why didn't you claim?

---

## Open Questions

**For Phase 0-1 validation:**
1. **Is 24h universally adequate?** Or do rural corridors need longer?
2. **Should notification timing differ by corridor?** (Urban: aggressive reminders, Rural: earlier heads-up)
3. **Do we need manual extension capability?** (Wait for user feedback)
4. **Should refund be instant or T+1?** (Depends on Bizum receiving bank policies)

**Will resolve through:**
- Real transaction data from beta testing
- User feedback surveys
- Corridor-specific analytics

---

## Future Enhancements (Post-MVP)

**Not committed to these, but worth considering after real usage data:**

### 1. Dynamic Processing Fee (Anti-Abuse)
**Rationale:** Prevent users from abusing the system by creating fake transactions

**Current:** Fixed 0.1% processing fee (€0.10 on €100)

**If abuse detected (>10% expiry rate from same user):**
- First expiry: €0.10 (0.1%) - benefit of the doubt
- Second expiry: €0.50 (0.5%) - warning
- Third+ expiry: €1.00 (1.0%) - full fee to discourage abuse

**Abuse patterns to watch:**
- Same sender repeatedly creating transactions that expire
- Sender creates transaction, recipient never attempts to claim
- Pattern suggests using Asgaya as "free parking" for EUR

**When to add:** If expiry rate >5% AND pattern analysis shows deliberate abuse

**Trade-off:** Penalizes legitimate users who have multiple coordination failures

---

### 2. Corridor-Specific Timeouts
**Rationale:** Urban areas might succeed faster than remote villages

**Implementation:**
- EUR→VES (Caracas): 24h (default)
- EUR→VES (rural): 48h
- EUR→HNL (remote): 72h

**When to add:** If expiry rate >10% in specific corridors

---

### 2. Sender-Initiated Extension
**Rationale:** "My sister is traveling, needs 2 more days"

**Implementation:**
- Before expiry: Sender taps "Extend 24h"
- Maximum: 1 extension (48h total)
- Recipient notified of extension

**When to add:** If >5% of users request it

---

### 3. Smart Expiry Based on Merchant Availability
**Rationale:** No point in 24h window if no merchants are available

**Implementation:**
- Check merchant availability in recipient's area
- If zero merchants available: Notify sender immediately, shorter timeout (6h)
- If merchants exist: Standard 24h window

**When to add:** When merchant network is larger (10+ merchants per corridor)

---

### 4. Partial Auto-Claim
**Rationale:** Reduce escrow volatility exposure

**Implementation:**
- At hour 20: If unclaimed, escrow sells 50% of BCH (locks in half the refund)
- At hour 24: Sells remaining 50%
- Reduces price movement risk for escrow

**When to add:** If BCH volatility causes significant variance in refund amounts

---

## Related Research

- **[RS010_Bizum.md](../research/RS010_Bizum.md)** — Bizum refund capabilities and timing
- **[RS015_Kraken_API.md](../research/RS015_Kraken_API.md)** — Round-trip fee calculation
- **[Two-Step Settlement Timing](two-step-settlement-timing.md)** — Why escrow buys BCH immediately

---

## Contributors

- **Initial Policy:** Suso (May 5, 2026)
- **Critical Gap Identified:** DeepSeek Review (May 4, 2026)
- **Implementation Design:** Coordination (May 5, 2026)

---

**Last updated:** May 5, 2026  
**Status:** Active (MVP)  
**Next review:** After Phase 1 (50+ transactions)
