# Passive Mode: Bot Automation (Not Manual Trading)

**The Constraint:** Control vs scaling

**The Question:** How do sellers provide liquidity without spending all day watching for transactions?

---

## What Constrains Us

Three forces limit marketplace liquidity:

1. **Human time** - Manual trading requires constant attention (doesn't scale)
2. **Response speed** - Users expect instant availability (24/7, not business hours)
3. **Capital efficiency** - Sellers need to process many transactions to earn meaningful income

**The trade-off:** Full control vs passive income. We need liquidity without human babysitting.

---

## The Decision: Passive Mode (Bot Handles Everything)

**What it is:**

Two modes for marketplace participants:

| Mode | User Action | Bot Role | Use Case |
|------|-------------|----------|----------|
| **Passive** | Post listing once | Handles all transactions 24/7 | Sellers/buyers providing liquidity |
| **Active** | Query when needed | None (direct interaction) | Senders/recipients using the system |

**How passive mode works:**
1. Seller posts listing: "Sell BCH for EUR via Bizum, 0.5% fee"
2. Bot monitors bulletin board for matching requests
3. Sender selects seller, pays via Bizum
4. Bot detects payment, locks BCH into covenant automatically
5. Seller earns fee without manual intervention

**How active mode works:**
1. Sender opens app: "Send €100 to Elena#142"
2. App queries bulletin board for available sellers
3. Sender picks best option, pays via Bizum
4. Done (no listing needed)

---

## The Trade-off

| Trade-off | Passive Mode | Active Mode |
|-----------|-------------|-------------|
| **Availability** | 24/7 (bot never sleeps) | When you're online |
| **Scaling** | Infinite (bot handles all) | Linear (your time = your limit) |
| **Control** | Bot decides, you audit | You decide every transaction |
| **Setup** | Technical, one-time | None (just use the app) |
| **Income** | Earn while you sleep | Earn while you work |
| **Best for** | Liquidity providers, miners, arbitrageurs | Occasional users, senders, recipients |

---

## Why Passive Mode (The Real Reason)

**Linear time investment doesn't scale.**

### The Math Problem

**Without bot automation:**
- Seller processes 1 transaction = 5 minutes attention
- €100 transaction = €0.50 fee (0.5%)
- 20 transactions/day = 100 minutes work = €10 earned
- **€10/day for 100 minutes = €6/hour**

Not worth it. Seller quits.

**With bot automation:**
- Initial setup = few hours (dedicated bank account, register as business, configure bot)
- Ongoing maintenance = ~1 hour/week (monitor, handle edge cases)
- Bot processes 20 transactions/day = €10 earned
- **€10/day for ~1 hour/week setup = ~€50/hour effective rate**

Worth it. Seller scales.

### Why Senders Pay 0.5%

Senders send money when they need to—Saturday night, Tuesday morning, 3am. Manual traders are online 8 hours/day and miss 66% of demand. Passive bots are online 24/7 and capture 100%.

When a sender sees 5 passive sellers with sub-minute response times, the system feels professional. When they see 20 manual sellers with 30-minute delays, it feels dead. Most P2P markets (LocalBitcoins, Bisq) are slow and inconsistent: post an offer, wait, maybe someone responds hours later.

Asgaya's Nostr layer provides real-time presence. Active users see "5 passive sellers online NOW." The 0.5% fee buys reliability and speed, not just BCH conversion.

**Even passive buyers with manual steps benefit:** Smartphone + CEX account + automation (except payment). Some business accounts can automate payments too (Bank Sabadell offers API for professional customers; Venezuelan banks likely have similar).

---

## The Same Person, Two Modes

**The key insight:** One person can be passive at some times, active at others.

### Example: Carlos the Merchant (Venezuela)

**Passive (most of the month):** Carlos posts as a BCH Buyer ("cash at store, 0.5% spread"). Recipients visit, claim remittances. Carlos accumulates BCH passively, converts to H€ tokens to avoid volatility. No effort, just foot traffic.

**Active (end of month, rent due):** Carlos opens the app, queries the bulletin board for the best VES buyer, and taps "Sell." His bot handles covenant creation and monitors his bank for the incoming payment. Rent paid. Couple of taps.

**Result:** Passive accumulation (scales) + active liquidation (control when needed)

---

## Phase 0 Validation: What We're Testing

| Area | Question | Hypothesis |
|------|----------|------------|
| **Adoption** | % of sellers who enable passive mode? | >70% |
| **Reliability** | Bot uptime? | >99% |
| | Median response time (payment → covenant posted)? | <2 minutes |
| **Economics** | Minimum daily transactions for passive to be worth it? | >10 |
| **UX** | Do senders prefer passive sellers (instant) vs active (wait)? | Yes, dramatically |
| | Do sellers report setup difficulty? | Some, but acceptable |

**Success criteria:**
- >70% sellers enable passive mode
- >99% bot uptime
- <2 minute median response time
- Senders rate "instant availability" as top benefit

---

## The Limitations (And Why We Accept Them)

| Limitation | Impact | Mitigation | Why We Accept |
|------------|--------|------------|---------------|
| **Bot complexity** | Requires technical setup (API keys, monitoring) | Phase 0: Suso runs bots for sellers; Phase 1: Turnkey bot packages | Complexity frontloaded (setup once), infinite passive benefit |
| **Trust in automation** | Seller doesn't review each transaction manually | Bot logs all activity; seller can audit; covenant protects capital | Trustlessness via covenants (bot can't steal BCH) |
| **No human judgment** | Bot can't handle edge cases (disputed payments, unclear concept field) | Alerts seller for manual review when uncertain; seller can disable bot anytime | 99% of transactions are straightforward; edge cases rare |
| **Dependence on infrastructure** | Nostr relay, blockchain node, payment detection APIs | Redundant infrastructure; bot retries failed operations; seller gets alerts | Standard operational risk (same as running any service) |

**Note on client automation:** Critical automation functions (notification listener, payment detection, covenant creation) are built into the official client as open-source, auditable code. This ensures equal access—no sophisticated bot operators dominating manual users. Users can propose modifications via GitHub. Sellers can fork the client if they disagree with governance, but running modified code for critical functions means bearing full liability if their modifications cause failures.

---

## The Real Constraint

**Passive mode isn't perfect. It's scalable.**

**The constraint we're optimizing:** Not "how do we give sellers perfect control?" but "how do we enable 24/7 liquidity without burning out sellers?"

**Passive mode is the answer. Not manual. Scalable.**

---

## Related Documents

- [Buyers and Sellers: Two Roles, Two Modes](../../the-mechanism/bulletin-board/README.md) (active vs passive framework)
- [Notification Bot Architecture](../../the-mechanism/notification-bot/README.md) (how bots detect payments and post covenants)
- Radio Asgaya Episode 6: BCH Sellers (passive income for liquidity providers)
- Radio Asgaya Episode 16: BCH Buyers (capital recycling via passive listings)

---

**Status:** Phase 0 Implementation  
**Last Updated:** 2026-06-22  
**Confidence:** High (proven pattern in P2P marketplaces, automation is table stakes for liquidity)
