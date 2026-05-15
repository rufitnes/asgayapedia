# UI Language - Regulatory Implications

**Status:** Open Question  
**Date:** 2026-05-14  
**Decision:** Deferred to Phase 0 User Testing  

---

## The Issue

**UI language reveals intent, and intent affects regulatory classification.**

### Current Language (Phase 0 Husk)

**Sender Screen 1 (Home):**
```
💸 Send Money
Transfer to family/friends
```

**Regulatory interpretation:**
- "Send Money" = Remittance service
- "Transfer to family/friends" = Money transmission
- **Risk:** Triggers MSB/CASP licensing + KYC requirements

### Technical Reality

**What actually happens:**
1. Sender buys BCH from BCH Seller (permissible)
2. BCH Seller posts BCH to covenant (smart contract)
3. Recipient claims BCH from covenant (permissible)
4. Merchant co-signs and receives BCH (permissible)

**No custody, no intermediation, no money transmission.**

### The Tension

**Honest language** (Send Money) reveals remittance intent  
**Neutral language** (Buy BCH) obscures actual use case

---

## Alternatives Considered

### Option A: Explicit Remittance Language (Current)

**Pro:**
- ✅ Users immediately understand purpose
- ✅ Honest about intent (builds trust)
- ✅ Matches actual use case
- ✅ Better UX (clear value proposition)

**Con:**
- ❌ Signals remittance service to regulators
- ❌ May trigger licensing scrutiny
- ❌ Creates regulatory attack surface

**Example:**
```
💸 Send Money to Venezuela
Cross-border remittance, <1% fees
```

---

### Option B: Neutral Crypto Language

**Pro:**
- ✅ Avoids "remittance" classification
- ✅ Technically accurate (user IS buying BCH)
- ✅ Lighter regulatory footprint
- ✅ Aligns with "permissionless crypto" narrative

**Con:**
- ❌ Users may not understand purpose
- ❌ Obscures value proposition
- ❌ Confusing for non-crypto users
- ❌ Dishonest if intent IS remittance

**Example:**
```
🪙 Purchase Bitcoin Cash
P2P covenant-based exchange
```

---

### Option C: Hybrid Language (Educational)

**Pro:**
- ✅ Clear use case + technical accuracy
- ✅ Educates users on architecture
- ✅ Shows "how it works" transparency
- ✅ May survive regulatory scrutiny (informed users)

**Con:**
- ❌ Verbose (more text)
- ❌ Cognitive load (need to understand covenants)
- ❌ May still be interpreted as remittance

**Example:**
```
💸 Send Value Cross-Border
Buy BCH → Covenant → Recipient Claims
<1% fees, no intermediaries
```

---

### Option D: Regional Language Switching

**Pro:**
- ✅ Adapt to local regulatory environment
- ✅ EU users see neutral language (lighter regulation)
- ✅ Non-EU users see explicit language (clearer UX)
- ✅ Optimizes for both regulation + usability

**Con:**
- ❌ Implementation complexity
- ❌ Inconsistent brand messaging
- ❌ May look like intentional evasion
- ❌ Doesn't solve underlying tension

**Example:**
- Spain (MiCA jurisdiction): "Buy BCH via Covenant"
- Venezuela (no MiCA): "Send Money to Family"

---

## Phase 0 Approach

### Decision: Keep Explicit Language, Test Usability First

**Rationale:**
1. **Phase 0 = Trusted parties** - They already understand the model
2. **Usability > Regulation initially** - Test if neutral language confuses users
3. **Gather feedback** - Let reviewers/users debate language
4. **Empirical data** - Does "Buy BCH" reduce adoption vs "Send Money"?
5. **Regulatory clarity comes later** - Don't over-optimize before user validation

**Current approach:**
- ✅ Use "Send Money" language in Phase 0 Husk
- ✅ Document regulatory concern (this file)
- ✅ Test with 3-5 trusted senders
- ✅ Ask: "Did you understand the flow? Was language clear?"
- ✅ Collect feedback from external reviewers
- ⏳ Revisit before Phase 1 (public launch)

### Open Questions for Phase 0 Testing

**For senders:**
1. If the button said "Buy BCH for Recipient" instead of "Send Money", would you understand what it does?
2. Does "Send Money" set false expectations (like Western Union instant transfer)?
3. Would you prefer honest language ("Send Money") or neutral language ("Buy BCH")?

**For external reviewers:**
4. Does explicit "Send Money" language create unacceptable regulatory risk?
5. Can we argue "informed consent" if users understand they're buying BCH (not using a remittance service)?
6. What's the regulatory difference between "Send Money" UI vs "Buy BCH" UI if underlying flow is identical?

**For regulators (if we engage):**
7. If users are explicitly shown the covenant architecture (BCH purchase → covenant → recipient claim), does that change classification?
8. Is there language that preserves UX clarity while avoiding "money transmission" classification?

---

## Legal Considerations

### MiCA (EU Markets in Crypto-Assets Regulation)

**Relevant provision:** Art. 3(1)(5) - Crypto-Asset Service Provider (CASP)

**Definition:** "Operating a crypto-asset trading platform"

**Question:** Does Asgaya "operate a trading platform" or just display bulletin board?

**Language implications:**
- "Send Money" → Suggests Asgaya facilitates transmission
- "Buy BCH from Seller" → Suggests users trade directly with sellers
- **Bulletin board framing** may preserve exemption regardless of language

### U.S. FinCEN Guidance (Relevant for global operations)

**Relevant guidance:** FIN-2019-G001 (Virtual Currency Business Activity)

**Money transmitter test:**
1. Accepts value from one person
2. Transmits value to another location/person
3. Does so as a business

**Language implications:**
- "Send Money" → Suggests Asgaya accepts value (step 1)
- "Buy BCH" → Suggests user-to-user exchange (no intermediary)
- **Covenant architecture** shows no custody (no step 1)

**Possible defense:**
- "Language describes user intent (sending value to family), not Asgaya's role (bulletin board)"
- "Users understand they're buying BCH from seller, covenant enforces delivery"
- "Asgaya is information service, like Google Maps for crypto covenants"

---

## Recommendations

### Before Phase 1 Public Launch

**1. User Testing (Phase 0)**
- Test both language options with 10-20 users
- Measure: Comprehension rate, completion rate, trust level
- Ask: "What is Asgaya doing for you?" (reveals mental model)

**2. Legal Review**
- Consult EU crypto lawyer on MiCA CASP classification
- Test "bulletin board + honest language" vs "bulletin board + neutral language"
- Get written opinion on language risk

**3. External Reviewer Feedback**
- Share with bitcoincashresearch forum
- Ask: "Does 'Send Money' language create unacceptable regulatory risk?"
- Collect opinions from regulators-adjacent people (lawyers, compliance folks)

**4. Decision Matrix**

| User Comprehension | Regulatory Risk | Recommended Language |
|-------------------|-----------------|---------------------|
| High with "Send Money" | Low | Use "Send Money" (honest + clear) |
| High with "Send Money" | High | Use hybrid ("Send Value via BCH Covenant") |
| Low with "Buy BCH" | Low | Use "Send Money" (clarity matters more) |
| Low with "Buy BCH" | High | Rethink UX entirely (onboarding education?) |

**5. Fallback Options**

If "Send Money" language proves too risky:
- **Educational flow:** First-time users see "What is Asgaya?" screen explaining covenant architecture
- **Two-tier language:** Advanced users see "Buy BCH", new users see "Send Money" + explanation
- **Tooltip/Footnote:** "You're buying BCH via covenant contract - recipient claims when ready"

---

## Phase 0 Husk Decision

**✅ USE "Send Money" language as-is**

**Justification:**
- Phase 0 = Trusted parties who understand model
- Clarity > Regulation for initial testing
- Gather empirical feedback before optimization
- Document concern (this file) for transparency
- Revisit after user testing + external review

**Review trigger:**
- [ ] After 20+ Phase 0 transactions
- [ ] After external reviewer feedback
- [ ] Before Phase 1 public launch
- [ ] If regulators express concern

---

## Related Documents

- [Core Regulatory Constraints](../concepts/core-regulatory-constraints.md) - No custody, no KYC rationale
- [Why No KYC](../core-architecture/why-no-kyc.md) - Permissionless access philosophy
- [Covenant Architecture](../concepts/overcollateralized-bounty-contracts.md) - Technical implementation
- [Phase 0 Validation Checklist](./phase-0-validation-checklist.md) - Testing criteria

---

## Version History

- **2026-05-14:** Initial documentation of concern (Phase 0 Husk review)
- **TBD:** User testing results + legal review + external feedback
- **TBD:** Final decision before Phase 1 launch
