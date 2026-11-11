package com.asgaya.husk

/** Trust of a display rate (`supported-fiats/01`). */
enum class FiatTrust { RESEARCHED, PROVISIONAL, DIVERGENT }

/** A currency in the display registry. */
data class FiatCurrency(
    val code: String,
    val name: String,
    val trust: FiatTrust
)

/**
 * Curated display-currency registry (`supported-fiats/01`, `RS087`).
 *
 * - **RESEARCHED** — source + official-vs-real divergence verified (safe to display).
 * - **PROVISIONAL** — `fawazahmed0` **reference** rate only; the real (street) rate may differ, so it
 *   must be surfaced as *indicative* (the full warn + report UX is `supported-fiats` step 4).
 *
 * The **covenant's settled face stays EUR** (`supported-fiats/01 §5`); this registry only drives the
 * **display** (recipient) currency. A currency moves to RESEARCHED only after an RS (`RS087` gate).
 */
object SupportedFiats {

    val ALL: List<FiatCurrency> = listOf(
        // Research verified (source known; divergence checked)
        FiatCurrency("EUR", "Euro", FiatTrust.RESEARCHED),
        FiatCurrency("USD", "US Dollar", FiatTrust.RESEARCHED),
        FiatCurrency("VES", "Venezuelan Bolívar", FiatTrust.RESEARCHED),   // DolarAPI `euros/paralelo` (RS047)
        FiatCurrency("ARS", "Argentine Peso", FiatTrust.RESEARCHED),      // DolarAPI `dolares/blue` (RS043)
        // Liquid — no parallel market, the reference IS the real rate (`RS090`)
        FiatCurrency("PHP", "Philippine Peso", FiatTrust.RESEARCHED),
        FiatCurrency("MXN", "Mexican Peso", FiatTrust.RESEARCHED),
        FiatCurrency("JPY", "Japanese Yen", FiatTrust.RESEARCHED),
        FiatCurrency("TWD", "New Taiwan Dollar", FiatTrust.RESEARCHED),
        FiatCurrency("THB", "Thai Baht", FiatTrust.RESEARCHED),
        FiatCurrency("GBP", "British Pound", FiatTrust.RESEARCHED),
        FiatCurrency("KES", "Kenyan Shilling", FiatTrust.RESEARCHED),
        FiatCurrency("COP", "Colombian Peso", FiatTrust.RESEARCHED),
        FiatCurrency("PEN", "Peruvian Sol", FiatTrust.RESEARCHED),
        FiatCurrency("BRL", "Brazilian Real", FiatTrust.RESEARCHED),
        FiatCurrency("ZAR", "South African Rand", FiatTrust.RESEARCHED),
        FiatCurrency("IDR", "Indonesian Rupiah", FiatTrust.RESEARCHED),
        FiatCurrency("AUD", "Australian Dollar", FiatTrust.RESEARCHED),
        FiatCurrency("CAD", "Canadian Dollar", FiatTrust.RESEARCHED),
        FiatCurrency("CHF", "Swiss Franc", FiatTrust.RESEARCHED),
        FiatCurrency("KRW", "South Korean Won", FiatTrust.RESEARCHED),
        FiatCurrency("SGD", "Singapore Dollar", FiatTrust.RESEARCHED),
        FiatCurrency("PLN", "Polish Złoty", FiatTrust.RESEARCHED),
        FiatCurrency("RON", "Romanian Leu", FiatTrust.RESEARCHED),
        FiatCurrency("ILS", "Israeli New Shekel", FiatTrust.RESEARCHED),
        FiatCurrency("AED", "UAE Dirham", FiatTrust.RESEARCHED),
        FiatCurrency("SAR", "Saudi Riyal", FiatTrust.RESEARCHED),
        FiatCurrency("DOP", "Dominican Peso", FiatTrust.RESEARCHED),
        FiatCurrency("GTQ", "Guatemalan Quetzal", FiatTrust.RESEARCHED),
        FiatCurrency("HNL", "Honduran Lempira", FiatTrust.RESEARCHED),
        FiatCurrency("UYU", "Uruguayan Peso", FiatTrust.RESEARCHED),
        FiatCurrency("NZD", "New Zealand Dollar", FiatTrust.RESEARCHED),
        FiatCurrency("HKD", "Hong Kong Dollar", FiatTrust.RESEARCHED),
        FiatCurrency("SEK", "Swedish Krona", FiatTrust.RESEARCHED),
        FiatCurrency("NOK", "Norwegian Krone", FiatTrust.RESEARCHED),
        FiatCurrency("DKK", "Danish Krone", FiatTrust.RESEARCHED),
        FiatCurrency("CZK", "Czech Koruna", FiatTrust.RESEARCHED),
        FiatCurrency("HUF", "Hungarian Forint", FiatTrust.RESEARCHED),
        FiatCurrency("BGN", "Bulgarian Lev", FiatTrust.RESEARCHED),
        FiatCurrency("ISK", "Icelandic Króna", FiatTrust.RESEARCHED),
        FiatCurrency("CLP", "Chilean Peso", FiatTrust.RESEARCHED),
        FiatCurrency("PYG", "Paraguayan Guaraní", FiatTrust.RESEARCHED),
        FiatCurrency("CRC", "Costa Rican Colón", FiatTrust.RESEARCHED),
        FiatCurrency("XOF", "West African CFA Franc", FiatTrust.RESEARCHED),   // pegged to EUR
        FiatCurrency("XAF", "Central African CFA Franc", FiatTrust.RESEARCHED), // pegged to EUR
        FiatCurrency("JMD", "Jamaican Dollar", FiatTrust.RESEARCHED),
        FiatCurrency("TTD", "Trinidad & Tobago Dollar", FiatTrust.RESEARCHED),
        FiatCurrency("XCD", "East Caribbean Dollar", FiatTrust.RESEARCHED),
        FiatCurrency("KWD", "Kuwaiti Dinar", FiatTrust.RESEARCHED),
        FiatCurrency("BHD", "Bahraini Dinar", FiatTrust.RESEARCHED),
        FiatCurrency("OMR", "Omani Rial", FiatTrust.RESEARCHED),
        FiatCurrency("JOD", "Jordanian Dinar", FiatTrust.RESEARCHED),
        // Divergent — managed / parallel market: we KNOW the reference understates the real rate
        // (`RS090`/`04`: NGN ~2-5%, GHS ~1-3%, INR ~1-2%) → the card shows an ORANGE warning.
        FiatCurrency("INR", "Indian Rupee", FiatTrust.DIVERGENT),
        FiatCurrency("NGN", "Nigerian Naira", FiatTrust.DIVERGENT),
        FiatCurrency("GHS", "Ghanaian Cedi", FiatTrust.DIVERGENT),
        // Provisional — reference only, divergence not verified; the card shows a YELLOW "indicative".
        FiatCurrency("VND", "Vietnamese Đồng", FiatTrust.PROVISIONAL),
        FiatCurrency("MYR", "Malaysian Ringgit", FiatTrust.PROVISIONAL),
        FiatCurrency("CNY", "Chinese Yuan", FiatTrust.PROVISIONAL),
        FiatCurrency("EGP", "Egyptian Pound", FiatTrust.PROVISIONAL),
        FiatCurrency("PKR", "Pakistani Rupee", FiatTrust.PROVISIONAL),
        FiatCurrency("BDT", "Bangladeshi Taka", FiatTrust.PROVISIONAL),
        FiatCurrency("LKR", "Sri Lankan Rupee", FiatTrust.PROVISIONAL),
        FiatCurrency("TRY", "Turkish Lira", FiatTrust.PROVISIONAL),
        FiatCurrency("RUB", "Russian Ruble", FiatTrust.PROVISIONAL),
        FiatCurrency("ETB", "Ethiopian Birr", FiatTrust.PROVISIONAL),
        FiatCurrency("DZD", "Algerian Dinar", FiatTrust.PROVISIONAL),
        FiatCurrency("IQD", "Iraqi Dinar", FiatTrust.PROVISIONAL),
        FiatCurrency("IRR", "Iranian Rial", FiatTrust.PROVISIONAL),
        FiatCurrency("NPR", "Nepalese Rupee", FiatTrust.PROVISIONAL),
        FiatCurrency("MNT", "Mongolian Tögrög", FiatTrust.PROVISIONAL),
        FiatCurrency("KHR", "Cambodian Riel", FiatTrust.PROVISIONAL),
        FiatCurrency("LAK", "Lao Kip", FiatTrust.PROVISIONAL),
        FiatCurrency("UGX", "Ugandan Shilling", FiatTrust.PROVISIONAL),
        FiatCurrency("TZS", "Tanzanian Shilling", FiatTrust.PROVISIONAL),
        FiatCurrency("RWF", "Rwandan Franc", FiatTrust.PROVISIONAL),
        FiatCurrency("ZMW", "Zambian Kwacha", FiatTrust.PROVISIONAL),
        FiatCurrency("BOB", "Bolivian Boliviano", FiatTrust.PROVISIONAL),
        FiatCurrency("UAH", "Ukrainian Hryvnia", FiatTrust.PROVISIONAL),
        FiatCurrency("LBP", "Lebanese Pound", FiatTrust.PROVISIONAL),
        FiatCurrency("HTG", "Haitian Gourde", FiatTrust.PROVISIONAL)
    )

    /** Codes in registry order (for the picker). */
    val codes: List<String> = ALL.map { it.code }

    fun byCode(code: String): FiatCurrency? = ALL.firstOrNull { it.code == code.uppercase() }

    /** Not fully verified: PROVISIONAL or DIVERGENT (the cash-out gate fires for both). */
    fun isProvisional(code: String): Boolean = byCode(code)?.trust != FiatTrust.RESEARCHED

    /** The warning badge for a currency's rate source (`ui-rethink/40`): 🟠 divergent, 🟡 provisional. */
    fun warningLevel(code: String): String? = when (byCode(code)?.trust) {
        FiatTrust.DIVERGENT -> "🟠"
        FiatTrust.PROVISIONAL -> "🟡"
        else -> null
    }

    /** Display label: `"PHP (Philippine Peso)"`; provisional adds `" · indicative"`. */
    fun label(code: String): String {
        val c = byCode(code) ?: return code
        val base = "${c.code} (${c.name})"
        return when (c.trust) {
            FiatTrust.PROVISIONAL -> "$base · indicative"
            FiatTrust.DIVERGENT -> "$base · parallel market"
            else -> base
        }
    }
}
