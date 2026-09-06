# Asgaya Roadmap

> Single source of truth for what ships in each phase. Mined systematically from `docs/` (see source column on every row).
> **Phase definitions are canonical in** [android-app/README.md](implementation/android-app/README.md) (Phase Model section).
> **Terminology:** Phase 0 = MVP (interchangeable). Phase 0 launches as a limited **mainnet beta** with real money and a small, trusted user set.

---

## Phase Model (Canonical)

| Phase | Definition | When |
|-------|------------|------|
| **Phase 0 (= MVP)** | Full value proposition end-to-end, launched as a limited **mainnet beta** with real money and a small, trusted user set (gated on MVP confidence; currently validated on testnet3) | Now / first |
| **Phase 0+** | Opportunistic wins added *during* Phase 0 testing (cheap UX/reliability) | During Phase 0 |
| **Phase 1** | Post-MVP enhancements driven by observed Phase 0 behavior | After MVP confidence |
| **Phase 1+** | Later, larger enhancements / geographic expansion | Future |

**Current status:** Phase 0 MVP validation on **testnet3** (Sep 1, 2026: first merchant-first transaction on-chain). Phase 0 mainnet beta not yet started — gated on MVP confidence.

---

## PHASE 0 — MVP (mainnet beta, real money, limited trusted users)

**North star:** full sender → seller → covenant → recipient → merchant loop working in person (cash) and via Bizum, with the **customer flow** (merchant accepts BCH) treated as a first-class path.

### On-ramp (fiat → BCH)

| # | Feature | Status | Source | Notes |
|---|---------|--------|--------|-------|
|  | **Cash Accounts (register + resolve)** | 🎯 REQUIRED | wallet.md, cash-accounts…md | Match key for seller auto-funding Bizum concept field; interface to legacy payment system (human readability = bonus). MVP = Phase 0. |
|  | Cash-in-person seller construct + fund (manual "cash received" trigger) | 🎯 Target | seller-auto-funding/06 | Price lock = seller constructs at payment receipt; forward-sale narrative |
|  | `[SELLER_REQUEST]` from sender device (all covenant params) | 🎯 Target | seller-auto-funding/06 | Even in person; sender is the contracting buyer |
|  | Seller verifies covenant is genuine (funderPubkey = seller) before funding | 🎯 Target | seller-auto-funding/06 | Sale due-diligence; buffer return depends on it |
|  | Bizum auto-funding (NotificationListener parse → fund) | 📅 Planned | implementation/README | Second milestone; shares the construct+fund core |
|  | Sender creates covenant with seller (funder≠sender) unfunded | ✅ Plumbing | SendViewModel | Needs create-only split (currently atomic self-fund) |

### Customer flow (merchant accepts BCH) — raised priority

| # | Feature | Status | Source | Notes |
|---|---------|--------|--------|-------|
|  | Customer flow = remittance config (recipient = merchant pubkey) | 🎯 Target | seller-auto-funding/06 | Same covenant; merchant terminal `claim()` |
|  | Merchant UI flag: claim before releasing goods (refund window) | 🎯 Target | (workspace decision) | Customer can `refund()` until merchant claims |
|  | Merchant auto-claim client (Phase 1 preferred) | 📅 Phase 1 | (workspace decision) | Notification-listener watches funding → claim; merchant/seller-focused client |

### Off-ramp (cashout)

| # | Feature | Status | Source | Notes |
|---|---------|--------|--------|-------|
|  | Merchant cashout (merchant-first) | ✅ Done on-chain | implementation/README | TXID 05301369… Sep 1 2026 |

### Discovery / coordination

| # | Feature | Status | Source | Notes |
|---|---------|--------|--------|-------|
|  | Bulletin board (on-chain NFT listings) | 🔨 Planned | android-app/README, bulletin-board.md | Electrum NFT queries; design complete |
|  | Nostr coordination (encrypted DMs) | 🔨 In progress | android-app/README, nostr.md | Replace Telegram copy-paste |
|  | Hardcoded test seller / merchant (no bulletin board yet) | ✅ | seller-auto-funding | Phase 0 test pattern |

### Core infra (done or nearly)

| # | Feature | Status | Source | Notes |
|---|---------|--------|--------|-------|
|  | Covenant v2.6.1 — 5 spend paths | ✅ Done | version-history.md | testnet3 |
|  | v0.2 hybrid architecture | ✅ Done | implementation/README | Kotlin network, WebView compute |
|  | Oracle (own, 16-byte, multi-source) | ✅ Done | time-oracle…md | MTP fallback |
|  | Multi-wallet management | ✅ Done | wallet.md | |
|  | Self-funded sender flow | ✅ Done | android-app/README | |

---

## PHASE 0+ — During Phase 0 (opportunistic)

| # | Feature | Why 0+ | Source |
|---|---------|--------|--------|
|  | Seed phrase backup (BIP39) | Cheap UX win | android-app/README |
|  | Seller device health monitoring | Already built (RS072/075) | android-app/README, notification-bot.md |
|  | Bank notification parsing (real bank apps) | Enables auto-fund | notification-bot.md |
|  | HD wallet derivation | UX | wallet.md |
|  | Covenant tracking (state) | UX | state-management.md |
|  | Market price subscription | Volume dependent | notification-bot.md |
|  | Electrum redundancy (3-5 servers) | Reliability | android-app/README |

*(Cash Accounts moved OUT of 0+ → they are Phase 0 / MVP REQUIRED, per Suso Sep 6.)*

---

## PHASE 1 — Post-MVP (observed-behavior driven)

| # | Feature | Driver | Source |
|---|---------|--------|--------|
|  | Merchant/seller-focused Asgaya client (auto-claim + auto-fund) | Refund-window fix; passive income | (workspace decision) |
|  | Auto-claim via notification listener (customer flow) | Close refund window in seconds | (workspace decision) |
|  | Stability layer H€/HAu activation | Merchant need (0+ launch-first per stability-layer.md) | stability-layer.md |
|  | Reputation system (on-chain reputation UTXO) | Trust for untrusted sellers | reputation-on-chain…md |
|  | Seller ranking algorithm | Discovery UX | seller-ranking-algorithm.md |
|  | Passive mode bot automation | 24/7 liquidity | passive-mode-bot-automation.md |
|  | Multi-covenant batching | Optimization | android-app/README |
|  | Move covenant UTXO fetch fully to Kotlin | WebView 100% network-free | implementation/README |
|  | Pure Kotlin covenant building (migrate from WebView) | Platform control | manual-construction.md |
|  | Own Nostr relay (if public relays unreliable) | Operational | nostr.md |
|  | 0-conf acceptance hardening | In-person UX | RS082 |

---

## PHASE 1+ — Future / Expansion

| # | Feature | Source |
|---|---------|--------|
|  | Offline-first (offline queue, cache, sync) | offline-first.md |
|  | Geographic expansion (Spain→Venezuela first; PagoMóvil, M-Pesa) | progressive-payment-rollout.md |
|  | iOS / web clients | implementation/README |
|  | Protocol specifications (formal) | implementation/README |
|  | N-of-M oracle support in covenant (v2.7+) | seller-auto-funding/06 |

---

## Flow Map (who does what, per phase)

_(To be completed from mining — 2-party contract framing: sender buys BCH at a future price; covenant determines price via refund/claim/merchant-counter.)_

| Leg | Phase 0 | Phase 1 |
|-----|---------|---------|
| **On-ramp cash** | sender shows `[SELLER_REQUEST]` → seller constructs at fresh price → sender verifies → cash → fund | merchant/seller client auto-processes |
| **On-ramp Bizum** | (2nd milestone) | auto-fund via notification listener |
| **Customer flow** | merchant claims manually before releasing goods (UI flag) | merchant auto-claims on funding |
| **Off-ramp cashout** | merchant-first cashout (done) | — |

---

## Mined corpus & method

- **Method:** deterministic extraction (grep, 729 phase-tagged lines across 94 docs) → grouped corpus → verified against authoritative seeds.
- **⚠️ gemma4:e2b was NOT usable for this** — it hallucinated document structure and failed even verbatim table copy (confirmed again Sep 6; consistent with `05-GEMMA-MINING-EXPERIMENT.md` which limits it to short bounded Suso-quote copying). Deterministic extraction is the reliable miner.
- **Corpus:** `roadmap-mining-corpus.md` (sibling file) — every phase-tagged line with file:line, grouped by doc.
- **Authoritative seeds:** `implementation/README.md` (Sep 1), `implementation/android-app/README.md` (Aug 24 — stale), `seller-auto-funding/06-…` (Sep 6).

---

## ⚠️ Known conflicts / stale items found during mining

| # | Item | Conflict | Resolution (Suso 🗨️, Sep 6) |
|---|------|----------|-------------------|
| 1 | **Cash Account registration phase** | `android-app/README.md` says **Phase 0+** (line 139); `wallet.md` says **Phase 1+** (line 20) | ✅ **MVP / Phase 0 — REQUIRED.** Cash Accounts are needed for the seller auto-funding Bizum concept field (match key). In Asgaya they exist to interface with the **legacy payment system**; human readability is a bonus, not the reason. (MVP = Phase 0, interchangeable.) |
| 2 | **Electrum network naming** | `android-app/README.md` says "Chipnet.imaginary.cash" + "v2.5 on chipnet" (lines 66, 184); `index.md` + `implementation/README.md` say **testnet3** | ✅ **testnet3** (Suso correction Sep 2). Server is local Pi-chan Fulcrum `192.168.1.100`. Fixed in android-app README. |
| 3 | **Merchant cash-out status** | `android-app/README.md` Phase 0 table lists "Merchant cash-out flow 🔨 Planned" (stale, dated Aug 24) | ✅ **DONE on-chain Sep 1** (merchant-first). Fixed in android-app README. |
| 4 | **Stability layer H€/HAu phase** | `stability-layer.md` = **Phase 0+ "launch first"** | ✅ Keep 0+ (launch BCH-only, add if merchants demonstrate need) |
| 5 | **Nostr = Phase 0 coordination** | Consistent across `nostr.md` (Phase 0 priority) + `nostr-coordination/README.md` | ✅ No conflict; Telegram = Phase 0 test/fallback only |
| 6 | **Seller auto-funding framing** | `notification-bot.md` called bank-notification auto-funding **Phase 1+** | ✅ Fixed (Sep 6): cash-in-person on-ramp = Phase 0 first; Bizum auto-fund = Phase 0 second milestone (shares construct+fund core) |
| 7 | **"Phase 0 = testnet" phrasing** | Several older docs imply Phase 0 runs on testnet | ✅ Phase 0 = **mainnet beta** (MVP, real money, limited trusted users); testnet3 = pre-Phase-0 MVP validation |

---

## Status legend

✅ Done on-chain/testnet3 · 🔨 In progress · 🔧 Edit needed · 🎯 Next implementation target · 📅 Planned milestone · 🔵 Opportunistic (0+) · 🟢 Future (1+)

**Last updated:** 2026-09-06
**Maintainer:** TightDS 🔍 (mining) + Coordination ⚙️ (assembly) + Suso 🗨️ (priority calls)
