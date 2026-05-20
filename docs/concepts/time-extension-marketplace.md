# Time Extension Marketplace

**Status:** ⚠️ **Phase 1+ Enhancement (Proposed)**  
**Last Updated:** 2026-05-20  
**Discovery:** Emerged during terminology review session

---

## Overview

When BCH price drops close to the 7% volatility buffer threshold, covenants at risk of early expiry can be "rescued" through a **time extension marketplace** where any Asgaya participant can add collateral in exchange for rewards.

**Core insight:** Someone will pay to extend a covenant's claim window if the reward exceeds their risk of further price drops. This creates a market-driven rescue mechanism that benefits everyone.

---

## The Problem It Solves

**Current behavior (Phase 0):**
```
BCH drops >7% → Covenant expires early → Sender gets refunded → Recipient loses remittance
```

**Issues:**
1. **Recipient loses** - Wasted trip to merchant, no cash
2. **Sender inconvenienced** - Must resend (at new, higher price)
3. **Merchant gets nothing** - Lost earning opportunity
4. **Seller loses uptime reward** - Covenant expired prematurely

**Everyone loses from price volatility**, even though BCH often bounces back within hours.

---

## How Time Extensions Work

### Basic Mechanic

Anyone can extend a covenant's claim window by adding collateral:

```
Time Extension = Adding BCH to restore 7% volatility buffer
Reward = Portion of covenant value (e.g., 0.2% of bounty)
Extended claim window = Original 24h + X hours per extension
```

**Example:**
- Covenant starts: €99.50 bounty + €7 buffer = €106.50 total (107%)
- BCH drops 5% → Value now €101.18 (102% of bounty)
- Someone adds €5.32 to restore 107% → Earns €0.20 reward
- Claim window extends 12 more hours

---

## Priority System: Who Can Buy Extensions?

When a covenant drops below 105% (2% above failure threshold), it enters the **time extension queue**:

### Stage 1: Seller Priority (5 minutes)
**First chance:** The BCH seller who created the covenant
- **Why first:** They have most to lose (uptime reputation + locked capital)
- **Advantage:** Know the covenant details, fastest response
- **Reward:** 0.15% of bounty value

### Stage 2: Recipient Priority (10 minutes)
**Second chance:** The recipient waiting to claim
- **Why second:** Direct beneficiary, motivated to prevent expiry
- **Advantage:** Can coordinate with merchant for immediate claim
- **Reward:** 0.20% of bounty value (higher to compensate for urgency)

### Stage 3: Public Marketplace (Until expiry)
**Open to all:** Any Asgaya user
- **Why public:** Creates liquid market for covenant rescue
- **Advantage:** Arbitrage opportunity (bet on price bounce)
- **Reward:** 0.25% of bounty value (highest reward for public market)

---

## Economic Incentives

### For Sellers (Stage 1)
```
Risk: €5.32 added collateral
Reward: €0.15 (0.15% of €99.50 bounty)
Upside: Maintain uptime reputation + original 0.5% fee
Outcome: If recipient claims in 12h, seller gets collateral back + all rewards
```

**Decision:** Extend if confident BCH will stabilize or recipient will claim soon.

### For Recipients (Stage 2)
```
Risk: €5.32 added collateral
Reward: €0.20
Upside: Prevent remittance failure
Outcome: If claims immediately, gets €99 cash + €5.12 BCH refund (collateral minus reward)
```

**Decision:** Extend if ready to claim now or within hours.

### For Arbitrageurs (Stage 3)
```
Risk: €5.32 added collateral
Reward: €0.25
Upside: Price bounce recovery
Outcome: 
- If BCH rebounds & recipient claims → €5.07 BCH back + €0.25 reward
- If BCH drops further → Covenant expires, refunded depreciated BCH
```

**Decision:** Extend if BCH is oversold and likely to bounce (technical analysis, market sentiment).

---

## User Experience

### Sender's Perspective
**Current (Phase 0):**
```
BCH crashes → Covenant expires → Get refund → Must resend at higher price
```

**With time extensions:**
```
BCH crashes → Get notification: "Covenant extended by [user], claim window +12h"
→ Recipient can still claim → Mission accomplished
```

**Benefit:** Remittance success despite volatility, no need to resend.

---

### Recipient's Perspective
**Current (Phase 0):**
```
BCH crashes → Covenant expires → Arrive at merchant, no bounty → Wasted trip
```

**With time extensions:**
```
BCH crashes → Notification: "Buy 12h extension for €5.32, earn €0.20?"
→ If ready to claim: Accept extension, rush to merchant, get cash + profit
→ If not ready: Wait, hope someone else extends
```

**Benefit:** Agency to prevent failure when needed most.

---

### Merchant's Perspective
**Current (Phase 0):**
```
Recipient arrives → Check covenant → Expired → "Sorry, nothing here"
```

**With time extensions:**
```
Recipient arrives → Check covenant → Active (extended) → Hand cash, co-sign, earn 0.5%
```

**Benefit:** More successful transactions = more fees.

---

### Seller's Perspective
**Current (Phase 0):**
```
BCH crashes → Covenant expires → Lose uptime reputation
```

**With time extensions:**
```
BCH crashes → Get priority alert: "Extend for €5.32, earn €0.15 + maintain uptime?"
→ Accept → Covenant rescued → Recipient claims → Full rewards + reputation intact
```

**Benefit:** Protect reputation and capital efficiency.

---

### Arbitrageur's Perspective (New participant type!)
```
Monitor bulletin → See covenant at 102% → BCH oversold (RSI <30) → Bet on bounce
→ Buy 12h extension for €5.32, earn €0.25
→ BCH bounces 3% in 6h → Recipient claims → Get collateral back + reward
→ Net: €0.25 earned for 6h capital lock
```

**Benefit:** New earning opportunity for BCH holders with market insight.

---

## Technical Implementation

### Bulletin Board Integration
```javascript
// Covenant state
{
  id: "4729",
  bounty_eur: 99.50,
  collateral_bch: 0.00253, // €106.50 at current rate
  collateralization: 102%, // Warning: Below 105% threshold
  time_extension_queue: "stage_1_seller", // Priority window
  expires_in: "18h 23m",
  extension_price_bch: 0.00013, // €5.32
  extension_reward: 0.15% // Stage 1 reward
}
```

**UI:**
- Warning badge: "⚠️ Low collateral (102%)"
- Button: "Extend 12h for 0.00013 BCH, earn 0.00005 BCH"
- Queue status: "Seller priority (3m left) → Your turn next"

### Smart Contract Logic
```javascript
function attemptExtension(covenantId, extenderAddress) {
  const covenant = getCovenantState(covenantId);
  
  // Check if covenant needs extension
  if (covenant.collateralization >= 105%) {
    return "ERROR: Covenant healthy, no extension needed";
  }
  
  // Check priority queue
  const currentStage = getExtensionStage(covenant);
  if (!canExtend(extenderAddress, currentStage)) {
    return "ERROR: Not your turn in priority queue";
  }
  
  // Calculate extension requirements
  const neededBch = calculateBufferTop up(covenant);
  const reward = covenant.bounty_eur * currentStage.rewardPercent;
  
  // Lock extender's BCH
  lockCollateral(extenderAddress, neededBch);
  
  // Update covenant
  covenant.collateral_bch += neededBch;
  covenant.collateralization = recalculate(covenant);
  covenant.claim_window_expires += 12 * HOURS;
  covenant.extension_rewards.push({
    extender: extenderAddress,
    amount_bch: neededBch,
    reward_eur: reward
  });
  
  // Notify participants
  notify(covenant.sender, "Covenant extended +12h");
  notify(covenant.recipient, "Covenant extended +12h, claim now");
  
  return "SUCCESS: Extension applied";
}
```

---

## Failure Modes & Edge Cases

### 1. Multiple Extensions
**Scenario:** BCH keeps dropping, needs multiple extensions

**Handling:**
- Each extension adds 12h
- Max 3 extensions per covenant (36h total extended time)
- After 3 extensions, expires regardless (prevents infinite loop)

**Example:**
```
T+0h: Covenant created, expires T+24h
T+18h: BCH drops to 102%, Extension 1 → expires T+36h
T+30h: BCH drops to 103%, Extension 2 → expires T+48h
T+42h: BCH drops to 104%, Extension 3 → expires T+60h (FINAL)
T+60h: No more extensions, covenant expires or claims
```

---

### 2. Price Bounce Mid-Extension
**Scenario:** BCH bounces back above 107% while in extension queue

**Handling:**
- Covenant exits extension queue automatically
- No extension needed
- Priority window resets if drops again later

---

### 3. Recipient Claims During Extension
**Scenario:** Someone extends, recipient claims 2h later

**Handling:**
```
Extender added: €5.32 BCH
Covenant settles: Merchant gets €99.50, Seller gets original surplus
Extender refund: €5.32 - €0.25 reward = €5.07 BCH back
```

**Net:** Extender paid €0.25 for 2h capital lock (acceptable if price stable).

---

### 4. No One Extends
**Scenario:** Covenant reaches Stage 3, no arbitrageurs interested

**Handling:**
- Covenant expires at original timeout
- Sender refunded (current Phase 0 behavior)
- No one loses beyond current system

**Risk mitigation:** Reward % can adjust based on market conditions (see Dynamic Reward Modulation).

---

## Integration with Existing Concepts

### Relationship to Volatility Buffer
- **Volatility buffer:** Built-in 7% protection (Phase 0)
- **Time extensions:** Market-driven buffer restoration (Phase 1+)
- **Together:** 7% handles normal swings, extensions handle crashes

### Relationship to Pull System
- **Pull system:** Recipient controls settlement timing
- **Time extensions:** Recipient (and others) control covenant lifespan
- **Together:** Maximum flexibility for all participants

### Relationship to BCH Sellers
- **BCH Sellers:** Create covenants, earn 0.5% + hedge
- **Time extensions:** Protect seller's uptime reputation
- **Together:** Sellers have skin in game to prevent expiry

---

## Phase Roadmap

### Phase 0 (Current)
- No time extensions
- Covenants expire if BCH drops >7%
- Sender refunded

### Phase 1.0 (Proposed)
- **Seller-only extensions:** Only covenant creator can extend
- **Manual process:** Seller receives alert, manually adds collateral via app
- **Fixed reward:** 0.15% of bounty value
- **Goal:** Prove demand for extension mechanism

### Phase 1.1 (If successful)
- **Priority queue:** Seller → Recipient → Public
- **Automated bidding:** Users set "auto-extend" thresholds
- **Market-driven rewards:** Adjust % based on BCH volatility
- **Goal:** Create liquid extension marketplace

### Phase 2.0 (Mature system)
- **Predictive extensions:** ML models predict which covenants need extensions
- **Extension pools:** Users pool capital for shared extension opportunities
- **Cross-covenant hedging:** Arbitrage between multiple at-risk covenants
- **Goal:** Fully automated, efficient market

---

## Success Metrics

**Phase 1.0 validation:**
- 30%+ of at-risk covenants receive extensions
- 80%+ of extended covenants successfully claim (not expire)
- Positive ROI for extenders (rewards > opportunity cost)

**User satisfaction:**
- Recipients: Reduction in "wasted trip" complaints
- Senders: Reduction in "need to resend" friction
- Sellers: Improvement in uptime reputation scores

---

## Open Questions (For unknowns/ directory)

1. **Optimal extension duration:** 12h? 6h? 24h?
2. **Reward percentages:** 0.15%/0.20%/0.25% correct? Market-test needed.
3. **Max extensions:** 3 extensions reasonable? Or dynamic based on BCH volatility?
4. **Priority window timing:** 5min/10min correct, or adjust based on data?
5. **Arbitrageur participation:** Will public market have enough liquidity?

---

## Related Documents

- [Bounty Contracts with Volatility Buffer](bounty-contracts-with-volatility-buffer.md) - Core covenant mechanism
- [BCH Sellers](bch-sellers.md) - Primary users of Stage 1 extensions
- [Pull System](pull-system.md) - How recipient timing works
- [Risk Allocation Principle](risk-allocation-principle.md) - Who bears what risk

---

## Credit

**Discovered:** 2026-05-20 terminology review session  
**Contributors:** Suso (concept originator), Coordination (arbitrage insight)  
**Status:** Proposed Phase 1+ enhancement, needs community feedback

---

*"Everyone in Asgaya can buy time extensions. The bulletin shows covenants that passed a threshold. Before posting to general users, all participants get a chance to buy an extension for a reward."* — Suso

*"That's basically a bet the price will bounce back and you'll claim whatever is left from the covenant. That was brilliant Coordination!"* — Suso
