← [Back to Documentation](../README.md)

# Phase 1: Stability Layer (MUSD + Global BCH Buyers)

**Status:** Planned (Post-Phase 0)  
**Timeline:** Q3-Q4 2026 (after Phase 0 validation)  
**Dependencies:** Phase 0 success (covenant model proven, merchant network established)

---

## Overview

Phase 1 introduces an **optional stability layer** for merchants who receive BCH from covenant settlements. This layer leverages existing infrastructure (MUSD stablecoin, CashToken swaps) to give merchants the choice between holding volatile BCH or converting to stable value — without requiring Asgaya to issue or control any tokens.

**Key principles:**
- Covenants remain the remittance spine (fiat → BCH)
- Stability layer is optional (merchants choose)
- Asgaya never touches stablecoins (information service only)
- Global permissionless participation (BCH buyers anywhere)

---

## The Problem Phase 1 Solves

### Phase 0 Merchant Experience

In Phase 0, merchants receive BCH directly from covenant execution:

```
Sender (Spain) → Bizum → BCH Seller → Covenant → Merchant receives BCH
```

The merchant then:
1. Holds BCH (exposure to volatility)
2. Sells BCH locally for cash (requires finding buyers)
3. Hands recipient their cash equivalent

**The friction:** BCH can move ±5% in hours. Merchants in dollarized economies (Venezuela, Argentina) prefer stable value they can hold or convert predictably.

### Phase 1 Solution

Add an **optional swap layer** after covenant settlement:

```
Covenant → Merchant receives BCH
             ↓ (optional)
          Swap to MUSD (stable USD-pegged token)
             ↓
          Merchant holds stable value
```

Merchants who want BCH exposure can keep it. Merchants who want stability can swap it.

---

## What is MUSD?

**MUSD** is a BCH-native, overcollateralized stablecoin built on CashTokens by Riften Labs (Moria Protocol).

| Property | Detail |
|----------|--------|
| **Peg** | 1 MUSD = $1 USD |
| **Collateral** | Overcollateralized by BCH (150% initial ratio) |
| **Backing** | No fiat reserves (pure crypto CDP model, like MakerDAO) |
| **Audit** | Hashlock security audit completed |
| **Liquidity** | Active MUSD/BCH pool on Cauldron DEX |
| **TVL** | ~$596K (as of May 2026) |
| **Status** | Live on BCH mainnet since May 2025 |

**How it works:**
1. Users lock BCH into Moria vaults (smart contracts)
2. Mint MUSD at 150% collateral ratio
3. MUSD can be swapped, held, or spent like any CashToken
4. Redemption mechanisms maintain $1 peg

**Why it matters for Asgaya:** Merchants can hold dollar-stable value without touching the traditional banking system or fiat currencies.

---

## Phase 1 Architecture

### Component 1: Bulletin Board Swap Listings

The Asgaya bulletin board (information service) will display:

**BCH/MUSD swap offers:**
```
BCH Buyer offers:
- Pay 99.50 MUSD for 0.098 BCH (rate: $1,015/BCH)
- Swap method: Cauldron DEX or atomic swap
- Available: 5 BCH inventory
```

Merchants see these offers alongside bounty listings and can choose to accept.

### Component 2: Global BCH Buyers

**BCH Buyers** are a new participant role (replacing the failed "Liquidity Provider" concept):

| Old LP (Removed) | New BCH Buyer (Phase 1) |
|------------------|------------------------|
| Held local fiat (inflation risk) | Holds MUSD (stable) |
| Geographically locked (Venezuela only) | Global (anywhere) |
| Competed with merchants | Complements merchants |
| Weak incentives (0.25% fee) | Market-driven spreads |
| High barrier (local bank account) | Permissionless (just MUSD + BCH wallet) |

**How BCH Buyers work:**
1. Buyer holds MUSD (minted from their BCH, or bought on Cauldron)
2. Buyer posts swap offer on bulletin board: "I'll buy X BCH for Y MUSD"
3. Merchant receives BCH from covenant, sees offer
4. Merchant accepts swap: BCH → MUSD
5. Buyer receives BCH (accumulation), Merchant receives MUSD (stability)

**Incentives:**
- Buyers profit from spread (buy BCH below market, sell above)
- Buyers accumulate BCH without touching fiat rails
- Permissionless: no approval, no KYC, no gatekeepers

### Component 3: Atomic Swaps (CashTokens)

CashTokens enable **trustless atomic swaps**: both parties exchange tokens in one on-chain transaction, or neither does.

```
Merchant holds BCH ↔ Atomic swap ↔ Buyer holds MUSD
      ↓ (both sign)                    ↓ (both sign)
      Both settle simultaneously (or neither)
```

No custody, no escrow, no intermediary. Pure peer-to-peer exchange.

---

## Integration Roadmap

### Phase 0 (Current): Pure BCH

- **Status:** In development
- **Merchant receives:** BCH only
- **Swap option:** Manual (merchants find buyers themselves)
- **Asgaya involvement:** None (pure covenant settlements)

### Phase 1.0: MUSD Rate Display

- **Timeline:** Q3 2026 (after Phase 0 validation)
- **Feature:** Bulletin board displays MUSD/BCH rate (from Moria oracle or Cauldron)
- **Merchant receives:** BCH (as before)
- **New option:** "Convert to MUSD" button → Links to Cauldron DEX
- **Asgaya involvement:** Information display only (no custody, no swaps)

**Implementation effort:** Low (read oracle, display rate, link to Cauldron)

### Phase 1.1: BCH Buyer Listings

- **Timeline:** Q4 2026 (3-6 months after Phase 1.0)
- **Feature:** BCH Buyers can post swap offers on bulletin board
- **Merchant receives:** BCH + list of buyer offers
- **New option:** Accept buyer offer → Coordinate atomic swap
- **Asgaya involvement:** Information listing service (like bounty listings)

**Implementation effort:** Medium (swap offer matching, UI for buyers/merchants)

### Phase 1.2: Atomic Swap Integration

- **Timeline:** 2027+ (after buyer market proven)
- **Feature:** One-click atomic swaps directly in Asgaya app
- **Merchant receives:** BCH, can swap to MUSD in-app
- **Asgaya involvement:** Facilitates signing, broadcasts transaction (no custody)

**Implementation effort:** High (CashToken swap contracts, wallet integration)

---

## Why Defer to Phase 1?

**The sequencing matters:**

Phase 0 must prove:
1. ✅ Covenant model works (overcollateralization, margin calls, refunds)
2. ✅ Merchants adopt (critical mass of recipients)
3. ✅ BCH sellers sustain (inventory, hedge, fee viability)

**Only then** can Phase 1 work, because:
- BCH Buyers need merchants regularly receiving BCH (demand to buy)
- MUSD swaps need transaction volume (liquidity depth)
- Atomic swaps need user familiarity (wallet management, signing)

**Building stability layer BEFORE proving covenant spine = premature optimization.**

Phase 0 focuses on the hard problem: fiat → BCH with volatility protection. Phase 1 adds the easy problem: BCH → stable value via existing infrastructure.

---

## Regulatory Compliance

### Asgaya's Role: Information Service Only

```
┌─────────────────────────────────────────┐
│ MUSD (Issued by Moria Protocol)        │
│ - Community token                       │
│ - Overcollateralized by BCH             │
│ - Asgaya never mints, holds, or controls│
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│ Asgaya Bulletin Board                   │
│ - Displays MUSD/BCH rates (information) │
│ - Lists BCH buyer offers (like bounties)│
│ - Never executes swaps (users do)       │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│ Atomic Swaps (CashToken contracts)      │
│ - Peer-to-peer (no intermediary)        │
│ - On-chain settlement (BCH network)     │
│ - Asgaya facilitates signing only       │
└─────────────────────────────────────────┘
```

**Result:** Clean separation. Asgaya remains an information society service (E-Commerce Directive), outside MiCA/PSD2 scope.

### Token Regulation (Not Asgaya's Problem)

MUSD is issued by Moria Protocol (Riften Labs). If MUSD triggers regulation:
- Riften Labs handles compliance (not Asgaya)
- Asgaya just displays rates (like CoinGecko displays Bitcoin prices)
- If MUSD becomes regulated, Asgaya can pivot to other stable tokens

**Regulatory risk:** Low. MUSD is community-governed, overcollateralized, no fiat backing. Similar to DAI, which operates globally without triggering security/payment regulation in most jurisdictions.

---

## Open Questions (To Validate in Phase 1)

### 1. Will BCH Buyers Emerge?

**Hypothesis:** Yes. Arbitrageurs will buy BCH below market from merchants, sell above market elsewhere.

**Validation:**
- Phase 1.0: Track Cauldron swap volume (do merchants use it?)
- Phase 1.1: Track buyer listings (do buyers post offers?)
- Phase 1.2: Track swap completion rate (do merchants accept?)

**Adjustment criteria:**
- If <5 active buyers after 3 months: Increase incentives (lower bulletin board listing fee?)
- If buyers dominant: Add merchant protections (minimum swap amounts, buyer reputation)

### 2. Will Merchants Want Stability?

**Hypothesis:** Most merchants prefer MUSD stability over BCH volatility.

**Counter-hypothesis:** If BCH adoption grows, merchants accumulate BCH for future use (no swap needed).

**Validation:**
- Phase 1.0: Survey merchants (would you use MUSD if available?)
- Phase 1.1: Track swap vs hold rate (% of merchants who swap)

**Adjustment criteria:**
- If >80% merchants swap: MUSD layer is critical, prioritize integration
- If <20% merchants swap: BCH accumulation working, MUSD optional

### 3. Will MUSD Peg Hold?

**Risk:** MUSD depegs during high volatility (MUSDv0 had issues, v1 improved).

**Validation:**
- Monitor MUSD/USD peg deviation (target: <5% most days)
- Track Moria vault health (collateralization ratios)
- Watch Cauldron liquidity depth (can merchants swap large amounts?)

**Contingency:**
- If MUSD unstable: Delay Phase 1, wait for Moria improvements
- Alternative: Support multiple stable tokens (diversify risk)

---

## Alternative Scenarios

### Scenario A: BCH Becomes Stable

If BCH achieves widespread adoption and price volatility drops significantly (e.g., <2% daily moves), the stability layer becomes unnecessary. Merchants hold BCH directly.

**Result:** Phase 1 is built but underutilized. This is a good outcome (BCH thesis validated).

### Scenario B: Multi-Currency Tokens Emerge

If community forks Moria Protocol to create corridor-specific tokens (MVES for Venezuela, MEUR for Europe), the architecture extends naturally:

```
Bulletin board lists:
- BCH/MUSD (global USD)
- BCH/MVES (Venezuela-specific)
- BCH/MEUR (Europe-specific)
- MVES/MEUR (cross-corridor swaps)
```

**Result:** Token-native corridors possible (sender holds MEUR, recipient gets MVES, no BCH volatility exposure).

**Requirement:** Each token needs sufficient liquidity and peg stability. Don't build until proven.

### Scenario C: MUSD Fails

If MUSD depegs permanently or Moria Protocol shuts down:
- Asgaya pivots to other BCH-native stablecoins (if available)
- Reverts to pure BCH model (Phase 0)
- Explores alternative stability mechanisms (futures hedging, insurance)

**Mitigation:** Never build MUSD as the only option. Architecture supports any CashToken-based stable asset.

---

## Success Metrics (Phase 1)

**Phase 1.0 (Rate Display):**
- 50%+ merchants aware of MUSD option
- 10%+ merchants manually swap on Cauldron
- <5% MUSD peg deviation (monthly average)

**Phase 1.1 (Buyer Listings):**
- 5+ active BCH buyers posting offers
- 20%+ merchant-buyer swap volume vs manual swaps
- <10% swap completion failure rate

**Phase 1.2 (Atomic Swaps):**
- 50%+ merchants use in-app swap (vs Cauldron)
- <2% swap transaction failure rate
- User satisfaction: 4+/5 rating for swap UX

---

## Related Documents

- [MUSD Analysis (RS053)](../research/RS053_MUSD.md) - Technical deep dive
- [CashToken Swaps (RS054)](../research/RS054_cashtoken_swaps.md) - Multi-corridor tokens
- [BCH Buyers Concept](../concepts/bch-buyers.md) - Participant role design
- [MUSD Integration Strategy](../decisions/musd-integration-strategy.md) - Implementation approach

---

## The Bottom Line

**Phase 0:** Prove the covenant model works (fiat → BCH remittances)  
**Phase 1:** Add merchant stability option (BCH → MUSD swaps)  
**Phase 2+:** Explore token-native corridors (if demand exists)

The stability layer is **optional, deferred, and built on existing infrastructure**. Asgaya never issues tokens, never holds custody, never intermediates swaps. It remains an information service that coordinates peer-to-peer exchange.

If BCH becomes stable enough that MUSD is unnecessary, Phase 1 isn't deployed. If merchants demand stability, Phase 1 provides it via community infrastructure (Moria/Cauldron), not via Asgaya-controlled assets.

**This is how you build a protocol that adapts to reality, not ideology.**

---

*Last updated: May 12, 2026*  
*Status: Planned (Post-Phase 0)*  
*Research by: Suso + DeepSeek*
