package com.asgaya.husk.nostr

import org.json.JSONObject
import java.security.SecureRandom

/**
 * NIP17GiftWrap - NIP-17 Private Direct Message wrapping
 *
 * Implements three-layer gift-wrapping:
 * 1. kind-14 rumor (unsigned, has id but no sig) - contains actual message
 * 2. kind-13 seal (SIGNED by sender, encrypted rumor) - sender authentication
 * 3. kind-1059 gift-wrap (signed by ephemeral key, encrypted seal) - sender anonymity
 *
 * Security properties:
 * - Sender authentication (seal signature)
 * - Sender anonymity (ephemeral gift-wrap signature)
 * - Forward secrecy (ephemeral keys per message)
 * - Timestamp obfuscation (randomized timestamps)
 *
 * References:
 * - NIP-17: Private Direct Messages
 * - NIP-44: Encrypted Payloads
 * - NIP-01: Basic protocol flow
 *
 * Per TightDS review: MUST verify signatures at each layer during unwrap.
 */
object NIP17GiftWrap {

    /**
     * Wrap a direct message using NIP-17 gift-wrapping
     *
     * Creates three-layer wrapped event:
     * - kind-14 rumor: actual message (unsigned, has id)
     * - kind-13 seal: encrypted rumor (SIGNED by sender)
     * - kind-1059 gift-wrap: encrypted seal (signed by ephemeral key)
     *
     * @param content Message content
     * @param senderPrivKeyHex Sender's private key (32 bytes hex)
     * @param senderPubKeyHex Sender's x-only public key (32 bytes hex)
     * @param recipientPubKeyHex Recipient's x-only public key (32 bytes hex)
     * @return kind-1059 gift-wrap event
     */
    fun wrapDM(
        content: String,
        senderPrivKeyHex: String,
        senderPubKeyHex: String,
        recipientPubKeyHex: String
    ): JSONObject {
        val now = System.currentTimeMillis() / 1000

        // 1. Build kind-14 rumor (unsigned, real timestamp)
        val rumor = buildRumor(
            content = content,
            senderPubKey = senderPubKeyHex,
            recipientPubKey = recipientPubKeyHex,
            timestamp = now
        )

        // 2. Encrypt rumor with NIP-44
        val rumorJson = rumor.toString()
        val encryptedRumor = NIP44.encrypt(rumorJson, senderPrivKeyHex, recipientPubKeyHex)

        // 3. Build kind-13 seal (SIGNED by sender, randomized past timestamp)
        val sealTimestamp = now - SecureRandom().nextInt(2 * 24 * 60 * 60) // Random ≤2 days past
        val seal = NostrEvent.buildEvent(
            pubkey = senderPubKeyHex,
            createdAt = sealTimestamp,
            kind = 13,
            tags = emptyList(), // No tags for privacy
            content = encryptedRumor,
            privKey = senderPrivKeyHex // SIGNED!
        )

        // 4. Generate ephemeral keypair
        val (ephemeralPriv, ephemeralPub) = generateEphemeralKeypair()

        // 5. Encrypt seal with ephemeral key → recipient
        val sealJson = seal.toString()
        val encryptedSeal = NIP44.encrypt(sealJson, ephemeralPriv, recipientPubKeyHex)

        // 6. Build kind-1059 gift-wrap (signed by ephemeral key, randomized past timestamp)
        val wrapTimestamp = now - SecureRandom().nextInt(2 * 24 * 60 * 60) // Random ≤2 days past
        val giftWrap = NostrEvent.buildEvent(
            pubkey = ephemeralPub,
            createdAt = wrapTimestamp,
            kind = 1059,
            tags = listOf(listOf("p", recipientPubKeyHex)), // Recipient hint
            content = encryptedSeal,
            privKey = ephemeralPriv // Signed by ephemeral
        )

        return giftWrap
    }

    /**
     * Unwrap a NIP-17 gift-wrapped DM
     *
     * Performs three-layer unwrapping with signature verification:
     * 1. Verify kind-1059 signature (ephemeral key)
     * 2. Decrypt to kind-13 seal
     * 3. Verify kind-13 signature (REAL sender key)
     * 4. Decrypt to kind-14 rumor
     * 5. Verify rumor.pubkey == seal.pubkey (anti-impersonation)
     * 6. Extract content
     *
     * @param giftWrap kind-1059 gift-wrap event
     * @param recipientPrivKeyHex Recipient's private key (32 bytes hex)
     * @return Pair of (message content, verified sender pubkey)
     * @throws IllegalArgumentException if any verification fails
     */
    fun unwrapDM(giftWrap: JSONObject, recipientPrivKeyHex: String): Pair<String, String> {
        // 1. Verify it's a kind-1059 gift-wrap
        require(giftWrap.getInt("kind") == 1059) { "Not a kind-1059 gift-wrap" }

        // 2. Verify gift-wrap signature (ephemeral key)
        val wrapId = giftWrap.getString("id")
        val wrapSig = giftWrap.getString("sig")
        val ephemeralPub = giftWrap.getString("pubkey")
        require(NostrEvent.verifySignature(wrapId, wrapSig, ephemeralPub)) {
            "Gift-wrap signature verification failed"
        }

        // 3. Decrypt to kind-13 seal
        val encryptedSeal = giftWrap.getString("content")
        val sealJson = NIP44.decrypt(encryptedSeal, recipientPrivKeyHex, ephemeralPub)
        val seal = JSONObject(sealJson)

        // 4. Verify it's a kind-13 seal
        require(seal.getInt("kind") == 13) { "Decrypted event is not a kind-13 seal" }

        // 5. Verify seal signature (REAL sender key)
        val sealId = seal.getString("id")
        val sealSig = seal.getString("sig")
        val senderPubKey = seal.getString("pubkey")
        require(NostrEvent.verifySignature(sealId, sealSig, senderPubKey)) {
            "Seal signature verification failed"
        }

        // 6. Decrypt to kind-14 rumor
        val encryptedRumor = seal.getString("content")
        val rumorJson = NIP44.decrypt(encryptedRumor, recipientPrivKeyHex, senderPubKey)
        val rumor = JSONObject(rumorJson)

        // 7. Verify it's a kind-14 rumor
        require(rumor.getInt("kind") == 14) { "Decrypted event is not a kind-14 rumor" }

        // 8. Verify rumor.pubkey == seal.pubkey (anti-impersonation check)
        val rumorPubKey = rumor.getString("pubkey")
        require(rumorPubKey == senderPubKey) {
            "Rumor pubkey ($rumorPubKey) does not match seal pubkey ($senderPubKey) - impersonation attempt detected"
        }

        // 9. Extract content
        val content = rumor.getString("content")

        return Pair(content, senderPubKey)
    }

    /**
     * Build kind-14 rumor (unsigned, has id but no sig)
     *
     * @param content Message content
     * @param senderPubKey Sender's x-only public key
     * @param recipientPubKey Recipient's x-only public key
     * @param timestamp Real timestamp (not obfuscated)
     * @return kind-14 rumor event (no signature)
     */
    private fun buildRumor(
        content: String,
        senderPubKey: String,
        recipientPubKey: String,
        timestamp: Long
    ): JSONObject {
        // Build event without signature
        return NostrEvent.buildEvent(
            pubkey = senderPubKey,
            createdAt = timestamp,
            kind = 14,
            tags = listOf(listOf("p", recipientPubKey)), // Recipient tag
            content = content,
            privKey = null // No signature!
        )
    }

    /**
     * Generate ephemeral keypair for gift-wrap
     *
     * Generates a fresh random keypair for each message.
     * The private key is discarded after wrapping (forward secrecy).
     *
     * @return Pair of (privKeyHex, pubKeyHex)
     */
    private fun generateEphemeralKeypair(): Pair<String, String> {
        val random = SecureRandom()
        var privKeyBigInt = java.math.BigInteger.ZERO

        // Generate valid private key
        do {
            val randomBytes = ByteArray(32)
            random.nextBytes(randomBytes)
            privKeyBigInt = java.math.BigInteger(1, randomBytes)
        } while (!NostrCrypto.isValidScalar(privKeyBigInt))

        // Normalize to even-y (BIP-340)
        val privKeyBytes = NostrCrypto.normalizePrivateKey(privKeyBigInt)
        val pubKeyBytes = NostrCrypto.deriveXOnlyPublicKey(privKeyBytes)

        return Pair(privKeyBytes.toHexString(), pubKeyBytes.toHexString())
    }

    /**
     * Convert byte array to hex string
     */
    private fun ByteArray.toHexString(): String {
        return this.joinToString("") { "%02x".format(it) }
    }
}
