package com.asgaya.husk.nostr

import org.junit.Test
import org.junit.Assert.*
import java.math.BigInteger

/**
 * NostrCrypto Test - BIP-340 Test Vectors
 *
 * Verifies production NostrCrypto implementation against BIP-340 test vectors.
 *
 * Tests verify:
 * 1. Scalar validation (< curve order n)
 * 2. X-only pubkey derivation (BIP-340 compliance)
 * 3. Even-y normalization (including odd-y cases)
 *
 * Test vectors from BIP-340:
 * https://github.com/bitcoin/bips/blob/master/bip-0340/test-vectors.csv
 *
 * IMPORTANT: Tests now call PRODUCTION code (NostrCrypto), not test copies.
 * Per TightDS file 15: "The tests do not verify the production code" issue fixed.
 */
class NostrKeyManagerTest {

    companion object {
        /**
         * Convert hex string to byte array
         */
        fun String.hexToBytes(): ByteArray {
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
        fun ByteArray.toHexString(): String {
            return this.joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * BIP-340 Test Vector 0 (even-y)
     * Private key: 0000000000000000000000000000000000000000000000000000000000000003
     * Public key:  F9308A019258C31049344F85F89D5229B531C845836F99B08601F113BCE036F9
     * Y is even: ✅
     */
    @Test
    fun testBip340Vector0_EvenY() {
        val privKeyHex = "0000000000000000000000000000000000000000000000000000000000000003"
        val expectedPubKeyHex = "F9308A019258C31049344F85F89D5229B531C845836F99B08601F113BCE036F9"

        val privKeyBytes = privKeyHex.hexToBytes()
        val pubKeyBytes = NostrCrypto.deriveXOnlyPublicKey(privKeyBytes)  // ✅ Production code!
        val actualPubKeyHex = pubKeyBytes.toHexString()

        assertEquals(
            "BIP-340 vector 0 must match",
            expectedPubKeyHex.lowercase(),
            actualPubKeyHex.lowercase()
        )

        // Verify y is even
        assertTrue("d=3 must have even y", NostrCrypto.hasEvenY(privKeyBytes))
    }

    /**
     * BIP-340 Test Vector 1 (even-y)
     * Private key: B7E151628AED2A6ABF7158809CF4F3C762E7160F38B4DA56A784D9045190CFEF
     * Public key:  DFF1D77F2A671C5F36183726DB2341BE58FEAE1DA2DECED843240F7B502BA659
     * Y is even: ✅
     */
    @Test
    fun testBip340Vector1_EvenY() {
        val privKeyHex = "B7E151628AED2A6ABF7158809CF4F3C762E7160F38B4DA56A784D9045190CFEF"
        val expectedPubKeyHex = "DFF1D77F2A671C5F36183726DB2341BE58FEAE1DA2DECED843240F7B502BA659"

        val privKeyBytes = privKeyHex.hexToBytes()
        val pubKeyBytes = NostrCrypto.deriveXOnlyPublicKey(privKeyBytes)  // ✅ Production code!
        val actualPubKeyHex = pubKeyBytes.toHexString()

        assertEquals(
            "BIP-340 vector 1 must match",
            expectedPubKeyHex.lowercase(),
            actualPubKeyHex.lowercase()
        )

        // Verify y is even
        assertTrue("d=B7E15162... must have even y", NostrCrypto.hasEvenY(privKeyBytes))
    }

    /**
     * Even-Y Normalization Test (odd-y case)
     * Private key: 0000000000000000000000000000000000000000000000000000000000000006
     * Y is odd: ✅ (triggers normalization)
     * Expected normalized key: n - 6
     * Expected pubkey: FFF97BD5755EEEA420453A14355235D382F6472F8568A18B2F057A1460297556
     *
     * Per TightDS file 15: "add an odd-y vector and assert normalization"
     */
    @Test
    fun testEvenYNormalization_OddYCase() {
        val privKeyHex = "0000000000000000000000000000000000000000000000000000000000000006"
        val privKeyBytes = privKeyHex.hexToBytes()

        // Verify d=6 has odd y (before normalization)
        assertFalse("d=6 must have odd y", NostrCrypto.hasEvenY(privKeyBytes))

        // Normalize
        val privKeyBigInt = BigInteger(1, privKeyBytes)
        val normalizedBytes = NostrCrypto.normalizePrivateKey(privKeyBigInt)

        // Verify normalized key = n - 6
        val expectedNormalized = NostrCrypto.CURVE_ORDER.subtract(BigInteger.valueOf(6))
        val actualNormalized = BigInteger(1, normalizedBytes)
        assertEquals("Normalized key must be n - 6", expectedNormalized, actualNormalized)

        // Verify normalized key has even y
        assertTrue("Normalized key must have even y", NostrCrypto.hasEvenY(normalizedBytes))

        // Verify x-only pubkey (x is unchanged by negation)
        val expectedPubKeyHex = "FFF97BD5755EEEA420453A14355235D382F6472F8568A18B2F057A1460297556"
        val pubKeyBytes = NostrCrypto.deriveXOnlyPublicKey(normalizedBytes)
        val actualPubKeyHex = pubKeyBytes.toHexString()

        assertEquals(
            "X-only pubkey must match (x unchanged by negation)",
            expectedPubKeyHex.lowercase(),
            actualPubKeyHex.lowercase()
        )
    }

    /**
     * Test scalar validation using production code
     */
    @Test
    fun testScalarValidation() {
        val n = NostrCrypto.CURVE_ORDER

        // Valid scalars
        assertTrue("1 is valid", NostrCrypto.isValidScalar(BigInteger.ONE))
        assertTrue("n-1 is valid", NostrCrypto.isValidScalar(n.subtract(BigInteger.ONE)))

        // Invalid scalars
        assertFalse("0 is invalid", NostrCrypto.isValidScalar(BigInteger.ZERO))
        assertFalse("n is invalid", NostrCrypto.isValidScalar(n))
        assertFalse("n+1 is invalid", NostrCrypto.isValidScalar(n.add(BigInteger.ONE)))
    }

    /**
     * Test hex encoding/decoding
     */
    @Test
    fun testHexEncoding() {
        val bytes = byteArrayOf(0x01, 0x02, 0x03, 0x0A, 0x0F, 0xFF.toByte())
        val expected = "0102030a0fff"

        val hex = bytes.toHexString()
        assertEquals("Hex encoding must be lowercase", expected, hex)
    }

    /**
     * Test hex decoding
     */
    @Test
    fun testHexDecoding() {
        val hex = "0102030a0fff"
        val expected = byteArrayOf(0x01, 0x02, 0x03, 0x0A, 0x0F, 0xFF.toByte())

        val bytes = hex.hexToBytes()
        assertArrayEquals("Hex decoding must work", expected, bytes)
    }

    /**
     * Test x-only pubkey is 32 bytes
     */
    @Test
    fun testXOnlyPubkeyLength() {
        val privKeyHex = "0000000000000000000000000000000000000000000000000000000000000003"
        val privKeyBytes = privKeyHex.hexToBytes()

        val pubKeyBytes = NostrCrypto.deriveXOnlyPublicKey(privKeyBytes)  // ✅ Production code!

        assertEquals("X-only pubkey must be 32 bytes", 32, pubKeyBytes.size)
    }

    /**
     * Test that invalid private keys are rejected (FIXED - now tests range check)
     * Per TightDS file 15: "pass exactly 32 bytes equal to n"
     */
    @Test(expected = IllegalArgumentException::class)
    fun testRejectsPrivKeyAtCurveOrder() {
        // Create 32-byte array equal to n (not 33 bytes!)
        val nBytes = NostrCrypto.CURVE_ORDER.toByteArray()
        val privKeyBytes = if (nBytes.size > 32) {
            // Strip leading zero byte
            nBytes.copyOfRange(nBytes.size - 32, nBytes.size)
        } else {
            nBytes
        }

        // Now size check passes, range check should throw
        NostrCrypto.deriveXOnlyPublicKey(privKeyBytes)  // ✅ Production code!
    }

    /**
     * Test that zero private key is rejected
     */
    @Test(expected = IllegalArgumentException::class)
    fun testRejectsZeroPrivKey() {
        val privKeyBytes = ByteArray(32) { 0 }
        NostrCrypto.deriveXOnlyPublicKey(privKeyBytes)  // ✅ Production code!
    }
}
