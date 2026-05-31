# 2. PagoMóvil SMS Parser (Android)

**Platform:** Android
**Payment System:** PagoMóvil (Venezuela)
**Device:** Merchant's phone
**When:** LP sends VES to merchant for BCH liquidity
**Priority:** ⏸️ Post-Beta (Week 3-4)

---

## Overview

**PagoMóvil** is Venezuela's instant mobile payment system (similar to Spain's Bizum).

**How it works:**
- Sender dials `*124#` or uses banking app
- Enters recipient's phone number and amount
- Recipient receives **SMS confirmation** from their bank
- Transfer is instant and free

**Why we parse it:**
- Merchant receives VES from LP
- Auto-confirmation eliminates manual checking
- Fast settlement → BCH released to LP quickly

---

## Venezuelan Banking Context

### Major Banks Supporting PagoMóvil

| Bank | Market Share | SMS Reliability | Format Consistency |
|------|--------------|-----------------|-------------------|
| **Banesco** | ~35% | 🟢 High | 🟢 Consistent |
| **Mercantil** | ~20% | 🟢 High | 🟡 Moderate |
| **Provincial** | ~15% | 🟡 Medium | 🟡 Moderate |
| **Venezuela** | ~10% | 🟡 Medium | 🟡 Moderate |
| **BNC** | ~8% | 🟡 Medium | 🔴 Varies |
| **Bancaribe** | ~7% | 🟢 High | 🟢 Consistent |
| **Others** | ~5% | 🔴 Variable | 🔴 Variable |

**Strategy:**
- **Beta:** Focus on Banesco and Mercantil (55% coverage)
- **MVP:** Add Provincial and Bancaribe (77% coverage)
- **Post-MVP:** Expand to others as we get examples

---

## SMS Format Examples

### Banesco (Most Common)

```
Recibiste Bs. 6.210,00 de 0414-123-4567 por PagoMovil.
Ref: ASGAYA_settle_9kLmP
Fecha: 27/04/26 10:45
```

**Key fields:**
- Amount: `6.210,00` (thousands separator: period, decimal: comma)
- Sender phone: `0414-123-4567` (with hyphens)
- Reference: `ASGAYA_settle_9kLmP`
- Date: `27/04/26 10:45`

---

### Mercantil

```
PagoMovil recibido: BsS 6210,00
Telefono: 04141234567
Referencia: ASGAYA_settle_9kLmP
Hora: 10:45 27/04/2026
```

**Key fields:**
- Amount: `6210,00` (no thousands separator, old currency symbol BsS)
- Sender phone: `04141234567` (no hyphens)
- Reference: `ASGAYA_settle_9kLmP`
- Timestamp in different format

---

### Provincial

```
Has recibido 6.210 Bs por PagoMovil desde 0414-123-456.
Concepto: ASGAYA_settle_9kLmP
```

**Key fields:**
- Amount: `6.210` (no cents, just bolivares)
- Sender phone: `0414-123-456` (partial, missing last digit!)
- Reference in "Concepto" field

**⚠️ Note:** Provincial sometimes truncates phone numbers - less reliable for fraud detection

---

### Bancaribe

```
Pago Movil recibido
Monto: Bs.D. 6.210,00
Origen: 0424-7654321
Ref: ASGAYA_settle_9kLmP
```

**Key fields:**
- Amount: `6.210,00` (old currency symbol Bs.D.)
- Sender phone: `0424-7654321`
- Reference: `ASGAYA_settle_9kLmP`

---

## Android Implementation

### Manifest Permissions

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />

<receiver
    android:name=".receivers.PagoMovilSmsReceiver"
    android:permission="android.permission.BROADCAST_SMS"
    android:exported="true">
    <intent-filter android:priority="999">
        <action android:name="android.provider.Telephony.SMS_RECEIVED" />
    </intent-filter>
</receiver>
```

**Priority:** 999 (high) to process before default SMS app

---

### SMS Receiver

```kotlin
package com.asgaya.merchant.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PagoMovilSmsReceiver : BroadcastReceiver() {

    private val TAG = "PagoMovilReceiver"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        // Extract SMS messages
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

        for (message in messages) {
            val sender = message.originatingAddress ?: continue
            val body = message.messageBody ?: continue

            Log.d(TAG, "SMS from $sender: ${body.take(50)}...")

            // Only process PagoMóvil SMS from known banks
            if (!isPagoMovilSms(sender, body)) {
                continue
            }

            // Parse PagoMóvil notification
            val notification = PagoMovilParser.parse(sender, body)

            if (notification != null) {
                Log.i(TAG, "PagoMóvil detected: ${notification.settlementId}")

                // Forward to backend
                scope.launch {
                    handlePagoMovilNotification(context, notification)
                }
            } else {
                Log.w(TAG, "Failed to parse PagoMóvil SMS: $body")

                // Send unparsed SMS to backend for review
                scope.launch {
                    reportUnparsedSms(context, sender, body)
                }
            }
        }
    }

    private fun isPagoMovilSms(sender: String, body: String): Boolean {
        // Known bank shortcodes
        val bankShortcodes = listOf(
            "BANESCO",
            "MERCANTIL",
            "PROVINCIAL",
            "BANCARIBE",
            "BNC"
        )

        // Check if sender is bank
        if (!bankShortcodes.any { sender.contains(it, ignoreCase = true) }) {
            return false
        }

        // Check if body mentions PagoMóvil
        return body.contains("PagoMovil", ignoreCase = true) ||
               body.contains("Pago Movil", ignoreCase = true) ||
               body.contains("Pago Móvil", ignoreCase = true)
    }
}
```

---

### PagoMóvil Parser

```kotlin
package com.asgaya.merchant.parsers

import java.util.regex.Pattern

object PagoMovilParser {

    /**
     * Parse PagoMóvil SMS from Venezuelan banks
     *
     * Returns PagoMovilNotification if successful, null if parsing fails
     */
    fun parse(sender: String, smsBody: String): PagoMovilNotification? {
        return try {
            // Detect bank and use bank-specific parser
            when {
                sender.contains("BANESCO", ignoreCase = true) ->
                    parseBanesco(smsBody)

                sender.contains("MERCANTIL", ignoreCase = true) ->
                    parseMercantil(smsBody)

                sender.contains("PROVINCIAL", ignoreCase = true) ->
                    parseProvincial(smsBody)

                sender.contains("BANCARIBE", ignoreCase = true) ->
                    parseBancaribe(smsBody)

                else ->
                    parseGeneric(smsBody)  // Fallback
            }
        } catch (e: Exception) {
            Log.e("PagoMovilParser", "Parse error", e)
            null
        }
    }

    private fun parseBanesco(sms: String): PagoMovilNotification? {
        /*
         * Format: "Recibiste Bs. 6.210,00 de 0414-123-4567 por PagoMovil.
         *          Ref: ASGAYA_settle_9kLmP"
         */

        // Extract amount
        val amountRegex = """Bs\.?\s*(\d{1,3}(?:\.\d{3})*),(\d{2})""".toRegex()
        val amountMatch = amountRegex.find(sms) ?: return null
        val amountStr = amountMatch.groupValues[1].replace(".", "") + "." + amountMatch.groupValues[2]
        val amount = amountStr.toDoubleOrNull() ?: return null

        // Extract sender phone
        val phoneRegex = """de\s*(0\d{3}-?\d{3}-?\d{4})""".toRegex()
        val phoneMatch = phoneRegex.find(sms)
        val senderPhone = phoneMatch?.groupValues?.get(1)?.replace("-", "")

        // Extract reference (settlement ID)
        val refRegex = """Ref:\s*ASGAYA_(settle_[A-Za-z0-9]+)""".toRegex()
        val refMatch = refRegex.find(sms) ?: return null
        val settlementId = refMatch.groupValues[1]

        return PagoMovilNotification(
            settlementId = settlementId,
            amountVes = amount,
            senderPhone = senderPhone,
            bank = "Banesco",
            rawSms = sms,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun parseMercantil(sms: String): PagoMovilNotification? {
        /*
         * Format: "PagoMovil recibido: BsS 6210,00
         *          Telefono: 04141234567
         *          Referencia: ASGAYA_settle_9kLmP"
         */

        // Extract amount (multiple currency symbols)
        val amountRegex = """(?:Bs\.?|BsS\.?|Bs\.D\.?)\s*(\d{1,10}),(\d{2})""".toRegex()
        val amountMatch = amountRegex.find(sms) ?: return null
        val amountStr = amountMatch.groupValues[1] + "." + amountMatch.groupValues[2]
        val amount = amountStr.toDoubleOrNull() ?: return null

        // Extract phone
        val phoneRegex = """Telefono:\s*(0\d{10})""".toRegex()
        val phoneMatch = phoneRegex.find(sms)
        val senderPhone = phoneMatch?.groupValues?.get(1)

        // Extract reference
        val refRegex = """Referencia:\s*ASGAYA_(settle_[A-Za-z0-9]+)""".toRegex()
        val refMatch = refRegex.find(sms) ?: return null
        val settlementId = refMatch.groupValues[1]

        return PagoMovilNotification(
            settlementId = settlementId,
            amountVes = amount,
            senderPhone = senderPhone,
            bank = "Mercantil",
            rawSms = sms,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun parseProvincial(sms: String): PagoMovilNotification? {
        /*
         * Format: "Has recibido 6.210 Bs por PagoMovil desde 0414-123-456.
         *          Concepto: ASGAYA_settle_9kLmP"
         */

        // Extract amount (may not have cents)
        val amountRegex = """(\d{1,3}(?:\.\d{3})*(?:,\d{2})?)\s*Bs""".toRegex()
        val amountMatch = amountRegex.find(sms) ?: return null
        val amountStr = amountMatch.groupValues[1].replace(".", "").replace(",", ".")
        val amount = amountStr.toDoubleOrNull() ?: return null

        // Extract phone (may be truncated!)
        val phoneRegex = """desde\s*(0\d{3}-?\d{3}-?\d{3,4})""".toRegex()
        val phoneMatch = phoneRegex.find(sms)
        val senderPhone = phoneMatch?.groupValues?.get(1)?.replace("-", "")

        // Extract reference
        val refRegex = """Concepto:\s*ASGAYA_(settle_[A-Za-z0-9]+)""".toRegex()
        val refMatch = refRegex.find(sms) ?: return null
        val settlementId = refMatch.groupValues[1]

        return PagoMovilNotification(
            settlementId = settlementId,
            amountVes = amount,
            senderPhone = senderPhone,
            bank = "Provincial",
            rawSms = sms,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun parseBancaribe(sms: String): PagoMovilNotification? {
        // Similar to parseBanesco but with "Bs.D." currency symbol
        // Implementation omitted for brevity (same pattern)
        // ...
    }

    private fun parseGeneric(sms: String): PagoMovilNotification? {
        /*
         * Generic fallback parser for unknown banks
         * Less specific patterns, may have false positives
         */

        // Extract amount (broad pattern)
        val amountRegex = """(?:Bs|BsS|Bs\.D)\.?\s*(\d{1,10}(?:[.,]\d{1,3})*[.,]?\d{0,2})""".toRegex()
        val amountMatch = amountRegex.find(sms) ?: return null
        val amountStr = amountMatch.groupValues[1]
            .replace(".", "")  // Remove thousands separator
            .replace(",", ".") // Comma to period for decimal
        val amount = amountStr.toDoubleOrNull() ?: return null

        // Extract settlement ID (most reliable field)
        val refRegex = """ASGAYA_(settle_[A-Za-z0-9]+)""".toRegex(RegexOption.IGNORE_CASE)
        val refMatch = refRegex.find(sms) ?: return null
        val settlementId = refMatch.groupValues[1]

        return PagoMovilNotification(
            settlementId = settlementId,
            amountVes = amount,
            senderPhone = null,  // May not be extractable
            bank = "Unknown",
            rawSms = sms,
            timestamp = System.currentTimeMillis()
        )
    }
}

data class PagoMovilNotification(
    val settlementId: String,      // "settle_9kLmP"
    val amountVes: Double,          // 6210.00
    val senderPhone: String?,       // "04141234567" (may be null if not extractable)
    val bank: String,               // "Banesco", "Mercantil", etc.
    val rawSms: String,             // Full SMS for debugging
    val timestamp: Long             // When SMS received
)
```

---

### Backend API Integration

```kotlin
package com.asgaya.merchant.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface MerchantApi {
    @POST("/api/v1/admin/settlements/{settlement_id}/confirm-ves")
    suspend fun confirmVesReceived(
        @Path("settlement_id") settlementId: String,
        @Header("X-User-Address") merchantBchAddress: String,
        @Header("X-Signature") signature: String,
        @Header("X-Timestamp") timestamp: String,
        @Body request: ConfirmVesRequest
    ): Response<ConfirmVesResponse>
}

data class ConfirmVesRequest(
    val amount_ves: Double,
    val bank: String,
    val sender_phone: String?,
    val payment_reference: String,  // Raw SMS
    val confirmed_by: String = "notification_listener"
)

data class ConfirmVesResponse(
    val settlement_id: String,
    val status: String,             // "ves_confirmed"
    val bch_amount: Double,         // Amount of BCH to be sent to LP
    val next_action: String         // "escrow_buying_bch"
)

suspend fun handlePagoMovilNotification(
    context: Context,
    notification: PagoMovilNotification
) {
    try {
        // Get merchant's BCH address (used as identity)
        val merchantAddress = getMerchantBchAddress(context)

        // Sign request with merchant's BCH private key
        val timestamp = Instant.now().toString()
        val signature = signWithBchKey(context, timestamp)

        // Call backend API
        val response = apiClient.confirmVesReceived(
            settlementId = notification.settlementId,
            merchantBchAddress = merchantAddress,
            signature = signature,
            timestamp = timestamp,
            request = ConfirmVesRequest(
                amount_ves = notification.amountVes,
                bank = notification.bank,
                sender_phone = notification.senderPhone,
                payment_reference = notification.rawSms.take(200)  // Truncate
            )
        )

        if (response.isSuccessful) {
            val data = response.body()!!
            Log.i(TAG, "VES confirmed! BCH amount: ${data.bch_amount}")

            // Show merchant notification
            showNotification(
                context = context,
                title = "VES Payment Received!",
                body = "Bs. ${notification.amountVes} confirmed. You'll receive ${data.bch_amount} BCH soon.",
                actionUrl = "asgaya://settlements/${notification.settlementId}"
            )

            // Update local database
            settlementRepository.updateStatus(
                notification.settlementId,
                SettlementStatus.VES_CONFIRMED
            )

        } else {
            Log.e(TAG, "Backend error: ${response.code()}")
            showErrorNotification(context, "Failed to confirm payment. Retrying...")

            // Retry with backoff
            retryWithBackoff {
                handlePagoMovilNotification(context, notification)
            }
        }

    } catch (e: Exception) {
        Log.e(TAG, "Error handling PagoMóvil notification", e)
        showErrorNotification(context, "Connection error. Will retry automatically.")

        // Queue for background retry
        WorkManager.getInstance(context).enqueue(
            RetryPagoMovilWork(notification)
        )
    }
}
```

---

## Permission Handling (Merchant App)

### Request SMS Permissions

```kotlin
@Composable
fun SmsPermissionScreen() {
    val context = LocalContext.current
    val permissionsGranted = remember {
        mutableStateOf(checkSmsPermissions(context))
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted.value = permissions.all { it.value }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SMS Access Required",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = """
                Asgaya needs SMS access to detect PagoMóvil payments
                from your Venezuelan bank.

                We only read PagoMóvil notifications and ignore all other messages.

                Your messages are never stored or shared.
            """.trimIndent()
        )

        if (permissionsGranted.value) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "✓ SMS permissions granted",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Button(
                onClick = {
                    launcher.launch(
                        arrayOf(
                            android.Manifest.permission.RECEIVE_SMS,
                            android.Manifest.permission.READ_SMS
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant SMS Access")
            }
        }
    }
}

fun checkSmsPermissions(context: Context): Boolean {
    val receiveSms = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.RECEIVE_SMS
    ) == PackageManager.PERMISSION_GRANTED

    val readSms = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.READ_SMS
    ) == PackageManager.PERMISSION_GRANTED

    return receiveSms && readSms
}
```

---

## Testing Strategy

### Unit Tests

```kotlin
class PagoMovilParserTest {

    @Test
    fun `parse Banesco PagoMóvil SMS`() {
        val sms = """
            Recibiste Bs. 6.210,00 de 0414-123-4567 por PagoMovil.
            Ref: ASGAYA_settle_9kLmP
        """.trimIndent()

        val result = PagoMovilParser.parse("BANESCO", sms)

        assertNotNull(result)
        assertEquals("settle_9kLmP", result?.settlementId)
        assertEquals(6210.0, result?.amountVes)
        assertEquals("04141234567", result?.senderPhone)
        assertEquals("Banesco", result?.bank)
    }

    @Test
    fun `parse Mercantil PagoMóvil SMS`() {
        val sms = """
            PagoMovil recibido: BsS 6210,00
            Telefono: 04141234567
            Referencia: ASGAYA_settle_9kLmP
        """.trimIndent()

        val result = PagoMovilParser.parse("MERCANTIL", sms)

        assertNotNull(result)
        assertEquals("settle_9kLmP", result?.settlementId)
        assertEquals(6210.0, result?.amountVes)
    }

    @Test
    fun `parse Provincial PagoMóvil SMS without cents`() {
        val sms = """
            Has recibido 6.210 Bs por PagoMovil desde 0414-123-456.
            Concepto: ASGAYA_settle_9kLmP
        """.trimIndent()

        val result = PagoMovilParser.parse("PROVINCIAL", sms)

        assertNotNull(result)
        assertEquals("settle_9kLmP", result?.settlementId)
        assertEquals(6210.0, result?.amountVes)
    }

    @Test
    fun `parse amount with thousands separator`() {
        val sms = "Recibiste Bs. 15.210,50 de 0414123456 por PagoMovil. Ref: ASGAYA_settle_X"

        val result = PagoMovilParser.parse("BANESCO", sms)

        assertEquals(15210.5, result?.amountVes)
    }

    @Test
    fun `ignore non-Asgaya PagoMóvil`() {
        val sms = "Recibiste Bs. 100,00 de 0414123456 por PagoMovil. Ref: AlmuerzoAmigos"

        val result = PagoMovilParser.parse("BANESCO", sms)

        assertNull(result)  // Should return null (no ASGAYA reference)
    }

    @Test
    fun `ignore regular SMS`() {
        val sms = "Hola! Como estas?"

        val result = PagoMovilParser.parse("FRIEND", sms)

        assertNull(result)
    }
}
```

---

### Integration Tests

```kotlin
@Test
fun `end-to-end PagoMóvil confirmation flow`() = runTest {
    // 1. Create settlement
    val settlement = apiClient.createSettlement(...)

    // 2. LP sends PagoMóvil to merchant
    // (Simulated by sending test SMS)

    // 3. Merchant's app parses SMS
    val sms = """
        Recibiste Bs. 6.210,00 de 0424-555-1234 por PagoMovil.
        Ref: ASGAYA_${settlement.id}
    """.trimIndent()

    val notification = PagoMovilParser.parse("BANESCO", sms)!!

    // 4. App confirms VES received
    handlePagoMovilNotification(context, notification)

    // 5. Verify settlement updated
    val updated = apiClient.getSettlement(settlement.id)
    assertEquals("ves_confirmed", updated.status)
}
```

---

## Security Considerations

### SMS Spoofing

**Risk:** Attacker sends fake PagoMóvil SMS to trigger false confirmation

**Mitigations:**

1. **Sender validation:** Only process SMS from known bank shortcodes
2. **Settlement ID verification:** Must match active settlement in backend
3. **Amount verification:** Backend checks amount matches expected
4. **Timing verification:** SMS must arrive within expected window (LP has 30 min to pay)
5. **Merchant confirmation:** Merchant must also confirm in app (two-factor)

```kotlin
fun verifyPagoMovilSms(
    sms: SmsMessage,
    notification: PagoMovilNotification,
    settlement: Settlement
): Boolean {
    // Check sender is known bank
    val trustedBanks = listOf("BANESCO", "MERCANTIL", "PROVINCIAL", "BANCARIBE")
    if (!trustedBanks.any { sms.originatingAddress?.contains(it, ignoreCase = true) == true }) {
        Log.w(TAG, "SMS from unknown sender: ${sms.originatingAddress}")
        return false
    }

    // Check amount matches (within 1% tolerance for exchange rate fluctuations)
    val expectedVes = settlement.amount_ves
    val tolerance = expectedVes * 0.01
    if (abs(notification.amountVes - expectedVes) > tolerance) {
        Log.w(TAG, "Amount mismatch: expected $expectedVes, got ${notification.amountVes}")
        return false
    }

    // Check timing (settlement created < 30 min ago)
    val settlementAge = System.currentTimeMillis() - settlement.created_at
    if (settlementAge > 1_800_000) {  // 30 minutes
        Log.w(TAG, "Settlement too old: ${settlementAge}ms")
        return false
    }

    return true
}
```

---

### Privacy

**Concern:** SMS permissions allow reading ALL messages

**Mitigations:**

1. **Filter immediately:** Only process bank SMS, ignore all others
2. **Don't store:** Forward to backend and discard, never save locally
3. **Privacy policy:** Clearly state what we read and why
4. **Merchant opt-in:** Only merchant devices have SMS permissions (not regular users)
5. **Open source:** Code is verifiable

---

## Error Handling

### Parsing Failures

```kotlin
fun handleUnparsedSms(context: Context, sender: String, body: String) {
    // Log for debugging
    Log.w(TAG, "Failed to parse PagoMóvil SMS from $sender")

    // Send to backend for review (helps improve parsers)
    apiClient.reportUnparsedSms(
        sender = sender,
        body = body,
        timestamp = System.currentTimeMillis()
    )

    // Show merchant notification asking for manual confirmation
    showNotification(
        context = context,
        title = "PagoMóvil Payment Detected",
        body = "We detected a payment but couldn't auto-confirm. Please review manually.",
        actionUrl = "asgaya://settlements/pending"
    )
}
```

---

### Network Failures

```kotlin
class RetryPagoMovilWork(
    private val notification: PagoMovilNotification
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            handlePagoMovilNotification(applicationContext, notification)
            Result.success()

        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()  // Exponential backoff
            } else {
                // Max retries exceeded, notify merchant
                showErrorNotification(
                    applicationContext,
                    "Failed to confirm payment after 3 attempts. Please confirm manually."
                )
                Result.failure()
            }
        }
    }
}
```

---

## Monitoring Dashboard

**Backend tracks:**

```python
# Parse success rate by bank
pagomovil_parse_success_rate = Gauge(
    'asgaya_pagomovil_parse_success_rate',
    'Parse success rate',
    ['bank']  # Banesco, Mercantil, etc.
)

# Parse failures by reason
pagomovil_parse_failures = Counter(
    'asgaya_pagomovil_parse_failures',
    'Parse failures',
    ['bank', 'reason']  # reason: format_mismatch, missing_field, etc.
)

# Unparsed SMS queue
pagomovil_unparsed_queue = Gauge(
    'asgaya_pagomovil_unparsed_queue_size',
    'Number of unparsed SMS awaiting review'
)
```

**Dashboard:**
```
PagoMóvil Parser Stats (Last 7 Days)
────────────────────────────────────
Banesco:     47/50 (94%) ✅
Mercantil:   31/35 (89%) 🟡
Provincial:  18/22 (82%) 🟡
Bancaribe:   12/13 (92%) ✅
Unknown:      3/8  (38%) ❌

Unparsed SMS Queue: 7 (view & update parsers)
```

---

## Future Enhancements

### V1.1: More Banks

Add parsers for:
- Banco de Venezuela
- BNC (Banco Nacional de Crédito)
- BOD (Banco Occidental de Descuento)
- Exterior
- Plus Bank

---

### V1.2: Machine Learning

Train model on SMS corpus to handle format variations automatically:

```kotlin
class MLPagoMovilParser {
    private val model = loadModel("pagomovil_parser_v2.tflite")

    fun parse(sms: String): PagoMovilNotification? {
        // Try regex first (fast)
        val regexResult = PagoMovilParser.parse("UNKNOWN", sms)
        if (regexResult != null) return regexResult

        // Fallback to ML
        val prediction = model.predict(sms)

        return if (prediction.confidence > 0.85) {
            prediction.notification
        } else {
            null  // Low confidence, manual review
        }
    }
}
```

---

### V2: Bank API Integration

Some Venezuelan banks offer APIs (e.g., Banesco API for businesses):

```kotlin
interface BanescoApi {
    @GET("/payments/incoming")
    suspend fun getIncomingPayments(
        @Header("Authorization") token: String
    ): List<Payment>
}

// Poll API instead of parsing SMS
suspend fun pollBanescoPayments() {
    val payments = banescoApi.getIncomingPayments(authToken)

    for (payment in payments) {
        if (payment.reference.startsWith("ASGAYA_")) {
            handlePagoMovilNotification(
                PagoMovilNotification(
                    settlementId = payment.reference.substringAfter("ASGAYA_"),
                    amountVes = payment.amount,
                    senderPhone = payment.sender_phone,
                    bank = "Banesco",
                    rawSms = payment.toJson(),
                    timestamp = payment.timestamp
                )
            )
        }
    }
}
```

**Advantages:**
- No SMS permissions needed
- More reliable (no parsing fragility)
- Lower latency

---

## Related Documents

- **Index:** [NotificationListener Architecture](README.md)
- **Bizum:** [bizum-android.md](bizum-android.md) - Similar pattern for EUR
- **OP_RETURN:** [opreturn-spv.md](opreturn-spv.md) - BCH notifications

---

*Created: April 28, 2026*
*Status: Draft (Post-Beta Implementation)*
*Testing needed: Real Venezuelan bank SMS samples*
*Priority: Medium (not needed for initial beta with EUR-only)*
