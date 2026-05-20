# Freelance Payments: Same Protocol, Different Sender

**Type:** Concept Document  
**Status:** ✅ Active — Phase 0  
**Date:** 2026-05-20  
**Related:** [Merchant Business Case](merchant-business-case.md), [Cold-Start Strategy](../decisions/cold-start-strategy.md), [BCH Sellers](bch-sellers.md), [Own Funds Path](../android-app/flows/sender-flows/own-funds-path/README.md)

---

## TL;DR

The Asgaya covenant architecture that moves family remittances is structurally identical to a freelance payment. The only difference is the relationship between sender and recipient: a family member becomes an employer or client.

**Two payment flows:**

1. **Own Funds (Recommended for businesses):** Client buys BCH first, sends through Asgaya. **0.5% total fee.** Cleaner accounting (standard crypto asset purchase + payment).

2. **Via BCH Seller:** Client pays BCH seller via Bizum/SEPA, seller posts covenant. **1.0% total fee.** No crypto purchase required, but more complex accounting.

A Venezuelan freelancer who currently loses 5-15% to PayPal, Deel, Bitwage, and P2P spreads can receive nearly full value through Asgaya. This is not a new feature—it's already supported by the existing protocol.

---

## 1. The Problem: Remote Workers Are Stranded from Their Own Money

Venezuela has one of the highest freelance workforce participation rates in the world. Nearly **52% of the workforce** depends on freelance work. An estimated **60-65% of working professionals** hold at least a college degree, and tens of thousands of engineers, developers, and technical professionals graduate annually—a highly educated workforce driven to international freelance platforms by economic necessity.

All of these workers face the same brutal problem: **getting paid**.

### What Venezuelan freelancers say

> "¿De qué manera podría recibir pagos yo de manera 'legal'? ¿Qué plataforma recomiendan para recibir pagos? Yo tengo Facebank (puedo recibir wires y ACH), Paypal, Payoneer. Agradezco consejos."

> "Por qué coño tiene que ser un mardito peo recibir pagos en dólares. Mi jefe jamás ha tenido experiencia pagándole a alguien fuera de USA (mucho menos de Venezuela), y hemos estado tratando de averiguar cómo hacerlo de una manera eficaz, rápidamente, sin límites y que pueda recibir el dinero sin tener que sacrificar mucho o ningún %."

*(Sources: Reddit r/vzla, Venezuelan freelancer communities)*

A 2025 academic study of 38 Venezuelan data workers describes a "gauntlet of international and domestic financial intermediaries—work platforms, digital crypto banks, and local brokers—each charging fees" that erode earnings at every step. Workers "typically receive much less than they earn" after passing through multiple intermediary layers.

### Why existing solutions fail

| Solution | The Problem |
|----------|-------------|
| **PayPal** | 3-5% conversion fees, transfer limits, holds, unfavorable rates |
| **Wise** | Not available in Venezuela |
| **Payoneer** | Restrictions, high minimums, complex verification |
| **Western Union** | BCV-rate conversion above $100 destroys 25-50% of value |
| **Deel / Bitwage** | 5-10% platform fees + P2P off-ramp costs |
| **Binance P2P** | Client must buy crypto first; freelancer still needs off-ramp with 2-5% spread |

**Crypto is already the default fallback.** 31% of Venezuelan freelancers receive payment in cryptocurrency—the highest rate in the world. But the problem remains: the client still needs to get fiat into crypto, and the freelancer still needs to get crypto back into spendable cash.

---

## 2. Two Ways to Pay Through Asgaya

### Flow A: Own Funds (Recommended for Businesses) — 0.5% Total Fee

**Best for:** Recurring payments, tech-savvy businesses, crypto-aware employers

If your business is comfortable purchasing cryptocurrency (or already holds BCH), this is the cleanest accounting path:

#### How it works

1. **Client purchases BCH** on Kraken, Coinbase, Binance, or any exchange
   - Bank statement: "Purchased cryptocurrency €1,500"
   - Accounting entry: Asset acquisition (Dr. Crypto Asset €1,500 / Cr. Cash €1,500)

2. **Client opens Asgaya app**, enters recipient CashAccount (e.g., `Maria#2841`)
   - Specifies amount: €1,500
   - App shows covenant details and merchant fee (0.5%)

3. **Client sends BCH from their wallet** to the covenant address
   - On-chain transaction: Client wallet → Covenant
   - Asgaya receipt generated: "Paid Maria#2841 €1,500 for services"

4. **Freelancer receives notification**
   - Walks to local neighborhood store
   - Presents claim code, receives €1,492.50 cash (€1,500 - 0.5% merchant fee)
   - OR claims BCH directly to own wallet (if prefers to hold crypto)

5. **Merchant co-signs covenant** (if cash path)
   - Receives €1,500 worth of BCH
   - Merchant earns €7.50 (0.5% fee)

#### Accounting reconciliation

**For client (Spain):**
- **Purchase:** Bank statement shows "Kraken €1,500" → Crypto asset acquired
- **Payment:** On-chain transaction shows BCH sent to covenant address
- **Documentation:** Freelancer invoice (€1,500) + Asgaya transaction receipt + blockchain proof
- **Books:** 
  ```
  Dr. Freelance Expense €1,500
  Cr. Crypto Asset €1,500
  ```

**Why this is cleaner:** Standard crypto asset accounting. Spanish gestors understand "we bought crypto and paid our contractor." No mysterious intermediaries. Direct on-chain payment from client to covenant.

---

### Flow B: Via BCH Seller (Alternative) — 1.0% Total Fee

**Best for:** One-off payments, crypto-averse clients, clients without exchange accounts

If your business doesn't want to handle cryptocurrency directly, Asgaya can connect you with a BCH seller who posts the covenant on your behalf:

#### How it works

1. **Client opens Asgaya app**, enters recipient CashAccount
   - App matches client with available BCH seller
   - Shows payment instructions: "Pay €1,500 via Bizum to BCH Seller [phone/IBAN]"
   - Concept field: `Maria#2841` (links payment to recipient)

2. **Client pays BCH seller** via Bizum, SEPA, or bank transfer
   - Bank statement: "Bizum to [BCH Seller]"
   - BCH seller receives notification, buys BCH on exchange

3. **BCH seller posts covenant**
   - Locks €1,605 worth of BCH (€1,500 + 7% volatility buffer)
   - Covenant holds EUR-denominated cash buy order for freelancer

4. **Freelancer receives notification**, same cash-out flow as Flow A

5. **Total fees:**
   - BCH seller: 0.5% (€7.50 on €1,500)
   - Merchant: 0.5% (€7.50 on €1,500)
   - **Freelancer receives:** €1,485 cash

#### Accounting reconciliation

**For client (Spain):**
- **Payment:** Bank statement shows "Bizum/SEPA to BCH Seller €1,500"
- **Documentation:** Freelancer invoice (€1,500) + Asgaya transaction receipt (links invoice to covenant) + bank statement
- **Books:**
  ```
  Dr. Freelance Expense €1,500
  Cr. Cash €1,500
  ```

**Note:** This requires 3-document reconciliation (invoice + bank record + Asgaya receipt). Similar to PayPal or Wise: bank shows payment to processor, not to contractor. Most Spanish gestors are familiar with this pattern, but it's slightly more complex than Flow A.

---

## 3. Concrete Examples

### Example 1: María, Graphic Designer in Caracas (Flow A - Own Funds)

**Situation:** María works for a Madrid marketing agency. She invoices €1,500/month. Client is tech-savvy, already holds some BCH.

**Current method:** Client pays via Deel (8% platform fee + María loses 3% on P2P off-ramp = 11% total loss). María receives ~€1,335.

**With Asgaya (Own Funds):**
1. Client sends €1,500 worth of BCH from their wallet through Asgaya covenant
2. María walks to her local neighborhood store, receives €1,492.50 cash
3. **Total cost:** 0.5% (€7.50)
4. **María receives:** €1,492.50 (vs €1,335 before = **€157.50 more per month**)

**Annual savings for María:** €1,890

---

### Example 2: Carlos, Software Developer in Valencia, Venezuela (Flow B - BCH Seller)

**Situation:** Carlos works for a Barcelona startup. He invoices €2,000/month. Client is crypto-averse, doesn't want to buy BCH.

**Current method:** PayPal (4% fees + 2% conversion spread = 6% total loss). Carlos receives ~€1,880.

**With Asgaya (Via BCH Seller):**
1. Client pays €2,000 via SEPA to BCH seller
2. BCH seller posts covenant for Carlos
3. Carlos receives €1,980 cash at local merchant
4. **Total cost:** 1.0% (€20)
5. **Carlos receives:** €1,980 (vs €1,880 before = **€100 more per month**)

**Annual savings for Carlos:** €1,200

---

## 4. Flow Comparison

| | Own Funds (Flow A) | Via BCH Seller (Flow B) |
|---|---|---|
| **Best for** | Recurring payments, tech-savvy employers | One-off payments, crypto-averse clients |
| **Total fee** | 0.5% (merchant only) | 1.0% (0.5% seller + 0.5% merchant) |
| **On €1,500** | €7.50 | €15 |
| **Freelancer receives** | €1,492.50 | €1,485 |
| **Client requires** | Ability to buy BCH on exchange | Just bank account (Bizum/SEPA) |
| **Accounting complexity** | Low (standard crypto asset accounting) | Medium (3-document reconciliation) |
| **Spanish gestor compatibility** | High (they understand crypto asset purchase + disposal) | Medium (requires explanation of intermediary) |
| **Payment speed** | Depends on client's BCH purchase speed | 15 min (instant Bizum → seller buys BCH → posts covenant) |
| **Recommended for** | Monthly salary, tech companies, recurring contractors | One-off projects, traditional businesses |

---

## 5. Compliance Path

### For the Freelancer (Venezuela)

The freelancer issues a standard invoice to the client with:
- Full name and tax ID (RIF if registered)
- Client company name and tax ID
- Description of services rendered
- Date, invoice number
- Agreed amount in EUR

Since the freelancer is outside the EU, **no VAT applies** and **no IRPF withholding is required** from the Spanish client. The invoice is the primary accounting document—it proves the business relationship and the amount owed.

**Venezuelan tax obligations:** Freelancers should consult a Venezuelan tax advisor regarding income declaration requirements under SENIAT. Asgaya provides payment infrastructure, not tax advice.

### For the Employer/Client (Spain)

**Flow A (Own Funds):**
- Step 1: Crypto purchase (bank statement shows EUR → exchange)
- Step 2: Payment to freelancer (on-chain transaction + Asgaya receipt)
- Documentation: Invoice + exchange receipt + Asgaya transaction receipt + blockchain proof
- Treatment: Crypto asset acquisition → business expense + asset disposal

**Flow B (BCH Seller):**
- Bank statement shows Bizum/SEPA to BCH seller (€1,500)
- Asgaya transaction receipt links payment to freelancer invoice
- Documentation: Invoice + bank statement + Asgaya receipt
- Treatment: Business expense (similar to PayPal/Wise payment)

### Form 347 (Spain)

If the employer processes more than **€3,005.06** in payments through the same BCH seller in a calendar year, they must file Form 347 with the AEAT (Spanish tax authority). This is a routine annual informative declaration, not a tax. Applies to Flow B only (Flow A has no intermediary to report).

### Important Disclaimers

⚠️ **This is not legal or accounting advice.** Asgaya provides payment infrastructure, not tax or compliance guidance. Both freelancers and clients must ensure compliance with tax laws in their respective jurisdictions.

**We strongly recommend:**
- **Spanish clients:** Consult a gestor or tax advisor familiar with international freelance payments and crypto-adjacent transactions
- **Venezuelan freelancers:** Consult a Venezuelan tax advisor familiar with SENIAT requirements for foreign income
- **Both parties:** Verify that your specific business structure and payment arrangement complies with local regulations

**Validation needed:** The invoice + transaction receipt model described above mirrors how businesses handle PayPal or Wise payments. However, Spanish tax treatment of crypto-mediated payments may vary. Verify with your gestor that this documentation meets Spanish tax requirements for your specific business structure.

---

## 6. Why This Matters for Cold Start

The sender is the bottleneck in Phase 0. We need people in Europe who will pay euros through Asgaya. Family remitters are the primary source, but **Venezuelan freelancers can actively recruit their own clients as senders.**

### The freelancer's incentive is direct and personal

A freelancer who earns €1,500/month and currently loses €75-225 to payment platforms has a powerful financial reason to convince their client to switch. **On a €1,500 monthly invoice, switching from PayPal (6% loss) to Asgaya (0.5-1% loss) saves €75-82.50 every month.** That's €900-990 per year.

The freelancer can even offer the client a discount: "Pay me through Asgaya and I'll reduce my rate by 2%." The client saves money, the freelancer keeps more, and Asgaya gains a recurring sender.

### Target segments for Phase 0

**Segment 1: Venezuelan freelancers with Spanish/EU clients**
- Reddit r/vzla, Venezuelan dev Telegram groups
- Upwork/Fiverr Venezuelan seller communities
- Venezuelan university alumni groups in Spain
- Pitch: "Keep €900/year instead of giving it to PayPal"

**Segment 2: Spanish tech companies with Venezuelan contractors**
- Barcelona/Madrid startup communities
- Remote-first companies, digital agencies
- Already crypto-aware (tech industry)
- Hate paying 5-10% to Deel/Bitwage
- **One Spanish agency with 5 Venezuelan contractors = 5 recurring monthly senders**
- Pitch: "Pay your contractors 0.5% instead of 8%"

**Segment 3: European businesses hiring Venezuelan talent**
- Businesses in Italy, Portugal, Germany, France
- Looking for cost-effective remote workers
- Often use Deel/Bitwage and resent the fees
- Pitch: "Hire Venezuelan talent, pay them fairly, save on fees"

### Corridor scope (Phase 0)

**Currently supported:** Spain→Venezuela, Europe→Venezuela (via SEPA)

**Not yet supported:** US→Venezuela

**For freelancers with US clients:** Asgaya does not currently serve the US→Venezuela corridor due to US money transmitter licensing complexity. We are researching the regulatory path for Phase 1+.

**Workaround:** If your US client has EU presence (European subsidiary, EU freelance platform account, payment agent in Europe), they can pay you through the Europe→Venezuela corridor today. Direct US→Venezuela support requires regulatory work beyond Phase 0 scope.

---

## 7. FAQ for Freelancers

### Can I receive in BCH directly instead of cash?

**Yes.** When claiming the covenant, you can specify your own BCH wallet address instead of coordinating with a merchant. You receive the full BCH amount (no merchant co-signature needed). You can then:
- Hold BCH as savings
- Swap for MUSD stablecoin (BCH-native, maintains USD value)
- Sell for cash later when you need it
- Spend at BCH-accepting merchants

### What if my client pays in USD, not EUR?

They need to convert to EUR first (most European businesses can send EUR via SEPA), or wait for USD→VES corridor (Phase 1+). Current Phase 0 focus is EUR-denominated payments through the Europe→Venezuela corridor.

### Does this work for one-time project payments or just monthly salary?

**Both.** The protocol works identically for recurring monthly salary and one-off project payments. The client follows the same flow whether paying you once or every month.

### Do I need KYC?

**No KYC required on Asgaya.** You don't need to verify identity, provide documents, or complete know-your-customer checks. The protocol is permissionless.

**However:** Your client might need to comply with their bank's requirements for international payments, which is standard for any cross-border freelance arrangement (same as PayPal, Wise, etc.).

### What if my client wants a formal payment processor invoice?

You issue the invoice to your client as usual (your standard freelance invoice with service description, amount, date, etc.). The Asgaya transaction receipt serves as proof of payment delivery—similar to a PayPal receipt. Your client's accountant reconciles your invoice with their bank statement + Asgaya receipt.

### Can my client pay multiple freelancers with one BCH purchase?

**Yes (Flow A only).** If your client buys €10,000 worth of BCH, they can send:
- €2,000 to Freelancer A
- €3,000 to Freelancer B  
- €1,500 to Freelancer C
- Keep €3,500 for future payments

This amortizes the exchange fees across multiple payments and simplifies their accounting (one crypto purchase, multiple contractor payments).

### What if there's no merchant near me?

**Option 1:** Claim BCH directly to your wallet (no merchant needed)

**Option 2:** Phase 0 includes "PagoMóvil merchants"—trusted users who send you a PagoMóvil bank transfer instead of cash. The fee is slightly higher (0.8% instead of 0.5%) to cover PagoMóvil costs, but works anywhere in Venezuela with mobile banking.

**Option 3:** Onboard a local merchant yourself. If you receive regular payments (€1,500/month), you can convince your local neighborhood store to join Asgaya. They earn 0.5% fee per transaction + potential product sales if you buy groceries. See [Merchant Business Case](merchant-business-case.md) for the pitch.

### What documentation do I need for taxes?

Consult a Venezuelan tax advisor familiar with SENIAT requirements for foreign income. At minimum, maintain:
- Invoices issued to clients (with date, amount, description)
- Asgaya transaction receipts (proof of payment received)
- Records of how much you withdrew as cash vs held as BCH

Asgaya provides payment infrastructure, not tax advice. Your tax obligations depend on your specific situation.

---

## 8. Related Documents

- [Merchant Business Case](merchant-business-case.md) — Why neighborhood stores self-onboard (freelancers = additional foot traffic)
- [Cold-Start Strategy](../decisions/cold-start-strategy.md) — How we recruit senders (freelancers recruit their own clients)
- [BCH Sellers](bch-sellers.md) — The seller role that enables Flow B (fiat → BCH on-ramp)
- [Own Funds Path](../android-app/flows/sender-flows/own-funds-path/README.md) — Technical flow for Flow A (sender already has BCH)
- [Buy from Seller Path](../android-app/flows/sender-flows/buy-seller-path/README.md) — Technical flow for Flow B (sender uses BCH seller)
- [Core Regulatory Constraints](core-regulatory-constraints.md) — Why Asgaya stays outside MiCA/PSD2 (no custody, no intermediation)

---

## 9. The Bottom Line

**For freelancers:** You're already losing 5-15% to payment platforms. Asgaya lets you keep 99-99.5% of your earnings. If you earn €1,500/month, that's €900-1,350 saved per year. That's real money.

**For clients:** You're paying 5-10% to Deel/Bitwage/PayPal for the "privilege" of paying your own contractors. Asgaya costs 0.5-1% with cleaner accounting. If you pay 5 contractors €1,500/month each, you save €3,375-6,750 per year.

**For Asgaya Phase 0:** Every freelancer who converts their client adds one recurring sender to the network. Freelancers have direct financial incentive to recruit. This accelerates cold start without requiring us to convince strangers—freelancers convince their own clients because they keep more money.

The architecture doesn't change. The covenant mechanism is identical to family remittances. This is documentation of an already-supported use case that happens to solve the sender bottleneck.

---

*Last updated: 2026-05-20*  
*Status: Active — Phase 0 (Spain→Venezuela, Europe→Venezuela)*  
*Next: Document tech company recruitment strategy in cold-start-strategy.md*

---

**Sources:**
- Jobbers Global Freelance Payment Methods Report 2026 (31% Venezuelan freelancers receive crypto payments)
- Posada 2025 study of Venezuelan data workers ("gauntlet of intermediaries")
- Reddit r/vzla freelancer payment threads
- RevoluGROUP Venezuela remittance data
- Banca y Negocios (remittance corridor analysis)
- Spanish tax requirements for cross-border freelance payments
- Venezuelan SENIAT income declaration requirements
