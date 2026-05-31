# Error: Bizum Timeout

**Part of:** [Sender Flows](../README.md) → Errors  
**Triggered from:** [Screen 6B: Tracking](../buy-seller-path/6b-tracking.md) (Buy from Seller Path)

---

## When Shown

Shown when sender doesn't send Bizum payment to BCH seller within the 5-minute volatility window.

**Trigger conditions:**
- 5 minutes elapsed since [Screen 5: Payment Instructions](../buy-seller-path/5-payment-instructions.md) shown
- BCH seller bot didn't receive Bizum SMS notification
- Covenant created but NOT funded (no collateral posted)

---

## Error Screen

```
┌─────────────────────────────────────┐
│           ⏰ Payment Expired         │
├─────────────────────────────────────┤
│                                     │
│  Your order #REM-89234 expired      │
│  because Bizum payment wasn't       │
│  received within 5 minutes.         │
│                                     │
│  No covenant was created.           │
│  No charges were made.              │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                     │
│  💡 Why 5 minutes?                  │
│  BCH sellers have volatility        │
│  exposure while waiting for your    │
│  Bizum. 5 minutes keeps their       │
│  risk low (0.5-1% typical).         │
│                                     │
│  ┌─────────────────────────────┐   │
│  │      Create New Order       │   │
│  └─────────────────────────────┘   │
│                                     │
│  [ Back to Home ]                   │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

- **Tap "Create New Order"** → Return to [Screen 2: Recipient Selection](../covenant-setup/2-recipient-selection.md)
  - Pre-fills previous recipient (Elena#142)
  - User can adjust amount or continue with same details
- **Tap "Back to Home"** → Return to [Screen 1: Home](../../home-screen.md)

---

## Why 5-Minute Window?

### BCH Seller Volatility Exposure

**During waiting period:**
- Seller commits to sell BCH at locked rate
- BCH price can fluctuate
- Seller has NO hedge until Bizum received
- Longer wait = higher volatility risk

**Typical BCH volatility:**
```
5 minutes:  0.5-1% typical
15 minutes: 1-2% typical
1 hour:     2-5% possible
```

**Seller's risk calculation:**
```
Rate locked: 1 BCH = €1,000
Wait time: 5 minutes
Max expected volatility: 1%
Buffer needed: ~7% (includes overhead)

If wait time = 1 hour:
Max expected volatility: 5%
Buffer needed: ~15-20% (unsustainable)
```

### Hedge Activation

**After Bizum received:**
- Seller receives €100 fiat (hedged)
- Seller posts BCH collateral to covenant
- Exposure reduced 94-97% (only margin at risk)
- Can wait 24h for recipient to claim (low risk)

**Related:** [BCH Sellers - Hedge Mechanism](../../../../concepts/bch-sellers.md#the-hedge-mechanism)

---

## What Happens on Timeout?

### Covenant Status

```javascript
// Covenant created but NOT funded
const covenantStatus = {
  id: "REM-89234",
  status: "cancelled", // Never funded
  sender: "Iris",
  recipient: "Elena#142",
  amount: 100.00,
  created: "2026-05-16T10:00:00Z",
  expired: "2026-05-16T10:05:00Z", // 5 min timeout
  funded: false, // No BCH collateral posted
  refund: "N/A" // No charges made
};
```

### No Charges Made

- **Sender:** No Bizum sent = No money lost
- **Seller:** No collateral posted = No exposure
- **Recipient:** Never notified = Unaware of attempt

### Clean State

- Covenant removed from pending list
- Sender can immediately create new order
- No duplicate covenant restriction (unfunded covenant doesn't count)

---

## Common Causes

### 1. User Changed Mind

**Scenario:**
- User saw payment instructions (Screen 5)
- Decided not to proceed
- Didn't send Bizum
- Timeout occurred naturally

**Recovery:**
- No action needed
- Can create new order anytime

### 2. Banking App Issues

**Scenario:**
- User tried to send Bizum
- Banking app crashed/timeout
- Payment not processed
- Asgaya shows timeout error

**Recovery:**
- Check bank app for pending transactions
- If Bizum sent but not received: Contact seller support
- Otherwise: Create new order and retry

### 3. Incorrect Payment Details

**Scenario:**
- User sent Bizum with wrong amount/concept
- Seller bot rejected (no match)
- Timeout occurred
- Money needs manual refund from seller

**Recovery:**
- Contact seller support with Bizum receipt
- Seller manually refunds incorrect payment
- Create new order with correct details

### 4. Network Issues

**Scenario:**
- Seller bot offline (no liveness signal)
- SMS notifications delayed
- Bizum received but not processed
- Timeout occurred

**Recovery:**
- Contact seller support
- Seller manually processes payment (if received)
- Or create new order with different seller

---

## Prevention Tips

**For Users:**
1. **Have Bizum ready:** Open banking app before starting order
2. **Copy details:** Use "Copy details" button (reduces errors)
3. **Send quickly:** 5 minutes is enough, but don't wait
4. **Double-check:** Verify amount/concept before sending
5. **Return to app:** Come back to confirm after Bizum sent

**For Sellers:**
1. **Reliable bot:** Keep smsbridge_loop.py running 24/7
2. **Fast notifications:** Use Bizum API (5-15 sec) instead of SMS polling
3. **Clear instructions:** Phone number, amount, concept all visible
4. **Longer window?** Could extend to 10-15 min if volatility allows

---

## Technical Implementation

### Countdown Timer

```javascript
// Start 5-minute countdown when Screen 5 shown
const paymentWindow = 5 * 60 * 1000; // 5 minutes in ms
const startTime = Date.now();
const expiryTime = startTime + paymentWindow;

// Update UI every second
setInterval(() => {
  const remaining = expiryTime - Date.now();
  
  if (remaining <= 0) {
    // Show Bizum timeout error
    showBizumTimeoutError();
  } else {
    // Update countdown display
    const minutes = Math.floor(remaining / 60000);
    const seconds = Math.floor((remaining % 60000) / 1000);
    updateDisplay(`${minutes}m ${seconds}s`);
  }
}, 1000);
```

### Seller Bot Check

```python
# Seller bot: Check for Bizum receipt
async def wait_for_bizum(covenant_id, timeout=300):
    start_time = time.time()
    
    while time.time() - start_time < timeout:
        # Check Bizum API or SMS
        bizum = check_bizum_notifications()
        
        if bizum and matches_covenant(bizum, covenant_id):
            # Fund covenant with BCH collateral
            fund_covenant(covenant_id, bizum.amount)
            return True
        
        await asyncio.sleep(5)  # Check every 5 seconds
    
    # Timeout: Mark covenant as cancelled
    cancel_covenant(covenant_id, reason="bizum_timeout")
    return False
```

---

## Related Documentation

- [Screen 5: Payment Instructions](../buy-seller-path/5-payment-instructions.md) - Where timer starts
- [Screen 6B: Tracking](../buy-seller-path/6b-tracking.md) - Shows countdown
- [Pending Covenant Error](./pending-covenant.md) - Duplicate covenant prevention
- [BCH Sellers Concept](../../../../concepts/bch-sellers.md) - Seller volatility exposure
- [With volatility buffer Bounty Contracts](../../../../concepts/bounty-contracts-with-volatility-buffer.md) - Covenant lifecycle

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
