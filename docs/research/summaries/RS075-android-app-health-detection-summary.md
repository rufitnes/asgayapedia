# RS075 Summary: Android App Health Detection

**Source:** 3C Toolbox decompilation  
**Date:** 2026-07-14

## Key APIs Discovered

### 1. Battery Optimization Check
```kotlin
val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
val isOptimized = !powerManager.isIgnoringBatteryOptimizations(packageName)
```
**Permission:** None required

### 2. App Installation Check
```kotlin
try {
    context.packageManager.getApplicationInfo(packageName, 0)
    // App is installed
} catch (e: PackageManager.NameNotFoundException) {
    // App not installed
}
```
**Permission:** None required

### 3. App Enabled/Disabled State
```kotlin
val state = context.packageManager.getApplicationEnabledSetting(packageName)
val isDisabled = state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                 state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
```
**Permission:** None required

### 4. Force-Stop Detection (via Usage Stats)
```kotlin
val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
val stats = usageStatsManager.queryUsageStats(
    UsageStatsManager.INTERVAL_DAILY,
    startTime,
    endTime
)
val lastUsed = stats.filter { it.packageName == packageName }
                    .maxByOrNull { it.lastTimeUsed }
                    ?.lastTimeUsed ?: 0L
```
**Permission:** PACKAGE_USAGE_STATS (user must grant in Settings)

## Complete Health Check

```kotlin
data class BankAppHealth(
    val isInstalled: Boolean,
    val isEnabled: Boolean,
    val isBatteryOptimized: Boolean,
    val isForceStopped: Boolean,
    val overallHealthy: Boolean
)

fun checkHealth(context: Context, packageName: String): BankAppHealth {
    val isInstalled = isAppInstalled(context, packageName)
    if (!isInstalled) return BankAppHealth(false, false, false, false, false)
    
    val isEnabled = !isAppDisabled(context, packageName)
    val isBatteryOptimized = isBatteryOptimized(context, packageName)
    val isForceStopped = isAppForceStopped(context, packageName)
    
    return BankAppHealth(
        isInstalled = true,
        isEnabled = isEnabled,
        isBatteryOptimized = isBatteryOptimized,
        isForceStopped = isForceStopped,
        overallHealthy = isEnabled && !isBatteryOptimized && !isForceStopped
    )
}
```

## Fraud Prevention Use Case

**Problem:** Malicious seller disables banking app after listing on Asgaya  
**Solution:** Broadcast health status via Nostr replaceable events  
**Buyer checks seller health BEFORE sending money**

### Nostr Heartbeat
```kotlin
{
  "kind": 30000,  // Replaceable event
  "content": {
    "seller_ready": true,
    "banks": [
      {
        "bank": "BBVA",
        "healthy": true,
        "issues": []
      }
    ],
    "last_update": 1752643200000
  }
}
```

## Key Findings

1. **All checks work without root** - 3C uses standard Android APIs
2. **Only Usage Stats needs permission** - others work immediately
3. **3C caches results** - doesn't poll continuously (CPU optimization)
4. **Multiple bank packages exist** - e.g., Sabadell has 2 different app packages

## Implementation Priority

1. ✅ Battery optimization (easy fix, high impact)
2. ✅ Installation check (instant detection)  
3. ✅ Enabled state (catches deliberate disabling)
4. ⚠️ Force-stop detection (needs permission)
5. ❌ Notification permission (can't check on Android 13+)

## CPU Optimization

**Current:** BizumParser uses 15% CPU  
**Target:** <2% CPU

**3C Approach:**
- Check health periodically (not continuously)
- Use WorkManager instead of constant Service
- Cache results for 6 hours

## Next Steps

1. Implement `BankHealthChecker.kt` in BizumParser
2. Add periodic health checks (WorkManager, every 6 hours)
3. Broadcast status to Nostr (replaceable events)
4. Add seller dashboard with health warnings
5. Optimize CPU usage

---

**Full document:** `/docs/research/RS075_android_app_health_detection.md`
