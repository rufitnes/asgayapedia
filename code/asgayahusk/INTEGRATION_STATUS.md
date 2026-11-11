# bitcoin-kit-android Integration Status

**Date:** 2026-07-31  
**Phase:** Phase 1 Complete ✅  
**Research:** RS080_bitcoin_kit_android_integration.md

---

## Phase 1: Library Integration & Validation (COMPLETE)

### Dependencies Configured

✅ **JitPack repository** added to `settings.gradle.kts`
```kotlin
maven { url = uri("https://jitpack.io") }
```

✅ **Java 17** upgrade (required by library)
```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin { jvmToolchain(17) }
```

✅ **bitcoin-kit-android** added (master-SNAPSHOT to avoid dependency issues)
```kotlin
implementation("com.github.horizontalsystems.bitcoin-kit-android:bitcoincore:master-SNAPSHOT") {
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
}
implementation("com.github.horizontalsystems.bitcoin-kit-android:bitcoincashkit:master-SNAPSHOT") {
    exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
}
```

### Dependency Resolution Issues Solved

**Issue 1:** Version 1.2.0 depends on hd-wallet-kit commit `66c2544` which doesn't exist on JitPack  
**Solution:** Use `master-SNAPSHOT` instead (depends on commit `a74b51f3` which exists)

**Issue 2:** Duplicate BouncyCastle classes (library brings bcprov-jdk15on-1.65, we have bcprov-jdk18on-1.78)  
**Solution:** Exclude transitive BouncyCastle dependency from bitcoin-kit modules

### Test Results

All tests passing ✅

**Test File:** `app/src/test/kotlin/com/asgaya/husk/CashAddrLibraryTest.kt`

**Test Vectors:**
1. **P2SH20 (standard):**  
   Hash: `F5BF48B397DAE70BE82B3CCA4793F8EB2B6CDAC9` (20 bytes)  
   Address: `bitcoincash:pr6m7j9njldwwzlg9v7v53unlr4jkmx6eyguug74nh`

2. **P2SH32 (covenant):**  
   Hash: `3173EF6623C6B48FFD1A3DCC0CC6489B0A07BB47A37F47CFEF4FE69DE825C060` (32 bytes)  
   Address: `bitcoincash:pvch8mmxy0rtfrlarg7ucrxxfzds5pamg73h7370aa87d80gyhqxqaw3dsfwg`

3. **Placeholder (all zeros):**  
   Hash: `0000...0000` (32 bytes)  
   Address: `bitcoincash:pvqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqae05xh4w`

### Production Helper

**File:** `app/src/main/kotlin/com/asgaya/husk/utils/CashAddressHelper.kt`

Simple wrapper for production use:
```kotlin
val helper = CashAddressHelper()
val covenantAddress = helper.encodeP2SH32(redeemScriptHash)
```

---

## Next Phases

### Phase 2: Integrate into SendActivity (PENDING)

**Replace broken CashAddrEncoder with library:**

1. Import CashAddressHelper in SendActivity.kt
2. Replace manual encoding with `CashAddressHelper.encodeCovenantAddress(redeemScriptHash)`
3. Test with actual covenant creation flow
4. Validate generated address with bitcoin-cli

**Estimated time:** 30-60 minutes

### Phase 3: Basic Wallet Features (FUTURE)

Using full bitcoin-kit SDK:
- HD wallet creation (BIP32/44)
- Balance tracking
- Transaction history
- UTXO management
- SPV sync (block headers)

**Estimated time:** 2-3 days

### Phase 4: Covenant Integration (FUTURE)

Covenant-specific features:
- Covenant UTXO detection
- Recipient address validation
- Nostr notification integration
- Bull pool settlement tracking

**Estimated time:** 1-2 weeks

---

## Build Commands

**Clean build:**
```bash
./gradlew clean assembleDebug
```

**Run tests:**
```bash
./gradlew test
```

**Install to device:**
```bash
./gradlew installDebug
```

---

## Notes

- **Library choice:** horizontalsystems/bitcoin-kit-android chosen for:
  - ✅ Proven CashAddr encoding (test vectors included)
  - ✅ Full SPV wallet SDK (future-proof)
  - ✅ Active maintenance
  - ✅ P2SH32 support out of the box

- **Version choice:** master-SNAPSHOT preferred over 1.2.0 due to dependency resolution issues

- **BouncyCastle:** Using bcprov-jdk18on-1.78 (newer, Java 17 compatible)

- **Foundation-first approach:** Building basic wallet first, then adding covenant features on top
