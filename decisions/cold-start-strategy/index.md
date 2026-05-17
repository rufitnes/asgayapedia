# Decision: Cold-Start Strategy

**Decision Date:** May 17, 2026  
**Status:** Phase 0 Approach Defined  
**Context:** Grok review identified this as missing content

---

## The Problem

**Asgaya has a chicken-and-egg liquidity problem:**

- **Senders** won't use the protocol if there are no BCH sellers to accept bounties
- **BCH sellers** won't participate if there's no transaction volume (low fee income)
- **Merchants** won't list availability if there are no recipients to serve
- **Recipients** can't claim remittances if there are no local merchants

**This is the cold-start problem:** How do you bootstrap a two-sided marketplace from zero?

---

## Phase 0: Trusted Bootstrapping (June-August 2026)

**Approach:** Manual coordination with known participants to prove the model works.

### BCH Seller Bootstrap

**Target:** 1-2 trusted BCH holders willing to post capital

**Ideal candidates:**
- BCH miners (steady BCH inflows, long-term holders)
- Early BCH adopters with significant holdings
- Bitcoin Cash community members aligned with adoption goals

**Incentives for Phase 0 sellers:**
- 0.5% fee per transaction (€0.50 on €100)
- Hedge position (convert 94-97% of exposure to EUR within 5 minutes)
- Potential price appreciation on surplus
- Early mover advantage (reputation building)
- **Mission alignment:** Help bootstrap BCH adoption in Venezuela

**Capital requirement:** €1,000-5,000 (10-50 concurrent bounties at €100 each with 7% overcollateralization)

**Risk mitigation:**
- Start with small amounts (€50-100 per transaction)
- 1-2 trusted senders (known family/friends in Venezuela)
- Monitor volatility closely (pause if >5% daily moves)

### Merchant Bootstrap

**Target:** 1-2 merchants in Caracas with:
- Existing cash business (liquidity available)
- Smartphone with reliable internet
- Willingness to hold BCH (or access to BCH buyers)
- Trusted relationship with local recipients

**Incentives:**
- 0.5% spread (€0.50 on €100 transaction)
- BCH accumulation opportunity (if holding)
- First-mover advantage in growing market
- **Free marketing:** Word-of-mouth from satisfied recipients

**Bootstrap approach:**
- Identify through Venezuelan Bitcoin Cash community
- Direct outreach to bodega owners, money changers, or market vendors
- Start with 1-2 test transactions to build confidence

### Sender Bootstrap

**Target:** 1-2 senders in Spain with family in Venezuela

**Why Spain→Venezuela:**
- High remittance corridor (€44M annually)
- Significant cost savings (6.49% → 1%)
- Known recipient addresses (trusted family members)
- Sender controls timing (can wait for BCH seller acceptance)

---

## Phase 1: Market-Driven Growth (September 2026+)

**Transition from trusted bootstrap to open market.**

### Seller Acquisition

**Strategy:** Make 0.5% + hedge attractive enough to pull in profit-seeking participants

**Levers to adjust if needed:**
| Scenario | Adjustment | Trade-off |
|----------|------------|-----------|
| Too few sellers | Increase seller fee to 0.6-0.7% | Reduce merchant fee or increase total cost |
| Seller capital lockup concern | Reduce timeout from 24h to 12h | Less time for recipients to claim |
| Volatility deterrent | Increase overcollateralization to 10% | Higher capital inefficiency |

**Target metrics:**
- **Liquidity depth:** >5 active sellers per corridor
- **Acceptance rate:** >90% of bounties accepted within 30 minutes
- **Coverage:** 18-hour daily availability (sellers in different time zones)

### Merchant Acquisition

**Strategy:** Geographic expansion via word-of-mouth and BCH community

**Approach:**
1. **Phase 0 success stories** - First merchants share experience
2. **BCH community outreach** - Existing BCH-accepting merchants
3. **Financial incentive** - 0.5% spread + BCH accumulation
4. **Low friction** - No upfront costs, instant payout

**Target metrics:**
- **Coverage:** 1 merchant per 10km² in metro areas
- **Availability:** >50% of merchants online during peak hours
- **Retention:** >80% of merchants active after first transaction

### Network Effects

**Once critical mass is reached:**

- **More senders** → More transaction volume → Higher fee income for sellers
- **More merchants** → Better recipient convenience → More senders
- **More BCH holders** → More sellers → Faster bounty acceptance
- **More transactions** → More data → Better validation of parameters

**Flywheel:**
```
More senders
    ↓
Higher volume
    ↓
More seller profit
    ↓
More BCH sellers
    ↓
Faster acceptance
    ↓
Better UX
    ↓
More senders (loop)
```

---

## Long-Term: Protocol Becomes Infrastructure (2027+)

**Vision:** Asgaya becomes unnecessary as BCH adoption reaches critical mass.

### Why Asgaya Would Become Redundant

**If successful:**
- Venezuelan merchants accept BCH directly (no cash-out needed)
- Recipients hold BCH for future payments (no conversion)
- BCH circular economy thrives (peer-to-peer payments)
- Asgaya only needed for legacy fiat interaction (edge cases)

**Success metric:** Asgaya transaction volume DECREASES as BCH usage increases

**The goal is not to build a platform - it's to build a ladder people climb and then kick away.**

---

## What Could Go Wrong (And How We'd Know)

### Failure Mode 1: No BCH Sellers Accept Bounties

**Signal:** <50% acceptance rate, >2 hour wait times

**Response:**
- Increase seller fee (0.6% or 0.7%)
- Reduce timeout window (12h instead of 24h to reduce capital lockup)
- Recruit directly from BCH mining community

**Pivot trigger:** If adjustments don't improve acceptance to >80%, pause and reassess model

### Failure Mode 2: Merchants Don't List Availability

**Signal:** <1 merchant per corridor, no availability during peak hours

**Response:**
- Increase merchant fee (0.6% or higher)
- Offer BCH buyer introductions (instant fiat settlement option)
- Reduce competition by limiting new merchant signups (exclusivity = higher volume per merchant)

**Pivot trigger:** If <3 active merchants after 3 months, reassess merchant value proposition

### Failure Mode 3: Senders Don't Trust the System

**Signal:** High bounce rate (see protocol, don't send)

**Response:**
- Publish Phase 0 success stories
- Add sender insurance option (small fee for guaranteed refund if issues)
- Video tutorials showing complete flows

**Pivot trigger:** If <10 unique senders after 3 months, UX or trust issue

---

## Validation Metrics (Phase 0 → Phase 1 Transition)

**To graduate from Phase 0 to Phase 1, we need:**

| Metric | Target | Purpose |
|--------|--------|---------|
| **Transactions completed** | >30 successful | Prove model works |
| **Unique senders** | >5 | Not dependent on one user |
| **Unique recipients** | >5 | Real use case |
| **BCH seller acceptance rate** | >80% | Liquidity sufficient |
| **Merchant availability** | >50% uptime | Coverage sufficient |
| **Dispute rate** | <5% | System mostly works |
| **Timeout rate** | <10% | Recipients claiming reliably |

**If we hit these targets:** Open to public (Phase 1)  
**If we miss:** Iterate on Phase 0, adjust parameters, retest

---

## Why This Is a Hypothesis

> ⚠️ **This strategy is untested. We don't know:**
> 
> - Will 0.5% attract enough sellers? (Maybe needs 0.7%)
> - Will merchants participate for 0.5%? (Maybe needs 0.6%)
> - Will network effects kick in, or will liquidity stay thin?
> - What's the minimum viable density of merchants for good UX?
> 
> **Phase 0 tests these assumptions.** We'll adjust based on real participant behavior.

---

## Related Decisions

- [Fee Splitting Model](fee-splitting-model.md) - How fees incentivize participants
- [Two-Step Settlement Timing](two-step-settlement-timing.md) - Why 24h timeout matters for capital efficiency
- [Phase 0 Validation Checklist](phase-0-validation-checklist.md) - Metrics we're tracking

---

*The cold-start problem is real, but not unique. LocalBitcoins, Uber, Airbnb all faced it. The solution: start small, prove value, scale via network effects.*
