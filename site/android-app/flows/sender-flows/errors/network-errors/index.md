# Error: Network Errors

**Part of:** [Sender Flows](../README.md) → [Errors](./README.md)  
**Triggered from:** Any screen that requires network connectivity

---

## When Shown

Shown when app cannot connect to required services:
- Asgaya bulletin board server (covenant queries)
- BCH blockchain nodes (transaction broadcasting)
- Exchange rate APIs (DolarAPI, CoinGecko)
- Notification services (WhatsApp/Telegram APIs)

---

## General Network Error

```
┌─────────────────────────────────────┐
│           ⚠️ Connection Error       │
├─────────────────────────────────────┤
│                                     │
│  Couldn't connect to Asgaya servers │
│                                     │
│  Please check your internet         │
│  connection and try again.          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │        Try Again            │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Go to Home ]                     │
│                                     │
└─────────────────────────────────────┘
```

**Interactions:**
- **Tap "Try Again"** → Retry previous action (reload data, retry transaction)
- **Tap "Go to Home"** → Return to [Screen 1: Home](../../home-screen.md)

---

## Specific Error Scenarios

### 1. Covenant Creation Failed

```
┌─────────────────────────────────────┐
│      ⚠️ Transaction Failed          │
├─────────────────────────────────────┤
│                                     │
│  Couldn't create covenant on        │
│  Bitcoin Cash blockchain.           │
│                                     │
│  Possible reasons:                  │
│  • Network connection lost          │
│  • BCH node temporarily down        │
│  • Insufficient wallet balance      │
│                                     │
│  Your funds are safe. No charges    │
│  were made.                         │
│                                     │
│  ┌─────────────────────────────┐   │
│  │        Retry                │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Check Wallet Balance ]           │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Triggered from:**
- [Screen 4A: Confirm Send](../own-wallet-path/4a-confirm-send.md) (Own Wallet)
- [Screen 5: Payment Instructions](../buy-seller-path/5-payment-instructions.md) (Buy from Seller)

**Common causes:**
- Internet connection dropped mid-transaction
- BCH node offline or syncing
- Insufficient BCH balance (wallet balance changed)
- Transaction fee too low (rare with BCH)

**Recovery:**
- Check internet connection
- Wait 30 seconds and retry
- Verify wallet balance sufficient
- Contact support if persists

---

### 2. Bulletin Board Unavailable

```
┌─────────────────────────────────────┐
│      ⚠️ Service Unavailable         │
├─────────────────────────────────────┤
│                                     │
│  Couldn't load available sellers.   │
│                                     │
│  The bulletin board server is       │
│  temporarily unavailable.           │
│                                     │
│  Please try again in a few moments. │
│                                     │
│  ┌─────────────────────────────┐   │
│  │        Retry                │   │
│  └─────────────────────────────┘   │
│                                     │
│  💡 You can still send from your    │
│     wallet balance (no seller       │
│     needed)                         │
│                                     │
│  [ Use My Wallet ]                  │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Triggered from:**
- [Screen 4.5: Select Seller](../buy-seller-path/4.5-select-seller.md)

**Common causes:**
- Bulletin board server down
- DNS resolution failure
- Firewall blocking connection
- Server under maintenance

**Recovery:**
- Wait and retry (server usually back quickly)
- Use own wallet path (doesn't require bulletin board)
- Contact support if persistent

---

### 3. Exchange Rate API Failed

```
┌─────────────────────────────────────┐
│      ⚠️ Rates Unavailable           │
├─────────────────────────────────────┤
│                                     │
│  Couldn't load current exchange     │
│  rates (EUR → VES).                 │
│                                     │
│  You can continue, but the amount   │
│  shown is an estimate. Final rate   │
│  determined when recipient claims.  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Continue with Estimate    │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Wait and Retry ]                 │
│  [ Back ]                           │
│                                     │
└─────────────────────────────────────┘
```

**Triggered from:**
- [Screen 3: Amount Entry](../covenant-setup/3-amount-entry.md)

**Common causes:**
- DolarAPI down (Venezuelan exchange rates)
- CoinGecko API rate limited
- Government API censored/blocked
- Network timeout

**Recovery:**
- Use cached rate (show age: "Updated 15 min ago")
- Continue with estimate (final rate at claim time)
- Retry after network restored
- Use alternative API endpoint

---

### 4. Notification Service Failed

```
┌─────────────────────────────────────┐
│      ⚠️ Notification Warning        │
├─────────────────────────────────────┤
│                                     │
│  Covenant created successfully!     │
│                                     │
│  ⚠️  Couldn't send notification     │
│     to Elena via WhatsApp.          │
│                                     │
│  Please contact Elena manually      │
│  to let her know funds are ready.   │
│                                     │
│  Bounty code: 8923                  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    Call Elena               │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    Message Elena            │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ View Tracking ]                  │
│                                     │
└─────────────────────────────────────┘
```

**Triggered from:**
- After covenant created, before [Screen 6A](../own-wallet-path/6a-tracking.md) or [Screen 6B](../buy-seller-path/6b-tracking.md)

**Common causes:**
- WhatsApp API down
- Telegram API rate limited
- Recipient phone number invalid
- Messaging service blocked in country

**Recovery:**
- Covenant still valid (notification failure doesn't affect it)
- Sender must contact recipient manually
- Recipient can check bulletin board directly
- Retry notification later (background job)

---

### 5. Blockchain Sync Delay

```
┌─────────────────────────────────────┐
│      ⏳ Processing Transaction       │
├─────────────────────────────────────┤
│                                     │
│  Your transaction is being          │
│  processed by the Bitcoin Cash      │
│  network.                           │
│                                     │
│  This may take a few minutes        │
│  during high network load.          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   [Spinner animation]       │   │
│  └─────────────────────────────┘   │
│                                     │
│  Transaction ID:                    │
│  abc123...def456                    │
│                                     │
│  [ View on Explorer ]               │
│  [ Continue Waiting ]               │
│                                     │
└─────────────────────────────────────┘
```

**Note:** This is not an error, but a delay state. BCH blocks every ~10 minutes on average, but can vary.

---

## Error Handling Best Practices

### User-Friendly Messages

❌ **Bad:** "ERR_CONNECTION_REFUSED: ECONNREFUSED 192.168.1.100:8080"  
✅ **Good:** "Couldn't connect to Asgaya servers. Please check your internet connection."

❌ **Bad:** "HTTP 503 Service Unavailable"  
✅ **Good:** "Service temporarily unavailable. Please try again in a few moments."

### Actionable Recovery Steps

Always provide clear next steps:
1. **Retry button** - Try same action again
2. **Alternative path** - Use different feature (e.g., own wallet instead of seller)
3. **Manual fallback** - Contact recipient manually if notification fails
4. **Go home** - Exit current flow safely

### Preserve User Data

**Don't lose progress on network error:**
```javascript
// Save form state before network request
localStorage.save({
  recipient: "Elena#142",
  amount: 100.00,
  currency: "VES",
  paymentMethod: "buy_from_seller"
});

// Restore if network request fails
if (networkError) {
  restoreFormState();
  showErrorDialog();
}
```

### Graceful Degradation

**Continue with limited functionality:**
- Can't load sellers → Offer own wallet path only
- Can't get exchange rates → Show cached rate with warning
- Can't send notification → Show manual contact options
- Can't verify covenant → Allow tracking via blockchain explorer

---

## Technical Implementation

### Timeout Configuration

```javascript
const API_TIMEOUTS = {
  bulletinBoard: 10000,    // 10 sec
  blockchain: 30000,       // 30 sec (block propagation)
  exchangeRates: 5000,     // 5 sec
  notifications: 15000     // 15 sec
};

// Retry strategy
const RETRY_CONFIG = {
  maxAttempts: 3,
  backoffMs: 1000,         // 1 sec, 2 sec, 4 sec
  exponentialBackoff: true
};
```

### Offline Detection

```javascript
// Detect offline state
window.addEventListener('offline', () => {
  showOfflineWarning();
  pauseBackgroundSync();
});

window.addEventListener('online', () => {
  hideOfflineWarning();
  resumeBackgroundSync();
  retryFailedRequests();
});
```

### Fallback APIs

```javascript
// Primary and fallback endpoints
const API_ENDPOINTS = {
  bulletinBoard: [
    'https://bulletin.asgaya.org',
    'https://bulletin-backup.asgaya.org',
    'ipfs://Qm...' // IPFS fallback
  ],
  exchangeRates: [
    'https://api.dolarapi.com',
    'https://api.coingecko.com',
    'https://backup-rates.asgaya.org'
  ],
  bchNodes: [
    'https://bch-node-1.asgaya.org',
    'https://bch-node-2.asgaya.org',
    'https://public-bch-node.com'
  ]
};

// Try each endpoint until success
async function fetchWithFallback(endpoints, path) {
  for (const endpoint of endpoints) {
    try {
      return await fetch(`${endpoint}${path}`);
    } catch (err) {
      console.warn(`Failed to fetch from ${endpoint}:`, err);
    }
  }
  throw new Error('All endpoints failed');
}
```

---

## Monitoring & Debugging

### Network Status Dashboard

**For developers:**
- Real-time service status
- API response times
- Error rate by endpoint
- User impact metrics

**For users:**
- Simple status page: "All systems operational" or "Investigating bulletin board issue"
- Subscribe to status updates

### Error Reporting

```javascript
// Send error telemetry (with user consent)
reportError({
  type: 'network_error',
  endpoint: 'bulletin-board',
  errorCode: 'ECONNREFUSED',
  userAgent: navigator.userAgent,
  timestamp: Date.now(),
  userId: hashedUserId, // Privacy-preserving
  recoveryAction: 'retry_success' // or 'user_gave_up'
});
```

---

## Related Documentation

- [Screen 1: Home](../../home-screen.md) - Entry point
- [Screen 4.5: Select Seller](../buy-seller-path/4.5-select-seller.md) - Bulletin board dependency
- [Screen 3: Amount Entry](../covenant-setup/3-amount-entry.md) - Exchange rate dependency
- [Bizum Timeout Error](./bizum-timeout.md) - Payment window expiry
- [Decentralized Pull System](../../../../concepts/pull-system.md) - Bulletin board architecture

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
