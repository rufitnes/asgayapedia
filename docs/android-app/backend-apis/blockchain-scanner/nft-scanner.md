# NFT-Based Discovery Scanner

**Category:** Core Blockchain Operations
**Priority:** 🔴 Critical (Phase 0)
**Related:** [Covenant Creation](../covenant-creation.md), [BCH-Native Architecture](../bch-native-architecture.md)

---

## Overview

Asgaya has **no bulletin board server**. Instead, mobile apps discover available sellers and merchants by scanning the Bitcoin Cash blockchain for CashTokens NFTs.

**Discovery mechanism:**
1. Query blockchain for UTXOs by NFT category (`ASGAYA_SELLER_V1`, `ASGAYA_MERCHANT_V1`)
2. Decode NFT commitment (128-byte data containing offer details)
3. Check UTXO balance (availability signal)
4. Display in app (Screen 4.5 seller selection, recipient merchant map)

**No server needed!** The blockchain IS the bulletin board.

---

## NFT Categories

### ASGAYA_SELLER_V1

**Purpose:** BCH seller signals availability + payment methods

**NFT commitment structure (128 bytes):**
```
Bytes 0-32:   seller_pubkey (33 bytes compressed)
Bytes 33-34:  payment_methods bitmask (2 bytes)
              0x01 = Bizum
              0x02 = SEPA
              0x04 = ATM deposit
              0x08 = Cash meetup
Bytes 35-36:  min_eur (2 bytes, e.g., 0x000A = €10)
Bytes 37-38:  max_eur (2 bytes, e.g., 0x01F4 = €500)
Bytes 39-40:  fee_bps (2 bytes, e.g., 0x0032 = 50 bps = 0.5%)
Bytes 41-80:  contact_info (40 bytes)
              - Nostr pubkey (32 bytes)
              - Endpoint URL hash (8 bytes)
Bytes 81-127: reserved (47 bytes for future use)
```

**UTXO structure:**
```javascript
{
  tx_hash: "abc123...",
  tx_pos: 0,
  value: 50000000,  // 0.5 BCH available for sale
  token_data: {
    category: "ASGAYA_SELLER_V1",
    commitment: "03abc123...def...",  // 128 bytes hex
    amount: 0,  // NFT (non-fungible)
    nft: {
      capability: "minting",  // Can update commitment
      commitment: "..."
    }
  }
}
```

**Interpretation:**
- **UTXO exists** = Seller online
- **value > 0** = BCH available for purchase
- **value = 0** = Out of stock (offline)

---

### ASGAYA_MERCHANT_V1

**Purpose:** Merchant signals cash-out availability

**NFT commitment structure (128 bytes):**
```
Bytes 0-32:   merchant_pubkey (33 bytes compressed)
Bytes 33-36:  location_lat (4 bytes, signed int, e.g., 10.5° = 105000000)
Bytes 37-40:  location_lon (4 bytes, signed int, e.g., -66.9° = -669000000)
Bytes 41-42:  hours_open (2 bytes bitmask)
              Bit 0-6: Days (Mon-Sun)
              Bit 8-15: Hours (0-23)
Bytes 43-50:  cash_float_ves (8 bytes, available VES cash)
Bytes 51-80:  merchant_name (30 bytes UTF-8)
Bytes 81-127: reserved (47 bytes)
```

**UTXO structure:**
```javascript
{
  tx_hash: "def456...",
  tx_pos: 0,
  value: 1000,  // Dust (signal only, not for sale)
  token_data: {
    category: "ASGAYA_MERCHANT_V1",
    commitment: "03def456...ghi...",  // 128 bytes hex
    amount: 0,  // NFT
    nft: {
      capability: "minting",
      commitment: "..."
    }
  }
}
```

**Interpretation:**
- **UTXO exists** = Merchant open/available
- **cash_float_ves > 0** = Has VES cash for payouts
- **UTXO spent** = Merchant closed/offline

---

## Querying NFTs via Electrum

### Setup Electrum Client

```typescript
import { ElectrumCluster } from 'electrum-cash';

// Connect to public Electrum server
const electrum = new ElectrumCluster('Asgaya Mobile App', '1.5', 1, 3);
await electrum.startup();

// Use public servers (no self-hosted needed!)
await electrum.addServer('fulcrum.fountainhead.cash', 50002, 'ssl');
await electrum.addServer('bch.imaginary.cash', 50002, 'ssl');
```

---

### Query All Sellers

```typescript
/**
 * Get all available BCH sellers
 * 
 * Returns sellers sorted by:
 * 1. Availability (online first)
 * 2. Available BCH amount (most first)
 * 3. Fee (lowest first)
 */
async function getAvailableSellers(): Promise<Seller[]> {
  // Query UTXOs with ASGAYA_SELLER_V1 category
  const utxos = await electrum.request(
    'blockchain.tokeninfo.utxos_by_category',
    ['ASGAYA_SELLER_V1']
  );

  // Decode each NFT commitment
  const sellers = utxos.map(utxo => {
    const commitment = Buffer.from(utxo.token_data.commitment, 'hex');
    
    return {
      seller_id: commitment.subarray(0, 33).toString('hex'),  // Pubkey
      available_bch: utxo.value / 1e8,  // Satoshis to BCH
      payment_methods: decodePaymentMethods(commitment.readUInt16BE(33)),
      limits: {
        min: commitment.readUInt16BE(35),  // Min EUR
        max: commitment.readUInt16BE(37)   // Max EUR
      },
      fee: commitment.readUInt16BE(39) / 10000,  // Basis points to decimal
      contact: {
        nostr: commitment.subarray(41, 73).toString('hex'),
        endpoint: commitment.subarray(73, 81).toString('hex')
      },
      status: utxo.value > 0 ? '🟢 Online' : '🔴 Out of stock',
      utxo_ref: `${utxo.tx_hash}:${utxo.tx_pos}`
    };
  });

  // Sort: online first, most BCH, lowest fee
  return sellers.sort((a, b) => {
    if (a.available_bch > 0 && b.available_bch === 0) return -1;
    if (a.available_bch === 0 && b.available_bch > 0) return 1;
    if (a.available_bch !== b.available_bch) return b.available_bch - a.available_bch;
    return a.fee - b.fee;
  });
}

// Helper: Decode payment methods bitmask
function decodePaymentMethods(bitmask: number): string[] {
  const methods = [];
  if (bitmask & 0x01) methods.push('Bizum');
  if (bitmask & 0x02) methods.push('SEPA');
  if (bitmask & 0x04) methods.push('ATM');
  if (bitmask & 0x08) methods.push('Cash');
  return methods;
}
```

**Example response:**
```javascript
[
  {
    seller_id: '03abc123...',
    available_bch: 0.5,
    payment_methods: ['Bizum', 'SEPA'],
    limits: { min: 10, max: 500 },
    fee: 0.005,  // 0.5%
    contact: {
      nostr: 'npub1abc...',
      endpoint: '7f8a9b...'
    },
    status: '🟢 Online',
    utxo_ref: 'abc123...:0'
  },
  {
    seller_id: '03def456...',
    available_bch: 0.3,
    payment_methods: ['Bizum'],
    limits: { min: 20, max: 300 },
    fee: 0.004,  // 0.4%
    contact: { ... },
    status: '🟢 Online',
    utxo_ref: 'def456...:0'
  }
]
```

**Display in Screen 4.5:**
```
Available Sellers (2 online)

┌─────────────────────────────┐
│ Seller#abc1 🟢              │
│ 0.5 BCH available           │
│ Payment: Bizum, SEPA        │
│ Limits: €10-500             │
│ Fee: 0.5%                   │
│ [Select]                    │
└─────────────────────────────┘

┌─────────────────────────────┐
│ Seller#def4 🟢              │
│ 0.3 BCH available           │
│ Payment: Bizum              │
│ Limits: €20-300             │
│ Fee: 0.4% (Cheapest!)       │
│ [Select]                    │
└─────────────────────────────┘
```

---

### Query Nearby Merchants

```typescript
/**
 * Get merchants near recipient's location
 * 
 * @param userLat - User's latitude (e.g., 10.5)
 * @param userLon - User's longitude (e.g., -66.9)
 * @param radiusKm - Search radius in kilometers
 */
async function getNearbyMerchants(
  userLat: number,
  userLon: number,
  radiusKm: number = 5
): Promise<Merchant[]> {
  // Query all merchant NFTs
  const utxos = await electrum.request(
    'blockchain.tokeninfo.utxos_by_category',
    ['ASGAYA_MERCHANT_V1']
  );

  // Decode and filter by distance
  const merchants = utxos
    .map(utxo => {
      const commitment = Buffer.from(utxo.token_data.commitment, 'hex');
      
      // Decode location (signed int, 7 decimal precision)
      const lat = commitment.readInt32BE(33) / 1e7;
      const lon = commitment.readInt32BE(37) / 1e7;
      
      // Calculate distance (haversine formula)
      const distance = calculateDistance(userLat, userLon, lat, lon);
      
      return {
        merchant_id: commitment.subarray(0, 33).toString('hex'),
        location: { lat, lon },
        distance_km: distance,
        hours: decodeHours(commitment.readUInt16BE(41)),
        cash_float: commitment.readBigUInt64BE(43),  // VES available
        name: commitment.subarray(51, 81).toString('utf8').trim(),
        status: utxo.value > 0 ? '🟢 Open' : '🔴 Closed',
        utxo_ref: `${utxo.tx_hash}:${utxo.tx_pos}`
      };
    })
    .filter(m => m.distance_km <= radiusKm && m.status === '🟢 Open');

  // Sort by distance (nearest first)
  return merchants.sort((a, b) => a.distance_km - b.distance_km);
}

// Helper: Calculate distance between two coordinates
function calculateDistance(
  lat1: number, lon1: number,
  lat2: number, lon2: number
): number {
  const R = 6371; // Earth radius in km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}

// Helper: Decode hours bitmask
function decodeHours(bitmask: number): string {
  const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  const openDays = days.filter((_, i) => bitmask & (1 << i));
  const hours = (bitmask >> 8) & 0xFF;  // Start hour
  return `${openDays.join(', ')} ${hours}:00-${hours+8}:00`;
}
```

**Example response:**
```javascript
[
  {
    merchant_id: '03ghi789...',
    location: { lat: 10.4806, lon: -66.9036 },
    distance_km: 0.8,
    hours: 'Mon-Sat 9:00-17:00',
    cash_float: 500000,  // 500,000 VES available
    name: 'Bodega Central',
    status: '🟢 Open',
    utxo_ref: 'ghi789...:0'
  },
  {
    merchant_id: '03jkl012...',
    location: { lat: 10.4912, lon: -66.8798 },
    distance_km: 2.3,
    hours: 'Mon-Fri 10:00-18:00',
    cash_float: 1000000,
    name: 'Tienda Elena',
    status: '🟢 Open',
    utxo_ref: 'jkl012...:0'
  }
]
```

**Display in Recipient Map:**
```
Nearby Merchants (2 within 5 km)

📍 Bodega Central
   0.8 km away
   Open: Mon-Sat 9:00-17:00
   Cash available: 500,000 VES
   [Navigate] [Select]

📍 Tienda Elena
   2.3 km away
   Open: Mon-Fri 10:00-18:00
   Cash available: 1,000,000 VES
   [Navigate] [Select]
```

---

## Caching Strategy

### Client-Side Cache (30 seconds)

**Why cache:**
- Reduce Electrum queries (network bandwidth)
- Faster UI updates (instant seller list)
- Still fresh enough (sellers don't change often)

**Implementation:**
```typescript
interface NFTCache {
  sellers: Seller[];
  merchants: Merchant[];
  timestamp: number;
}

const CACHE_DURATION = 30_000;  // 30 seconds

let cache: NFTCache | null = null;

async function getSellersWithCache(): Promise<Seller[]> {
  // Check cache
  if (cache && (Date.now() - cache.timestamp) < CACHE_DURATION) {
    console.log('Using cached sellers');
    return cache.sellers;
  }

  // Query blockchain
  console.log('Fetching fresh sellers from blockchain');
  const sellers = await getAvailableSellers();

  // Update cache
  cache = {
    sellers,
    merchants: cache?.merchants || [],
    timestamp: Date.now()
  };

  return sellers;
}
```

---

## Real-Time Updates (Optional - Phase 1+)

### Subscribe to UTXO Changes

**Electrum supports subscriptions:**
```typescript
// Subscribe to all ASGAYA_SELLER_V1 updates
electrum.subscribe(
  async (method, params) => {
    if (method === 'blockchain.tokeninfo.subscribe') {
      const [category, utxo] = params;
      
      if (category === 'ASGAYA_SELLER_V1') {
        console.log('Seller NFT updated:', utxo);
        
        // Invalidate cache
        cache = null;
        
        // Refresh seller list in UI
        const sellers = await getAvailableSellers();
        updateSellerListUI(sellers);
      }
    }
  },
  'blockchain.tokeninfo.subscribe',
  ['ASGAYA_SELLER_V1']
);
```

**Use case:**
- Seller goes offline (UTXO spent) → UI updates automatically
- Seller updates fee (NFT commitment changed) → UI reflects new fee
- New seller joins (new UTXO created) → Appears in list

**Phase 0:** Polling every 30s is sufficient  
**Phase 1:** Add subscriptions for real-time updates

---

## Error Handling

### Electrum Connection Failures

```typescript
async function getSellersWithFallback(): Promise<Seller[]> {
  try {
    return await getAvailableSellers();
  } catch (error) {
    console.error('Electrum query failed:', error);
    
    // Fallback to cached data (even if stale)
    if (cache && cache.sellers.length > 0) {
      console.warn('Using stale cache (Electrum unavailable)');
      return cache.sellers;
    }
    
    // No cache available
    throw new Error('SELLERS_UNAVAILABLE: Cannot connect to blockchain');
  }
}
```

**Display error to user:**
```
⚠️ Cannot connect to blockchain

Using cached sellers from 5 minutes ago.
Tap to retry.

[Retry] [Continue Anyway]
```

---

### No Sellers Available

```typescript
const sellers = await getAvailableSellers();

if (sellers.length === 0) {
  // No sellers online
  showError({
    title: 'No sellers available',
    message: 'There are no BCH sellers online right now. Try again later or use your own BCH.',
    actions: ['Use My BCH', 'Try Again', 'Cancel']
  });
}
```

---

### Invalid NFT Commitment

```typescript
function decodeSellerNFT(commitment: Buffer): Seller | null {
  try {
    // Validate commitment length
    if (commitment.length !== 128) {
      console.warn('Invalid commitment length:', commitment.length);
      return null;
    }

    // Validate pubkey (first 33 bytes)
    const pubkey = commitment.subarray(0, 33);
    if (!isValidPubkey(pubkey)) {
      console.warn('Invalid seller pubkey');
      return null;
    }

    // Decode successfully
    return {
      seller_id: pubkey.toString('hex'),
      // ... rest of decoding
    };
  } catch (error) {
    console.error('Failed to decode NFT commitment:', error);
    return null;  // Skip this seller
  }
}

// Filter out invalid sellers
const sellers = utxos
  .map(utxo => decodeSellerNFT(Buffer.from(utxo.token_data.commitment, 'hex')))
  .filter(seller => seller !== null);
```

---

## Testing (Phase 0)

### Regtest Seeding Script

**Seed test NFTs on pichan regtest:**

```bash
#!/bin/bash
# scripts/seed-test-nfts.sh

echo "Creating test seller NFTs..."

# Seller 1: Bizum + SEPA, €10-500, 0.5% fee
cashscript deploy SellerLiquidity \
  --network regtest \
  --value 50000000 \  # 0.5 BCH
  --nft-category ASGAYA_SELLER_V1 \
  --nft-commitment "03abc123...[payment_methods: 0x03][min: 0x000A][max: 0x01F4][fee: 0x0032]..."

# Seller 2: Bizum only, €20-300, 0.4% fee
cashscript deploy SellerLiquidity \
  --network regtest \
  --value 30000000 \  # 0.3 BCH
  --nft-category ASGAYA_SELLER_V1 \
  --nft-commitment "03def456...[payment_methods: 0x01][min: 0x0014][max: 0x012C][fee: 0x0028]..."

echo "Creating test merchant NFTs..."

# Merchant 1: Bodega Central (Caracas)
cashscript deploy MerchantAvailability \
  --network regtest \
  --value 1000 \  # Dust
  --nft-category ASGAYA_MERCHANT_V1 \
  --nft-commitment "03ghi789...[lat: 10.4806][lon: -66.9036][float: 500000][name: Bodega Central]..."

# Merchant 2: Tienda Elena (Caracas)
cashscript deploy MerchantAvailability \
  --network regtest \
  --value 1000 \
  --nft-category ASGAYA_MERCHANT_V1 \
  --nft-commitment "03jkl012...[lat: 10.4912][lon: -66.8798][float: 1000000][name: Tienda Elena]..."

# Mine block to confirm
bitcoin-cli --regtest generatetoaddress 1 $(bitcoin-cli --regtest getnewaddress)

echo "✓ Test NFTs seeded! Query with electrum on pichan:50002"
```

**Mobile app queries pichan:**
```typescript
// Connect to local regtest
const electrum = new ElectrumCluster('Asgaya Husk', '1.5');
await electrum.addServer('192.168.1.42', 50002, 'tcp');  // pichan regtest

// Should see 2 sellers, 2 merchants
const sellers = await getAvailableSellers();
console.log(`Found ${sellers.length} sellers`);  // 2

const merchants = await getNearbyMerchants(10.48, -66.90, 10);
console.log(`Found ${merchants.length} merchants`);  // 2
```

---

## Performance Considerations

### Query Optimization

**Problem:** Querying all NFTs can be slow (1000s of UTXOs)

**Solutions:**

1. **Cache aggressively** (30s client-side)
2. **Background refresh** (query while user browses)
3. **Paginate results** (show top 10 sellers only)
4. **Index by location** (Phase 1: query only nearby merchants)

**Typical query times:**
- Electrum query: ~200-500ms
- Decode 100 NFTs: ~50ms
- Sort + filter: ~10ms
- **Total: <1 second** (acceptable for Phase 0)

---

## Security Considerations

### NFT Commitment Validation

**Always validate decoded data:**

```typescript
function validateSellerNFT(seller: Seller): boolean {
  // Fee reasonable (0.1% - 5%)
  if (seller.fee < 0.001 || seller.fee > 0.05) {
    console.warn('Suspicious fee:', seller.fee);
    return false;
  }

  // Limits reasonable (€1 - €10,000)
  if (seller.limits.min < 1 || seller.limits.max > 10000) {
    console.warn('Suspicious limits:', seller.limits);
    return false;
  }

  // Min < Max
  if (seller.limits.min >= seller.limits.max) {
    console.warn('Invalid limits:', seller.limits);
    return false;
  }

  return true;
}

// Filter out suspicious sellers
const validSellers = sellers.filter(validateSellerNFT);
```

### Spam NFTs

**Problem:** Anyone can create ASGAYA_SELLER_V1 NFT (permissionless!)

**Defense:** Require minimum UTXO value

```typescript
const MIN_SELLER_BCH = 0.01;  // Minimum 0.01 BCH to be visible

const sellers = utxos
  .filter(utxo => utxo.value >= MIN_SELLER_BCH * 1e8)  // Filter spam
  .map(decodeSellerNFT);
```

**Result:** Seller must lock ≥€5 BCH to appear in list (spam prevention)

---

## Related Documentation

- **Covenant Creation:** [covenant-creation.md](../covenant-creation.md) - How sellers create NFT covenants
- **BCH-Native Architecture:** [bch-native-architecture.md](../bch-native-architecture.md) - NFT design rationale
- **User Flows:** [Sender Flow](../../flows/remittance-merchant-cash-out.md) - Where seller selection happens
- **Phase 0:** [Progressive Decentralization](../../../decisions/phase-0-progressive-decentralization.md)

---

*Created: May 16, 2026*  
*Philosophy: Blockchain IS the bulletin board. No server needed.*  
*Phase 0: Query Electrum for NFTs, decode commitments, display in app.*  
*Innovation: CashTokens NFTs = permissionless seller/merchant discovery*
