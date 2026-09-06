# Android App: Implementation Architecture

**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**Purpose:** Index of Asgaya client implementation docs (Android-first, platform-agnostic architecture)

**Status:** Phase 0 in progress — Core covenant flow production-proven (all paths validated on Pi-chan July-Aug 2026; first inter-device claim August 10, 2026)

---

## Overview

Asgaya implements peer-to-peer Bitcoin Cash remittances with **no backend servers**:
- **Blockchain** = database (covenants stored on-chain)
- **Electrum servers** = query layer (balance, broadcast, UTXO)
- **Nostr relays** = coordination layer (encrypted parameter exchange)
- **User's device** = execution (covenant creation, claiming, auto-funding)

**Core thesis:** Remittances are the funnel to merchants. The covenant's `merchantCashout` path enables cash pickup, making Asgaya a permissionless merchant acquisition system.

**Key principle:** Any competent developer (not blockchain expert) can build an Asgaya client from these docs.

---

## Development Philosophy

**Pragmatic approach (Jun-Aug 2026):**

1. **Study existing apps first** - Learn from working code, don't reinvent
2. **Small incremental changes** - Save strength for integration (reliability + UX)
3. **Choose our battles** - Identify blockers → decide: redesign vs restart vs defer
4. **Incremental validation** - Each milestone proves next step is possible

**Note:** This philosophy emerged from BizumParser work and WebView pivot. Future contributors (or AI in fresh context) benefit from understanding the pragmatic, incremental approach that got us to production.

---

## Phase Model (Canonical)

| Phase | Definition | Status |
|-------|------------|--------|
| **Phase 0** | MVP in development (covenant flow working, integration ongoing) | 🔴 In progress |
| **Phase 0+** | Features added *during* testing (cheap UX/reliability wins) | 🔵 Opportunistic |
| **Phase 1+** | Post-MVP enhancements based on observed behavior | 🟢 Future |

**Phase 0 MVP Definition:** Full value proposition working end-to-end:
- **Fiat onramp:** Sender → Seller (Bizum/cash) → Covenant funded → Recipient notified
- **Merchant off-ramp:** Recipient → Merchant (cash exchange) → BCH received via `merchantCashout`

---

## Current Implementation (Phase 0 — August 2026)

**Production-proven components:**

| Component | What It Does | Status | Doc |
|-----------|-------------|--------|-----|
| **Multi-Wallet** | Sender/recipient/merchant wallet switching, Room persistence | ✅ Working | [wallet.md](wallet.md) |
| **CovenantWebView** | Kotlin ↔ JS bridge, CashScript SDK integration, P2SH32 address generation | ✅ Working (v0.2 hybrid: compute-only) | [webview-covenant-bridge.md](webview-covenant-bridge.md) |
| **CovenantBuildService** | v0.2: Kotlin network ops (UTXO fetch + broadcast, native TCP) | ✅ Working | [webview-covenant-bridge.md](webview-covenant-bridge.md) |
| **ElectrumClient** | Balance queries, transaction broadcast, UTXO management | ✅ Working | [connection-management-patterns.md](connection-management-patterns.md) |
| **NotificationListener** | Telegram parameter parsing (testing tool + fallback; Nostr is Phase 0 target) | ✅ Working | [notification-bot.md](notification-bot.md) |
| **Connection Management** | TCP cooldown (5s after balance query), WebSocket cleanup, manual updates | ✅ Working (v0.2: WebSocket only for brief UTXO fetch) | [connection-management-patterns.md](connection-management-patterns.md) |
| **SendViewModel** | v0.2: viewModelScope transaction state, pending_transactions DB, rebroadcast, background confirmation (RS083) | ✅ Working | [state-management.md](state-management.md) |

**Covenant v2.6:** All 5 spending paths implemented (claim, merchantCashout, refund, abort, sellerRecoverBuffer); v2.5 validated on chipnet, abort on testnet3 (Aug 15)

**v0.2 hybrid architecture (Aug 20-21):** All 4 covenant operations (CREATE/REFUND/CLAIM/ABORT) now build in the WebView and broadcast from Kotlin. WebView is compute-only. Multi-device reliability restored (the WebSocket connection-accumulation bug is eliminated at the root).

**Evidence:** First inter-device claim August 10, 2026 ([TXID](https://github.com/bitcoin-cash-node/bitcoin-cash-node): 193c3c9e5287e13cc56e1401aed55de34db9a375312e052807aea060e58e3d96)

**See complete flow:** [claim-flow-end-to-end.md](claim-flow-end-to-end.md)

---

## Phase-Ranked Feature Map

### Phase 0 (In Development)

| Feature | Status | Notes |
|---------|--------|-------|
| Multi-wallet management | ✅ Done | Room database, WalletManager, reactive UI |
| Covenant v2.6 (5 paths) | ✅ Done | All paths validated (v2.5 on chipnet, abort on testnet3) |
| WebView + CashScript SDK | ✅ Done | v0.2 hybrid: compute-only (build/sign), broadcast in Kotlin |
| ElectrumClient (balance/broadcast) | ✅ Done | TCP + WebSocket support |
| Telegram parameter parsing | ✅ Done | Testing tool + fallback (Nostr is target) |
| Connection management patterns | ✅ Done | TCP cooldown, cleanup, manual updates |
| Self-funded sender flow | ✅ Done | Also a real future use case (BCH stable, B2B, CEX savings) |
| **v0.2 hybrid (build + Kotlin broadcast)** | ✅ Done | CREATE/REFUND/CLAIM/ABORT all hybrid (Aug 20-21) |
| **Nostr coordination (DM)** | 🔨 In progress | Phase 0 target (automation needed for real covenants) |
| **Bulletin board** | 🔨 Planned | Merchant/seller discoverability (MVP needs it) |
| **Cash Accounts** | 🔨 MVP REQUIRED | Register/resolve `Elena#142`; match key for seller auto-funding Bizum concept field (interface to legacy payment system) |
| **Merchant cash-out flow** | ✅ DONE on-chain | Merchant-first, TXID `05301369...` (Sep 1, 2026) |
| **First BCH seller (Suso)** | 🔨 Needed | MVP validation requires real seller |

### Phase 0+ (During Testing — Opportunistic)

| Feature | Why 0+ | Notes |
|---------|--------|-------|
| **Seed phrase backup** | Cheap UX win | BIP39 HD wallets, 12-word recovery |
| **Seller device health** | Already built! | RS072/RS075 DeviceHealthMonitor.kt exists; needs integration + bulletin board ranking |
| **Stability layer (H€/HAu)** | Retention incentive | Launch-first per `stability-layer.md`; merchant hook |
| **Kraken API** | Volume dependent | Manual/mining covers Phase 0; automate when needed |

### Phase 1+ (Post-MVP)

| Feature | Why defer | Notes |
|---------|-----------|-------|
| Offline-first | Can wait | Online-only acceptable for MVP |
| Own Nostr relay | Operational burden | Public relays sufficient; revisit if reliability issues |
| Cross-border expansion | Progressive rollout | Spain→Venezuela first, then PagoMóvil/M-Pesa |
| Multi-covenant batching | Optimization | Not needed at MVP scale |

---

## Component Documentation

### Phase 0 (Implemented / In Progress)

1. **[claim-flow-end-to-end.md](claim-flow-end-to-end.md)** ✅  
   Complete August 10 flow, architecture decisions, production evidence

2. **[connection-management-patterns.md](connection-management-patterns.md)** ✅  
   TCP cooldown, WebSocket cleanup, manual updates, seller resolution notifications (production-proven Aug 8-10)

3. **[webview-covenant-bridge.md](webview-covenant-bridge.md)** ✅  
   Kotlin ↔ JavaScript bridge, CashScript SDK bundling, Webpack config (4 claims + 3 refunds validated)

3b. **[merchant-cashout-flow.md](merchant-cashout-flow.md)** ✅  
   Merchant-first cashout (merchant pre-signs, recipient co-signs); first on-chain transaction Sep 1, 2026

4. **[asgaya-trinity.md](asgaya-trinity.md)** ✅  
   Create → Send → Claim architecture, scope boundary (wallet not infrastructure)

5. **[two-layer-architecture.md](two-layer-architecture.md)** ✅  
   Covenant (permissionless) vs Client (opinionated) separation principle

6. **[wallet.md](wallet.md)** 🔨  
   Multi-wallet ✅, HD derivation (Phase 0+), Cash Account registration (Phase 0+)

7. **[notification-bot.md](notification-bot.md)** 🔨  
   Telegram parsing ✅, bank notification parsing (Phase 0+), market price subscription (Phase 0+)

8. **[state-management.md](state-management.md)** 🔨  
   Wallet storage ✅ (Room + WalletEntity), covenant tracking (Phase 0+), bulletin board cache (Phase 0+)

### Phase 0+ / Phase 1+ (Future)

9. **[bulletin-board.md](bulletin-board.md)** — Electrum NFT queries, listing discovery, seller/merchant matching
10. **[nostr.md](nostr.md)** — Relay management, NIP-44 encrypted DMs, payment coordination
11. **[stability-layer.md](stability-layer.md)** — H€/HAu token detection, AnyHedge integration, merchant retention hook
12. **[offline-first.md](offline-first.md)** — Offline queue, cache strategies, sync patterns

**Legend:**
- ✅ Production-proven
- 🔨 Partially implemented or in progress
- No icon = Phase 1+ (design complete, not implemented)

---

## Key Implementation Challenges

**Phase 0 challenges (learned during Aug 2026 work):**
- **Connection management:** TCP cooldown prevents throttling; `finally` blocks prevent leaks
- **WebView broadcast instability:** WebView JS timers pause on screen-off → WebSocket hangs + connection accumulation. **Solved Aug 20 via v0.2 hybrid** — Kotlin owns the network, WebView does compute only
- **`send()` vs `build()`:** CashScript's `send()` polls for confirmation up to 10 min; `build()` returns signed hex locally. Always `build()` + Kotlin broadcast
- **UTXO field conventions:** libauth uses `tx_hash`/`tx_pos`; CashScript SDK uses `txid`/`vout`. Mixing them crashes with "reading 'length'" (Aug 21)
- **Covenant state sync:** Manual "Update Status" button works; background polling is Phase 0+
- **WebView bridge:** CashScript SDK integration solved manual encoding bugs; 10MB bundle acceptable

**Phase 0+ challenges (deferred):**
- **Notification bot reliability:** Must not miss payments (seller loses money); RS072 DeviceHealthMonitor addresses this
- **Cash Account collisions:** `name#number` format disambiguates (Phase 0+ implementation)
- **Offline queue management:** Queue size limits, stale item expiry (Phase 1+ when offline-first implemented)
- **Key recovery UX:** Manual WIF import works; BIP39 seed phrase is Phase 0+ UX win

**See component docs for detailed solutions.**

---

## External Dependencies

**Phase 0 (in use):**
- **Electrum/Fulcrum server** - Pi-chan `192.168.1.100` (60001 TCP, 60003 WS), **testnet3**; Phase 0+ adds redundancy (3-5 servers)
- **Telegram app** - Testing tool + fallback for parameter coordination

**Phase 0+ (planned):**
- **Nostr relays** - NIP-44 encrypted DMs (target coordination layer); public relays sufficient
- **Bank apps** - NotificationListener for seller auto-funding (Bizum, PagoMóvil)
- **Kraken API** - Optional BCH replenishment when manual/mining insufficient

**See component docs for integration details.**

---

## Related Documentation

**Conceptual (read first):**
- [The Mechanism](../../the-mechanism/README.md) - How covenants, bulletin board, Nostr work from user perspective
- [User Journeys](../../user-journeys/README.md) - Sender, recipient, merchant, trader flows
- [Why This Design](../../why-this-design/README.md) - Architectural constraints & rationale

**Implementation (covenants):**
- [Covenant Version History](../covenants/version-history.md) - v2.6 specification, all 5 spending paths
- [Manual Construction](../covenants/manual-construction.md) - Future: pure Kotlin covenant building (Phase 1+ migration from WebView)

**Research:**
- RS070 - Android app development decisions
- RS072 - Notification listener + device health monitoring (DeviceHealthMonitor.kt exists!)
- RS075 - Android app health detection (battery, app status, permissions)
- RS082 - 0-conf security economics for remittances
- RS083 - Transaction broadcast UI patterns (ViewModel, navigate-on-success, rebroadcast; hybrid architecture validation)

**Media (Phase 0+ content refresh needed):**
- **Radio Asgaya** 📻 - 105 podcast episodes, workflow proven; content needs re-audit against covenant v2.6 + funder principle before public use  
  Location: `knowledge/meta/radio_asgaya/`

---

## Success Criteria

**Phase 0 success (August 10, 2026):** ✅ Achieved
- Core flow working (covenant creation → funding → claiming)
- Production evidence (inter-device claim, all paths validated)
- Architecture decisions proven (WebView, connection patterns, Telegram coordination)

**Full MVP success (Phase 0 ongoing):** 🔨 In Progress
- Nostr coordination working (replace Telegram copy-paste)
- Bulletin board functional (merchant/seller discovery)
- Merchant cash-out integrated (covenant path tested, needs UI)
- First real seller (Suso) + first real merchant

**Documentation success:** ✅
- Contributors know what exists (don't search for non-existent code)
- Future vision preserved (don't lose the architecture)
- AI-readable (knowledge base tested Aug 14)

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Implementation](../README.md)** | **[📖 Glossary](../../glossary.md)**

**This section:** [Android App](README.md)  
**Related:** [Covenants](../covenants/version-history.md) | [The Mechanism](../../the-mechanism/README.md)

---

**Last updated:** 2026-08-24 (v0.2 hybrid + covenant v2.6 path count)  
**Status:** Phase 0 in progress (covenant flow proven, integration ongoing)  
**Evidence:** First inter-device claim TXID 193c3c9e5287e13cc56e1401aed55de34db9a375312e052807aea060e58e3d96
