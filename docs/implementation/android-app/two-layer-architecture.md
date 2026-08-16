# Two-Layer Architecture: Covenant vs Client

**Core principle:** The covenant is permissionless. The client is opinionated.

**Why this matters:** Separating "what's possible" (covenant) from "what's recommended" (client) gives users maximum sovereignty while maintaining good UX.

**Status (August 14, 2026):**
- ✅ **Covenant Layer:** Accurate (v2.6 specification with 5 functions: claim, merchantCashout, refund, abort, sellerRecoverBuffer)
- ⚠️ **Client Layer:** Code examples are illustrative/future (Nostr monitoring not in Phase 0)
- ✅ **Core Principle:** Validated in production (refund anytime works, auto-refund logic tested)

**Note:** This document explains the architectural principle. Client layer code examples show future patterns (Nostr, auto-refund monitoring). Phase 0 implements manual refund only.

---

## The Two Layers

### Layer 1: Covenant (On-Chain, Immutable)

**What it does:** Defines what's cryptographically **possible**.

**Example (v2.5 refund):**
```cash
function refund(sig senderSig) {
    require(checkSig(senderSig, sender));
    
    // Output 0: Payment → sender
    // Output 1: Buffer → seller
}
```

**No restrictions:** No time check, no price check, no oracle. Just signature verification.

**Why:** Emergency escape. If client fails, user can manually refund with raw transaction. Permissionless.

### Layer 2: Client (Off-Chain, Updateable)

**What it does:** Enforces what's **appropriate**.

**Example (client auto-refund logic):**
```javascript
async function shouldAutoRefund() {
    const timeExpired = currentTime >= expiryTime;
    const priceDropped = currentPrice < priceFloor;
    
    return timeExpired || priceDropped;  // Only refund if legitimate
}

// UI hides manual refund button
<button disabled={!shouldAutoRefund()}>Refund</button>
```

**Restrictions:** Time, price, user intent. Only auto-refunds when conditions met.

**Why:** Good UX. Prevents accidental/malicious refunds. Protects reputation.

---

## Why Separate?

### The Problem with Single-Layer

**Option A: Enforce everything in covenant (complex)**

```cash
function refund(sig senderSig, datasig oracleSig, bytes oracleMessage) {
    require(checkSig(senderSig, sender));
    require(checkDataSig(oracleSig, oracleMessage, oraclePubkey));
    
    // Parse oracle data
    int oracleTimestamp = int(oracleMessage.split(8)[0]);
    int currentPrice = int(oracleMessage.split(8)[1]);
    
    // Enforce time OR price condition
    bool expired = oracleTimestamp >= expiryOracleTime;
    bool dropped = currentPrice < floorPrice;
    
    require(expired || dropped);  // ← What if oracle offline?
    
    // Refund outputs...
}
```

**Problems:**
- ❌ Oracle dependency (what if oracle fails?)
- ❌ Immutable logic (can't fix bugs or add conditions)
- ❌ Complex covenant (larger attack surface)
- ❌ No emergency escape (user trapped if edge case)

**Option B: Allow everything in covenant (unsafe)**

```cash
function refund(sig senderSig) {
    require(checkSig(senderSig, sender));
    // That's it! No restrictions.
}
```

**Problems:**
- ❌ No UX guidance (users don't know when to refund)
- ❌ Easy to abuse (sender refunds immediately)
- ❌ Reputation damage (recipient sees refund as scam)

### The Solution: Two-Layer

**Covenant:** Allow everything (permissionless)  
**Client:** Guide users (opinionated)

**Benefits:**
- ✅ Emergency escape (permissionless refund always works)
- ✅ Good UX (client only shows refund when appropriate)
- ✅ Updateable logic (fix bugs, add features, no covenant redeployment)
- ✅ Simple covenant (smaller attack surface)
- ✅ Social layer integration (Nostr monitors refunds, reputation system)

---

## The Bitcoin Analogy

**Bitcoin protocol (Layer 1):**
- Allows sending to **any address** (valid or not)
- Allows **any fee** (even 0 sats/byte)
- Allows **RBF** (replace-by-fee, even to yourself)

**Bitcoin wallets (Layer 2):**
- **Warn** about unconfirmed sends
- **Recommend** fee rates (fast/normal/slow)
- **Hide** RBF for most users (advanced mode only)

**Same pattern:** Protocol is permissionless, wallet is opinionated.

**Result:** Power users can do anything. Normal users get guided UX.

---

## Asgaya's Implementation

### Covenant Layer (v2.6)

**5 functions, 4 actors, permissionless:**

| Function | Restrictions | Purpose |
|----------|-------------|---------|
| `claim` | Oracle + time + price | Normal recipient flow |
| `merchantCashout` | Oracle + time + price + merchant sig | Cash pickup option |
| `refund` | **None** | Emergency sender escape |
| `abort` | Oracle + price (≤93.5%) | Emergency exit when buffer exhausted |
| `sellerRecoverBuffer` | Oracle + time (post-expiry) | Seller capital recovery |

**Key insight:** Only `claim` and `merchantCashout` have restrictions (they move funds to new parties). `refund`, `abort`, and `sellerRecoverBuffer` return funds to original funders—should be permissionless.

**v2.6 addition:** `abort` fixes fund locking when price drops below buffer capacity. The overlap zone (93.5% threshold) ensures both `abort` and `refund` work at critical prices, preventing lock scenarios.

### Client Layer (AsgayaHusk)

**Auto-refund monitoring:**
```kotlin
lifecycleScope.launch {
    while (isActive) {
        covenants.forEach { covenant ->
            if (shouldAutoRefund(covenant)) {
                // Build refund transaction
                val tx = buildRefundTx(covenant)
                
                // Sign with sender's key
                val signedTx = signTx(tx, senderWif)
                
                // Broadcast to network
                electrumClient.broadcast(signedTx)
                
                // Log to Nostr (transparency)
                publishRefundEvent(covenant, "auto-refund: expired")
            }
        }
        delay(5.minutes)
    }
}

fun shouldAutoRefund(covenant: Covenant): Boolean {
    val timeExpired = System.currentTimeMillis() >= covenant.expiryTime
    
    // Compute floor price from covenant parameters
    // floorPrice = initialBchPriceInCents * minPricePercent / 100
    val floorPrice = covenant.initialBchPriceInCents * covenant.minPricePercent / 100
    val priceDropped = getCurrentPrice() < floorPrice
    
    return timeExpired || priceDropped
}
```

**Manual refund (hidden by default):**
```kotlin
// Only show button if user enables advanced mode
if (settings.advancedMode) {
    Button(onClick = { 
        showConfirmation("Are you sure? This will damage your reputation.")
    }) {
        Text("Manual Refund (Emergency)")
    }
}
```

**Nostr monitoring (social layer):**
```kotlin
// Publish all refunds to Nostr
fun publishRefundEvent(covenant: Covenant, reason: String) {
    val event = NostrEvent(
        kind = 30078,  // Asgaya covenant event
        content = json {
            "covenant_address" to covenant.address
            "action" to "refund"
            "reason" to reason
            "timestamp" to System.currentTimeMillis()
        },
        pubkey = senderPubkey
    )
    
    nostrClient.publish(event)
}

// Recipient's client monitors
nostrClient.subscribe(filter = {
    authors = listOf(senderPubkey)
    kinds = listOf(30078)
}) { event ->
    if (event.content.action == "refund") {
        if (event.content.reason == "auto-refund: expired") {
            // Legitimate, ignore
        } else {
            // Manual refund! Show warning
            showNotification("⚠️ Sender refunded manually. Reputation: -1")
        }
    }
}
```

---

## Trade-Offs

### What We Gain

✅ **User sovereignty:** Emergency escape always available  
✅ **Updateable logic:** Fix bugs without redeploying covenant  
✅ **Simple covenant:** Smaller attack surface  
✅ **Flexible UX:** Add new conditions (e.g., "refund if recipient offline 24hr")  
✅ **Social layer:** Nostr monitoring enables reputation without on-chain enforcement  

### What We Accept

⚠️ **Two-layer mental model:** Developers must understand covenant ≠ client  
⚠️ **Trust assumption:** Recipient trusts sender won't abuse early refund  
⚠️ **Client diversity:** Different clients may enforce different policies  

### Why Trade-Offs Are Acceptable

**Historical precedent:** Bitcoin, Lightning, Ethereum—all have protocol vs application separation.

**User choice:** If sender's client is too restrictive, they can switch clients or use raw transactions.

**Market forces:** Abusive clients lose users. Good UX wins.

**Gradual ossification:** As ecosystem matures, best practices converge. Early flexibility enables discovery.

---

## Client Enforcement Strategies

### Strategy 1: Soft Enforcement (Default)

**UX hides manual refund:**
- Button disabled unless `shouldAutoRefund() == true`
- Tooltip: "Auto-refund will trigger when conditions met"
- Advanced mode: Enable manual button (with warning)

**Nostr transparency:**
- All refunds published to Nostr (auto vs manual tagged)
- Recipient sees reason ("expired" vs "manual")
- Reputation system tracks manual refunds

**Result:** Most users never see manual refund option. Advanced users can access if needed.

### Strategy 2: Hard Enforcement (Not Recommended)

**Client refuses to sign manual refunds:**
```kotlin
fun refund(covenant: Covenant, manual: Boolean = false) {
    if (manual && !shouldAutoRefund(covenant)) {
        throw Error("Refund conditions not met")
    }
    
    // Build and broadcast refund tx
}
```

**Problem:** User has no emergency escape if client bug prevents auto-refund. Violates user sovereignty.

**Asgaya's choice:** Soft enforcement. Trust users, monitor via Nostr.

### Strategy 3: Progressive Enforcement (Future)

**Day 1-30:** Manual refund available anytime (onboarding grace period)  
**Day 31+:** Manual refund requires 2FA + cooldown (24 hours warning)  
**Post-dispute:** Manual refund disabled for 7 days (reputation recovery)  

**Result:** Flexible onboarding, social consequences for abuse, time-based trust building.

---

## Edge Case Handling

### Case 1: Oracle Offline

**Covenant refund:** No oracle needed (permissionless)  
**Client logic:** Detects oracle downtime, triggers auto-refund after timeout  
**User experience:** Funds returned, no manual intervention  

### Case 2: Client Bug

**Covenant refund:** Still works (user can sign raw transaction)  
**Mitigation:** Client diversity (multiple implementations)  
**Backstop:** Manual refund instructions in documentation  

### Case 3: Network Split

**Covenant refund:** Works on any chain (sender has private key)  
**Client logic:** Detects fork, waits for confirmation  
**Social layer:** Nostr on both chains (reputation intact)  

### Case 4: Sender Malicious

**Covenant refund:** Works anytime (permissionless by design)  
**Client defense:** Nostr publishes refund, recipient warned  
**Long-term:** Reputation system prevents future covenants with this sender  

**Key insight:** On-chain enforcement can't prevent determined malicious actors. Social layer (reputation + Nostr) is more effective than complex covenants.

---

## Documentation Boundaries

**This document explains:** Covenant vs client separation (architectural principle).

**What it does NOT explain:**
- Covenant construction: See [Manual Construction](../covenants/manual-construction.md)
- Transaction building: See [Asgaya Trinity](./asgaya-trinity.md)
- Nostr coordination: See [Nostr Integration](../../the-mechanism/nostr-coordination/README.md)
- Reputation system: See [Social Layer](../../why-this-design/social-vs-technical-enforcement.md)

**Related principles:**
- [Covenant Simplicity Principle](../../why-this-design/constraints/covenant-simplicity-principle.md)
- [User Sovereignty](../../why-this-design/constraints/user-sovereignty.md)

---

## TL;DR

**Covenant = Permissionless**  
What's cryptographically possible. Allows emergency escapes.

**Client = Opinionated**  
What's recommended. Guides users, enforces fairness.

**Why both?**  
Covenant is immutable (on-chain), client is updateable (off-chain). Separation enables sovereignty + good UX.

**The pattern:**  
Same as Bitcoin (protocol vs wallet), Lightning (BOLT vs implementation), Ethereum (EVM vs dApp).

**The realization:**  
Moving enforcement from covenant to client solved all edge cases. Covenant stays simple, client handles complexity.

---

**Last updated:** July 30, 2026  
**Related:** [Manual Construction](../covenants/manual-construction.md) | [Asgaya Trinity](./asgaya-trinity.md) | [Covenant Simplicity](../../why-this-design/constraints/covenant-simplicity-principle.md)
