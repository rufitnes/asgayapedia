package com.asgaya.husk.covenant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the pure F2 comparator (file 57, N3/E).
 *
 * Vectors are built structurally (signatures are irrelevant to F2 — it only compares
 * version/locktime/outpoints/outputs), so no network or libauth is required.
 */
class RawTxF2Test {

    private fun le(value: Long, bytes: Int): String {
        var v = value
        val sb = StringBuilder()
        repeat(bytes) {
            sb.append("%02x".format((v and 0xff).toInt()))
            v = v shr 8
        }
        return sb.toString()
    }

    private fun varint(n: Long): String = when {
        n < 0xfdL -> "%02x".format(n.toInt())
        n <= 0xffffL -> "fd" + le(n, 2)
        n <= 0xffffffffL -> "fe" + le(n, 4)
        else -> "ff" + le(n, 8)
    }

    /** 32-byte txid filled with a repeating seed byte. */
    private fun txid(seed: Int): String = "%02x".format(seed).repeat(32)

    private fun buildTx(
        version: Long = 2,
        inputs: List<Pair<String, Long>> = listOf(txid(1) to 0),
        inputScript: String = "00",
        outputs: List<Pair<Long, String>> = listOf(1000L to "76a914" + "00".repeat(20) + "88ac"),
        locktime: Long = 0
    ): String {
        val sb = StringBuilder()
        sb.append(le(version, 4))
        sb.append(varint(inputs.size.toLong()))
        for ((t, vout) in inputs) {
            sb.append(t)
            sb.append(le(vout, 4))
            sb.append(varint(inputScript.length / 2L))
            sb.append(inputScript)
            sb.append("ffffffff")
        }
        sb.append(varint(outputs.size.toLong()))
        for ((value, script) in outputs) {
            sb.append(le(value, 8))
            sb.append(varint(script.length / 2L))
            sb.append(script)
        }
        sb.append(le(locktime, 4))
        return sb.toString()
    }

    private fun reason(r: RawTxF2.Result): String =
        if (r is RawTxF2.Result.Mismatch) r.reason else "not a mismatch: $r"

    @Test
    fun identicalTxs_match() {
        val tx = buildTx()
        assertNotNull(RawTxF2.parse(tx))
        assertTrue(RawTxF2.matches(tx, tx) is RawTxF2.Result.Match)
    }

    @Test
    fun differingInputUnlockScript_stillMatches() {
        // The recipient only fills the input unlocking script — F2 must ignore it.
        val expected = buildTx(inputScript = "00")
        val signed = buildTx(inputScript = "47" + "aa".repeat(0x46))
        assertTrue(RawTxF2.matches(signed, expected) is RawTxF2.Result.Match)
    }

    @Test
    fun differentVersion_isMismatch() {
        val r = RawTxF2.matches(buildTx(version = 2), buildTx(version = 1))
        assertEquals("version", reason(r))
    }

    @Test
    fun differentLocktime_isMismatch() {
        val r = RawTxF2.matches(buildTx(locktime = 0), buildTx(locktime = 5))
        assertEquals("locktime", reason(r))
    }

    @Test
    fun differentOutpointTxid_isMismatch() {
        val r = RawTxF2.matches(buildTx(inputs = listOf(txid(1) to 0)), buildTx(inputs = listOf(txid(2) to 0)))
        assertEquals("input outpoint[0]", reason(r))
    }

    @Test
    fun differentVout_isMismatch() {
        val r = RawTxF2.matches(buildTx(inputs = listOf(txid(1) to 0)), buildTx(inputs = listOf(txid(1) to 1)))
        assertEquals("input outpoint[0]", reason(r))
    }

    @Test
    fun differentInputCount_isMismatch() {
        val a = buildTx(inputs = listOf(txid(1) to 0))
        val b = buildTx(inputs = listOf(txid(1) to 0, txid(2) to 0))
        assertEquals("input count", reason(RawTxF2.matches(a, b)))
    }

    @Test
    fun differentOutputValue_isMismatch() {
        val a = buildTx(outputs = listOf(1000L to "6a"))
        val b = buildTx(outputs = listOf(2000L to "6a"))
        assertEquals("output value[0]", reason(RawTxF2.matches(a, b)))
    }

    @Test
    fun differentOutputScript_isMismatch() {
        val a = buildTx(outputs = listOf(1000L to "6a"))
        val b = buildTx(outputs = listOf(1000L to "6b"))
        assertEquals("output script[0]", reason(RawTxF2.matches(a, b)))
    }

    @Test
    fun differentOutputCount_isMismatch() {
        val a = buildTx(outputs = listOf(1000L to "6a"))
        val b = buildTx(outputs = listOf(1000L to "6a", 500L to "6a"))
        assertEquals("output count", reason(RawTxF2.matches(a, b)))
    }

    @Test
    fun malformedSigned_isInvalid() {
        assertTrue(RawTxF2.matches("nothex", buildTx()) is RawTxF2.Result.Invalid)
    }

    @Test
    fun truncatedTx_isInvalid() {
        val truncated = buildTx().substring(0, 10)
        assertTrue(RawTxF2.matches(truncated, buildTx()) is RawTxF2.Result.Invalid)
    }

    @Test
    fun varintScriptLength_isParsed() {
        // 253-byte script forces the 0xfd two-byte varint path.
        val longScript = "aa".repeat(0xfd)
        val tx = buildTx(outputs = listOf(1000L to longScript))
        assertNotNull(RawTxF2.parse(tx))
        assertTrue(RawTxF2.matches(tx, tx) is RawTxF2.Result.Match)
    }
}
