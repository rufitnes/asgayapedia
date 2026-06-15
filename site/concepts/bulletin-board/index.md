# The Asgaya Bulletin Board

**Status:** Core Infrastructure  
**Phase:** 0 (Foundation)  
**Related:** [BCH Sellers](bch-sellers.md), [Merchant Business Case](merchant-business-case.md), [Fee Splitting Model](../decisions/fee-splitting-model.md)

---

## What It Is

The **Asgaya Bulletin Board** is a decentralized coordination layer for matching participants who want to exchange BCH and fiat.

**Key properties:**
- ✅ **On-chain storage:** All listings are NFT UTXOs on the Bitcoin Cash blockchain
- ✅ **Permissionless:** Anyone can post a listing by creating an NFT covenant
- ✅ **No central server:** Discovered by querying Electrum nodes for specific NFT categories
- ✅ **Censorship-resistant:** No gatekeeper can remove listings
- ✅ **Verifiable:** Listing data committed in NFT minting transaction

**Not a website.** Not a database. Not a company. Just **searchable on-chain data** that coordinates a peer-to-peer market.

---

## Two Listing Types (and Only Two)

The bulletin board has exactly **two fundamental roles:**

### 1. BCH Sellers (Provide BCH → Receive Fiat)

**Who they are:**
- BCH miners with mining rewards to deploy
- HODLers with existing BCH holdings
- Traders looking to earn fees on capital
- Participants with bank accounts in sender countries (Spain, USA, etc.)

**What they do:**
- Post BCH availability on bulletin board
- Lock their own BCH into covenants when sender pays fiat
- Earn 0.5% seller fee + volatility buffer surplus
- Enable remittances by providing BCH liquidity

**NFT listing format:**
```
Category: ASGAYA_SELLER_V1
Commitment fields:
├─ payment_methods: ["Bizum", "SEPA", "bank_transfer"]
├─ corridors: ["EUR-VES", "USD-VES"]
├─ limits: { min: €50, max: €500 }
├─ fee_rate: 0.5%
├─ buffer_rate: 7%
└─ contact: CashAccount or notification endpoint
```

**Discovered by:** Senders looking to fund a remittance covenant

**See:** [BCH Sellers](bch-sellers.md) for detailed economics

---

### 2. BCH Buyers (Provide Fiat → Receive BCH)

**Who they are:**
- **Merchants** providing cash at physical shops
- **Online buyers** providing fiat via bank transfer
- **Local buyers** in recipient countries (Venezuela, Honduras, etc.)
- Participants with bank accounts in recipient countries

**What they do:**
- Post fiat availability on bulletin board
- Pay fiat to merchants/participants who want to convert BCH
- Receive BCH via covenant after payment verified
- Earn profit from spread (typically 0.2-0.5%)

**NFT listing format:**
```
Category: ASGAYA_BUYER_V1
Commitment fields:
├─ payment_methods: ["cash", "PagoMóvil", "bank_transfer"]
├─ location: "Caracas, Calle Libertad 123" (if cash)
├─ hours: "Mon-Sat 9am-6pm" (if cash)
├─ corridors: ["EUR-VES", "USD-VES"]
├─ limits: { min: $10, max: $200 }
├─ spread: 0.3%
└─ contact: CashAccount or notification endpoint
```

**Discovered by:** 
- Merchants cashing out BCH from covenants
- Recipients who claimed BCH and want local fiat
- Anyone needing to convert BCH to fiat

---

## Payment Method Variants

Both listing types support multiple payment methods as **attributes**, not separate types:

### For BCH Sellers (fiat input)
| Payment Method | Description | Typical Use |
|---------------|-------------|-------------|
| **Bizum** | Spain instant bank transfer | Spanish senders |
| **SEPA Instant** | EU instant bank transfer | European senders |
| **Revolut** | Digital bank instant transfer | UK/EU senders |

**Note:** All payment methods must support instant or near-instant settlement for the notification listener bot to work effectively. Traditional SEPA (1-2 days) is not suitable.

### For BCH Buyers (fiat output)
| Payment Method | Description | Typical Use |
|---------------|-------------|-------------|
| **Cash** | Physical cash at shop | Recipients, merchants cashing out |
| **PagoMóvil** | Venezuelan instant transfer | Venezuelan recipients |
| **Bank transfer** | Wire to local bank | Any recipient country |
| **Mobile money** | M-Pesa, etc. | African corridors |

**Key insight:** A **merchant is just a BCH buyer with `payment_method: "cash"`** and a physical location.

---

## Merchants = BCH Buyers with Cash

**Why merchants are BCH buyers:**

When a recipient visits a merchant to claim their remittance:
1. Recipient triggers covenant claim
2. **Merchant provides cash** (fiat out)
3. **Merchant receives BCH** (from covenant)
4. Merchant earns 0.5% fee in BCH

**This is exactly the BCH buyer flow:**
- Buyer provides fiat → receives BCH
- Payment method: "cash at shop"
- Location required: physical address

**On the bulletin board:**
```
Listing Type: ASGAYA_BUYER_V1
Payment Method: "cash"
Location: "Bodega Rosa, Caracas, Av. Libertador 456"
Hours: "Mon-Sat 8am-8pm"
Spread: 0.5% (merchant fee)
```

**Merchants are NOT a special third category.** They're BCH buyers with a specific payment rail.

---

## Multi-Role Patterns

Participants can post **multiple listings** to play multiple roles:

### Pattern 1: The Double-Dip (Seller + Buyer)

**Participant:** Someone with bank accounts in **both** Spain and Venezuela

**Two listings:**
```
1. ASGAYA_SELLER_V1
   - payment_method: "Bizum"
   - corridor: "EUR-VES"
   - Earns: 0.5% seller fee
   
2. ASGAYA_BUYER_V1
   - payment_method: "PagoMóvil"
   - corridor: "EUR-VES"
   - Earns: 0.3% spread
```

**Capital flow:**
```
1. Seller listing: Receive €100 Bizum from sender in Spain
2. Lock €107 BCH into covenant (7% buffer)
3. Covenant matures → Merchant in Venezuela receives €99.50 BCH
4. Buyer listing: Buy €99.50 BCH from merchant for VES
5. Pay merchant via PagoMóvil
6. Receive BCH back into wallet
7. Net: €0.50 seller fee + €0.30 buyer spread = €0.80 per cycle
```

**Result:** Same BCH recycled through sender→merchant→buyer loop, earning fees on both sides.

---

### Pattern 2: The Triple-Dip (Merchant + Seller + Product Margin)

**Participant:** Merchant in Venezuela with **family member in Spain**

**Three revenue streams:**
```
1. ASGAYA_BUYER_V1 (Merchant role)
   - payment_method: "cash"
   - location: "Bodega Rosa, Caracas"
   - Earns: 0.5% merchant fee
   
2. ASGAYA_SELLER_V1 (via family in Spain)
   - payment_method: "Bizum" (family member's account)
   - corridor: "EUR-VES"
   - Earns: 0.5% seller fee
   
3. Product Sales (not on bulletin board)
   - Recipient buys groceries during visit
   - Earns: 15-30% product margin
```

**Capital flow:**
```
1. Seller listing (via family): Receive €100 Bizum in Spain
2. Lock €107 BCH into covenant
3. Covenant matures → Merchant receives €99.50 BCH
4. Merchant hands cash to recipient (buyer role)
5. Recipient buys groceries (product margin)
6. Merchant recycles BCH via seller listing again
7. Net: €0.50 merchant + €0.50 seller + €15 groceries = €16 per cycle
```

**Result:** Maximum earnings by playing all three roles.

---

### Pattern 3: Pure Arbitrage (Multiple Buyer Listings)

**Participant:** Online BCH accumulator

**Multiple buyer listings:**
```
1. ASGAYA_BUYER_V1
   - payment_method: "PagoMóvil" (Venezuela)
   - spread: 0.3%
   
2. ASGAYA_BUYER_V1
   - payment_method: "bank_transfer" (Honduras)
   - spread: 0.4%
   
3. ASGAYA_BUYER_V1
   - payment_method: "M-Pesa" (Kenya, future)
   - spread: 0.5%
```

**Strategy:** Accumulate BCH across multiple corridors, profit from local spreads.

---

## How Listings Work (Technical)

### Creating a Listing

**Step 1: Mint NFT covenant**
```
UTXO created with:
├─ NFT category: ASGAYA_SELLER_V1 or ASGAYA_BUYER_V1
├─ Commitment: JSON with listing parameters
├─ Value: Minimum BCH amount (anti-spam)
└─ Covenant: Spending rules (how to remove listing)
```

**Step 2: Broadcast to mempool**
- Transaction propagates to BCH network
- Confirmed in next block (~10 minutes)
- Now discoverable via Electrum queries

**Step 3: Keep UTXO alive**
- As long as UTXO exists, listing is active
- To remove: Spend the UTXO (covenant allows owner to reclaim)
- To update: Spend old UTXO, create new one with updated commitment

---

### Discovering Listings

**App queries Electrum node:**
```javascript
// Find all BCH sellers accepting Bizum in EUR-VES corridor
electrum.query({
  category: "ASGAYA_SELLER_V1",
  filter: {
    payment_methods: "Bizum",
    corridors: "EUR-VES"
  }
})

// Returns array of UTXOs with commitment data
[
  {
    txid: "abc123...",
    vout: 0,
    commitment: {
      payment_methods: ["Bizum", "SEPA"],
      corridors: ["EUR-VES"],
      fee_rate: 0.5,
      contact: "seller$asgaya.org"
    }
  },
  // ... more listings
]
```

**No central API.** No website. Just **standard Electrum queries** for NFT categories.

---

### Updating a Listing

**To change parameters (price, limits, contact):**
1. Spend existing listing UTXO
2. Create new UTXO with updated commitment
3. Old listing disappears, new one appears

**To pause listing temporarily:**
- Spend UTXO, don't create new one
- Can recreate later with same or different parameters

**To permanently remove:**
- Spend UTXO, reclaim BCH value
- Listing gone from bulletin board

---

## Reputation and Trust (Phase 1+)

**Phase 0:** Trusted participants only (family, friends, forum members)

**Phase 1+:** On-chain reputation system
```
Each participant's CashAccount accumulates:
├─ Total transactions: 47
├─ Success rate: 98% (46/47)
├─ Average response time: 12 minutes
├─ Volume: €12,400 BCH transacted
└─ Complaints: 1 (disputed, resolved)
```

**Stored where:** Additional NFT commitment in listing UTXO

**Enforced how:** App filters listings by minimum reputation threshold

**See:** [Universal Bot Fraud Prevention](universal-bot-fraud-prevention.md) for complete mechanism

---

## Why Two Types Is Enough

**Every participant flow maps to one of these roles:**

| Scenario | Bulletin Board Role | Payment Method |
|----------|-------------------|---------------|
| Sender needs BCH for remittance | Queries **BCH Sellers** | Bizum, SEPA |
| Merchant cashes out BCH | Queries **BCH Buyers** (or posts as seller) | PagoMóvil, cash |
| Recipient wants cash | Queries **BCH Buyers** (merchants) | Cash at shop |
| Recipient wants to sell BCH | Queries **BCH Buyers** (online) | Bank transfer |
| Trader accumulating BCH | Posts as **BCH Buyer** | Any fiat rail |
| Miner deploying BCH | Posts as **BCH Seller** | Any fiat rail |

**No third category needed.** Payment methods and locations are attributes, not types.

---

## Comparison to Traditional P2P Markets

| Feature | LocalBitcoins / Binance P2P | Asgaya Bulletin Board |
|---------|----------------------------|----------------------|
| **Storage** | Central database | On-chain NFT UTXOs |
| **Discovery** | Website API | Electrum queries |
| **Censorship** | Platform can ban users | Permissionless |
| **Listing cost** | Free (platform subsidized) | Minimal BCH (anti-spam) |
| **Trust model** | Platform escrow | Covenant + reputation |
| **Regulation** | Platform liable (KYC required) | No intermediary (MiCA compliant) |
| **Availability** | Depends on platform uptime | Depends on BCH network |

**Key difference:** Asgaya has **no platform** to shut down. The bulletin board is the blockchain itself.

---

## Anti-Spam Measures

**Problem:** Free listing creation → spam attacks

**Solution:** Minimum BCH value required in listing UTXO
```
Listing UTXO must contain:
├─ NFT commitment (listing data)
└─ ≥ 0.001 BCH (~€0.50 at current prices)

To spam with 1000 fake listings:
Cost = 1000 × €0.50 = €500
```

**Economic barrier:** Spam becomes expensive, legitimate listings cost pennies.

**Reclaim on removal:** When spending listing UTXO, BCH value returns to creator (minus tx fee).

**Phase 0 baseline:** This is the starting approach. Additional strategies (listing limits, device fingerprinting, payment method verification) are under consideration.

**See:** [Unknown: Bulletin Board Anti-Spam Strategies](../unknowns/bulletin-board-anti-spam.md) for full analysis of alternatives and testing plan.

---

## Privacy Considerations

**What's public (on-chain):**
- ✅ NFT category (SELLER or BUYER)
- ✅ Payment methods accepted
- ✅ Corridors served
- ✅ Fee rates and limits
- ✅ Contact info (CashAccount or endpoint)

**What's private (off-chain):**
- ❌ Participant's real identity (unless using real name in contact)
- ❌ Actual transaction amounts (covenant executions are separate UTXOs)
- ❌ Customer list (who you've transacted with)

**Pseudonymous by default.** Link your CashAccount to real identity only if you want to (merchants need location, online traders don't).

---

## Covenant Integration

**The bulletin board coordinates, covenants execute:**

### BCH Seller Flow (Sender → Seller)
```
1. Sender finds seller on bulletin board
2. Sender creates covenant with seller's details (on-chain)
3. Sender pays fiat to seller (off-chain)
4. Seller's bot detects payment, co-signs covenant
5. BCH released to sender's designated recipient
```

**Key:** The sender (active user) creates the covenant, not the seller (listing owner).

### BCH Buyer Flow (Recipient → Merchant)
```
1. Recipient finds merchant/buyer on bulletin board
2. Recipient creates covenant with merchant's details (on-chain)
3. Recipient receives fiat from merchant (off-chain)
4. Merchant's bot detects receipt, co-signs covenant
5. BCH released to merchant
```

**Key:** The recipient (active user) creates the covenant, not the merchant (listing owner).
```

**Bulletin board = discovery layer**  
**Covenants = execution layer**

They're separate but complementary.

---

## Future Enhancements

### V1.1: Advanced Filtering
- Reputation scores (success rate, response time)
- Geographic radius search (find merchants within 5km)
- Payment method preferences (cash only, bank only)
- Time-of-day availability (24/7 vs business hours)

### V1.2: Notification Subscriptions
- Subscribe to new listings matching criteria
- Push notifications when desired listing appears
- Automated bot responses (accept offer instantly)

### V1.3: Multi-Corridor Optimization
- BCH Buyer posts: "Buying in VES or HNL, best rate wins"
- App calculates optimal corridor based on spread + forex
- Cross-corridor arbitrage opportunities highlighted

### V2: Reputation & Merchant Highlighting

**Merchant Priority (Phase 0+):**

Merchant listings should be highlighted/prioritized because:
- **Physical location = reputation at stake** (can't disappear like online-only traders)
- **Easy to audit** (visit shop, verify business license)
- **Professional service** (ready documentation for accounting, regular hours)
- **Adoption engine goal** (we want merchants to earn money and promote Asgaya)

**Implementation:**
- "Verified Merchant" badge (requires proof of physical location)
- Sort order: Merchants first, then other traders
- Map view: Show merchant locations prominently

**Anti-spam benefit:** Merchants less likely to spam (reputation risk), so highlighting them reduces spam visibility.

**General Reputation (Phase 1+):**
- Web of trust (vouch for other participants)
- Social graph integration (prefer friends' listings)
- Dispute resolution history (how conflicts resolved)
- Trade count & volume (established traders ranked higher)

---

## Implementation Status

**Phase 0 (Current):**
- ⏳ **Pending:** NFT covenant implementation on Chipnet
- ⏳ **Pending:** Electrum query interface in Android app
- ⏳ **Pending:** Listing creation UI (simple form → NFT mint)

**Blockers:**
- None (standard BCH NFT functionality)

**Timeline:**
- Chipnet testing: May 2026
- Mainnet Phase 0: June 2026 (trusted participants only)

---

## Related Documentation

- **[BCH Sellers](bch-sellers.md)** — Economics of providing BCH liquidity
- **[Merchant Business Case](merchant-business-case.md)** — Why merchants are BCH buyers with cash
- **[Fee Splitting Model](../decisions/fee-splitting-model.md)** — How fees are distributed between seller, buyer, and merchant roles
- **[Bounty Contracts with Volatility Buffer](bounty-contracts-with-volatility-buffer.md)** — Technical covenant implementation
- **[Universal Bot Fraud Prevention](universal-bot-fraud-prevention.md)** — Reputation and fraud prevention

---

## Key Takeaways

1. **Two listing types:** BCH Sellers (BCH→fiat) and BCH Buyers (fiat→BCH)
2. **Merchants are BCH buyers** with `payment_method: "cash"` — not a special third type
3. **On-chain storage:** NFT UTXOs on BCH blockchain, no central server
4. **Permissionless:** Anyone can post, no gatekeeper required
5. **Multi-role:** Post multiple listings to earn fees on both sides (double-dip, triple-dip)
6. **Privacy:** Pseudonymous by default, reveal identity only if desired
7. **Covenant integration:** Bulletin board discovers, covenants execute

**The bulletin board is not a product. It's a protocol.** Just like DNS for domain names or BitTorrent for file sharing — it's infrastructure that anyone can use, no permission required.
