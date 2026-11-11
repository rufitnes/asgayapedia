package com.asgaya.husk.trade

import com.asgaya.husk.data.CashoutRequest

/**
 * Simple in-memory storage for cashout requests
 * Shared between TradeActivity and MerchantVerificationActivity
 *
 * For MVP testing - replace with database persistence later
 */
object CashoutRequestStorage {
    private val requests = mutableMapOf<String, CashoutRequest>()

    fun add(request: CashoutRequest) {
        requests[request.id] = request
    }

    fun get(id: String): CashoutRequest? {
        return requests[id]
    }

    fun getAll(): List<CashoutRequest> {
        return requests.values.sortedByDescending { it.timestamp }  // Newest first
    }

    fun getAllPending(): List<CashoutRequest> {
        return requests.values
            .filter { it.status != com.asgaya.husk.data.CashoutStatus.COMPLETED }
            .sortedByDescending { it.timestamp }  // Newest first (like Remittances tab)
    }

    fun update(request: CashoutRequest) {
        requests[request.id] = request
    }

    fun remove(id: String) {
        requests.remove(id)
    }

    fun clear() {
        requests.clear()
    }
}
