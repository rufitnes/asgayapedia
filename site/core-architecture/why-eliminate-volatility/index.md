# Why: Eliminate Volatility Risk

**Sub-requirement of:** [Why: Cheaper Than Legacy](why-cheaper-than-legacy.md)

**Core Requirement:** Protect users from cryptocurrency price fluctuations during the transfer.

---

## The Problem

### Cryptocurrency Volatility Kills Remittances

Bitcoin Cash price can move 2-5% in 20 minutes during volatile periods. While this volatility is acceptable for traders and investors, it's catastrophic for remittances.

**Example scenario:**
- Sender commits €100
- 20 minutes later, recipient cashes out
- If BCH dropped 3%, recipient gets €97 worth of value
- **Recipient loses €3 through no fault of their own**

For a family depending on remittances for food and rent, a surprise 3% loss is unacceptable.

### Why This Is Deal-Breaking

**Remittances require predictability:**
- Sender needs to know: "If I send €100, family gets €100 equivalent"
- Recipient needs to know: "I can pay my €95 rent with this €100 transfer"
- Merchant needs to know: "I'm getting €100 worth of value, not €97"

**One bad experience ends adoption:**
- User sends €100
- Family receives €97 equivalent
- User thinks: "Asgaya cheated me, back to Western Union"
- **Lost customer forever**

**The lesson:** You cannot build a remittance network on top of volatility risk. Users will not accept "maybe you lose 3%, maybe you gain 2%"—they need certainty.

---

## Why Zero Volatility Is Required

### 1. Trust Requires Predictability

Users switching from Western Union know exactly what they're getting:
- Send €100, family gets exact local currency equivalent
- Fees are transparent (even if unfair)
- **No surprise losses**

Asgaya must match or exceed this predictability. If we introduce volatility risk, we're asking users to gamble with their family's rent money.

**Success metric:** <0.5% slippage on 95%+ of transactions due to volatility.

### 2. Merchants Won't Accept Volatility Risk

Merchants operate on thin margins. A corner store owner can't accept:
- "You might get €100 worth of BCH, or €97, we'll see"
- Uncertainty in what they're receiving
- Risk they bear through no action of their own

**Without merchant certainty, there is no network.**

### 3. The 1% Fee Advantage Evaporates

If volatility causes 2-5% slippage, the entire economic advantage of <1% fees disappears:
- Asgaya fee: 1%
- Volatility loss: 3%
- **Total cost: 4%**

User thinks: "Western Union charges 6.49%, but I know what I'm getting. Asgaya is 4% but unpredictable—I'll stick with Western Union."

**Volatility protection isn't optional—it's foundational.**

---

## The Asymmetry Problem

**In traditional crypto transfers:**
- If price goes up 3% → User gains €3 (happy accident)
- If price goes down 3% → User loses €3 (Asgaya cheated me!)

**Human psychology:**
- Gains feel like luck
- Losses feel like theft
- **Losses are remembered 10x more than gains**

One user who loses €3 tells 10 friends. Those 10 friends never try Asgaya.

**We cannot build a network on a foundation of random losses.**

---

## Why "Close Enough" Isn't Good Enough

**Users don't think in averages—they remember losses:**
1. One bad experience erases ten good ones
2. Families can't budget with "might be €97, might be €103"
3. Merchants can't operate with uncertain revenue
4. Network growth requires consistent positive experiences

**The requirement is zero volatility exposure, not reduced volatility.**

---

## Who Should Bear Volatility Risk?

Not the sender (they're sending money to family, not speculating).
Not the recipient (they need certainty to pay bills).
Not the merchant (they operate on thin margins).

**Only parties who voluntarily accept it and are compensated for it.**

This means:
- Liquidity providers who profit from volatility arbitrage
- Speculators who want BCH exposure
- Long-term holders who believe in BCH appreciation

**These parties accept risk knowingly and benefit from it.**

---

## Why This Matters for Adoption

Volatility protection is the difference between:
- "Send money home reliably" (remittance network)
- "Gamble on crypto prices" (trading platform)

Users need the first. If we offer the second, we fail.

**Every transaction must prove:** "Your family got exactly what you sent, no surprises."

---

## Related Requirements

- [Why: Cheaper Than Legacy](why-cheaper-than-legacy.md) — Volatility losses erase the 1% fee advantage
- [Why: Promote Adoption](why-promote-adoption.md) — Surprise losses reverse network growth

---

## Trade-offs and Decisions

See the **Decisions** section for how we achieve zero volatility:

- **[Two-Step Settlement Timing](../decisions/two-step-settlement-timing.md)** — How pull-based BCH purchase eliminates volatility for all parties except LPs

---

## The Bottom Line

**Users don't need to understand Bitcoin Cash.**
**Users DO need certainty that €100 sent = €100 received.**

If we compromise on volatility protection, we're not building a remittance network—we're building a casino.

---

*Last updated: May 1, 2026*
*Core principle: "Remittances require certainty. One surprise loss ends adoption."*
