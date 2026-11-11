package com.asgaya.husk

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID

@Entity(tableName = "covenant_requests")
data class CovenantRequest(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val cashAccount: String,          // "Elena#142" - PRIMARY LOOKUP KEY
    val covenantAddress: String,      // "bchtest:pzrx..."
    val eurCents: Int,                // 10000 (€100.00)
    val expiryTime: Long,             // Unix timestamp
    val initialBchPrice: Int,         // EUR cents (65000 = €650/BCH)
    val minPricePercent: Int,         // 93 (7% buffer)
    val status: String,               // "awaiting_payment", "funded", "claimed"
    val redeemScript: String? = null, // Covenant redeemScript (hex) - needed for claiming
    val txid: String? = null,         // Transaction ID (once funded)
    // The outpoint that holds the covenant (`txid:vout`), recorded at funding. It identifies the
    // covenant unambiguously: a later audit asks the chain "is THIS output still unspent?" instead
    // of inferring "spent" from a (possibly lagging) empty address query (`ui-rethink/16` P0-1; `58`).
    val fundingTxid: String? = null,
    val fundingVout: Int? = null,
    // Full covenant params (received in [FUND_COVENANT]; persisted so the funder can rebuild
    // the covenant to auto-recover its buffer via sellerRecoverBuffer after expiry)
    val senderPubkey: String? = null,
    val recipientPubkey: String? = null,
    val funderPubkey: String? = null,
    val oraclePubkey: String? = null,
    val senderNostrPubkey: String? = null,  // Nostr author of the [FUND_COVENANT] (to notify on resolution)
    val payWindowSeconds: Int? = null,      // the sender's pay window for this request (`ui-rethink/27`); null = default
    val assetCurrency: String? = null,      // the ad's asset = the exchange currency (e.g. USD)
    val payAmount: Double? = null,          // the sender's committed payment amount (the datum, `ui-rethink/40`)
    val payCurrency: String? = null,        // ... in this currency (the exchange currency)
    val feePercent: Double? = null,         // the seller's ad fee (fraction, e.g. 0.005) → the card shows amount + fee = total
    val createdAt: Long = System.currentTimeMillis(),
    val fundedAt: Long? = null
)

@Dao
interface CovenantDao {
    @Insert
    suspend fun insert(covenant: CovenantRequest)

    @Query("SELECT * FROM covenant_requests WHERE cashAccount = :cashAccount AND status = 'awaiting_payment' LIMIT 1")
    suspend fun findByCashAccount(cashAccount: String): CovenantRequest?

    @Query("SELECT * FROM covenant_requests WHERE covenantAddress = :address LIMIT 1")
    suspend fun findByAddress(address: String): CovenantRequest?

    @Query("UPDATE covenant_requests SET status = :status, txid = :txid, fundedAt = :fundedAt WHERE id = :id")
    suspend fun updateFunded(id: String, status: String, txid: String, fundedAt: Long)

    @Query("UPDATE covenant_requests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE covenant_requests SET fundingTxid = :txid, fundingVout = :vout WHERE id = :id")
    suspend fun updateFundingOutpoint(id: String, txid: String, vout: Int)

    @Query("UPDATE covenant_requests SET status = :status, txid = :txid WHERE id = :id")
    suspend fun updateStatusAndTxid(id: String, status: String, txid: String)

    @Query("SELECT * FROM covenant_requests ORDER BY createdAt DESC")
    suspend fun getAll(): List<CovenantRequest>
}
