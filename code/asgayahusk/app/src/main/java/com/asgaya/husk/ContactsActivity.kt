package com.asgaya.husk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asgaya.husk.databinding.ActivityContactsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: ContactsAdapter
    private var selectMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if we're in select mode (called from SendActivity)
        selectMode = intent.getBooleanExtra("SELECT_MODE", false)

        setupToolbar()
        database = AppDatabase.getDatabase(this)

        setupRecyclerView()
        loadContacts()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (selectMode) "Select Contact" else "Test Wallets"
    }

    private fun setupRecyclerView() {
        adapter = ContactsAdapter(emptyList()) { contact ->
            if (selectMode) {
                // Return selected contact to SendActivity
                val resultIntent = android.content.Intent().apply {
                    putExtra("address", contact.bchAddress)
                    putExtra("name", contact.name)
                    putExtra("cashAccount", contact.cashAccount)
                    putExtra("publicKey", contact.publicKey)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
        binding.contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ContactsActivity)
            adapter = this@ContactsActivity.adapter
        }
    }

    private fun loadContacts() {
        lifecycleScope.launch {
            database.contactDao().getAllContacts().collectLatest { contacts ->
                adapter.updateContacts(contacts)

                // Show empty state if no contacts
                if (contacts.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.contactsRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.contactsRecyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

class ContactsAdapter(
    private var contacts: List<Contact>,
    private val onContactClick: ((Contact) -> Unit)? = null
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.contactName)
        val cashAccount: TextView = itemView.findViewById(R.id.contactCashAccount)
        val address: TextView = itemView.findViewById(R.id.contactAddress)
        val balance: TextView = itemView.findViewById(R.id.contactBalance)
        val notes: TextView = itemView.findViewById(R.id.contactNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_list_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.name.text = contact.name
        holder.cashAccount.text = contact.cashAccount
        holder.address.text = contact.bchAddress.take(25) + "..." // Truncate address

        // PRODUCTION: Don't show other wallets' balances - only show your own wallet balance in MainActivity
        holder.balance.visibility = View.GONE
        // holder.balance.text = contact.balance  // Commented for production

        holder.notes.text = contact.notes ?: ""

        // Click listener for selection mode
        holder.itemView.setOnClickListener {
            onContactClick?.invoke(contact)
        }

        // Hide notes if empty
        if (contact.notes.isNullOrEmpty()) {
            holder.notes.visibility = View.GONE
        } else {
            holder.notes.visibility = View.VISIBLE
        }
    }

    override fun getItemCount() = contacts.size

    fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}
