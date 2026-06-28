# Progressive Payment Rollout: Safety Over Speed

**The Constraint:** Speed vs safety, breadth vs depth

**The Question:** How do we expand to new corridors (countries, payment methods) without breaking things?

---

## What Constrains Us

Three forces limit payment method adoption:

1. **Payment infrastructure varies wildly** - Each country/bank has different notification formats, screenshot policies, legal frameworks
2. **We can't test what we don't understand** - Supporting a payment method without deep knowledge risks production failures
3. **Users need confidence** - If Bizum works perfectly but PagoMóvil fails 30% of the time, trust collapses

**The trade-off:** Launch fast in many corridors vs launch safely in a few, then expand.

---

## The Decision: Progressive Rollout, Crowdsourced Documentation

**Phase 0: Start with the simplest**
- **Cash-in-person** (all corridors) - No digital parsing, no bot complexity, pure reputation testing
- **Bizum** (Spain only) - Well-documented, universal notification format, name matching verified
- **SEPA** (Spanish IBANs only) - Progressively research, add after validation

**Phase 0+: Add methods based on user demand**
- Users volunteer as "payment method pioneers"
- Two pioneers test notification parsing (send payments to each other)
- Bot learns pattern, code reviewed, method added as "experimental"
- After 50-100 organic transactions → graduates to "stable"
- Pioneer badge awarded (special reputation marker)

**Phase 1+: Geographic expansion**
- Venezuela: PagoMóvil documented by Venezuelan pioneers
- Kenya: M-Pesa documented by Kenyan pioneers
- Each payment method = separate audited module
- Community verifies implementation before trusting it

---

## Why Progressive (The Real Reason)

**We don't know what we don't know.**

### The Documentation Gap

**What varies by payment method:**

| Aspect | Bizum (Spain) | PagoMóvil (Venezuela) | M-Pesa (Kenya) | Unknown Method |
|--------|---------------|----------------------|----------------|----------------|
| **Notification type** | Push notification | SMS? Email? Push? | SMS | ??? |
| **Screenshot policy** | Blocked (use push parsing) | Allowed? | Allowed? | ??? |
| **Name included?** | Yes (enables name matching) | Unknown | Unknown | ??? |
| **Transaction ID** | Yes (verifiable with bank) | Unknown | Unknown | ??? |
| **Legal framework** | Document forgery = felony | Unknown | Unknown | ??? |

**Example: Bizum took research to document:**
- Banks send push notifications (not SMS)
- Screenshot blocked (must parse push notification content)
- Recipient name included (enables false-accusation prevention)
- Transaction ID verifiable with bank API (in some cases)
- Spanish law: "Falsedad documental" (6 months - 3 years prison)

**We can't assume PagoMóvil works the same.** Must research each payment method individually.

---

### The Alternative: Launch Everywhere, Break Things

**Fast expansion approach:**
```
Day 1: Support Bizum, SEPA, PagoMóvil, M-Pesa, Zelle, Interac, PIX
Assumption: "Notifications probably work similarly"

Week 2: Venezuelan user pays via PagoMóvil
         Bot fails to parse notification (different format)
         Covenant expires, user blacklists seller unfairly
         Seller's reputation tanks, quits Asgaya
         Trust collapses in Venezuela corridor

Week 4: Kenyan M-Pesa user discovers screenshot is allowed
         Photoshops fake receipt
         Blacklists innocent seller
         No legal consequences (Kenya has different forgery laws)
         Scammer operates freely

Result: 7 payment methods, 2 broken, trust damaged
```

**Progressive expansion approach:**
```
Day 1: Cash-in-person (all corridors) + Bizum (Spain, documented)

Week 4: Venezuelan pioneers volunteer for PagoMóvil
        Two users test notification parsing (10 payments)
        Bot learns: SMS format, includes sender name, transaction ID
        Code reviewed, PagoMóvil added as "experimental"

Week 8: 50 organic PagoMóvil transactions completed
        No bot failures, no false accusations
        PagoMóvil graduates to "stable"
        Venezuela corridor opens

Result: 2 payment methods, both work reliably, trust builds
```

**We choose the second.**

---

## The Crowdsourced Documentation Process

**How new payment methods are added:**

### Step 1: Volunteers Apply

User posts on Nostr/forum:
```
"I want to add M-Pesa to Asgaya. 
Looking for another Kenyan user to help test.
I have: Safaricom M-Pesa account
Willing to: Send 10 test payments, document notification format"
```

Two users volunteer → become "pioneers" for that payment method.

---

### Steps 2-4: Pioneer Testing → Experimental → Stable

Two pioneers test notification parsing by sending small payments to each other (e.g., KES 100 via M-Pesa). The bot learns the notification format (SMS structure, field extraction, timestamp format), and the parsed data is reviewed on GitHub before code merge. The payment method launches as "experimental" with pioneers getting early access. After 50-100 organic transactions with >99% success rate and <1% disputes, the method graduates to "stable" and becomes available to all users in that country.

**See [Payment Method Documentation Process](../../cold-start-strategy/payment-method-rollout.md) for the detailed pioneer guide.**

---

### Step 5: Pioneer Rewards

**Once method graduates to stable:**
- ✅ Pioneer badge on reputation (permanent marker)
- ✅ Higher trust score (helped expand Asgaya)
- ✅ Credit in release notes ("M-Pesa documented by Alice#123 and Bob#456")

**Incentive:** Users document the payment methods they need.

---

## The Trade-off

| Launch Fast | Launch Safe |
|-------------|-------------|
| **Gains:** Broader market day 1, more corridors, faster growth | **Gains:** Each payment method reliable, trust builds, no production failures |
| **Losses:** Unknown payment formats break in production, trust collapses if failures occur | **Losses:** Slower geographic expansion, requires pioneer volunteers, patience |

**Example:**

**Fast:** Support 20 payment methods day 1, 3 break, reputation system damaged in those corridors

**Safe:** Support 2 payment methods day 1 (cash + Bizum), add 1 new method per month, 100% reliability

---

## Why We Accept the Slow Expansion

We accept slow expansion because cash-in-person remittances with covenant trustlessness are already revolutionary—we don't need 50 payment methods on day 1. Trust is fragile: one bad experience ("bot failed, my money is stuck") spreads faster than ten good ones. Better to serve 1,000 Spanish users perfectly than 10,000 global users unreliably. Payment infrastructure research takes time—understanding Bizum took weeks (notification format, screenshot policies, legal framework, name matching). We can't rush that for 20 methods. And community-driven expansion scales better: Kenyan users document M-Pesa, Venezuelan users document PagoMóvil, Brazilian users document PIX. Devs don't have bank accounts in 50 countries—users do. Crowdsourced beats centralized.

---

## Phase 0 Validation: What We're Testing

| Area | Question | Hypothesis |
|------|----------|------------|
| **Pioneer interest** | How many users volunteer to document payment methods? | >10 volunteers in first 3 months |
| **Documentation quality** | Do pioneer-documented methods work reliably? | >99% success rate |
| **Expansion pace** | How many payment methods added per quarter? | 2-4 (sustainable) |
| **User patience** | % of users who report "too slow expansion" in feedback? | <5% (clear communication about safety-first approach) |
| **Cash-in-person adoption** | Is cash-only sufficient for initial growth? | Yes (many corridors prefer cash) |

**Success criteria:**
- >10 pioneer volunteers in Q1
- >99% success rate for pioneer-documented methods
- 2-4 new payment methods per quarter (sustainable pace)
- <5% users report "too slow expansion" in feedback
- Cash-in-person drives >50% of Phase 0 transaction volume

---

## The Limitations (And Why We Accept Them)

| Limitation | Impact | Mitigation | Why We Accept |
|------------|--------|------------|---------------|
| **Slow geographic expansion** | Can't serve all corridors day 1 | Start with highest-demand corridors (Spain→Venezuela), expand based on user requests | Trust and reliability > speed |
| **Pioneer dependence** | Need volunteers to document methods | Pioneer badges + early access incentivize participation | Crowdsourced scales better than dev-led |
| **Experimental flag friction** | Users may avoid "experimental" methods | Clear communication, risk warnings, gradual rollout | Safety requires caution |
| **Per-method research burden** | Each payment method needs individual documentation | Burden distributed across community (not devs alone) | Community-driven is sustainable |

---

## The Real Constraint

**Progressive payment rollout isn't slow. It's safe.**

We could launch with 50 payment methods on day 1 and hope they work. Or we can launch with 2, verify they work perfectly, then add more based on user demand and community documentation.

**The constraint we're optimizing:** Not "how do we support all payment methods instantly?" but "how do we expand reliably without breaking trust?"

**Cash-in-person + Bizum in Spain is revolutionary enough. Everything else is progressive enhancement.**

---

## Related Documents

- [Dispute Resolution via Nostr Blacklist](../../the-mechanism/nostr-coordination/dispute-resolution.md) (why payment method differences matter for evidence)
- [Payment Method Documentation Process](../../the-mechanism/payment-method-documentation/README.md) (step-by-step guide for pioneers)
- [Reputation On-Chain](./reputation-on-chain-not-central-database.md) (pioneer badges as reputation markers)

---

**Status:** Phase 0 Implementation  
**Last Updated:** 2026-06-22  
**Confidence:** High (crowdsourced documentation proven in open-source projects; progressive rollout reduces risk)
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Constraints](README.md)** | **[📖 Glossary](../../glossary.md)**
