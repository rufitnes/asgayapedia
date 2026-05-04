# Why: No KYC

**Sub-requirement of:** [Why: Permissionless](core-architecture/why-permissionless.md)

**Core Requirement:** No participant needs to provide government ID or personal information to use Asgaya.

---

## The Problem

### KYC Excludes the People Who Need Remittances Most

Traditional remittance services require Know Your Customer (KYC) verification: government-issued ID, proof of address, sometimes credit checks and bank statements.

**Who this excludes:**

**Refugees and asylum seekers:**
- Fled their country, no valid government ID
- Can't prove address (living in temporary housing)
- Need to send money to family back home
- **Locked out of Western Union, MoneyGram, banks**

**Undocumented migrants:**
- Working to send money to family
- Fear deportation if they provide ID to authorities
- Use informal remittance networks (high fees, unreliable)
- **Choose between financial access and safety**

**The unbanked (1.7 billion people globally):**
- Can't get bank account without ID
- Can't get ID without address
- Can't get address without income
- Can't prove income without bank account
- **Trapped in catch-22**

**Politically exposed people:**
- Dissidents in authoritarian countries
- Activists under surveillance
- Journalists protecting sources
- **Need to move money without government tracking**

**Privacy-conscious users:**
- Don't want financial surveillance
- Don't want data sold to advertisers
- Don't want transactions tracked
- **Financial privacy shouldn't require being rich**

### The Irony of Financial Exclusion

The people who most need affordable remittances—those sending €100-500 to support family—are the ones most likely to be excluded by KYC requirements.

**Meanwhile:**
- The wealthy have private bankers who don't ask questions
- Corporations use complex structures to move billions
- Politicians use trusts and offshore accounts
- **The barriers apply selectively to the poor**

---

## Why No KYC Is Non-Negotiable

### 1. Our Users Don't Have What KYC Requires

**The target user:**
- Migrant worker in Spain
- Earning €1,200/month
- Sending €500/month to family
- May or may not have residence permit
- May or may not have bank account
- **Definitely doesn't have spare €50 to pay for notarized ID copies**

**The recipient:**
- Living in emerging market
- No bank account
- No government ID (or expired)
- No proof of address (informal housing)
- **Needs the money to survive**

If Asgaya requires KYC, we're not serving our users. We're serving the people who already have financial access.

### 2. KYC Is Surveillance

Every KYC verification creates a paper trail:
- Who sent money
- Who received it
- How much
- When
- **All recorded in government databases**

**Why this matters:**

**In authoritarian countries:**
- Dissidents' families receive money → Government flags them
- Activists send money abroad → Arrested for "financing terrorism"
- Journalists receive foreign payments → Accused of being "foreign agents"

**Even in democratic countries:**
- Financial surveillance enables abuse
- Data breaches expose sensitive information
- Governments change (today's privacy is tomorrow's persecution)

**No KYC isn't just convenient—it's necessary for safety.**

### 3. KYC Enables Gatekeeping

With KYC, someone decides who gets access:
- Bank says "we don't serve your country"
- Exchange says "your risk profile is too high"
- Regulator says "this corridor is prohibited"
- **Arbitrary decisions exclude millions**

**Without KYC:**
- No one to ask permission from
- No one to deny access
- No arbitrary exclusions
- **If you can fund the escrow, you can participate**

### 4. Financial Access Is a Human Right, Not a Privilege

**The current system treats financial access as a privilege:**
- Prove you're trustworthy (ID, credit check)
- Prove you're legitimate (address, bank statements)
- Ask permission from institutions
- **Only then can you send money to your family**

**Asgaya's position:**
Financial access is a human right. If you earned money, you should be able to send it to your family without asking permission from institutions.

---

## The Leverage Model

### 10 Escrows with KYC → 10,000 Recipients Without KYC

**The reality:** Someone needs Kraken account to convert fiat to BCH. That requires KYC.

**The solution:** A small number of escrow operators bear the KYC burden on behalf of thousands of users.

**How it works:**
- 10-50 escrow operators globally
- Each has Kraken account (KYC required)
- Each serves hundreds of recipients (no KYC)
- **Recipients get permissionless access**

**The key insight:** KYC burden doesn't scale linearly with users.
- 1 escrow with KYC → 1,000 recipients without KYC
- 10 escrows with KYC → 10,000 recipients without KYC
- **Leverage ratio: 1:1000**

**Escrows volunteer for this:**
- They already have bank access
- They're ideologically motivated (believe in financial freedom)
- They earn fees for providing this service
- **Their KYC enables others' freedom**

---

## Who Needs KYC, Who Doesn't

**Recipients (our primary users): NO KYC**
- Download Asgaya app
- Generate BCH wallet (local, no registration)
- Receive BCH
- Cash out at merchant or spend
- **Zero barriers**

**Merchants: NO KYC**
- Download app
- Accept BCH payments
- Earn fees from remittances
- **Zero barriers**

**Liquidity Providers: NO KYC**
- Use existing bank account
- Provide fiat to merchants
- Receive BCH
- **No additional verification**

**Senders: Already have KYC (with their bank)**
- Need Bizum or bank account to send EUR
- Already passed KYC for that account
- No additional KYC for Asgaya
- **Existing access, no new barriers**

**Escrows: YES, have KYC (with Kraken)**
- Small number (10-50 globally)
- Volunteer for this role
- Enable thousands of KYC-free users
- **Leverage for freedom**

**The pattern:** KYC burden is concentrated in roles that volunteer for it, while end users (recipients) have zero barriers.

### No-KYC Edge Cases (Future Expansion)

**Even senders can avoid KYC in some cases:**

**Cash ATM deposits (Spain and other countries):**
- Go to ATM
- Deposit cash into escrow's bank account
- No ID required (free service in many countries)
- Escrow receives funds, processes remittance
- **Fully no-KYC flow for sender**

**Reverse merchant flow:**
- Merchant holds BCH from previous remittances
- Sender gives cash to merchant in person
- Merchant sends BCH to escrow later
- **All participants no-KYC**

**Miners as escrows:**
- BCH miners already have Kraken accounts (for mining revenue)
- KYC already completed for mining operations
- Can serve as escrows without additional KYC burden
- See: [BCH Miners as Escrows](concepts/bch-miners-as-escrows.md)

**The vision:** As the network grows, more no-KYC paths emerge. The system is designed to support them.

---

## What We're NOT Saying

**We're NOT saying:**
- "KYC regulations are wrong" (that's a political debate)
- "Money laundering isn't a problem" (it is)
- "Financial crime should go unpunished" (it shouldn't)

**We're saying:**
- The current system uses KYC as a blunt instrument that excludes millions of innocent people
- Those excluded are predominantly poor, foreign, or politically vulnerable
- There's a better balance between security and access
- **Asgaya chooses access**

---

## Why This Matters for Adoption

**If Asgaya requires KYC:**
- We compete with Western Union (regulated, trusted, established)
- Western Union wins (they have brand recognition, physical offices, regulatory approvals)
- **We've built nothing new**

**If Asgaya has no KYC:**
- We serve people Western Union refuses
- We offer access to the unbanked
- We enable financial privacy
- **We've created something the world needs**

**The unbanked don't need "better Western Union." They need access.**

---

## Related Requirements

- [Why: Permissionless](core-architecture/why-permissionless.md) — No KYC is one aspect of truly permissionless access
- [Why: Cheaper Than Legacy](core-architecture/why-cheaper-than-legacy.md) — No KYC reduces compliance costs

---

## Trade-offs and Decisions

See the **Decisions** section for how we achieve no KYC while managing risks:

- Decision: Regulatory approach (coming soon)
- Decision: Escrow operator requirements (coming soon)

---

## The Bottom Line

**KYC is designed to exclude.**

It excludes the poor (who can't get IDs).
It excludes migrants (who fear deportation).
It excludes dissidents (who need privacy).
It excludes refugees (who fled their countries).

**Asgaya is designed to include.**

If you have a phone, you can receive money. That's it.

**Not your papers, not your permission, not your problem.**

---

*Last updated: May 1, 2026*
*Core principle: "Financial access is a human right, not a privilege. KYC excludes those who need access most."*
