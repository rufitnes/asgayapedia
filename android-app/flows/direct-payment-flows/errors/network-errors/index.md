# Error: Network Errors

**Part of:** [Direct Payment Flows](../README.md)  
**Triggered from:** [Screen 3: Confirm & Send](../3-confirm-send.md)  
**Date:** 2026-05-16

---

## Error Types

### 1. Broadcast Failure

**Screen Wireframe:**
```
┌─────────────────────────────────────┐
│ ◄ Back       Broadcast Failed        │
├─────────────────────────────────────┤
│                                     │
│           ⚠️                         │
│                                     │
│   Could not broadcast transaction   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Your BCH payment to        │   │
│  │  CafeRosa#789 could not be  │   │
│  │  sent to the network.       │   │
│  │                             │   │
│  │  Amount: 0.01 BCH (~€10)    │   │
│  │                             │   │
│  │  No BCH has been deducted   │   │
│  │  from your wallet.          │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│   Possible causes:                  │
│   • Poor internet connection        │
│   • BCH node temporarily offline    │
│   • Network congestion              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │       Retry                 │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Cancel payment ]                 │
│                                     │
└─────────────────────────────────────┘
```

**Trigger:**
```javascript
try {
  const txid = await broadcastTransaction(signedTx);
  navigateTo("4-complete.md");
} catch (error) {
  if (error.code === "BROADCAST_FAILED") {
    showError("broadcast-failed", {
      recipient: "CafeRosa#789",
      amount: 0.01 BCH,
      reason: error.message
    });
  }
}
```

---

### 2. Connection Timeout

**Screen Wireframe:**
```
┌─────────────────────────────────────┐
│ ◄ Back       Connection Timeout      │
├─────────────────────────────────────┤
│                                     │
│           ⏱️                         │
│                                     │
│   Connection timed out              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Could not reach BCH network│   │
│  │  within 30 seconds.         │   │
│  │                             │   │
│  │  No BCH has been sent.      │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│   Check your internet connection    │
│   and try again.                    │
│                                     │
│  ┌─────────────────────────────┐   │
│  │       Retry                 │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Cancel payment ]                 │
│                                     │
└─────────────────────────────────────┘
```

**Trigger:**
```javascript
try {
  const txid = await broadcastTransaction(signedTx, { timeout: 30000 });
} catch (error) {
  if (error.code === "TIMEOUT") {
    showError("connection-timeout");
  }
}
```

---

### 3. Invalid Transaction

**Screen Wireframe:**
```
┌─────────────────────────────────────┐
│ ◄ Back       Transaction Rejected    │
├─────────────────────────────────────┤
│                                     │
│           ❌                         │
│                                     │
│   Transaction rejected by network   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  The BCH network rejected   │   │
│  │  your transaction.          │   │
│  │                             │   │
│  │  Possible reasons:          │   │
│  │  • Insufficient funds       │   │
│  │  • Spent outputs (UTXO)     │   │
│  │  • Invalid signature        │   │
│  │                             │   │
│  │  No BCH has been sent.      │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│   Your wallet will refresh.         │
│   Please try again.                 │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Refresh & Retry           │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to home ]                   │
│                                     │
└─────────────────────────────────────┘
```

**Trigger:**
```javascript
try {
  const txid = await broadcastTransaction(signedTx);
} catch (error) {
  if (error.code === "INVALID_TX") {
    // Refresh wallet state
    await syncWallet();
    
    showError("transaction-rejected", {
      reason: error.message,
      technicalDetails: error.details
    });
  }
}
```

---

### 4. Node Unavailable

**Screen Wireframe:**
```
┌─────────────────────────────────────┐
│ ◄ Back       Node Unavailable        │
├─────────────────────────────────────┤
│                                     │
│           🔌                         │
│                                     │
│   BCH node unavailable              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  Cannot connect to BCH      │   │
│  │  network nodes.             │   │
│  │                             │   │
│  │  Asgaya is trying:          │   │
│  │  ✅ node1.fulcrum.cash      │   │
│  │  ⏳ node2.fulcrum.cash      │   │
│  │  ⏸️  node3.fulcrum.cash      │   │
│  │                             │   │
│  │  No BCH has been sent.      │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│   We're trying alternative nodes.   │
│   This may take a minute.           │
│                                     │
│  ┌─────────────────────────────┐   │
│  │    Keep Waiting             │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Cancel payment ]                 │
│                                     │
└─────────────────────────────────────┘
```

**Trigger:**
```javascript
// Try multiple nodes with fallback
const nodes = [
  "node1.fulcrum.cash",
  "node2.fulcrum.cash",
  "node3.fulcrum.cash"
];

let lastError = null;
for (const node of nodes) {
  try {
    const txid = await broadcastToNode(node, signedTx);
    navigateTo("4-complete.md");
    return; // Success
  } catch (error) {
    lastError = error;
    showNodeRetrying(node);
  }
}

// All nodes failed
showError("node-unavailable", {
  nodesAttempted: nodes,
  lastError: lastError
});
```

---

## Interactions

### Broadcast Failure
- **Retry** → Attempt broadcast again
- **Cancel payment** → Return to [Screen 1: Scan Merchant](../1-scan-merchant.md)

### Connection Timeout
- **Retry** → Attempt broadcast again
- **Cancel payment** → Return to [Screen 1](../1-scan-merchant.md)

### Invalid Transaction
- **Refresh & Retry** → Sync wallet, rebuild transaction, retry
- **Back to home** → Return to home screen

### Node Unavailable
- **Keep Waiting** → Continue trying alternative nodes
- **Cancel payment** → Return to [Screen 1](../1-scan-merchant.md)

---

## Error Handling Strategy

### 1. Automatic Retries (Silent)
```javascript
// Retry 3 times automatically before showing error
const MAX_RETRIES = 3;
let attempts = 0;

while (attempts < MAX_RETRIES) {
  try {
    const txid = await broadcastTransaction(signedTx);
    return txid; // Success
  } catch (error) {
    attempts++;
    if (attempts < MAX_RETRIES) {
      await sleep(1000); // Wait 1 second
    } else {
      showError(error); // Show error after 3 failures
    }
  }
}
```

### 2. Node Fallback (Automatic)
```javascript
// Try multiple nodes automatically
const nodes = getAvailableNodes();

for (const node of nodes) {
  try {
    return await broadcastToNode(node, signedTx);
  } catch (error) {
    console.log(`Node ${node} failed, trying next...`);
  }
}

showError("all-nodes-failed");
```

### 3. Wallet State Validation
```javascript
// Before broadcast, validate wallet state
const isValid = await validateWalletState({
  balance: userBalance,
  utxos: userUTXOs,
  address: userAddress
});

if (!isValid) {
  await syncWallet(); // Refresh wallet
  showWarning("Wallet refreshed, please try again");
}
```

---

## Technical Notes

### Broadcast Timeout Handling
```javascript
// Set timeout for broadcast (30 seconds)
const broadcastWithTimeout = (tx, timeout = 30000) => {
  return Promise.race([
    broadcastTransaction(tx),
    new Promise((_, reject) => 
      setTimeout(() => reject(new Error("TIMEOUT")), timeout)
    )
  ]);
};
```

### Network Error Detection
```javascript
// Detect specific network errors
const handleBroadcastError = (error) => {
  if (error.message.includes("insufficient funds")) {
    showError("insufficient-balance");
  } else if (error.message.includes("txn-mempool-conflict")) {
    showError("transaction-already-spent");
  } else if (error.message.includes("timeout")) {
    showError("connection-timeout");
  } else {
    showError("generic-network-error", { details: error.message });
  }
};
```

### UTXO Double-Spend Prevention
```javascript
// Mark UTXOs as "pending" immediately after user confirms
const markUTXOsAsPending = (utxos) => {
  utxos.forEach(utxo => {
    utxo.status = "pending";
    utxo.pendingSince = Date.now();
  });
  
  // Don't use these UTXOs in other transactions
  saveWalletState();
};

// If broadcast fails, unmark UTXOs
const unmarkUTXOs = (utxos) => {
  utxos.forEach(utxo => {
    utxo.status = "available";
    delete utxo.pendingSince;
  });
  
  saveWalletState();
};
```

---

## User Experience Principles

### 1. Safety First
- ✅ Always confirm "No BCH has been sent" on error
- ✅ Never leave user uncertain about transaction status
- ✅ Refresh wallet state after errors

### 2. Clear Communication
- ✅ Explain error in plain language (no technical jargon)
- ✅ Show possible causes and solutions
- ✅ Provide actionable next steps

### 3. Automatic Recovery
- ✅ Retry silently (3 attempts) before showing error
- ✅ Try multiple nodes automatically
- ✅ Refresh wallet state on validation errors

### 4. Transparency
- ✅ Show which nodes are being tried
- ✅ Show progress (if waiting for nodes)
- ✅ Provide option to cancel at any time

---

## Design Notes

**Why 30-second timeout?**
- BCH broadcast typically < 5 seconds
- 30 seconds allows for slow connections
- Prevents indefinite waiting

**Why show "No BCH has been sent"?**
- Users worry their money is lost
- Clear reassurance needed
- Prevents panic and support requests

**Why retry automatically?**
- Many errors are transient (temporary network issues)
- Better UX than showing error immediately
- Most retries succeed

**Why multiple nodes?**
- Single node can be offline or slow
- Increases reliability
- Standard wallet best practice

---

## Related Documentation

- **[Screen 3: Confirm & Send](../3-confirm-send.md)** - Where errors are triggered
- **[Screen 4: Complete](../4-complete.md)** - Success path
- **[Direct Payment Overview](../README.md)** - Flow structure

---

*Error documented: 2026-05-16*  
*Status: Active - Network Error Handling*
