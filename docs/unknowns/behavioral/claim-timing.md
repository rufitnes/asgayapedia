# Claim Timing — How Fast Do Recipients Claim Remittances?

**Status:** Not Started  
**Priority:** Critical  
**Last Updated:** 2026-05-20  
**Contributors Welcome:** Yes — see below

---

## What We Don't Know

**How quickly do remittance recipients claim their funds after being notified?**

We have no empirical data on:
- Median time from notification to merchant visit
- Distribution of claim times (percentiles: 10th, 50th, 90th, 99th)
- Factors that influence speed (urgency, distance, merchant hours, notification method)
- Difference between first-time vs. repeat users
- Geographic/cultural variations (Spain→Venezuela vs. other corridors)

**Current design assumes:** Recipients will claim within 24 hours (covenant expiry window).

**Reality unknown:** Do recipients claim in 30 minutes? 3 hours? 18 hours? Is the distribution bimodal (urgent vs. casual)?

---

## Why It Matters

### 1. **Time-Based Settlement Incentive Design**


- If recipients typically claim within 2 hours → 2h/6h/12h/24h windows make sense
- If recipients typically claim within 12-18 hours → Need different brackets
- If claim times are bimodal (30min or 20h, nothing in between) → Need different incentive structure

**Without data, we're designing blind.**

### 2. **Volatility Exposure Window**

Sender volatility risk scales with claim time:
- 2h median claim time → Low volatility exposure (BCH rarely moves >3% in 2h)
- 18h median claim time → High volatility exposure (BCH often moves >7% in 18h)

**If recipients naturally claim fast, volatility is less of a problem than we think.**  
**If they claim slow, we need stronger incentives or shorter expiry windows.**

### 3. **Capital Efficiency (Seller Economics)**

Seller effective APR depends on capital turnover:
- 2h average lockup → ~2,336% APR (0.4% per 2h)
- 18h average lockup → ~365% APR (0.75% per 18h)

**Slow claims kill seller economics. Fast claims make the model viable.**

### 4. **Covenant Parameter Validation**

The 24h expiry window is arbitrary:
- If 99th percentile claim time is 6h → 24h window is wasteful (capital locked unnecessarily)
- If 90th percentile claim time is 20h → 24h window is too short (too many timeouts)

**Data tells us whether current parameters are reasonable or need adjustment.**

---

## Current Hypothesis

**Educated guess (no data):**
- **Median claim time:** 3-6 hours
- **90th percentile:** 12-18 hours
- **99th percentile:** 22-24 hours

**Reasoning:**
- Remittances are often urgent (family needs money for food, medicine, bills)
- Recipients are notified via SMS/WhatsApp (high engagement)
- Merchants are local (low travel friction)
- BUT: Recipients have jobs, school, obligations (can't always drop everything)

**This is pure speculation. We need data.**

---

## Investigation Method

### Option 1: Survey (Low Cost, Moderate Reliability)

**Who to survey:**
- Venezuelan diaspora communities in Spain (r/vzla, Telegram groups)
- Venezuelan recipient communities (WhatsApp groups, local community centers)
- Remittance users on other platforms (ask about past behavior)

**Questions:**
1. "When you send money to family in Venezuela, how quickly do they usually pick it up?"
   - Within 1 hour
   - 1-3 hours
   - 3-6 hours
   - 6-12 hours
   - 12-24 hours
   - More than 24 hours

2. "What factors affect how quickly they claim it?"
   - Distance to pickup location
   - Merchant operating hours
   - Urgency of need
   - Work/school schedule
   - Trust in the system

3. "If they could get a small bonus (€0.30) for claiming within 2 hours, would that change their behavior?"
   - Yes, would prioritize fast claim
   - Maybe, depends on situation
   - No, other factors more important

**Sample size needed:** 50-100 responses for directional signal

**Tools:**
- Google Forms (free)
- Posted in relevant communities with context (Asgaya Phase 0 research)
- Incentive: Small BCH tip for completing survey (0.001 BCH = ~€0.30)

**Estimated effort:** 4-8 hours (survey design, posting, analysis)

**Reliability:** Moderate (self-reported behavior ≠ actual behavior, but better than nothing)

---

### Option 2: Existing Remittance Data (High Reliability, Hard to Obtain)

**Potential data sources:**
- Western Union / MoneyGram (unlikely to share)
- LocalBitcoins / Paxful (crypto P2P, might have public data)
- Smaller remittance platforms (might be willing to share anonymized data)
- Academic studies on remittance behavior

**What to look for:**
- Time from notification to pickup (if tracked)
- Distribution of claim times
- Factors correlated with speed

**Estimated effort:** 10-20 hours (research, outreach, negotiation)

**Reliability:** High (actual behavior, not self-reported)

---

### Option 3: Phase 0 Trial Measurement (Highest Reliability, Delayed)

**During Phase 0 trials, track:**
- Covenant creation timestamp
- Recipient claim timestamp
- Difference = actual claim time

**Measure:**
- Median, 90th, 99th percentile claim times
- Distribution shape (unimodal? bimodal? uniform?)
- Correlation with transaction amount, time of day, day of week

**Advantages:**
- Real-world data from actual Asgaya usage
- No survey bias

**Disadvantages:**
- Can't inform pre-Phase-0 design decisions
- Small sample size initially (10-30 transactions)

**Estimated effort:** Built into Phase 0 infrastructure (logging)

**Reliability:** Highest (actual protocol usage)

---

## Success Criterion

**This unknown is "answered" when we have:**

1. **Median claim time** (50th percentile) with 80% confidence interval
2. **90th percentile claim time** (defines timeout threshold)
3. **Distribution shape** (unimodal? bimodal? skewed?)
4. **Sensitivity to incentives** (will time bonuses work?)

**Minimum viable answer (for Phase 0 launch):**
- Survey data from 50+ respondents showing directional signal
- OR existing remittance data from comparable platform
- OR qualitative interviews with 10+ Venezuelan recipients

**Gold standard answer (for Phase 1 refinement):**
- Actual Phase 0 trial data from 30+ transactions
- Validated against survey predictions

---

## Phase 0 Trial Integration

### Logging Requirements

**Covenant metadata to track:**
```json
{
  "covenant_id": "...",
  "created_at": "2026-06-15T14:30:00Z",
  "claimed_at": "2026-06-15T16:45:00Z",
  "claim_duration_minutes": 135,
  "settlement_window": "6h",
  "time_bonus_earned": 0.002
}
```

### Metrics Dashboard

**Real-time Phase 0 metrics:**
- Average claim time (rolling 7-day window)
- Claim time distribution histogram
- % claimed within each time bracket (2h/6h/12h/24h)
- Time bonus redemption rate

**Iteration triggers:**
- If median claim time < 2h consistently → Consider 1h express option
- If 90th percentile > 20h → Consider longer expiry or stronger incentives
- If time bonuses not redeemed → Increase bonus amounts

---

## Contributor Guidance

### Skills Needed
- **Survey design** (basic questionnaire skills)
- **Spanish fluency** (for Venezuelan community outreach)
- **Data analysis** (basic statistics, percentiles, distributions)
- **Community access** (Venezuelan diaspora networks helpful but not required)

### Estimated Effort
- **Survey approach:** 4-8 hours (design, post, analyze)
- **Data research approach:** 10-20 hours (research, outreach)
- **Literature review approach:** 5-10 hours (search academic studies)

### How to Start

**Step 1:** Choose investigation method (survey recommended for speed)

**Step 2:** If surveying:
1. Draft survey questions (use template above)
2. Post in relevant communities:
   - r/vzla (Venezuelan subreddit)
   - Spanish expat Telegram groups
   - Venezuelan community Facebook groups
3. Offer small BCH incentive for completion (0.001 BCH)
4. Collect responses for 1-2 weeks

**Step 3:** Analyze results:
- Calculate median, 90th, 99th percentile claim times
- Plot distribution histogram
- Identify factors affecting speed
- Note sensitivity to time bonuses

**Step 4:** Document findings:
- Update this file with "Status: Answered"
- Add "Findings" section with data
- Submit findings via GitHub PR or email to rufitnes@proton.me

---

## Related Documents

- [Payment-First Covenant](../../the-mechanism/README.md) (covenant expiry and settlement timing)
- [Seller Economics](../../user-journeys/trader/README.md) (capital efficiency and turnover)
- [Glossary: Covenant](../../glossary.md#covenant) (technical details)

---

## Open Questions

1. **Does urgency correlate with amount?**
   - Are €50 remittances claimed faster than €200 remittances?
   - Or opposite (larger amounts = more urgent need)?

2. **Does time of day matter?**
   - Morning remittances claimed faster (recipient has full day)?
   - Evening remittances delayed (merchant hours, recipient obligations)?

3. **First-time vs. repeat behavior:**
   - Do first-time users claim slower (unfamiliar process)?
   - Do repeat users claim faster (trust established)?

4. **Cultural/geographic factors:**
   - Does Spain→Venezuela differ from other corridors?
   - Urban vs. rural recipient differences?

---

## Status Updates

**2026-05-20:** Investigation brief created, no data yet.

---

**Want to investigate this unknown?** Follow the contributor guidance above and share your findings. No approval needed—permissionless contribution in action.
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
