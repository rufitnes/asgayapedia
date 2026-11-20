# Mercar Asgaya

**Asgaya's discovery layer.** A peer-to-peer classifieds board where a merchant's
on-chain identity is bound to its Nostr ads, and the payment instructions come
from the merchant's own device.

[**Open the board**](https://mercar.asgaya.org) · [**Docs**](https://docs.asgaya.org) · [**Design**](./DESIGN.md) · [**Repo**](https://github.com/rufitnes/asgayapedia) · [**Demo video**](#demo-video) · [**Deck**](https://docs.google.com/presentation/d/1foh7Md6xj5F2xlk6AvZ8awtLSaG3CmxJNlKJKoPnSvE/edit?usp=sharing) · [**How it meets BLAZE**](#how-it-meets-bch-blaze-2026)

**BCH BLAZE 2026** (14–22 Nov). **Submission:** 22 Nov 14:00 UTC.

## What it is, in 60 seconds

The board finds; the chain proves the identity.

1. A merchant mints an **identity anchor**: a genesis CashToken NFT whose category
   is the funding input's txid. The key that signs it is the identity; the npub is
   not written on-chain.
2. The merchant publishes a **NIP-99 ad** on Nostr. The ad references the anchor
   (`txid:vout`) and carries the offer: currency, payment methods, fee, limits.
3. A buyer opens the ad. The **identity badge** fetches the anchor's genesis
   transaction and checks that its signer is the ad's BCH pubkey (same-key binding).
4. The buyer asks for **payment instructions**. The merchant's app answers privately,
   from the device (liveness). Mercar never stores the instructions.
5. Underneath, the merchant can **fund a covenant** (the remittance flow; testnet
   research).

On-chain space is for the durable identity, not the ad. An anchor commits only the
discovery tuple `(seller, asset, method, fee)`; ads that share it reuse the same
anchor, and everything ephemeral lives on Nostr.

## The flow

```
merchant app ── genesis CashToken NFT (anchor) ──▶ chain
      │                                              │
      └── NIP-99 ad ──▶ Nostr relay ──▶ web board ──▶ buyer
                                            │
                              identity badge │ (verify the genesis signer)
                                            ▼
                                    payment chat (DM)
                                            │
                          merchant app ◀────┘
                              └── [PAY_INSTRUCTIONS] ──▶ buyer
```

## Demo video

[**Watch the demo on YouTube**](https://youtu.be/ghSi2tEXGDM) — 75 s.

A short walk through the board: publish a listing, the identity badge verifying the
anchor, and a buyer asking for and receiving payment instructions from the merchant's
device. *(Link to be added.)*

## What works (18 Nov 2026)

- **The board** (Vite): the three categories (exchange, goods, services/restaurants),
  product cards, the ad detail, and the on-chain identity badge (chipnet, testnet3,
  mainnet).
- **Publish from the web**: connect with WizardConnect, mint or reuse the anchor,
  sign the NIP-99 listing. The browser never holds the private key.
- **The product flow**: a buyer sends `[PRODUCT_ORDER]`; the merchant answers
  `[ORDER_INSTRUCTIONS]` with its BCH address; the checkout asks the wallet to sign.
- **The payment chat**: the web sends `[PAY_INSTRUCTIONS_REQUEST]`; the merchant's app
  replies with `[PAY_INSTRUCTIONS]`. The demo transport is NIP-04; NIP-17 is the app's
  native transport and the convergence.
- **The companion app** (AsgayaHusk): mints the anchor **on the device**, reuses it per
  tuple, and answers the payment chat with the merchant's instructions.

## How it meets BCH BLAZE 2026

The Mainnet Track, requirement by requirement:

| Requirement | How Mercar meets it | Check it |
|---|---|---|
| A live, usable **web demo** | [`mercar.asgaya.org`](https://mercar.asgaya.org) is the board: publish, discover, verify | [open the board](https://mercar.asgaya.org) |
| ≥1 **mainnet transaction** | publishing an ad mints the identity anchor (a genesis CashToken NFT); the BLAZE artefact is live on mainnet | [`d7b1468f…`](https://explorer.salemkode.com/tx/d7b1468f1fd65295eebb4785489614eb58adde1f480c5cecabffcb78109c9ad4) |
| **WizardConnect** | the web connects the merchant's BCH wallet to mint/reuse the anchor and sign the listing | [`wizardconnect.js`](./wizardconnect.js) |
| **Presentation** ("Widget Factory" deck) | [the BLAZE deck](https://docs.google.com/presentation/d/1foh7Md6xj5F2xlk6AvZ8awtLSaG3CmxJNlKJKoPnSvE/edit?usp=sharing) | Google Slides (read-only) |
| **Original / improved in the hackathon** | the baseline is tagged `v0-prehackathon-baseline`; the board, the identity badge, the categories and the product flow land during BLAZE | git history |

## Proof on mainnet

| Artefact | Where | Notes |
|---|---|---|
| The identity anchor | [`d7b1468f…`](https://explorer.salemkode.com/tx/d7b1468f1fd65295eebb4785489614eb58adde1f480c5cecabffcb78109c9ad4) | the BLAZE artefact: the genesis CashToken NFT |
| The category | `29e3168c…` | the anchor's token category (named after the funding input) |
| The commitment | `0101455552023200` | type · asset · method · fee, < 128 B |
| The ad | NIP-99 on `relay.asgaya.org` | references the anchor; the badge verifies it on-chain |

The badge labels the network it verified. A testnet reputation is a demonstration,
not real reputation.

## Built on

| Piece | What it does in Mercar | Where |
|---|---|---|
| **Nostr (NIP-99)** | the ads (kind 30402) and the payment chat | [`nostr.js`](./nostr.js) |
| **WizardConnect** | the wallet connection for the anchor and the listing | [`wizardconnect.js`](./wizardconnect.js) |
| **CashScript / the anchor** | the genesis CashToken NFT and its commitment | [`anchor.js`](./anchor.js) |
| **Electrum (public WebSockets)** | the on-chain verification of the anchor | [`electrum.js`](./electrum.js) |
| **AsgayaHusk (Android)** | the merchant's device: the on-device anchor and the payment chat | [the app](../asgaya/knowledge/research_code/AsgayaHusk-v0.2) |

## Networks

| Network | What runs there |
|---|---|
| **testnet3** | the app and the on-device anchors (the demo) |
| **chipnet** | the web canary and its anchor |
| **mainnet** | the BLAZE artefact: the identity anchor (live) |

## Run the web

```bash
cd mercar
npm install
npm run dev      # http://localhost:5173/
npm run build    # static build in dist/
```

The board reads NIP-99 ads from a Nostr relay. Set `VITE_NOSTR_RELAY` (the public
build reads `.env.production`, which points at `wss://relay.asgaya.org`; the dev
server falls back to the LAN relay).
The identity badge uses public Electrum WebSockets: chipnet
`wss://chipnet.imaginary.cash:50004`, testnet3 `wss://testnet.imaginary.cash:50004`,
mainnet `wss://bch.imaginary.cash:50004`.

The companion app is not part of the web demo. The public
[anchor-mint spike](https://github.com/rufitnes/asgayapedia/tree/main/code/anchor-mint-spike)
shows the WizardConnect + anchor path.

## The repo

- `mercar/` — this web app: `app.js` (views, publish, badge, chat), `nostr.js`
  (NIP-99 + DMs), `electrum.js` (verification), `anchor.js` (the genesis commitment),
  `wizardconnect.js` (the wallet), `public/catalogue/` (the restaurant example).
- `code/anchor-mint-spike/` — the WizardConnect + anchor proof.
- `docs/` — the public documentation.

## For new collaborators

1. Read this README, then [`DESIGN.md`](./DESIGN.md) (the frozen design system).
2. Read the public docs: the identity/anchor model, the discovery architecture, the
   compliance notes.
3. Run the web (above). The companion app is not needed for the board; the anchor-mint spike shows the WizardConnect path.
4. Keep the public/private split: no keys, no secrets, no real identity data in the repo.
5. Where to help: the UI, the partner integration, the deployment (VPS + relay), the
   tests, the docs.

## For partners

Mercar is the **discovery layer** for BCH builders. Its first client was the
**remittance flow** (it needed a neutral, peer-to-peer place to exchange fiat and BCH);
**Nomos** (the marketplace) is the second. If you build on BCH and need discovery, on-chain
identity and access to the merchants, the board gives you all three. For early partners we
also host the demo.

## Status and next

The board, the identity badge, the on-device anchor, the product flow and the payment
chat work. Next: the **video**, and the **NIP-04 → NIP-17** convergence.

**Not in the web demo:** the covenant's on-chain flow (testnet research), the
on-chain notary escrow, the graph score, the tracking-hash scheme.

## Links

- **BLAZE 2026:** [DoraHacks (BUIDL 51216)](https://dorahacks.io/buidl/51216)
- **Public docs:** [docs.asgaya.org](https://docs.asgaya.org)
- **Baseline:** tag `v0-prehackathon-baseline`
- **Pre-BLAZE demo:** [`media/`](./media)
