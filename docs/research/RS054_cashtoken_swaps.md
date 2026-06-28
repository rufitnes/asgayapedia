# RS054: CashToken Swaps — Multi-Corridor Stable Settlements

**Research Type:** Architectural Exploration
**Status:** ✅ Draft
**Date:** 2026-05-09
**Related:** [RS053 MUSD Analysis](RS053_MUSD.md), [Decentralized Pull System](../glossary.md#pull-system), [BCH Sellers](../glossary.md#bch-seller-passive)

---

## TL;DR

**CashTokens on BCH enable native tokenized assets that can be swapped atomically. This opens the possibility of corridor-specific stable tokens (MVES for Venezuela, MEUR for Europe, etc.) for Asgaya's last-mile settlement — without the protocol issuing or controlling any tokens.**

However, the economic incentive design for token-only flows is less clear than the covenant-based model. The recommendation is: **covenants for the remittance spine, CashToken swaps for the last mile.**

---

## The Vision

### Beyond Single-Corridor MUSD

MUSD is USD-pegged. But Asgaya operates across corridors: EUR → VES, EUR → ARS, EUR → HNL. What if each corridor had its own stable token?

| Corridor | Stable Token | Peg | Collateral |
|----------|-------------|-----|------------|
| Venezuela | MVES | VES (Bolívar) | BCH |
| Argentina | MARS | ARS (Peso) | BCH |
| Honduras | MHNL | HNL (Lempira) | BCH |
| Europe | MEUR | EUR | BCH |

Each is a fork of the Moria Protocol vault model, but pegged to a different local currency. The tokens are issued by the community or by corridor-specific DAOs — **not by Asgaya**.

### How Swaps Would Work

Atomic swaps between CashTokens enable trustless exchange:

```
Alice holds MVES (wants MEUR)
  ↔ Bulletin board matches her with Bob (holds MEUR, wants MVES)
  ↔ Atomic swap: one on-chain transaction, both settle or neither does
  ↔ No custody, no escrow, no intermediary
```

For Asgaya, the bulletin board would:

1. **List token pairs** — MVES/BCH, MEUR/BCH, MVES/MEUR
2. **Show rates** — based on Moria oracle or Cauldron DEX prices
3. **Facilitate matching** — "Merchant wants to swap 100 MVES for BCH"
4. **Provide swap UI** — one-click atomic swap via CashToken contracts

---

## The Integration Idea: Best of Both Worlds

### Layer 1: Fiat → BCH (Covenant Spine)

The covenant handles the hard part: converting fiat to BCH with volatility protection and seller incentives. This doesn't change.

**Current flow:**
```
Sender → Bizum → Seller → Covenant → Merchant receives BCH
```

### Layer 2: BCH → Local Currency (Token Layer)

Once the merchant has BCH, they have options:

```
Merchant receives BCH
  ├── Option A: Keep BCH (if they want exposure)
  ├── Option B: Swap to MUSD on Cauldron (stable USD)
  ├── Option C: Swap to MVES via bulletin board atomic swap
  └── Option D: Hand cash to Elena (original flow)
```

The bulletin board adds swap offers alongside bounty offers. The merchant chooses what works best.

### Layer 3: Direct Token Remittances (Future)

In a more advanced scenario, a sender who already holds MEUR could send it directly to a recipient who holds MVES — no covenant needed at all.

```
Sender (holds MEUR) ↔ Bulletin board matches ↔ Atomic swap ↔ Recipient gets MVES
```

This is a **pure token-to-token corridor** — zero BCH volatility, zero covenant complexity. But it requires:
- Both parties to already hold the relevant tokens
- Sufficient liquidity in the token pairs
- No fiat on-ramp needed (sender must have acquired MEUR somehow)

---

## Economic Incentive Problem

The covenant model has clear incentives for every participant:

| Participant | Incentive |
|-------------|-----------|
| **Seller** | 0.5% fee + hedge against BCH volatility |
| **Merchant** | 0.5% fee + business from cash-out |
| **Sender** | <1% total fees (vs. 6.49% legacy) |
| **Recipient** | Receives cash instantly |

In a pure token-swap model:

| Participant | Incentive | Problem |
|-------------|-----------|---------|
| **Sender** | Low fees | Already holds token — no fiat on-ramp issue |
| **Recipient** | Receives token | Need to find someone to buy it for cash |
| **Merchant** | Swap fee | How do they earn? Spread on rates? |
| **Token issuer** | Stability | No profit motive to maintain the peg |

**The gap:** Who provides MVES liquidity? Who mints it? Who maintains the peg if the vault model doesn't attract enough collateral? These are unsolved questions for corridor-specific tokens.

MUSD works because Moria has a clear incentive (fees + MEUR holders wanting liquidity). A MVES token needs a similar bootstrap mechanism.

---

## Recommendation

### Phase 0 (MVP)
- Pure BCH covenant flow. No tokens.
- Manual cash-out for merchants.

### Phase 0.1 (Merchant Stability)
- Add MUSD support: merchants can swap BCH → MUSD via Cauldron.
- Bulletin board displays MUSD/BCH rate as reference.
- **No new tokens issued by Asgaya.**

### Phase 1 (Multi-Corridor Tokens)
- Re-evaluate corridor-specific tokens (MVES, MEUR) once MUSD is proven.
- Tokens should be community or DAO-issued, not protocol-controlled.
- Bulletin board adds atomic swap matching.

### Phase 2 (Token-Native Corridors)
- If token liquidity is sufficient, direct token-to-token remittances become possible.
- Covenant flow remains the default for fiat on-ramp.

---

## Open Questions

1. **Who mints MVES?** A Moria fork requires someone to collateralize BCH and accept the risk. Who does this for VES?
2. **Peg maintenance:** MUSD has struggled with peg stability. A lower-liquidity MVES would struggle more.
3. **Regulatory risk:** If a community-issued MVES becomes widely used for remittances, does it trigger regulation for the issuers?
4. **User experience:** Tokens require wallets, seed phrases, backup — the same friction the covenant model tries to avoid for recipients.
5. **Merchant adoption:** Would merchants prefer MVES over cash? In Venezuela's dollarized economy, cash is still king.

---

## Verdict

**Tokens as a last-mile stability layer (MUSD for merchants):** ✅ Promising, low risk, Phase 0.1.

**Tokens as a replacement for covenants (full token corridor):** ❌ Premature. Economic incentives for token-only flows are unclear. Covenant model is more mature for remittance use cases.

**Multi-currency token ecosystem:** ⏳ Wait. Let MUSD prove itself, then evaluate.

---

## Sources

- [CashTokens Specification (CHIP-2022-02)](https://github.com/bitjson/cashtokens)
- [Moria Protocol Docs](https://docs.moria.money/README.md)
- [Cauldron DEX](https://www.cauldron.quest/README.md)
- [MUSD on BULB](https://www.bulbapp.io/p/398b4486-caa2-425b-a014-78144c949861/musd-and-its-role-in-the-bitcoin-cash-ecosystem)

---

*Researched: May 9, 2026*
*Research by: Yakyak (OpenYak)*
