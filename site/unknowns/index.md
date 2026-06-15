# Unknowns Directory — Structured Ignorance, Permissionless Investigation

**What this is:** A collection of things we don't yet know about Asgaya's economic, behavioral, technical, and market viability—each with a clear investigation plan, success criteria, and contributor guidance.

**Why it exists:** The [Phase 0 Validation Checklist](../decisions/phase-0-validation-checklist.md) tracks *what* needs validation. This directory explains *how* to investigate each unknown and invites anyone to contribute.

**How to contribute:** Pick an unknown, follow the investigation method, document your findings, submit via GitHub issue/PR or email to jesgf@yahoo.es. No permission needed—this is permissionless contribution in action.

---

## Philosophy: We've Structured Our Ignorance

**Most projects hide their unknowns.** They bury assumptions in decision documents, present hypotheses as facts, or wait until launch to discover what's wrong.

**Asgaya documents its ignorance explicitly.** Each unknown in this directory:
- States clearly what we don't know
- Explains why it matters
- Provides a method to investigate it
- Defines what "answered" looks like
- Invites contributors to help

**This is not a weakness—it's a strength.** Projects that pretend to have all the answers don't get better. Projects that document their gaps invite improvement.

---

## Directory Structure

### 📊 Economic Unknowns
Parameters that determine protocol viability:
- [Volatility buffer Rate](economic/volatility buffer-rate.md) — Is 7% sufficient?
- [Seller Fee Sufficiency](economic/seller-fee-sufficiency.md) — Is 0.5% enough?
- [Merchant Spread Sufficiency](economic/merchant-spread-sufficiency.md) — Is 0.5% enough?
- [Fiat Chargeback Risk](economic/fiat-chargeback-risk.md) — How often do Bizum payments reverse?

### 🧠 Behavioral Unknowns
Human behavior that affects protocol operation:
- [Claim Timing](behavioral/claim-timing.md) — How fast do recipients claim remittances?
- [Merchant BCH Preference](behavioral/merchant-bch-preference.md) — Hold or convert?
- [Cash Float Management](behavioral/cash-float-management.md) — How do merchants manage cash inventory?

### ⚙️ Technical Unknowns
Infrastructure assumptions that need validation:
- [Universal Bot Reliability](technical/universal-bot-reliability.md) — Bot performance on real devices
- [SMS Delivery (Venezuela)](technical/sms-delivery-venezuela.md) — Latency and reliability
- [BCH Confirmation Reliability](technical/bch-confirmation-reliability.md) — 0-conf success rate
- [DolarAPI Accuracy](technical/dolarapi-accuracy.md) — Rate feed accuracy and uptime

### 🌍 Market Unknowns
Demand and adoption signals:
- [Seller Capital Recycling](market/seller-capital-recycling.md) — Real vs. modeled throughput
- [Recipient Smartphone Access](market/recipient-smartphone-access.md) — What devices do recipients have?
- [Corridor Demand Signals](market/corridor-demand-signals.md) — Which corridors show strongest demand?

### 🚀 Strategic Unknowns
Long-term effects and expansion strategy:
- [Adoption-Stabilization Effect](adoption-stabilization-effect.md) — Does BCH adoption reduce volatility over time?
- [Hyperinflation Holding Incentive](hyperinflation-holding-incentive.md) — Do merchants in hyperinflation economies hold BCH longer?
- [Cascade Effect](cascade-effect-stabilization.md) — Does hyperinflation-country success make stable-currency adoption easier?

---

## How to Use This Directory

### If You're a Contributor

**Pick an unknown that matches your skills:**
- Data analysis → Economic unknowns
- Surveying/interviewing → Behavioral unknowns
- Technical testing → Technical unknowns
- Market research → Market unknowns

**Follow the investigation method in the brief, document your findings, and submit:**
- GitHub issue: Link to unknown, share findings, suggest next steps
- GitHub PR: Update the unknown brief with your data
- Email: jesgf@yahoo.es with subject "Unknown: [brief name]"

**No approval needed.** If your investigation is sound and your findings are documented, it's a contribution.

### If You're an AI Reviewer

**These unknowns are answerable questions:**
- Each has a clear scope
- Each has a defined method
- Each has success criteria

**If you can help investigate one:**
- Read the brief
- Follow the method (web research, data analysis, simulation)
- Document findings in your review
- Reference the specific unknown you're addressing

**This is permissionless contribution for AI assistants too.**

### If You're a Researcher

**This is a structured research program:**
- 14 distinct unknowns across 4 categories
- Each with hypothesis, method, success criteria
- All answerable within Phase 0 timeline (3-6 months)

**Academic contributions welcome:**
- Survey design for behavioral unknowns
- Statistical modeling for economic unknowns
- Infrastructure testing for technical unknowns
- Market analysis for demand unknowns

**Publication-friendly.** If your research on Asgaya unknowns leads to insights worth publishing, we encourage it (CC-BY-4.0, attribution required).

---

## Investigation Brief Template

Every unknown follows this format:

```markdown
# [Unknown Title]

**Status:** [Not Started | In Progress | Phase 0 Trial | Answered]
**Priority:** [Critical | High | Medium | Low]
**Last Updated:** YYYY-MM-DD
**Contributors Welcome:** Yes

## What We Don't Know
[Clear statement of the gap]

## Why It Matters
[What breaks if this is wrong]

## Current Hypothesis
[What we assume and why]

## Investigation Method
[Specific, actionable steps]

## Success Criterion
[How we'll know this is answered]

## Phase 0 Trial Integration
[How to measure during trials]

## Contributor Guidance
**Skills needed:** [e.g., data analysis, surveying]
**Estimated effort:** [e.g., 2-4 hours]
**How to start:** [First step]

## Related Documents
- [Links to relevant docs]
```

**Consistency makes contribution easier.** Every brief has the same structure, so once you've read one, you can navigate them all.

---

## Priority Ranking (Phase 0)

### Critical (Must Answer Before Phase 0 Launch)
1. [Claim Timing](behavioral/claim-timing.md) — Drives time-based settlement design
2. [Volatility buffer Rate](economic/volatility buffer-rate.md) — Core risk parameter

### High (Should Answer During Phase 0)
3. [Seller Fee Sufficiency](economic/seller-fee-sufficiency.md) — Seller retention
4. [Merchant Spread Sufficiency](economic/merchant-spread-sufficiency.md) — Merchant retention
5. [Universal Bot Reliability](technical/universal-bot-reliability.md) — Technical foundation

### Medium (Nice to Answer During Phase 0)
6. [Merchant BCH Preference](behavioral/merchant-bch-preference.md) — Liquidity design
7. [DolarAPI Accuracy](technical/dolarapi-accuracy.md) — Rate feed quality
8. [Seller Capital Recycling](market/seller-capital-recycling.md) — Seller economics

### Low (Can Answer in Phase 1)
9. [Fiat Chargeback Risk](economic/fiat-chargeback-risk.md) — Rare edge case
10. [SMS Delivery (Venezuela)](technical/sms-delivery-venezuela.md) — Infrastructure
11. [BCH Confirmation Reliability](technical/bch-confirmation-reliability.md) — Infrastructure
12. [Recipient Smartphone Access](market/recipient-smartphone-access.md) — Market data
13. [Cash Float Management](behavioral/cash-float-management.md) — Merchant operations
14. [Corridor Demand Signals](market/corridor-demand-signals.md) — Market expansion

**Priority drives effort allocation.** Phase 0 focuses on Critical and High unknowns first.

---

## Current Status (May 2026)

| Category | Total Unknowns | Not Started | In Progress | Answered |
|----------|---------------|-------------|-------------|----------|
| Economic | 4 | 4 | 0 | 0 |
| Behavioral | 3 | 3 | 0 | 0 |
| Technical | 4 | 4 | 0 | 0 |
| Market | 3 | 3 | 0 | 0 |
| **Total** | **14** | **14** | **0** | **0** |

**We're at the beginning.** This is honest status reporting. As investigations complete, this table updates.

---

## How This Fits Into Asgaya's Development

**Phase -1 (Now):** Documentation-first, structured ignorance
- Identify unknowns
- Write investigation briefs
- Invite contributors

**Phase 0 (Trial Period):** Active investigation
- Critical unknowns answered before launch
- High unknowns measured during trials
- Medium unknowns tracked for iteration

**Phase 1 (Public Launch):** Informed decisions
- Parameters set based on data (not guesses)
- Known risks documented
- Remaining unknowns deprioritized or deferred

**The unknowns directory is a forcing function for intellectual honesty.**

---

## Related Concepts

- [Phase 0 Validation Checklist](../decisions/phase-0-validation-checklist.md) — What needs validation (points here for how)
- [Contributing Guide](../meta/contributing.md) — How to contribute to Asgaya docs
- [AI Review Guide](../meta/ai-review-guide.md) — How AI assistants can help investigate unknowns

---

## Meta-Note: Why This Works

**Traditional approach:**
1. Make assumptions
2. Build product
3. Launch
4. Discover what's wrong
5. Scramble to fix

**Asgaya's approach:**
1. Document assumptions as "unknowns"
2. Invite investigation (permissionless)
3. Answer critical unknowns before building
4. Build with data-informed parameters
5. Launch with known risks documented

**Documentation-first development means unknown-first validation.**

**We're not hiding our ignorance—we're weaponizing it as a contribution opportunity.**

---

**Ready to contribute?** Pick an unknown from the directory above and start investigating. No permission needed.

**Questions?** Email jesgf@yahoo.es or open a GitHub issue.

**Let's turn unknowns into knowns—together.**
