package com.asgaya.bizumparser

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class NotificationListener : NotificationListenerService() {

    private lateinit var database: AppDatabase
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val bankConfigs = mutableListOf<BankConfig>()

    companion object {
        private const val TAG = "NotificationListener"
        private val BIZUM_KEYWORDS = listOf("bizum", "recibido", "enviado")
    }

    data class BankConfig(
        val packageName: String,
        val bankName: String,
        val patterns: List<PatternConfig>
    )

    data class PatternConfig(
        val regex: Regex,
        val amountGroup: Int?,
        val senderGroup: Int?,
        val referenceGroup: Int?,
        val senderInTitle: Boolean = false,
        val referenceRegex: Regex? = null
    )

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🔥🔥🔥 SERVICE CREATED - NotificationListener.onCreate() 🔥🔥🔥")
        database = AppDatabase.getDatabase(this)
        loadBankPatterns()
        Log.d(TAG, "✅ Service initialized: loaded ${bankConfigs.size} bank configs")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "🟢 SERVICE CONNECTED - Ready to receive notifications!")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "🔴 SERVICE DISCONNECTED")
    }

    private fun loadBankPatterns() {
        try {
            val inputStream = assets.open("bank_patterns.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val bankObj = jsonArray.getJSONObject(i)
                val packageName = bankObj.getString("package")
                val bankName = bankObj.getString("bankName")
                val patternsArray = bankObj.getJSONArray("patterns")

                val patterns = mutableListOf<PatternConfig>()
                for (j in 0 until patternsArray.length()) {
                    val patternObj = patternsArray.getJSONObject(j)
                    val regex = Regex(patternObj.getString("regex"))
                    val amountGroup = patternObj.optInt("amountGroup", -1).takeIf { it >= 0 }
                    val senderGroup = patternObj.optInt("senderGroup", -1).takeIf { it >= 0 }
                    val referenceGroup = patternObj.optInt("referenceGroup", -1).takeIf { it >= 0 }
                    val senderInTitle = patternObj.optBoolean("senderInTitle", false)
                    val referenceRegex = patternObj.optString("referenceRegex", "").takeIf { it.isNotEmpty() }?.let { Regex(it) }

                    patterns.add(PatternConfig(regex, amountGroup, senderGroup, referenceGroup, senderInTitle, referenceRegex))
                }

                bankConfigs.add(BankConfig(packageName, bankName, patterns))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bank patterns", e)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        // Log EVERY notification to verify service is working
        Log.d(TAG, "📬 NOTIFICATION RECEIVED from: $packageName")

        // Find bank config for this package
        val bankConfig = bankConfigs.find { it.packageName == packageName }
        if (bankConfig == null) {
            Log.d(TAG, "⏭️  Skipping (not a monitored bank)")
            return  // Not a bank we're monitoring
        }

        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE, "")
        val text = extras.getString(Notification.EXTRA_TEXT, "")
        val timestamp = sbn.postTime

        Log.d(TAG, "===== BANK NOTIFICATION =====")
        Log.d(TAG, "Bank: ${bankConfig.bankName}")
        Log.d(TAG, "Package: $packageName")
        Log.d(TAG, "Title: $title")
        Log.d(TAG, "Text: $text")
        Log.d(TAG, "===========================")

        // Try to parse notification
        val parsed = parseNotification(bankConfig, title, text, timestamp)

        // Save to database (even if parsing failed, for debugging)
        serviceScope.launch {
            try {
                database.notificationDao().insert(parsed)
                if (parsed.parsedSuccessfully) {
                    Log.d(TAG, "✅ Parsed: €${parsed.amount} from ${parsed.sender} (${parsed.reference})")
                } else {
                    Log.d(TAG, "⚠️ Unparsed notification stored for debugging")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Database insert failed", e)
            }
        }
    }

    private fun parseNotification(
        bankConfig: BankConfig,
        title: String,
        text: String,
        timestamp: Long
    ): ParsedNotification {
        // Try each pattern for this bank
        for (pattern in bankConfig.patterns) {
            val match = pattern.regex.find(text)
            if (match != null) {
                // Extract fields using group indices from JSON
                val amount = pattern.amountGroup?.let {
                    match.groupValues.getOrNull(it)?.replace(",", ".")?.toDoubleOrNull()
                } ?: 0.0

                val sender = when {
                    pattern.senderInTitle -> title.trim()
                    pattern.senderGroup != null -> match.groupValues.getOrNull(pattern.senderGroup)?.trim() ?: ""
                    else -> ""
                }

                var reference = pattern.referenceGroup?.let {
                    match.groupValues.getOrNull(it)?.trim()
                } ?: ""

                // Try secondary reference regex if specified (BBVA case)
                if (reference.isEmpty() && pattern.referenceRegex != null) {
                    val refMatch = pattern.referenceRegex.find(text)
                    reference = refMatch?.groupValues?.getOrNull(1)?.trim() ?: ""
                }

                val rawText = if (pattern.senderInTitle) "$title | $text" else text

                return ParsedNotification(
                    timestamp = timestamp,
                    bankApp = bankConfig.bankName,
                    amount = amount,
                    sender = sender,
                    reference = reference,
                    rawText = rawText,
                    parsedSuccessfully = amount > 0.0 && sender.isNotEmpty()
                )
            }
        }

        // No pattern matched - check if it looks like a Bizum notification
        val lowerText = text.lowercase()
        val looksBizum = BIZUM_KEYWORDS.any { lowerText.contains(it) }

        // Store unparsed notification for debugging (TightDS recommendation)
        return ParsedNotification(
            timestamp = timestamp,
            bankApp = "${bankConfig.bankName} (unparsed)",
            amount = 0.0,
            sender = if (looksBizum) "PARSING FAILED - CHECK REGEX" else "Not Bizum",
            reference = "",
            rawText = "$title | $text",
            parsedSuccessfully = false
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // We don't care when notifications are dismissed
    }
}