package com.asgaya.husk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.asgaya.husk.covenant.CovenantRecovery
import com.asgaya.husk.wallet.WalletManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fired by the AlarmManager alarm scheduled at a funded covenant's expiry
 * (see [CovenantRecovery.scheduleExpiryAlarm]). Runs the buffer-recovery sweep immediately
 * so a passive seller acts at the expiry rather than at the next periodic tick.
 */
class CovenantExpiryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val isRetry = intent.getBooleanExtra(EXTRA_RETRY, false)
        if (isRetry) {
            Log.d(TAG, "⏰ Covenant retry alarm fired")
        } else {
            Log.d(TAG, "⏰ Covenant expiry alarm fired (covenant=${intent.getStringExtra(EXTRA_COVENANT_ID)})")
        }

        val appContext = context.applicationContext

        // Create the singletons on the main thread (onReceive runs on it)
        val database = AppDatabase.getDatabase(appContext)
        val walletManager = WalletManager.getInstance(appContext)
        val covenantWebView = CovenantWebView.getInstance(appContext)
        val prefs = appContext.getSharedPreferences("BizumParserPrefs", Context.MODE_PRIVATE)

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val recovered = CovenantRecovery.recoverAllDue(
                    context = appContext,
                    database = database,
                    walletManager = walletManager,
                    covenantWebView = covenantWebView,
                    waitForReady = false
                )

                // A single attempt must never leave the buffer stuck: if anything is still open,
                // re-arm a retry with backoff (covers cold start, Doze, network). Reset otherwise.
                if (CovenantRecovery.hasUnresolvedFunded(database, walletManager)) {
                    val attempt = prefs.getInt(CovenantRecovery.RETRY_PREF, 0)
                    val scheduled = CovenantRecovery.scheduleRetryAlarm(appContext, attempt)
                    prefs.edit().putInt(CovenantRecovery.RETRY_PREF, attempt + 1).apply()
                    Log.d(TAG, "recovered=$recovered, still open -> retry #${attempt + 1} scheduled=$scheduled")
                } else {
                    prefs.edit().putInt(CovenantRecovery.RETRY_PREF, 0).apply()
                    Log.d(TAG, "recovered=$recovered, all concluded")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Alarm-triggered recovery failed", e)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val TAG = "CovenantExpiryReceiver"
        const val EXTRA_COVENANT_ID = "covenant_id"
        const val EXTRA_RETRY = "retry"
    }
}
