# Mercar Asgaya: design system

**Status:** 🟢 **Frozen 2026-11-15.** Every new page uses these tokens; do not introduce new colours, sizes or radii.
**Source of truth:** the `:root` tokens in [`styles.css`](./styles.css). This page explains how to use them.

---

## Principles

- **Calm, honest, legible.** The product is a board; the ads and the merchants are the content. The UI stays quiet.
- **One strong surface:** the **hero gradient**. Everything else is a restrained light surface with the purple accent.
- **Colour is semantic:** the accent (purple) for action/identity; green/blue for the seller/buyer roles; amber for the network badge; mono for keys and addresses.
- **The identity is visible:** the merchant's `npub`/anchor is a trust line, not decoration.

## Palette

| Token | Value | Use |
|---|---|---|
| `--bg` | `#f7f5ff` | page background |
| `--panel` | `#ffffff` | cards, topbar, dialogs |
| `--ink` | `#17122b` | text; the dark links banner |
| `--muted` | `#6b6580` | secondary text |
| `--accent` | `#6d5bd0` | primary actions, links, the brand accent |
| `--accent-ink` | `#ffffff` | text on the accent |
| `--accent-soft` | `#efeaff` | the active category tile / soft accent |
| `--line` | `#e5e0f2` | borders, dividers |
| `--chipnet` | `#f59e0b` | the network status |
| `--hero-1/2/3` | `#1b1436` / `#3a2a7a` / `#6d5bd0` | the landing hero gradient (only there) |
| `--hero-ink` | `#ffffff` | hero text and the white primary button |
| `--hero-muted` | `#cfc8ff` | hero eyebrow, the H1 span, the trust line, the links banner |
| `--hero-sub` | `#e8e3ff` | the hero subline |
| `--ok-bg` / `--ok-ink` | `#dff7e6` / `#1a6b3a` | “Sells …” (seller) |
| `--info-bg` / `--info-ink` | `#e6ecff` / `#2a3d8f` | the buyer role |
| `--warn-bg` / `--warn-ink` / `--warn-line` | `#fdeccb` / `#7a4b00` / `#f7d69b` | the network badge |
| `--mono` | `ui-monospace, SFMono-Regular, Menlo, monospace` | addresses, txids, npub |

## Typography

- **Family:** `system-ui, -apple-system, "Segoe UI", Roboto, sans-serif`.
- **Body:** 16 px, line-height 1.45.
- **H1 (hero):** `clamp(36px, 6vw, 64px)`, weight 800, letter-spacing `-0.04em`, line-height 1.02. The second line in `--hero-muted`.
- **H2:** 24 px, letter-spacing `-0.02em`.
- **Section label:** 13 px, uppercase, letter-spacing 0.08em, `--muted`.
- **Small:** 13 px; **micro:** 11–12 px; **mono:** 10–12 px for keys.
- No web fonts. No italics for emphasis; use weight and colour.

## Layout & spacing

- **Page max:** `--page-max` 1120 px, 22 px side padding.
- **Cards:** radius 14 px (`--radius`), 14 px padding, 1 px `--line` border, `--panel` background.
- **Grids:** ads `minmax(260px, 1fr)` (`--card-min`), gap 14 px; menu items `minmax(220px, 1fr)`, gap 14 px.
- **Sections:** 22–26 px vertical rhythm; the hero has its own 64/56 px padding.
- **Dialogs:** panel, 480 px max, backdrop `rgba(23, 18, 43, 0.35)`.

## Components

- **Topbar:** panel, 1 px bottom border, brand (20 px, 800; “Asgaya” in `--accent`), the network badge, the connect button.
- **Links banner:** `--ink` background, 12 px, `--hero-muted` links; the judges' links.
- **Hero:** the gradient; the eyebrow (uppercase, 12 px); the H1 with the muted second line; the sub; the CTAs; the trust line.
- **Buttons:** `.primary` = accent/white on light surfaces; `.ghost` = transparent with a `--line` border; on the hero, `.btn.primary` = white/ink and `.btn.ghost` = white border. Disabled = 50 % opacity.
- **Ad card:** the role (seller/buyer) in the semantic colour, the merchant name, the fee on the right; the terms lines (for … / methods / min–max / location·hours); the `npub` in mono.
- **Badges:** the network (`--warn-*`); the role (`--ok-*` / `--info-*`).
- **Cart / checkout:** the same panel language; the totals in weight 700; the mono address.

## Search & categories

- **Search bar:** the panel/border/radius language; the accent focus ring; a 🔎 glyph; the placeholder “Search merchants, currencies, payment methods…”.
- **Category tiles:** a row of pill tiles (emoji + label). The active tile uses `--accent-soft` and the accent border; disabled tiles use 55 % opacity + a “soon” micro-label.
- **Category icons:** emoji, one per category (💱 exchange, 🍽️ restaurants, 📦 goods, 🛠️ services). No icon library.
- The tiles are navigation first; the search filters the current board content.
- **All categories** (🗂️) opens a modal with the taxonomy: a group title (the 13 px uppercase label) and a row of chips; each chip carries a count (muted micro). The exchange group is the core (All ads, Buy BCH, Sell BCH, EUR, USD, NGN, cash/card/Bizum); Restaurants opens the menu; Goods/Services are disabled with a “soon”.
- **Featured “Buy BCH”** (₿) toggles the ads where the merchant sells BCH (`adType === SELLER`), with the active `--accent-soft` state; the Restaurants tile anchors to the example section.

## Landing & views

- **The home does not list merchants.** It is the hero + the search + the categories. No ad or merchant appears on the landing by default (compliance: the front page must not rank or promote one merchant over another).
- **A category opens its view:** the exchange lists all the ads (fast UX); the restaurants show the **merchant card** first and the menu on click; Goods/Services are “soon”.
- **Back link** at the top of every view (“← All categories”; the menu adds “← Restaurants”); the browser stays on the same page.
- **The merchant card** is a full-width panel button: the name + the network badge, a one-line description, and the “View menu →” CTA. The exchange does not use the card; it lists the ads directly.
- The exchange title reflects the filter (“Currency exchange · Buy BCH”); the ads are sorted by fee or recency, never editorially.

## Copy & voice

- Sentence case; short lines. The ad reads **“Sells BCH”** or **“Sells NGN”**, then **“for EUR”**, methods, min–max.
- Say what is true: “demo”, “chipnet”, “coming” where it applies. No hype, no invented metrics.
- The identity line: “Merchant identity anchored on-chain” (the anchor is the promise; label it when it is live).
- No em dashes; use commas, colons or parentheses.
- Numbers: fiat with the currency; BCH in sats in the checkout; the fee as a percentage with two decimals.

## Do / don't

- **Do** use the tokens; reuse the card, badge, button and mono patterns.
- **Don't** add colours outside the palette; don't use the hero gradient on subpages; don't use the accent for large surfaces; don't mix the role colours with the network badge.
- Keep the **network badge** visible on every listing; keep the **identity/anchor** evidence near the merchant name.

## Files

- [`styles.css`](./styles.css): the tokens and the components.
- [`index.html`](./index.html): the landing (hero + exchange + restaurant example).
- [`app.js`](./app.js), [`nostr.js`](./nostr.js): the data; no styling decisions.
