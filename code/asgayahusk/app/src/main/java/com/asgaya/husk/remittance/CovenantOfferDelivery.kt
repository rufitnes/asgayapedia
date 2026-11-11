package com.asgaya.husk.remittance

import android.util.Log
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.nostr.NIP17GiftWrap
import com.asgaya.husk.nostr.NostrClient
import com.asgaya.husk.nostr.NostrKeyManager
import com.asgaya.husk.wallet.WalletManager

/**
 * Delivers the `[COVENANT_V25]` **offer** to the recipient over Nostr (`ui-rethink/35`).
 *
 * The **single** place that builds + publishes it, so **both** delivery paths use it: the always-on
 * `NotificationListener` (funding detected out-of-app) and the in-app `SendViewModel` monitor (which
 * moves the row to `PENDING` first, so it must send the offer itself — otherwise it is lost). The
 * recipient dedups by covenant address, so a redundant delivery is harmless.
 *
 * Best-effort — the manual "Resend to recipient" stays the fallback.
 */
object CovenantOfferDelivery {

    private const val TAG = "CovenantOffer"

    /** Deliver the offer; returns the relay count reached (0 = not delivered). */
    suspend fun send(
        database: AppDatabase,
        walletManager: WalletManager,
        nostrKeyManager: NostrKeyManager,
        cashAccount: String?,
        remittance: Remittance
    ): Int {
        try {
            val recipientBchPubkey = remittance.recipientPubkey
            if (recipientBchPubkey.isNullOrBlank()) {
                Log.w(TAG, "🎁 No recipientPubkey — cannot notify recipient")
                return 0
            }
            val contact = database.contactDao().getContactByPublicKey(recipientBchPubkey)
            val recipientNostrPubkey = contact?.nostrPubkey
            if (recipientNostrPubkey.isNullOrBlank()) {
                Log.w(TAG, "🎁 No Nostr pubkey for recipient ${recipientBchPubkey.take(20)}… — cannot notify")
                return 0
            }
            val activeWallet = walletManager.getActiveWallet()
            if (activeWallet == null) {
                Log.w(TAG, "🎁 No active wallet — cannot notify recipient")
                return 0
            }
            val myKeys = nostrKeyManager.ensureNostrKeys(activeWallet)
            val message = CovenantOffer.message(remittance, cashAccount)
            val giftWrap = NIP17GiftWrap.wrapDM(
                content = message,
                senderPrivKeyHex = myKeys.privateKeyHex,
                senderPubKeyHex = myKeys.publicKeyHex,
                recipientPubKeyHex = recipientNostrPubkey
            )
            val client = NostrClient.getInstance(myKeys.publicKeyHex, null)
            client.connect()
            val sent = client.publishEvent(giftWrap)
            Log.i(TAG, "🎁 [COVENANT_V25] delivered to recipient (${contact?.name ?: recipientNostrPubkey.take(12)}…) via $sent relays")
            return sent
        } catch (e: Exception) {
            Log.e(TAG, "🎁 Failed to deliver covenant offer to recipient", e)
            return 0
        }
    }
}
