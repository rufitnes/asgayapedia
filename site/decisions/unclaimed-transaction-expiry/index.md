← [Back to Decisions](README.md)

# Unclaimed Transaction Expiry Policy

**Status:** Active (MVP)  
**Category:** User Protection & Covenant Operations  
**Related:** [Two-Step Settlement](two-step-settlement-timing.md), [Transaction APIs](../android-app/backend-apis/transaction-apis.md)

---

## The Goal

Prevent funds from being locked indefinitely in covenant when recipients never claim their remittances.

---

## The Problem

**Scenario:** Sender sends €100 to recipient Elena in Venezuela. Elena never goes to a merchant to claim. What happens?

**Without expiry policy:**
- BCH remains locked in covenant indefinitely
- BCH seller's capital is tied up (with volatility buffer position)
- No clear resolution process
- Sender's money is in limbo
- BCH seller bears ongoing operational costs

**Critical gap identified by:** DeepSeek Review (May 4, 2026)

---

## The Constraint

**BCH Seller operational reality (Covenant System):**
1. BCH seller receives €100 via Bizum (instant)
2. BCH seller **locks BCH in covenant** (with volatility buffer: €107 worth)
3. Claim code generated and sent to recipient
4. **If recipient claims:** Covenant executes, BCH distributed automatically
5. **If recipient never claims:**
   - BCH remains locked in covenant for 24h (capital tied up)
   - Covenant timeout triggers automatic refund
   - No revenue earned (transaction never completed)
   - BCH seller must manually send Bizum refund to sender (administrative work)

**BCH Seller costs for unclaimed transaction:**
- BCH locked: €107 (with volatility buffer capital)
- Covenant gas fee: minimal
- Manual refund: ~10 min work
- Capital lock: €107 BCH tied up for 24h (opportunity cost)
- **Total cost:** Time + opportunity cost + volatility exposure

---

## Why Seller Fraud Doesn't Work

**The primary defense:** [Universal Bot Fraud Prevention](../concepts/universal-bot-fraud-prevention.md)

Both sender and seller run the same notification listener bot:
- **Sender's bot:** Creates cryptographic fraud proof when Bizum sent
- **Seller's bot:** Auto-signs covenant when Bizum received (no manual decision)
- **If seller doesn't sign:** Sender submits proof → Stake slashed → Seller banned

**Economic deterrence:**
- Seller can't predict which senders have working bots
- Expected value of fraud attempt: **-€90**
- One failed attempt = -€100 stake + permanent ban
- Opportunity cost: €3,600/year in honest earnings lost forever

**Timeout refund split (secondary protection):**

If covenant times out (recipient never claims), refund is **SPLIT**:
- **Merchant portion** (€99.50 worth of BCH) → **Sender**
- **Seller processing fee** (€0.50 worth of BCH) → **Seller**

**Why seller can't profit by ghosting:**
- Locks: €107 BCH
- Receives: €100 Bizum
- Gets back: €0.50 BCH (processing fee only)
- **Must refund €99.50 Bizum to break even**

> **Beyond the immediate loss:** Ghosting also means permanent exclusion from the seller role. A banned seller loses access to a business model that can generate ≈360% APR on deployed capital (see [Capital Recycling Strategy](../concepts/bounty-contracts-with-volatility-buffer.md#capital-recycling-strategy-the-sellers-business-model)). The opportunity cost dwarfs the €6.50 static loss.

**See:** [Universal Bot Fraud Prevention](../concepts/universal-bot-fraud-prevention.md) for complete analysis.

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
- Manual refund work (BCH seller sends Bizum back to sender)
- 24h capital lock (BCH seller's capital was tied up)
- Administrative overhead
- Volatility exposure during lock period

**What it does NOT cover:**
- ❌ Covenant gas fees (minimal)
- ❌ Volatility buffer surplus (already locked)

**Why 0.1%?**
- Start low, encourage legitimate use
- Can increase to 0.2-1.0% if abuse detected (discourage fake claims)
- Real BCH seller feedback will determine optimal rate

**Who gets notified:**
- ✅ **Recipient:** Action-oriented reminders (hours 12, 23)
- ✅ **Sender:** Status updates (hours 18, 24)
- ✅ **Both:** Final outcome notification (hour 24)

---

## Trade-offs

### What We Gained ✅
- **Clear deadline:** Everyone knows when funds will be refunded
- **Automatic resolution:** Covenant timeout handles refund automatically
- **Bounded BCH seller risk:** 24h max BCH volatility exposure
- **Fair processing fee:** Covers actual capital lock and admin costs
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
pending → active (covenant funded)
active → claimed (recipient claims)
active → expiring_soon (18h elapsed)
expiring_soon → claimed (recipient claims before expiry)
expiring_soon → expired_unclaimed (24h elapsed, no claim)
expired_unclaimed → refunded (covenant timeout, BCH returned to seller, EUR refunded to sender)
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

**BCH Seller automation:**
- Cron job checks covenant timeouts every 15 minutes
- At 24h mark: Covenant automatically refunds BCH to seller
- BCH seller sends Bizum refund to sender (€99.90) → Update state to `refunded`
- Covenant handles BCH distribution automatically

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
**Rationale:** Reduce BCH seller volatility exposure

**Implementation:**
- At hour 20: If unclaimed, covenant could trigger partial refund mechanism
- At hour 24: Full covenant timeout and refund
- Reduces price movement risk for BCH seller

**When to add:** If BCH volatility causes significant variance in refund amounts

**Note:** Requires covenant contract modification for partial timeout logic.

---

## Related Research

- **[RS010_Bizum.md](../research/RS010_Bizum.md)** — Bizum refund capabilities and timing
- **[Two-Step Settlement Timing](two-step-settlement-timing.md)** — Why covenant-based architecture prevents volatility risk

---

## Contributors

- **Initial Policy:** Suso (May 5, 2026)
- **Critical Gap Identified:** DeepSeek Review (May 4, 2026)
- **Implementation Design:** Coordination (May 5, 2026)

---

**Last updated:** May 5, 2026  
**Status:** Active (MVP)  
**Next review:** After Phase 1 (50+ transactions)
