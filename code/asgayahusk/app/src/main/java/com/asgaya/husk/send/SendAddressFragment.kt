package com.asgaya.husk.send

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.asgaya.husk.ContactsActivity
import com.asgaya.husk.databinding.FragmentSendAddressBinding

/**
 * SendAddressFragment - Step 1: Address input
 *
 * Priority flow:
 * 1. Select from contacts (primary)
 * 2. Paste from clipboard
 * 3. Manual entry
 */
class SendAddressFragment : Fragment() {

    private var _binding: FragmentSendAddressBinding? = null
    private val binding get() = _binding!!
    private lateinit var sendActivity: SendActivity

    // Contact selection result handler
    private val contactPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val address = result.data?.getStringExtra("address")
            val name = result.data?.getStringExtra("name")

            if (address != null) {
                binding.addressInput.setText(address)
                sendActivity.recipientAddress = address
                sendActivity.recipientName = name

                // Auto-advance if valid address selected
                if (sendActivity.validateStep(0)) {
                    sendActivity.nextStep()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendAddressBinding.inflate(inflater, container, false)
        sendActivity = requireActivity() as SendActivity
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        // Primary action: Select from contacts
        binding.selectContactButton.setOnClickListener {
            val intent = Intent(requireContext(), ContactsActivity::class.java).apply {
                putExtra("SELECT_MODE", true) // Tell ContactsActivity we want to select, not just view
            }
            contactPickerLauncher.launch(intent)
        }

        // Paste button
        binding.pasteButton.setOnClickListener {
            val clipboard = requireContext().getSystemService(android.content.ClipboardManager::class.java)
            val clip = clipboard?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val pastedText = clip.getItemAt(0).text.toString()
                binding.addressInput.setText(pastedText)
                sendActivity.recipientAddress = pastedText
            }
        }

        // Manual entry (text change listener)
        binding.addressInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                sendActivity.recipientAddress = s.toString().trim()
                sendActivity.recipientName = null // Clear name if manually edited
            }
        })

        // Next button
        binding.nextButton.setOnClickListener {
            if (sendActivity.validateStep(0)) {
                sendActivity.nextStep()
            }
        }

        // Restore previous input if any
        if (sendActivity.recipientAddress.isNotBlank()) {
            binding.addressInput.setText(sendActivity.recipientAddress)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
