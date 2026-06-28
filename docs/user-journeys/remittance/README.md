# Remittance Journeys
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**Use case:** Sending money across borders (Spain → Venezuela, etc.)

---

## Two Sides of the Same Transaction

Remittances involve two parties in different locations:

### [Sender](sender/README.md)
**Role:** Active BCH Buyer  
**Example:** María in Madrid sends €100 to Elena in Caracas  
**What they do:** Buy BCH via Asgaya, create covenant, send to recipient

### [Recipient](recipient/README.md)
**Role:** Active BCH Seller  
**Example:** Elena in Caracas receives €100 from María  
**What they do:** Claim BCH from covenant, cash out at local merchant

---

## Why These Are Grouped

**Sender and recipient are two perspectives on the same flow:**

1. Sender creates covenant (locks BCH for recipient)
2. Recipient claims covenant (gets BCH or cashes out)

**Geographically separated** - sender and recipient are in different countries (remittance corridor).

**Contrast with customer/merchant:**
- Customer and merchant are often in same location (commerce)
- Customer buys and spends BCH immediately (no covenant delay)

---

## Phase 0 Focus

**Remittances are the Phase 0 priority** because:
- High fees in traditional market (5-8%)
- Clear value proposition (1% vs 5-8%)
- Natural use case for volatility protection (H€/HAu tokens)
- Builds merchant network (recipients cash out → merchants accumulate BCH)

**Customer/commerce flows are Phase 1+** (natural extension once infrastructure exists).

---

**Explore:** [Sender Journey](sender/README.md) | [Recipient Journey](recipient/README.md)  
**Back:** [User Journeys](../README.md)

---

## Navigation

**[🏠 Home](../../index.md)** | **[📖 Glossary](../../glossary.md)**

**In this section:**
- [Sender](sender/README.md) - Active BCH buyer
- [Recipient](recipient/README.md) - BCH seller

**Related sections:** [Merchant Journey](../merchant/README.md) · [Trader Journey](../trader/README.md)
