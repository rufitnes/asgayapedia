# Mercar Asgaya

**Asgaya's discovery layer.** A peer-to-peer classifieds board where a merchant's
on-chain identity is bound to its Nostr ads, and the payment instructions come
from the merchant's own device.

**BCH BLAZE 2026** (14–22 Nov). **Submission:** 22 Nov 14:00 UTC.
**Docs:** [docs.asgaya.org](https://docs.asgaya.org) · **Repo:** [github.com/rufitnes/asgayapedia](https://github.com/rufitnes/asgayapedia) · **Design:** [`DESIGN.md`](./DESIGN.md)

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

## What works (16 Nov 2026)

- **The board** (Vite): categories, the exchange list from the relay, filters,
  the ad detail and the on-chain identity badge (chipnet and testnet3).
- **Publish from the web**: connect with WizardConnect, mint or reuse the anchor,
  sign the NIP-99 listing. The browser never holds the private key.
- **The companion app** (AsgayaHusk): mints the anchor **on the device** (the WebView
  signs with the wallet key; the app only broadcasts), reuses it per tuple, and
  answers the payment chat with the merchant's instructions.
- **The payment chat**: the web sends `[PAY_INSTRUCTIONS_REQUEST]`; the merchant's app
  replies with `[PAY_INSTRUCTIONS]`. The demo transport is NIP-04; NIP-17 is the app's
  native transport and the convergence.

## Networks

| Network | What runs there |
|---|---|
| **testnet3** | the app and the on-device anchors (the demo) |
| **chipnet** | the web canary and its anchor |
| **mainnet** | the BLAZE artefact: one identity anchor (pending) |

The badge labels the network it verified. Testnet reputation is a demonstration,
not real reputation.

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
`wss://chipnet.imaginary.cash:50004`, testnet3 `wss://testnet.imaginary.cash:50004`.

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

Mercar is the **discovery layer** for BCH builders: the merchants on the
board can fund the BCH side of a project, peer to peer, with no custodian. The first
integration is a general covenant (testnet). If you build on BCH and need the other side funded,
the board gives you discovery, on-chain identity and access to the merchants. For the
first partner we also host the demo.

## Status and next

Mid-hackathon. The board, the identity badge, the on-device anchor and the payment chat
work on testnet3/chipnet. Next: the **mainnet anchor** (the BLAZE artefact), the
**public deployment** (`mercar.asgaya.org` + a public WSS relay), the **NIP-04 → NIP-17**
convergence, and the first partner's demo.

**Not in the web demo:** the covenant's on-chain flow (testnet research), the
on-chain notary escrow, the graph score, the tracking-hash scheme.

## Links

- **BLAZE 2026:** [DoraHacks (BUIDL 51216)](https://dorahacks.io/buidl/51216)
- **Public docs:** [docs.asgaya.org](https://docs.asgaya.org)
- **Baseline:** tag `v0-prehackathon-baseline`
- **Pre-BLAZE demo:** [`media/`](./media)
