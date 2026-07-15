package com.asgaya.bizumparser

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.content.Context
import android.os.PowerManager
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.asgaya.bizumparser.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

data class BankHealth(
    val isInstalled: Boolean,
    val isEnabled: Boolean,
    val isBatteryOptimized: Boolean,
    val hasIssues: Boolean
)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NotificationAdapter
    private lateinit var database: AppDatabase
    private val NOTIFICATION_PERMISSION_CODE = 100

    companion object {
        const val PREFS_NAME = "BizumParserPrefs"
        const val DEBUG_MODE_KEY = "debug_mode"
        const val SELECTED_BANK_KEY = "selected_bank"
    }

    private val bankPackageMap = mapOf(
        "Caja Rural" to "com.rsi.nba",
        "BBVA" to "com.bbva.bbvacontigo",
        "Santander" to "es.bancosantander.apps",
        "Sabadell" to "es.bancsabadell.mobilebancohd",
        "Sabadell (Office Locator)" to "net.inverline.bancosabadell.officelocator.android",
        "ING" to "es.ingdirect.ing"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        // Setup RecyclerView
        adapter = NotificationAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Enable notification access button
        binding.enableButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        }

        // Debug mode toggle
        setupDebugMode()

        // Bank selection
        setupBankSelection()

        // Update status
        updateStatus()

        // Observe database changes
        observeNotifications()
    }

    private fun setupDebugMode() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDebugEnabled = prefs.getBoolean(DEBUG_MODE_KEY, false)

        binding.debugModeSwitch.isChecked = isDebugEnabled
        binding.debugWarning.visibility = if (isDebugEnabled) android.view.View.VISIBLE else android.view.View.GONE

        binding.debugModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showDebugModeWarning()
            } else {
                saveDebugMode(false)
                binding.debugWarning.visibility = android.view.View.GONE
            }
        }
    }

    private fun showDebugModeWarning() {
        AlertDialog.Builder(this)
            .setTitle("Enable Debug Mode?")
            .setMessage(
                "⚠️ This will log ALL notifications (including non-bank ones).\n\n" +
                "Steps:\n" +
                "1. Enable this mode\n" +
                "2. Send yourself a test Bizum\n" +
                "3. Check the list below for your bank notification\n" +
                "4. Copy the package name and notification details\n" +
                "5. Disable this mode\n\n" +
                "Use this to help us support new banks!"
            )
            .setPositiveButton("Enable") { _, _ ->
                saveDebugMode(true)
                binding.debugWarning.visibility = android.view.View.VISIBLE
            }
            .setNegativeButton("Cancel") { _, _ ->
                binding.debugModeSwitch.isChecked = false
            }
            .show()
    }

    private fun saveDebugMode(enabled: Boolean) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(DEBUG_MODE_KEY, enabled).apply()

        // Notify the NotificationListener service
        sendBroadcast(Intent("com.asgaya.bizumparser.DEBUG_MODE_CHANGED"))
    }

    private fun setupBankSelection() {
        val banks = bankPackageMap.keys.toList()
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            banks
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.bankSpinner.adapter = adapter

        // Load saved selection
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedBank = prefs.getString(SELECTED_BANK_KEY, banks[0])
        val savedIndex = banks.indexOf(savedBank).takeIf { it >= 0 } ?: 0
        binding.bankSpinner.setSelection(savedIndex)

        // Update health status on initial load
        checkAndDisplayBankHealth(savedBank ?: banks[0])

        // Save on change and check health
        binding.bankSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedBank = banks[position]
                prefs.edit().putString(SELECTED_BANK_KEY, selectedBank).apply()
                checkAndDisplayBankHealth(selectedBank)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun checkAndDisplayBankHealth(bankName: String) {
        val packageName = bankPackageMap[bankName] ?: return

        val health = checkBankHealth(packageName)

        if (health.hasIssues) {
            binding.bankHealthStatus.visibility = android.view.View.VISIBLE
            binding.bankHealthStatus.setTextColor(getColor(android.R.color.holo_orange_dark))

            val issues = buildList {
                if (!health.isInstalled) add("not installed")
                if (!health.isEnabled) add("disabled")
                if (health.isBatteryOptimized) add("battery optimized")
            }

            binding.bankHealthStatus.text = "⚠️ $bankName app ${issues.joinToString(", ")}"
        } else {
            binding.bankHealthStatus.visibility = android.view.View.VISIBLE
            binding.bankHealthStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            binding.bankHealthStatus.text = "✅ $bankName app is healthy"
        }
    }

    private fun checkBankHealth(packageName: String): BankHealth {
        android.util.Log.d("BizumParser", "Checking health for: $packageName")

        val isInstalled = try {
            packageManager.getApplicationInfo(packageName, 0)
            android.util.Log.d("BizumParser", "$packageName: isInstalled=true")
            true
        } catch (e: PackageManager.NameNotFoundException) {
            android.util.Log.d("BizumParser", "$packageName: isInstalled=false (${e.message})")
            false
        }

        val isEnabled = if (isInstalled) {
            val state = packageManager.getApplicationEnabledSetting(packageName)
            val enabled = state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED &&
                         state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
            android.util.Log.d("BizumParser", "$packageName: state=$state, isEnabled=$enabled")
            enabled
        } else {
            false
        }

        val isBatteryOptimized = if (isInstalled) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)
            android.util.Log.d("BizumParser", "$packageName: isIgnoring=$isIgnoring, isBatteryOptimized=${!isIgnoring}")
            !isIgnoring
        } else {
            false
        }

        val health = BankHealth(
            isInstalled = isInstalled,
            isEnabled = isEnabled,
            isBatteryOptimized = isBatteryOptimized,
            hasIssues = !isInstalled || !isEnabled || isBatteryOptimized
        )

        android.util.Log.d("BizumParser", "$packageName: health=$health")
        return health
    }

    override fun onResume() {
        super.onResume()
        updateStatus()

        // Check battery optimization
        if (!isBatteryOptimizationDisabled()) {
            requestBatteryOptimizationExemption()
        }

        // Check notification permission (Android 13+)
        checkAndRequestNotificationPermission()
    }

    private fun updateStatus() {
        val enabled = isNotificationListenerEnabled()
        binding.statusText.text = if (enabled) {
            "Status: ✅ Enabled"
        } else {
            "Status: ❌ Disabled (tap button above)"
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val packageName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(packageName)
    }

    private fun isBatteryOptimizationDisabled(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private fun requestBatteryOptimizationExemption() {
        AlertDialog.Builder(this)
            .setTitle("Battery Optimization")
            .setMessage(
                "BizumParser needs to stay active 24/7 to monitor Bizum notifications.\n\n" +
                        "Please disable battery optimization to ensure reliable operation."
            )
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .show()
    }


    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }
    private fun observeNotifications() {
        lifecycleScope.launch {
            database.notificationDao().getAllNotifications().collectLatest { notifications ->
                adapter.submitList(notifications)
                binding.countText.text = "Parsed: ${notifications.size} notifications"
            }
        }
    }
}