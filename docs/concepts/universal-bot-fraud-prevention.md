# Universal Bot Fraud Prevention System

**Status:** Core mechanism (Phase 0+)  
**Last Updated:** 2026-05-19

---

## Overview

Both senders AND sellers run the **same notification listener bot**. The bot performs dual roles:
- **Sender mode:** Listens for "Bizum sent" notifications → Creates fraud proof
- **Seller mode:** Listens for "Bizum received" notifications → Auto-signs covenant

**Key insight:** Seller cannot predict which senders have working bots. Attempting fraud has negative expected value (-€90 per attempt) plus permanent ban risk.

---

## How It Works

### The Universal Bot

```python
class AsgayaBot:
    """Same bot runs on sender AND seller devices"""
    
    def on_sms_received(self, sms):
        """NotificationListenerService callback"""
        
        # SENDER MODE: Listen for sent payments
        if "Bizum enviado" in sms.content or "Bizum sent" in sms.content:
            self.create_fraud_proof(sms)  # Insurance policy
        
        # SELLER MODE: Listen for received payments
        if "Bizum recibido" in sms.content or "Bizum received" in sms.content:
            self.sign_covenant_if_match(sms)  # Auto-sign
```

### Seller Bot: Auto-Sign on Receipt

```
1. Seller accepts bounty → Locks 0.107 BCH in covenant
2. Sender pays €100 Bizum
3. Bank sends SMS: "Bizum recibido: €100 de María"
4. Seller's bot parses SMS
5. Bot verifies: Amount matches covenant? Concept includes covenant ID?
6. Bot AUTO-SIGNS covenant condition 2 (trustless)
7. Covenant executes → Merchant gets BCH
```

**Seller has no manual decision point.** The bot controls signing.

### Sender Bot: Create Fraud Proof

```
1. Sender pays €100 Bizum
2. Bank sends SMS: "Bizum enviado: €100 a Juan"
3. Sender's bot parses SMS
4. Bot creates cryptographic proof:
   {
     "covenant_id": "4729",
     "amount": "€100",
     "timestamp": "2026-05-19 14:32:15",
     "sms_hash": "abc123...",
     "signature": "..."
   }
5. Proof stored locally (insurance policy)
```

**If seller doesn't sign:** Sender can submit this proof to slash seller's stake.

---

## Code Hash Verification

**Both bots must be unmodified official version:**

```
Official bot v1.0.0:
└─ Code hash: ABC123XYZ...

When seller registers:
├─ Bot calculates own hash: ABC123XYZ...
├─ Matches official? ✓
└─ Seller approved

When bot posts heartbeat:
├─ Includes code hash in signature
└─ Bulletin board verifies hash matches official version
```

**Why this matters:**
- Seller can't modify bot to skip auto-signing
- Sender can't modify bot to create fake proofs
- Hash mismatch = Rejected from network

---

## Economic Analysis: Why Seller Fraud is Irrational

### The Gamble

**What seller knows:**
```
✓ I received €100 Bizum (bank SMS shows it)
✓ Covenant is waiting for my signature
✓ I could choose not to sign...

❓ BUT: Does sender have fraud proof?
   └─ Unknown! Sender's bot status is invisible to me
```

**What seller doesn't know:**
- Is sender's bot working? (Maybe 95% uptime? 99%? 80%?)
- Did sender actually create fraud proof this time?
- Am I targeting a vulnerable sender or walking into a trap?

### Expected Value Calculation

**Assume 5% sender bot failure rate** (generous to attacker):

```
Scenario A: Sender bot working (95% probability)
├─ Seller doesn't sign
├─ Sender submits fraud proof
├─ Stake slashed: -€100
├─ Permanently banned
└─ Lose future earnings: -€3,600/year

Outcome: -€100 - €3,600/year = Catastrophic loss

Scenario B: Sender bot broken (5% probability)
├─ Seller doesn't sign
├─ Sender has no proof
├─ Seller keeps: €100 Bizum + 0.107 BCH refund
└─ Gain: €100

Outcome: +€100 (one-time gain)

Expected Value per attempt:
(0.95 × -€100) + (0.05 × €100) = -€90

Plus: 95% chance of losing €3,600/year forever
```

**Rational decision:** Sign honestly, earn €0.50 now + €10/day forever.

### The Information Asymmetry

**Seller has NO way to identify vulnerable targets:**

```
Can seller detect sender's bot is broken?
├─ Check sender's phone? ❌ (No access)
├─ Check sender's notification permissions? ❌ (Android privacy)
├─ Pattern analysis? ❌ (Each transaction independent)
└─ Ask sender "Is your bot working?" ❌ (Obviously not)

Result: Blind gambling with 95% loss rate
```

**Even if seller tries multiple times:**
```
Attempt 1: 95% chance banned = Game over
Attempt 2: Never happens (already banned)

Can't "learn" from mistakes. One strike = permanent exit.
```

---

## Outcome Matrix: All 9 Scenarios

| # | Sender Bot | Seller Bot | Seller Intent | Outcome |
|---|------------|------------|---------------|---------|
| 1 | ✅ Works | ✅ Works | Honest | **Happy path** - Covenant executes normally |
| 2 | ✅ Works | ❌ Fails | Honest | Sender submits proof → Resolution based on sender reputation |
| 3 | ❌ Fails | ✅ Works | Honest | **Seller bot saves the day** - Auto-signs, covenant executes |
| 4 | ✅ Works | (ignored) | **Dishonest** | **Sender bot saves the day** - Proof submitted, stake slashed |
| 5 | ❌ Fails | (ignored) | Dishonest | **Sender loses** - No proof, "keep your bot running" |
| 6 | ❌ Fails | ❌ Fails | Either | **Sender loses** - No proof, covenant refunds seller |
| 7 | ✅ Works | Fake proof | Dishonest | **Legal system** - Court order to cosign covenant |
| 8 | Never paid | (ignored) | Either | **Cannot submit proof** - Covenant refunds seller |
| 9 | Fake proof | (ignored) | Sender fraud | **Sender banned** - Dispute reveals fake proof |

---

## Reputation-Based Dispute Resolution

### Trusted Sender (Reputation ≥ 50)

**Fast track resolution:**

```
1. Covenant times out (seller didn't sign)
2. Sender submits fraud proof
3. System checks: sender.reputation >= 50? ✓
4. Auto-resolved (no manual review needed):
   ├─ Covenant matures based on proof
   ├─ Stake slashed: -0.1 BCH from seller
   ├─ Sender compensated: +0.1 BCH
   └─ Seller permanently banned
   
Resolution time: < 5 minutes
Privacy: Preserved (no identity disclosure)
```

**Why this works:**
- 50 successful transactions = Proven honest pattern
- False fraud claim would destroy valuable reputation
- Economic incentive: 50 × €0.XX fees invested in reputation
- Rational sender won't lie (loses more than gains)

### Untrusted Sender (Reputation < 50)

**Manual dispute process:**

```
1. Covenant times out (seller didn't sign)
2. Sender submits fraud proof
3. System checks: sender.reputation < 50? ✓
4. Dispute opened (both parties notified):
   
   ┌─────────────────────────────────────────────┐
   │ ⚠️ DISPUTE OPENED - Covenant #4729          │
   ├─────────────────────────────────────────────┤
   │                                             │
   │ Submit bank statement within 24 hours.     │
   │                                             │
   │ ⚠️ LEGAL NOTICE:                            │
   │ - Information shared with other party      │
   │ - Channel logged for authorities           │
   │ - False claims = Prosecution risk          │
   │                                             │
   │ [Upload Bank Statement] [Upload ID]       │
   └─────────────────────────────────────────────┘

5. Resolution paths:

   A. Both submit proof:
      └─ Manual arbitration (DAO review, 48h)
      
   B. Only sender submits:
      └─ Sender WINS (seller ghosted)
      └─ Stake slashed, sender compensated
      
   C. Only seller submits:
      └─ Seller WINS (sender lied)
      └─ Sender banned for false claim
      
   D. Neither submits:
      └─ Covenant refunds seller (default)
      └─ Both get reputation penalty

Resolution time: 24-48 hours
Privacy: Disclosed to other party only (not public)
```

---

## Privacy Considerations

### Three Privacy Levels

**Level 1: Normal Operation (95%+ of transactions)**
```
Privacy: 🟢 Full pseudonymity
├─ Cash Accounts used (Elena#142, not real names)
├─ No identity disclosure
├─ On-chain activity only
└─ SMS fraud proofs hashed (not full content)
```

**Level 2: Trusted Sender Dispute (4% of cases)**
```
Privacy: 🟢 Still pseudonymous
├─ Fraud proof auto-accepted
├─ No manual review
├─ No identity disclosure
└─ Fast resolution
```

**Level 3: Untrusted Sender Dispute (1% of cases)**
```
Privacy: 🟡 Partial disclosure
├─ Bank statements exchanged between parties
├─ Government ID verification required
├─ Disclosed to: Other party + arbitrators
├─ NOT disclosed to: General public
└─ Traceable by authorities if legal issue
```

**Level 4: Legal Action (0.1% of cases)**
```
Privacy: 🔴 Full disclosure
├─ Court subpoena
├─ All evidence provided to authorities
├─ Criminal/civil proceedings
└─ Public record (depending on jurisdiction)
```

### SMS Fraud Proof Privacy

**Fraud proof contains:**
```json
{
  "covenant_id": "4729",
  "amount_eur": "100",
  "timestamp": "2026-05-19T14:32:15Z",
  "sms_hash": "abc123...",  // ← Hash, not content
  "sender_pubkey": "...",
  "signature": "..."
}
```

**Full SMS only revealed if:**
- Dispute requires manual review
- Seller contests proof
- Arbitration needed

**SMS contains:**
- Sender's bank account details
- Seller's real name (potentially)
- Transaction ID

**Protection:** Only shared with other party in disputes, not publicly.

---

## Heartbeat Requirements (Both Sides)

### Seller Heartbeat: Strict

**To accept bounties:**
```
Payment method: Bizum
├─ Last "Bizum recibido" SMS: < 30 minutes ago
├─ Proves: "I can receive Bizum RIGHT NOW"
└─ Why strict: Must respond within 5-minute window

Status display:
├─ 🟢 ONLINE (< 30 min) → Can accept bounties
├─ 🟡 IDLE (30min - 2h) → May be slow
└─ ⚫ OFFLINE (> 2h) → Cannot accept bounties
```

### Sender Heartbeat: Relaxed

**To create bounties:**
```
Payment method: Bizum
├─ Last "Bizum enviado" SMS: < 48 hours ago
├─ Proves: "My bank sends me SMS confirmations"
└─ Why relaxed: Just needs fraud proof capability

If no recent SMS:
└─ "Send a test Bizum (€0.50) to verify SMS works"
```

**Why both are necessary:**
- Seller heartbeat: Proves bot can auto-sign (liquidity)
- Sender heartbeat: Proves bot can create fraud proof (security)

---

## Attack Vectors & Why They Fail

### Attack 1: Seller Modifies Bot to Not Auto-Sign

**Attempt:**
```
1. Download official bot
2. Modify code: Remove auto-sign logic
3. Run modified bot
4. Accept bounties
5. Receive Bizum but don't sign covenant
6. Keep €100 + 0.107 BCH refund
```

**Why it fails:**
```
├─ Modified bot has different code hash
├─ Bulletin board rejects: "Hash mismatch, you're running modified code"
├─ Cannot register as seller
└─ Attack prevented at registration
```

**Even if hash check bypassed:**
```
├─ Sender submits fraud proof (95% probability)
├─ Stake slashed, permanent ban
└─ Expected value: -€90 per attempt
```

### Attack 2: Sender Fakes Fraud Proof

**Attempt:**
```
1. Create bounty, seller accepts
2. DON'T pay Bizum (save €100)
3. Submit fake fraud proof: "I paid but seller didn't sign"
4. Claim seller's stake
```

**Why it fails:**
```
If sender has reputation (≥50):
└─ Can't submit proof without valid SMS
└─ Bot creates proof only when SMS received
└─ No SMS (didn't pay) = No proof to submit

If sender has NO reputation:
├─ Dispute triggered
├─ Must submit bank statement
├─ Can't produce valid statement (payment never happened)
├─ Seller provides: "No Bizum received in my account"
└─ Seller wins, sender banned for false claim
```

### Attack 3: Seller Stops Banking App After Accepting

**Attempt:**
```
1. Bot heartbeating (shows online)
2. Accept bounty
3. Stop banking app immediately
4. Sender pays Bizum
5. No SMS received (app stopped)
6. Don't sign covenant
7. Timeout → Get 0.107 BCH + €100 Bizum
```

**Why it fails:**
```
├─ Sender's bot creates fraud proof (sender bot still working)
├─ Sender submits proof
├─ Reputation-based resolution:
│  ├─ Trusted sender: Auto-resolved, stake slashed
│  └─ New sender: Dispute, seller can't prove "no payment"
└─ Economic result: -€100 stake + permanent ban
```

**Plus: Behavioral pattern detection**
```
Normal seller:
├─ Accepts many bounties
├─ All complete successfully
└─ Occasional timeout (rare technical glitch)

Suspicious seller:
├─ Accepts bounty
├─ Heartbeat stops immediately after
├─ Timeout occurs
├─ Pattern repeats
└─ Auto-flagged after 2-3 incidents
```

### Attack 4: Both Bots Fail, Seller Keeps Money

**Scenario:**
```
1. Sender pays €100 Bizum
2. Sender's bot crashes (no fraud proof)
3. Seller's bot also crashes (no auto-sign)
4. Covenant times out
5. Seller gets 0.107 BCH refund
6. Seller's bank has €100 Bizum
```

**This is NOT fraud** (both had technical issues), but outcome depends on seller honesty:

**Honest seller:**
```
├─ Bot restarts, sees "€100 received but covenant timed out"
├─ Bot alerts seller: "You have sender's money! Refund expected"
├─ Seller refunds €100 Bizum manually
└─ Reputation +5 (rewarded for honesty despite technical failure)
```

**Dishonest seller:**
```
├─ Keeps €100 (sender has no recourse)
├─ Sender loses €100 (harsh lesson)
└─ Warning shown: "Keep your bot running! It's your insurance policy"
```

**Frequency:** Very rare (both bots failing simultaneously ≈ 0.25% if each is 5% failure rate)

---

## Implementation Details

### Bot Registration (Phase 0)

```python
def register_seller(bot_code_hash, stake_amount):
    """Seller registration with bot verification"""
    
    # Verify code hash
    if bot_code_hash != OFFICIAL_BOT_HASH:
        raise ValidationError(
            "Modified bot detected. "
            "Download official bot from: asgaya.org/bot"
        )
    
    # Verify stake
    if stake_amount < 0.1:  # BCH
        raise ValidationError("Minimum stake: 0.1 BCH (~€100)")
    
    # Create seller account
    seller = Seller(
        bot_hash=bot_code_hash,
        stake=stake_amount,
        reputation=0,
        status="ACTIVE",
        registered_at=now()
    )
    
    return seller
```

### Fraud Proof Submission

```python
def submit_fraud_proof(covenant_id, proof, signature):
    """Sender submits fraud proof after timeout"""
    
    covenant = get_covenant(covenant_id)
    sender = get_user(covenant.sender_id)
    seller = get_user(covenant.seller_id)
    
    # Verify covenant timed out
    assert covenant.status == "TIMEOUT", "Covenant must be timed out"
    
    # Verify sender's signature
    assert verify_signature(proof, signature, sender.pubkey), "Invalid signature"
    
    # Verify timestamp was within window
    assert covenant.created_at < proof["timestamp"] < covenant.timeout_at, \
        "Payment timestamp outside covenant window"
    
    # Check sender reputation
    if sender.reputation >= 50:
        # TRUSTED SENDER - Auto-resolve
        mature_covenant_based_on_proof(covenant)
        slash_seller_stake(seller, 0.1)
        compensate_sender(sender, 0.1)
        ban_seller(seller, f"Failed to sign covenant #{covenant_id}")
        
        notify_sender("Seller banned. You've been compensated 0.1 BCH.")
    else:
        # UNTRUSTED SENDER - Open dispute
        dispute = open_dispute(
            covenant_id=covenant_id,
            sender_id=sender.id,
            seller_id=seller.id,
            sender_proof=proof
        )
        
        notify_both(
            "Dispute opened. Submit bank statement within 24h. "
            "Identity disclosure required."
        )
```

---

## Phase 0 Simplifications

**For initial testing:**

1. **Manual bot verification** (instead of automated hash checks)
   - Trusted testers only
   - Manual review of bot installation

2. **Higher reputation threshold** (trusted status after 10 transactions, not 50)
   - Faster trust building in small group

3. **Extended dispute window** (72 hours instead of 24)
   - More time for manual coordination

4. **Manual arbitration** (Suso + core team, not DAO)
   - Faster decisions during testing

**Phase 1 automation:**
- Automated code hash verification
- DAO-based arbitration
- Lower reputation threshold (50 transactions)
- Shorter dispute windows (24h)

---

## Success Metrics

**System working if:**

| Metric | Target | Indicates |
|--------|--------|-----------|
| **Fraud attempts** | < 1% of transactions | Economic deterrence working |
| **Sender bot uptime** | > 95% | Reliable fraud proof capability |
| **Seller bot uptime** | > 99% | Reliable auto-signing |
| **Disputes (trusted senders)** | < 0.1% | Reputation system effective |
| **Disputes (new senders)** | < 5% | Acceptable for untrusted users |
| **False fraud claims** | 0 | No sender gaming the system |

---

## Related Concepts

- [Fraud Proof Mechanism](fraud-proof-mechanism.md) - Technical implementation
- [Seller Heartbeat Mechanism](seller-heartbeat-mechanism.md) - Liveness proof system
- [Overcollateralized Bounty Contracts](overcollateralized-bounty-contracts.md) - Capital recycling strategy
- [Unclaimed Transaction Expiry](../decisions/unclaimed-transaction-expiry.md) - Timeout handling
- [Fee Splitting Model](../decisions/fee-splitting-model.md) - Economic incentives

---

## The Bottom Line

**The universal bot system secures Asgaya through economic impossibility, not technical prevention:**

✅ **Seller can't predict targets** (sender bot status unknown)  
✅ **Negative expected value** (-€90 per fraud attempt)  
✅ **One-strike permanent ban** (can't retry or learn)  
✅ **Opportunity cost** (€3,600/year honest earnings lost)  

**Result:** Rational sellers sign honestly. Irrational sellers get filtered out immediately.

The system doesn't prevent fraud attempts—it makes them economically suicidal. 🎯
