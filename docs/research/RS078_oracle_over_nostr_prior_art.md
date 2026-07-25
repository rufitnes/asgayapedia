# RS078: Oracle-over-Nostr Prior Art Analysis
**Date:** 2026-07-25  
**Type:** Technical research, Protocol analysis  
**Status:** Confirmed  
**Phase:** Phase 1.5 oracle architecture design

---

## Executive Summary

**Research question:** Has anyone implemented oracle price feeds over Nostr before Asgaya's distributed-monitoring architecture?

**Answer:** Yes - Two approaches exist:
1. **Simple price events (PR #1658)** - Kind 89 events for posting prices
2. **DLC oracles (NIP-88)** - Cryptographic attestations for on-chain contract enforcement

**Key finding:** Existing implementations use oracle for on-chain enforcement. **Asgaya's approach is different** - we use oracle-over-Nostr for monitoring/UX, with MTP fallback for trustless security. Multi-oracle consensus (median from multiple sources) and global price watch channel appear to be novel contributions.

**Validation:** Oracle-over-Nostr is proven infrastructure. DLC oracles demonstrate Nostr works well for oracle communication. Asgaya's trust model separation (oracle-for-UX, MTP-for-security) is architecturally sound.

---

## Background: Why This Research Matters

While designing Asgaya's distributed monitoring system with oracle-over-Nostr architecture, we needed to know:
- Has this been done before?
- Are there established patterns to follow?
- What can we learn from existing implementations?
- Is our approach fundamentally sound?

This research validates the oracle-over-Nostr infrastructure choice and identifies where Asgaya's design differs from existing approaches.

---

## Finding 1: Simple Price Events (PR #1658)

### What It Is

NIP proposal by Vitor Pamplona for posting asset prices to Nostr using **kind 89 events**.

### How It Works

- Oracles post prices (e.g., `$BTCUSD`) as Nostr events
- Clients subscribe to multiple price providers
- Calculate average from trusted sources

### Example

```javascript
// Oracle posts kind 89 event
{
  kind: 89,
  content: JSON.stringify({
    asset: "BTCUSD",
    price: 65432.10,
    timestamp: 1721937723,
    source: "coinbase"
  }),
  tags: [["asset", "BTCUSD"]]
}

// Client subscribes and averages
const avgPrice = prices.reduce((a,b) => a+b.price, 0) / prices.length;
```

### Limitation

From PR discussion: *"The data from this NIP cannot be used for trustless financial transactions. If you publish two valid but conflicting price events, there is no fallback, unlike with DLC oracles."*

### Status

Pull request still open, not merged into official NIPs.

**Source:** https://github.com/nostr-protocol/nips/pull/1658

---

## Finding 2: DLC Oracles over Nostr (NIP-88)

### What It Is

More sophisticated proposal using **Discreet Log Contracts** for cryptographic price attestations with on-chain enforcement.

### How It Works

1. Oracle publishes **announcement** (cryptographic commitment to future event)
2. Event happens (e.g., BTC price at specific time)
3. Oracle publishes **attestation** (signed outcome)
4. Winning party uses attestation to claim funds on-chain
5. Oracle can't steal funds - only provides signed proof

### Key Innovation

From NIP-88 proposal:

*"With Nostr as a layer of discoverability for DLC oracles, anyone with an internet connected device can discover other oracles on the same relay, hear announcements from any oracles who connect to the same relay, create conditional contracts based on those announcements, and create multi-oracle contracts which resolve based on the opinions of different oracles, possibly on distinct relays."*

### Trust Model

```
Oracle signs outcome → Contract enforces signature on-chain
```

Oracle signature controls contract execution, but multi-oracle contracts can mitigate this.

### Production Use

**DlcDevKit** implements NIP-88 with oracle marketplace for discovering and using DLC oracles in real applications.

### Status

Active development, real implementations exist, pull request #919 ongoing.

**Sources:**
- https://github.com/nostr-protocol/nips/pull/919
- https://stacker.news/items/608286
- https://bennyb.dev/blog/progress-report-1/
- https://adiabat.github.io/dlc.pdf (Original DLC paper)

---

## How Asgaya's Approach Differs

### Trust Model Comparison

**DLC Oracles (NIP-88):**
```
Oracle signs outcome → Contract enforces signature on-chain
Trust: Oracle's signature controls funds
```

**Asgaya:**
```
Oracles broadcast prices → Devices monitor → Client decides to refund
Covenant: Allows refund anytime (sender owns it)
Oracle: Improves UX, doesn't control funds
MTP fallback: Guarantees trustless refund
```

### Key Architectural Differences

| Aspect | DLC Oracles (NIP-88) | Asgaya Oracle-over-Nostr |
|--------|---------------------|--------------------------|
| **Purpose** | On-chain enforcement | Off-chain monitoring for UX |
| **Trust model** | Oracle signature required | Oracle optional, MTP guarantees trustlessness |
| **Single point of failure** | Yes (oracle controls outcome) | No (MTP fallback if oracle fails) |
| **Multi-oracle** | Supported but optional | Built-in (median from 3+ sources) |
| **Network effect** | Oracle marketplace (discovery) | Global price watch (resolution scaling) |
| **Cryptographic commitment** | Required (oracle signs) | Not needed (monitoring only) |

---

## Asgaya's Novel Contributions

### 1. Blockchain-as-Oracle Price Discovery

- Every covenant funding = trade signal (on-chain, unfakeable)
- Reputation-filtered VWAP from real Asgaya trades (not CEX speculation)
- User sovereignty: Sellers choose own price sources (Kraken, Coinbase, Bitstamp, etc.)
- Maximum censorship resistance (blockchain can't be shut down)
- Network effect: More trades = better price discovery

### 2. Bootstrap Strategy (Asgaya as Training Wheels)

- Phase 0: Asgaya acts as first seller + oracle (Kraken API)
- Phase 1-2: Hybrid weighting as network grows (user VWAP gains weight)
- Phase 3: Network self-reliant (95%+ user VWAP, Asgaya optional)
- Gradual decentralization from centralized bootstrap to permissionless market

### 3. Oracle-for-UX, MTP-for-Security Separation

- Oracle provides real-time detection (sub-second latency)
- MTP provides trustless fallback (covenant enforces)
- Oracle failure degrades UX, never security
- Unique trust model: monitoring vs enforcement

### 4. Market Self-Balancing Security

- Four natural defense mechanisms (rational buyers, inventory limits, merchant reactions, arbitrage)
- Reputation filter prevents Sybil attacks (90+ required for VWAP influence)
- Auto-refund attack catch-22 (maintain rep = losses OR reject = rep drops)
- Economic incentives resist manipulation without central authority

---

## What Asgaya Adopts from Prior Art

### 1. NIP-88 Event Structure

Follow established pattern for oracle broadcasts:

```javascript
{
  kind: 88001, // Price monitoring event (not attestation)
  content: JSON.stringify({
    pair: "BCH/EUR",
    price: 995.50,
    timestamp: 1721937723,
    source: "coinbase"
  }),
  tags: [
    ["pair", "BCH/EUR"],
    ["source", "coinbase"]
  ]
}
```

### 2. Oracle Marketplace Pattern

- Oracles publish profile events for discovery
- Devices discover available oracles via relay queries
- Enables permissionless oracle network

### 3. Multi-Relay Strategy

- Oracles broadcast to multiple relays
- Devices subscribe to multiple relays
- Censorship resistance through decentralization

---

## Validation of Asgaya's Design

### What This Research Confirms

**✅ Oracle-over-Nostr is proven infrastructure**
- DLC oracles demonstrate Nostr works well for oracle communication
- Multiple production implementations exist
- Active development community

**✅ Asgaya's trust model is sound**
- Separation of monitoring (oracle) from enforcement (MTP) is architecturally valid
- Oracle-for-UX, MTP-for-security eliminates oracle as single point of failure
- Novel approach validated by comparison with existing patterns

**✅ Multi-oracle consensus is improvement**
- DLC allows multi-oracle but single oracle is common
- Asgaya makes multi-oracle consensus default (median from 3+ sources)
- Better censorship resistance, no single point of failure

**✅ Global price watch is novel**
- Network effect scaling not present in DLC model
- Contributes to broader Nostr ecosystem (potential NIP proposal)

---

## References

### GitHub

- [PR #1658: Asset prices by vitorpamplona](https://github.com/nostr-protocol/nips/pull/1658)
- [PR #919: NIP-88 Discreet Log Contracts over Nostr](https://github.com/nostr-protocol/nips/pull/919)

### Technical Papers

- [Discreet Log Contracts (MIT DCI)](https://adiabat.github.io/dlc.pdf)
- [Bitcoin Optech: DLCs](https://bitcoinops.org/en/topics/discreet-log-contracts/)

### Implementation

- [DlcDevKit Progress Report #1](https://bennyb.dev/blog/progress-report-1/)
- [Introduction to DlcDevKit](https://www.bennyb.dev/blog/dlcdevkit)

### Discussion

- [NIP88: "Discreet Log Contracts over Nostr" has enormous potential](https://stacker.news/items/608286)

---

## Related Asgaya Documentation

**Implementation:**
- [Distributed Monitoring](../the-mechanism/nostr-coordination/distributed-monitoring.md) - Oracle-over-Nostr architecture
- [Time Oracle + MTP Fallback](../why-this-design/constraints/time-oracle-mtp-fallback-trustless-ux.md) - Trust model explanation
- [Auto-Refund UX](../user-journeys/remittance/sender/auto-refund-ux.md) - User-facing oracle monitoring

**Design decisions validated:**
- Oracle-over-Nostr is proven infrastructure ✅
- Multi-oracle consensus is improvement on existing pattern ✅
- Separation of monitoring (oracle) from enforcement (MTP) is sound ✅

---

## Future Considerations

### Potential Collaboration

- DlcDevKit community for oracle marketplace patterns
- Nostr protocol developers for multi-oracle consensus NIP

### NIP Proposal (Phase 2+)

Consider proposing NIP for "Multi-Oracle Price Consensus" pattern:
- Network effect scaling through global price watch
- Push-based consensus calculation
- Contribution back to Nostr ecosystem

---

**Status:** Research complete. Prior art analyzed. Asgaya's approach validated. Implementation path clear.

**Key takeaway:** Oracle-over-Nostr has prior art (DLC oracles, price events), but Asgaya's trust model (oracle-for-UX, MTP-for-security) and multi-oracle consensus with network effect scaling are novel contributions.
