# End-to-End Covenant Claim Flow

**Purpose:** Document the complete architecture for claiming a covenant from notification to on-chain transaction.

**Status:** ✅ Production-proven (August 10, 2026)  
**Evidence:** First successful claim TXID: `193c3c9e5287e13cc56e1401aed55de34db9a375312e052807aea060e58e3d96`

---

## Overview

The covenant claim flow enables a recipient to receive a guaranteed EUR-denominated payment from a covenant created by a sender on a different device.

**Key properties:**
- **Cross-device:** Sender (Moto G06) → Recipient (Pixel 6a)
- **Off-chain coordination:** Covenant parameters transported via Telegram (or Nostr)
- **On-chain enforcement:** Smart contract validates all outputs before accepting transaction
- **Guaranteed value:** Recipient receives exact EUR amount at claim-time BCH price
- **Volatility protection:** Sender (funder) receives buffer back

**Flow duration:** ~30 seconds (notification → claim → confirmation)

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│ SENDER DEVICE (Moto G06)                                            │
│                                                                      │
│  1. Create Covenant                                                 │
│     ├─ ReviewSendActivity                                           │
│     ├─ CovenantWebView.createCovenant()                             │
│     └─ Returns covenant address                                     │
│                                                                      │
│  2. Fund Covenant                                                   │
│     ├─ CovenantWebView.sendBch()                                    │
│     ├─ Broadcast funding transaction                                │
│     └─ Returns funding TXID                                         │
│                                                                      │
│  3. Share Parameters                                                │
│     ├─ Format [COVENANT_V25] block                                  │
│     ├─ Auto-copy to clipboard                                       │
│     └─ User pastes to Telegram → sends to recipient                 │
└─────────────────────────────────────────────────────────────────────┘
                            ↓
                    [Telegram Message]
                            ↓
┌─────────────────────────────────────────────────────────────────────┐
│ RECIPIENT DEVICE (Pixel 6a)                                         │
│                                                                      │
│  4. NotificationListener (Background Service)                       │
│     ├─ Android AccessibilityService                                 │
│     ├─ Monitors Telegram notifications                              │
│     ├─ Detects [COVENANT_V25] block                                 │
│     ├─ Parses covenant parameters (key=value format)                │
│     └─ Stores in database (isReceived = true)                       │
│                                                                      │
│  5. Remittance UI (User Action)                                     │
│     ├─ RemittanceAdapter shows "Someone sent you X €"               │
│     ├─ User taps "🔄 Update Status"                                 │
│     │   ├─ ElectrumClient queries balance (TCP port 60001)          │
│     │   ├─ Shows "✅ funded" or "⚠️ Expired"                        │
│     │   └─ 5-second TCP cooldown (prevents race condition)          │
│     ├─ User taps "💰 Claim" → Dialog appears                        │
│     └─ User taps "💰 CLAIM NOW"                                     │
│                                                                      │
│  6. Wallet Matching (Critical Step!)                                │
│     ├─ Find recipient wallet: walletManager.findWalletByPubkey(     │
│     │      remittance.recipientPubkey)                              │
│     ├─ Extract recipient WIF (for signing)                          │
│     ├─ Find SELLER wallet: walletManager.findWalletByPubkey(        │
│     │      remittance.sellerPubkey)  ← [Funder Principle!]          │
│     └─ Extract seller ADDRESS (for buffer output)                   │
│                                                                      │
│  7. Oracle Signature Generation                                     │
│     ├─ Get current BCH/EUR price (Pi-chan oracle /oracle/price)     │
│     ├─ Generate timestamp (Unix epoch)                              │
│     ├─ CovenantWebView.generateOracleSignature()                    │
│     └─ Returns { message, signature, price, timestamp }             │
│                                                                      │
│  8. Transaction Building (Kotlin ↔ JavaScript Bridge)               │
│     ├─ RemittanceActivity.executeClaim()                            │
│     ├─ Builds parameters JSON:                                      │
│     │   {                                                            │
│     │     covenantParams: { ... },                                  │
│     │     oracleSig: { ... },                                       │
│     │     recipientWIF: "...",                                      │
│     │     recipientAddress: "bchtest:qq2uxg...",                    │
│     │     sellerAddress: "bchtest:qrw5nuk...",  ← Critical!         │
│     │     fulcrumHost: "192.168.1.100",                             │
│     │     fulcrumPort: 60003                                        │
│     │   }                                                            │
│     └─ Calls CovenantWebView.claimCovenant(params)                  │
│                                                                      │
│  9. JavaScript Execution (covenant-bridge.html)                     │
│     ├─ Connect to Fulcrum (WebSocket port 60003)                    │
│     ├─ Recreate covenant contract from params                       │
│     ├─ Fetch covenant UTXO                                          │
│     ├─ Calculate payment amounts:                                   │
│     │   ├─ eurPayment = eurCents ÷ oraclePrice × 100000000          │
│     │   └─ buffer = utxoSats - eurPayment - feeSats                 │
│     ├─ Build transaction:                                           │
│     │   ├─ Input: Covenant UTXO + unlock script                     │
│     │   ├─ Output 0: eurPayment sats → recipientAddress             │
│     │   └─ Output 1: buffer sats → sellerAddress                    │
│     ├─ Sign with recipientWIF                                       │
│     ├─ Broadcast transaction                                        │
│     └─ Disconnect from Fulcrum (finally block)                      │
│                                                                      │
│ 10. Success Callback                                                │
│     ├─ JavaScript → Kotlin bridge: onClaimSuccess(txid)             │
│     ├─ Show toast: "✅ Claim successful! TXID: ..."                 │
│     └─ Reload remittance history                                    │
└─────────────────────────────────────────────────────────────────────┘
                            ↓
                    [Blockchain Verification]
                            ↓
┌─────────────────────────────────────────────────────────────────────┐
│ PI-CHAN FULCRUM NODE (Testnet)                                      │
│                                                                      │
│ 11. Transaction Validation                                          │
│     ├─ Verify covenant script execution                             │
│     ├─ Validate oracle signature (CHECKDATASIG)                     │
│     ├─ Check output 0 amount (exact EUR payment)                    │
│     ├─ Check output 0 address (matches recipientPubkey)             │
│     ├─ Check output 1 amount (buffer)                               │
│     ├─ Check output 1 address (matches sellerPubkey) ← Funder!      │
│     └─ Broadcast to network                                         │
│                                                                      │
│ 12. Confirmation                                                    │
│     ├─ Transaction enters mempool                                   │
│     ├─ Miner includes in block                                      │
│     └─ Both sender and recipient wallets show confirmed             │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Component Details

### 1. Covenant Parameter Transport (Telegram)

**Message Format:**

```
🎁 You received 5,00 € via Asgaya BCH!

Covenant ready to claim - open Asgaya app to receive your payment.

[COVENANT_V25]
covenantAddress=bchtest:pwek84sj8vk39gzkkd5z5fg89l8t54lgh9yaymq09d665544yqvjqtxflhjf8
senderPubkey=032774f66536468a06a244568724bf1f8762682be1573ff9910667c0fea1226ece
recipientPubkey=03886b4f4100f9e348caed5155474f0a1a96db3e2706dc59e15260ed3d2b35e148
sellerPubkey=032774f66536468a06a244568724bf1f8762682be1573ff9910667c0fea1226ece
oraclePubkey=02f2c7e020377863ca973d9b68b20dc40b8da38fcb77536333cde0e886c8400651
eurCents=500
expiryOracleTime=1786313404
initialBchPriceInCents=65000
minPricePercent=93
fundingTxid=9b98c94cf4c755b3e8d2b18ad8095451f959b6cb39ddef657bdc841b08c700bb
[/COVENANT_V25]

> **Note:** This example shows the Aug 10 milestone covenant with the *original* oracle pubkey. Since Aug 16, the app fetches the oracle pubkey dynamically from the Pi-chan oracle (`ORACLE_URL` + `fetchOraclePubkey()`) — no pubkey is hardcoded in the app. See [Asgaya Oracle Husk](../../the-mechanism/nostr-coordination/oracle-husk.md).

⏰ Expires: 2026-08-10 00:10:04 UTC
💰 You will receive 5,00 € worth of BCH at claim time

Open Asgaya app to claim!
```

**Key features:**
- Human-readable header (EUR amount, expiry)
- Structured parameter block (machine-parseable)
- Version tag (`COVENANT_V25`) for future compatibility
- Funding TXID included (allows balance verification)

**Transport options:**
- **Current:** Telegram (copy/paste)
- **Future:** Nostr DM, QR code, NFC

---

### 2. NotificationListener (Android NotificationListenerService)

**File:** `NotificationListener.kt`

**Architecture:**

```kotlin
class NotificationListener : NotificationListenerService() {
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // 1. Extract notification extras
        val notification = sbn.notification
        val extras = notification.extras
        
        // 2. Get notification text (title + content)
        val title = extras.getString(Notification.EXTRA_TITLE, "")
        val text = extras.getCharSequence(Notification.EXTRA_TEXT, "").toString()
        val notificationText = "$title $text"
        
        // 3. Detect [COVENANT_V25] block
        if (notificationText.contains("[COVENANT_V25]")) {
            parseAndStoreCovenant(notificationText)
        }
    }
    
    private fun parseAndStoreCovenant(text: String) {
        // 4. Extract parameter block
        val covenantBlock = extractCovenantBlock(text)
        
        // 5. Parse key=value pairs
        val params = parseCovenantParams(covenantBlock)
        
        // 6. Create Remittance entity
        val remittance = Remittance(
            covenantAddress = params["covenantAddress"],
            recipient = "Covenant Claim",  // Placeholder
            senderPubkey = params["senderPubkey"],
            recipientPubkey = params["recipientPubkey"],
            sellerPubkey = params["sellerPubkey"],
            oraclePubkey = params["oraclePubkey"],
            eurCents = params["eurCents"]?.toInt(),
            expiryOracleTime = params["expiryOracleTime"]?.toLong(),
            initialBchPriceInCents = params["initialBchPriceInCents"]?.toInt(),
            minPricePercent = params["minPricePercent"]?.toInt(),
            fundingTxid = params["fundingTxid"],
            isReceived = true,  // ← Key flag!
            timestamp = System.currentTimeMillis()
        )
        
        // 7. Store in database
        remittanceDao.insertRemittance(remittance)
        
        Log.d(TAG, "✅ Received covenant stored: €${eurCents/100.0}")
    }
}
```

**Key patterns:**
- **Background service:** Runs even when app closed
- **Passive monitoring:** No polling, event-driven
- **System-level access:** Intercepts all notifications (with permission)
- **Structured parsing:** Regex to extract [COVENANT_V25] block
- **Database insert:** Triggers UI update via Room Flow

**Permissions required:**
```xml
<service
    android:name=".NotificationListener"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```

**User setup:** Settings → Notifications → Notification access → Asgaya → Enable

**Note:** `NotificationListenerService` is less intrusive than `AccessibilityService`:
- ✅ Only reads notification content (not screen control)
- ✅ Standard Android permission model
- ✅ No "view and control your screen" warning dialog
- ✅ More appropriate for notification parsing use case

---

### 3. Database Storage (Room)

**Entity:** `Remittance.kt`

```kotlin
@Entity(tableName = "remittances")
data class Remittance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val covenantAddress: String,
    val recipient: String,
    val senderPubkey: String?,
    val recipientPubkey: String?,
    val sellerPubkey: String?,        // ← Funder's pubkey (critical!)
    val oraclePubkey: String?,
    val eurCents: Int?,
    val expiryOracleTime: Long?,
    val initialBchPriceInCents: Int?,
    val minPricePercent: Int?,
    val fundingTxid: String?,
    val isReceived: Boolean = false,  // ← Differentiates sent vs received
    val timestamp: Long
)
```

**Key field: `isReceived`**
- `false` = Covenant created by this device (sent)
- `true` = Covenant received via notification (claim)

**DAO query:**

```kotlin
@Query("SELECT * FROM remittances ORDER BY timestamp DESC")
fun getAllRemittances(): Flow<List<Remittance>>
```

**Flow updates UI automatically:** When NotificationListener inserts a new received covenant, the Flow emits, and RemittanceAdapter updates instantly.

---

### 4. UI Components (Received Covenant Card)

**File:** `RemittanceAdapter.kt`

**Layout for received covenants:**

```
┌─────────────────────────────────────┐
│ ✓ Confirmed           Just now      │
│                                      │
│     Someone sent you 5,00 €          │
│     balance: 0.00769231 BCH          │
│                                      │
│ Covenant Status: ✅ funded           │
│ ⏰ 7h 13m until expires              │
│                                      │
│         🔄 Update Status  💰 Claim   │
└─────────────────────────────────────┘
```

**Key differences from sent covenants:**
- Heading: "Someone sent you X €" (not "To: [name]")
- No Transaction ID section (not relevant for recipient)
- Countdown timer (⏰) shows time until expiry
- Claim button (💰) instead of refund (↩️)

**Binding logic:**

```kotlin
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val remittance = remittances[position]
    
    if (remittance.isReceived) {
        // Received covenant layout
        holder.sentSection.visibility = View.GONE
        holder.receivedSection.visibility = View.VISIBLE
        
        holder.receivedAmount.text = "Someone sent you ${formatEur(remittance.eurCents)}"
        
        // Show countdown if expiry exists
        remittance.expiryOracleTime?.let { expiry ->
            val remaining = calculateTimeRemaining(expiry)
            holder.expiryCountdown.text = "⏰ $remaining until expires"
            holder.expiryCountdown.visibility = View.VISIBLE
        }
        
        // Claim button click
        holder.claimButton.setOnClickListener {
            showClaimDialog(remittance)
        }
    } else {
        // Sent covenant layout (normal)
        // ... (existing sent covenant UI)
    }
}
```

---

### 5. Manual Balance Check (Connection Management)

**Pattern:** User-triggered balance query (not automatic)

**Why manual:**
- Prevents connection pool exhaustion (no spam)
- Eliminates race conditions (user controls timing)
- Clearer UX (user knows when network call happens)
- Simpler code (no complex pooling)

**Implementation:**

```kotlin
holder.checkBalanceButton.setOnClickListener {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Query balance via TCP (ElectrumClient)
            val balance = electrumClient.getBalance(
                remittance.covenantAddress,
                host = "192.168.1.100",
                port = 60001  // TCP port (not WebSocket!)
            )
            
            withContext(Dispatchers.Main) {
                if (balance > 0) {
                    holder.covenantStatus.text = "✅ funded"
                    holder.claimButton.visibility = View.VISIBLE
                } else {
                    holder.covenantStatus.text = "✅ Claimed or refunded"
                    holder.claimButton.visibility = View.GONE
                }
                
                // ⚠️ CRITICAL: 5-second TCP cooldown
                // Wait for OS to fully close TCP connection before
                // allowing WebSocket operations (claim/refund)
                Log.d(TAG, "⏳ Waiting 5s for TCP connection cleanup...")
                delay(5000)  // Android OS needs time to release TCP socket
                Log.d(TAG, "✅ TCP connection cleanup complete")
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}
```

**TCP Cooldown Rationale:**
- TCP connection (port 60001) for balance query
- WebSocket connection (port 60003) for claim transaction
- Android OS doesn't release TCP instantly (~2-5 seconds)
- If claim attempted immediately, WebSocket hangs (OS still holding TCP)
- **Solution:** 5-second delay after balance query (tested: 2s not enough)

**⚠️ Phase 0 Workaround:**  
This 5-second blocking delay works for testing but is a UX concern at scale. A user tapping "Update Status" → "Claim" experiences a ~5-second pause before the claim button responds. Future improvements should explore:
- **Connection pooling:** Maintain persistent connections instead of connect/disconnect per operation
- **Connection reuse:** Share ElectrumClient instances across queries
- **Separate connection pools:** Isolate TCP (balance queries) from WebSocket (transactions)
- **Async UI feedback:** Show "Preparing claim..." during cooldown instead of blocking

**For now:** The 5-second delay is documented, tested, and prevents production hangs. It's acceptable for Phase 0 single-device-pair testing.

---

### 6. Wallet Matching (Critical Step!)

**File:** `RemittanceActivity.kt` → `executeClaim()`

**The Challenge:**

A covenant has multiple pubkeys:
- `senderPubkey` - Who created the covenant
- `recipientPubkey` - Who can claim the payment
- `sellerPubkey` - Who funded the covenant (gets buffer back!)

**The recipient device must:**
1. Find the wallet matching `recipientPubkey` (for signing the claim)
2. Find the wallet matching `sellerPubkey` (for buffer output address)

**Implementation:**

```kotlin
private fun executeClaim(remittance: Remittance) {
    lifecycleScope.launch {
        // 1. Find RECIPIENT wallet (for signing)
        val recipientPubkey = remittance.recipientPubkey
            ?: throw Exception("No recipientPubkey in remittance")
        
        Log.d(TAG, "Looking for recipient wallet: ${recipientPubkey.take(20)}...")
        val recipientWallet = walletManager.findWalletByPubkey(recipientPubkey)
            ?: throw Exception("You don't have the recipient wallet")
        
        Log.d(TAG, "✅ Found recipient wallet: ${recipientWallet.label}")
        val recipientWIF = walletManager.getWIF(recipientWallet)
        
        // 2. Find SELLER wallet (for buffer output!)
        // ⚠️ CRITICAL: sellerPubkey = funder (might be sender in self-funded!)
        val sellerPubkey = remittance.sellerPubkey
            ?: throw Exception("No sellerPubkey in remittance")
        
        Log.d(TAG, "Looking for seller wallet: ${sellerPubkey.take(20)}...")
        val sellerWallet = walletManager.findWalletByPubkey(sellerPubkey)
            ?: throw Exception("Seller wallet not found")
        
        Log.d(TAG, "✅ Found seller wallet: ${sellerWallet.label}")
        Log.d(TAG, "Seller address (for buffer): ${sellerWallet.address}")
        
        // 3. Generate oracle signature
        val currentBchPriceInCents = fetchOraclePrice()  // From Pi-chan oracle /oracle/price
        val oracleSig = covenantWebView.generateOracleSignature(currentBchPriceInCents)
        
        // 4. Call claim with BOTH wallet addresses
        val txid = covenantWebView.claimCovenant(
            covenantParams = buildCovenantParams(remittance),
            oracleSig = oracleSig,
            recipientWIF = recipientWIF,
            recipientAddress = recipientWallet.address,  // Payment goes here
            sellerAddress = sellerWallet.address,        // Buffer goes here!
            fulcrumHost = "192.168.1.100",
            fulcrumPort = 60003  // WebSocket port
        )
        
        Log.d(TAG, "✅ CLAIM SUCCESSFUL! TXID: $txid")
    }
}
```

**Why wallet matching is critical:**

See [Funder Principle - August 10 Bug](../../why-this-design/constraints/funder-principle.md#critical-production-blocking-bug-august-10-2026) for the production disaster we avoided by understanding this!

---

### 7. Transaction Building (Kotlin ↔ JavaScript Bridge)

**Kotlin side:** `CovenantWebView.kt`

```kotlin
suspend fun claimCovenant(
    covenantParams: Map<String, Any>,
    oracleSig: Map<String, Any>,
    recipientWIF: String,
    recipientAddress: String,
    sellerAddress: String,  // ← Added Aug 10 (critical fix!)
    fulcrumHost: String = "192.168.1.100",
    fulcrumPort: Int = 60003
): String = suspendCancellableCoroutine { continuation ->
    
    // Build parameters JSON
    val params = JSONObject().apply {
        put("covenantParams", JSONObject(covenantParams))
        put("oracleSig", JSONObject(oracleSig))
        put("recipientWIF", recipientWIF)
        put("recipientAddress", recipientAddress)
        put("sellerAddress", sellerAddress)  // Buffer output!
        put("fulcrumHost", fulcrumHost)
        put("fulcrumPort", fulcrumPort)
    }
    
    // Call JavaScript
    webView.post {
        webView.evaluateJavascript(
            "claimCovenant(\"${params.toString().replace("\"", "\\\"")}\")",
            null
        )
    }
    
    // Await callback
    claimContinuation = { txid -> continuation.resume(txid) }
    claimErrorContinuation = { error -> continuation.resumeWithException(error) }
}
```

**JavaScript side:** `covenant-bridge.html` → `window.claimCovenant()`

```javascript
window.claimCovenant = async function(paramsJson) {
    try {
        const params = JSON.parse(paramsJson);
        const {
            covenantParams, oracleSig, recipientWIF, 
            recipientAddress, sellerAddress,
            fulcrumHost, fulcrumPort
        } = params;
        
        // 1. Connect to Fulcrum (WebSocket)
        const useSSL = (fulcrumPort === 60004 || fulcrumPort === 50003 || fulcrumPort === 50004);
        const socket = new ElectrumWebSocket(fulcrumHost, fulcrumPort, useSSL);
        const electrum = new ElectrumClient('Asgaya Claim', '1.4.1', socket);
        const provider = new ElectrumNetworkProvider('testnet3', {
            electrum,
            manualConnectionManagement: true
        });
        
        await electrum.connect();
        log('✅ Connected to Fulcrum');
        
        // 2. Recreate covenant contract
        const contract = new Contract(
            ARTIFACT,
            [
                Buffer.from(covenantParams.senderPubkey, 'hex'),
                Buffer.from(covenantParams.recipientPubkey, 'hex'),
                Buffer.from(covenantParams.sellerPubkey, 'hex'),
                Buffer.from(covenantParams.oraclePubkey, 'hex'),
                BigInt(covenantParams.eurCents),
                BigInt(covenantParams.expiryOracleTime),
                BigInt(covenantParams.initialBchPriceInCents),
                BigInt(covenantParams.minPricePercent)
            ],
            { provider, addressType: 'p2sh32' }
        );
        
        // 3. Get covenant UTXO
        const utxos = await contract.getUtxos();
        if (utxos.length === 0) {
            throw new Error('No UTXOs - covenant not funded or already claimed');
        }
        const utxo = utxos[0];
        
        // 4. Calculate payment amounts
        const eurPayment = calculateEurValue(
            covenantParams.eurCents, 
            oracleSig.price
        );
        const buffer = utxo.satoshis - eurPayment - 1000n;  // 1000 sats fee
        
        log(`Payment: ${eurPayment} sats to recipient`);
        log(`Buffer: ${buffer} sats to seller`);
        
        // 5. Build transaction with unlock script
        const transaction = await contract.functions
            .claim(
                new SignatureTemplate(recipientWIF),  // Recipient signs
                Buffer.from(oracleSig.signature, 'hex'),
                Buffer.from(oracleSig.message, 'hex')
            )
            .to(recipientAddress, eurPayment)  // Output 0: Payment
            .to(sellerAddress, buffer)         // Output 1: Buffer to SELLER!
            .withHardcodedFee(1000n)
            .build();
        
        // 6. Broadcast
        log('Broadcasting transaction...');
        const txid = await electrum.request('blockchain.transaction.broadcast', 
            transaction.toHex()
        );
        
        log(`✅ SUCCESS! TXID: ${txid}`);
        
        // 7. Clean up
        await electrum.disconnect();
        log('🔌 Disconnected from Fulcrum');
        
        // 8. Callback to Kotlin
        Android.onClaimSuccess(txid);
        
    } catch (error) {
        log(`❌ Error: ${error.message}`);
        Android.onClaimError(error.message);
    } finally {
        // Always disconnect (even on error!)
        if (electrum) {
            await electrum.disconnect();
        }
    }
};
```

**Key patterns:**
- **CashScript SDK:** Recreates contract from parameters (no need to store bytecode)
- **SignatureTemplate:** Signs with recipient's WIF
- **Two outputs:** Payment to recipient, buffer to seller
- **Finally block:** Always disconnect (prevents connection leaks)

---

### 8. On-Chain Validation

**Covenant script validates:**

```cash
function claim(
    sig recipientSig,
    datasig oracleSig,
    bytes oracleMessage
) {
    // 1. Verify recipient signature
    require(checkSig(recipientSig, recipient));
    
    // 2. Verify oracle signature
    require(checkDataSig(oracleSig, oracleMessage, oraclePubkey));
    
    // 3. Parse oracle data
    int oraclePrice = int(oracleMessage.split(8)[1]);
    
    // 4. Calculate EUR payment amount
    int eurPayment = calculateEurValue(eurCents, oraclePrice);
    
    // 5. Validate output 0 (recipient payment)
    require(tx.outputs[0].value == eurPayment);
    require(hash160(tx.outputs[0].lockingBytecode) == recipient);
    
    // 6. Validate output 1 (buffer to SELLER = funder!)
    int buffer = tx.inputs[0].value - eurPayment - tx.outputs[2].value;
    require(tx.outputs[1].value >= buffer);
    require(hash160(tx.outputs[1].lockingBytecode) == seller);  // ← Key check!
}
```

**If any check fails:** Transaction rejected (e.g., August 10 bug caught here!)

---

## Production Evidence

**First successful claim:** August 10, 2026  
**Devices:** Moto G06 (sender) → Pixel 6a (recipient)  
**TXID:** `193c3c9e5287e13cc56e1401aed55de34db9a375312e052807aea060e58e3d96`

**Transaction breakdown:**

```
Input:
  Covenant UTXO: 827,129 sats

Output 0 (Recipient - Isabel):
  Amount: 769,230 sats (€5.00 at €650/BCH)
  Address: bchtest:qq2uxg4cu9axyzd9gjnhxwrvealt44mcwunp7gzd0k

Output 1 (Seller/Sender - Buffer):
  Amount: 56,899 sats (7.4% volatility protection)
  Address: bchtest:qrw5nukh5jqend8922tf8zhxwyku6wfpxu9nl79hxf

Transaction fee: 1,000 sats

Verification: bitcoin-cli -testnet gettransaction 193c3c9e...
Result: Both wallets confirmed receipt ✅
```

---

## Key Files

### Android App

```
app/src/main/java/com/asgaya/husk/
├── NotificationListener.kt              # Parses [COVENANT_V25] blocks
├── remittance/
│   ├── RemittanceActivity.kt            # executeClaim() orchestration
│   ├── RemittanceAdapter.kt             # Received covenant UI
│   └── Remittance.kt                    # Database entity (isReceived flag)
├── wallet/WalletManager.kt              # findWalletByPubkey()
└── CovenantWebView.kt                   # Kotlin ↔ JS bridge

app/src/main/assets/
└── covenant-bridge.html                 # JavaScript claim execution
```

### Documentation

```
docs/
├── why-this-design/constraints/
│   └── funder-principle.md              # Why buffer goes to seller
├── implementation/
│   ├── covenants/version-history.md     # Covenant v2.5 spec
│   └── android-app/
│       ├── claim-flow-end-to-end.md     # ← This document
│       └── connection-management-patterns.md  # TCP cooldown, WebSocket cleanup
```

---

## Known Issues & Solutions

### Issue 1: Seller Address Bug (RESOLVED Aug 10)

**Problem:** Buffer sent to recipient instead of seller  
**Impact:** All claims rejected by covenant validation  
**Solution:** Find seller wallet by `sellerPubkey`, use seller's address  
**Documentation:** [Funder Principle - August 10 Bug](../../why-this-design/constraints/funder-principle.md#critical-production-blocking-bug-august-10-2026)

---

### Issue 2: TCP Connection Race Condition (RESOLVED Aug 9-10)

**Problem:** Balance query (TCP) → immediate claim (WebSocket) hangs  
**Root cause:** Android OS needs 2-5 seconds to release TCP connection  
**Solution:** 5-second delay after balance queries  
**Documentation:** [Connection Management Patterns](connection-management-patterns.md) (to be created)

---

### Issue 3: WebSocket Connection Leaks (RESOLVED Aug 9)

**Problem:** First operation works, second hangs (zombie connections)  
**Root cause:** `disconnect()` only called on success, not on error  
**Solution:** `finally` blocks to ALWAYS disconnect  
**Status:** Fixed in all WebSocket operations (sendBch, refund, claim)

---

## Testing Checklist

Before deploying claim flow changes:

- [ ] ✅ NotificationListener parses [COVENANT_V25] block correctly
- [ ] ✅ Received covenant appears in Remittances tab
- [ ] ✅ Manual balance check shows "✅ funded" or "⚠️ Expired"
- [ ] ✅ 5-second TCP cooldown completes before claim allowed
- [ ] ✅ Wallet matching finds correct recipient wallet
- [ ] ✅ Wallet matching finds correct SELLER wallet (funder!)
- [ ] ✅ Transaction builds with two outputs (payment + buffer)
- [ ] ✅ Covenant validation passes (transaction broadcast succeeds)
- [ ] ✅ Both wallets show confirmed outputs on-chain
- [ ] ✅ Math verified: payment + buffer + fee = funded amount

**Verification command:**
```bash
bitcoin-cli -testnet -rpcwallet=sender gettransaction <TXID>
bitcoin-cli -testnet -rpcwallet=recipient gettransaction <TXID>
```

---

## Future Enhancements

### 1. Real Oracle Price Feed

**Current (Aug 16):** Pi-chan oracle husk running — `/oracle/price` serves signed prices, `/oracle/set-price` enables test price drops. Price source is still test-controlled.  
**Future:** Real-time market feed (mainnet), then progressive decentralization to network VWAP.  
**See:** [Asgaya Oracle Husk](../../the-mechanism/nostr-coordination/oracle-husk.md)  
**Priority:** High (before mainnet)

---

### 2. Nostr Parameter Transport

**Current:** Telegram copy/paste  
**Future:** Nostr DM with covenant NIP (Nostr Implementation Possibility)  
**Benefits:** Decentralized, no reliance on Telegram  
**Priority:** Medium

---

### 3. Merchant Cash-Out Flow

**Current:** "🏪 Cash out at merchant" button (placeholder)  
**Future:** Query bulletin board for nearby merchants, QR code handoff  
**Priority:** High (next major feature)

---

### 4. Multi-Covenant Batch Claiming

**Current:** Claim one covenant at a time  
**Future:** Select multiple received covenants → claim all in one transaction  
**Benefits:** Reduced fees, faster UX  
**Priority:** Low (optimization)

---

## Summary

**End-to-end claim flow:**
1. Sender creates + funds covenant → shares via Telegram
2. NotificationListener parses → stores in database
3. Recipient sees covenant → checks balance → taps claim
4. Wallet matching finds recipient + seller wallets
5. JavaScript builds transaction → broadcasts
6. Covenant validates on-chain → transaction confirms
7. Recipient gets EUR amount, sender gets buffer back

**Key insights:**
- **Cross-device coordination** via off-chain messaging
- **Smart contract enforcement** prevents errors (August 10 bug caught!)
- **Manual UX patterns** (balance check, claim button) prevent connection issues
- **Wallet matching** critical for correct address usage
- **End-to-end testing** essential (unit tests wouldn't catch August 10 bug)

**Production status:** ✅ Proven on testnet3 with real devices and real blockchain

---

**Last Updated:** 2026-08-10  
**Status:** Production-ready  
**Evidence:** TXID `193c3c9e5287e13cc56e1401aed55de34db9a375312e052807aea060e58e3d96`
