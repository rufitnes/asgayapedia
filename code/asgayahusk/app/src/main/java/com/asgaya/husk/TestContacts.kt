package com.asgaya.husk

import com.asgaya.husk.nostr.TestNostrKeys

/**
 * Pre-populated test contacts from Pi-chan wallets
 * Real addresses and pubkeys from bitcoin-cli testnet wallets
 *
 * Data extracted 2026-07-31 from:
 * - seller wallet: 9.98 tBCH
 * - sender wallet: 2.03 tBCH
 * - recipient wallet: 0.014 tBCH
 * - merchant wallet: 0.007 tBCH
 *
 * Updated 2026-08-08:
 * - seller wallet: Real Pi-chan seller wallet (19.98 tBCH)
 *
 * Updated 2026-09-13:
 * - Added `nostrPubkey` (deterministic test identity, see TestNostrKeys)
 * - Dropped the phantom "Funder Wallet" contact (no matching wallet; file 46)
 *
 * TODO: Implement contact management UI
 *   - Add contact manually (name, address, pubkey)
 *   - Edit existing contacts
 *   - Delete contacts
 *   - Import from QR code  → see collaborative_workspace/contact-exchange/
 *   - Export contact list
 *   - Contact groups/categories
 */
object TestContacts {

    // BCH pubkeys of the seeded test wallets (must match WalletManager.seedTestWallets)
    private const val SENDER_PUBKEY = "032774f66536468a06a244568724bf1f8762682be1573ff9910667c0fea1226ece"
    private const val RECIPIENT_PUBKEY = "03886b4f4100f9e348caed5155474f0a1a96db3e2706dc59e15260ed3d2b35e148"
    private const val MERCHANT_PUBKEY = "03d6e1b33780e34dff1c6dc705c626deed385e827e762abfc596764be0ca3e4f45"

    fun getTestContacts(): List<Contact> = listOf(
        Contact(
            name = "Sender Wallet",
            cashAccount = "asgaya_sender#1167420",
            publicKey = SENDER_PUBKEY,
            bchAddress = "bchtest:qrw5nukh5jqend8922tf8zhxwyku6wfpxu9nl79hxf",
            nostrPubkey = TestNostrKeys.nostrPubKeyHexFor(SENDER_PUBKEY),
            notes = "Creates covenants, initiates payments (matches wallet)"
        ),
        Contact(
            name = "Recipient (Isabel)",
            cashAccount = "asgaya_recipient#1167420",
            publicKey = RECIPIENT_PUBKEY,
            bchAddress = "bchtest:qq2uxg4cu9axyzd9gjnhxwrvealt44mcwunp7gzd0k",
            nostrPubkey = TestNostrKeys.nostrPubKeyHexFor(RECIPIENT_PUBKEY),
            notes = "Receives covenant funds after claim"
        ),
        Contact(
            name = "Merchant (Bob)",
            cashAccount = "asgaya_merchant#1167420",
            publicKey = MERCHANT_PUBKEY,
            bchAddress = "bchtest:qz4llaasruxhme2l80dsnme76fmj24ve05h6d8ghe5",
            nostrPubkey = TestNostrKeys.nostrPubKeyHexFor(MERCHANT_PUBKEY),
            notes = "For merchant settlement testing (matches wallet)"
        )
    )

    /**
     * Seed the test contacts if the table is empty; otherwise **refresh** the seeded identities
     * (by pubkey) so an existing DB picks up updated values (e.g. real CashAccounts) without
     * touching user-added contacts.
     */
    suspend fun seedContactsIfNeeded(contactDao: ContactDao) {
        val test = getTestContacts()
        if (contactDao.getContactCount() == 0) {
            contactDao.insertAll(test)
            test.forEach { android.util.Log.i("TestContacts", "👤 ${it.name}: nostrPubkey=${it.nostrPubkey}") }
            return
        }
        for (c in test) {
            val existing = contactDao.getContactByPublicKey(c.publicKey) ?: continue
            if (existing.cashAccount != c.cashAccount || existing.nostrPubkey != c.nostrPubkey) {
                contactDao.update(existing.copy(cashAccount = c.cashAccount, nostrPubkey = c.nostrPubkey))
                android.util.Log.i("TestContacts", "♻️ Updated ${c.name}: cashAccount=${c.cashAccount}")
            }
        }
    }
}
