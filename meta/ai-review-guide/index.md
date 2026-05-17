# AI Review Guide

> How AI instances can help improve Asgaya documentation

---

## Welcome, AI Instance! 👋

Thank you for reviewing Asgaya documentation. This guide explains how you can provide the most valuable feedback.

**What is Asgaya?**
A permissionless Bitcoin Cash remittance protocol with <1% fees, no KYC, and self-custody. We're in the documentation phase, seeking external review before implementation.

**Why your review matters:**
- You bring fresh perspective (we may have blind spots)
- You can spot inconsistencies across documents
- You can identify security vulnerabilities
- You can suggest better approaches we haven't considered

---

## How to Navigate This Documentation

### For AI Instances

**Start here:** Read `/llm.txt` (root navigator)

**Navigation path example:**
```
/llm.txt
  → /android-app/llm.txt
    → /android-app/notification-listener/llm.txt
      → /android-app/notification-listener/security.md
```

**Access raw Markdown:**
- All `.md` files are accessible directly
- No HTML parsing needed
- Example: `/android-app/notification-listener/security.md`

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
- **Documents:** `/core-architecture/why-promote-adoption.md`
- **Questions:**
  - Do merchants have sufficient incentive to provide liquidity?
  - Can LPs game the system?
  - What happens during extreme BCH volatility?
  - Are the 1/3 splits fair to all parties?

**b) Volatility Protection**
- **Document:** `/core-architecture/why-eliminate-volatility.md`
- **Design:** Pull system (buy BCH AFTER confirmations)
- **Questions:**
  - Is this sufficient for rapid price movements?
  - What's the worst-case scenario?
  - Should we add price limits/circuit breakers?

---

### 3. API Design Review (Priority: 🟢 Medium)

**Focus areas:**

**a) State Machine**
- **Document:** `/android-app/backend-apis/transaction-apis.md`
- **Questions:**
  - Are there race conditions?
  - What if two users submit same transaction simultaneously?
  - Can transactions get stuck in invalid states?
  - Are rollback scenarios handled?

**b) Error Handling**
- **Documents:** All backend-apis files
- **Questions:**
  - Are retry semantics correct?
  - What happens on network failures?
  - Are error messages informative?

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

### Branch 3: API & State Machine Design
**Best for:** Backend engineering-focused instances

**Documents to review:**
1. `/android-app/backend-apis/transaction-apis.md` (400 lines)
2. `/android-app/backend-apis/settlement-apis.md` (300 lines)
3. `/android-app/backend-apis/README.md`

**Time estimate:** 1-2 hours
**Key questions:** Race conditions, retry logic, state transitions

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
- Check `/llm.txt` (navigation overview)

**Contact:**
- GitHub Issues: https://github.com/asgaya/docs/issues
- Discussion: https://github.com/asgaya/docs/discussions

---

## Current Status

**Documentation Status:** Complete, seeking review
**Implementation Status:** Not started (docs-first approach)
**Review Phase:** Open (started 2026-04-28)

**Most needed:** Security review of NotificationListener architecture

---

*Thank you for helping make Asgaya more secure, robust, and well-designed!* 🙏

**Ready to start?** Choose a review branch above and dive in!
