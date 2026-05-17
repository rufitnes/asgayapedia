# 4. Security Considerations

**Category:** Security & Fraud Prevention
**Priority:** 🔴 Critical
**Related:** [Index](android-app/notification-listener/README.md), [bizum-android.md](android-app/notification-listener/bizum-android.md), [pagomovil-android.md](android-app/notification-listener/pagomovil-android.md)

---

## Overview

NotificationListener components intercept sensitive financial notifications. Security is paramount.

**Threat model:**
- SMS spoofing (fake payment confirmations)
- Notification injection (malicious apps)
- Amount manipulation
- Timing attacks
- Privacy breaches

**Defense layers:**
1. Bank shortcode whitelist (primary defense)
2. Amount verification
3. Concept field verification (recipient phone)
4. Timing windows
5. Two-sided confirmation

---

## SMS/Notification Spoofing

### The Attack

**Scenario:** Attacker sends fake Bizum/PagoMóvil notification to trigger false payment confirmation

**Example:**
```
Attacker sends SMS to escrow device:
"Has recibido 100,00€ de +34612345678. Concepto: +34666999888"

NotificationListener parses it → Backend confirms payment →
Recipient gets OP_RETURN notification → Goes to merchant →
No actual payment occurred
```

---

### Primary Defense: Bank Shortcode Whitelist

**Key insight from smsbridge_loop.py:** SMS from banks come from known shortcodes, not regular phone numbers.

**Bank shortcodes cannot be spoofed** (controlled by telecom operators, not users).

**Implementation:**

```kotlin
object BankShortcodeWhitelist {
    /**
     * Spanish bank shortcodes (Bizum notifications)
     */
    val SPAIN_BANKS = setOf(
        "BBVA",           // BBVA shortcode
        "CaixaBank",      // CaixaBank shortcode
        "Santander",      // Santander shortcode
        "Sabadell",       // Banco Sabadell shortcode
        "BANCO SABADELL", // Alternative format
        "ING",            // ING Direct shortcode
        "25877"           // Example numeric shortcode
    )

    /**
     * Venezuelan bank shortcodes (PagoMóvil notifications)
     */
    val VENEZUELA_BANKS = setOf(
        "BANESCO",
        "MERCANTIL",
        "PROVINCIAL",
        "BANCARIBE",
        "BNC",
        "VENEZUELA",
        "BOD"
    )

    fun isTrustedSender(sender: String, country: String): Boolean {
        val whitelist = when (country) {
            "ES" -> SPAIN_BANKS
            "VE" -> VENEZUELA_BANKS
            else -> emptySet()
        }

        return whitelist.any { shortcode ->
            sender.contains(shortcode, ignoreCase = true)
        }
    }
}
```

**Usage in parsers:**

```kotlin
// Bizum parser (Android NotificationListener)
override fun onNotificationPosted(sbn: StatusBarNotification) {
    // Check if notification from Sabadell banking app
    if (sbn.packageName != "es.bancosabadell.wallet") {
        return  // Ignore non-Sabadell notifications
    }

    // Package name verification ensures notification came from legitimate app
    // Android prevents package name spoofing (signature verification)

    // Parse notification...
}

// PagoMóvil parser (SMS receiver)
override fun onReceive(context: Context, intent: Intent) {
    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

    for (message in messages) {
        val sender = message.originatingAddress ?: continue

        // CRITICAL: Check sender against whitelist
        if (!BankShortcodeWhitelist.isTrustedSender(sender, "VE")) {
            Log.w(TAG, "SMS from untrusted sender: $sender")
            return  // Reject SMS not from known bank
        }

        // Only process if from whitelisted bank shortcode
        parsePagoMovilSms(message)
    }
}
```

---

### Why This Works

**Telecom operator controls shortcodes:**
- Banks register shortcodes with telecom operators
- Users cannot send SMS from bank shortcodes
- Requires commercial agreement + technical integration
- Spoofing would require compromising telecom infrastructure

**Android package verification:**
- Android verifies app signatures
- Cannot install app with same package name unless same signing key
- Banking apps use production certificates (not accessible to attackers)

**Result:** Spoofing attack requires either:
1. Compromising telecom operator (extremely difficult)
2. Stealing bank's app signing key (extremely difficult)
3. Rooting user's device + installing malicious Xposed module (requires physical access)

**For MVP (escrow device = Suso's phone):**
- Controlled environment
- No untrusted apps
- Device not rooted
- Physical security

**Conclusion:** Shortcode whitelist provides strong protection for beta phase.

---

## Secondary Defenses

### 1. Concept Field Verification

**Recipient phone number in concept field serves as extra confirmation layer.**

**Flow:**
```
1. Backend creates transaction
   - Sender BCH address: qp3wjpa3...
   - Recipient BCH address: qr8xmk2a...
   - Recipient phone: +34666123456
   - Amount: €100

2. Backend shows Bizum payment instructions
   - Pay to escrow: +34609987654
   - Amount: €100
   - Concept: "+34666123456" (recipient phone)

3. Sender sends Bizum with recipient phone in concept

4. Escrow NotificationListener parses notification
   - Amount: €100
   - Concept: "+34666123456"

5. Backend verifies:
   ✓ Amount matches transaction (€100)
   ✓ Concept matches recipient phone (+34666123456)
   ✓ Recipient BCH address matches phone (stored in DB)

6. All checks pass → Confirm payment
```

**Why this helps:**

- Attacker must know both:
  - Active transaction ID
  - Recipient's phone number
- Phone number not exposed in app UI (only backend knows mapping)
- Adds second verification factor beyond just amount

**Implementation:**

```kotlin
// Backend verification
fun verifyBizumNotification(
    notification: BizumNotification,
    transaction: Transaction
): Boolean {
    // Check amount matches (±1% tolerance for rounding)
    val amountMatch = abs(notification.amountEur - transaction.amount_eur) < 0.01

    if (!amountMatch) {
        log.warn("Amount mismatch: expected ${transaction.amount_eur}, got ${notification.amountEur}")
        return false
    }

    // Check concept contains recipient phone
    val recipientPhone = transaction.recipient_phone  // e.g., "+34666123456"
    val conceptMatch = notification.concept.contains(recipientPhone)

    if (!conceptMatch) {
        log.warn("Concept mismatch: expected $recipientPhone in '${notification.concept}'")
        return false
    }

    return true
}
```

---

### 2. Timing Windows

**Notifications must arrive within expected timeframe.**

**Rules:**
- **Bizum:** Within 10 minutes of transaction creation
- **PagoMóvil:** Within 30 minutes of settlement creation
- **OP_RETURN:** Anytime (on-chain, immutable)

**Why:** Attacker would need to time spoofed SMS to coincide with active transaction.

**Implementation:**

```kotlin
fun verifyTiming(
    notificationTimestamp: Long,
    transactionCreatedAt: Long,
    maxAgeMillis: Long = 600_000  // 10 minutes
): Boolean {
    val age = notificationTimestamp - transactionCreatedAt

    if (age < 0) {
        log.warn("Notification predates transaction (clock skew?)")
        return false
    }

    if (age > maxAgeMillis) {
        log.warn("Notification too old: ${age}ms (max: ${maxAgeMillis}ms)")
        return false
    }

    return true
}
```

---

### 3. Two-Sided Confirmation

**Both merchant and recipient must confirm cash handoff.**

**Flow:**
```
1. Recipient shows code to merchant
2. Merchant enters code → Confirms "I gave cash"
3. Recipient taps "Confirm received" → Confirms "I got cash"
4. Backend only marks complete when BOTH confirm
```

**Why:** Even if attacker triggers false payment notification:
- Recipient goes to merchant
- Merchant says "I didn't get VES payment"
- Recipient doesn't receive cash
- Recipient doesn't confirm
- Transaction stuck → Escalated for review

**Manual review catches false positives.**

---

### 4. Amount Verification

**Backend checks notification amount matches transaction amount.**

```kotlin
fun verifyAmount(
    notificationAmount: Double,
    expectedAmount: Double,
    tolerance: Double = 0.01  // 1% tolerance
): Boolean {
    val diff = abs(notificationAmount - expectedAmount)
    val percentDiff = diff / expectedAmount

    return percentDiff <= tolerance
}
```

**Why 1% tolerance:**
- Exchange rate fluctuations during transaction
- Rounding differences (comma vs period decimal separators)
- Bank fee variations

---

## Escrow Centralization (By Design)

### Why Centralized Escrow for Beta

**You said:** "this is by design we don't know many things if we let anyone be an escrow at the beginning there might be ways we didn't think to exploit it."

**Rationale:**

**Unknown unknowns:**
- New attack vectors we haven't considered
- Edge cases in parser logic
- Race conditions in state machine
- Economic exploits (e.g., flashloan attacks on BCH/EUR rate)

**Controlled environment advantages:**
1. **Single point of control** - Can monitor all transactions
2. **Quick rollback** - Can pause escrow if issues detected
3. **Manual review** - Can intervene in suspicious transactions
4. **Learning phase** - Collect data on parser success rates, failure modes
5. **Incident response** - Can fix bugs without coordinating multiple escrow operators

**Beta phase (1-3 months):**
- Escrow = Suso's phone (trusted device)
- All transactions flow through single escrow
- Manual monitoring + automated alerts
- Build confidence in system security

**Post-beta (decentralization):**
- Once we've:
  - ✅ Tested parser reliability (>95% success rate)
  - ✅ Identified and patched security issues
  - ✅ Validated economic model
  - ✅ Built fraud detection systems
  - ✅ Established legal/compliance framework
- Then consider:
  - Multiple escrow operators (round-robin)
  - Escrow-as-a-service (third-party LPs)
  - Eventually: Direct P2P (no escrow, BCH-native contracts)

**Security posture:**
```
Beta:        High security, low scalability (acceptable)
Post-MVP:    High security, high scalability (goal)
```

---

## Privacy Protections

### Permission Minimization

**Regular users need ZERO invasive permissions:**
- ❌ No SMS access
- ❌ No notification listener
- ❌ No phone number verification
- ✅ Only: Internet + SPV wallet (standard)

**Only privileged roles need special permissions:**
- **Escrow device:** NotificationListener (1 device, Suso's phone)
- **Merchant devices:** SMS receiver (vetted partners, opt-in)

---

### Data Minimization

**NotificationListener only processes relevant notifications:**

```kotlin
override fun onNotificationPosted(sbn: StatusBarNotification) {
    // Filter 1: Only Sabadell package
    if (sbn.packageName != "es.bancosabadell.wallet") {
        // Don't even log, just ignore
        return
    }

    // Filter 2: Only Bizum notifications
    val title = sbn.notification.extras.getString("android.title") ?: ""
    if (!title.contains("Bizum", ignoreCase = true)) {
        return
    }

    // Filter 3: Only received (not sent)
    val text = sbn.notification.extras.getString("android.text") ?: ""
    if (!text.contains("recibido", ignoreCase = true)) {
        return
    }

    // Now process (only 1% of notifications reach here)
    parseBizumNotification(title, text)
}
```

**Non-Asgaya notifications never logged, never transmitted, never stored.**

---

### Open Source Commitment

**Code transparency:**
- All parser code open source (MIT license)
- Users can audit what we read
- Community can verify no data exfiltration
- Reproducible builds (verify APK matches source)

**Privacy policy:**
```
NOTIFICATION ACCESS PERMISSION

Asgaya escrow app requests notification access to monitor
Banco Sabadell Bizum payment confirmations.

What we access:
• Notifications from Banco Sabadell app only
• We read: payment amount, sender phone, concept field
• All other app notifications are ignored and not accessed

What we do with this data:
• Parse payment details (amount, sender, transaction ID)
• Forward to Asgaya backend for transaction processing
• Data is not stored on your device
• Data is transmitted securely via HTTPS
• Data is used only for transaction confirmation

What we DON'T do:
• We don't read notifications from other apps
• We don't store notification data locally
• We don't share data with third parties
• We don't use data for advertising or analytics

You can verify our code:
https://github.com/asgaya/escrow-app

You can revoke this permission at any time in:
Settings > Apps > Asgaya Escrow > Notification Access
```

---

## Fraud Detection

### Fuzzy Detector for Format Changes

**You said:** "the escrow app is going to be listening to bank notifications unrelated to asgaya all the time so we can notify the escrow when the pattern has changed"

**Implementation:**

```kotlin
class FuzzyFormatDetector {
    private val knownPatterns = mutableMapOf<String, NotificationPattern>()

    fun checkNotification(notification: Notification) {
        val bankId = detectBank(notification)
        val pattern = extractPattern(notification)

        val knownPattern = knownPatterns[bankId]

        if (knownPattern == null) {
            // First time seeing this bank
            knownPatterns[bankId] = pattern
            return
        }

        // Compare with known pattern
        val similarity = calculateSimilarity(pattern, knownPattern)

        if (similarity < 0.8) {
            // Pattern changed significantly!
            alertFormatChange(bankId, knownPattern, pattern)
        }
    }

    private fun extractPattern(notification: Notification): NotificationPattern {
        return NotificationPattern(
            titleKeywords = extractKeywords(notification.title),
            textStructure = extractStructure(notification.text),
            fieldOrder = detectFieldOrder(notification.text)
        )
    }

    private fun calculateSimilarity(p1: NotificationPattern, p2: NotificationPattern): Double {
        // Jaccard similarity or edit distance
        val titleSim = jaccardSimilarity(p1.titleKeywords, p2.titleKeywords)
        val structureSim = structuralSimilarity(p1.textStructure, p2.textStructure)

        return (titleSim + structureSim) / 2.0
    }

    private fun alertFormatChange(
        bankId: String,
        oldPattern: NotificationPattern,
        newPattern: NotificationPattern
    ) {
        Log.w(TAG, "⚠️ Bank notification format changed: $bankId")
        Log.w(TAG, "Old pattern: $oldPattern")
        Log.w(TAG, "New pattern: $newPattern")

        // Send alert to backend
        apiClient.reportFormatChange(
            bank = bankId,
            oldPattern = oldPattern,
            newPattern = newPattern,
            sampleNotification = "..." // Redacted notification text
        )

        // Show notification to escrow operator
        showNotification(
            title = "⚠️ Bank Format Changed",
            body = "$bankId notification format changed. Parser may need update.",
            actionUrl = "asgaya://admin/format-changes"
        )
    }
}
```

**Benefits:**
- Detects format changes even if they don't affect Asgaya transactions
- Can update parser proactively before breakage
- Alerts during ongoing transactions (manual fallback available)
- Builds corpus of notification samples for ML training

---

### Anomaly Detection

**Statistical outliers flag for manual review:**

```python
# Backend anomaly detection
class AnomalyDetector:
    def check_transaction(self, txn: Transaction) -> List[str]:
        anomalies = []

        # Check 1: Amount significantly higher than average
        avg_amount = get_average_transaction_amount(days=30)
        if txn.amount_eur > avg_amount * 3:
            anomalies.append(f"Amount 3x higher than average (€{txn.amount_eur} vs €{avg_amount})")

        # Check 2: User's first transaction
        if get_user_transaction_count(txn.sender_address) == 1:
            anomalies.append("First transaction for this user")

        # Check 3: Rapid succession (potential bot)
        recent_txns = get_recent_transactions(txn.sender_address, minutes=5)
        if len(recent_txns) > 3:
            anomalies.append(f"Multiple transactions in 5min ({len(recent_txns)})")

        # Check 4: Suspicious timing (midnight-6am)
        hour = datetime.now().hour
        if 0 <= hour < 6:
            anomalies.append(f"Unusual time: {hour}:00")

        return anomalies
```

**Manual review queue:**
```
Flagged Transactions (Needs Review)
────────────────────────────────────
txn_7Hk9mNpQ2wX
  • Amount 3x higher than average (€500 vs €150)
  • First transaction for this user
  [Review] [Approve] [Block]

txn_9kLmP2wX3zY
  • Multiple transactions in 5min (4)
  [Review] [Approve] [Block]
```

---

## Incident Response

### If Parser Compromised

**Scenario:** Attacker finds way to inject false notifications

**Response:**

1. **Immediate (< 5 min):**
   - Pause all transaction creation (backend config flag)
   - Alert escrow operator (SMS + app notification)
   - Review recent transactions for suspicious activity

2. **Short-term (< 1 hour):**
   - Switch to manual confirmation mode (disable auto-parsing)
   - Rollback suspicious transactions
   - Notify affected users

3. **Medium-term (< 24 hours):**
   - Patch vulnerability
   - Deploy updated parser
   - Resume auto-parsing with enhanced monitoring

4. **Long-term:**
   - Conduct security audit
   - Update threat model
   - Implement additional safeguards
   - Document incident for future reference

**Kill switch:**
```python
# Backend config
AUTO_PARSING_ENABLED = env.bool("AUTO_PARSING_ENABLED", default=True)

if not AUTO_PARSING_ENABLED:
    # Fallback to manual confirmation
    return Response({
        "status": "pending_manual_confirmation",
        "message": "Auto-parsing temporarily disabled. Please confirm manually."
    })
```

---

## Testing Security

### Penetration Testing Checklist

**Before production:**
- [ ] Attempt SMS spoofing from non-bank number
- [ ] Attempt notification injection from malicious app
- [ ] Test amount manipulation (€100 → €1000)
- [ ] Test timing attack (send notification for expired transaction)
- [ ] Test concept field manipulation
- [ ] Test rate limiting (flood notifications)
- [ ] Test escrow device compromise scenario
- [ ] Review all logs for sensitive data leaks

---

## Related Documents

- **Index:** [NotificationListener Architecture](android-app/notification-listener/README.md) - Overview
- **Testing:** [testing.md](android-app/notification-listener/testing.md) - Security testing details
- **Monitoring:** [README.md](android-app/notification-listener/README.md) - Security metrics

---

*Created: April 28, 2026*
*Status: Draft (Security Review Needed)*
*Philosophy: Defense in depth - multiple layers, graceful degradation*
