# Roadmap Mining Corpus (deterministic)

> Auto-extracted 2026-09-06: every line containing Phase 0/0+/1/1+/MVP in docs/ (research/ excluded).

## `docs/cold-start-strategy/README.md`  (4)

- Phase 0  | docs/cold-start-strategy/README.md:16  | - Phase 0 → Phase 1 transition criteria must be clear
- Phase 0  | docs/cold-start-strategy/README.md:20  | ### 1. Phase 0 Approach
- Phase 0  | docs/cold-start-strategy/README.md:23  | - Who are the Phase 0 participants?
- Phase 1  | docs/cold-start-strategy/README.md:24  | - What metrics define "ready for Phase 1"?

## `docs/glossary.md`  (1)

- Phase 0  | docs/glossary.md:217  | A parallel Bitcoin Cash blockchain with worthless coins, used for safe development and validation. All Phase 0 covenant testing happens on testnet3 before mainnet. (Chipnet is a separate testnet used for covenant‑upgrade testing.)

## `docs/implementation/README.md`  (5)

- Phase 0  | docs/implementation/README.md:10  | **Phase:** Phase 0 - Production Core Flow Ready 🏆
- Phase 1  | docs/implementation/README.md:48  | - ⏳ **Move covenant UTXO fetch to Kotlin** - REFUND/CLAIM/ABORT still use brief WebSocket for `contract.getUtxos()`; moving to Kotlin makes WebView 100% network-free (Phase 1 enhancement)
- Phase 1+  | docs/implementation/README.md:116  | ### 📝 To Be Created (Phase 1+)
- Phase 0  | docs/implementation/README.md:121  | ### 🔄 Needs Review (Phase 0)
- Phase 0  | docs/implementation/README.md:149  | **Phase 0 focus:** Minimalistic implementation. Less stuff to break. No optional backend services.

## `docs/implementation/android-app/README.md`  (36)

- Phase 0  | docs/implementation/android-app/README.md:7  | **Status:** Phase 0 in progress — Core covenant flow production-proven (all paths validated on Pi-chan July-Aug 2026; first inter-device claim August 10, 2026)
- Phase 0  | docs/implementation/android-app/README.md:42  | | **Phase 0** | MVP in development (covenant flow working, integration ongoing) | 🔴 In progress |
- Phase 0+  | docs/implementation/android-app/README.md:43  | | **Phase 0+** | Features added *during* testing (cheap UX/reliability wins) | 🔵 Opportunistic |
- Phase 1+  | docs/implementation/android-app/README.md:44  | | **Phase 1+** | Post-MVP enhancements based on observed behavior | 🟢 Future |
- Phase 0  | docs/implementation/android-app/README.md:46  | **Phase 0 MVP Definition:** Full value proposition working end-to-end:
- Phase 0  | docs/implementation/android-app/README.md:52  | ## Current Implementation (Phase 0 — August 2026)
- Phase 0  | docs/implementation/android-app/README.md:62  | | **NotificationListener** | Telegram parameter parsing (testing tool + fallback; Nostr is Phase 0 target) | ✅ Working | [notification-bot.md](notification-bot.md) |
- Phase 0  | docs/implementation/android-app/README.md:78  | ### Phase 0 (In Development)
- Phase 0  | docs/implementation/android-app/README.md:90  | | **Nostr coordination (DM)** | 🔨 In progress | Phase 0 target (automation needed for real covenants) |
- MVP  | docs/implementation/android-app/README.md:91  | | **Bulletin board** | 🔨 Planned | Merchant/seller discoverability (MVP needs it) |
- MVP  | docs/implementation/android-app/README.md:93  | | **First BCH seller (Suso)** | 🔨 Needed | MVP validation requires real seller |
- Phase 0+  | docs/implementation/android-app/README.md:95  | ### Phase 0+ (During Testing — Opportunistic)
- Phase 0  | docs/implementation/android-app/README.md:103  | | **Kraken API** | Volume dependent | Manual/mining covers Phase 0; automate when needed |
- Phase 1+  | docs/implementation/android-app/README.md:105  | ### Phase 1+ (Post-MVP)
- MVP  | docs/implementation/android-app/README.md:109  | | Offline-first | Can wait | Online-only acceptable for MVP |
- MVP  | docs/implementation/android-app/README.md:112  | | Multi-covenant batching | Optimization | Not needed at MVP scale |
- Phase 0  | docs/implementation/android-app/README.md:118  | ### Phase 0 (Implemented / In Progress)
- Phase 0+  | docs/implementation/android-app/README.md:139  | Multi-wallet ✅, HD derivation (Phase 0+), Cash Account registration (Phase 0+)
- Phase 0+  | docs/implementation/android-app/README.md:142  | Telegram parsing ✅, bank notification parsing (Phase 0+), market price subscription (Phase 0+)
- Phase 0+  | docs/implementation/android-app/README.md:145  | Wallet storage ✅ (Room + WalletEntity), covenant tracking (Phase 0+), bulletin board cache (Phase 0+)
- Phase 0+  | docs/implementation/android-app/README.md:147  | ### Phase 0+ / Phase 1+ (Future)
- Phase 1+  | docs/implementation/android-app/README.md:157  | - No icon = Phase 1+ (design complete, not implemented)
- Phase 0  | docs/implementation/android-app/README.md:163  | **Phase 0 challenges (learned during Aug 2026 work):**
- Phase 0+  | docs/implementation/android-app/README.md:168  | - **Covenant state sync:** Manual "Update Status" button works; background polling is Phase 0+
- Phase 0+  | docs/implementation/android-app/README.md:171  | **Phase 0+ challenges (deferred):**
- Phase 0+  | docs/implementation/android-app/README.md:173  | - **Cash Account collisions:** `name#number` format disambiguates (Phase 0+ implementation)
- Phase 1+  | docs/implementation/android-app/README.md:174  | - **Offline queue management:** Queue size limits, stale item expiry (Phase 1+ when offline-first implemented)
- Phase 0+  | docs/implementation/android-app/README.md:175  | - **Key recovery UX:** Manual WIF import works; BIP39 seed phrase is Phase 0+ UX win
- Phase 0  | docs/implementation/android-app/README.md:183  | **Phase 0 (in use):**
- Phase 0+  | docs/implementation/android-app/README.md:184  | - **Electrum servers** - Chipnet.imaginary.cash (60001 TCP, 60003 WS); Phase 0+ adds redundancy (3-5 servers)
- Phase 0+  | docs/implementation/android-app/README.md:187  | **Phase 0+ (planned):**
- Phase 1+  | docs/implementation/android-app/README.md:205  | - [Manual Construction](../covenants/manual-construction.md) - Future: pure Kotlin covenant building (Phase 1+ migration from WebView)
- Phase 0+  | docs/implementation/android-app/README.md:214  | **Media (Phase 0+ content refresh needed):**
- Phase 0  | docs/implementation/android-app/README.md:222  | **Phase 0 success (August 10, 2026):** ✅ Achieved
- Phase 0  | docs/implementation/android-app/README.md:227  | **Full MVP success (Phase 0 ongoing):** 🔨 In Progress
- Phase 0  | docs/implementation/android-app/README.md:250  | **Status:** Phase 0 in progress (covenant flow proven, integration ongoing)

## `docs/implementation/android-app/asgaya-trinity.md`  (13)

- Phase 1+  | docs/implementation/android-app/asgaya-trinity.md:132  | **Status:** ✅ WebView + CashScript SDK proven on testnet3 (August 1-2, 2026). Manual construction attempted but not validated - deferred to Phase 1+.
- Phase 0  | docs/implementation/android-app/asgaya-trinity.md:170  | ├─ Phase 0: Telegram fallback ✅
- Phase 1  | docs/implementation/android-app/asgaya-trinity.md:171  | ├─ Phase 1: Nostr coordination (planned)
- Phase 0  | docs/implementation/android-app/asgaya-trinity.md:183  | ### Telegram Fallback (Phase 0)
- Phase 1  | docs/implementation/android-app/asgaya-trinity.md:225  | ### Nostr Coordination (Phase 1)
- Phase 1  | docs/implementation/android-app/asgaya-trinity.md:266  | **Status:** ⏳ Planned for Phase 1
- MVP  | docs/implementation/android-app/asgaya-trinity.md:377  | ## The Complete MVP Flow
- Phase 0  | docs/implementation/android-app/asgaya-trinity.md:461  | ## Testing Plan (Phase 0)
- Phase 0  | docs/implementation/android-app/asgaya-trinity.md:481  | **If this works:** We have a working end-to-end MVP. Phase 0 complete.
- Phase 1  | docs/implementation/android-app/asgaya-trinity.md:506  | ⏳ Nostr coordination (Phase 1)
- Phase 1  | docs/implementation/android-app/asgaya-trinity.md:523  | **Overall MVP progress: ~95% complete** (Nostr coordination deferred to Phase 1)
- Phase 0  | docs/implementation/android-app/asgaya-trinity.md:530  | - Phase 0: Covenant infrastructure proven, ready for production testing
- Phase 1  | docs/implementation/android-app/asgaya-trinity.md:557  | **Send:** Notify recipient via Telegram/Nostr (90% done - Nostr Phase 1)

## `docs/implementation/android-app/bulletin-board.md`  (5)

- Phase 0  | docs/implementation/android-app/bulletin-board.md:7  | > **⚠️ Phase 0 Priority - Essential for Mainnet Launch**
- Phase 0  | docs/implementation/android-app/bulletin-board.md:11  | > **Why Phase 0 (High Priority):**
- Phase 0  | docs/implementation/android-app/bulletin-board.md:18  | > **Implementation Status (August 14, 2026):** 🔨 Planned — design complete, not yet implemented. Phase 0 MVP target for merchant/seller discoverability.
- Phase 0  | docs/implementation/android-app/bulletin-board.md:308  | **Cache TTL:** 5 minutes (Phase 0)
- Phase 0  | docs/implementation/android-app/bulletin-board.md:520  | **Status:** Phase 0 - Design complete, implementation TODO (HIGH PRIORITY - blocking mainnet)

## `docs/implementation/android-app/claim-flow-end-to-end.md`  (2)

- Phase 0  | docs/implementation/android-app/claim-flow-end-to-end.md:422  | **⚠️ Phase 0 Workaround:**
- Phase 0  | docs/implementation/android-app/claim-flow-end-to-end.md:429  | **For now:** The 5-second delay is documented, tested, and prevents production hangs. It's acceptable for Phase 0 single-device-pair testing.

## `docs/implementation/android-app/connection-management-patterns.md`  (13)

- Phase 0  | docs/implementation/android-app/connection-management-patterns.md:120  | ### Phase 0 Workaround Status
- Phase 0  | docs/implementation/android-app/connection-management-patterns.md:122  | **⚠️ This is a Phase 0 workaround, not a long-term solution.**
- Phase 0  | docs/implementation/android-app/connection-management-patterns.md:135  | **For now:** 5-second delay is documented, tested, and prevents production hangs. This is acceptable for Phase 0.
- Phase 0  | docs/implementation/android-app/connection-management-patterns.md:383  | ### Current Pattern: Manual Updates (Phase 0)
- Phase 0  | docs/implementation/android-app/connection-management-patterns.md:435  | **Manual Updates (Current - Phase 0):**
- Phase 0  | docs/implementation/android-app/connection-management-patterns.md:443  | **Subscriptions (Future - Post Phase 0):**
- Phase 0  | docs/implementation/android-app/connection-management-patterns.md:560  | - This is behavioral economics - Phase 0 will reveal actual seller behavior
- Phase 0  | docs/implementation/android-app/connection-management-patterns.md:565  | **Phase 0 (Current):**
- Phase 1  | docs/implementation/android-app/connection-management-patterns.md:570  | **Phase 1 (After merchant flow):**
- Phase 0  | docs/implementation/android-app/connection-management-patterns.md:588  | ### Short-Lived Connections (Current - Phase 0)
- Phase 0  | docs/implementation/android-app/connection-management-patterns.md:681  | **For Phase 0:** Stick with short-lived connections!
- Phase 0  | docs/implementation/android-app/connection-management-patterns.md:713  | **Workaround (Phase 0):** Document behavior, accept UX trade-off for reliability
- Phase 0  | docs/implementation/android-app/connection-management-patterns.md:809  | 4. **Manual updates** in Phase 0 (subscriptions are future enhancement)

## `docs/implementation/android-app/merchant-cashout-flow.md`  (1)

- Phase 1  | docs/implementation/android-app/merchant-cashout-flow.md:97  | | **Nostr** (Phase 1) | Yes | Yes | Remote / low friction |

## `docs/implementation/android-app/nostr.md`  (5)

- Phase 0  | docs/implementation/android-app/nostr.md:7  | > **⚠️ Phase 0 Priority - Required for Mainnet Launch**
- Phase 0  | docs/implementation/android-app/nostr.md:11  | > **Why Phase 0 (High Priority):**
- Phase 0  | docs/implementation/android-app/nostr.md:19  | > - **Implementation (August 14, 2026):** 🔨 Phase 0 target — encrypted DM transport needed for sender→seller→recipient coordination (replaces Telegram copy-paste)
- Phase 1+  | docs/implementation/android-app/nostr.md:477  | **Mitigation (Phase 1+):** Use Tor or VPN when connecting to relays
- Phase 0  | docs/implementation/android-app/nostr.md:517  | **Status:** Phase 0 - Design complete, implementation TODO (HIGH PRIORITY - blocking mainnet)

## `docs/implementation/android-app/notification-bot.md`  (30)

- Phase 0  | docs/implementation/android-app/notification-bot.md:9  | **Phase 0 ✅ Implemented:**
- Phase 1+  | docs/implementation/android-app/notification-bot.md:14  | **Phase 1+ 🔨 Future (This Document's Main Focus):**
- Phase 1+  | docs/implementation/android-app/notification-bot.md:20  | **Important:** Most of this document describes Phase 1+ seller auto-funding patterns. Phase 0 implemented only Telegram parameter parsing (simpler, recipient-side use case). The comprehensive bank notification parsing described below is future work.
- Phase 0  | docs/implementation/android-app/notification-bot.md:24  | ## ⚠️ Telegram's Role (Phase 0): Test + Emergency Fallback Only
- Phase 0  | docs/implementation/android-app/notification-bot.md:27  | 1. **Testing tool** — used during Phase 0 development (copy-paste `[COVENANT_V25]` blocks)
- Phase 1+  | docs/implementation/android-app/notification-bot.md:32  | **Phase 1+ broader transport:** the parameter-sharing/notification flow should work across multiple messengers — **WhatsApp, LINE, Signal, SMS, etc.** This is a low-priority enhancement, but the design should not assume Telegram specifically. The pattern that makes this easy: parameters are always a structured text block (`[COVENANT_V25]` / `[CASH_IN_PERSON]`) that any transport can carry, and the…
- Phase 0  | docs/implementation/android-app/notification-bot.md:34  | **Implication for implementation:** keep the "copy to clipboard → paste anywhere" pattern (transport-agnostic), and keep the parser tag-based. Don't hardcode Telegram-specific behavior beyond the Phase 0 listener.
- Phase 1+  | docs/implementation/android-app/notification-bot.md:49  | **Phase 1+ approach:** Auto-fund only if wallet has sufficient BCH (show notification if insufficient)
- Phase 1+  | docs/implementation/android-app/notification-bot.md:50  | **Phase 1++ extension:** Auto-buy from exchange if wallet balance low (see Optional Extensions section)
- Phase 0  | docs/implementation/android-app/notification-bot.md:59  | **Legacy SMS support:** Some users (grandfathered accounts) still receive SMS for Bizum payments. This is increasingly rare as banks phase out SMS to reduce costs. The `smsbridge_loop.py` prototype demonstrated SMS parsing works, but Notification Listener Service is the primary approach for Phase 0.
- Phase 1+  | docs/implementation/android-app/notification-bot.md:151  | **Supported bank apps (Phase 1+ - Planned):**
- Phase 1+  | docs/implementation/android-app/notification-bot.md:279  | **Currency conversion needed:** Bs → EUR (use exchange rate oracle, Phase 1+)
- Phase 1+  | docs/implementation/android-app/notification-bot.md:522  | ## Exchange Integration (Phase 1+ Extension)
- Phase 0  | docs/implementation/android-app/notification-bot.md:524  | **Status:** Optional extension, not required for Phase 0
- Phase 0  | docs/implementation/android-app/notification-bot.md:526  | **Phase 0 approach:** If wallet balance insufficient, show notification to seller ("Received payment but wallet has only X BCH, need Y BCH. Top up wallet to fund covenant.")
- Phase 1+  | docs/implementation/android-app/notification-bot.md:528  | **Phase 1+ enhancement:** Auto-replenish from exchange (Kraken example below)
- Phase 0  | docs/implementation/android-app/notification-bot.md:780  | **Phase 0 approach:** Show notification, wait for user to investigate
- Phase 0  | docs/implementation/android-app/notification-bot.md:786  | **Phase 0 behavior:**
- Phase 0+  | docs/implementation/android-app/notification-bot.md:802  | **Phase 0+ enhancement: Notify sender via Nostr**
- Phase 1+  | docs/implementation/android-app/notification-bot.md:815  | **Phase 1+ with exchange integration:**
- Phase 0  | docs/implementation/android-app/notification-bot.md:820  | // Fallback to Phase 0 behavior (notify user)
- Phase 0  | docs/implementation/android-app/notification-bot.md:864  | - **Impact:** Passive selling (auto-funding) is **not possible** on iOS Phase 0
- Phase 0  | docs/implementation/android-app/notification-bot.md:865  | - **Phase 0 workaround:** Manual workflow (Isabel checks bank app, manually opens Asgaya, pastes covenant ID, clicks "Fund")
- Phase 1+  | docs/implementation/android-app/notification-bot.md:868  | - **Phase 1+ solution:** Bank API integration
- Phase 1+  | docs/implementation/android-app/notification-bot.md:873  | **Recommendation for Phase 0:** Focus on Android (where passive selling works). iOS can launch with manual workflow, upgrade to bank API in Phase 1+ when volume justifies API integration costs.
- Phase 1+  | docs/implementation/android-app/notification-bot.md:899  | - Insufficient BCH (Phase 0: notify user, Phase 1+: auto-buy from exchange)
- Phase 1+  | docs/implementation/android-app/notification-bot.md:945  | **Status:** Phase 0 ✅ Telegram parsing implemented | Phase 1+ 🔨 Bank auto-funding (this doc's focus)
- Phase 1+  | docs/implementation/android-app/notification-bot.md:946  | **Updated:** 2026-08-14 (status clarified - most of this doc is Phase 1+ design)
- Phase 0  | docs/implementation/android-app/notification-bot.md:951  | **Phase 0 implementation:** Telegram parameter parsing only (NotificationListenerService for Telegram app)
- Phase 1+  | docs/implementation/android-app/notification-bot.md:952  | **Phase 1+ scope:** Bank notification parsing, seller auto-funding, market price subscription, exchange integration

## `docs/implementation/android-app/offline-first.md`  (4)

- Phase 0  | docs/implementation/android-app/offline-first.md:7  | > **⚠️ Phase 0 Critical Infrastructure - Foundational Design Principle**
- Phase 0  | docs/implementation/android-app/offline-first.md:11  | > **Why Phase 0 (Critical Priority):**
- Phase 1+  | docs/implementation/android-app/offline-first.md:631  | - **Service workers:** Can cache API responses (advanced, Phase 1+)
- Phase 0  | docs/implementation/android-app/offline-first.md:672  | **Status:** Phase 0 - Design complete, implementation TODO (CRITICAL - app unusable without this)

## `docs/implementation/android-app/stability-layer.md`  (27)

- Phase 0+  | docs/implementation/android-app/stability-layer.md:7  | > **Phase 0+ - Launch First, Add Based on Observed Need**
- Phase 0  | docs/implementation/android-app/stability-layer.md:10  | > 1. **Phase 0 launch** - BCH only, no stability layer
- Phase 0+  | docs/implementation/android-app/stability-layer.md:12  | > 3. **Phase 0+ implementation** - Add H€/HAu if merchants demonstrate need
- Phase 0  | docs/implementation/android-app/stability-layer.md:17  | > - **Resource allocation** - Focus Phase 0 on core flows (remittances working)
- Phase 0+  | docs/implementation/android-app/stability-layer.md:33  | **Phase 0+:** Optional enhancement after launch (add if merchant behavior demonstrates need)
- Phase 0  | docs/implementation/android-app/stability-layer.md:39  | ## Phase 0 H€ Minting Policy (Compliance)
- Phase 0  | docs/implementation/android-app/stability-layer.md:45  | ### Minting Triggers (Phase 0)
- Phase 0  | docs/implementation/android-app/stability-layer.md:201  | **Phase 0: Asgaya Bull Pool (Simplified)**
- Phase 0  | docs/implementation/android-app/stability-layer.md:205  | **Capacity:** ~€1,800 (60% of Phase 0 budget)
- Phase 0+  | docs/implementation/android-app/stability-layer.md:223  | **Queue for fairness (Phase 0+):**
- Phase 0  | docs/implementation/android-app/stability-layer.md:243  | **Phase 0 allocation (reference implementation):**
- Phase 1+  | docs/implementation/android-app/stability-layer.md:250  | **Phase 1+ alternative: Open Marketplace**
- Phase 1+  | docs/implementation/android-app/stability-layer.md:252  | For comparison, Phase 1+ could support open speculator marketplace:
- Phase 0  | docs/implementation/android-app/stability-layer.md:277  | **Phase 0 choice:** Bull pool (simplicity over scale)
- Phase 0  | docs/implementation/android-app/stability-layer.md:363  | **Burn economics (Phase 0):**
- Phase 0  | docs/implementation/android-app/stability-layer.md:366  | - Phase 0 uses 7-day contracts that auto-renew unless user exits
- Phase 0+  | docs/implementation/android-app/stability-layer.md:527  | ### Strategic Insight: Gold Shops as Early Adopters (Phase 0+)
- Phase 0+  | docs/implementation/android-app/stability-layer.md:558  | **Phase 0+ opportunity:** Target 2-3 gold shops as pilot users, measure adoption
- Phase 0  | docs/implementation/android-app/stability-layer.md:594  | **Why General Protocols for Phase 0:**
- Phase 1+  | docs/implementation/android-app/stability-layer.md:602  | ### Alternative Oracle Approaches (Phase 1+)
- Phase 0  | docs/implementation/android-app/stability-layer.md:681  | - Bootstrap problem (Phase 0 low volume)
- Phase 0  | docs/implementation/android-app/stability-layer.md:770  | ## Phase 0 Scope
- Phase 1+  | docs/implementation/android-app/stability-layer.md:778  | **Deferred to Phase 1+:**
- Phase 0  | docs/implementation/android-app/stability-layer.md:783  | **Alternative client features (not Phase 0):**
- Phase 0  | docs/implementation/android-app/stability-layer.md:790  | **Why defer most features?** Phase 0 focuses on remittance flow (BCH → BCH). Stability layer is enhancement, not critical path.
- Phase 1+  | docs/implementation/android-app/stability-layer.md:806  | - [notification-bot.md](notification-bot.md) - Could auto-hedge received BCH (Phase 1+)
- Phase 0+  | docs/implementation/android-app/stability-layer.md:810  | **Status:** Phase 0+ - Design complete, implementation deferred (add based on observed merchant behavior)

## `docs/implementation/android-app/state-management.md`  (14)

- Phase 0  | docs/implementation/android-app/state-management.md:8  | - **Phase 0 ✅ Implemented:** Multi-wallet storage (WalletEntity, Room database, WalletManager) + **`pending_transactions` table + SendViewModel** (RS083, Aug 17-18)
- Phase 1+  | docs/implementation/android-app/state-management.md:9  | - **Phase 1+ 🔨 Future:** Comprehensive schema (covenants tracking, bulletin board cache, sync strategies)
- MVP  | docs/implementation/android-app/state-management.md:11  | **What this document describes:** Full state management architecture for MVP (all five gears integrated)
- Phase 0  | docs/implementation/android-app/state-management.md:13  | **What Phase 0 implemented:** Wallet storage + pending transaction persistence (see [Pending Transactions (Phase 0)](#-pending-transactions-phase-0-rs083) below)
- Phase 0  | docs/implementation/android-app/state-management.md:17  | > **⚠️ Originally Planned for Phase 0 - Implemented Partially**
- Phase 0  | docs/implementation/android-app/state-management.md:21  | > **Phase 0 Reality:**
- Phase 0  | docs/implementation/android-app/state-management.md:26  | > 5. ❌ **Bulletin board cache** - No bulletin board in Phase 0
- Phase 1+  | docs/implementation/android-app/state-management.md:32  | > **Phase 1+ Sync Strategy:** Event-driven reconciliation (Electrum notifications → local DB updates)
- Phase 0  | docs/implementation/android-app/state-management.md:34  | > **This document:** Full MVP design. Phase 0 implemented wallet storage + pending transaction persistence (RS083).
- Phase 0  | docs/implementation/android-app/state-management.md:166  | ## Pending Transactions (Phase 0, RS083)
- Phase 1+  | docs/implementation/android-app/state-management.md:672  | - **Encryption:** SQLCipher for encrypted database (optional, Phase 1+)
- Phase 1+  | docs/implementation/android-app/state-management.md:719  | **Status:** Phase 0 ✅ Partially Implemented (wallet storage + pending_transactions working), Phase 1+ 🔨 Full design (covenant tracking, cache, sync)
- MVP  | docs/implementation/android-app/state-management.md:721  | **Originally written:** 2026-08-04 (pre-production, described full MVP architecture)
- Phase 0  | docs/implementation/android-app/state-management.md:726  | **Phase 0 Implementation (AsgayaHusk):**

## `docs/implementation/android-app/two-layer-architecture.md`  (2)

- Phase 0  | docs/implementation/android-app/two-layer-architecture.md:9  | - ⚠️ **Client Layer:** Code examples are illustrative/future (Nostr monitoring not in Phase 0)
- Phase 0  | docs/implementation/android-app/two-layer-architecture.md:12  | **Note:** This document explains the architectural principle. Client layer code examples show future patterns (Nostr, auto-refund monitoring). Phase 0 implements manual refund only.

## `docs/implementation/android-app/wallet.md`  (28)

- Phase 1+  | docs/implementation/android-app/wallet.md:3  | **Purpose:** Multi-wallet management, BCH key management, balance queries, transaction building, Cash Account integration (Phase 1+)
- Phase 1+  | docs/implementation/android-app/wallet.md:16  | ⏳ Receive screen (placeholder UI for now - QR code generation deferred to Phase 1+)
- Phase 0  | docs/implementation/android-app/wallet.md:18  | ⏳ HD wallet / BIP39 seed phrases (Phase 0 - needed for production)
- Phase 1+  | docs/implementation/android-app/wallet.md:19  | 📋 RFID tag private key storage (Phase 1+)
- Phase 1+  | docs/implementation/android-app/wallet.md:20  | 📋 Cash Account registration (Phase 1+)
- Phase 1+  | docs/implementation/android-app/wallet.md:29  | - **Key management:** ✅ WIF import (done) | ⏳ HD wallets with seed phrase backup (Phase 0) | 📋 RFID tags for private key storage (Phase 1+)
- Phase 1+  | docs/implementation/android-app/wallet.md:33  | - **Cash Accounts:** 📋 Register `name#number` on-chain, resolve to BCH address (Phase 1+)
- Phase 1+  | docs/implementation/android-app/wallet.md:80  | data class HDDerived(val accountIndex: Int)  // From BIP39 seed (Phase 1+)
- Phase 0  | docs/implementation/android-app/wallet.md:81  | data class ImportedKey(val wif: String)      // From covenant-params (Phase 0)
- Phase 1+  | docs/implementation/android-app/wallet.md:103  | - **Single seed backup** for HD wallets (Phase 1+)
- Phase 0  | docs/implementation/android-app/wallet.md:104  | - **Imported keys** for test wallets (Phase 0)
- Phase 0+  | docs/implementation/android-app/wallet.md:384  | **Note:** Tab 2 (Remittances) and Tab 3 (Marketplace) are not yet documented in separate files. They are planned for Phase 0+ but UI/UX design is still in progress.
- Phase 0  | docs/implementation/android-app/wallet.md:392  | > **Status:** ⏳ Phase 0 (needed for production) - Currently using imported WIF keys for testing
- Phase 0  | docs/implementation/android-app/wallet.md:578  | - Expiry: 8 hours (Phase 0) - after that, sender can reclaim funds if unclaimed
- Phase 0  | docs/implementation/android-app/wallet.md:647  | - Phase 0: WebView approach (proven, working)
- Phase 1+  | docs/implementation/android-app/wallet.md:648  | - Phase 1+: Manual construction (when validated, smaller APK)
- MVP  | docs/implementation/android-app/wallet.md:705  | > **Status:** ⏳ In progress (August 3, 2026 - MVP ~80% complete)
- Phase 1+  | docs/implementation/android-app/wallet.md:730  | 3. **Future:** QR code scanner (Phase 1+)
- Phase 1+  | docs/implementation/android-app/wallet.md:923  | > **Status:** ⏳ Placeholder UI (Phase 0) - Full QR code generation deferred to Phase 1+
- Phase 0  | docs/implementation/android-app/wallet.md:926  | **Phase 0 MVP (placeholder):**
- Phase 1+  | docs/implementation/android-app/wallet.md:931  | **Phase 1+ features (deferred):**
- Phase 0  | docs/implementation/android-app/wallet.md:941  | - Focus Phase 0 development on Send + Covenant integration
- Phase 0  | docs/implementation/android-app/wallet.md:947  | > **Status:** ⏳ Planned (Phase 0)
- Phase 1+  | docs/implementation/android-app/wallet.md:959  | **Nice to have (Phase 1+):**
- Phase 1+  | docs/implementation/android-app/wallet.md:1264  | - [bulletin-board.md](bulletin-board.md) - Create listing NFTs (Phase 1+)
- Phase 1+  | docs/implementation/android-app/wallet.md:1265  | - [nostr.md](nostr.md) - Sign messages with wallet keys (Phase 1+)
- Phase 1+  | docs/implementation/android-app/wallet.md:1266  | - [notification-bot.md](notification-bot.md) - Auto-fund covenants (Phase 1+)
- Phase 0  | docs/implementation/android-app/wallet.md:1270  | **Status:** Phase 0 - Active execution (multi-wallet ✅, covenant integration ✅, send flow ⏳)

## `docs/implementation/android-app/webview-covenant-bridge.md`  (5)

- Phase 0  | docs/implementation/android-app/webview-covenant-bridge.md:56  | **Trade-offs accepted for Phase 0:**
- Phase 0  | docs/implementation/android-app/webview-covenant-bridge.md:352  | **For Phase 0 (acceptable trade-offs):**
- Phase 1+  | docs/implementation/android-app/webview-covenant-bridge.md:358  | **For Phase 1+ (if manual construction succeeds):**
- Phase 0  | docs/implementation/android-app/webview-covenant-bridge.md:384  | **Acceptable for Phase 0**, but manual construction would reduce to <5MB RAM.
- Phase 1+  | docs/implementation/android-app/webview-covenant-bridge.md:484  | **Potential future paths (Phase 1+, not blocking):**

## `docs/implementation/covenants/README.md`  (2)

- Phase 1  | docs/implementation/covenants/README.md:11  | - `version-history.md` - Complete evolution from Phase 1 → v2.6
- Phase 0  | docs/implementation/covenants/README.md:105  | **Status:** Phase 0 production-ready

## `docs/implementation/covenants/manual-construction.md`  (2)

- Phase 0  | docs/implementation/covenants/manual-construction.md:172  | *The arguments below describe why manual construction remains the long-term goal. For Phase 0, we chose WebView + CashScript for speed of validation—see [Reality Check](#️-reality-check-what-actually-happened-august-2026).*
- Phase 0  | docs/implementation/covenants/manual-construction.md:287  | But **pragmatism won**: We need proven covenant infrastructure for Phase 0. Manual construction can be revisited later if:

## `docs/implementation/covenants/version-history.md`  (24)

- Phase 1  | docs/implementation/covenants/version-history.md:1  | # Covenant Version History: Phase 1 → v2.6
- Phase 1  | docs/implementation/covenants/version-history.md:3  | **Purpose:** Track covenant evolution from initial Phase 1 implementation through production v2.6.
- Phase 1  | docs/implementation/covenants/version-history.md:13  | | **Phase 1** | 2026-07-23 | ✅ Tested | MTP-only refund (baseline) |
- Phase 1  | docs/implementation/covenants/version-history.md:24  | ## Phase 1: The Baseline (MTP-Only Refund)
- Phase 1  | docs/implementation/covenants/version-history.md:71  | **Phase 1 complete!** 🎉 First successful price oracle covenant on chipnet.
- Phase 0  | docs/implementation/covenants/version-history.md:561  | **v2.5 is the production covenant.** Future versions may add features (multi-oracle, reputation systems), but v2.5 is complete for Phase 0.
- Phase 0  | docs/implementation/covenants/version-history.md:567  | - **Phase 0:** Bootstrap oracle (Asgaya/Pi-chan queries Kraken, signs price+timestamp)
- Phase 1+  | docs/implementation/covenants/version-history.md:568  | - **Phase 1+:** Oracle-over-Nostr (multiple sources, multi-source consensus, reputation-filtered VWAP)
- Phase 0  | docs/implementation/covenants/version-history.md:724  | - **1 output** → abort path taken (H€ minting trigger for Phase 0 compliance)
- Phase 0  | docs/implementation/covenants/version-history.md:768  | **v2.6 is the new production covenant.** It supersedes v2.5 by fixing the fund locking scenario while preserving all v2.5 functionality. The abort path enables Phase 0 H€ minting compliance (utility token, not money substitute).
- Phase 1+  | docs/implementation/covenants/version-history.md:778  | **Update to funder-principle doc:** The "Future Considerations → Phase 1+: Parameter Naming" section deferred this rename. It is now done at zero cost in v2.6.1. The naming confusion that caused the Aug 2 and Aug 10 bugs is permanently resolved.
- Phase 0  | docs/implementation/covenants/version-history.md:784  | **Important distinction (self-funded flow):** When the sender IS the funder (self-funded, as in Phase 0 testing), abort() sends **everything to the sender** — which is the funder. So in the self-funded flow the funder gets all the BCH (minus network fees incurred during funding and abort), not nothing. "The funder gets nothing" specifically means: the funder gets nothing **beyond what they funded*…
- Phase 0  | docs/implementation/covenants/version-history.md:797  | - Phase 0: app logic enforces fairness + the overlap zone keeps funds accessible in all scenarios.
- Phase 0  | docs/implementation/covenants/version-history.md:799  | **Future work:** The overlap between abort() and the other paths can be tightened with math refinement (dynamic buffer makes this easier — see [variable-buffer-rate](../../../unknowns/variable-buffer-rate.md)). Phase 0: current overlap is good enough.
- Phase 0  | docs/implementation/covenants/version-history.md:801  | ### H€ Minting Policy (Phase 0 Compliance)
- Phase 1  | docs/implementation/covenants/version-history.md:841  | | **Phase 1** | Basic refund path | MTP too slow (hours) | Chipnet needs fast paths |
- Phase 1  | docs/implementation/covenants/version-history.md:856  | ### 1. Byte Endianness (Phase 1)
- Phase 1  | docs/implementation/covenants/version-history.md:951  | - **Phase 1 (July 2026):** Developed on regtest for fast iteration
- Phase 0  | docs/implementation/covenants/version-history.md:952  | - **Phase 0 (August 2026):** Testing exclusively on testnet3 (reliable, mirrors mainnet)
- Phase 1  | docs/implementation/covenants/version-history.md:992  | - `ARCHIVE_price-oracle_20260723.cash` - Phase 1 covenant
- Phase 1  | docs/implementation/covenants/version-history.md:993  | - `ARCHIVE_claim-chipnet_20260723.mjs` - Phase 1 claim script (with LE fix!)
- Phase 0  | docs/implementation/covenants/version-history.md:1383  | - **Phase 0:** Bootstrap oracle (Asgaya queries centralized price source)
- Phase 1  | docs/implementation/covenants/version-history.md:1384  | - **Phase 1:** Oracle-over-Nostr (multiple sources, reputation-filtered consensus)
- Phase 1  | docs/implementation/covenants/version-history.md:1406  | - [Testing Plan](../../../knowledge/meta/phase-1.5-testing-plan.md) - Phase 1.5 testing strategy

## `docs/index.md`  (5)

- Phase 0  | docs/index.md:60  | How to bootstrap Phase 0:
- Phase 0  | docs/index.md:61  | - [cold-start-strategy/](cold-start-strategy/README.md) - Phase 0 rollout plan
- Phase 0  | docs/index.md:139  | **Phase:** Phase 0 — Active Implementation & Testnet3 Validation
- Phase 0  | docs/index.md:155  | **Status:** Android reference implementation under active development, validated on testnet3 with real devices. Preparing for Phase 0 pilot.
- Phase 0  | docs/index.md:165  | **Implementation:** Code contributions will be accepted after Phase 0 implementation begins.

## `docs/roadmap.md`  (12)

- Phase 0  | docs/roadmap.md:12  | | **Phase 0** | MVP — full value proposition end-to-end, launched as a limited **mainnet beta** with real money and a small, trusted user set (gated on MVP confidence, validated on testnet3) | Now / first |
- Phase 0+  | docs/roadmap.md:13  | | **Phase 0+** | Opportunistic wins added *during* Phase 0 testing (cheap UX/reliability) | During Phase 0 |
- Phase 0  | docs/roadmap.md:14  | | **Phase 1** | Post-MVP enhancements driven by observed Phase 0 behavior | After MVP confidence |
- Phase 1+  | docs/roadmap.md:15  | | **Phase 1+** | Later, larger enhancements / geographic expansion | Future |
- Phase 0  | docs/roadmap.md:17  | **Current status:** Phase 0 MVP validation on **testnet3** (Sep 1, 2026: first merchant-first transaction on-chain). Phase 0 mainnet beta not yet started — gated on MVP confidence.
- MVP  | docs/roadmap.md:21  | ## PHASE 0 — MVP (mainnet beta, real money, limited trusted users)
- Phase 1  | docs/roadmap.md:41  | |  | Merchant auto-claim client (Phase 1 preferred) | 📅 Phase 1 | (workspace decision) | Notification-listener watches funding → claim; merchant/seller-focused client |
- Phase 0  | docs/roadmap.md:55  | |  | Hardcoded test seller / merchant (no bulletin board yet) | ✅ | seller-auto-funding | Phase 0 test pattern |
- Phase 0  | docs/roadmap.md:69  | ## PHASE 0+ — During Phase 0 (opportunistic)
- MVP  | docs/roadmap.md:84  | ## PHASE 1 — Post-MVP (observed-behavior driven)
- Phase 0  | docs/roadmap.md:119  | | Leg | Phase 0 | Phase 1 |
- Phase 0+  | docs/roadmap.md:130  | - **Method:** deterministic grep for `Phase 0`, `Phase 0+`, `Phase 1`, `Phase 1+`, `MVP`, `roadmap` across `docs/` (excluding `research/`), cross-checked with gemma4:e2b verbatim extraction on high-value docs.

## `docs/testing/phase-1.5-testing-plan.md`  (5)

- Phase 1  | docs/testing/phase-1.5-testing-plan.md:1  | # Phase 1.5 Testing Plan: Blockchain-as-Oracle Price Discovery
- Phase 1  | docs/testing/phase-1.5-testing-plan.md:11  | Phase 1.5 implements blockchain-as-oracle price discovery where every covenant funding becomes a trade signal. This testing plan covers:
- Phase 0  | docs/testing/phase-1.5-testing-plan.md:184  | ### ⏳ T3.1: Phase 0 - Asgaya Oracle Dominant (100%)
- Phase 1  | docs/testing/phase-1.5-testing-plan.md:206  | ### ⏳ T3.2: Phase 1 - Hybrid Weighting (40% Asgaya, 60% User)
- Phase 1  | docs/testing/phase-1.5-testing-plan.md:863  | **Phase 1.5 ready for production when:**

## `docs/the-mechanism/README.md`  (2)

- Phase 0  | docs/the-mechanism/README.md:71  | typical daily swings. Unused buffer returns to the seller. Post-Phase 0, this evolves
- Phase 0  | docs/the-mechanism/README.md:108  | **Phase 0 capital:** €3K unified pool (approved assets: H€, HAu). High merchant velocity (weekly VES conversion) means low capital lock. Supports ~80 merchants initially.

## `docs/the-mechanism/bulletin-board/README.md`  (2)

- Phase 0  | docs/the-mechanism/bulletin-board/README.md:76  | **Phase 0 baseline:** Every listing UTXO must contain ≥ 0.001 BCH (~€0.50). To spam with 1,000 fake listings costs €500. You reclaim the deposit when you remove the listing (minus ~€0.002 transaction fee).
- Phase 0  | docs/the-mechanism/bulletin-board/README.md:78  | **This is an unknown.** We don't know if this simple economic barrier is sufficient, or if we need additional measures like listing limits per account, device fingerprinting, or payment method verification. Phase 0 will test whether the 0.001 BCH deposit alone deters spam effectively.

## `docs/the-mechanism/bulletin-board/seller-ranking-algorithm.md`  (10)

- Phase 0  | docs/the-mechanism/bulletin-board/seller-ranking-algorithm.md:5  | **Status:** Phase 0 Testing (Testnet validation required)
- Phase 0  | docs/the-mechanism/bulletin-board/seller-ranking-algorithm.md:24  | ## The Formula (Phase 0 Baseline)
- Phase 0  | docs/the-mechanism/bulletin-board/seller-ranking-algorithm.md:181  | ### Optional Safeguard (Phase 0 Testing)
- Phase 0  | docs/the-mechanism/bulletin-board/seller-ranking-algorithm.md:199  | **Decision point:** Test in Phase 0. Add rate limiting only if gaming attempts succeed.
- Phase 0  | docs/the-mechanism/bulletin-board/seller-ranking-algorithm.md:404  | ## Phase 0 Testing Plan
- Phase 0  | docs/the-mechanism/bulletin-board/seller-ranking-algorithm.md:445  | ## Open Questions (To Answer in Phase 0)
- Phase 0  | docs/the-mechanism/bulletin-board/seller-ranking-algorithm.md:519  | ## Success Metrics (Phase 0 Validation)
- Phase 0  | docs/the-mechanism/bulletin-board/seller-ranking-algorithm.md:530  | **Timeline:** 3 months testnet + 3 months mainnet Phase 0.
- Phase 0  | docs/the-mechanism/bulletin-board/seller-ranking-algorithm.md:591  | **During Phase 0 testing, we may A/B test alternatives:**
- Phase 0  | docs/the-mechanism/bulletin-board/seller-ranking-algorithm.md:636  | **Status:** Phase 0 Testnet Validation

## `docs/the-mechanism/nostr-coordination/README.md`  (16)

- Phase 1+  | docs/the-mechanism/nostr-coordination/README.md:223  | **Trade-off:** Nostr has *good* censorship resistance (multiple relays, anyone can run one) vs OP_RETURN's *maximum* censorship resistance (BCH blockchain). For Phase 0, good is enough. We can add OP_RETURN fallback in Phase 1+ if needed.
- Phase 1+  | docs/the-mechanism/nostr-coordination/README.md:269  | **Phase 0 acceptable.** Phase 1+ we can add onion routing or mix networks if metadata analysis becomes a concern.
- Phase 0  | docs/the-mechanism/nostr-coordination/README.md:398  | **Payment coordination (Phase 0):**
- Phase 1  | docs/the-mechanism/nostr-coordination/README.md:402  | **Market price feeds (Phase 1.5):**
- Phase 0  | docs/the-mechanism/nostr-coordination/README.md:411  | ## Nostr Features in Phase 0
- Phase 1+  | docs/the-mechanism/nostr-coordination/README.md:420  | **What's coming (Phase 1+):**
- Phase 1  | docs/the-mechanism/nostr-coordination/README.md:421  | - Oracle price feeds (Phase 1.5) — Already designed, implementation planned
- Phase 0  | docs/the-mechanism/nostr-coordination/README.md:428  | **Phase 0 keeps it simple:** Automated payment info exchange only. No manual messaging. Bot-to-bot coordination.
- Phase 1+  | docs/the-mechanism/nostr-coordination/README.md:450  | **Still works.** Just requires sellers to run infrastructure (Phase 1+ feature).
- Phase 1+  | docs/the-mechanism/nostr-coordination/README.md:456  | **Fallback (Phase 1+):**
- Phase 0  | docs/the-mechanism/nostr-coordination/README.md:461  | **Phase 0:** If Nostr completely fails, Asgaya pauses until relays recover. Acceptable risk—Nostr has been running reliably since 2022.
- Phase 0  | docs/the-mechanism/nostr-coordination/README.md:478  | **Phase 0 mitigation:**
- Phase 1+  | docs/the-mechanism/nostr-coordination/README.md:483  | **Phase 1+ mitigation:**
- Phase 1+  | docs/the-mechanism/nostr-coordination/README.md:527  | **Phase 0 uses Nostr.** Phase 1+ adds OP_RETURN fallback if needed.
- Phase 1+  | docs/the-mechanism/nostr-coordination/README.md:539  | - Seller should rotate Nostr keys periodically (Phase 1+ feature)
- Phase 1+  | docs/the-mechanism/nostr-coordination/README.md:543  | **A:** Phase 1+, yes. Phase 0, no—Nostr is automated for bot coordination only. But if you have a Nostr client app (Damus, Amethyst), you can message other users using your Asgaya Nostr keys.

## `docs/the-mechanism/nostr-coordination/device-health.md`  (20)

- Phase 0  | docs/the-mechanism/nostr-coordination/device-health.md:255  | ### Supported Banks (Phase 0)
- Phase 0  | docs/the-mechanism/nostr-coordination/device-health.md:259  | | **Caja Rural** | `com.rsi.nba` | Primary Phase 0 target |
- Phase 0  | docs/the-mechanism/nostr-coordination/device-health.md:260  | | **BBVA** | `com.bbva.bbvacontigo` | Phase 0 testing |
- Phase 1+  | docs/the-mechanism/nostr-coordination/device-health.md:261  | | **Santander** | `es.bancosantander.apps` | Phase 1+ |
- Phase 1+  | docs/the-mechanism/nostr-coordination/device-health.md:262  | | **Sabadell** | `es.bancsabadell.mobilebancohd` | Phase 1+ |
- Phase 1+  | docs/the-mechanism/nostr-coordination/device-health.md:263  | | **ING** | `es.ingdirect.ing` | Phase 1+ |
- Phase 1+  | docs/the-mechanism/nostr-coordination/device-health.md:317  | **Phase 0 focuses on Android** (where bank apps send actionable notifications). iOS support deferred to Phase 1+.
- Phase 1+  | docs/the-mechanism/nostr-coordination/device-health.md:329  | **Phase 1+ iOS strategy:**
- Phase 0  | docs/the-mechanism/nostr-coordination/device-health.md:350  | **Phase 0 acceptable.** Seller voluntarily discloses basic device health to facilitate trade. No identifying metadata leaks.
- Phase 0  | docs/the-mechanism/nostr-coordination/device-health.md:393  | **Phase 0 acceptable.** Missing health is suspicious, not fatal.
- Phase 1+  | docs/the-mechanism/nostr-coordination/device-health.md:415  | **Phase 1+ improvement:**
- Phase 0  | docs/the-mechanism/nostr-coordination/device-health.md:420  | **Phase 0 trade-off:** Accept rare edge case (battery dies mid-payment) in exchange for simpler implementation. Covenant timelock protects buyer.
- Phase 0  | docs/the-mechanism/nostr-coordination/device-health.md:439  | **Phase 0 philosophy:** Informed consent. We warn, user decides.
- Phase 1+  | docs/the-mechanism/nostr-coordination/device-health.md:443  | ## Future Enhancements (Phase 1+)
- Phase 0  | docs/the-mechanism/nostr-coordination/device-health.md:447  | **Phase 0:** Health checked once (at payment info request).
- Phase 1+  | docs/the-mechanism/nostr-coordination/device-health.md:449  | **Phase 1+:** Health checked continuously during payment window:
- Phase 0  | docs/the-mechanism/nostr-coordination/device-health.md:463  | **Phase 0:** Binary health (healthy/unhealthy).
- Phase 1+  | docs/the-mechanism/nostr-coordination/device-health.md:465  | **Phase 1+:** Predictive alerts based on historical patterns:
- Phase 0  | docs/the-mechanism/nostr-coordination/device-health.md:475  | **Phase 0:** One seller = one device.
- Phase 1+  | docs/the-mechanism/nostr-coordination/device-health.md:477  | **Phase 1+:** Sellers run bots on multiple devices (phone + VPS):

## `docs/the-mechanism/nostr-coordination/dispute-resolution.md`  (24)

- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:44  | - **Phase 0:** Cash-in-person (all corridors) + Bizum (Spain, documented)
- Phase 0+  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:45  | - **Phase 0+:** SEPA (first pioneer test), then additional methods based on user demand
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:92  | **Solution: Recipient name matching (Bizum Phase 0)**
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:173  | ### Phase 0 Mitigation
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:177  | - ✅ **Trusted sellers only** - Phase 0 uses pre-vetted sellers (Suso, early adopters)
- Phase 1+  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:182  | **Phase 1+ relies on:** Mature reputation system, large transaction history, market filtering of bad actors.
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:309  | **Phase 0 validators:** Trusted community members (Suso, early adopters)
- Phase 1+  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:311  | **Phase 1+ validators:** Any user with high reputation can validate
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:325  | **Beyond Phase 0: Counter-claims replace validators**
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:468  | 2. Phase 0: Validators confirm payment sent, BCH not locked
- Phase 1+  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:469  | 3. Phase 1+: Seller can counter-claim or refund
- Phase 1+  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:499  | **Mitigation for Phase 1+:**
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:505  | **Phase 0 approach:** Blacklist first, resolve later. Bias toward protecting active users (they took the capital risk by sending payment first).
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:523  | **Outcome (Phase 0 with validators):**
- Phase 1+  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:528  | **Outcome (Phase 1+ with counter-claims):**
- Phase 1+  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:580  | **Phase 1+ improvement:** Zero-knowledge proofs could prove "bank SMS exists" without revealing contents, even to validators. Phase 0 accepts encryption as sufficient.
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:620  | **Validator Bot (Phase 0 only):**
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:630  | ## Phase 0 Validation: What We're Testing
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:639  | | **Validator Efficiency** | Time to reach 3+ validator confirmations (Phase 0 only)? | <48 hours |
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:646  | - <48 hours to validator consensus (Phase 0 only)
- Phase 1+  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:657  | | **Validator trust** | Must trust validators to honestly assess evidence | Phase 0: trusted community; Phase 1+: reputation-weighted voting | Zero-knowledge proofs (Phase 2+) could eliminate validator trust |
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:668  | 3. **Progressive rollout** — Phase 0 starts with cash-in-person + Bizum (documented), adds methods based on user demand
- Phase 1+  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:674  | 9. **Phase 0 validators, Phase 1+ counter-claims** — Trusted validators for bootstrap, then disputes resolve between users
- Phase 0  | docs/the-mechanism/nostr-coordination/dispute-resolution.md:697  | **Status:** Phase 0 Implementation

## `docs/the-mechanism/nostr-coordination/distributed-monitoring.md`  (5)

- Phase 0  | docs/the-mechanism/nostr-coordination/distributed-monitoring.md:166  | ### Phase 0: Day 1 Launch
- Phase 1  | docs/the-mechanism/nostr-coordination/distributed-monitoring.md:195  | ### Phase 1: Growing Network (10-100 Sellers)
- Phase 0  | docs/the-mechanism/nostr-coordination/distributed-monitoring.md:247  | - Phase 0: Asgaya provides liquidity + price feed
- Phase 1  | docs/the-mechanism/nostr-coordination/distributed-monitoring.md:248  | - Phase 1-2: Network gradually takes over
- Phase 1  | docs/the-mechanism/nostr-coordination/distributed-monitoring.md:812  | **Status:** Phase 1.5 - Designed, implementation planned

## `docs/the-mechanism/nostr-coordination/oracle-husk.md`  (7)

- Phase 0  | docs/the-mechanism/nostr-coordination/oracle-husk.md:5  | **Status:** Phase 0 — running on the Raspberry Pi (Pi-chan), integrated with the app (Aug 16, 2026)
- Phase 0  | docs/the-mechanism/nostr-coordination/oracle-husk.md:23  | | **Phase 0 (now)** | Development infrastructure | Asgaya (Pi) | Test tool: full control over price + time |
- Phase 0  | docs/the-mechanism/nostr-coordination/oracle-husk.md:29  | ## Phase 0: Development Infrastructure
- Phase 0  | docs/the-mechanism/nostr-coordination/oracle-husk.md:42  | | `GET /oracle/info` | Serve the oracle public key (app fetches at covenant creation) | Phase 0 + production |
- Phase 0  | docs/the-mechanism/nostr-coordination/oracle-husk.md:43  | | `GET /oracle/price` | Serve a signed price (timestamp + price in cents) | Phase 0 + production |
- Phase 0  | docs/the-mechanism/nostr-coordination/oracle-husk.md:44  | | `POST /oracle/set-price` | Override price for testing (simulate price drops) | **Phase 0 only** |
- Phase 0  | docs/the-mechanism/nostr-coordination/oracle-husk.md:101  | **Status:** Phase 0 (running on Pi-chan, integrated with app)

## `docs/the-mechanism/notification-bot/README.md`  (2)

- Phase 0  | docs/the-mechanism/notification-bot/README.md:118  | ## Phase 0 Scope
- Phase 1+  | docs/the-mechanism/notification-bot/README.md:122  | **Phase 1+:** VPS deployment option, multi‑currency support, dispute mediation interface, advanced accounting exports, concurrent multi‑corridor support.

## `docs/the-mechanism/stability-layer/README.md`  (22)

- Phase 0  | docs/the-mechanism/stability-layer/README.md:65  | **Phase 0:** Founder provides liquidity (Option 2 available)
- Phase 1+  | docs/the-mechanism/stability-layer/README.md:66  | **Phase 1+:** Crowdfunded bull pool, competitive LP market emerges
- Phase 0  | docs/the-mechanism/stability-layer/README.md:128  | ## Phase 0: EUR & Gold
- Phase 0  | docs/the-mechanism/stability-layer/README.md:175  | **Phase 0 tests both:** We build H€/HAu and observe merchant behavior. If merchants don't want stability tokens (because BCH is stable enough), that's **excellent news** - it means the primary hypothesis worked.
- Phase 0  | docs/the-mechanism/stability-layer/README.md:187  | ### Tier 1: Existing Financial Oracles (Phase 0 - Months to Ship)
- Phase 0  | docs/the-mechanism/stability-layer/README.md:249  | **Phase 0:** Test H€ and HAu (prove mechanism works with existing oracles)
- Phase 1  | docs/the-mechanism/stability-layer/README.md:250  | **Phase 1:** Add more fiat-pegged options if demand exists (still existing oracles)
- Phase 0  | docs/the-mechanism/stability-layer/README.md:286  | - **Phase 0:** H€/HAu → Fiat/gold stability (existing oracles, shipping now)
- Phase 0  | docs/the-mechanism/stability-layer/README.md:304  | ## Oracle Requirements for Phase 0
- Phase 0  | docs/the-mechanism/stability-layer/README.md:319  | **Future assets (not Phase 0):** Commodities (copper, oil) require oracle adapters. Purchasing power baskets (H-basket, H-CPI) would require building custom decentralized data collection - a research project separate from Asgaya.
- Phase 0  | docs/the-mechanism/stability-layer/README.md:345  | **Phase 0:** High velocity (money tight, dump fast). €3K pool sufficient.
- Phase 1  | docs/the-mechanism/stability-layer/README.md:346  | **Phase 1:** Crowdfund bull pool (€50K+) when demand proven.
- Phase 0  | docs/the-mechanism/stability-layer/README.md:352  | **Phase 0 (€3K founder capital):**
- Phase 0  | docs/the-mechanism/stability-layer/README.md:372  | ### Phase 0: Founder Pool (€3K)
- Phase 1+  | docs/the-mechanism/stability-layer/README.md:385  | ### Phase 1+: Crowdfunded Pool (€50K+)
- Phase 0  | docs/the-mechanism/stability-layer/README.md:387  | **When Phase 0 proves demand:**
- Phase 0  | docs/the-mechanism/stability-layer/README.md:417  | - Phase 0 encourages onboarders to participate as liquidity providers
- Phase 0  | docs/the-mechanism/stability-layer/README.md:427  | **Phase 0 (Remittances):**
- Phase 1  | docs/the-mechanism/stability-layer/README.md:431  | **Phase 1 (Multi-Corridor):**
- Phase 0  | docs/the-mechanism/stability-layer/README.md:465  | **For Phase 0:** H€ (familiar) + HAu (reliable oracle). Test both. Let data decide.
- Phase 0  | docs/the-mechanism/stability-layer/README.md:485  | **Phase 0 (what we're building):**
- Phase 0  | docs/the-mechanism/stability-layer/README.md:512  | 2. **Oracle availability determines roadmap.** Phase 0: EUR + Gold (existing feeds). Future: Commodities if demand proves. Baskets = separate research project.

## `docs/the-mechanism/stability-layer/price-discovery.md`  (3)

- Phase 0  | docs/the-mechanism/stability-layer/price-discovery.md:4  | **Priority:** Medium (needed before Phase 0 launch)
- Phase 0  | docs/the-mechanism/stability-layer/price-discovery.md:82  | ## Phase 0 Minimal Viable Strategy
- Phase 0  | docs/the-mechanism/stability-layer/price-discovery.md:123  | **This is a placeholder.** Research needed before Phase 0 launch to validate oracle strategy and fallback mechanisms.

## `docs/unknowns/README.md`  (12)

- Phase 0  | docs/unknowns/README.md:7  | **Why it exists:** Phase 0 validation requires answering critical unknowns before launch. This directory explains *how* to investigate each unknown and invites anyone to contribute.
- Phase 0  | docs/unknowns/README.md:105  | - All answerable within Phase 0 timeline (3-6 months)
- Phase 0  | docs/unknowns/README.md:124  | **Status:** [Not Started | In Progress | Phase 0 Trial | Answered]
- Phase 0  | docs/unknowns/README.md:144  | ## Phase 0 Trial Integration
- Phase 0  | docs/unknowns/README.md:160  | ## Priority Ranking (Phase 0)
- Phase 0  | docs/unknowns/README.md:162  | ### Critical (Must Answer Before Phase 0 Launch)
- Phase 0  | docs/unknowns/README.md:166  | ### High (Should Answer During Phase 0)
- Phase 0  | docs/unknowns/README.md:171  | ### Medium (Nice to Answer During Phase 0)
- Phase 1  | docs/unknowns/README.md:177  | ### Low (Can Answer in Phase 1)
- Phase 0  | docs/unknowns/README.md:185  | **Priority drives effort allocation.** Phase 0 focuses on Critical and High unknowns first.
- Phase 0  | docs/unknowns/README.md:210  | **Phase 0 (Trial Period):** Active investigation
- Phase 1  | docs/unknowns/README.md:215  | **Phase 1 (Public Launch):** Informed decisions

## `docs/unknowns/adoption-stabilization-effect.md`  (12)

- Phase 0  | docs/unknowns/adoption-stabilization-effect.md:24  | Phase 0: 30% buffer (high volatility, low volume)
- Phase 1  | docs/unknowns/adoption-stabilization-effect.md:26  | Phase 1: 25% buffer (volatility declining, volume growing)
- Phase 0  | docs/unknowns/adoption-stabilization-effect.md:88  | ### Phase 0 Data Collection
- Phase 0  | docs/unknowns/adoption-stabilization-effect.md:90  | During Phase 0, we will record:
- Phase 0  | docs/unknowns/adoption-stabilization-effect.md:99  | 2. **Measure corridor-specific volatility:** After Phase 0 launch, calculate the same metrics for BCH/VES in the Spain→Venezuela corridor
- Phase 0  | docs/unknowns/adoption-stabilization-effect.md:115  | - Effect detectable after ~6 months of sustained Phase 0/1 volume
- Phase 0  | docs/unknowns/adoption-stabilization-effect.md:122  | 2. **Volume threshold too high:** Effect exists but requires 10x more volume than Phase 0 can generate
- Phase 0  | docs/unknowns/adoption-stabilization-effect.md:128  | ## Phase 0 Trial Integration
- Phase 0  | docs/unknowns/adoption-stabilization-effect.md:162  | 4. Contact the Asgaya team for access to Phase 0 transaction data when available
- Phase 0  | docs/unknowns/adoption-stabilization-effect.md:223  | **Status:** Hypothesis formation complete. Data collection begins with Phase 0 launch. Analysis framework designed. Seeking community review of methodology.
- Phase 0  | docs/unknowns/adoption-stabilization-effect.md:228  | 3. Begin Phase 0 data collection
- Phase 0  | docs/unknowns/adoption-stabilization-effect.md:234  | *This unknown is designed to be resolved through empirical measurement during Phase 0 and beyond. Success or failure both provide valuable information—but success would be transformative.*

## `docs/unknowns/behavioral/cash-float-management.md`  (2)

- Phase 0  | docs/unknowns/behavioral/cash-float-management.md:29  | 4. Observe actual practices in Phase 0
- Phase 0  | docs/unknowns/behavioral/cash-float-management.md:35  | ## Phase 0 Trial Integration

## `docs/unknowns/behavioral/claim-timing.md`  (9)

- Phase 0  | docs/unknowns/behavioral/claim-timing.md:116  | - Posted in relevant communities with context (Asgaya Phase 0 research)
- Phase 0  | docs/unknowns/behavioral/claim-timing.md:144  | ### Option 3: Phase 0 Trial Measurement (Highest Reliability, Delayed)
- Phase 0  | docs/unknowns/behavioral/claim-timing.md:146  | **During Phase 0 trials, track:**
- Phase 0  | docs/unknowns/behavioral/claim-timing.md:164  | **Estimated effort:** Built into Phase 0 infrastructure (logging)
- Phase 0  | docs/unknowns/behavioral/claim-timing.md:179  | **Minimum viable answer (for Phase 0 launch):**
- Phase 1  | docs/unknowns/behavioral/claim-timing.md:184  | **Gold standard answer (for Phase 1 refinement):**
- Phase 0  | docs/unknowns/behavioral/claim-timing.md:185  | - Actual Phase 0 trial data from 30+ transactions
- Phase 0  | docs/unknowns/behavioral/claim-timing.md:190  | ## Phase 0 Trial Integration
- Phase 0  | docs/unknowns/behavioral/claim-timing.md:208  | **Real-time Phase 0 metrics:**

## `docs/unknowns/behavioral/merchant-asset-preference.md`  (6)

- Phase 0  | docs/unknowns/behavioral/merchant-asset-preference.md:29  | - HAu might be unnecessary for Phase 0
- Phase 0  | docs/unknowns/behavioral/merchant-asset-preference.md:158  | **Answered = "Merchants prefer [asset] because [reasons], here's our Phase 0 design based on this."**
- Phase 0  | docs/unknowns/behavioral/merchant-asset-preference.md:193  | - Launch Phase 0 with H€ only, add HAu later
- Phase 0  | docs/unknowns/behavioral/merchant-asset-preference.md:198  | - Must support both from Phase 0
- Phase 1  | docs/unknowns/behavioral/merchant-asset-preference.md:205  | - Revisit stability layer in Phase 1
- Phase 0  | docs/unknowns/behavioral/merchant-asset-preference.md:207  | **This unknown determines scope of Phase 0 development.**

## `docs/unknowns/behavioral/merchant-bch-preference.md`  (4)

- Phase 1  | docs/unknowns/behavioral/merchant-bch-preference.md:16  | - Liquidity design for Phase 1
- Phase 0  | docs/unknowns/behavioral/merchant-bch-preference.md:29  | 4. Track actual behavior in Phase 0
- Phase 0  | docs/unknowns/behavioral/merchant-bch-preference.md:33  | Understanding of merchant BCH preferences with measured data from Phase 0 trials.
- Phase 0  | docs/unknowns/behavioral/merchant-bch-preference.md:35  | ## Phase 0 Trial Integration

## `docs/unknowns/behavioral/seller-buy-the-dip.md`  (4)

- Phase 0  | docs/unknowns/behavioral/seller-buy-the-dip.md:73  | **Phase 0 reveals:** Are aborts a bug or a feature from seller perspective?
- Phase 0  | docs/unknowns/behavioral/seller-buy-the-dip.md:184  | ## How Phase 0 Can Reveal This
- Phase 0  | docs/unknowns/behavioral/seller-buy-the-dip.md:290  | - Phase 0 has 3+ covenant aborts (enough data points)
- Phase 0  | docs/unknowns/behavioral/seller-buy-the-dip.md:316  | **Status:** Unknown - awaiting Phase 0 data

## `docs/unknowns/behavioral/sender-he-optin.md`  (4)

- Phase 0  | docs/unknowns/behavioral/sender-he-optin.md:142  | ### Step 5: Test Actual Behavior in Phase 0
- Phase 0  | docs/unknowns/behavioral/sender-he-optin.md:144  | **Phase 0 measurement:**
- Phase 0  | docs/unknowns/behavioral/sender-he-optin.md:154  | **Deliverable:** Real usage data from Phase 0 trials
- Phase 0  | docs/unknowns/behavioral/sender-he-optin.md:175  | - Plan for Phase 0 measurement

## `docs/unknowns/behavioral/token-holding-duration.md`  (6)

- Phase 0  | docs/unknowns/behavioral/token-holding-duration.md:18  | - Does holding duration change over time (Phase 0 vs Phase 1)?
- Phase 0  | docs/unknowns/behavioral/token-holding-duration.md:54  | **Venezuelan merchants (Phase 0):**
- Phase 0  | docs/unknowns/behavioral/token-holding-duration.md:166  | ### Step 5: Measure Actual Behavior in Phase 0
- Phase 0  | docs/unknowns/behavioral/token-holding-duration.md:180  | **Deliverable:** Real usage data from Phase 0 proving/disproving hypothesis
- Phase 0  | docs/unknowns/behavioral/token-holding-duration.md:213  | - Data analysis (if Phase 0 data available)
- Phase 0  | docs/unknowns/behavioral/token-holding-duration.md:276  | **Phase 0 will reveal if these optimizations are worth complexity.**

## `docs/unknowns/bulletin-board-anti-spam.md`  (17)

- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:4  | **Priority:** High (Phase 0 critical)
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:39  | - Phase 0 users see spam → assume system is broken
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:87  | - Might discourage Phase 0 participation
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:89  | **Phase 0 test:** Start with 0.001 BCH, monitor spam levels, raise if needed.
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:135  | **Phase 0 feasibility:** High (we already have notification bot infrastructure)
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:162  | **Phase 0 feasibility:** Low (privacy/complexity concerns)
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:175  | **Phase 0 implementation:**
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:189  | ### Phase 0 (Venezuela Launch)
- Phase 1  | docs/unknowns/bulletin-board-anti-spam.md:194  | - No device fingerprinting (Phase 1 feature)
- Phase 1+  | docs/unknowns/bulletin-board-anti-spam.md:207  | ### Phase 1+ (Post-Venezuela)
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:227  | **Phase 0 hypothesis:** 0.001 BCH is sufficient for Venezuela (€0.50 is non-trivial)
- Phase 1+  | docs/unknowns/bulletin-board-anti-spam.md:241  | **Recommendation:** Start client-side (Phase 0), move to on-chain if abuse detected (Phase 1+)
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:272  | 2. **No user complaints:** <10% of Phase 0 users report spam as a problem
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:289  | - [Phase 0 Validation](../glossary.md#progressive-decentralisation) - Testing approach
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:304  | 4. **Monitor Phase 0:** Track spam rates, user complaints, listing quality
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:308  | **Status:** Hypothesis formed. Baseline strategy documented. Awaiting Phase 0 data.
- Phase 0  | docs/unknowns/bulletin-board-anti-spam.md:311  | 1. Implement 0.001 BCH minimum commitment in Phase 0

## `docs/unknowns/cascade-effect-stabilization.md`  (17)

- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:45  | ### Phase 1: Hyperinflation Countries (Year 1-2)
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:66  | - NOW BCH is more stable (thanks to Phase 1)
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:164  | **Phase 1 Launch (Venezuela, Argentina):**
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:185  | - Merchant adoption FASTER in Phase 2 than Phase 1 (despite lower pain)
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:187  | - Can point to Phase 1 data: "Look, it's working and getting more stable"
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:195  | 1. **Global volatility declines during Phase 1:**
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:201  | - "Volatility concern" mentioned by <30% of merchants (vs >60% in Phase 1)
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:205  | - BCH community engagement increases during Phase 1
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:210  | - BCH/USD volatility unchanged during Phase 1
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:211  | - Phase 2 adoption no easier than Phase 1
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:215  | - BCH becomes MORE volatile during Phase 1 (Venezuela launch triggers regulatory attacks, etc.)
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:216  | - Phase 2 adoption HARDER than Phase 1
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:230  | - Keep low profile until Phase 1 proven
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:252  | **Risk:** BCH volatility declines during Phase 1, but due to other factors (ETF approval, bull market, etc.)
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:320  | 3. **Time horizon:** How long after Phase 1 launch should we expect to see stabilization effect?
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:350  | **Status:** Hypothesis formed. Measurement framework designed. Awaiting Phase 1 launch data. This is a multi-year question that will only be resolved retrospectively.
- Phase 1  | docs/unknowns/cascade-effect-stabilization.md:353  | 1. Launch Venezuela (Phase 1)

## `docs/unknowns/economic/7-percent-drop-frequency.md`  (6)

- Phase 0  | docs/unknowns/economic/7-percent-drop-frequency.md:128  | ### Step 5: Estimate Phase 0 Abort Impact
- Phase 0  | docs/unknowns/economic/7-percent-drop-frequency.md:133  | - 100 remittances/week in Phase 0
- Phase 0  | docs/unknowns/economic/7-percent-drop-frequency.md:141  | **Deliverable:** Phase 0 abort demand estimate
- Phase 0  | docs/unknowns/economic/7-percent-drop-frequency.md:156  | - "In Phase 0, expect X covenant aborts per week"
- Phase 0  | docs/unknowns/economic/7-percent-drop-frequency.md:165  | **Answered = "BCH drops >7% in X% of periods, here's the data, here's our Phase 0 plan."**
- Phase 1+  | docs/unknowns/economic/7-percent-drop-frequency.md:230  | **These are Phase 1+ optimizations, but good to understand patterns early.**

## `docs/unknowns/economic/bull-pool-capital.md`  (5)

- Phase 0  | docs/unknowns/economic/bull-pool-capital.md:12  | **Is €3,000 founder capital sufficient to bootstrap H€/HAu tokens for Phase 0?**
- Phase 0  | docs/unknowns/economic/bull-pool-capital.md:48  | **€3K is sufficient for Phase 0 if merchant velocity is high (weekly cash-out).**
- Phase 0  | docs/unknowns/economic/bull-pool-capital.md:169  | ### Step 5: Survey Potential Phase 0 Participants
- Phase 0  | docs/unknowns/economic/bull-pool-capital.md:198  | - "We need €N for comfortable Phase 0"
- Phase 0  | docs/unknowns/economic/bull-pool-capital.md:247  | - Phase 0 merchant target: ~10 merchants

## `docs/unknowns/economic/contract-period-duration.md`  (3)

- Phase 0  | docs/unknowns/economic/contract-period-duration.md:67  | - Monitor renewal success rate in Phase 0
- Phase 0  | docs/unknowns/economic/contract-period-duration.md:96  | - **High velocity (Phase 0):** Merchants convert H€ → VES weekly
- Phase 1  | docs/unknowns/economic/contract-period-duration.md:101  | - **Medium velocity (Phase 1):** Some hold monthly

## `docs/unknowns/economic/fiat-chargeback-risk.md`  (1)

- Phase 0  | docs/unknowns/economic/fiat-chargeback-risk.md:31  | ## Phase 0 Trial Integration

## `docs/unknowns/economic/merchant-spread-sufficiency.md`  (2)

- Phase 0  | docs/unknowns/economic/merchant-spread-sufficiency.md:29  | Merchant retention above 80% during Phase 0 trials with measured transaction volumes meeting projections.
- Phase 0  | docs/unknowns/economic/merchant-spread-sufficiency.md:31  | ## Phase 0 Trial Integration

## `docs/unknowns/economic/overcollateralization-rate.md`  (6)

- Phase 0  | docs/unknowns/economic/overcollateralization-rate.md:202  | **Minimum viable answer (for Phase 0 launch):**
- Phase 1  | docs/unknowns/economic/overcollateralization-rate.md:207  | **Gold standard answer (for Phase 1 refinement):**
- Phase 0  | docs/unknowns/economic/overcollateralization-rate.md:210  | - Phase 0 trial data confirming predictions
- Phase 0  | docs/unknowns/economic/overcollateralization-rate.md:214  | ## Phase 0 Trial Integration
- Phase 0  | docs/unknowns/economic/overcollateralization-rate.md:218  | **During Phase 0 trials:**
- Phase 0  | docs/unknowns/economic/overcollateralization-rate.md:352  | - Manual (Phase 0 team decision based on monitoring)?

## `docs/unknowns/economic/seller-fee-sufficiency.md`  (2)

- Phase 0  | docs/unknowns/economic/seller-fee-sufficiency.md:29  | Demonstrated seller interest at 0.5% fee level during Phase 0 trials, with measured capital recycling achieving target velocity.
- Phase 0  | docs/unknowns/economic/seller-fee-sufficiency.md:31  | ## Phase 0 Trial Integration

## `docs/unknowns/economic/volatility-buffer-rate.md`  (2)

- Phase 0  | docs/unknowns/economic/volatility-buffer-rate.md:31  | ## Phase 0 Trial Integration
- Phase 0  | docs/unknowns/economic/volatility-buffer-rate.md:43  | **Design intent (Suso, Aug 15):** 7% is Phase 0's fixed default. Dynamic sizing based on recent downward volatility is the target, and the covenant architecture accommodates it.

## `docs/unknowns/hyperinflation-holding-incentive.md`  (7)

- Phase 0  | docs/unknowns/hyperinflation-holding-incentive.md:29  | ### 2. Changes Phase 0 Design Priorities
- Phase 0  | docs/unknowns/hyperinflation-holding-incentive.md:62  | **Prediction:** Average merchant hold time > 7 days in Phase 0.
- Phase 0  | docs/unknowns/hyperinflation-holding-incentive.md:102  | We haven't launched Phase 0 yet, but informal conversations with Venezuelan merchants reveal:
- Phase 0  | docs/unknowns/hyperinflation-holding-incentive.md:123  | ### Phase 0 Measurement
- Phase 0  | docs/unknowns/hyperinflation-holding-incentive.md:233  | ## Phase 0 Experiments to Run
- Phase 0  | docs/unknowns/hyperinflation-holding-incentive.md:291  | 2. **During Phase 0:** Analyze hold time data monthly
- Phase 0  | docs/unknowns/hyperinflation-holding-incentive.md:297  | **Status:** Hypothesis formed. Measurement framework designed. Awaiting Phase 0 launch for data collection.

## `docs/unknowns/market/bull-pool-demand.md`  (15)

- Phase 0  | docs/unknowns/market/bull-pool-demand.md:17  | - How much capital can we attract at Phase 0 vs Phase 1?
- Phase 0  | docs/unknowns/market/bull-pool-demand.md:28  | - System scales beyond Phase 0
- Phase 0  | docs/unknowns/market/bull-pool-demand.md:32  | - Limited to 10-30 merchants in Phase 0
- Phase 0  | docs/unknowns/market/bull-pool-demand.md:47  | **Phase 0: Founder capital (€3K) is sufficient. External bulls not needed yet.**
- Phase 1  | docs/unknowns/market/bull-pool-demand.md:48  | **Phase 1: Can attract €20-50K from BCH community after proving mechanism.**
- Phase 0  | docs/unknowns/market/bull-pool-demand.md:52  | **Phase 0 (Bootstrap):**
- Phase 1  | docs/unknowns/market/bull-pool-demand.md:58  | **Phase 1 (Growth):**
- Phase 0  | docs/unknowns/market/bull-pool-demand.md:89  | > - What proof do you need before committing? (testnet, Phase 0 results, audit)"
- Phase 0  | docs/unknowns/market/bull-pool-demand.md:154  | ### Step 5: Test Bull Demand in Phase 0
- Phase 0  | docs/unknowns/market/bull-pool-demand.md:159  | - Post in BCH community: "Phase 0 successful, seeking bull capital for Phase 1"
- Phase 0  | docs/unknowns/market/bull-pool-demand.md:170  | **Deliverable:** Phase 1 bull recruitment campaign based on Phase 0 results
- Phase 1  | docs/unknowns/market/bull-pool-demand.md:184  | - "Can attract €X capital at Phase 1"
- Phase 0  | docs/unknowns/market/bull-pool-demand.md:189  | - Phase 0: Bootstrap with €3K (sufficient for pilot)
- Phase 1  | docs/unknowns/market/bull-pool-demand.md:190  | - Phase 1: Crowdfund €20-50K based on proven results
- Phase 0  | docs/unknowns/market/bull-pool-demand.md:250  | **Phase 0 reveals which path makes sense.**

## `docs/unknowns/market/corridor-demand-signals.md`  (3)

- Phase 1  | docs/unknowns/market/corridor-demand-signals.md:15  | - Phase 1 expansion priorities
- Phase 1  | docs/unknowns/market/corridor-demand-signals.md:34  | Prioritized list of 3-5 target corridors for Phase 1 expansion based on demand signals and feasibility.
- Phase 0  | docs/unknowns/market/corridor-demand-signals.md:36  | ## Phase 0 Trial Integration

## `docs/unknowns/market/recipient-smartphone-access.md`  (2)

- Phase 0  | docs/unknowns/market/recipient-smartphone-access.md:22  | 70%+ of remittance recipients in urban Venezuela have Android smartphones (version 8+), sufficient for Phase 0 targeting.
- Phase 0  | docs/unknowns/market/recipient-smartphone-access.md:36  | ## Phase 0 Trial Integration

## `docs/unknowns/market/seller-capital-recycling.md`  (3)

- Phase 0  | docs/unknowns/market/seller-capital-recycling.md:24  | 3. Measure actual recycling rates in Phase 0
- Phase 0  | docs/unknowns/market/seller-capital-recycling.md:29  | Measured seller capital recycling rates meeting or exceeding 300% annual velocity in Phase 0 trials.
- Phase 0  | docs/unknowns/market/seller-capital-recycling.md:31  | ## Phase 0 Trial Integration

## `docs/unknowns/market/seller-ranking-fairness.md`  (8)

- Phase 0  | docs/unknowns/market/seller-ranking-fairness.md:3  | **Status:** Phase 0 Testing (Testnet stress testing needed)
- Phase 0  | docs/unknowns/market/seller-ranking-fairness.md:33  | **Getting this right is essential for Phase 0 success.**
- Phase 0  | docs/unknowns/market/seller-ranking-fairness.md:67  | ### Phase 0 Testnet Stress Testing
- Phase 0  | docs/unknowns/market/seller-ranking-fairness.md:131  | ### Phase 0 Mainnet Validation (Real Users)
- Phase 0  | docs/unknowns/market/seller-ranking-fairness.md:175  | **Mainnet Phase 0 (3 months):**
- Phase 0  | docs/unknowns/market/seller-ranking-fairness.md:188  | ## Phase 0 Trial Integration
- Phase 0  | docs/unknowns/market/seller-ranking-fairness.md:341  | **Estimated effort:** 15-20 hours (testnet simulation) + ongoing monitoring (Phase 0)
- Phase 0  | docs/unknowns/market/seller-ranking-fairness.md:371  | - Flag any red flags for Phase 0 monitoring

## `docs/unknowns/payment-info-exchange.md`  (6)

- Phase 0  | docs/unknowns/payment-info-exchange.md:5  | **Phase:** Phase 0 critical
- Phase 0  | docs/unknowns/payment-info-exchange.md:118  | - Simple enough to implement in Phase 0
- Phase 0  | docs/unknowns/payment-info-exchange.md:172  | 1. Which option (Nostr or OP_RETURN) is better for Phase 0?
- Phase 0  | docs/unknowns/payment-info-exchange.md:201  | **Decision:** Not yet made (Phase 0 design)
- Phase 0  | docs/unknowns/payment-info-exchange.md:204  | **Timeline:** Decision needed before Phase 0 implementation begins
- Phase 0  | docs/unknowns/payment-info-exchange.md:222  | **This unknown represents a critical Phase 0 design decision. Your expertise can help Asgaya choose the right path.**

## `docs/unknowns/technical/anyhedge-claim-compatibility.md`  (1)

- Phase 0  | docs/unknowns/technical/anyhedge-claim-compatibility.md:231  | - **Status:** Viable and under our control - testnet validation before Phase 0

## `docs/unknowns/technical/auto-renewal-reliability.md`  (2)

- Phase 0  | docs/unknowns/technical/auto-renewal-reliability.md:174  | **Phase 0 preparation:**
- Phase 0  | docs/unknowns/technical/auto-renewal-reliability.md:276  | **Phase 0 dashboard should track:**

## `docs/unknowns/technical/bank-notification-parsing-validation.md`  (8)

- Phase 0  | docs/unknowns/technical/bank-notification-parsing-validation.md:4  | **Priority:** 🔴 CRITICAL — Phase 0 Launch Blocker
- Phase 0  | docs/unknowns/technical/bank-notification-parsing-validation.md:25  | **This is the highest technical risk in Phase 0.**
- Phase 1  | docs/unknowns/technical/bank-notification-parsing-validation.md:48  | ### Phase 1: Build Minimal Proof-of-Concept (Week 1)
- Phase 0  | docs/unknowns/technical/bank-notification-parsing-validation.md:79  | **Phase 0 launch criterion:**
- Phase 0  | docs/unknowns/technical/bank-notification-parsing-validation.md:86  | ## Phase 0 Trial Integration
- Phase 0  | docs/unknowns/technical/bank-notification-parsing-validation.md:95  | - Phase 0 sellers run the app on their personal phones
- Phase 0  | docs/unknowns/technical/bank-notification-parsing-validation.md:96  | - Real Bizum transactions from Phase 0 senders
- Phase 0  | docs/unknowns/technical/bank-notification-parsing-validation.md:118  | **Blocker status:** No other Phase 0 work should proceed until this is validated. This is the foundation.

## `docs/unknowns/technical/bch-confirmation-reliability.md`  (2)

- Phase 0  | docs/unknowns/technical/bch-confirmation-reliability.md:29  | Documented BCH transaction reliability with measured confirmation rates in Phase 0 trials.
- Phase 0  | docs/unknowns/technical/bch-confirmation-reliability.md:31  | ## Phase 0 Trial Integration

## `docs/unknowns/technical/dolarapi-accuracy.md`  (2)

- Phase 0  | docs/unknowns/technical/dolarapi-accuracy.md:21  | DolarAPI provides accurate market rates updated hourly with 99%+ uptime, sufficient for Phase 0 needs.
- Phase 0  | docs/unknowns/technical/dolarapi-accuracy.md:35  | ## Phase 0 Trial Integration

## `docs/unknowns/technical/oracle-accuracy.md`  (1)

- Phase 0  | docs/unknowns/technical/oracle-accuracy.md:250  | **Phase 0:** Start simple (single oracle), add safeguards in Phase 1 if needed.

## `docs/unknowns/technical/sms-delivery-venezuela.md`  (1)

- Phase 0  | docs/unknowns/technical/sms-delivery-venezuela.md:32  | ## Phase 0 Trial Integration

## `docs/unknowns/technical/universal-bot-reliability.md`  (3)

- Phase 0  | docs/unknowns/technical/universal-bot-reliability.md:23  | **Next steps:** Phase 0 trials with multiple users/devices to validate across device range.
- Phase 0  | docs/unknowns/technical/universal-bot-reliability.md:47  | Demonstrated 99%+ notification detection reliability across target device range in Phase 0 trials.
- Phase 0  | docs/unknowns/technical/universal-bot-reliability.md:49  | ## Phase 0 Trial Integration

## `docs/unknowns/variable-buffer-rate.md`  (22)

- Phase 0  | docs/unknowns/variable-buffer-rate.md:4  | **Priority:** Medium (Phase 0 enhancement)
- Phase 0  | docs/unknowns/variable-buffer-rate.md:268  | ### Phase 0a: Fixed Buffer (Baseline)
- Phase 0  | docs/unknowns/variable-buffer-rate.md:282  | ### Phase 0b: Variable Buffer (A/B Test)
- Phase 1  | docs/unknowns/variable-buffer-rate.md:294  | **If successful:** Roll out to 100% in Phase 1
- Phase 0  | docs/unknowns/variable-buffer-rate.md:380  | ### Phase 0: Fixed Everything (Venezuela Launch)
- Phase 1  | docs/unknowns/variable-buffer-rate.md:398  | ### Phase 1: Time-Based Fees (IF NEEDED - Data-Driven)
- Phase 0  | docs/unknowns/variable-buffer-rate.md:400  | **Consideration:** Before adding any variable pricing, evaluate Phase 0 data.
- Phase 0  | docs/unknowns/variable-buffer-rate.md:408  | - If Phase 0 works well with 7% / 0.5% / 24h, don't change it
- Phase 1  | docs/unknowns/variable-buffer-rate.md:446  | **Only consider if Phase 1 time-based fees succeed and users want more.**
- Phase 1  | docs/unknowns/variable-buffer-rate.md:460  | - Phase 1 time-based fees working perfectly
- Phase 0  | docs/unknowns/variable-buffer-rate.md:472  | ## Why Not Phase 0?
- Phase 0  | docs/unknowns/variable-buffer-rate.md:515  | **In Phase 0, with Venezuelan grandmothers sending remittances, this is way too much cognitive load.**
- Phase 0  | docs/unknowns/variable-buffer-rate.md:517  | ### The Phase 0 User Reality
- Phase 0  | docs/unknowns/variable-buffer-rate.md:544  | **Phase 0: Build trust through simplicity**
- Phase 1  | docs/unknowns/variable-buffer-rate.md:549  | **Phase 1: Add sophistication once trust established**
- Phase 0  | docs/unknowns/variable-buffer-rate.md:566  | **For Phase 0 (Venezuela - ONLY COMMITMENT):**
- Phase 0  | docs/unknowns/variable-buffer-rate.md:572  | **After Phase 0 (6 months - DATA-DRIVEN DECISION):**
- Phase 0  | docs/unknowns/variable-buffer-rate.md:579  | - **If simple works:** Keep Phase 0 pricing forever (don't fix what isn't broken)
- Phase 1  | docs/unknowns/variable-buffer-rate.md:580  | - **If data shows value:** Add Phase 1 time-based fees (one choice: timeout)
- Phase 1  | docs/unknowns/variable-buffer-rate.md:583  | **Phase 2 (two-dimensional pricing):** Only consider if Phase 1 succeeds and users explicitly request more control. Unlikely to be needed.
- Phase 1  | docs/unknowns/variable-buffer-rate.md:592  | - Phase 1: Variable buffer becomes default, senders save 50%+ on buffer costs through coordination
- Phase 1  | docs/unknowns/variable-buffer-rate.md:615  | // Buffer (same as Phase 1)

## `docs/user-journeys/README.md`  (2)

- Phase 1+  | docs/user-journeys/README.md:91  | ## Phase 1+ Goals (Spain → Venezuela)
- Phase 1+  | docs/user-journeys/README.md:94  | > **Note:** Phase 0 (bootstrap) uses trusted participants to validate demand. The founder plays most passive roles initially. If Phase 0 attracts 5 real merchants, that's success. These numbers reflect Phase 1+ goals once organic growth begins.

## `docs/user-journeys/customer/README.md`  (8)

- Phase 0  | docs/user-journeys/customer/README.md:115  | ## Phase 0 Status
- Phase 0  | docs/user-journeys/customer/README.md:117  | **This flow is NOT a Phase 0 priority.**
- Phase 0  | docs/user-journeys/customer/README.md:119  | **Phase 0 focus:** Remittances
- Phase 1+  | docs/user-journeys/customer/README.md:120  | **Phase 1+ expansion:** Commerce flows
- Phase 0  | docs/user-journeys/customer/README.md:145  | 1. **Phase 0:** Focus on remittance flow (recipient willing to wait for confirmation)
- Phase 1+  | docs/user-journeys/customer/README.md:146  | 2. **Phase 1+:** Revisit customer flow with either:
- Phase 1+  | docs/user-journeys/customer/README.md:171  | **Recommendation:** Phase 1+ customer flow should use 0-conf for small amounts, with merchant-configurable confirmation thresholds for larger transactions.
- Phase 1+  | docs/user-journeys/customer/README.md:181  | **Status:** Phase 1+ (customer flow designed, 0-conf validated, deferred pending remittance validation)

## `docs/user-journeys/customer/cross-border-living.md`  (1)

- Phase 1+  | docs/user-journeys/customer/cross-border-living.md:173  | **Phase 1+** - After remittance infrastructure proven

## `docs/user-journeys/customer/informal-economy-access.md`  (2)

- Phase 0  | docs/user-journeys/customer/informal-economy-access.md:184  | **Phase 0-1:** Build remittance infrastructure for banked users first (prove protocol safety and reliability)
- Phase 0+  | docs/user-journeys/customer/informal-economy-access.md:186  | **Phase 0+ (Cash as Global Default):**

## `docs/user-journeys/customer/merchant-as-atm.md`  (2)

- Phase 0  | docs/user-journeys/customer/merchant-as-atm.md:134  | **Phase 0-1:** Focus on remittances and merchant payments (build merchant network)
- Phase 0+  | docs/user-journeys/customer/merchant-as-atm.md:136  | **Phase 0+ (Cash as Default):** This flow is already enabled once cash is the global default payment option. Like with informal economy, it's not in our hands - cash as a payment option is permissionless. Merchants decide to offer this service organically based on demand.

## `docs/user-journeys/customer/tourist-payments.md`  (1)

- Phase 1+  | docs/user-journeys/customer/tourist-payments.md:59  | **Phase 1+** - After remittance infrastructure proven and merchant network established

## `docs/user-journeys/merchant/README.md`  (7)

- Phase 0+  | docs/user-journeys/merchant/README.md:77  | **No automated notification to Carlos** - Elena just walks to the store (or calls/messages via Nostr if she wants reassurance first). Phase 0+ enhancement: Asgaya direct messages for active coordination.
- Phase 0  | docs/user-journeys/merchant/README.md:185  | ### Phase 0 Solution: Stability Layer (H€ and HAu Tokens)
- Phase 0  | docs/user-journeys/merchant/README.md:232  | **Phase 0 capital:** €3K unified pool (approved assets: H€, HAu). High merchant velocity (weekly VES conversion) means low capital lock. Supports ~80 merchants initially.
- Phase 0  | docs/user-journeys/merchant/README.md:238  | ### Conservative Approach (Phase 0)
- Phase 1+  | docs/user-journeys/merchant/README.md:294  | **The goal: Make BCH boring.** Not exciting, not a get-rich scheme, just a boring tool that works. If Asgaya goes viral and BCH price rallies, that's a problem (breaks the stable-value proposition). Happy path: low-key adoption until inevitable, Phase 0 success → Phase 1+ organic growth.
- Phase 0  | docs/user-journeys/merchant/README.md:380  | **Phase 0 approach:** Focus on legitimate remittance corridors (Spain → Venezuela), where participants are families, not criminals.
- Phase 0  | docs/user-journeys/merchant/README.md:468  | **Status:** Phase 0 (Pre-Launch) - Q3 2026 Spain → Venezuela corridor

## `docs/user-journeys/remittance/README.md`  (3)

- Phase 0  | docs/user-journeys/remittance/README.md:39  | ## Phase 0 Focus
- Phase 0  | docs/user-journeys/remittance/README.md:41  | **Remittances are the Phase 0 priority** because:
- Phase 1+  | docs/user-journeys/remittance/README.md:47  | **Customer/commerce flows are Phase 1+** (natural extension once infrastructure exists).

## `docs/user-journeys/remittance/recipient/README.md`  (4)

- Phase 0+  | docs/user-journeys/remittance/recipient/README.md:151  | **Elena's dual role (Phase 0+):**
- Phase 0+  | docs/user-journeys/remittance/recipient/README.md:258  | **Phase 0+ priority:** Encourage at least one passive BCH buyer/seller with PagoMóvil payment option. Ideal candidate: Venezuelan with VES income (e.g., landlord collecting rent) who wants to preserve purchasing power via H€/HAu. They're highly motivated to provide liquidity and serve rural recipients remotely.
- Phase 0  | docs/user-journeys/remittance/recipient/README.md:288  | 2. **Elena waits** - covenant doesn't expire for 24 hours (8 hours Phase 0)
- Phase 0  | docs/user-journeys/remittance/recipient/README.md:390  | **Status:** Phase 0 (Pre-Launch) - Q3 2026 Spain → Venezuela corridor

## `docs/user-journeys/remittance/sender/README.md`  (11)

- Phase 0  | docs/user-journeys/remittance/sender/README.md:26  | **Phase 0 (Post-Launch):**
- Phase 0+  | docs/user-journeys/remittance/sender/README.md:28  | - **Venezuela:** Cash in person only (PagoMóvil and local bank transfers documented in Phase 0+)
- Phase 0  | docs/user-journeys/remittance/sender/README.md:39  | ### Flow A: Digital Payment (Bizum - Spain Only, Phase 0)
- Phase 0  | docs/user-journeys/remittance/sender/README.md:52  | - Expiry: 8 hours (Phase 0)
- Phase 0  | docs/user-journeys/remittance/sender/README.md:221  | **Scenario:** María sends €100, but Elena doesn't claim within 8 hours (Phase 0 time limit).
- Phase 0  | docs/user-journeys/remittance/sender/README.md:243  | - **Phase 0 conservative approach:** Research funds limited, validate core hypothesis first
- Phase 0  | docs/user-journeys/remittance/sender/README.md:272  | - If María chooses to stabilize (Phase 0 manual): App offers "Mint H€ to preserve €100 value?" (if bull pool has capacity)
- Phase 0  | docs/user-journeys/remittance/sender/README.md:281  | **Hypothesis:** This warning prevents procrastination and reduces abort rate (untested in Phase 0).
- Phase 0  | docs/user-journeys/remittance/sender/README.md:299  | **Phase 0 Resolution:**
- Phase 1+  | docs/user-journeys/remittance/sender/README.md:304  | **Phase 1+ Improvement:**
- Phase 0  | docs/user-journeys/remittance/sender/README.md:368  | **Status:** Phase 0 (Pre-Launch) - Q3 2026 Spain → Venezuela corridor

## `docs/user-journeys/remittance/sender/auto-refund-ux.md`  (3)

- Phase 0  | docs/user-journeys/remittance/sender/auto-refund-ux.md:188  | **Technical note:** H€ tokens are native BCH tokens (CashTokens). Anyone can be an H€ seller on the bulletin board - completely permissionless role. Phase 0: Asgaya bootstraps liquidity.
- Phase 0  | docs/user-journeys/remittance/sender/auto-refund-ux.md:283  | **Phase 0 note:** Asgaya bootstraps both bull pool and H€ bulletin board liquidity. Edge cases unlikely but possible during high volatility.
- Phase 1  | docs/user-journeys/remittance/sender/auto-refund-ux.md:477  | **Status:** Phase 1.5 - Documentation (auto-refund UX designed, implementation planned)

## `docs/user-journeys/trader/README.md`  (4)

- Phase 0  | docs/user-journeys/trader/README.md:116  | - Expiry: 8 hours (Phase 0 parameter)
- Phase 0  | docs/user-journeys/trader/README.md:232  | **Phase 0 expiry: 8 hours**
- Phase 1+  | docs/user-journeys/trader/README.md:302  | - Phase 0 vs Phase 1+ expectations
- Phase 0  | docs/user-journeys/trader/README.md:346  | **Status:** Phase 0 (Pre-Launch) - Q3 2026 Spain → Venezuela corridor

## `docs/user-journeys/trader/economics.md`  (22)

- Phase 0  | docs/user-journeys/trader/economics.md:123  | - Phase 0: 5-10 transactions/day (limited demand)
- Phase 1+  | docs/user-journeys/trader/economics.md:124  | - Phase 1+: 20-50 transactions/day (proven demand)
- Phase 0  | docs/user-journeys/trader/economics.md:127  | - €2.50-€5/day (Phase 0)
- Phase 1+  | docs/user-journeys/trader/economics.md:128  | - €10-€25/day (Phase 1+)
- Phase 0  | docs/user-journeys/trader/economics.md:142  | - Phase 0: 10-20 transactions/day
- Phase 1+  | docs/user-journeys/trader/economics.md:143  | - Phase 1+: 50-100 transactions/day
- Phase 0  | docs/user-journeys/trader/economics.md:146  | - €5-€10/day (Phase 0)
- Phase 1+  | docs/user-journeys/trader/economics.md:147  | - €25-€50/day (Phase 1+)
- Phase 0  | docs/user-journeys/trader/economics.md:161  | - Phase 0: 20-50 transactions/day (supply exceeds demand)
- Phase 1+  | docs/user-journeys/trader/economics.md:162  | - Phase 1+: 100+ transactions/day (if demand exists)
- Phase 0  | docs/user-journeys/trader/economics.md:165  | - €10-€25/day (Phase 0, bottlenecked by demand)
- Phase 1+  | docs/user-journeys/trader/economics.md:166  | - €50+/day (Phase 1+, if demand scales)
- Phase 0  | docs/user-journeys/trader/economics.md:168  | **Reality check:** Extra capital doesn't help if demand is low. Phase 0 might only see 50 total transactions/day across all sellers. Better to start small and scale capital as demand grows.
- Phase 0  | docs/user-journeys/trader/economics.md:184  | **Phase 0 reality:** Total market might only support 50-100 transactions/day across all sellers. Start with €500-€1,500, scale capital only when demand validates it.
- Phase 0  | docs/user-journeys/trader/economics.md:396  | **Behavioral unknown:** Will sellers actually buy the dip after aborts? Phase 0 will reveal actual behavior. See `unknowns/behavioral/seller-buy-the-dip.md` for research question.
- Phase 1+  | docs/user-journeys/trader/economics.md:400  | ## Phase 0 vs Phase 1+ Economics
- Phase 0  | docs/user-journeys/trader/economics.md:402  | ### Phase 0 Reality (Spain → Venezuela)
- Phase 0  | docs/user-journeys/trader/economics.md:415  | - €500 minimum (can handle Phase 0 volume)
- Phase 0  | docs/user-journeys/trader/economics.md:418  | **Reality check:** Phase 0 is about validation, not profit. €5-€10/day profit is attractive enough to recruit 5-10 passive sellers.
- Phase 1+  | docs/user-journeys/trader/economics.md:420  | ### Phase 1+ Projections (Multiple Corridors)
- Phase 1+  | docs/user-journeys/trader/economics.md:435  | **This is Phase 1+ speculation.** Phase 0 focuses on validation with small capital (€500-€1,500).
- Phase 0  | docs/user-journeys/trader/economics.md:460  | **Phase 0 starting point:** €500-€1,500 capital. Scale only when demand validates it.

## `docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md`  (13)

- Phase 0  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:40  | **Note on 8-hour windows:** RS062 tested 4-hour windows (0.55% abort) and 24-hour windows (2.30% abort). 8-hour windows likely fall between: ~0.8-1.2% abort rate. Longer windows = more volatility exposure. Phase 0 measures actual rate.
- Phase 0  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:57  | **Note:** If median claim time is shorter (2-4 hours as Phase 0 hypothesizes), cycles increase to 4-6 per day, improving capacity to €600K-€900K monthly. The 8-hour window is conservative.
- Phase 1+  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:103  | **Foreknowledge arbitrage (Phase 1+ strategy):** At scale, we predict our own transaction volume (payday patterns repeat monthly). Sellers pre-buy €100K BCH Friday (high liquidity, better price), use reserves Saturday (no market impact), replenish Monday (post-spike, better price). This is inventory management, not manipulation—Amazon pre-positions stock before Prime Day, airlines hedge fuel befor…
- Phase 0  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:113  | ## Phase 0 Validation: What We're Testing
- Phase 0  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:151  | - If wrong, we adjust during Phase 0 (5% if too conservative, 10% if insufficient)
- Phase 0  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:156  | **Phase 0 validates the guess. Then we know.**
- Phase 1  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:160  | ## Phase 1: Dynamic Buffer Evolution
- Phase 0  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:162  | **Phase 0 uses fixed 7% buffer to establish baseline.**
- Phase 1+  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:164  | **Phase 1+ implements dynamic buffer based on recent volatility** ([RS074: Dynamic Volatility Buffer](../../research/RS074_dynamic_volatility_buffer.md))
- Phase 0  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:181  | | High volatility (Phase 0 start) | 5% | 7% | €107 |
- Phase 0  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:195  | **Phase 0 validates the 7% baseline. Phase 1 makes it adaptive.**
- Phase 1+  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:222  | - [RS074: Dynamic Volatility Buffer](../../research/RS074_dynamic_volatility_buffer.md) (Phase 1+ evolution: adaptive buffer based on downside volatility)
- Phase 0  | docs/why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md:230  | **Status:** Phase 0 Validation (Phase 1 dynamic buffer designed)

## `docs/why-this-design/constraints/asgaya-remittances-inefficient-by-design.md`  (7)

- Phase 0  | docs/why-this-design/constraints/asgaya-remittances-inefficient-by-design.md:16  | **Note on 8-hour window:** Phase 0 hypothesis based on RS062 data (99.45% success at 4-hour windows) + stability layer protecting senders on abort. Allows gathering complete claim-timing data without excessive covenant failures.
- Phase 0  | docs/why-this-design/constraints/asgaya-remittances-inefficient-by-design.md:47  | **But this is an assumption.** Phase 0 validates whether recipients actually tolerate this UX.
- Phase 0  | docs/why-this-design/constraints/asgaya-remittances-inefficient-by-design.md:65  | ## Payment Timeout: 10 Minutes (Phase 0 Hypothesis)
- Phase 0  | docs/why-this-design/constraints/asgaya-remittances-inefficient-by-design.md:74  | **Note:** This timeout is a Phase 0 starting assumption, validated by sender behavior data. May be adjusted based on real-world usage patterns.
- Phase 0  | docs/why-this-design/constraints/asgaya-remittances-inefficient-by-design.md:78  | ## Phase 0 Validation
- Phase 0  | docs/why-this-design/constraints/asgaya-remittances-inefficient-by-design.md:137  | **If we're wrong:** Phase 0 abort rate will be >50%, recipients will complain, adoption fails. We adjust or pivot.
- Phase 0  | docs/why-this-design/constraints/asgaya-remittances-inefficient-by-design.md:154  | **Status:** Phase 0 Validation

## `docs/why-this-design/constraints/cash-accounts-permissionless-identity-layer.md`  (3)

- Phase 0  | docs/why-this-design/constraints/cash-accounts-permissionless-identity-layer.md:87  | ## Phase 0 Validation: What We're Testing
- Phase 0  | docs/why-this-design/constraints/cash-accounts-permissionless-identity-layer.md:93  | | **Adoption** | % of Phase 0 users who register Cash Accounts? | >80% |
- Phase 0  | docs/why-this-design/constraints/cash-accounts-permissionless-identity-layer.md:125  | **Status:** Phase 0 Implementation

## `docs/why-this-design/constraints/covenant-simplicity-principle.md`  (3)

- Phase 1  | docs/why-this-design/constraints/covenant-simplicity-principle.md:174  | | **Phase 1** | MTP-only refund | Basic refund path | Too slow for testing (hours) |
- Phase 1  | docs/why-this-design/constraints/covenant-simplicity-principle.md:255  | **Discovered:** July 2026 during Phase 1 covenant testing (v2.0 → v2.2 evolution)
- Phase 0  | docs/why-this-design/constraints/covenant-simplicity-principle.md:334  | **Phase 0 validates:** Does separation of concerns translate to real-world robustness?

## `docs/why-this-design/constraints/funder-principle.md`  (1)

- Phase 0  | docs/why-this-design/constraints/funder-principle.md:600  | **Current scope:** Single-funder covenants only (Phase 0-1)

## `docs/why-this-design/constraints/passive-mode-bot-automation.md`  (3)

- Phase 0  | docs/why-this-design/constraints/passive-mode-bot-automation.md:108  | ## Phase 0 Validation: What We're Testing
- Phase 0  | docs/why-this-design/constraints/passive-mode-bot-automation.md:131  | | **Bot complexity** | Requires technical setup (API keys, monitoring) | Phase 0: Suso runs bots for sellers; Phase 1: Turnkey bot packages | Complexity frontloaded (setup once), infinite passive benefit |
- Phase 0  | docs/why-this-design/constraints/passive-mode-bot-automation.md:159  | **Status:** Phase 0 Implementation

## `docs/why-this-design/constraints/progressive-payment-rollout.md`  (6)

- Phase 0  | docs/why-this-design/constraints/progressive-payment-rollout.md:23  | **Phase 0: Start with the simplest**
- Phase 0+  | docs/why-this-design/constraints/progressive-payment-rollout.md:28  | **Phase 0+: Add methods based on user demand**
- Phase 1+  | docs/why-this-design/constraints/progressive-payment-rollout.md:35  | **Phase 1+: Geographic expansion**
- Phase 0  | docs/why-this-design/constraints/progressive-payment-rollout.md:171  | ## Phase 0 Validation: What We're Testing
- Phase 0  | docs/why-this-design/constraints/progressive-payment-rollout.md:186  | - Cash-in-person drives >50% of Phase 0 transaction volume
- Phase 0  | docs/why-this-design/constraints/progressive-payment-rollout.md:221  | **Status:** Phase 0 Implementation

## `docs/why-this-design/constraints/reputation-on-chain-not-central-database.md`  (3)

- Phase 1+  | docs/why-this-design/constraints/reputation-on-chain-not-central-database.md:188  | **Phase 1+ optimization:** Nostr relays may cache the bulletin board (updated hourly) for faster queries, falling back to blockchain for trustless verification.
- Phase 0  | docs/why-this-design/constraints/reputation-on-chain-not-central-database.md:223  | ## Phase 0 Validation: What We're Testing
- Phase 0  | docs/why-this-design/constraints/reputation-on-chain-not-central-database.md:279  | **Status:** Phase 0 Implementation

## `docs/why-this-design/constraints/time-oracle-mtp-fallback-trustless-ux.md`  (9)

- Phase 1  | docs/why-this-design/constraints/time-oracle-mtp-fallback-trustless-ux.md:225  | **Phase 1.5 evolution:** [Distributed Monitoring](../../the-mechanism/nostr-coordination/distributed-monitoring.md) implements blockchain-as-oracle architecture where every covenant funding becomes a trade signal. Price discovery emerges from reputation-filtered VWAP of real Asgaya trades (on-chain), bootstrapped by Asgaya oracle until network matures. Censorship-resistant (blockchain can't be shu…
- Phase 0  | docs/why-this-design/constraints/time-oracle-mtp-fallback-trustless-ux.md:320  | ### Phase 0: Simple Split (✅ Complete)
- Phase 1  | docs/why-this-design/constraints/time-oracle-mtp-fallback-trustless-ux.md:357  | ### Phase 1: Price Oracle (🔜 Next)
- Phase 1  | docs/why-this-design/constraints/time-oracle-mtp-fallback-trustless-ux.md:486  | **Decision needed:** Phase 1 implementation (next session)
- Phase 1  | docs/why-this-design/constraints/time-oracle-mtp-fallback-trustless-ux.md:509  | **Decision needed:** Phase 1 implementation
- Phase 1  | docs/why-this-design/constraints/time-oracle-mtp-fallback-trustless-ux.md:534  | **Decision needed:** Phase 1 implementation
- Phase 1  | docs/why-this-design/constraints/time-oracle-mtp-fallback-trustless-ux.md:554  | **Decision needed:** Phase 1 implementation
- Phase 0  | docs/why-this-design/constraints/time-oracle-mtp-fallback-trustless-ux.md:561  | - [Phase 0 Covenant Testing (2026-07-20)](../../../knowledge/meta/project_blog/2026-07-20_time-oracle-decision-phase0-covenant.md) - Initial covenant architecture
- Phase 1  | docs/why-this-design/constraints/time-oracle-mtp-fallback-trustless-ux.md:570  | - Oracle research: TBD (Phase 1)

## `docs/why-this-design/fraud-protection.md`  (6)

- Phase 0  | docs/why-this-design/fraud-protection.md:272  | 4. Covenant expires after 8 hours (Phase 0 limit)
- Phase 0  | docs/why-this-design/fraud-protection.md:276  | **Resolution (Phase 0 design):**
- Phase 0  | docs/why-this-design/fraud-protection.md:318  | **Design decision (Phase 0):**
- Phase 0  | docs/why-this-design/fraud-protection.md:331  | **Phase 0 uses cosign approach.**
- Phase 0  | docs/why-this-design/fraud-protection.md:395  | - Mitigation: Focus Phase 0 on Spain-based sellers
- Phase 0  | docs/why-this-design/fraud-protection.md:409  | **Phase 0 will measure:**

## `docs/why-this-design/requirements/README.md`  (5)

- Phase 0  | docs/why-this-design/requirements/README.md:94  | - Bull pool structure (Suso provides long positions Phase 0)
- Phase 1  | docs/why-this-design/requirements/README.md:130  | **Discovered during Phase 1 implementation (July 2026)**
- Phase 0  | docs/why-this-design/requirements/README.md:218  | - Important but premature (Phase 0 validates demand first)
- Phase 1  | docs/why-this-design/requirements/README.md:231  | - **July 2026: Seller buffer recovery requirement discovered** (Phase 1 chipnet testing revealed edge case)
- Phase 0  | docs/why-this-design/requirements/README.md:254  | **Phase 0 validates:** Do these requirements translate to real-world adoption?

## `docs/why-this-design/ux-principles.md`  (36)

- Phase 0  | docs/why-this-design/ux-principles.md:5  | **Status:** Production-proven (Phase 0 - August 2026)
- Phase 0  | docs/why-this-design/ux-principles.md:63  | **For Phase 0:** Manual checking is acceptable. One covenant at a time. User expects to monitor.
- Phase 0  | docs/why-this-design/ux-principles.md:69  | ## Principle 2: Simplicity First (Phase 0 Philosophy)
- Phase 0  | docs/why-this-design/ux-principles.md:73  | ### What Phase 0 Includes
- Phase 0  | docs/why-this-design/ux-principles.md:90  | ### What Phase 0 Excludes
- Phase 0  | docs/why-this-design/ux-principles.md:113  | 4. **Phase 0 is about proving viability, not polish**
- Phase 0  | docs/why-this-design/ux-principles.md:117  | **After Phase 0 proves:**
- Phase 0  | docs/why-this-design/ux-principles.md:148  | - **Phase 0 use case:** One covenant at a time, user expects to monitor
- Phase 0  | docs/why-this-design/ux-principles.md:151  | - **ROI:** High complexity for minimal UX improvement in Phase 0
- Phase 0  | docs/why-this-design/ux-principles.md:154  | - ✅ Ship with manual "Update Status" button (Phase 0)
- Phase 0  | docs/why-this-design/ux-principles.md:170  | - **Phase 0 context:** Single-device testing, one covenant at a time, user can wait 5 seconds
- Phase 0  | docs/why-this-design/ux-principles.md:223  | **TCP cooldown (Phase 0):**
- Phase 0  | docs/why-this-design/ux-principles.md:280  | - **Verdict:** Manual is acceptable for Phase 0
- Phase 0  | docs/why-this-design/ux-principles.md:287  | **Phase 0 priority ranking:**
- Phase 0  | docs/why-this-design/ux-principles.md:304  | ### Phase 0: Prove Core Flow
- Phase 1  | docs/why-this-design/ux-principles.md:323  | ### Phase 1: Merchant Cash-Out Flow (Next)
- Phase 0  | docs/why-this-design/ux-principles.md:358  | 1. What pain points did Phase 0/1 reveal?
- Phase 0  | docs/why-this-design/ux-principles.md:374  | // Phase 0 implementation
- Phase 0  | docs/why-this-design/ux-principles.md:390  | // Phase 0 implementation
- Phase 0  | docs/why-this-design/ux-principles.md:447  | Phase 0 scope:
- Phase 0  | docs/why-this-design/ux-principles.md:466  | Phase 0 scope:
- Phase 0  | docs/why-this-design/ux-principles.md:469  | Phase 1 scope (after Phase 0 proven):
- Phase 1  | docs/why-this-design/ux-principles.md:472  | Phase 2 scope (after observing Phase 1 usage):
- Phase 0  | docs/why-this-design/ux-principles.md:505  | // Phase 0: Accept 5-second delay
- Phase 1  | docs/why-this-design/ux-principles.md:508  | // After Phase 1: Measure actual pain
- Phase 0  | docs/why-this-design/ux-principles.md:537  | **Phase 0 choice:** Manual copy-paste via Telegram
- Phase 0  | docs/why-this-design/ux-principles.md:552  | **For Phase 0:** Manual is safer (fewer failure modes)
- Phase 0  | docs/why-this-design/ux-principles.md:560  | **For Phase 0:** Manual wins (lower cost)
- Phase 0  | docs/why-this-design/ux-principles.md:570  | ## Measuring UX Success (Phase 0)
- Phase 0  | docs/why-this-design/ux-principles.md:585  | - Not optimizing for speed in Phase 0
- Phase 0  | docs/why-this-design/ux-principles.md:596  | ### What We're NOT Measuring (Phase 0)
- Phase 0  | docs/why-this-design/ux-principles.md:611  | ### At Scale (Post Phase 0)
- Phase 1+  | docs/why-this-design/ux-principles.md:624  | ### With Nostr Integration (Phase 1+)
- Phase 0  | docs/why-this-design/ux-principles.md:654  | 2. **Simplicity First** - Phase 0 is minimalist, add features incrementally
- Phase 1  | docs/why-this-design/ux-principles.md:662  | **What's next:** Apply same principles to Phase 1 (merchant flow). Prove it works before optimizing it.
- Phase 0  | docs/why-this-design/ux-principles.md:668  | **Evidence:** Phase 0 core flow working, documented, reproducible

