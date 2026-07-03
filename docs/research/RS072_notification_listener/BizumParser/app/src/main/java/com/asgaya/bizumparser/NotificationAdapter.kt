package com.asgaya.bizumparser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter : ListAdapter<ParsedNotification, NotificationAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val timeText: TextView = view.findViewById(R.id.timeText)
        private val bankText: TextView = view.findViewById(R.id.bankText)
        private val amountText: TextView = view.findViewById(R.id.amountText)
        private val senderText: TextView = view.findViewById(R.id.senderText)
        private val referenceText: TextView = view.findViewById(R.id.referenceText)

        fun bind(notification: ParsedNotification) {
            val dateFormat = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault())
            timeText.text = dateFormat.format(Date(notification.timestamp))
            bankText.text = notification.bankApp
            amountText.text = "€${String.format("%.2f", notification.amount)}"
            senderText.text = "From: ${notification.sender}"
            referenceText.text = "Ref: ${notification.reference}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ParsedNotification>() {
        override fun areItemsTheSame(oldItem: ParsedNotification, newItem: ParsedNotification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ParsedNotification, newItem: ParsedNotification): Boolean {
            return oldItem == newItem
        }
    }
}