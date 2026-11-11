package com.asgaya.husk.nostr

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.engines.ChaCha7539Engine
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV
import org.bouncycastle.jce.ECNamedCurveTable
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * NIP44 - Versioned Encrypted Payloads (Version 2)
 *
 * Pure cryptographic operations for NIP-44 encryption (no Android dependencies).
 * Follows the crypto-split pattern for testability.
 *
 * Algorithm:
 * 1. ECDH → conversation key (HKDF-extract with salt="nip44-v2")
 * 2. Per-message key derivation (HKDF-expand with nonce)
 * 3. Padding (power-of-two algorithm)
 * 4. ChaCha20 encryption
 * 5. HMAC-SHA256 MAC over nonce+ciphertext
 * 6. Base64 encoding: version(1) || nonce(32) || ciphertext || mac(32)
 *
 * References:
 * - NIP-44: https://github.com/nostr-protocol/nips/blob/master/44.md
 * - Test vectors: https://github.com/paulmillr/nip44/blob/main/nip44.vectors.json
 * - Cure53 audit: December 2023
 *
 * @see NostrCrypto for secp256k1 operations
 */
object NIP44 {

    // secp256k1 curve parameters (shared with NostrCrypto)
    private val curveParams = ECNamedCurveTable.getParameterSpec("secp256k1")
    private val curve = curveParams.curve
    private val G = curveParams.g

    // NIP-44 constants
    private const val VERSION: Byte = 0x02
    private const val NONCE_SIZE = 32
    private const val MAC_SIZE = 32
    private const val CHACHA_KEY_SIZE = 32
    private const val CHACHA_NONCE_SIZE = 12
    private const val HMAC_KEY_SIZE = 32
    private const val MIN_PLAINTEXT_SIZE = 1
    private const val MAX_PLAINTEXT_SIZE = 4_294_967_295L  // 2^32 - 1
    private const val MIN_PADDED_SIZE = 32
    private const val PADDING_THRESHOLD = 65_536  // Switch from 2-byte to 6-byte length prefix

    /**
     * Derive conversation key from ECDH shared secret
     *
     * Algorithm (NIP-44):
     * 1. Compute ECDH: shared_point = privKey_A * pubKey_B
     * 2. Extract x-coordinate (32 bytes, unhashed)
     * 3. Apply HKDF-extract with SHA256, IKM=shared_x, salt="nip44-v2"
     *
     * Result is symmetrical: conv(a, B) == conv(b, A)
     *
     * @param privKeyHex Sender/recipient private key (32 bytes hex)
     * @param pubKeyHex Recipient/sender public key (32 bytes hex, x-only)
     * @return 32-byte conversation key
     * @throws IllegalArgumentException if keys are invalid
     */
    fun getConversationKey(privKeyHex: String, pubKeyHex: String): ByteArray {
        // Parse keys
        val privKeyBytes = privKeyHex.hexToBytes()
        val pubKeyBytes = pubKeyHex.hexToBytes()

        require(privKeyBytes.size == 32) { "Private key must be 32 bytes" }
        require(pubKeyBytes.size == 32) { "Public key must be 32 bytes (x-only)" }

        val privKeyBigInt = BigInteger(1, privKeyBytes)
        require(NostrCrypto.isValidScalar(privKeyBigInt)) {
            "Private key must be in range [1, n-1]"
        }

        // Reconstruct public key point from x-only coordinate
        // NIP-44 uses compressed format: prepend 0x02 for even-y
        val compressedPubKey = ByteArray(33)
        compressedPubKey[0] = 0x02  // Even-y prefix
        System.arraycopy(pubKeyBytes, 0, compressedPubKey, 1, 32)
        val pubPoint = curve.decodePoint(compressedPubKey)

        // Perform ECDH: shared_point = privKey * pubKey
        val sharedPoint = pubPoint.multiply(privKeyBigInt).normalize()

        // Extract x-coordinate (32 bytes, unhashed)
        val sharedX = sharedPoint.affineXCoord.encoded

        // Apply HKDF-extract with salt="nip44-v2"
        val salt = "nip44-v2".toByteArray(Charsets.UTF_8)
        val conversationKey = hkdfExtract(salt, sharedX)

        return conversationKey
    }

    /**
     * Derive per-message keys from conversation key and nonce
     *
     * Algorithm (NIP-44):
     * 1. Apply HKDF-expand with SHA256, PRK=conversation_key, info=nonce, L=76
     * 2. Split output: chacha_key (32 bytes), chacha_nonce (12 bytes), hmac_key (32 bytes)
     *
     * @param conversationKey 32-byte conversation key from getConversationKey()
     * @param nonce 32-byte random nonce
     * @return Triple of (chacha_key, chacha_nonce, hmac_key)
     */
    fun getMessageKeys(conversationKey: ByteArray, nonce: ByteArray): Triple<ByteArray, ByteArray, ByteArray> {
        require(conversationKey.size == 32) { "Conversation key must be 32 bytes" }
        require(nonce.size == NONCE_SIZE) { "Nonce must be $NONCE_SIZE bytes" }

        // HKDF-expand: PRK=conversation_key, info=nonce, L=76
        val expanded = hkdfExpand(conversationKey, nonce, CHACHA_KEY_SIZE + CHACHA_NONCE_SIZE + HMAC_KEY_SIZE)

        // Split output
        val chachaKey = expanded.copyOfRange(0, CHACHA_KEY_SIZE)
        val chachaNonce = expanded.copyOfRange(CHACHA_KEY_SIZE, CHACHA_KEY_SIZE + CHACHA_NONCE_SIZE)
        val hmacKey = expanded.copyOfRange(CHACHA_KEY_SIZE + CHACHA_NONCE_SIZE, expanded.size)

        return Triple(chachaKey, chachaNonce, hmacKey)
    }

    /**
     * Encrypt plaintext using NIP-44
     *
     * Algorithm:
     * 1. Derive conversation key from sender privKey and recipient pubKey
     * 2. Generate random 32-byte nonce
     * 3. Derive message keys (chacha_key, chacha_nonce, hmac_key)
     * 4. Pad plaintext
     * 5. Encrypt with ChaCha20
     * 6. Compute HMAC-SHA256 MAC over nonce || ciphertext
     * 7. Encode as base64: version(1) || nonce(32) || ciphertext || mac(32)
     *
     * @param plaintext Plaintext string to encrypt
     * @param senderPrivKeyHex Sender's private key (32 bytes hex)
     * @param recipientPubKeyHex Recipient's x-only public key (32 bytes hex)
     * @return Base64-encoded ciphertext payload
     */
    fun encrypt(plaintext: String, senderPrivKeyHex: String, recipientPubKeyHex: String): String {
        // Get conversation key
        val conversationKey = getConversationKey(senderPrivKeyHex, recipientPubKeyHex)

        // Generate random nonce
        val nonce = ByteArray(NONCE_SIZE)
        SecureRandom().nextBytes(nonce)

        // Derive message keys
        val (chachaKey, chachaNonce, hmacKey) = getMessageKeys(conversationKey, nonce)

        // Pad plaintext
        val plaintextBytes = plaintext.toByteArray(Charsets.UTF_8)
        val padded = pad(plaintextBytes)

        // Encrypt with ChaCha20
        val ciphertext = chaCha20Encrypt(padded, chachaKey, chachaNonce)

        // Compute MAC over nonce || ciphertext
        val mac = computeHmac(hmacKey, nonce, ciphertext)

        // Build payload: version || nonce || ciphertext || mac
        val payload = ByteArray(1 + NONCE_SIZE + ciphertext.size + MAC_SIZE)
        var pos = 0
        payload[pos++] = VERSION
        System.arraycopy(nonce, 0, payload, pos, NONCE_SIZE)
        pos += NONCE_SIZE
        System.arraycopy(ciphertext, 0, payload, pos, ciphertext.size)
        pos += ciphertext.size
        System.arraycopy(mac, 0, payload, pos, MAC_SIZE)

        // Base64 encode
        return Base64.getEncoder().encodeToString(payload)
    }

    /**
     * Decrypt NIP-44 ciphertext
     *
     * Algorithm:
     * 1. Base64 decode payload
     * 2. Extract version, nonce, ciphertext, mac
     * 3. Derive conversation key
     * 4. Derive message keys
     * 5. Verify MAC (constant-time comparison)
     * 6. Decrypt with ChaCha20
     * 7. Unpad plaintext
     *
     * @param payload Base64-encoded ciphertext payload
     * @param recipientPrivKeyHex Recipient's private key (32 bytes hex)
     * @param senderPubKeyHex Sender's x-only public key (32 bytes hex)
     * @return Decrypted plaintext string
     * @throws IllegalArgumentException if MAC verification fails or payload is invalid
     */
    fun decrypt(payload: String, recipientPrivKeyHex: String, senderPubKeyHex: String): String {
        // Check for unsupported encoding prefix
        require(!payload.startsWith("#")) { "Unknown version or unsupported encoding" }

        // Base64 decode
        val decoded = Base64.getDecoder().decode(payload)
        require(decoded.size >= 1 + NONCE_SIZE + MIN_PADDED_SIZE + MAC_SIZE) {
            "Payload too short: ${decoded.size}"
        }

        // Extract components
        var pos = 0
        val version = decoded[pos++]
        require(version == VERSION) { "Unknown version: $version" }

        val nonce = ByteArray(NONCE_SIZE)
        System.arraycopy(decoded, pos, nonce, 0, NONCE_SIZE)
        pos += NONCE_SIZE

        val ciphertextLen = decoded.size - pos - MAC_SIZE
        val ciphertext = ByteArray(ciphertextLen)
        System.arraycopy(decoded, pos, ciphertext, 0, ciphertextLen)
        pos += ciphertextLen

        val receivedMac = ByteArray(MAC_SIZE)
        System.arraycopy(decoded, pos, receivedMac, 0, MAC_SIZE)

        // Derive conversation key
        val conversationKey = getConversationKey(recipientPrivKeyHex, senderPubKeyHex)

        // Derive message keys
        val (chachaKey, chachaNonce, hmacKey) = getMessageKeys(conversationKey, nonce)

        // Compute expected MAC
        val expectedMac = computeHmac(hmacKey, nonce, ciphertext)

        // Verify MAC (constant-time comparison)
        require(constantTimeEquals(receivedMac, expectedMac)) {
            "MAC verification failed"
        }

        // Decrypt with ChaCha20
        val padded = chaCha20Decrypt(ciphertext, chachaKey, chachaNonce)

        // Unpad
        val plaintextBytes = unpad(padded)

        // Convert to string
        return String(plaintextBytes, Charsets.UTF_8)
    }

    /**
     * Encrypt data using ChaCha20
     *
     * @param plaintext Plaintext bytes
     * @param key 32-byte ChaCha key
     * @param nonce 12-byte ChaCha nonce
     * @return Ciphertext bytes
     */
    private fun chaCha20Encrypt(plaintext: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray {
        val cipher = ChaCha7539Engine()
        val params = ParametersWithIV(KeyParameter(key), nonce)
        cipher.init(true, params)

        val ciphertext = ByteArray(plaintext.size)
        cipher.processBytes(plaintext, 0, plaintext.size, ciphertext, 0)

        return ciphertext
    }

    /**
     * Decrypt data using ChaCha20
     *
     * @param ciphertext Ciphertext bytes
     * @param key 32-byte ChaCha key
     * @param nonce 12-byte ChaCha nonce
     * @return Plaintext bytes
     */
    private fun chaCha20Decrypt(ciphertext: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray {
        val cipher = ChaCha7539Engine()
        val params = ParametersWithIV(KeyParameter(key), nonce)
        cipher.init(false, params)

        val plaintext = ByteArray(ciphertext.size)
        cipher.processBytes(ciphertext, 0, ciphertext.size, plaintext, 0)

        return plaintext
    }

    /**
     * Compute HMAC-SHA256 over nonce || ciphertext
     *
     * @param key HMAC key
     * @param nonce Nonce bytes
     * @param ciphertext Ciphertext bytes
     * @return 32-byte MAC
     */
    private fun computeHmac(key: ByteArray, nonce: ByteArray, ciphertext: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(key, "HmacSHA256")
        mac.init(keySpec)
        mac.update(nonce)
        mac.update(ciphertext)
        return mac.doFinal()
    }

    /**
     * Constant-time comparison of two byte arrays
     *
     * Uses MessageDigest.isEqual() which provides guaranteed constant-time comparison
     * on the JVM, preventing timing attacks on MAC verification.
     *
     * @param a First array
     * @param b Second array
     * @return true if arrays are equal
     */
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        return MessageDigest.isEqual(a, b)
    }

    /**
     * Get even y-coordinate for a given x-coordinate on secp256k1
     *
     * For NIP-44, we always use the even-y point (same as BIP-340).
     *
     * @param x X-coordinate
     * @return Even y-coordinate
     */
    private fun getEvenY(x: BigInteger): BigInteger {
        // Curve equation: y^2 = x^3 + 7 (mod p)
        val p = curve.field.characteristic
        val ySq = x.modPow(BigInteger.valueOf(3), p).add(BigInteger.valueOf(7)).mod(p)

        // Compute y = sqrt(y^2) mod p
        // For secp256k1 (p ≡ 3 mod 4), sqrt(a) = a^((p+1)/4) mod p
        val y = ySq.modPow(p.add(BigInteger.ONE).divide(BigInteger.valueOf(4)), p)

        // Return even y (if y is odd, return p - y)
        return if (y.testBit(0)) p.subtract(y) else y
    }

    /**
     * Calculate padded length for a given plaintext length
     *
     * NIP-44 padding algorithm (exact formula from spec):
     * ```
     * next_power = 1 << (floor(log2(unpadded - 1)) + 1)
     * chunk = if next_power <= 256 then 32 else next_power / 8
     * if unpadded <= 32: return 32
     * else: return chunk * (floor((unpadded - 1) / chunk) + 1)
     * ```
     *
     * @param unpadded Unpadded plaintext length (1 to 65536)
     * @return Padded length (not including length prefix)
     */
    fun calcPaddedLen(unpadded: Int): Int {
        require(unpadded >= MIN_PLAINTEXT_SIZE) { "Plaintext too short: $unpadded" }
        require(unpadded <= MAX_PLAINTEXT_SIZE) { "Plaintext too long: $unpadded" }

        if (unpadded <= 32) return 32

        // Calculate next power of 2 (BIP-340 spec: use unpadded - 1, not unpadded - 2)
        val nextPower = 1 shl (32 - Integer.numberOfLeadingZeros(unpadded - 1))

        // Determine chunk size
        val chunk = if (nextPower <= 256) 32 else nextPower / 8

        // Round up to next chunk boundary
        return chunk * ((unpadded - 1) / chunk + 1)
    }

    /**
     * Pad plaintext according to NIP-44 padding algorithm
     *
     * Format:
     * - Length < 65536: [u16 length big-endian][plaintext][zero padding]
     * - Length ≥ 65536: [0x00 0x00][u32 length big-endian][plaintext][zero padding]
     *
     * @param plaintext Plaintext bytes to pad
     * @return Padded bytes
     */
    fun pad(plaintext: ByteArray): ByteArray {
        val unpaddedLen = plaintext.size
        require(unpaddedLen >= MIN_PLAINTEXT_SIZE) { "Plaintext too short" }
        require(unpaddedLen <= MAX_PLAINTEXT_SIZE) { "Plaintext too long" }

        // Always use calcPaddedLen (no bypass for large messages)
        val paddedLen = calcPaddedLen(unpaddedLen)

        val prefixLen = if (unpaddedLen < PADDING_THRESHOLD) 2 else 6
        val totalLen = prefixLen + paddedLen

        val padded = ByteArray(totalLen)
        var pos = 0

        // Write length prefix
        if (unpaddedLen < PADDING_THRESHOLD) {
            // 2-byte u16 prefix (big-endian)
            padded[pos++] = (unpaddedLen shr 8).toByte()
            padded[pos++] = unpaddedLen.toByte()
        } else {
            // 6-byte prefix: [0x00 0x00][u32 big-endian]
            padded[pos++] = 0x00
            padded[pos++] = 0x00
            padded[pos++] = (unpaddedLen shr 24).toByte()
            padded[pos++] = (unpaddedLen shr 16).toByte()
            padded[pos++] = (unpaddedLen shr 8).toByte()
            padded[pos++] = unpaddedLen.toByte()
        }

        // Copy plaintext
        System.arraycopy(plaintext, 0, padded, pos, unpaddedLen)
        pos += unpaddedLen

        // Zero padding already present (ByteArray initializes to 0)

        return padded
    }

    /**
     * Unpad plaintext according to NIP-44 padding algorithm
     *
     * @param padded Padded bytes
     * @return Original plaintext
     * @throws IllegalArgumentException if padding is invalid
     */
    fun unpad(padded: ByteArray): ByteArray {
        require(padded.size >= MIN_PADDED_SIZE) { "Padded data too short" }

        var pos = 0

        // Read length prefix
        val unpaddedLen = if (padded[0].toInt() == 0 && padded[1].toInt() == 0) {
            // 6-byte prefix
            pos = 6
            ((padded[2].toInt() and 0xFF) shl 24) or
            ((padded[3].toInt() and 0xFF) shl 16) or
            ((padded[4].toInt() and 0xFF) shl 8) or
            (padded[5].toInt() and 0xFF)
        } else {
            // 2-byte prefix
            pos = 2
            ((padded[0].toInt() and 0xFF) shl 8) or
            (padded[1].toInt() and 0xFF)
        }

        require(unpaddedLen >= MIN_PLAINTEXT_SIZE && unpaddedLen <= MAX_PLAINTEXT_SIZE) {
            "Invalid unpadded length: $unpaddedLen"
        }

        require(pos + unpaddedLen <= padded.size) {
            "Unpadded length exceeds padded size"
        }

        // Extract plaintext
        val plaintext = ByteArray(unpaddedLen)
        System.arraycopy(padded, pos, plaintext, 0, unpaddedLen)

        // Verify padding (remaining bytes should be zero)
        for (i in (pos + unpaddedLen) until padded.size) {
            require(padded[i].toInt() == 0) { "Invalid padding at position $i" }
        }

        return plaintext
    }

    /**
     * HKDF-Extract (RFC 5869)
     *
     * Extract a pseudorandom key from input key material.
     *
     * @param salt Salt value (can be empty)
     * @param ikm Input key material
     * @return Pseudorandom key (PRK) - 32 bytes for SHA-256
     */
    private fun hkdfExtract(salt: ByteArray, ikm: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(salt, "HmacSHA256")
        mac.init(keySpec)
        return mac.doFinal(ikm)
    }

    /**
     * HKDF-Expand (RFC 5869)
     *
     * Expand a pseudorandom key to desired length.
     *
     * @param prk Pseudorandom key from HKDF-Extract
     * @param info Context-specific info
     * @param length Desired output length in bytes
     * @return Output key material
     */
    private fun hkdfExpand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(prk, "HmacSHA256")
        mac.init(keySpec)

        val hashLen = 32  // SHA-256 output size
        val n = (length + hashLen - 1) / hashLen  // Ceiling division
        require(n <= 255) { "HKDF-Expand output too long" }

        val output = ByteArray(length)
        var t = ByteArray(0)
        var pos = 0

        for (i in 1..n) {
            mac.reset()
            mac.update(t)
            mac.update(info)
            mac.update(i.toByte())
            t = mac.doFinal()

            val copyLen = minOf(hashLen, length - pos)
            System.arraycopy(t, 0, output, pos, copyLen)
            pos += copyLen
        }

        return output
    }

    /**
     * Extension function: Convert hex string to byte array
     */
    private fun String.hexToBytes(): ByteArray {
        val len = this.length
        require(len % 2 == 0) { "Hex string must have even length" }
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(this[i], 16) shl 4) +
                    Character.digit(this[i + 1], 16)).toByte()
        }
        return data
    }

    /**
     * Extension function: Convert byte array to hex string
     */
    private fun ByteArray.toHexString(): String {
        return this.joinToString("") { "%02x".format(it) }
    }
}
