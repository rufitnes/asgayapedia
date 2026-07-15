# Fraud Protection: Payment-First + Legal Recourse

**Last Updated:** 2026-07-15

---

## The Core Protection: Payment-First Covenant Model

**Traditional escrow (how others do it):**
```
1. Seller locks BCH first (shows good faith)
2. Buyer pays fiat
3. Escrow releases BCH to buyer
Problem: Seller has capital locked, limits throughput
```

**Asgaya's payment-first approach:**
```
1. María creates covenant (no BCH locked yet)
2. María pays Isabel €100.50 via Bizum
3. Isabel's bank confirms payment
4. Isabel's bot funds covenant with BCH
5. Elena receives BCH
Result: Seller never has capital at risk
```

**This seems backwards** - why would the buyer trust the seller to fund AFTER receiving payment?

**Answer: Because fraud is traceable and criminal.**

---

## Proactive Fraud Prevention: Device Health Checks

**Before payment-first deterrence kicks in, Asgaya prevents fraud proactively.**

When María requests payment details from Isabel's bot, she doesn't just get a bank account number—she also receives **device health metrics**:

```json
{
  "payment_info_response": {
    "account_number": "+34-612-345-678",
    "reference": "Elena#142",
    "amount_exact": "€100.50",
    
    "device_health": {
      "bank_app_installed": true,
      "bank_app_enabled": true,
      "battery_optimized": false,
      "battery_level": 67,
      "is_charging": true
    }
  }
}
```

**María's app evaluates health BEFORE she pays:**

**Healthy device (green light):**
```
✅ Seller ready (67% battery, charging)
[Open Bizum App] → Proceed with confidence
```

**Unhealthy device (warning):**
```
⚠️ SELLER DEVICE ISSUES
• Bank app disabled
• Battery at 8% (not charging)

Seller may not receive your payment notification.
[Pick Different Seller] ← Recommended
[Continue Anyway]
```

**Critical issues (payment blocked):**
```
🛑 SELLER NOT READY
• Bank app not installed

This seller cannot receive payment notifications.
DO NOT PAY.
[Pick Different Seller] ← Only option
```

### Why This Matters

**Without health checks:**
- María pays → Seller's bank app is disabled → No notification → No covenant funding → María waits 48h for timeout
- Fraud vector: Malicious seller disables app, collects payments, claims "never got notification"

**With health checks:**
- María sees "Bank app disabled" → Cancels covenant → Picks healthy seller → No money at risk
- Malicious sellers can't hide (unhealthy status visible to all buyers)
- Honest sellers maintain device health for good reputation

**This is fraud prevention BEFORE payment happens.** No legal recourse needed because no money changes hands with bad actors.

**See:** [Device Health Checks](../the-mechanism/nostr-coordination/device-health.md) for full technical documentation.

---

## Why Seller Fraud is Deterred (Payment-First Model)

### 1. Bank Transfer Traceability

**Bizum (Spain) includes:**
- Seller's full name (e.g., "Isabel García Rodríguez")
- Seller's phone number (linked to bank account)
- Seller's bank account number (IBAN)
- Transaction ID (permanent record)

**María has all this information the moment she pays.**

If Isabel doesn't fund the covenant:
- María knows exactly who Isabel is
- Has her bank account details
- Has proof of payment (bank statement)
- Can file police report with this information

**Isabel can't ghost anonymously.** Her identity is known.

---

### 2. Spanish Legal Precedent: IR006 Case

**Real case (IR006):**
- Person A sends €20 via Bizum to Person B
- Person B keeps the money, doesn't deliver promised item
- Person A files complaint
- **Spanish court rules:** This is **criminal misappropriation** (not just civil breach)

**Penalty:**
- €20 returned to Person A
- €180 fine paid by Person B
- Court costs paid by Person B
- Criminal record

**For €20!**

**For Asgaya (€100 remittance):**
- Same legal framework applies
- Isabel keeps €100, doesn't fund covenant
- María files complaint
- Isabel faces:
  - €100 return
  - ~€900 fine (proportional)
  - Court costs
  - Criminal record
  - Potential jail time for repeat offenses

**Risk/reward for Isabel:**
- Gain: €100 (one time)
- Risk: €1000+ penalty + criminal record

**Irrational to attempt.**

---

### 3. Economic Deterrence

**Isabel's calculation:**

**If honest:**
- Earns €0.50 per transaction (0.5% fee)
- Can process 10-20 transactions per day
- Daily income: €5-10
- Monthly income: €150-300
- **Annual income: €1,800-3,600** (sustainable)

**If dishonest (fraud ONE transaction):**
- Gains: €100 (immediate)
- Loses: 
  - €100 return (court order)
  - €900 fine (criminal penalty)
  - €200 court costs
  - **€1,800-3,600/year forever** (banned, can't earn fees)
  - Criminal record (affects future employment)
  - **Total loss: >€2,000 + lifetime income**

**Expected value of fraud:**
```
Gain: €100
Loss: €2,000 + €3,600/year × years_active
EV = -€1,900 (first year) - €3,600 (each additional year)
```

**Massively negative expected value.**

---

### 4. No Capital Lock = Scale Without Risk

**Why payment-first enables passive sellers:**

**Traditional escrow:**
```
Isabel has €1,000
Wants to process 100 transactions per day (€10,000 volume)
Must lock BCH first → Can only do 10 transactions
Throughput limited by capital
```

**Payment-first (Asgaya):**
```
Isabel has €1,000
Payment arrives → Immediately funds covenant → BCH released
Can process 100 transactions per day with same €1,000
Throughput unlimited (bot automation + no lock time)
```

**This is why notification bots are viable:**
- Seller posts listing once
- Bot handles 100+ transactions automatically
- No capital risk (fiat arrives first)
- Passive income at scale

**Payment-first unlocks the passive seller model.**

---

## Fraud Scenarios & Resolution

### Scenario 1: Seller Receives Payment, Doesn't Fund Covenant

**What happens:**
```
1. María pays Isabel €100.50 via Bizum
2. Isabel's bank confirms receipt
3. Isabel stops her bot (malicious)
4. Covenant times out (24 hours)
5. María receives notification: "Seller did not fund covenant"
```

**María's recourse:**

**Option A: Contact seller directly**
- App provides Nostr messaging
- María: "You received my €100, please fund the covenant"
- Isabel might have legitimate issue (bot crashed, forgot)
- Most disputes resolve here (good faith)

**Option B: Request refund**
- María has Isabel's bank info (from Bizum payment)
- Sends message: "Please return €100.50 to my account"
- Isabel can return via Bizum/bank transfer
- If Isabel returns money: No legal action needed

**Option C: Legal action**
- María files complaint with Spanish police
- Provides: Bizum receipt, covenant ID, Isabel's info
- Police investigate
- Court orders Isabel to return €100 + fines
- Isabel gets criminal record

**Timeline:**
- Option A: Hours to days (most common)
- Option B: Days to weeks (if seller responsive)
- Option C: Weeks to months (if seller truly fraudulent)

**Result: María gets her money back** (via refund or court order)

---

### Scenario 2: Covenant Expires, Elena Never Claims

**What happens:**
```
1. María pays Isabel, covenant funded
2. Elena receives notification
3. Elena doesn't claim (forgot, phone died, etc.)
4. Covenant expires after 8 hours (Phase 0 limit)
5. BCH locked returns to María's wallet
```

**Resolution (Phase 0 design):**
- Covenant expires → BCH locked returns to **María's wallet** (she owns the covenant)
- **No H€ minting** - María receives BCH back, accepts volatility exposure
- María can try again with different seller OR send BCH to Elena directly
- **Isabel has zero liability** - she fulfilled her obligation (funded covenant after receiving payment)

**Why Isabel has no liability:**
- Isabel received payment via Bizum (traceable, verified)
- Isabel funded the covenant with BCH (job complete)
- María got what she paid for (€100 worth of BCH)
- If Elena doesn't claim, that's between María and Elena
- Payment-first model: Isabel's only obligation is to fund after receiving payment

---

### Scenario 3: Merchant Receives BCH, Doesn't Give Cash

**What happens:**
```
1. Elena receives €100 BCH from covenant
2. Elena goes to Carlos's shop
3. Elena releases BCH to Carlos
4. Carlos doesn't give Elena cash (fraud)
5. Elena is stuck with no money
```

**Resolution:**

**Unlike sender/seller fraud, this is local commerce:**
- Elena and Carlos are physically present
- Carlos's shop has known location
- Elena can:
  - Call police (theft)
  - File complaint with local authorities
  - Report to Venezuelan consumer protection

**Why merchant fraud is less likely:**
- Physical presence (can't ghost online)
- Reputation matters (brick-and-mortar business)
- Local community knows Carlos
- BCH release is on-chain (provable Carlos received)

**Design decision (Phase 0):**

**Two approaches considered:**
1. **Cosign approach** (preferred): Covenant releases only after both Elena AND Carlos sign
   - Elena gets cash → signs
   - Carlos receives signature → signs
   - BCH releases to Carlos
   - **Benefit:** Limits when merchants can mint H€/HAu (only from covenants they cosigned)

2. **Two-transaction approach**: BCH → Elena's wallet first, then she manually sends to Carlos
   - More steps, worse UX
   - Elena has temporary custody (adds complexity)

**Phase 0 uses cosign approach.**

---

## Why This is Better Than Reputation Systems

**Old approach (MUSD, LocalBitcoins, etc.):**
- Complex reputation scoring
- Stake slashing mechanisms
- Dispute arbitration
- Mediator role (centralization point)

**Problems:**
- Builds trust system from scratch (takes time)
- Chicken-egg: New users have no reputation
- Arbitration requires human judgment (slow, subjective)
- Mediators can be targeted by regulators

**Asgaya approach:**
- **Leverage existing legal system** (Spanish courts already work)
- **Use existing trust infrastructure** (bank identity verification)
- **No custom reputation** (legal precedent = reputation)
- **No arbitrators needed** (courts handle disputes)

**We're not building a parallel justice system. We're using the one that exists.**

---

## Compliance Advantage

**Why payment-first helps compliance:**

### For Sellers (Isabel):
- Not providing escrow service (no custody)
- Not mediating disputes (no judgment role)
- Just selling BCH for fiat (legal exchange)
- Capital never locked (not operating money transmission)

**Regulatory classification:**
- Peer-to-peer sale (like selling item on Craigslist)
- Not money transmission (no funds held for others)
- Not securities (no investment contract)

### For Buyers (María):
- Just buying BCH to send to family
- Bank transfer is traceable (KYC via banking system)
- Legal recourse available (not operating in gray area)

**The banking system provides KYC. We don't need to.**

---

## Limitations & Risks

### What Payment-First Doesn't Protect:

**1. Seller has no capital recovery if buyer lies**
- If María claims "I paid" but didn't: Isabel has no recourse
- Mitigation: Seller bot only funds after SMS received
- Bot = automatic proof of payment

**2. Legal process is slow for cross-border**
- Spanish courts good for Spain-based sellers
- Venezuelan courts slower/less reliable
- Mitigation: Focus Phase 0 on Spain-based sellers

**3. Small amounts might not justify legal action**
- Filing lawsuit for €20 might not be worth time/cost
- Mitigation: €100 minimum makes it worthwhile
- IR006 case shows even €20 prosecuted

### Fraud Still Possible (Just Irrational)

**Payment-first doesn't make fraud impossible.**  
**It makes fraud irrational** (negative expected value).

Some people are irrational. Some will attempt fraud anyway.

**Phase 0 will measure:**
- Fraud attempt rate (expect <1%)
- Resolution success rate (expect >95% resolved via refund)
- Legal action needed (expect <0.1%)

---

## Success Metrics

| Metric | Target | Indicates |
|--------|--------|-----------|
| **Seller fraud attempts** | <1% | Economic deterrence working |
| **Disputes resolved without courts** | >95% | Refund path effective |
| **Legal actions filed** | <0.1% | Most disputes informal |
| **Average resolution time** | <7 days | Reasonable for users |
| **User losses due to fraud** | <€100 total | System protecting users |

---

## Related Documents

- [Device Health Checks](../the-mechanism/nostr-coordination/device-health.md) - Proactive fraud prevention
- [Payment-First Covenant Model](../the-mechanism/wallet/README.md)
- [Sender Journey - Fraud Prevention](../user-journeys/remittance/sender/README.md#what-prevents-fraud)
- [Notification Bot - Passive Seller](../the-mechanism/notification-bot/README.md)
- [Risks & Disclaimers](/risks-and-disclaimers.md)

---

## The Bottom Line

**Fraud protection in Asgaya:**

✅ **Device health checks** - Prevent fraud BEFORE payment (proactive)  
✅ **Payment-first** - Seller receives fiat before locking BCH  
✅ **Bank traceability** - Bizum includes full identity  
✅ **Legal precedent** - Spanish courts prosecute (IR006 case)  
✅ **Economic deterrence** - Fraud EV < -€1,900  
✅ **No reputation system** - Legal system provides trust  
✅ **Enables passive sellers** - No capital lock = scale

**We don't need to build a parallel justice system. We use the one that already works.**

---

*"The best fraud protection is making fraud stupid."* - Asgaya design philosophy
---

## Navigation

**[🏠 Home](../index.md)** | **[↑ Why This Design?](README.md)** | **[📖 Glossary](../glossary.md)**
