# Fraud Protection: Payment-First + Legal Recourse

**Last Updated:** 2026-06-18

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

## Why Seller Fraud is Deterred

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
4. Covenant expires after 24 hours
5. BCH locked in covenant, unusable
```

**Resolution:**

**Current design (needs refinement):**
- If Elena doesn't claim: Isabel can reclaim BCH after timeout
- María has legal recourse against Isabel (paid for service not delivered)
- Isabel must either:
  - Refund María's €100
  - Or help Elena claim (contact her, resolve issue)

**Why Isabel is still liable:**
- María paid for remittance service
- Service not completed (Elena didn't receive)
- Isabel took payment, must deliver or refund

**Current design (Phase 0):**
- Covenant expires → BCH locked goes to María's wallet (she owns the covenant)
- María's wallet auto-mints H€ or HAu (if settings enabled + bull pool has capacity)
- María can try again with different seller OR send H€ to Elena
- Isabel has **zero liability** on active covenant (payment-first approach - she already funded after receiving fiat)

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

- [Payment-First Covenant Model](../the-mechanism/wallet/README.md)
- [Sender Journey - Fraud Prevention](../user-journeys/remittance/sender/README.md#what-prevents-fraud)
- [Notification Bot - Passive Seller](../the-mechanism/notification-bot/README.md)
- [Risks & Disclaimers](/risks-and-disclaimers.md)

---

## The Bottom Line

**Fraud protection in Asgaya:**

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
