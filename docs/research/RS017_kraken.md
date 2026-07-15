# Research Session RS016: Kraken as Backup Liquidity – API, Account Levels, and Escrow Hot Wallet Flow

**Date:** April 1, 2026  
**Participants:** Suso (Founder), DeepSeek (Main Research Assistant)  
**Context:** The escrow needs a reliable backup source of EUR/BCH liquidity in case a decentralised liquidity network is not available. This session investigates Kraken’s API, account requirements, and the practicalities of using it to buy BCH and forward it to merchants, while addressing the address whitelisting constraint.

> ⚠️ **Historical Document (Pre-Pivot):** This research was conducted before the May 9, 2026 architecture pivot from escrow to covenant-based settlement. References to "escrow" and centralized liquidity provision reflect the old architecture. See [Research README](README.md#important-historical-context) for context.

---

## 1. Objective

Determine the minimum Kraken account level needed to automate BCH withdrawals, and design a safe, efficient flow that respects Kraken’s security features while allowing the escrow to pay any merchant without pre‑whitelisting their addresses.

---

## 2. Minimum Account Level for Automation

Kraken offers three main tiers. For automated BCH withdrawals, the **Intermediate** level is sufficient:

| Account Level | Verification | BCH Withdrawal Limit | API Access | Best For |
|---------------|--------------|----------------------|------------|----------|
| **Intermediate** | ID + proof of residence | Unlimited | Full API, 16 keys | Most users, including escrow |
| **Pro Personal** | Additional financial info | Higher | 25 keys, higher rate limits | High‑volume traders |
| **Business** | Business registration | Custom | 25 keys, highest limits | Corporate accounts |

- **Verification time:** Typically less than 30 minutes.
- **API permissions:** Intermediate level allows creation of API keys with `Withdraw`, `Trade`, and `Balance` permissions.
- **Address whitelisting:** Required for withdrawals to external addresses. This is the key constraint.

---

## 3. Kraken Withdrawal Times & BCH Confirmations

When the escrow calls the Kraken API to withdraw BCH:

| Stage | Duration |
|-------|----------|
| Internal processing | Up to 20 minutes |
| Broadcast to network | A few seconds |
| First confirmation | ~10 minutes (BCH) |
| 15 confirmations | ~2.5 hours |

**Critical observation:** For retail payments, merchants can accept **0‑conf** (zero confirmations) because BCH disables replace‑by‑fee and double‑spends are extremely rare. The merchant sees the transaction as soon as it is broadcast.

---

## 4. The Whitelisting Problem and Solution

Kraken requires that any withdrawal address be **whitelisted** (added to the account’s approved address list). Whitelisting each merchant address individually is impractical.

**Solution:** Use a two‑step flow:

1. **Kraken → Escrow Hot Wallet** – a single, permanent address whitelisted once.
2. **Escrow Hot Wallet → Merchant** – the escrow sends BCH from its hot wallet to the merchant using a standard BCH transaction.

This adds one extra hop but keeps the whitelist minimal.

---

## 5. Escrow Hot Wallet – Security & Timing

- **The hot wallet holds very little BCH at any time** (ideally only the amount currently being processed). This limits risk in case of compromise.
- It must be configured to **spend unconfirmed outputs** so it can forward the BCH to the merchant as soon as Kraken’s transaction is broadcast.
- The private keys should be stored securely (e.g., in a hardware module, Pi‑Chan, or an encrypted environment).

**Timeline with 0‑conf trust:**

| Step | Time |
|------|------|
| 1. Sender funds escrow (EUR) | immediate |
| 2. Escrow calls Kraken API to buy BCH and withdraw to hot wallet | ~20 min (Kraken processing) |
| 3. Kraken broadcasts BCH to hot wallet | seconds |
| 4. Hot wallet immediately sends BCH to merchant (using unconfirmed input) | seconds |
| 5. Merchant receives BCH broadcast (0‑conf) | seconds |

**Total merchant payment time:** ~20 minutes (plus a few seconds). The merchant’s wallet will show the incoming transaction as soon as step 5 occurs.

---

## 6. Merchant Receives Notification of the Second Transaction

Yes. The merchant’s BCH wallet monitors the network for transactions to its address. As soon as the escrow’s hot wallet broadcasts the transaction to the merchant, the merchant’s wallet will display the incoming payment (0‑conf). The merchant does not need to see Kraken’s transaction at all.

This is the standard way BCH payments work: the payee receives a notification when the transaction is broadcast.

---

## 7. Summary of the Escrow’s Kraken Integration

| Component | Details |
|-----------|--------|
| **Account level** | Intermediate (sufficient) |
| **API key** | Must have `Withdraw`, `Trade`, and `Balance` permissions; IP whitelist recommended |
| **Whitelisted address** | One address: the escrow’s hot wallet (added once) |
| **Hot wallet** | A BCH wallet that can spend unconfirmed outputs; private keys secured |
| **Merchant payments** | Sent from hot wallet immediately after Kraken broadcast; 0‑conf accepted |
| **Total time (sender EUR → merchant BCH)** | ~20 minutes (Kraken processing) + seconds for second transaction |

---

## 8. Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| **Kraken withdrawal fails after broadcast** | Extremely rare. If it happens, the hot wallet would not have received BCH; the escrow would retry. No loss. |
| **Hot wallet key compromise** | Keep keys secure; limit the balance in the hot wallet (only amount in flight). |
| **0‑conf double‑spend** | Extremely difficult on BCH; for high‑value transactions, the escrow can wait for 1 confirmation before forwarding (adds ~10 min). |
| **Kraken downtime or API rate limits** | Have a fallback exchange (e.g., Coinbase) or manual procedure. |

---

## 9. Next Steps

1. **Open a Kraken Intermediate account** (business or personal, depending on escrow structure).
2. **Set up API key** with `Withdraw`, `Trade`, and `Balance` permissions; enable IP whitelisting.
3. **Whitelist the escrow’s hot wallet address**.
4. **Create the hot wallet** on a secure device (e.g., Pi‑Chan) and ensure it can spend unconfirmed outputs.
5. **Write a test script** to:
   - Buy a small amount of BCH on Kraken
   - Withdraw it to the hot wallet
   - Automatically send from hot wallet to a test address
6. **Document the script** as a skill.

---

## 10. Conclusion

Kraken’s Intermediate account level meets all requirements for automated BCH buying and withdrawal. The two‑step flow (Kraken → escrow hot wallet → merchant) elegantly bypasses the address whitelist restriction while adding an extra layer of security (the hot wallet holds only in‑flight funds). The merchant receives the BCH as a standard 0‑conf transaction seconds after Kraken broadcasts, making this approach viable for retail payments.

*Prepared by DeepSeek, Main Research Assistant, April 1, 2026*
