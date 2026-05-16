# Direct BCH Payment Flows

**Part of:** [Android App Flows](../README.md)  
**Date:** 2026-05-16  
**Status:** Active - Standard Wallet Functionality

---

## Overview

This directory contains the **direct payment flow** for paying merchants who accept BCH but are **not in the Asgaya network**.

**Use case:** Pay a merchant directly with BCH you already have (standard wallet transaction).

**Example:** Buy coffee at CafeRosa, scan their BCH address, send 0.01 BCH.

---

## Key Characteristics

**Simple wallet functionality:**
- ✅ Own balance ONLY (no buy from seller option)
- ✅ Instant settlement (standard BCH transaction)
- ✅ No covenant (direct transfer)
- ✅ No claim process (merchant receives immediately)
- ✅ No 24h window (completed in seconds)
- ✅ Minimal fees (network fee only ~€0.002)

**Difference from Covenant Flow:**
- **Direct Payment:** Merchant gets BCH immediately (this flow)
- **Covenant Flow:** Recipient chooses BCH or cash later ([sender-flows/](../sender-flows/))

---

## Merchant Scenarios

This flow supports two types of merchant setups:

### Scenario A: Minimal Hardware (Simple QR Code)
**Merchant has:** Cardboard sign, business card, or printed QR code with Cash Account

**How it works:**
1. User scans QR code → Gets merchant's Cash Account (CafeRosa#789)
2. Merchant verbally states price: "5,000 bolívares" or "€10"
3. User enters amount with currency selector
4. App converts to BCH using live rates
5. User confirms and sends

**Why:** Low-tech merchants can accept BCH without PoS, electricity, or internet

---

### Scenario B: Point of Sale (Payment Request QR)
**Merchant has:** PoS terminal, payment app, or dynamic QR generator

**How it works:**
1. User scans QR code → Gets merchant + amount + currency (all pre-filled)
2. App shows payment request with equivalents
3. User confirms (optional: edit amount to add tip)
4. User sends

**Why:** Better UX for tech-enabled merchants, faster checkout

---

## Flow Structure

### Simple Linear Path (4 screens)

1. **[Scan Merchant](1-scan-merchant.md)** - Entry point (scan Cash Account or BCH address)
2. **[Enter Amount](2-enter-amount.md)** - Enter amount in any currency (BCH, EUR, VES, etc.) OR auto-fill from PoS QR
3. **[Confirm & Send](3-confirm-send.md)** - Review transaction (balance check here)
4. **[Complete](4-complete.md)** - Success screen

### Error Screens

- **[Insufficient Balance](errors/insufficient-balance.md)** - Must add BCH to wallet first
- **[Network Errors](errors/network-errors.md)** - Connection issues

**Total:** 6 files (4 screens + 2 errors)

---

## Flow Diagram

```
                ┌─────────────────┐
                │   Home Screen   │
                │  (Button 2)     │
                └────────┬────────┘
                         │
                         │ Tap "Pay with Bitcoin Cash"
                         │
                ┌────────▼─────────┐
                │ 1. Scan Merchant │
                │ CafeRosa#789     │
                └────────┬─────────┘
                         │
                ┌────────▼─────────┐
                │ 2. Enter Amount  │
                │ 0.01 BCH         │
                └────────┬─────────┘
                         │
                ┌────────▼─────────┐
                │ 3. Confirm       │
                │ Check Balance    │
                └────┬──────┬──────┘
                     │      │
         ✅ Sufficient│      │❌ Insufficient
                     │      │
            ┌────────▼──┐   └──────┐
            │ Send BCH  │          │
            └────┬──────┘   ┌──────▼────────┐
                 │          │ "Add BCH to   │
            ┌────▼──────┐   │  wallet first"│
            │ 4. Complete│   └───────────────┘
            └───────────┘
```

---

## Standardized Example

**Merchant:** CafeRosa#789 (Venezuela)  
**Amount:** 0.01 BCH  
**Fiat equivalent:** ~€10 = ~5,000 VES  
**Exchange rates:** 1 BCH = €1,000, 1 EUR = 500 VES  
**Network fee:** €0.002 (~0.000002 BCH)

---

## Balance Check Logic

**Before confirming payment:**

```javascript
// Check if user has sufficient balance
const userBalance = getUserBCHBalance(); // e.g., 0.5 BCH
const paymentAmount = 0.01 BCH; // User entered
const networkFee = 0.000002 BCH; // Estimated

if (userBalance >= paymentAmount + networkFee) {
  // Proceed to send
  showConfirmScreen();
} else {
  // Insufficient balance
  showError("insufficient-balance");
  // Offer: "Add BCH to wallet" → Bulletin board
}
```

**Key principle:** Users plan ahead and top up wallet for daily expenses (like cash).

---

## Design Principles

1. ✅ **Simple wallet UX** - Standard BCH transaction, no Asgaya complexity
2. ✅ **Own balance only** - No mid-payment BCH purchase
3. ✅ **Instant settlement** - No waiting, no claim process
4. ✅ **Minimal screens** - 4 screens total (vs 7-9 for covenant)
5. ✅ **Flexible input** - Enter in any currency with live conversion OR auto-fill from PoS
6. ✅ **Plan ahead** - Users top up wallet before going out
7. ✅ **Low-tech friendly** - Works with simple cardboard QR codes (no PoS needed)

---

## Comparison: Direct Payment vs Covenant

| Feature | Direct Payment | Covenant Flow |
|---------|---------------|---------------|
| **Use case** | Pay merchant NOW | Send to contact (claim later) |
| **Merchant** | Accepts BCH directly | May not accept BCH |
| **Settlement** | Instant | 24h claim window |
| **Recipient choice** | No (BCH only) | Yes (BCH or cash) |
| **Payment method** | Own balance only | Own balance OR buy from seller |
| **Screens** | 4 | 7-9 |
| **Complexity** | Simple | Complex (covenant) |
| **Fee** | €0.002 (network) | €0.50-1% (seller + merchant) |

---

## Related Flows

- **[Sender Flows (Covenant)](../sender-flows/)** - Send to Asgaya users (claim choice)
- **[Recipient Flows](../recipient-flows.md)** - How recipients claim covenants
- **[Merchant Flows](../merchant-flows.md)** - How merchants provide cash-out

---

## Navigation Tips

**For users:**
- This flow is for merchants **outside Asgaya** who accept BCH
- If paying someone in Asgaya network → Use [sender-flows/](../sender-flows/) instead
- If insufficient balance → Add BCH to wallet first (bulletin board)

**For developers:**
- Standard BCH wallet transaction (no covenant contracts)
- No seller involvement (own balance only)
- Immediate broadcast to BCH network

---

*Flow structure created: 2026-05-16*  
*Maintained by: Asgaya Contributors*  
*Based on: Standard BCH wallet functionality*
