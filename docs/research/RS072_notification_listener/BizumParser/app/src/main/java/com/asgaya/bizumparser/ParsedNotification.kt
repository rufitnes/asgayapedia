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