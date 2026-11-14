# Mercar Asgaya — BLAZE 2026 build

**Window:** 14–22 Nov 2026. **Submission:** 22 Nov 14:00 UTC. **Kickoff:** 14 Nov 14:00 UTC.
**Docs (public):** [asgayapedia](https://github.com/rufitnes/asgayapedia) · **Baseline:** tag `v0-prehackathon-baseline`.

## The slice

A live web board at `mercar.asgaya.org`:

1. **The board** — a listing (merchant, network badge, menu) built from a catalogue.
2. **The catalogue** — a restaurant menu as a static JSON, linked from the listing. The schema is in `public/catalogue/schema.json`.
3. **The direct checkout** — the customer adds dishes and pays from a BCH wallet with **WizardConnect**, straight to the merchant's address. The board builds the payment request and never holds money.

Networks: the **anchor is mainnet**; the demo shop and payment run on **chipnet**. Every listing carries a network badge.

**Prior work (the WizardConnect proof):** the [anchor-mint spike](https://github.com/rufitnes/asgayapedia/tree/main/code/anchor-mint-spike) connects a real wallet through WizardConnect and mints the genesis anchor on chipnet. The board's direct checkout uses the same connection; the spike is the proof that the wallet path works.

## Day 1 (14 Nov) — status

- [x] Catalogue schema (`public/catalogue/schema.json`) and the example restaurant (`public/catalogue/menu.json`).
- [x] Static board (`/`): menu by category, cart, total in EUR, checkout request with a demo fiat→BCH quote.
- [x] Pre-BLAZE demo recorded (three devices, side-by-side, GIF in `media/`).
- [x] WizardConnect direct payment (P2PKH) on chipnet: connect, build the tx to the merchant, sign and broadcast (test with Cashonize/Paytaca pending).
- [ ] Anchor mint page (mainnet) — reuse `asgaya-docs/code/anchor-mint-spike`.
- [ ] Validating relay / Nostr listing (the BLAZE demonstrator).
- [ ] Deploy to `mercar.asgaya.org` (Pages + CNAME).

## Run it

```bash
cd mercar
npm install
npm run dev
# open http://localhost:5173/
```

## Open build items (from `bulletin-board/26`)

- (a) **WizardConnect P2PKH path implemented** in the board; test it with Cashonize/Paytaca on chipnet.
- (b) The demo needs a **fiat→BCH quote**; `web/app.js` uses a fixed demo rate until the oracle is wired.
- (c) The **catalogue schema** is defined here; iterate with the merchant data.

## Not in the demo

Covenant on-ramp, notary/CDS escrow, graph score, `H(tracking)`. The direct checkout is the star; the app's universal on-ramp is the next step after BLAZE.
