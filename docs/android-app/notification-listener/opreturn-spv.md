# 3. OP_RETURN SPV Monitor

**Platform:** Android & iOS (cross-platform)
**Notification System:** BCH on-chain OP_RETURN messages
**Device:** All users (sender, recipient, merchant, LP)
**When:** Backend needs to notify users without centralized push services
**Priority:** ✅ MVP Critical (Week 2)

---

## Overview

**OP_RETURN** is Bitcoin Cash's way to store arbitrary data on the blockchain.

**How it works:**
- Transaction output with `OP_RETURN` script
- Can contain up to 223 bytes of data
- Data is permanently on-chain
- Requires small dust amount (546 sats) to be valid

**Why we use it:**
- ❌ **Replace:** Firebase Cloud Messaging (€50+/month, Google tracking)
- ❌ **Replace:** Apple Push Notification Service (device tokens, centralized)
- ✅ **Use:** On-chain BCH notifications (€0.006 each, decentralized, private)

**Philosophy:** Use Bitcoin Cash for everything you can, fiat only when you must.

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│              Escrow Backend                         │
│  • Transaction state changes                        │
│  • Settlement creation                              │
│  • BCH sent confirmations                           │
└───────────────────┬─────────────────────────────────┘
                    │
                    │ Broadcast OP_RETURN transaction
                    │ To: user's BCH address
                    │ Amount: 546 sats (dust)
                    │ Data: "ASGAYA_TXN_READY_7382"
                    │
                    ▼
┌─────────────────────────────────────────────────────┐
│              Bitcoin Cash Network                   │
│  • Transaction propagates to nodes                  │
│  • Gets included in next block                      │
│  • Confirmed on-chain                               │
└───────────────────┬─────────────────────────────────┘
                    │
                    │ SPV wallet monitors address
                    │
                    ▼
┌─────────────────────────────────────────────────────┐
│           User's Mobile App                         │
│  • SPV wallet (lightweight Bitcoin Cash client)     │
│  • Monitors user's BCH address                      │
│  • Detects incoming OP_RETURN transaction           │
│  • Parses message data                              │
│  • Shows notification to user                       │
└─────────────────────────────────────────────────────┘
```

**Key insight:** Every Asgaya user generates a BCH address on first app launch. This address IS their identity AND their notification inbox.

---

## OP_RETURN Message Formats

**Defined by backend, parsed by app:**

| Event | Message Format | When Sent | Recipient |
|-------|---------------|-----------|-----------|
| Payment ready | `ASGAYA_TXN_READY_7382` | Escrow receives EUR from sender | Recipient |
| Transaction completed | `ASGAYA_TXN_COMPLETED_txn_7Hk9mNpQ2wX` | Recipient confirms cash received | Sender |
| Settlement available | `ASGAYA_SETTLEMENT_settle_9kLmP` | LP needed for merchant payout | LP |
| BCH sent | `ASGAYA_BCH_SENT_0.00008` | Escrow sends BCH to merchant/LP | Merchant/LP |
| Merchant confirmed | `ASGAYA_MERCHANT_CONFIRMED_7382` | Merchant gives cash to recipient | Sender |

**Message structure:**
- Prefix: `ASGAYA_` (identifier for filtering)
- Event type: `TXN_READY`, `TXN_COMPLETED`, `SETTLEMENT`, etc.
- Data: Transaction code, ID, or amount

**Maximum size:** 223 bytes (BCH OP_RETURN limit)
**Asgaya messages:** 25-50 bytes (plenty of headroom)

---

## SPV Wallet Integration

### What is SPV?

**Simplified Payment Verification (SPV):**
- Lightweight Bitcoin Cash client
- Downloads block headers only (~80 bytes per block)
- Verifies transactions without full blockchain
- Monitors specific addresses for incoming transactions

**Why SPV (not full node):**
- ✅ Mobile-friendly (small data usage)
- ✅ Fast sync (minutes, not hours)
- ✅ Privacy (doesn't expose which addresses you're watching)
- ✅ Decentralized (connects to BCH network directly)

**Libraries:**
- **Android:** [bitcoincashj](https://github.com/pokkst/bitcoincashj-thin) (Java/Kotlin)
- **iOS:** [BitcoinKit](https://github.com/horizontalsystems/BitcoinKit.Swift) (Swift)
- **Cross-platform:** [Cash-Lib](https://cashweb.cash/) (Kotlin Multiplatform)

---

### Android Implementation (bitcoincashj)

**Dependencies:**

```gradle
// build.gradle.kts
dependencies {
    implementation("cash.bitcoinj:bitcoinj-core:0.16.2")
    implementation("org.slf4j:slf4j-android:1.7.36")
}
```

---

### SPV Wallet Setup

```kotlin
package com.asgaya.wallet

import org.bitcoinj.core.*
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.script.ScriptPattern
import org.bitcoinj.wallet.Wallet
import java.io.File

class AsgayaWallet(
    private val context: Context
) {
    private val params = MainNetParams.get()
    private var walletKit: WalletAppKit? = null
    private var userAddress: Address? = null

    /**
     * Initialize SPV wallet
     * - Generates BCH address on first launch
     * - Starts SPV sync
     * - Sets up transaction listener
     */
    fun initialize(onReady: (Address) -> Unit) {
        val walletDir = File(context.filesDir, "asgaya_wallet")

        walletKit = WalletAppKit(params, walletDir, "asgaya")

        walletKit?.apply {
            // Set bloom filter (privacy)
            setBlockingStartup(false)
            setDownloadHeaders(true)

            // Start sync
            startAsync()

            // Wait for wallet ready
            addListener(object : Service.Listener() {
                override fun running() {
                    val wallet = wallet()

                    // Get or create user's BCH address
                    userAddress = if (wallet.issuedReceiveAddresses.isEmpty()) {
                        wallet.freshReceiveAddress()
                    } else {
                        wallet.issuedReceiveAddresses.first()
                    }

                    // Set up transaction listener
                    wallet.addCoinsReceivedEventListener { wallet, tx, prevBalance, newBalance ->
                        handleIncomingTransaction(tx)
                    }

                    onReady(userAddress!!)
                }
            }, MoreExecutors.directExecutor())
        }
    }

    /**
     * Handle incoming transaction
     * Check if it contains OP_RETURN data
     */
    private fun handleIncomingTransaction(tx: Transaction) {
        Log.d(TAG, "Transaction received: ${tx.txId}")

        // Check each output for OP_RETURN
        for (output in tx.outputs) {
            if (ScriptPattern.isOpReturn(output.scriptPubKey)) {
                val opReturnData = extractOpReturnData(output)

                if (opReturnData != null) {
                    Log.i(TAG, "OP_RETURN data: $opReturnData")
                    parseAsgayaNotification(opReturnData, tx)
                }
            }
        }

        // Also got some BCH (dust)!
        val receivedAmount = tx.getValue(walletKit?.wallet())
        if (receivedAmount > Coin.ZERO) {
            Log.i(TAG, "Received ${receivedAmount.toFriendlyString()}")
        }
    }

    /**
     * Extract OP_RETURN data from output
     */
    private fun extractOpReturnData(output: TransactionOutput): String? {
        try {
            val chunks = output.scriptPubKey.chunks

            // OP_RETURN scripts have format: OP_RETURN <data>
            if (chunks.size >= 2 && chunks[0].opcode == 106) {  // OP_RETURN = 0x6a = 106
                val dataChunk = chunks[1]

                return if (dataChunk.data != null) {
                    String(dataChunk.data, Charsets.UTF_8)
                } else {
                    null
                }
            }

            return null

        } catch (e: Exception) {
            Log.e(TAG, "Error extracting OP_RETURN data", e)
            return null
        }
    }

    /**
     * Parse Asgaya notification from OP_RETURN data
     */
    private fun parseAsgayaNotification(message: String, tx: Transaction) {
        if (!message.startsWith("ASGAYA_")) {
            Log.d(TAG, "Not an Asgaya notification: $message")
            return
        }

        when {
            message.startsWith("ASGAYA_TXN_READY_") -> {
                // Format: "ASGAYA_TXN_READY_7382"
                val code = message.substringAfter("ASGAYA_TXN_READY_")
                handleTransactionReady(code, tx)
            }

            message.startsWith("ASGAYA_TXN_COMPLETED_") -> {
                // Format: "ASGAYA_TXN_COMPLETED_txn_7Hk9mNpQ2wX"
                val txnId = message.substringAfter("ASGAYA_TXN_COMPLETED_")
                handleTransactionCompleted(txnId, tx)
            }

            message.startsWith("ASGAYA_SETTLEMENT_") -> {
                // Format: "ASGAYA_SETTLEMENT_settle_9kLmP"
                val settlementId = message.substringAfter("ASGAYA_SETTLEMENT_")
                handleSettlementAvailable(settlementId, tx)
            }

            message.startsWith("ASGAYA_BCH_SENT_") -> {
                // Format: "ASGAYA_BCH_SENT_0.00008"
                val amount = message.substringAfter("ASGAYA_BCH_SENT_")
                handleBchSent(amount, tx)
            }

            message.startsWith("ASGAYA_MERCHANT_CONFIRMED_") -> {
                // Format: "ASGAYA_MERCHANT_CONFIRMED_7382"
                val code = message.substringAfter("ASGAYA_MERCHANT_CONFIRMED_")
                handleMerchantConfirmed(code, tx)
            }

            else -> {
                Log.w(TAG, "Unknown Asgaya notification format: $message")
            }
        }
    }

    private fun handleTransactionReady(code: String, tx: Transaction) {
        Log.i(TAG, "Transaction ready! Code: $code")

        // Show notification to user
        showNotification(
            title = "Cash Ready! 💵",
            body = "€100 confirmed! Your code: $code. Go to any Asgaya merchant.",
            actionUrl = "asgaya://transactions/code/$code"
        )

        // Update local transaction state
        transactionRepository.updateStatus(code, TransactionStatus.READY_FOR_PICKUP)

        // Play notification sound
        playNotificationSound()
    }

    private fun handleTransactionCompleted(txnId: String, tx: Transaction) {
        Log.i(TAG, "Transaction completed: $txnId")

        showNotification(
            title = "Transaction Complete! ✅",
            body = "Your remittance was successful. Tap to view details.",
            actionUrl = "asgaya://transactions/$txnId"
        )

        transactionRepository.updateStatus(txnId, TransactionStatus.COMPLETED)
    }

    private fun handleSettlementAvailable(settlementId: String, tx: Transaction) {
        Log.i(TAG, "New settlement available: $settlementId")

        showNotification(
            title = "Settlement Available",
            body = "New settlement ready. Tap to view and accept.",
            actionUrl = "asgaya://settlements/$settlementId"
        )

        settlementRepository.addPendingSettlement(settlementId)
    }

    private fun handleBchSent(amount: String, tx: Transaction) {
        Log.i(TAG, "BCH sent: $amount")

        showNotification(
            title = "BCH Received! 🎉",
            body = "You received $amount BCH. Check your balance.",
            actionUrl = "asgaya://wallet"
        )

        // Wallet balance already updated by bitcoincashj
    }

    private fun handleMerchantConfirmed(code: String, tx: Transaction) {
        Log.i(TAG, "Merchant confirmed handoff: $code")

        showNotification(
            title = "Merchant Confirmed",
            body = "Merchant confirmed cash handoff. Waiting for recipient confirmation.",
            actionUrl = "asgaya://transactions/code/$code"
        )

        transactionRepository.updateStatus(code, TransactionStatus.MERCHANT_CONFIRMED)
    }

    /**
     * Get user's BCH address (identity)
     */
    fun getUserAddress(): Address? = userAddress

    /**
     * Get current balance
     */
    fun getBalance(): Coin {
        return walletKit?.wallet()?.getBalance(Wallet.BalanceType.ESTIMATED) ?: Coin.ZERO
    }

    /**
     * Clean up
     */
    fun shutdown() {
        walletKit?.stopAsync()
        walletKit?.awaitTerminated()
    }

    companion object {
        private const val TAG = "AsgayaWallet"
    }
}
```

---

### Notification Display

```kotlin
private fun showNotification(
    title: String,
    body: String,
    actionUrl: String
) {
    val notificationManager = context.getSystemService(NotificationManager::class.java)

    // Create notification channel (Android 8+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "asgaya_notifications",
            "Asgaya Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Transaction and settlement notifications"
            enableVibration(true)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    // Create intent for tap action
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl)).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    // Build notification
    val notification = NotificationCompat.Builder(context, "asgaya_notifications")
        .setSmallIcon(R.drawable.ic_asgaya_logo)
        .setContentTitle(title)
        .setContentText(body)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    // Show notification
    notificationManager.notify(
        System.currentTimeMillis().toInt(),  // Unique ID
        notification
    )
}
```

---

## Cost Analysis

### Per Notification

**OP_RETURN transaction:**
- Dust amount: 546 sats (minimum UTXO)
- Transaction fee: ~100 sats (1 sat/byte × 100 bytes)
- **Total:** 646 sats

**At BCH price €1000:**
- 646 sats = 0.00000646 BCH
- **Cost per notification:** €0.006

---

### Volume Comparison

**100 transactions (EUR → VES):**
- 3 notifications per transaction (recipient, merchant, LP)
- Total: 300 notifications
- Cost: 300 × €0.006 = **€1.80**

**Firebase alternative:**
- Free tier: 200 notifications/day (OK for beta)
- Paid tier: €50+/month at scale
- **Asgaya savings:** 96% (€50 → €1.80)

**Bonus:**
- Users receive free BCH (546 sats per notification)
- 300 notifications = 163,800 sats = €1.64 distributed to users
- Users can save these micro-amounts and eventually withdraw

---

## Backend Integration

### Sending OP_RETURN (Backend)

```python
# Backend (Python example)
from bitcoincash import PrivateKey, Transaction
from bitcoincash.network import NetworkAPI

def send_opreturn_notification(
    recipient_bch_address: str,
    message: str,
    escrow_private_key: PrivateKey
):
    """
    Send OP_RETURN notification to user's BCH address

    Args:
        recipient_bch_address: User's BCH address (e.g., "bitcoincash:qp3wjpa3...")
        message: Notification message (e.g., "ASGAYA_TXN_READY_7382")
        escrow_private_key: Escrow's BCH private key (for signing)

    Returns:
        Transaction ID (txid)
    """

    # Validate message size
    if len(message.encode()) > 223:
        raise ValueError(f"Message too large: {len(message.encode())} bytes (max 223)")

    # Create transaction
    tx = Transaction()

    # Add input (escrow's BCH for dust + fee)
    escrow_address = escrow_private_key.address
    utxos = NetworkAPI.get_unspent(escrow_address)

    if not utxos:
        raise ValueError("Escrow has no BCH for notifications")

    # Use first UTXO
    utxo = utxos[0]
    tx.add_input(utxo['txid'], utxo['vout'])

    # Add OP_RETURN output
    tx.add_opreturn_output(message.encode())

    # Add dust output to recipient (makes it appear in their wallet)
    dust_amount = 546  # satoshis
    tx.add_output(recipient_bch_address, dust_amount)

    # Add change output back to escrow (if needed)
    fee = 100  # sats
    total_output = dust_amount + fee
    change = utxo['amount_satoshis'] - total_output

    if change > 546:  # Only add change if above dust threshold
        tx.add_output(escrow_address, change)

    # Sign transaction
    tx.sign(escrow_private_key)

    # Broadcast to BCH network
    txid = NetworkAPI.broadcast(tx.to_hex())

    print(f"OP_RETURN notification sent! TxID: {txid}")

    return txid
```

---

### Example Usage (Backend)

```python
# When escrow receives EUR payment
def on_payment_received(transaction_id: str, recipient_bch_address: str):
    # Update transaction state
    transaction.status = "payment_received"
    transaction.save()

    # Generate 4-digit code
    code = generate_pickup_code()  # e.g., "7382"
    transaction.pickup_code = code
    transaction.save()

    # Send OP_RETURN notification to recipient
    message = f"ASGAYA_TXN_READY_{code}"

    send_opreturn_notification(
        recipient_bch_address=recipient_bch_address,
        message=message,
        escrow_private_key=ESCROW_BCH_KEY
    )

    # Recipient's SPV wallet will detect transaction within ~30 seconds
    # App shows: "Cash Ready! Code: 7382"
```

---

## Delivery Time Analysis

**Typical flow:**

1. **Backend broadcasts OP_RETURN:** ~1 second
2. **Transaction propagates to BCH network:** ~2-5 seconds
3. **SPV wallet detects transaction:** ~10-20 seconds (next block header)
4. **App shows notification:** Immediate

**Total latency:** 15-30 seconds from backend trigger to user notification

**Compare to:**
- Firebase: 1-5 seconds (faster, but centralized)
- SMS: 5-30 seconds (similar, but costs more)
- Email: 10-60 seconds (slower)

**Trade-off:** Slightly slower than FCM, but:
- ✅ Fully decentralized
- ✅ No tracking or device tokens
- ✅ Works without Google/Apple services
- ✅ Permanent on-chain record

---

## Privacy & Security

### Privacy Benefits

**Traditional push notifications:**
- Device tokens stored on Google/Apple servers
- Can track which users are active
- Can correlate notifications across apps
- Can see message metadata

**OP_RETURN notifications:**
- No device tokens needed
- No centralized servers
- No tracking or profiling
- Fully pseudonymous (BCH address only)

**Only correlation:** On-chain analysis (someone watching the blockchain could see OP_RETURN messages)

**Mitigation:** Messages don't contain PII (just codes/IDs). Only the user knows what they mean.

---

### Security Considerations

**Spam prevention:**

Anyone could send OP_RETURN to user's address. How do we prevent spam?

```kotlin
fun parseAsgayaNotification(message: String, tx: Transaction) {
    // Only process messages with ASGAYA_ prefix
    if (!message.startsWith("ASGAYA_")) {
        return  // Ignore non-Asgaya OP_RETURN
    }

    // Validate sender (optional)
    // Could check if transaction came from known escrow address
    val fromEscrow = tx.inputs.any { input ->
        input.fromAddress == ESCROW_BCH_ADDRESS
    }

    if (!fromEscrow) {
        Log.w(TAG, "OP_RETURN not from escrow, ignoring")
        return
    }

    // Parse message...
}
```

**Rate limiting:**

```kotlin
class NotificationRateLimiter {
    private val recentNotifications = mutableListOf<Long>()

    fun shouldProcess(timestamp: Long): Boolean {
        // Clean old entries (older than 1 hour)
        recentNotifications.removeAll { it < timestamp - 3_600_000 }

        // Allow max 50 notifications per hour
        if (recentNotifications.size >= 50) {
            Log.w(TAG, "Rate limit exceeded")
            return false
        }

        recentNotifications.add(timestamp)
        return true
    }
}
```

---

## Testing Strategy

### Testnet Testing

**Use BCH testnet for development:**

```kotlin
// Use TestNet3Params instead of MainNetParams
val params = TestNet3Params.get()

val walletKit = WalletAppKit(params, walletDir, "asgaya_test")
```

**Get testnet BCH:**
- Testnet faucet: https://faucet.fullstack.cash/
- Free testnet BCH for testing

**Send test OP_RETURN:**
```python
# Backend sends test notification
send_opreturn_notification(
    recipient_bch_address="bchtest:qp...",  # Testnet address
    message="ASGAYA_TXN_READY_TEST",
    escrow_private_key=testnet_key
)
```

---

### Unit Tests

```kotlin
class OpReturnParserTest {

    @Test
    fun `parse ASGAYA_TXN_READY message`() {
        val message = "ASGAYA_TXN_READY_7382"
        val result = parseNotificationType(message)

        assertEquals(NotificationType.TXN_READY, result.type)
        assertEquals("7382", result.data)
    }

    @Test
    fun `parse ASGAYA_TXN_COMPLETED message`() {
        val message = "ASGAYA_TXN_COMPLETED_txn_7Hk9mNpQ2wX"
        val result = parseNotificationType(message)

        assertEquals(NotificationType.TXN_COMPLETED, result.type)
        assertEquals("txn_7Hk9mNpQ2wX", result.data)
    }

    @Test
    fun `ignore non-Asgaya OP_RETURN`() {
        val message = "MEMO:Hello World"
        val result = parseNotificationType(message)

        assertNull(result)
    }

    @Test
    fun `extract OP_RETURN data from transaction`() {
        // Mock transaction with OP_RETURN output
        val tx = createMockOpReturnTx("ASGAYA_TEST")

        val data = extractOpReturnData(tx.outputs[0])

        assertEquals("ASGAYA_TEST", data)
    }
}
```

---

### Integration Tests

```kotlin
@Test
fun `end-to-end OP_RETURN notification flow`() = runTest {
    // 1. Initialize SPV wallet
    val wallet = AsgayaWallet(context)
    wallet.initialize { address ->
        // 2. Backend sends OP_RETURN to this address
        val txid = backendApi.sendTestNotification(
            address = address.toString(),
            message = "ASGAYA_TXN_READY_9999"
        )

        // 3. Wait for SPV wallet to detect
        delay(30_000)  // 30 seconds

        // 4. Verify notification shown
        val notifications = getShownNotifications()
        assertTrue(notifications.any { it.contains("9999") })
    }
}
```

---

## Performance Optimization

### Block Header Download

**Initial sync can take 5-10 minutes:**

```kotlin
// Show sync progress to user
wallet.addListener(object : DownloadProgressTracker() {
    override fun progress(pct: Double, blocksSoFar: Int, date: Date?) {
        Log.d(TAG, "Sync progress: ${(pct * 100).toInt()}%")

        // Update UI
        showSyncProgress(pct)
    }

    override fun doneDownload() {
        Log.i(TAG, "Sync complete!")
        hideSyncProgress()
    }
}, MoreExecutors.directExecutor())
```

---

### Background Sync

**Keep wallet synced even when app closed:**

```kotlin
class WalletSyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Start wallet sync
            val wallet = AsgayaWallet(applicationContext)
            wallet.initialize { address ->
                Log.i(TAG, "Background sync complete for $address")
            }

            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Background sync failed", e)
            Result.retry()
        }
    }
}

// Schedule periodic sync (every 6 hours)
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "wallet_sync",
    ExistingPeriodicWorkPolicy.KEEP,
    PeriodicWorkRequestBuilder<WalletSyncWorker>(6, TimeUnit.HOURS).build()
)
```

---

## Monitoring & Observability

### Backend Metrics

```python
# Track OP_RETURN notifications sent
opreturn_notifications_sent = Counter(
    'asgaya_opreturn_notifications_sent',
    'Total OP_RETURN notifications sent',
    ['type']  # TXN_READY, TXN_COMPLETED, etc.
)

# Track delivery time (estimated)
opreturn_delivery_duration = Histogram(
    'asgaya_opreturn_delivery_duration_seconds',
    'Estimated delivery time for OP_RETURN notifications'
)

# Track BCH spent on notifications
opreturn_bch_spent = Counter(
    'asgaya_opreturn_bch_spent_satoshis',
    'Total satoshis spent on OP_RETURN notifications'
)
```

---

### App Metrics

```kotlin
// Track notifications received
val notificationsReceived = Counter.Builder()
    .name("asgaya_opreturn_notifications_received")
    .labelNames("type")
    .build()

// Track parsing errors
val parseErrors = Counter.Builder()
    .name("asgaya_opreturn_parse_errors")
    .labelNames("reason")
    .build()

// Track delivery latency (time from tx broadcast to app detection)
val deliveryLatency = Histogram.Builder()
    .name("asgaya_opreturn_delivery_latency_seconds")
    .build()
```

---

## Future Enhancements

### V1.1: Push Notifications Fallback

**For users who want faster notifications:**

```kotlin
// Hybrid approach: OP_RETURN primary, FCM fallback
class HybridNotificationManager {
    fun sendNotification(user: User, message: String) {
        // Always send OP_RETURN (decentralized)
        sendOpReturnNotification(user.bch_address, message)

        // If user opted into FCM, also send push
        if (user.fcm_token != null) {
            sendFcmNotification(user.fcm_token, message)
        }
    }
}
```

**User choice:**
- ✅ OP_RETURN only (privacy-first, 15-30s latency)
- ✅ OP_RETURN + FCM (faster, but less private)

---

### V2: Encrypted OP_RETURN

**Privacy enhancement: Encrypt message with recipient's public key**

```kotlin
// Backend encrypts message before sending
fun sendEncryptedOpReturn(
    recipient_bch_address: str,
    message: str,
    recipient_public_key: PublicKey
):
    # Encrypt message with recipient's public key
    encrypted = encrypt_with_ecies(message, recipient_public_key)

    # Send encrypted data as OP_RETURN
    send_opreturn_notification(
        recipient_bch_address,
        f"ASGAYA_ENC_{encrypted}"
    )

// App decrypts with private key
fun parseEncryptedOpReturn(encrypted: String): String {
    val decrypted = decryptWithBchKey(encrypted)
    return decrypted
}
```

**Benefit:** On-chain observers can't read notification content

---

### V3: CashAccounts Integration

**Use CashAccounts for human-readable names:**

Instead of:
- BCH address: `bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy`

Use:
- CashAccount: `iris#12345`

```kotlin
// Resolve CashAccount to BCH address
val address = cashAccountResolver.resolve("iris#12345")

// Send OP_RETURN to resolved address
sendOpReturnNotification(address, "ASGAYA_TXN_READY_7382")
```

**User experience:**
- Sender enters: "iris#12345" instead of long BCH address
- Backend resolves to address
- Sends OP_RETURN notification

---

## Cross-Platform Support

### iOS Implementation

**Use BitcoinKit.Swift:**

```swift
import BitcoinKit

class AsgayaWallet {
    private var wallet: Wallet?

    func initialize() {
        let configuration = Configuration(
            network: .mainnetBCH,
            syncMode: .full
        )

        wallet = try? Wallet(configuration: configuration)

        // Listen for transactions
        wallet?.transactionReceived = { transaction in
            self.handleIncomingTransaction(transaction)
        }

        // Start sync
        try? wallet?.start()
    }

    private func handleIncomingTransaction(_ transaction: Transaction) {
        // Check for OP_RETURN outputs
        for output in transaction.outputs {
            if let opReturnData = output.opReturnData,
               let message = String(data: opReturnData, encoding: .utf8) {
                parseAsgayaNotification(message)
            }
        }
    }
}
```

**Same notification formats work across platforms!**

---

## Related Documents

- **Index:** [NotificationListener Architecture](android-app/notification-listener/README.md) - Overview
- **Bizum:** [bizum-android.md](android-app/notification-listener/bizum-android.md) - EUR notifications
- **PagoMóvil:** [pagomovil-android.md](android-app/notification-listener/pagomovil-android.md) - VES notifications
- **User APIs:** [user-apis.md](android-app/backend-apis/user-apis.md) - OP_RETURN message formats
- **Architecture:** [README.md](android-app/README.md) - Philosophy

---

## Key Takeaways

1. **OP_RETURN = Decentralized Push Notifications** - No FCM, no tracking, no servers
2. **BCH address = User ID + Notification Inbox** - Elegant dual purpose
3. **Cost: €0.006 per notification** - 96% cheaper than Firebase at scale
4. **Delivery: 15-30 seconds** - Slightly slower than FCM, but fully decentralized
5. **Works across platforms** - Same messages, Android & iOS
6. **Privacy-first** - No device tokens, no tracking, pseudonymous
7. **Bonus: Users earn BCH** - 546 sats dust per notification adds up

**Philosophy:** Bitcoin Cash isn't just for settlement - it's the communication layer too.

---

*Created: April 28, 2026*
*Status: Draft (MVP Critical - Week 2)*
*Next: Implement and test on BCH testnet*
*This is the magic sauce - makes Asgaya feel truly decentralized*
