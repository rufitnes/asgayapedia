# Covenant Version History: Phase 1 → v2.3

**Purpose:** Track covenant evolution from initial Phase 1 implementation through simplified v2.2 design.

**Key insight:** Evolution from complex (enforce everything on-chain) to simple (covenant enables, client enforces).

---

## Quick Reference

| Version | Date | Status | Key Feature |
|---------|------|--------|-------------|
| **Phase 1** | 2026-07-23 | ✅ Tested | MTP-only refund (baseline) |
| **v2.0** | 2026-07-24 | ✅ Tested | Oracle-based refund (fast path) |
| **v2.1** | 2026-07-24 | ✅ Tested | Price drop protection (7% threshold) |
| **v2.2** | 2026-07-24 | ⏳ Testing | Simplified refund (sender anytime) |
| **v2.3** | TBD | 📋 Planned | Seller buffer recovery |

---

## Phase 1: The Baseline (MTP-Only Refund)

**Date:** 2026-07-23  
**Status:** ✅ Tested on chipnet  
**Archive:** `ARCHIVE_price-oracle_20260723.cash`

### What It Did

Basic price oracle covenant with MTP (Median Time Past) fallback for timeout:

```cash
function claim(sig recipientSig, datasig oracleSig, bytes oracleMessage) {
    // Verify oracle signature + timestamp
    // Calculate BCH amount from EUR price
    // Pay recipient + seller buffer
}

function refund(sig senderSig) {
    require(tx.time >= expiryMTP);  // MTP fallback
    // Refund to sender
}
```

### What We Learned

**✅ What worked:**
- Oracle signature verification (checkDataSig) ✅
- Price-based payment calculation ✅
- EUR-denominated covenant ✅
- Exact payment amounts enforced ✅

**❌ What didn't work:**
- **MTP too slow on chipnet** (hours of waiting, not 5 minutes)
- Poor UX for testing (can't iterate quickly)
- No fast path for legitimate refunds

**Critical bug discovered:** Little-endian vs big-endian byte order! CashScript reads bytes as little-endian, but we were creating oracle messages as big-endian. 3 hours of debugging. 🐛

**Fix:** Changed `writeBigInt64BE()` → `writeBigInt64LE()` in oracle signature creation.

### Chipnet Testing Results

- **Covenant created:** ✅
- **Funded:** ✅ 0.0075 BCH
- **Claim:** ✅ Successfully claimed 0.007 BCH to recipient, 0.00049 BCH to seller
- **TXID:** `a3bbf89a895c4e7e...`

**Phase 1 complete!** 🎉 First successful price oracle covenant on chipnet.

---

## v2.0: Oracle-Based Refund (Fast Path)

**Date:** 2026-07-24  
**Status:** ✅ Tested on chipnet  
**Motivation:** Solve MTP slowness on chipnet

### What Changed

Added oracle fast path for refund:

```cash
function refund(sig senderSig, datasig oracleSig, bytes oracleMessage) {
    require(checkSig(senderSig, sender));
    
    // Parse oracle timestamp
    int oracleTimestamp = int(oracleMessage.split(8)[0]);
    
    // Allow refund if EITHER condition met:
    bool oracleExpired = oracleTimestamp >= expiryOracleTime;
    bool mtpExpired = tx.time >= expiryMTP;
    
    require(oracleExpired || mtpExpired);
    
    // Refund to sender
}
```

### Why This Was Better

**✅ Improvements:**
- Fast refund via oracle (5 minutes vs hours)
- MTP still available as trustless fallback
- Enables practical testing on chipnet
- Better UX (no waiting for blockchain time)

**⚠️ Trade-offs:**
- Oracle dependency for refund (not fully permissionless)
- More complex logic (two time paths)
- Oracle could go offline (sender stuck until MTP)

### Chipnet Testing Results

- **Covenant created:** ✅
- **Funded:** ✅ 0.0075 BCH
- **Expiry:** 5 minutes (oracle time)
- **Refund:** ✅ Successfully refunded via oracle signature (no MTP wait!)
- **TXID:** `dd743868a0c19c2c...`

**v2.0 validated:** Oracle-based refund works! Fast testing possible.

---

## v2.1: Price Drop Protection (7% Threshold)

**Date:** 2026-07-24  
**Status:** ✅ Tested on chipnet  
**Motivation:** Requirement #4 - Automatic abort on price drops >7%

### What Changed

Added price floor enforcement in claim:

```cash
contract PriceOracle(
    // ... existing params ...
    int initialBchPriceInCents,  // NEW: Price when covenant funded
    int minPricePercent          // NEW: 93 = allow 7% drop max
)

function claim(...) {
    // Calculate current price from oracle
    int currentPrice = int(oracleMessage.split(8)[1]);
    
    // Calculate floor (93% of initial price)
    int priceFloor = (initialBchPriceInCents * minPricePercent) / 100;
    
    // Reject claim if price dropped too much
    require(currentPrice >= priceFloor);
    
    // ... rest of claim logic ...
}
```

### Why This Was Needed

**Problem:** If BCH price drops 10% during payment window:
- Recipient claims at low price → gets underpaid
- Sender exposed to tail risk volatility
- H€/HAu minting critical for stability

**Solution:** Covenant rejects claim if price < floor. Forces automatic refund + H€ minting.

### Chipnet Testing Results

**Test scenario:** Initial price €1000/BCH, price drops to €920/BCH (-8%)

- **Covenant created:** ✅ initialPrice = 100000 (€1000), minPercent = 93
- **Funded:** ✅ 0.0075 BCH
- **Oracle signature:** €920/BCH (below €930 floor)
- **Claim attempt:** ❌ Rejected! `currentPrice < priceFloor`
- **Result:** Claim blocked as expected ✅

**v2.1 validated:** Price drop protection works! Covenant enforces 7% threshold.

**⚠️ Limitation discovered:** If price drops at t=5, sender must wait until t=60 (expiry) to refund. Can't immediately recover BCH even though claim is impossible.

---

## v2.2: Simplified Refund (Covenant Simplicity Principle)

**Date:** 2026-07-24  
**Status:** ⏳ Deployed, testing in progress  
**Motivation:** User sovereignty over safety theater

### The Philosophical Shift

**Realization:** We kept adding complexity to handle edge cases. Moving logic to the client solved all issues at once.

**Core principle:**
> **Covenant = technical capability** ("CAN refund anytime")  
> **Client = business logic** ("SHOULD refund when appropriate")

**Tagline:** *The app enforces fairness. The covenant enforces ownership.*

### What Changed

**Removed all conditions from refund:**

```cash
function refund(sig senderSig) {
    require(checkSig(senderSig, sender));  // Just verify ownership
    
    // Output 0: Payment → sender
    // Output 1: Buffer → seller
}
```

**Client enforces fairness:**

```javascript
// Client decides when auto-refund is appropriate
async function shouldAutoRefund() {
    const timeExpired = currentTime >= expiryTime;
    const priceDropped = currentPrice < priceFloor;
    return timeExpired || priceDropped;
}

// Auto-refund when conditions met
if (shouldAutoRefund()) {
    await covenant.refund(senderKeypair);
}
```

### Why This Is Better

**✅ Benefits:**
- Simple covenant (easier to audit, less attack surface)
- Oracle only needed for claim (not refund)
- Immediate price-drop refund possible (no waiting for expiry)
- Client handles all business logic (updateable without redeployment)
- Emergency manual refund always available (permissionless!)

**⚠️ Trade-offs:**
- Two-layer mental model (covenant vs client)
- Recipient trusts sender won't abuse early refund
- Social layer needed (reputation, Nostr monitoring)

**Why we accept trade-offs:**
- Sender funds the infrastructure → deserves flexibility
- UX hides refund button (prevents impulsive refunds)
- Covenant is permissionless, app is opinionated
- Same pattern as Bitcoin (protocol allows, wallets guide)

### What This Enables

**For senders:**
- Zero-friction UX (set conditions once, system auto-refunds)
- Emergency escape (manual refund if client fails)
- Price protection + timeout protection (automatic)

**For developers:**
- Iterate on UX without redeploying covenant
- Add new conditions (e.g., "refund if recipient offline 24hr")
- Fix bugs in client without touching covenant

**For auditors:**
- Simple covenant (3 functions, <100 lines)
- Clear security model (less code = smaller attack surface)
- Easy to verify (no complex time/price logic)

### Testing Status

- **Covenant created:** ✅
- **Funded:** ⏳ Waiting for more testnet BCH
- **Simplified refund:** 📋 Testing planned
- **Auto-refund monitoring:** 📋 Client implementation in progress

---

## v2.3: Seller Buffer Recovery (Planned)

**Date:** TBD  
**Status:** 📋 Planned  
**Motivation:** Requirement #6 - Prevent seller capital lock

### The Edge Case

**Problem scenario:**
```
t=0:     Covenant funded (payment + buffer)
t=45:    💥 Sender device crashes / offline
t=60:    ⏰ Expiry reached
         ├─ Recipient can't claim (past expiry)
         └─ Sender can't refund (device offline)
         
Result:  🔒 Seller's buffer locked forever!
```

**Current v2.2:** Only 2 functions (claim, refund). If sender offline → seller stuck.

### The Solution

Add third function for seller to recover buffer after expiry:

```cash
function sellerRecoverBuffer(sig sellerSig, datasig oracleSig, bytes oracleMessage) {
    require(checkSig(sellerSig, seller));
    require(checkDataSig(oracleSig, oracleMessage, oraclePubkey));
    
    // Verify covenant has expired
    int oracleTimestamp = int(oracleMessage.split(8)[0]);
    require(oracleTimestamp >= expiryOracleTime);
    
    // Fair split (even though sender offline, sender gets payment back)
    // Output 0: Payment amount → sender address
    // Output 1: Buffer → seller address
}
```

### Why This Is Needed

**Capital efficiency:**
- Sellers provide liquidity infrastructure
- Buffer can't be locked indefinitely
- Real-world failure mode (devices crash/go offline)

**Three recovery paths:**
1. **Recipient claims** (before expiry) → seller gets buffer ✅
2. **Sender refunds** (anytime) → seller gets buffer ✅
3. **Seller splits** (after expiry, sender offline) → seller gets buffer ✅

**Same principle, different actor:** Seller gets independent recovery path. Simplicity doesn't mean fewer functions—it means each function does one clear thing.

### Testing Plan

**Scenario 1: Normal case (sender online)**
- Covenant expires
- Sender device auto-refunds
- Seller gets buffer ✅

**Scenario 2: Sender offline (edge case)**
- Covenant expires
- Sender device offline
- Seller calls `sellerRecoverBuffer()`
- Seller gets buffer, sender gets payment ✅

**Scenario 3: All offline**
- Covenant expires
- All devices offline
- When seller comes back online → recovers buffer ✅

---

## Evolution Summary Table

| Version | Problem Solved | Problem Created | Key Lesson |
|---------|---------------|-----------------|------------|
| **Phase 1** | Basic refund path | MTP too slow (hours) | Chipnet needs fast paths |
| **v2.0** | Fast refund (5 min) | Oracle dependency for refund | Oracle ≠ always available |
| **v2.1** | Claim rejects below floor | Can't refund on price drop before expiry | Covenant shouldn't trap funds |
| **v2.2** | Simple covenant, emergency escape | Two-layer mental model | User sovereignty > safety theater |
| **v2.3** | Seller capital recovery | (none - completes the design) | Three recovery paths = robust |

**The realization:** We kept adding complexity to handle edge cases. Moving logic to the client solved all issues at once.

---

## Critical Bugs Discovered

### 1. Byte Endianness (Phase 1)

**Bug:** CashScript reads bytes as little-endian, but we created oracle messages as big-endian.

**Symptom:** Covenant rejected claims with valid oracle signatures. Price value completely wrong.

**Fix:** Changed `writeBigInt64BE()` → `writeBigInt64LE()` in oracle signature creation.

**Time lost:** 3 hours of debugging 😅

**Lesson:** Always verify wire format when integrating off-chain data with on-chain logic.

### 2. Seller Buffer Lock (Discovered between v2.2 and v2.3)

**Bug:** If sender device offline after expiry, seller's buffer permanently locked.

**Symptom:** Only 2 functions (claim, refund). No recovery path for seller if sender offline.

**Fix:** Add `sellerRecoverBuffer()` function (v2.3).

**Lesson:** Edge cases matter. Real-world devices fail. Capital efficiency requires all participants to have recovery paths.

---

## Testing Methodology

### Chipnet vs Regtest Trade-offs

**Chipnet (testnet4):**
- ✅ Real network conditions
- ✅ Tests Electrum integration
- ✅ Validates oracle signatures
- ❌ Slow (MTP can take hours)
- ❌ Requires testnet BCH from faucets

**Regtest:**
- ✅ Fast iteration (instant blocks)
- ✅ Unlimited coins (generate as needed)
- ✅ Full control (can simulate any scenario)
- ❌ No Electrum (CashScript limitation)
- ❌ Doesn't test real network conditions

**Strategy:**
- **Fast iteration:** Use regtest for development/debugging
- **Integration testing:** Use chipnet for final validation
- **Phase 1 used chipnet** because we needed to test oracle + Electrum integration

### Test Parameters Used

**Chipnet covenant params:**
```json
{
  "actors": {
    "sender": { "pubkey": "...", "address": "bchtest:qrzlve3y..." },
    "recipient": { "pubkey": "...", "address": "bchtest:qqq5vtgu..." },
    "seller": { "pubkey": "...", "address": "bchtest:qpwgshlma..." },
    "oracle": { "pubkey": "...", "address": "bchtest:qz6..." }
  },
  "payment": {
    "eurCents": 700,        // €7.00
    "bufferSats": 49000     // 0.00049 BCH (7% buffer)
  },
  "timelock": {
    "expiryOracleTime": 1721937723,  // 5 minutes for testing
    "expiryMTP": 1721941323          // 1 hour fallback
  }
}
```

**v2.1 price drop params:**
```json
{
  "initialBchPriceInCents": 100000,  // €1000/BCH
  "minPricePercent": 93              // 93% = 7% drop max
}
```

---

## Implementation Files

**Archived:**
- `ARCHIVE_price-oracle_20260723.cash` - Phase 1 covenant
- `ARCHIVE_claim-chipnet_20260723.mjs` - Phase 1 claim script (with LE fix!)
- `ARCHIVE_create-oracle-sig-chipnet_20260723.mjs` - Oracle signature (LE fix)

**Current:**
- `price-oracle-v2.2.cash` - Simplified refund covenant
- `price-oracle-v2.3.cash` - Planned (seller recovery)

**Testing scripts:**
- `test-price-oracle.sh` - Automated testing on regtest/chipnet
- `check-balance.mjs` - UTXO verification
- `claim-chipnet.mjs` - Claim transaction builder
- `refund-v2.0-chipnet.mjs` - Oracle-based refund
- `refund-v2.2-chipnet.mjs` - Simplified refund

---

## Key Decisions & Rationale

### 1. Why Simplify v2.2 Instead of Adding More Features?

**Initial instinct:** Add `refundPriceDrop()` function to v2.1.

**Better solution:** Remove all conditions from `refund()`, move logic to client.

**Why:**
- Covenant complexity grows with every edge case
- Client can handle all conditions (updateable!)
- Emergency escape preserved (permissionless)
- Same pattern as Bitcoin (protocol enables, wallet guides)

**Reference:** [Covenant Simplicity Principle](../../why-this-design/constraints/covenant-simplicity-principle.md)

### 2. Why Oracle for Claim but Not Refund?

**Claim requires oracle:**
- Need current BCH/EUR price to calculate payment amount
- Oracle signature proves price data is authentic
- If oracle offline → recipient can't claim (but sender can still refund!)

**Refund doesn't require oracle:**
- Sender owns the covenant
- Refund is sender's right (their capital)
- Client decides when auto-refund is appropriate
- Emergency manual refund always possible

**Asymmetry is intentional:** Recipient has deadline, sender has flexibility.

### 3. Why Seller Gets Buffer in All Scenarios?

**Fair compensation:**
- Seller provided liquidity (locked their BCH)
- Seller took on coordination work (monitored payment, funded covenant)
- Seller earned fee (0.5% markup on BCH purchase)

**Three scenarios:**
1. **Recipient claims** → Seller gets buffer ✅
2. **Sender refunds** → Seller gets buffer ✅
3. **Seller splits** (v2.3) → Seller gets buffer ✅

**Capital efficiency:** Seller must be able to recover buffer in all cases, or liquidity dries up.

---

## Lessons Learned

### Technical Lessons

1. **Byte order matters** - Always verify wire format (3 hours of debugging saved future devs)
2. **Chipnet MTP is slow** - Need fast paths for testing (oracle-based refund)
3. **Edge cases are real** - Devices crash, users go offline (plan for failure)
4. **Simple covenants > complex** - Moving logic to client solved multiple problems at once

### Design Lessons

1. **User sovereignty matters** - It's the user's money, covenant shouldn't trap it
2. **Permissionless at protocol, opinionated at app** - Same pattern as Bitcoin
3. **Capital efficiency drives adoption** - Seller must be able to recover buffer
4. **Two-layer mental model is worth it** - Simple covenant + smart client = robust system

### Process Lessons

1. **Test on real network** - Chipnet revealed MTP slowness (regtest wouldn't show this)
2. **Iterate quickly** - v2.0 → v2.1 → v2.2 in one day (fast feedback loop)
3. **Archive old versions** - ARCHIVE_*_YYYYMMDD.* for reference
4. **Document as you go** - This version history prevents knowledge loss

---

## Future Directions

### Potential v2.4+: Merchant Cashout Path

**Requirement #5:** Recipient can cash out at merchant (dual-signature).

**Current v2.2:** Only direct claim to recipient wallet.

**Planned enhancement:**
```cash
function claimCashOut(
    sig recipientSig,
    sig merchantSig,
    datasig oracleSig,
    bytes oracleMessage
) {
    // Verify both signatures (cosign = proof of cash handover)
    require(checkSig(recipientSig, recipient));
    require(checkSig(merchantSig, merchant));
    
    // Payment → merchant (not recipient!)
    // Buffer → seller
}
```

**Why later:** Phase 1.5 focuses on core stability (price drop, timeout, seller recovery). Merchant cashout is Phase 2 feature.

### Potential v3.0: Multi-Oracle Consensus

**Current:** Single oracle (Asgaya in Phase 0).

**Future:** Multiple independent oracles, median price consensus.

**Challenge:** More complex covenant logic (verify N signatures, calculate median).

**Trade-off:** Decentralization vs covenant simplicity.

**Decision:** Phase 0 validates single oracle. Phase 2+ considers multi-oracle if needed.

---

## Related Documents

**Design rationale:**
- [Covenant Simplicity Principle](../../why-this-design/constraints/covenant-simplicity-principle.md) - User sovereignty over safety theater
- [Requirements](../../why-this-design/requirements/README.md) - Requirement #4 (price drop), #6 (seller recovery)

**User experience:**
- [Auto-Refund UX](../../user-journeys/remittance/sender/auto-refund-ux.md) - Zero-friction protection for senders

**Implementation:**
- [Distributed Monitoring](../../the-mechanism/nostr-coordination/distributed-monitoring.md) - 3-device price monitoring
- [Testing Plan](../../../knowledge/meta/phase-1.5-testing-plan.md) - Phase 1.5 testing strategy

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Covenants](./README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Covenant Simplicity](../../why-this-design/constraints/covenant-simplicity-principle.md) | [Auto-Refund UX](../../user-journeys/remittance/sender/auto-refund-ux.md)

---

**Status:** Phase 1.5 - v2.2 deployed, v2.3 planned  
**Updated:** 2026-07-25
