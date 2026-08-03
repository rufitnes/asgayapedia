# Tourist Payments

**Scenario:** International traveler paying local merchants when traditional payment methods fail

**Example:** Spanish tourist in Venezuela, credit card blocked/not accepted

---

## The Problem

**Traditional friction:**
- Credit cards don't work abroad (blocked by bank, not accepted locally)
- ATMs charge high fees or unavailable
- Cash exchange has poor rates and spreads
- Tourist stuck without payment method

---

## Asgaya Solution

**Flow:**
1. Tourist opens Asgaya at merchant
2. Pays BCH seller via Bizum/preferred payment method (Spanish bank account works)
3. Merchant receives BCH payment instantly
4. Tourist gets goods/services

**Key benefit:** Tourist's home bank account works anywhere there's an Asgaya merchant

---

## Why This Works

**For tourist:**
- Use familiar payment method (Bizum, bank transfer)
- Avoid cash exchange fees/spreads
- No credit card foreign transaction fees
- Works even when cards are blocked

**For merchant:**
- Receives BCH (converts to local currency if needed)
- Avoids credit card processing fees (2-3%)
- Expands customer base to international travelers
- 0-conf enables instant settlement

---

## Required Infrastructure

**Merchant network:** Dense enough that tourists can find participating merchants

**BCH liquidity:** Passive sellers providing BCH in tourist's home country

**0-conf acceptance:** Enables point-of-sale speed (1-3 minutes vs 10-15 minutes)

---

## Status

**Phase 1+** - After remittance infrastructure proven and merchant network established

---

## Technical Flow

This use case follows **[Customer Flow 2](README.md#flow-2-customer-needs-to-buy-bch--covenant-for-better-ux)** from the Customer Journey - tourist needs to buy BCH at point of sale using home payment method (Bizum), then merchant receives BCH payment.

**Key difference from standard customer flow:** Tourist is typically a one-time customer (not repeat), so merchant relationship is transactional rather than relationship-based.

---

**Related:** [Customer Journey](README.md), [Cross-Border Living](cross-border-living.md), [Merchant as ATM](merchant-as-atm.md)
