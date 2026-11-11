package com.asgaya.husk.remittance

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RemittanceDao {

    /**
     * List order (`ui-rethink/36`): **actionable covenants first** (awaiting funding / funded / confirmed),
     * then history, newest first within each group. `createdAt` ties (e.g. several offers received in the
     * same second on a relay replay) break by `id DESC`, so a just-arrived live covenant is never buried
     * under already-resolved ones.
     */
    @Query("""
        SELECT * FROM remittances
        ORDER BY
          CASE WHEN status IN ('awaiting_funding','pending','confirmed') THEN 0 ELSE 1 END,
          createdAt DESC, id DESC
    """)
    fun getAllRemittances(): Flow<List<Remittance>>

    @Query("SELECT * FROM remittances WHERE id = :id")
    suspend fun getRemittanceById(id: Long): Remittance?

    @Query("SELECT * FROM remittances WHERE status = :status ORDER BY createdAt DESC")
    fun getRemittancesByStatus(status: String): Flow<List<Remittance>>

    @Query("SELECT * FROM remittances WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getByStatus(status: String): List<Remittance>

    @Query("SELECT * FROM remittances WHERE covenantAddress = :address LIMIT 1")
    suspend fun findByCovenantAddress(address: String): Remittance?

    /** One active covenant per recipient (`ui-rethink/19`): the pending remittance for a recipient. */
    @Query("""
        SELECT * FROM remittances
        WHERE recipientAddress = :address
          AND status IN ('awaiting_funding','pending','confirmed')
        ORDER BY createdAt DESC LIMIT 1
    """)
    suspend fun findActiveByRecipient(address: String): Remittance?

    @Insert
    suspend fun insertRemittance(remittance: Remittance): Long

    @Update
    suspend fun updateRemittance(remittance: Remittance)

    @Query("UPDATE remittances SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE remittances SET txid = :txid, status = :status WHERE id = :id")
    suspend fun updateWithTxid(id: Long, txid: String, status: String)

    @Query("""
        UPDATE remittances
        SET status = :status, resolutionPath = :path, resolutionTxid = :txid, resolvedAt = :resolvedAt
        WHERE id = :id
    """)
    suspend fun updateResolution(id: Long, status: String, path: String, txid: String, resolvedAt: Long)

    @Delete
    suspend fun deleteRemittance(remittance: Remittance)
}
