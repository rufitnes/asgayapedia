# Home Screen - Asgaya Android App

**Part of:** [Android App Flows](README.md)  
**Date:** 2026-05-16  
**Status:** Active - Entry Point for All Flows

---

## Overview

The home screen is the **main entry point** for all Asgaya app functionality. It adapts based on user type and shows relevant actions.

**User types:**
1. **Sender** - Send money to other users
2. **Recipient** - Claim incoming remittances
3. **Merchant** - Provide cash-out service
4. **BCH Buyer/Seller** - Trade BCH in bulletin board

**Key principle:** One screen, all paths clearly separated by role.

---

## Screen Wireframe

```
┌─────────────────────────────────────┐
│      Asgaya                    ☰    │
├─────────────────────────────────────┤
│                                     │
│   👋 Welcome, María                 │
│   Balance: 0.15 BCH (~€150)         │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💸 Send & Receive                  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  📤 Send to Asgaya User     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  💳 Pay with Bitcoin Cash   │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  📥 Claim Money (1 pending) │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💰 Earn Money with Asgaya          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  📊 Trade BCH               │   │
│  │  (Buy/Sell in Bulletin)     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Recent Activity                    │
│  • Sent 0.1 BCH to Elena#142        │
│  • Paid 0.01 BCH to CafeRosa#789    │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

### Send & Receive (All Users)

**📤 Send to Asgaya User**
- Tap → Go to [sender-flows/](sender-flows/) (covenant-based remittance)
- Use case: Send money to family/friends in Asgaya network
- Features: Recipient chooses BCH or cash, 24h claim window

**💳 Pay with Bitcoin Cash**
- Tap → Go to [direct-payment-flows/](direct-payment-flows/)
- Use case: Pay merchants who accept BCH (outside Asgaya network)
- Features: Instant settlement, standard BCH transaction

**📥 Claim Money**
- Tap → Go to [recipient-flows.md](recipient-flows.md)
- Badge shows pending covenant count: `(1 pending)`
- Use case: Receive remittances sent to you
- Features: Claim as BCH (free) or cash at merchant (0.5% fee)

---

### Earn Money with Asgaya (Liquidity Providers)

**📊 Trade BCH**
- Tap → Go to [trade-bch-screen.md](trade-bch-screen.md)
- Use case: Provide liquidity, earn fees/spread
- Roles: BCH Seller, Merchant, BCH Buyer

---

### Recent Activity

Shows last 5 transactions:
- Sent covenants (with recipient Cash Account)
- Direct BCH payments (with merchant Cash Account)
- Claimed remittances (BCH or cash)
- Traded BCH (bought/sold in bulletin)

Tap any transaction → See details

---

## Navigation Map

```
Home Screen
├─ 📤 Send to Asgaya User → sender-flows/
│  ├─ covenant-setup/ (recipient, amount, payment method)
│  ├─ own-wallet-path/ (if sender has BCH)
│  └─ buy-seller-path/ (if sender buys BCH from bulletin)
│
├─ 💳 Pay with Bitcoin Cash → direct-payment-flows/
│  ├─ 1-scan-merchant.md
│  ├─ 2-enter-amount.md
│  ├─ 3-confirm-send.md
│  └─ 4-complete.md
│
├─ 📥 Claim Money → recipient-flows.md
│  ├─ Notification of incoming covenant
│  ├─ Choose: BCH (free) or Cash (0.5% fee)
│  └─ Merchant map (if cash chosen)
│
└─ 📊 Trade BCH → trade-bch-screen.md
   ├─ Sell BCH (post offer in bulletin)
   ├─ Buy BCH (enable merchant mode or buy online)
   └─ View bulletin board (all active offers)
```

---

## Balance Display

**Shows:**
- BCH balance (e.g., `0.15 BCH`)
- Fiat equivalent in user's currency (e.g., `~€150`)
- Updates in real-time

**Tap balance:**
- Go to wallet screen (view UTXOs, transaction history)
- See pending transactions
- Add BCH to wallet (via bulletin board)

---

## Notification Badges

**Claim Money badge:**
```
📥 Claim Money (1 pending)  ← Red badge with count
```

Shows number of pending covenants waiting to be claimed.

**Trade BCH badge (optional):**
```
📊 Trade BCH (3 offers matched)  ← Blue badge
```

Shows number of bulletin offers matching user's preferences (Phase 1+).

---

## User Type Adaptations

### Regular User (Sender/Recipient Only)
- Shows: Send & Receive section only
- Hides: Trade BCH section (until they enable it)

### Merchant
- Shows: All sections
- Trade BCH highlighted (primary income source)

### BCH Buyer/Seller
- Shows: All sections
- Trade BCH with active offer count

---

## Design Notes

### Why Single Home Screen (Not Tabs)?

**Phase 0 reasons:**
1. **Simplicity** - Clear role separation, one tap per function
2. **Clarity** - User knows exactly what they're doing
3. **Flexibility** - Easy to add/remove sections
4. **Trust-based** - Phase 0 users understand the app (family/friends)

**Future (Phase 1+):**
- Could add bottom tabs: Wallet | Trade | Profile
- Keep home screen as "quick actions" within each tab

### Why "Earn Money with Asgaya"?

**Better than:**
- ❌ "Merchant Services" - Sounds corporate
- ❌ "Liquidity Providers" - Too technical
- ❌ "Trade BCH" alone - Not clear you earn money

**✅ "Earn Money with Asgaya":**
- Clear value proposition
- Inviting (encourages adoption)
- Inclusive (anyone can earn)
- Phase 0 friendly (trusted network)

### Recent Activity Section

**Why include:**
- User sees app is working (tx history visible)
- Quick access to recent contacts (send again)
- Transparency (all actions logged)

**What to show:**
- Last 5 transactions
- Type (sent, paid, claimed, traded)
- Amount + recipient/merchant
- Tap for details

---

## Technical Notes

### Balance Fetching
```javascript
// Get user's BCH balance
const balance = await getUserBCHBalance(userAddress);
// Returns: { bch: 0.15, sats: 15000000 }

// Convert to fiat
const rates = await getExchangeRates();
const fiatBalance = balance.bch * rates.bchToUserCurrency;

// Display
displayBalance({
  bch: balance.bch.toFixed(8),
  fiat: fiatBalance.toFixed(2),
  currency: userCurrency // EUR, USD, VES, etc.
});
```

### Pending Covenants Count
```javascript
// Check for pending covenants to user's address
const pendingCovenants = await getCovenantsByRecipient(userAddress);
// Filter: status = "funded", not expired, not claimed
const count = pendingCovenants.filter(c => 
  c.status === "funded" && 
  c.expiresAt > Date.now() &&
  !c.claimed
).length;

// Show badge
if (count > 0) {
  showBadge("claim-money", count);
}
```

### Navigation Handler
```javascript
// Home screen button taps
const handleNavigation = (destination) => {
  switch(destination) {
    case "send-asgaya":
      navigate("sender-flows/covenant-setup/1-home");
      break;
    
    case "pay-bch":
      navigate("direct-payment-flows/1-scan-merchant");
      break;
    
    case "claim-money":
      navigate("recipient-flows");
      break;
    
    case "trade-bch":
      navigate("trade-bch-screen");
      break;
  }
};
```

---

## Related Documentation

- **[Sender Flows](sender-flows/)** - Covenant-based remittances
- **[Direct Payment Flows](direct-payment-flows/)** - Pay merchants with BCH
- **[Recipient Flows](recipient-flows.md)** - Claim incoming covenants
- **[Trade BCH Screen](trade-bch-screen.md)** - Bulletin board for liquidity providers

---

*Screen documented: 2026-05-16*  
*Status: Active - Main Entry Point*
