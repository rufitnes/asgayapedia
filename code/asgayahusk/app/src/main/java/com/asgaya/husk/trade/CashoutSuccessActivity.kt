package com.asgaya.husk.trade

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.asgaya.husk.databinding.ActivityCashoutSuccessBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * CashoutSuccessActivity - Show success after broadcasting merchantCashout
 *
 * Displays:
 * - Transaction ID
 * - BCH received
 * - "NOW give cash to customer" instruction
 */
class CashoutSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCashoutSuccessBinding

    companion object {
        const val EXTRA_REQUEST_ID = "request_id"
        const val EXTRA_AMOUNT_FIAT = "amount_fiat"
        const val EXTRA_AMOUNT_BCH = "amount_bch"
        const val EXTRA_CUSTOMER_NAME = "customer_name"
        const val EXTRA_TXID = "txid"
    }

    private var requestId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCashoutSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestId = intent.getStringExtra(EXTRA_REQUEST_ID)
        val amountFiat = intent.getDoubleExtra(EXTRA_AMOUNT_FIAT, 0.0)
        val amountBch = intent.getDoubleExtra(EXTRA_AMOUNT_BCH, 0.0)
        val customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME) ?: "Customer"
        val txid = intent.getStringExtra(EXTRA_TXID) ?: "unknown"

        displaySuccess(amountFiat, amountBch, customerName, txid)
        setupButtons()
    }

    private fun displaySuccess(fiat: Double, bch: Double, customerName: String, txid: String) {
        val fiatFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY).apply {
            currency = java.util.Currency.getInstance("EUR")
        }

        binding.bchReceived.text = "You received: ${String.format("%.8f", bch)} BCH"
        binding.txid.text = "txid: ${txid.take(16)}..."
        binding.cashInstruction.text = "💶 NOW give ${fiatFormat.format(fiat)} cash\n     to $customerName"
    }

    private fun setupButtons() {
        binding.completeButton.setOnClickListener {
            // TightDS Finding 2: Mark request as COMPLETED
            requestId?.let { id ->
                CashoutRequestStorage.get(id)?.let { request ->
                    val completedRequest = request.copy(
                        status = com.asgaya.husk.data.CashoutStatus.COMPLETED
                    )
                    CashoutRequestStorage.update(completedRequest)
                }
            }

            // Return to Trade tab (will now filter out COMPLETED)
            finish()
        }
    }
}
