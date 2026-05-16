# Trade BCH Screen - Bulletin Board Hub

**Part of:** [Android App Flows](README.md)  
**Previous:** [Home Screen](home-screen.md)  
**Date:** 2026-05-16  
**Status:** Active - Liquidity Provider Hub

---

## Overview

The **Trade BCH screen** is the hub for all liquidity provider roles in Asgaya. Users can:
1. **Sell BCH** - Post offers to sell BCH for fiat
2. **Buy BCH** - Enable merchant mode (cash) or buy online (restricted Phase 0)
3. **View Bulletin** - Browse all active offers

**Mental model:** This is where you **earn money** by providing liquidity to the Asgaya ecosystem.

**Phase 0:** Controlled access for online buying (Asgaya contributors only)  
**Phase 1+:** Open to all users once corridors have momentum

---

## Screen Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back    Earn Money with Asgaya    │
├─────────────────────────────────────┤
│                                     │
│  💰 Provide liquidity, earn fees    │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  📢 Sell BCH                        │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  I have BCH, want fiat      │   │
│  │                             │   │
│  │  Earn: 0.5% per transaction │   │
│  │                             │   │
│  │  [ Post Sell Offer ]        │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  🎯 Buy BCH                         │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  💵 Provide Cash (Merchant) │   │
│  │  ⭐ Most needed!             │   │
│  │                             │   │
│  │  Accept walk-in recipients  │   │
│  │  Earn spread on each sale   │   │
│  │                             │   │
│  │  [ Enable Merchant Mode ]   │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  💳 Buy Online              │   │
│  │  🔒 Asgaya Contributors     │   │
│  │                             │   │
│  │  Phase 0: Restricted access │   │
│  │                             │   │
│  │  [ Request Access ]         │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  📊 Bulletin Board                  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  View All Active Offers     │   │
│  │  (15 sellers, 8 buyers)     │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

### 📢 Sell BCH

**Tap "Post Sell Offer":**
- Go to BCH Seller flow (to be documented)
- Create offer: Amount, price, payment methods accepted
- Post to bulletin board
- Wait for buyers to match

**Use case:**
- User has BCH (from mining, trading, or receiving remittances)
- Wants to convert to fiat
- Posts offer in bulletin
- Earns 0.5% fee when sender buys their BCH for covenant

**Example offer:**
```
Selling 1.0 BCH for €1,000
Payment methods: Bizum, SEPA
Fee: 0.5% (€5)
Available: Now
Location: Spain
```

---

### 🎯 Buy BCH - Merchant Mode

**Tap "Enable Merchant Mode":**
- Go to [merchant-flows.md](merchant-flows.md)
- Set up merchant profile (shop name, location, hours)
- Accept walk-in recipients with covenants
- Provide VES cash, receive BCH

**Use case:**
- User owns a shop (bodega, farmacia, minimarket)
- Has VES cash on hand
- Accepts recipients who want to cash out
- Earns spread from covenant settlement

**Example:**
```
Merchant: Bodega María
Location: Caracas, Venezuela
Hours: 8am - 8pm daily
Accepts: VES cash payments
Earns: ~0.5% spread per transaction
Volume: ~10 recipients/day = 1 BCH/week
```

**Why "Most needed!":**
- Merchants are critical for ecosystem
- Provide cash-out service (enables remittances)
- Phase 0 priority: Onboard merchants first

---

### 💳 Buy Online (Restricted Phase 0)

**Tap "Request Access":**
- Show access request form
- Options: Asgaya contributor, creating new corridor, referred by contributor
- Submit for manual review

**Phase 0 restriction:**
- Only vetted users can buy BCH online
- Prevents scams, ensures quality
- Protects sellers from fraud

**Access criteria:**
```
✅ Asgaya core team member
✅ Creating new corridor (e.g., EUR→ARS)
✅ Onboarded 5+ merchants (future: NFT incentive)
✅ Vouched by 2 existing contributors
```

**Phase 1+ (open):**
- Anyone can buy BCH online
- Reputation system prevents fraud
- Merchants still prioritized in UI

---

### 📊 Bulletin Board

**Tap "View All Active Offers":**
- Show list of all offers (buyers and sellers)
- Filter by: Location, payment method, amount, currency
- Sort by: Price, reputation, distance
- Tap offer → See details, match with offer

**Example bulletin:**
```
┌─────────────────────────────────────┐
│ ◄ Back    Bulletin Board       🔍   │
├─────────────────────────────────────┤
│  Filters: [All] [Buy] [Sell]       │
│  Location: [Caracas, VE ▼]          │
│  Payment: [All ▼]                   │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💵 SELL OFFERS                     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🟢 Seller#3421              │   │
│  │ Selling: 0.5 BCH            │   │
│  │ Price: €1,000/BCH           │   │
│  │ Payment: Bizum, SEPA        │   │
│  │ ⭐⭐⭐⭐⭐ (1,247 tx)         │   │
│  │ [ Match Offer ]             │   │
│  └─────────────────────────────┘   │
│                                     │
│  🎯 BUY OFFERS                      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🏪 Bodega María              │   │
│  │ Buying: Any amount          │   │
│  │ Payment: VES cash           │   │
│  │ Location: Caracas (0.5km)   │   │
│  │ ⭐⭐⭐⭐⭐ (247 tx)            │   │
│  │ [ Match Offer ]             │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

---

## Fee Structure (Phase 0 - To Be Validated)

### Sell BCH
```
Sender pays: 0.5% fee
BCH Seller earns: 0.5% of transaction

Example:
- Iris sends €100 remittance
- Needs 0.1 BCH (at €1,000/BCH rate)
- Seller provides: 0.1 BCH
- Seller earns: €0.50 (0.5% of €100)
```

### Buy BCH (Merchant)
```
Recipient pays: 0.5% fee
Merchant earns: ~0.5% spread

Example:
- Elena claims 500,000 VES covenant
- Merchant provides: 500,000 VES cash
- Merchant receives: 0.0995 BCH
- Merchant's cost: 497,500 VES (buys BCH at discount)
- Merchant's profit: 2,500 VES spread
```

**Total fees:** 1% (0.5% to seller + 0.5% to merchant)

---

## Phase 0 vs Phase 1+ Differences

| Feature | Phase 0 (Now) | Phase 1+ (Future) |
|---------|---------------|-------------------|
| **Sell BCH** | Open to all | Open to all |
| **Buy BCH (Merchant)** | Open to all (verified) | Open to all |
| **Buy Online** | Restricted (contributors only) | Open to all |
| **Access control** | Manual approval | Automatic (NFT/reputation) |
| **Gamification** | None | Leaderboards, badges, streaks |
| **Bulletin visibility** | All offers visible | Personalized (algo) |

---

## Design Notes

### Why Separate "Merchant" from "Buy Online"?

**Merchant = Special BCH Buyer:**
- Has physical location (brick-and-mortar)
- Provides CASH (not bank transfer)
- Critical for ecosystem (enables cash-out)
- Should be highlighted/prioritized

**Online Buyer = Regular BCH Buyer:**
- No physical location
- Provides fiat via bank transfer (Bizum, SEPA, PagoMóvil)
- Good for liquidity, but not critical
- Phase 0: Restricted to prevent fraud

### Why "Earn Money with Asgaya"?

**Messaging:**
- Positive framing (earn, not "provide liquidity")
- Clear value proposition (money, not just "help the network")
- Inviting (anyone can participate)

**Target audience:**
- Small shop owners (merchants)
- People with BCH holdings (sellers)
- Asgaya contributors (online buyers)

### Why Restrict Online Buying in Phase 0?

**Reasons:**
1. **Quality control** - Vetted users only, prevent scams
2. **Trust-based** - Phase 0 is family/friends/trusted network
3. **Merchant priority** - Want cash-out to dominate (not online trading)
4. **Learn optimal fees** - Validate 0.5% structure before scaling

**Opens up when:**
- 50+ successful transactions in corridor
- 10+ active merchants
- < 1% dispute rate
- Consistent liquidity

---

## Technical Notes

### Bulletin Board Data Structure
```javascript
// Bulletin board offer
const offer = {
  id: "offer_abc123",
  type: "sell" | "buy",
  user: "Seller#3421", // Cash Account
  amount: 0.5, // BCH
  price: 1000, // EUR per BCH
  paymentMethods: ["bizum", "sepa"],
  location: {
    country: "ES",
    city: "Madrid",
    coords: { lat: 40.4168, lng: -3.7038 }
  },
  status: "active" | "matched" | "completed",
  reputation: {
    stars: 4.9,
    transactions: 1247,
    successRate: 99.2
  },
  createdAt: 1715875200000,
  expiresAt: 1715961600000 // 24h
};
```

### Merchant Mode Activation
```javascript
// Enable merchant mode
const enableMerchantMode = async (merchantData) => {
  // Create merchant profile
  await db.merchants.create({
    userId: merchantData.cashAccount,
    shopName: merchantData.shopName,
    location: merchantData.location,
    hours: merchantData.hours,
    acceptsCash: true,
    acceptsOnline: false,
    status: "active"
  });
  
  // Post standing buy offer in bulletin
  await postBulletinOffer({
    type: "buy",
    user: merchantData.cashAccount,
    amount: "any", // Accept any amount
    paymentMethods: ["cash_VES"],
    location: merchantData.location,
    priority: "high" // Merchants show first
  });
  
  // Enable merchant dashboard
  navigate("merchant-flows/dashboard");
};
```

### Access Request (Phase 0)
```javascript
// Request online buyer access
const requestAccess = async (requestData) => {
  // Create access request
  const request = await db.accessRequests.create({
    userId: requestData.cashAccount,
    type: "online_buyer",
    reason: requestData.reason, // contributor, new corridor, referred
    referredBy: requestData.referredBy || null,
    status: "pending",
    createdAt: Date.now()
  });
  
  // Notify admin for manual review
  await notifyAdmin({
    type: "access_request",
    requestId: request.id,
    user: requestData.cashAccount
  });
  
  // Show confirmation to user
  showConfirmation({
    title: "Request Submitted",
    message: "We'll review your request within 24 hours."
  });
};
```

---

## Related Documentation

- **[Home Screen](home-screen.md)** - Entry point to Trade BCH
- **[Merchant Flows](merchant-flows.md)** - Enable merchant mode, accept recipients
- **[BCH Seller Flows]** - Post sell offers (to be documented)
- **[BCH Buyer Flows]** - Buy BCH online (to be documented)

---

## Future Enhancements (Phase 1+)

**NFT Incentive:**
- Onboard 5 merchants → Earn "Online Buyer Access" NFT
- NFT grants online buying permission
- Circular economy: Onboarders buy BCH from merchants they onboarded

**Gamification:**
- Leaderboards (top volume traders)
- Reputation system (stars, ratings, badges)
- Streaks (consecutive days providing liquidity)
- Bounties (special rewards for high-demand periods)

**Personalization:**
- Algorithmic bulletin sorting (show best matches first)
- Notifications for matching offers
- Saved preferences (payment methods, locations)

---

*Screen documented: 2026-05-16*  
*Status: Active - Liquidity Provider Hub*
