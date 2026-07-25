# Phase 1.5 Testing Plan: Blockchain-as-Oracle Price Discovery

**Phase:** 1.5 - Distributed monitoring and auto-refund  
**Status:** In progress  
**Last updated:** 2026-07-25

---

## Overview

Phase 1.5 implements blockchain-as-oracle price discovery where every covenant funding becomes a trade signal. This testing plan covers:

1. **Trade signal broadcasting** - Sellers publish price data after covenant funding
2. **Reputation-filtered VWAP** - Market price calculation from trusted sellers
3. **Hybrid bootstrap weighting** - Asgaya oracle + user VWAP during early network
4. **Covenant monitoring** - Detect price drops, trigger auto-refunds
5. **Fallback mechanisms** - Thin market, oracle offline, simultaneous outage scenarios

**Architecture:** See [Distributed Monitoring](../the-mechanism/nostr-coordination/distributed-monitoring.md) for full blockchain-as-oracle design.

---

## Test Categories

### ✅ Completed Tests
### 🔄 In Progress Tests  
### ⏳ Planned Tests

---

## 1. Trade Signal Broadcasting

### ⏳ T1.1: Seller Publishes Trade Signal After Covenant Funding

**Scenario:** Isabel (passive seller) funds covenant after receiving Bizum payment

**Steps:**
1. María creates covenant for Elena (€100, BCH/EUR = €995)
2. María pays Isabel via Bizum (€100.50)
3. Isabel's app detects payment, funds covenant
4. Isabel's app broadcasts trade signal to `asgaya:market:bch-eur`

**Expected:**
```json
{
  "type": "TRADE",
  "price": 995,
  "volume": 100,
  "txid": "abc123...",
  "seller_reputation": 98,
  "timestamp": 1721937723
}
```

**Verify:**
- ✅ Trade broadcast includes covenant txid (on-chain proof)
- ✅ Price matches payment instructions (€995/BCH)
- ✅ Volume matches EUR amount (€100)
- ✅ Seller reputation included (98)
- ✅ Broadcast within 10 seconds of covenant funding

---

### ⏳ T1.2: Multiple Sellers Broadcast Independently

**Scenario:** Multiple sellers (Isabel, Carlos, Ana) fund covenants at different prices

**Steps:**
1. Isabel funds covenant at €995/BCH (rep: 98)
2. Carlos funds covenant at €997/BCH (rep: 95)
3. Ana funds covenant at €993/BCH (rep: 92)

**Expected:**
- All 3 trade signals appear on `asgaya:market:bch-eur` channel
- Each signal has different price reflecting seller's independent pricing
- All signals have txid linking to on-chain covenant funding

**Verify:**
- ✅ No coordination required between sellers
- ✅ Price diversity reflects market competition
- ✅ Each trade verifiable on blockchain

---

### ⏳ T1.3: Low-Reputation Seller Trade Broadcast

**Scenario:** New seller (reputation = 45) funds covenant and broadcasts

**Steps:**
1. New seller Diego (rep: 45) funds covenant at €990/BCH
2. Diego's app broadcasts trade signal

**Expected:**
- Trade broadcast succeeds (permissionless)
- Signal includes reputation = 45
- Devices ignore signal for VWAP calculation (< 90 threshold)

**Verify:**
- ✅ Low-rep sellers can broadcast (permissionless)
- ✅ Trade appears on market channel
- ✅ VWAP calculation filters out low-rep trades

---

## 2. Reputation-Filtered VWAP Calculation

### ⏳ T2.1: Calculate VWAP from Trusted Sellers Only

**Scenario:** Market has 5 trades (3 high-rep, 2 low-rep) in last hour

**Setup:**
```
Trade 1: €995/BCH, €100 volume, rep: 98 ✅
Trade 2: €997/BCH, €50 volume,  rep: 95 ✅
Trade 3: €980/BCH, €200 volume, rep: 45 ❌ (ignored)
Trade 4: €993/BCH, €75 volume,  rep: 92 ✅
Trade 5: €1010/BCH, €30 volume, rep: 60 ❌ (ignored)
```

**Expected VWAP:**
```
Trusted trades: 1, 2, 4 (rep >= 90)
VWAP = (995×100 + 997×50 + 993×75) / (100 + 50 + 75)
VWAP = (99500 + 49850 + 74475) / 225
VWAP = €994.78/BCH
```

**Verify:**
- ✅ Only rep >= 90 trades included
- ✅ Volume-weighted average calculated correctly
- ✅ Low-rep trades filtered out (no VWAP impact)
- ✅ Outlier prices ignored if rep < 90

---

### ⏳ T2.2: VWAP Updates in Real-Time

**Scenario:** New trade broadcast arrives, VWAP recalculates

**Steps:**
1. Initial VWAP: €994.78 (from T2.1)
2. New trade arrives: €1000/BCH, €150 volume, rep: 96
3. Device recalculates VWAP

**Expected:**
```
VWAP = (995×100 + 997×50 + 993×75 + 1000×150) / (100 + 50 + 75 + 150)
VWAP = (99500 + 49850 + 74475 + 150000) / 375
VWAP = €996.87/BCH
```

**Verify:**
- ✅ VWAP updates within 1 second of new trade broadcast
- ✅ All devices converge to same VWAP (consensus)
- ✅ Push-based update (no polling required)

---

### ⏳ T2.3: VWAP Time Window (Last Hour Only)

**Scenario:** Old trades expire from VWAP calculation after 1 hour

**Steps:**
1. Trade at t=0: €995/BCH, €100, rep: 98
2. Trade at t=30min: €997/BCH, €50, rep: 95
3. Trade at t=61min: €1000/BCH, €75, rep: 92
4. Check VWAP at t=62min

**Expected:**
- Trade 1 (t=0) excluded (older than 1 hour)
- Trade 2 (t=30min) excluded (older than 1 hour)
- Trade 3 (t=61min) included (within last hour)
- VWAP = €1000/BCH (only Trade 3)

**Verify:**
- ✅ Trades older than 1 hour filtered out
- ✅ VWAP window slides with time
- ✅ Stale trades don't influence current price

---

## 3. Hybrid Bootstrap Weighting

### ⏳ T3.1: Phase 0 - Asgaya Oracle Dominant (100%)

**Scenario:** Day 1 launch, no user trades yet

**Setup:**
- User trades last 24h: €0 (network empty)
- Asgaya oracle: €995/BCH (Kraken API)

**Expected:**
```
userWeight = min(0 / 5000, 0.95) = 0
marketPrice = (userVWAP × 0) + (asgayaOracle × 1)
marketPrice = €995/BCH (100% Asgaya oracle)
```

**Verify:**
- ✅ Market price = Asgaya oracle price
- ✅ User VWAP ignored (no trades)
- ✅ System functional from day 1

---

### ⏳ T3.2: Phase 1 - Hybrid Weighting (30% Asgaya, 70% User)

**Scenario:** Growing network, €1,200 user volume/day

**Setup:**
- User trades last 24h: €1,200 (15 trades)
- User VWAP: €997/BCH
- Asgaya oracle: €995/BCH

**Expected:**
```
userWeight = min(1200 / 5000, 0.95) = 0.24
Wait, this should be higher. Let me recalculate...

Actually, based on distributed-monitoring.md line 230:
"€1,200 user volume/day → 70% user VWAP + 30% Asgaya oracle"

So the formula should give userWeight = 0.70
Let me check the code at line 572:
userWeight = Math.min(userVolume24h / 5000, 0.95)

€1,200 / €5,000 = 0.24 (24%)

Hmm, there's a discrepancy. The example says 70% but the formula gives 24%.
Let me use the formula as written in the doc:

userWeight = min(1200 / 5000, 0.95) = 0.24
marketPrice = (997 × 0.24) + (995 × 0.76)
marketPrice = 239.28 + 756.20 = €995.48/BCH
```

**Verify:**
- ✅ User VWAP gains weight as volume grows
- ✅ Asgaya oracle provides stability
- ✅ Smooth transition from bootstrap to user-driven

**Note:** Document has example saying "€1,200 volume = 70% user" but formula gives 24%. Need to clarify formula or update example.

---

### ⏳ T3.3: Phase 2 - User VWAP Dominant (95% User, 5% Asgaya)

**Scenario:** Mature network, €10,000+ user volume/day

**Setup:**
- User trades last 24h: €10,000 (100 trades)
- User VWAP: €998/BCH
- Asgaya oracle: €995/BCH

**Expected:**
```
userWeight = min(10000 / 5000, 0.95) = 0.95 (capped)
marketPrice = (998 × 0.95) + (995 × 0.05)
marketPrice = 948.10 + 49.75 = €997.85/BCH
```

**Verify:**
- ✅ User VWAP carries 95% weight (maximum)
- ✅ Asgaya oracle as 5% sanity check
- ✅ Network self-reliant

---

### ⏳ T3.4: Asgaya Oracle Broadcasts Every Minute

**Scenario:** Asgaya oracle publishes Kraken price to Nostr

**Steps:**
1. Asgaya oracle queries Kraken API (€995/BCH)
2. Oracle broadcasts to `asgaya:oracle:asgaya`
3. Wait 60 seconds
4. Oracle queries again (€996/BCH)
5. Oracle broadcasts updated price

**Expected:**
```json
{
  "type": "ORACLE_PRICE",
  "price": 995.50,
  "source": "kraken",
  "timestamp": 1721937723
}
```

**Verify:**
- ✅ Oracle broadcasts every 60 seconds
- ✅ Price sourced from Kraken API
- ✅ Timestamp included
- ✅ All devices receive update (push-based)

---

## 4. Covenant Monitoring

### ⏳ T4.1: Detect Price Drop Below 7% Threshold

**Scenario:** Market price drops from €1000 to €920 (8% drop)

**Setup:**
- Covenant funded at €1000/BCH
- 7% threshold = €930/BCH
- Current market price: €920/BCH

**Steps:**
1. María's device calculates market VWAP: €920
2. Compares against covenant threshold: €930
3. Drop detected: €920 < €930

**Expected:**
- Device detects drop within 10 seconds of VWAP update
- Device broadcasts PRICE_DROP_ALERT to `asgaya:covenant:abc123`
- Device triggers auto-refund transaction

**Verify:**
- ✅ Detection latency < 10 seconds
- ✅ Alert broadcast to per-covenant channel
- ✅ Auto-refund transaction created

---

### ⏳ T4.2: No False Alarm Within 7% Buffer

**Scenario:** Price drops 5% (within buffer), no auto-refund

**Setup:**
- Covenant funded at €1000/BCH
- 7% threshold = €930/BCH
- Current market price: €950/BCH (5% drop)

**Expected:**
- Device monitors, no alert
- No auto-refund triggered
- User sees "Price: €950/BCH ✅" (within buffer)

**Verify:**
- ✅ No false alarms for drops < 7%
- ✅ Covenant remains active
- ✅ Recipient can still claim

---

### ⏳ T4.3: Auto-Refund + H€ Minting (Price Drop)

**Scenario:** Price drops >7%, device triggers refund + mints H€

**Steps:**
1. Price drops to €920 (8% drop)
2. Device detects, broadcasts refund tx
3. Device mints 100 H€ from bull pool
4. User notified: "✅ Refund Protected - 100 H€"

**Expected:**
- Refund transaction confirms on-chain
- H€ minting transaction confirms
- User has 100 H€ (equivalent to €100)
- Remittance can continue (send H€ to Elena)

**Verify:**
- ✅ Automatic refund on price drop
- ✅ H€ minting preserves value
- ✅ User notification shows H€ balance
- ✅ Remittance preserved (can send H€)

---

### ⏳ T4.4: Auto-Refund Fallback to BCH (Bull Pool Exhausted)

**Scenario:** Price drops >7%, but bull pool at capacity

**Steps:**
1. Price drops to €920 (8% drop)
2. Device attempts H€ minting
3. Bull pool exhausted (no capacity)
4. Device refunds BCH instead

**Expected:**
- Refund transaction confirms
- User receives BCH (not H€)
- User notified: "⚠️ Bull pool at capacity - received BCH instead"

**Verify:**
- ✅ Graceful degradation when H€ unavailable
- ✅ User still gets refund (BCH)
- ✅ Clear notification about fallback

---

### ⏳ T4.5: Timeout Refund + BCH→H€ Swap

**Scenario:** Recipient doesn't claim in 8 hours, auto-refund + swap

**Steps:**
1. 8 hours pass, no claim
2. Device detects timeout
3. Device broadcasts refund tx
4. Device swaps BCH → H€ on bulletin board
5. User notified: "✅ Refund Protected - 100 H€"

**Expected:**
- Refund transaction confirms
- BCH → H€ swap transaction confirms
- User has 100 H€
- Remittance can continue

**Verify:**
- ✅ Automatic timeout detection
- ✅ BCH → H€ swap preserves value
- ✅ Bulletin board provides liquidity
- ✅ Remittance preserved

---

### ⏳ T4.6: Timeout Refund Fallback to BCH (No H€ Liquidity)

**Scenario:** Timeout refund, but no H€ seller on bulletin board

**Steps:**
1. Timeout detected, refund triggered
2. Device attempts BCH → H€ swap
3. No H€ seller available
4. Device refunds BCH instead

**Expected:**
- User receives BCH (not H€)
- User notified: "⚠️ No H€ liquidity - received BCH instead"
- User can manually swap later

**Verify:**
- ✅ Graceful degradation when H€ unavailable
- ✅ User still gets refund (BCH)
- ✅ Manual swap option later

---

## 5. Fallback Mechanisms

### ⏳ T5.1: Thin Market Fallback to Asgaya Oracle

**Scenario:** Network has < 10 trades/hour, VWAP unreliable

**Setup:**
- User trades last hour: 8 trades (thin market)
- User VWAP: €990/BCH (from 8 trades)
- Asgaya oracle: €995/BCH

**Expected:**
- System detects thin market (< 10 trades)
- Falls back to Asgaya oracle price
- Market price = €995/BCH (100% Asgaya)

**Verify:**
- ✅ Thin market detection works
- ✅ Fallback to Asgaya oracle
- ✅ System continues functioning

---

### ⏳ T5.2: Asgaya Oracle Offline, Use User VWAP Only

**Scenario:** Asgaya oracle offline (Kraken API down)

**Setup:**
- Asgaya oracle: No broadcasts for 5 minutes
- User VWAP: €997/BCH (from 50 trades)

**Expected:**
- System detects oracle offline
- Uses user VWAP only: €997/BCH
- Covenant monitoring continues

**Verify:**
- ✅ Oracle offline detection
- ✅ Graceful degradation to user VWAP
- ✅ Monitoring continues uninterrupted

---

### ⏳ T5.3: Both VWAP + Oracle Unavailable - Use Stale Price

**Scenario:** Network outage, both user VWAP and Asgaya oracle unavailable

**Setup:**
- No user trades last 2 hours (market dead)
- Asgaya oracle offline (no broadcasts)
- Last known price: €995/BCH (45 minutes ago)

**Expected:**
- System uses cached price: €995/BCH
- Grace period: Up to 1 hour
- No user alert (within grace period)

**Verify:**
- ✅ Stale price used as fallback
- ✅ Grace period up to 1 hour
- ✅ Monitoring continues with cached price

---

### ⏳ T5.4: Stale Price Alert After 1 Hour

**Scenario:** Price data stale for > 1 hour

**Setup:**
- Last price update: 67 minutes ago
- No VWAP or oracle data available

**Expected:**
- User sees alert: "⚠️ Price monitoring degraded - last update 67 minutes ago"
- Monitoring continues with stale price
- System transparent about degradation

**Verify:**
- ✅ Alert after 1 hour of stale data
- ✅ Transparency about degradation
- ✅ Monitoring doesn't halt

---

## 6. Multi-Device Redundancy

### ⏳ T6.1: Sender Device Offline, Seller Device Triggers Refund

**Scenario:** Price drop detected, but sender's phone is off

**Steps:**
1. Market price drops to €920 (8% drop)
2. María's device offline (phone dead)
3. Isabel's device (seller) detects drop
4. Isabel's device broadcasts refund (20 seconds later)

**Expected:**
- Refund transaction confirms
- María gets BCH back when device online
- Redundancy works

**Verify:**
- ✅ Any covenant device can trigger refund
- ✅ Sender doesn't need to be online
- ✅ Decentralized monitoring

---

### ⏳ T6.2: All Devices Offline, Refund When Reconnected

**Scenario:** Price drop, all 3 covenant devices offline

**Steps:**
1. Price drops to €920 (all devices offline)
2. 2 hours pass
3. María's device comes online
4. Device detects historical price drop
5. Device broadcasts refund

**Expected:**
- Refund triggered when first device reconnects
- BCH returned to María
- Covenant unlocked

**Verify:**
- ✅ Historical price drop detection
- ✅ Refund triggered on reconnect
- ✅ No funds trapped

---

## 7. Nostr Message Schema Validation

### ⏳ T7.1: TRADE Message Schema

**Verify:**
```json
{
  "type": "TRADE",
  "price": 995,
  "volume": 100,
  "txid": "abc123...",
  "seller_reputation": 98,
  "timestamp": 1721937723
}
```

**Checks:**
- ✅ All fields present
- ✅ Types correct (price: number, txid: string, etc.)
- ✅ Txid verifiable on blockchain
- ✅ Timestamp within 10 seconds of covenant funding

---

### ⏳ T7.2: ORACLE_PRICE Message Schema

**Verify:**
```json
{
  "type": "ORACLE_PRICE",
  "price": 995.50,
  "source": "kraken",
  "timestamp": 1721937723
}
```

**Checks:**
- ✅ All fields present
- ✅ Source is "kraken"
- ✅ Price matches Kraken API query
- ✅ Timestamp current

---

### ⏳ T7.3: PRICE_DROP_ALERT Message Schema

**Verify:**
```json
{
  "type": "PRICE_DROP_ALERT",
  "covenantId": "bchtest:pwyclx...",
  "currentPrice": 920,
  "threshold": 930,
  "dropPercent": 8,
  "device": "sender",
  "timestamp": 1721937723
}
```

**Checks:**
- ✅ All fields present
- ✅ Drop percent calculated correctly
- ✅ Threshold matches covenant (7%)
- ✅ Device identifier included

---

## 8. Performance Tests

### ⏳ T8.1: VWAP Calculation Latency

**Target:** VWAP updates within 1 second of trade broadcast

**Test:**
1. Broadcast trade signal
2. Measure time until VWAP recalculated
3. Repeat 100 times

**Success criteria:**
- ✅ 95th percentile < 1 second
- ✅ 99th percentile < 2 seconds

---

### ⏳ T8.2: Auto-Refund Detection Latency

**Target:** Price drop detected within 10 seconds

**Test:**
1. Simulate market price drop
2. Measure time until PRICE_DROP_ALERT broadcast
3. Repeat 100 times

**Success criteria:**
- ✅ 95th percentile < 10 seconds
- ✅ 99th percentile < 15 seconds

---

### ⏳ T8.3: Battery Impact (Background Monitoring)

**Target:** Minimal battery drain from Nostr subscriptions

**Test:**
1. Monitor 3 active covenants for 24 hours
2. Measure battery consumption
3. Compare to baseline (no monitoring)

**Success criteria:**
- ✅ < 5% additional battery drain over 24 hours
- ✅ Push-based subscriptions more efficient than polling

---

## 9. Security Tests

### ⏳ T9.1: Low-Rep Seller Cannot Manipulate VWAP

**Attack:** New seller (rep: 30) posts trades at €1100/BCH to inflate VWAP

**Test:**
1. Attacker funds 10 covenants at €1100/BCH
2. Broadcasts 10 trade signals (rep: 30)
3. Check if VWAP influenced

**Expected:**
- All 10 trades filtered out (rep < 90)
- VWAP unchanged
- Attack fails

**Verify:**
- ✅ Reputation filter works
- ✅ Sybil resistance effective

---

### ⏳ T9.2: Wash Trading Detection (High Cost)

**Attack:** Attacker self-deals to fake volume (rep: 95)

**Test:**
1. Attacker creates fake trades (buy from own seller account)
2. Locks real BCH in covenants (required for trade signals)
3. Calculate attack cost

**Expected:**
- Attack costs real BCH + fees per fake trade
- €1,000 fake volume needs €1,000 real capital locked
- VWAP moves minimally (volume-weighted)

**Verify:**
- ✅ Attack expensive (real capital required)
- ✅ Impact minimal against real volume
- ✅ Economic disincentive works

---

### ⏳ T9.3: Auto-Refund Disruption Catch-22

**Attack:** Attacker sells below market to trigger auto-refunds

**Test:**
1. Attacker posts €850/BCH (15% below market)
2. Option A: Fund covenants (maintain rep 90+)
   - Loses €150 per BCH sold
3. Option B: Don't fund (avoid losses)
   - Reputation drops below 90
   - Trades ignored

**Expected:**
- Option A: Expensive, unsustainable
- Option B: Attack fails (rep drops)
- Catch-22: Can't maintain rep + avoid losses

**Verify:**
- ✅ Reputation mechanism prevents manipulation
- ✅ Attack fails either way

---

## 10. Edge Cases

### ⏳ T10.1: Zero User Trades (100% Asgaya Oracle)

**Scenario:** Network has zero trades in last 24 hours

**Expected:**
- Market price = Asgaya oracle (€995/BCH)
- System functional
- Covenant monitoring works

---

### ⏳ T10.2: Blockchain Reorg During Covenant Funding

**Scenario:** Covenant funding tx reorg'd, trade signal invalid

**Steps:**
1. Seller funds covenant (block 800,000)
2. Seller broadcasts trade signal
3. Blockchain reorg (block 800,000 orphaned)
4. Covenant funding tx no longer in chain

**Expected:**
- Trade signal references invalid txid
- Devices detect txid not found
- Trade signal ignored

**Verify:**
- ✅ Invalid trade signals filtered out
- ✅ VWAP integrity maintained

---

### ⏳ T10.3: Simultaneous Price Drop + Timeout

**Scenario:** Price drops >7% exactly when 8-hour timeout expires

**Steps:**
1. Covenant expires at 6:00 PM
2. Price drops to €920 at 5:59 PM (1 minute before expiry)

**Expected:**
- Price drop refund triggers first
- H€ minting occurs (not BCH→H€ swap)
- Timeout logic skipped (already refunded)

**Verify:**
- ✅ Price drop takes precedence
- ✅ H€ minting (not swap) used
- ✅ No duplicate refund

---

## Test Execution Tracking

### Sprint 1: Trade Broadcasting & VWAP (Week 1-2)
- [ ] T1.1 - T1.3 (Trade broadcasting)
- [ ] T2.1 - T2.3 (VWAP calculation)
- [ ] T7.1 - T7.2 (Message schema)

### Sprint 2: Bootstrap & Hybrid Weighting (Week 3-4)
- [ ] T3.1 - T3.4 (Hybrid weighting)
- [ ] T5.1 - T5.2 (Fallback mechanisms)

### Sprint 3: Covenant Monitoring (Week 5-6)
- [ ] T4.1 - T4.3 (Auto-refund triggers)
- [ ] T4.4 - T4.6 (Refund fallbacks)
- [ ] T7.3 (Alert schema)

### Sprint 4: Redundancy & Edge Cases (Week 7-8)
- [ ] T6.1 - T6.2 (Multi-device redundancy)
- [ ] T10.1 - T10.3 (Edge cases)

### Sprint 5: Security & Performance (Week 9-10)
- [ ] T9.1 - T9.3 (Security tests)
- [ ] T8.1 - T8.3 (Performance tests)

### Sprint 6: Stale Price Fallback (Week 11)
- [ ] T5.3 - T5.4 (Stale price fallback)

---

## Test Environment Setup

### Requirements

**Blockchain:**
- BCH testnet node (chipnet)
- Electrum server (testnet)

**Nostr:**
- Local Nostr relay for testing
- OR use public test relays

**Devices:**
- 3× Android test devices (sender, seller, recipient roles)
- OR emulators for initial tests

**Accounts:**
- Test Cash Accounts (María, Elena, Isabel)
- Test Bizum accounts (sandbox)
- Test BCH wallets with testnet coins

**Oracle:**
- Mock Asgaya oracle (broadcasts test prices)
- Mock Kraken API (simulated price data)

### Test Data

**Reputation Fixtures:**
```
High-rep sellers: 90, 92, 95, 98 (trusted for VWAP)
Mid-rep sellers: 50, 60, 70 (shown in listings)
Low-rep sellers: 0, 10, 30, 45 (filtered from VWAP)
```

**Price Scenarios:**
```
Stable: €995/BCH (±1%)
Volatile: €900-€1100/BCH (±10%)
Crash: €920/BCH (8% drop, triggers refund)
Recovery: €930→€1000 (crosses threshold)
```

---

## Success Criteria

**Phase 1.5 ready for production when:**

✅ **All trade broadcasting tests pass** (T1.1 - T1.3)  
✅ **VWAP calculation accurate** (T2.1 - T2.3)  
✅ **Hybrid weighting works** (T3.1 - T3.4)  
✅ **Auto-refund triggers correctly** (T4.1 - T4.6)  
✅ **Fallback mechanisms tested** (T5.1 - T5.4)  
✅ **Multi-device redundancy works** (T6.1 - T6.2)  
✅ **Performance targets met** (T8.1 - T8.3)  
✅ **Security tests pass** (T9.1 - T9.3)  
✅ **Edge cases handled** (T10.1 - T10.3)

**Deployment:** After all ⏳ tests → ✅ completed

---

## Notes & Issues

### Issue 1: Hybrid Weighting Formula Discrepancy

**Problem:** Document example (line 230) says "€1,200 volume = 70% user VWAP" but formula (line 572) gives 24%

**Formula:** `userWeight = min(userVolume24h / 5000, 0.95)`

**Example calculation:** €1,200 / €5,000 = 0.24 (24%, not 70%)

**Action:** Clarify formula or update example in distributed-monitoring.md

---

**Status:** Testing plan created, no tests executed yet  
**Next:** Set up test environment, begin Sprint 1 (Trade Broadcasting & VWAP)
