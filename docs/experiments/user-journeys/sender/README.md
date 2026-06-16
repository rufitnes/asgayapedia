# Sender Journey: The Active BCH Buyer

**Role:** BCH Buyer (Active)  
**Example:** María in Madrid (Spain) sending €100 to Elena in Caracas (Venezuela)

---

## Overview

A sender is someone who wants to send money across borders. In Asgaya's framework, they are simply **BCH buyers** who:
1. Use fiat (EUR) to buy BCH
2. Send that BCH to a recipient via a covenant
3. Pay a 1% total fee instead of 5-8% traditional remittances

**Mode:** Active - uses the app when they need to send money

---

## Step-by-Step: María Sends €100 to Elena

### 1. Open Wallet & Enter Recipient
- María opens her Asgaya wallet
- Enters Elena's Cash Account: `Elena#142`
- Specifies amount: €100

### 2. Create Covenant
- App creates a covenant (smart contract) on BCH blockchain
- Covenant terms:
  - Recipient: Elena's address
  - Amount: €100 worth of BCH
  - Expiry: 24 hours
  - Settlement options: Elena can claim BCH or cash out at merchant

### 3. Find BCH Seller on Bulletin Board
- App queries the bulletin board (NFTs on BCH blockchain)
- Filters: Accepts Bizum, has capacity for €100
- Shows list of available sellers with rates

### 4. Select Seller & Get Payment Instructions
- María selects Isabel (0.5% fee, 98% reputation)
- Via Nostr: Isabel sends payment instructions
  - Bank: Santander
  - Account: ES12 3456 7890 1234 5678
  - Reference: **Elena#142** (critical - this is how bot matches payment)
  - Amount: €100.50 (€100 + €0.50 fee)

### 5. Pay via Bizum
- María opens her banking app
- Sends €100.50 via Bizum to Isabel
- **Reference field:** Elena#142

### 6. Seller's Bot Detects Payment
- Isabel's notification bot monitors bank notifications
- Detects payment with reference `Elena#142`
- Matches to covenant using Cash Account lookup
- Automatically funds covenant with BCH

### 7. Elena Gets Notification
- Elena receives notification: "You have €100 worth of BCH"
- Options:
  - Claim to wallet (free)
  - Cash out at local merchant (0.5% fee)

**Total time:** 5 minutes - 4 hours (most is Elena's decision delay)  
**Total cost:** €0.50 (0.5% sender fee) + €0.50 (0.5% recipient fee if cash-out) = €1 total

---

## Active vs Passive

**María is an active user:**
- Opens app when she needs to send money
- Queries bulletin board each time
- Selects seller manually (or auto-selects best rate)
- Makes payment via banking app

**Contrast with passive merchant:**
- Posts listing once
- Bot handles everything automatically
- Earns money while sleeping

---

## Economics: Why María Uses Asgaya

### Cost Comparison

| Method | Fee | Time |
|--------|-----|------|
| Western Union | €5 (5%) | 1-2 days |
| Bank transfer | €8-15 (8-15%) | 3-5 days |
| Asgaya | €1 (1%) | 5 min - 4 hours |

### Benefits
- **90% cheaper** than traditional remittances
- **No KYC** - just payment rails María already uses (Bizum)
- **No company custody** - BCH moves peer-to-peer via covenants
- **Legal recourse** - payment is traceable via banking system

---

## What Prevents Fraud?

### Payment-First Covenant Model

**Key insight:** Seller never locks BCH until they receive fiat payment.

**Flow:**
1. María creates covenant (no BCH needed yet)
2. María pays Isabel €100.50 via Bizum (traceable, reversible)
3. Isabel's bot detects payment
4. Isabel's bot locks BCH into covenant
5. Elena can claim

**If Isabel ghosts:**
- María has Isabel's bank account from payment
- Payment is traceable via Bizum
- Legal recourse available (fraud via banking system)
- No BCH locked yet, so Isabel gains nothing

**Why this works:**
- Banking system acts as notary
- Fraud is criminal offense
- Personal payment information is traceable
- Covenant only executes after payment confirmed

---

## Edge Cases

### What if covenant expires before Elena claims?

**Scenario:** María sends €100, but Elena doesn't claim within 24 hours.

**Resolution:**
- Covenant has expiry clause
- If unclaimed: Isabel can reclaim BCH
- María has legal recourse (she paid, delivery failed)
- In practice: Elena gets push notification, rarely expires

### What if BCH price crashes during transaction?

**Scenario:** María pays €100, BCH drops 10% before covenant funded.

**Resolution:**
- Isabel locked 107% collateral (€107 worth of BCH)
- 7% volatility buffer absorbs typical swings
- If drop >7%: Isabel takes small loss, keeps transaction fees
- Covenant still delivers €100 worth to Elena (or slightly less)

**Isabel's risk management:**
- Diversify across many transactions
- Hedge with stability tokens (H€/HAu)
- Typical daily volatility: ±3%

### What if Nostr relay is down?

**Scenario:** María can't receive payment instructions via Nostr.

**Resolution:**
- App tries multiple relays (decentralized)
- Falls back to on-chain message (more expensive, slower)
- María can cancel covenant and try another seller

---

## User Experience Flow

```
[María's Phone]
  ↓
1. Open wallet → Enter Elena#142 → Amount €100
  ↓
2. App creates covenant on BCH blockchain
  ↓
3. Query bulletin board → Show sellers
  ↓
4. Select Isabel → Receive Nostr message with bank details
  ↓
5. Open banking app → Pay €100.50 to Isabel via Bizum
   Reference: Elena#142
  ↓
6. Wait 30 seconds - 5 minutes
  ↓
7. Notification: "Sent successfully! Elena will receive €100"
  ↓
[Done - Elena's turn to claim or cash out]
```

---

## Technical Details

**For implementation details, see:**
- [Wallet](/reference/android-app/wallet/) - Covenant creation
- [Bulletin Board](/reference/android-app/bulletin-board/) - Seller discovery
- [Nostr](/reference/android-app/nostr/) - Message coordination
- [Notification Bot](/reference/android-app/notification-bot/) - Payment matching

**For rationale, see:**
- [Why Payment-First?](/why-this-design/constraints/payment-first/)
- [Why Cash Accounts?](/why-this-design/requirements/identity/)
- [Why 7% Buffer?](/why-this-design/evidence/volatility/)

---

## Next Steps

**After sending, María might want to:**
- Send again (becomes recurring user)
- Recommend to friends (referral incentives)
- Become a seller herself (earn fees on reverse corridor)

**Related journeys:**
- [Recipient Journey](/user-journeys/recipient/) - Elena's perspective
- [Merchant Journey](/user-journeys/merchant/) - Carlos cashing out Elena
- [Trader Journey](/user-journeys/trader/) - Isabel's business model

---

**Status:** Phase 0 (Pre-Launch) - Q3 2026 Spain → Venezuela corridor  
**Updated:** 2026-06-16
