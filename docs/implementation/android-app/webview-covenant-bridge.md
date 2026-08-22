# WebView Covenant Bridge

**Status:** ✅ Production (August 2026) — **v0.2 Hybrid Architecture (Aug 20-21)**  
**Platform:** Android (Kotlin + JavaScript)  
**Validation:** 4 successful claims + 3 successful refunds on testnet3 (Aug 1-2, 2026); all 4 covenant operations on hybrid (Aug 20-21)

---

> ## ⚠️ MAJOR UPDATE (Aug 20-21, 2026): v0.2 Hybrid Architecture
>
> **The WebView no longer broadcasts transactions.** This is the single biggest change since this document was written.
>
> **Old (v0.1):** WebView did everything — build + sign + connect to Fulcrum via WebSocket + broadcast.
> **New (v0.2 hybrid):** WebView does **compute only** (build + sign). Kotlin owns **all network** (UTXO fetch + broadcast).
>
> ```
> v0.1 (broken at scale):  WebView: build → WebSocket → broadcast
> v0.2 (hybrid):           WebView: build+sign → return hex → Kotlin: broadcast (TCP)
> ```
>
> **Why:** WebView JavaScript timers pause when the screen is off / app is backgrounded. WebSocket connections from the WebView would hang indefinitely and accumulate (4+ ESTABLISHED observed), breaking multi-device use after 1-2 operations.
>
> **What changed:**
> - `TransactionBuilder.build()` instead of `.send()` — returns signed hex, fully local, no network
> - Kotlin `ElectrumClient.broadcast(txHex)` handles the broadcast (native TCP, 10s OS-level timeouts that actually fire)
> - New `CovenantBuildService.kt` wraps the Kotlin network operations
> - Results: CREATE ~100ms, REFUND/ABORT ~200ms, **zero WebSocket connections** on the critical path
>
> **Status of all 4 covenant operations:**
>
> | Operation | v0.2 Hybrid | Network Pattern |
> |-----------|------------|-----------------|
> | CREATE | ✅ Working | Kotlin TCP only (libauth, fully network-free WebView) |
> | REFUND | ✅ Working | Brief WebSocket for UTXO fetch + `build()` |
> | CLAIM | ✅ Working | Brief WebSocket for UTXO fetch + `build()` |
> | ABORT | ✅ Working | Brief WebSocket for UTXO fetch + `build()` |
>
> **The `build()` vs `send()` discovery (Aug 20):** CashScript's `send()` is actually `build + broadcast + wait_for_confirmation`. The `getTxDetails()` step polls `getRawTransaction()` for up to 10 minutes. We only wanted `build()`. See [connection-management-patterns.md](connection-management-patterns.md) Issue 4.
>
> **UTXO field naming (Aug 21):** Kotlin↔SDK conversion must use `txid`/`vout` (CashScript SDK convention), NOT `tx_hash`/`tx_pos` (libauth convention). CREATE uses libauth (needs `tx_hash`/`tx_pos`); covenant spend ops use CashScript SDK (needs `txid`/`vout`). Mixing them causes `hexToBin(undefined)` → "reading 'length'" crash.
>
> **This document below documents the v0.1 architecture.** Treat the WebSocket broadcast sections as historical. The hybrid pattern above is current.

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

## Architecture Overview (v0.2 Hybrid)

```
┌─────────────────────────────────────────────────────────────────┐
│                    Android App (Kotlin)                          │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ CovenantBuildService.kt      ← NETWORK OWNER (v0.2)        │ │
│  │  • fetchUTXOs(address)       → ElectrumClient (TCP 60001)  │ │
│  │  • broadcastTransaction(hex) → ElectrumClient (TCP 60001)  │ │
│  └──────────────┬─────────────────────────────────────────────┘ │
│                 │                                               │
│  ┌──────────────▼─────────────────────────────────────────────┐ │
│  │ CovenantWebView.kt                                         │ │
│  │  • Build request → evaluateJavascript("buildXTransaction") │ │
│  │  • Receives hex via onTransactionBuilt() callback          │ │
│  │  • Hands hex to CovenantBuildService.broadcastTransaction()│ │
│  └──────────────┬─────────────────────────────────────────────┘ │
│                 │                                               │
│                 ▼                                               │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ WebView (JavaScript) — COMPUTE ONLY (build+sign)           │ │
│  │  • covenant-bridge.html                                    │ │
│  │  • buildTransactionFromUTXOs() (CREATE, libauth)           │ │
│  │  • buildClaimTransaction() / buildRefundTransaction()      │ │
│  │  • buildAbortTransactionFromUTXOs() (CashScript SDK)       │ │
│  │  • Returns { txHex } via Android.onTransactionBuilt()      │ │
│  │  • NO broadcast, NO long-lived connections                 │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

**Key principle:** *WebView = compute (build/sign). Kotlin = network (broadcast/confirm).* This is the pattern mature apps (Selene, Paytaca) use — validated via RS083 addendum research.

**Note on UTXO fetch:** CREATE passes UTXOs from Kotlin (fully network-free WebView). REFUND/CLAIM/ABORT briefly connect to Fulcrum to fetch covenant UTXOs via `contract.getUtxos()` (needed for CashScript's `TransactionBuilder`), then disconnect. A future enhancement moves that fetch to Kotlin too.

---

## Architecture Overview (v0.1 — Historical)

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

### 1. Kotlin Side: `CovenantWebView.kt` (v0.2 hybrid)

**Responsibilities:**
- Load `covenant-bridge.html` from assets
- Expose JavaScript interface for Kotlin → JS calls
- Handle `onTransactionBuilt()` callback (hex from JS)
- **Hand the hex to `CovenantBuildService.broadcastTransaction()` for native broadcast**
- Manage WebView lifecycle

**Key methods (v0.2 hybrid):**
```kotlin
class CovenantWebView(context: Context) {
    // Create covenant (returns P2SH32 address — pure computation)
    fun createCovenant(...): String
    
    // v0.2 HYBRID: Kotlin fetches UTXOs, JS builds, Kotlin broadcasts
    suspend fun sendBchHybrid(...)   // CREATE — fully network-free WebView (libauth)
    suspend fun claimCovenantHybrid(...)   // CLAIM — brief UTXO fetch + build()
    suspend fun refundCovenantHybrid(...)  // REFUND — brief UTXO fetch + build()
    suspend fun executeAbortHybrid(...)    // ABORT — brief UTXO fetch + build()
}
```

**JavaScript Interface (v0.2 hybrid):**
```kotlin
@JavascriptInterface
fun onCovenantCreated(address: String, scriptHash: String, redeemScript: String) {
    // Callback to Kotlin with covenant details
}

@JavascriptInterface
fun onTransactionBuilt(resultJson: String) {
    // v0.2: JS returns { txHex }, Kotlin broadcasts natively
    val result = Json.decode(resultJson)
    viewModelScope.launch {
        val txid = CovenantBuildService.broadcastTransaction(result.txHex)
        _txState.value = TxState.Success(txid)
    }
}

@JavascriptInterface
fun onTransactionBuildError(error: String) {
    // Error handling (build failed, not broadcast)
}
```

**Bridge mechanism (unchanged):**
- Kotlin → JavaScript: `webView.evaluateJavascript("buildClaimTransaction('...')", null)`
- JavaScript → Kotlin: `Android.onTransactionBuilt(...)` where `Android` is the Kotlin-side interface injected via `webView.addJavascriptInterface(this, "Android")`
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

### 3. Covenant Operations: `covenant-bridge.html` (v0.2 hybrid)

**Key functions (v0.2 — build-only, no broadcast):**

```javascript
// CREATE (funding) — uses libauth directly, fully network-free (UTXOs from Kotlin)
window.buildTransactionFromUTXOs = async function(paramsJson) {
    const { senderWIF, senderAddress, recipientAddress, amountSats, utxos } = JSON.parse(paramsJson);
    // ... libauth UTXO selection + build + sign ...
    const txHex = libauth.binToHex(encodedTx);
    Android.onTransactionBuilt(JSON.stringify({ txHex }));  // Kotlin broadcasts
}

// CLAIM — CashScript SDK, brief UTXO fetch + build()
window.buildClaimTransaction = async function(paramsJson) {
    // ... connect briefly, contract.getUtxos(), build unlocker ...
    const txHex = txBuilder.build();      // ← build(), NOT send()!
    Android.onTransactionBuilt(JSON.stringify({ txHex }));
}

// REFUND — same pattern as claim
window.buildRefundTransaction = async function(paramsJson) { ... }

// ABORT — same pattern as claim
window.buildAbortTransactionFromUTXOs = async function(paramsJson) { ... }
```

**⚠️ CRITICAL — the two API conventions (Aug 21 discovery):**

| Library | UTXO field names | Used for |
|---------|-----------------|----------|
| **libauth** | `tx_hash` / `tx_pos` | CREATE (P2PKH funding tx) |
| **CashScript SDK** | `txid` / `vout` | CLAIM / REFUND / ABORT (covenant spends) |

When converting Kotlin UTXOs for the CashScript SDK, use `txid`/`vout`. Wrong names → `hexToBin(undefined)` → "Cannot read properties of undefined (reading 'length')". (See Issue 4 in connection-management-patterns.md.)

**⚠️ CRITICAL — `build()` vs `send()` (Aug 20 discovery):**
- `txBuilder.build()` → returns signed hex, fully local, no network, no broadcast
- `txBuilder.send()` → build + `sendRawTransaction` + **polls `getRawTransaction` up to 10 minutes**
- ALWAYS use `build()` and let Kotlin broadcast. Never `send()` in the WebView.

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
- **`evaluateJavascript()` returns `{}` for async functions** → Async JS functions return Promises, which serialize to an empty object. Real results arrive via the Android `@JavascriptInterface` callbacks, not the return value. (Discovered Aug 17 — this was the root of the stuck "Sending..." UI bug.)
- **Covenant address mismatch** → Check parameter order (reverse order for stack!)
- **Transaction broadcast fails** → Check Electrum server connectivity

---

## File Locations (v0.2)

```
app/src/main/
├── assets/
│   ├── covenant-bridge.html        # HTML wrapper (build functions, no broadcast)
│   └── cashscript-bundle.js        # Webpack output (~10MB)
├── java/com/asgaya/husk/
│   ├── covenant/
│   │   ├── CovenantBuildService.kt  # v0.2: Kotlin network ops (fetchUTXOs, broadcast)
│   │   └── CovenantWebView.kt       # Kotlin ↔ JS bridge
│   ├── viewmodel/
│   │   └── SendViewModel.kt         # v0.2: viewModelScope, DB persistence (RS083)
│   └── MainActivity.kt              # Usage example
```

---

## Usage Example

```kotlin
// In MainActivity or SendActivity
val covenantWebView = CovenantWebView(this)

// Create covenant
// NOTE: Since Aug 16, 2026 the oracle pubkey is fetched dynamically
// (fetchOraclePubkey() → GET {ORACLE_URL}/oracle/info), never hardcoded.
covenantWebView.createCovenant(
    senderPubkey = "02abc123...",
    recipientPubkey = "03def456...",
    sellerPubkey = "02ghi789...",
    oraclePubkey = fetchedOraclePubkey,  // From Pi-chan oracle API (no hardcoded keys)
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

## Migration Path

**Current state (Aug 2026):** v0.2 hybrid — WebView does compute (build/sign), Kotlin owns the network. This is the production architecture and matches the mature-app pattern (Selene, Paytaca).

**Potential future paths (Phase 1+, not blocking):**

1. **Move covenant UTXO fetch to Kotlin too** — REFUND/CLAIM/ABORT currently connect to Fulcrum briefly for `contract.getUtxos()`. Moving this to Kotlin (`ElectrumClient.getUTXOs()`) would make the WebView 100% network-free for all operations (matching CREATE). See [connection-management-patterns.md](connection-management-patterns.md).
2. **Pure Kotlin covenant construction (long-term ideal)** — replace the WebView entirely once manual construction is validated. A/B test in parallel, verify identical output, then sunset the WebView.
3. **Maintain WebView as fallback** — for platforms where manual construction isn't ported.

**Compatibility guarantee:** Covenant addresses/scripts are identical regardless of which construction path runs. Users can switch seamlessly.

---

## Further Reading

- [Manual Construction](../covenants/manual-construction.md) - Long-term ideal approach
- [Covenant Version History](../covenants/version-history.md) - v2.5 covenant specification
- [Covenant Simplicity Principle](../../why-this-design/constraints/covenant-simplicity-principle.md) - Why minimalism matters
- [Asgaya Trinity](./asgaya-trinity.md) - Three-part wallet architecture

---

**Last updated:** August 21, 2026 (v0.2 hybrid architecture update)  
**Production since:** August 1, 2026  
**Validation status:** ✅ Proven on testnet3 (7 transactions v0.1; all 4 operations on v0.2 hybrid Aug 20-21)
