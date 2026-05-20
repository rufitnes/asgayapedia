# Seller Liveness Signal

**Status:** Concept (Phase 1+ enhancement)  
**Last Updated:** 2026-05-12  
**Author:** Asgaya Protocol Team

---

## Problem Statement

In a permissionless remittance protocol, how do you verify that a BCH Seller is:
1. **Actually online and available** (not just listed on bulletin board)
2. **Running honest software** (not spoofing availability)
3. **Able to respond quickly** (critical for 5-minute Bizum window)

Traditional approaches:
- ❌ **Explicit pings** → Easy to fake, creates overhead
- ❌ **Uptime SLAs** → Requires central monitoring, contradicts permissionless design
- ❌ **Staking/bonds** → Creates barriers to entry, reduces replaceability
- ❌ **Reputation systems** → Slow to build, vulnerable to Sybil attacks

**We need a passive, trustless liveness proof that emerges naturally from honest operation.**

---

## Solution: Notification Noise as Liveness Signal

### Core Insight

BCH Sellers running `smsbridge_loop.py` (the Bizum notification parser) continuously process SMS notifications. This creates a **verifiable activity pattern** that fake nodes cannot easily replicate.

**The signal:**
```
Honest seller running smsbridge_loop.py
    ↓
Smartphone receives constant notification stream
    ↓
Bot parses every notification (Bizum, other apps, system alerts)
    ↓
Parsing activity creates continuous liveness signal
    ↓
Pattern proves: Real device + Real SIM + Honest software
```

**Why this works:**
- Real smartphones receive 10-100+ notifications daily
- Parsing pattern reflects real-world noise (unpredictable timing)
- Fake nodes would need to simulate realistic notification patterns (hard)
- No explicit "ping/pong" needed (passive proof)

---

## How It Works

### Phase 0 (Manual Verification)

**Implementation:**
- `smsbridge_loop.py` logs every notification parsed (timestamp, type, result)
- Seller shares log digest with protocol operators (trusted Phase 0 setup)
- Operators verify continuous parsing activity (not just Bizum, all notifications)

**Example log pattern (honest seller):**
```
2026-05-12 08:23:15 - Notification parsed: WhatsApp message
2026-05-12 08:45:32 - Notification parsed: Bizum payment (matched covenant ABC123)
2026-05-12 09:12:08 - Notification parsed: Calendar reminder
2026-05-12 09:34:55 - Notification parsed: Email notification
2026-05-12 10:05:12 - Notification parsed: Bizum payment (matched covenant DEF456)
2026-05-12 10:28:44 - Notification parsed: App update notification
```

**What this proves:**
- ✅ Smartphone is on and receiving notifications
- ✅ smsbridge_loop.py is running continuously
- ✅ Bot is parsing all notifications (not just Bizum = honest software)
- ✅ Timing is realistic (unpredictable, human-like)

**What fake nodes can't do:**
- ❌ Simulate realistic notification patterns (too much variance)
- ❌ Generate continuous non-Bizum noise (would need to replicate entire smartphone usage)
- ❌ Maintain pattern 24/7 (real sellers have natural activity rhythms)

---

### Phase 1 (Protocol-Level Integration)

**Potential implementation:**

1. **Liveness Proof Publication**
   - Sellers periodically publish anonymized notification digest to bulletin board
   - Digest includes: count, timing distribution, type diversity
   - No personal data (just statistical fingerprint)

2. **Verification**
   - Senders check liveness proof before creating covenant
   - Recent proof (< 1 hour old) = seller likely responsive
   - No proof = seller may be offline

3. **Anti-Gaming**
   - Proof must show diverse notification types (not just Bizum)
   - Timing must be realistic (not too regular = fake)
   - Historical pattern must be consistent (not sudden spike)

**Example liveness proof (anonymized):**
```json
{
  "seller_id": "bcash1q...",
  "timestamp": "2026-05-12T10:30:00Z",
  "window": "1h",
  "stats": {
    "total_notifications": 23,
    "types": {
      "messaging": 12,
      "system": 5,
      "payment": 3,
      "other": 3
    },
    "timing_variance": 0.87,  // High variance = realistic
    "entropy": 3.42           // High entropy = not scripted
  },
  "signature": "..."
}
```

**Interpretation:**
- 23 notifications in 1 hour = realistic smartphone usage
- Diverse types (not just Bizum) = honest parsing
- High variance/entropy = real human patterns, not bot

---

## Security Properties

### What Liveness Signal Proves

✅ **Device is real** - Fake nodes can't generate realistic notification noise  
✅ **SIM is active** - SMS-based notifications require active SIM card  
✅ **Software is honest** - Parsing all notifications, not just Bizum  
✅ **Seller is available** - Recent proof = responsive within hours

### What It Doesn't Prove

❌ **Seller has BCH** - Liveness ≠ liquidity (separate check)  
❌ **Seller will accept covenant** - Could be online but selective  
❌ **Seller won't disappear** - Proves current liveness, not future availability

### Attack Resistance

**Sybil Attack:**
- Attacker creates 100 fake seller nodes
- Problem: Each needs realistic notification pattern
- Cost: 100 real smartphones with active SIMs + continuous usage simulation
- **Defense:** Expensive to scale, pattern analysis detects fakes

**Replay Attack:**
- Attacker replays old liveness proofs
- **Defense:** Timestamp + recent-only acceptance (< 1 hour old)

**Pattern Spoofing:**
- Attacker generates synthetic notification patterns
- **Defense:** Statistical analysis (entropy, variance, type diversity) detects artificial patterns

---

## Implementation Considerations

### Phase 0 (Trusted Parties)

**Approach:** Manual verification, seller shares logs

**Benefits:**
- Simple to implement
- No protocol changes needed
- Proves concept with real data

**Limitations:**
- Requires trust (seller shares private logs)
- Not scalable beyond 5-10 sellers

---

### Phase 1 (Permissionless)

**Approach:** Anonymized liveness proofs published to bulletin board

**Benefits:**
- Permissionless (no manual verification)
- Privacy-preserving (statistical digest only)
- Scalable (automated verification)

**Challenges:**
- Pattern analysis complexity (need to define "realistic" thresholds)
- Privacy/anonymity trade-offs (digest still reveals some info)
- Storage overhead (proofs published regularly)

**Open questions:**
- How often should proofs be published? (1 hour? 4 hours? 24 hours?)
- How much history should senders verify? (Last proof? Last 24 hours?)
- What entropy/variance thresholds detect fakes? (Needs empirical data)

---

## Alternative Approaches (Rejected)

### 1. Explicit Ping/Pong
**Idea:** Senders ping sellers, wait for response  
**Problem:** Easy to fake, creates network overhead, timing vulnerable to gaming

### 2. Staking/Bonding
**Idea:** Sellers lock BCH as proof of commitment  
**Problem:** Barrier to entry, reduces replaceability, doesn't prove availability

### 3. Reputation History
**Idea:** Sellers build reputation over time  
**Problem:** Slow to accumulate, vulnerable to Sybil attacks, new sellers disadvantaged

### 4. Proof-of-Work Challenge
**Idea:** Sellers solve computational challenge  
**Problem:** Wastes energy, easy to fake with dedicated hardware, doesn't prove responsiveness

---

## Relationship to Other Mechanisms

### Complements (Not Replaces)

**Liveness signal works with:**
- **Liquidity checks** - Seller must also prove BCH inventory (separate)
- **Reputation systems** - Liveness + good history = high trust
- **Fee competition** - Online sellers compete on fees

**Liveness signal doesn't replace:**
- Covenant timeout mechanisms (still needed)
- Dispute resolution (Phase 0 = trusted parties)
- Anti-fraud measures (separate layer)

---

## Empirical Validation (Phase 0)

**Data to collect:**

1. **Notification patterns from real sellers**
   - Daily notification count distribution
   - Type diversity (messaging, system, payment, other)
   - Timing variance (gaps between notifications)
   - Entropy metrics (pattern predictability)

2. **Correlation with responsiveness**
   - Sellers with continuous parsing → Response time to covenant requests
   - Sellers with gaps in parsing → Delayed/missed covenants
   - Hypothesis: Continuous parsing = faster response (<30 seconds)

3. **Fake pattern detection**
   - Generate synthetic notification patterns
   - Measure statistical distance from real patterns
   - Define thresholds for "realistic" vs "artificial"

**Metrics:**
- Average notifications/hour (real sellers)
- Type diversity ratio (real sellers)
- Timing variance (real sellers)
- False positive rate (flagging honest sellers as fake)
- False negative rate (missing fake sellers)

---

## Future Enhancements (Phase 2+)

### 1. Cross-Seller Verification
Sellers verify each other's liveness proofs (distributed trust)

### 2. Machine Learning Pattern Analysis
Train classifier on real notification patterns, auto-detect fakes

### 3. Privacy-Preserving Proofs
Zero-knowledge proofs of "realistic notification pattern" without revealing digest

### 4. Tiered Liveness
- Basic: Published proof < 1 hour old (90% confidence)
- Enhanced: Published proof < 15 minutes old (99% confidence)
- Premium: Live proof during covenant negotiation (99.9% confidence)

---

## Summary

**Seller Liveness Signal = Passive, trustless proof that BCH Seller is online and running honest software**

**How:**
- smsbridge_loop.py parses all smartphone notifications (not just Bizum)
- Parsing pattern creates verifiable activity signal
- Real-world noise is hard to fake at scale

**Phase 0:**
- Manual log verification (trusted setup)
- Proves concept with empirical data

**Phase 1:**
- Anonymized liveness proofs published to bulletin board
- Automated verification by senders
- Scales to permissionless network

**Novel contribution:**
- Most protocols use explicit pings (easy to fake) or staking (barriers to entry)
- Asgaya uses **passive noise as proof** (emergent from honest operation)
- Aligns with permissionless, low-barrier design philosophy

**Status:** Concept stage, needs Phase 0 empirical validation before protocol integration

---

## Related Documentation

- **smsbridge_loop.py** - Bizum notification parser (implementation)
- **BCH Sellers** - Role definition and incentives
- **Phase 0 Validation Checklist** - Empirical data collection plan
- **Anti-Fraud Mechanisms** - Complementary trust layers (Phase 1+)
