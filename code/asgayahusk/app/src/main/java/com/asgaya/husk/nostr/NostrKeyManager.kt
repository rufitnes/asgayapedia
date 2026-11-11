package com.asgaya.husk.nostr

import android.content.Context
import android.content.SharedPreferences
import com.asgaya.husk.data.Wallet
import org.json.JSONObject
import java.math.BigInteger
import java.security.SecureRandom

/**
 * NostrKeyManager - Manages Nostr identity keys for each wallet
 *
 * Phase 0: Random secp256k1 keypair per wallet (unblocks Nostr implementation)
 * Phase 1+: Derive from HD wallet mnemonic at m/44'/1237'/0'/0/{walletIndex}
 *
 * Key Generation:
 * - Uses SecureRandom for cryptographic quality randomness
 * - Validates scalar < secp256k1 curve order n (retries if invalid)
 * - Derives BIP-340 x-only public key (32 bytes, even-y)
 * - Encodes as NIP-19 bech32 (nsec/npub)
 *
 * Storage:
 * - Phase 0: Plaintext in SharedPreferences (testnet, TODO: encrypt for production)
 * - Phase 1+: Android Keystore or encrypted storage
 *
 * References:
 * - BIP-340: Schnorr Signatures for secp256k1
 * - NIP-06: Key Derivation from Mnemonic
 * - NIP-19: bech32-encoded entities
 *
 * @see TightDS file 12 for approval and requirements
 */
class NostrKeyManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "nostr_keys",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_PREFIX = "nostr_keys_"
    }

    /**
     * Data class holding Nostr identity keys
     */
    data class NostrKeys(
        val privateKeyHex: String,  // 32-byte private key (hex)
        val publicKeyHex: String,   // 32-byte x-only public key (hex)
        val nsec: String,           // Bech32 encoded private key (NIP-19)
        val npub: String            // Bech32 encoded public key (NIP-19)
    )

    /**
     * Ensure wallet has Nostr keys (generates if missing)
     *
     * @param wallet The wallet to ensure has Nostr keys
     * @return NostrKeys for this wallet
     */
    fun ensureNostrKeys(wallet: Wallet): NostrKeys {
        // Check if keys already exist
        val existing = getNostrKeys(wallet.id)
        if (existing != null) return existing

        // Phase 0 test: deterministic identity for seeded test wallets, so the
        // contact's npub is reproducible across devices (file 46 debt). Production:
        // random now, HD-derived later. See collaborative_workspace/contact-exchange/.
        val testPrivHex = TestNostrKeys.nostrPrivKeyHexFor(wallet.publicKey)
        val keys = if (testPrivHex != null) {
            buildKeys(BigInteger(testPrivHex, 16))
        } else {
            generateNostrKeys()
        }

        // Store keys
        storeNostrKeys(wallet.id, keys)

        return keys
    }

    /**
     * Get existing Nostr keys for a wallet
     *
     * @param walletId Wallet ID (UUID string)
     * @return NostrKeys if exists, null otherwise
     */
    fun getNostrKeys(walletId: String): NostrKeys? {
        val json = prefs.getString(KEY_PREFIX + walletId, null) ?: return null

        return try {
            val obj = JSONObject(json)
            NostrKeys(
                privateKeyHex = obj.getString("privateKeyHex"),
                publicKeyHex = obj.getString("publicKeyHex"),
                nsec = obj.getString("nsec"),
                npub = obj.getString("npub")
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generate new Nostr keypair
     *
     * Algorithm:
     * 1. Generate 32 random bytes using SecureRandom
     * 2. Validate scalar < curve order n (retry if invalid)
     * 3. Normalize to even-y (BIP-340 requirement) via NostrCrypto
     * 4. Derive x-only public key (32 bytes)
     * 5. Encode as NIP-19 bech32 (nsec/npub)
     *
     * @return NostrKeys with fresh keypair (normalized to even-y)
     */
    private fun generateNostrKeys(): NostrKeys {
        val random = SecureRandom()

        // Generate valid private key (scalar < n)
        var privKeyBigInt: BigInteger

        do {
            val randomBytes = ByteArray(32)
            random.nextBytes(randomBytes)
            privKeyBigInt = BigInteger(1, randomBytes)
        } while (!NostrCrypto.isValidScalar(privKeyBigInt))

        return buildKeys(privKeyBigInt)
    }

    /**
     * Build a NostrKeys from a private key scalar.
     *
     * Normalizes to even-y (BIP-340), derives the x-only public key, and encodes
     * nsec/npub (bech32 is a Phase 0 placeholder). Shared by random generation
     * and the deterministic test path.
     */
    private fun buildKeys(privKeyBigInt: BigInteger): NostrKeys {
        // Normalize to even-y (BIP-340)
        val privKeyBytes = NostrCrypto.normalizePrivateKey(privKeyBigInt)

        // Derive x-only public key (BIP-340)
        val pubKeyBytes = NostrCrypto.deriveXOnlyPublicKey(privKeyBytes)

        return NostrKeys(
            privateKeyHex = privKeyBytes.toHexString(),
            publicKeyHex = pubKeyBytes.toHexString(),
            nsec = encodeBech32("nsec", privKeyBytes),
            npub = encodeBech32("npub", pubKeyBytes)
        )
    }

    /**
     * Derive BIP-340 x-only public key from private key
     *
     * Delegates to NostrCrypto for pure crypto operations.
     *
     * @param privKeyBytes 32-byte private key
     * @return 32-byte x-only public key
     */
    fun deriveXOnlyPublicKey(privKeyBytes: ByteArray): ByteArray {
        return NostrCrypto.deriveXOnlyPublicKey(privKeyBytes)
    }

    /**
     * Store Nostr keys for a wallet
     *
     * Phase 0: Plaintext storage in SharedPreferences
     * TODO: Encrypt in production (Android Keystore or AES encryption)
     *
     * @param walletId Wallet ID (UUID string)
     * @param keys NostrKeys to store
     */
    private fun storeNostrKeys(walletId: String, keys: NostrKeys) {
        val json = JSONObject().apply {
            put("privateKeyHex", keys.privateKeyHex)
            put("publicKeyHex", keys.publicKeyHex)
            put("nsec", keys.nsec)
            put("npub", keys.npub)
        }

        // TODO: ENCRYPT IN PRODUCTION
        // Phase 0: Plaintext acceptable for testnet (Nostr key not a funds key)
        // Phase 1: Use Android Keystore or encrypt with user PIN/biometric
        prefs.edit()
            .putString(KEY_PREFIX + walletId, json.toString())
            .apply()
    }

    /**
     * Encode bytes as NIP-19 bech32
     *
     * NIP-19 specifies bech32 encoding for Nostr entities:
     * - nsec: private key (32 bytes)
     * - npub: public key (32 bytes)
     *
     * @param hrp Human-readable part ("nsec" or "npub")
     * @param data Data bytes to encode
     * @return Bech32 encoded string
     */
    private fun encodeBech32(hrp: String, data: ByteArray): String {
        // TODO: Implement proper bech32 encoding (NIP-19)
        // For Phase 0, return placeholder (hex is sufficient for internal use)
        // Real implementation needs bech32 library or manual encoding
        return "$hrp:${data.toHexString()}"
    }

    /**
     * Convert byte array to hex string
     */
    private fun ByteArray.toHexString(): String {
        return this.joinToString("") { "%02x".format(it) }
    }
}
