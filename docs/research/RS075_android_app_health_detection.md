# RS075: Android App Health Detection APIs
**Research Date:** 2026-07-14  
**Source:** 3C All-in-One Toolbox (ccc71.at.free) decompilation  
**Purpose:** Document Android APIs for detecting banking app health status  
**Application:** Proactive fraud prevention in Asgaya Phase 0

---

## Executive Summary

3C Toolbox uses standard Android APIs (no root required) to monitor app health. We can implement identical checks in BizumParser to detect when banking apps are disabled, force-stopped, or battery-optimized before a buyer sends money.

**Key Finding:** All health checks use public Android APIs available to any app with appropriate permissions.

---

## 1. Battery Optimization Detection

### API
**File:** `c/nb2.java` (lines 253-260)

```java
public static boolean E(Context context) {
    PowerManager powerManager = (PowerManager) context.getSystemService("power");
    boolean z = (powerManager == null || powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) ? false : true;
    StringBuilder sb = new StringBuilder("Doze white-listed: ");
    sb.append(!z);
    Log.d("3c.ui.utils", sb.toString());
    return z;
}
```

### For BizumParser Use

```kotlin
fun isBatteryOptimized(context: Context, packageName: String): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return !powerManager.isIgnoringBatteryOptimizations(packageName)
}
```

**Interpretation:**
- `true` = app IS battery optimized (BAD - Android may kill it)
- `false` = app is whitelisted (GOOD - will stay alive)

**Permission Required:** None (can check any package)

---

## 2. Usage Stats Permission Check

### API
**File:** `c/a02.java` (lines 221-224)

```java
public static boolean u(Context context) {
    AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService("appops");
    return (appOpsManager != null ? appOpsManager.checkOpNoThrow("android:get_usage_stats", Process.myUid(), context.getPackageName()) : 0) == 0;
}
```

### For BizumParser Use

```kotlin
fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        "android:get_usage_stats",
        android.os.Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}
```

**Why This Matters:** Usage Stats permission is required to check if apps are recently active (detect force-stopped state).

**Permission Required:** User must grant "Usage Access" in Settings

---

## 3. App Installation Check

### API
**File:** `c/a02.java` (lines 53-59)

```java
public static ApplicationInfo d(Context context, String str) {
    try {
        return context.getPackageManager().getApplicationInfo(str, 0);
    } catch (PackageManager.NameNotFoundException unused) {
        return null;
    }
}
```

### For BizumParser Use

```kotlin
fun isAppInstalled(context: Context, packageName: String): Boolean {
    return try {
        context.packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
```

**Permission Required:** None

---

## 4. App Enabled/Disabled State

### API
**File:** `c/a02.java` (lines 171-189)

```java
public static boolean p(Context context, String str, String str2) {
    boolean z = true;
    try {
        if (str2 == null) {
            int applicationEnabledSetting = context.getPackageManager().getApplicationEnabledSetting(str);
            return applicationEnabledSetting == 2 || applicationEnabledSetting == 3;
        }
        // ... component checking logic
    } catch (Exception unused) {
        Log.w("3c.apps", "Failed to get component state: " + str + "/" + str2);
        return false;
    }
}
```

### For BizumParser Use

```kotlin
fun isAppDisabled(context: Context, packageName: String): Boolean {
    return try {
        val state = context.packageManager.getApplicationEnabledSetting(packageName)
        state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
        state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
    } catch (e: Exception) {
        false
    }
}
```

**States:**
- `COMPONENT_ENABLED_STATE_ENABLED = 1` (app is enabled)
- `COMPONENT_ENABLED_STATE_DISABLED = 2` (app disabled by user)
- `COMPONENT_ENABLED_STATE_DISABLED_USER = 3` (app disabled by system)
- `COMPONENT_ENABLED_STATE_DEFAULT = 0` (use manifest setting)

**Permission Required:** None

---

## 5. Recent App Activity (Force-Stop Detection)

### API
**File:** `c/a02.java` (lines 143-169)

```java
public static String[] o(Context context, int i) {
    if (!u(context)) {  // Check usage stats permission first
        return null;
    }
    ArrayList arrayList = new ArrayList();
    UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService("usagestats");
    long h = bp0.h();  // Current time
    UsageEvents.Event event = new UsageEvents.Event();
    UsageEvents queryEvents = usageStatsManager.queryEvents(h - 60000, h);  // Last 60 seconds
    while (queryEvents.hasNextEvent()) {
        queryEvents.getNextEvent(event);
        int eventType = event.getEventType();
        if (eventType == 1) {  // Activity resumed
            arrayList.add(0, event.getPackageName());
        } else if (eventType == 2) {  // Activity paused
            arrayList.remove(event.getPackageName());
        } else if (eventType == 23) {  // Activity stopped
            arrayList.remove(event.getPackageName());
        }
    }
    // ... truncate to top i apps
    return (String[]) arrayList.toArray(new String[0]);
}
```

### For BizumParser Use

```kotlin
fun getAppLastUsedTime(context: Context, packageName: String): Long {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    
    val now = System.currentTimeMillis()
    val startTime = now - (7 * 24 * 60 * 60 * 1000)  // Last 7 days
    
    val usageStats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        startTime,
        now
    )
    
    return usageStats
        .filter { it.packageName == packageName }
        .maxByOrNull { it.lastTimeUsed }
        ?.lastTimeUsed ?: 0L
}

fun isAppForceStopped(context: Context, packageName: String): Boolean {
    val lastUsed = getAppLastUsedTime(context, packageName)
    val daysSinceLastUse = (System.currentTimeMillis() - lastUsed) / (24 * 60 * 60 * 1000)
    
    // If app hasn't been used in 7+ days and is installed, likely force-stopped
    return lastUsed > 0 && daysSinceLastUse > 7
}
```

**Permission Required:** Usage Stats permission (same as #2)

**Event Types:**
- `1` = Activity resumed (app came to foreground)
- `2` = Activity paused (app left foreground)
- `23` = Activity stopped

---

## 6. Banking App Health Check (Complete Implementation)

### Proposed BizumParser Function

```kotlin
data class BankAppHealth(
    val packageName: String,
    val bankName: String,
    val isInstalled: Boolean,
    val isEnabled: Boolean,
    val isBatteryOptimized: Boolean,
    val lastUsedDaysAgo: Int,
    val isForceStopped: Boolean,
    val hasUsagePermission: Boolean,
    val overallHealthy: Boolean
)

fun checkBankAppHealth(context: Context, packageName: String, bankName: String): BankAppHealth {
    val hasUsagePermission = hasUsageStatsPermission(context)
    val isInstalled = isAppInstalled(context, packageName)
    
    if (!isInstalled) {
        return BankAppHealth(
            packageName = packageName,
            bankName = bankName,
            isInstalled = false,
            isEnabled = false,
            isBatteryOptimized = false,
            lastUsedDaysAgo = -1,
            isForceStopped = false,
            hasUsagePermission = hasUsagePermission,
            overallHealthy = false
        )
    }
    
    val isEnabled = !isAppDisabled(context, packageName)
    val isBatteryOptimized = isBatteryOptimized(context, packageName)
    
    val lastUsed = if (hasUsagePermission) {
        getAppLastUsedTime(context, packageName)
    } else {
        -1L
    }
    
    val daysSinceLastUse = if (lastUsed > 0) {
        ((System.currentTimeMillis() - lastUsed) / (24 * 60 * 60 * 1000)).toInt()
    } else {
        -1
    }
    
    val isForceStopped = hasUsagePermission && lastUsed > 0 && daysSinceLastUse > 7
    
    val overallHealthy = isInstalled && 
                         isEnabled && 
                         !isBatteryOptimized && 
                         !isForceStopped
    
    return BankAppHealth(
        packageName = packageName,
        bankName = bankName,
        isInstalled = isInstalled,
        isEnabled = isEnabled,
        isBatteryOptimized = isBatteryOptimized,
        lastUsedDaysAgo = daysSinceLastUse,
        isForceStopped = isForceStopped,
        hasUsagePermission = hasUsagePermission,
        overallHealthy = overallHealthy
    )
}
```

---

## 7. Permissions Summary

| Check | Permission | User Action |
|-------|-----------|-------------|
| Battery Optimization | None | None |
| App Installation | None | None |
| App Enabled State | None | None |
| Usage Stats | `PACKAGE_USAGE_STATS` | Settings → Special Access → Usage Access |

**Critical:** Only Usage Stats requires user permission grant.

---

## 8. Fraud Prevention Integration

### Seller Health Heartbeat (Nostr)

```kotlin
fun broadcastSellerHealth(context: Context, bankPackages: List<String>) {
    val healthChecks = bankPackages.map { pkg ->
        checkBankAppHealth(context, pkg, getBankName(pkg))
    }
    
    val allHealthy = healthChecks.all { it.overallHealthy }
    
    val nostrEvent = NostrEvent.createReplaceableEvent(
        kind = 30000,  // Replaceable event
        content = JSONObject().apply {
            put("seller_ready", allHealthy)
            put("banks", JSONArray(healthChecks.map { health ->
                JSONObject().apply {
                    put("bank", health.bankName)
                    put("healthy", health.overallHealthy)
                    put("issues", buildList {
                        if (!health.isInstalled) add("not_installed")
                        if (!health.isEnabled) add("disabled")
                        if (health.isBatteryOptimized) add("battery_optimized")
                        if (health.isForceStopped) add("force_stopped")
                    })
                }
            }))
            put("last_update", System.currentTimeMillis())
        }.toString()
    )
    
    // Broadcast to Nostr relays
    nostrClient.publishEvent(nostrEvent)
}
```

---

## 9. Known Limitations

### Without Root
- ❌ Cannot detect if app's background services are killed
- ❌ Cannot check notification permission state programmatically (Android 13+)
- ❌ Cannot force-enable disabled apps
- ❌ Cannot disable battery optimization for other apps

### With Root
3C Toolbox uses root for some advanced features, but **all health checks documented here work without root**.

---

## 10. CPU Optimization Notes

**Issue:** BizumParser currently uses 15% CPU (too high for 24/7 background service)

**3C Toolbox Approach:**
- Uses foreground service with `FOREGROUND_SERVICE_TYPE_SPECIAL_USE`
- Queries health status only when needed (not continuously)
- Caches results

**Recommendations for BizumParser:**
1. Don't poll continuously - check health:
   - When app starts
   - Every 6 hours (AlarmManager)
   - When user opens app
2. Use WorkManager for periodic checks instead of constant Service
3. Cache health status in SharedPreferences

---

## 11. Testing on Multiple Devices

### Discovered Package Variations
- **Sabadell:** `es.bancsabadell.mobilebancohd` (standard) vs `net.inverline.bancosabadell.officelocator.android` (office locator app)
- **Santander:** `es.bancosantander.apps`
- **BBVA:** `com.bbva.bbvacontigo`
- **Caja Rural:** `com.rsi.nba`
- **ING:** `es.ingdirect.ing`

**Lesson:** Users may have different banking app variants installed. Health checks must support multiple package names per bank.

---

## 12. Implementation Priority

### Phase 0 Requirements
1. ✅ **High Priority:** Battery optimization check (easy to fix, big impact)
2. ✅ **High Priority:** App installation check (instant detection)
3. ✅ **High Priority:** App enabled state (catches deliberate disabling)
4. ⚠️ **Medium Priority:** Force-stop detection (requires usage stats permission)
5. ❌ **Low Priority (Android 13+):** Notification permission (can't check programmatically)

### Notification Permission Check (Android 13+)
**Problem:** No API to check if another app has notification permission  
**Workaround:** Detect indirectly - if app is installed+enabled+not-battery-optimized but no notifications received in 24h, likely missing permission

---

## 13. References

### Android Documentation
- [PowerManager.isIgnoringBatteryOptimizations()](https://developer.android.com/reference/android/os/PowerManager#isIgnoringBatteryOptimizations(java.lang.String))
- [AppOpsManager.checkOpNoThrow()](https://developer.android.com/reference/android/app/AppOpsManager#checkOpNoThrow(java.lang.String,%20int,%20java.lang.String))
- [PackageManager.getApplicationEnabledSetting()](https://developer.android.com/reference/android/content/pm/PackageManager#getApplicationEnabledSetting(java.lang.String))
- [UsageStatsManager.queryEvents()](https://developer.android.com/reference/android/app/usage/UsageStatsManager#queryEvents(long,%20long))

### Related Research
- RS072: BizumParser NotificationListener implementation
- RS073: Debug mode for discovering unknown bank packages
- RS074: Dynamic volatility buffer (capital efficiency)
- Project Blog 2026-07-13: Debug mode & fraud prevention breakthrough

---

## 14. Next Steps

1. **Implement health checks in BizumParser**
   - Add `BankHealthChecker.kt` class
   - Integrate with existing `NotificationListener`
   - Store health status in Room database

2. **Optimize CPU usage**
   - Replace constant polling with WorkManager periodic tasks
   - Cache health status (check every 6 hours max)
   - Profile CPU usage after changes

3. **Design Nostr heartbeat system**
   - Replaceable events (kind 30000+)
   - Broadcast health status every 10 minutes
   - Buyers check seller health before sending money

4. **Add seller dashboard**
   - Show health status for all banking apps
   - Warning if any app unhealthy
   - Quick-fix buttons (battery optimization settings, etc.)

5. **Test resilience**
   - Power outage simulation (sudden shutdown)
   - Network disconnect handling
   - Transaction queueing when offline

---

**Status:** ✅ Research Complete  
**Confidence:** HIGH (all APIs verified in production app with millions of users)  
**Ready for Implementation:** YES
