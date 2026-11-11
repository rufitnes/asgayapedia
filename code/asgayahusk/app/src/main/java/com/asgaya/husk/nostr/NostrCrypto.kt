package com.asgaya.husk.nostr

import org.bouncycastle.jce.ECNamedCurveTable
import java.math.BigInteger

/**
 * NostrCrypto - Pure cryptographic operations for Nostr (no Android dependencies)
 *
 * Extracted as standalone object so it can be tested directly without Context/SharedPreferences.
 * This follows the crypto-split pattern from file 06 and enables proper unit testing.
 *
 * All functions are pure (no side effects, no Android deps) and testable with BIP-340 vectors.
 */
object NostrCrypto {

    // secp256k1 curve parameters
    private val curveParams = ECNamedCurveTable.getParameterSpec("secp256k1")
    private val curve = curveParams.curve
    private val G = curveParams.g
    private val n = curveParams.n  // Curve order

    // secp256k1 curve order (for scalar validation)
    val CURVE_ORDER = BigInteger(
        "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16
    )

    /**
     * Derive BIP-340 x-only public key from private key
     *
     * BIP-340 specifies:
     * - Public key is 32 bytes (x-coordinate only)
     * - Use even-y point (if y is odd, the private key should be negated)
     *
     * Note: This function returns the x-coordinate. The even-y normalization
     * should happen at key generation time (see normalizePrivateKey).
     *
     * @param privKeyBytes 32-byte private key
     * @return 32-byte x-only public key
     * @throws IllegalArgumentException if privKeyBytes is invalid
     */
    fun deriveXOnlyPublicKey(privKeyBytes: ByteArray): ByteArray {
        require(privKeyBytes.size == 32) { "Private key must be 32 bytes" }

        val privKeyBigInt = BigInteger(1, privKeyBytes)
        require(privKeyBigInt < CURVE_ORDER && privKeyBigInt != BigInteger.ZERO) {
            "Private key must be in range [1, n-1]"
        }

        // Calculate public key point: P = d * G
        val pubPoint = G.multiply(privKeyBigInt).normalize()

        // Extract x-coordinate (32 bytes)
        return pubPoint.affineXCoord.encoded
    }

    /**
     * Normalize private key to even-y (BIP-340 requirement)
     *
     * If the public key point P = d*G has odd y-coordinate, returns (n - d).
     * Otherwise returns d unchanged.
     *
     * This normalization ensures that the stored private key always corresponds
     * to an even-y public key, simplifying BIP-340 signature generation.
     *
     * @param privKeyBigInt Private key as BigInteger
     * @return Normalized private key (as 32-byte array)
     */
    fun normalizePrivateKey(privKeyBigInt: BigInteger): ByteArray {
        require(privKeyBigInt > BigInteger.ZERO && privKeyBigInt < CURVE_ORDER) {
            "Private key must be in range [1, n-1]"
        }

        // Calculate public key point: P = d * G
        val pubPoint = G.multiply(privKeyBigInt).normalize()
        val y = pubPoint.affineYCoord.toBigInteger()

        // Determine normalized private key
        val normalizedKey = if (!y.testBit(0)) {
            // y is even - use private key as-is
            privKeyBigInt
        } else {
            // y is odd - negate private key
            n.subtract(privKeyBigInt)
        }

        // Convert to 32-byte array
        return normalizedKey.toByteArray().let {
            // Ensure 32 bytes (BigInteger may add leading 0x00)
            if (it.size > 32) it.copyOfRange(it.size - 32, it.size)
            else if (it.size < 32) ByteArray(32 - it.size) + it
            else it
        }
    }

    /**
     * Check if a public key point has even y-coordinate
     *
     * @param privKeyBytes 32-byte private key
     * @return true if P = d*G has even y-coordinate
     */
    fun hasEvenY(privKeyBytes: ByteArray): Boolean {
        require(privKeyBytes.size == 32) { "Private key must be 32 bytes" }

        val privKeyBigInt = BigInteger(1, privKeyBytes)
        require(privKeyBigInt < CURVE_ORDER && privKeyBigInt != BigInteger.ZERO) {
            "Private key must be in range [1, n-1]"
        }

        val pubPoint = G.multiply(privKeyBigInt).normalize()
        val y = pubPoint.affineYCoord.toBigInteger()

        return !y.testBit(0)  // true if even (bit 0 is NOT set)
    }

    /**
     * Validate scalar is in valid range [1, n-1]
     *
     * @param scalar BigInteger to validate
     * @return true if valid, false otherwise
     */
    fun isValidScalar(scalar: BigInteger): Boolean {
        return scalar > BigInteger.ZERO && scalar < CURVE_ORDER
    }
}
