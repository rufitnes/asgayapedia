# Review Action Plan

**Created:** May 4, 2026  
**Based on:** [DeepSeek Review](meta/deepseek-review-2026-05-04.md)  
**Status:** ✅ Critical Items Complete! (May 5, 2026)  
**Next:** Warning items (May 6-11)

---

## Priority Matrix

| Priority | Count | Timeline | Status |
|----------|-------|----------|--------|
| 🔴 Critical | 4 | Days 1-3 | ✅ **COMPLETE** (May 5) |
| ⚠️ Warning | 7 | Days 4-7 | ⏳ Pending |
| 💡 Improvement | 2 | Post-critical | ⏳ Pending |

---

## 🔴 Critical Items (Days 1-3)

### 1. ✅ Fee Split Documentation Inconsistency (COMPLETE)
**Impact:** User confusion about earnings  
**Effort:** 1 hour (actual)  
**Owner:** Coordination  
**Completed:** May 5, 2026

**Current Problem:**
- Some docs say "1% split three ways" (implies €0.333 each)
- Correct formula: `1% = Kraken fee (~0.26%) + Equal split (~0.74% ÷ 3 = €0.247 each)`

**Solution:**
1. ✅ **Update docs/README.md:** Add fee breakdown in "What is Asgaya?" section
2. ✅ **Update core-architecture/why-cheaper-than-legacy.md:** Clarify Kraken deduction
3. ✅ **Update decisions/fee-splitting-model.md:** Ensure formula is prominent
4. ✅ **Update glossary.md:** Fee structure entry with clear breakdown

**Files to update:**
- `docs/README.md`
- `core-architecture/why-cheaper-than-legacy.md`
- `decisions/fee-splitting-model.md`
- `glossary.md`

**Verification:** Search all docs for "1%" and ensure consistent messaging

---

### 2. ✅ Dynamic Reward Modulation Contradiction (COMPLETE)
**Impact:** Architectural confusion  
**Effort:** 30 minutes (actual)  
**Owner:** Coordination  
**Completed:** May 5, 2026

**Current Problem:**
- `decisions/fee-splitting-model.md` says "no dynamic splits in V1.0"
- `concepts/dynamic-reward-modulation.md` implies it's a live mechanism

**Solution:**
Add prominent callout to `concepts/dynamic-reward-modulation.md`:

```markdown
> ⚠️ **STATUS: POST-MVP FEATURE (V1.1+)**
>
> Dynamic Reward Modulation is NOT part of the initial implementation (Phase 0-2).
> V1.0 uses fixed equal splits for simplicity and predictability.
> This concept documents a future enhancement for consideration after validating
> the core protocol with real users.
>
> See [Fee Splitting Model](../decisions/fee-splitting-model.md) for the V1.0 approach.
```

**Files to update:**
- `concepts/dynamic-reward-modulation.md` (add status callout at top)

**Verification:** Ensure concepts/README.md also clarifies which concepts are active vs future

---

### 3. ✅ Unclaimed Transaction Expiry (COMPLETE)
**Impact:** Funds could lock permanently  
**Effort:** 4 hours (actual)  
**Owner:** Suso + Coordination  
**Completed:** May 5, 2026

**Current Problem:**
- No documented process for when recipient never claims
- EUR would remain in escrow indefinitely

**Solution:**
Create `decisions/unclaimed-transaction-expiry.md`:

**Proposed Policy:**
- **Claim window:** 7 days from escrow funding
- **After expiry:** Automatic refund to sender
- **Refund deduction:** Small processing fee (€0.50 or 0.5%, whichever is greater)
- **Notification:** Sender receives refund notification with explanation

**API Changes Needed:**
- Add `claim_expires_at` timestamp to transaction
- Add `refund_initiated` state
- Add `GET /api/v1/transactions/:id/extend-expiry` endpoint (optional, for special cases)

**User Flow:**
1. Day 0: Sender funds escrow, recipient gets notification
2. Day 3: Reminder notification to recipient
3. Day 6: Final warning notification
4. Day 7 + 1 hour: Auto-refund initiated
5. Sender receives refunded EUR minus processing fee

**Files to create:**
- `decisions/unclaimed-transaction-expiry.md`

**Files to update:**
- `android-app/backend-apis/transaction-apis.md` (add expiry states)
- `android-app/flows/remittance-merchant-cash-out.md` (document expiry UX)
- `android-app/flows/recipient-flows.md` (add reminder notifications)

---

### 4. ✅ Dispute Resolution Framework (COMPLETE)
**Impact:** No process for merchant/recipient conflicts  
**Effort:** 8 hours (actual)  
**Owner:** Suso + Coordination  
**Completed:** May 5, 2026

**Current Problem:**
- `escrow_intervention` state exists but no process documented
- Critical questions unanswered:
  - Who decides disputes?
  - What evidence is required?
  - Appeal process?
  - How are funds held?

**Solution:**
Create `decisions/dispute-resolution.md`:

**Proposed Framework (MVP):**

**Phase 0-1 (Trusted Escrow):**
- Escrow operator manually reviews flagged transactions
- Evidence required: Screenshots, timestamps, GPS data (optional)
- Decision timeline: 48 hours
- Appeal: Email to escrow operator (one appeal allowed)
- Funds: Frozen during review, released based on decision

**Phase 2+ (Decentralized):**
- 3-of-5 multisig escrow operators vote
- Majority decision (2+ votes) wins
- Tie → Refund sender (conservative approach)
- Evidence published to IPFS for transparency
- Appeal: Request re-vote with new evidence (one time)

**Common Scenarios:**
1. **Merchant confirms, recipient disputes:** Evidence review (payment screenshot, merchant logs)
2. **Recipient confirms, merchant disputes:** Rare, but treat as merchant error
3. **Neither confirms:** After 24h, refund sender minus processing fee
4. **Both confirm but dispute amount:** Evidence review, partial refund if justified

**Fraud Detection:**
- Track dispute rate per user (>20% = flagged)
- Merchant/Recipient with 3+ lost disputes = temporarily suspended
- Pattern detection: Same users disputing repeatedly

**Files to create:**
- `decisions/dispute-resolution.md`

**Files to update:**
- `android-app/backend-apis/settlement-apis.md` (add dispute endpoints)
- `android-app/backend-apis/transaction-apis.md` (add dispute states)
- `android-app/flows/merchant-flows.md` (dispute flow)
- `android-app/flows/recipient-flows.md` (dispute flow)
- `ROADMAP.md` (add dispute resolution to Phase 0 checklist)

**Open Questions for Community:**
- Should disputes be public (transparency) or private (privacy)?
- Should there be a dispute fee (prevents frivolous disputes)?
- Should reputation scores affect dispute handling?

---

## ⚠️ Warning Items (Days 4-7)

### 5. LP Liquidity Exhaustion
**Impact:** Remittances blocked during surge  
**Effort:** Medium (2-3 hours)

**Proposed Solution:**
- Fallback to direct merchant settlement (no instant settlement option)
- Notification to sender: "Instant settlement unavailable, standard settlement (24h)"
- Track exhaustion events to plan LP recruitment

**Files to update:**
- `android-app/backend-apis/settlement-apis.md`
- `android-app/flows/lp-flows.md`

---

### 6. Merchant Cash Liquidity
**Impact:** Wasted recipient trips  
**Effort:** Low (1-2 hours)

**Proposed Solution:**
- Add `cash_available: boolean` to merchant profile
- Merchant can toggle via app
- Recipient sees "Cash unavailable" badge in merchant list
- Push notification if selected merchant goes unavailable

**Files to update:**
- `android-app/backend-apis/merchant-apis.md`
- `android-app/flows/merchant-flows.md`
- `android-app/flows/recipient-flows.md`

---

### 7. BCH Float Depletion
**Impact:** Operational risk in Phase 0  
**Effort:** Medium (2-3 hours)

**Proposed Solution:**
- Document initial float funding (€10,000 worth of BCH recommended)
- Alert at 20% remaining
- Emergency pause new transactions if <10%
- SEPA replenishment process documented

**Files to update:**
- `ROADMAP.md` (Phase 0 requirements)
- New file: `decisions/escrow-float-management.md`

---

### 8. Corridor Expansion Criteria
**Impact:** Unclear expansion decisions  
**Effort:** Low (1 hour)

**Proposed Checklist:**
- ✅ Minimum 5 merchants committed
- ✅ Local payment rail identified (PagoMóvil equivalent)
- ✅ Exchange rate API available (DolarAPI equivalent)
- ✅ Regulatory environment researched
- ✅ Community interest validated (survey or forum)
- ✅ 1+ LP committed with local currency

**Files to update:**
- `ROADMAP.md` (add corridor expansion section)

---

### 9. API Security Documentation
**Impact:** Security concerns unaddressed  
**Effort:** Medium (3-4 hours)

**Proposed Content:**
- Replay attack prevention (nonce + timestamp)
- Rate limiting (per IP, per user)
- Input validation standards
- TLS 1.3 required
- CORS policy
- API key management

**Files to create:**
- `android-app/backend-apis/security.md`

**Files to update:**
- `android-app/backend-apis/README.md` (link to security)

---

### 10. Bizum DoS/Harassment Vector
**Impact:** Privacy/harassment vulnerability  
**Effort:** Low (1 hour)

**Proposed Mitigation:**
- Rate limit: Max 3 Bizum notifications per phone number per day
- "Pause notifications" toggle in recipient app
- Auto-pause after 5 unclaimed remittances from different senders
- Report spam feature

**Files to update:**
- `decisions/bizum-concept-field.md` (add "Known Risks" section)
- `android-app/flows/recipient-flows.md` (pause feature)

---

### 11. Phase 0 Centralization
**Impact:** None (already documented)  
**Status:** ✅ No action needed

---

## 💡 Improvement Items (Post-Critical)

### 12. 5-Minute Quickstart
**Impact:** Easier onboarding  
**Effort:** Medium (2-3 hours)

**Proposed Content:**
Single-page narrative:
> María in Madrid wants to send €100 to her sister Elena in Caracas...

**Files to create:**
- `docs/quickstart.md`

**Files to update:**
- `docs/README.md` (link to quickstart)
- `docs/_sidebar.md` (add quickstart)

---

### 13. LP Role in Summary
**Impact:** More complete overview  
**Effort:** Very Low (15 minutes)

**Proposed Addition to docs/README.md:**
> **Liquidity Providers (LPs)** compete to offer instant settlement by fronting local currency to merchants, earning a share of the 1% fee for fast execution.

**Files to update:**
- `docs/README.md`

---

## Implementation Timeline

### Week 1 (May 4-11, 2026)

**Days 1-3: Critical Items**
- Day 1 AM: #1 Fee split clarity (Coordination)
- Day 1 PM: #2 Dynamic reward modulation (Coordination)
- Day 2: #3 Unclaimed transaction expiry (Suso + Coordination)
- Day 3: #4 Dispute resolution framework (Suso + Coordination)

**Days 4-5: High-Priority Warnings**
- Day 4: #5 LP liquidity exhaustion, #6 Merchant cash liquidity
- Day 5: #7 BCH float depletion, #8 Corridor expansion criteria

**Days 6-7: Security & Polish**
- Day 6: #9 API security documentation
- Day 7: #10 Bizum DoS mitigation, #12 Quickstart, #13 LP summary

---

## GitHub Issues

After completing critical items (1-4), open GitHub issues for community input:

**Template:**
```markdown
## [Issue #X] [Category] Title

**Priority:** 🔴 Critical / ⚠️ Warning / 💡 Improvement  
**From:** DeepSeek Review (May 4, 2026)  
**Reference:** [docs/meta/deepseek-review-2026-05-04.md](docs/meta/deepseek-review-2026-05-04.md)

### Problem
[Description from review]

### Proposed Solution
[Our proposed approach]

### Open Questions
[Community input needed]

### Related Files
- [ ] File 1
- [ ] File 2
```

---

## Success Criteria

**Critical items complete when:**
- ✅ All 4 critical gaps documented with clear policies
- ✅ Updated files pass consistency check (no contradictions)
- ✅ New decisions linked from relevant flows/APIs
- ✅ Changes pushed to GitHub, deployed to GitHub Pages

**Warning items complete when:**
- ✅ Policies documented or risks acknowledged
- ✅ Fallback mechanisms defined
- ✅ Operational procedures clear

**Ready for next review when:**
- ✅ All critical + high-priority items addressed
- ✅ GitHub issues opened for community input
- ✅ Documentation updated and deployed
- ✅ DeepSeek invited to review changes

---

## Team Assignments

**Suso:**
- Policy decisions (expiry window, dispute framework)
- Corridor expansion criteria
- Float management strategy

**Coordination:**
- Documentation updates
- Consistency checks
- GitHub issue creation
- Deployment

**DeepSeek:**
- Review completed changes
- Comment on GitHub issues
- Suggest refinements

---

## Progress Tracking

| # | Item | Status | Owner | ETA |
|---|------|--------|-------|-----|
| 1 | Fee split clarity | ⏳ Pending | Coordination | May 5 |
| 2 | Dynamic reward | ⏳ Pending | Coordination | May 5 |
| 3 | Unclaimed expiry | ⏳ Pending | Suso + Coord | May 6 |
| 4 | Dispute resolution | ⏳ Pending | Suso + Coord | May 7 |
| 5 | LP exhaustion | ⏳ Pending | Coordination | May 8 |
| 6 | Merchant cash | ⏳ Pending | Coordination | May 8 |
| 7 | BCH float | ⏳ Pending | Suso | May 9 |
| 8 | Corridor criteria | ⏳ Pending | Suso | May 9 |
| 9 | API security | ⏳ Pending | Coordination | May 10 |
| 10 | Bizum DoS | ⏳ Pending | Coordination | May 10 |
| 12 | Quickstart | ⏳ Pending | Coordination | May 11 |
| 13 | LP summary | ⏳ Pending | Coordination | May 11 |

---

**Last Updated:** May 4, 2026  
**Next Review:** May 11, 2026
