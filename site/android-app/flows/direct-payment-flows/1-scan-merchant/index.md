# Screen 1: Scan Merchant

**Part of:** [Direct Payment Flows](README.md)  
**Entry from:** Home → "Pay with Bitcoin Cash"  
**Next:** [Screen 2: Enter Amount](2-enter-amount.md)  
**Date:** 2026-05-16

---

## Screen Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back       Pay with BCH           │
├─────────────────────────────────────┤
│                                     │
│   Scan merchant or enter            │
│   Cash Account                      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │    ╔═══════════════════╗    │   │
│  │    ║                   ║    │   │
│  │    ║   [QR Scanner]    ║    │   │
│  │    ║                   ║    │   │
│  │    ║   Point camera    ║    │   │
│  │    ║   at merchant QR  ║    │   │
│  │    ║                   ║    │   │
│  │    ╚═══════════════════╝    │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┄┄┄┄┄┄┄┄┄┄ OR ┄┄┄┄┄┄┄┄┄┄          │
│                                     │
│  👤 Enter Cash Account:             │
│  CafeRosa#789                       │◄─ Merchant example
│                                     │
│  [ Enter BCH address manually ]    │◄─ Fallback
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│                                     │
│  💡 For merchants who accept BCH    │
│     (instant payment, no claim)    │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

**Primary Input:**
- **Camera opens automatically** for QR scanning
- Scan merchant's QR code → Detect Cash Account or BCH address → Continue
- **OR** Enter Cash Account manually (e.g., `CafeRosa#789`)
- **OR** Tap "Enter BCH address manually" for fallback

**After input:**
- Valid Cash Account → Resolve to BCH address → Go to [Screen 2](2-enter-amount.md)
- Valid BCH address → Go to [Screen 2](2-enter-amount.md)
- Invalid input → Show error, retry

**Navigation:**
- Tap **"◄ Back"** → Return to Home

---

## Input Examples

### Cash Account (Preferred)
```
Input: CafeRosa#789
Validation: Name#Number format ✓
Resolution: cashaccounts.bchdata.cash → bitcoincash:qr2x...
Display: "Paying CafeRosa#789"
```

### BCH Address (Fallback)
```
Input: bitcoincash:qr2x8w9t3fjlk042z2wv7hahsldgwhwy0rq9sywjpyy
Validation: Valid BCH address ✓
Display: "Paying bitcoincash:qr2x..."
```

### QR Code Scan
```
Scan: QR contains "CafeRosa#789" or "bitcoincash:qr2x..."
Auto-parse: Detect format
Continue: Go to amount entry
```

---

## Features

### Cash Account Support
- Human-readable merchant names (CafeRosa#789)
- BCH-native (no phone numbers, no centralized database)
- Works globally (no country-specific formatting)

### QR Code Scanning
- Supports Cash Account QR codes
- Supports standard BCH address QR codes
- No manual typing needed (reduces errors)

### Fallback Options
- Manual Cash Account entry
- Manual BCH address entry
- Paste from clipboard

---

## Design Notes

**Merchant vs Person:**
- Merchant: `CafeRosa#789` (business name)
- Person: `Elena#142` (personal name)
- Both use same Cash Account format

**Direct Payment = Instant:**
- No covenant creation
- No claim process
- Merchant receives BCH immediately
- Standard BCH wallet transaction

**For non-Asgaya merchants:**
- Merchant doesn't need Asgaya app
- Just accepts standard BCH payments
- Can use any BCH wallet to receive

---

## Technical Notes

**Cash Account Resolution:**
```javascript
// User enters: CafeRosa#789
const cashAccount = "CafeRosa#789";

// Resolve to BCH address
const address = await resolveCashAccount(cashAccount);
// Returns: bitcoincash:qr2x8w9t...

// Display to user
displayPaymentDetails({
  recipient: cashAccount,
  address: address
});
```

**QR Code Detection:**
```javascript
// Scan QR code
const qrData = await scanQRCode();

// Detect format
if (isCashAccount(qrData)) {
  const address = await resolveCashAccount(qrData);
} else if (isBCHAddress(qrData)) {
  const address = qrData;
} else {
  showError("Invalid QR code");
}
```

---

## Related Documentation

- **[Screen 2: Enter Amount](2-enter-amount.md)** - Next step
- **[Direct Payment Overview](README.md)** - Flow structure
- **[Cash Accounts Concept](../../../concepts/cash-accounts/)** - Technical details

---

*Screen documented: 2026-05-16*  
*Status: Active - Standard Wallet Functionality*
