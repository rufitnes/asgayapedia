# Distributed Monitoring: Censorship-Resistant Price Oracle

**Pattern:** Global price watch channel where all active covenant holders participate. Network effect: more users = better resolution for everyone.

**Oracle Architecture:** Oracles broadcast prices over Nostr (not HTTP API). Permissionless, censorship-resistant, multi-source consensus.

**Purpose:** Enable automatic refund on price drops with instant propagation, decentralized oracles, and network-scale redundancy.

---

## The Breakthrough

**Problem:** Auto-refund requires fast price detection, but:
- Constant HTTP polling is expensive (battery, API calls)
- Centralized oracle API is censorable
- Single oracle is a trust bottleneck

**Traditional solution:** Each device polls central oracle every 10-20 seconds (expensive, centralized, fragile).

**Distributed solution:** 
- **Oracles broadcast to Nostr** (not HTTP API) - censorship-resistant
- **All devices subscribe** to oracle channels - real-time push, no polling
- **Devices contribute samples** to global price watch - network effect
- **Multi-oracle consensus** - permissionless, trustless

**Result:**
- ⚡ Sub-second resolution (scales with users)
- 💰 Low cost per device (1 contribution per minute)
- 🛡️ Massive redundancy (hundreds of monitors, multiple oracles)
- 🔔 Instant propagation (Nostr pub/sub)
- 🚫 Censorship-resistant (can't block Nostr relays)
- 🔓 Permissionless (anyone can run oracle)

---

## How It Works

### Two-Layer Architecture

**Layer 1: Oracles Broadcast Prices (Push)**

Multiple independent oracles broadcast BCH/EUR prices to Nostr:

```
Oracle A (Coinbase):  asgaya:oracle:coinbase:bch-eur
Oracle B (Kraken):    asgaya:oracle:kraken:bch-eur
Oracle C (Bitstamp):  asgaya:oracle:bitstamp:bch-eur
```

Oracles broadcast every 10-30 seconds. **Devices subscribe (no polling!).**

**Layer 2: Devices Contribute Samples (Participate)**

All devices with active covenants:
1. Subscribe to oracle channels (receive real-time prices)
2. Subscribe to global price watch channel (see network samples)
3. Contribute their own price samples (staggered, once per minute)
4. Monitor their covenants against current consensus price

**Result:** Decentralized, censorship-resistant, multi-source price feed.

---

### Network Effect Scaling

**The magic:** Resolution improves as network grows, **cost per device stays constant**.

**10 active covenants (30 devices total):**
```
Each device contributes once per minute
30 samples / 60 seconds = 1 sample every 2 seconds
Resolution: 2-second
```

**100 active covenants (300 devices total):**
```
Each device contributes once per minute
300 samples / 60 seconds = 5 samples per second
Resolution: 200ms
```

**1,000 active covenants (3,000 devices total):**
```
Each device contributes once per minute
3,000 samples / 60 seconds = 50 samples per second
Resolution: 20ms
```

**Individual cost:** 1 contribution per minute (constant, doesn't increase with network size)

**Network benefit:** Resolution improves with every new user

---

### Example Timeline (100 Active Covenants)

```
t=0.0s: Device #001 broadcasts price sample → 995 EUR/BCH
        All 300 devices see update instantly

t=0.2s: Device #002 broadcasts price sample → 995 EUR/BCH

t=0.4s: Device #003 broadcasts price sample → 994 EUR/BCH

... (samples every 200ms from 300 devices)

t=30.0s: Oracle A broadcasts → 993 EUR/BCH (Coinbase)
         Oracle B broadcasts → 995 EUR/BCH (Kraken)
         Oracle C broadcasts → 994 EUR/BCH (Bitstamp)
         Devices calculate median: 994 EUR/BCH ✅

t=120.4s: Device #147 broadcasts → 920 EUR/BCH ❌
          Checks covenant threshold (930 EUR/BCH)
          DROP DETECTED!
          Broadcasts PRICE_DROP_ALERT to global channel

t=120.5s: ALL 300 DEVICES see alert (100ms later)
          Devices with affected covenants execute refunds
          Others just update UI: "Market volatility detected"

t=120.6s: First refund transaction broadcast
          Other devices see covenant UTXO spent → abort refund attempts

t=121.0s: 5 more devices confirm detection (redundancy check)
```

**Detection latency:** 200ms (from price change to network knows)  
**Propagation:** 100ms (Nostr relay latency)  
**Total:** Sub-second detection and response

---

## Nostr Message Schema

### Channel Types

**Global Price Watch Channel:**
```
asgaya:pricewatch
```
All devices with active covenants subscribe to this channel. Devices contribute price samples (staggered intervals) and see network-wide price updates.

**Oracle Broadcast Channels:**
```
asgaya:oracle:coinbase:bch-eur
asgaya:oracle:kraken:bch-eur
asgaya:oracle:bitstamp:bch-eur
```
Multiple independent oracles broadcast prices to Nostr. Devices subscribe to multiple oracles for consensus.

**Per-Covenant Coordination Channels:**
```
asgaya:covenant:<covenantId>
```
Example: `asgaya:covenant:bchtest:pwyclxrkdh2ndlsgfz89y4kc5zewcvf5lfdldcp6vy`

All three devices (sender, recipient, seller) subscribe to covenant-specific channel for coordination messages (alerts, claim/refund broadcasts).

---

### Message Types

#### 1. PRICE_DROP_ALERT

**When:** Any device detects price drop >7%

**Payload:**
```json
{
  "type": "PRICE_DROP_ALERT",
  "covenantId": "bchtest:pwyclx...",
  "timestamp": 1721937723,
  "currentPrice": 920,
  "initialPrice": 1000,
  "threshold": 930,
  "dropPercent": 8,
  "device": "sender",
  "action": "TRIGGERING_REFUND"
}
```

**Effect:**
- Other devices immediately know price dropped
- All devices prepare for refund transaction
- Recipient knows payment will abort

---

#### 2. TIMEOUT_ALERT

**When:** Any device detects covenant expired

**Payload:**
```json
{
  "type": "TIMEOUT_ALERT",
  "covenantId": "bchtest:pwyclx...",
  "timestamp": 1721937723,
  "expiryTime": 1721937600,
  "device": "seller",
  "action": "TRIGGERING_REFUND"
}
```

**Effect:**
- All devices know covenant expired
- Recipient knows can no longer claim
- Refund transaction imminent

---

#### 3. REFUND_BROADCAST

**When:** Device broadcasts refund transaction

**Payload:**
```json
{
  "type": "REFUND_BROADCAST",
  "covenantId": "bchtest:pwyclx...",
  "timestamp": 1721937725,
  "txid": "dd743868a0c19c2c...",
  "device": "sender",
  "reason": "PRICE_DROP"
}
```

**Effect:**
- Other devices stop monitoring (refund in progress)
- All devices watch for confirmation
- Prevents duplicate refund attempts

---

#### 4. REFUND_CONFIRMED

**When:** Refund transaction confirmed on-chain

**Payload:**
```json
{
  "type": "REFUND_CONFIRMED",
  "covenantId": "bchtest:pwyclx...",
  "timestamp": 1721937735,
  "txid": "dd743868a0c19c2c...",
  "confirmations": 1,
  "device": "recipient"
}
```

**Effect:**
- All devices stop monitoring covenant
- Sender knows BCH returned to wallet
- Seller knows buffer returned
- Recipient knows payment cancelled

---

#### 5. CLAIM_BROADCAST

**When:** Recipient broadcasts claim transaction

**Payload:**
```json
{
  "type": "CLAIM_BROADCAST",
  "covenantId": "bchtest:pwyclx...",
  "timestamp": 1721935000,
  "txid": "a3bbf89a895c4e7e...",
  "device": "recipient"
}
```

**Effect:**
- All devices stop monitoring price (claim in progress)
- Sender knows payment succeeded
- Seller knows buffer will be returned

---

#### 6. CLAIM_CONFIRMED

**When:** Claim transaction confirmed on-chain

**Payload:**
```json
{
  "type": "CLAIM_CONFIRMED",
  "covenantId": "bchtest:pwyclx...",
  "timestamp": 1721935010,
  "txid": "a3bbf89a895c4e7e...",
  "confirmations": 1,
  "device": "sender"
}
```

**Effect:**
- All devices stop monitoring covenant
- Sender knows payment delivered
- Recipient knows payment received
- Seller knows buffer returned

---

## Coordination Example: Price Drop Abort

```
t=0:     Covenant funded, initial price €1000/BCH
         All devices subscribe to oracle channels + price watch

t=0-120: Oracles broadcast stable prices €995-990/BCH
         Network contributes samples (200ms resolution)
         No threshold crossed

t=120.0: Coinbase oracle broadcasts €920/BCH (8% drop)
         ALL 300 devices see oracle broadcast simultaneously

t=120.2: Kraken oracle broadcasts €918/BCH
         Bitstamp oracle broadcasts €922/BCH
         Consensus median: €920/BCH

t=120.3: ALL devices detect >7% drop (consensus < €930 threshold)
         María's device broadcasts PRICE_DROP_ALERT to covenant channel
         → Elena: "⚠️ Payment cancelled - price drop"
         → Isabel: "⚠️ Covenant aborting - buffer will return"

t=120.5: María's device broadcasts REFUND_BROADCAST
         (includes H€ minting transaction)

t=121.0: Elena's device detects confirmation, broadcasts REFUND_CONFIRMED
         All devices stop monitoring
```

**Detection latency:** 300ms (oracle broadcast → consensus → alert)  
**Coordination:** Sub-second (oracle-over-Nostr push model)

---

## Failure Modes

| Failure | What Happens | Result |
|---------|-------------|--------|
| **One oracle offline** | Median calculated from remaining 2 sources (Kraken + Bitstamp) | System continues working |
| **Relay censorship** | Devices receive from backup relays (relay.damus.io, nostr.wine) | Uncensorable price feed |
| **One device offline** | Network still has 299 devices contributing; resolution drops 0.3% | Barely affected |
| **All covenant devices offline** | Refund executes when first device reconnects; covenant rejects claim anyway (price < floor) | Delayed but eventual |

**Note:** Oracle broadcasts are primary trigger. Global price watch is secondary confirmation layer and public good for network redundancy.

---

## Implementation Notes

### Multi-Channel Subscription

Each device subscribes to three channel types:

```javascript
// 1. Subscribe to oracle broadcasts (multiple sources)
const oracleChannels = [
    'asgaya:oracle:coinbase:bch-eur',
    'asgaya:oracle:kraken:bch-eur',
    'asgaya:oracle:bitstamp:bch-eur'
];

oracleChannels.forEach(channel => {
    relay.sub({ kinds: [1], '#e': [channel] }, handleOraclePrice);
});

// 2. Subscribe to global price watch
relay.sub({ kinds: [1], '#e': ['asgaya:pricewatch'] }, handleNetworkSample);

// 3. Subscribe to covenant-specific coordination
relay.sub({ 
    kinds: [4], 
    '#e': [`asgaya:covenant:${covenantId}`] 
}, (event) => {
    const message = decrypt(event.content, privateKey);
    handleCovenantMessage(message); // PRICE_DROP_ALERT, REFUND_BROADCAST, etc.
});
```

**Three layers:** Oracle feeds (censorship-resistant), network samples (resolution), covenant coordination (alerts).

---

### Race Condition Handling

Multiple devices might detect price drop simultaneously (oracle broadcasts reach all devices):

```javascript
async function handlePriceDrop(consensusPrice) {
    // Broadcast alert first (to covenant channel)
    await nostr.publish(`asgaya:covenant:${covenantId}`, {
        type: 'PRICE_DROP_ALERT',
        consensusPrice,  // From multi-oracle median
        oracleSources: ['coinbase', 'kraken', 'bitstamp'],
        device: userRole
    });
    
    // Check if someone else already refunded
    const utxos = await covenant.getUtxos();
    if (utxos.length === 0) {
        console.log('Already refunded by another device');
        return;
    }
    
    // Execute refund
    try {
        const tx = await covenant.refund(senderKeypair);
        await nostr.publish(`asgaya:covenant:${covenantId}`, {
            type: 'REFUND_BROADCAST',
            txid: tx.txid,
            device: userRole
        });
    } catch (error) {
        // Another device won the race - that's fine
        console.log('Refund executed by another device');
    }
}
```

**First transaction to confirm wins.** Others see empty UTXO and abort. Oracle consensus ensures all devices detect simultaneously.

---

## Cost Analysis

**Traditional HTTP polling:** 300 devices × 3 queries/min = 900 API calls/min. **Oracle-over-Nostr:** 3 oracles × 2 broadcasts/min = 6 broadcasts/min (150× reduction). Devices contribute 1 sample/min to global price watch. Per-covenant coordination: 2–5 messages total (CLAIM_BROADCAST/CONFIRMED or PRICE_DROP_ALERT/REFUND_BROADCAST/CONFIRMED). **Total cost per device:** Negligible (pure Nostr subscription, no polling).

---

## Related Documents

**Why this pattern:**
- [Auto-Refund UX](../../user-journeys/remittance/sender/auto-refund-ux.md) - User experience perspective
- [Covenant Simplicity Principle](../../why-this-design/constraints/covenant-simplicity-principle.md) - Why client enforces fairness

**How it's used:**
- [Price Drop Protection](../covenants/version-history.md) - v2.1 covenant feature
- [Nostr Coordination](./README.md) - General Nostr usage in Asgaya

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Nostr Coordination](./README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Auto-Refund UX](../../user-journeys/remittance/sender/auto-refund-ux.md) | [Device Health](./device-health.md)

---

**Status:** Phase 1.5 - Designed, implementation planned  
**Updated:** 2026-07-25
