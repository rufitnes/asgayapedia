# Sender Flows - Covenant Architecture

**Part of:** [Android App Flows](../README.md)  
**Date:** 2026-05-16  
**Status:** Active - Covenant Architecture

---

## Overview

This directory contains all **sender-side screens** for creating and tracking BCH covenants in the Asgaya network.

**Use case:** Cross-border remittance where sender creates covenant, recipient claims as BCH or cash.

**Example:** Iris in Spain sends €100 to Elena in Venezuela. Elena gets notified, can claim BCH instantly (free) or cash at merchant (0.5% fee).

---

## Flow Structure

**Entry point:** [Home Screen](../home-screen.md) → Tap "Send to Asgaya User"

### Common Path (All Senders)
1. **[Recipient Selection](covenant-setup/2-recipient-selection.md)** - Enter Cash Account (Elena#142)
2. **[Amount Entry](covenant-setup/3-amount-entry.md)** - Enter amount + currency
3. **[Payment Method](covenant-setup/4-payment-method.md)** - Choose: Own wallet vs Buy from seller

### Branch A: Own Wallet Path
4A. **[Confirm Send](own-wallet-path/4a-confirm-send.md)** - Review transfer from wallet  
5A. **[Tracking](own-wallet-path/6a-tracking.md)** - Monitor covenant state  
6A. **[Completion](own-wallet-path/7a-completion.md)** - Success + savings breakdown

### Branch B: Buy from Seller Path
4B. **[Confirm Purchase](buy-seller-path/4b-confirm-purchase.md)** - Review purchase details  
4.5. **[Select Seller](buy-seller-path/4.5-select-seller.md)** - Bulletin board (preselected seller)  
5. **[Payment Instructions](buy-seller-path/5-payment-instructions.md)** - Bizum to seller  
6B. **[Tracking](buy-seller-path/6b-tracking.md)** - Monitor covenant + Bizum  
7B. **[Completion](buy-seller-path/7b-completion.md)** - Success + savings breakdown

### Error Screens
- **[Pending Covenant](errors/pending-covenant.md)** - Duplicate covenant blocked
- **[Bizum Timeout](errors/bizum-timeout.md)** - 5-minute window expired
- **[Covenant Expiry](errors/covenant-expiry.md)** - 24-hour claim window expired
- **[Network Errors](errors/network-errors.md)** - Connection issues

---

## Flow Diagram

```
         ┌──────────────────────┐
         │  Home Screen         │
         │  (Tap "Send to       │
         │   Asgaya User")      │
         └──────────┬───────────┘
                    │
             ┌──────▼──────┐
             │ 1. Recipient│
             └──────┬──────┘
                    │
             ┌──────▼──────┐
             │  2. Amount  │
             └──────┬──────┘
                    │
             ┌──────▼──────────┐
             │ 3. Payment      │
             │    Method       │
             └────┬─────┬──────┘
                         │     │
        ┌────────────────┘     └────────────────┐
        │                                       │
┌───────▼────────┐                    ┌────────▼─────────┐
│ Branch A:      │                    │ Branch B:        │
│ Own Wallet     │                    │ Buy from Seller  │
└───────┬────────┘                    └────────┬─────────┘
        │                                      │
┌───────▼────────┐                    ┌────────▼─────────┐
│ 4A. Confirm    │                    │ 4B. Confirm      │
└───────┬────────┘                    └────────┬─────────┘
        │                                      │
        │                              ┌───────▼─────────┐
        │                              │ 4.5. Select     │
        │                              │      Seller     │
        │                              └────────┬────────┘
        │                                       │
        │                              ┌────────▼────────┐
        │                              │ 5. Pay Seller   │
        │                              │    (Bizum)      │
        │                              └────────┬────────┘
        │                                       │
┌───────▼────────┐                    ┌────────▼─────────┐
│ 6A. Tracking   │                    │ 6B. Tracking     │
└───────┬────────┘                    └────────┬─────────┘
        │                                       │
        └──────────┬────────────────────────────┘
                   │
            ┌──────▼──────┐
            │ 7. Complete │
            └─────────────┘
```

---

## Standardized Numbers

**Exchange Rates:**
- 1 BCH = €1,000
- 1 EUR = 500 VES
- 1 BCH = 500,000 VES

**Example Transaction (€100):**
- Sending: €100
- BCH amount: 0.1 BCH
- VES equivalent: 50,000 VES
- Seller fee (if buying): €0.50 (0.5%)

---

## Design Principles

1. ✅ **Clear progress indicators** - User always knows where they are
2. ✅ **Honest estimates** - No false promises on speed
3. ✅ **Branch clarity** - Each path clearly separated
4. ✅ **Error prevention** - Validation before proceeding
5. ✅ **Educational moments** - Show savings, explain choices
6. ✅ **24-hour claim window** - Recipient flexibility

---

## Value Proposition

- 🎯 **Core innovation:** Kickstarts merchant network (merchants earn spread)
- 🌍 **Cross-border:** Bypasses government rate manipulation
- 💰 **Cheaper than legacy:** <1% vs 6.49% average remittance cost
- 📱 **Recipient choice:** BCH (free) or cash (0.5% fee)
- ⏱️ **24-hour claim window:** Flexibility for recipient

---

## Related Flows

- **[Recipient Flows](../recipient-flows.md)** - How recipients claim BCH or cash
- **[Merchant Flows](../merchant-flows.md)** - How merchants provide cash-out
- **[BCH Seller Flows](../../backend-apis/seller-bot/README.md)** - How sellers post collateral

---

## Navigation Tips

**For reviewers:**
- Start with [1-home.md](home-screen.md) for entry point
- Follow one complete branch (A or B) to understand full flow
- Check [errors/](errors/) for edge cases

**For developers:**
- Each file is self-contained with wireframes + interactions
- Cross-references link to related files
- Standardized numbers throughout (see above)

---

*Flow structure created: 2026-05-16*  
*Maintained by: Asgaya Contributors*  
*Based on: Covenant architecture v3.0*
