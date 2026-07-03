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