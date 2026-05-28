← [Back to Decisions](README.md)

# Decision: MUSD Integration Strategy

**Status:** Planned (Post-Phase 0)  
**Date:** May 12, 2026  
**Related:** [Phase 1 Stability Layer](../roadmap/phase-1-stability-layer.md), [BCH Buyers](../concepts/bch-buyers.md)

---

## Decision Summary

**Integrate MUSD (Moria Protocol stablecoin) as an optional merchant stability layer in Phase 1+, using a staged rollout: display rates first, buyer listings second, atomic swaps third. Asgaya never issues, holds, or controls MUSD—purely an information service that coordinates peer-to-peer swaps.**

---

## Context

### The Need

Merchants receive volatile BCH from covenant settlements. In dollarized economies (Venezuela, Argentina), holding BCH exposes merchants to ±5% price swings. Some merchants want stability without touching traditional banking or fiat rails.

### The Options Considered

| Option | Evaluation |
|--------|------------|
| **Issue Asgaya stablecoin** | ❌ Regulatory nightmare (securities law, payment licensing) |
| **Integrate USDT/USDC** | ❌ Not BCH-native, requires bridges, centralized issuers |
| **Build our own CDP** | ❌ Too complex, not Phase 0 scope |
| **Integrate MUSD** | ✅ Already live, BCH-native, community-governed, audited |
| **Do nothing (pure BCH)** | ⚠️ Works but leaves merchants exposed to volatility |

### Why MUSD?

- **Live on BCH mainnet:** MUSDv1 deployed May 2025
- **Audited:** Hashlock security audit passed
- **Liquid:** ~$596K TVL, active Cauldron DEX pool
- **Non-custodial:** With volatility buffer CDP model (like MakerDAO)
- **Asgaya-independent:** Issued by Moria Protocol, not Asgaya

---

## The Decision

### Three-Stage Rollout

#### Phase 1.0: Rate Display (Q3 2026)

**Goal:** Let merchants see MUSD as an option, educate on stability layer.

**Implementation:**
```
Asgaya bulletin board:
- Read MUSD/BCH rate from Moria oracle or Cauldron API
- Display: "Swap to MUSD: ~99.50 MUSD for your 0.098 BCH"
- Link: "Swap on Cauldron DEX" (external)
```

**Technical work:**
- API integration: Moria oracle (read-only)
- UI component: MUSD rate display widget
- Documentation: "What is MUSD?" help section

**Effort:** 1-2 weeks dev time

**Success metric:** 10%+ merchants click Cauldron link (awareness)

---

#### Phase 1.1: BCH Buyer Listings (Q4 2026)

**Goal:** Enable global BCH buyers to post swap offers on bulletin board.

**Implementation:**
```
Buyer dashboard:
- "Post BCH buy offer" form
- Input: Amount, MUSD price, expiry
- Collateral: Stake X MUSD to prevent griefing (optional)

Merchant dashboard:
- See buyer offers alongside bounties
- Compare: Buyer rates vs Cauldron rate
- Accept: Contact buyer (Telegram/email) or coordinate via app
```

**Technical work:**
- Database: Buyer offers table
- API: POST /buyer-offers/create, GET /buyer-offers/list
- UI: Buyer form, merchant offer view
- Matching: Manual coordination (Phase 1.1), automated later

**Effort:** 4-6 weeks dev time

**Success metric:**
- 5+ active buyers posting offers
- 20%+ merchant swap volume via buyers (vs Cauldron)

---

#### Phase 1.2: Atomic Swap Integration (2027+)

**Goal:** One-click trustless swaps directly in Asgaya app.

**Implementation:**
```
CashToken atomic swap:
1. Merchant accepts buyer offer
2. App generates swap contract (BCH ↔ MUSD)
3. Both parties sign (merchant's BCH, buyer's MUSD)
4. Transaction broadcast to BCH network
5. Settlement (~30 seconds)
```

**Technical work:**
- Wallet integration (CashToken signing)
- Swap contract templates (CashScript)
- Transaction monitoring (confirmations)
- Error handling (failed swaps, refunds)

**Effort:** 8-12 weeks dev time (complex, security-critical)

**Success metric:**
- 50%+ merchants use in-app swap (vs external Cauldron)
- <2% swap failure rate
- 4+/5 user satisfaction

---

## Regulatory Compliance

### Asgaya's Role: Information Service Only

```
┌──────────────────────────────────────────┐
│ MUSD Token                               │
│ - Issued by: Moria Protocol (Riften Labs)│
│ - Type: CashToken (BCH-native)           │
│ - Collateral: BCH (with volatility buffer)   │
│ - Regulation: Moria's responsibility     │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│ Asgaya Bulletin Board                    │
│ - Displays MUSD rates (information)      │
│ - Lists buyer offers (like bounties)     │
│ - Never mints, holds, or controls MUSD   │
│ - Never executes swaps (users do)        │
│ - Role: Information aggregator           │
└──────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────┐
│ Atomic Swaps                             │
│ - Peer-to-peer (merchant ↔ buyer)       │
│ - On-chain (BCH network, no intermediary)│
│ - Asgaya: Wallet UI only (signing)      │
└──────────────────────────────────────────┘
```

**Legal posture:**
- **E-Commerce Directive (EU):** Information society service (exempt from PSD2/MiCA)
- **Token regulation:** Moria Protocol's problem, not Asgaya's
- **Securities law:** MUSD is with volatility buffer CDP (not security under most frameworks)
- **Payment licensing:** No custody, no intermediation = No PSD2 trigger

**Risk mitigation:**
- Never describe MUSD as "Asgaya's token"
- Never provide financial advice ("MUSD is stable")
- Display rates neutrally ("Current rate: X", not "Best rate!")
- If MUSD becomes regulated: Remove integration, pivot to alternatives

---

## Trade-Offs

### ✅ Gained

**For merchants:**
- Instant conversion to stable value
- No KYC exchanges needed
- Better rates than Cauldron (via buyers)
- Global liquidity (not geographically locked)

**For protocol:**
- Merchant retention (stability = less churn)
- Competitive advantage (legacy remittances don't offer this)
- BCH ecosystem growth (MUSD adoption benefits BCH)

### ❌ Lost

**Complexity:**
- Three-stage rollout = more dev work
- Wallet integration = security testing needed
- Token dependency = MUSD peg risk

**Opportunity cost:**
- Dev time spent on MUSD could go to other features
- If MUSD fails, work is wasted

**Regulatory surface:**
- Displaying token rates = more regulatory scrutiny (minimal, but non-zero)
- If token regulation tightens, must pivot

---

## Validation Plan

### Phase 1.0 Metrics (3 months)

| Metric | Target | Action if Not Met |
|--------|--------|-------------------|
| Merchants aware of MUSD | 50%+ | Increase education (help docs, tooltips) |
| Cauldron click-through | 10%+ | Improve UI visibility, add benefits explanation |
| MUSD peg stability | <5% deviation | Pause integration if peg breaks, wait for Moria fix |

### Phase 1.1 Metrics (6 months)

| Metric | Target | Action if Not Met |
|--------|--------|-------------------|
| Active BCH buyers | 5+ | Incentivize buyers (fee rebates, reputation rewards) |
| Buyer offer volume | 20%+ | Improve matching UX, add auto-coordination |
| Buyer completion rate | 80%+ | Add collateral staking, reputation system |

### Phase 1.2 Metrics (12 months)

| Metric | Target | Action if Not Met |
|--------|--------|-------------------|
| In-app swap adoption | 50%+ | Improve UX, reduce friction |
| Swap failure rate | <2% | Debug wallet integration, add retry logic |
| User satisfaction | 4+/5 | Survey feedback, iterate on pain points |

---

## Alternatives Considered

### Alternative 1: Build Asgaya Stablecoin

**Pros:** Full control, optimized for remittance use case  
**Cons:** Regulatory nightmare, high complexity, distracts from core mission

**Verdict:** ❌ Not viable. Token issuance triggers securities/payment regulation.

### Alternative 2: Integrate USDT/USDC

**Pros:** High liquidity, widely accepted  
**Cons:** Not BCH-native, requires bridges, centralized issuers, regulatory risk

**Verdict:** ❌ Not viable. Doesn't align with BCH-native architecture.

### Alternative 3: Futures Hedging

**Pros:** Merchants hedge BCH volatility without selling  
**Cons:** Requires derivatives exchange, margin requirements, complex UX

**Verdict:** ⏳ Defer. Too complex for Phase 1, consider for Phase 2+.

### Alternative 4: Do Nothing (Pure BCH)

**Pros:** Simplest, no dependencies  
**Cons:** Merchants exposed to volatility, may limit adoption

**Verdict:** ✅ Phase 0 approach. Add MUSD only if merchant demand proven.

---

## Contingency Plans

### If MUSD Peg Breaks

**Trigger:** MUSD trades <$0.95 or >$1.05 for >3 days

**Action:**
- Pause MUSD rate display (show warning)
- Suspend buyer offer matching
- Communicate to merchants: "MUSD unstable, use Cauldron at your own risk"
- Monitor Moria Protocol fixes
- Resume when peg stable for 7+ days

### If MUSD Shuts Down

**Trigger:** Moria Protocol announces shutdown or critical vulnerability

**Action:**
- Remove MUSD integration entirely
- Revert to pure BCH model
- Evaluate alternative stable tokens:
  - If other BCH-native stablecoin emerges: Integrate
  - If none exist: Wait for market to mature
  - If merchants demand stability: Explore other mechanisms (futures, insurance)

### If Buyers Don't Emerge

**Trigger:** <3 active buyers after 6 months (Phase 1.1)

**Action:**
- Survey potential buyers: Why not participating?
- Add incentives:
  - Lower bulletin board listing fees (or free)
  - Reputation system (visible buyer ratings)
  - Volume discounts (high-volume buyers get priority visibility)
- Partner with BCH accumulation services (e.g., Bitcoin.com, Cointext)

---

## Implementation Checklist

### Phase 1.0 (Rate Display)

- [ ] API integration: Moria oracle or Cauldron price feed
- [ ] UI component: MUSD rate widget on merchant dashboard
- [ ] Help documentation: "What is MUSD?" section
- [ ] External link: Cauldron DEX swap flow
- [ ] Testing: Rate accuracy, link functionality
- [ ] Monitoring: Click-through rate, merchant feedback

### Phase 1.1 (Buyer Listings)

- [ ] Database schema: Buyer offers table
- [ ] API endpoints: Create, list, accept offers
- [ ] Buyer UI: Post offer form
- [ ] Merchant UI: View offers, compare rates
- [ ] Matching logic: Manual coordination (Telegram/email)
- [ ] Reputation system: Track buyer completion rate
- [ ] Testing: Offer creation, merchant discovery, dispute resolution
- [ ] Monitoring: Buyer count, offer volume, completion rate

### Phase 1.2 (Atomic Swaps)

- [ ] Wallet integration: CashToken signing (merchant + buyer)
- [ ] Swap contracts: CashScript templates (BCH ↔ MUSD)
- [ ] Transaction monitoring: Broadcast, confirmations, errors
- [ ] UI flow: Accept offer → Sign → Confirm → Settlement
- [ ] Error handling: Failed swaps, refund logic
- [ ] Security audit: Contract review, signing vulnerabilities
- [ ] Testing: End-to-end swap flow, edge cases
- [ ] Monitoring: Swap success rate, user satisfaction

---

## Related Documents

- [Phase 1 Stability Layer (Roadmap)](../roadmap/phase-1-stability-layer.md) - Full Phase 1 vision
- [BCH Buyers (Concept)](../concepts/bch-buyers.md) - Participant role design
- [RS053: MUSD Analysis](../research/RS053_MUSD.md) - Technical deep dive
- [RS054: CashToken Swaps](../research/RS054_cashtoken_swaps.md) - Multi-corridor tokens

---

## The Bottom Line

**MUSD integration is deferred to Phase 1+ for good reason:** Phase 0 must prove covenant spine works first. Merchants need to be receiving BCH regularly before a stability layer makes sense.

**The three-stage rollout de-risks the integration:** Start with information display (low effort, low risk), add buyer market (medium effort, validate demand), finish with atomic swaps (high effort, full functionality).

**Asgaya never touches MUSD:** We display rates, list offers, facilitate signing. Moria Protocol issues the token, users hold it, atomic swaps execute peer-to-peer. Clean separation = regulatory compliance.

**If MUSD fails or BCH stabilizes, we pivot:** The architecture supports any CashToken-based stable asset (or none at all). Don't bet everything on one token.

**This is how you build optionality into a protocol.**

---

*Last updated: May 12, 2026*  
*Status: Planned (Post-Phase 0)*  
*Decision by: Suso + DeepSeek*
