package com.asgaya.husk

import android.content.Context
import com.asgaya.husk.data.WalletRole

/**
 * One client, two surfaces (`ui-rethink/08`).
 *
 * **OFF** = the remittance-first active client (Send / Receive; **self-fund hidden**; the wallet is
 * infrastructure). **ON** = the full client (Ads / My ads, the **Trade** tab, publish, self-fund)
 * for passive/merchant use.
 *
 * Stored once (SharedPreferences); a Merchant/Seller active wallet only *suggests* the default.
 */
object AdvancedMode {
    const val PREFS_NAME = "BizumParserPrefs"
    const val KEY = "advanced_mode"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY, enabled).apply()
    }

    /** Whether the pref has been set yet (drives the role-aware first-run default). */
    fun isSet(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).contains(KEY)

    /** A Merchant/Seller wallet suggests the advanced surface; everyone else defaults to simple. */
    fun defaultFor(role: WalletRole?): Boolean =
        role == WalletRole.MERCHANT || role == WalletRole.SELLER
}
