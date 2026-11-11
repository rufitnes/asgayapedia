package com.asgaya.husk.transaction

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Pending Transaction Entity
 *
 * Tracks transactions that have been broadcast but may not be confirmed yet.
 * Enables rebroadcast on app resume and proper transaction state management.
 *
 * Based on Selene wallet pattern (RS083).
 */
@Entity(
    tableName = "pending_transactions",
    indices = [
        Index(value = ["status"]),
        Index(value = ["created_at"]),
        Index(value = ["covenant_address"])
    ]
)
data class PendingTransaction(
    @PrimaryKey
    val txid: String,

    // Transaction data
    val tx_hex: String,

    // Status tracking
    val status: String,  // BROADCAST, MEMPOOL, CONFIRMED, FAILED

    // Timestamps
    val created_at: Long = System.currentTimeMillis(),
    val confirmed_at: Long? = null,

    // Blockchain confirmation
    val block_height: Int? = null,
    val confirmations: Int = 0,

    // Covenant linkage (if this transaction is covenant-related)
    val covenant_address: String? = null,
    val remittance_id: Long? = null,

    // Error tracking
    val error_message: String? = null,
    val retry_count: Int = 0,
    val last_retry_at: Long? = null
)

/**
 * Status constants for pending transactions
 */
object TransactionStatus {
    const val BROADCAST = "BROADCAST"      // Broadcast but not yet verified in mempool
    const val MEMPOOL = "MEMPOOL"          // Confirmed in mempool
    const val CONFIRMED = "CONFIRMED"      // Confirmed in a block
    const val FAILED = "FAILED"            // Broadcast failed or transaction not found
}

/**
 * Data Access Object for PendingTransaction
 */
@Dao
interface PendingTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: PendingTransaction)

    @Update
    suspend fun update(transaction: PendingTransaction)

    @Query("SELECT * FROM pending_transactions WHERE txid = :txid")
    suspend fun getByTxid(txid: String): PendingTransaction?

    @Query("SELECT * FROM pending_transactions WHERE status IN (:statuses) ORDER BY created_at DESC")
    suspend fun getByStatus(statuses: List<String>): List<PendingTransaction>

    @Query("SELECT * FROM pending_transactions WHERE status = :status ORDER BY created_at DESC")
    suspend fun getByStatus(status: String): List<PendingTransaction>

    @Query("SELECT * FROM pending_transactions WHERE covenant_address = :address ORDER BY created_at DESC")
    suspend fun getByCovenantAddress(address: String): List<PendingTransaction>

    @Query("SELECT * FROM pending_transactions WHERE remittance_id = :remittanceId")
    suspend fun getByRemittanceId(remittanceId: Long): PendingTransaction?

    @Query("UPDATE pending_transactions SET status = :status WHERE txid = :txid")
    suspend fun updateStatus(txid: String, status: String)

    @Query("""
        UPDATE pending_transactions
        SET status = :status,
            confirmed_at = :confirmedAt,
            block_height = :blockHeight,
            confirmations = :confirmations
        WHERE txid = :txid
    """)
    suspend fun updateConfirmation(
        txid: String,
        status: String,
        confirmedAt: Long?,
        blockHeight: Int?,
        confirmations: Int
    )

    @Query("UPDATE pending_transactions SET error_message = :error, retry_count = retry_count + 1, last_retry_at = :timestamp WHERE txid = :txid")
    suspend fun recordRetry(txid: String, error: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM pending_transactions WHERE txid = :txid")
    suspend fun delete(txid: String)

    @Query("DELETE FROM pending_transactions WHERE status = 'CONFIRMED' AND confirmed_at < :olderThan")
    suspend fun deleteOldConfirmed(olderThan: Long)

    @Query("SELECT COUNT(*) FROM pending_transactions WHERE status IN ('BROADCAST', 'MEMPOOL')")
    suspend fun getPendingCount(): Int
}
