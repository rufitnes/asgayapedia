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
- María's app checks price every 60 seconds (staggered with recipient/seller devices for 20s resolution)
- Monitors time remaining
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

When price drops below €930 (7% threshold), **system automatically refunds** and mints one of the stability tokens, H€ in the example, the this all happens in the back ground, the user just needs to know that the remittance is still going to arrive if Elena cashes before time out.

```
┌─────────────────────────────────────┐
│                                     │
│                                      │
│  Price dropped 7% (€930/BCH)        │
│  Below floor: €930/BCH              │
│                                      │
│                                     │
│     ✅ minted 100 H€                 │
│  ⏰ Expires in: 2h 38min            │
│ No action needed - monitoring.      │
│                                     │
└─────────────────────────────────────┘
```



**What happened:**
1. Price crossed 7% threshold
2. María's app detected drop (60s check cycle)
3. **App automatically broadcast refund transaction**
4. María gets notification (non-blocking)
5. BCH returned to wallet

**María did nothing.** System protected her automatically.

**Why refund:** Covenant would reject Elena's claim anyway (price < floor). Better to return BCH to María immediately than wait for timeout.
**stability layer** saved the remittance and now can be completed technically without a time limit but we don't want to overwelm the user.

---

### Auto-Refund Triggered (Timeout - 8 Hours)

When Elena doesn't claim within 8 hours:

```
┌─────────────────────────────────────┐
│  ✅ Payment Refunded                │
│                                      │
│  Timeout: Not claimed in 8 hours    │
│                                      │
│  Refunded: 0.0107 BCH               │
│  (~€106.65 at current price)        │
│                                      │
│  Your BCH is back in your wallet.   │
│                                      │
│  Contact Elena to try again?        │
│                                      │
│  [Send Again]  [View Transaction]   │
└─────────────────────────────────────┘
```

🗨 we can't mint H tokens at this point because it could be easily abused and exhaust the bull pool but we can trade the BCH automatically for H tokens in the bulleting board. H tokens being native can be swapped in seconds an the user doesn't have to know anything. If they want they can check the settings but with the basic set up we can do this as long as there is a H token seller in the bulletin board willing to trade.

**What happened:**
1. 8 hours passed, Elena didn't claim
2. Oracle timestamp confirmed expiry
3. **App automatically broadcast refund**
4. María notified (she might not have been watching)
5. BCH returned

**Edge case handled:** If María's device was offline, seller or recipient device would trigger refund. If all offline, María's device refunds when back online.

---

## What Senders NEVER See

### ❌ No "Refund" Button During Active Payment

**Why not:**
- Reduces UI clutter
- Prevents confusion ("Should I click this?")
- Eliminates impulsive refunds
- Forces intentional design (auto-refund only when appropriate)

**Emergency manual refund still possible:**
- Advanced users can broadcast refund transaction manually
- Requires technical knowledge (signing, broadcasting)
- Not exposed in UI (permissionless at protocol layer, opinionated at app layer)

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

## The Technical Details (For Nerds)

**How auto-refund monitoring works:**

### Distributed Monitoring (3 Devices)

- **Sender device:** Checks price every 60s (offset: 0s)
- **Recipient device:** Checks price every 60s (offset: 20s)
- **Seller device:** Checks price every 60s (offset: 40s)

**Result:** Effective 20-second resolution (60s / 3 devices), but each device only queries once per minute.

**Benefits:**
- ⚡ Fast detection (20s vs 60s)
- 💰 Low cost (1 query/min per device)
- 🛡️ Redundancy (if one offline, others monitor)
- 🔔 Instant notification via Nostr (all parties know immediately)

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

---

## Why This Design?

### User Sovereignty Over Safety Theater

**Quote from covenant design:**
> "It's the user's money. Covenants should enable, not imprison."

**Traditional approach:** Lock funds until all safety conditions met (time expired AND price dropped AND oracle signed AND...).

**Result:** Funds stuck in "protection theater" - trying to protect user by trapping their money.

**Asgaya approach:** 
- **Covenant:** Allows sender to refund anytime (sender owns the covenant)
- **App:** Enforces fairness (only auto-refunds when appropriate)

**Tagline:** *The app enforces fairness. The covenant enforces ownership.*

### Why No Manual Refund Button

**Prevents abuse:**
- Sender can't impulsively refund (ruins recipient trust)
- Forces system to solve edge cases properly (not push decisions to user)
- Reduces support burden ("When should I refund?")

**Preserves emergency escape:**
- Advanced users can manually broadcast refund if needed
- Covenant allows it (permissionless)
- App just doesn't expose it (opinionated UX)

**Same pattern as Bitcoin:**
- Bitcoin protocol: CAN send to typo'd address
- Wallet: "⚠️ This address looks invalid. Proceed?"
- User: Protected by UX, not protocol restriction

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

✅ **Senders never need to monitor actively**
- Set conditions once → system handles it
- Get notification when done
- No "check status" every 30 minutes

✅ **Zero decision fatigue**
- No "Should I refund?" prompts
- Conditions pre-agreed at covenant creation
- App decides, user informed

✅ **Emergency escape preserved**
- Advanced users CAN refund manually
- Covenant is permissionless (protocol layer)
- App is opinionated (application layer)

✅ **Failure modes handled gracefully**
- Devices offline → others monitor
- All offline → refunds when reconnected
- Edge cases don't trap funds

---

## Comparison: Traditional Wallet vs Asgaya

| Traditional Wallet | Asgaya |
|-------------------|--------|
| User must check status manually | **Automatic monitoring** |
| "Refund" button always visible | **No button (auto-refunds)** |
| User decides when to refund | **App decides (pre-agreed conditions)** |
| Funds stuck if user forgets to refund | **Impossible - auto-refund guaranteed** |
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
