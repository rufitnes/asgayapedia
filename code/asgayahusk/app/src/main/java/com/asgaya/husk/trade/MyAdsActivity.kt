package com.asgaya.husk.trade

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.data.AdType
import com.asgaya.husk.data.ListingEntity
import com.asgaya.husk.databinding.ActivityMyAdsBinding
import com.asgaya.husk.nostr.ListingEvent
import com.asgaya.husk.nostr.NostrClient
import com.asgaya.husk.nostr.NostrEvent
import com.asgaya.husk.nostr.NostrKeyManager
import com.asgaya.husk.wallet.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * "My ads" — manage your own bulletin-board listings.
 *
 * Add / view / edit / publish / delete. **Publish** signs a NIP-99 `kind:30402` event with the ad's
 * identity key and emits it to the relays (Phase 0 = Nostr discovery only; the on-chain anchor
 * comes later, `06`). **Pause** flips the local `isOnline` flag.
 */
class MyAdsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAdsBinding
    private lateinit var database: AppDatabase
    private lateinit var walletManager: WalletManager
    private lateinit var nostrKeyManager: NostrKeyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        walletManager = WalletManager.getInstance(this)
        nostrKeyManager = NostrKeyManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "My ads"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.addAdButton.setOnClickListener {
            startActivity(Intent(this, PublishListingActivity::class.java))
        }

        // Recover this wallet's published ads from the board (e.g. after changing device), paused.
        binding.importAdsButton.setOnClickListener { importMyAds() }

        lifecycleScope.launch {
            database.listingDao().observeAll().collect { rows -> render(rows) }
        }
    }

    private fun render(rows: List<ListingEntity>) {
        binding.adsContainer.removeAllViews()
        binding.emptyState.visibility = if (rows.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        rows.forEach { binding.adsContainer.addView(createCard(it)) }
    }

    private fun createCard(row: ListingEntity): android.view.View {
        val listing = row.toListing()
        val card = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            radius = 12f
            cardElevation = 4f
            setCardBackgroundColor(Color.parseColor(if (listing.isOnline) "#E0F2FE" else "#F1F5F9"))
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val role = if (listing.adType == AdType.SELLER) "Sell BCH for ${listing.assets.joinToString(", ")}"
        else "Sell ${listing.assets.joinToString(", ")} for BCH"
        content.addView(TextView(this).apply {
            text = role
            textSize = 16f
            setTextColor(Color.BLACK)
        })
        content.addView(TextView(this).apply {
            text = if (listing.isOnline) "🟢 Live on the board" else "⚪ Not published (draft)"
            textSize = 12f
            setTextColor(if (listing.isOnline) Color.parseColor("#16A34A") else Color.parseColor("#64748B"))
        })
        content.addView(TextView(this).apply {
            text = "Payment: ${listing.paymentMethods.joinToString(", ")} · Fee ${listing.feePercent} · Buffer ${listing.bufferPercent}"
            textSize = 12f
            setTextColor(Color.DKGRAY)
            setPadding(0, 4, 0, 4)
        })
        listing.location?.let {
            content.addView(TextView(this).apply {
                text = "📍 $it"
                textSize = 12f
                setTextColor(Color.DKGRAY)
            })
        }

        // Actions: Edit · Publish/Pause · Delete
        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 12, 0, 0)
        }
        val actionParams = { LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, 0, 8, 0) } }

        actions.addView(com.google.android.material.button.MaterialButton(this).apply {
            text = "✏️ Edit"
            textSize = 12f
            layoutParams = actionParams()
            setOnClickListener {
                startActivity(
                    Intent(this@MyAdsActivity, PublishListingActivity::class.java)
                        .putExtra(PublishListingActivity.EXTRA_EDIT_ID, row.id)
                )
            }
        })
        actions.addView(com.google.android.material.button.MaterialButton(this).apply {
            text = if (listing.isOnline) "⏸ Pause" else "📢 Publish"
            textSize = 12f
            layoutParams = actionParams()
            setOnClickListener { if (listing.isOnline) pause(row) else publishToBoard(row) }
        })
        actions.addView(com.google.android.material.button.MaterialButton(this).apply {
            text = "🗑 Delete"
            textSize = 12f
            layoutParams = actionParams()
            setOnClickListener { delete(row.id) }
        })
        content.addView(actions)

        card.addView(content)
        return card
    }

    /**
     * Import the ads published by this wallet's **npub** (recovered from the board cache) as local
     * ads, **paused** by default. The private payment instructions are not published, so a recovered
     * ad has none — and a device without them must not answer a payer (`07`).
     */
    private fun importMyAds() {
        lifecycleScope.launch {
            try {
                val wallet = withContext(Dispatchers.IO) { walletManager.getActiveWallet() }
                val npub = wallet?.let {
                    withContext(Dispatchers.IO) { nostrKeyManager.ensureNostrKeys(it).publicKeyHex }
                }
                if (npub.isNullOrBlank()) {
                    Toast.makeText(this@MyAdsActivity, "⚠️ No active wallet", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val rows = withContext(Dispatchers.IO) {
                    database.boardEventDao().getAll().filter { it.pubkey == npub }
                }
                if (rows.isEmpty()) {
                    Toast.makeText(this@MyAdsActivity, "No published ads found for this wallet", Toast.LENGTH_LONG).show()
                    return@launch
                }

                var imported = 0
                withContext(Dispatchers.IO) {
                    rows.forEach { row ->
                        val listing = runCatching {
                            com.asgaya.husk.nostr.ListingEvent.parse(org.json.JSONObject(row.raw))
                        }.getOrNull() ?: return@forEach
                        // Preserve the ad's real board status (`status` tag: active/paused) — it was
                        // imported FROM the board, so it IS live if the event says so.
                        database.listingDao().upsert(
                            com.asgaya.husk.data.ListingEntity.fromListing(listing)
                                .copy(updatedAt = System.currentTimeMillis())
                        )
                        imported++
                    }
                }
                Toast.makeText(this@MyAdsActivity, "📥 Imported $imported ad(s) from the board", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                android.util.Log.e("MyAdsActivity", "Import failed", e)
                Toast.makeText(this@MyAdsActivity, "❌ Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pause(row: ListingEntity) {
        emitListing(row, online = false)
    }

    /**
     * Publish the ad to the board: sign a NIP-99 `kind:30402` event with the ad's identity key and
     * emit it to the relays. Phase 0 = Nostr discovery only; the on-chain anchor comes later (`06`).
     */
    private fun publishToBoard(row: ListingEntity) {
        emitListing(row, online = true)
    }

    /**
     * Sign a NIP-99 `kind:30402` event for the ad — with its `status` tag = active/paused — emit it
     * to the relays, then persist the local flag. Used by both publish (active) and pause (paused)
     * so the board always reflects the local state.
     */
    private fun emitListing(row: ListingEntity, online: Boolean) {
        lifecycleScope.launch {
            try {
                val listing = row.toListing().copy(isOnline = online)

                val wallet = withContext(Dispatchers.IO) {
                    database.walletDao().getAllWallets().firstOrNull { it.publicKey == listing.publicKey }
                }
                if (wallet == null) {
                    Toast.makeText(this@MyAdsActivity, "⚠️ Wallet for this ad not found", Toast.LENGTH_LONG).show()
                    return@launch
                }
                val keys = withContext(Dispatchers.IO) { nostrKeyManager.ensureNostrKeys(wallet) }

                val event = withContext(Dispatchers.IO) {
                    NostrEvent.buildEvent(
                        pubkey = keys.publicKeyHex,
                        createdAt = System.currentTimeMillis() / 1000,
                        kind = ListingEvent.KIND,
                        tags = ListingEvent.tags(listing),
                        content = ListingEvent.content(listing),
                        privKey = keys.privateKeyHex
                    )
                }

                // Keep the client subscribed to the ACTIVE wallet's npub (don't clobber the DM sub).
                val clientNpub = withContext(Dispatchers.IO) {
                    walletManager.getActiveWallet()?.let { nostrKeyManager.ensureNostrKeys(it).publicKeyHex }
                } ?: keys.publicKeyHex

                val client = NostrClient.getInstance(clientNpub, null)
                client.connect()
                val sent = withContext(Dispatchers.IO) { client.publishEvent(event) }

                withContext(Dispatchers.IO) {
                    database.listingDao().upsert(row.copy(isOnline = online, updatedAt = System.currentTimeMillis()))
                }
                Toast.makeText(
                    this@MyAdsActivity,
                    if (online) "📢 Published to $sent relay(s)" else "⏸ Paused on $sent relay(s)",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(this@MyAdsActivity, "❌ Board update failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun delete(id: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { database.listingDao().delete(id) }
            Toast.makeText(this@MyAdsActivity, "🗑 Ad deleted", Toast.LENGTH_SHORT).show()
        }
    }
}
