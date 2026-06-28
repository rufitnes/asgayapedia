# RS057: Bitcoin Cash Protocol Capabilities (Laila Upgrade)

**Research Type:** Protocol Capability Audit  
**Date:** May 2026 (Laila upgrade activation)  
**Status:** ✅ Complete — Technical foundation validated

**Full Research:** `/home/suso/Documents/asgaya/knowledge/research/RS057_bitcoincash_laila.md`

---

## TL;DR

**Bitcoin Cash supports covenants, CashTokens (fungible + NFT), Cash Accounts, loops, functions, and sub-penny fees. All technical primitives needed for Asgaya's covenant architecture exist and are production-ready.**

---

## Key Capabilities Validated

### 1. Native Introspection Covenants (May 2022)

**What it enables:** Covenant contracts that enforce how BCH can be spent
- Transaction can inspect its own inputs/outputs
- Enforce multi-signature releases
- Require specific OP_RETURN commitments
- Time-locked refunds

**Asgaya usage:**
- Payment-first covenant (merchant + recipient co-sign)
- Hash commitment verification (OP_RETURN in covenant output)
- Volatility buffer enforcement (7% overcollateralization)

### 2. CashTokens (May 2023)

**Fungible tokens:**
- Used for: H€/HAu stability layer, MUSD integration
- Amount: Up to 9.2 quintillion units (64-bit)
- Native DEX support: Cauldron, FexCash

**NFTs:**
- Commitment field: 128 bytes (increased in P2S upgrade)
- Capabilities: immutable, mutable, minting
- Used for: Bulletin board (on-chain seller listings), state storage in covenants

**Critical feature:** UTXO can hold fungible tokens + NFT simultaneously

**Asgaya usage:**
- NFTs as bulletin board listings (seller capacity, fees, payment methods)
- H€/HAu tokens for merchant stability
- Potential reputation tokens (non-transferable fungibles)

### 3. Cash Accounts (May 2022)

**What it is:** Human-readable on-chain names (e.g., `Elena#142`)
- Registered via OP_RETURN transaction
- Maps to BCH address
- Collision resistance via sequential numbering

**Asgaya usage:**
- Replaces phone numbers as identity layer
- Appears on bank statements without triggering fraud alerts
- Foundation for permissionless identity (no central registry)

### 4. May 2026 Upgrade (Laila)

**New capabilities just activated:**

| Feature | Impact |
|---------|--------|
| **P2S (Pay-to-Script)** | Standardized covenant outputs; 128-byte NFT commitments |
| **Loops** | Bounded iteration in contracts (OP_BEGIN/OP_UNTIL) |
| **Functions** | Reusable contract logic (OP_DEFINE/OP_INVOKE) |
| **Bitwise ops** | Efficient cryptographic primitives |

**Asgaya relevance:**
- Smaller covenant bytecode (functions reduce duplication)
- More state in NFT commitments (128 bytes vs 40)
- Lower fees (more efficient contracts)

---

## Relevance to Asgaya

### Implementation Decisions Validated

1. **[Payment-First Covenants](../../glossary.md#payment-first-covenant)**
   - Native introspection proves covenant architecture is viable
   - Multi-signature release patterns exist and are production-tested
   - CashScript toolchain mature (Bitauth IDE for testing)

2. **[Bulletin Board](../../the-mechanism/bulletin-board/README.md)**
   - NFTs as on-chain seller listings validated
   - 128-byte commitment sufficient for all listing data
   - ~€0.0002 per listing update (bundled with covenant funding)

3. **[Cash Accounts](../../glossary.md#cash-account)**
   - Human-readable identity layer proven viable
   - Used in production by other BCH projects
   - Collision resistance adequate for Asgaya scale

4. **[Stability Layer](../../the-mechanism/stability-layer/README.md)**
   - CashTokens (H€/HAu) are native, not wrapped
   - DEX support exists (Cauldron for BCH ↔ MUSD swaps)
   - AnyHedge contracts use CashTokens as settlement layer

---

## Technical Risks & Limitations

**Covenant complexity:**
- Debugging requires Bitauth IDE + chipnet testing
- Smart contract bugs can lock funds permanently
- Formal verification recommended before mainnet

**CashToken liquidity:**
- H€/HAu pools require sufficient bull pool capital
- DEX slippage at low liquidity
- MUSD availability limited to Cauldron (€1.6M TVL)

**Cash Account adoption:**
- Requires on-chain registration transaction (~€0.0002)
- Not universally adopted yet (still niche)
- Collision numbering can be confusing to users

---

## Bottom Line

**All technical primitives needed for Asgaya exist on BCH mainnet today.** No protocol upgrades needed, no waiting for features. The May 2026 Laila upgrade adds efficiency improvements (smaller contracts, more state storage) but doesn't change fundamental feasibility.

**Primary technical risk is smart contract implementation quality, not protocol capability.**

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Summaries](README.md)** | **[📖 Glossary](../../glossary.md)**
