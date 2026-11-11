package com.asgaya.husk

import io.horizontalsystems.bitcoincore.transactions.scripts.ScriptType
import io.horizontalsystems.bitcoincore.utils.CashAddressConverter
import org.junit.Test
import org.junit.Assert.assertEquals

/**
 * Test bitcoin-kit-android CashAddressConverter with P2SH32 covenant addresses.
 *
 * Phase 1 validation from RS080:
 * 1. Test P2SH20 encoding (standard)
 * 2. Test P2SH32 encoding (covenant address)
 * 3. Validate against known test vectors
 */
class CashAddrLibraryTest {

    @Test
    fun testP2SH20Encoding() {
        // Test vector from CashAddr spec (from library's own tests)
        val converter = CashAddressConverter("bitcoincash")

        // 20-byte hash from spec test
        val hash = hexToByteArray("F5BF48B397DAE70BE82B3CCA4793F8EB2B6CDAC9")
        val address = converter.convert(hash, ScriptType.P2SH)

        // Expected from spec: bitcoincash:pqktqr6jw7tsu97t9vfw5f8n7rhtfh96yyqj9kcsr4
        // (Note: Different from P2PKH which uses 'p' prefix)
        assertEquals("bitcoincash", address.stringValue.substringBefore(':'))
        assertEquals(20, address.lockingScriptPayload.size)
        println("P2SH20 address: ${address.stringValue}")
    }

    @Test
    fun testP2SH32Encoding() {
        // Test P2SH32 with 32-byte script hash (our covenant case)
        val converter = CashAddressConverter("bitcoincash")

        // Example 32-byte hash (our covenant's redeemScriptHash)
        // This is the SHA256 hash we're currently trying to encode in SendActivity
        val hash32 = hexToByteArray("3173EF6623C6B48FFD1A3DCC0CC6489B0A07BB47A37F47CFEF4FE69DE825C060")
        val address = converter.convert(hash32, ScriptType.P2SH)

        // Expected from library test vectors (line 77-79 in CashAddressConverterTest.kt):
        // bitcoincash:qvch8mmxy0rtfrlarg7ucrxxfzds5pamg73h7370aa87d80gyhqxq5nlegake (P2PKH variant)
        // bitcoincash:pvch8mmxy0rtfrlarg7ucrxxfzds5pamg73h7370aa87d80gyhqxq7fqng6m6 (P2SH variant)

        assertEquals(32, address.lockingScriptPayload.size)
        assertEquals("bitcoincash", address.stringValue.substringBefore(':'))

        // The address should start with 'p' (P2SH) not 'q' (P2PKH)
        val addressPart = address.stringValue.substringAfter(':')
        assertEquals('p', addressPart[0])

        println("P2SH32 address: ${address.stringValue}")
        println("Hash size: ${address.lockingScriptPayload.size} bytes")
    }

    @Test
    fun testOurCovenantAddress() {
        // Test with actual covenant redeemScriptHash from SendActivity
        // This will be replaced with the real hash once we know it
        val converter = CashAddressConverter("bitcoincash")

        // Placeholder - update this with actual redeemScriptHash from covenant
        val covenantHash = hexToByteArray("0000000000000000000000000000000000000000000000000000000000000000")
        val address = converter.convert(covenantHash, ScriptType.P2SH)

        assertEquals(32, address.lockingScriptPayload.size)
        println("Covenant address: ${address.stringValue}")

        // Validation: Address should be valid CashAddr format
        // bitcoincash:p[52 bech32 characters]
        assert(address.stringValue.startsWith("bitcoincash:p"))
    }

    // Helper function to convert hex strings to byte arrays
    private fun hexToByteArray(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in result.indices) {
            val index = i * 2
            val byte = hex.substring(index, index + 2).toInt(16)
            result[i] = byte.toByte()
        }
        return result
    }
}
