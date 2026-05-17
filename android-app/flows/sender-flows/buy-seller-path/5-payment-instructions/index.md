# Screen 5: Payment Instructions (Bizum to BCH Seller)

**Part of:** [Sender Flows](../README.md) → [Buy from Seller Path](./4b-confirm-purchase.md)  
**Previous:** [4.5: Select Seller](./4.5-select-seller.md)  
**Next:** [6B: Tracking](./6b-tracking.md)

---

## Purpose

Provide clear instructions for sending Bizum payment to the selected BCH seller. The concept field links the payment to the recipient's covenant.

---

## Screen Layout

```
┌─────────────────────────────────────┐
│ ◄ Back      Payment Instructions    │
├─────────────────────────────────────┤
│                                     │
│   📱 Send Bizum Payment              │
│                                     │
│  ┌─────────────────────────────┐   │
│  │                             │   │
│  │  To: 612-XXX-XXX            │   │◄─ BCH seller phone
│  │                             │   │
│  │  Amount: €100.00            │   │
│  │                             │   │
│  │  Concept: Elena#142         │   │◄─ Recipient Cash Account
│  │                             │   │
│  │  [ Copy details ]           │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                    │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│                                     │
│  Instructions:                      │
│  1. Open your bank app              │
│  2. Send Bizum with exact details   │
│     (Concept field links payment    │
│      to covenant)                   │
│  3. Return here when sent           │
│                                     │
│  ⏱️ Complete within: 5 min          │
│     (Seller's volatility window)    │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━     │
│                                     │
│  💡 You're paying the BCH seller    │
│     who provides collateral for     │
│     Elena's covenant                │
│                                     │
│  ┌─────────────────────────────┐    │
│  │   I've sent the Bizum       │    │
│  └─────────────────────────────┘    │
│                                     │
│  [ Cancel order ]                   │
│                                     │
└─────────────────────────────────────┘
```

---

## Interactions

- Tap "Copy details" → Copies payment details to clipboard
- Tap "I've sent the Bizum" → Go to [Screen 6B: Tracking](./6b-tracking.md)
- Tap "Cancel order" → Return to home (covenant cancelled, no charges)

---

## Payment Details

### To (BCH Seller Phone)

- Phone number of selected BCH seller
- Example: 612-XXX-XXX (last digits masked for privacy)

### Amount

- Exact EUR amount including seller fee
- Example: €100.00 (includes 0.5% seller fee)

### Concept Field (Critical)

- **Recipient Cash Account** (e.g., Elena#142)
- Links Bizum payment to specific covenant
- Human-readable identifier
- Seller bot uses this to match payment to covenant

---

## Technical Notes

### 5-Minute Window

- Seller has full BCH exposure until Bizum received
- Typical BCH volatility in 5 min: 0.5-1% (within 7% buffer)
- Bizum usually arrives in 2-3 minutes
- After Bizum received, seller's hedge activates (94-97% exposure reduction)

**Related:** [BCH Sellers - Hedge Mechanism](../../../../concepts/bch-sellers.md#the-hedge-mechanism)

### Concept Field Matching

**Why concept = recipient Cash Account:**
- Links Bizum payment to covenant
- Human-readable identifier (seller can manually verify if needed)
- One pending covenant per (sender → recipient) pair ensures clean 1:1 matching
- Copy button reduces manual entry errors

### Seller Bot Matching Logic

```python
# Seller receives Bizum SMS notification
bizum_sender = "Iris"       # From Bizum sender field
bizum_amount = 100.00       # From SMS
concept = "Elena#142"       # From concept field

# Find matching covenant (always exactly one)
covenant = find_pending_covenant(
    sender=resolve_address(bizum_sender),  # Iris's BCH address
    recipient=concept,                      # Elena#142
    amount_eur=bizum_amount                # €100
)

# Sign and fund covenant with BCH collateral
sign_and_fund_covenant(covenant)
```

---

## Android Autocomplete Enhancement

**Future optimization:**
- Use Android's autofill framework
- Pre-fill Bizum fields in banking app
- Reduces context switching and typing errors
- Simpler UX, less manual errors

**Implementation:**
- Copy details directly to banking app
- User just confirms in bank app
- Returns to Asgaya app when done

---

## Error Handling

### Timeout (5 minutes elapsed)

If Bizum not received within 5 minutes:
- Covenant cancelled (unfunded)
- No charges made
- User shown [Bizum Timeout Error](../errors/bizum-timeout.md)
- Can retry immediately (create new order)

### Wrong Amount

If Bizum amount doesn't match:
- Seller bot rejects payment
- User notified to resend correct amount
- Original payment refunded manually by seller

### Wrong Concept Field

If concept doesn't match any pending covenant:
- Seller bot cannot match payment
- User contacted for clarification
- Manual resolution required

---

## Related Documentation

- [Screen 4.5: Select Seller](./4.5-select-seller.md) - Previous screen
- [Screen 6B: Tracking](./6b-tracking.md) - Next screen (waiting for Bizum confirmation)
- [Bizum Timeout Error](../errors/bizum-timeout.md) - If payment not sent in time
- [BCH Sellers Concept](../../../../concepts/bch-sellers.md) - Seller role details
- [Overcollateralized Bounty Contracts](../../../../concepts/overcollateralized-bounty-contracts.md) - Covenant specification

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
