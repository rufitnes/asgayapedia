# Overcollateralized Bounty Contracts

**Status:** Proposed Architecture (Discovered May 10, 2026)  
**Priority:** Core Innovation — Enables compliant pull system  
**Related:** [Core Regulatory Constraints](./core-regulatory-constraints.md), [RS052: Compliance Architecture](../research/RS052_compliance_architecture.md)

---

## The Problem We Solved

Asgaya's original **pull system** elegantly solved cryptocurrency volatility:
- Sender's EUR sat safely in escrow (zero volatility)
- Escrow only bought BCH when recipient was at merchant and ready
- Volatility window compressed to ~30 seconds
- Sender could "send and forget" — transaction completed on recipient's schedule

**But this required a central escrow** that:
- Held client EUR (custody)
- Provided conditional transfer services (intermediation)
- **→ Triggered MiCA CASP licensing requirement**
- **→ Made the architecture legally impossible to operate**

On May 9, 2026, we abandoned the pull system to comply with [Core Regulatory Constraints](./core-regulatory-constraints.md).

**This document describes how we got it back.**

---

## The Breakthrough

> **What if the seller is the liquidity provider, and the smart contract is the escrow?**

Instead of a **central entity** holding funds, a **CashScript covenant** (BCH smart contract) holds **overcollateralized BCH posted by the seller**. The contract only releases funds when both conditions are met:

1. **Merchant confirmed:** "I gave cash to recipient" (on-chain proof)
2. **Seller confirmed:** "I received Bizum from sender" (automated bot signature)

**If BCH price moves while waiting, the overcollateralization absorbs the volatility.**

Excess BCH returns to the seller. The recipient gets exactly the promised amount of local currency.

---

## How It Works — The Full Flow

### Step 1: Bounty Contract Creation

**Elena (recipient in Venezuela) goes to a merchant.**

They agree on terms:
- **Amount:** 500,000 VES cash
- **Equivalent:** ~€100 at current BCH/VES rate
- **Overcollateralization:** 7% (seller will post 107% of required BCH)

**Elena and merchant create a CashScript covenant on-chain:**

```
Covenant Parameters:
├─ Recipient address: Elena's wallet
├─ Merchant address: Merchant's wallet  
├─ Required BCH: 0.01 BCH (current €100 equivalent)
├─ Overfunding ratio: 1.07x
├─ Timeout: 24 hours
└─ Maturity conditions:
   ├─ [1] Merchant signature: "Cash delivered to Elena"
   └─ [2] Seller signature: "Bizum received from Iris (sender)"
```

**The contract is published to the Asgaya bulletin board as a standing bounty:**

```
╔═══════════════════════════════════════════════════════╗
║  BOUNTY #4729                                         ║
║  500,000 VES (€100 equivalent)                        ║
║  Requires: 0.0107 BCH (overcollateralized 7%)         ║
║  Timeout: 24h from seller acceptance                  ║
║  Status: OPEN — awaiting seller                       ║
╚═══════════════════════════════════════════════════════╝
```

**Key insight:** The covenant is **autonomous code**, not a service provider. No entity is offering custody.

---

### Step 2: Seller Accepts the Bounty

**A seller browsing the bulletin board sees the bounty and accepts it.**

The seller (could be a BCH miner, trader, or anyone with BCH inventory) decides:
- "I'll lock 0.0107 BCH for up to 24 hours"
- "If BCH stays stable, I earn my share of fees + get 0.0007 BCH back"
- "If BCH rises, I get a larger surplus back"
- "If BCH drops >7%, I'll need to add more or let it refund"

**Seller broadcasts a transaction:**
```
Inputs:  Seller's wallet (0.0107 BCH)
Outputs: Bounty Contract #4729 (0.0107 BCH)
```

**The contract now holds the overcollateralized BCH.** No central escrow, no custody service—just code enforcing conditions.

**Bounty status updates:**

```
╔═══════════════════════════════════════════════════════╗
║  BOUNTY #4729                                         ║
║  500,000 VES (€100 equivalent)                        ║
║  Seller: 0x1a2b3c... (BCH locked ✓)                   ║
║  Iris (sender) may now fund via Bizum                 ║
║  Timeout: 23h 59m remaining                           ║
╚═══════════════════════════════════════════════════════╝
```

---

### Step 3: Iris (Sender) Funds the Deal

**Iris sees that a seller accepted the bounty.** She's ready to send money to Elena.

**Iris sends €100 via Bizum to the seller** with concept field:
```
ASGAYA #4729 ELENA
```

**Seller's bot (`smsbridge_loop.py`) detects the Bizum:**
- Parses SMS notification from bank
- Matches concept field to Bounty #4729
- **Signs the covenant: "Condition 2 satisfied — Bizum received"**

**One condition met. Waiting for merchant confirmation.**

---

### Step 4: Elena Cashes Out at Merchant

**Elena is physically at the merchant counter.**

Merchant sees in their app:
```
╔═══════════════════════════════════════════════════════╗
║  BOUNTY #4729 — Ready to Execute                      ║
║  Sender funded: ✓ (Bizum received by seller)          ║
║  Your action: Hand 500,000 VES to Elena               ║
║  You will receive: 0.01 BCH (~€100)                   ║
║  Confirm after handing cash                           ║
╚═══════════════════════════════════════════════════════╝
```

**Merchant hands 500,000 VES cash to Elena.**

**Merchant taps: "I gave cash to Elena"**
- App generates merchant signature
- **Signs the covenant: "Condition 1 satisfied — Cash delivered"**

---

### Step 5: Contract Matures and Executes

**Both conditions satisfied. The CashScript covenant executes automatically:**

```
Contract State: MATURE
├─ Condition 1 (merchant): ✓
├─ Condition 2 (seller):   ✓
└─ Executing distribution...

Outputs:
├─ Merchant: 0.01 BCH (€100 worth at current rate)
├─ Seller:   0.0007 BCH (overfunding surplus)
└─ Transaction fees: ~0.00001 BCH
```

**Transaction complete:**
- ✅ Iris paid €100 (via Bizum to seller)
- ✅ Elena received 500,000 VES cash
- ✅ Merchant received €100 worth of BCH
- ✅ Seller earned fees + got surplus BCH back
- ✅ **No central escrow touched any funds**

---

## Volatility Protection via Overcollateralization

### Price Movement Scenarios

| Scenario | Example | Contract Behavior |
|----------|---------|-------------------|
| **BCH stable** | Required: 0.01 BCH, posted 0.0107 BCH | Merchant gets 0.01 BCH, seller gets 0.0007 BCH surplus |
| **BCH rises 5%** | 0.01 BCH now worth €105 | Merchant gets 0.0095 BCH (still €100), seller gets 0.0112 BCH surplus |
| **BCH drops 3%** | 0.01 BCH now worth €97 | Merchant gets 0.0103 BCH (still €100), seller gets 0.0004 BCH surplus |
| **BCH drops 8%** | 0.01 BCH now worth €92 | **MARGIN CALL** — seller must add BCH or deal cancels |

### Margin Call Mechanism

**If BCH price drops beyond the overfunding buffer:**

1. **Alert sent to seller:**
   ```
   ⚠️ MARGIN CALL — Bounty #4729
   BCH dropped 8% since you locked funds.
   Add 0.0003 BCH within 60 minutes or deal will refund.
   ```

2. **Seller has three options:**
   - **Add more BCH** to the contract (top up collateral)
   - **Let it refund** (timeout triggers, Iris gets refund, seller's reputation penalized)
   - **Hope price recovers** within the grace period

3. **If seller doesn't respond:**
   - Contract triggers timeout clause
   - Seller's BCH returns to them
   - Iris is notified: "Deal canceled — seller couldn't cover price drop"
   - Bulletin board marks seller with failed bounty (reputation impact)

**This is economically similar to MakerDAO liquidations,** but on a per-transaction basis.

---

## Why This Is Not Custody (MiCA Compliance)

### The Legal Distinction

| Central Escrow (Old — Regulated) | Bounty Contract (New — Compliant) |
|----------------------------------|-----------------------------------|
| Entity holds **client EUR** | Smart contract holds **seller's own BCH** |
| Provides **custody service** to sender | Seller posts **collateral** (owns it throughout) |
| Intermediates **sender → recipient** | No intermediation — bilateral transactions |
| Discretionary control (can freeze/reverse) | Autonomous code (executes deterministically) |
| **→ MiCA CASP license required** | **→ No license required** |

**Key regulatory principle:**

> **Locking one's own capital in a smart contract for profit is not providing a custody service to others.**

**Precedents:**
- **MakerDAO:** Users lock ETH to mint DAI (not custodying on behalf of clients)
- **Uniswap:** LPs provide liquidity using own tokens (not holding client deposits)
- **Aave:** Users deposit to earn interest on their own assets (not custody service)

**Asgaya sellers follow the same model:**
- Seller owns the BCH in the contract at all times
- Seller is making a private investment decision (accept bounty or not)
- Seller earns profit from price movements and fees
- **No one is depositing funds "with" the seller for safekeeping**

---

## Architecture Components

### 1. CashScript Covenant Specification

**Maturity conditions (both required):**
```javascript
// Simplified CashScript logic
contract BountyContract(
    pubkey merchantPubKey,
    pubkey sellerPubKey,
    pubkey recipientPubKey,
    int requiredAmount,      // BCH satoshis (e.g., 1,000,000 sats)
    int timeout              // Block height (24 hours from creation)
) {
    // Maturity path: Both signatures present
    function mature(sig merchantSig, sig sellerSig) {
        require(checkSig(merchantSig, merchantPubKey));
        require(checkSig(sellerSig, sellerPubKey));
        
        // Calculate outputs
        int merchantAmount = requiredAmount;
        int sellerSurplus = tx.value - requiredAmount - txFee;
        
        // Enforce outputs
        require(tx.outputs[0].value == merchantAmount);
        require(tx.outputs[0].lockingBytecode == merchantPubKey.lock());
        require(tx.outputs[1].value == sellerSurplus);
        require(tx.outputs[1].lockingBytecode == sellerPubKey.lock());
    }
    
    // Timeout path: Refund to seller if conditions not met
    function refund(sig sellerSig) {
        require(tx.time >= timeout);
        require(checkSig(sellerSig, sellerPubKey));
    }
}
```

**Security properties:**
- ✅ Merchant cannot claim BCH without Elena (recipient) being present (merchant needs seller signature too)
- ✅ Seller cannot reclaim BCH unless timeout expires (both signatures needed or timeout)
- ✅ Exact amounts enforced by covenant (merchant gets exactly required BCH, no more)
- ✅ Immutable once deployed (no one can change terms)

### 2. Seller Bot Automation

**The seller bot (`smsbridge_loop.py`) must:**

1. **Monitor bulletin board** for new bounties
2. **Accept bounties** matching seller's risk parameters:
   - Maximum lock duration (e.g., 24h)
   - Minimum profit margin (e.g., fees > 0.2%)
   - Maximum exposure (e.g., don't lock >10% of inventory at once)
3. **Broadcast funding transaction** to covenant
4. **Monitor bank SMS** for Bizum receipts
5. **Sign covenant** when Bizum detected
6. **Monitor BCH price** for margin call risk
7. **Top up collateral** if price drops >5%
8. **Claim refund** if timeout expires

**Automation is critical** — manual sellers would struggle with timing requirements.

### 3. Merchant App Integration

**Merchant app must:**

1. **Display active bounties** waiting for cash handover
2. **Show contract state** (funded by seller? sender paid?)
3. **Guide merchant through confirmation:**
   ```
   ┌─────────────────────────────────────┐
   │ Ready to hand cash to Elena?        │
   │ Amount: 500,000 VES                 │
   │                                     │
   │ [Hand Cash] → [I Gave Cash]         │
   └─────────────────────────────────────┘
   ```
4. **Sign the covenant** after merchant confirms
5. **Show BCH arrival** once contract matures

### 4. Bulletin Board Display

**Bounties listed with transparency:**

```
╔════════════════════════════════════════════════════════════╗
║  ACTIVE BOUNTIES                                           ║
╠════════════════════════════════════════════════════════════╣
║  #4729 | 500,000 VES | Seller: 0x1a2b... | Funded ✓       ║
║         Timeout: 23h 12m | Overfunded: 7%                  ║
╠════════════════════════════════════════════════════════════╣
║  #4730 | 300,000 VES | Awaiting seller  | Open            ║
║         Created: 12m ago                                   ║
╠════════════════════════════════════════════════════════════╣
║  #4728 | 1,000,000 VES | MATURE — executing               ║
║         Merchant & sender confirmed                        ║
╚════════════════════════════════════════════════════════════╝
```

**No entity is "matching" or "executing" — bulletin board just displays information.**

---

## Economic Analysis

### Seller Capital Requirements

**For €100 transaction with 7% overfunding:**
- Seller locks: €107 worth of BCH
- Seller earns (if stable): Share of 1% fee (€0.33) + €7 surplus = €7.33
- Seller time-locked: Up to 24 hours
- **Return on capital:** 7.33% for ≤24h = **2,670% APR** (if price stable)

**For €10,000/month remittance volume:**
- Average transaction: €100
- Transactions per month: 100
- Capital needed (if sequential): €107
- Capital needed (if 10 concurrent): €1,070
- Monthly earnings (10 concurrent, stable prices): €73.30 × 10 = €733
- **Monthly return:** 68.5% on €1,070 capital

**This is attractive for:**
- ✅ BCH miners (have inventory, want EUR liquidity)
- ✅ Traders (can hedge, earn spread)
- ✅ HODLers (willing to lock BCH short-term for yield)

**Not attractive for:**
- ❌ Entities without BCH (need to buy, then lock = low efficiency)
- ❌ Risk-averse participants (price volatility = margin call risk)

### Comparison to Old Escrow Model

| Metric | Central Escrow | Bounty Contract Seller |
|--------|----------------|------------------------|
| **Capital source** | Escrow's EUR (buy BCH on demand) | Seller's own BCH inventory |
| **Capital efficiency** | High (buy exact amount needed) | Lower (7% overcollateralization) |
| **Volatility risk** | Zero (escrow buys at moment needed) | Medium (seller absorbs ±7% swings) |
| **Profit model** | 1% fee split | 1% fee split + surplus if BCH rises |
| **Regulatory status** | MiCA CASP required | No license (own capital) |
| **Scalability** | Bottleneck (single escrow) | Distributed (many sellers) |

**Trade-off:** Lower capital efficiency, but legally viable.

---

## Phase 0 Implementation Considerations

### Chipnet Testing Requirements

**Before mainnet launch:**
1. **Deploy covenant to Chipnet** (BCH testnet)
2. **Simulate price drops** (margin call testing)
3. **Test timeout refunds** (seller doesn't fund, or conditions not met)
4. **Attack scenarios:**
   - Merchant tries to claim without giving cash
   - Seller tries to double-sign
   - Front-running attacks on maturity
5. **Audit covenant code** (BCH community review)

**Timeline:** 1-2 weeks of Chipnet testing before mainnet.

### Overfunding Ratio Selection

**Phase 0 recommendation: 7-10%**

**Rationale:**
- BCH daily volatility: ±5-8% typical
- 7% buffer covers most daily swings
- 10% buffer reduces margin calls significantly
- Higher buffer = more capital locked = fewer sellers willing

**Dynamic adjustment (future):**
- Phase 1: Adjust based on realized volatility
- If BCH volatility drops (market matures) → Lower overfunding (3-5%)
- If volatility spikes → Increase overfunding (15%+)

### Timeout Window

**Phase 0 recommendation: 24 hours**

**Rationale:**
- Gives recipient time to reach merchant (Venezuela infrastructure unreliable)
- Long enough for real-world delays (power outages, merchant hours)
- Short enough that seller capital isn't locked excessively
- Refund after 24h if deal doesn't complete

**Future optimization:**
- Shorten to 12h if data shows 95% of deals complete in <6h
- Extend to 48h for high-value transactions (>€500)

---

## Open Questions for Review

### 1. Covenant Complexity

**Question:** Is CashScript mature enough for production use?

**Considerations:**
- BCH covenant upgrade launches May 15, 2026 (mainnet)
- Tooling (Bitauth IDE, CashScript SDK) is production-ready
- But covenant security auditing is early-stage
- **Risk:** Covenant bug could lock funds permanently

**Mitigation:**
- Keep covenant logic simple (2-of-2 conditions + timeout)
- Extensive Chipnet testing
- Community code review before mainnet
- Phase 0: Low volume (€1K-5K/month) to limit exposure

---

### 2. Seller Liquidity Constraints

**Question:** Will enough sellers participate if capital requirements are high?

**Considerations:**
- Phase 0 target: €10K/month = €1,070 locked if 10 concurrent
- Miners in Venezuela/Colombia = natural sellers (have BCH, need EUR)
- But if overfunding too high (>10%), capital cost > profit

**Mitigation:**
- Start with 7% overfunding (balance profit vs. safety)
- Recruit BCH miners specifically (have inventory + motivation)
- Consider "fractional" bounties (multiple sellers pool collateral)

---

### 3. Margin Call UX

**Question:** How do sellers respond to margin calls fast enough?

**Considerations:**
- BCH can drop 8% in minutes during volatility spikes
- Seller might be asleep, offline, or unable to respond
- Too many margin call failures = deals cancel = bad UX

**Mitigation:**
- Automated top-up (seller pre-authorizes bot to add collateral)
- Higher initial overfunding (10% instead of 7%) = fewer calls
- Seller reputation system penalizes frequent failures

---

### 4. Fee Distribution

**Question:** Who gets what from the 1% total fee?

**Current model (escrow-based, obsolete):**
```
1% total fee
├─ Escrow: 0.333%
├─ Merchant: 0.333%
└─ LP: 0.333%
```

**New model (bounty-based):**
```
1% total fee
├─ Seller: 0.5% (provides capital + takes volatility risk)
├─ Merchant: 0.3% (provides cash + BCH acceptance)
└─ Protocol: 0.2% (bulletin board hosting)
```

**Needs validation:** Is 0.5% enough to incentivize sellers given capital requirements?

---

### 5. Regulatory Review

**Question:** Does this architecture truly avoid MiCA/PSD2 licensing?

**Next steps:**
- Consult Spanish fintech lawyer (€500-1K)
- Present RS052 + this document
- Get written opinion on CASP exemption
- Validate before Phase 0 launch

---

## What We Regained

| Feature | Escrow Pull (Lost May 9) | Bulletin Board (May 9) | Bounty Contract (May 10) |
|---------|--------------------------|------------------------|--------------------------|
| **Volatility protection** | ✅ EUR in escrow (0% volatility) | ❌ Immediate BCH purchase | ✅ Overcollateralization |
| **Recipient controls timing** | ✅ Escrow waits for signal | ❌ Sender timing determines | ✅ Covenant waits for both conditions |
| **Send-and-forget UX** | ✅ Sender pays once | ❌ Sender must coordinate | ✅ Sender pays once, covenant handles rest |
| **No CASP licensing** | ❌ Custody = regulated | ✅ No custody | ✅ Seller's own capital |
| **Permissionless** | ❌ Escrow gatekeeps | ✅ Anyone can participate | ✅ Anyone can be seller |
| **No central entity** | ❌ Escrow is single point | ✅ Pure P2P | ✅ Smart contract + distributed sellers |

**We got the pull system back — without the escrow.**

---

## Related Documents

- [Core Regulatory Constraints](./core-regulatory-constraints.md) — Why this architecture is necessary
- [RS052: Compliance Architecture](../research/RS052_compliance_architecture.md) — Full regulatory analysis
- [Decentralized Pull System](./decentralized-pull-system.md) — Original discovery document
- [Core Architecture: Why No Custody](../core-architecture/why-no-custody-no-intermediation.md) — Detailed legal reasoning (to be created)

---

**Documented:** May 10, 2026  
**Discovery:** Suso + Yakyak (OpenYak)  
**Analysis:** Coordination  
**Status:** Proposed architecture — pending Chipnet testing, legal review, and team consensus
