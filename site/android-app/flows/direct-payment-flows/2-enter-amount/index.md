# Screen 2: Enter Amount

**Part of:** [Direct Payment Flows](README.md)  
**Previous:** [Screen 1: Scan Merchant](1-scan-merchant.md)  
**Next:** [Screen 3: Confirm & Send](3-confirm-send.md)  
**Date:** 2026-05-16

---

## Screen Wireframe

### Scenario A: Simple QR Code (Manual Entry)

**Merchant has:** Cardboard sign with QR code, business card, or static address

```
┌─────────────────────────────────────┐
│ ◄ Back       Pay with BCH           │
├─────────────────────────────────────┤
│                                     │
│   Paying to: CafeRosa#789           │
│   🇻🇪 Venezuela                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   How much?                         │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  [VES ▼]       5,000        │   │◄─ Currency selector
│  └─────────────────────────────┘   │
│                                     │
│   You're sending:                   │
│   • 0.01 BCH                        │◄─ Calculated amount
│   • ~€10 (your currency)            │
│   • ~5,000 VES (merchant currency)  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Exchange rates:                   │
│   1 BCH = €1,000 = 500,000 VES      │
│   (Updated 2 min ago)               │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Network fee: 0.000002 BCH         │
│   (~€0.002 / ~1 VES)                │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         Continue            │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

**Currency Selector Options:**
```
[VES ▼] Venezuela Bolívar   ◄─ Merchant's local currency
[EUR ▼] Euro                ◄─ Your currency
[USD ▼] US Dollar
[ARS ▼] Argentine Peso
[BCH ▼] Bitcoin Cash        ◄─ Crypto-native option
```

---

### Scenario B: Point of Sale QR (Auto-filled)

**Merchant has:** PoS system, payment terminal, or dynamic QR code generator

```
┌─────────────────────────────────────┐
│ ◄ Back       Pay with BCH           │
├─────────────────────────────────────┤
│                                     │
│   Paying to: CafeRosa#789           │
│   🇻🇪 Venezuela                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   ✅ Payment request detected       │◄─ From PoS QR code
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Amount: 5,000 VES          │   │◄─ Pre-filled from QR
│  │  (requested by merchant)    │   │
│  │                             │   │
│  │  You're sending:            │   │
│  │  • 0.01 BCH                 │   │
│  │  • ~€10                     │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Edit amount ]                    │◄─ Optional override
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Exchange rate: 1 BCH = 500,000 VES│
│   Network fee: 0.000002 BCH         │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         Continue            │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

### Scenario A: Manual Entry (Simple QR Code)

**Primary Input:**
1. Select currency from dropdown (VES, EUR, USD, ARS, or BCH)
2. Enter amount in selected currency (e.g., 5,000 VES or 0.01 BCH)
3. See equivalents in other currencies automatically
4. Tap **"Continue"** → Go to [Screen 3: Confirm & Send](3-confirm-send.md)

**Real-time Updates:**
- As user types amount → Update all currency equivalents
- Currency selector changes → Recalculate equivalents
- Exchange rates refresh every 5 minutes

---

### Scenario B: Auto-filled (PoS QR Code)

**Automatic:**
- QR code contains payment request (amount + currency + recipient)
- All fields auto-fill immediately
- User sees pre-filled amount + equivalents

**Optional:**
- Tap **"Edit amount"** → Switch to manual entry mode
- Modify amount if needed (e.g., add tip)

**Navigation:**
- Tap **"Continue"** → Go to [Screen 3: Confirm & Send](3-confirm-send.md)
- Tap **"◄ Back"** → Return to [Screen 1: Scan Merchant](1-scan-merchant.md)

---

## Key Differences from Covenant Flow

### Direct Payment (This Flow)
```
✅ Currency selector (BCH, EUR, VES, USD, ARS)
✅ Enter in any currency, BCH calculated
✅ OR enter in BCH directly (crypto-first option)
✅ No pull system (rate locked at send time)
✅ Instant settlement (no claim window)
✅ Supports PoS payment requests (auto-fill)
```

### Covenant Flow
```
✅ Enter amount in fiat (recipient's currency)
✅ BCH calculated from fiat
✅ Pull system (rate determined at claim time)
✅ 24h claim window
❌ No PoS integration (manual only)
```

**Why currency selector here?**
- **Practicality:** Merchants without PoS can state price in local currency ("5,000 bolívares")
- **Flexibility:** User can enter in merchant's currency, their own currency, or BCH
- **Minimal hardware:** Works with simple cardboard QR codes (just Cash Account)
- **PoS ready:** Can also handle advanced payment requests with pre-filled amounts
- **Live rates:** DolarAPI + CoinGecko provide real-time conversion

---

## Calculations (Standardized Examples)

### Example 1: User enters in VES (Merchant's Currency)
```
Exchange rates:
- 1 BCH = €1,000
- 1 EUR = 500 VES
- 1 BCH = 500,000 VES

User enters: 5,000 VES

BCH calculation:
- VES → EUR: 5,000 ÷ 500 = €10
- EUR → BCH: €10 ÷ €1,000 = 0.01 BCH

Display:
- You're sending: 0.01 BCH
- ~€10 (your currency)
- ~5,000 VES (merchant currency)

Network fee: 0.000002 BCH (~€0.002)
Total cost: 0.010002 BCH
```

### Example 2: User enters in EUR (Their Currency)
```
User enters: €10

BCH calculation:
- EUR → BCH: €10 ÷ €1,000 = 0.01 BCH
- EUR → VES: €10 × 500 = 5,000 VES

Display:
- You're sending: 0.01 BCH
- ~€10 (your currency)
- ~5,000 VES (merchant currency)
```

### Example 3: User enters in BCH (Crypto-native)
```
User enters: 0.01 BCH

Fiat calculation:
- BCH → EUR: 0.01 × €1,000 = €10
- EUR → VES: €10 × 500 = 5,000 VES

Display:
- You're sending: 0.01 BCH
- ~€10 (your currency)
- ~5,000 VES (merchant currency)
```

### Example 4: PoS QR Code (Auto-filled)
```
QR code contains:
- Recipient: CafeRosa#789
- Amount: 5,000 VES
- Currency: VES

Auto-fill calculation:
- VES → BCH: 5,000 ÷ 500,000 = 0.01 BCH
- VES → EUR: 5,000 ÷ 500 = €10

Display (pre-filled):
- Amount: 5,000 VES (requested by merchant)
- You're sending: 0.01 BCH
- ~€10
```

---

## Real-Time Conversion

### Currency Selector Handler
```javascript
// User selects currency and types amount
const selectedCurrency = "VES"; // From dropdown
const inputAmount = 5000; // User types this

// Fetch current rates
const rates = {
  bchToEur: 1000,    // 1 BCH = €1,000
  eurToVes: 500,     // 1 EUR = 500 VES
  bchToVes: 500000,  // 1 BCH = 500,000 VES
  // Add more: USD, ARS, etc.
};

// Convert to BCH based on selected currency
let bchAmount;
switch (selectedCurrency) {
  case "BCH":
    bchAmount = inputAmount; // Already in BCH
    break;
  case "EUR":
    bchAmount = inputAmount / rates.bchToEur; // €10 → 0.01 BCH
    break;
  case "VES":
    bchAmount = inputAmount / rates.bchToVes; // 5,000 → 0.01 BCH
    break;
  case "USD":
    bchAmount = inputAmount / rates.bchToUsd;
    break;
}

// Calculate all equivalents
const eurAmount = bchAmount * rates.bchToEur;
const vesAmount = bchAmount * rates.bchToVes;

// Display equivalents
displayEquivalents({
  bch: bchAmount,
  eur: eurAmount,
  ves: vesAmount,
  userCurrency: selectedCurrency,
  merchantCurrency: "VES", // From recipient's country
  ratesUpdated: "2 min ago"
});
```

### Payment Request QR Handler
```javascript
// Scan QR code
const qrData = await scanQRCode();

// Parse payment request (BIP70-style or custom format)
if (qrData.includes("?amount=")) {
  const paymentRequest = parsePaymentRequest(qrData);
  // paymentRequest = {
  //   recipient: "CafeRosa#789",
  //   amount: 5000,
  //   currency: "VES"
  // }
  
  // Auto-fill amount
  autoFillAmount({
    amount: paymentRequest.amount,
    currency: paymentRequest.currency,
    recipient: paymentRequest.recipient
  });
  
  // Calculate BCH equivalent
  const bchAmount = paymentRequest.amount / rates.bchToVes;
  
  // Show pre-filled screen
  displayPrefilledScreen({
    requestedAmount: paymentRequest.amount,
    requestedCurrency: paymentRequest.currency,
    bchEquivalent: bchAmount,
    editable: true // Allow override
  });
}
```

---

## Features

### Flexible Currency Input
- **Currency selector:** BCH, EUR, VES, USD, ARS, etc.
- Enter amount in ANY supported currency
- Real-time conversion to BCH and other currencies
- Accommodates both crypto-native and fiat-first users

### Dual Mode Operation
- **Manual entry:** For simple QR codes (just Cash Account)
- **Auto-fill:** For PoS payment requests (amount pre-filled)
- **Edit mode:** Can modify pre-filled amounts (e.g., add tip)

### Multi-Currency Display
- **Sender's currency:** Helps sender understand cost (e.g., €10)
- **Merchant's currency:** Helps verify with merchant (e.g., 5,000 VES)
- **BCH amount:** Actual amount being sent (e.g., 0.01 BCH)
- All updated in real-time as user types

### Network Fee Transparency
- Shows estimated BCH network fee (~0.000002 BCH)
- Displays in multiple currencies (~€0.002 / ~1 VES)
- Much lower than covenant fees (€0.50-1%)
- Typical BCH transaction fee

### No Pull System
- Rate locked at payment time (when user clicks Continue)
- Instant settlement (no waiting for claim)
- No volatility risk (merchant receives immediately)
- Standard wallet behavior

### Low-Tech Friendly
- Works with cardboard QR codes (no PoS needed)
- Merchant just states price verbally
- User's phone handles all conversion
- No internet needed at merchant side

---

## Design Notes

### Two Merchant Scenarios

**Scenario A: Minimal Hardware (Cardboard QR)**
- Merchant has printed QR code on sign/card/menu
- QR contains only Cash Account (CafeRosa#789) or BCH address
- Merchant verbally states price: "5,000 bolívares" or "€10"
- User enters amount manually with currency selector
- **Why:** Low-tech merchants can accept BCH without PoS system

**Scenario B: Point of Sale System**
- Merchant has PoS terminal or dynamic QR generator
- QR contains payment request (recipient + amount + currency)
- All fields auto-fill, user just confirms
- Optional: User can edit amount (e.g., add tip)
- **Why:** Better UX for merchants with technology

### Why Currency Selector?

**Practicality:**
- Merchant states price in their local currency
- User doesn't need to calculate BCH amount manually
- Works in low-tech environments (no PoS needed)

**Flexibility:**
- User can choose: merchant's currency, their own currency, or BCH
- Accommodates different mental models (crypto-native vs fiat-first)
- Live exchange rates handle conversion automatically

**Minimal Hardware Requirement:**
- Only needs static QR code (printable)
- No electricity, internet, or PoS at merchant
- User's phone does all the work

**PoS Ready:**
- Can also handle sophisticated payment requests
- Auto-fill amount when QR contains payment details
- Backward compatible with simple QR codes

### Exchange Rates Strategy

**Real-time:**
- DolarAPI: EUR/VES/ARS blue dollar rates
- CoinGecko: BCH/EUR spot price
- Combined: Multi-hop conversions (VES → EUR → BCH)
- Updates every 5 minutes

**Rate lock:**
- Rate determined at payment time (when user clicks Continue)
- No pull system (instant settlement, no claim window)
- Merchant receives exact BCH amount sent

---

## Technical Notes

### Rate Sources
```javascript
// Fetch exchange rates
const rates = await Promise.all([
  // BCH price from CoinGecko
  fetch("https://api.coingecko.com/api/v3/simple/price?ids=bitcoin-cash&vs_currencies=eur,usd"),
  
  // Latin America fiat rates from DolarAPI
  fetch("https://dolarapi.com/v1/dolares/blue"), // VES, ARS
]);

// Build rate table
const rateTable = {
  bchToEur: rates[0].eur,        // 1 BCH = €1,000
  bchToUsd: rates[0].usd,        // 1 BCH = $1,100
  eurToVes: rates[1].ves,        // 1 EUR = 500 VES
  bchToVes: rates[0].eur * rates[1].ves  // 1 BCH = 500,000 VES
};

// Refresh every 5 minutes
setInterval(fetchRates, 5 * 60 * 1000);
```

### Payment Request QR Code Format

**Simple QR (Just Address):**
```
CafeRosa#789
OR
bitcoincash:qr2x8w9t3fjlk042z2wv7hahsldgwhwy0rq9sywjpyy
```

**Payment Request QR (BIP21-style):**
```
bitcoincash:qr2x8w9t3fjlk042z2wv7hahsldgwhwy0rq9sywjpyy?amount=0.01&label=CafeRosa

OR with fiat amount:

bitcoincash:qr2x...?amount=0.01&currency=VES&fiat=5000&label=CafeRosa#789
```

**Parsing Logic:**
```javascript
const parseQRCode = (qrData) => {
  // Check if it's a payment request
  if (qrData.includes("?")) {
    const [address, params] = qrData.split("?");
    const urlParams = new URLSearchParams(params);
    
    return {
      type: "payment_request",
      recipient: urlParams.get("label") || address,
      address: address.replace("bitcoincash:", ""),
      amount: parseFloat(urlParams.get("amount")), // BCH amount
      currency: urlParams.get("currency") || "BCH",
      fiatAmount: parseFloat(urlParams.get("fiat")), // Optional fiat
    };
  } else {
    // Simple Cash Account or address
    return {
      type: "simple",
      recipient: qrData,
      address: await resolveCashAccount(qrData)
    };
  }
};
```

### Network Fee Estimation
```javascript
// Estimate BCH network fee
const txSize = 250; // bytes (average)
const feeRate = 1; // sat/byte (current network rate)
const networkFee = (txSize * feeRate) / 100000000; // In BCH

// Convert to selected currency for display
const networkFeeFiat = networkFee * rates.bchToSelectedCurrency;

displayNetworkFee({
  bch: networkFee,
  fiat: networkFeeFiat,
  currency: selectedCurrency
});
```

### Currency Precision
```javascript
// Different currencies have different decimal places
const precisionMap = {
  BCH: 8,  // 0.00000001 BCH (1 satoshi)
  EUR: 2,  // €0.01
  USD: 2,  // $0.01
  VES: 0,  // No decimals (bolivares)
  ARS: 0   // No decimals (pesos)
};

// Format amount based on currency
const formatAmount = (amount, currency) => {
  const precision = precisionMap[currency];
  return amount.toFixed(precision);
};
```

---

## Related Documentation

- **[Screen 3: Confirm & Send](3-confirm-send.md)** - Next step (balance check)
- **[Screen 1: Scan Merchant](1-scan-merchant.md)** - Previous step
- **[Direct Payment Overview](README.md)** - Flow structure

---

*Screen documented: 2026-05-16*  
*Status: Active - Standard Wallet Functionality*
