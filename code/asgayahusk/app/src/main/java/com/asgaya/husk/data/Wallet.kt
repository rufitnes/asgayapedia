package com.asgaya.husk.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Wallet Entity - Supports both HD-derived and imported wallets
 *
 * Design: Hybrid approach (RS081)
 * - HD wallets for production (single seed backup)
 * - Imported keys for testing (existing test wallets)
 */
@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val label: String,                  // "Sender (Alice)"
    val role: WalletRole,               // SENDER, RECIPIENT, MERCHANT, ORACLE, CUSTOM

    // Wallet source (HD-derived or imported key)
    val sourceType: WalletSourceType,   // HD_DERIVED or IMPORTED
    val sourceData: String,             // Encrypted (accountIndex for HD, WIF for imported)

    // Derived data
    val address: String,                // bchtest:qrw5nu... (CashAddr format)
    val publicKey: String,              // Hex-encoded public key

    // State
    val isActive: Boolean = false,      // Only one wallet active at a time
    val createdAt: Long = System.currentTimeMillis(),

    // UI metadata (optional)
    val colorHex: String? = null,       // Role-based color (#3B82F6 for sender)
    val iconName: String? = null        // Optional icon identifier
)

/**
 * Wallet Role - Represents the purpose/identity of the wallet
 */
enum class WalletRole {
    SENDER,      // Initiates payments, funds covenants (self-funded)
    RECIPIENT,   // Receives payments, claims covenants
    SELLER,      // Funds covenants in exchange for fiat (bulletin board)
    MERCHANT,    // Receives merchant cashout payments
    ORACLE,      // Oracle service wallet (signs price data)
    CUSTOM;      // User-defined role

    /**
     * Get default color for this role
     */
    fun getDefaultColor(): String = when (this) {
        SENDER -> "#3B82F6"      // Blue
        RECIPIENT -> "#10B981"   // Green
        SELLER -> "#F59E0B"      // Orange
        MERCHANT -> "#A855F7"    // Purple
        ORACLE -> "#8B5CF6"      // Deep Purple
        CUSTOM -> "#6B7280"      // Gray
    }

    /**
     * Get display name for this role
     */
    fun getDisplayName(): String = when (this) {
        SENDER -> "Sender"
        RECIPIENT -> "Recipient"
        SELLER -> "Funder"
        MERCHANT -> "Merchant"
        ORACLE -> "Oracle"
        CUSTOM -> "Custom"
    }
}

/**
 * Wallet Source Type - How the wallet was created
 */
enum class WalletSourceType {
    HD_DERIVED,  // Derived from BIP39 seed (m/44'/145'/account')
    IMPORTED;    // Imported from WIF private key

    fun isHD(): Boolean = this == HD_DERIVED
    fun isImported(): Boolean = this == IMPORTED
}

/**
 * Helper factory methods for creating wallets
 */
object WalletFactory {
    /**
     * Create imported wallet from WIF
     */
    fun createImported(
        label: String,
        role: WalletRole,
        wif: String,
        address: String,
        publicKey: String
    ): Wallet = Wallet(
        label = label,
        role = role,
        sourceType = WalletSourceType.IMPORTED,
        sourceData = wif, // Will be encrypted by WalletManager
        address = address,
        publicKey = publicKey,
        colorHex = role.getDefaultColor()
    )

    /**
     * Create HD-derived wallet
     */
    fun createHD(
        label: String,
        role: WalletRole,
        accountIndex: Int,
        address: String,
        publicKey: String
    ): Wallet = Wallet(
        label = label,
        role = role,
        sourceType = WalletSourceType.HD_DERIVED,
        sourceData = accountIndex.toString(), // Will be encrypted by WalletManager
        address = address,
        publicKey = publicKey,
        colorHex = role.getDefaultColor()
    )
}
