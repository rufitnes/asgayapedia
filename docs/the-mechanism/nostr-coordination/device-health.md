# Device Health Checks: Proactive Fraud Prevention
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**Know your seller's ready before you pay.** Device health prevents fraud before it happens.

---

## What It Is

**Device health checks** are diagnostic metrics sent by the seller's bot as part of the Nostr payment info response. They tell the buyer whether the seller's device is in good condition to receive bank notifications and lock BCH.

When María requests payment details from a seller, she doesn't just get a bank account number—she also gets:
- ✅ Bank app installed and enabled
- ✅ Battery at 45%, charging
- ✅ Notifications working properly

**If the seller's device is unhealthy** (bank app disabled, battery dead, notifications broken), María sees a warning **before** she sends fiat. She can cancel the covenant and pick a different seller. **No money wasted on a seller who can't complete the trade.**

---

## Why It Matters

### The Fraud Vector

**Without health checks:**
```
1. María creates covenant
2. María sends €100 via Bizum
3. Seller's bank app is disabled (no notifications received)
4. Seller never locks BCH (doesn't know payment arrived)
5. María waits 48 hours, covenant expires, funds returned
6. María lost time, seller reputation damaged (maybe innocent)
```

**Or worse:**
```
1. María creates covenant
2. Seller's phone battery is at 2% (about to die)
3. María sends €100 via Bizum
4. Seller's phone dies 30 seconds later
5. Seller doesn't see notification, doesn't lock BCH
6. María stuck waiting
```

**Or actual fraud:**
```
1. Malicious seller disables bank app notifications
2. Posts bulletin board listing (looks legitimate)
3. María sends payment
4. Seller claims "never received notification"
5. Dispute process triggered (costly, slow)
6. Seller keeps fiat, never locks BCH
```

### The Solution

**With device health checks:**
```
1. María creates covenant
2. María requests payment info via Nostr
3. Seller's bot responds with health metrics
4. María's app sees: "⚠️ Seller's bank app is disabled"
5. María cancels covenant, picks different seller
6. No fiat sent, no fraud possible
```

**Proactive fraud prevention.** Catch problems **before** money moves.

---

## What Gets Checked

The seller's bot reports five critical metrics:

| Metric | What It Checks | Why It Matters |
|--------|----------------|----------------|
| **Bank app installed** | Is the seller's bank app present on their phone? | If not installed, notifications impossible |
| **Bank app enabled** | Is the app disabled by the user or system? | Disabled apps can't send notifications |
| **Battery optimization** | Is the bank app excluded from battery optimization? | Android kills optimized apps, blocking notifications |
| **Battery level** | Current battery percentage (0-100%) | Low battery means phone might die soon |
| **Charging state** | Is the phone plugged in? | Charging = more reliable availability |

**All checks automated.** The seller's bot performs them in milliseconds, sends results with payment info.

---

## The Nostr Integration

Health checks are part of the **payment_info_response** message.

### Request (from María)
```json
{
  "type": "payment_info_request",
  "covenant_id": "covenant_xyz789",
  "amount_eur": 100,
  "payment_method": "Bizum",
  "sender_pubkey": "npub1maria..."
}
```

### Response (from seller's bot)
```json
{
  "type": "payment_info_response",
  "covenant_id": "covenant_xyz789",
  "payment_method": "Bizum",
  "account_number": "+34-612-345-678",
  "reference": "ASGAYA-XYZ789",
  "amount_exact": "€100.00",
  "expires_at": "2026-06-10T15:30:00Z",
  
  "device_health": {
    "bank_app_installed": true,
    "bank_app_enabled": true,
    "battery_optimized": false,
    "battery_level": 67,
    "is_charging": true,
    "timestamp": "2026-06-10T15:00:00Z"
  }
}
```

**Encrypted.** Only María can read this. Health metrics are private—other buyers don't see the seller's battery level.

---

## User Experience: What María Sees

María doesn't see JSON blobs. The app translates health metrics into simple warnings or confirmations.

### Healthy Device
```
┌─────────────────────────────┐
│  Pay €100.00 to:             │
│    Phone: +34-612-345-678    │
│    Reference: ASGAYA-XYZ789  │
│                              │
│  ✅ Seller ready (67% battery, charging) │
│                              │
│  [Open Bizum App]            │
└─────────────────────────────┘
```

**Green light.** María proceeds with confidence.

---

### Unhealthy Device (Warning)
```
┌─────────────────────────────┐
│  ⚠️ SELLER DEVICE ISSUES     │
│                              │
│  • Bank app disabled         │
│  • Battery at 8% (not charging) │
│                              │
│  Seller may not receive your │
│  payment notification.       │
│                              │
│  [Pick Different Seller]     │
│  [Continue Anyway] ←        │
└─────────────────────────────┘
```

**María decides:** Cancel and pick a healthy seller (recommended), or proceed with risk (her choice).

---

### Unhealthy Device (Critical)
```
┌─────────────────────────────┐
│  🛑 SELLER NOT READY         │
│                              │
│  • Bank app not installed    │
│                              │
│  This seller cannot receive  │
│  payment notifications.      │
│  DO NOT PAY.                 │
│                              │
│  [Pick Different Seller]     │
└─────────────────────────────┘
```

**Red flag.** Payment blocked. Prevents definite fraud.

---

## Seller Experience: Keeping Health Green

Sellers don't manually report health—it's automatic. But they **do** need to maintain device health for good reputation.

### First-Time Setup

When a seller installs the Asgaya bot (or BizumParser monitoring app), the app guides them:

```
┌─────────────────────────────┐
│  SELLER SETUP CHECKLIST      │
│                              │
│  1. ✅ Bank app installed     │
│  2. ⚠️ Battery optimization   │
│     → Tap to disable         │
│  3. ✅ Notifications enabled  │
│  4. ℹ️ Keep battery above 20% │
│     or stay plugged in       │
│                              │
│  [Continue to Dashboard]     │
└─────────────────────────────┘
```

**One-time setup.** After this, the bot monitors health automatically.

---

### Runtime Monitoring

The seller's dashboard shows real-time health:

```
┌─────────────────────────────┐
│  DEVICE HEALTH               │
│                              │
│  ✅ Bank app: Healthy         │
│  ✅ Battery: 67% (charging)   │
│  ✅ Notifications: Working    │
│                              │
│  Last trade: 3 min ago       │
│  Total today: 12 trades      │
└─────────────────────────────┘
```

**If health degrades:**
```
┌─────────────────────────────┐
│  ⚠️ DEVICE HEALTH WARNING    │
│                              │
│  Battery at 15% (not charging) │
│                              │
│  Buyers will see warnings.   │
│  Plug in your phone to       │
│  maintain good reputation.   │
│                              │
│  [Dismiss]                   │
└─────────────────────────────┘
```

**Proactive alerts.** Sellers fix issues before buyers notice.

---

## Bank App Compatibility

Different banks, different apps, different Android package names. The bot needs to know which app to check.

### Supported Banks (Phase 0)

| Bank | Package Name | Notes |
|------|--------------|-------|
| **Caja Rural** | `com.rsi.nba` | Primary Phase 0 target |
| **BBVA** | `com.bbva.bbvacontigo` | Phase 0 testing |
| **Santander** | `es.bancosantander.apps` | Phase 1+ |
| **Sabadell** | `es.bancsabadell.mobilebancohd` | Phase 1+ |
| **ING** | `es.ingdirect.ing` | Phase 1+ |

**Seller selects their bank during setup.** The bot automatically checks the correct app.

**If bank not listed:** Debug mode allows sellers to capture their bank's notification format and package name, submit it for future support.

---

## Technical Implementation

### Android Platform

Health checks rely on Android system APIs:

```kotlin
// Check if bank app is installed
fun isBankAppInstalled(packageName: String): Boolean {
    return try {
        packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

// Check if bank app is enabled
fun isBankAppEnabled(packageName: String): Boolean {
    val state = packageManager.getApplicationEnabledSetting(packageName)
    return state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED
}

// Check battery optimization status
fun isBatteryOptimized(packageName: String): Boolean {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    return !powerManager.isIgnoringBatteryOptimizations(packageName)
}

// Get battery level and charging state
fun getBatteryInfo(): Pair<Int, Boolean> {
    val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                   status == BatteryManager.BATTERY_STATUS_FULL
    return Pair(level, charging)
}
```

**These APIs are standard Android.** No root required, no special permissions (beyond what the bot needs for notification listening).

---

### iOS Platform

**Phase 0 focuses on Android** (where bank apps send actionable notifications). iOS support deferred to Phase 1+.

**Why Android first:**
- Spanish banks send rich Bizum notifications on Android
- NotificationListenerService API allows full notification monitoring
- Battery optimization controls available to users

**iOS challenges:**
- Notification access more restricted
- Bank apps may not expose same notification format
- Battery optimization handled differently

**Phase 1+ iOS strategy:**
- Manual notification forwarding (user screenshots Bizum notification, bot parses)
- Or web-based seller bot (runs on VPS, seller forwards notifications via API)

---

## Privacy Considerations

### What Health Checks Reveal

**To the buyer (María):**
- ✅ Seller's device is healthy/unhealthy
- ✅ Specific issues (battery low, app disabled)
- ❌ Seller's phone model, Android version, or other identifying info

**To relays (Nostr intermediaries):**
- ❌ Nothing—health data is encrypted in the Nostr message

**To the blockchain:**
- ❌ Nothing—health checks happen off-chain, never recorded on BCH

**Phase 0 acceptable.** Seller voluntarily discloses basic device health to facilitate trade. No identifying metadata leaks.

---

### Spoofing Attacks

**Can a malicious seller fake health metrics?**

**Yes.** The seller's bot self-reports health. A custom bot could lie:
```json
{
  "device_health": {
    "bank_app_installed": true,  // LIE: actually false
    "battery_level": 100,         // LIE: actually 5%
    "is_charging": true           // LIE: actually unplugged
  }
}
```

**But it doesn't help them:**
1. Buyer sends fiat payment based on fake health report
2. Seller's bank app is actually disabled → no notification received
3. Seller doesn't lock BCH (can't, didn't get notification)
4. Covenant expires, funds returned to buyer after 48 hours
5. Seller gains nothing, loses reputation (buyers mark as unreliable)

**Lying about health is self-sabotage.** Honest sellers report accurate health to **complete trades and earn fees**.

**Reputation reinforces honesty:** Sellers who frequently fail to lock BCH (due to real or fake health issues) get downranked by the app. Buyers avoid them.

---

## Failure Modes and Mitigations

### What if health check fails to send?

**Scenario:** Seller's bot is online (responds to Nostr), but health data is missing from the response.

**Mitigation:**
- Buyer's app treats missing health as "unknown"
- Shows warning: "⚠️ Seller didn't report device health. Proceed with caution."
- Buyer can cancel or continue (their choice)

**Phase 0 acceptable.** Missing health is suspicious, not fatal.

---

### What if health degrades mid-trade?

**Scenario:** Health check passes at payment info request, but battery dies while María is opening her Bizum app.

**Timeline:**
```
15:00:00 - Health check: 30% battery, not charging
15:00:05 - María receives payment instructions
15:00:45 - María completes Bizum payment (40 seconds later)
15:00:50 - Seller's phone dies (battery drained)
15:00:51 - Notification never received
```

**Mitigation:**
- Covenant timelock: 48-hour expiration
- María's funds automatically returned if seller doesn't lock BCH
- Seller's reputation takes a hit (failed to complete trade)

**Phase 1+ improvement:**
- Real-time health monitoring during payment window
- If health degrades before payment, buyer notified ("Seller went offline, cancel payment")
- Requires persistent connection between buyer/seller during critical window

**Phase 0 trade-off:** Accept rare edge case (battery dies mid-payment) in exchange for simpler implementation. Covenant timelock protects buyer.

---

### What if buyer ignores health warnings?

**Scenario:** María sees "Bank app disabled" warning, clicks "Continue Anyway," sends payment.

**Outcome:**
- Seller doesn't receive notification (app is actually disabled)
- Seller doesn't lock BCH
- Covenant expires after 48 hours, María gets BCH refund
- María wasted 48 hours of time (but no money lost)

**Mitigation:**
- App emphasizes warning severity ("DO NOT PAY" for critical issues)
- Requires explicit confirmation ("Type CONTINUE to proceed anyway")
- Logs buyer decision (for support/dispute context)

**Phase 0 philosophy:** Informed consent. We warn, user decides.

---

## Future Enhancements (Phase 1+)

### Continuous Health Monitoring

**Phase 0:** Health checked once (at payment info request).

**Phase 1+:** Health checked continuously during payment window:
```
1. María requests payment info → health check #1
2. María opens Bizum app (20 seconds later) → health check #2
3. María confirms payment (40 seconds later) → health check #3
4. If any check fails → alert María before she sends fiat
```

**Requires:** WebSocket connection or frequent Nostr pings during critical window. More complex, but catches degrading health in real-time.

---

### Predictive Health Warnings

**Phase 0:** Binary health (healthy/unhealthy).

**Phase 1+:** Predictive alerts based on historical patterns:
- "Seller's battery drains 5%/hour. At this rate, phone dies in 2 hours. Proceed?"
- "Seller's bank app crashes 10% of the time. Higher failure risk."

**Requires:** Long-term health telemetry, machine learning models.

---

### Multi-Device Sellers

**Phase 0:** One seller = one device.

**Phase 1+:** Sellers run bots on multiple devices (phone + VPS):
```json
{
  "device_health": [
    {
      "device_id": "phone",
      "bank_app_installed": true,
      "battery_level": 25,
      "is_charging": false
    },
    {
      "device_id": "vps",
      "bank_app_installed": false,  // VPS doesn't have bank app
      "always_online": true,
      "forwards_notifications": true  // Phone forwards notifications to VPS
    }
  ]
}
```

**Redundancy:** If phone dies, VPS continues operating.

---

## Comparison to Traditional Exchanges

| Feature | Asgaya (Device Health) | Centralized Exchange (Coinbase, Kraken) |
|---------|------------------------|------------------------------------------|
| **Fraud prevention** | Proactive (check before payment) | Reactive (freeze account after fraud) |
| **Seller screening** | Automated, instant | Manual KYC (days/weeks) |
| **Privacy** | No KYC, health metrics stay private | Full KYC, government surveillance |
| **User control** | Buyer sees warnings, decides | Platform decides (account bans) |
| **Seller experience** | Run bot, maintain device health | Submit documents, wait for approval |

**Asgaya's approach:** Trust, but verify. Sellers self-report health, buyers validate before payment. Reputation replaces KYC.

---

## Key Takeaways

1. **Proactive fraud prevention** — Catch bad sellers before money moves
2. **Five critical metrics** — Bank app status, battery level, charging state, battery optimization, app enabled
3. **Integrated with Nostr** — Health sent with payment info (encrypted, private)
4. **User-friendly** — María sees simple warnings ("Seller ready" or "Seller not ready")
5. **Seller-friendly** — Automated checks, dashboard alerts, one-time setup
6. **Privacy-preserving** — No KYC, no permanent records, encrypted transmission
7. **Reputation-enforced** — Sellers who fake health or have chronic issues get downranked

**Device health turns Asgaya from reactive (wait for fraud, then dispute) to proactive (prevent fraud before it happens).** Buyers pay with confidence. Sellers maintain good reputation. Fraudsters can't hide.

When regulators ask "how do you prevent fraud without KYC?" this is the answer: **Automated device health checks, covenant timelocks, and reputation-based filtering.** No centralized database. No government surveillance. Just code doing what banks can't.

---

**Author:** Suso + Claude Sonnet 4.5  
**Updated:** 2026-07-15  
**Related research:** RS075 (3C Toolbox decompilation), RS072 (BizumParser notification listener)

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ The Mechanism](../README.md)** | **[↑ Nostr Coordination](README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Nostr Coordination Overview](README.md) · [Dispute Resolution](dispute-resolution.md) · [Notification Bot](../notification-bot/README.md) · [Implementation Guide](../../implementation/README.md)
