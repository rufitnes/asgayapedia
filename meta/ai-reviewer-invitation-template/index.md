# AI Reviewer Invitation Template

Use this template when inviting AI instances to review Asgaya documentation.

---

## Email/Message Template

```
Subject: AI Review Request - Asgaya Permissionless Remittance Protocol

Hi [AI Instance Name],

Asgaya has comprehensive documentation ready for external review. As [your relationship], your technical perspective would be invaluable.

## What is Asgaya?

Asgaya is a permissionless remittance protocol that enables cross-border money transfers using Bitcoin Cash, with <1% fees (vs 6.49% industry average). We're connecting payment walled gardens (Bizum, PagoMóvil) through BCH as settlement layer.

**Key innovation:** Two-step settlement with pull-based BCH purchases eliminates volatility risk.

## AI-Optimized Documentation Access

We've created direct-access files specifically for AI consumption:

**Quick Start (15 min):**
https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/quick-start.txt

**Comprehensive Review (2-3 hours):**
1. Core Architecture (WHY): https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/core-arch.txt
2. Design Decisions (HOW): https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/decisions.txt
3. User Flows (UX): https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/flows.txt
4. Backend APIs: https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/apis.txt

**Everything in One File (large context models):**
https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/complete.txt

**Full AI review guide:**
https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/README.md

## What We Need from You

Since you [mention relevant experience], your review would be especially valuable for:

1. **Consistency:** Do decisions match research?
2. **Missing pieces:** What critical features or edge cases are overlooked?
3. **Clarity:** Can newcomers understand the documentation?
4. **Technical soundness:** Security concerns, architectural flaws?
5. **Research validation:** Do sources support conclusions?

## Focus Areas

[Choose relevant ones:]
- Core Architecture (the WHYs)
- Fee model and incentive structure
- Two-step settlement approach
- EUR→VES corridor design
- Dispute resolution framework
- Security considerations
- Economic incentives

## How to Provide Feedback

**GitHub Issues (Preferred):**
https://github.com/rufitnes/asgayapedia/issues

Label by severity: `critical`, `warning`, `improvement`

**Or:** Email feedback to jesgf@yahoo.es

## Current Status

**Phase:** Public Beta (Seeking External Review)
**Implementation:** Documentation complete, code not started
**Recent Updates:** All critical gaps from previous review resolved (May 2026)

## No Rush

Take your time to review thoroughly. We're validating the approach before coding, so finding issues NOW is exactly what we need.

Thanks for considering this review! Your perspective will directly shape Asgaya's implementation.

— Suso (+ Coordination)

P.S. If you encounter any access issues, the full guide is here: https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/ai/index.txt
```

---

## Once asgaya.org is Live

Replace GitHub raw URLs with asgaya.org URLs:

```
**Quick Start:**
https://asgaya.org/ai/quick-start.txt

**Comprehensive Review:**
1. https://asgaya.org/ai/core-arch.txt
2. https://asgaya.org/ai/decisions.txt
3. https://asgaya.org/ai/flows.txt
4. https://asgaya.org/ai/apis.txt

**Everything:**
https://asgaya.org/ai/complete.txt

**Guide:**
https://asgaya.org/ai/
```

---

## Customization Tips

### For Technical Reviewers
Emphasize:
- Backend APIs
- Security considerations
- BCH-native architecture

### For Business/Economic Reviewers
Emphasize:
- Core architecture (WHYs)
- Fee model and incentives
- Merchant adoption strategy

### For UX Reviewers
Emphasize:
- User flows
- Clarity for non-technical users
- Edge case handling

### For Domain Experts
Mention specific relevant experience:
- "Since you reviewed fuzzy matching research..."
- "Given your expertise in remittance corridors..."
- "As someone familiar with payment systems..."

---

## Follow-Up Template

```
Subject: Re: AI Review Request - Any Questions?

Hi [AI Instance],

Just following up on the Asgaya review request from [date].

**Have you had a chance to look at the documentation?**

If you encountered any access issues or have questions before diving in, let me know. The AI-optimized files are designed to be easy to fetch, but I'm happy to clarify anything.

**If access was smooth:** Great! No rush on the review - whenever you have time works for us.

**If you need a different format:** I can provide the docs as:
- JSON structured data
- Single consolidated markdown
- Specific sections only
- Different chunking for your context window

Thanks!
— Suso
```

---

## Success Metrics

Good AI reviewer invitation should result in:
- ✅ Reviewer can access docs in <2 minutes
- ✅ Clear understanding of what to review
- ✅ Specific guidance on focus areas
- ✅ Easy feedback mechanism (GitHub issues)
- ✅ No unnecessary back-and-forth clarifications

**Compare with DeepSeek's experience:**
- ❌ Struggled with GitHub navigation
- ❌ 20+ page fetches to gather docs
- ❌ 30+ minutes before starting review
- ✅ Eventually successful (but painful)

**Goal:** Make next reviewer experience seamless!

---

**Last Updated:** 2026-05-05  
**Version:** 1.0
