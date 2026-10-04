# Asgaya Roadmap

**What this is:** the plan — each phase's **focus**, and the **milestones that unlock the next capabilities**.
**What works today:** see the [implementation README](implementation/README.md) and the [home page](index.md).

---

## Phases at a glance

| Phase | Focus | Gate to move on |
|-------|-------|-----------------|
| **Phase 0** — the **MVP** | The whole loop end to end, launched as a limited **mainnet beta** (real money, a small trusted user set) | **MVP confidence** — the loop is proven and reliable |
| **Phase 0+** | Cheap, opportunistic wins added *while* the beta runs | — |
| **Phase 1** | Post-MVP work, **driven by what we observe** in Phase 0 | Demonstrated user behaviour / demand |
| **Phase 1+** | Larger enhancements and geographic expansion | Adoption |

Phase 0 is validated on **testnet3** first; the **mainnet beta** begins once the MVP is proven.

---

## Phase 0 — the MVP (current focus)

**North star:** the full **sender → seller → covenant → recipient → merchant** loop, in person (cash) and over **Bizum**, with the **customer flow** (a merchant accepting BCH) treated as a first-class path.

**The plan — milestones, each unlocking the next:**

| # | Milestone | Status | Unlocks |
|---|-----------|--------|---------|
| 1 | **In-person on-ramp** — the sender sets terms; the seller funds the covenant on "cash received" | ✅ Proven (4 devices) | The basic loop |
| 2 | **Cash-out** — the merchant cashes the recipient out (merchant-first) | ✅ On-chain | The off-ramp |
| 3 | **Discovery & coordination** — bulletin board (Nostr listings) + Nostr DMs (NIP-17) | ✅ Working | Finding a seller/merchant without a central server |
| 4 | **Cash Accounts** — register and use a `name#number` as the **payment reference** | 🎯 Next | The reference the **banking rails** match on → **Bizum auto-funding** |
| 5 | **Bizum auto-funding** — the seller's app reads the bank notification and funds the covenant automatically | 📅 Planned | Removes the manual step → the **merchant/seller client** (Phase 1) |
| 6 | **The customer flow** — a merchant accepts BCH directly (same covenant; merchant `claim()` before releasing goods) | 🎯 Target | A merchant that anyone can pay in BCH |

**Core infrastructure (done):** covenant **v2.6.1** (five spend paths); the **hybrid app** (native network + WebView compute); the **oracle** (own, signed 16-byte price + time, MTP fallback).

**What "MVP confidence" means:** the loop runs end-to-end, the seller is paid only after the fiat lands, the recipient can always claim (or the funder recovers the buffer), and it survives real devices and real networks.

---

## Phase 0+ — opportunistic (during Phase 0)

Cheap wins to add *while* the beta runs, as they become convenient — not gating anything:

- **Seed-phrase backup** (BIP39)
- **Seller device-health monitoring**
- **HD wallet derivation**
- **Covenant state tracking** in the client
- **Market-price subscription** in the UI
- **Electrum redundancy** (multiple servers)

---

## Phase 1 — post-MVP (driven by observed behaviour)

What Phase 0 shows us it needs next:

- **A merchant/seller-focused client** — auto-fund and auto-claim → closes the refund window and makes the passive side truly passive.
- **Stability layer (H€/HAu) activation** — offered once merchants demonstrate the need (the beta launches **BCH-only**).
- **Reputation + seller ranking** (derived from settlement history) — trust for sellers the user doesn't know.
- **Passive-mode bot automation** — 24/7 liquidity without manual action.
- **0-conf acceptance hardening**, multi-covenant batching, moving the remaining covenant work out of the WebView, and our **own Nostr relay** if the public ones prove unreliable.

---

## Phase 1+ — future / expansion

- **Offline-first** (queue, cache, sync for unreliable connectivity).
- **Geographic expansion** — the Spain → Venezuela corridor first, then other methods and regions (PagoMóvil, M-Pesa).
- **iOS / web clients.**
- **Formal protocol specifications.**
- **N-of-M oracle support** in the covenant (multiple distinct price sources).

---

## The plan's logic

The milestones are **gated, not parallel**: **Cash Accounts** (the payment reference) unblock the **banking rails**; the rails unblock **Bizum auto-funding**; auto-funding plus the **merchant claim** unblock the **merchant client**. Quiet dependencies matter elsewhere too — the **stability tokens** wait for a demonstrated merchant need, and everything larger waits on **adoption**. That is why the phases are ordered this way.

---

## Status legend

✅ Done / proven · 🎯 Next target · 📅 Planned milestone · 🔵 Opportunistic (Phase 0+) · 🟢 Future (Phase 1+)

**Last updated:** 2026-10-04
