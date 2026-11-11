package com.asgaya.husk

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Contact entity for storing wallet information
 * Used for quick covenant creation and sending funds
 */
@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,                    // "Sender Wallet", "Recipient Wallet", etc.
    val cashAccount: String,             // "sender#100", "recipient#142", etc.
    val publicKey: String,               // 33-byte compressed pubkey (hex)
    val bchAddress: String,              // BCH testnet address
    val nostrPubkey: String? = null,     // 32-byte x-only Nostr pubkey (hex) for NIP-17 DMs
    val balance: String = "0.00 tBCH",   // TESTING ONLY - Don't populate in production (privacy)
    val notes: String? = null,           // Optional notes
    val createdAt: Long = System.currentTimeMillis()
)
