# Screen 6A: Tracking (Own Wallet Path)

**Part of:** [Sender Flows](../README.md) → [Own Wallet Path](./4a-confirm-send.md)  
**Previous:** [4A: Confirm Send](./4a-confirm-send.md)  
**Next:** [7A: Completion](./7a-completion.md)

---

## Purpose

Track covenant progress when sender uses their own BCH wallet balance. No Bizum payment needed, so covenant is created and funded immediately.

---

## State 1: Covenant Created, Notifying Recipient

```
┌─────────────────────────────────────┐
│  Sending 0.1 BCH to Elena#142      │◄─ BCH amount + Cash Account
├─────────────────────────────────────┤
│                                     │
│      📱 Notifying Elena...          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │         [Progress bar]      │   │
│  │  ████████░░░░░░░░  40%      │   │
│  └─────────────────────────────┘   │
│                                     │
│   To: Elena#142                     │◄─ Cash Account only
│   Amount: 0.1 BCH                   │
│   (€100 → 500,000 VES)              │
│                                     │
│   Progress:                         │
│   ✅ Covenant created               │
│   ✅ BCH funded from your wallet    │
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
- **No "Waiting for Bizum" state** - covenant created immediately from wallet balance
- Notification sent via WhatsApp/Telegram/LINE
- Bounty code shown to sender (last 4 digits of covenant ID)
- 24-hour claim window starts immediately
- Covenant is public on bulletin board (all merchants can see)

---

## State 2: Expiring Soon (18h Elapsed, No Claim)

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
│   Amount: 0.1 BCH                   │
│   (€100 → 500,000 VES)              │
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
│   If unclaimed after 24h, full      │
│   refund to your wallet             │
│   (0.1 BCH returned)                │
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
- **Full refund (no seller fee)** - sender used own BCH, no seller involved
- All 0.1 BCH returns to sender's wallet if unclaimed
- Related policy: [With volatility buffer Bounty Contracts - Timeout Cascade](../../../../concepts/bounty-contracts-with-volatility-buffer.md#timeout-cascade)

---

## State 3: Merchant Co-Signing

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
│   Amount: 0.1 BCH                   │
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

## State 4: Cash Delivered (Both Co-Signed)

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
│   Amount: 0.1 BCH                   │
│   (Elena received: 500,000 VES)     │
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
- Tap "See details" → Go to [Screen 7A: Completion](./7a-completion.md)
- Real-time updates (push notifications or polling)

**Notes:**
- Both merchant AND recipient co-signed (cryptographic signatures)
- Final VES amount shown (actual rate at claim time)
- Total time tracked (transparency)
- Covenant matured → BCH distributed to merchant
- **No seller involved** - sender used own BCH wallet

---

## Key Differences from Buy Seller Path (6B)

| Feature | Own Wallet (6A) | Buy from Seller (6B) |
|---------|-----------------|----------------------|
| Bizum payment | ❌ Not needed | ✅ Required |
| "Waiting for Bizum" state | ❌ Skipped | ✅ Shown |
| Covenant creation | ✅ Immediate | ⏳ After Bizum confirmed |
| 24h window starts | ✅ Immediately | ⏳ After Bizum confirmed |
| Refund if unclaimed | 100% to sender | Split: 99.5% sender, 0.5% seller |

---

## Related Documentation

- [Screen 4A: Confirm Send](./4a-confirm-send.md) - Previous screen
- [Screen 7A: Completion](./7a-completion.md) - Next screen
- [Screen 6B: Tracking (Buy from Seller)](../buy-seller-path/6b-tracking.md) - Alternative tracking screen
- [Covenant Expiry Error](../errors/covenant-expiry.md) - If recipient doesn't claim
- [With volatility buffer Bounty Contracts](../../../../concepts/bounty-contracts-with-volatility-buffer.md) - Covenant specification

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
