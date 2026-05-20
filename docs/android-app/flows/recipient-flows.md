# Recipient Flows - Cash Claim Process

**Part of:** [Android App Flows](../flows/README.md)
**Date:** 2026-05-10
**Status:** Active - Covenant Architecture

---

## Overview

This document details **every screen** a recipient sees when claiming a remittance sent through Asgaya.

**Recipient Role:** Receive notification of remittance → Find merchant → Claim cash

**Key Principles:**
- ✅ Clear instructions (first-time users understand immediately)
- ✅ Map-based merchant discovery (visual, no addresses to type)
- ✅ 24-hour claim window (from covenant creation to merchant visit)
- ✅ Secure confirmation (cryptographic co-signing of covenant)
- ✅ Educational moments (show BCH benefits, encourage holding)

---

## Recipient User Journey

**Total screens:** 6
- Notification (push/SMS)
- Remittance details
- Merchant map
- Navigation to merchant
- Co-sign covenant
- Receipt/success

**User journey:**
```
Notification → View details → Find merchant → Navigate → Co-sign covenant → Receive BCH
```

**Timeline:**
- ⏱️ **24 hours** to claim after covenant created (timer starts when sender funds covenant)
- **Reminders sent at:** 12h (general), 18h (sender notified), 23h (urgent)
- **After 24h:** Covenant expires → Split refund (merchant portion to Iris, seller fee to seller)
- Full policy: [With volatility buffer Bounty Contracts - Timeout Cascade](../../concepts/bounty-contracts-with-volatility-buffer.md#timeout-cascade)

---

## Screen 1: Notification (Entry Point)

### Push Notification

```
┌─────────────────────────────────────┐
│  💰 Asgaya                          │
│                                     │
│  You received 500,000 VES           │
│  from Iris#5832                     │◄─ Sender Cash Account (if available)
│                                     │
│  Tap to claim at nearby merchant    │
│                                     │
│  Expires in 24 hours                │
└─────────────────────────────────────┘
```

### Messaging App Fallback (if app not installed)

**Sender chooses recipient's preferred messaging app:**

**WhatsApp/Telegram/LINE Message:**
```
💰 Asgaya Remittance

You received 500,000 VES from Iris#5832

📍 Claim at nearby merchant within 24h
🔑 Code: 8923

Download Asgaya app:
https://asgaya.org/app

Questions? Reply to this message.
```

**Interactions:**
- Tap notification → Opens app to Screen 2
- Tap download link → Installs app, opens to Screen 2
- Reply to message → Sender receives question (can help recipient)

**Notes:**
- Notification sent when covenant funded by BCH seller
- 24-hour timer starts immediately
- If app not installed, sender notifies via WhatsApp/Telegram/LINE (free, no SMS cost)
- Sender already has recipient phone number, likely connected on messaging app
- Message delivered when recipient has internet (more reliable than SMS)
- Recipient can ask sender questions if confused

**Why messaging apps, not SMS:**
- ✅ Free (SMS costs money per message)
- ✅ More reliable (delivered when internet available)
- ✅ Two-way communication (recipient can ask sender for help)
- ✅ Already used by sender/recipient (existing relationship)
- ✅ Rich formatting (links, emojis make message clearer)

---

## Screen 1.5: Claim Method Choice

### Purpose
Let recipient choose how to claim: instant BCH (free) or cash pickup (0.5% fee)

### Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back      Claim Your BCH          │
├─────────────────────────────────────┤
│                                     │
│   How do you want to claim?         │
│                                     │
│   You received: 0.0198 BCH          │
│   From: Iris#5832                   │◄─ Sender Cash Account
│   (~€9.90 worth)                    │
│                                     │
│  ┌───────────────────────────────┐  │
│  │                               │  │
│  │  🪙 Claim as BCH (Instant)    │  │◄─ Default/recommended
│  │                               │  │
│  │  ✓ FREE (no fees)             │  │
│  │  ✓ Available immediately      │  │
│  │  ✓ Co-sign and done           │  │
│  │                               │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │                               │  │
│  │  💵 Cash Pickup (24h window)  │  │◄─ Alternative
│  │                               │  │
│  │  0.5% fee (~€9.85 cash)       │  │
│  │  Find merchant on map         │  │
│  │  Walk to shop, claim cash     │  │
│  │                               │  │
│  └───────────────────────────────┘  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  💡 Most people claim BCH (free).   │◄─ Social proof
│     Cash pickup available if you    │
│     need physical money.            │
│                                     │
└─────────────────────────────────────┘
```

### Interactions

**User taps "🪙 Claim as BCH (Instant)":**
- Skip merchant map/navigation
- Go directly to Screen 5b (Co-Sign Covenant)
- No merchant involved (recipient co-signs with sender)
- Covenant matures instantly
- Recipient receives full BCH amount (no merchant fee)
- Total fees: 0.5% (sender fee only)

**User taps "💵 Cash Pickup (24h window)":**
- Go to Screen 2 (Remittance Details)
- Continue to Screen 3 (Merchant Map)
- Find merchant, navigate, claim cash
- Merchant co-signs covenant
- Merchant earns 0.5% fee
- Recipient gets cash minus 0.5% merchant fee
- Total fees: 1% (sender 0.5% + merchant 0.5%)

### Design Principles

**1. Default to BCH (Nudge without forcing):**
- ✅ BCH option shown first (top position)
- ✅ "FREE" badge (psychological advantage)
- ✅ Fewer steps (instant vs 24h window)
- ✅ No merchant coordination needed

**2. Cash option clearly available:**
- ✅ Shows fee (0.5%) upfront (no surprises)
- ✅ 24h window explained
- ✅ Not hidden or discouraged, just factual

**3. Educational moment:**
- "Most people claim BCH" (social proof)
- Explains why: free, instant, convenient
- Cash option for those who need it

### Fee Comparison

| Choice | Recipient Gets | Merchant Earns | Total Fees |
|--------|---------------|----------------|------------|
| **BCH Claim** | 0.0198 BCH (~€9.90) | €0 (no merchant) | 0.5% (sender only) |
| **Cash Pickup** | ~€9.85 cash | €0.05 (0.5% of €9.90) | 1% (sender + merchant) |

### Notes

- **Positioning:** Bitcoin wallet, not remittance service
- **Sender paid 0.5%:** Already paid to BCH Seller (sunk cost for recipient)
- **Recipient choice:** BCH free, cash costs 0.5% extra
- **Merchant incentive:** Only earn if recipient chooses cash (aligned)
- **BCH adoption:** Natural incentive to hold BCH (save 0.5% + instant)

**Why this works:**
- ✅ Transparent (fees shown upfront)
- ✅ Flexible (recipient controls method)
- ✅ Nudges BCH (free + instant)
- ✅ Doesn't force (cash available if needed)

---

## Screen 2: Remittance Details

### Purpose
Show recipient what they're claiming and where to claim it (merchant cash-out path only).

### Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Home      Remittance Details      │
├─────────────────────────────────────┤
│                                     │
│   💰 You Received Money!             │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Amount: 500,000 VES        │   │
│  │                             │   │
│  │  From: Iris#5832            │   │◄─ Sender Cash Account
│  │  (Your friend)              │   │
│  │                             │   │
│  │  Message: "Para la comida"  │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ⏱️ Claim within: 23h 45min         │
│     (Expires if unclaimed)          │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  📍 Find Merchant to Claim Cash     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    View Merchant Map        │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Tip: Keep it in Bitcoin Cash    │
│     instead! Pay anywhere, no fees. │
│                                     │
│  [ Learn about BCH ]                │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "View Merchant Map" → Go to Screen 3
- Tap "Learn about BCH" → Educational popup about holding BCH
- Countdown timer updates in real-time

**Notes:**
- Shows sender name and optional message (personal context)
- 24-hour countdown starts from covenant creation
- Educational nudge to keep BCH instead of cashing out
- Covenant transaction can be viewed on block explorer

---

## Screen 3: Merchant Map

### Purpose
Show nearby merchants where recipient can claim cash.

### Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back       Find Merchant      🔍  │
├─────────────────────────────────────┤
│                                     │
│  Claim: 500,000 VES                 │
│  Time left: 23h 42min               │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         MAP VIEW            │   │
│  │                             │   │
│  │    📍 You are here          │   │
│  │                             │   │
│  │    🏪 Bodega María          │   │
│  │       (450m) ⭐⭐⭐⭐⭐        │   │
│  │                             │   │
│  │    🏪 Farmacia Central      │   │
│  │       (1.2km) ⭐⭐⭐⭐         │   │
│  │                             │   │
│  │    🏪 Arepera Don José      │   │
│  │       (2.5km) ⭐⭐⭐⭐⭐        │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Merchants sorted by distance       │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🏪 Bodega María             │   │
│  │    450m away • ⭐⭐⭐⭐⭐       │   │
│  │    Open: 8am - 10pm         │   │
│  │    [ Navigate ]             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🏪 Farmacia Central         │   │
│  │    1.2km away • ⭐⭐⭐⭐        │   │
│  │    Open: 7am - 9pm          │   │
│  │    [ Navigate ]             │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap merchant pin on map → Shows merchant details
- Tap "Navigate" → Opens navigation app (Google Maps/Waze)
- Tap merchant card → Expands with more details (address, hours, rating)
- Search icon → Filter by name or location

**Notes:**
- Map shows recipient location + nearby merchants
- Merchants sorted by distance (closest first)
- Rating based on previous transactions (speed, reliability)
- Real-time open/closed status
- Can navigate to merchant via external app

---

## Screen 4: Navigation (External)

**Purpose:** Get recipient to merchant location

**Flow:**
- Tap "Navigate" → Opens Google Maps/Waze with merchant address
- Recipient walks/drives to merchant
- Returns to Asgaya app when arrived

**Notes:**
- Uses device's preferred navigation app
- Address pre-loaded, no typing needed
- Recipient can call merchant if needed (phone number in details)

---

## Screen 5a: Show Cash Account to Merchant

### Purpose
Display recipient's Cash Account for merchant to look up covenant.

### Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back      Claim Cash          ⚙️  │
├─────────────────────────────────────┤
│                                     │
│   🏪 Bodega María                    │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  You're claiming:           │   │
│  │  500,000 VES                │   │
│  │                             │   │
│  │  ━━━━━━━━━━━━━━━━━━━━━━     │   │
│  │                             │   │
│  │  👤 Your Cash Account:      │   │
│  │                             │   │
│  │      Elena#142              │   │◄─ Large text
│  │                             │   │
│  │  ┌─────────────────────┐   │   │
│  │  │  Copy: Elena#142    │   │   │
│  │  └─────────────────────┘   │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ⚠️ Instructions:                   │
│                                     │
│  1️⃣ Tell merchant: "Elena#142"      │
│  2️⃣ Merchant enters in their app    │
│  3️⃣ Merchant hands you cash         │
│  4️⃣ COUNT your cash carefully       │
│     (verify 500,000 VES)            │
│  5️⃣ After receiving cash, both      │
│     co-sign covenant to complete    │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Waiting for merchant to accept...  │
│  (This screen auto-advances when    │
│   merchant accepts your covenant)   │
│                                     │
│  [ Problem? Contact Support ]       │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Display Cash Account: Elena#142
- Tap "Copy" → Copies to clipboard
- Recipient tells Cash Account to merchant verbally
- When merchant accepts → Auto-advance to Screen 5b
- Tap "Contact Support" → Opens support chat/phone

**Notes:**
- Recipient doesn't tap anything to proceed (auto-advances)
- Cash Account identifies recipient's active covenant
- Merchant enters Cash Account, sees VES amount and BCH to receive
- After merchant hands cash → both co-sign covenant

**Cash Account format:**
- Format: Name#Number (e.g., Elena#142)
- Human-readable and easy to communicate verbally
- Resolves to BCH address on blockchain
- Merchant uses it to find active covenant for this recipient

---

## Screen 5b: Co-Sign Covenant

### Purpose
Recipient co-signs covenant to confirm cash receipt and trigger settlement.

### Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back    Confirm Receipt       ⚙️  │
├─────────────────────────────────────┤
│                                     │
│  ✅ Merchant accepted your bounty!  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ⚠️ CRITICAL - Safe Cash-Out:       │
│                                     │
│  1️⃣ Merchant hands you cash         │
│  2️⃣ COUNT cash: 500,000 VES         │
│  3️⃣ Verify amount is correct        │
│  4️⃣ Co-sign ONLY AFTER getting      │
│     ALL the cash in your hand       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 What is co-signing?             │
│                                     │
│  You and merchant both sign the     │
│  covenant with your private keys.   │
│  This triggers settlement:          │
│  - Merchant gets BCH for selling    │
│    VES to you                       │
│  - You get BCH from sender          │
│                                     │
│  Both signatures needed to complete │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  🔒 Protection:                     │
│  - Merchant cannot complete without │
│    your signature                   │
│  - You cannot complete without      │
│    merchant's signature             │
│  - Both must agree transaction is   │
│    complete                         │
│                                     │
│  Only tap after you have cash!      │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ❓ What if merchant doesn't give   │
│     me cash?                        │
│                                     │
│  DON'T co-sign. After 24 hours,     │
│  the covenant automatically refunds:│
│  - Your portion → Back to sender    │
│  - Seller fee → BCH seller          │
│                                     │
│  No one can steal funds by refusing │
│  to co-sign. The timeout protects   │
│  everyone.                          │
│                                     │
│  See: Timeout Cascade explained     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ⏸️  When you've received cash:     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ✅ Co-Sign Covenant        │   │◄─ Primary action
│  └─────────────────────────────┘   │
│                                     │
│  ⚠️ Only tap after receiving cash!  │
│                                     │
│  [ Problem? Cancel Transaction ]    │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**

**Step 1: Recipient receives cash**
- Merchant hands 500,000 VES cash to recipient
- Recipient counts cash (verify amount correct)
- Recipient confirms cash in hand

**Step 2: Recipient co-signs covenant**
- Recipient taps "Co-Sign Covenant"
- App signs covenant with recipient's private key (automatic)
- Signature submitted to blockchain

**Step 3: Waiting for merchant**
- If merchant hasn't signed yet, show waiting screen
- If merchant already signed, transaction completes immediately

**If merchant signed first (waiting for recipient):**
```
┌─────────────────────────────────────┐
│     Waiting for Merchant...         │
├─────────────────────────────────────┤
│                                     │
│  You've signed covenant             │
│                                     │
│  Waiting for merchant to co-sign... │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Spinner]           │   │
│  └─────────────────────────────┘   │
│                                     │
│  Both signatures trigger payment    │
│                                     │
│  This usually takes 10-30 seconds   │
│                                     │
│  If merchant doesn't sign within    │
│  5 minutes, transaction cancels     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  [ Cancel Transaction ]             │
│                                     │
└─────────────────────────────────────┘
```

**If both sign successfully:**
- Transaction completes → Go to Screen 6 (Success)
- Covenant distributes BCH to merchant and seller
- Recipient receives notification

**Security model:**
- **Recipient protected:** Cannot complete without merchant signature (merchant can't claim they gave cash if they didn't)
- **Merchant protected:** Cannot complete without recipient signature (recipient can't claim completion without merchant's consent)
- **Cryptographic:** Uses BCH Script covenant signatures (secp256k1)

**Why co-signing works:**
- **Accountability:** Both parties confirm the exchange happened
- **Self-enforcing:** Recipient won't sign without cash, merchant won't sign without cash handed
- **Trustless:** No third party needed to verify (blockchain enforces)
- **Simple UX:** Just tap button (app handles key management)

**Alternative: RFID Card (Future)**
```
┌─────────────────────────────────────┐
│  💡 Alternative (Phase 1+):         │
│                                     │
│  If you have an RFID card:          │
│  - Tap card on merchant's device    │
│  - Card signature counts as your    │
│    co-sign                          │
│  - Faster than app button           │
│  - Works even if phone offline      │
│                                     │
└─────────────────────────────────────┘
```

**Post-MVP:** NFC-enabled RFID cards for recipients without smartphones or unreliable internet.

---

## Screen 6: Success / Receipt

### Wireframe

```
┌─────────────────────────────────────┐
│            ✅ Success!               │
├─────────────────────────────────────┤
│                                     │
│   You received 500,000 VES!          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  From: Iris M.              │   │
│  │  Amount: 500,000 VES        │   │
│  │  Merchant: Bodega María     │   │
│  │  Date: May 10, 2026 3:45pm  │   │
│  │                             │   │
│  │  Transaction:               │   │
│  │  a3f2bc47...8d9c            │   │◄─ BCH txid
│  │  [ View on Explorer ]       │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 You also received Bitcoin Cash!  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  +0.0995 BCH                │   │
│  │  (~505,000 VES value)       │   │
│  │                             │   │
│  │  Keep it to pay anywhere    │   │
│  │  with near-zero fees!       │   │
│  │                             │   │
│  │  [ Learn More ]             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Make future remittances easier: │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Register Cash Account      │   │◄─ Optional onboarding
│  └─────────────────────────────┘   │
│                                     │
│  Get a name like Elena#142 so      │
│  senders can save you in contacts  │
│  for one-tap sending. Cost: ~$0.01 │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │      Back to Home           │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Share Receipt ]  [ Get Help ]    │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "Learn More" → Educational content about BCH
- Tap "Back to Home" → Return to main screen
- Tap "Share Receipt" → Share transaction details (screenshot or PDF)
- Tap "Get Help" → Support options

**Notes:**
- Shows cash received (500,000 VES) AND BCH received (0.0995 BCH)
- Educational moment about BCH benefits
- Receipt available for record-keeping
- Encourages holding BCH for future use

**What recipient actually received:**
- **Cash in hand:** 500,000 VES (from merchant)
- **BCH in wallet:** 0.0995 BCH (from covenant, worth ~505,000 VES at current rate)
- **Total value:** ~1,005,000 VES equivalent (if recipient holds BCH)

**Why recipient gets BCH:**
- Covenant distributes BCH to both merchant and recipient
- Recipient can hold BCH (recommended) or sell to BCH buyer
- BCH useful for future remittances (accumulate, send back to sender)
- Educational opportunity (introduce BCH to new users)

---

## Error States

### Expired Remittance

```
┌─────────────────────────────────────┐
│           ⏰ Expired                 │
├─────────────────────────────────────┤
│                                     │
│  This remittance expired because    │
│  it wasn't claimed within 24 hours. │
│                                     │
│  Covenant: a3f2bc47...8d9c          │
│  Amount: 500,000 VES                │
│  From: Iris M.                      │
│                                     │
│  Covenant timed out → Split refund: │
│  - Merchant portion → Sender (Iris) │
│  - Seller fee → BCH seller (earned) │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Contact Sender            │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**What happened:**
- Covenant reached 24-hour timeout without maturity (both signatures)
- **Split refund triggered:**
  - Merchant portion (0.0995 BCH) → Refunded to Iris's address
  - Seller fee (0.0075 BCH) → Sent to BCH seller (earned for providing service)
- Elena cannot claim anymore (covenant locked)
- Iris can create new covenant if Elena still needs funds

**Why split refund:**
- **Seller incentive:** Seller posted BCH for 24h, deserves fee even if unused
- **Iris's loss:** Iris paid sender fee (€0.50), lost to timeout
- **Merchant unaffected:** Never committed (didn't see bounty until recipient arrived)

**See:** [With volatility buffer Bounty Contracts - Timeout Cascade](../../concepts/bounty-contracts-with-volatility-buffer.md#timeout-cascade) for complete timeout logic

---

### Dispute Detected

```
┌─────────────────────────────────────┐
│          ⚠️ Dispute Detected        │
├─────────────────────────────────────┤
│                                     │
│  Merchant says they gave you cash,  │
│  but you say you didn't receive it. │
│                                     │
│  Your confirmation: NO ❌            │
│  Merchant's confirmation: YES ✓     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Transaction under review (24h max) │
│                                     │
│  📧 Submit evidence to:             │
│  support@asgaya.org                 │
│                                     │
│  Helpful evidence:                  │
│  • Your side of the story           │
│  • Photos (if no cash received)     │
│  • Witness statements               │
│  • Your location data               │
│                                     │
│  Covenant: a3f2bc47...8d9c          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Email Evidence Now         │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**What Happens (Phase 0):**
- Covenant times out (24h) → Automatic split refund
  - Sender gets merchant portion back (€99.50 BCH)
  - BCH seller keeps fee portion (€7.50 BCH)
- No formal investigation or arbitration
- Sender knows both parties personally (family/friends), can mediate offline
- Phase 0: Trusted merchants only, disputes should be rare (<1%)

**V1 Enhancement (Future):**
- Parties can post evidence on social media (`#AsgayaDispute`)
- Community sees evidence and decides merchant trustworthiness
- Full policy: [Dispute Resolution Framework](../../decisions/dispute-resolution.md)

**Should be RARE:** Co-signing mechanism prevents most disputes (both parties must confirm).

**How disputes happen:**
- **Merchant signed but didn't give cash:** Rare (merchant knows recipient won't sign without cash)
- **Recipient signed but claims no cash:** Rare (why would recipient sign?)
- **Technical glitch:** One party thinks they signed, but signature didn't submit
- **Misunderstanding:** Wrong amount given, partial cash, etc.

**Phase 0 mitigation:** Trusted merchants only (sender's family/friends), personal accountability.

---

### Merchant Not Available

```
┌─────────────────────────────────────┐
│      ⚠️ Merchant Unavailable         │
├─────────────────────────────────────┤
│                                     │
│  Bodega María is currently          │
│  unavailable or offline.            │
│                                     │
│  Try another merchant:              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🏪 Farmacia Central         │   │
│  │    1.2km away • Open now    │   │
│  │    [ Navigate ]             │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ View All Merchants ]             │
│                                     │
│  Time left to claim: 22h 15min      │
│                                     │
└─────────────────────────────────────┘
```

**What happened:**
- Merchant's app is offline
- Merchant closed shop temporarily
- Merchant has insufficient VES cash to sell

**Solution:**
- Recipient tries different merchant
- All merchants see same bounty on bulletin board
- First merchant to accept bounty wins (race condition)

---

### Already Claimed

```
┌─────────────────────────────────────┐
│         ✅ Already Claimed           │
├─────────────────────────────────────┤
│                                     │
│  This remittance was already        │
│  claimed on May 10, 2026 at 3:45pm. │
│                                     │
│  Merchant: Bodega María             │
│  Amount: 500,000 VES                │
│                                     │
│  If you didn't claim this, contact  │
│  support immediately.               │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Contact Support           │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ View Receipt ]  [ Back to Home ] │
│                                     │
└─────────────────────────────────────┘
```

**What happened:**
- Covenant already matured (both parties co-signed)
- Someone else claimed with recipient's code
- **Security concern:** If recipient didn't claim, someone stole their code

**Prevention:**
- Bounty codes should only be shared with merchant in person
- Don't share code via messaging (someone could intercept)
- Report theft immediately (freeze account, investigate)

---

### Timeout Waiting for Merchant

```
┌─────────────────────────────────────┐
│      ⏰ Merchant Didn't Sign        │
├─────────────────────────────────────┤
│                                     │
│  You signed covenant, but merchant  │
│  did not sign within 5 minutes      │
│                                     │
│  Transaction cancelled              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  What probably happened:            │
│  • Merchant changed their mind      │
│  • Merchant app crashed             │
│  • Network issue prevented sign     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ⚠️ If merchant gave you cash but   │
│  didn't sign, report theft:         │
│                                     │
│  [ Report Theft ]                   │
│                                     │
│  Otherwise, try different merchant: │
│                                     │
│  [ Find Another Merchant ]          │
│                                     │
└─────────────────────────────────────┘
```

**What happened:**
- Recipient signed covenant (confirmed cash received)
- Merchant didn't sign within 5-minute window
- **Timeout triggered:** Covenant cancels (neither party paid)

**Recipient's risk:**
- **If merchant gave cash:** Merchant has cash but covenant didn't complete (theft)
- **If merchant didn't give cash:** No loss (recipient shouldn't have signed anyway)

**Prevention:**
- **Clear warnings:** "Only co-sign after receiving cash"
- **Visual cues:** Big red warning on co-sign screen
- **5-minute window:** Generous time for merchant to sign after recipient
- **Self-enforcing:** Recipient won't sign without cash in hand

**Recovery:**
- Report to support (evidence: recipient signed, merchant didn't)
- Merchant flagged (reputation system)
- Recipient tries different merchant
- Covenant still valid (recipient can claim with different merchant)

---

## Technical Notes

### 24-Hour Claim Window

**Timer starts:** When covenant funded by BCH seller  
**Timer visible:** In notification, remittance details, map screen, claim screen  
**What happens at expiry:**
- Covenant marked as expired
- Split refund triggered (merchant portion to Iris, seller fee to seller)
- Recipient can no longer claim
- Sender notified of expiry

**Why 24 hours:**
- Gives recipient time to get to merchant
- Not so long that funds are locked unnecessarily
- Balances recipient convenience vs sender risk

**Related decision:** [With volatility buffer Bounty Contracts - Timeout Cascade](../../concepts/bounty-contracts-with-volatility-buffer.md#timeout-cascade)

---

### Merchant Discovery

**Data sources:**
- Merchant location (GPS coordinates)
- Merchant availability (online/offline status)
- Merchant rating (from previous transactions)
- Merchant hours (business hours)

**Sorting logic:**
1. Distance (closest first)
2. Rating (higher rated preferred)
3. Availability (online merchants first)

**Bulletin board:**
- All merchants see same bounties (public covenant bulletin)
- First merchant to enter recipient's code "claims" the bounty
- Other merchants see "already claimed" if they try same code

---

### Co-Signing Mechanism

**Cryptographic signatures (not completion codes):**
- Recipient signs with recipient private key (secp256k1)
- Merchant signs with merchant private key (secp256k1)
- Both signatures submitted to covenant
- Covenant verifies signatures on-chain (BCH Script)
- If valid → Settlement triggered (BCH distributed)

**Why cryptographic (not numeric codes):**
- **Security:** Cannot be spoofed (neither party can fake the other's signature)
- **Trustless:** Blockchain enforces (no central authority needed)
- **Simple UX:** Tap "Co-Sign" button (app handles key management)
- **Accountability:** Both parties provably confirmed transaction

**Key management:**
- Recipient wallet: HD wallet (BIP32/44) stored on device
- Merchant wallet: Same (or NFC device in Phase 1+)
- Apps handle signing automatically (user just taps button)
- No manual key entry required

**Signing order:**
- Either party can sign first (order doesn't matter)
- Covenant waits for both signatures
- 5-minute timeout if one party signs but other doesn't
- After 5 minutes, unsigned covenant can be cancelled

---

### Covenant Settlement Flow

**After both signatures:**
```
1. Both parties co-sign covenant (recipient + merchant)
2. Backend verifies both signatures present
3. Covenant distributes BCH:
   ├─ Merchant receives: 0.0995 BCH (promised amount)
   ├─ Recipient receives: (if any BCH allocated to recipient by sender)
   └─ Seller receives: Surplus BCH (after merchant paid)
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

### API Flow

**Covenant state machine:**
```
1. POST /recipient/claim-bounty {bounty_code} → Recipient enters code
2. POST /merchant/accept-bounty {bounty_code} → Merchant accepts
3. POST /recipient/sign-covenant → Recipient signs
4. POST /merchant/sign-covenant → Merchant signs
5. Backend verifies both signatures → Covenant matures → BCH distributed
```

**State transitions:**
```
pending → claimed → accepted → partially_signed → completed
                                    ↓
                               timed_out (if only one signature after 5 min)
```

---

## Related Documentation

**Flows:**
- [Sender Flows](sender-flows/) — How Iris creates covenant and sends
- [Merchant Flows](merchant-flows.md) — How merchant sells VES for BCH (other side of co-signing)
- [Trade BCH Screen](trade-bch-screen.md) — How BCH sellers post offers in bulletin

**Decisions:**
- [How Exchange Rates Work](../../decisions/how-exchange-rates-work.md) — EUR-denominated covenant, BCH settlement
- [Two-Step Settlement Timing](../../decisions/two-step-settlement-timing.md) — When BCH distributed
- [Fee Splitting Model](../../decisions/fee-splitting-model.md) — How fees work

**Concepts:**
- [Pull System](../../concepts/pull-system.md) — How recipient timing control works
- [With volatility buffer Bounty Contracts](../../concepts/bounty-contracts-with-volatility-buffer.md) — Complete covenant specification
- [Decentralized Pull System](../../concepts/pull-system.md) — How bulletin board works

---

## Design Principles Applied

**✅ Clear Instructions:**
- Step-by-step on claim screen
- Visual map for merchant discovery
- Countdown timer creates urgency

**✅ 24-Hour Claim Window:**
- Balances recipient convenience vs sender risk
- Timer visible throughout flow
- Clear expiry consequences

**✅ Secure Confirmation:**
- Cryptographic co-signing (both signatures required)
- Balanced power between merchant and recipient
- RFID card alternative planned (Phase 1+)

**✅ Educational Moments:**
- Success screen shows BCH received
- Encourages holding BCH for future use
- "Learn More" buttons throughout

**✅ Bounty Code System:**
- 4-digit code (easy to remember)
- Links recipient to specific covenant
- Fast to type and tell merchant

---

*Flow documented: May 10, 2026*  
*Status: Active - Covenant Architecture*  
*Replaces: Escrow-era transaction codes and completion codes*
