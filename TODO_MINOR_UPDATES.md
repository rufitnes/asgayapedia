# TODO: Minor Covenant Architecture Updates

**Status:** 9 major files complete, ~40-50 files need minor terminology updates  
**Date:** 2026-05-10  
**Next Step:** Haiku batch updates (find/replace operations)

---

## Completed ✅

### Major Files (11 commits)
1. merchant-flows.md
2. recipient-flows.md  
3. remittance-merchant-cash-out.md
4. README.md
5. ROADMAP.md
6. settlement-apis.md (archived)
7. why-cheaper-than-legacy.md
8. why-no-kyc.md
9. why-promote-adoption.md

---

## Remaining Minor Updates

### Quick Find/Replace Operations

**Pattern 1: "escrow" → "covenant" or "BCH seller" (context-dependent)**
- Check each file for context before replacing
- "Escrow operator" → "BCH seller"
- "Escrow holds EUR" → "BCH seller receives Bizum"
- Historical references (OK to keep): "old escrow model"

**Pattern 2: "LP" / "Liquidity Provider" → "BCH Buyer" (context-dependent)**
- In instant settlement context → "BCH Buyer (optional)"
- "LP provides liquidity" → "BCH buyer offers to buy BCH"
- "LP earns fees" → "BCH buyer acquires BCH at market rate"

**Pattern 3: "Kraken" references**
- "Kraken purchase" → Remove or update to "covenant settlement"
- "Kraken fee" → Remove (no exchange fee)
- Research files with Kraken → Keep (historical context)

---

## Files by Priority

### HIGH PRIORITY (Core Concepts - Users Read First)

**Concepts:**
- [ ] concepts/pull-system.md (already mostly updated, verify)
- [ ] concepts/market-making-partners.md (LP references)
- [ ] concepts/dynamic-reward-modulation.md (LP/escrow references)
- [ ] concepts/bubble-prevention.md (escrow references)
- [ ] concepts/live-exchange-rates.md (escrow/Kraken references)
- [ ] concepts/README.md (overview, update references)

**Decisions:**
- [ ] decisions/fee-splitting-model.md (already mostly updated, verify)
- [ ] decisions/unclaimed-transaction-expiry.md (escrow references)
- [ ] decisions/two-step-settlement-timing.md (escrow/Kraken references)
- [ ] decisions/dispute-resolution.md (escrow references)
- [ ] decisions/payment-timeout-window.md (escrow references)
- [ ] decisions/bizum-concept-field.md (escrow references)
- [ ] decisions/README.md (overview, update references)

**Core Architecture:**
- [ ] core-architecture/why-market-rate-exchanges.md (Kraken references)
- [ ] core-architecture/why-permissionless.md (escrow references)
- [ ] core-architecture/why-self-custody.md (escrow references)
- [ ] core-architecture/why-minimal-hardware.md (escrow references)
- [ ] core-architecture/README.md (overview, update references)

### MEDIUM PRIORITY (Technical Docs)

**Android App:**
- [ ] android-app/flows/bch-payment-flows.md (escrow references)
- [ ] android-app/flows/lp-flows.md (LP flows - may need archiving)
- [ ] android-app/flows/README.md (overview, update references)

**Backend APIs:**
- [ ] android-app/backend-apis/transaction-apis.md (escrow/LP references)
- [ ] android-app/backend-apis/merchant-apis.md (escrow references)
- [ ] android-app/backend-apis/user-apis.md (escrow references)
- [ ] android-app/backend-apis/rate-apis.md (escrow/Kraken references)
- [ ] android-app/backend-apis/leaderboard-apis.md (LP references)
- [ ] android-app/backend-apis/bch-native-architecture.md (escrow references)
- [ ] android-app/backend-apis/README.md (overview)

**Notification Listener:**
- [ ] android-app/notification-listener/bizum-android.md (escrow references)
- [ ] android-app/notification-listener/pagomovil-android.md (escrow/LP references)
- [ ] android-app/notification-listener/opreturn-spv.md (LP references)
- [ ] android-app/notification-listener/security.md (escrow references)
- [ ] android-app/notification-listener/testing.md (escrow references)
- [ ] android-app/notification-listener/README.md (overview)

### LOW PRIORITY (Research/Historical)

**Research Files (Keep for historical context):**
- [ ] research/RS016_EUR_BCH_exchanges.md (Kraken - OK to keep)
- [ ] research/RS017_kraken.md (historical - OK to keep)
- [ ] research/RS018_kraken_setup.md (historical - OK to keep)
- [ ] research/RS019_kraken_query.md (historical - OK to keep)
- [ ] research/RS036_kraken_ticker_api.md (historical - OK to keep)
- [ ] research/RS044_kraken_trading_withdrawal.md (historical - OK to keep)
- [ ] research/RS045_kraken_complete_fee_analysis.md (historical - OK to keep)
- [ ] research/RS042_bizum_concept_field_constraints.md (escrow refs)
- [ ] research/RS047_dolarapi_venezuela_rates.md (escrow refs)
- [ ] research/RS010_Honduras.md (escrow refs)

**Meta/Other:**
- [ ] meta/deepseek-review-2026-05-04.md (escrow references - historical)
- [ ] meta/review-action-plan.md (escrow references)
- [ ] glossary.md (LP/escrow definitions)
- [ ] risks-and-disclaimers.md (escrow references)

---

## Haiku Instructions

**For each file:**

1. **Read the file** (use Read tool with appropriate offset/limit)
2. **Identify context** of each escrow/LP/Kraken reference
3. **Update appropriately:**
   - Escrow operator → BCH seller
   - LP/Liquidity Provider → BCH Buyer (optional)
   - Kraken purchase → Covenant settlement
   - Keep historical context (e.g., "old escrow model")
4. **Commit with clear message** explaining changes

**Example commit message:**
```
Update [filename] for covenant architecture

Changed:
- "escrow" → "BCH seller" (5 instances)
- "LP" → "BCH Buyer" (3 instances)
- Removed Kraken purchase references (2 instances)

Kept historical context where appropriate.
```

**Batch strategy:**
- Group similar files (e.g., all concept files in one commit)
- Or do individual commits for traceability
- Prioritize high-priority files first

---

## Verification Checklist

After updates, verify:
- [ ] No "escrow operator" references (except historical)
- [ ] No "LP provides liquidity" (except historical/archived)
- [ ] No "Kraken purchase" in active documentation
- [ ] All links still work (check decision/concept cross-references)
- [ ] Terminology consistent across all files

---

## Notes for Haiku

**Context-aware replacements:**
- "Escrow" in historical context → Keep with note "(old model)"
- "LP" in archived settlement-apis.md → Already archived, OK
- "Kraken" in research files → Keep (historical research)

**Don't update:**
- Archived files (already marked as superseded)
- Historical research (RS0xx files - marked as research)
- Comments explaining old vs new model

**Do update:**
- Active user-facing documentation
- Core architecture explanations
- Current API specifications
- Active flow documentation

---

**Total estimated time:** 2-3 hours for Haiku (batch operations)  
**Total estimated commits:** 15-25 (depending on batching strategy)
