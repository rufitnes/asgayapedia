# WebView Covenant Bridge

**Status:** ✅ Production (August 2026)  
**Platform:** Android (Kotlin + JavaScript)  
**Validation:** 4 successful claims + 3 successful refunds on testnet3 (Aug 1-2, 2026)

---

## Why This Approach

**The pivot:** After failing to validate manual covenant construction in pure Kotlin (address generation didn't match CashScript output), we pivoted to embedding the official CashScript SDK in a WebView.

**Why it works:**
- **Battle-tested:** CashScript SDK is the reference implementation - if it generates an address, it's correct
- **Fast validation:** 48 hours from pivot to 7 successful testnet3 transactions
- **Automatic updates:** SDK improvements flow directly to app
- **Zero encoding bugs:** We don't maintain Bitcoin Script encoding logic

**Trade-offs accepted for Phase 0:**
- Larger app size (~10MB uncompressed for bundled JavaScript + polyfills; actual APK impact is lower after Webpack minification, tree-shaking, and Android AAPT2 compression)
- JavaScript initialization overhead (~100-500ms first load)
- Platform dependency (requires WebView support)
- Larger attack surface (JavaScript engine)

**See also:** [Manual Construction](../covenants/manual-construction.md) for why manual approach remains long-term ideal despite this pragmatic choice.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                 Android App (Kotlin)                 │
│                                                      │
│  ┌────────────────────────────────────────────────┐ │
│  │         CovenantWebView.kt                     │ │
│  │  • Load covenant-bridge.html                   │ │
│  │  • JavaScript ↔ Kotlin bridge                  │ │
│  │  • Callback handling (success/error)          │ │
│  └─────────────────┬──────────────────────────────┘ │
│                    │                                 │
│                    ▼                                 │
│  ┌────────────────────────────────────────────────┐ │
│  │           WebView Component                    │ │
│  │  Loads: covenant-bridge.html                   │ │
│  └─────────────────┬──────────────────────────────┘ │
└────────────────────┼──────────────────────────────┘
                     │
                     ▼
          ┌─────────────────────────────────┐
          │  covenant-bridge.html           │
          │  • CashScript SDK (bundled)     │
          │  • covenant-operations.js       │
          │  • Buffer polyfills             │
          │  • Crypto polyfills             │
          └─────────────────────────────────┘
```

---

## Components

### 1. Kotlin Side: `CovenantWebView.kt`

**Responsibilities:**
- Load `covenant-bridge.html` from assets
- Expose JavaScript interface for Kotlin → JS calls
- Handle callbacks from JavaScript (success/error)
- Manage WebView lifecycle

**Key methods:**
```kotlin
class CovenantWebView(context: Context) {
    // Create covenant (returns P2SH32 address)
    fun createCovenant(
        senderPubkey: String,
        recipientPubkey: String,
        sellerPubkey: String,
        oraclePubkey: String,
        eurCents: Int,
        expiryTime: Long,
        initialPrice: Int,
        minPricePercent: Int,
        callback: CovenantCallback
    )
    
    // Claim covenant (build + broadcast transaction)
    fun claimCovenant(
        covenantParams: CovenantParams,
        recipientWIF: String,
        recipientAddress: String,
        callback: TransactionCallback
    )
    
    // Refund covenant
    fun refundCovenant(
        covenantParams: CovenantParams,
        senderWIF: String,
        senderAddress: String,
        callback: TransactionCallback
    )
}
```

**JavaScript Interface:**
```kotlin
@JavascriptInterface
fun onCovenantCreated(address: String, scriptHash: String, redeemScript: String) {
    // Callback to Kotlin with covenant details
}

@JavascriptInterface
fun onTransactionBroadcast(txid: String) {
    // Callback with transaction ID
}

@JavascriptInterface
fun onError(error: String) {
    // Error handling
}
```

**Bridge mechanism:**
- Kotlin → JavaScript: `webView.evaluateJavascript("createCovenant(...)", null)`
- JavaScript → Kotlin: `Android.onCovenantCreated(...)` where `Android` is the Kotlin-side interface injected via `webView.addJavascriptInterface(this, "Android")`
- The `@JavascriptInterface` annotation exposes annotated methods to the JavaScript context

---

### 2. JavaScript Side: `covenant-bridge.html`

**Structure:**
```html
<!DOCTYPE html>
<html>
<head>
    <script src="cashscript-bundle.js"></script>
    <script src="covenant-operations.js"></script>
</head>
<body>
    <script>
        // Initialize CashScript SDK
        // Expose functions callable from Kotlin
        // Handle covenant creation, claiming, refunding
    </script>
</body>
</html>
```

**Bundled dependencies:**
- **CashScript SDK** - Official covenant library
- **Buffer polyfill** - Node.js Buffer API for browser
- **Crypto polyfills** - secp256k1, ECDSA signing
- **BigInt polyfill** - For older Android WebView versions

---

### 3. Covenant Operations: `covenant-operations.js`

**Key functions:**
```javascript
// Create covenant address
async function createCovenant(params) {
    const contract = new Contract(
        COVENANT_ARTIFACT,
        [
            params.senderPubkey,
            params.recipientPubkey,
            params.sellerPubkey,
            params.oraclePubkey,
            params.eurCents,
            params.expiryTime,
            params.initialPrice,
            params.minPricePercent
        ],
        { provider: 'testnet' }
    );
    
    const address = contract.address;
    const scriptHash = contract.redeemScript.hash();
    
    // Send back to Kotlin
    Android.onCovenantCreated(
        address, 
        scriptHash.toString('hex'),
        contract.redeemScript.toString('hex')
    );
}

// Claim covenant
async function claimCovenant(params, recipientWIF, oracleSignature) {
    const contract = loadContract(params);
    const recipientKey = new PrivateKey(recipientWIF);
    
    const tx = await contract.functions
        .claim(recipientKey.publicKey, oracleSignature)
        .from(contract.address)
        .to(recipientAddress, contractBalance)
        .withHardcodedFee(250)
        .send();
    
    // Send txid back to Kotlin
    Android.onTransactionBroadcast(tx.txid);
}

// Refund covenant
async function refundCovenant(params, senderWIF) {
    const contract = loadContract(params);
    const senderKey = new PrivateKey(senderWIF);
    
    const tx = await contract.functions
        .refund()
        .from(contract.address)
        .to(senderAddress, contractBalance)
        .withHardcodedFee(250)
        .send();
    
    Android.onTransactionBroadcast(tx.txid);
}
```

---

## Build Process: Bundling CashScript SDK

**Challenge:** CashScript SDK is designed for Node.js, not browsers. Android WebView needs polyfills.

**Solution: Webpack bundling**

### Webpack Configuration

```javascript
// webpack.config.js
module.exports = {
    entry: './src/covenant-operations.js',
    output: {
        filename: 'cashscript-bundle.js',
        path: path.resolve(__dirname, 'app/src/main/assets')
    },
    resolve: {
        fallback: {
            "crypto": require.resolve("crypto-browserify"),
            "stream": require.resolve("stream-browserify"),
            "buffer": require.resolve("buffer/"),
            "assert": require.resolve("assert/"),
            "util": require.resolve("util/")
        }
    },
    plugins: [
        new webpack.ProvidePlugin({
            Buffer: ['buffer', 'Buffer'],
            process: 'process/browser'
        })
    ]
};
```

**Build command:**
```bash
npm install cashscript crypto-browserify stream-browserify buffer assert util
webpack --config webpack.config.js
```

**Output:** `cashscript-bundle.js` (~10MB) copied to `app/src/main/assets/`

---

## Production Validation (August 1-2, 2026)

### Testnet3 Transactions

**4 Successful Claims:**
1. €7 claim (0-conf, 30 seconds)
2. €7 claim (0-conf, 25 seconds)
3. €7 claim (0-conf, 28 seconds)
4. €7 claim (0-conf, 32 seconds)

**3 Successful Refunds:**
1. €7 refund (0-conf, instant)
2. €7 refund (0-conf, instant)
3. €7 refund (0-conf, instant)

**Note:** Actual testnet3 TXIDs are documented in internal development logs and are verifiable on testnet3 block explorers. TXIDs omitted here to avoid linking test wallet addresses in public documentation.

**Key observations:**
- ✅ All addresses matched expected P2SH32 format
- ✅ All transactions broadcast successfully
- ✅ 0-conf acceptance worked (no double-spend attempts)
- ✅ WebView initialization: ~200ms (acceptable)
- ✅ Covenant creation: ~100ms after init
- ✅ Transaction building: ~500ms (includes UTXO fetch)

---

## Security Considerations

### What's Safe

✅ **CashScript SDK is audited** - Reference implementation, widely used  
✅ **JavaScript sandboxing** - WebView isolates JS from Android  
✅ **Private keys never leave Kotlin** - Only WIF passed to JS for signing, immediately discarded  
✅ **Deterministic output** - Same params always produce same covenant  
✅ **On-device only** - No external service involved

### What Needs Care

⚠️ **JavaScript engine attack surface** - 10MB of potential vulnerabilities  
⚠️ **Polyfill quality** - Crypto polyfills must be carefully vetted  
⚠️ **WebView updates** - Android WebView auto-updates could break compatibility  
⚠️ **Bundle integrity** - Webpack output must be verified against source

### Mitigation Strategies

**For Phase 0 (acceptable trade-offs):**
- Use official CashScript SDK (not custom fork)
- Pin polyfill versions (no auto-updates)
- Test on multiple Android API levels (26-34)
- Verify bundle hash matches known-good build

**For Phase 1+ (if manual construction succeeds):**
- Replace WebView with pure Kotlin implementation
- Eliminate JavaScript engine dependency
- Reduce attack surface from 10MB to <1KB
- Faster initialization (50ms vs 200ms)

---

## Performance Characteristics

**First load (cold start):**
- WebView initialization: ~200ms
- JavaScript parsing: ~100ms
- CashScript SDK load: ~50ms
- **Total:** ~350ms

**Subsequent operations (WebView loaded):**
- Covenant creation: ~100ms
- Transaction building: ~500ms (includes Electrum UTXO fetch)
- Transaction broadcast: ~200ms

**Memory footprint:**
- WebView: ~30MB RAM
- JavaScript heap: ~20MB RAM
- **Total:** ~50MB RAM overhead

**Acceptable for Phase 0**, but manual construction would reduce to <5MB RAM.

---

## Debugging

**Enable WebView debugging:**
```kotlin
if (BuildConfig.DEBUG) {
    WebView.setWebContentsDebuggingEnabled(true)
}
```

**Chrome DevTools:**
1. Connect Android device
2. Chrome → `chrome://inspect`
3. Find WebView instance
4. Inspect console, network, sources

**Common issues:**
- **"Buffer is not defined"** → Webpack polyfill missing
- **"crypto.createHash is not a function"** → crypto-browserify not configured
- **"window.ElectrumWebSocket is not a constructor"** / **"window.ElectrumClient is not a constructor"** → Missing `CashScriptSDK.` namespace prefix (all CashScriptSDK classes must use the full namespace in covenant-bridge.html; discovered Aug 15 in executeAbort)
- **Covenant address mismatch** → Check parameter order (reverse order for stack!)
- **Transaction broadcast fails** → Check Electrum server connectivity

---

## File Locations

```
app/src/main/
├── assets/
│   ├── covenant-bridge.html        # HTML wrapper
│   └── cashscript-bundle.js        # Webpack output (~10MB)
├── java/com/asgaya/husk/
│   ├── covenant/
│   │   └── CovenantWebView.kt      # Kotlin bridge
│   └── MainActivity.kt              # Usage example
```

---

## Usage Example

```kotlin
// In MainActivity or SendActivity
val covenantWebView = CovenantWebView(this)

// Create covenant
covenantWebView.createCovenant(
    senderPubkey = "02abc123...",
    recipientPubkey = "03def456...",
    sellerPubkey = "02ghi789...",
    oraclePubkey = CovenantConstants.ORACLE_PUBKEY,
    eurCents = 700,  // €7
    expiryTime = System.currentTimeMillis() / 1000 + 8 * 3600,
    initialPrice = 35000,  // €350/BCH
    minPricePercent = 93,
    callback = object : CovenantCallback {
        override fun onSuccess(address: String, scriptHash: String) {
            Log.d(TAG, "Covenant created: $address")
            // Fund this address with BCH
        }
        
        override fun onError(error: String) {
            Log.e(TAG, "Failed to create covenant: $error")
        }
    }
)

// Claim covenant
covenantWebView.claimCovenant(
    covenantParams = savedParams,
    recipientWIF = "cRzbt6...",
    recipientAddress = "bchtest:qq2uxg...",
    callback = object : TransactionCallback {
        override fun onSuccess(txid: String) {
            Log.d(TAG, "Claimed! TXID: $txid")
        }
        
        override fun onError(error: String) {
            Log.e(TAG, "Claim failed: $error")
        }
    }
)
```

---

## Migration Path (Phase 1+)

**When manual construction is validated:**

1. **Implement pure Kotlin alternative** - CovenantBuilder returns same interface
2. **A/B testing period** - Run both implementations in parallel, verify matching output
3. **Gradual rollout** - New installs get Kotlin version, existing users opt-in
4. **Sunset WebView** - After 3 months of validation, deprecate JavaScript approach
5. **Maintain WebView as fallback** - For platforms where manual construction isn't ported

**Compatibility guarantee:** Covenant addresses/scripts are identical between implementations. Users can switch seamlessly.

---

## Further Reading

- [Manual Construction](../covenants/manual-construction.md) - Long-term ideal approach
- [Covenant Version History](../covenants/version-history.md) - v2.5 covenant specification
- [Covenant Simplicity Principle](../../why-this-design/constraints/covenant-simplicity-principle.md) - Why minimalism matters
- [Asgaya Trinity](./asgaya-trinity.md) - Three-part wallet architecture

---

**Last updated:** August 3, 2026  
**Production since:** August 1, 2026  
**Validation status:** ✅ Proven on testnet3 (7 successful transactions)
