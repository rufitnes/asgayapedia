package com.asgaya.husk

import android.content.Context

/**
 * Auto-Refund Protection setting (`ui-rethink/03 §3`, `09`).
 *
 * When a covenant is not completed (timeout, or an abort on a >7% drop), the wallet protects the
 * sender's value: on **abort** it can mint H€; on **timeout** it refunds. This setting decides how
 * much happens automatically.
 *
 *  - [Mode.AUTOMATIC]  — protect automatically, no prompt (recommended).
 *  - [Mode.MANUAL]     — warn, act on the user's tap.
 *  - [Mode.ALWAYS_BCH] — never stabilise; always come back as BCH.
 */
object AutoRefundPrefs {
    private const val PREFS = "BizumParserPrefs"
    private const val KEY = "auto_refund_mode"

    enum class Mode { AUTOMATIC, MANUAL, ALWAYS_BCH }

    fun load(context: Context): Mode {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, Mode.AUTOMATIC.name)
        return runCatching { Mode.valueOf(raw ?: Mode.AUTOMATIC.name) }.getOrDefault(Mode.AUTOMATIC)
    }

    fun save(context: Context, mode: Mode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, mode.name).apply()
    }

    /** Whether a timeout/abort should act without asking. */
    fun isAutomatic(context: Context): Boolean = load(context) == Mode.AUTOMATIC
}
