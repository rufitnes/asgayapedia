# Merchant Cashout Flow (Merchant-First): Selling BCH at a Merchant
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**Status:** 🟢 **Production-proven on-chain** — first transaction 2026-09-01 (TXID `05301369…`); **Nostr transport working end-to-end** 2026-09-16 (TXID `17cfed7f…`)
**Flow version:** Merchant-first (replaced recipient-first after architecture review, Aug 31 2026)

---

## What This Is

A recipient converts the BCH they hold into local currency at a merchant. From the merchant's side, the merchant **buys BCH** and pays fiat. The two parties co-sign one transaction that spends the recipient's covenant via the `merchantCashout()` path.

In Asgaya's language the **recipient sells BCH** and the **merchant buys BCH** — never "money transfer." This framing is deliberate (compliance): the two are trading an asset, not moving money.

---

## Why Merchant-First?

The original design had the **recipient pre-sign first**. Architecture review found this was wrong:

1. **Price volatility risk:** The recipient fetched the oracle price, then the merchant scanned it up to 60s later. With a 0.5% target spread, normal volatility wiped out the margin.
2. **MerchantPubkey discovery problem:** The recipient's signature commits to output 0 = the merchant's address. So the recipient had to know *which* merchant before approaching.
3. **Wrong hierarchy:** The merchant is top of the totem pole; they should control the timing and price.

**Merchant-first fixes all three:** the merchant fetches a fresh oracle when they quote, provides their own pubkey, and controls when to broadcast.

---

## The Flow

The three messages are **transport-agnostic**. **Nostr is the primary transport** (works remotely); a **two-way QR exchange is the offline fallback** (the recipient needs no connectivity). The same messages work over Telegram for testing.

### On Nostr (primary)

```
Recipient (Elena)                        Merchant (Carlos)
─────────────────                        ─────────────────
[BCH_FOR_SALE]  ────────────────────────►
   (covenant params, no signatures)      fetches FRESH oracle,
                                         builds + pre-signs
[BCH_PURCHASE_COSIGN] ◄──────────────────
   (partial tx, merchant pubkey,
    oracle data, quoteId, expiry)
   verifies freshness + floor, co-signs
[SIGNED_TX] ─────────────────────────────►
                                         broadcasts (BCH-first),
                                         then hands cash
```

1. **Recipient → Merchant: `[BCH_FOR_SALE]`.** Only the covenant parameters — no signature, no oracle, no merchant info. The recipient can send it to **any** merchant (no pre-commitment).
2. **Merchant → Recipient: `[BCH_PURCHASE_COSIGN]`.** The merchant fetches a **fresh oracle**, builds the transaction (output 0 = merchant, output 1 = funder) and **pre-signs**. The quote carries an **expiry** and a unique id, so a stale price can't be reused later and only the live quote is honoured.
3. **Recipient → Merchant: `[SIGNED_TX]`.** The recipient verifies and co-signs, then returns the fully-signed transaction.
4. **Merchant broadcasts, then hands cash.** The merchant taps a call to action; the app **never broadcasts on its own** — a person controls the moment.

**Liveness = response.** There is no heartbeat or presence system: the merchant's reply *is* proof it is online, and the recipient is online because it just sent the request. The exchange finalises in seconds.

### On QR (offline fallback)

The same three messages, shown as QR codes: recipient shows `[BCH_FOR_SALE]` → merchant shows `[BCH_PURCHASE_COSIGN]` → recipient shows `[SIGNED_TX]` → merchant broadcasts and hands cash. In this path the recipient needs **no connectivity at all**; only the merchant needs internet.

---

## Message Formats

`[BCH_FOR_SALE]`:

```
[BCH_FOR_SALE]
covenantAddress=bchtest:p...
senderPubkey=...
recipientPubkey=...
funderPubkey=...
oraclePubkey=...
eurCents=900
expiryOracleTime=...
initialBchPriceInCents=65000
minPricePercent=93
[/BCH_FOR_SALE]
```

`[BCH_PURCHASE_COSIGN]` carries the partial `transactionHex`, `merchantPubkey`, `merchantAddress`, `funderAddress`, the fresh `oracleSig`/`oracleMessage`, the price/amount display fields, and a `quoteId` + `expiresAt`. `[SIGNED_TX]` carries `covenantAddress`, `quoteId`, and the signed transaction hex.

> Legacy formats `[CASH_IN_PERSON]` / `[CASHOUT_REQUEST]` come from the earlier recipient-first flow and are kept only for backward compatibility.

---

## Why the Co-Sign Exchange Is a Round Trip

The recipient's signature (SIGHASH_ALL) commits to the **exact output amounts**, and those amounts depend on the oracle price (`paymentSats = eurCents × 1e8 / price`, covenant-enforced). So:

- The recipient **cannot pre-sign without the oracle** (they'd commit to wrong amounts)
- Only the merchant can fix the oracle fresh
- Therefore the recipient co-signs **after** the merchant builds

The exchange is structurally required — not a UX inefficiency, but the covenant working as designed.

---

## Transport Options

| Path | Recipient online? | Merchant online? | Use case |
|------|-------------------|------------------|----------|
| **Nostr** (primary) | Yes | Yes | Remote / low friction |
| **QR two-way** (offline fallback) | No | Yes | Face-to-face, offline recipient |
| **Telegram** | Yes | Yes | Testing / async fallback |

---

## Security Model

- **The merchant can't be cheated on outputs** — the covenant enforces output structure (`outputs[0] = paymentSats to merchantPubkey`, `outputs[1] = remainder to funder`).
- **The recipient can't be cheated on price** — they verify the fresh-price check before co-signing (the merchant cannot reuse a stale low-price oracle).
- **BCH-first + confirm-then-pay** — the merchant broadcasts (BCH moves) before handing cash. For **remote or larger amounts, wait for 1 confirmation**; until confirmation a competing spend could still win, so a 0-conf handover is a trust choice for small, in-person trades.
- **You can't trade with yourself** — the merchant board never offers your own ad as a counterparty (self-dealing would fake volume and poison reputation).
- **Signature extraction by position** — the unlocking-script order is fixed, so extracting signatures by position (not byte-length) is deterministic.

---

## Related Documents

- [Nostr Transport](./nostr.md) — the coordination transport used here
- [Bulletin Board](./bulletin-board.md) — how the two sides find each other
- [Covenant version history](../../implementation/covenants/version-history.md) — `merchantCashout()`
- [WebView Covenant Bridge](./webview-covenant-bridge.md) — the build/sign layer
- [Connection Management](./connection-management-patterns.md) — native TCP for broadcast
- [Funder Principle](../../why-this-design/constraints/funder-principle.md) — buffer → funder
- [Merchant Journey](../../user-journeys/merchant/README.md)

---

**Status:** Production-proven (on-chain); Nostr transport working end-to-end  
**Last Updated:** 2026-09-16
