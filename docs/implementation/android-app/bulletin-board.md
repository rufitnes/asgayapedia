# Bulletin Board Component

**Purpose:** Query Electrum for NFT listings, parse metadata, filter by currency/amount, cache results

**Complexity:** Low - Simple Electrum queries + JSON parsing

---

## Overview

The Bulletin Board is how users discover buy/sell opportunities:
- **Isabel (seller):** Creates NFT listing: "I sell €100-500 EUR for BCH"
- **María (buyer):** Queries bulletin board: "Who's selling EUR?"
- **Matching:** Client-side filtering by currency, amount, rating

**No centralized server:** All listings stored as NFT UTXOs on BCH blockchain, queried via Electrum.

---

## How Listings Work

### What Is a Listing NFT?

**Definition:** CashTokens NFT with metadata commitment containing seller's offer

**On-chain structure:**
```
UTXO {
  value: 100000 satoshis (0.001 BCH anti-spam deposit)
  script: P2PKH to seller's address
  token: {
    category: ASGAYA_LISTING_CATEGORY  // Fixed identifier
    nft: {
      commitment: JSON metadata (see below)
    }
  }
}
```

**Why NFT?** 
- Anti-spam: 0.001 BCH deposit (~€1.00) makes spam expensive, reclaimable on delist
- Easy to query (all listings have same category)
- Self-expiring (seller can spend UTXO to delist)

---

## Listing Metadata Format

### JSON Schema

**Pseudocode:**
```json
{
  "version": 1,
  "type": "buy" | "sell",
  "currency": "EUR" | "VES" | "USD" | "ARS" | "COP",
  "amount_min": 100,
  "amount_max": 500,
  "payment_methods": ["bizum", "sepa"],
  "cash_account": "Isabel#142",
  "location": "Madrid",  // Optional, required for cash-in-person
  "expires_at": 1735689600  // Unix timestamp
}
```

**Size limit:** 128 bytes (BCH NFT commitment max, post-P2S upgrade May 15, 2026)

**Required fields:**
- `version`, `type`, `currency`, `amount_min`, `amount_max`, `payment_methods`, `cash_account`
- `location` (required only if payment_methods includes "cash")

**Optional fields:**
- `expires_at` (default: 30 days from creation)

**Note on reputation:** Seller rating and completed trades are stored in a separate on-chain reputation UTXO (see `reputation-on-chain.md`), not in the listing NFT. The app queries reputation separately when displaying seller options.

---

## Creating a Listing

### Pseudocode

```
function createListing(type, currency, amount_min, amount_max, payment_methods):
  my_cash_account = getMyCashAccount()  // From wallet
  
  metadata = {
    version: 1,
    type: type,
    currency: currency,
    amount_min: amount_min,
    amount_max: amount_max,
    payment_methods: payment_methods,
    cash_account: my_cash_account,
    expires_at: now() + 30_DAYS
  }
  
  // Add location if cash-in-person
  if "cash" in payment_methods:
    metadata.location = my_location  // e.g., "Madrid"
  
  // Validate size
  if json_size(metadata) > 128:
    return error("Metadata too large (max 128 bytes)")
  
  // Create NFT UTXO
  nft_tx = createTransaction({
    outputs: [
      {
        value: 100000,  // satoshis (0.001 BCH anti-spam deposit, reclaimable)
        script: p2pkh(my_address),
        token: {
          category: ASGAYA_LISTING_CATEGORY,
          nft: {commitment: json_encode(metadata)}
        }
      }
    ]
  })
  
  broadcast(nft_tx)
  
  return {
    listing_id: nft_tx.txid,
    metadata: metadata
  }
```

**Cost:** ~€0.001 (transaction fee) + €1.00 deposit (reclaimable when delisting)

**Visibility:** Instant (as soon as transaction confirmed, ~10 minutes)

---

## Querying the Bulletin Board

### Electrum Query Pattern

**What:** Find all active listings for a specific currency

**Pseudocode:**
```
function queryListings(currency, type):
  // Query Electrum for all NFTs with Asgaya category
  utxos = electrumQuery("blockchain.nft.list_category", {
    category: ASGAYA_LISTING_CATEGORY
  })
  
  listings = []
  
  for utxo in utxos:
    // Parse NFT commitment (JSON metadata)
    metadata = json_decode(utxo.token.nft.commitment)
    
    // Filter expired listings
    if metadata.expires_at < now():
      continue
    
    // Filter by currency
    if metadata.currency != currency:
      continue
    
    // Filter by type
    if metadata.type != type:
      continue
    
    listings.push({
      listing_id: utxo.txid,
      seller: metadata.cash_account,
      currency: metadata.currency,
      amount_min: metadata.amount_min,
      amount_max: metadata.amount_max,
      payment_methods: metadata.payment_methods,
      rating: metadata.rating
    })
  
  // Sort by rating (highest first)
  listings.sort(by: rating, desc)
  
  return listings
```

**Electrum JSON-RPC:**
```json
{
  "method": "blockchain.nft.list_category",
  "params": {
    "category": "01010102..."  // Asgaya listing category ID
  }
}
```

**Note:** `blockchain.nft.list_category` is not a standard Electrum method. Actual implementation would query token-aware UTXO listings (exact method depends on Electrum server capabilities). Some implementations may require client-side scanning of outputs with the Asgaya token category prefix.

**Response:**
```json
{
  "result": [
    {
      "tx_hash": "abc123...",
      "tx_pos": 0,
      "height": 850000,
      "value": 100000,
      "token": {
        "category": "01010102...",
        "nft": {
          "commitment": "7b2276657273696f6e223a312c..."  // Hex-encoded JSON
        }
      }
    },
    ...
  ]
}
```

---

## Filtering and Matching

### Client-Side Filtering

**Why client-side?** No backend server, full privacy

**Filters:**
1. **Currency:** María wants EUR, filter out VES/USD/etc.
2. **Amount:** María needs €100, filter sellers with min > €100
3. **Payment method:** María uses Bizum, filter sellers without Bizum
4. **Location:** For cash-in-person, filter by proximity (e.g., "Madrid")

**Pseudocode:**
```
function findMatchingListings(my_amount, my_currency, my_payment_method, my_location):
  all_listings = queryListings(my_currency, type="sell")
  
  matched = []
  
  for listing in all_listings:
    // Check amount range
    if my_amount < listing.amount_min or my_amount > listing.amount_max:
      continue
    
    // Check payment method overlap
    if my_payment_method not in listing.payment_methods:
      continue
    
    // Check location for cash-in-person
    if my_payment_method == "cash":
      if not listing.location or listing.location != my_location:
        continue  // Skip if no location match
    
    matched.push(listing)
  
  return matched
```

**UI sorting options:**
- Best rating first (query reputation UTXO separately)
- Lowest amount first
- Highest amount first
- Recently listed

**Note:** Seller ratings are queried from separate on-chain reputation UTXOs after filtering, not from listing metadata.

---

## Caching Strategy

### Problem
Querying Electrum every time user opens app is slow (500ms-2s per query)

### Solution: Cache + TTL

**Pseudocode:**
```
function getCachedListings(currency, type):
  cache_key = "listings_" + currency + "_" + type
  
  cached = getFromCache(cache_key)
  
  if cached and (now() - cached.timestamp < CACHE_TTL):
    return cached.listings
  
  // Cache miss or expired - query Electrum
  fresh_listings = queryListings(currency, type)
  
  setCache(cache_key, {
    listings: fresh_listings,
    timestamp: now()
  })
  
  return fresh_listings
```

**Cache TTL:** 5 minutes (Phase 0)
- Listings don't change rapidly
- 5 minutes balances freshness vs performance
- User can manually refresh

**Cache invalidation:**
- After 5 minutes (automatic)
- When user creates/deletes their own listing
- Manual refresh button

**Storage:** In-memory cache (no persistence needed)

---

## Delisting (Removing a Listing)

### How to Delist

**Problem:** Isabel wants to stop selling (ran out of BCH)

**Solution:** Spend the NFT UTXO (removes it from query results)

**Pseudocode:**
```
function delistListing(listing_id):
  // Find the NFT UTXO
  utxo = getUTXO(listing_id)
  
  if not utxo:
    return error("Listing not found")
  
  // Spend it (send back to self, burn NFT, reclaim deposit)
  tx = createTransaction({
    inputs: [{utxo: utxo}],
    outputs: [
      {
        value: utxo.value,  // Return 0.001 BCH deposit to self
        script: p2pkh(my_address)
        // No token (NFT burned)
      }
    ]
  })
  
  broadcast(tx)
  
  return success("Listing removed, €1.00 deposit returned")
```

**Cost:** ~€0.0002 (transaction fee)

**Deposit returned:** 0.001 BCH (~€1.00) goes back to seller

**Effect:** Listing disappears from bulletin board immediately (after confirmation)

---

## Error Handling

### Listing Creation Failures

```
if metadata_size > 128_BYTES:
  show_error("Listing too complex. Reduce text (max 128 bytes).")

if insufficient_funds:
  show_error("Need ~€1.00 deposit + €0.001 fee to create listing (deposit reclaimable).")

if network_error:
  queue_for_retry(listing_creation)
  show_message("Offline. Listing queued.")
```

### Query Failures

```
try:
  listings = queryListings(currency, type)
catch ElectrumTimeout:
  // Use cached results if available
  if has_cached_results:
    show_warning("Using cached results (network slow)")
    return cached_listings
  else:
    show_error("Network unavailable. Try again later.")
```

### Invalid Metadata

```
for utxo in electrum_results:
  try:
    metadata = json_decode(utxo.commitment)
    validate_metadata_schema(metadata)
  catch ParseError:
    // Skip malformed listings (don't crash)
    log_warning("Invalid listing: " + utxo.txid)
    continue
```

### Expired Listings

```
if metadata.expires_at < now():
  // Silently filter out (don't show to user)
  continue

if metadata.expires_at - now() < 1_DAY:
  // Show warning to listing owner
  show_warning("Your listing expires in " + time_remaining(metadata.expires_at))
```

---

## Platform-Specific Notes

### Android
- **Caching:** LruCache for in-memory storage
- **Background refresh:** WorkManager for periodic cache refresh (optional)
- **Offline support:** Cached listings available offline (stale but usable)

### iOS
- **Caching:** NSCache for in-memory storage
- **Background refresh:** Background app refresh (if enabled by user)
- **Offline support:** Same as Android

### Web/Desktop
- **Caching:** localStorage for persistence (optional)
- **Refresh:** Manual refresh button (no background workers)
- **Privacy note:** localStorage less secure than mobile (encrypt if persisting)

---

## Testing Strategy

### Unit Tests
- Metadata serialization (JSON encoding)
- Filtering logic (currency, amount, payment method)
- Cache TTL expiration
- Schema validation (reject invalid metadata)

### Integration Tests (Testnet)
- Create listing on testnet
- Query listing by currency
- Filter by amount range
- Delist listing (spend NFT UTXO)

### Edge Cases
- Malformed JSON in NFT commitment (should skip, not crash)
- Expired listings (should not appear)
- Empty bulletin board (no listings for currency)
- Very large bulletin board (1000+ listings, performance test)

---

## Performance Considerations

### Query Performance

**Problem:** Querying 1000+ NFTs might be slow

**Optimizations:**
1. **Pagination:** Electrum supports offset/limit (query 50 at a time)
2. **Indexing:** Electrum indexes by category (fast lookup)
3. **Caching:** Reduces queries to 1 per 5 minutes

**Expected performance:**
- First query: 500ms-2s (cold cache)
- Subsequent queries: <10ms (cache hit)
- Pagination: +200ms per page

### Metadata Size Optimization

**Problem:** 128-byte limit requires compact encoding

**Tips for developers:**
- Use short currency codes (EUR, not "Euro")
- Use integer amounts (100, not "one hundred euros")
- Omit optional fields if not needed (expires_at defaults to 30 days)
- Use abbreviated payment method codes ("biz" = Bizum, "sep" = SEPA, "pm" = PagoMóvil)
- Use short location names ("MAD" instead of "Madrid" for cash-in-person)

**Example (compact, without location):**
```json
{"v":1,"t":"sell","c":"EUR","min":100,"max":500,"pm":["biz","sep"],"ca":"Isabel#142","exp":1735689600}
```
**Size:** 98 bytes

**Example (with location for cash-in-person):**
```json
{"v":1,"t":"sell","c":"EUR","min":100,"max":500,"pm":["cash"],"ca":"Isabel#142","loc":"MAD","exp":1735689600}
```
**Size:** 107 bytes

**Both fit comfortably within 128-byte limit**

---

## Related Components

**Uses:**
- Wallet (my_address, transaction creation)
- Electrum servers (NFT UTXO queries)

**Used by:**
- [nostr.md](nostr.md) - Contact seller after finding listing
- [state-management.md](state-management.md) - Track my own listings

**Interacts with:**
- [notification-bot.md](notification-bot.md) - Passive sellers have active listings

---

**Status:** Phase 0 - Implementation pending  
**Updated:** 2026-06-25  
**Complexity:** Low (simple Electrum queries + filtering)  
**Research:** See RS070 (implementation documentation strategy)
---

## Navigation

**[🏠 Home](../../../index.md)** | **[↑ Android App](README.md)** | **[📖 Glossary](../../../glossary.md)**
