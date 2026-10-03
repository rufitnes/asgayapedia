# Payment Timing (per method) — How long does a payment actually take?

**Status:** Not Started  
**Priority:** High  
**Last Updated:** 2026-09-27  
**Contributors Welcome:** Yes

---

## What We Don't Know

For each payment method, **two latencies** that together bound how long a seller must wait for a
payment before it is safe to consider the request dead:

1. **Sender → payment:** from seeing the payment instructions to actually **completing** the
   payment (opening the bank app, entering details, confirming).
2. **Payment → notification:** from the payment landing to the **seller's** notification arriving
   (the bank app's push/SMS that lets the seller auto-fund the covenant).

Both vary: e.g. a bank notification may arrive **immediately**, or be **delayed by minutes**.

**For cash in person** the two collapse into one: the time for the sender to physically hand over
the cash (a plausible default is **~5 minutes**).

**For Bizum / SEPA / other rails** we have **no data**.

## Why It Matters

The **covenant fund request** needs a **pay-by window** per method. Too short → legitimate payments
get cancelled while in flight. Too long → a pending request blocks a reference (we allow **one
active covenant per CashAccount**). The window must be **method-specific**, not a global constant.

> **Where this is used:** the **pay-by window** on the [fund-request lifecycle](../../the-mechanism/fund-request-lifecycle.md). Cash in person currently defaults to **5 minutes**.

## Current Hypothesis

- **Cash in person:** ~5 minutes is enough.
- **Bizum:** sender ~30-60 s; the bank notification is the risky part — minutes, variable.
- **SEPA / others:** unknown, likely hours (different rails entirely).

## Investigation Method

1. Instrument the client to log, per payment: `request → marked sent` and `marked sent → seller
   notification received`.
2. Collect real samples per method and region; compute **P50 / P90 / P99**.
3. Correlate with bank / carrier / OS to see the variance drivers.
4. Derive a **per-method pay-by window** (with safety margin) and re-measure.

## Success Criterion

Per-method latency distributions (P50/P90/P99) for both legs, with a justified **pay-by window** per
payment method.

## Phase 0 Trial Integration

Log both timings (request→sent, sent→notification) on every payment, tagged by method.

## Phase 0 as-shipped (2026-10-02)

- Cash in person ships with a **5-minute** default pay-by window, carried on the `[FUND_COVENANT]` message (`payWindowSeconds`).
- A pending request **blocks its reference** for the window (one active fund request per reference).
- The 5-minute value is a **guess** until the samples above exist — revisit per method.

## Contributor Guidance

**Skills needed:** client instrumentation, access to testers across banks/regions  
**Estimated effort:** 4-8 hours (instrumentation) + ongoing data collection  
**How to start:** add the two timestamps to the payment flow and a small stats dump

## Related Documents

- [Payment Info Exchange](../payment-info-exchange.md)
- [Notification Bot](../../the-mechanism/notification-bot/README.md)

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
