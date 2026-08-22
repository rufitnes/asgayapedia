# RS083: Transaction Broadcast UI State Management Patterns

**Date:** 2026-08-17 (research) · mirrored 2026-08-21  
**Type:** Technical research — Production wallet analysis  
**Status:** ✅ Confirmed + Implemented (Aug 17-21)  
**Phase:** 0 → 1 transition (Production readiness)  
**Full research:** `asgaya/knowledge/research/RS083_transaction_broadcast_ui_patterns.md` (private knowledge base)

---

## Executive Summary

**Observation:** Asgaya's covenant funding/refund/abort operations suffered from "stuck sending..." UI bugs where transactions succeed but the UI never updates.

**Key findings from studying production wallets (Selene, Paytaca):**
1. **Never reset the "sending" flag on success** — navigate away instead
2. **Persist transactions to the database immediately** after broadcast, with status tracking (BROADCAST → MEMPOOL → CONFIRMED)
3. **`lifecycleScope` is wrong for broadcasts** — it cancels coroutines on activity destruction, losing callbacks even when the tx succeeds
4. **Rebroadcast on resume is standard** — apps check for unconfirmed transactions and retry on startup

**Recommendation (adopted):** navigate on success, persist transactions, rebroadcast on resume, use `viewModelScope`.

---

## The Problem (Asgaya, Aug 17)

- `ReviewSendActivity.sendTransaction()` ran in `lifecycleScope.launch`
- If the user navigated away mid-broadcast: activity destroyed → coroutine cancelled → tx succeeded on-chain but no callback → UI stuck at "Sending..."
- No persistence, no rebroadcast, no DB record

**Observed:** transaction succeeded on-chain (TXID `e9254e7e...`) but UI showed failure/timeout.

---

## What Was Adopted (Implemented Aug 17-18)

1. **`SendViewModel` using `viewModelScope`** — survives activity destruction
2. **`pending_transactions` table** (Room DB) — status BROADCAST → MEMPOOL → CONFIRMED
3. **Background confirmation monitoring** — `viewModelScope` coroutine polls Electrum (60s, up to 1h), updates DB
4. **Rebroadcast on resume** — `onResume()` checks non-confirmed txs, rebroadcasts if dropped
5. **Navigate-on-success** — don't reset the "sending" flag; navigate away (Selene pattern)
6. **Toast crash fix** — toasts were crashing when called from background threads; moved to `Dispatchers.Main`

**Architecture:** `viewModelScope` + Room `pending_transactions` + `CovenantBuildService` (v0.2 hybrid) for network ops.

---

## Relationship to v0.2 Hybrid (Aug 20-21)

RS083 is the **state-management** layer; the v0.2 hybrid is the **network-reliability** layer. Together:
- RS083: transactions survive activity death + rebroadcast (state)
- v0.2 hybrid: `build()` + Kotlin broadcast (network) — eliminates the WebSocket hang class entirely

The hybrid made broadcast reliable at the network level; RS083 made it reliable at the state level. Both are required and now both are implemented.

---

## Implementation Reference

- [state-management.md](../implementation/android-app/state-management.md) — `pending_transactions` schema + SendViewModel
- [connection-management-patterns.md](../implementation/android-app/connection-management-patterns.md) — Issue 4/5 (superseded workarounds → real fixes)
- [webview-covenant-bridge.md](../implementation/android-app/webview-covenant-bridge.md) — v0.2 hybrid (build + Kotlin broadcast)

---

**Status:** ✅ Confirmed + Implemented (Aug 17-21, 2026)  
**Evidence:** Multi-device testing passing after v0.2 hybrid; transactions persist and confirm in background
