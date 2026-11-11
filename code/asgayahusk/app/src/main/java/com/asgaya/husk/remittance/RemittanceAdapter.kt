package com.asgaya.husk.remittance

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asgaya.husk.ElectrumClient
import com.asgaya.husk.R
import com.asgaya.husk.covenant.CovenantRules
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RemittanceAdapter(
    private var remittances: List<Remittance>,
    private val onRefundClick: (Remittance) -> Unit = {},
    private val onAbortClick: (Remittance) -> Unit = {},
    private val onClaimClick: (Remittance) -> Unit = {},
    private val onCashOutClick: (Remittance) -> Unit = {},  // NEW: Cash out at merchant
    private val onBuyClick: (Remittance) -> Unit = {},      // Buy ⟨main currency⟩ at Market (`ui-rethink/41`)
    private val onShareParamsClick: (Remittance) -> Unit = {},
    private val onSendAgainClick: (Remittance) -> Unit = {}  // "Send again" (`ui-rethink/34`)
) : RecyclerView.Adapter<RemittanceAdapter.RemittanceViewHolder>() {

    class RemittanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var balanceQueryJob: Job? = null  // Track coroutine job for cancellation
        val statusBadge: TextView = itemView.findViewById(R.id.statusBadge)
        val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        val recipientName: TextView = itemView.findViewById(R.id.recipientName)

        // Sent remittance amount section
        val amountSectionSent: View = itemView.findViewById(R.id.amountSectionSent)
        val amountSent: TextView = itemView.findViewById(R.id.amountSent)
        val amountSentBch: TextView = itemView.findViewById(R.id.amountSentBch)
        val amountReceived: TextView = itemView.findViewById(R.id.amountReceived)

        // Received remittance amount section
        val amountSectionReceived: View = itemView.findViewById(R.id.amountSectionReceived)
        val receivedAmount: TextView = itemView.findViewById(R.id.receivedAmount)
        val receivedAmountBch: TextView = itemView.findViewById(R.id.receivedAmountBch)
        val txidLabel: TextView = itemView.findViewById(R.id.txidLabel)
        val txid: TextView = itemView.findViewById(R.id.txid)
        val covenantStatusLabel: TextView = itemView.findViewById(R.id.covenantStatusLabel)
        val covenantStatus: TextView = itemView.findViewById(R.id.covenantStatus)
        val covenantCountdown: TextView = itemView.findViewById(R.id.covenantCountdown)
        val shareParamsButton: MaterialButton = itemView.findViewById(R.id.shareParamsButton)
        val checkBalanceButton: MaterialButton = itemView.findViewById(R.id.checkBalanceButton)
        val claimMerchantButton: MaterialButton = itemView.findViewById(R.id.claimMerchantButton)
        val refundButton: MaterialButton = itemView.findViewById(R.id.refundButton)
        val sendAgainButton: MaterialButton = itemView.findViewById(R.id.sendAgainButton)
    }

    companion object {
        private const val TAG = "RemittanceAdapter"
        private val electrumClient = ElectrumClient()
    }

    /** Selected currency (`fiat_currency`) + its display rate, set by the activity (`FiatDisplay`). */
    var mainCurrency: String = "EUR"
    @Volatile var eurPerMain: Double? = null

    /** The covenant's EUR face rendered in the device's selected currency (`FiatDisplay`). */
    private fun amountStr(eurCents: Int?): String =
        com.asgaya.husk.FiatDisplay.amount(eurCents ?: 0, mainCurrency, eurPerMain)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemittanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.remittance_card_item, parent, false)
        return RemittanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: RemittanceViewHolder, position: Int) {
        val remittance = remittances[position]

        // Expiry (`ui-rethink/36`): an OPEN covenant past its oracle-time expiry is NOT active —
        // dim it into history (its buffer is recoverable / refundable), regardless of stored status.
        val expired = CovenantTimeout.isExpired(remittance, System.currentTimeMillis() / 1000)

        // Presentation (`ui-rethink/36`): actionable covenants (awaiting funding / funded / confirmed,
        // and NOT expired) are full-strength and outlined so the live one stands out; the rest dim.
        val isActive = !expired && (remittance.status == RemittanceStatus.AWAITING_FUNDING ||
            remittance.status == RemittanceStatus.PENDING ||
            remittance.status == RemittanceStatus.CONFIRMED)
        holder.itemView.alpha = if (isActive) 1f else 0.55f
        (holder.itemView as? com.google.android.material.card.MaterialCardView)?.apply {
            if (isActive) {
                strokeWidth = (2 * resources.displayMetrics.density).toInt()
                strokeColor = 0xFF1976D2.toInt() // blue accent = needs attention
            } else {
                strokeWidth = 0
            }
        }

        // "Send again" (`ui-rethink/34`): repeat this remittance — sent side only, any status
        // (expired/funded/settled/refunded/claimed). Recreates with a fresh quote; the card stays.
        if (!remittance.isReceived) {
            holder.sendAgainButton.visibility = View.VISIBLE
            holder.sendAgainButton.setOnClickListener { onSendAgainClick(remittance) }
        } else {
            holder.sendAgainButton.visibility = View.GONE
        }

        // Status badge (`ui-rethink/36`): cover **every** status, so resolved cards never keep a
        // stale badge from a recycled view. Received + confirmed reads as actionable ("Ready to claim").
        if (expired) {
            holder.statusBadge.text = "⌛ Expired"
            holder.statusBadge.setTextColor(0xFF616161.toInt())
            holder.statusBadge.setBackgroundColor(0xFFF5F5F5.toInt())
        } else when (remittance.status) {
            RemittanceStatus.AWAITING_FUNDING -> {
                holder.statusBadge.text = "… Waiting for funding"
                holder.statusBadge.setTextColor(0xFF6D4C41.toInt())
                holder.statusBadge.setBackgroundColor(0xFFFFF8E1.toInt())
            }
            RemittanceStatus.PENDING -> {
                // PENDING = funded, awaiting 1 confirmation — say so, don't read as "not paid".
                holder.statusBadge.text = "⏳ Funded · confirming"
                holder.statusBadge.setTextColor(0xFFFF6F00.toInt())
                holder.statusBadge.setBackgroundColor(0xFFFFF3E0.toInt())
            }
            RemittanceStatus.CONFIRMED -> {
                holder.statusBadge.text = if (remittance.isReceived) "✓ Ready to claim" else "✓ Confirmed"
                holder.statusBadge.setTextColor(0xFF2E7D32.toInt())
                holder.statusBadge.setBackgroundColor(0xFFE8F5E9.toInt())
            }
            RemittanceStatus.DELIVERED, RemittanceStatus.CLAIMED, RemittanceStatus.SETTLED -> {
                holder.statusBadge.text = when (remittance.status) {
                    RemittanceStatus.DELIVERED -> "✓ Delivered"
                    RemittanceStatus.CLAIMED -> "✓ Claimed"
                    else -> "✓ Settled"
                }
                holder.statusBadge.setTextColor(0xFF1976D2.toInt())
                holder.statusBadge.setBackgroundColor(0xFFE3F2FD.toInt())
            }
            RemittanceStatus.REFUNDED -> {
                holder.statusBadge.text = "↩️ Refunded"
                holder.statusBadge.setTextColor(0xFF616161.toInt())
                holder.statusBadge.setBackgroundColor(0xFFF5F5F5.toInt())
            }
            RemittanceStatus.ABORTED -> {
                holder.statusBadge.text = "⚠️ Aborted"
                holder.statusBadge.setTextColor(0xFF616161.toInt())
                holder.statusBadge.setBackgroundColor(0xFFF5F5F5.toInt())
            }
            RemittanceStatus.EXPIRED -> {
                holder.statusBadge.text = "⌛ Expired"
                holder.statusBadge.setTextColor(0xFF616161.toInt())
                holder.statusBadge.setBackgroundColor(0xFFF5F5F5.toInt())
            }
            RemittanceStatus.FAILED -> {
                holder.statusBadge.text = "✗ Failed"
                holder.statusBadge.setTextColor(0xFFC62828.toInt())
                holder.statusBadge.setBackgroundColor(0xFFFFEBEE.toInt())
            }
            else -> {
                holder.statusBadge.text = "• ${remittance.status}"
                holder.statusBadge.setTextColor(0xFF616161.toInt())
                holder.statusBadge.setBackgroundColor(0xFFF5F5F5.toInt())
            }
        }

        // Timestamp
        holder.timestamp.text = formatTimeAgo(remittance.createdAt)

        // Show different layout for received vs sent remittances
        if (remittance.isReceived) {
            // Received covenant - simple layout
            holder.recipientName.visibility = View.GONE
            holder.amountSectionSent.visibility = View.GONE
            holder.amountSectionReceived.visibility = View.VISIBLE

            // Format amount
            val formatAmount = NumberFormat.getCurrencyInstance(Locale.getDefault())
            formatAmount.currency = Currency.getInstance(remittance.paymentCurrency)
            holder.receivedAmount.text = "Someone sent you ${amountStr(remittance.eurCents)}"
            holder.receivedAmountBch.text = String.format(Locale.US, "balance: %.8f BCH", remittance.amountBch)
        } else {
            // Sent remittance - full layout
            holder.recipientName.visibility = View.VISIBLE
            holder.amountSectionSent.visibility = View.VISIBLE
            holder.amountSectionReceived.visibility = View.GONE

            holder.recipientName.text = remittance.recipientName ?: run {
                // Truncate address if no name
                val addr = remittance.recipientAddress
                if (addr.length > 20) "${addr.take(10)}...${addr.takeLast(6)}" else addr
            }

            // Amount sent
            val formatSent = NumberFormat.getCurrencyInstance(Locale.getDefault())
            formatSent.currency = Currency.getInstance(remittance.paymentCurrency)
            holder.amountSent.text = amountStr(remittance.eurCents)
            holder.amountSentBch.text = String.format(Locale.US, "%.8f BCH", remittance.amountBch)

            // Amount received
            val formatReceived = NumberFormat.getCurrencyInstance(Locale.getDefault())
            formatReceived.currency = Currency.getInstance(remittance.recipientCurrency)
            holder.amountReceived.text = formatReceived.format(remittance.amountRecipientCurrency)
        }

        // Transaction ID (hide for received covenants, show for sent)
        if (!remittance.isReceived && remittance.txid != null && remittance.status != RemittanceStatus.PENDING) {
            holder.txidLabel.visibility = View.VISIBLE
            holder.txid.visibility = View.VISIBLE
            val txidShort = remittance.txid.let {
                if (it.length > 16) "${it.take(8)}...${it.takeLast(6)}" else it
            }
            holder.txid.text = txidShort
        } else {
            holder.txidLabel.visibility = View.GONE
            holder.txid.visibility = View.GONE
        }

        // Manual covenant balance check (button-triggered)
        if (remittance.covenantAddress != null) {
            holder.covenantStatusLabel.visibility = View.VISIBLE
            holder.covenantStatus.visibility = View.VISIBLE
            holder.covenantStatus.text = ""  // Will be set by balance check
            holder.covenantStatus.setTextColor(0xFF666666.toInt())

            // Update button text based on covenant type
            holder.checkBalanceButton.visibility = View.VISIBLE
            holder.checkBalanceButton.text = if (remittance.isReceived) "🔄 Update Status" else "🔄 Check Status"

            holder.claimMerchantButton.visibility = View.GONE
            holder.refundButton.visibility = View.GONE

            // Received covenant: the three actions are always available (`ui-rethink/41`) —
            // Update status (the button above) · Claim · Buy ⟨main currency⟩.
            if (remittance.isReceived) {
                holder.refundButton.visibility = View.VISIBLE
                holder.refundButton.text = "💰 Claim"
                holder.refundButton.setOnClickListener { onClaimClick(remittance) }
                holder.claimMerchantButton.visibility = View.VISIBLE
                holder.claimMerchantButton.text = "🛒 Buy $mainCurrency"
                holder.claimMerchantButton.setOnClickListener { onBuyClick(remittance) }
            }

            // Show countdown timer for received covenants
            if (remittance.isReceived && remittance.expiryOracleTime != null) {
                holder.covenantCountdown.visibility = View.VISIBLE
                holder.covenantCountdown.text = formatCountdown(remittance.expiryOracleTime)
            } else {
                holder.covenantCountdown.visibility = View.GONE
            }

            // Cancel previous balance query if still running
            holder.balanceQueryJob?.cancel()

            // Check/Update Balance button handler
            holder.checkBalanceButton.setOnClickListener {
                holder.covenantStatus.text = "Checking..."
                holder.covenantStatus.setTextColor(0xFF666666.toInt())

                // Cancel previous query
                holder.balanceQueryJob?.cancel()

                // Query balance asynchronously
                holder.balanceQueryJob = CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val balance = withContext(Dispatchers.IO) {
                            electrumClient.getBalance(remittance.covenantAddress!!)
                        }

                        // Update UI based on balance
                        if (balance == 0.0) {
                            // Covenant spent. Show only what THIS device actually recorded — deriving
                            // the path from isReceived made the same covenant read "Claimed" on one
                            // device and "Refunded" on another (file 57 / Suso). Unknown stays neutral;
                            // the on-chain resolution path is the audit-trail work (file 58).
                            // Covenant spent → show the chain-derived outcome. If we haven't
                            // reconciled yet (status still active), say so — never a misleading
                            // "Settled" (`ui-rethink/58`; Suso 2026-10-01).
                            val stillActive = remittance.status == RemittanceStatus.AWAITING_FUNDING ||
                                remittance.status == RemittanceStatus.PENDING ||
                                remittance.status == RemittanceStatus.CONFIRMED
                            holder.covenantStatus.text = if (stillActive) "⏳ Resolving…"
                                else CovenantRules.resolutionLabel(remittance.status)
                            holder.covenantStatus.setTextColor(0xFF2E7D32.toInt()) // Green
                            holder.checkBalanceButton.visibility = View.GONE
                            holder.claimMerchantButton.visibility = View.GONE
                            holder.refundButton.visibility = View.GONE
                            holder.shareParamsButton.visibility = View.GONE
                        } else {
                            // Covenant has funds
                            if (remittance.isReceived) {
                                // Anti-scam: only treat as a real payment if the UTXO covers the
                                // expected funding (eurCents at creation price + buffer). Otherwise
                                // dust would surface a fake "someone sent you €X" (social engineering).
                                val expectedBch = expectedFundingBch(remittance)
                                val isFunded = expectedBch == null || balance >= expectedBch * 0.95

                                if (!isFunded) {
                                    holder.covenantStatus.text = "⚠️ Insufficient funding"
                                    holder.covenantStatus.setTextColor(0xFFC62828.toInt()) // Red
                                    holder.claimMerchantButton.visibility = View.GONE
                                    holder.refundButton.visibility = View.GONE
                                    holder.shareParamsButton.visibility = View.GONE
                                    Log.w(TAG, "Received covenant under-funded: $balance BCH < expected $expectedBch BCH")
                                } else {
                                    // Received covenant - show "funded" and single Claim button
                                    holder.covenantStatus.text = "✅ funded"
                                    holder.covenantStatus.setTextColor(0xFF2E7D32.toInt()) // Green

                                    // Three actions under the card: Update status · Claim · Buy ⟨main⟩
                                    // (`ui-rethink/41`) — the claim/cash-out dialog chain is gone.
                                    holder.refundButton.visibility = View.VISIBLE
                                    holder.refundButton.text = "💰 Claim"
                                    holder.refundButton.setOnClickListener { onClaimClick(remittance) }
                                    holder.claimMerchantButton.visibility = View.VISIBLE
                                    holder.claimMerchantButton.text = "🛒 Buy $mainCurrency"
                                    holder.claimMerchantButton.setOnClickListener { onBuyClick(remittance) }
                                }
                            } else {
                                // Sent covenant - show balance and Refund button
                                holder.covenantStatus.text = String.format(Locale.US, "%.8f BCH", balance)
                                holder.covenantStatus.setTextColor(0xFFFF6F00.toInt()) // Orange

                                // Refundable if it's a SENT covenant with live funds (sender owns it).
                                // Applies to BOTH self-funded and seller-funded covenants:
                                // the sender is the contracting buyer and can refund() anytime.
                                val canRefund = (remittance.status == RemittanceStatus.PENDING ||
                                                 remittance.status == RemittanceStatus.CONFIRMED ||
                                                 remittance.status == RemittanceStatus.AWAITING_FUNDING)

                                if (canRefund) {
                                    holder.refundButton.visibility = View.VISIBLE
                                    holder.refundButton.text = "↩️ Refund / ⚠️ Abort"
                                    holder.refundButton.setOnClickListener {
                                        // Show dialog with both options
                                        android.app.AlertDialog.Builder(holder.itemView.context)
                                            .setTitle("Covenant Action")
                                            .setMessage("Choose an action:")
                                            .setPositiveButton("↩️ Refund") { _, _ ->
                                                onRefundClick(remittance)
                                            }
                                            .setNegativeButton("⚠️ Abort") { _, _ ->
                                                onAbortClick(remittance)
                                            }
                                            .setNeutralButton("Cancel", null)
                                            .show()
                                    }
                                } else {
                                    holder.refundButton.visibility = View.GONE
                                }

                                // Share Parameters button: only for SENT covenants (sender needs to re-share params).
                                // The recipient already has the params (they received the message); the sender may
                                // have missed the auto-copy window after creating the covenant.
                                holder.shareParamsButton.visibility = View.VISIBLE
                                holder.shareParamsButton.setOnClickListener {
                                    onShareParamsClick(remittance)
                                }
                            }
                        }

                        Log.d(TAG, "Covenant ${remittance.covenantAddress?.take(20)}... balance: $balance BCH")

                        // Wait 5 seconds to ensure TCP connection is fully closed by OS
                        // This prevents WebSocket connection conflicts when user immediately
                        // navigates to create new covenant, refund, or claim
                        Log.d(TAG, "⏳ Waiting 5s for TCP connection cleanup...")
                        delay(5000)
                        Log.d(TAG, "✅ TCP connection cleanup complete")

                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to query covenant balance", e)
                        holder.covenantStatus.text = "❌ Query failed"
                        holder.covenantStatus.setTextColor(0xFFC62828.toInt()) // Red
                        holder.checkBalanceButton.visibility = View.VISIBLE
                        holder.claimMerchantButton.visibility = View.GONE
                        holder.refundButton.visibility = View.GONE
                    }
                }
            }
        } else {
            // No covenant address (shouldn't happen for self-funded)
            holder.covenantStatusLabel.visibility = View.GONE
            holder.covenantStatus.visibility = View.GONE
            holder.covenantCountdown.visibility = View.GONE
            holder.checkBalanceButton.visibility = View.GONE
            holder.claimMerchantButton.visibility = View.GONE
            holder.refundButton.visibility = View.GONE
        }
    }

    /**
     * Expected funding for a received covenant, in BCH.
     *
     * = eurCents worth at the covenant's creation price, plus the volatility buffer.
     * Used to distinguish a real funding from dust (anti-scam / social engineering).
     * Returns null if the covenant lacks the params to compute it.
     */
    private fun expectedFundingBch(remittance: Remittance): Double? {
        val eurCents = remittance.eurCents ?: return null
        val price = remittance.initialBchPriceInCents ?: return null
        val minPct = remittance.minPricePercent ?: 93
        if (eurCents <= 0 || price <= 0) return null
        val paymentSats = eurCents.toDouble() * 1e8 / price
        val buffer = (100 - minPct) / 100.0
        return paymentSats * (1.0 + buffer) / 1e8
    }

    override fun getItemCount() = remittances.size

    override fun onViewRecycled(holder: RemittanceViewHolder) {
        super.onViewRecycled(holder)
        // Cancel balance query when view is recycled
        holder.balanceQueryJob?.cancel()
    }

    fun updateRemittances(newRemittances: List<Remittance>) {
        remittances = newRemittances
        notifyDataSetChanged()
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "$days ${if (days == 1L) "day" else "days"} ago"
            }
            else -> {
                val date = Date(timestamp)
                java.text.SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
            }
        }
    }

    /**
     * Format countdown timer from expiry timestamp
     * Returns "⏳ 2h 15m until expires" or "⚠️ Expired"
     */
    private fun formatCountdown(expiryTime: Long): String {
        val now = System.currentTimeMillis() / 1000  // Convert to seconds
        val diff = expiryTime - now

        return when {
            diff <= 0 -> "⚠️ Expired"
            diff < 3600 -> {
                // Less than 1 hour - show minutes only
                val minutes = diff / 60
                "⏳ ${minutes}m until expires"
            }
            diff < 86400 -> {
                // Less than 24 hours - show hours and minutes
                val hours = diff / 3600
                val minutes = (diff % 3600) / 60
                "⏳ ${hours}h ${minutes}m until expires"
            }
            else -> {
                // More than 24 hours - show days and hours
                val days = diff / 86400
                val hours = (diff % 86400) / 3600
                "⏳ ${days}d ${hours}h until expires"
            }
        }
    }
}
