# Merchant Cashout Flow (Merchant-First)
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**Status:** 🟢 **Production-proven** (first on-chain transaction 2026-09-01, TXID `05301369c518a8be60a3453cf6b09f048cdeae1a5925755c828c4f866a69f22`)
**Flow version:** Merchant-first (replaced recipient-first after architecture review, Aug 31 2026)

---

## Why Merchant-First?

The original design had the **recipient pre-sign first**. Architecture review found this was wrong:

1. **Price volatility risk:** The recipient fetched the oracle price, then the merchant scanned it up to 60s later. With a 0.5% target spread, normal volatility wiped out the margin.
2. **MerchantPubkey discovery problem:** The recipient's signature commits to output 0 = the merchant's address. So the recipient had to know *which* merchant before approaching — requiring a bulletin board lookup.
3. **Wrong hierarchy:** The merchant is top of the totem pole; they should control the timing and price.

**Merchant-first fixes all three:** the merchant fetches a fresh oracle at the counter (seconds old), provides their own pubkey, and controls when to broadcast.

---

## The Flow (4 Steps, ~60 seconds)

```
Recipient (Elena)                  Merchant (Carlos)
─────────────────                  ─────────────────
1. Shows cashout request QR        ← 2. Scans, fetches FRESH oracle, pre-signs,
   (just covenant params,            shows response QR
    no signatures)
                                    → 3. Scans, verifies 8 checks, co-signs,
                                       shows fully-signed tx QR
                                   ← 4. Scans, broadcasts (BCH-first),
                                       gives cash
```

### Step 1: Recipient Shows Request QR

Recipient generates a QR containing **only the covenant parameters** — no signature, no oracle, no merchant info:

```
[CASHOUT_REQUEST]
covenantAddress=bchtest:p...
senderPubkey=...
recipientPubkey=...
funderPubkey=...
oraclePubkey=...
eurCents=900
expiryOracleTime=...
initialBchPriceInCents=65000
minPricePercent=93
[/CASHOUT_REQUEST]
```

The recipient can show this to **any** merchant (no pre-commitment).

### Step 2: Merchant Fetches Fresh Oracle, Pre-Signs

The merchant's app:
1. Fetches a **fresh oracle** at the counter (price is seconds old)
2. Builds the transaction: output 0 = merchant's own address, output 1 = funder's address
3. **Pre-signs** with the merchant's WIF
4. Shows a response QR with the partial transaction + merchant pubkey + oracle data

**The merchant controls the price freshness — that's the security property.**

### Step 3: Recipient Verifies and Co-Signs

The recipient's app:
1. Verifies the **8 checks**: covenant match, fresh oracle (<60s), price floor, correct outputs, etc.
2. **Co-signs** the transaction with the recipient's WIF
3. Shows a fully-signed transaction QR

**The recipient must verify the fresh-price check** — otherwise the merchant could use a stale low-price oracle to extract more BCH.

### Step 4: Merchant Broadcasts, Gives Cash

The merchant scans the signed tx, **broadcasts immediately** (BCH-first — the refund race is blocked once broadcast), then hands the cash to the recipient.

---

## Why the Co-Sign Exchange Is a Round Trip

The recipient's signature (SIGHASH_ALL) commits to the **exact output amounts**, and those amounts depend on the oracle price (`paymentSats = eurCents × 1e8 / price`, covenant-enforced). So:

- The recipient **cannot pre-sign without the oracle** (they'd commit to wrong amounts)
- Only the merchant can fix the oracle fresh at the counter
- Therefore the recipient co-signs **after** the merchant builds

This makes the two-way QR exchange structurally required — not a UX inefficiency, but the covenant working as designed.

---

## Transport Options

| Path | Recipient online? | Merchant online? | Use case |
|------|-------------------|------------------|----------|
| **QR two-way** | No | Yes (oracle + broadcast) | Face-to-face, offline recipient |
| **Nostr** (Phase 1) | Yes | Yes | Remote / low friction |
| **Telegram** | Yes | Yes | Testing / async fallback |

**Key property:** in the QR path the recipient needs **no connectivity** (shows request QR → scans response QR → signs locally → returns). Only the merchant needs internet.

---

## Security Model

- **Merchant can't be cheated on outputs** — the covenant enforces output structure (`outputs[0] = paymentSats to merchantPubkey`, `outputs[1] = remainder to funder`)
- **Recipient can't be cheated on price** — they verify the fresh-price check before co-signing
- **BCH-first** — merchant broadcasts before handing cash; the sender's `refund()` is blocked once the UTXO is spent
- **Signature extraction by position** — the unlocking script order is fixed by the CashScript SDK, so extracting signatures by position (not byte-length) is deterministic

---

## Related Documents

- [Covenant v2.6.1: merchantCashout path](../../implementation/covenants/version-history.md)
- [WebView Covenant Bridge](./webview-covenant-bridge.md) (the JS build/sign layer)
- [Connection Management](./connection-management-patterns.md) (Kotlin TCP for broadcast, WebView compute-only)
- [Funder Principle](../../why-this-design/constraints/funder-principle.md) (buffer → funder)
- [Merchant Journey](../../user-journeys/merchant/README.md)

---

**Status:** Production-proven (on-chain, Sep 2026)  
**Last Updated:** 2026-09-01
