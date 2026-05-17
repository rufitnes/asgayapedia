# Screen 2: Recipient Selection

**Part of:** [Sender Flows](../README.md) → [Covenant Setup](./)  
**Previous:** [Screen 1: Home](1-home.md)  
**Next:** [Screen 3: Amount Entry](3-amount-entry.md)  
**Date:** 2026-05-16

---

## Screen Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back         Send Money           │
├─────────────────────────────────────┤
│                                     │
│   Who are you sending money to?     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  👤 Cash Account or Address  │   │
│  │                             │   │
│  │  Elena#142                  │   │◄─ Primary: Cash Account
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   Recent recipients:                │◄─ Quick send (currency preselected)
│   • Elena#142 (🇻🇪 VES)             │
│   • Carlos#5890 (🇦🇷 ARS)           │
│                                     │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  [ 📷 Scan QR ]  [ 📱 From Contacts ]│◄─ Quick access
│                                     │
│  ┌─────────────────────────────┐    │
│  │         Continue            │    │
│  └─────────────────────────────┘    │
│                                     │
│                                     │
│  💡 Recipient will be notified      │
│     and can choose where to pick    │
│     up the cash                     │
│                                     │
│  [ View Merchant Map ]              │◄─ Preview available merchants
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

**Primary Input:**
- Enter Cash Account (e.g., `Elena#142`) or paste BCH address
- Tap **"Continue"** → Resolve Cash Account → Check corridor → Go to [Screen 3](3-amount-entry.md)

**Quick Actions:**
- Tap recent recipient → Auto-fill recipient AND currency
- Tap **"Scan QR"** → Open camera, scan Cash Account/BCH QR code
- Tap **"From Contacts"** → Select contact with saved Cash Account
- Tap **"View Merchant Map"** → Preview merchants in recipient's country

**Navigation:**
- Tap **"◄ Back"** → Return to [Screen 1: Home](1-home.md)

---

## Validation Logic

### Cash Account Input
```javascript
Input: "Elena#142"

Validation steps:
1. Format check: Name#Number ✓
2. Resolve: cashaccounts.bchdata.cash → bitcoincash:qp3...
3. Corridor check: EUR→VES available? ✓
4. Merchant check: Active merchants in Venezuela? ✓

Success → Continue to Screen 3
Failure → Show error: "Cannot resolve Elena#142. Ask recipient to verify their Cash Account name."
```

### BCH Address Input (Fallback)
```javascript
Input: "bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy"

Validation steps:
1. Format check: Valid BCH address? ✓
2. Country detection: From metadata or manual selection
3. Corridor check: Available? ✓

Success → Continue to Screen 3
```

---

## Features

### Recent Recipients (Quick Send)
- **Cash Account + Currency preselected**
- Example: Tap "Elena#142 (🇻🇪 VES)" → Auto-fills:
  - Recipient: Elena#142
  - Currency: VES (in Screen 3)
- **Benefit:** Reduces taps for recurring payments

### Contact Integration
- User saves `Elena#142` in phone contacts
- Tap "From Contacts" → One-tap send
- Streamlines frequent transfers

### QR Code Scanning
- Supports Cash Account QR codes
- Supports standard BCH address QR codes
- No manual typing needed

### Merchant Map Preview
- Shows available merchants in recipient's country
- Builds confidence before sending
- Visual corridor verification

---

## Technical Notes

**Cash Accounts:**
- BCH-native identifier (no centralized database)
- Format: `Name#Number` (e.g., `Elena#142`)
- Resolves to BCH address via cashaccounts.bchdata.cash
- Replaces phone numbers (more private, global)

**Corridor Detection:**
- Auto-detected from Cash Account metadata
- Block height → Historical country data
- Fallback: Manual selection if ambiguous

**Recent Recipients Storage:**
- Stores Cash Account + last currency used
- Enables one-tap recurring payments
- Privacy: Local storage only

---

## Related Documentation

- **[Screen 1: Home](1-home.md)** - Entry point
- **[Screen 3: Amount Entry](3-amount-entry.md)** - Next step
- **[Cash Accounts Concept](../../../concepts/cash-accounts.md)** - Technical details
- **[Corridor Availability](../../../concepts/corridors.md)** - Supported countries

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
