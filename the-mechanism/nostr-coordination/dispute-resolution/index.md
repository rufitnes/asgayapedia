# Dispute Resolution via Nostr Blacklist

**Encrypted. Decentralized. Evidence-based.** Accountability without arbitrators.

---

## The Problem

**What happens when a passive user doesn't complete a transaction?**

```
Scenario:
1. María (active buyer) creates covenant (empty, no BCH yet)
2. María pays seller via cash-in-person / Bizum / SEPA (fiat leaves her account)
3. Seller's phone dies / bot crashes / seller ghosts
4. Seller never locks BCH into covenant
5. Covenant expires as an empty shell (no BCH to refund)
6. María lost her money, must pursue refund manually
```

**This is the payment-first risk model.** Active users pay BEFORE BCH is locked. If the seller never locks BCH, the covenant can't refund anything—it was never funded.

**Traditional solution:** Central arbitrator reviews evidence, makes judgment, enforces outcome.

**Asgaya's constraint:** No central arbitrator. No company to enforce rules. Permissionless.

**How do users hold each other accountable without intermediaries?**

---

## The Payment Infrastructure Constraint

**Critical insight:** Not all payment systems work the same.

**What varies by payment method:**
- Evidence format (receipt photo, bank notification, witness testimony)
- Notification delivery (SMS, push notification, email, none)
- Screenshot policies (allowed, blocked, N/A)
- Verification mechanism (transaction ID, bank statement, physical receipt)
- Legal framework (document forgery penalties vary by country)

**Asgaya's approach:** Progressive payment rollout

- **Phase 0:** Cash-in-person (all corridors) + Bizum (Spain, documented)
- **Phase 0+:** SEPA (first pioneer test), then additional methods based on user demand
- **Incentive:** "Pioneer badge" for users who document new payment methods
- **Each method documented individually** (e.g., SEPA via pioneer testing, then Venezuela: PagoMóvil, then bank apps one by one)

**This document describes the framework.** Specific evidence formats are documented per payment method.

**Why cash-in-person first:** Simplest evidence ("Did you receive cash?"), no digital infrastructure dependencies, tests reputation system without parsing complexity.

---

## The Solution: Permissionless Blacklist

**Active users post blacklist events to Nostr with evidence.**

When a passive user fails to complete a transaction, the active user:

1. **Posts blacklist event to Nostr** (kind 30078 with privacy-preserving hashes)
2. **Includes evidence** (bank SMS screenshot showing payment sent)
3. **Blacklisted user's reputation drops** (visible to all users querying bulletin board)
4. **Blacklisted user can't transact** until they manually resolve the dispute

**No judge. No arbitrator. Just evidence and reputation.**

**Primary evidence source:** The notification listener bot (automated, trustless)

**For digital payments (Bizum, SEPA):**
- Passive seller's bot parses incoming payment notification
- If payment received to correct account → bot locks BCH automatically
- If bot never sees payment → strong evidence of non-payment

**For cash-in-person:**
- Active buyer hands cash to passive seller
- Passive seller confirms receipt in app
- If seller doesn't confirm → dispute

**Edge case: Active user pays wrong recipient**

```
Problem:
1. María wants to pay seller at +34-612-345-678
2. María types +34-612-345-679 (typo in Bizum app)
3. Payment goes to wrong person
4. Seller never receives payment
5. María blacklists seller (has bank notification showing "payment sent")
6. Seller's reputation unfairly damaged
```

**Solution: Recipient name matching (Bizum Phase 0)**

Bizum notifications include recipient name. María's app checks:

```python
if payment_notification.recipient_name == seller.expected_name:
    # Safe to blacklist (payment went to correct person)
    allow_blacklist()
else:
    # Name mismatch detected
    warn_user("You paid someone else. Seller not at fault.")
    block_blacklist()
```

**This prevents false accusations automatically.**

**For payment methods without name verification:** Blacklist expires after 30 days if passive user provides counter-evidence (bank statement showing no payment received).

---

## Why Blacklist Matters: The Payment-First Risk Model

**Asgaya's architecture puts the risk on the active user (the one initiating the transaction).**

### The Timeline

```
1. Active user creates covenant (empty shell, no BCH yet)
2. Active user pays passive user via fiat (€100 leaves active user's bank)
3. Passive user's bot detects payment
4. Passive user's bot locks BCH into covenant ← IF THIS NEVER HAPPENS, ACTIVE USER LOSES €100
5. Recipient claims BCH from covenant
```

### Two Failure Modes

**Covenant never funded (seller ghosts):**
```
Steps 1-2: María pays €100
Step 3: Seller receives payment
Step 4 NEVER HAPPENS: Seller ghosts, never locks BCH

Result:
✗ Covenant is an empty shell (no BCH was ever locked)
✗ Seller has María's €100 in their bank account  
✗ Covenant expires with NOTHING TO REFUND
✗ María must pursue refund manually (blacklist or legal action)
```

**Covenant funded but expired/aborted:**
```
Steps 1-4: María pays, seller locks BCH
Step 5 doesn't happen: Recipient never claims OR BCH drops >7% (covenant aborts)

Result:
✓ BCH returns to María automatically (covenant refund works)
✗ Seller keeps the €100 fiat
⚠️ María gets BCH back (but may have net loss if price dropped)
```

### Why This Design?

**This is intentional, not a flaw:**

1. **Prevents money transmission classification** - Asgaya never holds funds, never intermediates
2. **No custody** - BCH is locked by seller after they receive payment, not before
3. **No company liability** - Seller is responsible for locking BCH, not Asgaya protocol

**Trade-off:** Active users take payment-first risk in exchange for permissionless, non-custodial architecture.

### Why Blacklist Is Critical

**For unfunded covenants, blacklist is the ONLY recourse:**

- Covenant can't refund what was never locked
- Legal action is slow and expensive (small claims court for €100)
- Blacklist provides fast social enforcement (seller's reputation tanks immediately)
- Market pressure: Blacklisted sellers can't get new customers

**This is why reputation and dispute resolution must work reliably.** The active user has no other protection.

### Phase 0 Mitigation

**How we reduce payment-first risk initially:**

- ✅ **Trusted sellers only** - Phase 0 uses pre-vetted sellers (Suso, early adopters)
- ✅ **Cash-in-person focus** - Simplest case, immediate evidence
- ✅ **Small amounts** - Test with €20-50 transactions, not €500
- ✅ **Strong reputation system** - On-chain stats + Nostr dispute history visible

**Phase 1+ relies on:** Mature reputation system, large transaction history, market filtering of bad actors.

---

## How It Works

### Step 1: Transaction Fails

**Scenario A: Malicious passive seller (cash-in-person)**
```
María hands €100 cash to seller at café
Seller counts money, takes it
Seller ghosts (doesn't lock BCH in covenant)
Covenant expires after 24 hours as empty shell
María has no automatic refund (covenant never had BCH)
Seller has her €100, María must pursue manual refund
```

**Scenario B: Technical failure (digital payment)**
```
María pays €100 via Bizum (fiat leaves her bank)
Seller's phone dies mid-transaction
Notification listener bot never sees payment
Bot never locks BCH (covenant remains empty)
Covenant expires with nothing to refund
María must contact seller for manual refund
```

**Scenario C: Malicious passive seller (digital payment)**
```
María pays €100 via Bizum (fiat in seller's bank)
Seller receives payment (bot sees notification)
Seller deliberately doesn't lock BCH (keeps covenant empty)
Covenant expires as empty shell
Seller has María's €100, no BCH was ever locked
María must pursue refund via blacklist pressure or legal action
```

**All scenarios:** Payment delivered to seller, covenant never funded, no automatic refund possible.

---

### Step 2: Active User Posts Blacklist Event

María's app prompts:

```
Transaction not completed.
Want to blacklist this seller?

Evidence required (select one):
• Bot-parsed payment notification (automatic)
• Cash-in-person witness statement
• Manual evidence upload

[Continue] [Cancel]
```

**Evidence by payment method:**

**Cash-in-person:**
- María attests: "I handed €100 cash to seller at [location] on [date]"
- Optional: Photo of seller, location timestamp, witness contact
- Seller can counter with: "Never met this person" or "Amount was €80, not €100"

**Bizum (Spain):**
- María's notification listener bot already parsed the outgoing payment
- Bot provides: recipient phone, recipient name, amount, timestamp
- App verifies: `payment.recipient_name == seller.expected_name`
- If match → automatic blacklist with bot evidence
- If mismatch → blocks blacklist, warns María

**SEPA (Spanish IBAN):**
- María's app accesses bank push notification content (no screenshot needed)
- Parses: recipient IBAN, recipient name, amount, transaction ID
- Some Spanish banks block screenshots; push notification parsing is more reliable

**For payment methods without programmatic access:**
- User can manually upload evidence (photo of receipt, email confirmation)
- Higher burden of proof (requires validator review)

**María's app then:**
1. Hashes the payment details (recipient, amount, timestamp)
2. Creates Nostr blacklist event (kind 30078)
3. Includes evidence hash and encrypted full evidence
4. Posts to multiple Nostr relays

---

### Step 3: Blacklist Event Structure

```json
{
  "kind": 30078,
  "tags": [
    ["d", "blacklist_entry"],
    ["target_cash_account", "Elena#142"],
    ["evidence_hash", "sha256:a1b2c3..."],
    ["evidence_type", "bank_notification"],
    ["amount", "100.00"],
    ["currency", "EUR"],
    ["payment_method", "bizum"],
    ["timestamp", "1719067935"]
  ],
  "content": "encrypted_evidence_blob",  // Full SMS screenshot, encrypted
  "pubkey": "npub1maria...",
  "created_at": 1719067940,
  "sig": "signature..."
}
```

**Public data (visible to all):**
- Target Cash Account (Elena#142)
- Evidence hash (proves evidence exists without revealing details)
- Basic transaction info (amount, currency, payment method)

**Private data (encrypted, only accessible to validators):**
- Full bank SMS screenshot
- Sender's bank account details
- Exact payment reference

---

### Step 4: Bot-Validated Consensus

**Blacklist events are not immediately trusted.** Validators check evidence:

**Phase 0 validators:** Trusted community members (Suso, early adopters)

**Phase 1+ validators:** Any user with high reputation can validate

**Validation process:**
1. Validator decrypts evidence
2. Checks bank notification authenticity
3. Verifies payment matches claimed amount/timestamp
4. Votes: "Legitimate blacklist" or "False accusation"

**Consensus threshold:** 3+ validators agree → blacklist confirmed

**Result:**
- ✅ Confirmed blacklist → Elena#142 flagged in all user apps
- ❌ Rejected blacklist → Event ignored, María's reputation drops (false accusation penalty)

**Beyond Phase 0: Counter-claims replace validators**

Goal: Disputes resolve between users. Legal system is the ultimate arbiter.

**When blacklist is posted, passive user can:**
1. **Refund immediately** → blacklist auto-removed (redemption)
2. **Ignore** → reputation drops, market filters them out
3. **Counter-claim** → post evidence of innocence (bank statement, transaction history)

**If both parties blacklist each other:**
- Stalemate (both have disputed blacklists)
- Both reputation scores tank (<90% completion rate = untrusted)
- Market avoids both users
- Legal system resolves if stakes warrant it

**False accusers get blacklisted too:**
- Passive sellers configure: "Don't accept covenants from users with >1 active blacklist"
- Market filters bad actors on both sides

---

### Step 5: Reputation Impact

Once blacklist is validated, Elena#142's on-chain reputation reflects it:

```javascript
// Elena's reputation BEFORE blacklist
{
  cash_account: "Elena#142",
  total_transactions: 50,
  completed_transactions: 49,
  completion_rate: 98%  // 49/50
}

// Elena's reputation AFTER blacklist
{
  cash_account: "Elena#142",
  total_transactions: 51,
  completed_transactions: 49,
  completion_rate: 96%  // 49/51 (blacklisted transaction counts as failed)
}
```

**Additionally, Nostr dispute history shows:**
- Date of blacklist event
- Evidence hash (proof exists)
- Validator consensus (3+ agreed)

Users querying Elena's profile see: ⚠️ **Unresolved dispute (June 22, 2026)**

---

### Step 6: Redemption (Manual Refund)

**Elena can restore her reputation by refunding María:**

1. Elena manually sends €100 to María via Bizum
2. María receives refund, confirms in app
3. María's app posts "dispute resolved" event to Nostr
4. Elena's blacklist flag is removed
5. Completion rate recalculated (disputed transaction no longer counts)

**Redemption is automated:** María's notification listener bot detects the refund payment, automatically posts resolution event. Elena doesn't need to ask María to forgive her—evidence of refund is enough.

---

## Why Bank Notifications Work as Evidence

**Bank notifications are notarized proof** (push notifications, SMS, or email depending on bank/country).

**In Spain (2026):** Most banks send push notifications, not SMS. This is actually better—push notifications can be accessed programmatically by the app without requiring screenshots.

### What Banks Provide

Spanish banks (and most KYC'd banks) include in payment notifications:

- **Sender's account** (or sender's phone for Bizum)
- **Recipient's account** (or recipient's phone)
- **Amount** (exact euros and cents)
- **Timestamp** (date and time)
- **Reference code** (if provided)
- **Transaction ID** (unique identifier)

**This is sufficient to prove:**
- Payment was sent
- To the correct recipient
- For the correct amount
- At the claimed time

**Critical for Bizum (Spain):** Notifications include recipient name

This enables automatic false-accusation prevention:

```python
# María's notification listener bot parses outgoing Bizum notification
payment = parse_bizum_notification(notification)

if payment.recipient_name == seller.expected_name:
    # Payment went to correct seller, safe to blacklist if covenant expires
    allow_blacklist()
else:
    # María paid wrong person (typo in phone number)
    warn_user("Payment sent to '{payment.recipient_name}', not seller '{seller.name}'")
    block_blacklist()  # Seller not at fault
```

**Without name verification (some payment methods):** Blacklist requires additional evidence or expires after 30 days if disputed.

### Legal Framework: Tampering is a Felony

**Editing a bank notification is document forgery.** In Spain, this is "falsedad documental" (Article 390-392, Spanish Penal Code): 6 months to 3 years in prison. For a €100 dispute, facing felony charges is irrational. Banks can verify transaction existence via transaction ID, making fraud easy to prove.

**Banks act as de facto notaries**—the notification is trusted third-party evidence without requiring a third-party arbitrator.

**If evidence is disputed, validators can:** check transaction ID with bank APIs, request full bank statements, contact seller's bank to verify non-payment, or compare payment details hash with on-chain covenant data. False accusations are quickly detected.

---

## Edge Cases

### Edge Case 1: Passive Seller's Infrastructure Fails

**Digital payment scenario:**
```
María pays seller via Bizum
Seller's phone battery dies
Notification listener bot never sees payment
Bot never locks BCH
Covenant expires empty—no automatic refund, María must pursue seller
```

**Cash-in-person scenario:**
```
María hands €100 cash to seller at café
Seller's phone is dead, can't confirm receipt in app
Covenant expires empty—no automatic refund
María must pursue seller for manual refund
```

**Who's at fault?** Seller (passive role = responsibility to maintain infrastructure).

**What happens:**
1. María blacklists seller (evidence: bot-parsed notification or witness statement)
2. Phase 0: Validators confirm payment sent, BCH not locked
3. Phase 1+: Seller can counter-claim or refund
4. Seller's reputation drops until dispute resolved
5. Seller refunds María manually when infrastructure restored
6. Blacklist auto-removed upon refund detection

**Fair outcome:** Passive sellers are responsible for uptime. If their infrastructure fails, they must make it right.

---

### Edge Case 2: Bot Fails to Parse Notification

**Digital payment:**
```
María pays seller via Bizum
Seller's notification listener bot has a parsing bug
Bot doesn't recognize the new notification format
Bot never locks BCH
Covenant expires as an empty shell—no BCH was ever locked
```

**Cash-in-person:**
```
María hands cash to seller
Seller confirms receipt verbally but forgets to tap "Confirm" in app
Bot waiting for confirmation, never locks BCH
Covenant expires as an empty shell—no automatic refund
```

**Same outcome as Edge Case 1:** Seller's responsibility to maintain working systems.

**Mitigation for Phase 1+:**
- Seller can prove bot failure (show logs, explain bug)
- Seller can counter-claim with evidence of good faith
- Seller refunds María, includes apology note
- Community sees seller taking responsibility → reputation recovers faster

**Phase 0 approach:** Blacklist first, resolve later. Bias toward protecting active users (they took the capital risk by sending payment first).

---

### Edge Case 3: False Accusation

**Digital payment false claim:**
```
María never paid seller (covenant expired empty, no refund to get)
María fabricates bank notification (attempts to blacklist seller anyway)
```

**Cash-in-person false claim:**
```
María never met seller
María posts blacklist claiming she handed cash
```

**Outcome (Phase 0 with validators):**
- Validators decrypt evidence, check transaction ID with bank
- No transaction found → blacklist rejected
- María's reputation drops (false accusation penalty)

**Outcome (Phase 1+ with counter-claims):**
- Seller posts counter-claim with bank statement showing no payment received
- Both have disputed blacklists, both reputation scores tank
- Market avoids both users
- If María persists, seller files legal complaint (false accusations are a crime)

**Why this is rare:**
- Bank transaction IDs are easy to verify
- False accusations damage the accuser's reputation
- Legal consequences (filing false reports is a crime in most jurisdictions)

---

### Edge Case 4: Seller Refunds Before Blacklist

```
Seller realizes bot failed
Seller manually refunds María before she blacklists
María receives refund
```

**No blacklist needed.** María can optionally post a "positive resolution" note (increases seller's reputation for handling failure gracefully).

**Incentive for sellers:** Refund immediately when you realize a problem. Faster resolution = less reputation damage.

---

## Privacy: What's Revealed vs Hidden

### Public Data (Visible to All Users)

When a blacklist event is posted:

- ✅ Target Cash Account (Elena#142)
- ✅ Amount (€100)
- ✅ Currency (EUR)
- ✅ Payment method (Bizum)
- ✅ Timestamp (2026-06-22 14:32)
- ✅ Evidence hash (sha256:a1b2c3...)

### Private Data (Encrypted, Only Validators)

- ❌ Full bank SMS screenshot
- ❌ Sender's bank account/phone
- ❌ Recipient's bank account/phone
- ❌ Transaction ID

**Why encrypt evidence?**
- Prevents doxxing (bank accounts are sensitive)
- Reduces incentive to scrape Nostr for payment data
- Validators must be trusted to decrypt (prevents mass surveillance)

**Phase 1+ improvement:** Zero-knowledge proofs could prove "bank SMS exists" without revealing contents, even to validators. Phase 0 accepts encryption as sufficient.

---

## Comparison to Traditional Dispute Systems

| Feature | Nostr Blacklist | Central Arbitrator | No System |
|---------|-----------------|-------------------|-----------|
| **Speed** | Minutes (post evidence) | Days-weeks (review, decision) | N/A (no recourse) |
| **Cost** | Free | $10-50 per case | Free |
| **Censorship** | Resistant (decentralized) | Vulnerable (company can refuse) | N/A |
| **Evidence** | Bank notifications | Submitted to company | N/A |
| **Enforcement** | Reputation drop (social) | Account ban (technical) | N/A |
| **Redemption** | Automatic (refund detected) | Manual (company decides) | N/A |
| **Permissionless** | ✅ Yes | ❌ No (company controls) | ✅ Yes |

**Trade-off:** Nostr blacklist relies on social enforcement (reputation), not technical enforcement (account ban). A blacklisted user can still try to transact—but no one will accept. Central arbitrators can ban accounts completely.

**Why social enforcement works:** Active users check reputation before sending fiat. A seller with 96% completion rate and a recent dispute is skipped in favor of a seller with 99% completion rate and clean history. Market-driven accountability.

---

## Bot Automation: How It Works

**Dispute resolution is fully automated.** Users don't manually monitor transactions—bots handle detection, evidence collection, and posting:

**Notification Listener Bot (Passive Seller):**
- Parses payment notifications (Bizum push, SEPA email, bank SMS)
- Checks payment reference matches expected covenant ID
- If match → locks BCH into covenant, updates reputation
- If unrecognized → alerts seller for manual review
- If parsing fails → covenant expires empty, seller must manually refund to restore reputation

**Dispute Listener Bot (Active Buyer):**
- Monitors covenant status continuously
- If covenant expires AND payment was sent → posts blacklist event with evidence
- Includes: payment notification, amount, timestamp, seller Cash Account
- Updates local reputation cache to avoid transacting with blacklisted users
- Fully automatic—buyer doesn't need to manually report

**Validator Bot (Phase 0 only):**
- Listens for new blacklist events on Nostr (kind 30078)
- Decrypts evidence, verifies bank notification authenticity
- Checks transaction ID with bank (if API available) or validates notification format
- Posts confirmation vote if evidence valid, rejection vote if fake
- Flags false accusers (damages their reputation)
- Validators check daily/weekly—3+ confirmations within 48 hours = blacklist confirmed

---

## Phase 0 Validation: What We're Testing

| Area | Question | Hypothesis |
|------|----------|------------|
| **Evidence Quality (cash)** | Is witness statement sufficient for cash-in-person disputes? | Yes (burden of proof on accuser) |
| **Evidence Quality (Bizum)** | Do push notification parsing + name matching prevent false accusations? | Yes (recipient name verification catches payment errors) |
| **Evidence Quality (SEPA)** | Are SEPA notifications parseable without screenshots? | Yes (push notification access works) |
| **False Accusation Rate** | % of blacklist events that are false/disputed? | <1% (name matching catches errors, tampering is a felony) |
| **Redemption Rate** | % of blacklisted users who manually refund? | >80% (reputation is valuable) |
| **Validator Efficiency** | Time to reach 3+ validator confirmations (Phase 0 only)? | <48 hours |
| **User Trust** | Do users check blacklist history before transacting? | Yes, >90% |
| **Payment Method Adoption** | Which payment methods see most usage? | Cash-in-person (simplest), then Bizum (documented) |

**Success criteria:**
- <1% false accusations
- >80% redemption rate (blacklisted users refund to restore reputation)
- <48 hours to validator consensus (Phase 0 only)
- >90% users check dispute history before selecting passive counterparty
- Name matching prevents >95% of "paid wrong person" false accusations (Bizum)

---

## The Limitations (And Why We Accept Them)

| Limitation | Impact | Mitigation | Why We Accept |
|------------|--------|------------|---------------|
| **Social enforcement only** | Blacklisted user can still attempt transactions | Active users check reputation before sending fiat; low completion rate = no business | Technical enforcement (account bans) requires central authority (unacceptable) |
| **Validator trust** | Must trust validators to honestly assess evidence | Phase 0: trusted community; Phase 1+: reputation-weighted voting | Zero-knowledge proofs (Phase 2+) could eliminate validator trust |
| **Evidence required** | Active user must keep bank notifications | Standard practice for financial transactions; most users screenshot automatically | Without evidence, blacklist system is vulnerable to false accusations |
| **24-hour grace period** | Passive user has 24 hours before covenant expires (can ghost for 24h) | Acceptable capital lockup for active users; prevents instant blacklisting without waiting | Shorter timeout = higher false positive rate (bot delays, bank delays) |
| **No refund guarantee** | Blacklisted user may never refund | Active user gets automatic covenant refund only if BCH was locked (funded covenants); for unfunded covenants, blacklist is the only recourse | Permissionless system can't force refunds (no company to enforce) |

---

## Key Takeaways

1. **Permissionless accountability** — No central arbitrator, no company, just evidence and reputation
2. **Payment-method agnostic** — Framework works for cash-in-person, Bizum, SEPA, or any future payment method
3. **Progressive rollout** — Phase 0 starts with cash-in-person + Bizum (documented), adds methods based on user demand
4. **Bank notifications as proof** — Timestamped, notarized (push notifications, email, or SMS), tampering is a felony
5. **Bot-automated enforcement** — Dispute detection, evidence posting, name matching all automated
6. **Name matching prevents false accusations** — For Bizum, bot checks payment.recipient_name == seller.name
7. **Social enforcement** — Reputation drop prevents future transactions (market-driven)
8. **Redemption via refund** — Blacklisted user can restore reputation by manually refunding
9. **Phase 0 validators, Phase 1+ counter-claims** — Trusted validators for bootstrap, then disputes resolve between users
10. **Pioneer badges** — Users who document new payment methods earn special reputation badges

**Dispute resolution isn't perfect. It's permissionless and payment-agnostic.**

Traditional systems have central arbitrators who can enforce outcomes (ban accounts, force refunds). Asgaya has evidence and reputation. Users choose who to trust based on completion rate and dispute history.

**The market decides.** A seller with 99% completion rate and no disputes outcompetes a seller with 96% and a recent blacklist. Reputation is valuable, so users refund to restore it. Accountability without authority.

**Safety over speed.** Asgaya starts with cash-in-person (simplest, no digital dependencies) and progressively adds payment methods only after they're fully documented and tested. Each corridor expands when users demand it and verify the implementation works.

**Cash-in-person + Bizum in Spain is revolutionary enough.** Adding 50 payment methods on day 1 would be fast. Deeply understanding each payment system before launching is safe. We choose safe.

---

## Related Documents

- [Permissionless Blacklist with Redemption](../../../asgaya/knowledge/concepts/C007_permissionless_blacklist_with_redemption.md) (original blacklist concept)
- [Reputation On-Chain (Not Central Database)](../../why-this-design/constraints/reputation-on-chain-not-central-database.md) (reputation constraint)
- [Nostr: How Buyers and Sellers Coordinate Privately](./README.md) (happy path coordination)

---

**Status:** Phase 0 Implementation  
**Last Updated:** 2026-06-22  
**Confidence:** High (bank notifications proven as evidence, blacklist reuses Nostr infrastructure)
