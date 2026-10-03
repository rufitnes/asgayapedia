# Bulletin Board Component

**Purpose:** Discover listings, parse metadata, filter by payment method / asset / amount, cache results.

**Complexity:** Low — Nostr subscription (NIP-99) + JSON parsing + a local cache.

> **⚠️ Phase 0 Priority - Essential for Mainnet Launch**
>
> The bulletin board is a **fundamental gear** of Asgaya and critical to our compliance strategy:
>
> **Why Phase 0 (High Priority):**
> 1. **Sender discovery** - Senders find BCH sellers to fund covenants (no bulletin board = no way to create remittances)
> 2. **Recipient cashout** - Recipients find nearby merchants to cash out BCH (essential for completing remittance flow)
> 3. **Compliance requirement** - Asgaya **helps users discover each other**, not intermediates transactions. Bulletin board proves we're a discovery tool, not a money transmitter.
>
> **Without this component:** Mainnet launch impossible. Users can't find counterparties.
>
> **Implementation status (2026-09-20):** 🟢 **Phase-0 board shipped.** Discovery is **Nostr** — signed, self-published listings (**NIP-99, `kind:30402`**) cached locally. A mock source ships for development; the on-chain anchor is **Phase 0+ (planned)**.
>
> **Known limitation (2026-09-16) — discovery:** our Electrum/Fulcrum server **cannot enumerate listings by token category** (verified: `blockchain.nft.list_category` unsupported; only scripthash-scoped queries work). That is **why Phase-0 discovery is Nostr** — the on-chain query arrives with the planned anchor/explorer (Phase 0+).

---

## Overview

The Bulletin Board is how users discover counterparties:
- **Sellers (people with BCH)** post an ad: *"I sell BCH for fiat."* This is the **sender's** counterparty.
- **BCH buyers / merchants (people with an asset)** post an ad: *"I sell ⟨asset⟩ for BCH."* This is the **recipient's** counterparty when cashing out.
- **Matching:** client-side filtering by payment method, asset, amount, and location.

Every listing carries the poster's **BCH pubkey** (builds the funding covenant) and their **Nostr pubkey (`npub`)** — the coordination address — so a counterparty can open the encrypted channel ([Nostr](nostr.md)) without any other contact information.

**No centralized server:** listings are **signed Nostr events** (NIP-99, `kind:30402`); each device stores the ones it sees and filters locally. *(The on-chain anchor — a censorship-resistant copy of the essential fields — is Phase 0+; see below.)*

---

## How Listings Work

### The listing event (NIP-99, `kind:30402`)

**Addressable** — an update replaces the previous version — and **signed by the poster's npub**. It carries the listing two ways:
- **Tags** (the indexable view): `d` (stable ad id), `t` (`asgaya-board`, `asgaya-seller`/`asgaya-buyer`, `asset:<slug>`, `pay:<slug>`), `bchpub`, `fee`, `buffer`, `location`, `min`, `max`, `hours`, `cash`, `status`.
- **`content` JSON** (canonical, authoritative payload).

### Identity in the listing

- `bchpub` — the poster's **BCH pubkey** (the covenant funder).
- The event is **signed by the poster's `npub`** (the coordination key).
- The planned **on-chain anchor** (Phase 0+) will bind the two cryptographically: the `npub` is the **x-coordinate of the BCH identity key**, so a client can verify the binding offline.

### Updating or Removing

Publish/replace the **same `d`** → the relay replaces the prior version. Set `status: paused` to hide an ad without deleting it.

---

## Listing Fields

| Field | Meaning |
|---|---|
| `adType` | `SELLER` (sells BCH) or `BUYER` (sells an asset for BCH) |
| `assets` | non-BCH side: fiat code (`EUR`, `VES`), token (`H€`, `HAu`), or free text |
| `paymentMethods` | the fiat rail (`Bizum`, `Cash in person`, …) — **the effective filter** |
| `bchPubkey` / `npub` | identity (funding key / coordination key) |
| `fee` | the seller's fee, e.g. `0.5%` |
| `buffer` | volatility buffer (`minPricePercent`, e.g. `93` = 7%) |
| `min` / `max` | amount bracket |
| `location` | required for cash-in-person |
| `hours` / `status` | availability / online |

---

## Two Ad Types

| Ad | Posted by | Meaning | Used by |
|---|---|---|---|
| **Seller ad** (`SELLER`) | someone with BCH | "I sell BCH for fiat" | the **sender** (buying BCH for a remittance) |
| **Buyer ad** (`BUYER`) | a merchant / BCH buyer | "I sell ⟨asset⟩ for BCH" | the **recipient** (cashing out) |

The recipient's cash-out picker shows **buyer ads**; the sender's picker shows **seller ads**. Both carry `bchpub` + `npub`.

> **The payment method is the filter.** A recipient who picks *"Cash in person"* sees only the ads that buy BCH for cash; the sender filters by *Bizum* / *SEPA*. The **asset** is a multi-value field (Phase 0+: a protocol bitmask + an `OTHER` escape hatch for the long tail).

---

## You Can't Trade With Yourself

Your own ad is on the board like anyone else's. The app therefore **never offers your own ad as a counterparty** — it matches the acting wallet against the listing's wallet (BCH key or `npub`). This prevents user mistakes **and** self-dealing, which would fake trade volume and **poison reputation data**.

---

## Creating a Listing

**Pseudocode:**
```
function publishListing(adType, assets, paymentMethods, fee, buffer, min, max, location):
  listing = {
    id: uuid(),                         // stable `d` — reused on update
    adType: adType,
    assets: assets,
    paymentMethods: paymentMethods,
    fee: fee, buffer: buffer,
    min: min, max: max, location: location,
    bchPubkey: myBchPubkey(),           // identity / funder
    npub: myNpub()                      // coordination (also the event signer)
  }

  event = nip99_event(
    kind: 30402,
    tags: buildTags(listing),           // d, t:asgaya-board, t:asgaya-<type>, t:asset:*, t:pay:*, bchpub, …
    content: json(listing)
  )
  event.sign(myNpub())

  broadcast(event, relays)              // no on-chain transaction in Phase 0
  cacheLocally(listing)

  return listing.id
```

**Cost:** a relay publish (free). **Visibility:** seconds.

---

## Querying the Bulletin Board

**One subscription, two filters** — the app sends a single REQ carrying the DM filter and the board filter together:

```json
{
  "kinds": [30402],
  "#t": ["asgaya-board"]
}
```

Each returned event is **cached locally** (keyed by author + `d`, so a newer version replaces the old); the pickers then read from the cache. No category lookup, no Electrum enumeration.

> **Note:** relays only index **single-letter** tags, so filtering happens **client-side** over the cached set — fine for Phase 0's scale, and it keeps the app **index-agnostic** (a future on-chain index slots in behind the same listing source).

---

## Filtering and Matching

### Client-Side Filtering

**Why client-side?** No backend server, full privacy.

**Filters:**
1. **Payment method:** María uses Bizum → drop ads without it.
2. **Asset:** María wants EUR → drop VES/USD/etc.
3. **Amount:** drop ads whose `[min, max]` doesn't include María's amount.
4. **Location:** for cash-in-person, filter by proximity.
5. **Self:** drop the acting wallet's own ads.

The **ranking** then orders the survivors — see [Seller Ranking Algorithm](../../the-mechanism/bulletin-board/seller-ranking-algorithm.md).

---

## Caching Strategy

- **Storage:** local database — `board_events` (discovered ads, raw event JSON keyed by author + `d`) and `listings` (your own ads).
- **Freshness:** an addressable update replaces the cached row; expired listings are pruned.
- **Offline:** cached listings remain usable; the app re-syncs on reconnect.

---

## The On-Chain Anchor (Phase 0+, planned)

Nostr relays are **censorable**. Phase 0+ adds a **per-seller NFT** on Bitcoin Cash carrying the *discovery-critical* fields (identity, `asset`, `payment_methods`, fee), so listings survive relay censorship and can one day be enumerated directly on-chain.

Then the model is: **find** a listing via any index (Nostr or on-chain), **verify** it against the anchor — *"index = find, chain = trust."* This is **not required for Phase 0** and is tracked as Phase-0+ work.

---

## Error Handling

- **Unparseable event:** skip it (never crash) and log.
- **No listings / no relay:** show the empty state; fall back to **known contacts** and **QR** (see `contact-exchange`).
- **Relay drop:** reconnect; the client degrades gracefully across plural relays.

---

## Testing Strategy

- **Unit:** event ↔ `Listing` round-trip (tags + content), filtering logic, addressable replace.
- **Integration (devices):** publish on device A → discover on device B. Verified on two devices + `nak` (damus/snort) during Phase 0.

---

## Related Components

**Used by:**
- [nostr.md](nostr.md) — contact the counterparty after finding a listing
- [state-management.md](state-management.md) — track your own listings

**Interacts with:**
- [notification-bot.md](notification-bot.md) — passive sellers keep active listings

---

**Status:** ✅ Phase 0 shipped (Nostr discovery); on-chain anchor = Phase 0+ (planned)
**Updated:** 2026-09-21
**Complexity:** Low (relay publish + local cache + client-side filtering)
**Priority:** Essential for discovery layer (sender → seller, recipient → merchant)
**Compliance:** Proves Asgaya is discovery tool, not intermediary

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Android App](README.md)** | **[📖 Glossary](../../glossary.md)**
