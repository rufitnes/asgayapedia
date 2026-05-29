# Asgaya Risk Allocation Principle

**Status:** Core Design Principle (Phase 0 Active)  
**Category:** Protocol Architecture  
**Related:** [With volatility buffer Bounty Contracts](bounty-contracts-with-volatility-buffer.md), [Pull System](pull-system.md), [Two-Step Settlement Timing](../decisions/two-step-settlement-timing.md)

---

## The Principle

**Asgaya's covenant architecture allocates risk explicitly to protect local merchants from cryptocurrency volatility.**

### Five Core Rules

1. **Merchants never accept undercollateralized claims**
   - Merchant software validates covenant sufficiency BEFORE handing cash
   - Invalid covenants are rejected automatically
   - No trust required, no possibility of loss

2. **Merchants never receive partial BCH settlement**
   - Covenant either pays full EUR-equivalent value OR doesn't execute
   - No "best effort" partial fills
   - Binary outcome: full payment or transaction cancellation

3. **Sellers provide collateral reliability**
   - Sellers post 107% volatility buffer to covenant
   - Sellers can voluntarily time extension to earn uptime incentives
   - Seller reputation tied to covenant completion rate

4. **Senders bear extreme volatility risk beyond collateral thresholds**
   - If BCH drops >7%, covenant expires early
   - Remaining BCH refunds to SENDER (not merchant, not seller)
   - Sender opted into BCH exposure by creating covenant

5. **Under severe volatility, transfers fail safely via sender refund**
   - No one loses money except the sender (who bears tail risk)
   - Merchant never participates in failed transactions
   - System degrades gracefully (refunds, not losses)

---

## Why This Matters

### The Problem We're Solving

**Traditional crypto remittance failure mode:**
```
BCH crashes during transit → Someone loses money → Who?
├─ Escrow holds the loss? (Requires insurance, licensing)
├─ Merchant eats the loss? (Kills merchant adoption)
├─ Sender eats the loss? (Bad UX, kills sender adoption)
└─ Recipient eats the loss? (Worst outcome, kills trust)
```

**Asgaya's solution:**
```
BCH crashes >7% → Covenant expires early → Sender receives refund
├─ Merchant: Never participated (zero loss) ✅
├─ Seller: Received fair exchange at market rate (zero loss) ✅
├─ Recipient: Doesn't get money (no worse off than before) ✅
└─ Sender: Gets depreciated BCH back (bears tail risk) ⚠️
```

### Why Sender Risk Allocation Is Correct

**Senders are the only participant who:**
- Can afford to bear volatility risk (sending €100, not life savings)
- Opted into BCH exposure voluntarily (chose Asgaya, not Western Union)
- Have alternative options if uncomfortable with risk (use traditional remittance)
- Benefit from lower fees (1% vs 6.49%) as compensation for risk

**Senders are NOT:**
- Venezuelan recipients (who need reliable, predictable transfers)
- Local merchants (who provide physical cash liquidity and can't absorb losses)
- BCH sellers (who provide collateral infrastructure, not risk warehousing)

**Result:** The participant MOST able to bear risk (sender) bears it. The participants LEAST able (merchant, recipient) are fully protected.

---

## Common Misconceptions

### ❌ Misconception 1: "Merchants bear volatility risk"

**Reality:** Merchants only participate when covenants are valid and fully collateralized.

**Why people think this:**
- In traditional finance, "liquidity providers" bear settlement risk
- In DeFi, "counterparties" bear undercollateralization risk
- Terminology like "collateral," "settlement," "promise" triggers these mental models

**Why it's wrong:**
- Merchant software validates covenant BEFORE handing cash
- Invalid covenants never execute
- Merchant never receives underpaid BCH

### ❌ Misconception 2: "Sellers bear tail risk"

**Reality:** Sellers receive fair exchange at market rate, even in crashes.

**Why people think this:**
- "Margin call" sounds like penalty/liquidation
- "Time extension opportunity" sounds mandatory to avoid loss
- Standard collateral systems penalize non-response

**Why it's wrong:**
- Default outcome is fair exchange (not penalty)
- Time extensions are voluntary (rewarded, not mandatory)
- Refund goes to SENDER, not seller

### ❌ Misconception 3: "Covenant holds cash buy order for EUR to merchant"

**Reality:** Covenant holds conditional bounty, cancellable if BCH crashes.

**Why people think this:**
- "Promise" implies obligation regardless of circumstances
- "Settlement" implies guaranteed execution
- Legal language of contracts and obligations

**Why it's wrong:**
- Bounty is conditional on collateral sufficiency
- Covenant can abort (refund to sender)
- No obligation to pay merchant from insufficient collateral

---

## Technical Implementation

### Covenant Validation (Merchant Side)

**Before handing cash, merchant software checks:**

```javascript
function isCovenantValid(covenant) {
  const currentBchPrice = getCurrentBchPrice();
  const covenantValue = covenant.lockedBch * currentBchPrice;
  const requiredValue = covenant.eurPromise;
  
  // Covenant must have ≥100% of specified EUR value
  if (covenantValue < requiredValue) {
    return false; // REJECT - undercollateralized
  }
  
  // Covenant must be within 24h window
  if (Date.now() > covenant.expiryTime) {
    return false; // REJECT - expired
  }
  
  // Both conditions must be signable
  if (!covenant.sellerSignedCondition1) {
    return false; // REJECT - seller hasn't confirmed payment
  }
  
  return true; // ACCEPT - valid covenant
}
```

**Result:** Invalid covenants are rejected BEFORE cash changes hands. Merchant bears zero risk.

### Early Maturity (Sender Refund)

**When BCH drops >7%, covenant automatically:**

```javascript
function handleEarlyMaturity(covenant) {
  const currentBchPrice = getCurrentBchPrice();
  const covenantValue = covenant.lockedBch * currentBchPrice;
  const collateralRatio = covenantValue / covenant.eurPromise;
  
  // If collateral < 100%, mature early
  if (collateralRatio < 1.0) {
    // Calculate refund split
    const merchantPortion = covenant.eurPromise * 0.995; // €99.50
    const sellerFee = covenant.eurPromise * 0.005; // €0.50
    
    // Refund distribution
    refundToSender(merchantPortion); // Sender gets €99.50 in BCH
    refundToSeller(sellerFee);       // Seller gets €0.50 in BCH
    
    // Mark covenant as expired
    covenant.status = "MATURED_EARLY";
    
    // Merchant never involved
    return;
  }
}
```

**Result:** Sender receives depreciated BCH. Merchant never participates. System fails safely.

---

## Risk Allocation Matrix

| Risk Type | Sender | Merchant | Seller | Recipient |
|-----------|--------|----------|--------|-----------|
| **BCH crash > 7%** | ✅ YES | ❌ NO | Partial | ❌ NO |
| **BCH upside opportunity cost** | ✅ YES | ✅ YES | ✅ YES | ✅ YES |
| **Local cash liquidity** | ❌ NO | ✅ YES | ❌ NO | ❌ NO |
| **Covenant execution failure** | Partial | Partial | Partial | Partial |
| **Capital lockup (24h)** | ❌ NO | ❌ NO | ✅ YES | ❌ NO |
| **Failed claim timing** | ✅ YES | ❌ NO | ✅ YES | Partial |
| **Fiat chargeback** | ❌ NO | ❌ NO | ✅ YES | ❌ NO |

**Key insight:** Volatility risk is concentrated on sender, who is most able to bear it. Merchant is completely isolated.

---

## Comparison to Traditional Systems

### Western Union (Custodial)

| Scenario | Western Union | Asgaya |
|----------|---------------|--------|
| **BCH crashes** | WU eats loss (has insurance) | Sender eats loss (refunded depreciated BCH) |
| **Merchant location** | WU locations fixed | Any participating merchant |
| **Regulatory status** | Licensed money transmitter | No license required (no custody) |
| **Fee** | 6.49% average | 1% total |
| **Who bears risk** | Central entity | Sender (distributed) |

### Coinbase Remittance (Centralized)

| Scenario | Coinbase | Asgaya |
|----------|----------|--------|
| **BCH crashes** | Coinbase guarantees rate (eats loss) | Sender bears loss |
| **Custody** | Yes (Coinbase holds funds) | No (covenant holds, no entity custody) |
| **Regulatory status** | Requires VASP licensing | No licensing required |
| **Fee** | 2-3% | 1% |
| **Who bears risk** | Central entity | Sender (distributed) |

### LocalBitcoins P2P

| Scenario | LocalBitcoins | Asgaya |
|----------|---------------|--------|
| **BCH crashes** | Buyer/seller negotiate (disputes) | Sender bears loss (clear rules) |
| **Custody** | LocalBitcoins escrow | Covenant (no entity custody) |
| **Merchant role** | No merchant layer | Merchants provide local liquidity |
| **Fee** | Variable (1-5%) | 1% fixed |
| **Who bears risk** | Unclear (leads to disputes) | Clear (sender bears tail risk) |

**Asgaya advantage:** Crystal-clear risk allocation with no disputes, no custody, no licensing.

---

## Phase 0 Validation

### What We're Testing

1. **Do senders understand they bear tail risk?**
   - Metric: % of senders who are surprised by refunds after >7% drop
   - Target: <10% surprise rate (documentation works)

2. **Do merchants trust the protection mechanism?**
   - Metric: Merchant retention after first volatility event
   - Target: >90% retention (protection works)

3. **What's the actual refund frequency?**
   - Metric: % of covenants that mature early (>7% drop)
   - Hypothesis: <5% if using 2h claim windows
   - See: [Volatility buffer Rate Unknown](../unknowns/economic/volatility buffer-rate.md)

4. **Does sender risk tolerance match usage patterns?**
   - Metric: Do senders choose longer or shorter claim windows?
   - Hypothesis: >60% choose 2h windows (minimize risk)

---

## Related Documents

- [With volatility buffer Bounty Contracts](bounty-contracts-with-volatility-buffer.md) - Technical implementation
- [Two-Step Settlement Timing](../decisions/two-step-settlement-timing.md) - Detailed scenarios
- [Pull System](pull-system.md) - Why recipient control matters
- [Fee Splitting Model](../decisions/fee-splitting-model.md) - Economic incentives
- [Phase 0 Validation Checklist](../decisions/phase-0-validation-checklist.md) - Testing plan

---

## Summary: The Core Principle

**Asgaya protects merchants by making sender refunds the default failure mode.**

When BCH crashes >7%:
- ❌ Traditional system: Someone must eat the loss (escrow, merchant, or sender)
- ✅ Asgaya: Sender receives depreciated BCH refund (opted into risk, can afford it)

When BCH stays stable:
- ✅ Traditional system: Works fine (but expensive, 6.49% fees)
- ✅ Asgaya: Works fine (cheaper, 1% fees)

**Result:** Lower fees in normal conditions, clear risk allocation in edge cases, no custody/licensing required.

**The merchant is NEVER exposed to cryptocurrency volatility risk. This is the foundation of Asgaya's economic model.**

---

**Last updated:** May 19, 2026  
**Status:** Core principle, actively implemented in Phase 0 covenant architecture
