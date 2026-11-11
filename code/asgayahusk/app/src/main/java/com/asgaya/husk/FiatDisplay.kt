package com.asgaya.husk

/**
 * Render a covenant's **EUR face** in the device's **selected currency** (primary), keeping the
 * guaranteed **EUR face** visible (secondary) — `supported-fiats`; Suso 2026-10-09.
 *
 * Rationale: the covenant is EUR-denominated (`eurCents`) and the option that avoids complexity is
 * to show the selected currency big + the EUR face small. *(We "eurize" the corridors as the
 * pragmatic trade-off until BCH is stable or there is a non-central-bank alternative.)*
 *
 * @param eurPer the selected-currency units per 1 EUR (`FiatRateSource.eurPer(mainCurrency)`).
 */
object FiatDisplay {

    /** "27945 NGN" — the selected currency in its own terms; the EUR face is internal now.
     *  Prefixed with the rate-source badge (`SupportedFiats`): 🟠 divergent, 🟡 provisional. */
    fun amount(eurCents: Int, mainCurrency: String, eurPer: Double?): String {
        val cur = mainCurrency.ifBlank { "EUR" }
        val s = if (cur == "EUR" || eurPer == null || eurPer <= 0.0) "€%.2f".format(eurCents / 100.0)
                else "%s %,.0f".format(cur, (eurCents / 100.0) * eurPer)
        return SupportedFiats.warningLevel(cur)?.let { "$it $s" } ?: s
    }
}
