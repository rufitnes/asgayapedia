← [Back to Concepts](README.md)

# BCH Miners as Escrows (Historical)

**Status:** ⚠️ **Obsolete - Renamed**  
**Date:** Originally April 2026, superseded May 10, 2026  
**Replaced by:** [BCH Sellers](bch-sellers.md)

---

## Notice

This concept has been **renamed and updated** to reflect the covenant-based architecture (May 10, 2026 pivot).

**Old name:** BCH Miners as Escrows  
**New name:** [BCH Sellers](bch-sellers.md)

**What changed:**
- **Architecture:** Escrow model → Covenant smart contracts
- **Role name:** "Escrow Operator" → "BCH Seller"
- **Mechanism:** Centralized custody → Covenant + volatility buffers (no custody)
- **Incentives:** Fee-only → Fee + hedge mechanism

---

## Why the Rename?

The original concept explored how BCH miners could serve as trusted escrow operators, earning fees for holding funds in custody during settlement.

In the covenant redesign (May 10, 2026), we discovered that:
1. **Custody triggers regulation** (MiCA CASP licensing)
2. **Covenants eliminate custody** (smart contracts hold funds, not operators)
3. **"Escrow" is misleading** (implies intermediary control, which covenants don't have)
4. **"BCH Seller" is accurate** (they sell BCH inventory to covenants, don't custody user funds)

---

## For Current Information

**See:** [BCH Sellers](bch-sellers.md) - The current covenant-based design

**Key differences:**
- No custody (covenant holds BCH autonomously)
- Volatility buffer (7% buffer for volatility)
- Hedge mechanism (sellers win in both price directions)
- Permissionless (anyone can be a BCH seller, not just miners)
- Automated (bot-driven, not manual)

---

## Historical Context

The "BCH Miners as Escrows" concept was part of the April 2026 escrow-based architecture that was abandoned after RS052 regulatory research revealed MiCA compliance issues.

**Escrow model problems:**
- Custody → CASP licensing required (€50k+ legal fees)
- Manual operations → High overhead
- Trusted intermediary → Regulatory exposure
- Limited to miners → Not permissionless

**Covenant model solved these:**
- No custody → No CASP license
- Automated → Bot-driven
- Trustless → Smart contract execution
- Permissionless → Anyone can participate

---

## Related Documents

**Current:**
- [BCH Sellers](bch-sellers.md) - Covenant-era replacement
- [With volatility buffer Bounty Contracts](bounty-contracts-with-volatility-buffer.md) - Covenant mechanism
- [Core Regulatory Constraints](core-regulatory-constraints.md) - Why no custody

**Historical:**
- [Market Making Partners](market-making-partners.md) - Old LP concept (also obsolete)
- [Dynamic Reward Modulation](dynamic-reward-modulation.md) - Deferred (referenced old model)

---

*This stub created May 12, 2026 to resolve broken references in historical documents.*  
*For current BCH Seller design, see: [bch-sellers.md](bch-sellers.md)*
