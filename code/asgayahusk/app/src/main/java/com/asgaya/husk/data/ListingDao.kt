package com.asgaya.husk.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ListingEntity)

    @Query("SELECT * FROM listings ORDER BY updatedAt DESC")
    suspend fun getAll(): List<ListingEntity>

    @Query("SELECT * FROM listings ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE publicKey = :publicKey")
    suspend fun getByPublicKey(publicKey: String): List<ListingEntity>

    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ListingEntity?

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun delete(id: String)
}
