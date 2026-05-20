# Screen 4: Complete

**Part of:** [Direct Payment Flows](README.md)  
**Previous:** [Screen 3: Confirm & Send](3-confirm-send.md)  
**Date:** 2026-05-16

---

## Screen Wireframe

```
┌─────────────────────────────────────┐
│         Payment Complete!            │
├─────────────────────────────────────┤
│                                     │
│           ✅                         │
│                                     │
│   Payment sent successfully!        │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  To: CafeRosa#789           │   │
│  │                             │   │
│  │  Amount sent: 0.01 BCH      │   │
│  │  (~€10 = ~5,000 VES)        │   │
│  │                             │   │
│  │  Network fee: €0.002        │   │
│  │                             │   │
│  │  ━━━━━━━━━━━━━━━━━━━━━━━   │   │
│  │                             │   │
│  │  Total cost: 0.010002 BCH   │   │
│  │  (~€10.002)                 │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Your new balance:                 │
│   0.489998 BCH (~€490)              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   Transaction ID:                   │
│   tx:a3f2...9c8d                    │◄─ Blockchain txid
│   [ View on explorer ]              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│   ⚡ Merchant received immediately   │
│   (Standard BCH transaction)        │
│                                     │
│  ┌─────────────────────────────┐   │
│  │       Done                  │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Make another payment ]           │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

**Primary Action:**
- Tap **"Done"** → Return to Home screen

**Secondary Actions:**
- Tap **"Make another payment"** → Return to [Screen 1: Scan Merchant](1-scan-merchant.md)
- Tap **"View on explorer"** → Open blockchain explorer (e.g., blockchair.com)

**Automatic:**
- Transaction broadcast to BCH network
- Merchant receives in ~10 seconds (0-conf)
- Confirmed in ~10 minutes (first block)

---

## Transaction Details

**Standardized Example:**
```
Recipient: CafeRosa#789 (bitcoincash:qr2x...)
Amount sent: 0.01 BCH (~€10 = ~5,000 VES)
Network fee: 0.000002 BCH (~€0.002)
Total cost: 0.010002 BCH (~€10.002)

User balance before: 0.5 BCH
User balance after: 0.489998 BCH

Transaction ID: a3f2bc47...8d9c (full txid)
Broadcast time: 2026-05-16 14:32:15 UTC
Confirmations: 0 (merchant receives immediately)
```

---

## Features

### Transaction Receipt
- Shows complete payment details
- Recipient (Cash Account + address)
- Amount sent (BCH + fiat equivalent)
- Network fee (transparent)
- Total cost (BCH + fiat)

### Balance Update
- Shows updated balance immediately
- Fiat equivalent based on current rate
- Matches balance check from Screen 3

### Blockchain Verification
- Transaction ID (txid) visible
- Link to blockchain explorer
- User can verify payment publicly
- Merchant can check receiving address

### Instant Settlement
- No waiting period
- No tracking screen needed
- Merchant received immediately
- Standard BCH behavior (0-conf acceptance)

---

## Comparison with Covenant Flow

| Feature | Direct Payment | Covenant Flow |
|---------|---------------|---------------|
| **Completion time** | Instant (10s) | After claim (up to 24h) |
| **Tracking needed** | No | Yes (Screen 6) |
| **Transaction type** | Standard BCH tx | Covenant contract |
| **Receipt** | Txid only | Covenant ID + txid |
| **Merchant action** | None (received) | Must claim (unless auto-claim) |

---

## Design Notes

**Why no tracking screen?**
- Standard BCH transaction (instant broadcast)
- Merchant receives immediately (0-conf)
- No claim process (direct transfer)
- User sees "Done" right away

**Why show txid?**
- Transparency (user can verify on blockchain)
- Merchant can check their address
- Standard wallet UX (all wallets show txid)

**Why "Merchant received immediately"?**
- Sets expectations (vs 24h covenant)
- Explains no tracking needed
- Standard BCH behavior

---

## Technical Notes

**Transaction Broadcast:**
```javascript
// After user confirmed on Screen 3
const tx = buildTransaction({
  from: userUTXOs,
  to: merchantAddress,
  amount: 0.01 BCH,
  fee: 0.000002 BCH
});

// Sign and broadcast
const signedTx = tx.sign(userPrivateKey);
const txid = await broadcastTransaction(signedTx);

// Update UI
displayCompletionScreen({
  txid: txid,
  recipient: "CafeRosa#789",
  amount: 0.01 BCH,
  fee: 0.000002 BCH,
  newBalance: getUserBCHBalance() // 0.489998 BCH
});
```

**Confirmation Status:**
```javascript
// Optional: Track confirmations in background
const confirmations = await getConfirmations(txid);

// 0 confirmations: Merchant sees in mempool (instant)
// 1+ confirmations: Included in block (~10 min)
// 6+ confirmations: Fully confirmed (~1 hour)

// Note: Most merchants accept 0-conf for small amounts
```

**Explorer Link:**
```javascript
// Link to blockchain explorer
const explorerUrl = `https://blockchair.com/bitcoin-cash/transaction/${txid}`;

// User can verify:
// - Transaction exists on blockchain
// - Correct recipient address
// - Correct amount
// - Fee paid
```

---

## Related Documentation

- **[Screen 3: Confirm & Send](3-confirm-send.md)** - Previous step
- **[Screen 1: Scan Merchant](1-scan-merchant.md)** - Start new payment
- **[Direct Payment Overview](README.md)** - Flow structure

---

*Screen documented: 2026-05-16*  
*Status: Active - Standard Wallet Functionality*
