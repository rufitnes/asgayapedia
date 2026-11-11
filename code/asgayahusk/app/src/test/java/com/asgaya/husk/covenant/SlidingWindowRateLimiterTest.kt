package com.asgaya.husk.covenant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for the per-sender auto-quote rate limiter (file 60 §5). */
class SlidingWindowRateLimiterTest {

    private val t0 = 1_700_000_000_000L

    @Test
    fun allowsUpToTheLimit() {
        val rl = SlidingWindowRateLimiter(maxPerWindow = 3, windowMs = 60_000)
        assertTrue(rl.allow("alice", t0))
        assertTrue(rl.allow("alice", t0 + 1))
        assertTrue(rl.allow("alice", t0 + 2))
    }

    @Test
    fun blocksBeyondTheLimit() {
        val rl = SlidingWindowRateLimiter(maxPerWindow = 3, windowMs = 60_000)
        repeat(3) { rl.allow("alice", t0 + it) }
        assertFalse(rl.allow("alice", t0 + 10))
    }

    @Test
    fun allowsAgainAfterTheWindowSlides() {
        val rl = SlidingWindowRateLimiter(maxPerWindow = 2, windowMs = 60_000)
        assertTrue(rl.allow("alice", t0))
        assertTrue(rl.allow("alice", t0 + 1))
        assertFalse(rl.allow("alice", t0 + 2))
        // First hit ages out at t0 + window; a fresh slot opens.
        assertTrue(rl.allow("alice", t0 + 60_001))
    }

    @Test
    fun budgetIsIndependentPerKey() {
        val rl = SlidingWindowRateLimiter(maxPerWindow = 1, windowMs = 60_000)
        assertTrue(rl.allow("alice", t0))
        assertFalse(rl.allow("alice", t0 + 1))
        // A different sender must not be starved by alice.
        assertTrue(rl.allow("bob", t0 + 1))
    }

    @Test
    fun prunesWhenManyKeys() {
        val rl = SlidingWindowRateLimiter(maxPerWindow = 5, windowMs = 60_000, maxKeys = 8)
        for (i in 0 until 50) rl.allow("sender$i", t0 + i)
        // Still functional after pruning.
        assertTrue(rl.allow("fresh", t0 + 100))
    }
}
