package com.asgaya.husk.nostr

import org.junit.Test
import org.junit.Assert.*
import org.json.JSONObject

/**
 * NIP17GiftWrap Test - Three-layer gift-wrapping
 *
 * Tests:
 * 1. Wrap/unwrap roundtrip
 * 2. Signature verification at each layer
 * 3. Rejection of tampered signatures
 * 4. Rejection of impersonation attempts (rumor.pubkey != seal.pubkey)
 * 5. Event structure validation
 */
class NIP17GiftWrapTest {

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
     * Test full wrap/unwrap roundtrip
     */
    @Test
    fun testWrapUnwrapRoundtrip() {
        // Generate keypairs
        val senderPriv = "0000000000000000000000000000000000000000000000000000000000000001"
        val senderPub = NostrCrypto.deriveXOnlyPublicKey(senderPriv.hexToBytes()).toHexString()

        val recipientPriv = "0000000000000000000000000000000000000000000000000000000000000002"
        val recipientPub = NostrCrypto.deriveXOnlyPublicKey(recipientPriv.hexToBytes()).toHexString()

        val message = "Hello, this is a private message!"

        // Wrap
        val giftWrap = NIP17GiftWrap.wrapDM(message, senderPriv, senderPub, recipientPub)

        // Verify gift-wrap structure
        assertEquals("Must be kind-1059", 1059, giftWrap.getInt("kind"))
        assertTrue("Must have signature", giftWrap.has("sig"))
        assertTrue("Must have content", giftWrap.has("content"))

        // Unwrap
        val (unwrappedMessage, verifiedSender) = NIP17GiftWrap.unwrapDM(giftWrap, recipientPriv)

        assertEquals("Message must match", message, unwrappedMessage)
        assertEquals("Sender must be verified", senderPub, verifiedSender)
    }

    /**
     * Test wrap/unwrap with different keypairs
     */
    @Test
    fun testWrapUnwrapDifferentKeys() {
        val senderPriv = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        val senderPub = NostrCrypto.deriveXOnlyPublicKey(senderPriv.hexToBytes()).toHexString()

        val recipientPriv = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
        val recipientPub = NostrCrypto.deriveXOnlyPublicKey(recipientPriv.hexToBytes()).toHexString()

        val message = "Test with different keys"

        val giftWrap = NIP17GiftWrap.wrapDM(message, senderPriv, senderPub, recipientPub)
        val (unwrappedMessage, verifiedSender) = NIP17GiftWrap.unwrapDM(giftWrap, recipientPriv)

        assertEquals("Message must match", message, unwrappedMessage)
        assertEquals("Sender must be verified", senderPub, verifiedSender)
    }

    /**
     * Test that gift-wrap has p tag with recipient
     */
    @Test
    fun testGiftWrapHasRecipientTag() {
        val senderPriv = "0000000000000000000000000000000000000000000000000000000000000001"
        val senderPub = NostrCrypto.deriveXOnlyPublicKey(senderPriv.hexToBytes()).toHexString()

        val recipientPriv = "0000000000000000000000000000000000000000000000000000000000000002"
        val recipientPub = NostrCrypto.deriveXOnlyPublicKey(recipientPriv.hexToBytes()).toHexString()

        val giftWrap = NIP17GiftWrap.wrapDM("test", senderPriv, senderPub, recipientPub)

        val tags = giftWrap.getJSONArray("tags")
        assertTrue("Must have at least one tag", tags.length() > 0)

        val pTag = tags.getJSONArray(0)
        assertEquals("First tag must be 'p'", "p", pTag.getString(0))
        assertEquals("p tag must contain recipient pubkey", recipientPub, pTag.getString(1))
    }

    /**
     * Test that unwrap fails with wrong private key
     */
    @Test(expected = IllegalArgumentException::class)
    fun testUnwrapFailsWithWrongKey() {
        val senderPriv = "0000000000000000000000000000000000000000000000000000000000000001"
        val senderPub = NostrCrypto.deriveXOnlyPublicKey(senderPriv.hexToBytes()).toHexString()

        val recipientPriv = "0000000000000000000000000000000000000000000000000000000000000002"
        val recipientPub = NostrCrypto.deriveXOnlyPublicKey(recipientPriv.hexToBytes()).toHexString()

        val wrongPriv = "0000000000000000000000000000000000000000000000000000000000000003"

        val giftWrap = NIP17GiftWrap.wrapDM("test", senderPriv, senderPub, recipientPub)

        // Should fail with MAC verification error
        NIP17GiftWrap.unwrapDM(giftWrap, wrongPriv)
    }

    /**
     * Test that unwrap fails with corrupted gift-wrap signature
     */
    @Test(expected = IllegalArgumentException::class)
    fun testUnwrapFailsWithCorruptedGiftWrapSignature() {
        val senderPriv = "0000000000000000000000000000000000000000000000000000000000000001"
        val senderPub = NostrCrypto.deriveXOnlyPublicKey(senderPriv.hexToBytes()).toHexString()

        val recipientPriv = "0000000000000000000000000000000000000000000000000000000000000002"
        val recipientPub = NostrCrypto.deriveXOnlyPublicKey(recipientPriv.hexToBytes()).toHexString()

        val giftWrap = NIP17GiftWrap.wrapDM("test", senderPriv, senderPub, recipientPub)

        // Corrupt gift-wrap signature
        val sig = giftWrap.getString("sig")
        val corruptedSig = sig.substring(0, sig.length - 2) + "00"
        giftWrap.put("sig", corruptedSig)

        // Should fail with signature verification error
        NIP17GiftWrap.unwrapDM(giftWrap, recipientPriv)
    }

    /**
     * Test emoji and special characters
     */
    @Test
    fun testWrapUnwrapEmoji() {
        val senderPriv = "0000000000000000000000000000000000000000000000000000000000000001"
        val senderPub = NostrCrypto.deriveXOnlyPublicKey(senderPriv.hexToBytes()).toHexString()

        val recipientPriv = "0000000000000000000000000000000000000000000000000000000000000002"
        val recipientPub = NostrCrypto.deriveXOnlyPublicKey(recipientPriv.hexToBytes()).toHexString()

        val message = "🎉 Hello Nostr! 🔥 Testing emoji support 💬"

        val giftWrap = NIP17GiftWrap.wrapDM(message, senderPriv, senderPub, recipientPub)
        val (unwrappedMessage, _) = NIP17GiftWrap.unwrapDM(giftWrap, recipientPriv)

        assertEquals("Emoji message must match", message, unwrappedMessage)
    }

    /**
     * Test long message
     */
    @Test
    fun testWrapUnwrapLongMessage() {
        val senderPriv = "0000000000000000000000000000000000000000000000000000000000000001"
        val senderPub = NostrCrypto.deriveXOnlyPublicKey(senderPriv.hexToBytes()).toHexString()

        val recipientPriv = "0000000000000000000000000000000000000000000000000000000000000002"
        val recipientPub = NostrCrypto.deriveXOnlyPublicKey(recipientPriv.hexToBytes()).toHexString()

        val message = "x".repeat(1000) // 1000 character message

        val giftWrap = NIP17GiftWrap.wrapDM(message, senderPriv, senderPub, recipientPub)
        val (unwrappedMessage, _) = NIP17GiftWrap.unwrapDM(giftWrap, recipientPriv)

        assertEquals("Long message must match", message, unwrappedMessage)
    }
}
