# Screen 6B: Tracking (Buy from Seller Path)

**Part of:** [Sender Flows](../README.md) → [Buy from Seller Path](./4b-confirm-purchase.md)  
**Previous:** [5: Payment Instructions](./5-payment-instructions.md)  
**Next:** [7B: Completion](./7b-completion.md)

---

## Purpose

Track covenant progress when sender buys BCH from a seller. Includes "Waiting for Bizum" state before covenant is funded.

---

## State 1: Waiting for Bizum

```
┌─────────────────────────────────────┐
│  Sending 0.1 BCH to Elena#142      │◄─ BCH amount + Cash Account
├─────────────────────────────────────┤
│                                     │
│      ⏳ Waiting for payment...      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████░░░░░░░░░░░░░░  20%    │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena#142                     │◄─ Cash Account only
│   Amount: €100 → 500,000 VES        │
│                                     │
│   Progress:                         │
│   ✅ Covenant created               │◄─ Covenant first
│   ⏳ Bizum to BCH seller pending... │
│   ⏸️  Notifying Elena...            │
│   ⏸️  Waiting for claim...          │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   ⏱️ Time remaining (5 min window): │
│       4m 32s                        │
│                                     │
│  [ Cancel (full refund) ]           │
│                                     │
└─────────────────────────────────────┘
```

**Notes:**
- **Title shows:** BCH amount being sent + recipient Cash Account (not order ID)
- **Progress order:** Covenant created FIRST, then Bizum payment (covenant exists before funding)
- 5-minute Bizum window (seller's volatility exposure)
- Sender can cancel before Bizum confirmed (full refund, covenant unfunded)
- Automatic timeout if Bizum not received within 5 minutes

---

## State 2: Covenant Created, Notifying Recipient

```
┌─────────────────────────────────────┐
│  Sending 0.1 BCH to Elena#142      │
├─────────────────────────────────────┤
│                                     │
│      📱 Notifying Elena...          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████████░░░░░░░░  40%      │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena#142                     │
│   Amount: €100 → 500,000 VES        │
│                                     │
│   Progress:                         │
│   ✅ Bizum received (seller paid)   │
│   ✅ Covenant created (24h window)  │
│   🔄 Notifying Elena...             │
│   ⏸️  Waiting for claim...          │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Elena has been notified!          │
│   Bounty code: 8923                 │
│                                     │
│   Covenant can be claimed at any    │
│   merchant in the Asgaya network    │
│                                     │
│   ⏱️ Claim window: 23h 58m          │
│                                     │
└─────────────────────────────────────┘
```

**Notes:**
- Notification sent via WhatsApp/Telegram/LINE (see [Recipient Flows](../../recipient-flows.md))
- Bounty code shown to sender (can share if needed: last 4 digits of covenant ID)
- 24-hour claim window starts when covenant funded (after Bizum received)
- Covenant is public on bulletin board (all merchants can see)

---

## State 3: Expiring Soon (18h Elapsed, No Claim)

```
┌─────────────────────────────────────┐
│  Sending 0.1 BCH to Elena#142      │
├─────────────────────────────────────┤
│                                     │
│      ⚠️  Claim window closing       │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████░░░░░░░░░░░░  25%      │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena#142                     │
│   Amount: €100 → 500,000 VES        │
│                                     │
│   Progress:                         │
│   ✅ Covenant created               │
│   ⚠️  Elena hasn't claimed yet      │
│   ⏸️  Waiting for claim...          │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   ⏱️ Time remaining: 5h 42m         │
│                                     │
│   ⚠️  Elena hasn't claimed yet.     │
│   If unclaimed after 24h, split     │
│   refund:                           │
│   - Merchant portion → You (€99.50) │
│   - Seller fee → Seller (€0.50)    │
│                                     │
│   Contact Elena: +58-412-XXX-5678   │
│                                     │
│  [ Call Elena ]  [ Message Elena ]  │
│                                     │
└─────────────────────────────────────┘
```

**Triggers:**
- Shown at 18-hour mark if recipient hasn't claimed yet
- Notification sent to sender: "Elena hasn't claimed yet"
- Urgent reminder sent to recipient simultaneously

**Interactions:**
- Sender can contact recipient directly (phone number shown)
- "Call" button opens phone dialer
- "Message" button opens WhatsApp/SMS

**Notes:**
- Empowers sender to coordinate with recipient
- Clear warning about split refund mechanism
- **Split refund rationale:**
  - Merchant portion (€99.50) → Refunded to you (Iris)
  - Seller fee (€0.50) → Kept by seller (earned for 24h service)
- Related policy: [Overcollateralized Bounty Contracts - Timeout Cascade](../../../../concepts/overcollateralized-bounty-contracts.md#timeout-cascade)

---

## State 4: Merchant Co-Signing

```
┌─────────────────────────────────────┐
│  Sending 0.1 BCH to Elena#142      │
├─────────────────────────────────────┤
│                                     │
│      🏪 Merchant co-signing...      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████████████░░░  60%       │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena#142                     │
│   Amount: €100 → 500,000 VES        │
│   Code: 8923                        │
│                                     │
│   Progress:                         │
│   ✅ Covenant created               │
│   ✅ Elena notified                 │
│   ✅ Merchant entered code          │
│   🔄 Both co-signing covenant...    │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   💡 Elena is at a merchant now     │
│   Both parties co-signing covenant  │
│   (cryptographic signatures)        │
│                                     │
│   Covenant matures when both sign   │
│                                     │
│   Elena is on her way!              │
│                                     │
└─────────────────────────────────────┘
```

**Note:**
- Covenant requires both merchant and recipient signatures
- No numeric codes (cryptographic co-signing via BCH Script)
- Settlement triggered when both signatures present

**Related:** [Merchant Flows - Co-Sign Covenant](../../merchant-flows.md#screen-3-hand-ves--co-sign-covenant)

---

## State 5: Cash Delivered (Both Co-Signed)

```
┌─────────────────────────────────────┐
│  Sending 0.1 BCH to Elena#142      │
├─────────────────────────────────────┤
│                                     │
│      💰 Cash delivered!             │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████████████████  100%     │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena#142                     │
│   Amount: €100 → 500,000 VES        │
│                                     │
│   Progress:                         │
│   ✅ Covenant created               │
│   ✅ Elena notified                 │
│   ✅ Merchant: Bodega María         │
│   ✅ Both co-signed covenant        │
│   ✅ Cash delivered & confirmed     │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   🎉 Elena confirmed receipt!       │
│   Transaction complete.             │
│                                     │
│   Total time: 42 minutes            │
│                                     │
│   [ See details ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- Tap "See details" → Go to [Screen 7B: Completion](./7b-completion.md)
- Real-time updates (push notifications or polling)

**Notes:**
- Both merchant AND recipient co-signed (cryptographic signatures)
- Final VES amount shown (actual rate at claim time)
- Total time tracked (transparency)
- Covenant matured → BCH distributed to merchant and seller

---

## Key Differences from Own Wallet Path (6A)

| Feature | Buy from Seller (6B) | Own Wallet (6A) |
|---------|----------------------|-----------------|
| Bizum payment | ✅ Required | ❌ Not needed |
| "Waiting for Bizum" state | ✅ Shown | ❌ Skipped |
| Covenant creation | ⏳ After Bizum confirmed | ✅ Immediate |
| 24h window starts | ⏳ After Bizum confirmed | ✅ Immediately |
| Refund if unclaimed | Split: 99.5% sender, 0.5% seller | 100% to sender |

---

## Related Documentation

- [Screen 5: Payment Instructions](./5-payment-instructions.md) - Previous screen
- [Screen 7B: Completion](./7b-completion.md) - Next screen
- [Screen 6A: Tracking (Own Wallet)](../own-wallet-path/6a-tracking.md) - Alternative tracking screen
- [Bizum Timeout Error](../errors/bizum-timeout.md) - If payment not sent in time
- [Covenant Expiry Error](../errors/covenant-expiry.md) - If recipient doesn't claim
- [Overcollateralized Bounty Contracts](../../../../concepts/overcollateralized-bounty-contracts.md) - Covenant specification

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
