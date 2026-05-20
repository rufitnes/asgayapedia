# Merchant Flows - VES Cash Sales for BCH

**Part of:** [Android App Flows](../flows/README.md)
**Date:** 2026-05-10
**Status:** Active - Covenant Architecture

---

## Overview

This document details **every screen** a merchant sees when selling VES cash for BCH.

**Merchant profile:** Small shop owner (bodega, farmacia, minimarket) selling local currency for Bitcoin Cash

**Key principle:** Merchant sells VES for BCH - simple, trustworthy, profitable

**Total screens:** 4 main + 1 optional (bulletin board → confirm → hand cash & co-sign → complete → BCH buyer bulletin)

**Mental model:** Merchant is a **VES seller**, not a remittance facilitator. They see bounties offering BCH for VES cash.

---

## Merchant Perspective

**From merchant's point of view:**

```
Someone wants VES cash, offering BCH
├─ Bulletin board shows: "Wants: 500,000 VES | You get: 0.0995 BCH"
├─ Customer arrives, provides their Cash Account (Elena#142) to claim bounty
├─ Merchant decides if they have enough VES cash to sell
├─ If yes: Hand VES cash → Both co-sign covenant → BCH received
└─ If no: Decline bounty → Customer tries different merchant
```

**Merchant doesn't know:**
- Who sent the remittance (sender is anonymous to merchant)
- Why customer needs VES (could be family support, business, ATM-like withdrawal)
- Where BCH came from (covenant distributes from BCH seller's collateral)

**Merchant knows:**
- Customer wants to buy VES with BCH (via covenant mechanism)
- Exact VES amount to hand out
- Exact BCH amount merchant receives
- Current market rate (VES per BCH)

**Use case focus:** Documentation focuses on remittance use case (Iris sends to Elena). Other uses emerge naturally.

---

## Merchant Flow: VES Cash Sale for BCH

**Use case:** Elena walks into Bodega María to claim her remittance

**Complete flow:**
```
1. Merchant browses bulletin board (active bounties waiting)
2. Customer arrives, says: "I'm Elena#142"
3. Merchant enters Cash Account → Sees VES amount to sell & BCH to receive
4. Merchant confirms they have enough VES cash
5. Merchant hands VES cash to customer
6. Both co-sign covenant → Settlement triggered → Merchant receives BCH
7. Merchant chooses: Hold BCH (recommended) OR Sell BCH for fiat
```

**Time:** 2-5 minutes per sale

---

## Screen 1: Customer Lookup (Dashboard)

### Purpose
Customer walks in, merchant enters their Cash Account to see their active bounties.

### Wireframe

```
┌─────────────────────────────────────┐
│ ☰      Asgaya Merchant          👤  │
├─────────────────────────────────────┤
│                                     │
│  💰 VES Cash Out Service            │
│                                     │
│  Customer here to claim remittance? │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  👤 Enter Cash Account:     │   │
│  │                             │   │
│  │  Elena#142                  │   │◄─ Customer tells you their name
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         Look Up             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Customer says their Cash        │
│     Account name (e.g., Elena#142)  │
│     and you'll see their active     │
│     bounties to claim               │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  [ 📋 Browse All Bounties ]         │◄─ Optional: see market demand
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Today's Stats:                     │
│  💰 2 sales • 0.18 BCH earned       │
│  📈 ~912,000 VES value              │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- **Customer arrives:** Says "I'm Elena#142"
- **Merchant enters:** Type `Elena#142` in Cash Account field
- **Tap "Look Up":** Backend fetches all active bounties for Elena#142
- **If bounties found:** Go to Screen 1b (Select Bounty)
- **If no bounties:** Show error "No active bounties for Elena#142"
- **Optional:** Tap "Browse All Bounties" → See full bulletin board (market demand view)

**Why this is better:**
- ✅ **Direct lookup** - No browsing hundreds of bounties
- ✅ **No 4-digit codes** - Cash Account IS the identifier
- ✅ **Handles multiple remittances** - Elena might have 2-3 active bounties
- ✅ **More private** - Merchant only sees bounties for customers physically present
- ✅ **Faster** - Customer walks in → Says name → Merchant looks up → Done

**Validation:**
```
Input: Elena#142
Backend checks:
1. Valid Cash Account format? ✓ (Name#Number)
2. Resolve Cash Account → BCH address
3. Query active bounties for that address
4. Filter: Not claimed, not expired, funded by sender
5. Return list of active bounties

If bounties found → Show Screen 1b (Select Bounty)
If no bounties → Error message
```

**Error Handling:**

**No bounties found:**
```
┌─────────────────────────────────────┐
│         ⚠️ No Active Bounties       │
├─────────────────────────────────────┤
│                                     │
│  No active bounties for Elena#142   │
│                                     │
│  Possible reasons:                  │
│  • No remittances sent to this      │
│    Cash Account                     │
│  • All bounties already claimed     │
│  • Bounties expired (24h timeout)   │
│                                     │
│  Ask customer to verify their       │
│  Cash Account name                  │
│                                     │
│  [ OK - Back to Dashboard ]         │
│                                     │
└─────────────────────────────────────┘
```

**Invalid Cash Account:**
```
┌─────────────────────────────────────┐
│      ⚠️ Invalid Cash Account        │
├─────────────────────────────────────┤
│                                     │
│  Cannot resolve "Elena142"          │
│                                     │
│  Cash Accounts must include the     │
│  # symbol and number:               │
│  Example: Elena#142                 │
│                                     │
│  Ask customer for their full        │
│  Cash Account name                  │
│                                     │
│  [ OK - Back to Dashboard ]         │
│                                     │
└─────────────────────────────────────┘
```

---

## Screen 1b: Select Bounty (If Multiple Found)

### Purpose
Show all active bounties for the customer, let merchant select which one to process.

### Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back    Elena#142's Bounties      │
├─────────────────────────────────────┤
│                                     │
│  Active bounties for Elena#142:     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ From: Iris#5832             │   │
│  │ Amount: 500,000 VES         │   │
│  │ You get: 0.0995 BCH         │   │
│  │ Expires: 23h 15m            │   │
│  │ [ Process This ]            │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ From: Carlos#2341           │   │
│  │ Amount: 300,000 VES         │   │
│  │ You get: 0.0597 BCH         │   │
│  │ Expires: 12h 42m            │   │
│  │ [ Process This ]            │   │
│  └─────────────────────────────┘   │
│                                     │
│  💡 Customer may have multiple      │
│     remittances. Ask which sender   │
│     to confirm the right one.       │
│                                     │
│  [ Cancel - Back to Dashboard ]     │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- See all active bounties for this customer
- Ask customer: "Who sent you money? Iris or Carlos?"
- Customer confirms: "Iris sent it"
- Tap "Process This" on Iris's bounty → Go to Screen 2 (Confirm Sale)

**Why this matters:**
- Elena might receive remittances from multiple senders (family, friends, employer)
- All remittances go to same Elena#142 Cash Account
- Merchant helps Elena identify which remittance to claim
- After claiming one, others remain active for later

**Common case:**
- Usually only 1 active bounty per customer
- Screen 1b skipped automatically → Go straight to Screen 2
- Only shows if 2+ active bounties found

---

## Screen 2: Confirm VES Sale

### Purpose
Merchant sees exact VES amount to hand out, BCH amount to receive, current market rate.

### Wireframe

```
┌─────────────────────────────────────┐
│           Confirm VES Sale          │
├─────────────────────────────────────┤
│                                     │
│  ✅ Bounty #8923 verified!          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  🎯 Customer wants:         │   │
│  │                             │   │
│  │     500,000 VES             │   │◄─ Big, clear VES amount
│  │                             │   │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━   │   │
│  │                             │   │
│  │  💰 You receive:            │   │
│  │                             │   │
│  │     0.0995 BCH              │   │
│  │     (~505,000 VES value)    │   │◄─ VES equivalency shown
│  │                             │   │
│  │  Current rate: 1 BCH ≈      │   │
│  │  5,075,377 VES              │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Note: BCH worth ~1% more than   │
│  VES you hand out (your spread)     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Do you have 500,000 VES cash?      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ✅ Yes, I'll Sell          │   │◄─ Primary action
│  └─────────────────────────────┘   │
│                                     │
│  [ ❌ Cancel - Not Enough Cash ]    │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- **Merchant verifies they have VES cash:** Check register, safe
- **If yes:** Tap "Yes, I'll Sell" → Screen 3 (Hand Cash & Co-Sign)
- **If no:** Tap "Cancel" → Covenant returns to bulletin board (customer tries different merchant)

**Display Logic:**
- **VES-centric:** Primary display is VES amount (what merchant familiar with)
- **BCH amount:** Shown as "you receive" (what merchant earns)
- **VES equivalency:** Show BCH worth in VES (~505,000 VES value)
- **Spread visible:** Merchant sees they earn ~1% more than VES handed out

**Why VES equivalency matters:**
- Merchant thinks in VES (their daily currency)
- Shows profit: Hand out 500,000 VES → Get 505,000 VES worth of BCH
- Transparent: Merchant sees they earn ~1% spread

**Merchant flexibility:**
- **Can decline:** No penalty if insufficient liquidity
- **No commitment yet:** Just viewing bounty details
- **Customer's risk:** If declined, customer walks to different merchant

---

## Screen 3: Hand VES & Co-Sign Covenant

### Purpose
Merchant hands VES cash, both parties co-sign covenant to trigger settlement.

### Wireframe

```
┌─────────────────────────────────────┐
│      Hand Cash & Co-Sign            │
├─────────────────────────────────────┤
│                                     │
│  ⚠️ Safe Handout Instructions:      │
│                                     │
│  1. Count 500,000 VES carefully     │
│  2. Hand ALL cash to customer       │
│  3. After customer receives cash,   │
│     both of you co-sign covenant    │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Co-signing means:               │
│  - You confirm you gave the cash    │
│  - Customer confirms they received  │
│  - Both signatures trigger payment  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  🔒 Protection:                     │
│  Customer cannot complete without   │
│  your signature. You cannot get     │
│  paid without customer's signature. │
│                                     │
│  Both must agree sale is complete.  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ❓ What if customer doesn't show   │
│     up to claim?                    │
│                                     │
│  No problem! After 24 hours, the    │
│  covenant automatically refunds:    │
│  - Your portion → Back to sender    │
│  - You never gave cash → No loss    │
│                                     │
│  Your BCH is safe in the covenant.  │
│  Timeout protects you from no-shows.│
│                                     │
│  See: Timeout Cascade explained     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ⏸️  When you've handed cash:       │
│                                     │
│  ┌─────────────────────────────┐   │
│  │     Co-Sign Covenant        │   │◄─ Both tap this
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ⚠️ Only tap after handing cash!    │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**

**Step 1: Merchant hands cash**
- Count 500,000 VES carefully (avoid disputes)
- Hand ALL cash to customer physically
- Customer receives cash in hand

**Step 2: Both co-sign covenant**
- Merchant taps "Co-Sign Covenant" (signs with merchant private key)
- Customer taps "Co-Sign Covenant" on their device (signs with recipient private key)
- **Order doesn't matter:** Either party can sign first
- **Both required:** Covenant only settles when both signatures present

**Step 3: Settlement triggered**
- Backend verifies both signatures
- Covenant distributes BCH:
  - Merchant receives: 0.0995 BCH (to merchant wallet)
  - Seller receives: Surplus BCH (after merchant paid)
- Transaction complete → Screen 4

**Security model:**
- **Merchant protected:** Cannot complete without customer signature (prevents "I gave cash but customer denies")
- **Customer protected:** Cannot complete without merchant signature (prevents "customer claims completion without receiving cash")
- **Cryptographic:** Uses BCH Script covenant signatures (not numeric completion codes)

**Why both signatures:**
- **Accountability:** Both parties confirm the exchange happened
- **Self-enforcing:** Customer won't sign without cash, merchant won't sign without cash handed
- **Trustless:** No third party needed to verify (blockchain enforces)

**Alternative: RFID Card (Future)**
```
┌─────────────────────────────────────┐
│  💡 Alternative (Phase 1+):         │
│                                     │
│  If customer has RFID card:         │
│  - Hand cash to customer            │
│  - Customer taps card on device     │
│  - Card signature counts as         │
│    customer's co-sign               │
│  - Faster than app co-signing       │
│                                     │
└─────────────────────────────────────┘
```

**Post-MVP:** NFC-enabled merchant devices can read recipient RFID card signature directly.

**Waiting for customer to co-sign:**

```
┌─────────────────────────────────────┐
│     Waiting for Customer...         │
├─────────────────────────────────────┤
│                                     │
│  ⏳ You've signed. Waiting for      │
│  customer to co-sign...             │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Spinner]           │   │
│  └─────────────────────────────┘   │
│                                     │
│  Customer should tap "Co-Sign"      │
│  on their Asgaya app now            │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  This usually takes 10-30 seconds   │
│                                     │
│  If customer doesn't sign within    │
│  5 minutes, transaction cancels     │
│                                     │
└─────────────────────────────────────┘
```

**Timeout scenario:** See "Timeout & Error Scenarios" section below.

---

## Screen 4: Sale Complete & BCH Options

### Purpose
Show successful completion, BCH received, option to hold or sell BCH.

### Wireframe

```
┌─────────────────────────────────────┐
│       ✅ Sale Complete!             │
├─────────────────────────────────────┤
│                                     │
│  Transaction successful!            │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         ✓                   │   │
│  │    Large checkmark          │   │
│  └─────────────────────────────┘   │
│                                     │
│  Bounty: #8923                      │
│  VES handed out: 500,000 VES        │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💰 BCH Received:                   │
│                                     │
│  0.0995 BCH                         │
│  (~505,000 VES value)               │
│                                     │
│  Sent to: bitcoincash:qr5h...       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  What do you want to do?            │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  💎 Hold BCH (Recommended)  │   │◄─ Preferred option
│  └─────────────────────────────┘   │
│                                     │
│  💡 Holding BCH means:              │
│  - Use for suppliers accepting BCH  │
│  - Sell to BCH buyers when needed   │
│  - Participate in circular economy  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  [ 💵 Sell BCH Now ]                │◄─ Alternative
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Today's Total:                     │
│  3 sales • 0.28 BCH earned          │
│  (~1,421,000 VES value)             │
│                                     │
│  [ Back to Dashboard ]              │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**

**Option A: Hold BCH (Recommended)**
- Tap "Hold BCH" → Returns to dashboard
- BCH stays in merchant wallet
- Merchant can spend with suppliers or hold long-term
- Circular economy: Use BCH for business expenses

**Option B: Sell BCH Now**
- Tap "Sell BCH Now" → Screen 4a (BCH Buyer Bulletin)
- Shows P2P offers (Pagomóvil, cash buyers)
- Merchant can sell BCH for fiat if needed
- Trades small spread (~0.5-1%) for instant liquidity

**Settlement details:**
- BCH sent directly to merchant wallet (no intermediary)
- Amount shown in both BCH and VES equivalency
- Wallet address displayed (merchant can verify on blockchain)
- Transparent: Exact BCH amount, no hidden fees

**Stats & Gamification:**
- Today's total (sales + earnings in BCH + VES value)
- Motivates repeat participation
- Shows daily volume (merchant sees business value)

---

## Screen 4a: BCH Buyer Bulletin (Optional)

### Purpose
If merchant chooses to sell BCH, show P2P buyers offering fiat for BCH.

### Wireframe

```
┌─────────────────────────────────────┐
│ ←      BCH Buyer Bulletin           │
├─────────────────────────────────────┤
│                                     │
│  💵 Sell Your BCH                   │
│                                     │
│  You have: 0.0995 BCH               │
│  (~505,000 VES value)               │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Active buyers:                     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🟢 Buyer offers:            │   │
│  │ 500,000 VES (Pagomóvil)     │   │
│  │ For: 0.0995 BCH             │   │
│  │ Spread: ~1% below market    │   │
│  │ Payment: 2-5 min            │   │
│  │ [ Accept Offer ]            │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🟢 Buyer offers:            │   │
│  │ 498,000 VES (Cash in person)│   │
│  │ For: 0.0995 BCH             │   │
│  │ Location: 500m away         │   │
│  │ Spread: ~1.4% below market  │   │
│  │ [ Accept Offer ]            │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Note: Selling BCH means losing  │
│  ~1% to spread. Consider holding    │
│  for suppliers or future sales.     │
│                                     │
│  [ ← Back - Hold BCH Instead ]      │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**

**Merchant browses offers:**
- See all active BCH buyer offers
- Compare spreads (how much below market)
- Choose payment method (Pagomóvil instant vs cash in-person)

**Merchant accepts offer:**
- Tap "Accept Offer" on specific buyer
- **Recycled covenant flow:** Creates new covenant (merchant = seller, BCH buyer = recipient)
- Process identical to remittance covenant:
  1. Merchant posts BCH (0.0995 BCH)
  2. BCH buyer sends Pagomóvil (or arranges cash pickup)
  3. Both co-sign covenant
  4. Merchant receives VES fiat, BCH buyer receives BCH

**Why separate bulletin:**
- **Market separation:** BCH buyers are different role than remittance senders
- **Same mechanism:** Uses identical covenant infrastructure
- **Circular economy:** Enables BCH ↔ VES trades in both directions
- **Permissionless:** Anyone can be BCH buyer (no special onboarding)

**Neutral documentation:**
- Show feature functionality (bulletin board exists)
- Don't explicitly promote "decentralized ATM" use case
- Focus on supplier payment and circular economy uses
- Let users discover other uses naturally (BitTorrent analogy)

**Why merchant might sell:**
- **Immediate liquidity:** Need fiat for supplier that doesn't accept BCH
- **Risk management:** Prefer stable VES over BCH volatility
- **Cash flow:** Need working capital today, not tomorrow
- **Convenience:** Easier than finding BCH-accepting supplier

**Why merchant might hold:**
- **Better spread:** Avoid losing 1% to BCH buyer
- **Supplier payments:** Use BCH directly with suppliers accepting it
- **Accumulation:** Build BCH reserve for future use
- **Circular economy:** Participate in BCH-native commerce

**Post-MVP enhancement:**
- Merchant can set alert: "Notify me if buyer offers >99% of market rate"
- Merchant can make counter-offer: "I'll sell 0.0995 BCH for 502,000 VES"
- Merchant can schedule sale: "Sell my BCH tomorrow at market rate"

---

## Timeout & Error Scenarios

### Scenario 1: Merchant Doesn't Co-Sign (Elena's Theft Risk)

**What happened:** Merchant signed, but Elena never co-signs within 5 minutes

**Merchant perspective:**
```
┌─────────────────────────────────────┐
│      ⚠️ Customer Didn't Sign        │
├─────────────────────────────────────┤
│                                     │
│  You signed covenant, but customer  │
│  did not co-sign within 5 minutes   │
│                                     │
│  Transaction cancelled              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  What happened:                     │
│  • Customer left without signing    │
│  • Customer's app crashed           │
│  • Network issue prevented sign     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  If you gave cash but customer      │
│  didn't sign, this is theft.        │
│                                     │
│  Report to: support@asgaya.org      │
│                                     │
│  [ Report Theft ]  [ OK ]           │
│                                     │
└─────────────────────────────────────┘
```

**What actually happened:**
- Merchant signed (confirmed they gave cash)
- Elena didn't co-sign (within 5-minute window)
- **Timeout triggered:** Covenant cancels (neither party paid)
- **Merchant's risk:** If merchant gave cash, Elena has cash but covenant failed

**Prevention:**
- **Clear warnings:** "Only tap Co-Sign after handing cash"
- **Visual cues:** Big red warning on co-sign screen
- **Both signatures required:** Elena cannot complete without merchant's signature
- **Self-enforcing:** Elena won't leave without signing if she wants BCH

**Merchant protection:**
- Report to support (evidence: merchant signed, Elena didn't)
- Elena's covenant never matures (she doesn't get BCH)
- Future covenants with Elena flagged (reputation system)

**Why this is rare:**
- Elena wants her BCH (won't leave without completing)
- Merchant can see Elena's signature status in real-time
- 5-minute window is generous (most co-signs happen in 10-30 seconds)

---

### Scenario 2: Elena Doesn't Co-Sign (Merchant's Fraud Risk)

**What happened:** Merchant handed cash, but Elena claims she didn't receive

**Elena's perspective:**
```
┌─────────────────────────────────────┐
│    ⚠️ Merchant Didn't Give Cash?    │
├─────────────────────────────────────┤
│                                     │
│  If merchant did NOT give you cash, │
│  DO NOT co-sign covenant            │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Only co-sign if:                   │
│  ✅ You received full 500,000 VES   │
│  ✅ Cash is in your hand            │
│  ✅ You counted and verified        │
│                                     │
│  If merchant refuses to give cash:  │
│  [ Cancel Transaction ]             │
│                                     │
│  If you received cash:              │
│  [ Co-Sign Covenant ]               │
│                                     │
└─────────────────────────────────────┘
```

**Merchant's perspective:**
```
┌─────────────────────────────────────┐
│     ⚠️ Customer Refusing Sign       │
├─────────────────────────────────────┤
│                                     │
│  Customer is not co-signing         │
│                                     │
│  If you gave cash, explain to       │
│  customer they must co-sign         │
│                                     │
│  If customer still refuses:         │
│  [ Report Dispute ]                 │
│                                     │
│  If you didn't give cash yet:       │
│  [ Cancel Transaction ]             │
│                                     │
└─────────────────────────────────────┘
```

**What happens (Phase 0):**
- Covenant times out (24h) → Automatic split refund
  - Merchant portion (€99.50 BCH) → Sender (Iris gets BCH back)
  - Seller fee (€7.50 BCH) → BCH Seller
- No formal investigation or bans
- Sender (Iris) knows both merchant and Elena personally, can mediate offline
- Phase 0: Trusted parties only (family/friends), disputes should be rare (<1%)

**V1 Enhancement (Future):**
- Parties can post evidence on social media (`#AsgayaDispute`)
- Future users see evidence and decide merchant trustworthiness
- Community-driven reputation, no central judgment

**Should be RARE:**
- Both parties know other must sign (accountability)
- Merchant won't hand cash without confidence Elena will sign
- Elena won't walk in without intent to complete
- Phase 0: Trusted relationships prevent disputes

---

### Scenario 3: Covenant Already Claimed

**What happened:** Customer provides Cash Account, but covenant already claimed by different merchant

**Merchant sees:**
```
┌─────────────────────────────────────┐
│      ⚠️ Already Claimed             │
├─────────────────────────────────────┤
│                                     │
│  Covenant for Elena#142 was already │
│  claimed by another merchant        │
│                                     │
│  Completed: May 10, 2026 at 14:32   │
│  Merchant: Bodega Los Amigos        │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Customer might have:               │
│  • Already claimed at other shop    │
│  • Given you wrong Cash Account     │
│  • Different person's remittance    │
│                                     │
│  Ask customer to verify Cash Account│
│  in their Asgaya app                │
│                                     │
│  [ OK - Back to Dashboard ]         │
│                                     │
└─────────────────────────────────────┘
```

**Prevention:**
- Covenant can only settle once (blockchain enforced)
- Elena's app shows "Already claimed" if she tries to reuse it
- Merchant sees who claimed it (other merchant name/ID)

---

### Scenario 4: Covenant Expired (24h Timeout)

**What happened:** Covenant created 24 hours ago, Elena never claimed

**Merchant sees:**
```
┌─────────────────────────────────────┐
│          ⚠️ Expired                 │
├─────────────────────────────────────┤
│                                     │
│  Covenant for Elena#142 expired     │
│  (24-hour timeout reached)          │
│                                     │
│  Created: May 9, 2026 at 14:00      │
│  Expired: May 10, 2026 at 14:00     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Covenant refunded to sender        │
│  (split refund mechanism)           │
│                                     │
│  Customer should contact sender     │
│  to create new covenant             │
│                                     │
│  [ OK - Back to Dashboard ]         │
│                                     │
└─────────────────────────────────────┘
```

**What happened (backend):**
- Covenant reached 24h timeout without maturity (both signatures)
- **Split refund triggered:**
  - Merchant portion (0.0995 BCH) → Refunded to Iris's address
  - Seller fee (0.0075 BCH) → Sent to BCH seller (earned for providing service)
- Elena cannot claim anymore (covenant locked)
- Iris can create new covenant if Elena still needs funds

**Why split refund:**
- **Seller incentive:** Seller posted BCH for 24h, deserves fee even if unused
- **Iris's loss:** Iris paid sender fee (€0.50), lost to timeout
- **Merchant unaffected:** Never committed (didn't see bounty until customer arrived)

**Timeout rationale:**
- 24 hours is generous (most pickups happen within hours)
- Prevents indefinite BCH lock-up (sellers need capital freed)
- Forces inactive recipients to claim or forfeit

**See:** [With volatility buffer Bounty Contracts - Timeout Cascade](../../concepts/bounty-contracts-with-volatility-buffer.md#timeout-cascade) for complete timeout logic

---

## Technical Notes

### Cash Account Format & Validation

**Format:** Name#Number (e.g., Elena#142)

**Why Cash Accounts:**
- **Human-readable:** "Elena#142" instead of random codes
- **Global uniqueness:** BCH-native identity system (blockchain registered)
- **No database needed:** Resolves to BCH address via Cash Accounts protocol
- **Privacy-preserving:** Merchant doesn't need customer's phone number or ID
- **Verifiable:** Customer can prove ownership by signing with private key

**Resolution:**
```javascript
// Merchant enters: Elena#142
const cashAccount = "Elena#142";

// Resolve to BCH address
const address = await resolveCashAccount(cashAccount);
// Returns: bitcoincash:qr2x8w9t3fjlk042z2wv7hahsldgwhwy0rq9sywjpyy

// Check for active covenant to this address
const covenant = await getActiveCovenant(address);
```

**Backend validation:**
```
Cash Account entered: Elena#142

Backend checks:
1. Format valid? (Name#Number)
2. Resolves to valid BCH address?
3. Active covenant exists for this address?
4. Covenant funded by BCH seller?
5. Not already claimed? (covenant not mature yet)
6. Not expired? (< 24h since creation)

If all checks pass → Show confirm screen
Otherwise → Show specific error
```

**Collision handling:**
- Cash Accounts are globally unique (blockchain enforced)
- No two users can have the same Name#Number
- If merchant enters wrong account → Customer's name won't match, merchant notices

---

### Co-Signing Mechanism

**Cryptographic signatures (not completion codes):**
- Merchant signs with merchant private key (secp256k1)
- Recipient signs with recipient private key (secp256k1)
- Both signatures submitted to covenant
- Covenant verifies signatures on-chain (BCH Script)
- If valid → Settlement triggered (BCH distributed)

**Why cryptographic (not numeric codes):**
- **Security:** Cannot be spoofed (merchant can't fake Elena's signature)
- **Trustless:** Blockchain enforces (no central authority needed)
- **Simple UX:** Both tap "Co-Sign" button (app handles key management)
- **Accountability:** Both parties provably confirmed transaction

**Key management:**
- Merchant wallet: HD wallet (BIP32/44) stored on device
- Recipient wallet: Same (or RFID card in Phase 1+)
- Apps handle signing automatically (user just taps button)
- No manual key entry required

---

### Covenant Settlement Flow

**After both signatures:**
```
1. Both parties co-sign covenant (merchant + recipient)
2. Backend verifies both signatures present
3. Covenant distributes BCH:
   ├─ Merchant receives: 0.0995 BCH (promised amount)
   └─ Seller receives: 0.0045 BCH (surplus after merchant paid)
4. Transaction confirmed on BCH blockchain
5. Both apps show "Complete" screen
```

**On-chain verification:**
- Covenant is BCH Script (CashScript)
- Anyone can verify settlement on blockchain explorer
- Transparent: All amounts, signatures, timing public
- Immutable: Once settled, cannot be reversed

**See:** [With volatility buffer Bounty Contracts](../../concepts/bounty-contracts-with-volatility-buffer.md) for complete covenant specification

---

### BCH Buyer Bulletin (Separate Market)

**Why separate from remittance bulletin:**
- **Different role:** BCH buyers want BCH, remittance senders provide BCH
- **Same infrastructure:** Uses identical covenant mechanism
- **Circular economy:** Enables BCH ↔ VES trades in both directions
- **No artificial distinction:** Anyone can be buyer or seller

**How it works:**
1. Merchant wants to sell BCH for VES fiat
2. Merchant browses BCH buyer bulletin (people wanting BCH)
3. Merchant accepts offer (e.g., "I'll pay 500,000 VES for 0.0995 BCH")
4. **Recycled covenant:** Merchant posts BCH → BCH buyer sends Pagomóvil → Both co-sign
5. Merchant receives VES, BCH buyer receives BCH

**Pagomóvil buyer use cases:**
- Buy BCH to send remittance to family abroad
- Accumulate BCH for long-term holding
- Trade BCH (buy in Venezuela, sell elsewhere)
- **ATM-like use:** Cash out BCH at merchant (not explicitly documented)

**Documentation strategy:**
- Show bulletin exists (neutral feature documentation)
- Focus on remittance and circular economy uses
- Don't explicitly promote ATM/ToS violation use cases
- Let users discover other uses naturally
- **BitTorrent analogy:** Document file sharing, users discover piracy themselves

---

## Related Documentation

**Flows:**
- [Recipient Flows](recipient-flows.md) — Elena's perspective (other side of co-signing)
- [Sender Flows](sender-flows/) — Iris creates covenant and sends
- [Trade BCH Screen](trade-bch-screen.md) — How BCH sellers post offers in bulletin

**Decisions:**
- [How Exchange Rates Work](../../decisions/how-exchange-rates-work.md) — EUR-denominated covenant, BCH settlement
- [Fee Splitting Model](../../decisions/fee-splitting-model.md) — Where merchant spread comes from
- [Two-Step Settlement Timing](../../decisions/two-step-settlement-timing.md) — When BCH distributed

**Concepts:**
- [Pull System](../../concepts/pull-system.md) — How recipient timing control works
- [With volatility buffer Bounty Contracts](../../concepts/bounty-contracts-with-volatility-buffer.md) — Complete covenant specification
- [BCH Sellers](../../concepts/bch-sellers.md) — Who provides BCH and why they profit
- [Decentralized Pull System](../../concepts/pull-system.md) — How bulletin board works

---

## Design Principles Applied

**✅ VES-Centric Display:**
- Primary amounts shown in VES (merchant's daily currency)
- BCH shown as "you receive" with VES equivalency
- Spread visible (hand out 500K VES → get 505K VES worth of BCH)

**✅ Merchant = VES Seller:**
- Framed as "sell VES for BCH" not "help recipient cash out"
- Bounty bulletin shows demand for VES
- Merchant makes profit decision (not charity)

**✅ Simple & Fast:**
- 4 screens total (browse → confirm → hand & co-sign → complete)
- Code-only entry (4 digits, fast to type)
- Clear instructions at each step

**✅ Trustworthy:**
- Both signatures required (accountability)
- Subtle warnings ("only co-sign after handing cash")
- Timeout protection (5 min window)
- Dispute resolution process

**✅ Transparent:**
- Exact VES and BCH amounts shown
- Current market rate visible
- Spread calculated and displayed
- No hidden fees

**✅ Flexible:**
- Can decline bounty (no penalty)
- Can hold or sell BCH after sale
- BCH buyer bulletin for instant liquidity

---

*Flow documented: May 10, 2026*  
*Status: Active - Covenant Architecture*  
*Replaces: Escrow-era merchant flows (completion codes, LP instant settlement)*
