# Research Documents

Research documents supporting Asgaya's design decisions.

---

## About This Directory

Research documents are numbered sequentially (RS###) and represent exploratory work, API testing, market analysis, and technical feasibility studies conducted during Asgaya's development.

**Document numbering:** RSxxx (Research Study #xxx)

---

## Important Historical Context

**Architecture Pivot (May 9, 2026):** Asgaya pivoted from an escrow-based model to a covenant-based model for MiCA compliance (avoiding CASP licensing).

**What this means for research docs:**

### Pre-Pivot Documents (RS016-RS045)
Documents created before May 9, 2026 may reference concepts from the **old escrow architecture**:
- Liquidity Providers (LPs) instead of BCH Sellers
- Escrow settlement instead of covenants
- 1/3 fee splits (sender, merchant, LP) instead of 2-way splits (BCH seller 0.5%, merchant 0.5%)
- Different state machine flows

**These documents are retained for historical reference** and to show the research that informed later decisions, but may not accurately reflect current architecture.

### Post-Pivot Documents (RS047+)
Documents from May 2026 onward reflect the **current covenant architecture**:
- BCH Sellers (not LPs)
- Covenant + volatility buffers (107%)
- EUR-denominated cash buy orders, BCH settlement
- 24-hour timeout cascade
- 2-way fee split

---

## Key Research Documents (Current Architecture)

### Exchange Rates & Markets
- **[RS047: DolarAPI Venezuela Rates](RS047_dolarapi_venezuela_rates.md)** — Parallel market VES rates from DolarAPI. Used for EUR↔VES conversion in current architecture.
- **[RS036: Kraken Ticker API](RS036_kraken_ticker_api.md)** — BCH/EUR rates from Kraken (used in fee calculations)
- **[RS041: Cross-Corridor Exchange Rates](RS041_cross_corridor_exchange_rates.md)** — How exchange rates work across corridors (still relevant)

### Payment Rails
- **[RS042: Bizum Concept Field Constraints](RS042_bizum_concept_field_constraints.md)** — Testing that revealed Bizum concept field restrictions (critical for notification matching)

### Future Tech
- **[RS053: MUSD](RS053_MUSD.md)** — Research on MUSD stablecoin for future integration
- **[RS054: CashToken Swaps](RS054_cashtoken_swaps.md)** — CashTokens as EUR commitment mechanism

---

## Historical Documents (Pre-Pivot)

The following documents describe the **old escrow architecture** and are retained for historical reference:

- **[RS016: EUR/BCH Exchanges](RS016_EUR_BCH_exchanges.md)** — Analysis of DEX options (informed decision to use Kraken)
- **[RS017: Kraken Overview](RS017_kraken.md)** — Kraken exchange research
- **[RS018: Kraken Setup](RS018_kraken_setup.md)** — Kraken account setup (escrow operator context)
- **[RS019: Kraken Query](RS019_kraken_query.md)** — Kraken API testing
- **[RS044: Kraken Trading/Withdrawal](RS044_kraken_trading_withdrawal.md)** — Automated trading research (escrow model)
- **[RS045: Kraken Complete Fee Analysis](RS045_kraken_complete_fee_analysis.md)** — Fee structure (escrow model)

**Why kept?** These documents show the research process and constraints that informed the eventual covenant architecture.

---

## Other Research

- **[RS010: Honduras](RS010_Honduras.md)** — Early corridor research (archived)

---

## How to Read Research Docs

1. **Check the date** — Documents from before May 9, 2026 may reference old architecture
2. **Look for terminology** — "LP" = old, "BCH Seller" = current; "escrow" = old, "covenant" = current
3. **Cross-reference decisions/** — Most research informed specific decisions documented in `/decisions/`

---

## Contributing Research

New research documents should:
1. Use next sequential number (RS055, RS056, etc.)
2. Include date and author
3. Reference current covenant architecture
4. Link to relevant `/decisions/` docs if research informs a decision

---

*Research is iterative. These documents show the path to the current design, including dead ends and pivots.*
