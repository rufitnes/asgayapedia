# Stability Layer Component

**Purpose:** Detect H€/HAu tokens, create AnyHedge contracts, mint/burn stable tokens, check pool capacity

**Complexity:** Medium - CashTokens integration + AnyHedge oracle interaction

> **Phase 0+ - Launch First, Add Based on Observed Need**
> 
> **Phasing strategy:**
> 1. **Phase 0 launch** - BCH only, no stability layer
> 2. **Observe merchant behavior** - Do they cash out immediately? Hold BCH? Ask for stability?
> 3. **Phase 0+ implementation** - Add H€/HAu if merchants demonstrate need
> 
> **Why this approach:**
> - **Learn from reality** - Watch actual merchant behavior, not assumptions
> - **Avoid premature optimization** - Don't build features that may not be needed
> - **Resource allocation** - Focus Phase 0 on core flows (remittances working)
> - **Data-driven decision** - Real usage patterns inform feature priority
> 
> **Hypothesis to test:** "Venezuelan merchants want stable value vs BCH volatility speculation"
> 
> **Status:** Design complete, implementation deferred until merchant behavior observed.

---

## Overview

The Stability Layer enables users to hold EUR/Gold-pegged tokens instead of volatile BCH:
- **H€ (Hedged Euro):** 1 H€ = 1 EUR (always)
- **HAu (Hedged Gold):** 1 HAu = 1 gram gold (always)
- **Mechanism:** AnyHedge contracts (on-chain, non-custodial)

**Phase 0+:** Optional enhancement after launch (add if merchant behavior demonstrates need)

**Hypothesis:** Venezuelan merchants want stable value, not BCH volatility speculation.

---

## Phase 0 H€ Minting Policy (Compliance)

**Regulatory constraint:** H€ must remain a **utility token**, not a money substitute.

**Design implication:** Limit H€ minting to specific, justifiable use cases where hedging is legitimate:

### Minting Triggers (Phase 0)

| Covenant Path | Minting? | Justification |
|---------------|----------|---------------|
| **merchantCashout()** | ✅ Yes | Merchant has EUR fiat, needs BCH hedge |
| **abort()** | ✅ Yes | Sender loses BCH exposure, needs EUR hedge |
| **claim()** | ❌ No | Recipient gets BCH (no hedge needed) |
| **refund()** | ❌ No | Sender gets BCH back (no hedge needed) |
| **sellerRecoverBuffer()** | ❌ No | Seller gets BCH back (no hedge needed) |

### On-Chain Detection

**How to distinguish minting-eligible paths:**

```javascript
function shouldMintH€(transaction) {
    const outputCount = transaction.outputs.length;
    
    if (outputCount === 1) {
        // abort() path - single output to sender
        return {
            mint: true,
            recipient: transaction.outputs[0].address,
            reason: 'abort - emergency price protection'
        };
    }
    
    if (outputCount === 2) {
        // Check signatures to distinguish paths
        if (hasMerchantSignature(transaction)) {
            // merchantCashout() path
            return {
                mint: true,
                recipient: getMerchantAddress(transaction),
                reason: 'merchantCashout - fiat hedge conversion'
            };
        }
        // claim/refund/sellerRecoverBuffer - no minting
        return { mint: false };
    }
}
```

**Critical insight:** Output count (1 vs 2) + signature analysis provides on-chain proof for minting eligibility.

**Compliance proof:** If BCH price stabilizes, H€ minting stops (proves utility nature, not money substitute).

**Reference:** [Covenant v2.6 - abort() design](../covenants/version-history.md#v26-emergency-abort--overlap-zone-fund-locking-fix)

---

## What Is AnyHedge?

### Non-Custodial Hedging

**Definition:** BCH smart contract where two parties bet on price movement

**Participants:**
1. **Hedge (Elena):** Wants stable EUR value (protected from BCH volatility)
2. **Long (Speculator):** Wants BCH exposure (takes on Elena's volatility risk)

**Contract example:**
```
- Elena locks 0.02 BCH (worth €100 today)
- Speculator locks 0.02 BCH (€100 collateral)
- Oracle reports BCH/EUR price every hour
- If BCH drops 10%: Elena keeps €100 worth, speculator gets remainder
- If BCH rises 10%: Elena still keeps €100 worth, speculator gets gains
```

**Elena's result:** Always has €100 worth of BCH (stable value)

**No middleman:** Contract enforced by BCH script + oracle signatures

---

## H€/HAu Token Architecture

### CashTokens Implementation

**What:** BCH native tokens (fungible, like ERC-20 but better)

**H€ token:**
```
Token {
  category: H€_TOKEN_CATEGORY,  // Fixed identifier
  amount: 10000,  // 100.00 H€ (2 decimal places)
  nft: null  // Fungible token (no NFT)
}
```

**HAu token:**
```
Token {
  category: HAu_TOKEN_CATEGORY,  // Different from H€
  amount: 5000,  // 50.00 HAu (2 decimal places)
  nft: null
}
```

**Storage:** Tokens live in BCH UTXOs alongside satoshis

---

## Detecting H€/HAu in Wallet

### Query Balance

**What:** Check if user has H€/HAu tokens

**Pseudocode:**
```
function getStableTokenBalance():
  wallet_utxos = getWalletUTXOs()
  
  h€_balance = 0
  hau_balance = 0
  
  for utxo in wallet_utxos:
    if utxo.token:
      if utxo.token.category == H€_TOKEN_CATEGORY:
        h€_balance += utxo.token.amount
      else if utxo.token.category == HAu_TOKEN_CATEGORY:
        hau_balance += utxo.token.amount
  
  return {
    h€: h€_balance / 100,  // Convert to decimal (10000 → 100.00 H€)
    hau: hau_balance / 100
  }
```

**Display in UI:**
```
Wallet Balance:
- 0.05 BCH (€243.50)
- 100.00 H€ (€100.00)
- 50.00 HAu (€3,250.00 at current gold price)
```

---

## Minting H€/HAu (Hedging)

### When to Mint

**User wants stability:**
- Elena receives €100 BCH from covenant
- Elena converts to 100 H€ (locks in EUR value)
- Elena holds H€ until ready to spend

**Trigger:** User clicks "Convert BCH → H€"

---

### Minting Process

**Phase 0: Asgaya Bull Pool (Simplified)**

**What:** Single liquidity pool provides speculator side of AnyHedge contracts

**Capacity:** ~€1,800 (60% of Phase 0 budget)

**Pseudocode:**
```
function checkBullPoolCapacity(amount_eur):
  pool_capacity = queryBullPoolCapacity()  // Background query, cached
  
  if pool_capacity >= amount_eur:
    return {available: true, capacity: pool_capacity}
  else:
    return {available: false, capacity: pool_capacity}
```

**UI behavior:**
- Query runs in background on app launch
- "Convert BCH → H€" button only shown if pool has capacity
- Avoids user disappointment ("Sorry, no liquidity")

**Queue for fairness (Phase 0+):**
```
if multiple_users_want_to_mint and pool_capacity_insufficient:
  // First-come-first-served queue
  queue_minting_request(user, amount_eur, timestamp)
  
  when new_liquidity_added_to_pool:
    process_queue_oldest_first()
```

**Example:**
- Pool has €500 capacity
- 3 users want to mint: €300, €400, €200
- First user (€300) gets minted immediately
- Pool now has €200 capacity
- Second user (€400) queued (insufficient capacity)
- Third user (€200) gets minted immediately
- Pool now has €0 capacity
- When pool refilled, second user's request processed

**Phase 0 allocation (reference implementation):**
- Total budget: €3,000
- Passive BCH seller (covenants): €500
- Passive BCH buyer (VES): €226
- Reserve (refunds): €500
- **Bull pool (stability): €1,800** ← 60% of capital

**Phase 1+ alternative: Open Marketplace**

For comparison, Phase 1+ could support open speculator marketplace:

```
function findAnyHedgeSpeculator(amount_eur, duration_hours):
  // Query AnyHedge marketplace for available speculators
  offers = anyHedgeQuery("marketplace.list_offers", {
    type: "long",  // Looking for speculators
    currency: "EUR",
    min_amount: amount_eur,
    max_duration: duration_hours
  })
  
  if offers.length == 0:
    return null  // No speculator available
  
  // Sort by best terms (lowest fees)
  offers.sort(by: fee_percent, asc)
  
  return offers[0]
```

**Trade-off:**
- Bull pool: Simple UX, requires capital, limited by pool size
- Marketplace: Complex UX, no capital needed, unlimited capacity

**Phase 0 choice:** Bull pool (simplicity over scale)

**Step 2: Create AnyHedge contract**
```
function createAnyHedgeContract(amount_eur, speculator_offer):
  current_price = getOraclePrice("BCH/EUR")
  bch_amount = amount_eur / current_price
  
  contract = {
    hedge_party: my_address,
    long_party: speculator_offer.address,
    hedge_amount: bch_amount,
    long_amount: bch_amount,  // 1:1 collateral ratio
    start_price: current_price,
    duration: speculator_offer.duration,
    oracle: speculator_offer.oracle_pubkey,
    settlement_type: "mutual"  // Both parties can settle early
  }
  
  // Sign contract
  contract_tx = buildAnyHedgeTx(contract)
  signed_contract = signTransaction(contract_tx)
  
  // Speculator signs their side
  fully_signed = await_speculator_signature(signed_contract)
  
  // Broadcast
  tx_id = broadcast(fully_signed)
  
  return {
    contract_id: tx_id,
    contract: contract
  }
```

**Step 3: Mint H€ tokens**
```
function mintH€FromContract(contract_id, amount_eur):
  // Create minting transaction
  mint_tx = createTransaction({
    inputs: [
      {contract_utxo: contract_id}  // Spend from AnyHedge contract
    ],
    outputs: [
      {
        value: 1000,  // Dust satoshis
        script: p2pkh(my_address),
        token: {
          category: H€_TOKEN_CATEGORY,
          amount: amount_eur * 100  // 100 EUR → 10000 token units
        }
      }
    ]
  })
  
  // Sign with contract terms (oracle signature required)
  signed_mint = signWithOracleAttestation(mint_tx, contract_id)
  
  broadcast(signed_mint)
  
  return success("Minted " + amount_eur + " H€")
```

**Result:** User now has 100 H€ tokens in wallet (stable EUR value)

---

## Burning H€/HAu (Converting Back to BCH)

### When to Burn

**User wants BCH:**
- Elena has 100 H€
- Elena wants to spend BCH (merchant doesn't accept H€)
- Elena burns 100 H€ → gets ~€100 BCH (at current price)

**Trigger:** User clicks "Convert H€ → BCH"

**BUT: Trading is preferred over burning**

**Better option:**
- Elena lists 100 H€ on bulletin board: "Sell 100 H€ for €100 EUR via Bizum"
- María buys Elena's H€ (needs stable tokens for next remittance)
- Elena gets EUR instantly, María gets H€ instantly
- No burn needed, tokens circulate

**Burn economics (Phase 0):**

**Auto-renewing contracts:**
- Phase 0 uses 7-day contracts that auto-renew unless user exits
- Contract renews automatically → Elena keeps stable H€ indefinitely
- Exit anytime: free at expiry, small fee if early

**Free burning at contract expiry:**
- Elena mints 100 H€ (7-day AnyHedge contract)
- Day 7: Contract expires → burn free, get BCH back
- Or: Let contract auto-renew → hold H€ another 7 days

**Early exit fee (before expiry):**
```
days_remaining = contract_expiry - now()
total_days = 7  // 7-day contract duration

early_exit_fee_percent = (days_remaining / total_days) * 0.5%

Example:
- Day 1 (6 days left): 6/7 × 0.5% = 0.43% fee
- Day 4 (3 days left): 3/7 × 0.5% = 0.21% fee  
- Day 7 (0 days left): 0/7 × 0.5% = 0% fee (free)
```

**Fee goes to:** Bull pool speculator (compensation for early settlement)

**Dual purpose of exit fee:**
1. Fair compensation to speculator for early contract termination
2. Encourages token circulation (trade H€ peer-to-peer instead of burning)

**Effect:** Natural nudge toward trading (instant + free) vs burning (delayed OR expensive)

**Why this works:**
- Burning releases BCH from bull pool (good for liquidity)
- But trading keeps tokens circulating (better for ecosystem)
- Fee structure encourages patience OR trading
- Emergency exit still possible (pay fee for convenience)

---

### Burning Process

**Step 1: Settle AnyHedge contract**
```
function settleAnyHedgeContract(contract_id):
  contract = getContract(contract_id)
  
  current_price = getOraclePrice("BCH/EUR")
  start_price = contract.start_price
  
  // Calculate payouts
  if current_price < start_price:
    // BCH dropped - hedge party gets more BCH
    hedge_payout = (contract.hedge_amount * start_price) / current_price
    long_payout = (contract.hedge_amount + contract.long_amount) - hedge_payout
  else:
    // BCH rose - hedge party gets less BCH (but same EUR value)
    hedge_payout = (contract.hedge_amount * start_price) / current_price
    long_payout = (contract.hedge_amount + contract.long_amount) - hedge_payout
  
  // Build settlement transaction
  settlement_tx = createTransaction({
    inputs: [{contract_utxo: contract_id}],
    outputs: [
      {value: hedge_payout, script: p2pkh(contract.hedge_party)},
      {value: long_payout, script: p2pkh(contract.long_party)}
    ]
  })
  
  // Sign with oracle attestation (proves price)
  signed_settlement = signWithOracleAttestation(settlement_tx, current_price)
  
  broadcast(signed_settlement)
  
  return hedge_payout
```

**Step 2: Burn H€ tokens**
```
function burnH€(amount_h€):
  // Find UTXOs with H€ tokens
  h€_utxos = findTokenUTXOs(H€_TOKEN_CATEGORY, amount_h€ * 100)
  
  // Create burn transaction (spend tokens, don't create new token output)
  burn_tx = createTransaction({
    inputs: h€_utxos,
    outputs: [
      {
        value: sum(h€_utxos.value),  // Dust satoshis return to user
        script: p2pkh(my_address)
        // No token field (tokens burned)
      }
    ]
  })
  
  broadcast(burn_tx)
  
  return success("Burned " + amount_h€ + " H€")
```

**Result:** User receives BCH (EUR-equivalent at current price)

---

## Pool Capacity Checking

### Problem

**Limited liquidity:** AnyHedge speculators (long parties) have finite capital

**Example:**
- Total speculator pool: €50,000
- Elena wants to hedge €60,000
- Not enough capacity → Elena must wait or split hedge

---

### Check Capacity

**Pseudocode:**
```
function checkAnyHedgeCapacity(amount_eur, currency):
  offers = anyHedgeQuery("marketplace.list_offers", {
    type: "long",
    currency: currency
  })
  
  total_capacity = 0
  
  for offer in offers:
    total_capacity += offer.max_amount
  
  if total_capacity >= amount_eur:
    return {available: true, capacity: total_capacity}
  else:
    return {available: false, capacity: total_capacity, shortage: amount_eur - total_capacity}
```

**UI feedback:**
```
if not capacity.available:
  show_warning("Only €" + capacity.capacity + " available for hedging. Try smaller amount or wait.")
```

---

## Oracle Integration

### What Is the Oracle?

**Definition:** Trusted price feed (BCH/EUR, BCH/XAU)

**AnyHedge oracles:**
- General Protocols (primary)
- Alternative oracles (backup)

**How it works:**
- Oracle signs price data every hour
- Signature proves price at specific time
- Contract settlement uses oracle signature

---

### Strategic Insight: Gold Shops as Early Adopters (Phase 0+)

**Observation:** Physical gold dealers could be natural HAu adopters

**Why gold shops make sense:**
- **Spain:** "Compro oro" shops common in every city
- **Venezuela:** Gold merchants common (VES collapse → gold refuge)
- **They already:** Buy/sell physical gold, understand hedging, have cash on hand

**Potential use cases:**

1. **Cash-in-person BCH sellers:**
   - List on Asgaya: "Buy BCH €100-1,000, cash in person, Madrid"
   - Receive BCH → buy HAu from users
   - HAu = digital gold exposure (easier to hold than physical)

2. **Remittance off-ramp (Venezuela):**
   - Recipient brings HAu (received from remittance)
   - Gold shop buys HAu for VES or physical gold
   - Natural fit: Gold merchants already accumulate gold

**Why HAu specifically:**
- 1 HAu = 1 gram gold (direct mapping to their business)
- Hedging feels natural (they're already gold bulls)
- Could hold HAu as inventory (digital gold reserves)

**Challenges:**
- Taxation (needs research - is HAu taxed like gold or currency?)
- Regulatory (gold shops already have AML requirements, BCH might add complexity)
- Education (need to explain CashTokens + AnyHedge)

**Phase 0+ opportunity:** Target 2-3 gold shops as pilot users, measure adoption

---

### Query Oracle Price

**Pseudocode:**
```
function getOraclePrice(pair):
  // Query General Protocols oracle
  response = http_get("https://oracle.generalprotocols.com/price/" + pair)
  
  price_data = {
    pair: pair,
    price: response.price,
    timestamp: response.timestamp,
    signature: response.signature
  }
  
  // Verify signature
  if not verify_oracle_signature(price_data):
    return error("Invalid oracle signature")
  
  return price_data.price
```

**Response example:**
```json
{
  "pair": "BCH/EUR",
  "price": 485.32,
  "timestamp": 1735689600,
  "signature": "3045022100..."
}
```

**Why General Protocols for Phase 0:**
- Established BCH oracle (used by AnyHedge)
- Cryptographic signatures (tamper-proof)
- Reliable uptime
- Standard integration

---

### Alternative Oracle Approaches (Phase 1+)

**Option A: Custom Multi-Exchange Oracle**

If General Protocols charges fees, build own oracle:

```
function getMultiExchangePrice(pair):
  prices = []
  
  // Query multiple exchanges
  prices.push(krakenAPI.getPrice("BCH/EUR"))
  prices.push(binanceAPI.getPrice("BCH/EUR"))
  prices.push(coinbaseAPI.getPrice("BCH/EUR"))
  prices.push(bitfinexAPI.getPrice("BCH/EUR"))
  
  // Use median (resistant to single exchange manipulation)
  median_price = median(prices)
  
  return median_price
```

**Advantages:**
- No oracle fees
- Harder to manipulate (need to move market on 4 exchanges)
- Real-time (query on demand)

**Disadvantages:**
- API rate limits (might need caching)
- Exchange downtime risk (use 5+ exchanges for redundancy)
- No cryptographic proof (trust exchanges)

---

**Option B: Asgaya as Oracle (Phase 2+)**

Use Asgaya's own transaction prices as oracle (requires high volume)

**Requirements:**
1. Transaction every 5 minutes (minimum)
2. Volume-weighted average price (VWAP), not last-price
3. Manipulation defense bot (Asgaya steps in if price diverges >5% from exchanges)

**Attack vector:**
- Alice manipulates Asgaya price to €400/BCH (real: €500)
- Bob's bot detects, buys €1,000 BCH at €400 (€200 profit)
- This affects minting (users get bad AnyHedge contract rates)

**Defense strategy:**
```
function getAsgayaOraclePrice():
  asgaya_vwap = calculate_vwap_last_100_transactions()
  exchange_median = getMultiExchangePrice("BCH/EUR")
  
  if abs(asgaya_vwap - exchange_median) > exchange_median * 0.05:  // >5% divergence
    log_alert("Price manipulation detected")
    
    // Asgaya bot steps in (uses bull pool funds)
    if asgaya_vwap < exchange_median:
      // Asgaya price too low - buy BCH to raise price
      asgaya_bot_buys_bch(amount_to_correct_price)
    else:
      // Asgaya price too high - sell BCH to lower price
      asgaya_bot_sells_bch(amount_to_correct_price)
    
    // Use external oracle until manipulation cleared
    return exchange_median
  
  return asgaya_vwap
```

**Advantages:**
- No oracle dependency
- Reflects actual Asgaya market conditions
- Community-driven price discovery

**Disadvantages:**
- Requires high volume (200+ transactions/day)
- Needs manipulation defense (costs capital)
- Bootstrap problem (Phase 0 low volume)

**Verdict:** Phase 2+ only, when volume high + defense bot ready + community confident

---

## Error Handling

### No Speculator Available

```
if not findAnyHedgeSpeculator(amount, duration):
  show_error("No speculators available for this amount. Try again later or reduce amount.")
  suggest_action("Join Telegram channel for updates on speculator availability")
```

### Insufficient Pool Capacity

```
capacity = checkAnyHedgeCapacity(amount, currency)

if not capacity.available:
  show_error("Only €" + capacity.capacity + " hedging capacity available (you need €" + amount + ")")
  suggest_action("Hedge €" + capacity.capacity + " now, wait for more capacity later")
```

### Oracle Unavailable

```
try:
  price = getOraclePrice("BCH/EUR")
catch OracleTimeout:
  show_error("Price oracle unavailable. Cannot create hedge contract.")
  suggest_action("Try again in a few minutes")
```

### Contract Settlement Fails

```
try:
  settleAnyHedgeContract(contract_id)
catch SettlementError:
  // Contract might be liquidated (price moved too far)
  show_error("Contract settlement failed. Check contract status.")
  // Manual intervention needed
```

---

## Platform-Specific Notes

### Android
- **CashTokens library:** bitcoincashj with CashTokens support
- **AnyHedge SDK:** Use General Protocols SDK (if available) or raw protocol
- **Background monitoring:** Check contract expiry (WorkManager periodic task)

### iOS
- **CashTokens library:** BitcoinKit (Swift, CashTokens support)
- **AnyHedge SDK:** Same as Android (cross-platform REST API)

### Web/Desktop
- **CashTokens library:** cashscript-sdk (JavaScript)
- **AnyHedge SDK:** REST API (language-agnostic)

---

## Testing Strategy

### Unit Tests
- Token balance calculation
- Minting/burning logic
- Payout calculation (price up/down scenarios)
- Oracle signature verification

### Integration Tests (Testnet)
- Create AnyHedge contract on testnet
- Mint H€ tokens
- Wait for price change (or mock oracle)
- Settle contract
- Burn tokens

### Edge Cases
- Pool capacity exhausted
- Oracle goes offline mid-contract
- Contract liquidation (price moved beyond collateral)
- Multiple contracts per user (portfolio management)

---

## Phase 0 Scope

**Included:**
- Detect H€/HAu balance
- Display stable token value in UI
- Basic minting via Asgaya bull pool
- Basic burning (with early exit fee)

**Deferred to Phase 1+:**
- Automatic hedging suggestions
- Multi-contract management
- Advanced speculator matching (open marketplace)

**Alternative client features (not Phase 0):**
- **Portfolio rebalancing:** Automatically adjust holdings to maintain target ratios (e.g., keep 50% BCH, 50% H€)
  - **Who needs this:** Professional traders, market makers, arbitrageurs
  - **Who doesn't:** Remittance users (María just sends, Elena just receives)
  - **Example:** Elena has 60% BCH, 40% H€ → app suggests "Rebalance to 50/50?"
  - **Phase 2+ feature:** Could be added to advanced clients (not core UX)

**Why defer most features?** Phase 0 focuses on remittance flow (BCH → BCH). Stability layer is enhancement, not critical path.

---

## Related Components

**Uses:**
- [wallet.md](wallet.md) - UTXO management, transaction signing
- Electrum servers (CashTokens UTXO queries)
- AnyHedge oracles (price feeds)
- General Protocols API (speculator marketplace)

**Used by:**
- UI layer (display stable token balances)

**Optional for:**
- [notification-bot.md](notification-bot.md) - Could auto-hedge received BCH (Phase 1+)

---

**Status:** Phase 0+ - Design complete, implementation deferred (add based on observed merchant behavior)  
**Updated:** 2026-08-04  
**Complexity:** Medium (CashTokens + AnyHedge integration)  
**Strategy:** Launch without this, watch merchant behavior, add if they demonstrate need for stability  
**Hypothesis to test:** Do Venezuelan merchants want stable tokens vs immediate cashout?
---

## Navigation

**[🏠 Home](../../../index.md)** | **[↑ Android App](README.md)** | **[📖 Glossary](../../../glossary.md)**
