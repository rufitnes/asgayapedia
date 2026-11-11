# Asgaya Husk - Phase 0 Wallet App

**Status:** Active Development (Testnet3 Testing)  
**Purpose:** Mobile wallet for claiming BCH covenants, Phase 0 proof-of-concept  
**Based on:** BizumParser notification listener (proven coordination layer)

---

## What is AsgayaHusk?

AsgayaHusk is the skeleton/foundation of the full Asgaya mobile app. It builds on top of BizumParser's proven notification detection and adds:

1. **Wallet UI** - Balance display, pending covenant notifications
2. **Covenant claiming** - WebView bridge to CashScript for transaction building
3. **Electrum client** - UTXO queries, transaction broadcasting
4. **Transaction history** - Record of claimed covenants

**"Husk" = outer shell**, the framework that will grow into the full production Asgaya app.

---

## Architecture

```
┌─────────────────────────────────────────┐
│   AsgayaHusk (Native Android - Kotlin)   │
│                                          │
│  ✅ NotificationListener (from BizumParser)
│  ✅ MainActivity (Wallet home screen)    │
│  🔧 ClaimActivity (Covenant claiming UI) │
│  🔧 ElectrumClient (Blockchain queries)  │
│  🔧 CovenantBridge (WebView interface)   │
│                                          │
│  ┌────────────────────────────────────┐ │
│  │   WebView (CashScript operations)  │ │
│  │   - claimCovenant()                │ │
│  │   - Returns transaction hex        │ │
│  └────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

---

## Current Status

**✅ Working:**
- Notification detection (Telegram, future: Nostr)
- Notification parsing (amount, covenant address)
- SQLite database (transaction history)
- Material 3 UI framework

**🔧 In Progress:**
- Wallet home screen (balance + pending covenant card)
- Covenant claiming flow
- WebView bridge integration
- Electrum client

**📋 TODO:**
- Error handling (expired covenant, network failures)
- Transaction history screen
- Settings screen (currency preference, testnet/mainnet toggle)

---

## Key Differences from BizumParser

| Feature | BizumParser | AsgayaHusk |
|---------|-------------|------------|
| **Purpose** | Parse Bizum notifications | Full wallet app |
| **UI** | Notification list only | Balance + claiming + history |
| **Blockchain** | None | Electrum client (queries, broadcast) |
| **WebView** | None | CashScript bridge for claiming |
| **Users** | Testing tool | Production wallet (Phase 0) |

---

## Currency Display

**BCH first, then fiat** based on user locale:
- Spain: `0.15 BCH ≈ €100.00`
- Venezuela: `0.15 BCH ≈ 45,000 VES`
- Argentina: `0.15 BCH ≈ 125,000 ARS`

BCH is the bridge currency, fiat is just a reference.

---

## Building & Running

```bash
cd /home/suso/Documents/asgaya/knowledge/research_code/AsgayaHusk

# Build APK
./gradlew assembleDebug

# Install on device (via ADB)
adb install app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat | grep "Asgaya"
```

---

## Next Steps

**This session:**
1. ✅ Copied BizumParser → AsgayaHusk
2. ✅ Renamed package com.asgaya.bizumparser → com.asgaya.husk
3. ✅ Updated app name, theme, manifest
4. 🔧 Next: Design wallet home screen UI

**Next session:**
1. Redesign `activity_main.xml` (wallet home screen layout)
2. Update `MainActivity.kt` (show balance, pending covenant card)
3. Create `ClaimActivity` (claiming flow UI)
4. Build ElectrumClient stub (query balance, broadcast tx)

---

**Started:** 2026-07-29  
**Testnet3 Testing:** 1-2 months (Q3 2026)  
**Mainnet Pilot:** Q4 2026 (after thorough testing)
