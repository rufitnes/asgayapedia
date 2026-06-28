# AnyHedge Claim Contract Compatibility

**Status:** Not Started  
**Priority:** 🚨 **CRITICAL - BLOCKING FOR PHASE 0**  
**Last Updated:** 2026-06-21  
**Contributors Welcome:** Yes

---

## ⚠️ CRITICAL COMPLIANCE ISSUE IDENTIFIED

**P2PKH proxy workaround is NOT viable** - triggers full regulatory compliance:
- **Custody:** Proxy holds user funds (even temporarily) = custodian = MiCA CASP license required
- **Intermediation:** Proxy routes payments = payment intermediary = PSD2/MSB licenses required
- **Defeats Asgaya's core value proposition:** Compliance WITHOUT licensing

**Only acceptable paths:**
1. AnyHedge natively supports covenant payouts (needs validation)
2. Fork AnyHedge to add native support (MIT license, under our control)
3. Build custom mechanism from scratch (inspired by AnyHedge/StableHedge)

**All other workarounds either require licenses or unacceptable UX degradation.**

---

## What We Don't Know

**Can AnyHedge contracts pay out to BCH covenant (claim contract) addresses?**

Specifically:
- When merchant creates H€ AnyHedge contract, can the payout address be a covenant?
- When user burns H€ tokens, can settlement pay to a claim contract?
- Or must payouts go to standard P2PKH addresses only?

---

## Why It Matters

**This determines if H€/HAu mechanism is technically feasible.**

### Scenario 1: Covenant Abort (María Gets H€)
```
Covenant aborts (BCH drops >7%)
↓
Covenant needs to mint H€ and send to María
↓
QUESTION: Can covenant create AnyHedge contract where payout goes to María's wallet?
Or must María's wallet create the contract (requiring her to interact)?
```

**If AnyHedge requires María's signature:**
- Covenant abort can't auto-mint H€ (María not online)
- Falls back to BCH delivery (María exposed to volatility)
- Major UX degradation

### Scenario 2: Merchant Cashout (Carlos Gets H€)
```
Carlos receives BCH from Elena
↓
Carlos's wallet prompts: "Convert to H€?"
↓
QUESTION: Can wallet create AnyHedge contract where payout is covenant-locked?
Or does it require standard address?
```

**If AnyHedge requires standard addresses:**
- H€ tokens less secure (no covenant protection)
- Or: Need additional wrapper contract (complexity)

### Scenario 3: Token Burning (Return to BCH)
```
Merchant burns H€ tokens
↓
AnyHedge contract settles at maturity
↓
QUESTION: Can settlement pay to any BCH address type (P2PKH, P2SH, covenant)?
```

**If restricted to P2PKH only:**
- Limits smart contract integration
- May require intermediate steps

**Bottom line:** If AnyHedge doesn't support covenant addresses, we need workarounds (or can't use AnyHedge at all).

---

## Current Hypothesis

**AnyHedge contracts are flexible enough to support covenant payouts.**

**Reasoning:**
1. AnyHedge contracts are just BCH transactions
2. BCH supports multiple address types (P2PKH, P2SH, covenant)
3. Smart contracts should be able to pay to any valid address
4. No known restriction in AnyHedge documentation

**But:** We haven't tested this. Could be wrong.

**Fallback if wrong:**
- Use intermediate wallet (covenant → wallet → AnyHedge → wallet → covenant)
- Fork AnyHedge to support covenant addresses
- Find different hedging mechanism
- Abandon H€/HAu entirely

---

## Investigation Method

### Step 1: Read AnyHedge Documentation
- Review [AnyHedge technical specs](https://anyhedge.com)
- Find section on payout addresses
- Check: Any restrictions on address types?

**Deliverable:** Summary of AnyHedge address requirements

### Step 2: Review AnyHedge Smart Contract Code
- Find AnyHedge contract source code (GitHub)
- Read payout logic
- Check: Does it validate address type, or accept any valid BCH address?

**Deliverable:** Code snippet showing payout logic

### Step 3: Ask AnyHedge Community
- Post in [BitcoinCashResearch forum](https://bitcoincashresearch.org)
- Post in AnyHedge Telegram/Discord
- Question: "Can AnyHedge contracts pay to covenant addresses (CashScript)? Has anyone tested this?"

**Deliverable:** Community responses and expert opinions

### Step 4: Test on BCH Testnet
If no clear answer from docs/code/community:

**Test A: Simple Contract**
1. Create simple AnyHedge contract on testnet
2. Set payout to covenant address
3. Let contract mature
4. Check: Does payout execute successfully?

**Test B: Covenant-Initiated Contract**
1. Write CashScript covenant that creates AnyHedge contract
2. Deploy on testnet
3. Trigger covenant
4. Check: Does AnyHedge contract get created with covenant payout?

**Deliverable:** Testnet transaction IDs proving success/failure

### Step 5: Contact AnyHedge Developers
If tests fail or documentation unclear:
- Reach out to GeneralProtocols (AnyHedge developers)
- Explain Asgaya use case
- Ask: "Can you support covenant payout addresses? Or is there a workaround?"

**Deliverable:** Developer response and roadmap

---

## Success Criterion

**This unknown is answered when:**

1. ✅ **We know definitively:**
   - Can AnyHedge contracts pay to covenant addresses? (Yes/No)
   - If yes: Any limitations or special considerations?
   - If no: What are the alternatives?

2. ✅ **We have proof:**
   - Documentation citation, OR
   - Testnet transaction demonstrating it works, OR
   - Developer confirmation

3. ✅ **We can make architectural decision:**
   - If compatible: Proceed with H€/HAu design as documented
   - If not compatible: Implement workaround or find alternative
   - If uncertain: Build prototype to test before committing

**Answered = "AnyHedge [can/cannot] pay to covenant addresses, here's proof, here's our plan."**

---

## Contributor Guidance

**Skills needed:**
- BCH smart contract knowledge (CashScript, covenants)
- AnyHedge protocol understanding
- Testnet testing ability (optional, advanced)
- Research skills (documentation, forums)

**Estimated effort:** 2-4 hours (research), 4-8 hours (if testing required)

**How to start:**
1. Read [AnyHedge documentation](https://anyhedge.com)
2. Search for "payout address" or "settlement address"
3. Check [CashScript examples](https://cashscript.org) for AnyHedge integration
4. Post question in BitcoinCashResearch forum
5. Document findings in GitHub issue or email rufitnes@proton.me

**Quick win:** Even finding relevant documentation sections helps! Full testnet testing not required for initial contribution.

---

## Related Documents

- [Stability Layer Overview](../../the-mechanism/stability-layer/README.md)
- [How They Interact](../../the-mechanism/how-they-interact.md)
- [Sender Journey - Covenant Abort](../../user-journeys/sender/README.md#what-if-bch-price-crashes-during-transaction)

---

## Risk Assessment

**If this unknown is answered "No" (not compatible):**

**Impact:** High - Core H€/HAu mechanism may not work as designed

**Workarounds (ordered by preference):**
1. ~~**P2PKH proxy pattern (StableHedge approach)**~~ **❌ NOT VIABLE - TRIGGERS REGULATORY COMPLIANCE**
   - Covenant → P2PKH proxy wallet → AnyHedge contract → settlement to proxy → proxy to user
   - ~~Pro: Known to work (StableHedge uses this), standard AnyHedge compatible~~
   - **FATAL FLAW: Proxy wallet = CUSTODY + INTERMEDIATION**
     - Whoever operates proxy holds user funds (even temporarily) = **custodian**
     - Whoever routes payments through proxy = **payment intermediary**
     - **Triggers:** MiCA CASP license (EU), PSD2 Payment Institution (EU), MSB + state licenses (US), VASP registration (Spain)
     - **Defeats entire purpose of Asgaya** (compliance without licensing)
   - **This workaround is unacceptable** - violates core "no custody, no intermediation" requirement
   - Source: [RS069 StableHedge Analysis](../../../knowledge/research/RS069_stablehedge_analysis.md)
   
2. **Fork AnyHedge:** Modify to support covenant payouts natively ⭐ **PREFERRED PATH**
   - Pro: Clean architecture, best UX, maintains "no custody" principle
   - Con: Maintenance burden, security audit needed, oracle dependency
   - Note: MIT license allows this ([RS067 AnyHedge Fork Analysis](../../../knowledge/research/RS067_anyhedge_fork_analysis.md))
   - **Status:** Viable and under our control - testnet validation before Phase 0
   
3. **Build custom hedging mechanism:** Inspired by AnyHedge/StableHedge, designed for covenants
   - Pro: Full control, optimized for Asgaya, no proxy needed
   - Con: Significant R&D, security responsibility, need oracle infrastructure
   - **Status:** Viable if fork proves too complex - same testnet validation approach
   
4. **Intermediate wallet pattern:** Covenant pays to user wallet → wallet creates AnyHedge → settlement returns to wallet
   - Pro: User controls entire flow, no third-party custody
   - Con: Extra steps, user must be online during covenant abort (defeats automation), poor UX
   - **Status:** Fallback for sender-initiated minting only (not covenant abort auto-mint)
   
5. **Abandon H€/HAu:** Accept BCH volatility, focus on core remittance only
   - Pro: Simpler system, fewer unknowns
   - Con: Merchant retention problem unsolved, sender tail risk unprotected
   - **Status:** Last resort - volatility protection is critical for adoption

**This is why it's "Critical" priority - must answer before coding.**
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
