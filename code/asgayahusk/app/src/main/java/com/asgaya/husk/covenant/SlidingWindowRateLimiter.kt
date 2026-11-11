package com.asgaya.husk.covenant

/**
 * Per-key sliding-window rate limiter for the merchant auto-quote path.
 *
 * File 60 §5: the auto-quote budget must be **per-sender** (keyed on the authenticated npub),
 * not global — otherwise a single sender can starve every other customer (a DoS).
 *
 * Pure and JVM-testable. Thread-safe via `synchronized`.
 */
class SlidingWindowRateLimiter(
    private val maxPerWindow: Int,
    private val windowMs: Long,
    private val maxKeys: Int = 512
) {
    private val hits = HashMap<String, ArrayDeque<Long>>()

    /** True if [key] may act now; records the hit when allowed. */
    @Synchronized
    fun allow(key: String, now: Long): Boolean {
        val q = hits.getOrPut(key) { ArrayDeque() }
        while (q.isNotEmpty() && now - q.first() > windowMs) q.removeFirst()
        if (q.size >= maxPerWindow) return false
        q.addLast(now)

        // Opportunistic prune so the map can't grow without bound.
        if (hits.size > maxKeys) {
            hits.entries.removeAll { (_, v) -> v.isEmpty() || now - v.last() > windowMs }
        }
        return true
    }
}
