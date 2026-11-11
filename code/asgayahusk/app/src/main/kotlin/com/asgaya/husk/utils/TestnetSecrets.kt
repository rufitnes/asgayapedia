package com.asgaya.husk.utils

import android.content.Context
import android.util.Log
import org.json.JSONObject

/**
 * Local-only testnet secrets (never committed).
 *
 * The real file lives at `app/src/main/assets/secrets/testnet-keys.json` and is
 * git-ignored; the committed example is `testnet-keys.example.json`.
 *
 * If the file is missing, test-wallet seeding and the local oracle signing are
 * disabled with a clear log instead of crashing.
 */
object TestnetSecrets {
    private const val TAG = "TestnetSecrets"
    private const val FILE = "secrets/testnet-keys.json"

    data class Keys(
        val senderWif: String,
        val recipientWif: String,
        val merchantWif: String,
        val oracleWif: String
    )

    fun load(context: Context): Keys? = try {
        val raw = context.assets.open(FILE).bufferedReader().use { it.readText() }
        val j = JSONObject(raw)
        Keys(
            senderWif = j.getString("sender_wif"),
            recipientWif = j.getString("recipient_wif"),
            merchantWif = j.getString("merchant_wif"),
            oracleWif = j.getString("oracle_wif")
        )
    } catch (e: Exception) {
        Log.w(TAG, "No local testnet secrets found ($FILE) — test wallets and the oracle are disabled", e)
        null
    }
}
