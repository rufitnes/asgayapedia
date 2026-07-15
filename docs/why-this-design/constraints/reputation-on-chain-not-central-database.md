# Reputation On-Chain (Not Central Database)

**The Constraint:** Privacy vs trust, cost vs discoverability

**The Question:** How do users evaluate sellers without a centralized rating database?

---

## What Constrains Us

Three forces limit reputation system design:

1. **Permissionless requirement** - No central database, no admin to verify/moderate ratings
2. **Discovery UX** - Users need to filter sellers by payment method and status WITHOUT messaging everyone
3. **Blockchain economics** - On-chain storage is permanent but expensive (~€0.01 per update)

**The trade-off:** Free, private updates vs trustless, filterable discovery. We need reputation without intermediaries.

---

## The Decision: Minimal Stats On-Chain, Details on Nostr

**What goes where:**

| Data | Storage | Why |
|------|---------|-----|
| **Buyer/seller status** | On-chain | Enables pre-filtering ("show only BCH sellers") |
| **Payment methods** | On-chain | Enables pre-filtering ("show only Bizum sellers") |
| **Location** | On-chain | Critical for cash-in-person ("Madrid sellers only") |
| **Fee %** | On-chain | Price filtering ("show sellers <0.6%") |
| **Transaction bracket** | On-chain | Capacity filtering (prevents spam to small sellers) |
| **Total transactions** | On-chain | Core trust signal, needs trustless verification |
| **Completed transactions** | On-chain | App calculates completion rate from this |
| **Average transaction value** | On-chain | Fraud detection (detects self-dealing patterns) |
| **Last activity** | On-chain | Filter stale listings |
| **Real-time availability** | Nostr | Changes frequently, not worth blockchain space |
| **Current limits** | Nostr | Operational detail, updated frequently |
| **Average reply time** | Nostr | Real-time metric |
| **Value std deviation** | Nostr | Fraud signal (app can flag suspicious patterns) |
| **Dispute history** | Nostr | Privacy-sensitive (see blacklist system) |

**On-chain reputation structure (~180 bytes per seller):**
```javascript
{
  cash_account: "Isabel#142",
  mode: ["seller"],                  // Array: ["buyer"], ["seller"], or ["buyer", "seller"]
  payment_methods: ["bizum", "sepa"],
  location: "madrid",                // General area (city/region)
  fee_percent: 0.5,                  // Enables price filtering
  transaction_bracket: [50, 500],    // [min, max] EUR capacity
  total_transactions: 127,
  completed_transactions: 121,       // App calculates completion_rate: 121/127 = 95.3%
  avg_value_eur: 145.80,
  last_activity: unix_timestamp
}
```

**How updates work:**

- **Initial listing:** ~€0.01 (one BCH transaction to create reputation UTXO)
- **Transaction stats:** Updated automatically when funding covenants—the bot spends the old reputation UTXO and creates a new one with updated stats in the same transaction. Cost: ~€0.0002 (adds ~180 bytes to an already-necessary transaction).
- **Manual updates** (payment methods, location, fee, mode): ~€0.01 per change (separate transaction required)
- **Frequency:** Stats update with every covenant; manual updates rarely (<1x/month) 

---

## The Trade-off

| On-Chain Reputation | Nostr-Only Reputation |
|---------------------|----------------------|
| **Gains:** Trustless filtering, no messaging spam, fraud detection, permanent record | **Gains:** Free updates, privacy, rich metadata, real-time changes |
| **Losses:** Costs €0.01 to update, public/permanent, limited data (blockchain bloat) | **Losses:** Can't pre-filter, must message to discover, relay censorship risk |

---

## Why On-Chain (The Real Reason)

**Pre-filtering eliminates messaging spam.**

### The Discovery Problem

**Without on-chain filtering:**
```
Sender needs: BCH seller accepting Bizum in Madrid

Current approach:
1. Query bulletin board → 50 sellers visible
2. Message all 50: "Do you accept Bizum? Are you in Madrid?"
3. Wait for replies (30 seconds? 5 minutes? 2 hours?)
4. 12 respond "Yes"
5. Of those, 8 are actually active
6. Of those, 3 accept Bizum in Madrid
7. Wasted 47 messages, wasted time
```

**With on-chain filtering:**
```
Query blockchain:
  mode CONTAINS "seller"
  payment_methods CONTAINS "bizum"
  location = "madrid"
  last_activity < 24 hours

Results: 8 sellers match
Message only those 8
Response rate: >90% (they're actually active)
Time to first transaction: <2 minutes
```

**UX win:** Instant, trustless filtering. No spam. No waiting.

### The Fraud Detection Benefit

**Average value variance detects self-dealing:**

**Attack pattern (eBay-style reputation gaming):**
```
Attacker creates two Cash Accounts:
- Alice#123 (Bank Account 1)
- Bob#456 (Bank Account 2, same person)

Trades €100 back and forth 100 times:
- Alice → Bob: €100.00
- Bob → Alice: €100.00
- Repeat 50x

Goal: Inflate transaction count on both accounts
```

**Detection:**

The bot checks if two Cash Accounts share payment details (phone, IBAN, or name) using the same privacy-preserving hashing as the blacklist system. If 2 out of 3 fields match AND all transactions are suspiciously similar amounts, those transactions don't count toward reputation. To evade detection, the attacker needs different KYC'd bank accounts, different phone numbers, and different legal names—prohibitively expensive in Spain.

**Here's how it works in practice:**
```javascript
// Fraud signals
if (std_deviation(values) < 5% of average) {
  flag = "suspicious_pattern";  // All transactions same amount
}

if (payment_details_match(Alice, Bob)) {
  flag = "self_dealing";  // Same person, different accounts
  // Don't count these transactions
}
```

**Mitigation:** Bot checks payment details (phone, IBAN, name) using same hashing as blacklist system. If 2/3 fields match → same person → transactions don't count toward reputation.

**Why attacker can't easily evade:**
- Need different name (hard in Spain, KYC'd banks)
- Need different phone (hard, Bizum requires diferent phone numbers)
- Need different IBAN (new bank account each time)
- **Even if they do all this, statistical pattern still flags suspicious**

**Note:** Detailed fraud prevention mechanism documented separately (see Related Documents).

---

## Example: Sender Discovers Seller

**Scenario:** Carlos (sender) needs to send €200 to Venezuela via Asgaya.

**Step 1: Query on-chain reputation**

Carlos opens the app and enters €200. In the background, the app queries the blockchain: "Show sellers accepting Bizum, in Caracas, with capacity ≥€200, fee ≤1%, completion rate ≥90%, active in the last 24 hours." Results load by the time Carlos taps "Find sellers."

**The query in detail:**
```javascript
// Carlos's app queries blockchain in the background
// (payment method from settings, location auto-detected, amount typed: €200)
const sellers = await blockchain.querySellers({
  mode: ["seller"],
  payment_methods: ["bizum"],           // From Carlos's settings
  location: "caracas",                  // Auto-detected or set by user
  transaction_bracket_includes: 200,    // €200 is within [min, max]
  max_fee_percent: 1.0,                 // Carlos's threshold
  min_completion_rate: 90%,             // Calculated: completed/total
  min_total_transactions: 50,
  last_activity_within: 86400           // 24 hours
});

// Results: 12 sellers match
// Sorted by: fee_percent ASC, completion_rate DESC, total_transactions DESC
```

**UX note:** The query happens seamlessly while Carlos types the amount. By the time he taps "Find sellers," results are already loaded. If he changes payment method in settings, the app re-queries instantly.

**Phase 1+ optimization:** Nostr relays may cache the bulletin board (updated hourly) for faster queries, falling back to blockchain for trustless verification.

**Step 2: Check Nostr for real-time details**
```javascript
// Carlos's app queries Nostr for the top 5 sellers
const details = await nostr.getSellerDetails(sellers.slice(0, 5));

// Shows:
// - Current limits: €50-€500
// - Average reply time: <1 minute
// - Availability: Online NOW
// - Detailed terms/conditions
```

**Step 3: Select and transact**
```
Carlos picks Isabel#142:
- 0.5% fee (€1 on his €200 transaction)
- Capacity: €50-€500 (his €200 fits comfortably)
- 95.3% completion rate (121/127 completed)
- Avg value: €145.80 (handles similar amounts)
- Location: Caracas
- Online now, <1min reply time
- Accepts Bizum

Carlos messages Isabel via Nostr
Isabel's bot responds under 30 seconds
Transaction proceeds
```

**For Carlos:** Instant filtering → only message relevant sellers → fast transaction  
**For Isabel:** Only receives messages from qualified senders (reduces spam)

---

## Phase 0 Validation: What We're Testing

| Area | Question | Hypothesis |
|------|----------|------------|
| **Discovery** | % of users who find matching seller in <2 minutes? | >90% |
| | % reduction in messaging spam vs no filtering? | >70% |
| **Trust** | Do users check completion rate before selecting? | Yes, >80% |
| | Minimum acceptable completion rate? | ~85-90% |
| **Fraud** | Self-dealing attempts detected? | 100% (payment detail matching) |
| | False positive rate (legitimate users flagged)? | <1% |
| **Economics** | Is €0.01 listing cost acceptable to sellers? | Yes (one-time cost, permanent benefit) |
| | How often do sellers update payment methods? | Rarely (<1x per month) |

**Success criteria:**
- >90% users find matching seller <2 minutes
- >85% check completion rate before selecting
- 100% self-dealing detection, <1% false positives
- Sellers rate on-chain cost as "acceptable"

---

## The Limitations (And Why We Accept Them)

| Limitation | Impact | Mitigation | Why We Accept |
|------------|--------|------------|---------------|
| **Update cost** | Changing payment methods costs €0.01 | Updates are rare (seller sets once); transaction stats update automatically | One-time cost, permanent filtering benefit |
| **Public visibility** | Reputation is public, linked to Cash Account | Acceptable for sellers (public business); buyers can use fresh Cash Account per transaction | Transparency builds trust |
| **Update correlation** | Reputation updates after each covenant allow observers to estimate transaction volume and rhythm | Sellers are public businesses by design; listing on bulletin board already reveals activity pattern; volume visibility is inherent to the model | Sellers choose what information to reveal (can use LLC, P.O. Box, company phone for additional privacy) |
| **Immutability** | Can't delete transaction history | Seller can create new Cash Account if needed; completion rate naturally improves over time | Same trade-off as blockchain transactions |
| **Blockchain bloat** | ~180 bytes per seller (vs free Nostr) | Only essential filtering data on-chain; detailed metadata on Nostr | ~95% reduction vs putting all data on-chain |

**Note on fraud prevention:** Payment detail matching prevents self-dealing by detecting when two Cash Accounts belong to the same person (same phone/IBAN/name). See [Fraud Protection](../fraud-protection.md) for technical implementation.

---

## The Real Constraint

**Reputation on-chain isn't free. It's filterable.**

Central database reputation is free to update but requires trust. Nostr-only reputation is permissionless but requires messaging spam. On-chain reputation costs €0.01 but enables instant, trustless filtering.

**The constraint we're optimizing:** Not "how do we make reputation free?" but "how do we enable discovery without messaging spam or central databases?"

**On-chain reputation is the answer. Not free. Filterable.**

---

## Related Documents

- [Dispute Resolution via Nostr Blacklist](../../the-mechanism/nostr-coordination/dispute-resolution.md) (permissionless blacklist, privacy-preserving hashes)
- [Fraud Protection](../fraud-protection.md) (device health checks, proactive fraud prevention)
- [Bulletin Board Architecture](../../the-mechanism/bulletin-board/README.md) (Nostr integration, real-time queries)
- [Cash Accounts: Permissionless Identity Layer](./cash-accounts-permissionless-identity-layer.md) (how identity works)

---

**Status:** Phase 0 Implementation  
**Last Updated:** 2026-07-15  
**Confidence:** High (minimal on-chain data proven in other P2P systems; fraud detection reuses blacklist mechanism)
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Constraints](README.md)** | **[📖 Glossary](../../glossary.md)**
