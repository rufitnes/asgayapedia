← [Back to Android App](android-app/README.md)

# Android App Flows

**Date:** 2026-05-16  
**Status:** Active - Covenant Architecture

This section documents all user flows in the Asgaya Android app, organized by actor and use case.

---

## Entry Point: Home Screen

All flows begin at the **[Home Screen](home-screen.md)**, which serves as the main navigation hub.

**Main sections:**
1. **Send & Receive** - For all users (send/pay/claim)
2. **Earn Money with Asgaya** - For liquidity providers (sell/buy BCH)

**Navigation:**
```
Home Screen
├─ 📤 Send to Asgaya User → Sender Flows
├─ 💳 Pay with Bitcoin Cash → Direct Payment Flows
├─ 📥 Claim Money → Recipient Flows
└─ 📊 Trade BCH → Trade BCH Screen (liquidity providers)
```

**[→ View home screen documentation](home-screen.md)**

---

## Core Flows (Send & Receive)

### [Sender Flows](sender-flows/) — Covenant Creation & Tracking

**Purpose:** Send BCH to Asgaya users (covenant-based remittances).

**Entry point:** Home → "Send to Asgaya User"

**Flow structure:**
- **Common setup:** Recipient selection → Amount entry → Payment method choice
- **Branch A:** Send from own wallet (3 screens, FREE)
- **Branch B:** Buy from seller (5 screens, 0.5% fee)
- **Tracking:** Monitor covenant state until recipient claims
- **Completion:** Success screen + savings breakdown

**Total screens:** 7-9 (depending on branch)

**Key features:**
- Two payment options (own BCH or buy from seller)
- Recipient chooses: BCH (free) or cash (0.5% fee)
- 24-hour claim window
- One pending covenant per (sender → recipient) pair
- Uses Cash Accounts (Elena#142)

**Participants:** Sender + BCH Seller (optional) + Recipient + Merchant (optional)

**[→ View detailed sender flows](sender-flows/)**

---

### [Direct BCH Payment](direct-payment-flows/) — Standard Wallet

**Purpose:** Pay merchants who accept BCH but are not in the Asgaya network.

**Entry point:** Home → "Pay with Bitcoin Cash"

**Flow structure:**
- **Linear path:** Scan merchant → Enter BCH amount → Confirm & send → Complete
- **Simple:** 4 screens + 2 error screens
- **Own balance only:** No mid-payment BCH purchase

**Key features:**
- Standard BCH transaction (no covenant)
- Instant settlement (~10 seconds)
- Minimal fees (€0.002 network fee only)
- BCH-first input (enter 0.01 BCH, see ~€10 equivalent)
- Balance check before broadcast
- Uses Cash Accounts (CafeRosa#789)

**Participants:** Sender + Merchant

**[→ View detailed direct payment flows](direct-payment-flows/)**

---

## Liquidity Provider Flows (Earn Money)

### [Trade BCH Screen](trade-bch-screen.md) — Bulletin Board Hub

**Purpose:** Hub for all liquidity provider roles (sell BCH, buy BCH, provide cash).

**Entry point:** Home → "Trade BCH"

**Three paths:**
1. **Sell BCH** - Post offers, earn 0.5% fee per transaction
2. **Buy BCH (Merchant Mode)** - Provide cash, earn spread ⭐ Most needed!
3. **Buy BCH (Online)** - Restricted to Asgaya contributors (Phase 0)

**Key features:**
- Bulletin board (view all active offers)
- Merchant mode prioritized in UI
- Phase 0: Controlled access for online buying
- Phase 1+: Open to all users

**Fee structure (to be validated):**
- BCH Seller earns: 0.5% from sender
- Merchant earns: ~0.5% spread from recipient
- Total: 1% (vs 5-10% Western Union/MoneyGram)

**[→ View trade BCH screen](trade-bch-screen.md)**

---

## Participant-Specific Flows

### [Recipient Flows](recipient-flows.md)

How recipients claim remittances.

**Screens:** 6 (notification → claim choice → merchant/BCH claim → success)

**Key features:**
- Choose: BCH (free) or cash at merchant (0.5% fee)
- 24-hour claim window
- Merchant map for cash-out locations
- Co-signing with merchant (if cash chosen)

---

### [Merchant Flows](merchant-flows.md)

How merchants provide cash-out services.

**Entry point:** Home → Trade BCH → "Enable Merchant Mode"

**Screens:** 4 (enter Cash Account → verify covenant → hand cash → co-sign)

**Key features:**
- Direct Cash Account lookup (Elena#142)
- Earn spread from covenant settlement (~0.5%)
- Co-sign covenant with recipient
- Can hold BCH or sell to BCH buyers in bulletin

**Why critical:**
- Merchants enable cash-out (core of remittance system)
- Phase 0 priority: Onboard merchants first
- Merchant = Special BCH buyer with physical location + cash

---

## Archived Flows

See **[archive/](archive/)** folder for deprecated flows.

### ⚠️ [LP Flows](archive/lp-flows.md) - ARCHIVED

**Archived:** 2026-05-16  
**Reason:** LP architecture removed in covenant model

**Replaced by:** [trade-bch-screen.md](trade-bch-screen.md) + future BCH Buyer flows

**Salvageable concepts:**
- Gamification (leaderboards, bounties, competitive rewards)
- BCH Buyer role (buy from merchants, close the loop)
- Reputation system

---

### ⚠️ [BCH Payment Flows](archive/bch-payment-flows.md) - ARCHIVED

**Archived:** 2026-05-16  
**Reason:** Escrow-based architecture replaced by covenant model

**Replaced by:** [direct-payment-flows/](direct-payment-flows/)

**See also:** [archive/README.md](archive/README.md) for all archived flows

---

## Complete Flow Hierarchy

```
📱 Asgaya Android App
│
├─ 🏠 Home Screen (home-screen.md)
│  │
│  ├─ 💸 Send & Receive
│  │  ├─ 📤 Send to Asgaya User → sender-flows/
│  │  │  ├─ covenant-setup/ (recipient, amount, payment method)
│  │  │  ├─ own-wallet-path/ (if sender has BCH)
│  │  │  ├─ buy-seller-path/ (if sender buys BCH)
│  │  │  └─ errors/ (4 error screens)
│  │  │
│  │  ├─ 💳 Pay with Bitcoin Cash → direct-payment-flows/
│  │  │  ├─ 1-scan-merchant.md
│  │  │  ├─ 2-enter-amount.md (currency selector + PoS auto-fill)
│  │  │  ├─ 3-confirm-send.md
│  │  │  ├─ 4-complete.md
│  │  │  └─ errors/ (insufficient balance, network errors)
│  │  │
│  │  └─ 📥 Claim Money → recipient-flows.md
│  │     ├─ Notification of incoming covenant
│  │     ├─ Choose: BCH (free) or Cash (0.5% fee)
│  │     └─ Merchant map (if cash chosen)
│  │
│  └─ 💰 Earn Money with Asgaya
│     └─ 📊 Trade BCH → trade-bch-screen.md
│        ├─ 📢 Sell BCH (post offer, earn 0.5%)
│        ├─ 🎯 Buy BCH - Merchant Mode → merchant-flows.md
│        ├─ 💳 Buy BCH - Online (restricted Phase 0)
│        └─ 📊 View Bulletin Board
│
└─ 📋 Archive
   ├─ lp-flows.md (to be rewritten as BCH Buyer flows)
   └─ bch-payment-flows.md (merged into direct-payment-flows/)
```

---

## User Types & Their Paths

**1. Sender (Iris):**
- Home → Send to Asgaya User OR Pay with Bitcoin Cash
- Uses: sender-flows/ or direct-payment-flows/

**2. Recipient (Elena):**
- Home → Claim Money (badge shows pending covenants)
- Uses: recipient-flows.md

**3. Merchant (Bodega María):**
- Home → Trade BCH → Enable Merchant Mode
- Uses: merchant-flows.md
- Can also: Sell accumulated BCH in bulletin

**4. BCH Seller:**
- Home → Trade BCH → Sell BCH
- Posts offers in bulletin
- Earns 0.5% per transaction

**5. BCH Buyer (Online):**
- Home → Trade BCH → Buy Online (Phase 0: restricted)
- Buys BCH from sellers in bulletin
- Phase 1+: Open to all users

---

## Phase 0 Strategy

**Open to all:**
- ✅ Send to Asgaya User (sender-flows/)
- ✅ Pay with Bitcoin Cash (direct-payment-flows/)
- ✅ Claim Money (recipient-flows.md)
- ✅ Sell BCH (trade-bch-screen.md → sell)
- ✅ Enable Merchant Mode (trade-bch-screen.md → merchant)

**Restricted (contributors only):**
- 🔒 Buy BCH Online (trade-bch-screen.md → buy online)
- Opens in Phase 1 when corridor has momentum

**Why restrict online buying:**
- Quality control (prevent scams)
- Merchant priority (want cash-out to dominate)
- Validate fee structure before scaling
- Trust-based Phase 0 (family/friends)

---

*Flow structure updated: 2026-05-16*  
*Entry point: home-screen.md*  
*Total documented screens: 30+ across all flows*
