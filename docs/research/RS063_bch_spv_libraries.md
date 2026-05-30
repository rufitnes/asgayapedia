# RS063: BCH SPV Wallet Libraries & Electrum Client Strategy

**Date:** May 30, 2026  
**Researcher:** Suso + DeepSeek  
**Context:** Documentation link cleanup revealed broken library references. Investigation led to architectural decision.  
**Status:** ✅ Resolved - Electrum client approach recommended

---

## Research Trigger

During broken link cleanup, found two 404 errors in `opreturn-spv.md`:
- `github.com/pokkst/bitcoincashj-thin` (Android SPV library)
- `cashweb.cash/` (Cross-platform Kotlin Multiplatform library)

Both libraries appeared to be discontinued. Needed to verify current state of BCH SPV ecosystem and provide updated recommendations.

---

## Research Objectives

1. **Android:** Verify bitcoincashj status and find maintained replacement
2. **Cross-platform:** Investigate Cash-Lib status and KMP alternatives  
3. **iOS:** Confirm BitcoinKit.Swift is still recommended
4. **Architecture:** Determine best approach for OP_RETURN monitoring on mobile

**Technical Requirements:**
- SPV wallet functionality OR lightweight alternative
- OP_RETURN message parsing
- Monitor BCH addresses for incoming transactions
- Mobile-friendly (Android/iOS)

---

## Findings

### 1. Android: bitcoincashj Is Unmaintained

**Status:** `pokkst/bitcoincashj` is no longer maintained (confirmed by user)

**Investigation:**
- Original repo: `github.com/pokkst/bitcoincashj` 
- 2025 modular split (`bitcoincashj-core`, `bitcoincashj-wallet`) did not sustain maintenance
- No direct, actively maintained SPV wallet replacement exists for BCH on Android

**Alternatives Evaluated:**

| Option | Status | Viability | Notes |
|--------|--------|-----------|-------|
| Fork network | Unknown | Low | Unverified community forks, risky dependency |
| Bitcoin-Verde | Active | Low | Full node (softwareverde/bitcoin-verde), too heavy for mobile |
| **Electrum client** | **Active** | **High** | **Query Fulcrum server directly - lightweight, uses existing infra** |

**Recommendation:** **Use Electrum JSON-RPC client library**

**Rationale:**
- Asgaya already uses Fulcrum/Electrum infrastructure (pichan setup)
- No need for local SPV node on mobile device
- Simpler architecture: query server for address history + OP_RETURN data
- Keeps mobile app thin and fast
- Uses controlled infrastructure (own Electrum server)

---

### 2. Cross-Platform: Cash-Lib Is Discontinued

**Status:** `cashweb.cash` domain is dead, project discontinued

**Investigation:**
- Original Cash-Lib (Kotlin Multiplatform) no longer exists
- Successor project: `CashPay KMP` (`github.com/kabuto-fork/CashPay-KMP`)
  - Focus: Payment protocol integration
  - Does NOT provide SPV functionality

**No mature KMP SPV library exists for BCH**

**Recommendation:** Platform-native approach
- Android: Electrum client (Kotlin/Java)
- iOS: BitcoinCashKit.Swift
- Shared logic: KMP module abstracts platform-specific calls

---

### 3. iOS: BitcoinKit Active, BitcoinCashKit Preferred

**Status:** `horizontalsystems/BitcoinKit.Swift` is active and maintained ✅

**Better option:** `horizontalsystems/BitcoinCashKit.Swift`
- Built on top of BitcoinKit
- Adds BCH-specific features (OP_RETURN, CashAddr format)
- Active maintenance
- Correct choice for Asgaya

**URL:** `github.com/horizontalsystems/BitcoinCashKit.Swift`

---

## Architectural Decision: Electrum > SPV for Mobile

### Why Electrum Client Wins

**For Asgaya's use case (OP_RETURN notification monitoring):**

| Aspect | SPV Wallet | Electrum Client |
|--------|-----------|-----------------|
| **Complexity** | High (full wallet implementation) | Low (JSON-RPC queries) |
| **Resources** | Heavy (blockchain sync, storage) | Light (server queries only) |
| **Infrastructure** | New dependency | Uses existing Fulcrum server |
| **Maintenance** | bitcoincashj unmaintained | Electrum protocol stable |
| **Functionality** | Full wallet + OP_RETURN | OP_RETURN monitoring only (sufficient) |

**What Asgaya needs:**
- ✅ Monitor user's BCH address for incoming transactions
- ✅ Parse OP_RETURN messages (e.g., `ASGAYA_TXN_READY_7382`)
- ✅ Notify user when covenant events happen

**What Asgaya does NOT need:**
- ❌ Full SPV wallet with UTXO management
- ❌ Private key storage on mobile (covenants handle BCH)
- ❌ Transaction broadcasting from mobile (backend does this)

**Conclusion:** Electrum client provides exactly what's needed, nothing more.

---

## Implementation Recommendations

### Android: Electrum Client

**Library options to evaluate:**
- `electrumj` (if exists for Kotlin/Java)
- Custom Electrum JSON-RPC client (simple to implement)
- Retrofit + OkHttp for Electrum server communication

**API calls needed:**
```kotlin
// Monitor address for new transactions
electrum.getHistory(address)

// Get transaction details (includes OP_RETURN)
electrum.getTransaction(txid)

// Parse OP_RETURN from transaction
parseOpReturn(tx.outputs)
```

### iOS: BitcoinCashKit.Swift

**Library:** `github.com/horizontalsystems/BitcoinCashKit.Swift`

**Features:**
- SPV wallet functionality (optional for iOS)
- OP_RETURN parsing built-in
- CashAddr support
- Active maintenance

### Cross-Platform Strategy

**Approach:** Platform-native with shared KMP logic

```
┌─────────────────────────────────────────┐
│   Kotlin Multiplatform Module (KMP)    │
│   - OP_RETURN message parsing logic    │
│   - Notification data models            │
│   - Business logic (state management)   │
└──────────────┬──────────────────────────┘
               │
       ┌───────┴────────┐
       │                │
┌──────▼──────┐  ┌──────▼──────────┐
│   Android   │  │      iOS        │
│   Electrum  │  │  BitcoinCashKit │
│   Client    │  │                 │
└─────────────┘  └─────────────────┘
```

---

## Documentation Updates Applied

### 1. opreturn-spv.md

**File:** `/docs/android-app/notification-listener/opreturn-spv.md`

**Changes:**
1. Updated library recommendations (lines 108-117):
   - Android: Electrum Client (recommended) + note on bitcoincashj deprecation
   - iOS: BitcoinCashKit.Swift (BCH-specific)
   - Cross-platform: Platform-native approach with KMP shared logic

2. Wrapped legacy bitcoincashj implementation in deprecation warning + collapsible details section

3. Added recommendation for BitcoinCashKit.Swift in iOS section

---

## Key Takeaways

1. **bitcoincashj is dead** - No maintained Android BCH SPV library exists
2. **Cash-Lib is dead** - No mature KMP solution for BCH
3. **Electrum client is better** - Lighter, simpler, uses existing infrastructure
4. **Architecture simplified** - Mobile apps don't need full SPV wallets
5. **iOS has good option** - BitcoinCashKit.Swift is active and BCH-specific

**Philosophy:** Use the simplest tool that solves the problem. For OP_RETURN monitoring, Electrum client > SPV wallet.

---

## Next Steps

- [x] Update `opreturn-spv.md` with new library recommendations
- [ ] Copy RS063 to `asgaya-docs/docs/research/`
- [ ] Document Electrum client integration approach (future RS064)
- [ ] Evaluate specific Electrum client libraries for Kotlin
- [ ] Update architecture diagrams to reflect Electrum dependency

---

## Sources

- **DeepSeek Research:** May 30, 2026 investigation
- **User confirmation:** bitcoincashj no longer maintained
- GitHub repositories:
  - `pokkst/bitcoincashj` (archived/unmaintained)
  - `horizontalsystems/BitcoinCashKit.Swift` (active)
  - `softwareverde/bitcoin-verde` (active but full node)
- Electrum protocol docs: https://electrum.readthedocs.io/en/latest/protocol.html

---

*Research completed: May 30, 2026*  
*Outcome: Architectural decision - Electrum client approach for mobile OP_RETURN monitoring*  
*Impact: Simplifies mobile implementation, removes dependency on unmaintained libraries*
