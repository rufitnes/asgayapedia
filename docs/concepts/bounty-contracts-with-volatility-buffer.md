# With volatility buffer Bounty Contracts

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

Instead of a **central entity** holding funds, a **CashScript covenant** (BCH smart contract) holds **BCH + volatility buffer posted by the seller**. The contract only releases funds when both conditions are met:

1. **Merchant confirmed:** "I gave cash to recipient" (on-chain proof)
2. **Seller confirmed:** "I received Bizum from sender" (automated bot signature)

**If BCH price moves while waiting, the volatility buffer absorbs the volatility.**

Excess BCH returns to the seller. The recipient gets exactly the promised amount of local currency.

---

## 🎯 CRITICAL RISK ALLOCATION

**If BCH falls below collateral threshold (>7% drop):**

* ❌ **Merchant NEVER receives undercollateralized BCH**
* ❌ **Merchant NEVER bears BCH volatility risk**
* ✅ **Merchant either:**
  * Receives full EUR-equivalent BCH from a valid covenant
  * OR does not participate in settlement at all
* 🔄 **Under-collateralized covenants refund remaining BCH to the SENDER**
* ⚖️ **Therefore:**
  * **SENDER** bears tail volatility risk beyond collateral buffer
  * **MERCHANT** bears execution/liquidity risk only (providing local fiat)
  * **SELLER** bears opportunity cost + reliability reputation risk

**The merchant is NOT short BCH downside volatility.**

**Why this matters:** Merchants are local Venezuelan individuals providing physical cash. They cannot absorb crypto volatility losses. The covenant architecture ensures they only participate when settlement is guaranteed at the specified EUR value. Invalid covenants are rejected by their software before any cash changes hands.

**📖 See:** [Risk Allocation Principle](risk-allocation-principle.md) for the complete foundational principle, common misconceptions, technical implementation details, and comparison to traditional systems.

---

## How It Works — The Full Flow

### Covenant Creation & Claim Paths

**Key Architectural Decision:** Sender always creates the covenant (for simplicity), but recipient chooses how to claim.

**Covenant Creation:**
- Sender creates EUR-denominated covenant with recipient's address
- BCH Seller posts BCH + volatility buffer (107%)
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
- **Volatility buffer:** 7% (seller will post 107% of required BCH)

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
║  Requires: 0.0107 BCH (with volatility buffer 7%)         ║
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

**The contract now holds the BCH + volatility buffer.** No central escrow, no custody service—just code enforcing conditions.

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

## Who Bears Which Risk?

Understanding risk allocation across all participants:

| Risk Type | Sender | Merchant | Seller |
|-----------|--------|----------|--------|
| **BCH crash > collateral buffer (>7%)** | ✅ YES | ❌ NO | Partial (opportunity cost) |
| **BCH upside opportunity cost** | ✅ YES | ✅ YES | ✅ YES |
| **Local cash liquidity risk** | ❌ NO | ✅ YES | ❌ NO |
| **Covenant execution failure** | Partial | Partial | Partial |
| **Capital lockup (24h)** | ❌ NO | ❌ NO | ✅ YES |
| **Failed claim timing (recipient no-show)** | ✅ YES | ❌ NO | ✅ YES |
| **Fiat payment chargeback** | ❌ NO | ❌ NO | ✅ YES |

**Key insights:**
- **Merchant bears zero volatility risk** - only provides local liquidity when covenant is valid
- **Sender bears tail volatility risk** - gets depreciated BCH back if >7% drop
- **Seller bears opportunity cost** - capital locked for 24h, bears reliability reputation risk

---

## Volatility Protection via Volatility buffer

### Price Movement Scenarios

| Scenario | Example | Contract Behavior |
|----------|---------|-------------------|
| **BCH stable** | Required: 0.01 BCH, posted 0.0107 BCH | Merchant gets 0.01 BCH, seller gets 0.0007 BCH surplus |
| **BCH rises 5%** | 0.01 BCH now worth €105 | Merchant gets 0.0095 BCH (still €100), seller gets 0.0112 BCH surplus |
| **BCH drops 3%** | 0.01 BCH now worth €97 | Merchant gets 0.0103 BCH (still €100), seller gets 0.0004 BCH surplus |
| **BCH drops 8%** | 0.01 BCH now worth €92 | **MARGIN CALL** — seller can add BCH or covenant expires early |

> ⚠️ **CRITICAL: What DOES NOT Happen to Merchants**
>
> **Common misconception:** "If BCH crashes >7%, the covenant drains its remaining balance to the merchant."
>
> **This does NOT happen.** If the collateral falls below the EUR target:
> 
> 1. ❌ The covenant does **NOT** attempt to pay the merchant with insufficient BCH
> 2. ❌ The merchant does **NOT** receive partial payment
> 3. ❌ The merchant does **NOT** advance cash before covenant validation
> 4. ✅ The covenant expires early and refunds ALL remaining BCH to the **SENDER**
> 5. ✅ The merchant's software detects invalid covenant and refuses to participate
> 6. ✅ The transaction is cancelled - merchant never involved, takes zero loss
>
> **Merchants are completely isolated from BCH price crashes.** Their participation is conditional on covenant validity at the exact moment of cash exchange.

### Top-Up Opportunity (How Sellers Earn Reliability Rewards)

**When BCH price drops beyond the 7% buffer during the 24-hour window, sellers have a voluntary opportunity to earn rewards:**

---

#### Path 1: Automatic Fair Exchange (No Action Required)

**What happens by default:**

When the covenant's collateral value reaches exactly €100 (the fair exchange point), the covenant matures automatically at the 24-hour timeout:

**Automatic distribution:**
- Sender receives: €99.50 worth of BCH (merchant portion)
- Seller receives: €0.50 worth of BCH (processing fee)

**Net outcome for seller:**
- Received: €100 Bizum (upfront from sender)
- Returned: €99.50 worth of BCH (at current market rate)
- Net: **Fair exchange** — sold €100 worth of BCH for €100 fiat at current price

**This is NOT a penalty:**
- Seller effectively sold BCH at current market rate
- No loss, no punishment
- The sender bears the price risk (they opted into BCH exposure)

---

#### Path 2: Voluntary Top-Up (Reliability Rewards)

**During a 60-minute grace period** after the collateral hits €100, the seller can *choose* to add more BCH:

**Alert sent to seller:**
```
💡 TOP-UP OPPORTUNITY — Bounty #4729
BCH dropped 8%. Add 0.0003 BCH to keep covenant alive
and earn Reliability Rewards.

Current tier: 2 time extensions → Next reward at 3 time extensions
Time remaining: 57 minutes
```

**How to time extension:**
1. Seller already has €100 Bizum from sender
2. Buy additional BCH on exchange or via Asgaya bulletin
3. Broadcast BCH to covenant address
4. Covenant stays alive, transaction can complete normally

**Why time extension?**
- ✅ Transaction completes → Earn full 0.5% fee
- ✅ Build reliability reputation → Unlock rewards
- ✅ Capital recycling continues → Keep earning

---

#### Reliability Reward Tiers

**Sellers who demonstrate reliability by topping up earn escalating benefits:**

| Top-Ups Completed | Reward Tier | Transaction Limit | Bulletin Priority | Additional Benefits |
|-------------------|-------------|-------------------|-------------------|---------------------|
| **0 (Default)** | Standard | €200 | Normal listing | Base 0.5% fee |
| **1st time extension** | 🛡️ Reliable | €220 (+10%) | Normal listing | "Reliable Seller" badge |
| **3 time extensions** | 📊 Trusted | €260 (+30%) | Higher in search | Highlighted in app |
| **5 time extensions** | ⭐ Priority | €300 (+50%) | Top of listings | Auto-select eligible |
| **10 time extensions** | 🎯 Premium | €360 (+80%) | Featured placement | Premium badge + priority routing |
| **20+ time extensions** | 💎 Elite | €400 (+100%) | Always visible | Maximum visibility + limits |

**Example progression:**
```
Start: €200 limit, normal listing
→ 1st time extension: €220 limit, "Reliable" badge appears
→ 3rd time extension: €260 limit, moves to top half of listings
→ 5th time extension: €300 limit, auto-select enabled (passive income!)
→ 10th time extension: €360 limit, featured seller
→ 20th time extension: €400 limit, elite status (maximum rewards)
```

**Economic value of rewards:**
- **Higher limits** → Serve larger remittances → More volume per transaction
- **Better visibility** → More senders choose you → More 0.5% fees earned
- **Auto-select** → Bot handles everything → Passive income stream
- **Compounding effect** → More transactions → More time extension opportunities → Faster tier progression

**Opportunity cost of NOT topping up:**
- Lose chance to advance tiers
- Competitors with rewards get more visibility
- Miss out on [Capital Recycling Strategy](bounty-contracts-with-volatility-buffer.md#capital-recycling-strategy-the-sellers-business-model) compounding (≈360% APR)

---

#### Philosophy: Reward, Not Punish

**We don't penalize sellers for market movements.**

- Default outcome (no time extension) = Fair exchange at current market rate
- Time extension = Voluntary action that earns visibility and limit rewards
- Creates a race to the top for reliability, not a race to avoid punishment

**Similar models in DeFi:**
- MakerDAO: Vault owners can add collateral during liquidations (voluntary)
- Compound: Users can add collateral to avoid liquidation (voluntary)
- Aave: Borrowers can repay to improve health factor (voluntary)

**Asgaya**: Sellers can time extension to earn reliability status (voluntary, rewarded)

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
└─ Seller receives: 0.0075 BCH (fee + volatility buffer)

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
7. **Time extension collateral** if price drops >5%
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
| **Capital efficiency** | High (buy exact amount needed) | Lower (7% volatility buffer) |
| **Volatility risk** | Zero (escrow buys at moment needed) | Medium (seller absorbs ±7% swings) |
| **Profit model** | 1% fee split | 1% fee split + surplus if BCH rises |
| **Regulatory status** | MiCA CASP required | No license (own capital) |
| **Scalability** | Bottleneck (single escrow) | Distributed (many sellers) |

**Trade-off:** Lower capital efficiency, but legally viable.

---

## Capital Recycling Strategy: The Seller's Business Model

**Key insight:** Sellers don't just lock capital and wait. They run an automated market-making operation.

### The 5-10 Minute Refill Loop

**Optimal seller strategy with exchange integration:**

```
1. Accept bounty → Lock 0.107 BCH in covenant
2. Receive €100 Bizum → Instant notification (SMS parsed by bot)
3. Bot auto-executes → Buy €100 BCH on exchange at market rate
4. BCH confirms → 5-10 minutes (exchange withdrawal + blockchain confirmation)
5. Hot wallet refilled → 0.1 BCH ready for next bounty
6. Accept next bounty → Repeat loop
```

**Capital utilization:**

```
Single €100 capital:
├─ Without recycling: 1 bounty per 24h = €0.50/day
└─ With recycling: 12-24 bounties per day = €6-12/day

€1,000 capital:
├─ 10 concurrent covenants locked (€1,070 total)
├─ Each completes within 1-6 hours average
├─ Refill every 5-10 minutes enables continuous flow
└─ Daily throughput: 20-30 transactions × €0.50 = €10-15/day
```

**Monthly return calculation:**

```
Capital: €1,000
Daily transactions: 20 (conservative estimate with recycling)
Daily earnings: 20 × €0.50 = €10
Monthly earnings: €10 × 30 days = €300
Monthly return: 30% on €1,000 capital
Annual return: 360% APR
```

**This transforms 0.5% fee from "mediocre" to "highly attractive" through volume.**

---

### Market-Making Model, Not Speculation

**Seller isn't betting on BCH price direction — they're capturing spread on flow:**

**Traditional holding:**
```
Buy: 0.1 BCH at €1,000/BCH = €100
Hold: 24 hours
Price moves: ±5-10% typical
Risk: Full exposure on entire 0.1 BCH
Return: Pure speculation
```

**Covenant market-making:**
```
Sell: 0.1 BCH at €1,005/BCH (+0.5% markup) = €100.50 Bizum received
Lock: 0.107 BCH in covenant (includes 0.007 BCH collateral buffer)
Buy back: €100 BCH on exchange at market rate = 0.1 BCH
Exposure: Only 0.007 BCH collateral at risk (7% buffer)
Return: €0.50 fee (0.5%) + recycled capital in 5-10 min

Risk reduction: 93% less exposure (0.007 vs 0.1 BCH)
```

**The fee is captured upfront:**
- Seller sells BCH at +0.5% markup to sender
- Example: Market €1,000/BCH → Seller charges €1,005/BCH
- Sender pays €100 Bizum for 0.0995 BCH worth of remittance value
- Seller locks 0.107 BCH (at market rate €1,000) = €107 locked
- **Fee already in hand** (€0.50 premium in Bizum payment)

**Only the collateral buffer is exposed to volatility:**
- Total locked: 0.107 BCH
- Principal going to merchant: 0.1 BCH (€99.50 value)
- Collateral buffer: 0.007 BCH (€7 value at lock time)
- **Volatility exposure: 7% of locked amount, not 100%**

---

### Why Sellers Want Many Concurrent Covenants

**Each covenant = 0.5% fee earning over 24 hours**

**Scenario: Seller with €1,000 capital**

```
Morning (9 AM):
├─ Accept 10 bounties simultaneously
├─ Lock: 10 × 0.107 BCH = €1,070 worth
├─ Receive: 10 × €100 = €1,000 Bizum
├─ Bot auto-buys: €1,000 BCH on exchange
└─ Wait: 5-10 minutes for hot wallet refill

Mid-morning (10 AM):
├─ Hot wallet refilled with ~1 BCH
├─ First 3 recipients claim (covenants execute)
├─ Receive back: 3 × 0.0075 BCH surplus
├─ Accept 3 new bounties immediately
└─ Keep the wheel spinning

Afternoon (2 PM):
├─ 7 more covenants execute
├─ Accept 7 new bounties
└─ Continuous flow of fees

Evening (8 PM):
├─ 20 total transactions completed
├─ Earnings: 20 × €0.50 = €10
├─ Capital still working: 10 covenants overnight
└─ Tomorrow: Repeat
```

**The more covenants locked, the more fees earning simultaneously.**

**Capital efficiency improves with volume:**
- Low volume (1-2 covenants/day): 7% APR (not worth it)
- Medium volume (10 concurrent): 68% monthly return
- High volume (20-30/day with recycling): 360% APR

**This is why 0.5% fee works — it's a high-frequency, low-margin business model.**

---

### Why Miners Are Ideal Sellers

**BCH miners have natural advantages:**

1. **Steady BCH inflow** (mining rewards)
   - No need to buy BCH on exchanges
   - No exchange fees eating into profits
   - Unlimited inventory capacity

2. **Already running infrastructure**
   - Hot wallets for mining payouts
   - Automated systems (pool monitoring, payout scripts)
   - Technical capability to run seller bots

3. **Natural BCH exposure**
   - Already holding BCH as core business
   - Covenant locks don't change risk profile
   - Reduced exposure vs. just holding (7% buffer vs 100% exposure)

4. **Revenue diversification**
   - Mining rewards: Variable (difficulty, price, luck)
   - Seller fees: Steady (volume-driven, predictable)
   - Combined revenue stream more stable

**Example: Small-scale BCH miner**

```
Daily mining output: 0.5 BCH
Daily value at €1,000/BCH: €500

Mining revenue only:
└─ €500/day from block rewards

Mining + Asgaya seller:
├─ €500/day from block rewards
├─ Accept 50 bounties/day (using mined BCH)
├─ Earn: 50 × €0.50 = €25/day additional
└─ Total: €525/day (+5% revenue boost)

Monthly impact:
├─ Mining: €15,000/month
├─ Asgaya fees: €750/month
└─ Total: €15,750/month (+5% boost)
```

**No capital cost, pure upside for miners.**

---

### The Hedge Mechanism: Why Sellers Always Win

**Even in worst-case volatility scenarios, sellers are better off than holding:**

**Scenario 1: BCH Price Stable (0% movement)**

```
Seller locks: 0.107 BCH worth €107
Receives: €100 Bizum upfront
Buys back: €100 worth BCH = 0.1 BCH

After covenant executes:
├─ Merchant takes: 0.0995 BCH (€99.50 worth)
├─ Seller gets back: 0.0075 BCH (€7.50 worth = €7 buffer + €0.50 fee)
└─ Total BCH recovered: 0.1 BCH (bought) + 0.0075 BCH (surplus) = 0.1075 BCH

Seller's position:
├─ Net BCH: 0.1075 - 0.107 = +0.0005 BCH (rounding/appreciation)
├─ Net EUR: €0 (spent €100 buying BCH, received €100 Bizum)
└─ Profit: €0.50 fee (captured in markup)
```

**Scenario 2: BCH Drops 7% (Margin Call Threshold)**

```
Seller locks: 0.107 BCH worth €107 at €1,000/BCH
BCH drops to: €930/BCH (-7%)
Receives: €100 Bizum upfront (before drop)
Buys back: €100 worth BCH = 0.1075 BCH (at €930 rate, gets more BCH!)

After covenant executes:
├─ Covenant now worth: 0.107 × €930 = €99.51
├─ Merchant takes: €99.50 ÷ €930 = 0.107 BCH (almost all of it)
├─ Seller gets back: ~0 BCH (collateral consumed)
└─ Total BCH held: 0.1075 BCH (from buy-back)

Seller's position:
├─ Net BCH: 0.1075 BCH (vs 0.107 originally locked)
├─ Net EUR: €0
└─ Result: Seller GAINED 0.0005 BCH despite margin call!

Comparing to holding:
├─ Held 0.107 BCH: Now worth €99.51 (-€7.49 loss)
└─ Locked + recycled: Hold €100 worth of BCH = 0.1075 BCH
    Better off by: €0.49 + 0.0005 BCH extra
```

**The counterintuitive result: When BCH drops, sellers buy back MORE BCH with the same €100.**

**Scenario 3: BCH Rises 5%**

```
Seller locks: 0.107 BCH worth €107 at €1,000/BCH
BCH rises to: €1,050/BCH (+5%)
Receives: €100 Bizum upfront (before rise)
Buys back: €100 worth BCH = 0.0952 BCH (at €1,050 rate, gets less BCH)

After covenant executes:
├─ Covenant now worth: 0.107 × €1,050 = €112.35
├─ Merchant takes: €99.50 ÷ €1,050 = 0.0948 BCH
├─ Seller gets back: 0.107 - 0.0948 = 0.0122 BCH (€12.81 worth)
└─ Total BCH held: 0.0952 (bought) + 0.0122 (surplus) = 0.1074 BCH

Seller's position:
├─ Net BCH: 0.1074 - 0.107 = +0.0004 BCH
├─ Net EUR: €0
└─ Profit: €0.50 fee + €0.42 surplus appreciation

Comparing to holding:
├─ Held 0.107 BCH: Now worth €112.35 (+€5.35 gain)
└─ Locked + recycled: Hold €105 worth of BCH = 0.1074 BCH worth €112.77
    Better off by: €0.42 (still outperformed holding!)
```

**In ALL scenarios, the hedge protects sellers:**
- ✅ **Stable:** Earn €0.50 fee, no volatility impact
- ✅ **Price drops:** Buy back more BCH, lose less than holding
- ✅ **Price rises:** Still profitable, earn surplus
- ✅ **Even margin call:** Better outcome than holding equivalent BCH

**The economic alignment is clear: Completing transactions is ALWAYS better than ghosting.**

---

### Why Capital Recycling Changes Everything

**Old analysis (static):**
```
Lock €107 for 24 hours → Earn €0.50
Return: 0.47% per day = 171% APR
Verdict: "Okay, but not amazing"
```

**New analysis (with recycling):**
```
Lock €107 for 24 hours → But recycle every 5-10 minutes
Throughput: 20-30 transactions/day on same €1,000 capital
Earnings: €10-15/day
Return: 1-1.5% per day = 365-547% APR
Verdict: "Extremely attractive for market makers"
```

**Capital recycling is the key to making 0.5% fee economically viable.**

**Sellers are incentivized to:**
1. ✅ Accept as many bounties as possible (more fees)
2. ✅ Keep covenants locked (each one earning 0.5%)
3. ✅ Complete transactions quickly (free up capital for next round)
4. ✅ Run automated bots (manual operation can't achieve volume)
5. ✅ Minimize downtime (5-10 min refill vs 24h wait = 144x more transactions possible)

**The business model: High-frequency, low-margin liquidity provision.**

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
- Automated time extension (seller pre-authorizes bot to add collateral)
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
| **Volatility protection** | ✅ EUR in escrow (0% volatility) | ❌ Immediate BCH purchase | ✅ Volatility buffer |
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
