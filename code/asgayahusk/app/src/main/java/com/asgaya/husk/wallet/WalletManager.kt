package com.asgaya.husk.wallet

import android.content.Context
import android.util.Log
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.data.*
import com.asgaya.husk.utils.TestnetSecrets
import kotlinx.coroutines.flow.Flow

/**
 * WalletManager - Central manager for all wallet operations
 *
 * Handles:
 * - Importing wallets from WIF
 * - Creating HD wallets (future)
 * - Switching active wallet
 * - Retrieving wallet data
 * - Seeding test wallets
 */
class WalletManager(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val walletDao = database.walletDao()

    companion object {
        private const val TAG = "WalletManager"

        @Volatile
        private var INSTANCE: WalletManager? = null

        fun getInstance(context: Context): WalletManager {
            return INSTANCE ?: synchronized(this) {
                val instance = WalletManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    // ========== Wallet Creation ==========

    /**
     * Import wallet from WIF private key
     */
    suspend fun importWallet(
        label: String,
        role: WalletRole,
        wif: String,
        address: String,
        publicKey: String
    ): Wallet {
        Log.d(TAG, "Importing wallet: $label ($role)")

        // Check if wallet already exists
        if (walletDao.walletExists(address)) {
            Log.w(TAG, "Wallet already exists: $address")
            throw IllegalArgumentException("Wallet with address $address already exists")
        }

        // Create wallet entity
        val wallet = WalletFactory.createImported(
            label = label,
            role = role,
            wif = wif,  // TODO: Encrypt in production
            address = address,
            publicKey = publicKey
        )

        // Insert into database
        walletDao.insertWallet(wallet)

        Log.d(TAG, "✅ Wallet imported: ${wallet.id}")
        return wallet
    }

    /**
     * Create HD-derived wallet (future implementation)
     */
    suspend fun createHDWallet(
        label: String,
        role: WalletRole,
        accountIndex: Int
    ): Wallet {
        // TODO: Implement HD wallet derivation using BIP32/39/44
        // For now, throw not implemented
        throw NotImplementedError("HD wallet creation not yet implemented. Use importWallet() for now.")
    }

    // ========== Wallet Management ==========

    /**
     * Switch active wallet
     */
    suspend fun switchWallet(walletId: String) {
        Log.d(TAG, "Switching to wallet: $walletId")

        val wallet = walletDao.getWalletById(walletId)
            ?: throw IllegalArgumentException("Wallet not found: $walletId")

        // Atomic switch (set all inactive, then activate target)
        walletDao.switchActiveWallet(walletId)

        Log.d(TAG, "✅ Active wallet: ${wallet.label}")
    }

    /**
     * Delete wallet
     */
    suspend fun deleteWallet(walletId: String) {
        Log.d(TAG, "Deleting wallet: $walletId")

        val wallet = walletDao.getWalletById(walletId)
        if (wallet?.isActive == true) {
            throw IllegalStateException("Cannot delete active wallet. Switch to another wallet first.")
        }

        walletDao.deleteWallet(walletId)
        Log.d(TAG, "✅ Wallet deleted: $walletId")
    }

    /**
     * Rename wallet
     */
    suspend fun renameWallet(walletId: String, newLabel: String) {
        Log.d(TAG, "Renaming wallet $walletId to: $newLabel")
        walletDao.updateWalletLabel(walletId, newLabel)
    }

    // ========== Queries ==========

    /**
     * Get active wallet
     */
    suspend fun getActiveWallet(): Wallet? {
        return walletDao.getActiveWallet()
    }

    /**
     * Get active wallet as Flow (reactive)
     */
    fun getActiveWalletFlow(): Flow<Wallet?> {
        return walletDao.getActiveWalletFlow()
    }

    /**
     * Get all wallets
     */
    suspend fun getAllWallets(): List<Wallet> {
        return walletDao.getAllWallets()
    }

    /**
     * Get all wallets as Flow
     */
    fun getAllWalletsFlow(): Flow<List<Wallet>> {
        return walletDao.getAllWalletsFlow()
    }

    /**
     * Get wallets by role
     */
    suspend fun getWalletsByRole(role: WalletRole): List<Wallet> {
        return walletDao.getWalletsByRole(role)
    }

    /**
     * Get wallet count
     */
    suspend fun getWalletCount(): Int {
        return walletDao.getWalletCount()
    }

    // ========== Key Access ==========

    /**
     * Get WIF for wallet (decrypts if needed)
     */
    suspend fun getWIF(wallet: Wallet): String {
        if (wallet.sourceType != WalletSourceType.IMPORTED) {
            throw IllegalArgumentException("Cannot get WIF for HD-derived wallet. Use getPrivateKey() instead.")
        }

        // TODO: Decrypt in production
        return wallet.sourceData
    }

    /**
     * Get public key for wallet
     */
    fun getPublicKey(wallet: Wallet): String {
        return wallet.publicKey
    }

    /**
     * Find wallet by public key
     * Returns null if no wallet matches
     */
    suspend fun findWalletByPubkey(pubkey: String): Wallet? {
        val allWallets = getAllWallets()
        return allWallets.find { it.publicKey.equals(pubkey, ignoreCase = true) }
    }

    // ========== Test Wallet Seeding ==========

    /**
     * Seed test wallets on first launch
     *
     * Creates wallets for:
     * - Sender (from covenant-params-v2.3-testnet3.json)
     * - Recipient/Isabel (from covenant-params-v2.3-testnet3.json)
     * - Merchant (placeholder for future testing)
     */
    suspend fun seedTestWallets() {
        val count = getWalletCount()
        if (count > 0) {
            Log.d(TAG, "Wallets already seeded (count: $count)")
            return
        }

        val secrets = TestnetSecrets.load(context)
        if (secrets == null) {
            Log.w(TAG, "Test wallets not seeded: local testnet secrets are missing")
            return
        }

        Log.d(TAG, "🌱 Seeding test wallets...")

        try {
            // Sender wallet
            val sender = importWallet(
                label = "Sender",
                role = WalletRole.SENDER,
                wif = secrets.senderWif,
                address = "bchtest:qrw5nukh5jqend8922tf8zhxwyku6wfpxu9nl79hxf",
                publicKey = "032774f66536468a06a244568724bf1f8762682be1573ff9910667c0fea1226ece"
            )

            // Recipient wallet (Isabel)
            val recipient = importWallet(
                label = "Recipient (Isabel)",
                role = WalletRole.RECIPIENT,
                wif = secrets.recipientWif,
                address = "bchtest:qq2uxg4cu9axyzd9gjnhxwrvealt44mcwunp7gzd0k",
                publicKey = "03886b4f4100f9e348caed5155474f0a1a96db3e2706dc59e15260ed3d2b35e148"
            )

            // Merchant wallet (real funder WIF from covenant-params-v2.6.1-testnet3.json)
            val merchant = importWallet(
                label = "Merchant (Bob)",
                role = WalletRole.MERCHANT,
                wif = secrets.merchantWif,  // local, git-ignored
                address = "bchtest:qz4llaasruxhme2l80dsnme76fmj24ve05h6d8ghe5",  // Funder from covenant-params
                publicKey = "03d6e1b33780e34dff1c6dc705c626deed385e827e762abfc596764be0ca3e4f45"
            )

            // Set sender as default active wallet
            switchWallet(sender.id)

            Log.d(TAG, "✅ Test wallets seeded:")
            Log.d(TAG, "   - Sender: ${sender.address}")
            Log.d(TAG, "   - Recipient: ${recipient.address}")
            Log.d(TAG, "   - Merchant: ${merchant.address}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to seed test wallets", e)
            throw e
        }
    }

    /**
     * Reset all wallets (for testing/development)
     */
    suspend fun resetWallets() {
        Log.d(TAG, "⚠️ Resetting all wallets...")
        walletDao.deleteAllWallets()
        Log.d(TAG, "✅ All wallets deleted")
    }
}
