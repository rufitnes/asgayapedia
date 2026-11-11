package com.asgaya.husk.covenant

/**
 * F2: structural comparison of a recipient-signed cash-out tx against the merchant's pre-signed tx.
 *
 * The recipient only adds its unlocking signature; everything else must be byte-identical:
 * version, locktime, every input outpoint (txid + vout) and every output (value + script).
 * The input unlocking script is intentionally IGNORED — that is the field the recipient fills.
 *
 * Pure Kotlin (no Android, no WebView) so it can be unit-tested in the JVM and reused on any
 * device. Supersedes the previous libauth/WebView comparison (file 57, N3/E).
 */
object RawTxF2 {

    sealed class Result {
        /** Same inputs/outputs/version/locktime. */
        object Match : Result()

        /** Structurally valid tx but a field differs from the expectation. */
        data class Mismatch(val reason: String) : Result()

        /** One of the txs could not be decoded. */
        data class Invalid(val reason: String) : Result()
    }

    internal data class Tx(
        val version: String,
        val inputs: List<Pair<String, String>>,  // txid(32 bytes) → vout(4 bytes), raw LE hex
        val outputs: List<Pair<String, String>>, // value(8 bytes) → script, raw LE hex
        val locktime: String
    )

    /** Compare the returning signed tx to the merchant's pre-signed tx. */
    fun matches(signedTxHex: String, expectedTxHex: String): Result {
        val signed = parse(signedTxHex) ?: return Result.Invalid("signed tx decode failed")
        val expected = parse(expectedTxHex) ?: return Result.Invalid("expected tx decode failed")

        if (!signed.version.equals(expected.version, ignoreCase = true)) return Result.Mismatch("version")
        if (!signed.locktime.equals(expected.locktime, ignoreCase = true)) return Result.Mismatch("locktime")

        if (signed.inputs.size != expected.inputs.size) return Result.Mismatch("input count")
        signed.inputs.forEachIndexed { i, a ->
            val b = expected.inputs[i]
            if (!a.first.equals(b.first, ignoreCase = true) || !a.second.equals(b.second, ignoreCase = true)) {
                return Result.Mismatch("input outpoint[$i]")
            }
        }

        if (signed.outputs.size != expected.outputs.size) return Result.Mismatch("output count")
        signed.outputs.forEachIndexed { i, a ->
            val b = expected.outputs[i]
            if (!a.first.equals(b.first, ignoreCase = true)) return Result.Mismatch("output value[$i]")
            if (!a.second.equals(b.second, ignoreCase = true)) return Result.Mismatch("output script[$i]")
        }

        return Result.Match
    }

    /** Decode a raw BCH transaction into the fields F2 compares. Returns null if malformed. */
    internal fun parse(hex: String): Tx? = try {
        val c = Cursor(hex)
        val version = c.readHex(4)

        val nIn = c.readVarint()
        require(nIn in 1..MAX_INPUTS) { "input count" }
        val inputs = ArrayList<Pair<String, String>>(nIn.toInt())
        repeat(nIn.toInt()) {
            val txid = c.readHex(32)
            val vout = c.readHex(4)
            c.skip(c.readVarint())   // unlocking script — not compared
            c.skip(4)                // sequence
            inputs.add(txid to vout)
        }

        val nOut = c.readVarint()
        require(nOut in 0..MAX_OUTPUTS) { "output count" }
        val outputs = ArrayList<Pair<String, String>>(nOut.toInt())
        repeat(nOut.toInt()) {
            val value = c.readHex(8)
            val script = c.readHex(c.readVarint())
            outputs.add(value to script)
        }

        val locktime = c.readHex(4)
        Tx(version, inputs, outputs, locktime)
    } catch (e: Exception) {
        null
    }

    private const val MAX_INPUTS = 100_000L
    private const val MAX_OUTPUTS = 100_000L

    /** Minimal hex cursor (raw byte order — F2 compares bytes, never interprets endianness). */
    private class Cursor(private val hex: String) {
        private var pos = 0  // in hex chars

        fun readHex(bytes: Long): String {
            require(bytes >= 0) { "negative length" }
            val chars = (bytes * 2).toInt()
            val end = pos + chars
            require(end <= hex.length) { "unexpected end of tx" }
            val s = hex.substring(pos, end)
            require(s.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) { "non-hex byte" }
            pos = end
            return s.lowercase()
        }

        fun skip(bytes: Long) { readHex(bytes) }

        fun readVarint(): Long {
            val first = readHex(1).toInt(16)
            return when {
                first < 0xfd -> first.toLong()
                first == 0xfd -> readUIntLe(2)
                first == 0xfe -> readUIntLe(4)
                else -> readUIntLe(8)
            }
        }

        private fun readUIntLe(n: Int): Long {
            val bytes = readHex(n.toLong())
            var v = 0L
            for (i in n - 1 downTo 0) {
                v = (v shl 8) or bytes.substring(i * 2, i * 2 + 2).toLong(16)
            }
            return v
        }
    }
}
