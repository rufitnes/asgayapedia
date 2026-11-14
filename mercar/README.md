# Mercar Asgaya — BLAZE 2026 build

**Window:** 14–22 Nov 2026. **Submission:** 22 Nov 14:00 UTC. **Kickoff:** 14 Nov 14:00 UTC.
**Docs (public):** [asgayapedia](https://github.com/rufitnes/asgayapedia) · **Baseline:** tag `v0-prehackathon-baseline`.

## The slice

A live web board at `mercar.asgaya.org`:

1. **The board** — a listing (merchant, network badge, menu) built from a catalogue.
2. **The catalogue** — a restaurant menu as a static JSON, linked from the listing. The schema is in `catalogue/schema.json`.
3. **The direct checkout** — the customer adds dishes and pays from a BCH wallet with **WizardConnect**, straight to the merchant's address. The board builds the payment request and never holds money.

Networks: the **anchor is mainnet**; the demo shop and payment run on **chipnet**. Every listing carries a network badge.

## Day 1 (14 Nov) — status

- [x] Catalogue schema (`catalogue/schema.json`) and the example restaurant (`catalogue/menu.json`).
- [x] Static board (`/`): menu by category, cart, total in EUR, checkout request with a demo fiat→BCH quote.
- [ ] WizardConnect direct payment (P2PKH) on chipnet — the next step.
- [ ] Anchor mint page (mainnet) — reuse `asgaya-docs/code/anchor-mint-spike`.
- [ ] Validating relay / Nostr listing (the BLAZE demonstrator).
- [ ] Deploy to `mercar.asgaya.org` (Pages + CNAME).

## Run it

```bash
cd mercar
python3 -m http.server 8080
# open http://localhost:8080/
```

## Open build items (from `bulletin-board/26`)

- (a) Confirm the **simple P2PKH payment path** via WizardConnect on chipnet (the spike proved the anchor mint; the payment is the next test).
- (b) The demo needs a **fiat→BCH quote**; `web/app.js` uses a fixed demo rate until the oracle is wired.
- (c) The **catalogue schema** is defined here; iterate with the merchant data.

## Not in the demo

Covenant on-ramp, notary/CDS escrow, graph score, `H(tracking)`. The direct checkout is the star; the app's universal on-ramp is the next step after BLAZE.
