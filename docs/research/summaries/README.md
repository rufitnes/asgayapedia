# Research Summaries
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**Purpose:** Distilled findings from research sessions relevant to experimental documentation.

**Full research archive:** `/home/suso/Documents/asgaya/knowledge/research/`

---

## What Goes Here

**Key research summaries that support:**
- Requirements (what constraints did we discover?)
- Constraints (what data validates our design trade-offs?)
- Evidence (what backs up our claims?)

**Format:**
- Extract key findings only
- Link to full research document
- Explain relevance to constraints/requirements
- Note caveats and limitations

---

## Available Summaries

### RS062: Seller Profitability Simulation
**Key Finding:** 7% buffer achieves 99.45% success rate in 4-hour claim windows

**Relevance:**
- [7% Volatility Buffer](../why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md) - Validates buffer adequacy
- Money velocity assumptions (fast capital recycling)

**Full Research:** [RS062_seller_profitability_simulation.md](../../../asgaya/knowledge/research/RS062_seller_profitability_simulation.md)

---

### RS039: Temporal Market Impact
**Key Finding:** Remittances concentrate on paydays; at €500K volume, Asgaya dominates BCH weekend markets

**Relevance:**
- [7% Volatility Buffer](../why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md) - Payday stress test
- Foreknowledge arbitrage strategy (Phase 1+)
- Capital efficiency under concentration

**Full Research:** [RS039_temporal_market_impact.md](../../../asgaya/knowledge/research/RS039_temporal_market_impact.md)

---

## Adding New Summaries

**When to add:**
- Research validates a constraint or requirement
- Data answers an open question
- Evidence backs up a design decision

**What to include:**
1. Key finding (one sentence)
2. Relevance to constraints/requirements
3. Link to full research
4. Caveats (what doesn't it prove?)

**What NOT to include:**
- Full research sessions (link to main archive instead)
- Exploratory research without conclusions
- Superseded findings (archive, don't summarize)

---

## TODO: Summaries Needed

**Research referenced in implementation docs but not yet summarized here:**

### RS026: Android Notifications Architecture
**Referenced in:** `/implementation/android-app/README.md`
**Why needed:** Validates Notification Listener Service approach (bank app notifications > SMS parsing)
**Full research:** `/knowledge/research/RS026_android_notifications.md`

### RS057: Bitcoin Cash & Cash Accounts (Laila)
**Referenced in:** `/implementation/android-app/README.md`
**Why needed:** Validates Cash Account implementation, BCH protocol details
**Full research:** `/knowledge/research/RS057_bitcoin_cash_laila.md`

**Action:** Create evidence summaries (extract key findings, relevance to implementation decisions)

---

**Status:** Active - summaries added as experimental documentation needs them  
**Maintained by:** Documentation contributors

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Research](../README.md)** | **[📖 Glossary](../../glossary.md)**
