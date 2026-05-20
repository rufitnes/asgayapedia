# Engineering Journey: Escrow → Covenant Architecture

**Timeline:** April → May 2026  
**Status:** Complete rewrite (75% of backend eliminated)  
**Decision:** [Phase 0 Progressive Decentralization](../../../decisions/phase-0-progressive-decentralization.md)

---

## TL;DR

**What changed:** Asgaya went from centralized escrow server to pure blockchain queries in 4 weeks.

| Old (April 2026) | New (May 2026) |
|------------------|----------------|
| REST API backend | No backend |
| PostgreSQL database | BCH blockchain |
| Escrow buys BCH on Kraken | BCH seller locks BCH upfront |
| LP matching | P2P bulletin board (NFTs) |
| Server tracks state | Covenant = state machine |

**Why:** No server = no custody = no licenses = permissionless.

---

## The Old Architecture (April 2026)

### Centralized Escrow Model

```
Sender → Backend API → PostgreSQL
              ↓
         Escrow Server
         ├── Receives EUR
         ├── Holds in bank
         ├── Buys BCH on Kraken (after confirmations)
         └── Sends to recipient
```

**Components:**
- **19 REST endpoints** (transaction APIs, merchant APIs, settlement APIs, etc.)
- **PostgreSQL** tracking transaction state
- **LP (Liquidity Provider) role** matching buyers/sellers
- **Escrow backend** coordinating everything

**Rationale:** Familiar fintech pattern. Seemed easier to implement. "We'll decentralize later."

---

## What We Discovered (Problems)

### 1. Custody Risk
- Escrow holding EUR = money transmitter (FinCEN, MiCA)
- Requires licensing, KYC, compliance team
- Direct conflict with "no custody" design goal

### 2. Single Point of Failure
- Server down = system down
- DNS seizure = service unavailable
- Cloud provider ban = dead

### 3. Scalability Bottleneck
- Server processes every transaction
- Database becomes chokepoint
- Horizontal scaling requires coordination layer

### 4. Regulatory Attack Surface
- Clear "operator" to target
- Server logs = evidence
- Centralized = easy to shut down

### 5. LP Complexity
- Liquidity fragmentation (multiple LPs, multiple pools)
- Order matching algorithm needed
- Dispute resolution required

---

## The Breakthrough (May 2026)

### BCH Research Session (RS057)

**Discovery:** Bitcoin Cash has ALL the primitives needed:
- **CashTokens NFTs** (May 2023 upgrade) - 128-byte commitments
- **Native introspection covenants** (May 2022) - Trustless escrow
- **OP_RETURN** (220 bytes) - On-chain notifications
- **Cash Accounts** (2019) - Human-readable names

**Key insight from collaborative debate:**
> "The blockchain CAN be the database. Not 'blockchain-backed REST API' - blockchain IS the coordination layer."

### NFT-Based Bulletin Board

**Realization:** Sellers don't need to "register via API." They broadcast a transaction.

```javascript
// Old way
POST /api/v1/sellers/register
Body: { payment_methods, limits, fee }
→ Stored in PostgreSQL

// New way
Broadcast transaction with ASGAYA_SELLER NFT
NFT commitment: { payment_methods, limits, fee }
→ Exists on blockchain, queryable by anyone
```

**Liveness = UTXO existence.** No heartbeat API needed.

### Covenant State Machine

**Realization:** Transaction state doesn't need a database. The UTXO IS the state.

| State | Blockchain Evidence |
|-------|-------------------|
| Created | UTXO exists |
| Funded | BCH locked in covenant |
| Signed | Witness data present |
| Complete | UTXO spent |
| Expired | Timelock passed |

**Mobile app checks state:** "Does covenant UTXO exist? Query Electrum."

---

## Architecture Evolution

### April 2026: Centralized

```
┌─────────────────────────────────────────┐
│ Mobile App                              │
│   ↓                                     │
│ REST API (19 endpoints)                 │
│   ↓                                     │
│ PostgreSQL (sellers, txs, LPs)          │
│   ↓                                     │
│ Escrow Backend                          │
│   ├── Holds EUR                         │
│   ├── Buys BCH (Kraken API)             │
│   └── Coordinates settlement            │
└─────────────────────────────────────────┘
```

### May 2026: Decentralized

```
┌─────────────────────────────────────────┐
│ Mobile App                              │
│   ↓                                     │
│ Electrum JSON-RPC                       │
│   ↓                                     │
│ BCH Blockchain                          │
│   ├── ASGAYA_SELLER NFTs (bulletin)     │
│   ├── Covenants (escrow)                │
│   └── OP_RETURN (notifications)         │
│                                         │
│ Seller Bots (run by sellers, not us)   │
│   └── smsbridge_loop.py (local)         │
└─────────────────────────────────────────┘
```

---

## What Changed (File-Level)

### Eliminated (289 lines total)
- `merchant-apis.md` - GET /api/v1/merchants/nearby → NFT scanning
- `settlement-apis.md` - LP settlement bounties → LP role eliminated
- `notification-apis.md` - Push notifications → OP_RETURN
- `leaderboard-apis.md` - LP rankings → No LP role

### Rewritten (100% content change)
- `README.md` - REST API index → "How to query blockchain"
- `bch-native-architecture.md` - Escrow + Kraken → Covenants + BCH sellers
- `transaction-apis.md` → `covenant-creation.md` - Server state → Blockchain state

### Updated (targeted edits)
- `rate-apis.md` - Remove Kraken references, update fee model
- `user-apis.md` - Remove centralized transaction history

### Created (new concepts)
- `blockchain-scanner/nft-scanner.md` - How to discover sellers
- `seller-bot/README.md` - How to run your own seller bot
- `development-tools/pichan-regtest-setup.md` - Local BCH node testing

---

## Key Decisions

### 1. Real Electrum Server (Not Mock)
**Debate:** Should Husk v0.1 use a mock backend or real blockchain?

**Decision:** Real Bitcoin Cash Node + Fulcrum on pichan (Raspberry Pi 5).
- Week 1-2: Regtest (instant blocks, local)
- Week 2-3: Chipnet (testnet)
- Week 3-4: Mainnet (public Electrum servers)

**Rationale:** "Practice how you play." Mobile app uses production Electrum client from day one.

### 2. Seller Bots (Decentralized)
**Critical distinction:** `smsbridge_loop.py` is run by **individual sellers**, not by Asgaya.

**Documentation tone shift:**
- Old: "Seller API Reference"
- New: "How to Become a Seller (Run This Yourself)"

### 3. Progressive Decentralization
**Strategy:** B → C → A
- B: Real Electrum on regtest (development)
- C: Real Electrum on chipnet (validation)
- A: Public Electrum on mainnet (production, no backend)

---

## Impact

### Eliminated Components
- ❌ Backend server (Python/Node.js)
- ❌ PostgreSQL database
- ❌ Kraken API integration
- ❌ LP matching algorithm
- ❌ WebSocket notification server
- ❌ JWT authentication

### New Components
- ✅ CashScript covenant contracts
- ✅ NFT commitment encoding/decoding
- ✅ Electrum JSON-RPC client
- ✅ Seller bot documentation (for sellers to run)

### Metrics
- **Backend code:** ~5,000 lines → 0 lines
- **Documentation:** 19 endpoints → 0 endpoints
- **Database tables:** 8 tables → 0 tables
- **External dependencies:** Kraken API → None

---

## Lessons Learned

### 1. Don't Assume You Need a Server
**Initial assumption:** "We need a backend to coordinate."  
**Reality:** Blockchain coordination works if you design for it.

### 2. Primitives Compound
CashTokens (NFTs) + Covenants + OP_RETURN = full remittance system.  
No server glue needed.

### 3. Permissionless Scales
No registration API = no approval process = no bottleneck = infinite sellers.

### 4. Regulatory Clarity
No server = no "operator" = no custody = no licenses (information society service).

### 5. Documentation First
**Quote from Suso:**
> "Drifting from documentation for more than a few hours is asking for trouble considering everyone's limitations."

**Result:** Caught architectural mismatch BEFORE building wrong thing.

---

## For Reviewers

### If You See Old References

Forum posts, GitHub issues, or commit messages before **May 2026** may reference:
- "LP matching algorithm"
- "Escrow margin calculations"
- "Kraken API integration"
- "Merchant registration endpoints"
- "Settlement bounty system"

**These describe the April 2026 architecture** (centralized escrow model).

**Current architecture** (May 2026+): See [`/backend-apis/README.md`](../README.md)

### Why We Document This

**Transparency:** Architecture pivots look suspicious without explanation.  
**Credibility:** Shows deliberate evolution, not chaos.  
**Learning:** Others building on BCH can learn from our journey.

---

## Timeline

| Date | Event |
|------|-------|
| **2026-04-04** | Initial backend-apis docs written (escrow model) |
| **2026-05-11** | BCH research session (RS057) discovers CashTokens + Covenants |
| **2026-05-14** | Collaborative debate: Mock vs Real Electrum |
| **2026-05-15** | Decision: Complete backend-apis rewrite |
| **2026-05-15** | Phase 0 Progressive Decentralization plan finalized |
| **2026-05-16** | Backend-apis rewrite begins (this archive created) |

---

## Related Documents

- [Current Backend-APIs README](../README.md) - What exists now
- [Phase 0 Progressive Decentralization](../../../decisions/phase-0-progressive-decentralization.md) - Full strategy
- [BCH Native Architecture](../bch-native-architecture.md) - Why BCH chosen
- [Overcollateralized Bounty Contracts](../../../concepts/overcollateralized-bounty-contracts.md) - Covenant design

---

## Appendix: Collaborative Debate Process

**Participants:**
- Suso (Orchestrator, final decision maker)
- Claude Sonnet 4.5 (Technical analysis, documentation)
- DeepSeek (Heavy lifting, rewrite proposals)

**Workflow:**
1. Claude identifies architectural concern
2. Suso confirms or redirects
3. DeepSeek researches deeply (RS057 BCH audit)
4. Collaborative debate in `/knowledge/colaborative_workspace/`
5. Consensus emerges
6. Implementation begins

**Insight:** Using multiple AI perspectives prevented groupthink and validated decisions.

---

*Archive created: 2026-05-16*  
*Last updated: 2026-05-16*  
*Lines: 295 / 400 limit*
