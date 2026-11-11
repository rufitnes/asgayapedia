package com.asgaya.husk.nostr

import org.junit.Test
import org.junit.Assert.*

/**
 * NostrEvent Test - Event building and BIP-340 signing
 *
 * Tests:
 * 1. Event ID calculation (NIP-01)
 * 2. BIP-340 Schnorr signature generation
 * 3. Signature verification
 * 4. Event structure
 */
class NostrEventTest {

    companion object {
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

        fun ByteArray.toHexString(): String {
            return this.joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * Test event ID calculation
     */
    @Test
    fun testEventIdCalculation() {
        val pubkey = "0000000000000000000000000000000000000000000000000000000000000001"
        val createdAt = 1234567890L
        val kind = 1
        val tags = emptyList<List<String>>()
        val content = "hello world"

        val eventId = NostrEvent.calculateEventId(pubkey, createdAt, kind, tags, content)

        // Event ID should be 32 bytes (64 hex chars)
        assertEquals("Event ID must be 64 hex chars", 64, eventId.length)

        // Should be deterministic
        val eventId2 = NostrEvent.calculateEventId(pubkey, createdAt, kind, tags, content)
        assertEquals("Event ID must be deterministic", eventId, eventId2)
    }

    /**
     * Test event building without signature
     */
    @Test
    fun testBuildUnsignedEvent() {
        val pubkey = NostrCrypto.deriveXOnlyPublicKey("0000000000000000000000000000000000000000000000000000000000000001".hexToBytes()).toHexString()
        val createdAt = System.currentTimeMillis() / 1000
        val kind = 1
        val tags = listOf(listOf("p", pubkey))
        val content = "test message"

        val event = NostrEvent.buildEvent(pubkey, createdAt, kind, tags, content, privKey = null)

        assertEquals("Pubkey must match", pubkey, event.getString("pubkey"))
        assertEquals("Created_at must match", createdAt, event.getLong("created_at"))
        assertEquals("Kind must match", kind, event.getInt("kind"))
        assertEquals("Content must match", content, event.getString("content"))
        assertTrue("Must have event ID", event.has("id"))
        assertFalse("Must not have signature", event.has("sig"))
    }

    /**
     * Test event building with signature
     */
    @Test
    fun testBuildSignedEvent() {
        val privKey = "0000000000000000000000000000000000000000000000000000000000000001"
        val pubkey = NostrCrypto.deriveXOnlyPublicKey(privKey.hexToBytes()).toHexString()
        val createdAt = System.currentTimeMillis() / 1000
        val kind = 1
        val tags = emptyList<List<String>>()
        val content = "signed message"

        val event = NostrEvent.buildEvent(pubkey, createdAt, kind, tags, content, privKey = privKey)

        assertTrue("Must have signature", event.has("sig"))
        val signature = event.getString("sig")
        assertEquals("Signature must be 128 hex chars (64 bytes)", 128, signature.length)
    }

    /**
     * Test signature verification roundtrip
     */
    @Test
    fun testSignatureVerificationRoundtrip() {
        val privKey = "0000000000000000000000000000000000000000000000000000000000000001"
        val pubkey = NostrCrypto.deriveXOnlyPublicKey(privKey.hexToBytes()).toHexString()
        val createdAt = System.currentTimeMillis() / 1000
        val kind = 1
        val tags = emptyList<List<String>>()
        val content = "verify me"

        // Build and sign event
        val event = NostrEvent.buildEvent(pubkey, createdAt, kind, tags, content, privKey = privKey)

        val eventId = event.getString("id")
        val signature = event.getString("sig")

        // Verify signature
        val isValid = NostrEvent.verifySignature(eventId, signature, pubkey)
        assertTrue("Signature must verify", isValid)
    }

    /**
     * Test signature verification with wrong pubkey fails
     */
    @Test
    fun testSignatureVerificationWrongPubkey() {
        val privKey1 = "0000000000000000000000000000000000000000000000000000000000000001"
        val privKey2 = "0000000000000000000000000000000000000000000000000000000000000002"
        val pubkey1 = NostrCrypto.deriveXOnlyPublicKey(privKey1.hexToBytes()).toHexString()
        val pubkey2 = NostrCrypto.deriveXOnlyPublicKey(privKey2.hexToBytes()).toHexString()

        val createdAt = System.currentTimeMillis() / 1000
        val kind = 1
        val tags = emptyList<List<String>>()
        val content = "test"

        // Sign with privKey1
        val event = NostrEvent.buildEvent(pubkey1, createdAt, kind, tags, content, privKey = privKey1)
        val eventId = event.getString("id")
        val signature = event.getString("sig")

        // Verify with wrong pubkey should fail
        val isValid = NostrEvent.verifySignature(eventId, signature, pubkey2)
        assertFalse("Signature must not verify with wrong pubkey", isValid)
    }

    /**
     * Test signature verification with corrupted signature fails
     */
    @Test
    fun testSignatureVerificationCorruptedSignature() {
        val privKey = "0000000000000000000000000000000000000000000000000000000000000001"
        val pubkey = NostrCrypto.deriveXOnlyPublicKey(privKey.hexToBytes()).toHexString()
        val createdAt = System.currentTimeMillis() / 1000
        val kind = 1
        val tags = emptyList<List<String>>()
        val content = "test"

        val event = NostrEvent.buildEvent(pubkey, createdAt, kind, tags, content, privKey = privKey)
        val eventId = event.getString("id")
        val signature = event.getString("sig")

        // Corrupt signature (flip last byte)
        val corruptedSig = signature.substring(0, signature.length - 2) + "00"

        val isValid = NostrEvent.verifySignature(eventId, corruptedSig, pubkey)
        assertFalse("Corrupted signature must not verify", isValid)
    }

    /**
     * Test event with tags
     */
    @Test
    fun testEventWithTags() {
        val privKey = "0000000000000000000000000000000000000000000000000000000000000001"
        val pubkey = NostrCrypto.deriveXOnlyPublicKey(privKey.hexToBytes()).toHexString()
        val createdAt = System.currentTimeMillis() / 1000
        val kind = 1
        val tags = listOf(
            listOf("p", "some_pubkey"),
            listOf("e", "some_event_id"),
            listOf("t", "nostr")
        )
        val content = "tagged message"

        val event = NostrEvent.buildEvent(pubkey, createdAt, kind, tags, content, privKey = privKey)

        val tagsArray = event.getJSONArray("tags")
        assertEquals("Must have 3 tags", 3, tagsArray.length())

        val firstTag = tagsArray.getJSONArray(0)
        assertEquals("First tag must be 'p'", "p", firstTag.getString(0))
        assertEquals("First tag value must match", "some_pubkey", firstTag.getString(1))
    }

    /**
     * BIP-340 Official Test Vector 0
     *
     * From https://github.com/bitcoin/bips/blob/master/bip-0340/test-vectors.csv
     * Tests that our implementation produces valid BIP-340 signatures
     */
    @Test
    fun testBIP340_OfficialVector0() {
        // Vector 0: secret key = 3
        val privKey = "0000000000000000000000000000000000000000000000000000000000000003"
        val pubkey = "f9308a019258c31049344f85f89d5229b531c845836f99b08601f113bce036f9"
        val message = "0000000000000000000000000000000000000000000000000000000000000000"
        val expectedSig = "e907831f80848d1069a5371b402410364bdf1c5f8307b0084c55f1ce2dca821525f66a4a85ea8b71e482a74f382d2ce5ebeee8fdb2172f477df4900d310536c0"

        // Verify the official BIP-340 test vector signature
        val officialVerifies = NostrEvent.verifySignature(message, expectedSig, pubkey)
        assertTrue("Official BIP-340 test vector must verify", officialVerifies)

        // Our signature should also verify (with random aux_rand, will differ from official)
        val signature = NostrEvent.signEvent(message, privKey)
        val isValid = NostrEvent.verifySignature(message, signature, pubkey)
        assertTrue("Our signature must verify", isValid)
    }

    /**
     * BIP-340 Official Test Vector 1
     */
    @Test
    fun testBIP340_OfficialVector1() {
        val privKey = "b7e151628aed2a6abf7158809cf4f3c762e7160f38b4da56a784d9045190cfef"
        val pubkey = "dff1d77f2a671c5f36183726db2341be58feae1da2deced843240f7b502ba659"
        val message = "243f6a8885a308d313198a2e03707344a4093822299f31d0082efa98ec4e6c89"
        val expectedSig = "6896bd60eeae296db48a229ff71dfe071bde413e6d43f917dc8dcf8c78de33418906d11ac976abccb20b091292bff4ea897efcb639ea871cfa95f6de339e4b0a"

        // Verify we can verify the official signature
        val officialVerifies = NostrEvent.verifySignature(message, expectedSig, pubkey)
        assertTrue("Official BIP-340 test vector 1 must verify", officialVerifies)

        // Our signature should also verify
        val signature = NostrEvent.signEvent(message, privKey)
        val isValid = NostrEvent.verifySignature(message, signature, pubkey)
        assertTrue("Our signature must verify", isValid)
    }

    /**
     * BIP-340 Official Test Vector 2
     */
    @Test
    fun testBIP340_OfficialVector2() {
        val privKey = "c90fdaa22168c234c4c6628b80dc1cd129024e088a67cc74020bbea63b14e5c9"
        val pubkey = "dd308afec5777e13121fa72b9cc1b7cc0139715309b086c960e18fd969774eb8"
        val message = "7e2d58d8b3bcdf1abadec7829054f90dda9805aab56c77333024b9d0a508b75c"
        val expectedSig = "5831aaeed7b44bb74e5eab94ba9d4294c49bcf2a60728d8b4c200f50dd313c1bab745879a5ad954a72c45a91c3a51d3c7adea98d82f8481e0e1e03674a6f3fb7"

        // Verify official signature
        val officialVerifies = NostrEvent.verifySignature(message, expectedSig, pubkey)
        assertTrue("Official BIP-340 test vector 2 must verify", officialVerifies)
    }

    /**
     * BIP-340 Signing Test: Reproduce Official Vector 0 with aux_rand
     *
     * This test proves our signing implementation is BIP-340 compliant
     * by reproducing the exact official signature when given the same inputs.
     */
    @Test
    fun testBIP340_SigningVector0() {
        val privKey = "0000000000000000000000000000000000000000000000000000000000000003"
        val pubkey = "f9308a019258c31049344f85f89d5229b531c845836f99b08601f113bce036f9"
        val message = "0000000000000000000000000000000000000000000000000000000000000000"
        val auxRand = "0000000000000000000000000000000000000000000000000000000000000000"
        val expectedSig = "e907831f80848d1069a5371b402410364bdf1c5f8307b0084c55f1ce2dca821525f66a4a85ea8b71e482a74f382d2ce5ebeee8fdb2172f477df4900d310536c0"

        // Sign with the official aux_rand
        val signature = NostrEvent.signEvent(message, privKey, auxRand)

        // Our signature must exactly match the official signature
        assertEquals("Signature must match official BIP-340 vector 0", expectedSig, signature)

        // Verify pubkey matches
        val derivedPubkey = NostrCrypto.deriveXOnlyPublicKey(privKey.hexToBytes()).toHexString()
        assertEquals("Pubkey must match", pubkey, derivedPubkey)
    }

    /**
     * BIP-340 Signing Test: Reproduce Official Vector 1 with aux_rand
     */
    @Test
    fun testBIP340_SigningVector1() {
        val privKey = "b7e151628aed2a6abf7158809cf4f3c762e7160f38b4da56a784d9045190cfef"
        val pubkey = "dff1d77f2a671c5f36183726db2341be58feae1da2deced843240f7b502ba659"
        val message = "243f6a8885a308d313198a2e03707344a4093822299f31d0082efa98ec4e6c89"
        val auxRand = "0000000000000000000000000000000000000000000000000000000000000001"
        val expectedSig = "6896bd60eeae296db48a229ff71dfe071bde413e6d43f917dc8dcf8c78de33418906d11ac976abccb20b091292bff4ea897efcb639ea871cfa95f6de339e4b0a"

        // Sign with the official aux_rand
        val signature = NostrEvent.signEvent(message, privKey, auxRand)

        // Our signature must exactly match the official signature
        assertEquals("Signature must match official BIP-340 vector 1", expectedSig, signature)

        // Verify pubkey matches
        val derivedPubkey = NostrCrypto.deriveXOnlyPublicKey(privKey.hexToBytes()).toHexString()
        assertEquals("Pubkey must match", pubkey, derivedPubkey)
    }

    /**
     * BIP-340 Signing Test: Reproduce Official Vector 2 with aux_rand
     */
    @Test
    fun testBIP340_SigningVector2() {
        val privKey = "c90fdaa22168c234c4c6628b80dc1cd129024e088a67cc74020bbea63b14e5c9"
        val pubkey = "dd308afec5777e13121fa72b9cc1b7cc0139715309b086c960e18fd969774eb8"
        val message = "7e2d58d8b3bcdf1abadec7829054f90dda9805aab56c77333024b9d0a508b75c"
        val auxRand = "c87aa53824b4d7ae2eb035a2b5bbbccc080e76cdc6d1692c4b0b62d798e6d906"
        val expectedSig = "5831aaeed7b44bb74e5eab94ba9d4294c49bcf2a60728d8b4c200f50dd313c1bab745879a5ad954a72c45a91c3a51d3c7adea98d82f8481e0e1e03674a6f3fb7"

        // Sign with the official aux_rand
        val signature = NostrEvent.signEvent(message, privKey, auxRand)

        // Our signature must exactly match the official signature
        assertEquals("Signature must match official BIP-340 vector 2", expectedSig, signature)

        // Verify pubkey matches
        val derivedPubkey = NostrCrypto.deriveXOnlyPublicKey(privKey.hexToBytes()).toHexString()
        assertEquals("Pubkey must match", pubkey, derivedPubkey)
    }
}
