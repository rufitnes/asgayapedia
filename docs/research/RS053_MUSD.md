# RS053: MUSD (Moria Protocol) — BCH-Native Overcollateralized Stablecoin

**Research Type:** Protocol Analysis  
**Status:** ⚠️ **No Longer Active** (Contract disabled Apr 2026)  
**Date:** 2026-05-09 (Updated: 2026-06-13)  
**Related:** [RS054 CashToken Swaps](research/RS054_cashtoken_swaps.md), [BCH Sellers](../concepts/bch-sellers.md), [Decentralized Pull System](../decentralized-pull-system.md)

---

## ⚠️ CRITICAL UPDATE (April 23, 2026)

**MUSD (Moria V1) was disabled in April 2026 following discovery of a smart contract vulnerability.**

**Timeline:**
- **16:00 CET:** External security researcher disclosed vulnerability in Moria V1 contracts
- **17:00 CET:** Riften Labs confirmed vulnerability, halted basis-point oracle
- **23:00 CET:** Emergency response - Riften Labs used the vulnerability themselves to redeem ALL outstanding loans (protective measure to prevent malicious exploitation), then set oracle price to 0 (effectively disabling the contract)

**Current Status:**
- ✅ No user funds lost (all loans redeemed before exploitation)
- ❌ Moria V1 contract permanently disabled
- ✅ Riften Labs committed to 6-month buyback program (1:1 USD value)
- ❓ Moria V2 status unknown (no public timeline for relaunch)

**Impact on Asgaya:** 
- ❌ **Cannot integrate MUSD for Phase 0** (contract no longer active)
- ❌ Integration recommendations below are no longer applicable
- ✅ Original research preserved below for historical context and lessons learned

**Source:** [Moria Money Emergency Notice](https://app.moria.money/) - April 23, 2026

---

## TL;DR (Historical - May 2026)

**MUSD WAS a BCH-native, overcollateralized stablecoin built on CashTokens by Riften Labs (Moria Protocol). Users locked BCH into vaults to mint MUSD at a 1:1 USD peg. It was not fiat-backed (no USDT style reserves) — it was a decentralized CDP (Collateralized Debt Position) model similar to MakerDAO, but on BCH.**

- ~~Already live, audited by Hashlock, TVL fluctuating between $25K-$600K~~ **No longer active**
- ~~Can be swapped on Cauldron DEX (MUSD/BCH pool)~~ **Pool closed**
- ~~Peg is maintained through redemption mechanisms and liquidations~~ **Contract disabled**
- ~~Asgaya could integrate MUSD as a stable settlement option for merchants~~ **Not currently possible**

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

## Verdict (Updated June 13, 2026)

**MUSD is NOT viable for Asgaya. The protocol has been permanently disabled due to smart contract vulnerability.**

~~It's already live, audited, and has basic liquidity.~~ **This was true on May 9, 2026. On April 23, 2026, the contract was discovered to have a critical vulnerability despite the Hashlock audit.**

**Key lesson:** Even audited smart contracts can have critical vulnerabilities. Young protocols (MUSD launched May 2025, failed April 2026 = 11 months lifespan) carry significant risk.

### Updated Recommendations

| Phase | MUSD Integration |
|-------|-----------------|
| **Phase 0** (MVP) | ❌ **NOT POSSIBLE** - Protocol defunct |
| **Phase 0.1** | ❌ **NOT POSSIBLE** - No active contract |
| **Phase 0.2** | ❌ **NOT POSSIBLE** - Would need Moria V2 (timeline unknown) |

**Alternative approaches:**
- Accept BCH volatility as lesser evil than VES hyperinflation (see: merchants already deal with this successfully)
- Explore mature hedging solutions (AnyHedge - see RS067 [to be created])
- Wait for proven stablecoins to emerge (2027+ timeline)
- DO NOT build on young/unproven protocols (this failure validates caution)

---

## Lessons Learned

**For Asgaya architecture decisions:**

1. **"Too young" is a valid technical objection** - MUSD was only 11 months old when it failed
2. **Audits don't guarantee safety** - Hashlock audit passed, vulnerability still existed
3. **TVL is not proof of stability** - $600K TVL didn't prevent catastrophic failure
4. **Smart contract risk is real** - Even non-custodial systems can fail
5. **Simplicity has value** - Complex stablecoin mechanisms introduce attack surface

**The core principle remains:**
> "If merchants face even a small risk of loss, adoption collapses."

Building on immature stablecoin infrastructure violates this principle. The MUSD failure proves that "audited and live" ≠ "safe for production."

**Recommendation:** Asgaya Phase 0 should ship with simple BCH covenants only. Merchants already manage BCH volatility successfully (LocalBitcoins, Binance P2P). Don't introduce NEW risks (smart contract failure) to solve an EXISTING risk (volatility) that merchants already handle.

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
