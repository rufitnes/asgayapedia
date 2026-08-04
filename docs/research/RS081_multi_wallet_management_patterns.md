# RS081: Multi-Wallet Management Patterns in Production BCH Wallets

**Date:** 2026-08-02  
**Type:** UX Research + Technical Architecture  
**Status:** Implemented (August 2-3, 2026)  
**Phase:** Phase 0 - Wallet UI Enhancement

---

## Executive Summary

**Goal:** Research how production BCH wallets handle multiple wallet/account management to inform Asgaya's wallet switching UI design.

**Key Findings:**
1. **HD Wallet Standard (BIP44)** - Industry standard uses account-level derivation (`m/44'/145'/account'`) for multiple wallets from single seed
2. **UX Pattern Consensus** - MetaMask, Trust Wallet, and major wallets use **top-center account selector** with dropdown list
3. **Bitcoin.com Architecture** - Proven implementation using Copay/Bitcore Wallet Service
4. **Storage Strategy** - Local encrypted private keys, BIP39 mnemonic backup, BIP32 HD derivation

**Asgaya Implementation:**
- **Approach:** Hybrid (HD wallet + imported test keys)
- **UI Pattern:** Top-bar wallet selector (💼 Sender ▼)
- **Storage:** Room database with Flow-based reactive updates
- **Testing:** 3 pre-seeded wallets (Sender, Recipient, Merchant) for rapid covenant testing

**Status:** ✅ Implemented August 2-3, 2026. Multi-wallet switching working with instant balance updates.

**See:** [Wallet Component](../implementation/android-app/wallet.md) for complete implementation documentation.

---

## Research Context

**Problem:** Asgaya needs multi-wallet switching to enable:
1. Fast testing of covenant flows (sender → recipient → merchant on one device)
2. Role-based wallet management (sender, recipient, merchant, oracle)
3. Single-device development workflow

**Research Question:** How do production BCH wallets implement multi-wallet/account management, and what UX patterns work best?

---

## Key Findings

### 1. HD Wallet Standard (BIP32/39/44)

**Sources:**
- [BIP44 Derivation Paths - River Financial](https://river.com/learn/terms/b/bip-44-derivation-paths-for-p2pkh/)
- [HD Wallets Derivation Paths - LearnMeABitcoin](https://learnmeabitcoin.com/technical/keys/hd-wallets/derivation-paths/)
- [COLDCARD - Bitcoin Derivation Paths](https://coldcard.com/learn/how-bitcoin-works/bitcoin-derivation-paths/)

**Standard Hierarchy:**
```
m / purpose' / coin_type' / account' / change / address_index

For Bitcoin Cash:
m / 44' / 145' / account' / 0 / 0
         └─BCH──┘ └─account─┘
```

**Account Switching:**
- **Account 0:** `m/44'/145'/0'/0/0` (default wallet)
- **Account 1:** `m/44'/145'/1'/0/0` (second wallet)
- **Account 2:** `m/44'/145'/2'/0/0` (third wallet)

**Key Benefit:** Single seed phrase recovers ALL accounts. User can switch between wallets while maintaining single backup.

---

### 2. UX Patterns - Account Switching

**Sources:**
- [MetaMask - Switching Accounts](https://support.metamask.io/configure/accounts/switching-accounts-in-metamask/)
- [MetaMask - How to Add Accounts](https://support.metamask.io/managing-my-wallet/accounts-and-addresses/how-to-add-accounts-in-your-wallet/)
- [Trust Wallet - Import Wallet](https://trustwallet.com/blog/guides/how-to-import-your-wallet-from-metamask-to-trust-wallet)

**MetaMask Mobile Pattern:**
```
┌─────────────────────────────────┐
│  [☰]    Account 1 ▼    [⚙️]    │  ← Tap to open selector
│         └─Selector─┘             │
├─────────────────────────────────┤
│  Balance: 0.5 ETH                │
│  ≈ $1,234.56                     │
└─────────────────────────────────┘
```

**Trust Wallet Pattern:**
```
┌─────────────────────────────────┐
│  💼 My Wallets ▼                │  ← Wallet selector
├─────────────────────────────────┤
│  Balance: $1,234.56              │
└─────────────────────────────────┘
```

**Common UX Principles:**
1. **Top-center placement** - Account selector at top of home screen
2. **Current account name** - Shows active wallet/account
3. **Dropdown/modal** - Tap to see all accounts
4. **Visual indicator** - Checkmark shows active account
5. **Quick add** - "+ Add account" option in selector
6. **Truncated addresses** - Show first/last chars (0x1234...5678)
7. **Balance preview** - Optional balance shown in selector list

---

### 3. Bitcoin.com Wallet Architecture

**Source:** [Bitcoin.com Wallet GitHub](https://github.com/Bitcoin-com/Wallet)

**Key Architecture:**
- **Base:** Fork of Copay Wallet
- **Backend:** Bitcore Wallet Service (BWS) for blockchain queries
- **Frontend:** Angular.js
- **Multi-sig Support:** 3-of-5, 2-of-3 configurations
- **Address Generation:** BIP32 hierarchical deterministic
- **Backup:** BIP39 mnemonics
- **Derivation:** BIP44 standard (`m/44'/coin'/account'`)

**Multi-Wallet Implementation:**
- Multiple wallets per app instance
- Each wallet can be different coin type
- All private keys stored locally (not cloud)
- Extended public keys used for P2SH addresses
- BWS handles peer sync and blockchain data

**Key Insight:** Uses **Bitcore Wallet Service** as abstraction layer between app and blockchain, enabling clean separation of wallet logic from UI.

---

### 4. Security & Storage Patterns

**Key Management:**
- **Single Seed:** BIP39 mnemonic (12-24 words) generates all accounts
- **Encrypted Storage:** Android KeyStore or EncryptedSharedPreferences
- **Local Only:** Private keys NEVER leave device
- **Backup:** Mnemonic phrase is only backup needed

**Storage Options Evaluated:**

**Option A: HD Wallet Only**
- ✅ Single backup (12-word phrase)
- ✅ Unlimited accounts from one seed
- ✅ Industry standard
- ❌ Can't import existing test wallets

**Option B: Imported Keys Only**
- ✅ Works with existing test wallets
- ✅ More control per wallet
- ❌ No single backup
- ❌ Multiple WIFs to manage

**Option C: Hybrid (RECOMMENDED & IMPLEMENTED)**
- ✅ HD for production users (clean UX, single backup)
- ✅ Imported for testing (use existing test wallets)
- ✅ Best of both worlds
- ⚠️ Slightly more complex implementation

---

## Asgaya Implementation (August 2026)

### Architecture Decision: Hybrid Approach

**Data Model:**
```kotlin
sealed class WalletSource {
    data class HDDerived(val accountIndex: Int)  // From BIP39 seed (Phase 1+)
    data class ImportedKey(val wif: String)      // From covenant-params (Phase 0)
}

data class Wallet(
    val id: String,
    val label: String,
    val role: WalletRole,  // SENDER, RECIPIENT, MERCHANT
    val source: WalletSource,
    val address: String,
    val publicKey: String,
    val isActive: Boolean = false
)
```

**Benefits:**
- HD wallets ready for production users (Phase 1+)
- Imported keys work with existing test infrastructure (Phase 0)
- No breaking changes to covenant testing workflow

---

### UI Implementation

**Asgaya Wallet Selector:**
```
┌─────────────────────────────────┐
│  [☰] Asgaya    💼 Sender ▼ [⚙️] │  ← Tap to switch
│                  └─────┘          │
├─────────────────────────────────┤
│  Balance: 0.05234 BCH            │
│  ≈ €52.34                        │
└─────────────────────────────────┘
```

**Selector Dialog:**
```
┌─────────────────────────────────┐
│  Select Wallet                   │
├─────────────────────────────────┤
│  ✓ 📤 Sender                     │  ← Active (checkmark)
│     bchtest:qrw5nu...            │
│     0.052 BCH                    │
│                                  │
│    📥 Recipient (Isabel)         │
│     bchtest:qq2uxg...            │
│     0.007 BCH                    │
│                                  │
│    🏪 Merchant (Bob)             │
│     bchtest:qz4lla...            │
│     0.000 BCH                    │
└─────────────────────────────────┘
```

**Role Icons:**
- 📤 SENDER - Creates covenants, sends payments
- 📥 RECIPIENT - Claims covenants, receives payments
- 🏪 MERCHANT - Cashes out, provides liquidity

**Note:** Role labels are **testing convenience only**, not enforced. Production users can label wallets however they want ("Merchant hot wallet", "Personal savings", "BCH seller funding", etc.)

---

### Testing Workflow Enabled

**Covenant Claim Test (All on One Device):**
```
1. Switch to "Sender" wallet 💼
2. Fund covenant with 0.00749 BCH
3. Send Telegram notification
4. Switch to "Recipient (Isabel)" wallet 💼
5. Tap "Claim Now" → WebView executes claim
6. Success! Balance updates instantly
```

**Impact:** Complete covenant testing on single device with instant role switching. Development velocity increased significantly.

---

## Production Impact

**Immediate Benefits (Phase 0):**
1. ✅ **Faster testing** - Switch between roles instantly
2. ✅ **Better UX** - Proven wallet selector pattern
3. ✅ **Single device** - Complete covenant testing on one phone
4. ✅ **Real wallets** - Actually sign transactions with different keys

**Long-term Benefits (Phase 1+):**
1. **User feature** - Some users need multiple wallets (personal, business, different countries)
2. **Role separation** - Clear visual distinction between sender/recipient/merchant roles
3. **Standard compliance** - BIP32/39/44 means compatible with other wallets
4. **Future-proof** - Can add hardware wallet support, multi-sig, etc.

**Security Considerations:**
- Encrypted storage (Android KeyStore)
- Local-only keys (never cloud)
- Optional PIN/biometric per wallet switch (Phase 1+)
- Separate backups for imported vs HD wallets

---

## Alternative Approaches Considered

**Option 1: Separate Apps**
- **Pros:** Complete isolation  
- **Cons:** Cumbersome, need multiple devices or constant reinstalling  
- **Verdict:** ❌ Too slow for development

**Option 2: Contact-Based Switching (No Real Wallets)**
- **Pros:** Simple, just changes display  
- **Cons:** Can't actually sign transactions with different keys  
- **Verdict:** ❌ Doesn't solve testing problem

**Option 3: HD Wallet ONLY**
- **Pros:** Clean, single backup  
- **Cons:** Can't import existing test wallets (sender, Isabel from covenant-params)  
- **Verdict:** ❌ Breaks existing test infrastructure

**Option 4: Imported Keys ONLY**
- **Pros:** Works with existing test wallets  
- **Cons:** No single backup for production users, multiple WIFs to manage  
- **Verdict:** ⚠️ Works but not ideal for production

**Option 5: Hybrid (HD + Imported) ✅ IMPLEMENTED**
- **Pros:** HD for production, imported for testing, best of both worlds
- **Cons:** Slightly more complex implementation  
- **Verdict:** ✅ Best approach

---

## References

**Wallet Architecture:**
- [Bitcoin.com Wallet (GitHub)](https://github.com/Bitcoin-com/Wallet)
- [Selene Wallet](https://selene.cash/)
- [Best Bitcoin Cash Wallets 2026](https://99bitcoins.com/bitcoin-wallet/bitcoin-cash/)

**BIP Standards:**
- [BIP44 Derivation Paths - River Financial](https://river.com/learn/terms/b/bip-44-derivation-paths-for-p2pkh/)
- [HD Wallets Derivation Paths - LearnMeABitcoin](https://learnmeabitcoin.com/technical/keys/hd-wallets/derivation-paths/)
- [COLDCARD Bitcoin Derivation Paths](https://coldcard.com/learn/how-bitcoin-works/bitcoin-derivation-paths/)
- [Trezor BIP44 Explanation](https://trezor.io/learn/advanced/standards-proposals/what-is-bip44)

**UX Patterns:**
- [MetaMask - Switching Accounts](https://support.metamask.io/configure/accounts/switching-accounts-in-metamask/)
- [MetaMask - Adding Accounts](https://support.metamask.io/managing-my-wallet/accounts-and-addresses/how-to-add-accounts-in-your-wallet/)
- [Trust Wallet - Import Guide](https://trustwallet.com/blog/guides/how-to-import-your-wallet-from-metamask-to-trust-wallet)

**Bitcoin Cash Specifics:**
- [Bitcoin Cash Research - HD Wallets](https://bitcoincashresearch.org/t/hd-wallet-backup-what-about-we-re-standardize-derivation-path/878)
- [Bitcoin.com Wallet - Derivation Issue](https://github.com/Bitcoin-com/Wallet/issues/68)

---

## Related Documentation

**Implementation:**
- [Wallet Component](../implementation/android-app/wallet.md) - Complete implementation details
- [Asgaya Trinity](../implementation/android-app/asgaya-trinity.md) - 3-part covenant architecture

**User Journeys:**
- [Sender Journey](../user-journeys/sender/README.md) - How senders create covenants
- [Recipient Journey](../user-journeys/recipient/README.md) - How recipients claim covenants

**Design Decisions:**
- [Why This Design](../why-this-design/README.md) - Architectural principles

---

**Status:** ✅ Research Complete → Implementation Complete (August 2-3, 2026)  
**Implementation Time:** ~90 minutes (as estimated)  
**Result:** Multi-wallet switching working with instant balance updates, 3 pre-seeded test wallets
