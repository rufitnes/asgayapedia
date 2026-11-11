package com.asgaya.husk.trade

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asgaya.husk.data.CashoutRequest
import com.asgaya.husk.databinding.ItemCashoutRequestBinding
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Adapter for displaying cashout requests in the Trade tab
 *
 * Each card shows:
 * - Fiat amount (€100.00)
 * - BCH amount (0.154 BCH)
 * - From name (Elena or pubkey preview)
 * - Covenant address preview
 * - Timestamp (relative time)
 * - "Verify & Accept" button
 */
class CashoutRequestsAdapter(
    private val requests: List<CashoutRequest>,
    private val onVerifyClick: (CashoutRequest) -> Unit
) : RecyclerView.Adapter<CashoutRequestsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCashoutRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCashoutRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]

        with(holder.binding) {
            // Amount in fiat (large, bold)
            val fiatFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply {
                currency = java.util.Currency.getInstance("EUR")
            }
            amountFiat.text = "${fiatFormat.format(request.amountFiat)} Cash Out"

            // Amount in BCH (smaller)
            amountBch.text = "For ${String.format("%.8f", request.amountBCH)} BCH"

            // From: Buyer (person buying fiat with BCH)
            this.fromName.text = "From: Buyer"

            // Covenant address preview
            covenantAddress.text = "Covenant: ${request.addressPreview}"

            // Timestamp (relative time)
            val elapsed = System.currentTimeMillis() - request.timestamp
            val relativeTime = when {
                elapsed < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                elapsed < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed)
                    "$minutes minute${if (minutes > 1) "s" else ""} ago"
                }
                elapsed < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(elapsed)
                    "$hours hour${if (hours > 1) "s" else ""} ago"
                }
                else -> {
                    val days = TimeUnit.MILLISECONDS.toDays(elapsed)
                    "$days day${if (days > 1) "s" else ""} ago"
                }
            }
            timestamp.text = "⏰ $relativeTime"

            // Verify button click
            verifyButton.setOnClickListener {
                onVerifyClick(request)
            }
        }
    }

    override fun getItemCount() = requests.size
}
