package com.asgaya.husk.covenant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.CovenantRequest
import com.asgaya.husk.data.CovenantResolved
import com.asgaya.husk.nostr.NIP17GiftWrap
import com.asgaya.husk.nostr.NostrClient
import com.asgaya.husk.nostr.NostrKeyManager
import com.asgaya.husk.remittance.CovenantTimeout
import com.asgaya.husk.remittance.Remittance
import com.asgaya.husk.wallet.WalletManager
import kotlinx.coroutines.flow.first

/**
 * Tells the covenant's counterparties that it resolved (`[COVENANT_RESOLVED]`).
 *
 * This is a **hint, not the truth**: the receiver verifies on-chain before concluding
 * (`ui-rethink/10`, `58`). Best-effort — a failure here never blocks the resolution.
 */
object CovenantResolutionNotifier {

    private const val TAG = "CovenantResolution"
    private const val AUDIT_CHANNEL_ID = "asgaya_covenant_audit"
    private const val AUDIT_NOTIFICATION_ID = 4201
    private const val EXPIRY_CHANNEL_ID = "asgaya_covenant_expiry"
    private const val EXPIRY_PREFS = "BizumParserPrefs"
    private const val EXPIRY_NOTIFIED_KEY = "expiry_notified"

    /**
     * Notify the device's user about **every** covenant it is involved in that has expired
     * (`ui-rethink/39`), once each. Runs on the sender (sent), the recipient (received) and inside
     * the always-on service. Idempotent via a per-remittance dedup set.
     */
    suspend fun notifyExpiredOnce(context: Context, database: AppDatabase) {
        try {
            val prefs = context.getSharedPreferences(EXPIRY_PREFS, Context.MODE_PRIVATE)
            val notified = (prefs.getStringSet(EXPIRY_NOTIFIED_KEY, emptySet()) ?: emptySet()).toMutableSet()
            var added = false
            val now = System.currentTimeMillis() / 1000
            val all = database.remittanceDao().getAllRemittances().first()
            CovenantTimeout.expired(all, now).forEach { r ->
                val key = "r:${r.id}"
                if (key !in notified) {
                    notified.add(key)
                    added = true
                    postExpired(context, key.hashCode(), r)
                }
            }
            if (added) prefs.edit().putStringSet(EXPIRY_NOTIFIED_KEY, notified).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to surface expiry notifications", e)
        }
    }

    private fun postExpired(context: Context, notificationId: Int, r: Remittance) {
        val amount = "${"%.2f".format(r.amountPaymentCurrency)} ${r.paymentCurrency}"
        val (title, text) = if (r.isReceived) {
            "⏰ Covenant expired" to
                "The covenant for $amount expired and is no longer claimable."
        } else {
            "⏰ Your covenant expired" to
                "$amount: the covenant expired — the sender can refund (or recover the buffer)."
        }
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                nm.createNotificationChannel(
                    NotificationChannel(EXPIRY_CHANNEL_ID, "Covenant expiry", NotificationManager.IMPORTANCE_HIGH)
                )
            }
            val notification = NotificationCompat.Builder(context, EXPIRY_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            nm.notify(notificationId, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post the expiry notification", e)
        }
    }

    /**
     * Surface a failed on-chain audit (`ui-rethink/16` P0-1; Suso `ui-rethink/15 §6`): if the app
     * cannot audit a covenant's history it must fail **loudly** — never show a benign/assumed state.
     */
    fun notifyAuditFailure(context: Context, count: Int) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                nm.createNotificationChannel(
                    NotificationChannel(AUDIT_CHANNEL_ID, "Covenant status", NotificationManager.IMPORTANCE_HIGH)
                )
            }
            val notification = NotificationCompat.Builder(context, AUDIT_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("Couldn't verify a covenant")
                .setContentText("The on-chain audit failed for $count covenant(s). Check your connection.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            nm.notify(AUDIT_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to surface the audit failure", e)
        }
    }

    suspend fun notify(context: Context, covenant: CovenantRequest, path: String, txid: String) {
        notifyNpub(context, covenant.covenantAddress, covenant.senderNostrPubkey, path, txid)
    }

    /** Tell a counterparty (identified by npub) that a covenant resolved. Best-effort hint (`58`). */
    suspend fun notifyNpub(
        context: Context,
        covenantAddress: String,
        counterpartyNpub: String?,
        path: String,
        txid: String
    ) {
        val recipientNpub = counterpartyNpub?.takeIf { it.isNotBlank() }
        if (recipientNpub == null) {
            Log.d(TAG, "No counterparty npub for $covenantAddress - skipping notify")
            return
        }
        try {
            val walletManager = WalletManager.getInstance(context)
            val wallet = walletManager.getActiveWallet() ?: return
            val keys = NostrKeyManager(context).ensureNostrKeys(wallet)

            val message = CovenantResolved(
                covenantAddress = covenantAddress,
                path = path,
                txid = txid
            ).toMessage()

            val client = NostrClient.getInstance(keys.publicKeyHex, null)
            client.connect()
            val giftWrap = NIP17GiftWrap.wrapDM(message, keys.privateKeyHex, keys.publicKeyHex, recipientNpub)
            val sent = client.publishEvent(giftWrap)
            Log.i(TAG, "📣 [COVENANT_RESOLVED $path] sent to ${recipientNpub.take(12)}… via $sent relays")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to notify resolution", e)
        }
    }
}
