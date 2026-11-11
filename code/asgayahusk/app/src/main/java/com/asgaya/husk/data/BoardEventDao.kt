package com.asgaya.husk.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BoardEventEntity)

    @Query("SELECT * FROM board_events ORDER BY createdAt DESC")
    suspend fun getAll(): List<BoardEventEntity>

    @Query("SELECT * FROM board_events ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BoardEventEntity>>

    @Query("DELETE FROM board_events")
    suspend fun clear()
}
