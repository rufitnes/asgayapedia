# Freelance Payments: Same Protocol, Pro Seller for Invoicing

**Type:** Concept Document  
**Status:** ✅ Active — Phase 0  
**Date:** 2026-05-20  
**Related:** [Merchant Business Case](merchant-business-case.md), [Cold-Start Strategy](../decisions/cold-start-strategy.md), [BCH Sellers](bch-sellers.md)

---

## TL;DR

**Freelance payments use the exact same Asgaya protocol as family remittances.**

The only differences:
- **Sender uses "Pro Seller"** — BCH sellers who can issue professional business invoices (for accountants)
- **Higher amounts** — €500-2,000 typical vs €100-200 for family remittances
- **Business documentation** — Proper invoices, tax IDs, accounting-ready paperwork

**The flow, fees, and covenant mechanics are identical.** A Venezuelan freelancer receiving payment through Asgaya follows the same process as a family remittance recipient.

**Total fee:** 0.5-1.0% (vs 5-15% through PayPal/Deel/Bitwage/P2P spreads)

---

## 1. The Problem: Remote Workers Lose 5-15% of Every Payment

Venezuela has one of the highest freelance workforce participation rates in the world:
- **52% of the workforce** depends on freelance work
- **60-65% of professionals** hold college degrees
- **31% already receive payment in cryptocurrency** (highest rate globally)

All face the same problem: **getting paid costs too much.**

### What Venezuelan freelancers lose

| Payment Method | Cost on €1,500 | What Freelancer Receives |
|----------------|----------------|--------------------------|
| **PayPal** | €75-150 (5-10%) | €1,350-1,425 |
| **Deel/Bitwage** | €120-225 (8-15%) | €1,275-1,380 |
| **Binance P2P** | €45-75 (3-5%) | €1,425-1,455 |
| **Asgaya** | €7.50-15 (0.5-1%) | **€1,485-1,492.50** |

A freelancer earning €1,500/month saves **€900-1,350 per year** by switching to Asgaya.

---

## 2. How It Works (Exact Same as Remittances)

### Simple Example: María, Graphic Designer in Caracas

**María invoices €1,000 to her Madrid client for design work.**

**Step 1: Client opens Asgaya app**
- Enter: `Maria#2841` (María's CashAccount)
- Amount: €1,000
- Payment method: Select "Pro Seller"

**Step 2: App shows payment instructions**
- Pay **€1,005** to Pro Seller [IBAN/phone]
- (€1,000 payment + €5 Pro Seller fee)

**Step 3: Client pays via Bizum/SEPA**
- Bank transfer to Pro Seller: €1,005
- **Pro Seller sends invoice:** "Crypto Asset Purchase for Payment Covenant - €1,005"

**Step 4: Pro Seller posts covenant**
- Buys BCH, posts covenant to María's CashAccount
- María receives notification

**Step 5: María claims payment**

**Option A: Cash at merchant**
- Walks to local neighborhood store
- Shows claim code, receives €995 cash (€1,000 - 0.5% merchant fee)
- Merchant earns €5 fee + potential product sales if María buys groceries

**Option B: Claim as BCH**
- Taps "Claim to my wallet"
- Receives €1,000 worth of BCH directly
- No merchant needed, can hold or sell later

**Total cost:** 
- Cash path: €1,005 (0.5% seller + 0.5% merchant = 1.0%)
- BCH path: €1,005 (0.5% seller only)

**vs PayPal/Deel:** €75-150 saved

---

## 3. What Is a "Pro Seller"?

**Pro Seller = Regular BCH Seller + Professional Invoicing**

### Regular BCH Seller (for family remittances)
- Handles €50-200 transactions
- SMS/app notifications
- No formal invoices
- Serves individuals

### Pro Seller (for freelance payments)
- Handles €500-5,000+ transactions
- **Issues professional business invoices** with tax IDs
- Accounting-ready documentation
- Serves businesses
- Often registered as **autónomos** (self-employed) in Spain

**The protocol mechanics are identical.** The only difference is documentation.

---

## 4. The Accounting (Simple)

### Employer's Perspective

**Documents received:**
1. **Invoice from freelancer:** "Design services - €1,000"
2. **Invoice from Pro Seller:** "Crypto asset purchase for payment covenant - €1,005"

**Bank statement:**
- Paid Pro Seller €1,005 via Bizum/SEPA

**Accounting entry:**
```
Dr. Freelance Expense          €1,000
Dr. Payment Processing Fee        €5
Cr. Cash                      €1,005
```

**Or simplified:**
```
Dr. Freelance Expense  €1,005
Cr. Cash             €1,005
```

### How Spanish Gestor Sees It

"Company purchased cryptocurrency from a dealer (Pro Seller) to pay a foreign contractor. Net expense: €1,005 for €1,000 service. Payment processing fee: 0.5%."

**This is standard cross-border freelance accounting.** Similar to how companies use PayPal, Wise, or Deel:
- Bank shows payment to payment processor
- Invoice from contractor shows services rendered  
- Payment processor receipt links the two

The only difference: Instead of paying PayPal, they're paying a crypto dealer. The accounting structure is identical.

---

## 5. The Merchant Side: Already Professional

When María claims cash at her local neighborhood store, **that merchant is already operating as a business:**

- Registered business entity (or informal but established)
- Handles cash transactions daily
- Already does bookkeeping (even if informal)
- **Accepts Asgaya cash-outs as part of their business operations**

The merchant earns:
1. **0.5% fee** (€5 on €1,000)
2. **Product margin** (15-30%) if María buys groceries
3. **Potential seller fee** if merchant recycles BCH (triple-dip)

See [Merchant Business Case](merchant-business-case.md) for full economics.

---

## 6. Triple-Dip: When Merchants Become Sellers

**If a Venezuelan merchant has family in Spain**, they can close the loop:

### The Setup

**In Venezuela:** Merchant accepts Asgaya cash-outs (earns merchant fees)  
**In Spain:** Family member registers as **autónomo** (self-employed business), becomes a Pro Seller

### The Triple Revenue Stream

1. **Merchant fee (0.5%)** — Venezuelan merchant earns €5 per €1,000 cash-out
2. **Product margin (15-30%)** — Earns €50-150 if freelancer spends €500 on groceries
3. **Seller fee (0.5%)** — Spanish family member (as Pro Seller) earns €5 per €1,000 payment

**On a €1,000 freelance payment where recipient spends €500 on groceries:**
- Venezuelan merchant: €5 merchant fee + €75-150 product margin = **€80-155**
- Spanish family member: €5 seller fee
- **Total family earnings: €85-160 per transaction**

**For merchants processing 10 freelance payments per month:**
- Venezuelan side: €800-1,550/month
- Spanish side: €50/month
- **Total: €850-1,600/month**

This makes the Venezuelan merchant aggressively motivated to recruit local freelancers to use Asgaya.

### Registering as Autónomo in Spain

**Requirements:**
- Spanish residence (legal or family member with papers)
- Register with Agencia Tributaria (tax authority)
- Pay monthly social security (~€300-400/month base)
- Issue professional invoices with tax ID

**When it makes sense:**
- Processing €2,000+/month in freelance payments (seller fees cover autónomo costs)
- Family member already living in Spain (no additional visa needed)
- Want to build legitimate business income in Spain

**This is standard practice** for Spanish residents earning side income. Many Venezuelan migrants in Spain already operate as autónomos (consultants, delivery drivers, freelancers).

---

## 7. Comparison: Asgaya vs Traditional Methods

### On €1,500/month (typical freelancer salary)

| Method | Monthly Cost | Annual Cost | Freelancer Receives |
|--------|-------------|-------------|---------------------|
| **PayPal** | €75-150 | €900-1,800 | €1,350-1,425/month |
| **Wise** | Not available | — | — |
| **Deel/Bitwage** | €120-225 | €1,440-2,700 | €1,275-1,380/month |
| **Binance P2P** | €45-75 | €540-900 | €1,425-1,455/month |
| **Western Union** | €97+ (6.49%) | €1,164+ | €1,403/month |
| **Asgaya (Pro Seller)** | **€7.50-15** | **€90-180** | **€1,485-1,492.50/month** |

**Annual savings for €1,500/month freelancer:**
- vs PayPal: €810-1,620 saved
- vs Deel: €1,350-2,520 saved
- vs Binance P2P: €450-720 saved

---

## 8. Why This Matters for Cold Start

### Freelancers Recruit Their Own Clients

**The sender bottleneck:** Phase 0 needs 150 senders. Spanish Venezuelan families provide 50-100. **Freelancers provide the other 50-100.**

**Why freelancers are powerful:**
- **Direct financial incentive:** Save €900-1,350/year → will aggressively pitch client
- **Self-recruiting:** Freelancer convinces their own client (we don't pitch strangers)
- **Higher transaction value:** €500-2,000 vs €100-200 family remittances
- **Guaranteed recurring:** Monthly salary vs sporadic remittances
- **Tech-savvy:** Already use crypto, understand the value proposition

### Spanish Tech Companies: Priority Target

One Spanish startup with 5 Venezuelan contractors = 5 recurring monthly senders.

**Their motivation:**
- Save €600-1,200/month vs Deel/Bitwage (on 5 × €1,500 payments)
- Clean accounting (crypto asset purchase vs payment platform)
- Support their contractors (who keep more money)

**Outreach channels:**
- Barcelona/Madrid startup communities
- Remote-first company networks
- "Hire Venezuelan Talent" positioning
- Tech agency associations

### Venezuelan Freelancer Communities

**Target segments:**
- Reddit r/vzla (Venezuelan freelancers seeking payment solutions)
- Venezuelan developer Telegram groups (massive community)
- Upwork/Fiverr Venezuelan seller forums
- Venezuelan university alumni groups in Spain

**The pitch:** "Keep €900/year instead of giving it to PayPal. Takes 5 minutes to set up."

---

## 9. Corridor Scope & Limitations

### Currently Supported (Phase 0)

✅ **Spain → Venezuela**  
✅ **Europe → Venezuela** (via SEPA)

Freelancers with clients in Spain or other EU countries can use Asgaya today.

### Not Yet Supported

❌ **US → Venezuela** 

**Why:** US money transmitter licensing requirements are extremely complex. Serving US senders could trigger FinCEN registration + 50 state licenses (each with bonding requirements, background checks, and compliance costs).

**Workaround:** If your US client has EU presence (European subsidiary, EU payment agent, EU freelance platform account), they can pay you through the Europe→Venezuela corridor.

**Timeline:** We are researching the regulatory path for US→Venezuela in Phase 1+. For Phase 0, the focus is proving the model works in Spain/Europe first.

---

## 10. FAQ for Freelancers

### Can I receive in BCH directly instead of cash?

**Yes.** When claiming, tap "Claim to my wallet" instead of visiting a merchant. You receive the full payment amount in BCH (no merchant fee). You can then:
- Hold BCH as savings
- Swap for MUSD stablecoin (maintains USD value)
- Sell for cash later when needed
- Spend at BCH-accepting businesses

### What if my client pays in USD, not EUR?

They need to convert to EUR first (most European businesses can send EUR via SEPA). USD→VES corridor is planned for Phase 1+ but requires separate infrastructure.

### Do I need KYC?

**No.** Asgaya is permissionless. You don't verify identity, provide documents, or complete know-your-customer checks.

**However:** Your client might need to comply with their bank's requirements for international payments (standard for any cross-border freelance arrangement).

### What if I don't have a merchant nearby?

**Option 1:** Claim BCH directly to your wallet (no merchant needed)

**Option 2:** Use "PagoMóvil merchant" (Phase 0 feature) — trusted users in Venezuela who send you a bank transfer instead of cash. Fee is slightly higher (0.8%) to cover PagoMóvil costs.

**Option 3:** Onboard your local neighborhood store as an Asgaya merchant. They earn 0.5% fee per transaction + potential product sales. See [Merchant Business Case](merchant-business-case.md).

### Can my client pay multiple freelancers?

**Yes.** One Pro Seller can handle payments to multiple recipients:
- Client pays €5,000 to Pro Seller
- Pro Seller posts 3 separate covenants:
  - €2,000 to Freelancer A
  - €1,500 to Freelancer B  
  - €1,500 to Freelancer C

This is how Spanish tech companies with multiple Venezuelan contractors use Asgaya.

### What documentation do I need for taxes?

**Consult a Venezuelan tax advisor** familiar with SENIAT requirements for foreign income. At minimum, maintain:
- Invoices issued to clients
- Asgaya transaction receipts (proof of payment received)
- Records of amounts received (cash vs BCH)

**Asgaya provides payment infrastructure, not tax advice.** Your tax obligations depend on your specific situation.

### What if my client needs a receipt from the Pro Seller?

The Pro Seller provides a professional invoice:

```
INVOICE #12345
From: [Pro Seller Business Name]
Tax ID: [Spanish CIF/NIF]

To: [Client Company Name]  
Tax ID: [Client Tax ID]

Service: Cryptocurrency Purchase for Payment Covenant
Amount: €1,005
Date: [Date]

Description: Purchased BCH for payment to vnzlancer2001
Purpose: Facilitate international contractor payment
```

This is accounting-ready documentation that Spanish gestors understand. It bridges the bank statement (payment to Pro Seller) to the freelancer's service invoice.

---

## 11. The Bottom Line

**For freelancers earning €1,500/month:**
- Current methods: Lose €75-225/month to fees
- Asgaya: Lose €7.50-15/month
- **Savings: €900-1,350/year**

That's real money. Enough to pay for groceries, rent, internet, phone. Enough to matter.

**For Spanish businesses with Venezuelan contractors:**
- Current methods: Pay €600-1,800/month in Deel/PayPal fees (for 5 contractors)
- Asgaya: Pay €37.50-75/month
- **Savings: €562.50-1,762.50/month = €6,750-21,150/year**

That's a significant P&L improvement for a tech startup or digital agency.

**For Asgaya Phase 0:**
- Every freelancer who converts their client adds one recurring sender
- Freelancers self-recruit (direct financial benefit)
- Spanish tech companies with 5 contractors = 5 monthly senders
- **This solves the sender bottleneck**

The protocol doesn't change. The covenant mechanics are identical. The only difference is that businesses need professional invoicing—which is what Pro Sellers provide.

---

## 12. Related Documents

- [Merchant Business Case](merchant-business-case.md) — Why neighborhood stores accept Asgaya (triple-dip economics)
- [Cold-Start Strategy](../decisions/cold-start-strategy.md) — How we recruit freelancers + tech companies as senders
- [BCH Sellers](bch-sellers.md) — The seller role (Pro Sellers are just registered business versions)
- [Core Regulatory Constraints](core-regulatory-constraints.md) — Why Asgaya stays outside MiCA/PSD2
- [Fee-Splitting Model](../decisions/fee-splitting-model.md) — How 0.5% seller + 0.5% merchant fees work

---

*Last updated: 2026-05-20*  
*Status: Active — Phase 0 (Spain→Venezuela, Europe→Venezuela)*  
*Architecture: Identical to family remittances, just with Pro Seller for professional invoicing*

---

**Sources:**
- Jobbers Global Freelance Payment Methods Report 2026 (31% Venezuelan freelancers receive crypto)
- Posada 2025 study of Venezuelan data workers
- Reddit r/vzla freelancer payment threads
- Spanish autónomo registration requirements
- Cross-border freelance payment accounting standards
