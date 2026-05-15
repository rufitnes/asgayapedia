# Backend-APIs: How Asgaya Queries the BCH Blockchain

**Status:** Active — Covenant Architecture (NFT-Native)  
**Last Updated:** 2026-05-16  
**Version:** 3.0 (Complete Rewrite for NFT-Based Bulletin Board)

---

## TL;DR

**Asgaya does not have a traditional backend.** In production, the mobile app queries the Bitcoin Cash blockchain directly via public Electrum servers. There is no central server, no REST API, no database. The blockchain IS the database.

**What this folder documents:**
- How the mobile app discovers sellers and merchants (by scanning the blockchain for NFTs)
- How covenants track remittance state (on-chain, no server needed)
- How sellers run their own automation (locally, not as an Asgaya service)
- How we test everything locally (pichan + regtest for development)

**If you're looking for REST API docs, there aren't any.** This folder explains why, and what replaces them.

---

## Why No Backend?

| Traditional Backend | Asgaya |
|---------------------|--------|
| REST API endpoints | Electrum JSON-RPC (blockchain queries) |
| PostgreSQL / MongoDB | BCH blockchain (UTXO set + NFT commitments) |
| JWT authentication | BCH signatures (prove address ownership) |
| WebSocket heartbeats | UTXO existence = liveness |
| Server tracks transaction state | Covenant UTXO IS the state |
| Sellers register via API | Sellers broadcast NFT covenant transaction |
| Notifications via push server | OP_RETURN on blockchain |

**The blockchain is permissionless.** Anyone can post a seller NFT. Anyone can query it. No approval needed. No server to shut down.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                   PRODUCTION (Phase 0+)                   │
│                                                          │
│  Mobile App ──── Electrum JSON-RPC ──── BCH Blockchain   │
│  (Pixel/Moto)      (public server)      (mainnet)        │
│                                                          │
│  Seller Bot (smsbridge_loop.py)                          │
│  └── Run by individual sellers, NOT by Asgaya            │
│                                                          │
│  No central server. No REST API. No database.            │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                  DEVELOPMENT (Husk v0.1-v0.2)             │
│                                                          │
│  Mobile App ──── Electrum JSON-RPC ──── pichan (local)   │
│                     192.168.1.42:50002    └── bitcoind   │
│                                              └── Fulcrum │
│  Same code, different endpoint.                          │
└─────────────────────────────────────────────────────────┘
```

---

## Folder Structure

```
backend-apis/
├── README.md                     ← YOU ARE HERE
│
├── blockchain-scanner/           How mobile app reads the blockchain
│   ├── nft-scanner.md            Discover sellers/merchants via NFTs
│   ├── covenant-watcher.md       Track remittance state on-chain
│   └── cashaccount-resolver.md   Resolve human-readable names
│
├── seller-bot/                   For sellers to run (NOT centralized)
│   ├── README.md                 "How to become a BCH seller"
│   ├── smsbridge_loop.md         Parse payment notifications
│   ├── nft-manager.md            Create/update your seller NFT
│   └── covenant-signer.md        Sign covenants to release BCH
│
├── mobile-wallet/                BCH operations on the phone
│   ├── electrum-client.md        Connect to Electrum servers
│   ├── covenant-creation.md      Build covenant transactions (CashScript)
│   └── transaction-building.md   General BCH transaction utilities
│
├── development-tools/            Local testing infrastructure
│   ├── pichan-regtest-setup.md   Run BCH node + Fulcrum on Raspberry Pi
│   ├── multi-device-test-plan.md Test with Pixel + Moto + pichan
│   └── covenant-testing.md       Test CashScript contracts on regtest/chipnet
│
├── rate-apis.md                  Exchange rate estimates (CoinGecko + DolarAPI)
├── user-apis.md                  BCH address = user identity
├── common-patterns.md            BCH signature authentication
├── bch-native-architecture.md    WHY BCH was chosen (architectural rationale)
│
├── archive/                      Pre-covenant architecture (April 2026)
│   └── ENGINEERING_JOURNEY.md    Evolution story (escrow → covenant)
│
└── llm.txt                       AI context file
```

---

## Key Concepts

### 1. Blockchain IS the Database

**Sellers don't call an API to register.** They broadcast a BCH transaction that creates an NFT (CashToken) with an `ASGAYA_SELLER_V1` category. The NFT commitment (128 bytes) contains their offer: payment methods, limits, fee, contact info.

**The mobile app discovers sellers by querying Electrum:** "Give me all UTXOs with category `ASGAYA_SELLER_V1`." Each UTXO represents an available seller. The UTXO value = how much BCH they have available to sell. No database, no server.

### 2. Covenant = State Machine

**Transaction state is NOT tracked in a database.** When a covenant is created, a UTXO appears on the blockchain. Its locking script enforces the conditions. The mobile app checks covenant state by querying: does the UTXO exist? Has it been spent? What signatures are still needed?

| Covenant State | What's on Blockchain |
|----------------|---------------------|
| Created | UTXO exists, no signatures yet |
| Funded | BCH locked in covenant, NFT attached |
| Merchant Signed | One signature present |
| Recipient Signed | Both signatures present → UTXO spent → Complete |
| Expired | Timelock passed → UTXO refundable |

### 3. No Central Coordination

**The BCH seller runs their own bot.** `smsbridge_loop.py` monitors their bank notifications. When a Bizum payment arrives, the bot matches it to an open bounty, signs the covenant, and BCH is released. Asgaya does not provide this as a service—it provides the **documentation** so anyone can set it up themselves.

### 4. Development Tools Are Temporary

**pichan (Raspberry Pi 5) runs a local BCH node and Electrum server during development.** This lets us test real covenant contracts on regtest (instant blocks) and chipnet (real testnet) before deploying to mainnet. In production, the mobile app queries public Electrum servers. pichan becomes the seller bot (if the developer chooses to run a seller).

---

## For Reviewers

**You might be wondering:**
- **"Where are the REST API docs?"** — There aren't any. Asgaya queries the blockchain directly via Electrum JSON-RPC. This folder documents how.
- **"How do sellers sign up?"** — They broadcast a transaction. No approval. No API key.
- **"How do you prevent spam/fake sellers?"** — UTXOs require real BCH collateral. A fake seller with no BCH in their covenant UTXO has nothing to sell. Reputation can be built via on-chain transaction history.
- **"What about disputes?"** — The covenant executes autonomously. If both parties sign, BCH is released. If neither signs within the timeout, it refunds. Phase 0 uses trusted parties only.
- **"Is this really a backend?"** — No. It's documentation of how to query the BCH blockchain. The 'backend' is the blockchain itself.

---

## Progression

| Phase | Electrum Endpoint | Blockchain | Backend? |
|-------|-------------------|------------|----------|
| Husk v0.1 | pichan:50002 | regtest (local) | pichan (dev tool) |
| Husk v0.2 | pichan:50002 | chipnet (testnet) | pichan (dev tool) |
| Phase 0 | fulcrum.fountainhead.cash:50002 | mainnet | **None** |
| Phase 1+ | Multiple public Electrum servers | mainnet | **None** |

---

## Related Documents

- [BCH Native Architecture](bch-native-architecture.md) — Why BCH was chosen
- [Engineering Journey](archive/ENGINEERING_JOURNEY.md) — Evolution from escrow to covenant architecture
- [Pichan Regtest Setup](pichan-regtest-setup.md) — Local development environment
- [Multi-Device Test Plan](multi-device-test-plan.md) — Systematic testing
- [Phase 0 Decentralization Plan](../../decisions/phase-0-progressive-decentralization.md) — Full progression strategy

---

*Last rewrite: 2026-05-16 (Version 3.0)*  
*Previous version: Centralized escrow model (see archive/ENGINEERING_JOURNEY.md)*
