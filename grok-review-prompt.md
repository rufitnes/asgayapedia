# Grok Review Prompt for Asgaya Documentation

## Context

You're being asked to review the complete documentation for **Asgaya**, a permissionless Bitcoin Cash remittance protocol. This is an external review to validate architecture, identify issues, and provide fresh perspective.

**Why your review matters:** You're the first external AI instance to review the updated documentation (May 2026 covenant architecture). DeepSeek provided initial feedback which has been implemented.

---

## What is Asgaya?

**Tagline:** Bitcoin Cash adoption engine disguised as a remittance protocol

**Core value proposition:**
- <1% fees (vs 6.49% traditional remittances)
- No KYC, no custody, self-sovereign
- Covenant-based settlement (overcollateralized BCH)
- Every remittance designed to create a new BCH merchant

**Key innovation:** BCH sellers post 107% collateral to smart contracts (covenants) with EUR-denominated promises. Merchants co-sign after providing cash to recipients. 24-hour timeout cascade if unclaimed.

**Current status:** Documentation phase, seeking external review before implementation

---

## Quick Access Documentation

**Best starting points:**

1. **15-minute overview:**  
   https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/quick-start.txt  
   (500 lines - architecture, flows, key concepts)

2. **Complete documentation:**  
   https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/llms-full.txt  
   (15,250 lines - everything concatenated)

3. **Core architecture only:**  
   https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/core-arch.txt  
   (2,100 lines - design principles and economics)

4. **AI Review Guide:**  
   https://docs.asgaya.org/meta/ai-review-guide/  
   (Explains what we need help with, review branches by expertise)

**Navigation:**
- Main site: https://docs.asgaya.org
- Sitemap: https://docs.asgaya.org/llms.txt
- GitHub: https://github.com/rufitnes/asgayapedia

---

## What We Need From You

### Priority Areas

**🔴 Critical:**
1. **Security vulnerabilities** - Covenant timeout cascade exploits, SMS spoofing, co-signing mechanism
2. **Economic exploits** - Overcollateralization gaming, fee split manipulation, volatility edge cases
3. **Logic errors** - State machine race conditions, refund split bugs

**🟡 Important:**
1. **Inconsistencies** - Contradictions between documents
2. **Missing edge cases** - What happens if...?
3. **Architectural concerns** - Fundamental design issues

**🟢 Medium:**
1. **Usability issues** - User flows, terminology clarity
2. **Documentation gaps** - Concepts not explained well

### What We're NOT Looking For
- ❌ Style/formatting suggestions
- ❌ "Use X tech instead of Y" debates
- ❌ Feature requests beyond MVP scope

---

## Key Architectural Concepts (Read This First!)

### Covenant-Based Settlement
BCH sellers post overcollateralized BCH (107%) to smart contracts that promise EUR-denominated amounts. Contracts execute autonomously—no custody, no intermediation.

### Three-Party Flow
1. **Sender** (Spain) → Pays BCH seller €100 via Bizum
2. **BCH Seller** → Posts ~€107 BCH to covenant, keeps ~€0.50 fee + surplus
3. **Merchant** (Venezuela) → Gives recipient cash, co-signs covenant to claim ~€100.50 BCH
4. **Recipient** → Gets cash from merchant

### 24-Hour Timeout Cascade
- Recipient has 24 hours to claim
- If unclaimed, automatic refund split: merchant portion → sender, seller fee → seller

### Fee Model (2-Way Split)
- BCH Seller: 0.5% (€0.50 on €100)
- Merchant: 0.5% spread (€0.50 on €100)
- No protocol fee = MiCA/PSD2 compliant

---

## Suggested Review Approach

### Option 1: Quick Strategic Review (1-2 hours)
**Goal:** High-level validation of approach and major issues

1. Read quick-start.txt (500 lines, 15 min)
2. Review core-arch.txt section on covenant mechanics (30 min)
3. Skim user flows in `/android-app/flows/` (30 min)
4. Identify top 3-5 concerns (30 min)

**Output:** Brief summary with major issues flagged

### Option 2: Deep Technical Review (3-5 hours)
**Goal:** Comprehensive security and architecture audit

**Pick a branch from AI Review Guide:**
- Branch 1: Security (NotificationListener, SMS spoofing, BCH signatures)
- Branch 2: Economics (overcollateralization incentives, volatility protection)
- Branch 3: Covenant State Machine (race conditions, timeout cascade)
- Branch 4: BCH Protocol (OP_RETURN usage, SPV security)

**Output:** Detailed issue list with severity levels and suggested fixes

### Option 3: Complete Review (Full day+)
**Goal:** Exhaustive documentation validation

1. Read llms-full.txt completely
2. Cross-reference all documents for consistency
3. Test logic flows for edge cases
4. Verify all design decisions have documented rationale

**Output:** Comprehensive review report

---

## Feedback Format

**We prefer specific, actionable feedback:**

### Good Example
```
**Document:** /android-app/backend-apis/transaction-apis.md
**Section:** Covenant State Transitions
**Issue:** Race condition in co-signing flow

**Details:**
If merchant and recipient submit co-signatures simultaneously,
the covenant might process them out of order, potentially
allowing double-claiming.

**Suggested fix:**
Add optimistic locking with transaction IDs to ensure
atomic state transitions.

**Severity:** 🔴 Critical
```

### Less Helpful Example
```
"Security could be better in some areas"
```

---

## Important Context

### Recent Changes (May 9-17, 2026)
- Pivoted from escrow to covenant architecture (MiCA compliance)
- Updated terminology: "LPs" → "BCH Sellers"
- Fee split changed from 3-way (1/3 each) to 2-way (0.5% + 0.5%)
- Implemented DeepSeek feedback (AI review guide rewrite, risk disclosures)

### What DeepSeek Already Found
- ✅ AI review guide was stale (fixed)
- ✅ Risk disclosures needed expansion (fixed)
- ✅ Ecosystem credits incomplete (fixed)
- ✅ Some aspirational claims too strong (softened)

**Your focus:** Find what DeepSeek missed or new issues from recent changes

---

## Questions for Your Review

**Architecture:**
1. Does the overcollateralization mechanism (107%) adequately protect against volatility?
2. Can the 24-hour timeout cascade be exploited?
3. Is the covenant state machine sound?

**Security:**
1. SMS spoofing - is bank shortcode whitelist sufficient?
2. Can merchants game the co-signing mechanism?
3. BCH signature authentication - replay attack risks?

**Economics:**
1. Are BCH sellers sufficiently incentivized to post capital?
2. Can merchants collude with recipients to extract extra value?
3. What happens if BCH drops >7% during the 24-hour window?

**Usability:**
1. Are user flows clear and safe?
2. Terminology - is "BCH Seller" clear to non-crypto users?
3. Error handling - what happens when things go wrong?

---

## Contact & Next Steps

**After your review:**
1. Share feedback via GitHub Issues: https://github.com/rufitnes/asgayapedia/issues
2. Or post in Discussions: https://github.com/rufitnes/asgayapedia/discussions

**We will:**
1. Review your feedback carefully
2. Update documentation based on valid issues
3. Credit you in acknowledgments
4. Iterate until consensus on critical issues

---

## Thank You!

External review is critical for Asgaya's success. Your fresh perspective helps identify blind spots and makes the protocol more robust.

**Note:** This is experimental, unregulated technology operating in legal gray areas. See `/risks-and-disclaimers.md` for full context on regulatory and financial risks.

---

*Ready to review? Start with quick-start.txt and let us know what you find!*
