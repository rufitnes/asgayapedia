# Backend APIs: Optional Services

**Status:** Documentation in progress

---

## Overview

Asgaya's core protocol is fully peer-to-peer and requires NO backend services. However, optional backend services can enhance user experience:

1. **Push Notification Service:** Faster notifications than polling blockchain
2. **Price Oracle API:** Real-time BCH/EUR/VES exchange rates
3. **Reputation Aggregator:** Cache on-chain ratings for fast queries
4. **Analytics (Optional):** Usage metrics for protocol improvements

**Important:** All backend services are OPTIONAL. The protocol works without them.

---

## Services

### 1. Push Notification Service (Optional)
**What:** WebSocket server for real-time covenant notifications  
**Why:** Faster than polling blockchain every 10 minutes  
**How:** Subscribe to user's Cash Account, push notification when covenant detected

**Status:** Not implemented in Phase 0 (using blockchain polling instead)

---

### 2. Price Oracle API (Optional)
**What:** REST API for current BCH/EUR/VES exchange rates  
**Why:** Wallet needs current rate to calculate covenant amounts  
**How:** Aggregate from multiple exchanges (Kraken, Binance, LocalBitcoins)

**Status:** Using public APIs in Phase 0 (CoinGecko, Kraken)

---

### 3. Reputation Aggregator (Optional)
**What:** Cache layer for on-chain reputation ratings  
**Why:** Faster than querying blockchain for every user's rating  
**How:** Index all rating transactions, serve via REST API

**Status:** Not implemented in Phase 0 (querying blockchain directly)

---

### 4. Analytics (Optional, Privacy-Preserving)
**What:** Usage metrics for protocol improvements  
**Why:** Understand adoption patterns, optimize UX  
**How:** Count transactions, volume, active users (no PII collected)

**Status:** Not implemented in Phase 0 (privacy-first approach)

---

## Why Backend Services Are Optional

**Asgaya's design philosophy:**
- **No central point of failure** - backend down = protocol still works
- **No custody** - backends never hold user funds
- **No PII collection** - backends never see names, emails, phone numbers
- **Open source** - anyone can run their own backend

**Phase 0 approach:** Minimize backend dependencies, prove protocol works peer-to-peer.

**Phase 1+:** Add optional backends for UX improvements (push notifications, faster queries).

---

**Status:** Placeholder - backend APIs not required in Phase 0  
**Updated:** 2026-06-16
