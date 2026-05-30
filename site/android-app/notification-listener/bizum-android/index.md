# RS046-6: NotificationListener Service

**Research Type:** Technical Specification
**Status:** Draft
**Created:** 2026-04-27
**Related:** [RS046-5 Backend APIs](../backend-apis/README.md)

---

## Overview

This document specifies the Android **NotificationListenerService** that intercepts Sabadell bank notifications for Bizum payments and forwards them to the escrow backend.

**Core function:** Bridge between Sabadell banking app and Asgaya escrow backend.

**Why this is needed:** Bizum notifications arrive as Android notifications, not SMS. We can't use traditional SMS parsing - we need NotificationListener permission.

---

## Architecture Context

### The Flow

```
┌─────────────────────────────────────────────────┐
│  Iris sends €100 Bizum to escrow               │
│  Concept: "34ASGAYA7Hk9mNpQ2wX"                 │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│  Sabadell Banking App                           │
│  (receives Bizum, generates notification)       │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│  Android NotificationManager                    │
│  (system service manages all notifications)     │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────┐
│  Asgaya NotificationListener Service            │ ← THIS DOCUMENT
│  • Intercepts Sabadell notifications            │
│  • Parses Bizum payment details                 │
│  • Extracts transaction ID from concept         │
│  • Forwards to escrow backend                   │
└──────────────────┬──────────────────────────────┘
                   │ HTTPS POST
                   ▼
┌─────────────────────────────────────────────────┐
│  Escrow Backend API                             │
│  POST /api/v1/admin/parse-notification          │
│  • Matches transaction ID                       │
│  • Updates transaction status                   │
│  • Notifies recipient                           │
└─────────────────────────────────────────────────┘
```

---

## Why NotificationListener (Not SMS)

### Sabadell Banking Notifications

**Pre-2023:** Bizum notifications arrived as SMS messages
- Easy to parse with `android.permission.READ_SMS`
- `SmsReceiver` worked perfectly

**Post-2023:** Sabadell switched to app-generated notifications
- No SMS sent for Bizum payments
- Notification displayed by Sabadell app
- Requires `NotificationListenerService` permission

**Comparison:**

| Method | Permission | Reliability | Privacy | Setup |
|--------|-----------|-------------|---------|-------|
| SMS Reading | `READ_SMS` | ❌ Not working (no SMS) | ⚠️ Can read ALL SMS | Simple |
| NotificationListener | `BIND_NOTIFICATION_LISTENER_SERVICE` | ✅ Works | ✅ Only app notifications | Complex |

**Decision:** Must use NotificationListener for Sabadell Bizum notifications.

---

## Android NotificationListenerService

### What It Is

**Official docs:** https://developer.android.com/reference/android/service/notification/NotificationListenerService

**Purpose:** System service that listens to all notifications posted/removed by any app.

**Use cases:**
- Smart watches (mirror notifications)
- Notification managers (organize/filter)
- Automation apps (trigger actions on notifications)
- **Our use case:** Parse banking notifications for payment confirmation

**Permission required:** User must explicitly enable in Settings > Notification Access

---

## Implementation Architecture

### Service Declaration (AndroidManifest.xml)

```xml
<service
    android:name=".services.BizumNotificationListener"
    android:label="Asgaya Payment Monitor"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```

**Key attributes:**
- `permission`: Required for NotificationListenerService
- `exported="true"`: Must be accessible to system
- `label`: Shows in Settings > Notification Access

---

### Service Class Structure

```kotlin
package com.asgaya.escrow.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.*
import java.text.NumberFormat
import java.util.Locale

class BizumNotificationListener : NotificationListenerService() {

    private val TAG = "BizumListener"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Called when notification is posted
     * This is where we intercept Sabadell Bizum notifications
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        // Filter: Only process Sabadell notifications
        if (sbn.packageName != SABADELL_PACKAGE) {
            return
        }

        // Extract notification details
        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString("android.title") ?: ""
        val text = extras.getString("android.text") ?: ""

        Log.d(TAG, "Sabadell notification: title='$title', text='$text'")

        // Parse Bizum payment
        parseBizumNotification(title, text)
    }

    /**
     * Called when notification is removed
     * Not used, but must override
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }

    /**
     * Parse Bizum notification and extract payment details
     */
    private fun parseBizumNotification(title: String, text: String) {
        // Check if this is a Bizum received notification
        if (!title.contains("Bizum", ignoreCase = true)) {
            return
        }

        if (!text.contains("recibido", ignoreCase = true) &&
            !text.contains("recibida", ignoreCase = true)) {
            return
        }

        // Parse notification text
        val paymentDetails = extractBizumDetails(text)

        if (paymentDetails != null) {
            Log.i(TAG, "Bizum payment detected: $paymentDetails")

            // Forward to escrow backend
            serviceScope.launch {
                sendToEscrow(paymentDetails)
            }
        }
    }

    /**
     * Extract payment details from notification text
     */
    private fun extractBizumDetails(text: String): BizumPayment? {
        /*
         * Expected format (observed from Sabadell app):
         * "Has recibido un Bizum de 100,00 € de 612345678. Concepto: 34ASGAYA7Hk9mNpQ2wX"
         * Or variations:
         * "Bizum recibido. De: 612345678. Importe: 100,00 EUR. Concepto: 34ASGAYA7Hk9mNpQ2wX"
         */

        try {
            // Extract amount
            val amountRegex = """(\d+[.,]\d{2})\s*€?""".toRegex()
            val amountMatch = amountRegex.find(text)
            val amountStr = amountMatch?.groupValues?.get(1)?.replace(",", ".")
            val amount = amountStr?.toDoubleOrNull()

            // Extract sender phone
            val phoneRegex = """(?:de|De:)\s*(\+?\d{9,15})""".toRegex()
            val phoneMatch = phoneRegex.find(text)
            val senderPhone = phoneMatch?.groupValues?.get(1)

            // Extract concept (transaction ID)
            val conceptRegex = """[Cc]oncepto:\s*(.+?)(?:\.|$)""".toRegex()
            val conceptMatch = conceptRegex.find(text)
            val concept = conceptMatch?.groupValues?.get(1)?.trim()

            // Validate all fields present
            if (amount == null || senderPhone == null || concept == null) {
                Log.w(TAG, "Missing fields - amount: $amount, phone: $senderPhone, concept: $concept")
                return null
            }

            // Validate concept contains "ASGAYA"
            if (!concept.contains("ASGAYA", ignoreCase = true)) {
                Log.d(TAG, "Not an Asgaya transaction (concept: $concept)")
                return null
            }

            // Extract transaction ID from concept
            val transactionId = extractTransactionId(concept)
            if (transactionId == null) {
                Log.w(TAG, "Could not extract transaction ID from concept: $concept")
                return null
            }

            return BizumPayment(
                amount = amount,
                senderPhone = senderPhone,
                concept = concept,
                transactionId = transactionId,
                timestamp = System.currentTimeMillis()
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Bizum notification", e)
            return null
        }
    }

    /**
     * Extract transaction ID from concept field
     * Format: "34ASGAYA7Hk9mNpQ2wX" → "txn_7Hk9mNpQ2wX"
     */
    private fun extractTransactionId(concept: String): String? {
        val regex = """ASGAYA(\w+)""".toRegex(RegexOption.IGNORE_CASE)
        val match = regex.find(concept)
        val code = match?.groupValues?.get(1)

        return if (code != null) {
            "txn_$code"
        } else {
            null
        }
    }

    /**
     * Send parsed payment to escrow backend
     */
    private suspend fun sendToEscrow(payment: BizumPayment) {
        try {
            val response = EscrowApiClient.submitBizumNotification(payment)

            if (response.isSuccessful) {
                Log.i(TAG, "Successfully sent to escrow: ${payment.transactionId}")
            } else {
                Log.e(TAG, "Escrow API error: ${response.code()} ${response.message()}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to send to escrow", e)
            // TODO: Retry logic with exponential backoff
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val SABADELL_PACKAGE = "es.bancosabadell.wallet"
    }
}

/**
 * Data class for parsed Bizum payment
 */
data class BizumPayment(
    val amount: Double,
    val senderPhone: String,
    val concept: String,
    val transactionId: String,
    val timestamp: Long
)
```

---

## API Client for Escrow Backend

```kotlin
package com.asgaya.escrow.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface EscrowApi {
    @POST("api/v1/admin/parse-notification")
    suspend fun submitBizumNotification(
        @Header("Authorization") authToken: String,
        @Body notification: BizumNotificationRequest
    ): Response<BizumNotificationResponse>
}

data class BizumNotificationRequest(
    val notification_type: String = "bizum_received",
    val sender_phone: String,
    val amount: Double,
    val concept: String,
    val timestamp: String,
    val raw_text: String
)

data class BizumNotificationResponse(
    val matched_transaction: String?,
    val status: String,
    val next_action: String
)

object EscrowApiClient {
    private const val BASE_URL = "https://api.asgaya.com/"
    private const val ESCROW_API_KEY = "escrow_key_xyz" // TODO: Secure storage

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(EscrowApi::class.java)

    suspend fun submitBizumNotification(payment: BizumPayment): Response<BizumNotificationResponse> {
        val request = BizumNotificationRequest(
            sender_phone = payment.senderPhone,
            amount = payment.amount,
            concept = payment.concept,
            timestamp = java.time.Instant.ofEpochMilli(payment.timestamp).toString(),
            raw_text = "Parsed from notification"
        )

        return api.submitBizumNotification(
            authToken = "Bearer $ESCROW_API_KEY",
            notification = request
        )
    }
}
```

---

## Permission Handling

### Request Permission (UI)

```kotlin
package com.asgaya.escrow.ui

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun NotificationPermissionScreen() {
    val context = LocalContext.current
    val isEnabled = checkNotificationListenerPermission(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Notification Access Required",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = """
                Asgaya needs notification access to detect Bizum payments
                from your Sabadell banking app.

                We only read Sabadell notifications and ignore all others.
            """.trimIndent()
        )

        if (isEnabled) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "✓ Permission granted",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permission")
            }
        }
    }
}

/**
 * Check if NotificationListener permission is granted
 */
fun checkNotificationListenerPermission(context: android.content.Context): Boolean {
    val packageName = context.packageName
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )

    return flat?.contains(packageName) == true
}
```

---

## Testing Strategy

### Test Cases

**1. Permission Handling**
- [ ] Request permission flow works
- [ ] Permission status detected correctly
- [ ] App handles permission denial gracefully

**2. Notification Parsing**
- [ ] Parse Bizum received notification (standard format)
- [ ] Extract amount correctly (handles comma decimal separator)
- [ ] Extract sender phone correctly
- [ ] Extract concept correctly
- [ ] Extract transaction ID from concept
- [ ] Ignore non-Bizum notifications
- [ ] Ignore Bizum from non-Asgaya concepts

**3. API Integration**
- [ ] Send parsed notification to escrow backend
- [ ] Handle successful API response
- [ ] Handle API errors (retry logic)
- [ ] Handle network failures (offline queue)

**4. Edge Cases**
- [ ] Notification arrives when app closed (background service)
- [ ] Multiple notifications in quick succession
- [ ] Notification text format variations
- [ ] Special characters in concept field
- [ ] Large amounts (thousands of euros)

---

## Sabadell Notification Formats (Observed)

From RS026 testing, we observed these formats:

**Format 1 (Most common):**
```
Title: Bizum
Text: Has recibido un Bizum de 100,00 € de 612345678. Concepto: 34ASGAYA7Hk9mNpQ2wX
```

**Format 2 (Alternative):**
```
Title: Bizum recibido
Text: De: 612345678. Importe: 100,00 EUR. Concepto: 34ASGAYA7Hk9mNpQ2wX
```

**Format 3 (Multiline):**
```
Title: Bizum
Text: Bizum recibido
      Importe: 100,00 €
      De: +34612345678
      Concepto: 34ASGAYA7Hk9mNpQ2wX
```

**Regex must handle:**
- Comma as decimal separator (100,00 not 100.00)
- Optional "+" prefix on phone numbers
- "De:" or "de" for sender
- "Concepto:" or "concepto:" for concept
- Euro symbol (€) or "EUR"
- Newlines in multiline format

---

## Security Considerations

### 1. Permission Scope
**Problem:** NotificationListener can read ALL notifications from ALL apps.

**Mitigation:**
- Only process notifications from `es.bancosabadell.wallet`
- Log when non-Sabadell notifications are ignored (transparency)
- Privacy policy clearly states we only read Sabadell
- Open source the listener code (verifiable)

### 2. Sensitive Data Handling
**Problem:** Bizum notifications contain phone numbers and amounts.

**Mitigation:**
- Don't log full phone numbers (mask middle digits)
- Don't store notifications locally (forward and forget)
- Encrypt API requests (HTTPS only)
- Escrow API key stored in Android KeyStore

### 3. Man-in-the-Middle
**Problem:** API requests could be intercepted.

**Mitigation:**
- Certificate pinning for escrow API
- Validate TLS certificate
- Use HTTPS only (no HTTP fallback)

### 4. Malicious Notifications
**Problem:** Attacker could send fake Sabadell notifications.

**Mitigation:**
- Validate transaction ID exists in escrow backend
- Escrow verifies amount matches expected
- Two-sided confirmation (merchant + recipient)
- Invalid codes flagged for review

---

## Background Service Lifecycle

### Service States

```
┌─────────────────────────────────────────┐
│  User enables permission in Settings    │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│  Android binds NotificationListener     │
│  Service starts (even if app closed)    │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│  Service running in background          │
│  • Monitors all notifications           │
│  • Filters for Sabadell package         │
│  • Parses Bizum payments                │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│  Bizum notification posted              │
│  → onNotificationPosted() called        │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│  Parse → Send to escrow → Complete      │
└─────────────────────────────────────────┘
```

**Key points:**
- Service runs even when app is closed
- Android keeps it alive as long as permission granted
- If Android kills service (low memory), it's automatically restarted
- No battery drain concerns (event-driven, not polling)

---

## Debugging Tools

### Log Viewer

```kotlin
@Composable
fun NotificationLogViewer() {
    val logs = remember { mutableStateListOf<String>() }

    // In BizumNotificationListener, write logs to shared prefs
    // Read and display here

    LazyColumn {
        items(logs) { log ->
            Text(
                text = log,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
```

### Test Notification Generator (Debug Build Only)

```kotlin
@Composable
fun TestNotificationButton() {
    Button(onClick = {
        sendTestBizumNotification()
    }) {
        Text("Send Test Bizum Notification")
    }
}

fun sendTestBizumNotification() {
    val notificationManager = context.getSystemService(NotificationManager::class.java)

    val notification = NotificationCompat.Builder(context, "test_channel")
        .setContentTitle("Bizum")
        .setContentText("Has recibido un Bizum de 5,00 € de 612345678. Concepto: 34ASGAYAtestABC123")
        .setSmallIcon(R.drawable.ic_notification)
        .build()

    notificationManager.notify(12345, notification)
}
```

---

## Error Handling & Retry Logic

### Failed API Requests

```kotlin
private suspend fun sendToEscrow(payment: BizumPayment) {
    var retries = 0
    val maxRetries = 3

    while (retries < maxRetries) {
        try {
            val response = EscrowApiClient.submitBizumNotification(payment)

            if (response.isSuccessful) {
                Log.i(TAG, "Successfully sent to escrow")
                return // Success, exit
            } else if (response.code() == 409) {
                // Transaction already processed, not an error
                Log.i(TAG, "Transaction already processed")
                return
            } else {
                Log.w(TAG, "API error: ${response.code()}, retry $retries")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Network error, retry $retries", e)
        }

        retries++
        if (retries < maxRetries) {
            delay(2000L * retries) // Exponential backoff
        }
    }

    // All retries failed, save to local queue
    Log.e(TAG, "All retries failed, queueing for later")
    saveToOfflineQueue(payment)
}

private fun saveToOfflineQueue(payment: BizumPayment) {
    // TODO: Save to Room database for later retry
    // Periodic background job will retry failed notifications
}
```

---

## Battery & Performance Optimization

### Best Practices

**1. Event-Driven (Not Polling)**
- NotificationListener is event-based
- Only runs when notification posted
- No continuous battery drain

**2. Minimal Processing**
- Quick regex parsing
- Single API call
- No heavy computation

**3. Coroutines for Network**
- Non-blocking API calls
- No thread overhead
- Automatic lifecycle management

**4. Filter Early**
- Check package name first
- Skip non-Sabadell notifications immediately
- Minimize wasted cycles

---

## Privacy Policy Requirements

**Must disclose in privacy policy:**

```
NOTIFICATION ACCESS PERMISSION

Asgaya requests notification access to monitor Banco Sabadell
notifications for Bizum payment confirmations.

What we access:
• Notifications from Banco Sabadell app only
• We read Bizum payment amounts, sender phone numbers, and concepts
• All other app notifications are ignored and not accessed

What we do with this data:
• Parse payment details (amount, sender, transaction ID)
• Forward to Asgaya escrow backend for transaction processing
• Data is not stored on your device
• Data is transmitted securely via HTTPS

What we DON'T do:
• We don't read notifications from other apps
• We don't store notification data locally
• We don't share data with third parties
• We don't use data for advertising or analytics

You can revoke this permission at any time in:
Settings > Apps > Asgaya > Notification Access
```

---

## Alternative Approaches Considered

### 1. SMS Receiver (Traditional)
**Pros:**
- Simpler permission (`READ_SMS`)
- Well-documented approach
- Works for older banking apps

**Cons:**
- ❌ Sabadell doesn't send SMS anymore
- ❌ Not viable for modern banking apps

### 2. Screen Scraping (Accessibility Service)
**Pros:**
- Could read notification content

**Cons:**
- ❌ Accessibility abuse (against Play Store policy)
- ❌ Very fragile (breaks on UI changes)
- ❌ Privacy nightmare
- ❌ Not approved for this use case

### 3. Manual Entry
**Pros:**
- No special permissions needed
- User in full control

**Cons:**
- ❌ Poor UX (extra steps)
- ❌ Error-prone (typos)
- ❌ Slow (kills conversion)

**Decision:** NotificationListener is the only viable approach for modern banking apps.

---

## Future Enhancements

### V1.1: Multiple Bank Support
- Add parsers for other banks (CaixaBank, BBVA, etc.)
- Bank-specific regex patterns
- Unified payment data structure

### V1.2: Notification History
- Store parsed notifications in Room database
- Show user their payment history
- Export for accounting purposes

### V2: Machine Learning Parser
- Train ML model on notification formats
- Handle bank UI changes automatically
- Reduce maintenance burden

### V2: Push Notifications Alternative
- If banks offer payment webhooks
- Direct API integration (no notification scraping)
- More reliable, less fragile

---

## Testing Checklist

**Before Production:**
- [ ] NotificationListener permission flow tested
- [ ] Sabadell notification parsing works on real device
- [ ] API integration tested with escrow backend
- [ ] Retry logic verified (disconnect network during test)
- [ ] Battery usage measured (should be negligible)
- [ ] Privacy policy updated and reviewed
- [ ] Edge cases tested (format variations, special characters)
- [ ] Background service lifecycle verified (app killed, service continues)
- [ ] Multiple quick notifications handled correctly
- [ ] Non-Asgaya Bizum payments ignored correctly

**Devices Tested:**
- [ ] Pixel 6a (Android 14)
- [ ] Motorola G06 (Android 13)
- [ ] Samsung Galaxy (Android 12) - if available

---

## Related Documents


- **APIs:** [RS046-5 Backend APIs](../backend-apis/README.md) - Escrow endpoint spec
- **UX:** [RS046-2 Remittance & Merchant Cash-Out](../flows/archive/remittance-merchant-cash-out.md) - User journey context
- **Security:** [RS046-8 Security](security.md) - Full security spec (TBD)

---

*Created: April 27, 2026*
*Status: Draft for review*
*Next: RS046-7 (UI Components)*
*Critical path: This is the bridge between Sabadell and Asgaya - must be rock solid*
