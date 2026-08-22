# Research Documents
**📖 Unfamiliar terms?** See the [glossary](../glossary.md) for definitions.

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

### Payment Rails & Automation
- **[RS042: Bizum Concept Field Constraints](RS042_bizum_concept_field_constraints.md)** — Testing that revealed Bizum concept field restrictions (critical for notification matching)
- **[RS072: Bizum Notification Patterns](RS072_notification_listener/)** — Notification parsing patterns for five Spanish banks (Phase -1 validation)
- **[RS073: NotifyFlow Comparison](RS072_notification_listener/RS073_notifyflow_decompilation.md/)** — Analysis of existing notification-based remittance app

### Volatility & Risk Management
- **[RS074: Dynamic Volatility Buffer](RS074_dynamic_volatility_buffer.md)** — Downside volatility-based dynamic buffer algorithm (Phase 1+ improvement over fixed 7%)
- **[RS039: Temporal Market Impact](RS039_temporal_market_impact.md)** — Remittance concentration on paydays and foreknowledge arbitrage strategy

### Future Tech
- **[RS053: MUSD](RS053_MUSD.md)** — Research on MUSD stablecoin for future integration
- **[RS054: CashToken Swaps](RS054_cashtoken_swaps.md)** — CashTokens as EUR commitment mechanism

### UX & Implementation
- **[RS081: Multi-Wallet Management Patterns](RS081_multi_wallet_management_patterns.md)** — Research on multi-wallet UX patterns from production BCH wallets (MetaMask, Trust Wallet, Bitcoin.com). Informed Asgaya's hybrid HD + imported key approach. Implemented August 2-3, 2026.
- **[RS083: Transaction Broadcast UI Patterns](RS083_transaction_broadcast_ui_patterns.md)** — Research from Selene/Paytaca on transaction state: navigate-on-success, DB persistence, rebroadcast on resume, `viewModelScope`. Implemented Aug 17-18; foundation of the v0.2 hybrid architecture (Aug 20-21).
- **[RS075: Android App Health Detection](RS075_android_app_health_detection.md)** — Detecting when wallet app is killed/frozen on Android

### Oracle & Data Feeds
- **[RS078: Oracle Over Nostr Prior Art](RS078_oracle_over_nostr_prior_art.md)** — Research on DLC oracles and Nostr-based oracle implementations

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
3. **Cross-reference** — Most research informed design decisions documented in [Why This Design?](../why-this-design/README.md)

---

## Contributing Research

New research documents should:
1. Use next sequential number (RS066, RS067, etc.)
2. Include date and author
3. Reference current covenant architecture (payment-first, H€/HAu, 7-day contracts)
4. Link to relevant documentation in [Why This Design?](../why-this-design/README.md) or [Unknowns](../unknowns/README.md)

---

*Research is iterative. These documents show the path to the current design, including dead ends and pivots.*

---

## Navigation

**[🏠 Home](../index.md)** | **[📖 Glossary](../glossary.md)**

**In this section:**
- [Research Summaries](summaries/README.md) - Research Summaries

**Related sections:** [Unknowns](../unknowns/README.md) · [Why This Design?](../why-this-design/README.md)
