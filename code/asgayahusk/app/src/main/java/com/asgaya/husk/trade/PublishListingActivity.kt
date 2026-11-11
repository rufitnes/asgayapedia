package com.asgaya.husk.trade

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.asgaya.husk.AppDatabase
import com.asgaya.husk.data.AdType
import com.asgaya.husk.data.Listing
import com.asgaya.husk.data.ListingEntity
import com.asgaya.husk.data.Wallet
import com.asgaya.husk.databinding.ActivityPublishListingBinding
import com.asgaya.husk.nostr.NostrKeyManager
import com.asgaya.husk.wallet.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Compose or edit a bulletin-board ad (Phase 0: saved locally).
 *
 * One ad = "I sell ⟨asset⟩ for ⟨what it accepts⟩" (workspace `09`): a SELLER ad offers BCH for
 * fiat; a BUYER ad offers an asset for BCH. Board broadcast (Nostr NIP-99 `kind:30402`) and the
 * on-chain anchor come next; this screen only composes + stores the ad.
 *
 * Pass [EXTRA_EDIT_ID] to edit an existing ad.
 */
class PublishListingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPublishListingBinding
    private lateinit var database: AppDatabase
    private lateinit var walletManager: WalletManager
    private lateinit var nostrKeyManager: NostrKeyManager

    /** Non-null when editing an existing ad. */
    private var editId: String? = null

    companion object {
        private const val TAG = "PublishListing"

        const val EXTRA_EDIT_ID = "edit_listing_id"

        // Fixed for now (workspace `09`): the fee and the volatility buffer are not user-editable
        // yet — revisit when the on-chain anchor + the fee-enforcement loop land.
        private const val FIXED_FEE = 0.005          // 0.5%
        private const val FIXED_BUFFER_PERCENT = 7   // → minPricePercent = 93

        private val KNOWN_ASSETS = setOf("EUR", "USD", "VES", "H€", "HAu")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublishListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        walletManager = WalletManager.getInstance(this)
        nostrKeyManager = NostrKeyManager(this)

        editId = intent.getStringExtra(EXTRA_EDIT_ID)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = if (editId == null) "New ad" else "Edit ad"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        showIdentity()

        binding.assetOtherChip.setOnCheckedChangeListener { _, checked ->
            binding.assetOtherInput.visibility = if (checked) View.VISIBLE else View.GONE
            updateTypeLabels()
        }
        // Field 1 labels mirror the asset chosen in field 2:
        // SELLER = "Sell BCH for ⟨asset⟩", BUYER = "Sell ⟨asset⟩ for BCH".
        // Per-chip listeners: ChipGroup's group listener is single-selection oriented and misses
        // multi-select changes.
        listOf(binding.assetEur, binding.assetUsd, binding.assetVes, binding.assetHeur, binding.assetHau)
            .forEach { chip -> chip.setOnCheckedChangeListener { _, _ -> updateTypeLabels() } }
        binding.assetOtherInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = updateTypeLabels()
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.publishButton.setOnClickListener { publish() }

        // The payment-instructions section follows the selected methods.
        listOf(binding.methodCash, binding.methodCard, binding.methodBizum, binding.methodSepa, binding.methodPagoMovil)
            .forEach { chip -> chip.setOnCheckedChangeListener { _, _ -> updateInstructions() } }

        // The volatility buffer only applies to the BCH-selling ad (`09 §10`).
        binding.adTypeGroup.setOnCheckedChangeListener { _, _ -> updateTypeSections() }

        updateTypeLabels()
        updateInstructions()
        updateTypeSections()

        editId?.let { prefill(it) }
    }

    private fun showIdentity() {
        lifecycleScope.launch {
            val wallet = withContext(Dispatchers.IO) { walletManager.getActiveWallet() }
            binding.identityText.text = if (wallet == null) {
                "⚠️ No active wallet — create or switch one in the Wallets tab."
            } else {
                "Publishing as: ${wallet.label} · ${wallet.publicKey.take(16)}…"
            }
        }
    }

    private fun prefill(id: String) {
        lifecycleScope.launch {
            val entity = withContext(Dispatchers.IO) { database.listingDao().getById(id) } ?: return@launch
            val listing = entity.toListing()

            binding.typeBuyer.isChecked = listing.adType == AdType.BUYER
            binding.typeSeller.isChecked = listing.adType == AdType.SELLER

            binding.assetEur.isChecked = "EUR" in listing.assets
            binding.assetUsd.isChecked = "USD" in listing.assets
            binding.assetVes.isChecked = "VES" in listing.assets
            binding.assetHeur.isChecked = "H€" in listing.assets
            binding.assetHau.isChecked = "HAu" in listing.assets
            val other = listing.assets.firstOrNull { it !in KNOWN_ASSETS }
            if (other != null) {
                binding.assetOtherChip.isChecked = true
                binding.assetOtherInput.setText(other)
            }

            binding.methodCash.isChecked = "Cash in person" in listing.paymentMethods
            binding.methodCard.isChecked = "Card in person" in listing.paymentMethods
            binding.methodBizum.isChecked = "Bizum" in listing.paymentMethods
            binding.methodSepa.isChecked = "SEPA" in listing.paymentMethods
            binding.methodPagoMovil.isChecked = "PagoMóvil" in listing.paymentMethods

            binding.minInput.setText(listing.minAmount?.let { fmtNum(it) } ?: "")
            binding.maxInput.setText(listing.maxAmount?.let { fmtNum(it) } ?: "")
            binding.locationInput.setText(listing.location ?: "")
            binding.hoursInput.setText(listing.availableHours ?: "")
            binding.feeInput.setText(fmtNum(listing.fee * 100))

            binding.instrNameInput.setText(listing.establishment ?: "")
            binding.instrPhoneInput.setText(listing.contactPhone ?: "")
            binding.instrIbanInput.setText(listing.contactIban ?: "")
            binding.notesInput.setText(listing.notes ?: "")

            updateInstructions()
            updateTypeSections()
        }
    }

    /** Keep field 1's labels in sync with the asset chosen in field 2. */
    private fun updateTypeLabels() {
        val label = selectedAssets().joinToString(", ")
        binding.typeSeller.text = if (label.isEmpty()) "Sell BCH for ⟨the asset⟩" else "Sell BCH for $label"
        binding.typeBuyer.text = if (label.isEmpty()) "Sell ⟨the asset⟩ for BCH" else "Sell $label for BCH"
    }

    /** Show the private instruction fields required by the selected methods (`07`). */
    private fun updateInstructions() {
        val cash = binding.methodCash.isChecked
        val card = binding.methodCard.isChecked
        val bizum = binding.methodBizum.isChecked
        val sepa = binding.methodSepa.isChecked
        val any = cash || card || bizum || sepa
        binding.instructionsSection.visibility = if (any) View.VISIBLE else View.GONE
        binding.instrNameLayout.visibility = if (any) View.VISIBLE else View.GONE
        binding.instrAddressLayout.visibility = if (cash || card) View.VISIBLE else View.GONE
        binding.instrHoursLayout.visibility = if (cash || card) View.VISIBLE else View.GONE
        binding.instrPhoneLayout.visibility = if (bizum) View.VISIBLE else View.GONE
        binding.instrIbanLayout.visibility = if (sepa) View.VISIBLE else View.GONE
        binding.instrNotesLayout.visibility = if (any) View.VISIBLE else View.GONE
    }

    /** The volatility buffer only applies to the BCH-selling ad (`09 §10`). */
    private fun updateTypeSections() {
        binding.bufferSection.visibility =
            if (selectedAdType() == AdType.SELLER) View.VISIBLE else View.GONE
    }

    private fun selectedAdType(): AdType =
        if (binding.typeBuyer.isChecked) AdType.BUYER else AdType.SELLER

    private fun selectedAssets(): List<String> {
        val assets = mutableListOf<String>()
        if (binding.assetEur.isChecked) assets += "EUR"
        if (binding.assetUsd.isChecked) assets += "USD"
        if (binding.assetVes.isChecked) assets += "VES"
        if (binding.assetHeur.isChecked) assets += "H€"
        if (binding.assetHau.isChecked) assets += "HAu"
        if (binding.assetOtherChip.isChecked) {
            binding.assetOtherInput.text.toString().trim().takeIf { it.isNotEmpty() }?.let { assets += it }
        }
        return assets
    }

    private fun selectedMethods(): List<String> {
        val methods = mutableListOf<String>()
        if (binding.methodCash.isChecked) methods += "Cash in person"
        if (binding.methodCard.isChecked) methods += "Card in person"
        if (binding.methodBizum.isChecked) methods += "Bizum"
        if (binding.methodSepa.isChecked) methods += "SEPA"
        if (binding.methodPagoMovil.isChecked) methods += "PagoMóvil"
        return methods
    }

    private fun publish() {
        // Read the form on the main thread.
        val adType = selectedAdType()
        val assets = selectedAssets()
        if (assets.isEmpty()) {
            Toast.makeText(this, "⚠️ Pick at least one asset", Toast.LENGTH_LONG).show()
            return
        }
        val methods = selectedMethods()
        if (methods.isEmpty()) {
            Toast.makeText(this, "⚠️ Pick at least one payment method", Toast.LENGTH_LONG).show()
            return
        }
        // Required for the selected methods — an ad can't go live incomplete.
        val establishment = binding.instrNameInput.text.toString().trim()
        val contactPhone = binding.instrPhoneInput.text.toString().trim()
        val contactIban = binding.instrIbanInput.text.toString().trim()
        val notes = binding.notesInput.text.toString().trim()
        if (methods.contains("Bizum") && (establishment.isBlank() || contactPhone.isBlank())) {
            Toast.makeText(this, "⚠️ Bizum needs a name + phone", Toast.LENGTH_LONG).show()
            return
        }
        if (methods.contains("SEPA") && (establishment.isBlank() || contactIban.isBlank())) {
            Toast.makeText(this, "⚠️ SEPA needs a name + IBAN", Toast.LENGTH_LONG).show()
            return
        }
        val minAmount = binding.minInput.text.toString().toDoubleOrNull()
        val maxAmount = binding.maxInput.text.toString().toDoubleOrNull()
        val location = binding.locationInput.text.toString().trim().takeIf { it.isNotEmpty() }
        val hours = binding.hoursInput.text.toString().trim().takeIf { it.isNotEmpty() }
        // The ad's fee, in % in the form → fraction on the ad. May be negative (`bulletin-board/20`).
        val fee = (binding.feeInput.text.toString().trim().toDoubleOrNull() ?: (FIXED_FEE * 100)) / 100.0
        if (kotlin.math.abs(fee) > 0.5) {
            Toast.makeText(this, "⚠️ Fee too large (max ±50%)", Toast.LENGTH_LONG).show()
            return
        }

        if ((methods.contains("Cash in person") || methods.contains("Card in person")) && (establishment.isBlank() || location.isNullOrBlank())) {
            Toast.makeText(this, "⚠️ Needs an establishment name + address", Toast.LENGTH_LONG).show()
            return
        }

        binding.publishButton.isEnabled = false
        lifecycleScope.launch {
            try {
                val wallet = withContext(Dispatchers.IO) { walletManager.getActiveWallet() }
                if (wallet == null) {
                    Toast.makeText(this@PublishListingActivity, "⚠️ No active wallet — create or switch one first", Toast.LENGTH_LONG).show()
                    binding.publishButton.isEnabled = true
                    return@launch
                }

                val newId = adId(wallet, adType, assets, methods)
                val listing = Listing(
                    id = newId,
                    adType = adType,
                    cashAccount = "${wallet.label}#${wallet.publicKey.takeLast(4)}",
                    publicKey = wallet.publicKey,
                    nostrPubkey = withContext(Dispatchers.IO) {
                        nostrKeyManager.ensureNostrKeys(wallet).publicKeyHex
                    },
                    assets = assets.toSet(),
                    paymentMethods = methods,
                    fee = fee,
                    minPricePercent = if (adType == AdType.BUYER) 100 else 100 - FIXED_BUFFER_PERCENT,
                    location = location,
                    minAmount = minAmount,
                    maxAmount = maxAmount,
                    availableHours = hours,
                    isOnline = false   // saved as a draft — Publish (in My ads) puts it on the board
                )

                withContext(Dispatchers.IO) {
                    // Editing: drop the previous row if the id changed (assets/type changed).
                    val previousId = editId
                    if (previousId != null && previousId != newId) database.listingDao().delete(previousId)
                    database.listingDao().upsert(
                        ListingEntity.fromListing(listing.copy(
                            establishment = establishment.takeIf { it.isNotEmpty() },
                            contactPhone = contactPhone.takeIf { it.isNotEmpty() },
                            contactIban = contactIban.takeIf { it.isNotEmpty() },
                            notes = notes.takeIf { it.isNotEmpty() }
                        ))
                    )
                }
                Log.i(TAG, "✅ Ad saved locally: ${listing.id} (${adType.name}, ${assets.joinToString()})")
                Toast.makeText(
                    this@PublishListingActivity,
                    "✅ Saved — press 📢 Publish in My ads to put it on the board",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save ad", e)
                binding.publishButton.isEnabled = true
                Toast.makeText(this@PublishListingActivity, "❌ Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Stable per (wallet, ad type, asset set) → re-publishing the same ad replaces it. */
    private fun adId(wallet: Wallet, adType: AdType, assets: Collection<String>, methods: Collection<String>): String {
        val a = assets.sorted().joinToString("_") { it.replace(Regex("[^A-Za-z0-9]+"), "") }
        val m = methods.sorted().joinToString("_") { it.replace(Regex("[^A-Za-z0-9]+"), "") }
        // The method is part of the id: two ads differing only by rail (cash vs card) must be distinct
        // (Room PK + the Nostr addressable `d` tag) — otherwise the 2nd replaces the 1st.
        return "${wallet.publicKey.take(16)}-${adType.name}-$a-$m"
    }

    private fun fmtNum(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
}
