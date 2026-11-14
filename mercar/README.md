# Mercar Asgaya — BLAZE 2026 build

**Window:** 14–22 Nov 2026. **Submission:** 22 Nov 14:00 UTC. **Kickoff:** 14 Nov 14:00 UTC.
**Docs (public):** [asgayapedia](https://github.com/rufitnes/asgayapedia) · **Baseline:** tag `v0-prehackathon-baseline`.

## The slice

**Mercar Asgaya is Asgaya's discovery layer**, a classifieds board at `mercar.asgaya.org`:

1. **Categories** — currency exchange first, then restaurants, goods and services. Each ad shows the merchant's **npub and on-chain anchor** (the identity ↔ anchor binding).
2. **The exchange section (the core)** — reuse the AsgayaHusk ad template: currency, fee, min/max, and **payment methods** (cash, card, Bizum, …). Payment methods + automation are what make Asgaya a universal on-ramp.
3. **The restaurant example** — a catalogue (static JSON) with a menu and the direct BCH checkout via **WizardConnect**; a product category, not the core.

Networks: the **anchor is mainnet**; the demo shop and payment run on **chipnet**. Every listing carries a network badge.

**Prior work (the WizardConnect proof):** the [anchor-mint spike](https://github.com/rufitnes/asgayapedia/tree/main/code/anchor-mint-spike) connects a real wallet through WizardConnect and mints the genesis anchor on chipnet. The board's direct checkout uses the same connection; the spike is the proof that the wallet path works.

## Day 1 (14 Nov) — status

- [x] Catalogue schema (`public/catalogue/schema.json`) and the example restaurant (`public/catalogue/menu.json`).
- [x] Static board (`/`): menu by category, cart, total in EUR, checkout request with a demo fiat→BCH quote.
- [x] Pre-BLAZE demo recorded (three devices, side-by-side, GIF in `media/`).
- [x] WizardConnect direct payment (P2PKH) proven on chipnet: `d469ea55…` (merchant 962,094 sats; change 34,879; fee 1,500).
- [x] Exchange list from the LAN relay: the board renders the existing NIP-99 ads published from the app's Trade tab (4 real ads: EUR card/cash, NGN buy, USD cash), with asset/method filters and fee sort.
- [ ] Identity ↔ anchor badge; categories (exchange/restaurant/goods); publish from the web.
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
