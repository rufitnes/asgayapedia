# Error: Seller Non-Delivery

**Part of:** [Sender Flows](../README.md)  
**Triggered from:** [Buy Seller Path - Payment Instructions](../buy-seller-path/5-payment-instructions.md)  
**Date:** 2026-05-16

---

## Error Scenario

**What happened:** Iris paid BCH seller via Bizum, but seller hasn't locked BCH in covenant.

**Possible causes:**
1. Seller's notification bot is offline
2. Seller's bot parsed SMS but failed to fund covenant (insufficient balance, network error)
3. Seller is malicious (took Bizum, not providing BCH)

---

## Error Screen Wireframe

```
┌─────────────────────────────────────┐
│ ◄ Back    Seller Not Responding     │
├─────────────────────────────────────┤
│                                     │
│           ⚠️                         │
│                                     │
│   Waiting for BCH seller...         │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Your Bizum payment:        │   │
│  │  ✅ Sent: €100.50           │   │
│  │  ✅ Seller notified         │   │
│  │                             │   │
│  │  ━━━━━━━━━━━━━━━━━━━━━━     │   │
│  │                             │   │
│  │  Covenant funding:          │   │
│  │  ⏳ Waiting for 0.1 BCH     │   │
│  │  ⏱️  Time remaining: 15 min │   │◄─ 30-minute timeout
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  📊 Seller Bot Status:              │
│                                     │
│  ⏸️  Bot last seen: 5 min ago       │◄─ Bot heartbeat
│  ✅ SMS notification parsed         │
│  ❌ Covenant funding failed         │
│                                     │
│  Reason: Insufficient BCH balance   │◄─ Bot error message
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  We've notified the seller to       │
│  refund your payment or provide     │
│  BCH within 30 minutes.             │
│                                     │
│  [ Contact Seller ]                 │
│  [ Request Refund ]                 │
│  [ Wait ]                           │
│                                     │
└─────────────────────────────────────┘
```

---

## Timeout Stages

### Stage 1: First 10 Minutes (Patient Wait)
```
Status: "Waiting for BCH seller..."
Bot status: Shows real-time updates
- SMS parsed: ✅ or ⏸️
- Covenant funded: ⏳ (pending)
```

### Stage 2: 10-20 Minutes (Warning)
```
Status: "⚠️ Seller taking longer than usual"
Notification sent to seller:
- SMS: "Please fund covenant or refund within 20 min"
- App push notification
- Email alert
```

### Stage 3: 20-30 Minutes (Urgent)
```
Status: "⚠️ Seller must respond in 10 minutes"
Escalation:
- Second SMS to seller
- Asgaya support team notified
- Option to request refund now
```

### Stage 4: 30+ Minutes (Timeout)
```
Status: "❌ Seller failed to deliver BCH"
Actions:
- Automatic refund request filed
- Seller's reputation downgraded
- Case escalated to dispute resolution
```

---

## Bot Status Indicators

### ✅ Healthy Bot
```
⏸️  Bot last seen: 1 min ago
✅ SMS notification parsed
✅ Covenant funding in progress
```

### ⚠️ Bot Offline
```
🔴 Bot offline for 10 min
⏸️  SMS notification pending
❌ Covenant not funded
```

### ❌ Bot Error
```
⏸️  Bot last seen: 2 min ago
✅ SMS notification parsed
❌ Covenant funding failed
Reason: Insufficient BCH balance
```

### 🛠️ Bot Recovering
```
⏸️  Bot last seen: 30 sec ago
✅ SMS notification parsed
⏳ Retrying covenant funding (attempt 2/3)
```

---

## User Actions

### Contact Seller
- Opens direct message to seller
- Provides payment proof (Bizum receipt)
- Seller can respond: "Bot issue, fixing now" or "Refunding you"

### Request Refund
- Files automatic refund request
- Seller notified: "Refund requested, you have 24h to respond"
- If seller doesn't respond: Case escalated to Asgaya support

### Wait
- User stays on this screen
- Real-time bot status updates
- Auto-refresh every 30 seconds

---

## Recovery Paths

### Path 1: Seller Fixes Bot (Success)
```
1. Seller's bot comes back online
2. Covenant funded successfully
3. Navigate to tracking screen
4. Normal flow continues
```

### Path 2: Seller Issues Refund (Neutral)
```
1. Seller sends Bizum refund to Iris
2. Iris receives €100.50 back
3. Show refund confirmation
4. Option to retry with different seller
```

### Path 3: Timeout Without Response (Dispute)
```
1. 30-minute timeout reached
2. Automatic dispute filed
3. Asgaya support investigates
4. Refund processed within 24-48h
5. Seller's account flagged
```

---

## Technical Notes

### Bot Heartbeat Monitoring
```javascript
// Monitor seller bot status
const botStatus = await monitorSellerBot(sellerId);
// Returns:
// {
//   lastSeen: 1715875200000, // timestamp
//   smsNotificationParsed: true,
//   covenantFundingStatus: "failed",
//   errorMessage: "Insufficient BCH balance",
//   retryAttempts: 2
// }

// Display to user
if (Date.now() - botStatus.lastSeen > 600000) {
  // Bot offline for 10+ min
  showWarning("Seller bot offline");
} else if (botStatus.covenantFundingStatus === "failed") {
  showError("Covenant funding failed", botStatus.errorMessage);
}
```

### Timeout Handler
```javascript
// Start 30-minute countdown after Bizum payment
const timeoutHandle = setTimeout(async () => {
  // Seller hasn't funded covenant
  const covenantFunded = await checkCovenantStatus(covenantId);
  
  if (!covenantFunded) {
    // Automatic refund request
    await fileRefundRequest({
      senderId: iris.cashAccount,
      sellerId: seller.cashAccount,
      amount: 100.50,
      reason: "Seller failed to fund covenant within 30 minutes"
    });
    
    // Downgrade seller reputation
    await updateSellerReputation(sellerId, -10);
    
    // Show timeout screen
    navigateTo("seller-timeout");
  }
}, 30 * 60 * 1000); // 30 minutes
```

### Refund Process
```javascript
// User requests refund
const refundRequest = await requestRefund({
  senderId: iris.cashAccount,
  sellerId: seller.cashAccount,
  amount: 100.50,
  bizumTransactionId: "BIZ-12345",
  proof: bizumReceipt
});

// Notify seller
await sendNotification(sellerId, {
  type: "refund_requested",
  message: "Iris requested refund. Please respond within 24h.",
  actionRequired: true
});

// If seller refunds within 24h: No penalty
// If seller doesn't respond: Account flagged, automatic Asgaya refund
```

---

## Design Principles

### Transparency
- Show exactly what's happening (bot status, error messages)
- Real-time updates every 30 seconds
- Clear timeline (30-minute countdown)

### Trust Building
- User sees we're monitoring the seller
- Multiple recovery options (contact, refund, wait)
- Automatic escalation if needed

### Fairness
- Seller gets 30 minutes to respond (reasonable time)
- Bot errors explained (insufficient balance, network issue)
- No immediate punishment (seller's bot might be temporarily offline)

---

## Related Documentation

- **[Buy Seller Path - Payment Instructions](../buy-seller-path/5-payment-instructions.md)** - Where this error originates
- **[Buy Seller Path - Tracking](../buy-seller-path/6b-tracking.md)** - Normal tracking flow (if covenant funded)
- **[Network Errors](network-errors.md)** - Related error handling

---

*Error documented: 2026-05-16*  
*Status: Active - Seller Bot Monitoring*
