package com.asgaya.husk.send

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.asgaya.husk.databinding.FragmentSendAmountBinding
import com.asgaya.husk.wallet.WalletManager
import com.asgaya.husk.ElectrumClient
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * SendAmountFragment - Step 2: Amount and fee selection
 */
class SendAmountFragment : Fragment() {

    private var _binding: FragmentSendAmountBinding? = null
    private val binding get() = _binding!!
    private lateinit var sendActivity: SendActivity
    private lateinit var walletManager: WalletManager

    private var currentBalanceBch: Double = 0.0
    private var bchPriceInFiat: Double = 350.0 // TODO: Fetch real price
    private var isRestoringState = false // Prevent TextWatcher from overriding during restore

    companion object {
        private const val TAG = "SendAmountFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendAmountBinding.inflate(inflater, container, false)
        sendActivity = requireActivity() as SendActivity
        walletManager = WalletManager.getInstance(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadBalance()
        setupUI()
    }

    private fun loadBalance() {
        lifecycleScope.launch {
            try {
                val activeWallet = walletManager.getActiveWallet()
                if (activeWallet != null) {
                    // Query actual balance from Electrum
                    val electrumClient = ElectrumClient()
                    currentBalanceBch = electrumClient.getBalance(activeWallet.address)
                    updateBalanceDisplay()
                    Log.d(TAG, "✅ Balance loaded: $currentBalanceBch BCH for ${activeWallet.address}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load balance", e)
                Toast.makeText(requireContext(), "⚠️ Failed to query balance", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBalanceDisplay() {
        binding.availableBalance.text = "Available: %.8f BCH".format(currentBalanceBch)
    }

    private fun setupUI() {
        // Amount input
        binding.amountInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (isRestoringState) return // Don't override during state restoration

                val input = s.toString().trim()
                if (input.isNotBlank()) {
                    try {
                        // Replace comma with period for parsing (locale-safe)
                        val normalizedInput = input.replace(',', '.')
                        val amount = normalizedInput.toDouble()
                        sendActivity.amountBch = amount
                        updateFiatPreview(amount)
                        Log.d(TAG, "Amount set: $amount BCH")
                    } catch (e: NumberFormatException) {
                        Log.w(TAG, "Invalid amount input: $input")
                        sendActivity.amountBch = 0.0
                    }
                } else {
                    sendActivity.amountBch = 0.0
                    binding.fiatPreview.text = ""
                }
            }
        })

        // Max button (send all)
        binding.maxButton.setOnClickListener {
            val maxAmount = currentBalanceBch - 0.00001 // Reserve for fee
            if (maxAmount > 0) {
                binding.amountInput.setText(maxAmount.toString())
            } else {
                Toast.makeText(requireContext(), "Insufficient balance", Toast.LENGTH_SHORT).show()
            }
        }

        // BCH uses fixed low fee - no selection needed
        sendActivity.feeSatPerByte = 1L
        Log.d(TAG, "Using standard BCH fee: 1 sat/byte")

        // Navigation buttons
        binding.backButton.setOnClickListener {
            sendActivity.previousStep()
        }

        binding.nextButton.setOnClickListener {
            Log.d(TAG, "Next button clicked. Current amount: ${sendActivity.amountBch}")
            if (validateAmount()) {
                Log.d(TAG, "✅ Validation passed, advancing to step 3")
                sendActivity.nextStep()
            } else {
                Log.w(TAG, "❌ Validation failed")
            }
        }

        // Restore previous input if any
        if (sendActivity.amountBch > 0.0) {
            Log.d(TAG, "Restoring amount: ${sendActivity.amountBch}")
            isRestoringState = true
            binding.amountInput.setText(sendActivity.amountBch.toString())
            updateFiatPreview(sendActivity.amountBch)
            isRestoringState = false
        }
    }

    private fun updateFiatPreview(amountBch: Double) {
        val fiatAmount = amountBch * bchPriceInFiat
        val prefs = requireContext().getSharedPreferences("BizumParserPrefs", Context.MODE_PRIVATE)
        val currencyCode = prefs.getString("fiat_currency", "EUR") ?: "EUR"

        val format = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            currency = Currency.getInstance(currencyCode)
        }

        binding.fiatPreview.text = "≈ ${format.format(fiatAmount)}"
    }

    private fun validateAmount(): Boolean {
        return if (sendActivity.amountBch <= 0.0) {
            Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
            false
        } else if (sendActivity.amountBch > currentBalanceBch) {
            Toast.makeText(requireContext(), "Insufficient balance", Toast.LENGTH_SHORT).show()
            false
        } else {
            sendActivity.validateStep(1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
