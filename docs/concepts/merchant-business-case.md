# The Merchant Business Case: Why Neighborhood Stores Will Self-Onboard

**Type:** Concept Document  
**Status:** ✅ Active — Core Pitch  
**Date:** 2026-05-20  
**Related:** [Fee-Splitting Model](../decisions/fee-splitting-model.md), [Cold-Start Strategy](../decisions/cold-start-strategy.md), [BCH Sellers](bch-sellers.md), [RS062 — Seller Profitability Simulation](../research/RS062_seller_profitability_simulation.md)

---

## TL;DR

A Venezuelan neighborhood store that accepts Asgaya cash-outs earns **three times** on a single remittance:

1. **0.5% merchant fee** (€0.90) — for handing over cash  
2. **15–30% product margin** (€21-42) — on the groceries the recipient buys during the visit  
3. **0.5% seller fee** (€0.90) — when the merchant later sells the same BCH back to a new sender *(requires family in Spain)*

**On a €180 remittance where the recipient spends €140 on groceries** (realistic assumption), the merchant earns:
- **Triple-dip** (with family in Spain): **€22.80–43.80**
- **Double-dip** (no family abroad): **€21.90–42.90** (merchant fee + product margin only)

A competitor who doesn't accept Asgaya earns **€0**.  
Self-onboarding is economically inevitable.

---

## 1. The Venezuelan Reality: Remittances Are Survival, Not Luxury

- **32% of household income** in Venezuela goes to food alone (Atenas Grupo, 2025).  
- The basic food basket for a family of five costs **$500–650/month** (CENDAS-FVM, 2026).  
- **60% of all consumption** depends on remittances. For many families, remittances represent up to **80% of monthly income**.  
- When Elena receives a remittance, she doesn't put it in savings — she buys **food, medicine, cooking oil, phone credit**. The money is spent immediately.

A €180 remittance covers roughly **28% of a family's monthly food budget**. It is survival money, not discretionary income.

---

## 2. The Triple-Dip: How One Remittance Pays the Merchant Three Times

### First Dip — The Merchant Fee (0.5%)

Elena walks into the neighborhood store, presents her claim code, receives cash. The merchant co-signs the covenant. The covenant releases BCH to the merchant, including **€0.90** (0.5% of €180) as the merchant fee.

### Second Dip — Product Sales (15–30% margin)

Elena does not walk out with €180 in cash. She buys groceries, hygiene items, phone credit. Neighborhood stores in Venezuela typically operate on **15–30% margins** for basic goods. Even if Elena spends only **30% of the remittance** in the store — a conservative floor — the merchant earns an additional **€8.10–16.20** in product margin on a €180 remittance.

In reality, many recipients spend nearly the entire remittance on essentials. A neighborhood store that stocks the right goods can capture a much higher share.

### Third Dip — The Seller Fee (0.5%)

The BCH the merchant received from the covenant can be sold to a **new sender**. The merchant posts it on the Asgaya bulletin board. The merchant's family member in Spain (who has a Spanish bank account) receives the sender's fiat payment. The merchant's bot detects the payment and auto-signs the covenant. The BCH moves on-chain. The merchant earns another **€0.90**.

**The same BCH earned the merchant a fee coming in, and another fee going out.**

---

## 3. Concrete Example: Neighborhood store "La Esperanza"

| Line | Amount |
|------|--------|
| Remittance value | €180.00 |
| **Merchant fee (0.5%)** | **€0.90** |
| Grocery spend by recipient (€54 at 15% margin) | **€8.10** |
| **Seller fee (0.5%) when BCH is recycled** | **€0.90** |
| **Total value to merchant (conservative)** | **€9.90** |
| | |
| **If recipient spends €140 at 30% margin** | **€42.00** |
| **Total value (aggressive scenario)** | **€43.80** |

**Realistic middle scenario (€100 spend at 20% margin):**
- Merchant fee: €0.90
- Product margin: €20.00
- Seller fee: €0.90
- **Total: €21.80 per remittance**

**Note on ranges:** The TL;DR assumes €140 spend (realistic based on household spending data) with 15-30% margin, yielding €22.80-43.80. The table above shows the full spectrum from conservative (€54 spend) to aggressive (€140 spend) scenarios. Most Phase 0 merchants will land in the €20-25 range.

The average Venezuelan neighborhood store earns **$200–500/month** in total revenue.  
**A single remittance per day adds €21.80/day → €654/month.**  
The merchant can **double or triple their income**.

---

## 4. The Competitive Moat: Why Merchants Can't Afford to Opt Out

Asgaya recipients are **walking, paying customers**. When Elena goes to a neighborhood store that accepts Asgaya, she spends money there. The neighborhood store next door — which doesn't accept Asgaya — watches her walk past.

**First-mover advantage:**
- The first merchant in a neighborhood to accept Asgaya captures all remittance foot traffic.  
- The recipient now associates that store with "where I can collect my money and buy groceries."  
- Competitors must catch up, but the first mover has already built loyalty.

**Margin flexibility:**
Because the merchant earns extra income from the fee and the seller spread, they can **cut product margins slightly** to attract even more recipients, making the competing store even less competitive. This is not predatory — it's standard retail strategy when foot traffic brings compound revenue.

**Permissionless: Why no one can gatekeep.**  
Asgaya has no approval process, no territory restrictions, no franchise fee. The first merchant doesn't need permission. Their competitors don't need permission either. The market stays competitive by design: if a merchant overcharges or provides poor service, a new merchant posts a listing and captures the foot traffic. **Permissionless access is the accelerant** — the network spreads because it can't be stopped.

---

## 5. The BCH Adoption Flywheel

```
Remittance arrives → Recipient visits neighborhood store → Merchant earns fees + margin
→ Merchant holds/recycles BCH → BCH circulates locally
→ More merchants see the economics → More merchants join
→ Network density increases → Recipients have more choices
→ Remittance volume grows → BCH adoption compounds
```

Every remittance does **three things at once**:

1. Delivers value to a family that needs it.  
2. Onboards or deepens a merchant's BCH participation.  
3. Creates a fresh BCH liquidity event that feeds the next remittance.

As the network grows, BCH circulates without ever touching a fiat exchange. The circular economy becomes self-sustaining.

> **💡 BCH Price Volatility Is Nearly Irrelevant to Merchants**
>
> The merchant receives BCH and typically recycles it within hours or days (either selling to a new sender or spending with suppliers). The income streams (€0.90 merchant fee + €20-42 product margin + €0.90 seller fee) are denominated in EUR and realized immediately. Even if BCH drops 10% between receiving and selling, the merchant's total earnings (€22-44 per remittance) remain almost unchanged. The triple-dip economics work regardless of BCH price direction.

---

## 6. This Is Not Ideology — It's Business

The merchant doesn't need to "believe in" Bitcoin Cash. They need to see that their competitor is earning more than they are. The math speaks for itself.

**Asgaya's pitch to a neighborhood store owner:**
> "A family in Spain sends money through us. The recipient comes to YOUR store, picks up cash, and buys groceries. You earn a fee on the cash, margin on the groceries, and another fee when you sell the BCH back. The store down the street gets none of this. How many customers are you willing to lose?"

---

## 7. What the Merchant Needs

| Requirement | Already Have? |
|-------------|---------------|
| A smartphone with the Asgaya app | Most neighborhood store owners have one |
| A BCH wallet (Cash Account recommended) | Free, setup < 5 minutes |
| Physical cash float | Already maintained for daily operations |
| A family member in Spain with a bank account | Optional (only needed for the seller triple-dip) |

**The barrier to entry is near zero.** The merchant already has the cash, the phone, and the store. Asgaya adds a new revenue stream on top.

---

## 8. Double-Dip vs Triple-Dip: The Family Connection Matters

**Double-dip (no family abroad):**
- Merchant fee: 0.5%
- Product margin: 15-30%
- **Cannot act as seller** (no family in Spain to receive fiat)
- Still profitable, but misses the third revenue stream

**Triple-dip (family member in Spain):**
- Merchant fee: 0.5%
- Product margin: 15-30%
- Seller fee: 0.5%
- **Full circular economy participation**
- Maximum earnings per remittance

**Phase 0 strategy:** Target merchants with family connections first. They have the highest earning potential and understand remittances from both sides.

---

## 9. Household Spending Data (Sources)

| Source | Food % | Notes |
|--------|--------|-------|
| Atenas Grupo Consultor (Oct 2025) | 32% | 14% proteins + 18% other foods |
| Leonardo Soto / Tendencias de Consumo (Jun 2025) | ~50% | Food + medicine combined at 70% |
| ANSA (Sep 2025) | $315/month | Family of 4, essentials only |
| CENDAS-FVM (Feb 2026) | $645.67/month | Family of 5, full basic food basket |

**Conservative assumption for the business case:** At least 30% of a remittance is spent on groceries in the same store where cash is collected. In practice, the true figure is often much higher.

---

## 10. Why This Matters for BCH Adoption

Traditional BCH merchant adoption efforts ask merchants to "accept Bitcoin Cash" — which means:
- Learning new technology
- Price volatility risk
- Converting to fiat later
- No immediate benefit

Asgaya flips this:
- **Merchants earn immediately** (triple revenue streams)
- **No volatility risk** (covenant guarantees EUR value)
- **No conversion needed** (can recycle BCH directly)
- **Competitive advantage** (first mover captures foot traffic)

**This is not adoption through ideology. It's adoption through self-interest.**

Every merchant that joins Asgaya becomes a BCH stakeholder who wants the network to succeed, because their income depends on it.

---

## 11. Related Documents

- [Fee-Splitting Model](../decisions/fee-splitting-model.md) — Why 0.5% to the merchant  
- [Cold-Start Strategy](../decisions/cold-start-strategy.md) — How we recruit the first merchants  
- [BCH Sellers](bch-sellers.md) — How merchants participate as sellers (triple-dip)  
- [BCH Buyers](bch-buyers.md) — How merchants off-ramp BCH for cash if they choose  
- [Pull System](pull-system.md) — How recipient timing creates the merchant opportunity
- [RS062 — Seller Profitability Simulation](../research/RS062_seller_profitability_simulation.md) — Historical data on seller returns

---

## 12. Open Questions (For Phase 0 Validation)

1. **What % of remittances are actually spent in-store?** Conservative: 30%, Likely: 60-80%
2. **Average product margins in Venezuelan neighborhood stores?** Using 15-30% range, needs validation
3. **How many merchants have family in Spain?** Critical for triple-dip potential
4. **Competitor response time?** How quickly do other stores adopt after first mover?
5. **BCH holding behavior?** Do merchants recycle immediately or hold for price appreciation?

**See:** [unknowns/behavioral/merchant-spending-patterns.md](../unknowns/behavioral/) for investigation methods

---

*Document authored: May 20, 2026*  
*Authors: Suso + DeepSeek + Coordination*  
*Status: ✅ Active — This is the merchant pitch*
