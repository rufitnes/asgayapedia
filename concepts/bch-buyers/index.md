← [Back to Concepts](README.md)

# BCH Buyers: Global Liquidity Without Geographic Lock-In

**Status:** Planned (Phase 1+)  
**Related:** [Phase 1 Stability Layer](../roadmap/phase-1-stability-layer.md), [BCH Sellers](bch-sellers.md), [Pull System](pull-system.md)

---

## Overview

**BCH Buyers** are participants who hold stable assets (MUSD, EUR, etc.) and offer to purchase BCH from merchants who just received it from covenant settlements. This inverts the failed "Liquidity Provider" model from the old escrow architecture, turning a geographically-locked, inflation-exposed role into a permissionless global market.

**Key innovation:** Buyers don't need to be in Venezuela with bolívares. They can be anywhere with MUSD, buying BCH from Venezuelan merchants via the Asgaya bulletin board.

---

## The Problem: Merchants Receive Volatile BCH

In the covenant model, merchants receive BCH when bounties mature:

```
Covenant executes → Merchant receives 0.098 BCH (~€102)
                 → Merchant hands €100 equivalent cash to recipient
                 → Merchant now holds 0.098 BCH
```

The merchant has three options:
1. **Hold BCH** (exposure to volatility, potential gains/losses)
2. **Spend BCH** (if local BCH economy exists)
3. **Sell BCH** (convert to stable value)

**The friction:** Option 3 requires finding a buyer. Traditional options:
- Local Bitcoin Cash buyers (limited, must coordinate in person)
- Exchanges (requires KYC, bank account, fees, withdrawal delays)
- Peer-to-peer platforms (geographic limits, trust issues)

**BCH Buyers solve this:** A global, permissionless market of people who want to buy BCH, accessible via the bulletin board merchants already use.

---

## How BCH Buyers Work

### Step 1: Buyer Posts Offer

BCH Buyer (Alice, in Germany):
- Holds 500 MUSD (minted from her BCH via Moria Protocol)
- Wants to accumulate more BCH
- Posts offer on Asgaya bulletin board:

```
OFFER: Buy BCH with MUSD
- Paying: 99.50 MUSD per 0.098 BCH
- Rate: $1,015/BCH (market is $1,020, Alice offers slight discount)
- Available inventory: 5 BCH worth
- Swap method: Cauldron DEX or atomic swap
- Expiry: 24 hours
```

### Step 2: Merchant Sees Offer

Merchant (Carlos, in Caracas):
- Just received 0.098 BCH from covenant execution
- Wants stable value (not BCH volatility)
- Opens Asgaya app, sees bulletin board

```
Active BCH Buyer Offers:
1. Alice (Germany): 99.50 MUSD for 0.098 BCH [ACCEPT]
2. Bob (USA): 99.00 MUSD for 0.098 BCH [ACCEPT]
3. Carol (Argentina): 100.00 MUSD for 0.098 BCH [ACCEPT]
```

Carlos compares:
- Alice: Best price (99.50 MUSD)
- Current Cauldron rate: 99.20 MUSD (after fees)
- Alice's offer is better than market

Carlos presses: **[ACCEPT]**

### Step 3: Atomic Swap

Backend coordinates CashToken atomic swap:

```
Carlos signs: "I give 0.098 BCH to Alice"
Alice signs: "I give 99.50 MUSD to Carlos"
→ Both signatures broadcast to BCH network
→ Transaction settles atomically (both or neither)
→ ~30 seconds confirmation
```

### Step 4: Settlement

**Carlos receives:** 99.50 MUSD (stable dollar value)  
**Alice receives:** 0.098 BCH (accumulated at slight discount)

Carlos now holds MUSD, which he can:
- Keep as stable savings
- Swap for VES cash with local buyer
- Use to pay suppliers who accept MUSD
- Convert back to BCH later (if he changes strategy)

---

## Why This Works: Inverting the LP Role

### The Old LP Model (Removed in Covenant Pivot)

In the original escrow architecture, **Liquidity Providers** were supposed to:

| Role | Problem |
|------|---------|
| Hold local fiat (VES) | Inflation destroys value (100%+ annually in Venezuela) |
| Provide cash to merchants | Geographic lock-in (must be in Venezuela) |
| Receive BCH after settlement | Weak incentive (small fee, big risk) |
| Compete with merchants | Fragmented market, unclear value prop |

**Result:** No one wanted to be an LP. The role was economically unattractive and operationally complex.

### The New BCH Buyer Model (Phase 1+)

BCH Buyers flip the incentives:

| Property | Advantage |
|----------|-----------|
| **Hold stable assets (MUSD)** | No inflation risk, dollar-pegged value |
| **Operate globally** | Can be anywhere (Germany, USA, Japan), no geographic lock-in |
| **Buy BCH (not sell)** | Accumulation strategy (bullish on BCH, want more) |
| **Market-driven spreads** | Buyers compete for merchant business (best rate wins) |
| **Permissionless entry** | Just need MUSD + BCH wallet (no approval, no KYC) |

**Result:** Natural economic incentives. Buyers profit from arbitrage, merchants get stability.

---

## Economic Incentives

### For BCH Buyers

**Primary incentive:** Accumulate BCH below market price

Example:
- Cauldron market rate: $1,020/BCH
- Alice offers: $1,015/BCH to merchant
- Alice profit: $5/BCH spread (0.5%)
- On 100 BCH/month: $500 profit

**Secondary incentives:**
- Build inventory for future price appreciation
- Avoid KYC exchanges (peer-to-peer accumulation)
- Support BCH adoption (ideological alignment)
- Earn from volatility arbitrage (buy low, sell high)

**Capital requirement:** Low (just MUSD, which is stable)

**Risk:** Minimal (atomic swaps = no counterparty risk, MUSD peg stable)

### For Merchants

**Primary incentive:** Convert volatile BCH to stable MUSD instantly

Example:
- Merchant receives: 0.098 BCH worth $102
- BCH could drop 5% tomorrow: -$5.10
- Swap to MUSD: Lock in $99.50 (vs $102 if held)
- Net: Small discount ($2.50) for certainty

**Alternative comparison:**
- Cauldron swap: $99.20 MUSD (after 0.3% fee)
- Alice's offer: $99.50 MUSD (better rate)
- Savings: $0.30 vs market

**Trade-off:** Merchant gives up BCH upside (if price rises) for stability (if price falls).

**Risk:** None (atomic swap, instant settlement)

---

## Integration with Bulletin Board

The BCH Buyer role uses the same bulletin board infrastructure as bounty coordination:

### Bulletin Board Sections

```
┌──────────────────────────────────────┐
│ ASGAYA BULLETIN BOARD                │
├──────────────────────────────────────┤
│ 1. Active Bounties                   │
│    - Covenant #7392: €100 → 4,250 VES│
│    - Covenant #8451: €150 → 6,375 VES│
│                                       │
│ 2. BCH Buyer Offers (Phase 1+)       │
│    - Alice: Buy 0.098 BCH for 99.50 MUSD│
│    - Bob: Buy 0.050 BCH for 50.00 MUSD│
│                                       │
│ 3. BCH Seller Offers (Phase 1+)      │
│    - Carol: Sell 1.0 BCH for €1,025  │
│    - David: Sell 0.5 BCH for €510    │
└──────────────────────────────────────┘
```

**Key insight:** The bulletin board is just an information aggregator. It doesn't execute trades, hold custody, or intermediate swaps. Users coordinate peer-to-peer via atomic swaps.

---

## Atomic Swaps: No Custody, No Trust

CashTokens enable **trustless atomic swaps**:

```
Step 1: Merchant and Buyer create swap contract
  - Merchant commits: 0.098 BCH
  - Buyer commits: 99.50 MUSD
  - Both sign contract

Step 2: Contract broadcasts to BCH network
  - If both signatures valid → Execute (both receive)
  - If either invalid → Abort (neither receives)
  - No intermediary, no escrow, no custody

Step 3: Settlement (~30 seconds)
  - Merchant receives MUSD
  - Buyer receives BCH
  - Transaction immutable on-chain
```

**Asgaya's role:** Facilitate signing UI (wallet integration). Never touches tokens.

---

## Comparison to Other Models

### vs. Centralized Exchanges (Kraken, Coinbase)

| Factor | Kraken | BCH Buyers |
|--------|--------|------------|
| KYC required | ✅ Yes | ❌ No |
| Geographic restrictions | ✅ Yes (many countries blocked) | ❌ No (global) |
| Withdrawal delays | ✅ Yes (1-3 days) | ❌ No (instant) |
| Fees | ✅ 0.26% maker / 0.16% taker | ~0.5% spread |
| Custody | ✅ Exchange holds funds | ❌ Self-custody |
| Counterparty risk | ✅ Exchange can freeze | ❌ Atomic swap |

**Result:** BCH Buyers offer lower friction, no KYC, instant settlement.

### vs. Peer-to-Peer Platforms (LocalBitcoins, Bisq)

| Factor | LocalBitcoins | BCH Buyers |
|--------|---------------|------------|
| Geographic matching | ✅ Local only | ❌ Global |
| Trust required | ✅ Reputation system | ❌ Atomic swap (trustless) |
| Escrow | ✅ Platform holds funds | ❌ On-chain atomic swap |
| Fees | ✅ 1% platform fee | ~0.5% spread |
| Speed | ✅ Minutes-hours | ❌ 30 seconds |

**Result:** BCH Buyers are faster, trustless, global.

### vs. Cauldron DEX

| Factor | Cauldron | BCH Buyers |
|--------|----------|------------|
| Liquidity | ✅ Pool-based (24/7 available) | ❌ Offer-based (depends on active buyers) |
| Rate | ✅ Market rate (AMM) | Better (buyers compete for merchants) |
| Slippage | ✅ Yes (for large trades) | ❌ Fixed rate (locked in offer) |
| User experience | ✅ DeFi interface (complex) | Integrated (Asgaya app) |

**Result:** Cauldron is backup (always available). BCH Buyers offer better rates when active.

---

## Risks and Mitigations

### Risk 1: Insufficient Buyer Liquidity

**Scenario:** Only 1-2 buyers post offers, merchants can't always swap.

**Mitigation:**
- Phase 1.0: Cauldron DEX as fallback (always available)
- Phase 1.1: Incentivize buyers (lower bulletin board listing fees?)
- Track: Buyer-to-merchant ratio (target: 1 buyer per 5 merchants)

### Risk 2: Buyer Offer Manipulation

**Scenario:** Buyer posts attractive offer, then doesn't complete swap (griefing).

**Mitigation:**
- Atomic swaps prevent this (swap settles or doesn't, no partial completion)
- Reputation system (Phase 1.2): Track buyer completion rate
- Collateral requirement: Buyers stake MUSD when posting offer (forfeit if they ghost)

### Risk 3: MUSD Depeg

**Scenario:** MUSD trades at $0.85 instead of $1.00 (peg breaks).

**Mitigation:**
- Monitor MUSD peg health (pause swaps if deviation >5%)
- Support multiple stable tokens (diversify risk)
- Merchants can reject offers if rate seems off

### Risk 4: Regulatory Risk (Token Classification)

**Scenario:** MUSD classified as security, triggers regulation for Asgaya.

**Mitigation:**
- Asgaya never issues, holds, or controls MUSD (pure information service)
- If MUSD regulated, pivot to other stable tokens
- Bulletin board neutrality: Lists any CashToken, doesn't endorse specific tokens

---

## Phase 1 Implementation Plan

### Phase 1.0: Rate Display (Q3 2026)

**Scope:** Display MUSD/BCH rate on bulletin board
- Read Moria oracle or Cauldron price
- Show: "MUSD option available (rate: $1,020/BCH)"
- Link to Cauldron DEX (merchants swap manually)

**Effort:** Low (API integration, UI display)

### Phase 1.1: Buyer Listings (Q4 2026)

**Scope:** Allow buyers to post offers
- Buyer UI: "Post BCH buy offer" form
- Merchant UI: See buyer offers on dashboard
- Matching: Merchants select best offer manually
- Coordination: Off-app (Telegram, email) or bulletin board chat

**Effort:** Medium (offer listing, merchant discovery)

### Phase 1.2: Atomic Swap Integration (2027+)

**Scope:** One-click swaps in Asgaya app
- Wallet integration (CashToken signing)
- Atomic swap contract templates
- Transaction broadcast + monitoring
- Settlement confirmation UI

**Effort:** High (wallet security, contract testing)

---

## Success Metrics

**Phase 1.0:**
- 10%+ merchants use Cauldron swap (manual)
- MUSD peg stable (<5% deviation)

**Phase 1.1:**
- 5+ active BCH buyers posting offers
- 20%+ swap volume via buyer offers (vs Cauldron)
- Buyer offer completion rate >80%

**Phase 1.2:**
- 50%+ merchants use in-app atomic swaps
- Swap failure rate <2%
- User satisfaction: 4+/5 stars

---

## Open Questions

1. **Will buyers emerge organically?** Or do we need incentives (fee rebates, reputation rewards)?
2. **What's the right buyer-to-merchant ratio?** Too many buyers = fierce competition (good for merchants). Too few = insufficient liquidity.
3. **Should buyers stake collateral?** To prevent griefing, require buyers to lock X MUSD when posting offers?
4. **How to handle disputes?** If swap fails (network issue, not fraud), who pays gas fees?

**Validation:** Phase 0 must prove merchant demand exists. Phase 1.0 tests if stability layer has uptake. Phase 1.1 tests if buyer market forms naturally.

---

## Related Documents

- [Phase 1 Stability Layer (Roadmap)](../roadmap/phase-1-stability-layer.md) - Full Phase 1 plan
- [BCH Sellers](bch-sellers.md) - Comparison to seller role (opposite flow)
- [Pull System](pull-system.md) - How covenant execution feeds into buyer market
- [MUSD Integration Strategy](../decisions/musd-integration-strategy.md) - Implementation details

---

## The Bottom Line

**BCH Buyers** turn merchant BCH holdings from a liability (volatility risk) into an asset (instant liquidity to stable value). By operating globally via MUSD, buyers remove the geographic lock-in that killed the old LP model.

**For merchants:** Instant conversion to stability, better rates than exchanges, no KYC.  
**For buyers:** Accumulate BCH below market, permissionless entry, minimal risk.  
**For Asgaya:** Information service only (no custody, no swaps, no tokens).

If Phase 0 proves the covenant model, Phase 1 gives merchants the stability they need to scale. And it does so via community infrastructure (Moria, Cauldron), not via Asgaya-issued assets.

**This is how you build a protocol that serves users, not ideology.**

---

*Last updated: May 12, 2026*  
*Status: Planned (Post-Phase 0)*  
*Research by: Suso + DeepSeek*
