# Remittance with Merchant Cash-Out - Complete Flow

**Part of:** [Android App Flows](android-app/flows/README.md)
**Date:** 2026-05-10
**Status:** Active - Covenant Architecture

---

## Overview

This document details the **core Asgaya remittance use case**: Send money to recipient who claims cash at a local merchant.

**Use case:** Cross-border remittance with merchant claim window

**Example:** Iris in Spain sends €100 to Elena in Venezuela. Elena gets notified, finds nearby merchant, walks to shop, claims cash.

**Why build this SECOND:**
- ✅ More complex (recipient selection, notification, merchant matching)
- ✅ More actors (sender, BCH seller, recipient, merchant)
- ✅ Two-sided co-signing needed (merchant + recipient)
- ✅ 24-hour claim window management
- ✅ Covenant architecture (overcollateralization, timeout cascade)

**Value proposition:**
- 🎯 **Core innovation:** Kickstarts merchant network (merchants earn spread)
- 🌍 **Cross-border:** Bypasses government rate manipulation
- 💰 **Cheaper than legacy:** <1% vs 6.49% average remittance cost
- 📱 **Recipient choice:** Pick convenient merchant from map
- ⏱️ **24-hour claim window:** Flexibility for recipient

**User journey:**
```
Sender → Creates covenant → Funds BCH seller (Bizum) → Recipient notified →
Finds merchant on map → Walks to shop → Claims cash → Both co-sign → Complete
```

**Total screens:**
- Sender: 7 screens
- Recipient: 6 screens (see [recipient-flows.md](android-app/flows/recipient-flows.md))
- Merchant: 5 screens (see [merchant-flows.md](android-app/flows/merchant-flows.md))

**Timeline:** 30-60 minutes (mostly waiting for recipient to claim)

**Design principles:**
- ✅ Clear progress indicators (sender sees real-time updates)
- ✅ Honest estimates (no false promises on speed)
- ✅ Educational moments (show savings, recruit participants)
- ✅ 24-hour claim window (recipient flexibility)

---

## Sender Screens

### Screen 1: Home (Entry Point)

```
┌─────────────────────────────────────┐
│ ☰                    Asgaya      🌐 │
├─────────────────────────────────────┤
│                                     │
│     Welcome to Asgaya               │
│     Your Bitcoin Cash Wallet        │◄─ Position as wallet
│                                     │
│  ┌───────────────────────────────┐  │
│  │                               │  │
│  │   💸 Send to Asgaya User      │  │◄─ Covenant-based flow
│  │   Recipient chooses BCH/cash  │  │
│  │                               │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │                               │  │
│  │   🪙 Pay with Bitcoin Cash    │  │◄─ Standard BCH payment
│  │   Direct payment to merchant  │  │
│  │                               │  │
│  └───────────────────────────────┘  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  Recent activity:                   │
│  • Sent 0.1 BCH to Elena ✓         │◄─ Show BCH amounts
│  • Paid 0.01 BCH to Café Rosa ✓    │
│                                     │
│  [ Settings ]      [ Help ]         │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "Send to Asgaya User" → Go to Screen 2 (covenant-based flow)
- Tap "Pay with Bitcoin Cash" → Go to standard BCH payment flow (wallet functionality)

**Notes:**
- **Two distinct flows clearly separated:**
  
  **Button 1: "Send to Asgaya User"**
  - For people in Asgaya network (family, friends, contacts)
  - Creates covenant (24-hour claim window)
  - Recipient chooses: BCH (free) or Cash at merchant (0.5% fee)
  - Uses Cash Accounts (Elena#142)
  - Covenant-based architecture
  
  **Button 2: "Pay with Bitcoin Cash"**
  - For merchants/anyone who accepts BCH but NOT in Asgaya network
  - Standard BCH wallet transaction (no covenant)
  - Direct payment (instant settlement, no claim process)
  - Uses standard BCH addresses or Cash Accounts
  - No Asgaya infrastructure needed (pure wallet functionality)

- **Fail-safe design:** Even if user picks wrong button, BCH ends up at recipient (just different UX)
- **This merges bch-payment-flows.md:** "Pay with Bitcoin Cash" handles non-Asgaya merchants
- Positioned as Bitcoin wallet (regulatory framing)
- Recent activity shows both types: "Sent" (Asgaya) vs "Paid" (direct)

---

### Screen 2: Enter Recipient Cash Account

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

**Interactions:**
- Enter Cash Account (e.g., `Elena#142`) or paste BCH address
- Tap recent recipient → Auto-fill
- Tap "From Contacts" → Select contact with saved Cash Account
- Tap "Scan QR" → Scan recipient's Cash Account QR code
- Tap "View Merchant Map" → Show merchants in recipient's country
- Tap "Continue" → Resolve Cash Account → Check corridor availability → Go to Screen 3

**Validation:**
```
Input: Elena#142
Checks:
1. Valid Cash Account format? ✓ (Name#Number)
2. Resolve via cashaccounts.bchdata.cash → bitcoincash:qp3...
3. Corridor available (EUR→VES)? ✓
4. Active merchants in Venezuela? ✓

If any check fails:
"Cannot resolve Elena#142. Ask recipient to verify their Cash Account name."
```

**Alternative input (fallback):**
```
Input: bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy
Checks:
1. Valid BCH address format? ✓
2. Detect country from address metadata or manual selection
3. Proceed to amount entry
```

**Notes:**
- Cash Accounts replace phone numbers as primary identifier (BCH-native, no centralized database)
- **Recent recipients:** Cash Account + currency preselected (makes recurring payments easier)
  - Tap "Elena#142 (🇻🇪 VES)" → Auto-fills recipient AND currency in Screen 3
  - Reduces taps for frequent senders
- Contact integration: User saves `Elena#142` in phone contacts → One-tap send
- QR code scanning works with Cash Account QR or standard BCH address QR
- Corridor auto-detected from Cash Account metadata (block height → historical country data) or manual selection
- **Merchant map preview:** "View Merchant Map" shows available merchants in recipient's country (builds confidence)

---

### Screen 3: Enter Amount

```
┌─────────────────────────────────────┐
│ ◄ Back         Send BCH             │◄─ "Send BCH" (wallet framing)
├─────────────────────────────────────┤
│                                     │
│   Sending to: Elena#142             │
│   🇻🇪 Venezuela                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   How much do you want to send?     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ [VES ▼]  500,000.00         │   │◄─ Currency selector + amount
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│   Exchange rate: 1 EUR = 500 VES│◄─ Only if VES ≠ default
│   €9.95 (incl. 0.5% fee)           │◄─ EUR with sender fee
│   (Updated 3 min ago)               │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Elena receives: 0.1 BCH        │◄─ BCH amount (crypto-first)
│   (~€9.90 worth)                    │
│                                     │
│   💡 Elena can claim instantly as   │◄─ Educational note
│      BCH, or cash out at merchant   │
│      (she pays 0.5% for cash)       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   ⚠️ Exchange rate determined when  │◄─ Pull system: rate at claim time
│      Elena claims (she controls     │
│      timing & gets rate at that     │
│      moment)                        │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         Continue            │   │
│  └─────────────────────────────┘   │
│                                     │
│  💡 Min: €10 (varies by seller)     │◄─ Dynamic limits
│                                     │
└─────────────────────────────────────┘
```

**Calculations:**
```
User enters: 50,000 VES
Exchange rate: 1 EUR = 500 VES
Base amount: 500,000 ÷ 50,500 = €9.90
Sender fee (0.5%): €9.90 × 1.005 = €9.95
Sender pays: €9.95
Elena receives: 0.1 BCH (~€9.90 worth at current rate)
```

**Interactions:**
- Tap currency selector [VES ▼] → Choose EUR, VES, USD, ARS, etc.
- Type amount in selected currency
- If currency ≠ default (EUR for Spain sender): Show exchange rate + EUR conversion
- If currency = default (EUR): Hide exchange rate line, just show amount
- Rate updates every 5 minutes (DolarAPI + CoinGecko)
- Tap "Continue" → Check for pending covenant → Go to Screen 4 (or show error)

**Validation before Screen 4:**
```javascript
// Check: Does sender already have pending covenant for this recipient?
const hasPendingCovenant = checkBlockchain({
  sender: currentUserAddress,
  recipient: "Elena#142", // Resolved from Cash Account
  status: "pending" // Not expired, not matured
});

if (hasPendingCovenant) {
  showPendingCovenantError(); // See error screen below
} else {
  goToScreen4(); // Continue to payment method
}
```

**Currency Selector Logic:**
```javascript
// App settings define default (EUR for Spain, VES for Venezuela)
if (selectedCurrency === defaultCurrency) {
  // Hide exchange rate, show only selected currency
  "€100.00"
  "Your fee: €0.50 (0.5%)"
  "You pay: €100.50"
  "Elena receives: 0.1 BCH"
}

if (selectedCurrency !== defaultCurrency) {
  // Show exchange rate + conversion to default currency
  "[VES ▼]  500,000.00"
  "Exchange rate: 1 EUR = 500 VES"
  "€9.95 (incl. 0.5% fee)"
  "Elena receives: 0.1 BCH"
}
```

**Notes:**
- **Currency selector:** Adapts to sender/recipient preferences (EUR default for Spain)
- **BCH amount shown:** Recipient receives BCH (not EUR) - wallet-first UX
- **Sender fee only:** 0.5% to BCH Seller (merchant fee optional, recipient pays)
- **Educational note:** Explains recipient choice (BCH free, cash 0.5% extra)
- **Pull system exchange rate:** Rate determined when recipient claims (NOT when sender confirms) - recipient controls timing and gets rate at claim moment
- **Dynamic limits:** Min €10 (or lower if sellers accept), varies by active sellers with smsbridge_loop
- **Exchange rate display:** Current rate shown for estimation only (actual rate at claim time may differ)

**Related decisions:** 
- [How Exchange Rates Work](decisions/how-exchange-rates-work.md)
- [UI Language Regulatory Implications](decisions/ui-language-regulatory-implications.md) - "Send BCH" vs "Send Money"
- [Fee Splitting Model](decisions/fee-splitting-model.md) - Recipient choice affects total fees

---

### Screen 3.5: Pending Covenant Error (Only if Validation Fails)

**Only shown if sender already has pending covenant for this recipient**

```
┌─────────────────────────────────────┐
│ ◄ Back    ⚠️ Pending Transaction    │
├─────────────────────────────────────┤
│                                     │
│   You already sent to Elena#142     │
│   and she hasn't claimed it yet.    │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Recipient: Elena#142       │   │
│  │  Amount: €100.00            │   │
│  │  Status: Waiting for claim  │   │
│  │                             │   │
│  │  Time remaining: 18h 23m    │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  You can send another transaction   │
│  after Elena claims this one or     │
│  it expires (24 hours).             │
│                                     │
│  💡 Reason: Prevents duplicate      │
│     transactions and ensures        │
│     clean matching in Bizum         │
│     concept field.                  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Track This Transaction    │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Choose Different Recipient │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "Track This Transaction" → Go to Screen 6 (tracking page for existing covenant)
- Tap "Choose Different Recipient" → Go back to Screen 2
- Tap "Back to Home" → Return to home screen

**Notes:**
- Prevents duplicate covenants per (sender → recipient) pair
- Ensures clean 1:1 matching for Bizum concept field (Cash Account)
- User can track existing transaction or choose different recipient
- After covenant matures/expires, user can send to same recipient again

---

### Screen 4: Select Payment Method

**User chooses how to fund the transfer:**

```
┌─────────────────────────────────────┐
│ ◄ Back         Select Payment       │
├─────────────────────────────────────┤
│                                     │
│   How do you want to pay?           │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  From your wallet           │   │
│  │  2.1 BCH available          │   │◄─ Balance shown
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Buy Bitcoin Cash in        │   │◄─ Fixed typo
│  │  Asgaya's bulletin board    │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  [ Back to edit ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "From your wallet" → Go to Screen 4A
- Tap "Buy Bitcoin Cash in Asgaya's bulletin board" → Go to Screen 4B

---

### Screen 4A: Sending BCH from Your Wallet Balance

```
┌─────────────────────────────────────┐
│ ◄ Back         Confirm Order        │
├─────────────────────────────────────┤
│                                     │
│   Review your transfer              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Elena#142 receives:        │   │
│  │  0.1 BCH                    │   │◄─ Fixed spacing
│  │                             │   │
│  │  (~50,000 VES) = (~€100)    │   │◄─ Fiat equivalent
│  │                             │   │
│  │  Exchange rate:             │   │
│  │  1 EUR = 500 VES         │   │
│  │                             │   │
│  │  Network fee: €0.002        │   │◄─ BCH network fee only
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  What happens next:                 │
│  1. Elena#142 gets notified         │
│  2. Can claim BCH immediately       │
│  3. Or cash out at merchant (24h)   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Confirm & Send BCH        │   │◄─ Fixed button text!
│  └─────────────────────────────┘   │
│                                     │
│  [ Change payment method ]          │◄─ Switch to bulletin
│  [ Back to edit ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "Confirm & Send BCH" → Create covenant → Go to Screen 6 (Tracking)
- **No Screen 4.5 or 5 needed** (no seller, no Bizum)
- Tap "Change payment method" → Go back to Screen 4
- Tap "Back to edit" → Return to Screen 3

---

### Screen 4B: Buying BCH from Seller

```
┌─────────────────────────────────────┐
│ ◄ Back         Confirm Order        │
├─────────────────────────────────────┤
│                                     │
│   Review your transfer              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  You pay:                   │   │
│  │  €100.50 (Bizum)            │   │◄─ EUR amount + seller fee
│  │                             │   │
│  │  BCH purchased:             │   │
│  │  0.1005 BCH                 │   │◄─ BCH being bought from seller
│  │  (~€100.50 worth)           │   │
│  │                             │   │
│  │  Elena#142 receives:        │   │
│  │  0.1 BCH                 │   │◄─ Amount after fees
│  │  (~50,000 VES)           │   │
│  │                             │   │
│  │  Exchange rate:             │   │
│  │  1 EUR = 500 VES         │   │
│  │                             │   │
│  │  Corridor:                  │   │
│  │  🇪🇸 Spain → 🇻🇪 Venezuela  │   │
│  │                             │   │
│  │  Seller fee: €0.50 (0.5%)   │   │◄─ Clear fee display
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  What happens next:                 │
│  1. Elena#142 gets notified         │
│  2. Can claim BCH immediately       │
│  3. Or cash out at merchant (24h)   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Confirm & Pay Seller      │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Change payment method ]          │◄─ Switch to own BCH
│  [ Back to edit ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "Confirm & Pay Seller" → Go to Screen 4.5 (Bulletin Board with preselected seller)
- Then Screen 5 (Payment Instructions)
- Then Screen 6 (Tracking)
- Tap "Change payment method" → Go back to Screen 4
- Tap "Back to edit" → Return to Screen 3

**Payment Method Logic:**
```javascript
// Check sender's BCH balance
const senderBalance = 0.5 BCH; // Example: €500 worth
const transferNeeds = 0.1 BCH; // €100 worth at current rate
const covenantCreationFee = 0.0001 BCH; // ~€0.10

if (senderBalance >= transferNeeds + covenantCreationFee) {
  // Show Option A: "Send from My BCH Wallet" (recommended)
  // No seller fee (FREE)
}

// Always show Option B: "Buy BCH from Seller"
// Seller fee: 0.5%
```

**Notes:**
- **Bitcoin wallet functionality:** Sender can use own BCH (like any wallet)
- **Or buy BCH:** If wallet empty or wants to save BCH, buy from seller
- **Fee difference:** Own BCH = FREE, Buy BCH = 0.5% seller fee
- **Nudge own BCH:** Shown first if balance sufficient (save 0.5%)
- **Clear expectations:** Timeline updated (depends on recipient claim time)
- **Simplified flow:** Removed step-by-step (covenant handles automatically)

---

### Screen 4.5: Select BCH Seller (Bulletin Board)

**Only shown if user selected "Buy BCH from Seller" in Screen 4**

**Purpose:** Browse active BCH sellers and choose one based on limits, payment method, and availability. **Recommended seller preselected** based on user preferences.

```
┌─────────────────────────────────────┐
│ ◄ Back      Select BCH Seller       │
├─────────────────────────────────────┤
│                                     │
│   Buy 0.1 BCH (~€100)            │
│                                     │
│  ⭐ Recommended for you:             │◄─ Preselected seller
│                                     │
│  ┌═══════════════════════════════┐  │◄─ Highlighted border
│  │ 🟢 Seller#3421  ⭐ SELECTED   │  │
│  │                               │  │
│  │ Limits: €10 - €500            │  │
│  │ Payment: Bizum, SEPA          │  │
│  │ Fee: 0.5%                     │  │
│  │ Avg response: 8 sec           │  │◄─ Fastest response
│  │ Completed: 1,247 tx           │  │◄─ Most experienced
│  │                               │  │
│  │ ✓ Best match for your         │  │◄─ Why recommended
│  │   preferences (fast response) │  │
│  │                               │  │
│  │ [ Continue with this seller ] │  │◄─ Primary action
│  └═══════════════════════════════┘  │
│                                     │
│  Other sellers:                     │◄─ Can still browse
│                                     │
│  ┌───────────────────────────────┐  │
│  │ 🟢 Seller#8190                │  │
│  │                               │  │
│  │ Limits: €50 - €1,000          │  │
│  │ Payment: SEPA, Cash, ATM      │  │
│  │ Fee: 0.4% (lower!)            │  │
│  │ Avg response: 15 sec          │  │◄─ Slower API polling
│  │                               │  │
│  │ [ Select ]                    │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │ 🟡 Seller#5029                │  │◄─ Slow response
│  │                               │  │
│  │ Limits: €5 - €200             │  │
│  │ Payment: Bizum only           │  │
│  │ Fee: 0.5%                     │  │
│  │ Avg response: 45 sec          │  │◄─ Manual verification
│  │                               │  │
│  │ [ Select ]                    │  │
│  └───────────────────────────────┘  │
│                                     │
│  💡 Sort by: [ Fee ▼ ]             │
│     Filter: [ Bizum only ]          │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- **Recommended seller preselected** (highlighted with ⭐ badge)
- Tap "Continue with this seller" → Go to Screen 5 (Payment Instructions)
- Tap any other seller card → Deselect recommended, select new seller
- Tap "Select" on alternative seller → Updates selection → Go to Screen 5
- Sort by: Fee (lowest first), Response time (fastest first), Limits (highest first)
- Filter: Payment method (Bizum, SEPA, Cash, ATM), Currency (EUR, USD, GBP)
- User can scroll and compare all sellers before confirming

**Seller Information Displayed:**

**1. Availability Status:**
```javascript
🟢 Online (liveness signal active in last 5 min)
🟡 Slow (liveness signal 5-15 min ago)
🔴 Offline (no signal >15 min)
```

**2. Limits:**
- Min/Max transaction amounts
- Validates sender's €9.90 is within range
- Grayed out if amount doesn't fit

**3. Payment Methods:**
- **Bizum** - Instant (5-15 sec avg) ← Bot parses Bizum API notifications
- **SEPA** - Slower (30-60 sec) ← API polling delay
- **Cash** - Meetup required
- **ATM deposit** - Manual verification (varies)
- **Future:** Other cryptos (BTC, ETH, USDT) - seller accepts crypto for BCH

**4. Fee:**
- Standard: 0.5%
- Some sellers may charge less (0.3-0.4%) to attract volume
- Or more (0.6-0.8%) for instant service

**5. Average Response Time:**
- Based on historical data (smsbridge_loop.py notification parsing)
- **Fast:** 5-15 sec (Bizum API, automated verification)
- **Normal:** 15-30 sec (SEPA polling, slower APIs)
- **Slow:** 30-60+ sec (manual verification, ATM deposits)
- **Phase 0 testing needed:** How fast can seller bot parse different payment methods?

**6. Transaction Counter:**
- Shows completed transactions (experience indicator)
- "Completed: 1,247 tx" → Experienced, trustworthy seller
- "Completed: 12 tx" → New seller, less history
- Helps senders choose reliable sellers
- Combined with other metrics (response time, fee, limits)

**Selection Logic:**
```javascript
// Sender needs €100 worth of BCH
const senderAmount = 100.00;

// Filter sellers
const availableSellers = allSellers.filter(seller => 
  seller.status === 'online' &&
  seller.minLimit <= senderAmount &&
  seller.maxLimit >= senderAmount &&
  seller.paymentMethods.includes('Bizum') // If sender filtered by Bizum
);

// Sort by fee (default) or response time
availableSellers.sort((a, b) => a.fee - b.fee);

// Display top 5-10 sellers
```

**Preselection Logic (Compliance-Friendly):**
```javascript
// Preselect recommended seller based on user preferences
const recommendedSeller = selectBestSeller({
  sellers: availableSellers,
  criteria: userPreference, // "fastest" | "cheapest" | "most_experienced"
  amount: 100.00,
  paymentMethod: "Bizum"
});

// ALWAYS show Screen 4.5 (never skip)
// Recommended seller is PRESELECTED with ⭐ badge
// User sees who they're paying and can change if desired
showScreen4_5({
  preselected: recommendedSeller,
  alternatives: otherSellers
});

// Transparency for compliance:
// - User sees bulletin board (not hidden)
// - Preselection is visual hint, not forced
// - User can change seller before continuing
// - Clear "Why recommended" explanation shown
```

**Why preselect instead of auto-skip:**
1. **Compliance:** User sees who they're paying (transparent)
2. **Trust:** User understands recommendation logic ("Best match for fast response")
3. **Choice:** User can still browse and compare alternatives
4. **Consent:** Explicit tap on "Continue with this seller" (not automatic)

**Future Enhancements (Phase 1+):**
- **Reputation system:** Star rating, completed transactions, dispute rate
- **Seller profiles:** "BCH Miner since 2019, 500+ transactions, 0 disputes"
- **Multi-currency:** Accept USD via Wise/Revolut, swap to BCH
- **Multi-crypto:** Accept BTC/ETH/USDT, swap to BCH (via atomic swaps or DEX)
- **Seller notes:** "Fast response, friendly, available 9am-9pm CET"

**Notes:**
- This is the **P2P marketplace core** - bulletin board in action
- Permissionless: Anyone can be a seller (post liveness signal)
- Competitive: Sellers compete on fee, speed, limits
- Flexible: Multiple payment methods, currencies (future)
- Antifragile: If top sellers go offline, others visible immediately

**Merchant Circular Flow (Key Insight!):**
- **Merchants are natural BCH sellers** - they accumulate BCH from cash-outs, then sell it back to new senders
- **Two-way liquidity:**
  - Receive BCH → Give cash (cash-out service, earn 0.5%)
  - Receive cash → Post BCH (BCH selling, earn 0.5%)
- **Self-sustaining:** Merchants keep BCH circulating without needing to buy/sell on exchanges
- **Same infrastructure:** Merchant already has smsbridge_loop.py for cash-out notifications, can reuse for BCH selling
- **Example:** Merchant does 10 cash-outs (gets 1 BCH) → Sells that 1 BCH to 10 new senders → Those recipients do cash-outs → Loop continues

**Why this screen is critical:**
- Shows Asgaya is truly P2P (not single seller, not company)
- User sees market competition (fees vary, choose best deal)
- Transparency (all terms shown upfront)
- Choice (pick based on preference: fastest, cheapest, or highest limit)

**Phase 0 simplification:**
- May only have 1-2 sellers (you + trusted friend)
- Auto-select if only one available
- Build UI for future scalability (5-10+ sellers)

---

### Screen 5: Payment Instructions (Bizum to BCH Seller)

```
┌─────────────────────────────────────┐
│ ◄ Back      Payment Instructions    │
├─────────────────────────────────────┤
│                                     │
│   📱 Send Bizum Payment              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  To: 612-XXX-XXX            │   │◄─ BCH seller phone
│  │                             │   │
│  │  Amount: €100.00            │   │
│  │                             │   │
│  │  Concept: Elena#142         │   │◄─ Recipient Cash Account
│  │                             │   │
│  │  [ Copy details ]           │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                    │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│                                     │
│  Instructions:                      │
│  1. Open your bank app              │
│  2. Send Bizum with exact details   │
│     (Concept field links payment    │
│      to covenant)                   │
│  3. Return here when sent           │
│                                     │
│  ⏱️ Complete within: 5 min          │
│     (Seller's volatility window)    │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 You're paying the BCH seller    │
│     who provides collateral for     │
│     Elena's covenant                │
│                                     │
│  ┌─────────────────────────────┐    │
│  │   I've sent the Bizum       │    │
│  └─────────────────────────────┘    │
│                                     │
│  [ Cancel order ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "Copy details" → Copies to clipboard
- Tap "I've sent the Bizum" → Go to Screen 6
- Tap "Cancel order" → Return to home

**Notes:**
- 5-minute window (seller's BCH volatility exposure)
- **Concept field = recipient Cash Account** (Elena#142) - links Bizum payment to covenant
- Human-readable identifier (seller can manually verify if needed)
- One pending covenant per (sender → recipient) pair ensures clean 1:1 matching
- Copy button reduces manual entry errors
- Explains BCH seller role (not central entity)
- **Android autocomplete enhancement:** If autocomplete can copy these details directly to banking app → simpler UX, less manual errors
  - Implementation: Use Android's autofill framework
  - Pre-fill Bizum fields in banking app
  - Reduces context switching and typing errors

**Seller Bot Matching Logic:**
```python
# Seller receives Bizum SMS notification
bizum_sender = "Iris"       # From Bizum sender field
bizum_amount = 100.00       # From SMS
concept = "Elena#142"       # From concept field

# Find matching covenant (always exactly one)
covenant = find_pending_covenant(
    sender=resolve_address(bizum_sender),  # Iris's BCH address
    recipient=concept,                      # Elena#142
    amount_eur=bizum_amount                # €100
)

# Sign and fund covenant with BCH collateral
sign_and_fund_covenant(covenant)
```

**Why 5 minutes:**
- Seller has full BCH exposure until Bizum received
- Typical BCH volatility in 5 min: 0.5-1% (within 7% buffer)
- Bizum usually arrives in 2-3 minutes
- After Bizum received, seller's hedge activates (94-97% exposure reduction)

**Related concept:** [BCH Sellers - Hedge Mechanism](concepts/bch-sellers.md#the-hedge-mechanism-why-sellers-always-win-)

---

### Screen 6: Tracking (Multiple States)

#### State 1: Waiting for Bizum

```
┌─────────────────────────────────────┐
│  Sending 0.1 BCH to Elena#142      │◄─ BCH amount + Cash Account
├─────────────────────────────────────┤
│                                     │
│      ⏳ Waiting for payment...      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████░░░░░░░░░░░░░░  20%    │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena#142                     │◄─ Cash Account only
│   Amount: €100 → 50,000 VES        │
│                                     │
│   Progress:                         │
│   ✅ Covenant created               │◄─ Covenant first
│   ⏳ Bizum to BCH seller pending... │
│   ⏸️  Notifying Elena...            │
│   ⏸️  Waiting for claim...          │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   ⏱️ Time remaining (5 min window): │
│       4m 32s                        │
│                                     │
│  [ Cancel (full refund) ]           │
│                                     │
└─────────────────────────────────────┘
```

**Notes:**
- **Title shows:** BCH amount being sent + recipient Cash Account (not order ID)
- **Progress order:** Covenant created FIRST, then Bizum payment (covenant exists before funding)
- 5-minute Bizum window (seller's volatility exposure)
- Sender can cancel before Bizum confirmed (full refund, covenant unfunded)
- Automatic timeout if Bizum not received within 5 minutes
- Simplified progress steps (removed "Merchant co-signing" and "Cash delivery" - happens later)

#### State 2: Covenant Created, Notifying Recipient

```
┌─────────────────────────────────────┐
│         Order #REM-89234            │
├─────────────────────────────────────┤
│                                     │
│      📱 Notifying Elena...          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████████░░░░░░░░  40%      │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena (+58-412-XXX)           │
│   Amount: €100 → 50,000 VES        │
│                                     │
│   Progress:                         │
│   ✅ Bizum received (seller paid)   │
│   ✅ Covenant created (24h window)  │
│   🔄 Notifying Elena...             │
│   ⏸️  Merchant co-signing...        │
│   ⏸️  Cash delivery...              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Elena has been notified!          │
│   Bounty code: 8923                 │
│                                     │
│   Covenant can be claimed at any    │
│   merchant in the Asgaya network    │
│                                     │
│   ⏱️ Claim window: 23h 58m          │
│                                     │
└─────────────────────────────────────┘
```

**Notes:**
- Notification sent via WhatsApp/Telegram/LINE (see [recipient-flows.md](android-app/flows/recipient-flows.md))
- Bounty code shown to sender (can share if needed: last 4 digits of covenant ID)
- 24-hour claim window starts when covenant created
- Covenant is public on bulletin board (all merchants can see)

#### State 2b: Expiring Soon (18h Elapsed, No Claim)

```
┌─────────────────────────────────────┐
│         Order #REM-89234            │
├─────────────────────────────────────┤
│                                     │
│      ⚠️  Claim window closing       │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████░░░░░░░░░░░░  25%      │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena (+58-412-XXX)           │
│   Amount: €100 → 50,000 VES        │
│                                     │
│   Progress:                         │
│   ✅ Covenant created               │
│   ⚠️  Elena hasn't claimed yet      │
│   ⏸️  Merchant co-signing...        │
│   ⏸️  Cash delivery...              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   ⏱️ Time remaining: 5h 42m         │
│                                     │
│   ⚠️  Elena hasn't claimed yet.     │
│   If unclaimed after 24h, split     │
│   refund:                           │
│   - Merchant portion → You (€99.50) │
│   - Seller fee → Seller (€0.50)    │
│                                     │
│   Contact Elena: +58-412-XXX-5678   │
│                                     │
│  [ Call Elena ]  [ Message Elena ]  │
│                                     │
└─────────────────────────────────────┘
```

**Triggers:**
- Shown at 18-hour mark if recipient hasn't claimed yet
- Notification sent to sender: "Elena hasn't claimed yet"
- Urgent reminder sent to recipient simultaneously

**Interactions:**
- Sender can contact recipient directly (phone number shown)
- "Call" button opens phone dialer
- "Message" button opens WhatsApp/SMS

**Notes:**
- Empowers sender to coordinate with recipient
- Clear warning about split refund mechanism
- **Split refund rationale:**
  - Merchant portion (€99.50) → Refunded to you (Iris)
  - Seller fee (€0.50) → Kept by seller (earned for 24h service)
- Related policy: [Overcollateralized Bounty Contracts - Timeout Cascade](concepts/overcollateralized-bounty-contracts.md#timeout-cascade)

#### State 3: Merchant Co-Signing

```
┌─────────────────────────────────────┐
│         Order #REM-89234            │
├─────────────────────────────────────┤
│                                     │
│      🏪 Merchant co-signing...      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████████████░░░  60%       │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena (+58-412-XXX)           │
│   Amount: €100 → 50,000 VES        │
│   Code: 8923                        │
│                                     │
│   Progress:                         │
│   ✅ Covenant created               │
│   ✅ Elena notified                 │
│   ✅ Merchant entered code          │
│   🔄 Both co-signing covenant...    │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   💡 Elena is at a merchant now     │
│   Both parties co-signing covenant  │
│   (cryptographic signatures)        │
│                                     │
│   Covenant matures when both sign   │
│                                     │
│   Elena is on her way!              │
│                                     │
└─────────────────────────────────────┘
```

**Note:** 
- Covenant requires both merchant and recipient signatures
- No numeric codes (cryptographic co-signing via BCH Script)
- Settlement triggered when both signatures present

**Related flow:** [Merchant Flows - Co-Sign Covenant](android-app/flows/merchant-flows.md#screen-3-hand-ves--co-sign-covenant)

#### State 4: Cash Delivered (Both Co-Signed)

```
┌─────────────────────────────────────┐
│         Order #REM-89234            │
├─────────────────────────────────────┤
│                                     │
│      💰 Cash delivered!             │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████████████████  100%     │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena (+58-412-XXX)           │
│   Amount: €100 → 50,000 VES      │
│                                     │
│   Progress:                         │
│   ✅ Covenant created               │
│   ✅ Elena notified                 │
│   ✅ Merchant: Bodega María         │
│   ✅ Both co-signed covenant        │
│   ✅ Cash delivered & confirmed     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   🎉 Elena confirmed receipt!       │
│   Transaction complete.             │
│                                     │
│   Total time: 42 minutes            │
│                                     │
│   [ See details ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "See details" → Go to Screen 7 (completion)
- Real-time updates (push notifications or polling)

**Notes:**
- Both merchant AND recipient co-signed (cryptographic signatures)
- Final VES amount shown (actual rate at claim time)
- Total time tracked (transparency)
- Covenant matured → BCH distributed to merchant and seller

---

### Screen 7: Completion & Savings

```
┌─────────────────────────────────────┐
│ ◄ Back      ✅ Complete!            │
├─────────────────────────────────────┤
│                                     │
│   Transfer successful!              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │         ✓                   │   │
│  │    Large checkmark          │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│   0.1 BCH sent to Elena#142        │◄─ BCH amount + Cash Account
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│   Sent: €100.00                     │
│   Elena received: 50,000 VES        │
│   (€100 worth at final rate)        │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│   Your cost: €0.50 (0.50%)          │
│                                     │
│  [ See detailed breakdown ]         │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Asgaya is a P2P network         │
│     How can you contribute?         │
│                                     │
│  • Become a BCH seller (earn fees)  │
│  • Become a merchant (earn spread)  │
│  • Tell friends about Asgaya        │
│                                     │
│  [ Learn More ]                     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ┌─────────────────────────────┐    │
│  │      Send Another           │    │
│  └─────────────────────────────┘    │
│                                     │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Savings breakdown screen:**

```
┌─────────────────────────────────────┐
│ ◄ Back      Your Savings            │
├─────────────────────────────────────┤
│                                     │
│   💡 You saved €5.50! 🎉             │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   Your €100 transfer:               │
│                                     │
│   Asgaya:        €0.50  (0.50%) ✓   │
│                                     │
│   vs Traditional:                   │
│   Western Union: €5.00  (5.00%)     │
│   MoneyGram:     €4.50  (4.50%)     │
│   Bank wire:     €15.00 (15.0%)     │
│                                     │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   Cost Breakdown:                   │
│   You sent: €100.00                 │
│   Sender fee: €0.50 (0.5%)          │
│   Elena gets: €99.50 worth of BCH   │
│                                     │
│   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   Where the €0.50 goes:             │
│   • Merchant: ~€0.25 (spread)       │
│   • BCH seller: ~€0.25 (fee)        │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│   Why Bitcoin Cash?                 │
│   • Global settlement               │
│   • No intermediaries               │
│   • Network fees: ~€0.001           │
│   • Open protocol                   │
│                                     │
│  ┌─────────────────────────────┐    │
│  │   Share on Twitter          │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │   Tell a Friend             │    │
│  └─────────────────────────────┘    │
│                                     │
└─────────────────────────────────────┘
```

**Viral growth mechanism:**
- Dramatic savings shown (vs Western Union, MoneyGram, bank wire)
- Transparent cost breakdown (builds trust)
- Recruit BCH sellers/merchants (grow network)
- Easy sharing (pre-filled social media messages)

**Fee breakdown explanation:**
- **Sender pays:** €0.50 (0.5% of €100)
- **Elena gets:** €99.50 worth of BCH (calculated at maturity spot rate)
- **Merchant spread:** ~€0.25 (sells 50,000 VES for 0.0995 BCH worth ~€99.75)
- **BCH seller fee:** ~€0.25 (earned for posting collateral and service)

**Related decision:** [Fee Splitting Model](decisions/fee-splitting-model.md)

---

## Error States

### Network Error

```
┌─────────────────────────────────────┐
│           ⚠️ Connection Error       │
├─────────────────────────────────────┤
│                                     │
│  Couldn't connect to Asgaya servers │
│                                     │
│  Please check your internet         │
│  connection and try again.          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │        Try Again            │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Go to Home ]                     │
│                                     │
└─────────────────────────────────────┘
```

### Bizum Timeout (Sender Didn't Pay Within 5 Min)

```
┌─────────────────────────────────────┐
│           ⏰ Payment Expired         │
├─────────────────────────────────────┤
│                                     │
│  Your order #REM-89234 expired      │
│  because Bizum payment wasn't       │
│  received within 5 minutes.         │
│                                     │
│  No covenant was created.           │
│  No charges were made.              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  💡 Why 5 minutes?                  │
│  BCH sellers have volatility        │
│  exposure while waiting for your    │
│  Bizum. 5 minutes keeps their       │
│  risk low (0.5-1% typical).         │
│                                     │
│  ┌─────────────────────────────┐   │
│  │      Create New Order       │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Notes:**
- Timeout protects BCH seller from volatility exposure
- No covenant created = no charges
- Sender can retry immediately (create new order)

### Covenant Expired (Recipient Didn't Claim Within 24h)

```
┌─────────────────────────────────────┐
│           ⏰ Covenant Expired        │
├─────────────────────────────────────┤
│                                     │
│  Order #REM-89234 expired           │
│                                     │
│  Elena didn't claim the remittance  │
│  within 24 hours.                   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Split Refund:                      │
│                                     │
│  Merchant portion:   €99.50 ✓       │
│  (Refunded to you)                  │
│                                     │
│  Seller fee:         €0.50          │
│  (Kept by BCH seller - earned       │
│   for 24h service)                  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ℹ️  Seller fee covers:             │
│     - 24h collateral lock           │
│     - Volatility risk               │
│     - Service provision             │
│                                     │
│  Your BCH refund (~0.0995 BCH)      │
│  should arrive within minutes.      │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Contact: Elena (+58-412-XXX-5678)  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    Contact Elena            │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Create New Order ]               │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Notes:**
- **Split refund mechanism:**
  - Merchant portion (0.0995 BCH worth €99.50) → Refunded to you (Iris)
  - Seller fee (0.0005 BCH worth €0.50) → Kept by BCH seller
- **Why seller keeps fee:**
  - Seller posted BCH collateral for 24 hours
  - Seller had volatility risk exposure
  - Seller provided service (covenant infrastructure)
  - Incentive alignment: Sellers rewarded even if unused
- **Your net cost:** €0.50 (0.5% sender fee)
- **Refund timing:** Immediate (on-chain BCH transaction)
- Encourage sender to contact recipient (might try again with coordination)

**Related concept:** [Overcollateralized Bounty Contracts - Timeout Cascade](concepts/overcollateralized-bounty-contracts.md#timeout-cascade)

### Corridor Unavailable

```
┌─────────────────────────────────────┐
│       ❌ Not Available Yet           │
├─────────────────────────────────────┤
│                                     │
│  We don't serve Venezuela yet.      │
│                                     │
│  We're working on expanding to      │
│  more countries!                    │
│                                     │
│  💡 Know someone who could help?    │
│    Recommend us to merchants        │
│     in Venezuela.                   │
│                                     │
│  ┌─────────────────────────────┐    │
│  │   Refer a Merchant          │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │   Notify Me When Ready      │    │
│  └─────────────────────────────┘    │
│                                     │
│  Current corridors:                 │
│  • 🇪🇸 Spain → 🇦🇷 Argentina       │
│                                     │
│  [ Back ]                           │
│                                     │
└─────────────────────────────────────┘
```

**Note:** Turn limitation into growth opportunity (recruit participants)

---

## Why Build This Flow SECOND

### ✅ More Complex Architecture

**Participants:** 4 (vs 2-3 in payment flow)
- Sender (initiates remittance, pays BCH seller)
- BCH Seller (posts collateral, receives Bizum)
- Recipient (claims cash)
- Merchant (provides cash, earns spread)

**Additional complexity:**
- Recipient notification system (WhatsApp/Telegram/LINE)
- 24-hour claim window management
- Merchant discovery/selection (map, distance, ratings)
- Two-sided co-signing (merchant + recipient both sign)
- Failed claim scenarios (expired, merchant unavailable)
- Covenant timeout cascade (split refund logic)

### ✅ Covenant Architecture

**Payment flow:** Direct BCH transfer (simple)  
**Remittance flow:** Overcollateralized covenant (complex)

Requires understanding of:
- EUR-denominated futures contracts (settle in BCH)
- Overcollateralization mechanics (7% buffer)
- Timeout cascade (5-min Bizum, 24h claim, split refund)
- Co-signing mechanism (cryptographic, not numeric codes)

### ✅ Kickstarts Merchant Network

This is the **core innovation** that drives merchant adoption:
- Merchants earn spread from each claim (~1% of VES sold)
- Creates economic incentive to join network
- More merchants → more convenient for recipients → more remittances

**Payment flow helps BCH ecosystem broadly**  
**Remittance flow builds Asgaya-specific merchant network**

---

## Design Patterns & Components

### Reusable Components

**1. Progress Indicator**
```
████████░░░░░░░░  40%
```

**2. Status Icons**
```
⏳ Pending
🔄 Processing
✅ Complete
❌ Failed
⏸️  Waiting
```

**3. Amount Display**
```
€  100.00
[Large, centered, easy to read]
```

**4. Info Cards**
```
┌─────────────────────────────┐
│  Label: Value               │
│  Label: Value               │
└─────────────────────────────┘
```

**5. Action Buttons**
```
Primary (filled):
┌─────────────────────────────┐
│      Confirm & Pay          │
└─────────────────────────────┘

Secondary (outline):
[ Back to Home ]
```

---

## Navigation Patterns

**Back button behavior:**
- Always shown (top-left)
- Returns to previous screen
- Warns if order in progress ("Cancel order?")

**Home button:**
- Always accessible via hamburger menu (☰)
- Shows confirmation if order active

**Bottom navigation:**
- Home 🏠
- Orders 📋
- Profile 👤
- Help ❓

---

## Technical Notes

### 24-Hour Claim Window

**Timer starts:** When covenant funded by BCH seller (after Bizum received)  
**Timer visible:** In sender tracking screen, recipient notification  
**What happens at expiry:**
- Covenant marked as expired
- Split refund triggered:
  - Merchant portion (0.0995 BCH worth €99.50) → Sender's address (Iris)
  - Seller fee (0.0005 BCH worth €0.50) → BCH seller
- Recipient can no longer claim
- Sender and recipient both notified

**Related:** [Recipient Flows](android-app/flows/recipient-flows.md) - Recipient claim process

### Covenant Settlement Timing

**Step 1:** Sender pays Bizum to BCH seller (€100)  
**Step 2:** Seller posts BCH collateral to covenant (~0.107 BCH)  
**Step 3:** Recipient claims → Both co-sign → Covenant matures  
**Step 4:** BCH distributed (merchant gets 0.0995 BCH, seller gets surplus)

**Why EUR-denominated covenant?**
- Merchant always gets promised EUR value (€99.50 worth of BCH)
- Overcollateralization absorbs short-term volatility
- Seller hedges by receiving €100 fiat before price moves
- No rate locking needed (covenant calculates BCH at maturity rate)

**Related decision:** [How Exchange Rates Work](decisions/how-exchange-rates-work.md)

### Rate Locking

**Estimate shown:** When sender creates order (informational only)  
**Final rate:** When recipient claims at merchant (DolarAPI blue dollar rate)  
**Variance:** Usually <1% (blue dollar relatively stable short-term)

**Why lock at claim time, not payment time?**
- Covenant promises EUR value, settles in BCH at maturity rate
- Recipient gets latest market rate (fairness)
- Seller hedges volatility (Bizum received before price moves)
- Overcollateralization protects merchant (always gets €99.50 worth)

**Related decision:** [How Exchange Rates Work](decisions/how-exchange-rates-work.md)

---

## Related Documentation

**Flows:**
- [BCH Payment Flows](android-app/flows/bch-payment-flows.md) — Simpler flow, build FIRST
- [Recipient Flows](android-app/flows/recipient-flows.md) — Recipient claim process (6 screens)
- [Merchant Flows](android-app/flows/merchant-flows.md) — Merchant VES sale process (5 screens)
- [BCH Seller Flows](android-app/flows/bch-seller-flows.md) — Seller collateral posting

**Decisions:**
- [How Exchange Rates Work](decisions/how-exchange-rates-work.md) — EUR-denominated covenant, BCH settlement
- [Two-Step Settlement Timing](decisions/two-step-settlement-timing.md) — Covenant maturity timing
- [Fee Splitting Model](decisions/fee-splitting-model.md) — How fees distributed

**Concepts:**
- [Pull System](concepts/pull-system.md) — Recipient-driven settlement
- [Overcollateralized Bounty Contracts](concepts/overcollateralized-bounty-contracts.md) — Complete covenant specification
- [BCH Sellers](concepts/bch-sellers.md) — Seller role and hedge mechanism
- [Decentralized Pull System](concepts/decentralized-pull-system.md) — Bulletin board architecture

---

## Accessibility Notes

**Font sizes:**
- Headers: 24px
- Body: 16px
- Amounts: 32px (large, prominent)
- Small text: 12px (disclaimers)

**Colors:**
- Primary action: Blue (#0066CC)
- Success: Green (#28A745)
- Warning: Orange (#FFA500)
- Error: Red (#DC3545)
- Neutral: Gray (#6C757D)

**Touch targets:**
- Minimum 44×44px
- Buttons well-spaced (16px margin)

**Contrast:**
- WCAG AA compliant (4.5:1 ratio)
- Works in varying lighting conditions

---

*Flow documented: May 10, 2026*  
*Status: Active - Covenant Architecture*  
*Replaces: Escrow-era flows (10-min timeout, LP instant settlement, Kraken purchase)*
