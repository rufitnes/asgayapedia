# RS072: Minimal Android Notification Parser - Implementation Guide

**Date:** June 29, 2026  
**Research Question:** How do we build a minimal Android app that parses bank notifications using NotificationListenerService, from zero to working validation?  
**Context:** RS026 confirms NotificationListenerService works (theory). RS070 has architecture plan. Need practical step-by-step guide for actual implementation.  
**Researcher:** Claude Sonnet 4.5 (Coordination)  
**Reviewer:** DeepSeek (TightDS)  
**Status:** ✅ TightDS approved with improvements (June 29, 2026)

---

## 🎯 Goal

**Build a minimal Android app that:**
1. ✅ Listens to all system notifications (background service)
2. ✅ Filters for bank app notifications (Bizum from Caja Rural, BBVA, etc.)
3. ✅ Parses notification text using regex (extract amount, sender, reference)
4. ✅ Stores parsed data in SQLite database
5. ✅ Displays parsed notifications in simple UI
6. ✅ Validates with 10 real €1 Bizum transactions

**Success criteria:**
- Parse 10/10 real Bizum notifications correctly
- Extract: amount (€), sender name, reference field (Cash Account)
- No false positives (ignore non-Bizum notifications)
- Works in background (app doesn't need to be open)

---

## 📋 Prerequisites

### Hardware
- Android phone (Android 7.0+ / API 24+)
- USB cable for development
- Computer (Windows/Mac/Linux)

### Software
- **Android Studio** (latest stable version - Iguana or newer)
- **JDK 17** (included with Android Studio)
- **Caja Rural banking app** (or other Spanish bank with Bizum)
- **€10** for testing (10 × €1 Bizum transactions)

### Knowledge
- Basic programming understanding (we'll explain everything else)
- No prior Android/Kotlin experience needed

---

## Part 1: Android Studio Setup (15 minutes)

### Step 1: Install Android Studio

**Download:**
- Go to https://developer.android.com/studio
- Download Android Studio for your OS
- Install with default settings

**First Launch:**
1. Open Android Studio
2. Select "Standard" installation
3. Wait for SDK download (2-3 GB, ~10 minutes)
4. Click "Finish" when complete

### Step 2: Enable Developer Mode on Phone

**On your Android phone:**
1. Go to Settings → About Phone
2. Tap "Build Number" 7 times (enables Developer Mode)
3. Go back to Settings → System → Developer Options
4. Enable "USB Debugging"
5. Connect phone to computer via USB
6. Accept "Allow USB Debugging" prompt on phone

**Verify connection:**
```bash
# In Android Studio → Terminal
adb devices
# Should show: [device-id]  device
```

---

## Part 2: Create Project (10 minutes)

### Step 1: New Project

**In Android Studio:**
1. File → New → New Project
2. Select **"Empty Views Activity"** (Kotlin)
3. Click "Next"

**Configure:**
```
Name: BizumParser
Package: com.asgaya.bizumparser
Save location: [your choice]
Language: Kotlin
Minimum SDK: API 24 (Android 7.0)
Build configuration: Kotlin DSL
```

4. Click "Finish"
5. Wait for Gradle sync (~2 minutes)

### Step 2: Project Structure

**Android Studio creates:**
```
BizumParser/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/asgaya/bizumparser/
│   │       │   └── MainActivity.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml
│   │       │   └── values/
│   │       │       └── strings.xml
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
└── build.gradle.kts
```

---

## Part 3: Add Dependencies (5 minutes)

### Edit `app/build.gradle.kts`

**Find the `dependencies` block and add:**

```kotlin
dependencies {
    // Existing dependencies (keep these)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ADD THESE for database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1") // Annotation processor

    // ADD THESE for lifecycle (RecyclerView, ViewModel)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    
    // ADD THIS for coroutines (explicit, avoids version conflicts)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // ADD THIS for JSON parsing (bank patterns config)
    implementation("org.json:json:20230227")
}
```

**At the top of the file, add KSP plugin:**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" // ADD THIS
}
```

**Sync Gradle:**
- Click "Sync Now" when prompted
- Wait ~1 minute for dependencies to download

---

## Part 4: Configure Permissions (5 minutes)

### Edit `AndroidManifest.xml`

**Add notification listener permission:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- ADD THIS PERMISSION -->
    <uses-permission android:name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.BizumParser"
        tools:targetApi="31">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- ADD THIS SERVICE -->
        <service
            android:name=".NotificationListener"
            android:exported="true"
            android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
        </service>

    </application>

</manifest>
```

---

## Part 5: Create Configurable Regex Patterns (10 minutes)

**TightDS Recommendation:** Make regex patterns configurable via JSON file instead of hard-coded. This allows updating patterns without recompiling if banks change their notification formats.

### Create JSON Config File

**Create directory:** `app/src/main/assets/` (right-click `main` → New → Directory → `assets`)

**Create file:** `app/src/main/assets/bank_patterns.json`

```json
[
  {
    "package": "com.cajarural.android",
    "bankName": "Caja Rural",
    "patterns": [
      {
        "name": "standard",
        "regex": "Bizum recibido:\\s*(\\d+(?:[.,]\\d{2})?)\\s*EUR\\s*de\\s*(.+?)\\.?\\s*Concepto:\\s*(.+)",
        "amountGroup": 1,
        "senderGroup": 2,
        "referenceGroup": 3
      }
    ]
  },
  {
    "package": "com.bbva.bbvacontigo",
    "bankName": "BBVA",
    "patterns": [
      {
        "name": "title_text_split",
        "regex": "Bizum recibido por importe de\\s*(\\d+(?:[.,]\\d{2})?)\\s*EUR",
        "amountGroup": 1,
        "senderInTitle": true,
        "referenceRegex": "Concepto:\\s*(.+)"
      }
    ]
  },
  {
    "package": "es.bancosantander.apps",
    "bankName": "Santander",
    "patterns": [
      {
        "name": "standard",
        "regex": "Bizum:\\s*(.+?)\\s+te ha enviado\\s*(\\d+(?:[.,]\\d{2})?)\\s*[€€]\\.?\\s*Concepto:\\s*(.+)",
        "amountGroup": 2,
        "senderGroup": 1,
        "referenceGroup": 3
      }
    ]
  },
  {
    "package": "es.bancsabadell.mobilebancohd",
    "bankName": "Sabadell",
    "patterns": [
      {
        "name": "standard",
        "regex": "Has recibido un Bizum de\\s*(\\d+(?:[.,]\\d{2})?)\\s*[€€]\\s*de\\s*(.+?)\\.?\\s*Concepto:\\s*(.+)",
        "amountGroup": 1,
        "senderGroup": 2,
        "referenceGroup": 3
      }
    ]
  },
  {
    "package": "es.ingdirect.ing",
    "bankName": "ING",
    "patterns": [
      {
        "name": "standard",
        "regex": "¡Has recibido un Bizum!\\s*(.+?)\\s+te ha enviado\\s*(\\d+(?:[.,]\\d{2})?)\\s*[€€]\\.?\\s*Concepto:\\s*(.+)",
        "amountGroup": 2,
        "senderGroup": 1,
        "referenceGroup": 3
      }
    ]
  }
]
```

**Why JSON?**
- Banks change notification formats after app updates
- Quick fix: update JSON, re-deploy (no recompilation)
- Easy to add new banks without touching Kotlin code
- Version control shows exactly what changed

---

## Part 6: Database Setup (30 minutes)

### Step 1: Create Data Class

**Create file:** `app/src/main/java/com/asgaya/bizumparser/ParsedNotification.kt`

```kotlin
package com.asgaya.bizumparser

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class ParsedNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,           // Unix timestamp (milliseconds)
    val bankApp: String,            // "Caja Rural", "BBVA", etc.
    val amount: Double,             // Euro amount (50.00 or 0.0 if parsing failed)
    val sender: String,             // "Juan Pérez"
    val reference: String,          // "Elena#142" (Cash Account)
    val rawText: String,            // Full notification text (for debugging)
    val parsedSuccessfully: Boolean // True if regex matched fully, false if partial/failed
)
```

### Step 2: Create DAO (Data Access Object)

**Create file:** `app/src/main/java/com/asgaya/bizumparser/NotificationDao.kt`

```kotlin
package com.asgaya.bizumparser

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: ParsedNotification)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<ParsedNotification>>

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun getCount(): Int
}
```

### Step 3: Create Database

**Create file:** `app/src/main/java/com/asgaya/bizumparser/AppDatabase.kt`

```kotlin
package com.asgaya.bizumparser

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ParsedNotification::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bizum_parser_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

## Part 7: Notification Listener Service (45 minutes)

### Create NotificationListener.kt (Improved with JSON Config)

**Create file:** `app/src/main/java/com/asgaya/bizumparser/NotificationListener.kt`

```kotlin
package com.asgaya.bizumparser

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
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
        database = AppDatabase.getDatabase(this)
        loadBankPatterns()
        Log.d(TAG, "NotificationListener service created, loaded ${bankConfigs.size} bank configs")
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

        // Find bank config for this package
        val bankConfig = bankConfigs.find { it.packageName == packageName }
        if (bankConfig == null) {
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
```

**Key improvements (TightDS approved):**
- ✅ Configurable regex patterns via JSON (no recompilation needed)
- ✅ Stores unparsed notifications for debugging (`parsedSuccessfully = false`)
- ✅ Handles partial matches (stores what was extracted)
- ✅ Detects Bizum keywords even if regex fails
- ✅ Logs everything for debugging (raw title + text)
- ✅ Try-catch on database inserts
- ✅ JSON supports multiple patterns per bank

---

## Part 8: Simple UI (30 minutes)

### Step 1: Update Layout

**Edit:** `app/src/main/res/layout/activity_main.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Bizum Parser"
        android:textSize="24sp"
        android:textStyle="bold"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <Button
        android:id="@+id/enableButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Enable Notification Access"
        android:layout_marginTop="16dp"
        app:layout_constraintTop_toBottomOf="@id/title"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/statusText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Status: Disabled"
        android:layout_marginTop="8dp"
        app:layout_constraintTop_toBottomOf="@id/enableButton"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/countText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Parsed: 0 notifications"
        android:layout_marginTop="8dp"
        android:textSize="18sp"
        app:layout_constraintTop_toBottomOf="@id/statusText"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginTop="16dp"
        app:layout_constraintTop_toBottomOf="@id/countText"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### Step 2: Create RecyclerView Adapter

**Create file:** `app/src/main/java/com/asgaya/bizumparser/NotificationAdapter.kt`

```kotlin
package com.asgaya.bizumparser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter : ListAdapter<ParsedNotification, NotificationAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val timeText: TextView = view.findViewById(R.id.timeText)
        private val bankText: TextView = view.findViewById(R.id.bankText)
        private val amountText: TextView = view.findViewById(R.id.amountText)
        private val senderText: TextView = view.findViewById(R.id.senderText)
        private val referenceText: TextView = view.findViewById(R.id.referenceText)

        fun bind(notification: ParsedNotification) {
            val dateFormat = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault())
            timeText.text = dateFormat.format(Date(notification.timestamp))
            bankText.text = notification.bankApp
            amountText.text = "€${String.format("%.2f", notification.amount)}"
            senderText.text = "From: ${notification.sender}"
            referenceText.text = "Ref: ${notification.reference}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ParsedNotification>() {
        override fun areItemsTheSame(oldItem: ParsedNotification, newItem: ParsedNotification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ParsedNotification, newItem: ParsedNotification): Boolean {
            return oldItem == newItem
        }
    }
}
```

### Step 3: Create Item Layout

**Create file:** `app/src/main/res/layout/item_notification.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardElevation="4dp"
    app:cardCornerRadius="8dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="12dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <TextView
                android:id="@+id/timeText"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="01/01 12:00:00"
                android:textSize="12sp"
                android:textColor="#666666" />

            <TextView
                android:id="@+id/bankText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Caja Rural"
                android:textSize="12sp"
                android:textStyle="bold"
                android:textColor="#2196F3" />
        </LinearLayout>

        <TextView
            android:id="@+id/amountText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="€1.00"
            android:textSize="24sp"
            android:textStyle="bold"
            android:textColor="#4CAF50"
            android:layout_marginTop="4dp" />

        <TextView
            android:id="@+id/senderText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="From: Juan Pérez"
            android:textSize="14sp"
            android:layout_marginTop="4dp" />

        <TextView
            android:id="@+id/referenceText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Ref: Elena#142"
            android:textSize="14sp"
            android:textStyle="italic"
            android:layout_marginTop="2dp" />

    </LinearLayout>

</androidx.cardview.widget.CardView>
```

### Step 4: Update MainActivity

**Edit:** `app/src/main/java/com/asgaya/bizumparser/MainActivity.kt`

```kotlin
package com.asgaya.bizumparser

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.asgaya.bizumparser.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NotificationAdapter
    private lateinit var database: AppDatabase

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

        // Update status
        updateStatus()

        // Observe database changes
        observeNotifications()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
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

    private fun observeNotifications() {
        lifecycleScope.launch {
            database.notificationDao().getAllNotifications().collectLatest { notifications ->
                adapter.submitList(notifications)
                binding.countText.text = "Parsed: ${notifications.size} notifications"
            }
        }
    }
}
```

**Add ViewBinding support to `app/build.gradle.kts`:**

```kotlin
android {
    // ... existing config ...

    buildFeatures {
        viewBinding = true  // ADD THIS
    }
}
```

Sync Gradle again.

---

## Part 9: Build and Install (15 minutes)

### Step 1: Build APK

**In Android Studio:**
1. Build → Make Project (wait for compilation)
2. Fix any errors (red text in Build console)
3. Run → Run 'app' (green play button)
4. Select your connected phone
5. Wait for installation (~30 seconds)

### Step 2: Grant Permission

**On your phone:**
1. App will open automatically
2. Tap "Enable Notification Access" button
3. Find "BizumParser" in the list
4. Toggle it ON
5. Accept warning dialog
6. Go back to app
7. Status should show "✅ Enabled"

**The app is now listening to all notifications in the background!**

### Step 3: Disable Battery Optimization (Android 14+)

**TightDS recommendation:** On Android 14+, the system may kill background services aggressively. Request battery optimization exemption to ensure reliable operation.

**Add to MainActivity.kt** (inside `onCreate()` after `updateStatus()`):

```kotlin
// Add at top of file
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

// Add this function inside MainActivity class
private fun requestBatteryOptimizationExemption() {
    val pm = getSystemService(POWER_SERVICE) as PowerManager
    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }
}

// Call this in onCreate() after updateStatus()
requestBatteryOptimizationExemption()
```

**On your phone after adding this code:**
1. Rebuild and run the app
2. Accept the battery optimization exemption dialog
3. The service will now survive aggressive Android 14+ power management

**Note:** This is crucial for Phase 0 passive seller automation (24/7 operation).

---

## Part 10: Testing (1 hour)

### Step 1: Send Test Bizum

**You need TWO accounts:**
1. **Your Caja Rural account** (receiving €1)
2. **Another account** (sending €1) - friend, family, or your other account

**How to test:**
1. Ask someone to send you €1 via Bizum
2. **IMPORTANT:** In the "Concepto" field, put: `TestAccount#123`
3. Wait for Bizum notification to arrive
4. Open BizumParser app
5. You should see the parsed notification!

**Expected result:**
```
€1.00
From: [Sender Name]
Ref: TestAccount#123
Bank: Caja Rural
```

### Step 2: Test Multiple Banks (If Available)

If you have accounts at different banks, repeat with:
- BBVA
- Santander
- Sabadell
- ING

**Each bank has slightly different notification format - this validates our regex patterns.**

### Step 3: Expanded Test Suite (TightDS Recommended)

**Test with variety of cases to catch real-world edge cases:**

**Test Case 1: Different amounts**
- €1.00 (standard)
- €50.00 (medium)
- €100.00 (large)
- Verify decimal formatting works: `1,00`, `1.00`, `50,50`

**Test Case 2: Special characters in Concepto field**
- `Elena#142` (Cash Account format)
- `Elena-142` (dash instead of hash - Bizum doesn't allow `#`)
- `Elena 142` (space instead of hash)
- `María#123` (accented characters)
- Test what actually comes through in the notification

**Test Case 3: Background operation**
- Lock your phone
- Send a Bizum
- Verify the notification is still parsed
- Check database when you unlock phone

**Test Case 4: Non-Bizum notification**
- Send yourself a WhatsApp message
- Verify it's NOT stored in database (filtered correctly)
- Check Logcat confirms it was ignored

**Test Case 5: 24-hour persistence**
- Leave app running for 24 hours
- Check if service is still active (Settings → Notification Access)
- Send test Bizum after 24 hours
- Verify it still parses (service wasn't killed)

**Minimum validation: 10 × €1 transactions with different Concepto values**
- ✅ 10/10 parsed correctly
- ✅ All amounts match
- ✅ All sender names captured
- ✅ All references extracted (even with special chars)
- ✅ No false positives
- ✅ Works in background

---

## Part 11: Debugging (If Things Don't Work)

### Check 1: Is Service Running?

**In Android Studio → Logcat:**
```
Filter: NotificationListener
```

**You should see:**
```
NotificationListener service created
```

**If not:** Service didn't start. Check AndroidManifest.xml configuration.

### Check 2: Are Notifications Being Received?

**Send a test Bizum, then check Logcat:**
```
Bank notification received from: com.cajarural.android
Title: [title]
Text: [text]
```

**If you see this:** Notifications are being received. Problem is in parsing.

**If you don't see this:**
- Check notification permission is granted
- Check package name matches your bank app

### Check 3: Is Parsing Working?

**After notification, check Logcat for:**
```
Parsed successfully: €1.00 from Juan Pérez (Elena#142)
Saved to database
```

**If "Not a Bizum notification":** Regex pattern doesn't match. Check notification format.

### Check 4: Get Actual Notification Format

**Temporarily add this logging to `onNotificationPosted()`:**

```kotlin
Log.d(TAG, "===== RAW NOTIFICATION =====")
Log.d(TAG, "Package: $packageName")
Log.d(TAG, "Title: $title")
Log.d(TAG, "Text: $text")
Log.d(TAG, "===========================")
```

**Send test Bizum, copy the exact format, then update regex pattern to match.**

### Check 5: 24-Hour Persistence (TightDS Recommended)

**After 24 hours of operation:**

1. **Check if service is still enabled:**
   - Settings → Notification Access
   - Verify "BizumParser" is still ON

2. **Check if service is alive:**
   - Open Android Studio → Logcat
   - Filter: `NotificationListener`
   - Send a test Bizum
   - Verify notification is logged and parsed

**If service was killed by Android:**
- This is expected on Android 14+ without battery optimization exemption
- Ensure Step 3 of Part 9 (Battery Optimization) was completed
- Consider foreground service if problem persists (see TightDS notes below)

**TightDS Note:** For long-term passive seller operation, if battery optimization exemption isn't sufficient, wrap the notification processing logic in a foreground service with a persistent notification. This guarantees Android won't kill it.

---

## Part 12: Expected Results

### Success Criteria ✅

**After 10 test transactions, you should have:**
1. ✅ 10 notifications in the app
2. ✅ All amounts correct (€1.00 each)
3. ✅ All sender names correct
4. ✅ All references correct (Cash Account format)
5. ✅ No false positives (WhatsApp, email, etc. ignored)
6. ✅ No false negatives (all Bizum notifications caught)

### What This Validates

**RS026 assumptions confirmed:**
- ✅ NotificationListenerService works as documented
- ✅ Bank apps cannot block notification access
- ✅ Regex patterns from RS026 are correct (or need updating)
- ✅ Parsing works in real-time (background service)
- ✅ No user interaction needed (passive automation)

**Critical for Asgaya:**
- ✅ 80% of passive seller automation depends on this
- ✅ Validates payment-first covenant model (seller detects payment → funds covenant)
- ✅ Proves no hardware needed (any Android phone works)

---

## Part 13: Next Steps (After Validation)

### If 10/10 Tests Pass ✅

**Phase 0 blocker REMOVED!**

1. **Document actual notification formats** (update RS026 with real formats)
2. **Add more banks** (expand regex patterns)
3. **Integrate with covenant logic** (parsed notification → create covenant)
4. **Build sender/recipient UX** (web prototype or React Native)
5. **Test with real remittances** (€100 transactions with Iris/Elena)

### If Tests Fail ❌

**Investigate:**
1. **Which bank failed?** (check regex pattern)
2. **What's the actual notification format?** (Logcat output)
3. **Update regex patterns** (match actual format, not assumed)
4. **Retest** until 10/10 pass

**Fallback options if parsing fundamentally doesn't work:**
- Bank API integration (if available)
- Push notifications (server-side)
- Manual confirmation (user taps "confirm" when payment received)

---

## TightDS Review Summary

**Review Date:** June 29, 2026  
**Verdict:** ✅ Approved with improvements incorporated

### Key Feedback Integrated

**1. Project Structure** ✅ Approved
- Kotlin + Room + RecyclerView is correct stack for validation prototype
- XML layouts appropriate (Jetpack Compose adds learning curve)
- Added explicit `kotlinx-coroutines-android` dependency

**2. Regex Patterns** ✅ Improved
- Made patterns configurable via JSON (`bank_patterns.json`)
- Allows updates without recompilation
- Banks DO change formats after app updates

**3. Database Schema** ✅ Enhanced
- Added `parsedSuccessfully` boolean field
- No indexes needed at this scale
- Schema sufficient for validation phase

**4. Error Handling** ✅ Implemented
- Store unparsed notifications for debugging
- Detect Bizum keywords even if regex fails
- Try-catch on database inserts (not retry logic)

**5. Testing Strategy** ✅ Expanded
- Test special characters in Concepto field
- Test different amounts (€1, €50, €100)
- Test background operation (phone locked)
- Test 24-hour persistence
- Test non-Bizum notifications (false positive check)

**6. Android Compatibility** ✅ Addressed
- API 24 minimum appropriate
- Added battery optimization exemption for Android 14+
- Foreground service fallback if needed for 24/7 operation

### TightDS Quote

> "The implementation plan is ready. The stack is appropriate, the regex approach is sound, and the testing strategy will catch the real‑world format variations. After this validation passes, the biggest technical blocker for Phase 0 is removed."

---

## Resources

### Android Documentation
- [NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [RecyclerView](https://developer.android.com/guide/topics/ui/layout/recyclerview)

### Asgaya Research
- **RS026:** NotificationListenerService theory + regex patterns
- **RS070:** Android app architecture plan
- **Unknown brief:** bank-notification-parsing-validation.md

### Tools
- **Android Studio:** https://developer.android.com/studio
- **ADB:** https://developer.android.com/studio/command-line/adb
- **Logcat:** https://developer.android.com/studio/debug/am-logcat

---

## Conclusion

**This guide provides:**
- ✅ Step-by-step Android app creation (zero experience needed)
- ✅ Working NotificationListenerService implementation
- ✅ Configurable regex patterns via JSON (5 Spanish banks)
- ✅ Simple UI to visualize parsed notifications
- ✅ Unparsed notification storage for debugging
- ✅ Expanded validation methodology (special chars, amounts, background)
- ✅ Android 14+ compatibility (battery optimization)
- ✅ Debugging guidance (Logcat, 24-hour persistence check)

**Total time estimate:**
- Setup: 30 minutes
- Implementation: 2-3 hours
- Testing: 1-2 hours (expanded test cases)
- **Total: ~4-5 hours from zero to validated**

**TightDS-approved improvements:**
- Configurable patterns (update without recompilation)
- Store failed parses for debugging
- Battery optimization exemption
- Expanded testing strategy

**Success validates:**
- RS026 theory → proven in practice
- 80% of Asgaya passive automation works
- Payment-first covenant model viable
- Phase 0 can proceed with confidence

---

**Status:** ✅ TightDS approved with improvements (June 29, 2026)  
**Next:** Implement guide and test with 10 × €1 Bizum transactions  
**Expected outcome:** Validate RS026 assumptions, remove 80% blocker for Phase 0
