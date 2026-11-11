package com.asgaya.husk.send

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.asgaya.husk.CovenantWebView
import com.asgaya.husk.databinding.FragmentSendConfirmBinding
import com.asgaya.husk.wallet.WalletManager
import kotlinx.coroutines.launch

/**
 * SendConfirmFragment - Step 3: Review and broadcast transaction
 */
class SendConfirmFragment : Fragment() {

    private var _binding: FragmentSendConfirmBinding? = null
    private val binding get() = _binding!!
    private lateinit var sendActivity: SendActivity
    private lateinit var walletManager: WalletManager
    private lateinit var covenantWebView: CovenantWebView

    private var isSending = false

    companion object {
        private const val TAG = "SendConfirmFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendConfirmBinding.inflate(inflater, container, false)
        sendActivity = requireActivity() as SendActivity
        walletManager = WalletManager.getInstance(requireContext())
        covenantWebView = CovenantWebView.getInstance(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        // Refresh display every time fragment becomes visible
        displayReview()
    }

    private fun displayReview() {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📋 REVIEW SCREEN")
        Log.d(TAG, "  Recipient: ${sendActivity.recipientAddress}")
        Log.d(TAG, "  Amount: ${sendActivity.amountBch} BCH")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // Display recipient
        val recipientDisplay = sendActivity.recipientName?.let { name ->
            "$name\n${sendActivity.recipientAddress}"
        } ?: sendActivity.recipientAddress

        binding.recipientDisplay.text = recipientDisplay

        // Display amount (use US locale for BCH - always use period)
        binding.amountDisplay.text = String.format(java.util.Locale.US, "%.8f BCH", sendActivity.amountBch)

        // Display fee (BCH uses standard 1 sat/byte)
        val estimatedFeeBch = 0.00000250 // ~250 bytes × 1 sat/byte
        binding.feeDisplay.text = String.format(java.util.Locale.US, "%.8f BCH", estimatedFeeBch)

        // Display total
        val total = sendActivity.amountBch + estimatedFeeBch
        binding.totalDisplay.text = String.format(java.util.Locale.US, "%.8f BCH", total)
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener {
            sendActivity.previousStep()
        }

        binding.sendButton.setOnClickListener {
            if (!isSending) {
                confirmAndSend()
            }
        }
    }

    private fun confirmAndSend() {
        isSending = true
        binding.sendButton.isEnabled = false
        binding.sendButton.text = "Sending..."

        lifecycleScope.launch {
            try {
                val activeWallet = walletManager.getActiveWallet()
                    ?: throw IllegalStateException("No active wallet")

                Log.d(TAG, "Building transaction...")
                Log.d(TAG, "  From: ${activeWallet.address}")
                Log.d(TAG, "  To: ${sendActivity.recipientAddress}")
                Log.d(TAG, "  Amount: ${sendActivity.amountBch} BCH")

                // Get WIF for signing
                val wif = walletManager.getWIF(activeWallet)
                Log.d(TAG, "  WIF: ${wif.take(10)}...")

                // Convert BCH to satoshis
                val amountSats = (sendActivity.amountBch * 100_000_000).toLong()
                Log.d(TAG, "  Amount (sats): $amountSats")

                // Call WebView to build, sign, and broadcast transaction
                Log.d(TAG, "Calling WebView sendBch()...")
                val txid = covenantWebView.sendBch(
                    senderWIF = wif,
                    senderAddress = activeWallet.address,
                    recipientAddress = sendActivity.recipientAddress,
                    amountSats = amountSats,
                    fulcrumHost = "localhost",
                    fulcrumPort = 60003
                )

                Log.d(TAG, "✅ Transaction broadcast: $txid")

                Toast.makeText(
                    requireContext(),
                    "✅ Transaction sent!\nTXID: ${txid.take(16)}...",
                    Toast.LENGTH_LONG
                ).show()

                // Close activity on success
                requireActivity().finish()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to send transaction", e)
                Toast.makeText(
                    requireContext(),
                    "❌ Failed to send: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                binding.sendButton.isEnabled = true
                binding.sendButton.text = "Send BCH"
                isSending = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Don't destroy the singleton WebView - it's shared across the app
        _binding = null
    }
}
