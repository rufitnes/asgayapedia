package com.asgaya.husk.nostr

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.math.ec.ECPoint
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * NostrEvent - Nostr event building and signing (NIP-01 + BIP-340)
 *
 * Implements:
 * - Event serialization per NIP-01
 * - Event ID calculation (SHA-256 of canonical JSON)
 * - BIP-340 Schnorr signatures for secp256k1
 * - Event verification
 *
 * References:
 * - NIP-01: Basic protocol flow description
 * - BIP-340: Schnorr Signatures for secp256k1
 * - NIP-17: Private Direct Messages (kind-14, 13, 1059)
 */
object NostrEvent {

    // secp256k1 curve parameters
    private val curveParams = ECNamedCurveTable.getParameterSpec("secp256k1")
    private val curve = curveParams.curve
    private val G = curveParams.g
    private val n = curveParams.n

    /**
     * Build a Nostr event
     *
     * NIP-01 event structure:
     * {
     *   "id": <32-byte event id>,
     *   "pubkey": <32-byte x-only pubkey>,
     *   "created_at": <unix timestamp>,
     *   "kind": <integer>,
     *   "tags": [[tag], ...],
     *   "content": <string>,
     *   "sig": <64-byte schnorr signature>
     * }
     *
     * @param pubkey X-only public key (32 bytes hex)
     * @param createdAt Unix timestamp
     * @param kind Event kind
     * @param tags Array of tag arrays
     * @param content Event content
     * @param privKey Optional private key for signing (32 bytes hex). If null, event is unsigned.
     * @return Event as JSONObject
     */
    fun buildEvent(
        pubkey: String,
        createdAt: Long,
        kind: Int,
        tags: List<List<String>>,
        content: String,
        privKey: String? = null
    ): JSONObject {
        val event = JSONObject()
        event.put("pubkey", pubkey)
        event.put("created_at", createdAt)
        event.put("kind", kind)

        // Build tags array
        val tagsArray = JSONArray()
        for (tag in tags) {
            val tagArray = JSONArray()
            for (element in tag) {
                tagArray.put(element)
            }
            tagsArray.put(tagArray)
        }
        event.put("tags", tagsArray)
        event.put("content", content)

        // Calculate event ID
        val id = calculateEventId(pubkey, createdAt, kind, tags, content)
        event.put("id", id)

        // Sign if private key provided
        if (privKey != null) {
            val signature = signEvent(id, privKey)
            event.put("sig", signature)
        }

        return event
    }

    /**
     * Calculate event ID per NIP-01
     *
     * ID is SHA-256 of the serialized event:
     * [0, pubkey, created_at, kind, tags, content]
     *
     * @return Event ID (32 bytes hex)
     */
    fun calculateEventId(
        pubkey: String,
        createdAt: Long,
        kind: Int,
        tags: List<List<String>>,
        content: String
    ): String {
        // Build canonical JSON array: [0, pubkey, created_at, kind, tags, content]
        val array = JSONArray()
        array.put(0)
        array.put(pubkey)
        array.put(createdAt)
        array.put(kind)

        // Tags array
        val tagsArray = JSONArray()
        for (tag in tags) {
            val tagArray = JSONArray()
            for (element in tag) {
                tagArray.put(element)
            }
            tagsArray.put(tagArray)
        }
        array.put(tagsArray)
        array.put(content)

        // Serialize to JSON (compact, no whitespace)
        var serialized = array.toString()

        // FIX: Android's org.json escapes forward slashes as \/, but JavaScript (used by Nostr relays)
        // does NOT escape them. This causes event ID mismatch. Unescape to match JS behavior.
        serialized = serialized.replace("\\/", "/")

        // DEBUG: Log the serialized data to diagnose relay rejection
        android.util.Log.d("NostrEvent", "Event ID serialization: $serialized")
        android.util.Log.d("NostrEvent", "Event ID serialization length: ${serialized.length}")
        android.util.Log.d("NostrEvent", "Contains whitespace: ${serialized.contains(" ")}")

        // SHA-256 hash
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(serialized.toByteArray(Charsets.UTF_8))

        val eventId = hash.toHexString()
        android.util.Log.d("NostrEvent", "Calculated event ID: $eventId")

        return eventId
    }

    /**
     * Sign event using BIP-340 Schnorr signature
     *
     * BIP-340 signing algorithm (with tagged hashes):
     * 1. Normalize d to even-y: if P=d*G has odd y, d' = n - d
     * 2. Generate k using auxiliary randomness (tagged hash)
     * 3. R = k*G, ensure R has even y
     * 4. e = hash_BIP0340/challenge(R_x || P_x || m)
     * 5. s = (k + e*d') mod n
     * 6. sig = R_x || s (64 bytes)
     *
     * @param messageHash 32-byte message hash (event ID)
     * @param privKeyHex 32-byte private key (hex)
     * @param auxRandHex Optional auxiliary randomness (32 bytes hex) - for testing only
     * @return 64-byte Schnorr signature (hex)
     */
    fun signEvent(messageHash: String, privKeyHex: String, auxRandHex: String? = null): String {
        val message = messageHash.hexToBytes()
        val privKey = privKeyHex.hexToBytes()

        require(message.size == 32) { "Message must be 32 bytes" }
        require(privKey.size == 32) { "Private key must be 32 bytes" }

        var d = BigInteger(1, privKey)
        require(NostrCrypto.isValidScalar(d)) { "Invalid private key" }

        // BIP-340: Normalize d to even-y (step 3)
        var P = G.multiply(d).normalize()
        val Py = P.affineYCoord.toBigInteger()
        if (Py.testBit(0)) {
            // P has odd y, negate d
            d = n.subtract(d)
            P = G.multiply(d).normalize()
        }
        val Px = P.affineXCoord.encoded

        // Generate k using auxiliary randomness (BIP-340 step 4-5)
        val aux = if (auxRandHex != null) {
            auxRandHex.hexToBytes()
        } else {
            ByteArray(32).also { SecureRandom().nextBytes(it) }
        }
        require(aux.size == 32) { "Auxiliary randomness must be 32 bytes" }

        // t = bytes(d) XOR hash_BIP0340/aux(aux_rand)  [BIP-340 step 4]
        val dBytes = d.toByteArray().let {
            if (it.size > 32) it.copyOfRange(it.size - 32, it.size)
            else if (it.size < 32) ByteArray(32 - it.size) + it
            else it
        }
        val auxHash = taggedHash("BIP0340/aux", aux)
        val t = ByteArray(32) { i -> (dBytes[i].toInt() xor auxHash[i].toInt()).toByte() }

        // k = hash_BIP0340/nonce(t || Px || m) mod n  [BIP-340 step 5]
        val kBytes = taggedHash("BIP0340/nonce", t + Px + message)
        var k = BigInteger(1, kBytes).mod(n)
        if (k == BigInteger.ZERO) k = BigInteger.ONE

        // R = k*G
        var R = G.multiply(k).normalize()
        val Ry = R.affineYCoord.toBigInteger()

        // If R has odd y, negate k
        if (Ry.testBit(0)) {
            k = n.subtract(k)
            R = G.multiply(k).normalize()
        }

        val Rx = R.affineXCoord.encoded

        // e = hash_BIP0340/challenge(Rx || Px || m) mod n
        val eBytes = taggedHash("BIP0340/challenge", Rx + Px + message)
        val e = BigInteger(1, eBytes).mod(n)

        // s = (k + e*d) mod n
        val s = k.add(e.multiply(d)).mod(n)

        // Signature: Rx || s (64 bytes)
        val sBytes = s.toByteArray().let {
            if (it.size > 32) it.copyOfRange(it.size - 32, it.size)
            else if (it.size < 32) ByteArray(32 - it.size) + it
            else it
        }

        return (Rx + sBytes).toHexString()
    }

    /**
     * Verify BIP-340 Schnorr signature
     *
     * @param messageHash 32-byte message hash (event ID)
     * @param signatureHex 64-byte signature (hex)
     * @param pubKeyHex 32-byte x-only public key (hex)
     * @return true if signature is valid
     */
    fun verifySignature(messageHash: String, signatureHex: String, pubKeyHex: String): Boolean {
        return try {
            val message = messageHash.hexToBytes()
            val signature = signatureHex.hexToBytes()
            val pubKeyBytes = pubKeyHex.hexToBytes()

            require(message.size == 32) { "Message must be 32 bytes" }
            require(signature.size == 64) { "Signature must be 64 bytes" }
            require(pubKeyBytes.size == 32) { "Public key must be 32 bytes" }

            // Extract r and s from signature
            val r = BigInteger(1, signature.copyOfRange(0, 32))
            val s = BigInteger(1, signature.copyOfRange(32, 64))

            // Validate r and s
            if (r >= NostrCrypto.CURVE_ORDER || s >= NostrCrypto.CURVE_ORDER) {
                return false
            }

            // Reconstruct public key point P (with even y)
            val Px = BigInteger(1, pubKeyBytes)
            val P = reconstructPoint(Px) ?: return false

            // e = hash_BIP0340/challenge(r || Px || m) mod n
            val rBytes = r.toByteArray().let {
                if (it.size > 32) it.copyOfRange(it.size - 32, it.size)
                else if (it.size < 32) ByteArray(32 - it.size) + it
                else it
            }
            val eBytes = taggedHash("BIP0340/challenge", rBytes + pubKeyBytes + message)
            val e = BigInteger(1, eBytes).mod(n)

            // R = s*G - e*P
            val sG = G.multiply(s)
            val eP = P.multiply(e)
            val R = sG.subtract(eP).normalize()

            // Check if R_x == r and R has even y
            val Rx = R.affineXCoord.toBigInteger()
            val Ry = R.affineYCoord.toBigInteger()

            Rx == r && !Ry.testBit(0)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Reconstruct EC point from x-coordinate (with even y)
     *
     * @param x X-coordinate
     * @return EC point with even y, or null if x is invalid
     */
    private fun reconstructPoint(x: BigInteger): ECPoint? {
        return try {
            val p = curve.field.characteristic
            // y^2 = x^3 + 7 (mod p)
            val ySq = x.modPow(BigInteger.valueOf(3), p).add(BigInteger.valueOf(7)).mod(p)
            // y = sqrt(y^2) mod p (p ≡ 3 mod 4)
            var y = ySq.modPow(p.add(BigInteger.ONE).divide(BigInteger.valueOf(4)), p)
            // Ensure even y
            if (y.testBit(0)) y = p.subtract(y)

            curve.createPoint(x, y)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * SHA-256 hash
     */
    private fun sha256(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data)
    }

    /**
     * BIP-340 tagged hash
     *
     * Per BIP-340 spec:
     * hash_tag(x) = SHA256(SHA256(tag) || SHA256(tag) || x)
     *
     * @param tag Tag string (e.g., "BIP0340/aux", "BIP0340/nonce", "BIP0340/challenge")
     * @param data Data to hash
     * @return Tagged hash
     */
    private fun taggedHash(tag: String, data: ByteArray): ByteArray {
        val tagHash = sha256(tag.toByteArray(Charsets.UTF_8))
        val taggedData = tagHash + tagHash + data
        return sha256(taggedData)
    }

    /**
     * Convert hex string to byte array
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
     * Convert byte array to hex string
     */
    private fun ByteArray.toHexString(): String {
        return this.joinToString("") { "%02x".format(it) }
    }
}
