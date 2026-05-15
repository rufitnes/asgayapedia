# Phase 0: Progressive Decentralization Strategy

**Status:** ✅ Decided  
**Date:** 2026-05-15  
**Decision Makers:** Suso + Claude Sonnet 4.5 + DeepSeek  
**Context:** How to build Asgaya Husk and Phase 0 given that covenant contracts may not be ready when UI testing begins.

---

## The Question

Should Asgaya Husk (Phase 0 testing version) use:
- **Position A:** No backend (mobile queries Electrum directly from day one)
- **Position B:** Mock backend (faster iteration, controlled test data)
- **Position C:** Hybrid chipnet (real blockchain with helper scripts)

---

## The Decision

**Progressive Decentralization: B → C → A**

Start with mock (Position B), transition to chipnet (Position C), launch with no backend (Position A).

**Critical constraint:** The mock backend must be **structurally identical** to the Electrum JSON-RPC API. No custom REST endpoints, no business logic. Just a local server returning Electrum-shaped responses.

---

## Rationale

### The Chicken-Egg Problem

**"The covenant contracts are the database schema. Until they exist, there is no blockchain state to query."**

You cannot validate a bulletin board UI against an empty blockchain. Testing Screen 4.5 (seller selection) requires either:
1. Deployed covenant contracts + seeded NFTs on chipnet, OR
2. A mock that simulates the same data structure

Position A (pure Electrum from day one) only becomes viable **after** covenant contracts are deployed and seeded. Before that, it's a blank screen.

### Why This Isn't "Throwaway Work"

The mock backend implements the **Electrum JSON-RPC interface exactly**. The mobile app code that queries it is **production code from day one**. When covenants are deployed on chipnet, you change one config value (server URL) and the same code runs against real blockchain state.

```javascript
// Week 1: Husk v0.1
const electrum = new ElectrumClient('192.168.1.42', 50002);  // pichan mock
const sellers = await electrum.getUTXOsByCategory('ASGAYA_SELLER_V1');

// Week 2: Husk v0.2
const electrum = new ElectrumClient('chipnet.server', 50002);  // chipnet
const sellers = await electrum.getUTXOsByCategory('ASGAYA_SELLER_V1');

// Week 3: Phase 0
const electrum = new ElectrumClient('fulcrum.fountainhead.cash', 50002);  // mainnet
const sellers = await electrum.getUTXOsByCategory('ASGAYA_SELLER_V1');
```

**Zero mobile app code changes.** Only the endpoint URL changes.

The mock server itself (~50 lines of Node.js) serves JSON fixtures that become permanent test data for CI/CD. Nothing is wasted.

---

## The Progression

| Phase | When | Backend | Mobile App Connects To | Purpose |
|-------|------|---------|------------------------|---------|
| **Husk v0.1** | Week 1-2 | **Position B** - Mock Electrum on pichan | `192.168.1.42:50002` | Validate UI flows rapidly (10+ iterations/day). Test multi-device scenarios. |
| **Husk v0.2** | Week 2-3 | **Position C** - Chipnet Electrum | `192.168.1.42:60002` or chipnet server | Validate covenant contracts, NFT scanning, real blockchain latency. Share on bitcoincashresearch.org. |
| **Phase 0** | Week 3-4 | **Position A** - No backend | `fulcrum.fountainhead.cash:50002` | Real BCH, 3-5 trusted testers, fully decentralized. |

---

## Multi-Device Testing Setup

### Hardware:
- **susopc** - Development laptop (build Husk app)
- **pichan** - Raspberry Pi 5 8GB with SIM hat (mock server, then seller bot)
- **Pixel 6a** - Smartphone with SIM (sender testing)
- **Motorola G06** - Smartphone with SIM (recipient/merchant testing)

### Week 1-2: Husk v0.1
**Pichan runs real Electrum server (Fulcrum) indexing regtest blockchain:**
- Bitcoin Cash Node in regtest mode (instant block generation)
- Real ASGAYA_SELLER NFTs (deployed via CashScript)
- Real ASGAYA_MERCHANT NFTs (on-chain)
- Real covenant UTXOs (actual CashScript contracts)
- Real Cash Account OP_RETURNs (Elena#142 registered on-chain)

**Both phones query pichan** over local WiFi:
- Real Electrum protocol (not simulated)
- Realistic network latency (WiFi round-trip)
- Multi-device scenarios (sender on Pixel, recipient on Moto)
- Same hardware used for smsbridge_loop.py testing (proven reliable)

### Week 2-3: Husk v0.2
**Pichan proxies to chipnet Electrum**, returns real blockchain state:
- Covenant contracts deployed on chipnet
- Helper scripts seed test NFTs (`chipnet-seed.sh`)
- Validates covenant logic, NFT encoding, Cash Account resolution

### Week 3-4: Phase 0
**Phones query public mainnet Electrum servers**:
- No dependency on pichan (except as one of the BCH sellers)
- Fully decentralized architecture validated
- Pichan runs seller bot (smsbridge_loop.py with SIM hat)

---

## Preventing Confusion

### Husk UI Labeling
Every screen in Husk v0.1 displays a translucent banner:
```
🔶 MOCK DATA — Production uses BCH blockchain directly
```

### Documentation Clarity
Husk README states explicitly:
> "Husk v0.1 uses a local mock Electrum server to simulate blockchain state for UI testing. The production app queries public BCH Electrum servers directly with no intermediary."

Bitcoincashresearch.org reviewers are technically sophisticated and will understand this progression immediately.

---

## Why Progressive Decentralization Works

**From DeepSeek's analysis:**
> "The team that builds a mock backend 'just for testing' without asking whether it corrupts the architecture is the team that accidentally ships a centralized system. The team that refuses to build any mock and insists on pure blockchain queries from day one is the team that ships late and tests in production. You're doing neither."

**This progression:**
- ✅ Validates UI flows **before** covenant contracts are ready (parallel development)
- ✅ Tests mobile Electrum client against **production-shaped** responses (no false confidence)
- ✅ Catches integration issues **early** (mock → chipnet transition reveals mismatches)
- ✅ Ends with **zero backend** (fully decentralized mainnet launch)

---

## What Gets Built

### Real Electrum Server on Regtest (Week 1)
```
pichan:~/asgaya-regtest/
├── bitcoind.conf               # BCH Node regtest config
├── fulcrum.conf                # Fulcrum Electrum server config
├── scripts/
│   ├── regtest-seed.sh         # Deploy NFTs, covenants, Cash Accounts
│   ├── regtest-reset.sh        # Wipe regtest, start fresh
│   └── scenarios/
│       ├── 01-empty-bulletin.sh      # Deploy zero sellers
│       ├── 02-one-seller.sh          # Deploy one seller NFT
│       ├── 03-competitive.sh         # Deploy 5 competing sellers
│       └── 04-full-flow.sh           # Complete sender→recipient→merchant
├── contracts/
│   ├── SellerLiquidityV1.cash       # CashScript seller covenant
│   ├── RecipientCovenantV1.cash     # Recipient covenant
│   └── compiled/                     # Compiled bytecode
└── README.md                         # Setup guide
```

### Mobile App Electrum Client (Production Code)
```javascript
// shared/electrum.ts
export class ElectrumClient {
  constructor(host: string, port: number, protocol: 'tcp' | 'ssl') {
    // Connect to Electrum server (mock, chipnet, or mainnet)
  }

  async getUTXOsByCategory(category: string) {
    // Query blockchain for NFTs
    // Works with mock, chipnet, and mainnet (same API)
  }

  async getCashAccountAddress(cashAccount: string) {
    // Resolve Elena#142 → BCH address
  }
}
```

### Chipnet Helper Scripts (Week 2)
```bash
scripts/
├── chipnet-seed.sh          # Create test NFTs on chipnet
├── chipnet-reset.sh         # Burn test data, start fresh
└── chipnet-scenarios/       # Reproducible test scenarios
```

---

## Alignment with Existing Decisions

| Previous Decision | Alignment |
|-------------------|-----------|
| **No custody** (core-regulatory-constraints.md) | ✅ Even mock server holds no funds, signs no transactions |
| **Permissionless** (why-no-kyc.md) | ✅ Mock simulates permissionless bulletin board (anyone can be seller) |
| **Bitcoin wallet positioning** (ui-language-regulatory-implications.md) | ✅ Mobile app is a BCH wallet from day one (Electrum client) |
| **Pull system** (overcollateralized-bounty-contracts.md) | ✅ Covenant states in mock match production covenant logic |

---

## Success Criteria

### Husk v0.1 (Mock)
- [ ] All sender screens (1-7) navigable with realistic data
- [ ] All recipient screens (1-4) navigable
- [ ] All merchant screens (1-4) navigable
- [ ] Multi-device testing works (sender/recipient on different phones)
- [ ] UI mockups match final design

### Husk v0.2 (Chipnet)
- [ ] Covenant contracts deployed on chipnet
- [ ] Mobile app queries chipnet Electrum successfully
- [ ] NFT scanning returns correct seller/merchant data
- [ ] Cash Account resolution works
- [ ] No code changes needed from v0.1 (just config)

### Phase 0 (Mainnet)
- [ ] Mobile app queries public Electrum servers
- [ ] 3-5 trusted testers complete real transactions
- [ ] Zero backend dependency confirmed
- [ ] Seller bot runs on pichan with real SIM
- [ ] No issues with decentralized architecture

---

## Review Triggers

Re-evaluate this decision if:
- [ ] Covenant contracts ready in Week 1 (could skip mock, go straight to chipnet)
- [ ] Mock → chipnet transition requires mobile code changes (mock wasn't Electrum-shaped)
- [ ] Chipnet Electrum unavailable or unreliable (need local mock longer)
- [ ] Reviewers confused by mock despite labeling (communication issue)

---

## Related Documents

- [Backend-APIs Architecture Overhaul](../android-app/backend-apis/README.md) - Updated for NFT-based bulletin board
- [Overcollateralized Bounty Contracts](../concepts/overcollateralized-bounty-contracts.md) - Covenant architecture
- [UI Language Regulatory Implications](./ui-language-regulatory-implications.md) - Bitcoin wallet positioning
- [Phase 0 Validation Checklist](./phase-0-validation-checklist.md) - Testing criteria

---

## Collaborative Decision Process

This decision emerged from a three-way debate:
1. **Claude** initially proposed minimal backend (Electrum proxy + caching)
2. **User (Suso)** suggested mock backend for faster testing
3. **DeepSeek** analyzed both positions and recommended B→C→A progression

**Key insight from DeepSeek:**
> "The covenant contracts are the database schema. Until they exist, there is no blockchain state to query. The mock is not a deviation from the architecture, it's a tool for building it faster."

**Consensus:** Progressive decentralization is standard engineering practice for decentralized protocols. Build the scaffold, validate against it, then remove it and run against the real thing.

---

## Version History

- **2026-05-15:** Initial decision - B→C→A progression with Electrum-shaped mock
- **2026-05-15:** Added multi-device testing setup (pichan + 2 phones)

---

*Decision document based on collaborative debate in: `/knowledge/colaborative_workspace/backend_apis/`*  
*Contributors: Suso, Claude Sonnet 4.5, DeepSeek*
