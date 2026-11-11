package com.asgaya.husk.send

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.asgaya.husk.databinding.ActivitySendBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * SendActivity - Multi-step send flow
 *
 * Step 1: Address input (contact selection / manual entry)
 * Step 2: Amount entry (BCH amount + fee selection)
 * Step 3: Confirmation (review + broadcast transaction)
 */
class SendActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendBinding
    private lateinit var adapter: SendPagerAdapter

    // Shared data across fragments
    var recipientAddress: String = ""
    var recipientName: String? = null
    var amountBch: Double = 0.0
    var feeSatPerByte: Long = 1L
    var transactionHex: String? = null

    companion object {
        private const val TAG = "SendActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Send Bitcoin Cash"
        }
    }

    private fun setupViewPager() {
        adapter = SendPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false // Disable swipe, use buttons

        // Setup step indicator
        TabLayoutMediator(binding.stepIndicator, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Address"
                1 -> "Amount"
                2 -> "Confirm"
                else -> ""
            }
        }.attach()

        Log.d(TAG, "Send flow initialized")
    }

    /**
     * Navigate to next step
     */
    fun nextStep() {
        val currentItem = binding.viewPager.currentItem
        if (currentItem < adapter.itemCount - 1) {
            binding.viewPager.currentItem = currentItem + 1
            Log.d(TAG, "→ Step ${currentItem + 2}")
        }
    }

    /**
     * Navigate to previous step
     */
    fun previousStep() {
        val currentItem = binding.viewPager.currentItem
        if (currentItem > 0) {
            binding.viewPager.currentItem = currentItem - 1
            Log.d(TAG, "← Step $currentItem")
        }
    }

    /**
     * Validate current step data
     */
    fun validateStep(step: Int): Boolean {
        return when (step) {
            0 -> {
                // Validate address
                if (recipientAddress.isBlank()) {
                    Toast.makeText(this, "Please enter a recipient address", Toast.LENGTH_SHORT).show()
                    false
                } else if (!isValidBchAddress(recipientAddress)) {
                    Toast.makeText(this, "Invalid BCH address", Toast.LENGTH_SHORT).show()
                    false
                } else {
                    true
                }
            }
            1 -> {
                // Validate amount
                if (amountBch <= 0.0) {
                    Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                    false
                } else {
                    true
                }
            }
            2 -> true // Final step, ready to broadcast
            else -> false
        }
    }

    /**
     * Basic BCH address validation
     */
    private fun isValidBchAddress(address: String): Boolean {
        // Basic validation - should start with bitcoincash: or bchtest:
        return address.startsWith("bitcoincash:") ||
               address.startsWith("bchtest:") ||
               // Also accept without prefix for now
               (address.length >= 42 && address.matches(Regex("^[qpzry9x8gf2tvdw0s3jn54khce6mua7l]+$")))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        // If on first step, exit. Otherwise go back a step.
        if (binding.viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            previousStep()
        }
    }
}
