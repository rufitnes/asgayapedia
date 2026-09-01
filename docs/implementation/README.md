# Implementation: Technical Details
**📖 Unfamiliar terms?** See the [glossary](../glossary.md) for definitions.

**Purpose:** This section contains implementation details, API documentation, and technical specifications.

---

## Current Status

**Phase:** Phase 0 - Production Core Flow Ready 🏆  
**Last Update:** September 1, 2026  
**Historic Milestone:** First inter-device covenant claim successful!

**Production-Proven Capabilities:**
- ✅ **Covenant v2.6.1** - Production-proven on testnet3 (abort path validated Aug 15, 2026)
  - All 5 spending paths implemented (claim, merchantCashout, refund, abort, sellerRecoverBuffer)
  - Claim path proven with real devices (Aug 10, 2026)
  - Refund path tested and reliable (Aug 8, 2026)
  - Smart contract validation working (rejected incorrect claim attempt)
- ✅ **End-to-End Claim Flow** - Cross-device payment proven
  - Sender device (Moto G06) → Recipient device (Pixel 6a)
  - Telegram parameter transport ([COVENANT_V25] format)
  - NotificationListener auto-parsing
  - Manual balance check + claim execution
  - On-chain verification (€5 payment + 7% buffer distribution confirmed)
- ✅ **Self-Funding Sender Flow** - Production-ready
  - Create covenant → Fund → Share parameters → Refund safety net
  - Copy-to-share mechanism (Telegram/Nostr pattern)
  - Connection management (5-second TCP cooldown, WebSocket cleanup)
- ✅ **Multi-Wallet Management** - Complete (sender/recipient/seller wallet matching)
- ✅ **v0.2 Hybrid Architecture** - Kotlin owns network, WebView does compute (Aug 20-21)
  - CREATE/REFUND/CLAIM/ABORT all on hybrid (build() + Kotlin broadcast)
  - CREATE ~100ms, covenant ops ~200ms, zero WebSocket connections on critical path
  - Multi-device bug (WebSocket connection accumulation) eliminated at the root
- ✅ **WebView Integration** - Compute-only (CashScript build/sign); broadcast moved to Kotlin (Aug 20)
- ✅ **Oracle Husk (Pi-chan)** - Running at `192.168.1.100:3001`, dynamic pubkey fetching, zero hardcoded keys (Aug 16)
- ✅ **Timeout & Lifecycle Handling** - `withTimeout()` + ViewModel migration (RS083); stuck "Sending..." bug resolved (Aug 17-20)
- ✅ **Merchant Cashout (Merchant-First)** - **Production-proven on-chain** (Sep 1, 2026)
  - Merchant pre-signs first (fresh oracle at counter → protects 0.5% margin)
  - Recipient verifies (8 checks) + co-signs
  - QR transport (face-to-face, offline recipient) + broadcast via Kotlin TCP
  - First on-chain transaction: `05301369c518a8be60a3453cf6b09f048cdeae1a5925755c828c4f866a69f22`
  - See [merchant-cashout-flow.md](android-app/merchant-cashout-flow.md)

**In Progress:**
- ⏳ **BCH Seller Auto-Funding** - Sender creates unfunded covenant (funderPubkey = seller); seller auto-funds on matched Bizum payment. The last core Phase-0 feature.
- ⏳ **Multi-Covenant Batching** - Claim multiple covenants in one transaction
- ⏳ **Move covenant UTXO fetch to Kotlin** - REFUND/CLAIM/ABORT still use brief WebSocket for `contract.getUtxos()`; moving to Kotlin makes WebView 100% network-free (Phase 1 enhancement)

**Key Achievements (August 8 - September 1, 2026):**
- 🏆 **First guaranteed-value BCH transfer using native covenants between two devices** (Aug 10)
- 🏆 **v0.2 hybrid architecture** — eliminated the WebView connection bug class at the root (Aug 20-21)
- 🏆 **3-device testing milestone** — Pixel 6a + Moto G06 + 3rd device, all running AsgayaHusk v0.2 (Aug 23)
- 🏆 **First merchant cashout on-chain (manual paste)** — dual-signature co-signing proven (Aug 27)
- 🏆 **QR merchant cashout production-ready** (Aug 28)
- 🏆 **Merchant-first flow reversal + first merchant-first transaction on-chain** (Aug 31 - Sep 1)
- Covenant lifecycle complete (create → fund → claim/refund → verified on-chain)
- Critical bug discovered and fixed (seller address must match funder - documented)
- Connection management patterns discovered (TCP cooldown prevents WebSocket hangs)
- Complete documentation (funder principle, claim flow, version history, merchant cashout flow)

**Next Milestone:** BCH seller auto-funding (completes the sender side) → bulletin board + Nostr → full Phase-0 E2E starting from a Bizum notification

---

## Overview

After understanding [The Mechanism](/the-mechanism/README.md), [User Journeys](/user-journeys/README.md), and [Why This Design](/why-this-design/README.md), you might want to:

- **Build:** Implement your own Asgaya client
- **Integrate:** Connect existing app to Asgaya protocol
- **Extend:** Add new features or payment rails
- **Debug:** Understand error codes and edge cases

This section provides the technical details you need.

---

## Key References

### [Android App](/implementation/android-app/README.md)
**What:** Reference implementation of Asgaya client

Contains:
- App architecture
- Component implementations (wallet, bulletin board, nostr, notification bot)
- Code examples
- Electrum integration (blockchain queries)
- Error handling

**Read this if:** You're building or extending the Android client

---

### [Glossary](/glossary.md)
**What:** Definitions of technical terms (site-wide reference)

Contains:
- Asgaya-specific terms (Cash Account, covenant, volatility buffer, H€/HAu)
- BCH concepts (CashTokens, OP_RETURN, Nostr)
- Payment systems (Bizum, PagoMóvil, SEPA)
- Economic concepts (money velocity, capital recycling)

**Note:** Glossary is at root level (serves all documentation sections)

**Read this if:** You're confused by terminology

---

## Content Status

### ✅ Already Exists
- Android app implementation (in `/android-app/`)
- Glossary (at `/glossary.md`)

### 📝 To Be Created (Phase 1+)
- Protocol specifications (formal spec)
- Integration guides (how to add new payment rails)
- iOS/web client implementations

### 🔄 Needs Review (Phase 0)
- Android app docs - verify accuracy during trials

---

## How to Use This Section

### If You're Building
Start with [Android App](/implementation/android-app/README.md) → see reference implementation and blockchain query patterns

### If You're Confused
Read [Glossary](/glossary.md) → define terms

---

## What's NOT in This Section

### Conceptual Explanations
**See:** [The Mechanism](/the-mechanism/README.md) - what components do, how they work

### Rationale
**See:** [Why This Design](/why-this-design/README.md) - why these choices were made

### User Guides
**See:** [User Journeys](/user-journeys/README.md) - step-by-step flows for end users

**This section is purely technical** - code, blockchain queries, specs, debugging.

**Phase 0 focus:** Minimalistic implementation. Less stuff to break. No optional backend services.

---

## Contributing to This Section

**We welcome contributions:**
- Code improvements (Android app)
- New client implementations (iOS, web, desktop)
- API documentation (backend services)
- Integration guides (new payment rails)

**How to contribute:**
- Fork GitHub repository
- Submit pull request with implementation
- Document new features in this section

---

## Navigation

**[🏠 Home](../index.md)** | **[📖 Glossary](../glossary.md)**

**In this section:**
- [Android App](android-app/README.md) - 7 technical components

**Related sections:** [The Mechanism](../the-mechanism/README.md) · [User Journeys](../user-journeys/README.md) · [Why This Design?](../why-this-design/README.md)
