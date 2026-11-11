package com.asgaya.husk

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Display FX source for the recipient's local currency (`supported-fiats/01`, Phase-0 VES slice).
 *
 * - **VES** → DolarAPI `ve/v1/euros/paralelo` (street rate).
 * - **ARS** → DolarAPI `dolares/blue` (USD→ARS) × the reference EUR→USD.
 * - **else** → `fawazahmed0/exchange-api` **reference** rate (provisional; `RS087`).
 *
 * Returns `null` if unavailable — **never** a hardcoded fallback (`ui-rethink/16` P0-2). This is the
 * **display** leg only; the covenant's settled face stays EUR (`supported-fiats/01 §5`).
 */
object FiatRateSource {

    private const val TAG = "FiatRateSource"
    private const val CACHE_TTL_MS = 5 * 60_000L

    private val cache = ConcurrentHashMap<String, Pair<Double, Long>>()

    /** EUR per 1 unit of [code] (e.g. VES per EUR); `null` if unavailable. */
    suspend fun eurPer(code: String): Double? {
        val c = code.uppercase()
        if (c == "EUR") return 1.0
        val now = System.currentTimeMillis()
        cache[c]?.let { (rate, at) -> if (now - at < CACHE_TTL_MS) return rate }
        val rate = try {
            resolve(c)
        } catch (e: Exception) {
            Log.e(TAG, "❌ $c rate unavailable", e)
            null
        }
        if (rate != null) cache[c] = rate to now
        return rate
    }

    private suspend fun resolve(code: String): Double? = withContext(Dispatchers.IO) {
        when (code) {
            "VES" -> dolarValue(httpGetJson("https://ve.dolarapi.com/v1/euros/paralelo"))
            "ARS" -> {
                val usdArs = dolarValue(httpGetJson("https://dolarapi.com/v1/dolares/blue"))
                    ?: return@withContext null
                val eurUsd = referenceRate("usd") ?: return@withContext null
                usdArs * eurUsd
            }
            else -> referenceRate(code)
        }
    }

    /** DolarAPI value: `promedio` when present, else `venta`. */
    private fun dolarValue(j: JSONObject): Double? {
        val p = j.optDouble("promedio", Double.NaN)
        if (!p.isNaN() && p > 0) return p
        val v = j.optDouble("venta", Double.NaN)
        return if (!v.isNaN() && v > 0) v else null
    }

    /** `fawazahmed0` reference rate (EUR base). jsDelivr primary, Cloudflare fallback (`RS087`). */
    private fun referenceRate(code: String): Double? {
        val urls = listOf(
            "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/eur.min.json",
            "https://latest.currency-api.pages.dev/v1/currencies/eur.min.json"
        )
        for (u in urls) {
            try {
                val eur = httpGetJson(u).optJSONObject("eur") ?: continue
                val v = eur.optDouble(code.lowercase(), Double.NaN)
                if (!v.isNaN() && v > 0) return v
            } catch (e: Exception) {
                Log.w(TAG, "reference $u failed: ${e.message}")
            }
        }
        return null
    }

    private fun httpGetJson(url: String): JSONObject {
        val conn = URL(url).openConnection()
        conn.connectTimeout = 8_000
        conn.readTimeout = 8_000
        return JSONObject(conn.getInputStream().bufferedReader().use { it.readText() })
    }
}
