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

### Covenant Creation & Claim Paths

**Key Architectural Decision:** Sender always creates the covenant (for simplicity), but recipient chooses how to claim.

**Covenant Creation:**
- Sender creates EUR-denominated covenant with recipient's address
- BCH Seller posts overcollateralized BCH (107%)
- Covenant published to bulletin board
- 24-hour claim window starts

**Two Claim Paths:**

#### Path A: Direct BCH Claim (Recipient Only)
```
Maturity condition: Recipient signature only
├─ Recipient signs covenant → Claims BCH to wallet
├─ No merchant involved
├─ Instant settlement (~30 seconds)
└─ Total fees: 0.5% (sender to BCH Seller only)
```

**Use case:** Recipient wants to hold BCH (or convert via Cauldron DEX, or sell P2P later)

#### Path B: Merchant Cash-Out (Recipient + Merchant)
```
Maturity conditions: Recipient AND Merchant signatures
├─ Recipient goes to merchant
├─ Merchant hands cash
├─ Both co-sign covenant → Merchant receives BCH
├─ Settlement in ~30 seconds
└─ Total fees: 1% (sender 0.5% + merchant 0.5%)
```

**Use case:** Recipient needs physical cash immediately

**Architectural simplicity:**
- Sender doesn't need to know recipient's claim preference
- Same covenant supports both paths
- Recipient controls timing AND format (pull system)
- Merchant only involved if recipient chooses cash

---

### Step 1: Bounty Contract Creation (Sender Side)

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

**If BCH price drops beyond the overfunding buffer during the 24-hour window:**

1. **Alert sent to seller:**
   ```
   ⚠️ MARGIN CALL — Bounty #4729
   BCH dropped 8% since you locked funds.
   Add 0.0003 BCH within 60 minutes or contract matures early.
   ```

2. **Seller has three options:**
   - **Add more BCH** to the contract (top up collateral manually)
   - **Let contract mature early** (all BCH goes to sender, seller keeps the €100 fiat already received)
   - **Hope price recovers** within the 60-minute grace period

3. **How seller tops up (manual transaction):**
   - Seller already received €100 Bizum from sender (Iris)
   - Seller can buy more BCH with that EUR (on exchange or via Asgaya bulletin)
   - Seller broadcasts additional BCH to covenant address
   - **Note:** Automated Bizum→BCH purchase likely violates banking TOS, so manual intervention required

4. **If seller doesn't respond within 60 minutes:**
   - **Contract matures early** (before 24-hour timeout)
   - **All BCH goes to sender** (Iris receives full covenant balance)
   - Seller keeps: €100 Bizum received (covered fiat leg)
   - Seller loses: BCH collateral posted (penalty for non-response)
   - Bulletin board marks seller with failed margin call (reputation impact)

**Economic model:**
- Seller has capital to respond (€100 fiat in hand)
- Early maturity penalty incentivizes fast response
- Similar to MakerDAO liquidations, but on per-transaction basis
- 60-minute window balances seller convenience vs sender protection

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
    pubkey senderRefundPubKey,  // Iris's address for timeout refund
    int merchantAmount,         // BCH satoshis for merchant (e.g., 995,000 sats = 0.00995 BCH)
    int sellerFee,              // BCH satoshis for seller fee (e.g., 75,000 sats = 0.00075 BCH)
    int bizumWindow,            // Block height for Bizum payment (5 min from acceptance)
    int timeout                 // Block height for full timeout (24h from acceptance)
) {
    // Maturity path: Both signatures present (normal execution)
    function mature(sig merchantSig, sig sellerSig) {
        require(checkSig(merchantSig, merchantPubKey));
        require(checkSig(sellerSig, sellerPubKey));
        
        // Calculate amounts
        int merchantPayout = merchantAmount;
        int sellerPayout = tx.value - merchantAmount - txFee;
        
        // Enforce outputs
        require(tx.outputs[0].value == merchantPayout);
        require(tx.outputs[0].lockingBytecode == merchantPubKey.lock());
        require(tx.outputs[1].value == sellerPayout);
        require(tx.outputs[1].lockingBytecode == sellerPubKey.lock());
    }
    
    // Early cancel: Seller can reclaim if no Bizum within 5 min
    function cancelNoBizum(sig sellerSig) {
        require(tx.time >= bizumWindow);
        require(tx.time < timeout);
        require(checkSig(sellerSig, sellerPubKey));
        
        // Full refund to seller (no Bizum received, no service provided)
        require(tx.outputs[0].value == tx.value - txFee);
        require(tx.outputs[0].lockingBytecode == sellerPubKey.lock());
    }
    
    // Timeout refund: Split refund if Elena never cashes out
    function refundTimeout(sig sellerSig) {
        require(tx.time >= timeout);
        require(checkSig(sellerSig, sellerPubKey));
        
        // Split refund:
        // - Merchant portion → Sender's refund address (Iris)
        // - Seller fee → Seller (earned for providing service)
        require(tx.outputs[0].value == merchantAmount);
        require(tx.outputs[0].lockingBytecode == senderRefundPubKey.lock());
        require(tx.outputs[1].value == sellerFee);
        require(tx.outputs[1].lockingBytecode == sellerPubKey.lock());
    }
}
```

**Security properties:**
- ✅ Merchant cannot claim BCH without seller signature (both must sign for normal execution)
- ✅ Seller cannot reclaim BCH unless timeout expires (both signatures needed or timeout)
- ✅ Iris gets refund if Elena never cashes out (merchant portion returned automatically)
- ✅ Seller always earns fee (compensation for time/risk/capital lockup)
- ✅ Exact amounts enforced by covenant (merchant gets exactly required BCH, no more)
- ✅ Immutable once deployed (no one can change terms)

---

### Timeout & Refund Mechanisms

**Complete timeout cascade with three execution paths:**

#### Path 1: Normal Execution (Within 24h) ✅

```
Timeline:
├─ T+0min:  Seller accepts, posts 0.107 BCH
├─ T+2min:  Iris sends €100 Bizum
├─ T+3min:  Seller bot detects Bizum → signs condition 2
├─ T+45min: Elena goes to merchant, hands cash
├─ T+46min: Merchant signs condition 1
└─ MATURE:  Covenant executes

Distribution:
├─ Merchant receives: 0.0995 BCH (merchant portion)
└─ Seller receives: 0.0075 BCH (fee + overcollateralization)

Everyone happy! ✅
```

---

#### Path 2: No Bizum Received (0-5 min window) 🔄

```
Timeline:
├─ T+0min: Seller accepts, posts 0.107 BCH
├─ T+5min: Bizum window expires (Iris never paid)
└─ CANCEL: Seller calls cancelNoBizum()

Refund:
└─ Seller receives: 0.107 BCH (full amount back)

Why:
- No Bizum received = no service provided
- Seller gets full refund (no penalty)
- Seller wasted 5 minutes (opportunity cost)
- Iris loses nothing (never paid)
```

**5-minute window rationale:**
- Short enough to not significantly affect seller's capital efficiency
- Long enough for Bizum to process (typically instant, but allows for delays)
- Typical BCH volatility in 5 min: 0.5-1% (well within 7% buffer)
- Seller has full BCH exposure during this window (acceptable risk for BCH holders)

---

#### Path 3: Bizum Received, But Elena Never Cashes Out (5 min - 24h) 💰

```
Timeline:
├─ T+0min:  Seller accepts, posts 0.107 BCH
├─ T+2min:  Iris sends €100 Bizum (condition 2 met)
├─ T+24h:   Timeout expires (Elena never went to merchant)
└─ TIMEOUT: Seller calls refundTimeout()

Refund (split):
├─ Iris's address receives: 0.0995 BCH (merchant portion)
└─ Seller receives: 0.0075 BCH (fee earned)

Seller also keeps: €100 Bizum (already received)

Accounting:
├─ Seller: Posted 0.107 BCH, received €100 + 0.0075 BCH
│          Net: €107.50 - €107 = €0.50 profit ✅
│
└─ Iris:   Paid €100 Bizum, received back 0.0995 BCH
           Net: Lost €0.50 (cost of failed transaction) ❌
```

**Why this refund split:**
- ✅ Seller earned their fee (provided service: posted BCH, took risk, locked capital)
- ✅ Iris gets 99.5% refund (fair for transaction that didn't complete)
- ✅ Iris pays 0.5% cost (incentive to ensure Elena is ready before creating covenant)
- ✅ All automatic (no manual Bizum returns needed)

**Incentive alignment:**
- Seller incentivized to accept (always earns fee, even if Elena doesn't show)
- Iris incentivized to coordinate with Elena (loses €0.50 if timeout)
- Elena has no penalty (she never participated)

---

### Timing Windows Summary

| Window | Duration | Trigger | Refund Path | Who Wins |
|--------|----------|---------|-------------|----------|
| **Bizum payment** | 0-5 min | Iris must pay seller | `cancelNoBizum()` | Seller gets full BCH back |
| **Elena cash-out** | 5 min-24h | Elena+merchant must co-sign | `refundTimeout()` | Iris gets merchant portion, seller keeps fee |
| **Normal** | < 24h | Both conditions met | `mature()` | Everyone wins ✅ |

**Seller's hedge activates AFTER receiving Bizum** (typically ~2-3 min after acceptance).

**5-minute exposure window:**
- Seller has full BCH exposure while waiting for Bizum
- Typical volatility: 0.5-1% (well within 7% buffer)
- Acceptable risk for anyone who normally holds BCH through full market swings
- Once Bizum received, hedge activates (94-97% exposure reduction)

---

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
├─ Merchant: 0.5% (provides cash + BCH acceptance)
└─ Protocol: 0% (NO protocol fee = pure bulletin board)
```

**Why no protocol fee:**
Taking a protocol fee suggests Asgaya provides a service, which would trigger 
MiCA/PSD2 licensing requirements. Zero protocol fee = information society service 
= regulatory exemption (per RS052 analysis).

**Authoritative source:** See [fee-splitting-model.md](../decisions/fee-splitting-model.md) for detailed rationale.

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
- [Pull System](./pull-system.md) — Decentralized bulletin board architecture
- [Core Architecture: Why No Custody](../core-architecture/why-no-custody-no-intermediation.md) — Detailed legal reasoning (to be created)

---

**Documented:** May 10, 2026  
**Discovery:** Suso + Yakyak (OpenYak)  
**Analysis:** Coordination  
**Status:** Proposed architecture — pending Chipnet testing, legal review, and team consensus
