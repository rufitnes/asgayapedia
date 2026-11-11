package com.asgaya.husk.nostr

import java.math.BigInteger

/**
 * Deterministic Nostr identities for the seeded TEST wallets.
 *
 * Phase 0 test-only. Production identities are random now (Phase 0) and will be
 * HD-derived later (Phase 1+). The purpose here is reproducibility: the Sender
 * device's "Recipient" contact and the Recipient device's own wallet must derive
 * the SAME Nostr pubkey, across devices, without copying values from logcat.
 *
 * See: collaborative_workspace/contact-exchange/ (QR exchange replaces this later).
 *
 * Mapping is keyed by the wallet's BCH public key (stable across installs because
 * the test wallets are seeded from fixed WIFs). The Nostr private keys below are
 * obviously-fake test scalars.
 */
object TestNostrKeys {

    private val BCH_TO_NOSTR_PRIV: Map<String, String> = mapOf(
        // Sender
        "032774f66536468a06a244568724bf1f8762682be1573ff9910667c0fea1226ece" to "11".repeat(32),
        // Recipient (Isabel)
        "03886b4f4100f9e348caed5155474f0a1a96db3e2706dc59e15260ed3d2b35e148" to "22".repeat(32),
        // Merchant (Bob)
        "03d6e1b33780e34dff1c6dc705c626deed385e827e762abfc596764be0ca3e4f45" to "33".repeat(32)
    )

    /** Fixed Nostr private key (hex) for a seeded test wallet, or null if not a test wallet. */
    fun nostrPrivKeyHexFor(bchPubKey: String): String? =
        BCH_TO_NOSTR_PRIV[bchPubKey.lowercase()]

    /** Deterministic x-only Nostr public key (hex) for a seeded test wallet, or null. */
    fun nostrPubKeyHexFor(bchPubKey: String): String? {
        val privHex = nostrPrivKeyHexFor(bchPubKey) ?: return null
        val privBytes = NostrCrypto.normalizePrivateKey(BigInteger(privHex, 16))
        return NostrCrypto.deriveXOnlyPublicKey(privBytes).joinToString("") { "%02x".format(it) }
    }
}
