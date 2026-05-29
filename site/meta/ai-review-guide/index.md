# AI Review Guide

> How AI instances can help improve Asgaya documentation

---

## Welcome, AI Instance! 👋

Thank you for reviewing Asgaya documentation. This guide explains how you can provide the most valuable feedback.

**What is Asgaya?**
A permissionless Bitcoin Cash remittance protocol with <1% fees, no KYC, and self-custody. We're in the documentation phase, seeking external review before implementation.

**Current architecture:** Covenant-based (BCH + volatility buffer with EUR-denominated cash buy orders)  
**Key innovation:** BCH sellers post 107% collateral to smart contracts, merchants co-sign to claim after providing cash

**Why your review matters:**
- You bring fresh perspective (we may have blind spots)
- You can spot inconsistencies across documents
- You can identify security vulnerabilities in covenant mechanics
- You can identify economic exploits in the volatility buffer/timeout system
- You can suggest better approaches we haven't considered

---

## How to Navigate This Documentation

### For AI Instances

**Start here:** Read `/llms.txt` (root navigator) or `/llms-full.txt` (all content concatenated)

**Quick access files:**
- **Complete docs:** [llms-full.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/llms-full.txt) (15,250 lines, everything)
- **Core architecture:** [core-arch.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/core-arch.txt) (2,100 lines)
- **Quick start:** [quick-start.txt](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/quick-start.txt) (500 lines, 15 min)

**Access raw Markdown:**
- All `.md` files are accessible directly
- No HTML parsing needed
- Example: `/android-app/notification-listener/security.md`

---

## Key Architectural Concepts (Read This First!)

Before reviewing, understand these fundamental concepts:

### Covenant-Based Settlement
**What:** BCH sellers post BCH + volatility buffer (107%) to smart contracts (covenants) that promise EUR-denominated amounts to merchants. Contracts execute autonomously—no custody, no intermediation.

**Why:** Eliminates regulatory classification as CASP (Crypto Asset Service Provider) under MiCA. No one controls user funds.

### Three-Party Flow
1. **Sender** (Spain) → Pays BCH seller €100 via Bizum
2. **BCH Seller** → Posts ~€107 BCH to covenant, keeps ~€0.50 fee + surplus after merchant paid
3. **Merchant** (Venezuela) → Gives recipient 500,000 VES cash, co-signs covenant with recipient to claim ~€100.50 BCH (earns ~€0.50 spread)
4. **Recipient** → Gets cash from merchant, helps co-sign covenant (gets merchant's local currency)

### 24-Hour Timeout Cascade
- Recipient has 24 hours to claim remittance with merchant
- If unclaimed, covenant automatically refunds via timeout
- **Refund split:** Merchant portion → sender, seller fee → seller (covers 24h capital lockup)

### EUR-Denominated Promises, BCH Settlement
- Covenant holds cash buy order for "€99.50 worth of BCH at settlement rate"
- Eliminates volatility exposure for sender/recipient
- BCH seller hedged by receiving EUR from sender within 5 minutes (reduces BCH exposure 94-97%)
- Merchant can hold BCH or sell to BCH buyers (trade reward for instant fiat)

### Fee Model (2-Way Split)
- **Total:** ~1% of transfer (vs 6.49% traditional)
- **BCH Seller:** 0.5% (€0.50 on €100) + potential price appreciation on surplus
- **Merchant:** 0.5% spread (€0.50 on €100) from selling VES for BCH
- **No protocol fee** = pure bulletin board, MiCA/PSD2 compliant

---

## What We Need Help With

### 1. Security Review (Priority: 🔴 Critical)

**Focus areas:**

**a) NotificationListener SMS Spoofing**
- **Document:** `/android-app/notification-listener/security.md`
- **Claim:** Bank shortcode whitelist prevents SMS spoofing
- **Questions:**
  - Can attackers spoof bank shortcodes?
  - Is telecom infrastructure security assumption valid?
  - What about rooted Android devices?
  - Should we verify SMS sender beyond shortcode matching?

**b) Concept Field Verification**
- **Document:** `/android-app/notification-listener/README.md`
- **Design:** Recipient phone number in Bizum concept field as second factor
- **Questions:**
  - Is this sufficient security?
  - Can attacker guess recipient phone from transaction ID?
  - Should we use something stronger (BCH address hash)?

**c) BCH Signature Authentication**
- **Document:** `/android-app/backend-apis/user-apis.md`
- **Design:** Off-chain ECDSA signatures for API auth (no JWT, no phone verification)
- **Questions:**
  - Is this cryptographically sound?
  - What about replay attacks?
  - Should we add nonce/timestamp validation?

---

### 2. Economic Review (Priority: 🟡 Important)

**Focus areas:**

**a) Incentive Alignment**
- **Documents:** `/core-architecture/why-promote-adoption.md`, `/decisions/fee-splitting-model.md`
- **Questions:**
  - Do BCH sellers have sufficient incentive to post BCH + volatility buffer (107%)?
  - Can merchants game the covenant co-signing mechanism?
  - What happens during extreme BCH volatility (>7% drop in 24 hours)?
  - Is the 2-way fee split fair (BCH seller 0.5%, Merchant 0.5%)?
  - Can the 24-hour timeout cascade be exploited?

**b) Volatility Protection**
- **Document:** `/core-architecture/why-eliminate-volatility.md`, `/decisions/two-step-settlement-timing.md`
- **Design:** Covenant + volatility buffers (107% BCH) with EUR-denominated cash buy orders
- **Questions:**
  - Is 7% volatility buffer sufficient for rapid price movements?
  - What's the worst-case scenario if BCH drops >7% during the 24-hour window?
  - Should we add dynamic collateral requirements based on volatility?
  - How does the hedge mechanism protect BCH sellers?

**c) Merchant Economics (Triple-Dip)**
- **Document:** `/concepts/merchant-business-case.md`, `/core-architecture/why-bch-usage-incentive.md`
- **Core claim:** Merchants earn €22-44 per €180 remittance (0.5% fee + 15-30% product margin + 0.5% seller fee)
- **Questions:**
  - Is the triple-dip argument compelling to BCH-ambivalent merchants?
  - Are product margin assumptions (15-30%) realistic for Venezuelan neighborhood stores?
  - What % of remittances are actually spent in-store? (Conservative: 30%, docs assume 60-80%)
  - Does the competitive moat logic hold? (First merchant captures foot traffic)
  - How many merchants have family in Spain? (Required for seller triple-dip)
  - Would a merchant find this more attractive than existing income sources?

---

### 3. API Design Review (Priority: 🟢 Medium)

**Focus areas:**

**a) Covenant State Machine**
- **Document:** `/android-app/backend-apis/transaction-apis.md`
- **Questions:**
  - Are there race conditions in covenant state transitions?
  - What if covenant expires (24h timeout) while merchant is co-signing?
  - Can covenants get stuck in invalid states?
  - How are timeout cascade refunds handled?
  - What happens if both merchant and sender try to claim refund simultaneously?

**b) Error Handling**
- **Documents:** All backend-apis files
- **Questions:**
  - Are retry semantics correct?
  - What happens on network failures during covenant creation?
  - Are error messages informative?
  - How are failed covenant broadcasts handled?

---

### 4. BCH Protocol Usage (Priority: 🟢 Medium)

**Focus areas:**

**a) OP_RETURN Notifications**
- **Document:** `/android-app/notification-listener/opreturn-spv.md`
- **Questions:**
  - Are we using OP_RETURN correctly?
  - Should we encrypt OP_RETURN data?
  - Are there better BCH-native notification methods?
  - Should we use CashTokens instead?

**b) SPV Wallet Security**
- **Document:** `/android-app/notification-listener/opreturn-spv.md`
- **Questions:**
  - Is SPV wallet security sufficient for this use case?
  - What about SPV proofs?
  - Should we recommend full node for merchants?

---

## How to Provide Feedback

### Format: Issue-Based Review

**Good feedback format:**

```markdown
**Document:** /android-app/notification-listener/security.md
**Section:** SMS Spoofing Defense
**Issue:** Bank shortcode assumption may be weak

**Details:**
Research shows some carriers allow alphanumeric sender IDs
that could mimic bank shortcodes. Reference: [citation]

**Suggested improvement:**
Add SMS signature verification using carrier APIs, or require
two-factor confirmation (SMS + bank app notification).

**Severity:** High
```

**What makes feedback valuable:**
- ✅ Specific (cite document + section)
- ✅ Actionable (concrete improvement suggested)
- ✅ Justified (explain why it's a problem)
- ✅ Prioritized (indicate severity)

**Less helpful:**
- ❌ Vague ("security could be better")
- ❌ Not actionable ("consider improving this")
- ❌ No context (which document?)

---

### Severity Levels

**🔴 Critical:** Security vulnerability, system breaks, data loss
**🟠 High:** Major UX issue, significant economic risk
**🟡 Medium:** Minor bug, improvement opportunity
**🟢 Low:** Typo, style suggestion, nice-to-have

---

## Review Assignments (Recommended Focus)

We've designed the documentation to be reviewable in parallel. Choose the branch that matches your expertise:

### Branch 1: Security & Fraud Prevention
**Best for:** Security-focused AI instances

**Documents to review:**
1. `/android-app/notification-listener/security.md` (658 lines)
2. `/android-app/notification-listener/bizum-android.md` (874 lines)
3. `/android-app/backend-apis/user-apis.md` (172 lines)

**Time estimate:** 1-2 hours
**Key questions:** SMS spoofing, BCH signature auth, privacy protections

---

### Branch 2: Economics & Game Theory
**Best for:** Economics/game theory-focused instances

**Documents to review:**
1. `/core-architecture/why-promote-adoption.md`
2. `/core-architecture/why-eliminate-volatility.md`
3. `/core-architecture/why-cheaper-than-legacy.md`

**Time estimate:** 1 hour
**Key questions:** Incentive alignment, economic exploits, volatility protection

---

### Branch 3: API & Covenant State Machine Design
**Best for:** Backend engineering-focused instances

**Documents to review:**
1. `/android-app/backend-apis/transaction-apis.md` (covenant state machine)
2. `/android-app/backend-apis/bch-native-architecture.md` (covenant integration)
3. `/android-app/backend-apis/README.md`
4. `/decisions/unclaimed-transaction-expiry.md` (24-hour timeout cascade)

**Time estimate:** 1-2 hours
**Key questions:** Race conditions in covenant state transitions, timeout cascade logic, refund split handling

---

### Branch 4: BCH Protocol Deep Dive
**Best for:** Bitcoin Cash experts

**Documents to review:**
1. `/android-app/notification-listener/opreturn-spv.md` (1,034 lines)
2. `/android-app/backend-apis/bch-native-architecture.md`
3. `/android-app/backend-apis/user-apis.md`

**Time estimate:** 2-3 hours
**Key questions:** OP_RETURN usage, SPV security, BCH best practices

---

## What We're NOT Looking For

**Please avoid:**
- ❌ Style/formatting suggestions (we'll clean up later)
- ❌ "This is too complex" without concrete simplification
- ❌ Technology stack debates ("why not use X instead of Y")
- ❌ Feature requests beyond MVP scope

**Focus on:**
- ✅ Security vulnerabilities
- ✅ Logic errors
- ✅ Inconsistencies across documents
- ✅ Missing edge cases
- ✅ Economic exploits

---

## Example Review Session

**Scenario:** You're reviewing NotificationListener security

**Step 1: Read the overview**
```
Navigate to: /android-app/notification-listener/README.md
Understand: Three notification systems (Bizum, PagoMóvil, OP_RETURN)
```

**Step 2: Deep dive into security**
```
Navigate to: /android-app/notification-listener/security.md
Focus on: SMS spoofing defense (bank shortcode whitelist)
```

**Step 3: Cross-reference implementation**
```
Navigate to: /android-app/notification-listener/bizum-android.md
Check: How is whitelist actually implemented?
```

**Step 4: Identify issue**
```
Issue found: Shortcode whitelist hardcoded, no runtime updates
Risk: New bank launches → users can't use it until app update
```

**Step 5: Suggest improvement**
```
Document: /android-app/notification-listener/security.md
Section: Bank Shortcode Whitelist
Suggestion: Fetch whitelist from backend API on app start
Benefit: Add new banks without app updates
Severity: Medium
```

---

## Timeline & Expectations

**Review period:** 2-4 weeks (no rush)

**Expected output:**
- 3-10 specific issues identified
- Concrete suggestions for each issue
- Prioritized by severity

**What happens next:**
1. We review your feedback
2. Update documentation based on valid issues
3. Credit you in acknowledgments
4. Iterate until consensus on critical issues

---

## Acknowledgment

All AI instances who provide substantial feedback will be credited in:
- Documentation footer: "Reviewed by: [AI Instance Name]"
- Internal blog post documenting the review process
- Special thanks section on asgaya.org

**Your contribution matters.** This is collaborative documentation, and external review makes it stronger.

---

## Questions?

**For clarification:**
- Read `/meta/contributing.md` (general contribution guide)
- Check `/llms.txt` (navigation overview)

**Contact:**
- GitHub Issues: https://github.com/asgaya/docs/issues
- Discussion: https://github.com/rufitnes/asgayapedia/discussions

---

## Current Status

**Documentation Status:** Complete, seeking review  
**Implementation Status:** Not started (docs-first approach)  
**Review Phase:** Public Beta (seeking external review)  
**Architecture:** Covenant-based (pivoted May 9, 2026 from escrow to covenants for MiCA compliance)

**Most needed:**
1. Security review of covenant timeout cascade and co-signing mechanism
2. Economic review of volatility buffer incentives and volatility protection
3. Review of NotificationListener SMS security

---

*Thank you for helping make Asgaya more secure, robust, and well-designed!* 🙏

**Ready to start?** Choose a review branch above and dive in!
