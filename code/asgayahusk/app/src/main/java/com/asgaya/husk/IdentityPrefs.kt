package com.asgaya.husk

import android.content.Context

/**
 * The user's **identity** defaults (`ui-rethink/03` group 1).
 *
 * Only the identity-level bits live here (Cash Account, location, accepted methods). The
 * **payment-instruction details** (name / phone / IBAN) do **not**: those are completed in the
 * **ad form** (the same place an ad is published/edited) so an ad can't go live with incomplete
 * instructions, and the private data is stored per ad — never in the public listing (`07`).
 */
object IdentityPrefs {
    private const val PREFS = "BizumParserPrefs"
    private const val K_CASH_ACCOUNT = "identity_cash_account"
    private const val K_LOCATION = "identity_location"
    private const val K_METHODS = "identity_methods"   // CSV: cash,bizum,sepa

    const val METHOD_CASH = "cash"
    const val METHOD_CARD = "card"
    const val METHOD_BIZUM = "bizum"
    const val METHOD_SEPA = "sepa"

    data class Identity(
        val cashAccount: String = "",
        val location: String = "",
        val methods: Set<String> = setOf(METHOD_CASH)
    )

    fun load(context: Context): Identity {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val methods = (p.getString(K_METHODS, METHOD_CASH) ?: METHOD_CASH)
            .split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        return Identity(
            cashAccount = p.getString(K_CASH_ACCOUNT, "") ?: "",
            location = p.getString(K_LOCATION, "") ?: "",
            methods = if (methods.isEmpty()) setOf(METHOD_CASH) else methods
        )
    }

    fun save(context: Context, identity: Identity) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(K_CASH_ACCOUNT, identity.cashAccount)
            .putString(K_LOCATION, identity.location)
            .putString(K_METHODS, identity.methods.joinToString(","))
            .apply()
    }
}
