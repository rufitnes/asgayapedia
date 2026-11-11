package com.asgaya.husk.nostr

import android.content.Context
import android.util.Log

/**
 * Health of the Nostr transport (`ui-rethink/16` P0-3/P0-5). A failure to initialise or to
 * process an event must be **loud**, never a silent drop — recorded (and persisted) so the UI
 * can surface it.
 */
object NostrHealth {

    private const val TAG = "NostrHealth"
    private const val PREFS = "NostrHealthPrefs"
    private const val KEY_LAST_ERROR = "last_error"
    private const val KEY_LAST_ERROR_AT = "last_error_at"

    @Volatile var lastError: String? = null
        private set
    @Volatile var lastErrorAt: Long = 0L
        private set

    @Volatile private var appContext: Context? = null

    /** Call once (from the service or the UI) so failures can be persisted without a Context arg. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun report(error: String, e: Throwable? = null) {
        lastError = error
        lastErrorAt = System.currentTimeMillis()
        Log.e(TAG, "⚠️ Nostr health: $error", e)
        appContext?.let { ctx ->
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_LAST_ERROR, error)
                .putLong(KEY_LAST_ERROR_AT, lastErrorAt)
                .apply()
        }
    }

    fun report(context: Context, error: String, e: Throwable? = null) {
        init(context)
        report(error, e)
    }

    fun clear(context: Context) {
        lastError = null
        lastErrorAt = 0L
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    /** The most recent recorded error and when it happened, or `null` if the transport is healthy. */
    fun last(context: Context): Pair<String, Long>? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val error = prefs.getString(KEY_LAST_ERROR, null) ?: return null
        return error to prefs.getLong(KEY_LAST_ERROR_AT, 0L)
    }
}
