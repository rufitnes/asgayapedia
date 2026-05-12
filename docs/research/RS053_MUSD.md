# RS053: MUSD (Moria Protocol) — BCH-Native Overcollateralized Stablecoin

**Research Type:** Protocol Analysis
**Status:** ✅ Draft
**Date:** 2026-05-09
**Related:** [RS054 CashToken Swaps](research/RS054_cashtoken_swaps.md), [BCH Sellers](../concepts/bch-sellers.md), [Decentralized Pull System](../decentralized-pull-system.md)

---

## TL;DR

**MUSD is a BCH-native, overcollateralized stablecoin built on CashTokens by Riften Labs (Moria Protocol). Users lock BCH into vaults to mint MUSD at a 1:1 USD peg. It is not fiat-backed (no USDT style reserves) — it's a decentralized CDP (Collateralized Debt Position) model similar to MakerDAO, but on BCH.**

- Already live, audited by Hashlock, TVL fluctuating between $25K-$600K
- Can be swapped on Cauldron DEX (MUSD/BCH pool)
- Peg is maintained through redemption mechanisms and liquidations
- Asgaya could integrate MUSD as a stable settlement option for merchants

---

## How MUSD Works

### The Vault Model

Users lock BCH into Moria smart contracts (CashScript covenants with CashTokens) to mint MUSD:

```
User locks 0.15 BCH (worth $150 at $1,000/BCH)
  → Can mint up to $100 MUSD (150% collateral ratio)
  → MUSD tokens are sent to user's wallet
  → User can repay MUSD + interest to unlock BCH
```

**Collateral requirements (v1):**
- Initial ratio: 150%
- Minimum before liquidation: 120%
- Users choose their own interest rate (determines redemption priority)

### Peg Maintenance

MUSD stays at $1 through two mechanisms:

1. **Redemption:** If MUSD trades below $1, third parties can repay the *lowest-interest* loan at face value, claiming the BCH collateral. This reduces MUSD supply and pushes price back up.

2. **Liquidation:** If collateral falls below 120%, the position is liquidated. Third parties repay the loan and claim the BCH (plus a premium).

### Oracle

Moria uses an on-chain oracle system (d3lphi) that tracks:
- MUSD price on Cauldron DEX (for redemption triggers)
- BCH/USD price (for liquidation calculations)

---

## Current Status (as of May 2026)

| Milestone | Status |
|-----------|--------|
| MUSDv0 test run (Dec 2024 - May 2025) | ✅ Concluded |
| MUSDv1 deployment (May 2025) | ✅ Live |
| Security audit (Hashlock) | ✅ Passed |
| DefiLlama integration | ✅ Active |
| Cauldron DEX MUSD/BCH pool | ✅ Active |
| Moria Cash (merchant payments) | ✅ Beta |
| TVL | ~$596K (post DefiLlama listing) |

**Moria v1 improvements over v0:**
- Fixed 110% liquidation → bumped to 120% (safer during oracle outages)
- Interest-free loans → user-chosen interest rates
- Redemption prioritizes lowest-interest loans (market-driven)
- Loan NFTs — loans can be transferred, wrapped into other contracts

---

## Why MUSD Matters for Asgaya

### The Problem It Solves

In the current covenant architecture, the **Merchant receives BCH** when the contract matures. For a merchant in Venezuela, receiving volatile BCH adds risk they didn't ask for:

- BCH could drop 5% while they're converting to VES
- They need to find a buyer for the BCH
- They're exposed to price swings they can't hedge

MUSD solves this neatly:

1. Merchant receives BCH from the covenant
2. Merchant swaps BCH → MUSD on Cauldron DEX (or via bulletin board)
3. Merchant holds MUSD — stable, predictable, spendable
4. Merchant can convert MUSD to VES when convenient

### The Integration Surface

Asgaya doesn't need to issue or hold MUSD. The bulletin board can:

- **Show MUSD/BCH swap offers** alongside BCH/fiat offers
- Let merchants signal: "I accept BCH, but prefer MUSD"
- Provide rate estimates: "This covenant will deliver ~$99.50 worth of BCH, which is ~99.50 MUSD after swap"

**Minimal integration effort:** The bulletin board just needs to read MUSD price from Moria oracle or Cauldron and display it as an option for merchants.

---

## Limitations to Consider

| Factor | Detail |
|--------|--------|
| TVL is small | ~$600K — enough for early adoption, but liquidity depth for large swaps is limited |
| Peg history | MUSDv0 had trouble maintaining $1 peg during high volatility. v1 improved this but it's still experimental |
| Oracle dependency | Relies on d3lphi oracle — if oracle fails, liquidations and redemptions pause |
| Not multi-currency | MUSD is USD-pegged only. For VES-pegged stability, a separate token would be needed (see RS054) |
| Swap fees | Cauldron DEX charges fees on MUSD/BCH swaps — may need to factor into merchant pricing |

---

## Verdict

**MUSD is viable as a merchant-friendly settlement layer for Asgaya Phase 0.1+.**

It's already live, audited, and has basic liquidity. The covenant gets BCH to the merchant — MUSD lets them stabilize it. Asgaya doesn't issue or control the token, so there's no regulatory risk for the protocol.

For Phase 0 (MVP), pure BCH is fine. Merchants who want stability can convert manually. For Phase 0.1, adding MUSD swap visibility on the bulletin board is a low-effort high-value feature.

### Recommendations

| Phase | MUSD Integration |
|-------|-----------------|
| **Phase 0** (MVP) | None. Ships use cash. Merchants manually convert if desired |
| **Phase 0.1** | Bulletin board displays MUSD/BCH rate. Merchants can signal "I accept MUSD" |
| **Phase 0.2** | Atomic swaps between MUSD and BCH directly on the bulletin board (see RS054) |

---

## Sources

- [Moria Protocol Docs](https://docs.moria.money/)
- [MUSD on BULB](https://www.bulbapp.io/p/398b4486-caa2-425b-a014-78144c949861/musd-and-its-role-in-the-bitcoin-cash-ecosystem)
- [Cauldron DEX](https://www.cauldron.quest/)
- [Riften Labs](https://www.riftenlabs.com/)
- [Moria Whitepaper (PDF)](https://bitcoincashpodcast.com/assets/files/Moria-Whitepaper-3989c99303f69c8a282bfa9fa4d618a5.pdf)

---

*Researched: May 9, 2026*
*Research by: Yakyak (OpenYak)*
