package com.asgaya.husk.trade

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.data.AdType
import com.asgaya.husk.data.BoardFilter
import com.asgaya.husk.data.Listing
import com.asgaya.husk.data.NostrBoardSource
import com.asgaya.husk.databinding.ActivityMarketBinding
import com.asgaya.husk.nostr.NostrKeyManager
import com.asgaya.husk.wallet.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Market — the bulletin board as a first-class tab (`ui-rethink/41`).
 *
 * Lists **every** ad (sell + buy) with a **Filters** dropdown (asset × payment method) and a
 * **Sort** dropdown (fee ascending / descending). Opened as a tab it is plain browsing; opened
 * from a covenant card it carries the covenant so tapping a merchant starts the cash-out.
 */
class MarketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMarketBinding
    private lateinit var database: AppDatabase
    private lateinit var walletManager: WalletManager
    private lateinit var nostrKeyManager: NostrKeyManager

    private var allListings: List<Listing> = emptyList()
    private var assetOptions: List<String> = listOf(ANY, BCH)
    private val methodOptions = listOf(ANY, "Cash in person", "Card in person", "Bizum")
    private var selectedAsset: String = ANY
    private var selectedMethod: String = ANY
    private var ascending = true
    private var adType: AdType = AdType.BUYER

    private var covenant: String? = null
    private var pickSeller = false

    companion object {
        private const val TAG = "MarketActivity"
        const val EXTRA_CASHOUT_COVENANT = "market_cashout_covenant"
        const val EXTRA_PRESELECT_ASSET = "market_preselect_asset"
        // "Pick a seller for a send" mode (the sender's `Find sellers`, `ui-rethink/41`): tapping a
        // SELLER ad returns it to the caller via setResult.
        const val EXTRA_PICK_SELLER = "market_pick_seller"
        const val EXTRA_PICKED_ID = "market_picked_id"
        const val EXTRA_PICKED_NPUB = "market_picked_npub"
        private const val ANY = "Any"
        private const val BCH = "BCH"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        walletManager = WalletManager.getInstance(this)
        nostrKeyManager = NostrKeyManager(this)

        covenant = intent.getStringExtra(EXTRA_CASHOUT_COVENANT)
        pickSeller = intent.getBooleanExtra(EXTRA_PICK_SELLER, false)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Market"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.filtersButton.setOnClickListener { showFiltersDialog() }
        binding.sortButton.setOnClickListener { showSortDialog() }

        // Buy / Sell — another filter, from the ad type (`ui-rethink/41`).
        binding.adTypeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                adType = if (checkedId == com.asgaya.husk.R.id.btnBuy) AdType.BUYER else AdType.SELLER
                updateFilterLabel()
                render()
            }
        }

        // `Find sellers` mode: start on the Sell side (SELLER ads).
        if (pickSeller) binding.adTypeToggle.check(com.asgaya.husk.R.id.btnSell)

        load()
    }

    private fun load() {
        lifecycleScope.launch {
            val active = withContext(Dispatchers.IO) { walletManager.getActiveWallet() }
            val myNpub = active?.let {
                withContext(Dispatchers.IO) { nostrKeyManager.ensureNostrKeys(it).publicKeyHex }
            }
            val listings = withContext(Dispatchers.IO) {
                runCatching { NostrBoardSource(database.boardEventDao()).listings() }.getOrDefault(emptyList())
            }
            // The board: every ad (sell + buy), never our own.
            allListings = BoardFilter.excludingSelf(listings, active?.publicKey, myNpub)

            // Every asset on the board + BCH (so you can check all ads appear), `ui-rethink/41`.
            val assets = allListings.flatMap { it.assets }.filter { it.isNotBlank() && it != BCH }.distinct()
            assetOptions = listOf(ANY, BCH) + assets.sorted()
            // Optional preselect (from the card's "Buy ⟨main⟩"): the asset = the device's main currency.
            intent.getStringExtra(EXTRA_PRESELECT_ASSET)?.takeIf { it.isNotBlank() }?.let { pre ->
                if (pre in assetOptions) selectedAsset = pre
            }
            updateFilterLabel()
            render()
        }
    }

    private fun showFiltersDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }
        layout.addView(TextView(this).apply { text = "Asset" })
        val assetSpinner = Spinner(this).apply {
            adapter = spinnerAdapter(assetOptions)
            setSelection(assetOptions.indexOf(selectedAsset).coerceAtLeast(0))
        }
        layout.addView(assetSpinner)
        layout.addView(TextView(this).apply { text = "Payment method"; setPadding(0, 16, 0, 0) })
        val methodSpinner = Spinner(this).apply {
            adapter = spinnerAdapter(methodOptions)
            setSelection(methodOptions.indexOf(selectedMethod).coerceAtLeast(0))
        }
        layout.addView(methodSpinner)

        AlertDialog.Builder(this)
            .setTitle("Filters")
            .setView(layout)
            .setPositiveButton("Apply") { _, _ ->
                selectedAsset = assetOptions[assetSpinner.selectedItemPosition]
                selectedMethod = methodOptions[methodSpinner.selectedItemPosition]
                updateFilterLabel()
                render()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSortDialog() {
        val options = arrayOf("Fee ascending", "Fee descending")
        AlertDialog.Builder(this)
            .setTitle("Sort")
            .setSingleChoiceItems(options, if (ascending) 0 else 1) { dialog, which ->
                ascending = which == 0
                binding.sortButton.text = if (ascending) "Sort: Fee ↑" else "Sort: Fee ↓"
                render()
                dialog.dismiss()
            }
            .show()
    }

    private fun updateFilterLabel() {
        binding.filtersButton.text = "Filters: $selectedAsset · $selectedMethod"
    }

    private fun spinnerAdapter(options: List<String>): ArrayAdapter<String> =
        ArrayAdapter(this, android.R.layout.simple_spinner_item, options).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

    private fun render() {
        // "BCH"/"Any" = no asset constraint (every ad trades BCH); a method of "Any" = no method filter.
        val asset = selectedAsset.takeIf { it != ANY && it != BCH }
        val methods = if (selectedMethod == ANY) emptySet() else setOf(selectedMethod)
        val filtered = BoardFilter.matching(allListings, adType, asset, methods, null, null)
            .let { if (ascending) it.sortedBy { l -> l.fee } else it.sortedByDescending { l -> l.fee } }

        binding.adsContainer.removeAllViews()
        binding.emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        filtered.forEach { binding.adsContainer.addView(createCard(it)) }
    }

    private fun createCard(listing: Listing): View {
        val card = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            radius = 12f
            cardElevation = 4f
            setCardBackgroundColor(Color.parseColor(if (listing.adType == AdType.BUYER) "#E0F2FE" else "#FFEDD5"))
            isClickable = true
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }
        content.addView(TextView(this).apply {
            text = if (listing.adType == AdType.SELLER) "Sells BCH for ${listing.assets.joinToString(", ")}"
            else "Sells ${listing.assets.joinToString(", ")} for BCH"
            textSize = 16f
            setTextColor(Color.BLACK)
        })
        content.addView(TextView(this).apply {
            text = "👤 ${listing.displayName}  (${listing.cashAccount})"
            textSize = 13f
            setTextColor(Color.parseColor("#334155"))
            setPadding(0, 4, 0, 0)
        })
        content.addView(TextView(this).apply {
            text = "💳 ${listing.paymentMethods.joinToString(", ")} · Fee ${listing.feePercent}"
            textSize = 13f
            setTextColor(Color.DKGRAY)
            setPadding(0, 4, 0, 0)
        })
        listing.location?.takeIf { it.isNotBlank() }?.let {
            content.addView(TextView(this).apply {
                text = "📍 $it"
                textSize = 12f
                setTextColor(Color.DKGRAY)
                setPadding(0, 4, 0, 0)
            })
        }
        card.addView(content)
        card.setOnClickListener { onMerchantTap(listing) }
        return card
    }

    private fun onMerchantTap(listing: Listing) {
        val npub = listing.nostrPubkey
        if (npub.isNullOrBlank()) {
            Toast.makeText(this, "This ad has no coordination key", Toast.LENGTH_SHORT).show()
            return
        }
        // `Find sellers` mode: hand the chosen seller back to the send flow.
        if (pickSeller && listing.adType == AdType.SELLER) {
            setResult(
                RESULT_OK,
                Intent().putExtra(EXTRA_PICKED_ID, listing.id).putExtra(EXTRA_PICKED_NPUB, npub)
            )
            finish()
            return
        }
        val id = covenant
        if (id == null || listing.adType != AdType.BUYER) {
            Toast.makeText(
                this,
                "Open a payment and tap “Buy ${listing.assets.firstOrNull() ?: "at a merchant"}” to cash out here",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        // Opened from a covenant card: hand the context to RemittanceActivity, which runs the flow.
        startActivity(
            Intent(this, com.asgaya.husk.remittance.RemittanceActivity::class.java).apply {
                putExtra(com.asgaya.husk.remittance.RemittanceActivity.EXTRA_CASHOUT_COVENANT, id)
                putExtra(com.asgaya.husk.remittance.RemittanceActivity.EXTRA_CASHOUT_MERCHANT_ID, listing.id)
                putExtra(com.asgaya.husk.remittance.RemittanceActivity.EXTRA_CASHOUT_MERCHANT_NPUB, npub)
            }
        )
    }
}
