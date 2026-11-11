package com.asgaya.husk.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * WalletDao - Database access for wallet operations
 */
@Dao
interface WalletDao {

    // ========== Queries ==========

    /**
     * Get all wallets
     */
    @Query("SELECT * FROM wallets ORDER BY createdAt ASC")
    suspend fun getAllWallets(): List<Wallet>

    /**
     * Get all wallets as Flow (reactive updates)
     */
    @Query("SELECT * FROM wallets ORDER BY createdAt ASC")
    fun getAllWalletsFlow(): Flow<List<Wallet>>

    /**
     * Get active wallet (only one should be active)
     */
    @Query("SELECT * FROM wallets WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveWallet(): Wallet?

    /**
     * Get active wallet as Flow
     */
    @Query("SELECT * FROM wallets WHERE isActive = 1 LIMIT 1")
    fun getActiveWalletFlow(): Flow<Wallet?>

    /**
     * Get wallet by ID
     */
    @Query("SELECT * FROM wallets WHERE id = :walletId")
    suspend fun getWalletById(walletId: String): Wallet?

    /**
     * Get wallets by role
     */
    @Query("SELECT * FROM wallets WHERE role = :role ORDER BY createdAt ASC")
    suspend fun getWalletsByRole(role: WalletRole): List<Wallet>

    /**
     * Get wallet count
     */
    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun getWalletCount(): Int

    /**
     * Check if wallet with address exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM wallets WHERE address = :address LIMIT 1)")
    suspend fun walletExists(address: String): Boolean

    // ========== Inserts ==========

    /**
     * Insert new wallet
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: Wallet): Long

    /**
     * Insert multiple wallets
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<Wallet>)

    // ========== Updates ==========

    /**
     * Update wallet
     */
    @Update
    suspend fun updateWallet(wallet: Wallet)

    /**
     * Set all wallets inactive (used before activating a different wallet)
     */
    @Query("UPDATE wallets SET isActive = 0")
    suspend fun setAllWalletsInactive()

    /**
     * Set specific wallet as active
     */
    @Query("UPDATE wallets SET isActive = 1 WHERE id = :walletId")
    suspend fun setWalletActive(walletId: String)

    /**
     * Update wallet label
     */
    @Query("UPDATE wallets SET label = :newLabel WHERE id = :walletId")
    suspend fun updateWalletLabel(walletId: String, newLabel: String)

    // ========== Deletes ==========

    /**
     * Delete wallet by ID
     */
    @Query("DELETE FROM wallets WHERE id = :walletId")
    suspend fun deleteWallet(walletId: String)

    /**
     * Delete all wallets (for testing/reset)
     */
    @Query("DELETE FROM wallets")
    suspend fun deleteAllWallets()

    // ========== Transactions ==========

    /**
     * Switch active wallet (atomic operation)
     */
    @Transaction
    suspend fun switchActiveWallet(newWalletId: String) {
        setAllWalletsInactive()
        setWalletActive(newWalletId)
    }
}
