package com.asgaya.husk.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CashoutMessageDao {

    /** Insert with UNIQUE(messageHash) dedup; returns rowId or -1 if duplicate delivery. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: CashoutMessage): Long

    @Query("SELECT * FROM cashout_messages WHERE covenantAddress = :addr AND type = :type ORDER BY createdAt DESC LIMIT 1")
    suspend fun get(addr: String, type: String): CashoutMessage?

    @Query("SELECT * FROM cashout_messages WHERE covenantAddress = :addr AND type = :type AND consumed = 0 LIMIT 1")
    suspend fun getPending(addr: String, type: String): CashoutMessage?

    @Query("SELECT * FROM cashout_messages WHERE type = :type AND consumed = 0 ORDER BY createdAt DESC")
    fun observePending(type: String): Flow<List<CashoutMessage>>

    @Query("UPDATE cashout_messages SET consumed = 1 WHERE id = :id")
    suspend fun markConsumed(id: Long)
}
