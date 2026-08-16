# Funder Principle: Buffer Ownership Follows Funding

**The Constraint:** Who should receive the buffer when a covenant is refunded or expires?

**The Question:** Should buffer ownership be determined by role (sender/recipient/merchant) or by who provided the funds?

---

## What Constrains Us

- **Financial fairness** - Whoever risked capital should get it back
- **Parameter semantics** - Covenant parameters must have clear, consistent meaning
- **Multiple use cases** - Same covenant serves remittances AND merchant payments
- **User sovereignty** - No one should lose access to funds they provided
- **Simplicity** - Fewer special cases = easier to understand and audit

---

## The Decision: Buffer Goes to Funder

**The principle:**

> **Buffer ownership follows funding.** Whoever funded the covenant with BCH receives the buffer back, regardless of their role in the transaction.

---

### **The Tagline**

> *"Your BCH, your buffer. Always."*

---

**In practice:**

```
REMITTANCE FLOW (sender creates, BCH seller funds):
┌────────────────────────────────────────────┐
│ María (sender) creates covenant:           │
│ - Recipient: Elena                         │
│ - Amount: €100                             │
│ - Buffer: 7% (€7)                          │
│ - Funder: Isabel (BCH seller)              │
└────────────────────────────────────────────┘
                ↓
┌────────────────────────────────────────────┐
│ Isabel (BCH seller) funds covenant:        │
│ - Sends: 0.00749 BCH (€107)                │
│ - Gets back on refund: €107 (€100 + €7)    │
└────────────────────────────────────────────┘

MERCHANT PAYMENT FLOW (sender creates and funds):
┌────────────────────────────────────────────┐
│ Tourist creates covenant:                  │
│ - Recipient: Merchant                      │
│ - Amount: €50                              │
│ - Buffer: 7% (€3.50)                       │
│ - Funder: Tourist (sender)                 │
└────────────────────────────────────────────┘
                ↓
┌────────────────────────────────────────────┐
│ Tourist funds covenant:                    │
│ - Sends: 0.00374 BCH (€53.50)              │
│ - Gets back on refund: €53.50 (€50 + €3.50)│
└────────────────────────────────────────────┘
```

---

## The Covenant Parameter

**Covenant v2.5 parameter:**
```javascript
pubkey seller;  // Actually means: funder's public key (CashScript)
```

**Why "seller" instead of "funder"?**

The covenant was designed during remittance-first development (July 2026), where:
- The BCH **seller** provided the funds (always the funder)
- Naming matched the primary use case

**When merchant flows were validated (August 2026):**
- Tourist/sender provides funds (sender is funder)
- Parameter semantics remained correct (buffer → funder)
- Naming became slightly misleading (but didn't break functionality)

**Clarification:**
- `seller` parameter = public key hash of whoever funded the covenant
- Buffer always flows back to that address
- **No code changes needed** - just semantic understanding

---

## The Trade-off

| Gain | Consideration |
|------|---------------|
| **Financial fairness** (funder recovers capital) | Parameter name doesn't match all use cases |
| **Consistent semantics** (one rule, all flows) | Developers must understand "seller = funder" |
| **User sovereignty** (your BCH, your control) | Documentation burden (explain the naming) |
| **No special cases** (remittances = merchant = same logic) | Could cause confusion during code review |
| **Buffer protection** (funder never loses excess) | Future developers might expect "seller" to mean BCH seller only |

---

## Why This Design Works

### 1. Financial Fairness is Paramount

**Scenario:** María creates a €100 remittance covenant. Isabel (BCH seller) funds it with €107 BCH.

**Question:** If the covenant expires unclaimed, who should get the €107?

**Answer:** Isabel. She provided the capital. María never risked her own BCH.

**Principle:** Buffer ownership follows capital risk, not transaction initiation.

---

### 2. Multiple Use Cases, One Covenant

**Remittance Flow:**
- **Funder:** BCH seller (Isabel)
- **Buffer recipient:** Isabel ✅
- **Parameter value:** Isabel's pubkey hash

**Merchant Payment Flow:**
- **Funder:** Sender (Tourist)
- **Buffer recipient:** Tourist ✅  
- **Parameter value:** Tourist's pubkey hash

**Same covenant, same logic, different funding sources.** No special cases needed.

---

### 3. User Sovereignty Alignment

**Core principle:** Users control funds they provide.

**If buffer went to sender (always):**
- ❌ Remittances: BCH seller (Isabel) loses €7 if unclaimed
- ❌ Violates capital risk principle
- ❌ Disincentivizes BCH sellers

**If buffer went to recipient (always):**
- ❌ Merchant payments: Tourist loses €3.50 if rejected
- ❌ Violates "your BCH, your buffer" principle
- ❌ Recipient could grieft sender by not claiming

**If buffer goes to funder (current design):**
- ✅ Remittances: BCH seller gets €107 back if unclaimed
- ✅ Merchant payments: Tourist gets €53.50 back if rejected
- ✅ Funder always made whole
- ✅ No special cases

---

## Discovery Story (August 2, 2026)

**Context:** Testing merchant payment flows (tourist → merchant covenant)

**Observation:** Buffer was going to BCH seller (Isabel) instead of tourist (sender)

**Initial assumption:** "This is a bug - buffer should go to sender in merchant flows"

**Investigation:** Checked covenant parameters. Tourist's pubkey hash was in... `recipientPubkey` field, not `sellerPubkey`.

**Root cause:** Parameter population error - test script put tourist in wrong field.

**Realization:** Covenant logic was **correct**. Buffer goes to `seller` parameter (the funder). In merchant flows, sender **IS** the funder, so sender's pubkey should be in `seller` field.

**Outcome:**
- Fixed test scripts (put funder's pubkey in `seller` field, regardless of role)
- Documented parameter semantics (seller = funder)
- Validated 2 successful refunds with correct buffer distribution

**See:** [Version History - Funder Parameter Semantics](../../implementation/covenants/version-history.md#3-funder-parameter-semantics-discovered-august-2-2026)

---

## 🔥 CRITICAL: Production-Blocking Bug (August 10, 2026)

**Status:** Show-stopper bug discovered during first end-to-end claim test  
**Impact:** NO covenant could be claimed successfully  
**Resolution:** Fixed after 2 hours of debugging  
**Evidence:** First successful claim TXID: `193c3c9e5287e13cc56e1401aed55de34db9a375312e052807aea060e58e3d96`

---

### The Bug: Seller Address vs Seller Pubkey

**Context:** Implementing recipient claim flow for first production-ready end-to-end covenant test between two Android devices (Moto G06 → Pixel 6a).

**The critical confusion:**
- ✅ **August 2:** Discovered `sellerPubkey` parameter semantics (seller = funder)
- ✅ **Fixed:** Put correct pubkey in `sellerPubkey` field
- ❌ **August 10:** Forgot to use seller's **ADDRESS** for buffer output!

**What went wrong:**

```kotlin
// ❌ WRONG - Initial claim implementation (CovenantWebView.kt)
val params = JSONObject().apply {
    put("recipientAddress", recipientWallet.address)  // ✅ Correct
    put("sellerAddress", recipientAddress)  // ❌ WRONG! Buffer to recipient!
}
```

**Why this was catastrophic:**
- We correctly populated `sellerPubkey` in covenant parameters (August 2 lesson learned)
- But when building the CLAIM transaction, we sent buffer output to **recipient's address**
- The covenant validates that buffer output goes to the address matching `sellerPubkey`
- **Result:** Every claim attempt rejected by covenant validation!

---

### Covenant Rejection (Working as Designed!)

**Error message:**
```
Error: PriceOracle.cash Error in transaction at input 0
Reason: Unsuccessful evaluation: completed with a non-truthy value 
on top of the stack. Top stack item: ""
```

**What the covenant was checking:**

```cash
// Simplified claim path validation (CashScript)
require(recipientOutput.value == eurPayment);              // ✅ Passed
require(hash160(recipientOutput.lockingBytecode) == recipient);  // ✅ Passed

require(bufferOutput.value == buffer);                     // ✅ Passed
require(hash160(bufferOutput.lockingBytecode) == seller);  // ❌ FAILED!
// Expected: seller (sender's) address
// Got: recipient's address
// Transaction rejected!
```

**The covenant saved us!** It enforced the funder principle on-chain, preventing incorrect buffer distribution.

---

### Why This Bug Was Hard to Find

1. **Cryptic error:** "Unsuccessful evaluation" doesn't say WHICH output failed
2. **Multiple possibilities:** 
   - Oracle signature? ✅ (checked, was correct)
   - Price calculation? ✅ (checked, was correct)
   - UTXO state? ✅ (checked, covenant funded)
   - Port configuration? ✅ (checked, WebSocket working)
   - **Output addresses?** ← Found it after ~2 hours!

3. **Semantic confusion:** We fixed `sellerPubkey` parameter but forgot about `sellerAddress` in transaction building

4. **Self-funded covenant context:** 
   - In self-funded: sender = seller = funder
   - We had THREE wallets: sender, recipient, seller
   - But seller IS sender (same wallet!)
   - Easy to confuse which address to use

---

### The Fix

```kotlin
// ✅ CORRECT - After debugging (RemittanceActivity.kt + CovenantWebView.kt)

// 1. Find the SELLER wallet (funder) by pubkey
val sellerPubkey = remittance.sellerPubkey
val sellerWallet = walletManager.findWalletByPubkey(sellerPubkey)
    ?: throw Exception("Seller wallet not found")

// 2. Pass SELLER's ADDRESS for buffer output
val txid = covenantWebView.claimCovenant(
    covenantParams = covenantParams,
    oracleSig = oracleSig,
    recipientWIF = recipientWIF,
    recipientAddress = recipientWallet.address,  // Payment to recipient
    sellerAddress = sellerWallet.address,         // ✅ Buffer to seller (funder)!
    fulcrumHost = "192.168.1.100",
    fulcrumPort = 60003
)
```

**Key insight:** Just like we match `recipientPubkey` to find recipient wallet, we must match `sellerPubkey` to find seller wallet and use its address!

---

### On-Chain Verification (First Successful Claim)

**Date:** August 10, 2026  
**TXID:** `193c3c9e5287e13cc56e1401aed55de34db9a375312e052807aea060e58e3d96`  
**Verification:** Pi-chan testnet node (bitcoin-cli)

**Transaction breakdown:**
```
Covenant funded:    827,129 sats (€5 + 7% buffer at €650/BCH)

Output 0 (Recipient - Isabel):
  Amount: 0.00769230 BCH (769,230 sats)
  Address: bchtest:qq2uxg4cu9axyzd9gjnhxwrvealt44mcwunp7gzd0k
  Calculation: €5 ÷ €650 per BCH = 769,230 sats ✅

Output 1 (Seller/Sender - Volatility buffer):
  Amount: 0.00056899 BCH (56,899 sats)  
  Address: bchtest:qrw5nukh5jqend8922tf8zhxwyku6wfpxu9nl79hxf ← SENDER!
  Calculation: 827,129 - 769,230 - 1,000 (fee) = 56,899 sats ✅

Buffer percentage: 56,899 ÷ 769,230 ≈ 7.4% ✅
```

**Verification command:**
```bash
# On Pi-chan
bitcoin-cli -testnet -rpcwallet=sender gettransaction 193c3c9e5287...
# Shows BOTH outputs received by sender wallet ✅

bitcoin-cli -testnet -rpcwallet=recipient gettransaction 193c3c9e5287...
# Shows payment output received by recipient wallet ✅
```

**Result:** First guaranteed-value BCH transfer using native covenants between two Android devices! 🎉

---

### Lessons Learned

1. **Parameter vs Address:** Understanding `sellerPubkey` semantics ≠ using seller's address in transaction
   - August 2: Fixed parameter population (which pubkey goes where)
   - August 10: Fixed transaction building (which address gets buffer output)
   - **Both are critical!**

2. **Covenant as Safety Net:** The smart contract **prevented** shipping broken code
   - We couldn't "accidentally" send buffer to wrong address
   - Covenant validation forced us to debug and fix
   - **Design constraints enforced on-chain = production insurance**

3. **End-to-End Testing is Essential:** Unit tests wouldn't have caught this
   - Transaction built successfully (no syntax errors)
   - Only covenant validation (on-chain) caught the semantic error
   - **Always verify transactions on actual blockchain**

4. **Documentation Prevents Regression:** This bug WILL be reintroduced if not documented
   - Semantic confusion is easy (seller = funder, but which address?)
   - New developers won't know to match sellerPubkey → sellerWallet → sellerAddress
   - **This document is production insurance**

---

### Implementation Checklist (Updated)

When implementing covenant claiming or refunding:

- [ ] ✅ **Identify the funder:** Who provided BCH capital?
- [ ] ✅ **Check `sellerPubkey` parameter:** Whose pubkey is in the covenant?
- [ ] ✅ **Find seller wallet:** `walletManager.findWalletByPubkey(sellerPubkey)`
- [ ] ✅ **Get seller ADDRESS:** `sellerWallet.address` (not sender's, not recipient's!)
- [ ] ✅ **Pass seller address for buffer output:** `sellerAddress = sellerWallet.address`
- [ ] ✅ **Verify on-chain:** Check both outputs after transaction confirms

**Red flags:**
- ❌ Hardcoding sender address for buffer (wrong if BCH seller funded!)
- ❌ Sending buffer to recipient (violates funder principle!)
- ❌ Using active wallet instead of matching sellerPubkey (wrong wallet!)

---

### Why August 2 Fix Wasn't Enough

**August 2 fix:** Put correct pubkey in `sellerPubkey` parameter
- ✅ Covenant parameters populated correctly
- ✅ Covenant created with right pubkey hash
- ✅ Refund tests worked (refund path uses sender, not seller)

**What we missed:** Claim transaction building uses SELLER, not sender!
- ❌ Claim flow needs seller's ADDRESS (not just pubkey)
- ❌ Must look up wallet by sellerPubkey
- ❌ Extract address from that specific wallet

**Why refund worked but claim didn't:**
- Refund path: Sender refunds, so we used sender's wallet (obvious)
- Claim path: Recipient claims, buffer to seller (not obvious which wallet!)

**Takeaway:** Parameter semantics understanding must extend to **transaction building**, not just **parameter population**.

---

## Implementation Notes

### Correct Parameter Population

**Remittance covenant:**
```javascript
const covenantParams = {
    senderPubkey: maria.pubkey,        // Creates the covenant
    recipientPubkey: elena.pubkey,     // Claims the payment
    sellerPubkey: isabel.pubkey,       // ← Funder (BCH seller)
    oraclePubkey: oracle.pubkey,
    eurCents: 10000,                   // €100
    expiryOracleTime: now() + 8h,
    initialBchPriceInCents: 350,       // €3.50/BCH
    minPricePercent: 93                // 7% buffer
}
```

**Merchant payment covenant:**
```javascript
const covenantParams = {
    senderPubkey: tourist.pubkey,      // Creates the covenant
    recipientPubkey: merchant.pubkey,  // Claims the payment
    sellerPubkey: tourist.pubkey,      // ← Funder (same as sender!)
    oraclePubkey: oracle.pubkey,
    eurCents: 5000,                    // €50
    expiryOracleTime: now() + 1h,
    initialBchPriceInCents: 350,
    minPricePercent: 93
}
```

**Key insight:** `seller` = `sender` in merchant flows (tourist funds their own covenant).

---

### Buffer Distribution Logic

**Covenant v2.5 refund function (CashScript):**
```javascript
function refund(
    sig senderSig,
    datasig oracleSig,
    bytes8 oracleMessage
) {
    // Verify sender signature
    require(checkSig(senderSig, sender));
    
    // Parse oracle data
    (int price, int timestamp) = parseOracleMessage(oracleMessage);
    require(checkDataSig(oracleSig, oracleMessage, oraclePubkey));
    
    // Calculate refund amounts
    int bchSatoshis = tx.inputs[0].value;
    int eurPayment = calculateEurValue(eurCents, price);
    int buffer = bchSatoshis - eurPayment;
    
    // Distribute
    require(tx.outputs[0].value == eurPayment);
    require(hash160(tx.outputs[0].lockingBytecode) == sender);
    
    require(tx.outputs[1].value == buffer);
    require(hash160(tx.outputs[1].lockingBytecode) == seller);  // ← Buffer to funder
}
```

**Buffer always goes to `seller` parameter (the funder).** No conditional logic based on flow type.

---

## Alternative Approaches Considered

### Option 1: Separate "Funder" Parameter

**Proposal:** Add explicit `funderPubkey` parameter, rename `seller` to something clearer

**Pros:**
- Clear naming
- No confusion about parameter semantics

**Cons:**
- Adds complexity (one more parameter)
- Increases covenant bytecode size
- Breaks existing test infrastructure
- Not needed - current design works correctly

**Verdict:** ❌ Not worth the complexity for a naming issue

---

### Option 2: Role-Based Buffer Logic

**Proposal:** Buffer goes to sender in remittances, recipient in merchant payments

**Pros:**
- Matches role intuition

**Cons:**
- ❌ Violates capital risk principle (funder doesn't always get buffer)
- ❌ Requires on-chain flow detection (more complexity)
- ❌ Special cases = harder to audit
- ❌ Breaks financial fairness

**Verdict:** ❌ Violates core principles

---

### Option 3: Keep Current Design, Improve Documentation ✅

**Proposal:** Document parameter semantics clearly, update test scripts

**Pros:**
- ✅ No code changes (covenant already correct)
- ✅ Maintains simplicity (one rule, all flows)
- ✅ Financial fairness preserved
- ✅ Easy to understand once explained

**Cons:**
- Requires careful documentation
- Developers must learn "seller = funder" mapping

**Verdict:** ✅ Best approach - document and educate

---

## Production Impact

### Security Properties

**Guarantee:** Funder can always recover full capital (payment + buffer)

**Protection:**
- Remittances: BCH seller doesn't lose funds if recipient doesn't claim
- Merchant payments: Sender doesn't lose funds if merchant doesn't claim
- Price protection: Buffer absorbs volatility for both flows

**No griefing:** Recipient can't steal buffer by refusing to claim

---

### User Experience

**Remittance flow (Maria → Elena via Isabel):**
```
María creates covenant (€100)
Isabel funds with €107 BCH (seller = Isabel)
    ↓
If Elena claims:
  - Elena gets: €100
  - Isabel gets: €7 buffer back
  
If expired/unclaimed:
  - María refunds (she's sender)
  - Isabel gets: €107 (payment + buffer)
```

**Merchant flow (Tourist → Merchant):**
```
Tourist creates and funds covenant (€50 + €3.50 buffer)
    ↓
If merchant claims:
  - Merchant gets: €50
  - Tourist gets: €3.50 buffer back
  
If rejected/expired:
  - Tourist refunds
  - Tourist gets: €53.50 (payment + buffer)
```

**Consistent UX:** Funder always recovers capital. No surprises.

---

## Documentation Checklist

When explaining covenant parameters:

- [ ] ✅ Explain `seller` = funder (not always BCH seller)
- [ ] ✅ Show remittance example (BCH seller funds)
- [ ] ✅ Show merchant example (sender funds)
- [ ] ✅ Emphasize buffer follows funding
- [ ] ✅ Link to version-history.md for discovery story
- [ ] ✅ Note that naming is historical (covenant designed for remittances first)

---

## Related Constraints

**Related principles:**
- [Covenant Simplicity Principle](covenant-simplicity-principle.md) - One rule for all flows, no special cases
- [Progressive Payment Rollout](progressive-payment-rollout.md) - Remittances first, merchant payments later (explains naming evolution)

**Related documentation:**
- [Version History - Funder Parameter Semantics](../../implementation/covenants/version-history.md#funder-parameter-semantics) - Discovery details (August 2, 2026)
- [Covenant v2.5 Specification](../../implementation/covenants/version-history.md#v25-permissionless-refund-august-2026) - Technical implementation
- [Wallet Component](../../implementation/android-app/wallet.md) - Multi-wallet testing that revealed the parameter semantics

---

## Future Considerations

### Parameter Naming: RESOLVED in v2.6.1

**Status:** ✅ Done (August 15, 2026)

The `seller` → `funder` rename was applied in `price-oracle-v2.6.1.cash` at **zero cost** — constructor parameter names live in the artifact ABI, not the spending bytecode, so the compiled output is byte-identical to v2.6 (same address, no redeployment).

**Decision:** The version stays v2.6 (same address); `price-oracle-v2.6.1.json` is the production artifact with the correct `funder` parameter name.

**What this resolves:** The naming confusion that caused the Aug 2 (parameter population) and Aug 10 (seller address) bugs. Future developers see `funder`, which matches the semantics.

---

### Multi-Party Covenants

If future covenants involve multiple funders (e.g., 50/50 split funding):
- Buffer distribution would need pro-rata logic
- Current single-funder model would need extension
- Principle remains: buffer follows funding ratio

**Current scope:** Single-funder covenants only (Phase 0-1)

---

## Summary

**The funder principle:**
- Buffer ownership follows who provided the BCH
- Parameter semantics: `seller` = funder's pubkey hash
- Works correctly for both remittances and merchant payments
- No special cases needed
- Financial fairness preserved

**Key insight:** It's not about role (sender/recipient/merchant), it's about capital. Whoever risked BCH gets the buffer back.

**Your BCH, your buffer. Always.** 🔐

---

**Status:** Production-proven (August 2-10, 2026)  
**Implementation:** Covenant v2.5  
**Testing:** 2 successful testnet3 refunds + 1 successful claim with correct buffer distribution (August 10)  
**First Claim TXID:** `193c3c9e5287e13cc56e1401aed55de34db9a375312e052807aea060e58e3d96`  
**Documentation:** Complete - includes production bug discovery and resolution
