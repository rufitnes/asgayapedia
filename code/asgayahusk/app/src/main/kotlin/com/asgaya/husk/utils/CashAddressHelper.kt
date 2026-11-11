package com.asgaya.husk.utils

import io.horizontalsystems.bitcoincore.transactions.scripts.ScriptType
import io.horizontalsystems.bitcoincore.utils.CashAddressConverter

/**
 * Helper class for Bitcoin Cash address encoding using bitcoin-kit-android library.
 *
 * Usage example for covenant P2SH32 addresses:
 * ```
 * val helper = CashAddressHelper()
 * val redeemScriptHash = // 32-byte SHA256 hash of covenant redeem script
 * val covenantAddress = helper.encodeP2SH32(redeemScriptHash)
 * ```
 *
 * This replaces the broken CashAddrEncoder with proven library implementation.
 */
class CashAddressHelper(
    private val hrp: String = "bitcoincash"  // Use "bchtest" for testnet
) {
    private val converter = CashAddressConverter(hrp)

    /**
     * Encode a 20-byte hash as P2SH address (standard Bitcoin Cash P2SH).
     *
     * @param hash20 20-byte HASH160 of redeem script
     * @return CashAddr formatted address (e.g., "bitcoincash:pr6m7j9njldwwzlg9v7v53unlr4jkmx6eyguug74nh")
     */
    fun encodeP2SH20(hash20: ByteArray): String {
        require(hash20.size == 20) { "P2SH20 requires 20-byte hash" }
        val address = converter.convert(hash20, ScriptType.P2SH)
        return address.stringValue
    }

    /**
     * Encode a 32-byte hash as P2SH32 address (used for covenant addresses).
     *
     * @param hash32 32-byte SHA256 of redeem script
     * @return CashAddr formatted address (e.g., "bitcoincash:pvch8mmxy0rtfrlarg7ucrxxfzds5pamg73h7370aa87d80gyhqxqaw3dsfwg")
     */
    fun encodeP2SH32(hash32: ByteArray): String {
        require(hash32.size == 32) { "P2SH32 requires 32-byte hash" }
        val address = converter.convert(hash32, ScriptType.P2SH)
        return address.stringValue
    }

    /**
     * Decode a CashAddr string to extract the raw hash bytes.
     *
     * @param addressString CashAddr formatted address
     * @return Pair of (hash bytes, is P2PKH)
     */
    fun decode(addressString: String): Pair<ByteArray, Boolean> {
        val address = converter.convert(addressString)
        val isP2PKH = address.scriptType == ScriptType.P2PKH
        return Pair(address.lockingScriptPayload, isP2PKH)
    }

    companion object {
        /**
         * Quick helper for mainnet P2SH32 encoding (most common use case).
         */
        fun encodeCovenantAddress(redeemScriptHash: ByteArray): String {
            return CashAddressHelper().encodeP2SH32(redeemScriptHash)
        }
    }
}
