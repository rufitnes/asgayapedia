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

### Phase 0-1 (MVP): Covenant + Manual Mediation

**Smart contract role:** Autonomous execution (no human discretion)  
**Mediator role:** Investigate signature deadlock, apply strikes, recommend resolution  
**Timeline:** 24 hours max (covenant timeout)  
**Evidence:** Email submission from both parties  
**Default:** Favor merchant (scarce resource, unless previous strikes)

**How it works:**
1. **Happy path:** Both parties sign covenant → automatic distribution (no mediator involved)
2. **Dispute path:** One party refuses to sign → manual mediation investigates
3. **Timeout:** 24h from BCH lock → covenant refunds seller if no signatures
4. **Key difference from escrow:** Mediator doesn't control funds, covenant does

**Why this works for MVP:**
- 1-2 trusted merchants (personally vetted)
- Low volume (manual investigation feasible)
- Small amounts (€50-100 max)
- Trusted tester network (fraud unlikely)
- **Regulatory compliance:** No entity has discretionary control over distribution

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

**Mediator action:** Internal investigation
- Circumstances? (Internet outage, safety issue, misunderstanding?)
- Evidence review (video, photos, witnesses)
- Recommendation logged

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

### Phase 0: Covenant Co-Signature (Smartphone App)

```
1. Recipient shares covenant details with merchant (QR code or numeric code)
2. Merchant reviews terms in app:
   - Amount: 500,000 VES
   - Merchant fee: 2,500 VES (0.5%)
   - Covenant conditions: [1] Seller paid ✅, [2] Merchant confirms cash given ⏳
3. Merchant accepts bounty (commits to terms)
4. ⚠️ Merchant HANDS CASH to recipient (FIRST!)
5. Merchant signs covenant: "Cash delivered" (cryptographic signature)
6. Recipient reviews same terms in THEIR app
7. Recipient signs covenant: "Cash received" (cryptographic signature)
8. Covenant executes automatically → BCH distributed on-chain
```

**Why this works:**
- Covenant requires BOTH signatures (merchant cannot complete alone)
- Recipient won't sign without cash in hand
- Signatures are cryptographic (blockchain-verifiable, immutable)
- Covenant execution is autonomous code (no entity discretion)

**Security properties:**
- **Blockchain-anchored:** Signatures recorded on BCH blockchain
- **Non-repudiable:** Cannot claim "I didn't sign" after signing
- **Atomic execution:** Both conditions met → distribution happens automatically
- **Timeout protection:** 24h from funding → auto-refund if no signatures
- **No central control:** Smart contract executes, not human operator

**UI Enforcement:**
- Merchant screen: "⚠️ Hand cash BEFORE signing covenant"
- Recipient screen: "⚠️ ONLY sign AFTER receiving cash"
- Both screens show: "Once you sign, covenant executes automatically"

**Security residual risk (acknowledged):**
- ⚠️ **Social engineering possible:** Merchant can pressure recipient to sign before giving cash
- **Mitigation:** UI warnings, reputation system, trusted merchants (Phase 0)
- **Phase 0 acceptance:** 1-2 personally vetted merchants
- **Future defense:** Video verification, bonds/insurance, community reputation (Phase 2+)
- **Key advantage over escrow:** Dispute resolution doesn't control funds (covenant does)
- **Honest assessment:** Acceptable for Phase 0, requires additional layers for scale

---

### Dispute Scenario: Signature Deadlock

**Problem:** What if one party refuses to sign the covenant?

**Scenarios:**
- Merchant claims they gave cash, recipient refuses to sign
- Recipient claims they didn't receive cash (or wrong amount)
- Merchant backs out after seeing recipient
- Network failure prevents signature submission
- App crashes during signing process

**Covenant timeout mechanism (automatic):**

**If both signatures within 24h:**
- Covenant executes automatically
- BCH distributed on-chain
- Transaction complete (immutable)

**If timeout expires (24h from BCH lock):**
- Covenant refunds BCH seller automatically
- No human intervention needed
- Seller refunds sender's Bizum payment (minus small processing fee)
- Transaction cancelled (no BCH moved to merchant)

**Manual mediation (signature deadlock):**

**1. Dispute flagged by either party**
- Merchant claims: "I gave cash, recipient won't sign"
- Recipient claims: "Merchant didn't give cash / wrong amount"
- Both submit evidence to `disputes@asgaya.org`

**2. Mediator investigates (24h max)**
- Reviews evidence hierarchy (video > photo > GPS > word)
- Checks merchant strike history
- Analyzes timeline (when was cash supposed to be given?)
- **Key limitation:** Mediator CANNOT force covenant execution
- Mediator CAN recommend resolution + apply strikes

**3. Possible outcomes:**
- **Merchant vindicated:** Recipient instructed to sign, Strike 1 applied to recipient for bad faith
- **Recipient vindicated:** Timeout expires, covenant refunds seller, Strike applied to merchant
- **Unclear evidence:** Default favor merchant (scarce resource), but Strike 1 applied
- **Both at fault:** Timeout expires, both flagged for review

**Why covenant model is better:**
- Mediator doesn't control funds (covenant does)
- No discretionary intermediation (regulatory compliance)
- Timeout prevents indefinite BCH lock
- Immutable audit trail (blockchain signatures)
- Cannot reverse completed transactions (once both signed, autonomous execution)

---

### Future: RFID Card Alternative (Phase 1+)

For recipients without smartphones, an RFID card can be used instead:

```
1-3. [Same as above]
4. Merchant hands cash to recipient
5. Merchant device shows: "Ask recipient to TAP their Asgaya card"
6. Recipient taps RFID card on merchant device (NFC)
7. Card cryptographically signs transaction → completes
```

**See:** [RFID Card Recipients](concepts/rfid-card-recipients.md) for full specification.

**Phase 0 status:** Not implemented yet (smartphone app only)

---

## Evidence Collection

**Dispute mediation email:** `disputes@asgaya.org` (or similar)

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

**Timeline:** 24h max from dispute flag (matches covenant timeout)

**Critical constraint:** Mediator cannot force covenant execution, only make recommendations

**Default resolution (if evidence unclear):**
- **Favor merchant** (scarce resource for network growth)
- **Exception:** Merchant has Strike 1 or Strike 2 → scrutinize more carefully

**Possible outcomes:**

1. **Merchant vindicated** (evidence shows cash was given)
   - Mediator instructs recipient to sign covenant
   - If recipient complies → covenant executes, BCH sent to merchant
   - If recipient refuses → timeout expires, covenant refunds seller, recipient banned from network
   - Strike 1 applied to recipient (bad faith)

2. **Recipient vindicated** (evidence shows cash was NOT given)
   - Mediator recommends: let timeout expire
   - Covenant refunds BCH seller automatically
   - Seller refunds sender's Bizum (minus €0.10 processing)
   - Strike applied to merchant

3. **Insufficient evidence** (he-said-she-said)
   - Default: Favor merchant (instruct recipient to sign)
   - Strike 1 applied to merchant (permanent record, even if favored)
   - If dispute pattern emerges → escalate to Strike 2

4. **Both at fault** (evidence shows mutual bad behavior)
   - Let timeout expire (covenant refunds)
   - Both flagged for review
   - Both may be banned depending on severity

**Key innovation:** Covenant timeout provides automatic resolution if mediation fails. No funds stay locked indefinitely.

---

## Open Questions (Validate in Trials)

1. **Strike 2 redemption threshold:** Is €2,000 the right amount? Too high? Too low?
2. **Strike 1 permanent record:** Should it ever expire? (€20K? €50K? Never?)
3. **Evidence requirements:** Should video be mandatory at Strike 2?
4. **Time zones:** Is 24h sufficient for global corridors?
5. **Appeal process:** Should merchants be able to appeal Strike 2/3?

---

## Phase 2+: Community Mediation (Future)

**Not for MVP - validate approach first:**

### Dispute Pool Fee
- Voluntary 0.1% fee on transactions → dispute mediation fund
- Pays community mediators for investigation work
- Compensates edge cases where neither party has proof
- First few hundred merchants: Give benefit of doubt (cost of real-world data)

### Community Mediators (Not Arbitrators)
- Trusted network members investigate disputes
- 3-of-5 mediator consensus required for recommendations
- Mediators earn fee for participation
- **Key distinction:** Mediators recommend, covenant executes autonomously
- Prevents single point of failure (central mediator)
- Maintains regulatory compliance (no entity discretion over funds)

### Automated Evidence Verification
- GPS timestamps (recipient at merchant location?)
- Blockchain analysis (covenant signature timing patterns)
- Machine learning fraud detection (repeat offenders)
- Video verification (facial recognition, cash handover detection)

**When to add:** After 500+ successful transactions, when manual mediation doesn't scale.

**Why this preserves regulatory compliance:**
- Community mediates (investigates, recommends)
- Covenant executes (autonomous code, no human discretion)
- Clear separation: advice vs. control

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
- **Covenant-based architecture update:** Suso + Coordination (May 10, 2026)
- **Critical gap identified:** DeepSeek Review (May 4, 2026)

---

## Architecture Notes

**Key change (May 10, 2026):** Updated from escrow-based dispute resolution to covenant-based mediation.

**Old model:**
- Escrow operator decides distribution
- Entity has discretionary control over funds
- Triggered MiCA CASP licensing requirements

**New model:**
- Covenant executes based on signatures (autonomous code)
- Mediator investigates and recommends (no fund control)
- Covenant timeout provides automatic resolution
- MiCA/PSD2 compliant (no discretionary intermediation)

**Preserved from original design:**
- 3-strike merchant system
- Evidence hierarchy
- Safe confirmation sequencing (now via covenant signatures)
- 24h resolution timeline
- Default favor merchant (scarce resource)

---

**Last updated:** May 10, 2026  
**Status:** Active (MVP, covenant-based)  
**Next review:** After Phase 1 (50+ transactions, first dispute)
