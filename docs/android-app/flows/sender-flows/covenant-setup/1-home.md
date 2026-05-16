# Screen 1: Home (Entry Point)

**Part of:** [Sender Flows](../README.md) → Covenant Setup  
**Next:** [Screen 2: Recipient Selection](2-recipient-selection.md)  
**Date:** 2026-05-16

---

## Screen Wireframe

```
┌─────────────────────────────────────┐
│ ☰                    Asgaya      🌐 │
├─────────────────────────────────────┤
│                                     │
│     Welcome to Asgaya               │
│     Your Bitcoin Cash Wallet        │◄─ Position as wallet
│                                     │
│  ┌───────────────────────────────┐  │
│  │                               │  │
│  │   💸 Send to Asgaya User      │  │◄─ Covenant-based flow
│  │   Recipient chooses BCH/cash  │  │
│  │                               │  │
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │                               │  │
│  │   🪙 Pay with Bitcoin Cash    │  │◄─ Standard BCH payment
│  │   Direct payment to merchant  │  │
│  │                               │  │
│  └───────────────────────────────┘  │
│                                     │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━���━   │
│                                     │
│  Recent activity:                   │
│  • Sent 0.1 BCH to Elena ✓         │◄─ Show BCH amounts
│  • Paid 0.01 BCH to Café Rosa ✓    │
│                                     │
│  [ Settings ]      [ Help ]         │
└─────────────────────────────────────┘
```

---

## Interactions

**Primary Actions:**
- Tap **"Send to Asgaya User"** → Go to [Screen 2: Recipient Selection](2-recipient-selection.md)
- Tap **"Pay with Bitcoin Cash"** → Go to standard BCH payment flow (wallet functionality, non-Asgaya)

**Secondary Actions:**
- Tap recent activity item → View transaction details
- Tap **Settings** → App settings
- Tap **Help** → Help center

---

## Two Distinct Flows

### Button 1: "Send to Asgaya User" (Covenant Flow)

**Purpose:** Send money to someone in Asgaya network

**Characteristics:**
- Creates covenant (24-hour claim window)
- Recipient chooses: BCH (free) or Cash at merchant (0.5% fee)
- Uses Cash Accounts (Elena#142)
- Covenant-based architecture
- For: Family, friends, contacts in Asgaya network

**Flow continues:** [Screen 2: Recipient Selection](2-recipient-selection.md)

---

### Button 2: "Pay with Bitcoin Cash" (Direct Payment)

**Purpose:** Pay merchant/anyone who accepts BCH (not in Asgaya)

**Characteristics:**
- Standard BCH wallet transaction (no covenant)
- Direct payment (instant settlement, no claim process)
- Uses standard BCH addresses or Cash Accounts
- No Asgaya infrastructure needed
- For: Merchants outside Asgaya, direct BCH payments

**Flow continues:** Standard wallet functionality (not covered in sender-flows)

---

## Design Notes

**Regulatory Framing:**
- Positioned as "Bitcoin Cash Wallet" (not remittance service)
- "Send BCH" language (crypto-first)
- Recent activity shows BCH amounts (not fiat)

**UX Clarity:**
- Two buttons clearly separated
- Visual distinction between flows
- Recent activity differentiates: "Sent" (Asgaya) vs "Paid" (direct)

**Fail-Safe:**
- Even if user picks wrong button, BCH reaches recipient
- Different UX, same end result (BCH delivered)

---

## Related Documentation

- **[Sender Flows Overview](../README.md)** - Complete flow structure
- **[Recipient Flows](../../recipient-flows.md)** - How recipients claim
- **[Merchant Flows](../../merchant-flows.md)** - How merchants provide cash

---

*Screen documented: 2026-05-16*  
*Status: Active - Covenant Architecture*
