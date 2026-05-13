# Why: BCH Usage Incentive (Recipients)

**Sub-requirement of:** [Why: Promote Adoption](core-architecture/why-promote-adoption.md)

**Core Requirement:** Recipients must have a reason to keep received funds in BCH rather than immediately cashing out to fiat.

---

## The Problem

### Building "Better Western Union" Isn't the Goal

If 100% of recipients instantly convert BCH to fiat, Asgaya is just another payment rail—cheaper than Western Union, but still trapping users in the legacy banking system.

**This solves nothing long-term:**
- Users still depend on banks for day-to-day spending
- Merchants still deal with bank fees, chargebacks, account freezes
- The banking system still controls who can transact
- We've built a better trap, not freedom

**The actual goal:** Create economic incentives for recipients to hold and spend BCH, bootstrapping a parallel economy where banking system problems don't exist.

---

## Why Recipients Holding BCH Matters

### 1. Breaking the Chicken-and-Egg Problem

**The cycle that kills crypto adoption:**
- Users don't hold BCH → Merchants don't accept it (no customers)
- Merchants don't accept BCH → Users don't hold it (nowhere to spend)

**Traditional approach (ideology):**
"BCH is fast and cheap! Everyone should use it!"
**Result:** 99.9% of merchants ignore it because 99.9% of customers don't have it.

**Asgaya's approach (economics):**
Every remittance creates:
1. A merchant who accepts BCH (earning passive income from remittances)
2. A recipient who received BCH (and sees merchants who accept it)
3. An economic reason to hold BCH (lower fees on repeat spending)

**Result:** Self-reinforcing cycle where each transaction makes the next one more likely.

### 2. Network Growth Requires Retention

**If recipients cash out immediately:**
- 1,000 remittances = 1,000 fiat conversions
- Network size: Still 0 BCH holders
- Growth: None

**If 30% of recipients hold BCH:**
- 1,000 remittances = 300 new BCH holders
- Each holder sees 5-10 local merchants (from remittance network)
- Those holders spend BCH locally (near-zero fees)
- Merchants see more BCH customers, promote acceptance
- **Network compounds**

**The math is simple:** We're not building Asgaya to process remittances forever. We're building it to bootstrap BCH adoption, then become redundant.

---

## The Economic Case

### Scenario: Recipient in Venezuela Receives 5 Transfers/Month

**Option A: Cash out every transfer (stay in fiat system)**
```
Transfer 1: €100 - 1% fee = €99 cash
Transfer 2: €100 - 1% fee = €99 cash  
Transfer 3: €100 - 1% fee = €99 cash
Transfer 4: €100 - 1% fee = €99 cash
Transfer 5: €100 - 1% fee = €99 cash

Total received: €500
Total fees paid: €5 (1% × 5 transactions)
Net: €495
```

**Option B: Hold BCH, spend at local merchants**
```
Transfer 1: €100 → 0.260 BCH (held)
Transfer 2: €100 → 0.260 BCH (held)
Transfer 3: €100 → 0.260 BCH (held)
Transfer 4: €100 → 0.260 BCH (held)  
Transfer 5: €100 → 0.260 BCH (held)

Total received: 1.300 BCH (€500 equivalent)
Spending fees: 5 BCH transactions × ~$0.01 = $0.05 total
Net: €499.95 equivalent
```

**Savings by holding BCH: €4.95/month = €59.40/year**

For someone receiving €500/month in remittances, that's 12% more value by simply spending BCH instead of cashing out to fiat.

### The Compounding Effect

**But wait, there's more:**

If recipient holds BCH and local merchants accept it:
- No bank account required (saves account fees)
- No risk of account freezes or capital controls
- No currency devaluation (BCH is global, not subject to local inflation)
- Instant settlement (no waiting days for bank transfers)
- Can send money to family/friends at near-zero cost

**The value proposition writes itself.**

---

## Why BCH Specifically?

### Practical Requirements for Remittance Settlement

**What Asgaya needs from a settlement layer:**
1. **Low transaction fees** (<$0.01 per transaction, consistently)
2. **Fast confirmations** (seconds for 0-conf, minutes for final settlement)
3. **Proven reliability** (years of operation without critical failures)
4. **Smart contract capabilities** (for covenants, CashTokens, on-chain communication)
5. **Sufficient liquidity** (fiat pairs on major exchanges)

**Why BCH:**

Bitcoin Cash delivers all requirements today, with a proven track record:
- **Fees:** ~$0.001-0.01 per transaction (proven over years)
- **Speed:** Instant for 0-conf merchant payments, ~10min for settlement
- **Reliability:** 7+ years of continuous operation, survived multiple challenges
- **Smart contracts:** Covenants and CashTokens enable advanced settlement patterns
- **Liquidity:** EUR/BCH, USD/BCH pairs on Kraken and other exchanges

**More importantly:** Trust in continued delivery. BCH has consistently shipped the features Asgaya needs (smart contracts, low fees, reliability improvements) without breaking existing functionality.

### Why Not Alternatives?

**Stablecoins (USDC, USDT):**
- Require trust in central issuer (defeats permissionless requirement)
- Subject to regulatory freezes and account blacklists
- No smart contract flexibility for complex settlement

**Other cryptocurrencies:**
- Most have significantly higher fees (disqualifies them for micro-payments)
- Or lack the smart contract features Asgaya requires
- Or haven't proven reliability at scale over years

**The choice is pragmatic, not political:** BCH has the features Asgaya needs, working today, with a community that ships improvements we can build on (covenants, CashTokens, network communication primitives).

---

## Why This Matters for Adoption

### The Vision: Asgaya Becomes Redundant

**Year 1:**
- Users send remittances via Asgaya (cheaper than Western Union)
- Recipients cash out to fiat (old habits)
- Merchants earn passive income from remittances

**Year 2:**
- Same remittance volume, but 30% of recipients hold BCH
- Recipients discover they can pay 10+ local merchants in BCH (near-zero fees)
- Merchants see BCH customers, promote acceptance

**Year 3:**
- 50% of recipients hold BCH long-term
- New merchants join just to accept BCH (not for remittance income)
- Recipients prefer BCH-to-BCH transfers (why pay 1% when you can pay $0.01?)

**Year 5:**
- Most transactions are BCH-to-BCH (not remittances)
- Asgaya's remittance feature rarely used (only for new users onboarding)
- **Mission accomplished:** Created a parallel economy where banking system problems don't apply

**This is the endgame.** Remittances are the kindling. BCH adoption is the fire.

---

## The Honest Pitch to Recipients

**We're not asking you to "believe in crypto."**

**We're asking you to do the math:**
- Hold €500 in fiat bank account: Pay fees, deal with banks, lose purchasing power to inflation
- Hold €500 equivalent in BCH: Spend at 10+ local merchants, near-zero fees, no bank required

**Which costs less? Which gives you more control?**

The answer is obvious once merchants exist. Asgaya's job is to create those merchants by making remittances fund their onboarding.

---

## Related Requirements

- [Why: Promote Adoption](core-architecture/why-promote-adoption.md) — The full adoption flywheel
- [Why: Cheaper Than Legacy](core-architecture/why-cheaper-than-legacy.md) — The fee savings that fund the incentives
- [Why: Permissionless](core-architecture/why-permissionless.md) — Why self-custody and no KYC matter for this vision

---

## Trade-offs and Decisions

See the **Decisions** section for how we achieve this:

- **[Fee Splitting Model](decisions/fee-splitting-model.md)** — Why recipients holding BCH changes the fee calculation
- **[Two-Step Settlement Timing](decisions/two-step-settlement-timing.md)** — How settlement creates the merchant network

---

## The Bottom Line

**Technical merit alone hasn't driven widespread adoption.**

**Clear economic incentives create a stronger foundation.**

Give recipients a clear financial reason to hold BCH (lower fees + more merchants), and they will. Give merchants a clear financial reason to accept BCH (passive remittance income), and they will.

**The network grows because participants are better off financially, not because they believe in a vision.**

---

*Last updated: May 1, 2026*
*Core principle: "We're not building better Western Union. We're building the on-ramp to a parallel economy."*
