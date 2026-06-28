# Sender Journey: The Active BCH Buyer
**📖 Unfamiliar terms?** See the [glossary](../../../glossary.md) for definitions.

**Role:** BCH Buyer (Active)  
**Example:** María in Madrid (Spain) sending €100 to Elena in Caracas (Venezuela)

---

## Overview

A sender is someone who wants to send money to another Asgaya user. In Asgaya's framework, they are simply **BCH buyers** who:
1. Use fiat (EUR, VES, etc.) to buy BCH
2. Send that BCH to a recipient via a covenant
3. Pay a 1% total fee instead of 5-8% traditional remittances

**Mode:** Active - uses the app when they need to send money

**Typical use case:** Cross-border remittances (Spain → Venezuela), but also works domestically

---

## Payment Method Availability

**Cash in person is the global default.** Digital payment methods are added progressively as they're documented and tested.

**Phase 0 (Post-Launch):**
- **Spain:** Cash in person + Bizum + SEPA (Spanish IBANs)
- **Venezuela:** Cash in person only (PagoMóvil and local bank transfers documented in Phase 0+)
- **Rest of world:** Cash in person only

**Why progressive rollout:** Each payment method requires thorough documentation (notification formats, bank compatibility, legal framework). Cash in person works everywhere immediately. See [Progressive Payment Rollout](../../../why-this-design/constraints/progressive-payment-rollout.md).

**For global users:** Cash payment flow (shown below) is the primary method. Digital payments are a convenience where available.

---

## Step-by-Step: Two Flows

### Flow A: Digital Payment (Bizum - Spain Only, Phase 0)

### 1. Open Wallet & Enter Recipient
- María opens her Asgaya wallet
- Enters Elena's Cash Account: `Elena#142`
- Specifies amount: €100

### 2. Create Covenant
- App creates a covenant (smart contract) on BCH blockchain
- **Covenant is an empty shell** - no BCH locked yet, seller will fund after detecting payment
- Covenant terms:
  - Recipient: Elena's address
  - Amount: €100 worth of BCH (EUR-denominated, not fixed BCH amount)
  - Expiry: 8 hours (Phase 0)
  - Settlement options: Elena can claim BCH or cash out at merchant

### 3. Find BCH Seller on Bulletin Board
- App queries the bulletin board (NFTs on BCH blockchain)
- Filters: Accepts Bizum, has capacity for €100
- Shows list of available sellers with rates

### 4. Select Seller & Get Payment Instructions
- María selects Isabel (0.5% fee, 98% reputation)
- Via Nostr: Isabel sends encrypted payment instructions:
  - **Name:** Isabel Rodríguez (prevents false accusations, enables self-dealing detection)
  - **Phone:** 654321098
  - **Concepto:** Elena142 (app uses fuzzy match to parse Cash Account - no # symbol allowed in Bizum)
  - **Amount:** €100.50

If Isabel's payment info matches blacklist hashes (name/phone/IBAN from previous fraud), María's wallet shows warning before payment.

### 5. Pay via Bizum
- María opens banking app
- Sends Bizum to Isabel's phone: 654321098
- **Concepto field:** Elena142 (Isabel's bot will match this to Elena's covenant)
- **Amount:** €100.50

### 6. Seller's App Detects Payment & Locks BCH
- Isabel's app monitors bank notifications (built-in automation)
- Detects payment with reference `Elena142`
- Matches to covenant using Cash Account lookup
- **Locks €107 of Isabel's own BCH** from her inventory into covenant (€100 face value + €7 volatility buffer)

### 7. Elena Gets Notification
- Elena receives notification: "You have €100 worth of BCH"
- Options:
  - Claim to wallet (free)
  - Cash out at local merchant (0.5% fee)

**Total time:** 5 minutes - 4 hours (most is Elena's decision delay)  
**Total cost:** €0.50 (0.5% sender fee) + €0.50 (0.5% recipient fee if cash-out) = €1 total

---

### Flow B: Cash Payment (In-Person, Same Country - Global Default)

**When to use:** María doesn't have access to documented digital payment methods, or prefers cash.

**Common scenario:** Local BCH seller acts as intermediary. Could be a locutorio owner (immigrant community shop), corner shop owner, café worker (with owner's consent), or coworker helping colleagues. **Much easier than becoming a Western Union agent** - no licensing, no contracts, just download app and accept cash.

### 1. Open Wallet & Enter Recipient
- María opens her Asgaya wallet
- Enters Elena's Cash Account: `Elena#142`
- Specifies amount: €100

### 2. Create Covenant
- App creates a covenant (smart contract) on BCH blockchain
- **Covenant is an empty shell** - no BCH locked yet
- Covenant terms identical to Flow A

### 3. Find BCH Seller on Bulletin Board
- App queries bulletin board
- Filters: **Accepts cash in person** (same city), has capacity for €100
- Shows list of available sellers with rates and locations

### 4. Select Seller & Get Meeting Instructions
- María selects Isabel (locutorio owner, 0.5% fee, located 500m away)
- Via Nostr: Isabel sends encrypted instructions:
  - **Name:** Isabel Rodríguez
  - **Location:** Locutorio, Calle Principal 123 (or café, corner shop, etc.)
  - **Reference:** Elena142
  - **Amount:** €100.50 cash

### 5. Visit Seller & Pay Cash
- María walks to Isabel's shop
- Shows Asgaya app (covenant reference: Elena142)
- Pays €100.50 cash to Isabel
- Isabel confirms receipt in app

### 6. Seller's App Confirms & Locks BCH
- Isabel's app confirms cash received (manual confirmation)
- **Locks €107 of Isabel's own BCH** from her inventory into covenant
- Transaction recorded on-chain

### 7. Elena Gets Notification
- Elena receives notification: "You have €100 worth of BCH"
- Same claim/cash-out options as Flow A

**Total time:** 10 minutes - 4 hours (includes María walking to merchant + Elena's decision delay)  
**Total cost:** €0.50 (0.5% sender fee) + €0.50 (0.5% recipient fee if cash-out) = €1 total

**Key difference from Flow A:** Manual cash payment confirmation (Carlos confirms receipt) vs automatic digital payment detection (Bizum notification parsed by app).

---

## Active vs Passive

**María is an active user:**
- Opens app when she needs to send money
- Queries bulletin board each time
- Selects seller manually, accepts preselected best rate, or auto-selects
- Makes payment (cash in person is global default; digital methods like Bizum available where documented)

**Contrast with passive merchant:**
- Posts listing once
- App handles everything automatically (built-in automation)
- Earns money while sleeping

---

## Economics: Why María Uses Asgaya

### Cost Comparison

| Method | Fee | Time |
|--------|-----|------|
| Western Union | €5 (5%) | 1-2 days |
| Bank transfer | €8-15 (8-15%) | 3-5 days |
| Asgaya | €1 (1%) | 5 min - 4 hours |

### Benefits
- **90% cheaper** than traditional remittances
- **No KYC** - just payment rails María already uses (Bizum/cash)
- **No company custody** - BCH moves peer-to-peer via covenants
- **Legal recourse** - payment is traceable via banking system

### Fee Attribution
- **María pays:** €0.50 to BCH seller (0.5% markup on BCH purchase, included in €100.50 payment)
- **Elena pays:** €0.50 to merchant if cashing out (0.5% spread deducted from cash amount)
- **Total system fee:** €1.00 (1% of €100 sent)
- **Seller earns:** €0.50 per transaction
- **Merchant earns:** €0.50 per cash-out (if Elena chooses cash instead of wallet)

---

## What Prevents Fraud?

### Payment-First Covenant Model

**Key insight:** Seller never locks BCH until they receive fiat payment.

**Flow:**
1. María creates covenant (no BCH needed yet)
2. María pays Isabel €100.50 via Bizum (traceable, **irreversible** - unless Isabel refunds)
3. Isabel's app detects payment (built-in notification listener)
4. Isabel's app locks BCH into covenant
5. Elena can claim

**If Isabel ghosts:**
- María has Isabel's full name and phone number from payment
- Payment is traceable via Bizum
- Legal recourse available: **apropiación indebida** (criminal misappropriation in Spain)
- Isabel gains €100.50 but faces criminal charges

**Why this works:**
- Banking system acts as notary (Bizum includes name, phone, account)
- Refusing to refund after accepting payment = **apropiación indebida** (criminal offense)
- Even if Isabel is a mule (scammer used social engineering), **she's liable** - courts order refund regardless
- María's risk: €100.50 (traceable, legally recoverable)
- Covenant only executes after payment confirmed

---

## Edge Cases

### What if covenant expires before Elena claims?

**Scenario:** María sends €100, but Elena doesn't claim within 8 hours (Phase 0 time limit).

**Two possible cases:**

**Case A: Covenant was funded (seller locked BCH)**
- Covenant expires → BCH locked returns to **María's wallet** (not Isabel)
- **No H€ minting** - María receives BCH back, accepts volatility exposure
- María still has legal recourse against Isabel (she paid, delivery failed)

**Case B: Covenant was never funded (seller ghosted)**
- Covenant expires as **empty shell** - no BCH was ever locked
- **No BCH to refund** - covenant has nothing to return
- María must pursue seller via blacklist or legal action (**apropiación indebida**)
- María has seller's name, phone, payment trace - legal recourse available

**In practice:** Elena gets push notification, expires are rare.

**Why no minting on expiry:**
- **Attack vector prevention:** Users could deliberately let covenants expire to drain bull pool
- **H€ minting only allowed in two cases:**
  1. Covenant abort (>7% BCH drop) - protects sender from tail risk
  2. Merchant cashout - recipient converts BCH → H€ for stability
- **Phase 0 conservative approach:** Research funds limited, validate core hypothesis first

### What if BCH price crashes during transaction?

**How the EUR-denominated covenant works:**

Isabel receives €100.50 fiat (locked - this is her revenue regardless of BCH price). Isabel locks €107 worth of her own BCH into the covenant. The covenant promises **€100 worth of BCH** at claim time (not a fixed BCH amount).

- **If BCH rises:** Fewer BCH needed to satisfy €100, surplus returns to Isabel
- **If BCH drops <7%:** Buffer absorbs loss, €100 still delivered to Elena
- **If BCH drops >7%:** Covenant aborts (protects Elena from tail risk)

**Isabel's risk:** Limited to 7% buffer volatility. **Isabel's profit:** €0.50 fiat (locked).

**Scenario:** Price drops 10% before Elena claims (covenant funded, but Elena delays)

**What happens:**

**If drop <7%:** Buffer absorbs the volatility
- Isabel locked 107% collateral (€107 worth of BCH)
- 7% volatility buffer absorbs typical swings
- Covenant delivers €100 worth to Elena
- Unused buffer returns to Isabel

**If drop >7%:** Covenant aborts to protect Elena and María
- **Who's at risk:** Elena and María (volatility exposure they didn't choose)
- **Who's NOT at risk:** Isabel has €100.50 fiat - can buy back the same 0.107 BCH for less than €100.50 (keeps profit even after >7% drop)
- **What happens:**
  - BCH locked → María's wallet
  - If María chooses to stabilize (Phase 0 manual): App offers "Mint H€ to preserve €100 value?" (if bull pool has capacity)
  - If pool exhausted or María declines: María holds BCH (accepts volatility)
- María can still send H€ to Elena (merchants accept at cash-out)

**-3% Early Warning System (Hypothesis):**
When BCH drops 3%, both Elena and María get notifications with urgency based on bull pool status:
- **Pool healthy:** "Price dropped 3% - claim soon to avoid >7% abort"
- **Pool exhausted:** "⚠️ URGENT: No H€ available. Cash out before >7% drop or risk volatility"

**Hypothesis:** This warning prevents procrastination and reduces abort rate (untested in Phase 0).

**Why H€ minting matters:**
- María bought BCH to send remittance, not to hold BCH
- H€ preserves €100 value + remittance completes
- End-of-covenant-lifecycle minting (one of two H€ scenarios)

**Who uses H€/HAu tokens:**
- Passive sellers (Isabel) typically don't - they have fiat income
- Exception: If Isabel also has BCH buyer listing (e.g., VES income → wants BCH)
- Merchants escaping hyperinflation use H€/HAu as stability

**Unknown to document:** Not all merchants attract remittance customers. **Hypothesis:** Small neighbourhood shops where recipients buy essentials regularly become main cash-out points. This is documented in [Merchant Asset Preference](../../../why-this-design/open-questions/behavioral/merchant-asset-preference.md).

### What if Nostr relay is down?

**Scenario:** María can't receive payment instructions via Nostr.

**Phase 0 Resolution:**
- App tries multiple relays (decentralized)
- If all fail, María can cancel covenant and try another seller
- Simple fail-safe: pick different seller

**Phase 1+ Improvement:**
- Lobby to integrate Nostr relay into BCH node implementation
- Passive sellers running nodes would have built-in relay capability
- Reduces external infrastructure dependency

---

## User Experience Flow

```
[María's Phone]
  ↓
1. Open wallet → Enter Elena142 → Amount €100
  ↓
2. App creates covenant on BCH blockchain
  ↓
3. Query bulletin board → Show sellers
  ↓
4. Select Isabel → Receive Nostr message with bank details
  ↓
5. Open banking app → Pay €100.50 to Isabel via Bizum
   Reference: Elena142
  ↓
6. Wait 30 seconds - 5 minutes
  ↓
7. Notification: "Sent successfully! Elena will receive €100"
  ↓
[Done - Elena's turn to claim or cash out]
```

---

## Technical Details

**For implementation details, see:**
- [Wallet](../../../implementation/android-app/wallet/) - Covenant creation
- [Bulletin Board](../../../implementation/android-app/bulletin-board/) - Seller discovery
- [Nostr](../../../implementation/android-app/nostr/) - Message coordination
- [Notification Bot](../../../implementation/android-app/notification-bot/) - Payment matching

**For rationale, see:**
- [Why Payment-First?](../../../why-this-design/constraints/asgaya-remittances-inefficient-by-design.md) - Constraint #1: Inefficient by Design
- [Why Cash Accounts?](../../../why-this-design/constraints/cash-accounts-permissionless-identity-layer.md) - Constraint #3: Permissionless Identity
- [Why 7% Buffer?](../../../why-this-design/constraints/7%-volatility-buffer-money-velocity-enabler.md) - Constraint #2: Money Velocity Enabler
- [Dispute Resolution](../../../the-mechanism/nostr-coordination/dispute-resolution.md) - Payment-first risk model explained

---

## Next Steps

**After sending, María might want to:**
- Send again (becomes recurring user)
- Recommend to friends (referral incentives)
- Become a seller herself (earn fees on reverse corridor)
- Intermediate for less tech savvy users (this is very likely - earn small fee for helping neighbors/family)

**Related journeys:**
- [Recipient Journey](../recipient/) - Elena's perspective
- [Merchant Journey](../../merchant/) - Carlos cashing out Elena
- [Trader Journey](../../trader/) - Isabel's business model
- [Customer Journey](../../customer/) - Similar flow, different context (paying merchant)

---

**Status:** Phase 0 (Pre-Launch) - Q3 2026 Spain → Venezuela corridor  
**Updated:** 2026-06-16
