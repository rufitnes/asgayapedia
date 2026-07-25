package com.asgaya.bizumparser

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.PowerManager
import android.util.Log

/**
 * Device Health Monitor
 *
 * Implements 5 health checks for proactive fraud prevention:
 * 1. Bank app installed
 * 2. Bank app enabled
 * 3. Battery optimization disabled (for bank app)
 * 4. Battery level sufficient
 * 5. Device charging state
 *
 * Based on 3C Toolbox API research (RS075) and device health documentation.
 */

data class DeviceHealth(
    val batteryLevel: Int,
    val isCharging: Boolean,
    val batteryStatus: BatteryStatus
) {
    val hasLowBattery: Boolean
        get() = batteryLevel < 20

    val hasCriticalBattery: Boolean
        get() = batteryLevel < 10

    fun getHealthSummary(): String {
        return when {
            hasCriticalBattery && !isCharging -> "CRITICAL: ${batteryLevel}% battery (not charging)"
            hasLowBattery && !isCharging -> "WARNING: ${batteryLevel}% battery (not charging)"
            hasLowBattery && isCharging -> "OK: ${batteryLevel}% battery (charging)"
            else -> "HEALTHY: ${batteryLevel}% battery${if (isCharging) " (charging)" else ""}"
        }
    }
}

enum class BatteryStatus {
    CHARGING,
    DISCHARGING,
    FULL,
    NOT_CHARGING,
    UNKNOWN
}

data class BankAppHealth(
    val packageName: String,
    val isInstalled: Boolean,
    val isEnabled: Boolean,
    val isBatteryOptimized: Boolean
) {
    val hasIssues: Boolean
        get() = !isInstalled || !isEnabled || isBatteryOptimized

    fun getIssuesList(): List<String> = buildList {
        if (!isInstalled) add("not installed")
        if (!isEnabled) add("disabled")
        if (isBatteryOptimized) add("battery optimized (will be killed)")
    }

    fun getHealthSummary(): String {
        return if (hasIssues) {
            "⚠️ Issues: ${getIssuesList().joinToString(", ")}"
        } else {
            "✅ Healthy"
        }
    }
}

data class CompleteHealth(
    val deviceHealth: DeviceHealth,
    val bankAppHealth: BankAppHealth
) {
    val isHealthy: Boolean
        get() = !deviceHealth.hasCriticalBattery && !bankAppHealth.hasIssues

    val hasWarnings: Boolean
        get() = deviceHealth.hasLowBattery || bankAppHealth.hasIssues

    fun getCompleteSummary(): String = buildList {
        add("Device: ${deviceHealth.getHealthSummary()}")
        add("Bank App: ${bankAppHealth.getHealthSummary()}")
    }.joinToString("\n")
}

object DeviceHealthMonitor {
    private const val TAG = "DeviceHealthMonitor"

    /**
     * Check device battery health
     * Based on 3C Toolbox battery monitoring
     */
    fun checkDeviceHealth(context: Context): DeviceHealth {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)

        // Battery level (0-100)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }

        // Charging state
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

        val batteryStatusEnum = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
            else -> BatteryStatus.UNKNOWN
        }

        Log.d(TAG, "Device health - Battery: $batteryPct%, Charging: $isCharging, Status: $batteryStatusEnum")

        return DeviceHealth(
            batteryLevel = batteryPct,
            isCharging = isCharging,
            batteryStatus = batteryStatusEnum
        )
    }

    /**
     * Check bank app health
     * Based on 3C Toolbox app monitoring APIs
     */
    fun checkBankAppHealth(context: Context, packageName: String): BankAppHealth {
        Log.d(TAG, "Checking bank app health for: $packageName")

        // 1. Check if app is installed
        val isInstalled = isAppInstalled(context, packageName)
        Log.d(TAG, "$packageName: isInstalled=$isInstalled")

        // 2. Check if app is enabled
        val isEnabled = if (isInstalled) {
            isAppEnabled(context, packageName)
        } else {
            false
        }
        Log.d(TAG, "$packageName: isEnabled=$isEnabled")

        // 3. Check battery optimization status
        val isBatteryOptimized = if (isInstalled) {
            isBatteryOptimized(context, packageName)
        } else {
            false
        }
        Log.d(TAG, "$packageName: isBatteryOptimized=$isBatteryOptimized")

        return BankAppHealth(
            packageName = packageName,
            isInstalled = isInstalled,
            isEnabled = isEnabled,
            isBatteryOptimized = isBatteryOptimized
        )
    }

    /**
     * Check complete health (device + bank app)
     */
    fun checkCompleteHealth(context: Context, bankPackageName: String): CompleteHealth {
        val deviceHealth = checkDeviceHealth(context)
        val bankAppHealth = checkBankAppHealth(context, bankPackageName)

        return CompleteHealth(
            deviceHealth = deviceHealth,
            bankAppHealth = bankAppHealth
        )
    }

    // ========== Helper Methods ==========

    /**
     * Check if app is installed
     * API from 3C Toolbox: c/a02.java lines 53-59
     */
    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Check if app is enabled (not disabled by user or system)
     * API from 3C Toolbox: c/a02.java lines 171-189
     *
     * States:
     * - COMPONENT_ENABLED_STATE_DEFAULT = 0 (enabled, uses manifest)
     * - COMPONENT_ENABLED_STATE_ENABLED = 1 (explicitly enabled)
     * - COMPONENT_ENABLED_STATE_DISABLED = 2 (disabled by user)
     * - COMPONENT_ENABLED_STATE_DISABLED_USER = 3 (disabled by system)
     */
    private fun isAppEnabled(context: Context, packageName: String): Boolean {
        return try {
            val state = context.packageManager.getApplicationEnabledSetting(packageName)
            state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED &&
            state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if app is enabled: ${e.message}")
            false
        }
    }

    /**
     * Check if app is battery optimized (will be killed in Doze mode)
     * API from 3C Toolbox: c/nb2.java lines 253-260
     *
     * Returns true if the app IS battery optimized (bad - will be killed)
     * Returns false if the app is ignoring battery optimizations (good - will stay alive)
     */
    private fun isBatteryOptimized(context: Context, packageName: String): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName)
        // Return true if optimized (i.e., NOT ignoring optimizations)
        return !isIgnoring
    }
}
