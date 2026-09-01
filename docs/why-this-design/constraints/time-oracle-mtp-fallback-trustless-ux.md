# Time Oracle + MTP Fallback: Trustless UX Design

**Status:** Implemented in Phase 2 (2026-07-21)  
**Category:** Covenant Design Constraint  
**Trust Model:** Oracle for UX, MTP for Security

---

## TL;DR

**Problem:** Time-locked covenants need reliable time measurement, but BCH's Median Time Past (MTP) only advances when blocks are mined (~10 minutes).

**Solution:** Dual time enforcement system:
- **Time Oracle** (primary): Provides real-time clock for UX and fast covenant execution
- **MTP Timelock** (fallback): Guarantees trustless refund if oracle fails or sender disappears

**Key Insight:** The oracle doesn't need to be trusted because MTP provides an unbreakable safety net. Oracle failure only degrades UX, never security.

---

## The Problem

### Covenant Needs Time Measurement

Asgaya's covenant requires time-locked refunds to protect senders:

**Scenario:**
1. Sender creates covenant with 1.07 BCH (€100 payment + €7 buffer)
2. Sender sends "claim code" to recipient in Venezuela
3. Recipient never claims (lost phone, wrong person, scam, etc.)
4. **Sender must get refund** - can't lock funds forever

**Requirement:** After 8 hours, if recipient hasn't claimed, sender gets full refund.

### Why Not Just Use MTP?

**Median Time Past (MTP)** is Bitcoin Cash's built-in time measurement:
- Calculated as median of last 11 blocks
- Guaranteed to be in the past (prevents timestamp manipulation)
- **Only advances when blocks are mined** (~10 minutes on average)

**Problem for 8-hour covenants:**

```
Intended:   Covenant created at 10:00 AM → Expires at 6:00 PM (8 hours)
Reality:    Block mined at 10:00 AM (MTP: 10:00)
            Next block at 10:07 AM (MTP: 10:03) - small advance
            Next block at 10:22 AM (MTP: 10:11) - block gap!
            Next block at 10:35 AM (MTP: 10:18)
            ...
            Expiry block could be anywhere from 5:45 PM to 6:30 PM
```

**MTP limitations:**
- ✅ Trustless (can't be manipulated)
- ✅ Consensus-enforced (guaranteed by blockchain)
- ❌ Irregular (10-minute average, but varies: 1 min to 60+ min)
- ❌ Coarse granularity (can't measure sub-30-minute intervals reliably)

### Why Time Precision Matters

**UX problem:**
```
User creates covenant at 10:00 AM, expecting 8-hour expiry at 6:00 PM
Reality: MTP-based expiry could trigger anywhere from 5:45 PM to 6:25 PM

Recipient tries to claim at 5:50 PM:
  Option A: MTP already advanced past expiry → COVENANT LOCKED (refund only)
  Option B: MTP hasn't advanced yet → Claim succeeds

Result: Unpredictable UX, recipient frustrated
```

**Merchant problem:**
```
Merchant wants to cash out recipient's covenant at 5:55 PM
Merchant app checks: "5 minutes left, safe to proceed"
Merchant completes KYC, gets recipient's cash ready
Merchant tries to broadcast claim transaction
Result: "ERROR: Covenant expired" (MTP jumped ahead during KYC)

Now merchant is stuck:
- Gave cash to recipient
- Can't claim covenant (expired)
- Recipient disappears with both cash AND covenant claim code
```

**Bottom line:** MTP-only covenants create timing uncertainty that breaks UX and enables fraud.

---

## The Solution: Dual Time Enforcement

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│ COVENANT PARAMETERS                                          │
├─────────────────────────────────────────────────────────────┤
│ expiryMTP:        1784661278 (MTP timestamp, 8 hours ahead) │
│ expiryOracleTime: 1784661278 (Real-time clock)              │
│ oraclePubkey:     02abc...def (Price oracle's signing key)  │
└─────────────────────────────────────────────────────────────┘

CLAIM PATH (before expiry):
  ┌───────────────────────────────────────────────────┐
  │ 1. Check oracle signature on latest price data    │
  │    require(checkDataSig(oracleSig, oracleData))  │
  │ 2. Verify oracle timestamp < expiryOracleTime    │
  │ 3. Verify recipient signature                     │
  │ 4. Calculate BCH amount from EUR price            │
  │ 5. Split outputs (payment + buffer)               │
  └───────────────────────────────────────────────────┘

REFUND PATH (after expiry):
  ┌───────────────────────────────────────────────────┐
  │ 1. require(tx.time >= expiryMTP)  ← MTP ENFORCEMENT
  │    This is UNBREAKABLE - can't be faked           │
  │ 2. Verify sender signature                        │
  │ 3. Refund full amount to sender                   │
  └───────────────────────────────────────────────────┘
```

### How It Works

**Normal flow (oracle works):**
1. Covenant created at 10:00 AM
2. Oracle signs price updates every 5 minutes
3. At 5:58 PM (2 minutes before expiry):
   - Recipient tries to claim
   - Oracle timestamp shows 5:58 PM
   - Covenant: "Oracle says 5:58 PM < 6:00 PM expiry → Allow claim"
   - Transaction succeeds

4. At 6:02 PM (after expiry):
   - Recipient tries to claim
   - Oracle timestamp shows 6:02 PM
   - Covenant: "Oracle says 6:02 PM >= 6:00 PM expiry → REJECT"
   - Recipient can't claim anymore

**Failure mode (oracle offline):**
1. Covenant created at 10:00 AM, expiry at 6:00 PM
2. Oracle goes offline at 3:00 PM (no more price updates)
3. At 5:58 PM:
   - Recipient tries to claim
   - Oracle last signed at 3:00 PM (3 hours old!)
   - Covenant: "Oracle data too stale → REJECT claim"
   - **BUT:** Recipient can wait - MTP hasn't reached expiry yet

4. At 6:15 PM (MTP advances past 6:00 PM expiry):
   - Sender can now refund via MTP path
   - `require(tx.time >= expiryMTP)` succeeds
   - Sender gets funds back

**Key property:** Oracle failure never locks funds forever. MTP provides guaranteed escape hatch.

---

## Trust Analysis

### What If Oracle Misbehaves?

**Scenario 1: Oracle signs future timestamps (claims covenant expired early)**

```
Actual time: 5:55 PM (5 minutes before expiry)
Oracle lies: Signs timestamp of 6:05 PM (10 minutes AFTER expiry)

Recipient tries to claim:
  - Covenant checks: oracleTime (6:05) >= expiry (6:00) → REJECT
  - Recipient can't claim via oracle path

But MTP hasn't advanced past 6:00 PM yet!
  - Sender tries to refund via MTP path
  - require(tx.time >= expiryMTP) → FAILS (tx.time still ~5:55)
  - Sender can't refund yet

Result: Temporary deadlock (5 minutes)
When MTP advances past 6:00 PM → Sender CAN refund via MTP path
```

**Impact:** Oracle can create 5-10 minute UX delay, but can't steal funds.

**Scenario 2: Oracle signs past timestamps (keeps covenant open indefinitely)**

```
Actual time: 6:10 PM (10 minutes after expiry)
Oracle lies: Signs timestamp of 5:50 PM (claims still 10 min before expiry)

Recipient tries to claim:
  - Covenant checks: oracleTime (5:50) < expiry (6:00) → ALLOW
  - Recipient can claim via oracle path

But sender can ALSO refund:
  - require(tx.time >= expiryMTP) → SUCCEEDS (actual MTP is past 6:00)
  - Sender can refund via MTP path

Result: Race condition
```

**Resolution:**
- Whichever transaction hits the blockchain first wins
- Sender and recipient both broadcast simultaneously
- Miners include whichever they see first (likely sender's, as they're watching)
- Covenant UTXO is spent → other transaction fails (double-spend prevention)

**Impact:** Oracle can enable brief race condition, but doesn't benefit either party. Both parties watching mempool. Sender wins race by monitoring blockchain.

**Scenario 3: Oracle refuses to sign (censorship)**

```
Oracle sees recipient is from sanctioned country
Oracle refuses to sign any price updates
```

**Result:**
- Recipient can't claim via oracle path (no signature)
- After MTP advances past expiry → Sender refunds via MTP path
- Oracle effectively helps sender, hurts recipient

**But:** This is acceptable! Sender created covenant voluntarily. If oracle censors, sender gets refund - no funds lost.

**Mitigation:** Use multiple oracle providers (future enhancement)

**Phase 1.5 evolution:** [Distributed Monitoring](../../the-mechanism/nostr-coordination/distributed-monitoring.md) implements blockchain-as-oracle architecture where every covenant funding becomes a trade signal. Price discovery emerges from reputation-filtered VWAP of real Asgaya trades (on-chain), bootstrapped by Asgaya oracle until network matures. Censorship-resistant (blockchain can't be shut down), permissionless (anyone can be seller), eliminates single-oracle failure mode.

**Scenario 4: Oracle colludes with recipient**

```
Sender and recipient agree on 8-hour covenant
Recipient bribes oracle to sign fake timestamps
Oracle signs timestamp of 5:55 PM when actual time is 6:10 PM
```

**Result:** Same as Scenario 2 - race condition
- Recipient broadcasts claim with stale oracle signature
- Sender broadcasts refund with MTP proof
- First to confirm wins

**Defense:**
- Sender monitors blockchain
- Sender broadcasts refund immediately when MTP crosses expiry
- Sender likely wins race (already monitoring, recipient has to wait for bribed oracle)

---

## Why This Is Better Than Alternatives

### Alternative 1: MTP Only (No Oracle)

**Pros:**
- ✅ Fully trustless
- ✅ No external dependencies
- ✅ Simpler covenant code

**Cons:**
- ❌ Terrible UX (10-60 minute timing uncertainty)
- ❌ Enables timing-based fraud (merchant edge case)
- ❌ Users can't predict when covenant expires
- ❌ Android app can't show countdown timer

**Verdict:** Technical purity at the expense of usability. Doesn't work for real users.

### Alternative 2: Oracle Only (No MTP)

**Pros:**
- ✅ Precise timing (second-level accuracy)
- ✅ Great UX (predictable expiry)
- ✅ Android countdown timers work

**Cons:**
- ❌ **TRUSTED ORACLE** - Oracle can lock funds forever
- ❌ Single point of failure
- ❌ Oracle can censor users
- ❌ Oracle can collude with recipient/sender

**Verdict:** Unacceptable. Introduces trusted third party into "trustless" system.

### Alternative 3: On-Chain Time Oracle (Block Height)

**Idea:** Use block height instead of timestamps
```
Current block: 850,000
Expiry block: 850,048 (48 blocks = ~8 hours)
```

**Pros:**
- ✅ Trustless (block height is consensus)
- ✅ More predictable than MTP (48 blocks = 8 hours average)

**Cons:**
- ❌ Still variable (48 blocks could take 6-10 hours)
- ❌ Doesn't solve merchant timing attack
- ❌ Can't show precise countdown ("~47 blocks left" is confusing)

**Verdict:** Better than MTP-only, but still has UX problems. Oracle+MTP is strictly better.

### Alternative 4: Lighthouse/CDS Schemes

**Idea:** Use CheckDataSig with challenge-response protocol
```
Sender challenges oracle every block
If oracle doesn't respond → automatic refund
```

**Pros:**
- ✅ Detects oracle failure quickly

**Cons:**
- ❌ Requires constant on-chain interaction (expensive)
- ❌ Sender phone must be online (defeats "send and forget")
- ❌ Doesn't solve oracle misbehavior (still need MTP fallback)

**Verdict:** More complex, more expensive, doesn't solve the core problem. Oracle+MTP is simpler and better.

---

## Implementation Status

### Phase 0: Simple Split (✅ Complete)
**File:** `simple-split.cash`

Tests basic covenant mechanics:
- Transaction introspection
- Output splitting (payment + buffer)
- Signature verification

**No time locks yet** - just validates CashScript basics work.

### Phase 2: Time-Locked Refund (✅ Complete)
**File:** `time-refund.cash`

Adds MTP time enforcement:
```cashscript
function claim(sig recipientSig) {
    require(checkSig(recipientSig, recipient));
    require(tx.outputs[0].value == paymentSats);
    require(tx.outputs[0].lockingBytecode == new LockingBytecodeP2PKH(hash160(recipient)));
    require(tx.outputs[1].lockingBytecode == new LockingBytecodeP2PKH(hash160(seller)));
}

function refund(sig senderSig) {
    require(checkSig(senderSig, sender));
    require(tx.time >= expiryMTP);  // ← MTP ENFORCEMENT
    require(tx.outputs[0].value == paymentSats);
    require(tx.outputs[0].lockingBytecode == new LockingBytecodeP2PKH(hash160(sender)));
    require(tx.outputs[1].lockingBytecode == new LockingBytecodeP2PKH(hash160(seller)));
}
```

**Status:**
- ✅ Compiles correctly (CashScript 0.13.2)
- ✅ Bytecode validated
- ✅ MTP enforcement syntax correct
- 🔜 Integration testing on testnet (deferred)

### Phase 1: Price Oracle (🔜 Next)

Will add oracle time enforcement:
```cashscript
function claim(sig recipientSig, datasig oracleSig, bytes oracleMessage) {
    // Verify oracle signature
    require(checkDataSig(oracleSig, oracleMessage, oraclePubkey));
    
    // Parse oracle data: {timestamp, bchPriceInCents}
    bytes8 oracleTimestamp = oracleMessage.split(8)[0];
    bytes8 bchPrice = oracleMessage.split(8)[1].split(8)[0];
    
    // Check oracle timestamp < expiry
    require(int(oracleTimestamp) < expiryOracleTime);
    
    // Calculate BCH amount from EUR price
    int bchAmount = (eurAmount * 100) / int(bchPrice);
    
    // ... rest of claim logic
}

function refund(sig senderSig) {
    require(checkSig(senderSig, sender));
    require(tx.time >= expiryMTP);  // ← MTP fallback still here!
    // ... rest of refund logic
}
```

**Key properties:**
- Oracle provides UX (precise timing + price calculation)
- MTP provides security (trustless refund guarantee)
- Both paths coexist independently

---

## Key Insight: Refund() Can Happen Anytime

**Correction to the naive reading of the refund path:** `refund()` is not gated by the oracle at all. It's the sender's money — the sender should always control it. `refund()` requires only:
- The **sender's signature** (proves ownership)
- A **price oracle** (to split the UTXO fairly between sender and funder)

**There is no oracle-time requirement for refund** — only the price to compute the split. The MTP timelock is an *additional* safety net that guarantees the refund even if the sender's device or the oracle is unavailable.

**Why this matters for the funder (the user who needs it most):**
> "The user that really needs this feature is the funder. If the oracle fails and the sender is offline, the volatility buffer is locked. MTP + fallback price oracle prevents the buffer from being locked until the oracle is back online." — Suso

**Scenario:**
- Oracle fails, sender is offline
- Funder's 7% buffer is locked in the covenant — can't recover it
- **MTP advances past expiry** → sender's refund path opens regardless of oracle
- Buffer returns to funder (funder principle)

**MTP's real value is as the fallback that prevents fund-locking** — not as the primary refund mechanism. It's the escape hatch when the oracle is down.

**Also note:** seller/funder bots watch the covenant with a timer, ready to recover the buffer as soon as the MTP refund path opens. The MTP fallback is what makes their capital recoverable.

**Fallback hierarchy (what "fallback" really means):**
> "If the Asgaya oracle failed, our fallback oracle provides price; MTP provides time. Not ideal but better than nothing." — Suso

| Component | Provides | Fails? |
|-----------|----------|--------|
| Asgaya price oracle | Price + timestamp (fresh, precise) | Price fallback oracle takes over |
| Fallback price oracle | Price (stale but valid) | MTP still covers time |
| MTP timelock | Time (guaranteed by consensus) | Cannot fail |

---

## Design Principles

### 1. **Oracle for UX, MTP for Security**

**Principle:** External data (oracle) can enhance user experience, but must never be the sole security mechanism.

**Application:**
- Oracle provides real-time clock for countdown timers
- Oracle provides price data for dynamic payment calculation
- MTP provides unbreakable refund guarantee
- Users can verify covenant will refund via MTP even if oracle disappears

### 2. **Graceful Degradation**

**Principle:** System should work (albeit with degraded UX) even if components fail.

**Failure modes:**
- Oracle offline → Users wait for MTP expiry (slow but works)
- Oracle censors → Sender gets refund via MTP (safe default)
- Oracle misbehaves → MTP prevents theft (worst case: temporary deadlock)

**Never:** Oracle failure never locks funds permanently

### 3. **Trust Minimization, Not Trust Elimination**

**Principle:** Don't fetishize "trustlessness" at the expense of usability. Minimize trust, don't eliminate it when elimination costs too much UX.

**Trade-off:**
- 100% trustless (MTP only) = Terrible UX, timing attacks, merchant fraud risk
- Minimal trust (Oracle + MTP) = Great UX, no new attack vectors (MTP covers oracle failure)

**Key insight:** The oracle can't steal funds because MTP exists. This is very different from "trusted oracle" designs where oracle failure = permanent fund loss.

### 4. **Separation of Concerns**

**Principle:** Each component should have one job.

**Application:**
- Oracle's job: Provide timely data (price + timestamp)
- MTP's job: Provide trustless time guarantee
- Covenant's job: Enforce rules using both data sources
- Android app's job: Monitor blockchain and broadcast refunds

**Benefit:** Can replace oracle provider without changing covenant logic. Can update MTP fallback window without touching oracle code.

---

## Open Questions

### 1. Oracle Selection
**Status:** Unresolved

**Options:**
- Reuse existing BCH price oracles (e.g., General Protocols Oracle)
- Build dedicated Asgaya oracle (more control, more maintenance)
- Multi-oracle design (combine 3+ oracles, majority vote)

**Trade-offs:**
- Existing oracle: Free, but might not support EUR pairs or timestamp proofs
- Custom oracle: Full control, but operational burden (hosting, uptime, signing keys)
- Multi-oracle: Most robust, but complex covenant logic (more fees)

**Decision needed:** Phase 1 implementation (next session)

### 2. Oracle Message Format
**Status:** Unresolved

**Requirements:**
- Must include timestamp (for time enforcement)
- Must include BCH/EUR price (for payment calculation)
- Must be compact (fits in OP_RETURN or script)
- Must be parseable in CashScript

**Proposed format:**
```
bytes8: Unix timestamp (seconds since epoch)
bytes8: BCH price in cents (e.g., 10000 = €100.00)
bytes32: Signature (ECDSA secp256k1)
```

**Open questions:**
- Include exchange name? (Kraken, Binance, etc.)
- Include confidence interval? (±€2)
- Include sequence number? (detect replay attacks)

**Decision needed:** Phase 1 implementation

### 3. Stale Oracle Data Handling
**Status:** Unresolved

**Problem:**
```
Covenant expiry: 6:00 PM
Oracle last signed: 5:30 PM (30 minutes ago)
Current time: 5:55 PM (5 minutes before expiry)

Should covenant accept 30-minute-old oracle data?
```

**Options:**
- **Option A:** Accept any oracle data before expiry timestamp
  - Pro: Simpler covenant logic
  - Con: Allows race condition if oracle stale
- **Option B:** Require oracle timestamp within 5 minutes of claim
  - Pro: Prevents stale data attacks
  - Con: More complex covenant logic, more oracle requests
- **Option C:** Accept stale data, rely on MTP for conflicts
  - Pro: Simplest covenant
  - Con: Enables brief race conditions

**Decision needed:** Phase 1 implementation

### 4. MTP Fallback Window
**Status:** Unresolved

**Question:** Should MTP expiry be:
- Exactly same as oracle expiry? (expiryMTP = expiryOracleTime)
- 10 minutes after oracle expiry? (expiryMTP = expiryOracleTime + 600)
- 30 minutes after? (expiryMTP = expiryOracleTime + 1800)

**Trade-offs:**
- Same time: Simplest, but enables race conditions when oracle stale
- 10-minute gap: Small buffer prevents races, minimal UX impact
- 30-minute gap: Large safety margin, but confusing UX ("why does covenant stay open after expiry?")

**Recommendation:** 10-minute gap (one block average)
- Oracle expiry: 6:00 PM (user expectation)
- MTP expiry: 6:10 PM (safety net)
- UX: "Covenant expires at 6:00 PM (with 10-min safety margin)"

**Decision needed:** Phase 1 implementation

---

## Related Documentation

**Implementation:**
- [Phase 0 Covenant Testing (2026-07-20)](../../../knowledge/meta/project_blog/2026-07-20_time-oracle-decision-phase0-covenant.md) - Initial covenant architecture
- [Phase 2 Covenant Testing (2026-07-21)](../../../knowledge/meta/project_blog/2026-07-21_phase0-phase2-covenant-testing.md) - MTP time lock validation

**Design Context:**
- [7% Volatility Buffer](7%-volatility-buffer-money-velocity-enabler.md) - Why 7% buffer exists
- [Progressive Payment Rollout](progressive-payment-rollout.md) - How covenant parameters evolve over phases

**Research:**
- Kraken API price scripts: `knowledge/research_code/` (Python scripts for oracle data)
- Oracle research: TBD (Phase 1)

---

## Summary

**The time oracle + MTP fallback architecture solves a fundamental tension in covenant design:**

- **Users want precise timing** → Oracle provides this
- **Users need trustlessness** → MTP provides this
- **Both can coexist** → Oracle enhances UX without adding trust

**Key insight:** The oracle doesn't need to be trusted because MTP acts as an unbreakable safety net. Oracle misbehavior can only create temporary UX degradation, never permanent fund loss.

**This is the design we're building.**

---

**Document Status:** Living document, updated as implementation progresses  
**Last Updated:** 2026-09-01 (refund() insight, funder perspective added)  
**Maintained By:** Suso + Coordination (Claude)

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Constraints](README.md)** | **[📖 Glossary](../../glossary.md)**
