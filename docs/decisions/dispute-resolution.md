← [Back to Decisions](README.md)

# Dispute Resolution Framework

**Status:** Active (MVP)  
**Category:** Trust & Safety  
**Related:** [Two-Sided Confirmation](android-app/backend-apis/transaction-apis.md), [Merchant Flows](android-app/flows/merchant-flows.md)

---

## The Problem

**Two-sided confirmation requires both parties to agree:**
- Merchant confirms: "I gave cash to recipient"
- Recipient confirms: "I received cash from merchant"

**What if they don't match?**

| Merchant | Recipient | Scenario | Resolution Needed? |
|----------|-----------|----------|-------------------|
| ✅ Confirm | ✅ Confirm | Happy path | No |
| ✅ Confirm | ❌ Deny | **Disputed delivery** | YES - Who's lying? |
| ❌ Deny | ✅ Confirm | Merchant backing out | YES - Why no cash? |
| ❌ Deny | ❌ Deny | Coordination failure | YES - What happened? |

**Most common dispute:** Merchant confirms, recipient denies (fraud by one party).

---

## The Decision: Phased Approach

### Phase 0-1 (MVP): Escrow Manual Resolution

**Who decides:** Escrow operator (trusted human)  
**Timeline:** 24 hours max (accounts for time zones)  
**Evidence:** Email submission from both parties  
**Default:** Favor merchant (scarce resource, unless previous strikes)

**Why this works for MVP:**
- 1-2 trusted merchants (personally vetted)
- Low volume (manual investigation feasible)
- Small amounts (€50-100 max)
- Trusted tester network (fraud unlikely)

---

## 3-Strike Merchant System

**Philosophy:** Progressive enforcement + permanent trust history

### Strike 1: Internal Flag (PERMANENT)
**Trigger:** First disputed transaction  
**Impact:**
- Recorded permanently (never removed, even after €20K successful txns)
- Not visible to recipients
- Enhanced warning to **recipient**: 
  > ⚠️ **IMPORTANT:** Tell merchant to accept AFTER they hand you the cash!

**Escrow action:** Internal investigation
- Circumstances? (Internet outage, safety issue, misunderstanding?)
- Evidence review (video, photos, witnesses)
- Decision logged

**Why permanent?** Trust signal - merchants with zero strikes are "pristine"

---

### Strike 2: Public Warning (Redeemable)
**Trigger:** Second disputed transaction  
**Impact:**
- Warning visible to recipients:
  > ⚠️ This merchant has disputed transactions. Use caution.
- Merchant can still operate
- Recipients choose whether to proceed

**Redemption:** Removed after **€2,000** worth of successful transactions  
*(Arbitrary - validate during trials)*

**Why redeemable?** Merchants in challenging environments (Honduras, rural areas) deserve second chance.

---

### Strike 3: Final Warning (Permanent)
**Trigger:** Third disputed transaction  
**Impact:**
- Severe warning to recipients:
  > ⚠️ WARNING: Multiple disputes. Cash out at your own risk.
- Merchant still operational (permissionless network)
- Effectively "community flagged"

**No redemption** - Pattern indicates systemic issue.

---

## Safe Confirmation Sequence (Critical UX)

**Self-auditing through proper sequencing:**

```
1. Recipient shows code to merchant
2. Merchant enters code → Validates amount
3. ⚠️ Merchant HANDS CASH to recipient (FIRST!)
4. Merchant presses "I gave cash" (while recipient watches)
5. Recipient presses "I received cash" (while merchant watches)
6. Both confirmations FACE-TO-FACE
```

**UI Enforcement:**
- Merchant screen: "⚠️ Hand cash BEFORE confirming"
- Recipient screen: "Confirm AFTER receiving cash, in merchant's presence"

---

## Evidence Collection

**Escrow email:** `disputes@asgaya.org` (or similar)

**Evidence hierarchy:**
1. **Video** of transaction at register (gold standard)
2. **Photos** of recipient with cash
3. **GPS proof** (recipient was at merchant location)
4. **Witness statements** (other customers, merchant staff)
5. **Word-of-mouth** (lowest weight)

**Merchant onboarding:** Ask "What hardware do you have?" (CCTV, camera, none)
- Sets evidence expectations
- Doesn't require it (low-tech environments)

**Both parties can submit:**
- Merchant: Proof of delivery
- Recipient: Counter-evidence, explanation

---

## Resolution Rules

**Timeline:** 24h max from dispute flag

**Default resolution (if evidence unclear):**
- **Favor merchant** (scarce resource for network growth)
- **Exception:** Merchant has Strike 1 or Strike 2 → scrutinize more carefully

**Possible outcomes:**
1. **Merchant wins** → BCH sent to merchant, transaction complete
2. **Recipient wins** → EUR refunded to sender (minus €0.10 processing), merchant flagged
3. **Insufficient evidence** → Favor merchant (default), but Strike 1 applied
4. **Both at fault** → Split loss, both flagged

---

## Open Questions (Validate in Trials)

1. **Strike 2 redemption threshold:** Is €2,000 the right amount? Too high? Too low?
2. **Strike 1 permanent record:** Should it ever expire? (€20K? €50K? Never?)
3. **Evidence requirements:** Should video be mandatory at Strike 2?
4. **Time zones:** Is 24h sufficient for global corridors?
5. **Appeal process:** Should merchants be able to appeal Strike 2/3?

---

## Phase 2+: Community Arbitration (Future)

**Not for MVP - validate approach first:**

### Dispute Pool Fee
- Voluntary 0.1% fee on transactions → dispute resolution fund
- Compensates edge cases where neither party has proof
- First few hundred merchants: Give benefit of doubt (cost of real-world data)

### Community Arbitrators
- Trusted network members vote on disputes
- 3-of-5 arbitrator consensus required
- Arbitrators earn fee for participation
- Prevents single point of failure (escrow operator)

### Automated Evidence Verification
- GPS timestamps (recipient at merchant location?)
- Blockchain analysis (transaction timing patterns)
- Machine learning fraud detection (repeat offenders)

**When to add:** After 500+ successful transactions, when manual resolution doesn't scale.

---

## Success Metrics

**MVP (Phase 0-1):**
- **<2% dispute rate** (most transactions complete smoothly)
- **100% resolved in 24h** (no backlog)
- **Zero Strike 3 merchants** (network stays healthy)

**What would prove us wrong:**
- **>10% dispute rate** → Trust model broken, need stronger verification
- **Merchants gaming system** → Strike thresholds too lenient
- **Recipient fraud epidemic** → Default should favor recipients instead

---

## Related Research

- **Safe confirmation UX** prevents most disputes before they happen
- **Honduras merchant context** (unsafe environments → CCTV likely installed)
- **Trust networks** (social reputation in local communities)

---

## Contributors

- **Framework design:** Suso + Coordination (May 5, 2026)
- **Critical gap identified:** DeepSeek Review (May 4, 2026)

---

**Last updated:** May 5, 2026  
**Status:** Active (MVP)  
**Next review:** After Phase 1 (50+ transactions, first dispute)

**Line count:** 195 lines ✅
