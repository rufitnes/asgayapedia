# Android App: Reference Implementation

**This section will contain content moved from `/android-app/`**

---

## Overview

The Asgaya Android app is the reference implementation of the protocol. It demonstrates:

1. **Wallet:** Self-custody BCH, Cash Account management, covenant creation
2. **Bulletin Board:** Query blockchain NFTs, parse listings, filter results
3. **Nostr:** P2P messaging, encrypted coordination, relay management
4. **Notification Bot:** Bank notification monitoring, payment matching, auto-funding
5. **Stability Layer:** H€/HAu token integration, AnyHedge contracts

---

## Architecture

### Components

```
/app
  /wallet
    - BCH self-custody
    - Cash Account (identity)
    - Covenant creation/management
  
  /bulletin-board
    - NFT indexing
    - Listing queries
    - Match algorithms
  
  /nostr
    - Relay connections
    - Encrypted messaging
    - P2P coordination
  
  /notification-bot
    - Bank SMS monitoring
    - Payment matching
    - Auto-covenant funding
  
  /stability
    - H€/HAu integration
    - AnyHedge contracts
    - Pool management
```

---

## Key Implementation Details

### 1. Cash Accounts as Identity
**File:** `/wallet/CashAccountManager.kt`  
**What:** Registers `name#number` on BCH blockchain, creates lookup key

### 2. Covenant Creation
**File:** `/wallet/CovenantBuilder.kt`  
**What:** Builds BCH script for bounty contracts with volatility buffer

### 3. Bulletin Board Indexing
**File:** `/bulletin-board/NFTIndexer.kt`  
**What:** Queries BCH blockchain for listing NFTs, parses metadata

### 4. Nostr Messaging
**File:** `/nostr/RelayManager.kt`  
**What:** Connects to Nostr relays, sends/receives encrypted messages

### 5. Payment Matching
**File:** `/notification-bot/PaymentMatcher.kt`  
**What:** Uses Cash Account from bank reference to match payments to covenants

---

## Content to be migrated from `/android-app/`

- App architecture
- Component implementations
- Code examples
- API documentation
- Setup instructions

**Status:** Placeholder - content migration pending  
**Source:** `/android-app/` (OLD structure)
