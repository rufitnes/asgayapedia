# Auto-Refund UX: Zero-Friction Protection

**Role:** Sender (María in Madrid sending €100 to Elena in Caracas)  
**Feature:** Automatic refund protection without manual intervention  
**Philosophy:** It's the user's money. Protection should be automatic, not manual.

---

## The Core Idea

**Traditional wallets:** User must remember to check, decide, and click "refund."  
**Asgaya:** User sets conditions once. System monitors automatically. Refunds when appropriate. **No refund button.**

**Why this matters:** Senders fund the infrastructure (pay fiat to sellers, enable ecosystem). They deserve **zero-friction UX**.

---

## The User Experience

### When Creating Payment

María sees conditions explained **once** at covenant creation:

```
┌─────────────────────────────────────┐
│  💸 Sending €100.00 / 84000 VES     │
│  To: Elena#142                      │
│                                     │
│  You will be automatically          │
│  refunded if:                       │
│  • Not claimed in 8 hours           │
│  • BCH price drops >7% (€930)       │
│                                     │
│  ✅ selected BCH seller fee 0.5%     │
│  [ request Payment instructions]    │
│          total  €100.50             │
│                                     │
└─────────────────────────────────────┘
```

**Key insight:** Conditions shown upfront. María understands protection before paying.

---

### During Active Payment

After María pays Isabel via Bizum, she sees:

```
┌─────────────────────────────────────┐
│  💸 Sending €100.00                 │
│  To: Elena#142                      │
│                                      │
│  ⏰ Expires in: 7h 58min             │
│  📊 Price: €995/BCH ✅              │
│  🔒 Covenant: 0.0107 BCH locked     │
│                                      │
│  ● Payment Active                   │
│  🤖 Auto-monitoring enabled          │
│                                      │
│  Waiting for Elena to claim...      │
│                                      │
│  ← Back                             │
└─────────────────────────────────────┘
```

**What's happening in background:**
- María's app subscribes to oracle price broadcasts over Nostr (push, not polling)
- Contributes consensus price to global price watch once per minute
- Monitors covenant against multi-oracle consensus price
- Watches for claim transaction
- **Requires zero user interaction**

**Note:** No "Refund" button visible. System handles it automatically.

---

### Price Warning (3% Drop - Informational)

When price drops 3% (early warning), María sees:

```
┌─────────────────────────────────────┐
│  💸 Sending €100.00                 │
│  To: Elena#142                      │
│                                      │
│  ⏰ Expires in: 6h 12min             │
│  ⚠️ Price: €970/BCH (-3%)           │
│  🔒 Covenant: 0.0107 BCH locked     │
│                                      │
│  ⚠️ Price Drop Warning              │
│                                      │
│  BCH dropped 3%. Auto-refund will   │
│  trigger if it drops below €930     │
│  (7% threshold).                    │
│                                      │
│  No action needed - monitoring.     │
│                                      │
│  ← Back                             │
└─────────────────────────────────────┘
```

**What this is:**
- **Not an error** - just informational
- Price still within 7% buffer
- System continues monitoring
- Elena can still claim

---

### Auto-Refund Triggered (Price Drop >7%)

When price drops below €930 (7% threshold), **system automatically refunds and protects the value** by default.

```
┌─────────────────────────────────────┐
│  ✅ Refund Protected                │
│                                      │
│  Price dropped >7% (€930/BCH)       │
│                                      │
│  ✅ Protected: 100 H€                │
│  (equivalent to €100)               │
│                                      │
│  Remittance can still complete.     │
│  Elena can cash out H€.             │
│                                      │
│  [Send H€ to Elena]                 │
└─────────────────────────────────────┘
```

*Note: H€ minting uses bull pool capacity. If pool exhausted, you'll receive BCH instead.*

**What happened automatically (default setting):**
1. Price crossed 7% threshold
2. María's app detected drop (60s check cycle)
3. App automatically broadcast refund transaction
4. **App immediately minted 100 H€** (stability layer protection)
5. María gets notification (non-blocking)

**María did nothing.** System protected her automatically.

**Why automatic protection:**
- ⚠️ **Volatility window** - Can't wait for user input when price is dropping
- ✅ **Preserves remittance** - Elena can still cash out H€ at merchant
- ✅ **Zero friction** - Most users want stability, not BCH exposure
- ⚙️ **Configurable** - Advanced users can disable in Settings (see below)

**Technical note:** Covenant would reject Elena's claim anyway (price < floor). Auto-refund + H€ minting preserves the remittance value.

---

### Auto-Refund Triggered (Timeout - 8 Hours)

When Elena doesn't claim within 8 hours, **system automatically refunds and protects the value** by default.

```
┌─────────────────────────────────────┐
│  ✅ Refund Protected                │
│                                      │
│  Timeout: Not claimed in 8 hours    │
│                                      │
│  ✅ Protected: 100 H€                │
│  (swapped from 0.0107 BCH)          │
│                                      │
│  Your €100 value preserved.         │
│  Elena can still cash out.          │
│                                      │
│  [Send H€ to Elena]  [View Swap]    │
└─────────────────────────────────────┘
```

*Note: BCH → H€ swap requires H€ seller on bulletin board. If no liquidity, you'll receive BCH instead.*

**What happened automatically (default setting):**
1. 8 hours passed, Elena didn't claim
2. Oracle timestamp confirmed expiry
3. App automatically broadcast refund transaction
4. **App immediately swapped BCH → H€** on bulletin board
5. María notified (she might not have been watching)

**Why automatic swap:**
- ⚠️ **Can't mint on timeout** - Would allow abuse (deliberately timeout to drain bull pool)
- ✅ **Bulletin board swap** - H€ seller provides liquidity (permissionless!)
- ✅ **Preserves remittance** - María can still send H€ to Elena
- ✅ **Native tokens** - Swap happens in seconds, user doesn't need to know
- ⚙️ **Configurable** - Advanced users can receive BCH instead (Settings)

**Technical note:** H€ tokens are native BCH tokens (CashTokens). Anyone can be an H€ seller on the bulletin board - completely permissionless role. Phase 0: Asgaya bootstraps liquidity.

**Edge case handled:** If María's device was offline, seller or recipient device would trigger refund. If all offline, María's device refunds when back online. Auto-swap happens when device reconnects.

---

## What Senders NEVER See

### ❌ No Complex Decision Screens

Traditional wallet might show:
```
⚠️ Refund Conditions Not Met
Time remaining: 6h 12min
Price: €970 (above floor €930)

Refund anyway? This may be unfair to recipient.
[Cancel] [Refund Anyway]
```

**Asgaya doesn't show this.** System decides based on pre-agreed conditions. No decision fatigue.

---

## Settings: Auto-Refund Protection (Advanced Users)

**Default behavior (90% of users):**
- ✅ Automatic H€ minting/swapping on refund
- ✅ Instant protection from volatility
- ✅ Zero friction (never see this menu)

**Advanced users can configure:**

```
┌─────────────────────────────────────┐
│  ⚙️ Auto-Refund Protection          │
│                                      │
│  ● Automatic (Recommended)          │
│    Protect refunds from volatility  │
│    • Price drop: Mint H€            │
│    • Timeout: Swap BCH → H€         │
│                                      │
│  ○ Manual Control                   │
│    You decide after each refund     │
│    ⚠️ Exposed to volatility         │
│                                      │
│  ○ Always BCH                       │
│    Never auto-convert               │
│    ⚠️ Maximum volatility exposure   │
│                                      │
│  [Save Settings]                    │
└─────────────────────────────────────┘
```

### Why Default is Automatic

**For most senders:**
- Sending remittance (€100 to Elena), not trading BCH
- Want stability, not volatility exposure
- Can't wait for manual input (price drops fast!)
- Remittance should "just work"

**Example scenario without auto-protect:**
```
t=0:   Price drops >7% → refund triggered
t=5:   María sees notification: "Convert to H€?"
t=10:  María clicks "Yes" → price dropped another 3%
Result: Lost €3 waiting for user input
```

**With auto-protect (default):**
```
t=0:   Price drops >7% → refund + mint H€ (instant)
t=5:   María sees notification: "✅ Protected: 100 H€"
Result: €100 value preserved
```

### When to Disable Auto-Protect

**Disable if:** You're bullish on BCH, trading rather than sending remittances, or accept full volatility risk. **For everyone else:** Leave it on (automatic protection).

### Edge Cases (Fallback to BCH)

**When auto-protect can't execute:**

**Price drop abort:**
- If bull pool exhausted → You receive BCH instead of H€
- Notification: "⚠️ Bull pool at capacity - received BCH instead"
- Can manually swap BCH → H€ later (when pool refills)

**Timeout:**
- If no H€ seller on bulletin board → You receive BCH instead
- Notification: "⚠️ No H€ liquidity - received BCH instead"
- Can manually swap later (when H€ seller appears)

**Phase 0 note:** Asgaya bootstraps both bull pool and H€ bulletin board liquidity. Edge cases unlikely but possible during high volatility.

---

## The Technical Details (For Nerds)

**How auto-refund monitoring works:**

### Blockchain-as-Oracle Architecture

**How price monitoring works:**

**Price Discovery from Real Trades**
- Every covenant funding = trade signal (on-chain proof)
- Sellers set their own prices (Isabel chooses Kraken, Carlos chooses Coinbase, etc.)
- Network calculates reputation-filtered VWAP (only trust high-rep sellers)
- Asgaya oracle bootstraps (Kraken API) until network matures

**Market Price Calculation**
- María's device subscribes to Asgaya market channel (Nostr)
- Receives trade broadcasts in real-time (price + volume + seller reputation)
- Calculates VWAP from trusted trades (rep >= 90)
- Hybrid weighting during bootstrap (user VWAP + Asgaya oracle)

**Covenant Monitoring**
- Compares market price against covenant threshold (7% drop)
- Detects crossing, triggers auto-refund
- Broadcasts alerts to per-covenant channels

**Benefits:**
- 🔒 Maximum censorship resistance (blockchain can't be shut down)
- 📊 Real market prices (actual trades, not CEX speculation)
- 💰 Zero cost per device (pure subscription, no HTTP polling)
- 🛡️ Massive redundancy (hundreds of monitoring devices)
- 🌐 Fully permissionless (anyone can be seller, contribute price signals)

### Nostr Coordination

When any device detects refund condition:

```javascript
// Device broadcasts alert
nostr.publish('asgaya:covenant:abc123', {
    type: 'PRICE_DROP_ALERT',
    currentPrice: 920,
    threshold: 930,
    action: 'TRIGGERING_REFUND'
});
```

**Other devices see alert:**
- María: Gets notification "Auto-refund triggered"
- Elena: Gets notification "Payment cancelled - price drop"
- Isabel: Gets notification "Covenant expired - buffer returned"

**Everyone informed instantly.** No coordination failures.

**Nostr message types used:**
- `PRICE_DROP_ALERT` - Any device detects >7% drop
- `REFUND_BROADCAST` - Sender broadcasted refund tx
- `REFUND_CONFIRMED` - Refund tx confirmed (seller gets buffer)
- `CLAIM_BROADCAST` - Recipient broadcasted claim tx
- `CLAIM_CONFIRMED` - Claim tx confirmed (seller gets buffer)

*Full Nostr message schema documented in [Distributed Monitoring](../../../the-mechanism/nostr-coordination/distributed-monitoring.md)*

---

## Why This Design?

**Core philosophy:** It's the user's money. Covenants should enable, not imprison.

**Asgaya's approach:**
- **Covenant:** Allows sender to refund anytime (sender owns it)
- **App:** Auto-refunds only when appropriate (timeout or price drop >7%)
- **Stability layer:** Protects value automatically (but configurable in Settings)

**Why automatic protection by default:**

**Manual prompt approach:**
```
⚠️ Price dropped >7%. Convert to H€?
[Yes] [No]
```

**Automatic approach (Asgaya):**
```
✅ Refund Protected
Protected: 100 H€ (equivalent to €100)
```

The difference: **Volatility doesn't wait for user input.** In 30 seconds, price could drop another 2%. Automatic protection (configurable in Settings) serves 90% of users who want stability, not volatility exposure.

**Full rationale:** See [Covenant Simplicity Principle](../../../why-this-design/constraints/covenant-simplicity-principle.md) for the complete design philosophy and Bitcoin protocol analogy.

---

## Edge Cases & Failures

### What if María's device is offline when refund condition triggers?

**Scenario:** Price drops >7%, but María's phone is dead.

**What happens:**
1. **Recipient device detects drop** (20s later) → broadcasts refund
2. **Seller device detects drop** (40s later) → broadcasts refund
3. First refund transaction confirms → others see it and stop

**Result:** María gets refund even if offline. Redundancy works.

---

### What if all devices offline?

**Scenario:** Price drops >7%, but María, Elena, and Isabel all offline.

**What happens:**
1. Covenant remains locked temporarily
2. When María comes back online → app detects condition → auto-refunds
3. BCH returned to María's wallet

**Edge case:** Elena's claim would fail anyway (price < floor), so no urgency. María gets BCH back whenever her device reconnects.

---

### What if María wants to refund immediately (changed mind)?

**Scenario:** María sent payment but realizes Elena doesn't need it.

**Asgaya position:** This is discouraged but not prevented.

**What María can do:**
1. **Wait for timeout** (8 hours) → auto-refund triggers
2. **Contact Elena** → ask her not to claim → timeout refunds
3. **Advanced: Manual refund** (not exposed in UI, requires technical knowledge)

**Why not easy refund button:**
- Protects Elena (she might already expect payment)
- Protects ecosystem (prevents reputation damage)
- Forces coordination (María + Elena should agree)

**If truly urgent:** María can broadcast refund manually (covenant allows it). App just doesn't make it easy.

---

## Success Criteria

**This UX succeeds when:**
- ✅ **Senders never monitor actively** - Set once, system handles it
- ✅ **Zero decision fatigue** - No prompts, automatic protection by default
- ✅ **Emergency escape preserved** - Advanced users can override in Settings
- ✅ **Failure modes handled gracefully** - Devices offline, edge cases don't trap funds

---

## Comparison: Traditional Wallet vs Asgaya

| Traditional Wallet | Asgaya |
|-------------------|--------|
| User must check status manually | **Automatic monitoring** |
| "Refund" button always visible | **No button (auto-refunds)** |
| User decides when to refund | **App decides (pre-agreed conditions)** |
| User decides if/when to stabilize | **Auto-protect by default (configurable)** |
| Funds stuck if user forgets to refund | **Impossible - auto-refund guaranteed** |
| Volatility exposure on refunds | **Automatic H€ minting/swap (default)** |
| Complex UI with many options | **Simple - just status display** |
| Decision fatigue ("Should I refund?") | **Zero decisions - just notifications** |
| Emergency escape requires manual refund | **Same (advanced users can broadcast)** |

---

## Related Documents

**Why this design:**
- [Covenant Simplicity Principle](../../../why-this-design/constraints/covenant-simplicity-principle.md) - User sovereignty over safety theater
- [Distributed Monitoring](../../../the-mechanism/nostr-coordination/distributed-monitoring.md) - 3-device staggered price monitoring

**How it works:**
- [Covenant Implementation](../../../implementation/covenants/version-history.md) - v2.2 simplified refund
- [Auto-Refund Monitoring](../../../implementation/android-app/wallet/auto-refund-monitoring.md) - Background service implementation

**Full sender journey:**
- [Sender Journey](./README.md) - Complete flow including happy path

---

## Navigation

**[🏠 Home](../../../index.md)** | **[↑ Sender Journey](./README.md)** | **[📖 Glossary](../../../glossary.md)**

**Related:** [Covenant Simplicity](../../../why-this-design/constraints/covenant-simplicity-principle.md)

---

**Status:** Phase 1.5 - Documentation (auto-refund UX designed, implementation planned)  
**Updated:** 2026-07-25
