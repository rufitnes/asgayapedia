# Merchant Journey: The Passive BCH Buyer (Triple-Dip Economics)
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**Role:** BCH Buyer (Passive) + Product Seller — **the star of Asgaya**  
**Example:** Carlos in Caracas (Venezuela) running a grocery store

> **Honest note — the roles are interchangeable.** A merchant, a **trader**, and a passive **seller** are the *same liquidity provider* seen from different seats, and the **app unifies them**. A merchant who takes BCH can also **sell** it (fund a covenant) and **replenish** it on the board. We keep this as a **dedicated merchant journey** because the merchant is the point of the whole thing — but read the [Trader Journey](../trader/README.md) for the other seats.

> **The merchant's edge.** A merchant is a **physical establishment** anyone can walk into. Part of reputation comes from that: a merchant with a **bad reputation has more to lose** — customers, the shop, the badge. That makes a merchant's reliability a **stronger anchor** than an anonymous online counterparty, and it is why Asgaya funnels **customers and fees toward merchants**.

---

## Overview

Carlos is not just a BCH buyer — he's a **triple-dip profit machine**:

1. **0.5% spread** on every BCH purchase from a recipient
2. **15-30% margin** on grocery sales (the recipient spends cash at his store)
3. **Stable value** held in **H€ / HAu** (a hedge against VES inflation, once the stability layer is available)

**Mode:** Passive — posts a listing once, the app handles the rest.  
**Economic insight:** recipients need cash, Carlos needs BCH. Perfect match.

---

## The Triple-Dip: Why Carlos Wins

### Elena cashes out €100 at Carlos's store

| Revenue Source | Amount | How It Works |
|---------------|--------|--------------|
| **1. Spread** | €0.50 (0.5%) | Carlos buys €100 of BCH for €99.50 |
| **2. Product margin** | €15-42 (15-30%) | Elena spends cash on groceries — the reason she came |
| **3. Stable value** | Variable | Carlos keeps the BCH, or stabilises as H€/HAu (vs ±40%/mo VES inflation) |

**Illustrative total on a €100 transaction:** €15.50 - €42.50 — *while running his normal business.* Even 1-2 transactions a day is meaningful in a high-inflation economy.

---

## One-Time Setup (5 minutes)

1. **Install & create identity** — Asgaya wallet + a Cash Account (`Carlos#487` — the payment reference).
2. **Post a listing** (bulletin board, over Nostr): asset, payment method (cash in person for Phase 0), fee, limits, location, hours.
3. **Set your float** — you **don't need to hold much BCH** (see *Safety*). A small float plus on-demand replenishment is the design, not a limitation.
4. **Set accept/reject preferences** — e.g. a daily cap; you can decline a request when your VES register is low.

**Setup complete. Now Carlos waits.**

---

## Safety: hold little, stay boring

**Payment-first is minimal capital deployment.** Because fiat moves before BCH does, your **operational wallet only needs enough BCH for one or two concurrent transactions** — then you replenish. That isn't a constraint; it's a **security feature**:

- **A linkable hot wallet with big numbers is a target.** A bad actor can correlate on-chain amounts with your **bulletin-board listing** (and your shop's location/hours) and threaten you to hand over the funds or the keys. A wallet holding **€200** is not the same target as **€2,000 or €20,000**.
- **Keep savings separate** — cold storage, or **H€/HAu** — *not* in the operational wallet that's linked to your board identity.
- **Be honest about what this does and doesn't change:** your **cash register** is still the bigger target of a robbery; the point is that **holding BCH adds little** on top of the risk a shop already carries. The mitigation is *minimise the balance at rest*, not "custody perfectly."
- **If you're short at a request, decline gracefully** — tell the sender you can't cover it right now and they pick another seller. You don't stall; and you don't over-promise capacity you can't meet.

> **Keep the float, keep your reputation.** Because a merchant's reputation is anchored to the **establishment**, the safe behaviour (small float, honest declines) *also* protects the thing you can't replace — the shop's name.

---

## Daily Operations

**Morning.** Elena receives a remittance, queries the board, finds Carlos (nearby, cash in person, tracked reputation), and heads to the shop. She can coordinate over **Nostr** first, or just walk in and quote face-to-face via QR.

**At the store.** Elena shops first, cashes out at checkout.

**The transaction (merchant-first):**
- Elena sends her **BCH-for-sale request** (over Nostr, or a QR if she has no data).
- Carlos's app fetches a **fresh price** (seconds old — this protects his 0.5% margin) and **pre-signs** the transaction.
- Elena reviews the checks (fresh price, covenant, outputs), co-signs, and sends it.
- Carlos **broadcasts immediately** → **hands Elena the cash** (0-conf for small amounts).
- **The merchant controls the timing and the freshness** — the point of merchant-first.

---

## Why This Works: The Remittance-to-Retail Loop

**Before Asgaya:** Elena picks up cash at Western Union (€5 fee, a trip), *then* travels to a store — and the merchant never sees the remittance.

**With Asgaya:** Elena goes straight to Carlos's store, shops, and cashes out at checkout. **Carlos captures both the remittance liquidity and the retail sale.** Remittance recipients are grocery customers; one location serves both needs.

---

## The Economics (illustrative)

| Scenario | Transactions/day | Spread (0.5%) | Grocery margin | Total/day |
|----------|------------------|---------------|----------------|-----------|
| Conservative | 5 × €100 | €2.50 | ~€105 (15%, half shop) | ~€107 |
| Optimistic | 10 × €100 | €5 | ~€375 (25%, most shop) | ~€380 |

**Reality check:** the value is **purchasing power preserved** — even one or two cash-outs a day is attractive where the local currency loses value fast. *(Held value is stabilised in H€/HAu when available; see below — the point is not to amass BCH in the hot wallet.)*

---

## The Stability Problem: BCH Volatility

**Carlos's concern:** "I receive BCH today; rent is due in two weeks. What if BCH drops 20%?"

**Solution (Phase 0+): the stability layer.** When Carlos takes BCH, his wallet offers:

- **Keep BCH** — accept the volatility (long-term bet).
- **Convert to H€** (Euro-pegged) — stable value, auto-renewing; falls back to BCH if the pool is at capacity.
- **Convert to HAu** (gold-pegged) — hedges BCH *and* VES; the most robust anchor.

**Phase 0 launches BCH-only;** stability tokens are added when merchants demonstrate the need. Without a stabiliser, one bad drop makes merchants quit — that's the retention risk the layer solves.

---

## Replenishment: keep the flywheel turning

After funding a covenant you need **more BCH**. **Buy it on the board if it's easier or cheaper than a CEX** — and that purchase is itself **a trade**:

- It improves your **numbers and reputation** (volume, distinct counterparties).
- It puts **real liquidity** in front of other sellers → **competition pushes fees down** (which is the whole point).

The board serves **two** demands: remittance **senders** *and* **sellers replenishing**. A newcomer can build reputation as a *reliable buyer* too — not only as a seller. *(See the board workspace on the capacity signal and replenishment.)*

---

## Edge Cases

- **Not enough VES cash?** Partial trade, defer, or bank transfer — keep a **VES float** (the flow is predictable).
- **Not enough BCH?** **Replenish**, or **decline gracefully** and let the sender choose another seller.
- **BCH crashes overnight?** Diversify into H€/HAu for near-term needs; remember the alternative (VES) inflates *faster*.
- **Is the incoming BCH clean?** You buy BCH and provide goods/services (merchant exemption); small amounts are below AML thresholds. Track record + judgment are your filters, and legitimate remittance corridors drive Phase 0.

---

## Passive Mode & Retention

**Why passive matters:** setup once (listing + float); then accept/reject each cash-out in ~30 seconds. Time invested after setup: minutes a day.

**Why merchants stay:** the first BCH drop scares an unstabilised merchant into quitting; with **stability** (H€/HAu) and **modest, safe floats**, they turn from transient participants into **long-term merchants** — and the network effect compounds: recipients become customers, customers start paying in BCH, and the merchant becomes a **true BCH merchant**.

**The endgame:** merchant adoption → direct BCH payments → Asgaya becomes unnecessary. That is success.

---

## Technical Details

**For implementation:** [Wallet](../../implementation/android-app/wallet.md) · [Bulletin Board](../../implementation/android-app/bulletin-board.md) · [Nostr](../../implementation/android-app/nostr.md) · [Notification Bot](../../implementation/android-app/notification-bot.md) · [Stability Layer](../../implementation/android-app/stability-layer.md)

**For rationale:** [Inefficient by Design](../../why-this-design/constraints/asgaya-remittances-inefficient-by-design.md) · [Value-Guaranteed Delivery](../../why-this-design/constraints/7%-volatility-buffer-value-guaranteed-delivery.md) · [Derived Reputation](../../why-this-design/constraints/reputation-on-chain-not-central-database.md)

---

## Related journeys

- [Sender Journey](../remittance/sender/README.md) — where remittances come from
- [Recipient Journey](../remittance/recipient/README.md) — Elena's perspective
- [Trader Journey](../trader/README.md) — the same liquidity provider, both seats
- [Customer Journey](../customer/README.md) — paying a merchant with Asgaya
- [The Fund-Request Lifecycle](../../the-mechanism/fund-request-lifecycle.md) — the seller side: request → funding → resolution

---

**Status:** Phase 0 (Pre-Launch) — Spain → Venezuela corridor  
**Updated:** 2026-10-04  
**Key insight:** Merchants are the retention layer — and the goal. Triple-dip + stability + **small, safe floats** = merchant evangelism.

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ User Journeys](../README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Trader](../trader/README.md) · [Customer](../customer/README.md)
