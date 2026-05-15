# Tone Softening - Batch Edit Instructions for Haiku

**Context:** We're systematically softening adversarial language throughout documentation based on DeepSeek's comprehensive audit. These are surgical edits with clear before/after text.

**Philosophy:**
- ✅ Keep: Technical reality (permissionless, distributed, no custody)
- ✅ Focus on: Utility, resilience, open access, pragmatism
- ❌ Remove: Confrontational framing, "us vs them," regulatory defiance

**Execution:** Make each edit exactly as specified. Commit after each file.

---

## FILE 1: docs/core-architecture/why-no-kyc.md

### Edit 1.1 (CRITICAL - Lines 117-124)

**OLD:**
```
**KYC is designed to exclude.** It excludes the poor (who can't get IDs). It excludes migrants (who fear deportation). It excludes dissidents (who need privacy). It excludes refugees (who fled their countries). **Asgaya is designed to include.** If you have a phone, you can receive money. That's it. **Not your papers, not your permission, not your problem.**
```

**NEW:**
```
KYC requirements, however well-intentioned, create barriers. The poor may lack ID. Migrants may fear exposing their status. Refugees may have fled without documents. **Asgaya's approach:** If you have a phone, you can receive money. That's it. **Access through technology, not paperwork.**
```

---

### Edit 1.2 (HIGH - Lines 36-44, section header + content)

**OLD:**
```
### 2. KYC Is Surveillance
```

**NEW:**
```
### 2. KYC Creates Permanent Data Trails
```

**Note:** Also soften the content in this section (lines 37-44) to focus on privacy concerns rather than surveillance framing. Keep factual points about data retention, but remove adversarial tone.

---

### Edit 1.3 (HIGH - Lines 44-49)

**OLD:**
```
### 3. KYC Enables Gatekeeping

With KYC, someone decides who gets access:
- Bank says 'we don't serve your country'
- Exchange says 'your risk profile is too high'
- Regulator says 'this corridor is prohibited'
- **Arbitrary decisions exclude millions**
```

**NEW:**
```
### 3. KYC Limits Access

KYC requirements can exclude people:
- Banks may not serve certain countries
- Exchanges may deny service based on risk profiles
- Some corridors face regulatory restrictions
- **These barriers disproportionately affect those who need access most**
```

---

**Commit message:**
```
Soften adversarial language in why-no-kyc.md

Critical fixes:
- Removed "Not your papers, not your permission" slogan
- Changed "KYC Is Surveillance" → "KYC Creates Permanent Data Trails"
- Changed "KYC Enables Gatekeeping" → "KYC Limits Access"
- Reframed from confrontational to factual barrier description

Maintains substance (KYC excludes people) without adversarial framing.
```

---

## FILE 2: docs/core-architecture/why-self-custody.md

### Edit 2.1 (HIGH - Line 29, section header)

**OLD:**
```
### 3. Custodial Enables Censorship
```

**NEW:**
```
### 3. Custodial Concentrates Control
```

---

### Edit 2.2 (MEDIUM - Lines 29-34)

**OLD:**
```
If Asgaya controlled funds, authorities could compel us to:
- Freeze accounts from certain countries
- Block transactions to certain addresses
- Report all transactions to government
- Deny service to 'high-risk' users
- **We'd become the gatekeepers we're trying to replace**
```

**NEW:**
```
If Asgaya controlled funds, it would have the power to restrict access—something the protocol is designed to avoid. Examples of custodial limitations:
- Account restrictions by jurisdiction
- Transaction filtering by address
- Mandatory reporting requirements
- Risk-based service denial
- **With self-custody, only the user controls their assets**
```

---

### Edit 2.3 (MEDIUM - Lines 50-52)

**OLD:**
```
Censorship resistance:
- No one can stop your transactions
- No one can block your access
- No one can discriminate based on nationality, politics, or beliefs
```

**NEW:**
```
Permissionless access:
- Transactions proceed without central approval
- Access doesn't depend on nationality, politics, or beliefs
- The protocol treats all participants equally
```

---

**Commit message:**
```
Soften adversarial language in why-self-custody.md

Changes:
- "Custodial Enables Censorship" → "Custodial Concentrates Control"
- Removed "gatekeepers we're trying to replace" framing
- "Censorship resistance" → "Permissionless access"
- Focus on architecture benefits vs regulatory confrontation
```

---

## FILE 3: docs/core-architecture/why-permissionless.md

### Edit 3.1 (HIGH - Lines 98-101)

**OLD:**
```
We're not asking permission to build a better financial system. We're just building it.
```

**NEW:**
```
This is an open-source protocol—anyone can participate, anyone can build on it. No gatekeepers, no approvals needed.
```

---

### Edit 3.2 (MEDIUM - Lines 99-100)

**OLD:**
```
Censorship-resistance: No central authority to freeze accounts
```

**NEW:**
```
Resilience: Distributed architecture means no single point of control or failure
```

---

**Commit message:**
```
Soften adversarial language in why-permissionless.md

Changes:
- Removed "We're not asking permission... We're just building it"
- "Censorship-resistance" → "Resilience"
- Focus on open-source collaboration vs regulatory defiance
```

---

## FILE 4: docs/core-architecture/why-market-rate-exchanges.md

### Edit 4.1 (HIGH - Lines 25-32, section header)

**OLD:**
```
### 2. Bypassing Financial Repression
```

**NEW:**
```
### 2. Accessing Market-Driven Exchange Rates
```

---

### Edit 4.2 (HIGH - Lines 30-32)

**OLD:**
```
**Asgaya (escapes control):**
EUR → Exchange (global market) → BCH → Covenant → Local market → Local currency
Result: Market determines value, government can't intercept
```

**NEW:**
```
**Asgaya flow:**
EUR → Exchange (global market) → BCH → Covenant → Local market → Local currency
Result: Market rates determine value throughout, without intermediary markups
```

---

### Edit 4.3 (LOW - Line 36)

**OLD:**
```
**ZERO flexibility on exchange rates**
```

**NEW:**
```
**Exchange rate integrity is non-negotiable**
```

---

**Commit message:**
```
Soften adversarial language in why-market-rate-exchanges.md

Changes:
- "Bypassing Financial Repression" → "Accessing Market-Driven Exchange Rates"
- "Escapes control... government can't intercept" → Market rates without markups
- "ZERO flexibility" → "Exchange rate integrity is non-negotiable"
- Focus on market efficiency vs government evasion
```

---

## FILE 5: docs/decisions/how-exchange-rates-work.md

### Edit 5.1 (HIGH - Line 8)

**OLD:**
```
rates with zero markup, bypassing both private company spreads and government-imposed rate manipulation
```

**NEW:**
```
rates with zero markup, avoiding both private company spreads and official exchange rate constraints
```

---

### Edit 5.2 (MEDIUM - Lines 98-103, diagram labels)

**Find and replace all instances of "Bypass" in the flow diagram:**

- "Bypass via Bizum P2P" → "Access market rate via Bizum P2P"
- "Bypass via P2P cash market" → "Access market rate via P2P cash market"
- "Sender bypasses official EUR/VES rate" → "Sender accesses market EUR/VES rate"
- "Recipient bypasses official VES rate" → "Recipient receives at market VES rate"
- "Merchant bypasses capital controls" → "Merchant holds BCH, outside currency restrictions"

---

**Commit message:**
```
Soften adversarial language in how-exchange-rates-work.md

Changes:
- "bypassing... government-imposed rate manipulation" → "avoiding... official rate constraints"
- Five instances of "Bypass" in flow diagram → "Access/Receive market rate"
- Focus on market efficiency vs regulatory evasion framing
```

---

## FILE 6: docs/core-architecture/why-promote-adoption.md

### Edit 6.1 (MEDIUM - Line 7)

**OLD:**
```
**Ideology alone has failed to spread the use of Bitcoin Cash.**
```

**NEW:**
```
**Technical benefits alone haven't driven widespread adoption.**
```

---

### Edit 6.2 (MEDIUM - Line 18)

**OLD:**
```
They won't adopt BCH out of ideology—they need to make more money by accepting it than by refusing it.
```

**NEW:**
```
They need a clear business case—accepting BCH must be more profitable than refusing it.
```

---

**Commit message:**
```
Soften dismissive "ideology" language in why-promote-adoption.md

Changes:
- "Ideology alone has failed" → "Technical benefits alone haven't driven adoption"
- "out of ideology" → "clear business case"
- Maintain pragmatic focus without dismissing community efforts
```

---

## FILE 7: docs/core-architecture/why-bch-usage-incentive.md

### Edit 7.1 (MEDIUM - Lines 101-102)

**OLD:**
```
**Ideology has failed to spread BCH adoption.**
**Economics won't fail.**
```

**NEW:**
```
**Technical merit alone hasn't driven widespread adoption.**
**Clear economic incentives create a stronger foundation.**
```

---

**Commit message:**
```
Soften dismissive "ideology" language in why-bch-usage-incentive.md

Changes:
- "Ideology has failed" → "Technical merit alone hasn't driven adoption"
- "Economics won't fail" → "Clear economic incentives create stronger foundation"
- Less overconfident, maintains pragmatic argument
```

---

## FILE 8: docs/ROADMAP.md

### Edit 8.1 (LOW - Line 83)

**OLD:**
```
More censorship-resistant
```

**NEW:**
```
More resilient notification delivery
```

**Alternative (if in context of OP_RETURN):**
```
OP_RETURN-based notifications (redundant to push notifications)
```

---

**Commit message:**
```
Soften adversarial language in ROADMAP.md

Change: "censorship-resistant" → "resilient notification delivery"
Focus on technical reliability vs regulatory confrontation.
```

---

## Summary of Changes

**Total files to edit:** 8
**Total edits:** 18 changes across 8 files
**Estimated time:** 20-30 minutes (surgical edits with clear specs)

**Priority order (already sorted above):**
1. why-no-kyc.md (CRITICAL slogan + 2 HIGH)
2. why-self-custody.md (3 changes)
3. why-permissionless.md (2 changes)
4. why-market-rate-exchanges.md (3 changes)
5. how-exchange-rates-work.md (6 changes - find/replace cluster)
6. why-promote-adoption.md (2 changes)
7. why-bch-usage-incentive.md (1 change)
8. ROADMAP.md (1 change)

**Final step:** After all edits complete, report back with summary of changes made.

---

## Notes for Haiku

- Use exact text replacements as specified
- Commit after each file (8 commits total)
- If any text doesn't match exactly, report the mismatch (don't guess)
- Preserve all other formatting, markdown, structure
- These are tone changes only - no technical content changes

**Philosophy recap:** We're communicating the SAME technical reality (permissionless, distributed, no custody, market rates) but focusing on utility and resilience rather than regulatory confrontation.

Ready to execute? Start with FILE 1 (why-no-kyc.md) - the critical one.
