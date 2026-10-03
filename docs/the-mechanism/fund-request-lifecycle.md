# The Fund-Request Lifecycle

**How a covenant gets funded — and how it ends.** A [payment-first covenant](../glossary.md#payment-first-covenant) is funded only *after* the payment, so a remittance moves through two steps: the **request** (the sender asks a seller to fund) and the **funding** (the seller locks BCH once paid). This page follows one remittance through that lifecycle.

**Status:** Phase 0 (testnet). The seller checks below are **client-side** for testing; a covenant change — an explicit `floor` plus an on-chain collateral check — is scheduled for **v0, before Phase 0 launch** (see [What is not yet enforced](#what-is-not-yet-enforced)).

---

## Why a request, not a funded covenant

Under the payment-first model the sender must not lock BCH *before* paying fiat. So the sender creates a covenant that is **unfunded (awaiting payment)**, and a **BCH seller funds it** once the fiat payment is confirmed. Until then the covenant carries no money — the request is an offer with agreed terms.

The request's **reference** is the **recipient's Cash Account** (`Elena#142`, or the destination address if there is none). It is human-readable *and* it is the **payment concept** the seller's notification bot uses to match a bank notification to the right covenant (see the caveat under [Scale](#scale-a-known-refinement)).

## The lifecycle

1. **Sender creates** the covenant (awaiting payment) with the agreed terms and an `initialBchPriceInCents` (see [The floor](#the-floor)).
2. **Sender returns home** to a **card** for the new remittance and taps **Request funding** → a `[FUND_COVENANT]` message goes to the chosen seller over [Nostr](nostr-coordination/README.md) (a NIP-17 DM). It carries the reference, the seller's **ad id**, fee, buffer, the **pay-by window**, and the sender's `initial` price. *(The card is the sender's single place to watch a remittance move: requesting → funded → claimed/refunded.)*
3. **Seller runs its checklist** (below). If it passes and the payment is confirmed, the seller **funds** the covenant — it now holds BCH plus the seller's buffer.
4. **Recipient claims**, or the covenant **expires** (8 h) — see [Resolution](#resolution-the-chain-is-the-truth).

## The seller's checklist (client-side, Phase 0)

Before funding, the seller's app checks:

- **A unique request id** — the **reference** (the recipient's Cash Account; recipient address as fallback). The seller allows **one active fund request per reference**, so a second request cannot create ambiguity. *(No separate id field.)*
- **The ad still matches** — the request's oracle pubkey, fee, buffer, currency/corridor and payment method must match the ad the seller quoted (guards ad drift).
- **The price is still above the floor** — funding happens only if

  `current ≥ initial × (1 − 1.5 %)`.

  A **rise** is free (it only makes the buffer larger). A **fall** of more than ~1.5 % since creation means the request is already doomed → the seller **does not fund** and the sender recreates.

> These checks stop a seller from funding a request that was set up to fail. They are **client-side** for testing — a buggy or hostile client could bypass them — so they are not "the guarantee"; the v0 covenant makes the important ones **on-chain** (below).

## The pay-by window (per method)

The sender has a **pay-by window** to complete the payment. Past the window the request is **cancelled — but not deleted**: the sender may simply have forgotten to mark it sent, so the seller keeps a window to **rescue** the request.

The window is **method-specific**, not a global constant:

- **Cash in person: 5 minutes** (Phase 0 default).
- **Bizum / SEPA / other rails:** to be measured — this is an open question, [`payment-timing-per-method`](../unknowns/technical/payment-timing-per-method.md).

One pending request **blocks the reference** for its window — a bounded wait, fine for Phase 0.

### Scale: a known refinement

The reference (the recipient's Cash Account) is **unique per recipient, not per request**, so two senders paying the same recipient **serialize** and share the same concept. Making the reference unique **per request** (e.g. a short nonce in the concept) is a durability refinement for later phases.

## Resolution: the chain is the truth

A covenant ends in one of five ways: `claim`, `merchantCashout`, `refund`, `abort`, `sellerRecoverBuffer`. When it ends, the actor tells the counterparties over Nostr with a **`[COVENANT_RESOLVED]`** message (`covenantAddress`, `path`, `txid`). That message is a **hint, not the truth**: the receiver **verifies on-chain** (the covenant is spent, the tx exists) before concluding. The **spending transaction is the source of truth**; the DM only triggers a timely look.

At **expiry (8 h)** the **funder automatically recovers its buffer** (`sellerRecoverBuffer`: an exact alarm, a catch-up on app/boot start, and a short retry). The **sender's `refund()` is manual** (available any time before a claim). **No stability tokens are minted at expiry** — minting happens only on `merchantCashout` and on `abort` (>7 % drop). See [Auto-Refund UX](../user-journeys/remittance/sender/auto-refund-ux.md) and [Funder Principle](../why-this-design/constraints/funder-principle.md).

## The seller's / merchant's card

The seller's app groups its remittances into three:

- **Funding requests** — awaiting payment, not yet expired.
- **Active covenants** — the ones this device funded, with a countdown to expiry and a manual **🛡️ Recover buffer** as a last resort (the automated recovery usually wins the race).
- **History** — concluded: `🛡️ Buffer recovered`, `✅ Settled (on-chain)`, `✅ Claimed`, with the txid.

Statuses surface as `✅ Claimed` / `✅ Refunded` / `⏱️ Expired` / `✅ Settled`.

## The floor

`initialBchPriceInCents` is the **floor reference, not the sale price**. The seller **sells BCH in advance** — it holds the fiat first and delivers BCH at claim — so its **sale price is the *resolution* price**, and a stale `initial` shifts only **where the floor sits**, not the seller's economics. The seller sizes its buffer (and, in v0, an explicit `floor` param) to leave headroom above the expected funding price. See [Value-Guaranteed Delivery](../why-this-design/constraints/7%-volatility-buffer-value-guaranteed-delivery.md).

## What is not yet enforced

Honest scope, so the guarantees are not overstated:

- The seller checks above are **client-side**, not covenant-enforced, in this slice.
- **v0 covenant (before Phase 0):** an explicit **`floor`** constructor param, and an **on-chain collateral check** (`V ≥ eurCents × 1e8 / floor`) so the funded collateral always covers the euro amount down to the floor.
- **All three `[COVENANT_RESOLVED]` directions are emitted** — refund/abort → funder; claim and `sellerRecoverBuffer` → sender. The **npub sources are best-effort** (the listing board, contacts, DMs), and the receiver **always verifies on-chain** — the message is a hint, never the truth.
- The app is **Phase 0 / testnet**; the price oracle is currently LAN-bound.

---

**Navigation**

**[🏠 Home](../index.md)** | **[↑ The Mechanism](README.md)** | **[📖 Glossary](../glossary.md)**

**Related:** [How They Interact](how-they-interact.md) · [Nostr Coordination](nostr-coordination/README.md) · [Payment Timing (per method)](../unknowns/technical/payment-timing-per-method.md) · [Sender Journey](../user-journeys/remittance/sender/README.md) · [Merchant Journey](../user-journeys/merchant/README.md)
