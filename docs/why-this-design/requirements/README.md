# Requirements: What Asgaya Must Achieve
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**The ultimate goal:** Bitcoin Cash merchant adoption

**Why remittances?** A niche where we can extract value from legacy providers (5-8% fees) and redirect it to merchants as adoption incentive.

---

## The Three Core Requirements

### 1. Permissionless

**No central authority, anyone can participate**

**What this means:**
- No custody (users control their keys)
- No KYC (peer-to-peer coordination)
- No central server (bulletin board on BCH blockchain)
- Autonomous execution (covenants handle settlement)

**Why it matters:**
- Censorship resistance (no entity can shut down)
- Global accessibility (works for unbanked populations)
- Permissionless innovation (anyone can build clients)

**Design choices driven by this:**
- Payment-first covenants (no funds locked by intermediary)
- Nostr for coordination (encrypted P2P messaging)
- NFTs on BCH blockchain as bulletin board (no central database)
- Cash Accounts as universal identifier (no phone number registry)

---

### 2. Compliance

**Legal to operate without MSB licenses**

**What this means:**
- No custody = no CASP license (MiCA)
- No intermediation = no Payment Institution license (PSD2)
- No money transmission = no MSB registration (FinCEN)
- Clear transaction separation (remittance vs commerce)

**Why it matters:**
- Regulatory sustainability (can't be ordered to shut down)
- Global operation (no jurisdiction restrictions)
- Permissionless participation (no entity needs license to provide infrastructure)

**Design choices driven by this:**
- Payment-first covenants (Isabel receives fiat BEFORE funding covenant)
- Two-transaction model (María → Elena remittance, Elena → Carlos commerce)
- Bank traceability (Bizum includes full identity, legal recourse via courts)
- H€/HAu restricted minting (utility tokens at covenant endpoints, not money substitutes)
- ⚠️ **NO P2PKH proxy patterns** (custody + intermediation = licensing required)

**This was the missing requirement** that, once identified, made everything click into place. Payment-first covenants, H€/HAu compliance framing, Nostr blacklist - all driven by compliance constraints.

---

### 3. Price Stability 

**Merchants need certainty, not ±20% swings**

**What this means:**
- Merchants can stabilize received BCH (H€/HAu tokens)
- Senders protected from covenant abort tail risk (H€ minting option)
- Recipients can cash out quickly (merchant network for liquidity)
- BCH as fallback (system works even if stability layer exhausted)

**Why it matters:**
- Merchant retention (volatility kills adoption within weeks)
- Sender protection (didn't sign up for 7% loss if covenant aborts)
- Network effects (stable value increases circulation velocity)

**Design choices driven by this:**
- H€/HAu stability tokens (AnyHedge-backed, auto-renewing)
- 7% volatility buffer in covenants (absorbs typical price swings)
- Bull pool structure (Suso provides long positions Phase 0)
- Merchant auto-stabilization (convert BCH → H€/HAu on receipt)

---

## Sub-Requirements

Each core requirement has specific implementation constraints:

### Permissionless Sub-Requirements
- Offline-capable (intermittent connectivity supported)
- Minimal hardware (RFID cards for recipients, smartphones for merchants)
- Minimal knowledge (simple UI, no crypto jargon)
- Self-custody education (mandatory backup verification)

### Compliance Sub-Requirements
- No custody at any point (including temporary/intermediary)
- No intermediation (even automated)
- Clear legal standing (bank transfers traceable, court precedent)
- **Market-rate exchanges only** (no hidden markups, verifiable rates)
  - Users can verify rates match public market data (CoinGecko, DolarAPI)
  - Zero markup on EUR/BCH or BCH/VES exchanges
  - Freedom on convenience, ZERO flexibility on exchange rates
  - Prevents replication of legacy extraction (Western Union 2-3% hidden markups)

### Volatility Protection Sub-Requirements
- Multiple stability options (H€, HAu, BCH - user choice)
- Automatic stabilization (merchant bot handles conversion)
- Quick cash-out paths (merchant liquidity network)
- Graceful degradation (BCH fallback if pool exhausted)

---

## How Requirements Drive Design

**Example 1: Payment-First Covenants**
- **Permissionless:** No custody (Isabel never locks funds before receiving payment)
- **Compliance:** No intermediation (no entity holds funds between parties)
- **Result:** Counterintuitive but critical design choice

**Example 2: H€/HAu Tokens**
- **Volatility Protection:** Merchants get stable value
- **Compliance:** Multiple assets (EUR, gold) prove "utility tool, not money"
- **Permissionless:** Bull pool operator chooses assets (Asgaya doesn't dictate)
- **Result:** Legal defense architecture + product feature

**Example 3: Nostr Blacklist**
- **Permissionless:** No central authority controls blacklist (user bots post independently)
- **Compliance:** Discourages fraud without becoming intermediary
- **Result:** Distributed reputation without central database

---

## What's NOT a Core Requirement

**Speed:** 
- Not a requirement (5 min to 4 hours is acceptable)
- Faster than Western Union (1-2 days) anyway
- Recipient's decision delay, not technical limitation

**Cost:**
- Important but derivative (permissionless + no intermediation = low cost)
- <1% achieved by market rates + no markup
- Result of architecture, not primary driver

**Scalability:**
- Important but premature (Phase 0 validates demand first)
- BCH handles volume (10-minute blocks, low fees)
- Bulletin board scales via NFT pruning (later optimization)

---

## History & Evolution

**Key milestones:**
- February 2026: Initial requirements identified (flawed, missing compliance)
- April 2026: Compliance requirement added (everything clicked)
- May-June 2026: Volatility protection elevated to core (H€/HAu development)
- June 2026: 3-core framework finalized (Permissionless, Compliance, Volatility)

---

## Success Criteria

**This architecture succeeds when:**

1. ✅ **Permissionless achieved:**
   - Anyone can participate without permission
   - No entity can shut down the protocol
   - Users maintain custody throughout

2. ✅ **Compliance achieved:**
   - No licenses needed to operate infrastructure
   - Legal to use in Spain, Venezuela, and target corridors
   - Regulatory risk minimized

3. ✅ **Volatility protection achieved:**
   - Merchants retain 80%+ after 6 months
   - Senders protected from covenant abort losses
   - H€/HAu tokens circulate (not just held)

**Phase 0 validates:** Do these requirements translate to real-world adoption?

---

## Related Documents

- [Constraints](../constraints/) - What design decisions were forced by reality
- [Research Summaries](../../research/summaries/) - Research supporting these requirements
- [Fraud Protection](../fraud-protection.md) - How payment-first covenants satisfy compliance
- [Stability Layer](../../the-mechanism/stability-layer/) - How H€/HAu satisfy volatility protection

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Why This Design?](../README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Constraints](../constraints/README.md)
