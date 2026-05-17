# LP (Liquidity Provider) Flows - Fiat-to-Merchant Settlement

**⚠️ STALE - PENDING REVIEW ⚠️**
**Issue:** Uses escrow/LP architecture. LPs don't exist in covenant model.
**Valid concepts to salvage:**
- **Gamification**: Leaderboard, first-to-accept bounties, competitive rewards
- **BCH Buyers**: People who buy BCH from merchants (circular economy)
- **Merchant liquidity**: Merchants accumulate BCH → sell to BCH buyers → buyers become senders
**Status:** TO BE REWRITTEN AS "BCH BUYER FLOWS" WITH GAMIFICATION

**Part of:** [Android App Flows](android-app/flows/README.md)
**Date:** 2026-05-03
**Status:** ~~Active~~ STALE - Critical for instant settlement

---

## Overview

This document defines the screen-by-screen user experience for **Liquidity Providers (LPs)** in the Asgaya Android app.

**LP Role (CORRECTED):** Provide **local fiat currency** (VES, ARS, etc.) to merchants who selected instant settlement. LPs send fiat to merchant, receive BCH + reward from escrow.

**Value proposition:**
- 💰 **Earn BCH:** Get paid in BCH for providing fiat liquidity
- ⚡ **Instant settlements:** First to accept bounty wins
- 🎯 **Predictable rewards:** Know exactly how much you'll earn
- 🏆 **Gamification:** Leaderboard rankings
- 📱 **Low barrier:** Anyone with local fiat can participate

**How it works:**
```
1. Merchant claims remittance, selects instant settlement
2. LP gets bounty notification: "Earn 250 VES by sending 50,000 VES to merchant"
3. LP accepts bounty (5 min window)
4. LP sends fiat to merchant via local payment method
5. Merchant app parses notification, confirms receipt
6. Escrow sends BCH + reward to LP wallet
```

**Key principle:** LPs provide **fiat liquidity**, not BCH. They earn BCH by settling remittances quickly.

---

## Screen 1: LP Dashboard (Setup & Status)

### Purpose
Configure LP settings and show available bounties.

### Wireframe

```
┌─────────────────────────────────────┐
│  ☰    LP Dashboard            🔔 ⚙️ │
├─────────────────────────────────────┤
│                                     │
│  💰 Earn BCH Settling Remittances   │
│     Cashed at Merchants             │
│                                     │
│  [ How It Works? ]                  │◄─ Link to detailed explanation
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  📱 Payment Method Setup            │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Payment Method: PagoMóvil  │   │◄─ Dropdown: Bizum, PagoMóvil, Nequi, etc.
│  │  Currency: VES              │   │◄─ Auto-detected from payment method
│  │                             │   │
│  │  Phone: +58-412-XXX-XXXX    │   │◄─ LP's phone for receiving payment instructions
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  💵 Available Liquidity             │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Amount: 500,000 VES        │   │◄─ How much fiat LP has available
│  │                             │   │
│  │  (~€10 at current rate)     │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ✅ Active - Receiving bounties     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   [ Pause Notifications ]   │   │◄─ Stop receiving bounties
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  This Week:                         │
│  • 12 settlements completed         │
│  • +0.0034 BCH earned               │
│  • (~€115 at current rate)          │
│                                     │
│  📊 Leaderboard: #8  ↑2             │
│                                     │
│  [View Earnings]  [Leaderboard]     │
│                                     │
└─────────────────────────────────────┘
```

**Key Elements:**

**Payment Method:**
- 2 Dropdowns: one for country other for payment Bizum, PagoMóvil, Nequi, etc.
- Currency auto-detected (Bizum → EUR, PagoMóvil → VES)
- Pairs LP with merchants accepting same payment method

**Available Liquidity:**
- How much fiat LP has available to send
- Asgaya only shows bounties LP can afford
- **Automatically decreases when LP accepts bounty** (50k bounty → -50k from available)
- **Restored when settlement completes** (LP receives BCH, can sell for more fiat)
- LP can manually update this anytime (when they add more fiat)

**Active/Pause:**
- Active: LP receives bounty notifications
- Paused: LP doesn't receive notifications (useful if temporarily out of funds)

**This Week Summary:**
- Settlements completed
- BCH earned
- Leaderboard position

**Notes:**
- LP must configure payment method + amount before receiving bounties
- Asgaya matches LPs to merchants by payment method + corridor
- Amount tells system which bounties to show (don't show 100,000 VES bounty to LP with 50,000 VES)

---

## Screen 2: Bounty Notification (Incoming Settlement)

### Purpose
LP receives notification of available bounty. First to accept wins.

### Push Notification

```
┌─────────────────────────────────────┐
│  💰 Asgaya Bounty                   │
│                                     │
│  Earn 250 VES                       │
│  Send 50,000 VES to merchant        │
│                                     │
│  Tap to accept                      │
│  (First come, first served)         │
└─────────────────────────────────────┘
```

### In-App Screen (if LP opens notification)

```
┌─────────────────────────────────────┐
│  ◄ Back        New Bounty       ⏱️  │
├─────────────────────────────────────┤
│                                     │
│  💰 Bounty Available                │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Earn: 250 VES              │   │◄─ LP reward
│  │                             │   │
│  │  Send: 50,000 VES           │   │◄─ Amount to send to merchant
│  │                             │   │
│  │  Merchant: Bodega Caracas   │   │
│  │  Location: Valencia, VES    │   │
│  │                             │   │
│  │  BCH reward: 0.0012 BCH     │   │◄─ BCH equivalent of 250 VES
│  │  (~€0.42 at current rate)   │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  How it works:                      │
│  1. Accept bounty (5 min window)    │
│  2. Send 50,000 VES to merchant     │
│  3. Merchant confirms receipt       │
│  4. You receive 50,250 VES worth    │
│     of BCH (50k + 250 reward)       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ⏱️ Bounty expires in: 2 min        │◄─ Countdown (other LPs competing)
│                                     │
│  ┌─────────────────────────────┐   │
│  │      Accept Bounty          │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Decline ]                        │
│                                     │
└─────────────────────────────────────┘
```

**Key Elements:**

**Reward Clarity:**
- Earn: 250 VES (LP reward from fee split)
- Send: 50,000 VES (amount merchant claimed)
- Total BCH received: 50,250 VES worth

**Merchant Info:**
- Name and location (transparency)
- LP knows who they're sending to

**Bounty Timer:**
- Bounty available to multiple LPs
- First to accept wins
- Timer creates urgency

**Notes:**
- Multiple LPs can see same bounty
- First to tap "Accept Bounty" wins the settlement
- Creates competitive gamification

---

## Screen 3: Challenge Accepted (Payment Instructions)

### Purpose
LP accepted bounty. Now has 5 minutes to send fiat to merchant.

### Wireframe

```
┌─────────────────────────────────────┐
│  ◄ Back    Send Payment         ⏱️  │
├─────────────────────────────────────┤
│                                     │
│  💰 Bounty Accepted!                │
│                                     │
│  You have 5 minutes to send payment │
│  to merchant.                       │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  📱 Send PagoMóvil Payment          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  To: 0412-XXX-XXXX          │   │◄─ Merchant phone
│  │                             │   │
│  │  Amount: 50,000 VES [copy]  │   │
│  │                             │   │
│  │  Reference: REM-89234 [copy]│   │◄─ Remittance ID (for merchant matching)
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Instructions:                      │
│  1. Open your bank app              │
│  2. Send PagoMóvil with details     │
│  3. Return here when sent           │
│                                     │
│  ⏱️ Time remaining: 4:32            │◄─ 5-minute countdown
│     (Bounty released if timeout)    │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   I've Sent the Payment     │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Cancel (release bounty) ]        │
│                                     │
└─────────────────────────────────────┘
```

**Key Elements:**

**Payment Details:**
- Merchant phone (recipient)
- Exact amount (50,000 VES - no fee, LP earns from BCH reward)
- Reference (REM-89234 - merchant matches this to claim)

**5-Minute Timer:**
- LP has 5 min to complete payment
- After timeout, bounty released to other LPs
- Creates urgency but reasonable time

**Copy Buttons:**
- Reduce manual entry errors
- Amount and reference easy to copy

**Cancel Option:**
- LP can release bounty if they can't complete
- Bounty immediately available to other LPs
- No penalty for canceling (better than timeout)

**Notes:**
- Similar to sender payment instructions
- Tailored for LP's payment method (PagoMóvil in this example)
- If LP banking app sends notification, app could parse it as proof

---

## Screen 4: Waiting for Merchant Confirmation

### Purpose
LP sent payment, waiting for merchant to confirm receipt.

### Wireframe

```
┌─────────────────────────────────────┐
│         Waiting for Merchant        │
├─────────────────────────────────────┤
│                                     │
│  ⏳ Waiting for merchant to confirm │
│     payment received...             │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Spinner]           │   │
│  └─────────────────────────────┘   │
│                                     │
│  Bounty: REM-89234                  │
│  Merchant: Bodega Caracas           │
│  Amount sent: 50,000 VES            │
│  Your reward: 250 VES (0.0012 BCH)  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Usually takes 1-5 minutes          │
│                                     │
│  💡 Merchant will confirm when      │
│     their app parses the payment    │
│     notification from bank.         │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  [ Problem? Report Issue ]          │
│                                     │
└─────────────────────────────────────┘
```

**Key Elements:**

**Status:**
- Clear: waiting for merchant confirmation
- Estimated time: 1-5 minutes

**Details:**
- Bounty ID (REM-89234)
- Merchant name
- Amount sent
- Reward pending

**Automatic Confirmation:**
- Merchant app parses bank notification (PagoMóvil SMS, Bizum push, etc.)
- Merchant app sends confirmation to escrow
- Escrow releases BCH + reward to LP

**Issue Reporting:**
- If merchant doesn't confirm, LP can report
- Triggers manual review process

---

## Screen 5: Mission Accomplished (Settlement Complete)

### Purpose
Merchant confirmed, LP receives BCH reward.

### Wireframe

```
┌─────────────────────────────────────┐
│          ✅ Mission Accomplished!   │
├─────────────────────────────────────┤
│                                     │
│  🎉 Settlement Complete!            │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │         ✓                   │   │
│  │    Large checkmark          │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  Bounty: REM-89234                  │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│  You sent: 50,000 VES               │
│  You earned: 250 VES                │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│  BCH received: 0.0012 BCH           │
│  (~€0.42 at settlement rate)        │
│                                     │
│  50,250 VES worth of BCH sent       │
│  to your wallet!                    │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Time to complete: 3 minutes        │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   [ View Leaderboard ]      │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to Dashboard ]              │
│                                     │
└─────────────────────────────────────┘
```

**Key Elements:**

**Settlement Summary:**
- Amount sent (50,000 VES)
- Reward earned (250 VES)
- BCH received (0.0012 BCH = 50,250 VES worth)

**Transparency:**
- Total VES value converted to BCH
- EUR equivalent shown
- Time to complete tracked

**Next Action:**
- View leaderboard (gamification)
- Back to dashboard (find more bounties)

**Notes:**
- Escrow sent BCH directly to LP's wallet
- LP now has more BCH than before
- Can use BCH for payments or sell for more fiat liquidity

---

## Screen 6: Timer Runs Out (Failure Handling)

### Purpose
LP's 5-minute window expired. Handle gracefully.

### Wireframe

```
┌─────────────────────────────────────┐
│          ⏰ Time's Up               │
├─────────────────────────────────────┤
│                                     │
│  Your 5-minute window expired.      │
│                                     │
│  Bounty REM-89234 will be           │
│  published again for other LPs.     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  Did you send the payment?          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ✓ Yes, I sent it           │   │◄─ Ask merchant for manual confirmation
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ✗ No, I didn't send it     │   │◄─ Release bounty immediately
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  If you sent the payment:           │
│  • We'll ask merchant to confirm    │
│  • New 12-hour timer starts         │
│  • If merchant offline, you can     │
│    provide payment proof            │
│                                     │
└─────────────────────────────────────┘
```

### If LP says "Yes, I sent it"

```
┌─────────────────────────────────────┐
│       ⏳ Waiting for Merchant       │
├─────────────────────────────────────┤
│                                     │
│  We've asked the merchant to        │
│  manually confirm your payment.     │
│                                     │
│  New timer: 12 hours                │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  If merchant doesn't respond:       │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Upload Payment Proof       │   │◄─ Screenshot, bank statement
│  └─────────────────────────────┘   │
│                                     │
│  • Screenshot of payment sent       │
│  • Bank statement showing transfer  │
│  • SMS confirmation from bank       │
│                                     │
│  Transaction flagged for manual     │
│  review by escrow.                  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 Merchant might be offline       │
│     (power outage, no internet)     │
│                                     │
│  We'll check both:                  │
│  • Merchant confirmation, OR        │
│  • Your payment proof               │
│                                     │
│  Funds released after verification. │
│                                     │
└─────────────────────────────────────┘
```

**Failure Scenarios:**

**Scenario 1: LP didn't send payment**
- LP taps "No, I didn't send it"
- Bounty immediately released to other LPs
- No penalty for LP (better than timeout)

**Scenario 2: LP sent payment, merchant offline**
- LP taps "Yes, I sent it"
- System asks merchant for manual confirmation
- 12-hour timer (merchant might be offline due to power/internet)
- LP can upload payment proof (screenshot, bank statement)
- Escrow manually verifies

**Scenario 3: LP banking app sent notification**
- If LP's banking app sends confirmation SMS/push
- Asgaya app could parse it as proof
- Auto-confirm payment sent
- Escrow releases funds even if merchant offline

**Key Principle:**
- Don't punish LP for unreliable infrastructure (intermittent internet, power outages)
- Multiple verification paths: merchant confirmation OR payment proof OR banking app notification
- Manual escrow review as fallback

**Notes:**
- In problematic corridors (frequent merchant offline), can disable instant settlement
- LP knows upfront if corridor is reliable
- Leaderboard could show LP reliability score (% of successful settlements)

---

## Screen 7: Leaderboard

### Purpose
Gamification - show top LPs by settlements completed.

### Wireframe

```
┌─────────────────────────────────────┐
│  ←  LP Leaderboard              🏆  │
├─────────────────────────────────────┤
│                                     │
│  🏆 Top Liquidity Providers         │
│     (Last 30 days)                  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🥇 #1  Carlos_VEN           │   │
│  │    • 127 settlements         │   │
│  │    • 0.0456 BCH earned       │   │
│  │    • 98% success rate        │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🥈 #2  María_ARG            │   │
│  │    • 98 settlements          │   │
│  │    • 0.0389 BCH earned       │   │
│  │    • 99% success rate        │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🥉 #3  Pedro_VEN            │   │
│  │    • 87 settlements          │   │
│  │    • 0.0334 BCH earned       │   │
│  │    • 96% success rate        │   │
│  └─────────────────────────────┘   │
│                                     │
│  ...                                │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🎯 #8  You (Elena_VEN)      │   │◄─ Current user highlighted
│  │    • 52 settlements          │   │
│  │    • 0.0198 BCH earned       │   │
│  │    • 94% success rate        │   │
│  │    ↑2 from last week         │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ View My Earnings ]               │
│                                     │
└─────────────────────────────────────┘
```

**Key Elements:**

**Ranking Factors:**
- Settlements completed (primary)
- BCH earned (secondary)
- Success rate (reliability)

**Success Rate:**
- % of bounties accepted that completed successfully
- Low success rate = LP accepts but doesn't send payment
- Penalizes unreliable LPs

**Current User Highlight:**
- User's position clearly marked
- Movement from last week (↑2, ↓3, →)
- Motivates competition

**Notes:**
- Updated after every settlement
- Creates competitive dynamics (first to accept bounty)
- Could add corridor-specific leaderboards (VEN, ARG, etc.)

---

## Technical Notes

### Payment Method Matching

**LP Configuration:**
- Payment method: PagoMóvil
- Currency: VES (auto-detected)
- Available liquidity: 500,000 VES

**Merchant Configuration:**
- Payment method: PagoMóvil
- Instant settlement: YES

**System Matching:**
- Only show VES bounties to VES LPs
- Only match PagoMóvil LPs with PagoMóvil merchants
- Only show bounties ≤ LP's available liquidity

**Result:** LP sees relevant bounties only.

---

### Bounty Distribution

**Multiple LPs eligible:**
- All LPs with sufficient liquidity see bounty
- Push notification sent to all eligible LPs
- First to tap "Accept" wins
- Creates competitive urgency

**LP Selection Algorithm:**
1. Filter by payment method (PagoMóvil only)
2. Filter by corridor (VEN only)
3. Filter by available liquidity (≥ 50,000 VES)
4. Exclude LPs with pending settlements (locked during settlement)
5. Send notification to all matching LPs
6. First acceptance wins

**Self-Regulating System:**
- **Automatic liquidity deduction:** When LP accepts bounty, fiat amount deducted from available liquidity
  - LP starts with 500,000 VES
  - Accepts 50,000 VES bounty
  - Now has 450,000 VES available
  - Eventually runs out, must manually top up
- **Locked during settlement:** LP ineligible for new bounties while settlement pending
  - Prevents LP from accepting more than they can handle
  - Makes LPs careful about accepting
  - Natural rate limiting
- **Result:** Even highly successful LP eventually runs out, opening opportunities for others

**No round-robin needed** - liquidity constraints naturally distribute bounties across LPs.

---

### Failure Handling Philosophy

**Problem:** Remittances happen in places with unreliable infrastructure:
- Intermittent internet
- Power outages
- Slow payment processing

**Solutions:**

**1. 5-Minute Window (LP Payment):**
- Reasonable time to open banking app and send payment
- Not too long (merchant/recipient waiting)

**2. 12-Hour Window (Merchant Confirmation):**
- Merchant might be offline (power, internet)
- LP can provide payment proof if merchant doesn't respond
- Manual escrow review as fallback

**3. Multiple Verification Paths:**
- Merchant app confirms (ideal)
- LP provides payment proof (screenshot, bank statement)
- LP's banking app notification parsed (automatic proof)

**4. Corridor Reliability:**
- Track failure rate per corridor
- If instant settlement fails >20%, disable feature for that corridor
- Fallback to normal settlement (no LP, merchant waits for BCH)

**5. Merchant Penalty for Flagged Settlements:**
- If settlement flagged for manual review (merchant didn't confirm, LP provided payment proof)
- Merchant's instant settlement option is disabled
- Merchant can still cash out remittances (normal settlement)
- Re-enabled after manual review confirms merchant was offline/innocent
- **Incentivizes merchants to be responsive and reliable**

**Related decision:** [Two-Step Settlement](decisions/two-step-settlement-timing.md)

---

### LP Banking App Notification Parsing

**If LP's banking app sends confirmation:**

**Example (PagoMóvil SMS):**
```
Pago Movil enviado
Monto: 50,000 VES
A: 0412-XXX-XXXX
Referencia: REM-89234
```

**Asgaya app could:**
1. Request SMS permission (one-time)
2. Parse confirmation message
3. Match reference (REM-89234)
4. Auto-confirm payment sent
5. No need to wait for merchant (faster settlement)

**Benefits:**
- LP doesn't need to tap "I've sent payment"
- Automatic proof of payment
- Works even if merchant offline

**Privacy:**
- Only parse payment confirmations (not read all SMS)
- Optional feature (LP can decline SMS permission)

**Related:** [Notification Listener](../notification-listener/) architecture

---

## Related Documents

**Flows:**
- [Remittance Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md) — Why LP is needed
- [Recipient Flows](android-app/flows/recipient-flows.md) — Recipient claims at merchant
- [Merchant Flows](android-app/flows/merchant-flows.md) — Merchant confirms LP payment

**Decisions:**
- [Fee Splitting Model](decisions/fee-splitting-model.md) — Where LP reward comes from
- [Two-Step Settlement](decisions/two-step-settlement-timing.md) — Why instant settlement is optional

**Concepts:**
- [Market Making Partners](concepts/market-making-partners.md) — LP role in ecosystem

---

## Design Principles Applied

**✅ Low Barrier to Entry:**
- Anyone with local fiat can be an LP
- No BCH required upfront (earn BCH by providing fiat)
- Simple setup (payment method + amount)

**✅ Clear Value Proposition:**
- "Earn 250 VES by sending 50,000 VES"
- Exact reward shown before accepting
- Transparent BCH conversion

**✅ Gamification:**
- Leaderboard rankings
- First-to-accept competition
- Success rate tracking
- Weekly earnings summary

**✅ Failure Resilience:**
- Multiple verification paths
- 12-hour merchant confirmation window
- Payment proof upload option
- Manual escrow review fallback
- Corridor reliability tracking

**✅ No Burden on LP:**
- 5-minute window (reasonable)
- Can cancel without penalty
- Payment proof accepted if merchant offline
- Auto-parsing of banking notifications

---

## Problematic Corridors - Disable Instant Settlement

**If corridor has:**
- >20% LP timeout rate
- >20% merchant offline rate
- Frequent power/internet outages

**Action:**
- Disable instant settlement option for that corridor
- Merchants only get normal settlement (wait for BCH)
- No LP involvement
- Re-enable when infrastructure improves

**Rationale:**
- Don't frustrate LPs with unreliable settlements
- Don't lock LP funds in unconfirmed bounties
- Better to have slower but reliable settlement

**User sees:**
```
┌─────────────────────────────────────┐
│  Instant Settlement Unavailable     │
│                                     │
│  Due to unreliable infrastructure   │
│  in this corridor, instant          │
│  settlement is temporarily disabled.│
│                                     │
│  Your remittance will be processed  │
│  normally (10-30 min settlement).   │
│                                     │
│  [ Continue with Normal Settlement ]│
└─────────────────────────────────────┘
```

---

## Future Enhancements

### Merchant-Level Instant Settlement Eligibility (Beyond MVP)

**Current approach (MVP):**
- Disable instant settlement for entire corridor if >20% failure rate
- Simple, but too coarse-grained

**Future approach (post-beta):**
- Gauge **individual merchant reliability** instead of entire corridor
- Enable/disable instant settlement per merchant based on infrastructure

**Merchant Infrastructure Tiers:**

**Tier 1 - High Reliability (Instant Settlement Enabled):**
- Urban location
- Backup power (generator, UPS)
- Reliable internet (fiber, satellite backup)
- High success rate (>95%)
- **Example:** Established bodega in Valencia with backup generator

**Tier 2 - Medium Reliability (Instant Settlement with Warnings):**
- Semi-urban location
- No backup power but generally stable
- Mobile internet (4G)
- Moderate success rate (80-95%)
- **Example:** Small shop in suburban Caracas

**Tier 3 - Low Reliability (Instant Settlement Disabled):**
- Rural location
- Frequent power outages
- Intermittent internet
- Low success rate (<80%)
- **Example:** Informal market stall deep in Andes mountains

**Implementation Complexity:**
- Track per-merchant success rate
- Merchant self-reports infrastructure (honor system + verification)
- Automatic tier adjustment based on performance
- LP sees merchant tier before accepting bounty

**Why defer to post-MVP:**
- Adds significant complexity (merchant profiling, tier management, UI indicators)
- MVP can start with corridor-wide disable (simpler, faster to ship)
- Real-world data from beta will inform tier thresholds
- **Principle: Start simple, add nuance based on actual usage**

**When to implement:**
- After beta reveals which merchants are reliably responsive
- When corridor-wide disable becomes too restrictive
- When LP/merchant feedback shows need for granular control

**Related concept:** [Dynamic Reward Modulation](concepts/dynamic-reward-modulation.md) - Could adjust LP rewards based on merchant tier (higher reward for riskier Tier 2/3 merchants)

---

*Flow documented: May 3, 2026*
*Status: Corrected - LPs provide FIAT liquidity, not BCH*
*Key insight: Failure handling is critical for unreliable infrastructure*
*Future: Merchant-level reliability tiers instead of corridor-wide disable*
