# Fraud Proof Mechanism

**Status:** Core security feature (Phase 0+)  
**Last Updated:** 2026-05-19

---

## Overview

The fraud proof mechanism protects senders when sellers don't sign covenants despite receiving payment. It works through:
1. **Sender's bot** automatically creates cryptographic proof when Bizum is sent
2. **Covenant timeout** (5 minutes) if seller doesn't sign
3. **Sender submits proof** to bulletin board
4. **Reputation-based resolution** (auto-resolve for trusted senders, manual dispute for new users)
5. **Stake slashing** + permanent ban for dishonest sellers

**Core concept:** Both sender and seller run the [Universal Bot](universal-bot-fraud-prevention.md), creating a trustless fraud prevention system.

---

## How It Works

### Step 1: Sender's Bot Creates Proof

**When sender pays:**
```
1. Sender pays €100 Bizum to seller
2. Bank sends SMS: "Bizum enviado: €100 a Juan García"
3. Sender's bot parses SMS automatically
4. Bot creates fraud proof:
   {
     "covenant_id": "4729",
     "amount_eur": "100",
     "recipient": "Juan García",
     "timestamp": "2026-05-19T14:32:15Z",
     "sms_hash": "abc123...",  // Privacy: hash not full SMS
     "sender_pubkey": "...",
   }
5. Bot signs proof with sender's private key
6. Proof stored locally (insurance policy)
```

**Privacy:** Only the SMS hash is stored initially. Full SMS is kept locally for potential disputes.

### Step 2: Covenant Timeout (Seller Doesn't Sign)

**Normal flow (seller honest):**
```
1. Seller receives Bizum
2. Seller's bot parses SMS
3. Bot AUTO-SIGNS covenant immediately
4. Covenant executes
5. Sender's fraud proof discarded (not needed) ✓
```

**Fraud attempt (seller dishonest):**
```
1. Seller receives Bizum
2. Seller stops banking app (no SMS received)
3. Covenant waits for signature...
4. 5-minute timeout expires
5. Covenant status: TIMEOUT
6. Sender's turn to act
```

### Step 3: Sender Submits Fraud Proof

```python
def on_covenant_timeout(covenant_id):
    """Sender's bot detects timeout"""
    
    # Check if we have fraud proof
    proof = self.payment_proofs.get(covenant_id)
    
    if not proof:
        # WARNING: No proof available
        show_alert(
            "⚠️ Covenant timed out but no payment proof detected!\n"
            "Without proof, you CANNOT claim fraud.\n"
            "Your €100 is at risk.\n\n"
            "ALWAYS keep your bot running during transactions!"
        )
        return
    
    # Submit proof to bulletin board
    bulletin_board.submit_fraud_proof(
        covenant_id=covenant_id,
        proof=proof["proof"],
        signature=proof["signature"]
    )
    
    log(f"Fraud proof submitted for covenant #{covenant_id}")
```

### Step 4: Reputation-Based Resolution

**Path A: Trusted Sender (Reputation ≥ 50)**

```
Bulletin board checks sender reputation:
├─ reputation >= 50? ✓
├─ Verify fraud proof signature: ✓
├─ Verify timestamp within window: ✓
└─ AUTO-RESOLVED (no manual review)

Actions:
├─ Covenant matures based on proof
├─ BCH distributed to merchant (as if seller signed)
├─ Seller's stake SLASHED: -0.1 BCH
├─ Sender compensated: +0.1 BCH
├─ Seller BANNED permanently
└─ Public record: "Seller #147 banned for fraud"

Resolution time: < 5 minutes
Privacy: Preserved (no identity disclosure)
```

**Why auto-resolution is safe:**
- 50 successful transactions = Proven honest pattern
- Lying would destroy valuable reputation (worth €5-50 in fees earned)
- Economic incentive to be honest (reputation more valuable than one-time gain)

**Path B: Untrusted Sender (Reputation < 50)**

```
Bulletin board checks sender reputation:
├─ reputation < 50? ✓
├─ Verify fraud proof signature: ✓
└─ DISPUTE OPENED (manual review required)

Notification to both parties:
┌─────────────────────────────────────────────┐
│ ⚠️ DISPUTE OPENED - Covenant #4729          │
├─────────────────────────────────────────────┤
│                                             │
│ Submit bank statement within 24 hours.     │
│                                             │
│ ⚠️ LEGAL NOTICE:                            │
│ - Information shared with other party      │
│ - Channel logged for authorities           │
│ - False claims = Criminal prosecution risk │
│                                             │
│ [Upload Bank Statement] [Upload ID]       │
└─────────────────────────────────────────────┘

Resolution paths:

1. Both submit proof:
   └─ Manual arbitration (DAO review, 48h)
   
2. Only sender submits:
   └─ Sender WINS (seller ghosted)
   └─ Stake slashed, sender compensated
   
3. Only seller submits:
   └─ Seller WINS (sender lied)
   └─ Sender banned for false claim
   
4. Neither submits:
   └─ Covenant refunds seller (default)
   └─ Both get reputation penalty

Resolution time: 24-48 hours
Privacy: Disclosed to other party only
```

---

## The Economic Reality

### Why Seller Fraud is Irrational

**What the seller doesn't know:**
```
When seller receives €100 Bizum:
├─ Did sender's bot create fraud proof? ❓
├─ Is sender's bot working? ❓
├─ Will I get caught? ❓
└─ NO WAY TO TELL (information asymmetry)
```

**Expected value calculation:**
```
Assume 5% sender bot failure rate (generous to attacker):

Scenario A: Sender bot working (95% probability)
├─ Seller doesn't sign
├─ Sender submits proof
├─ Seller loses: €100 stake
├─ Seller BANNED (lose €3,600/year forever)
└─ Total loss: €100 + €3,600/year

Scenario B: Sender bot broken (5% probability)
├─ Seller doesn't sign
├─ Sender has no proof
├─ Seller keeps: €100 Bizum + 0.107 BCH refund
└─ Gain: €100

Expected Value:
(0.95 × -€100) + (0.05 × €100) = -€90 per attempt

Plus: 95% chance of permanent ban (€3,600/year lost)
```

**Rational decision:** Sign honestly, earn €0.50 now + €10/day forever.

**See:** [Universal Bot Fraud Prevention](universal-bot-fraud-prevention.md) for complete economic analysis.

---

## Privacy Protection

### Three-Tier Privacy Model

**Tier 1: Normal Operation (95%+ of transactions)**
```
Privacy: 🟢 Full pseudonymity
├─ Cash Accounts (Elena#142)
├─ Fraud proof uses SMS hash (not content)
├─ No identity disclosure
└─ On-chain only
```

**Tier 2: Trusted Sender Fraud Claim (4% of cases)**
```
Privacy: 🟢 Still pseudonymous
├─ Fraud proof auto-accepted
├─ No manual review needed
├─ No identity disclosure
└─ Fast resolution
```

**Tier 3: Untrusted Sender Dispute (1% of cases)**
```
Privacy: 🟡 Partial disclosure
├─ Bank statements required
├─ Government ID verification
├─ Shared with: Other party + arbitrators
├─ NOT shared with: Public
└─ Traceable by authorities if legal issue
```

### What's in a Fraud Proof

**Public data (submitted to bulletin board):**
```json
{
  "covenant_id": "4729",
  "amount_eur": "100",
  "timestamp": "2026-05-19T14:32:15Z",
  "sms_hash": "abc123...",  // ← Hash, preserves privacy
  "sender_pubkey": "...",
  "signature": "..."
}
```

**Private data (kept locally, revealed only in disputes):**
```
Full SMS content:
├─ "Bizum enviado: €100 a Juan García"
├─ Transaction ID: TX-12345-67890
├─ Sender's bank account: ES12 1234 5678 9012 3456 7890
└─ Timestamp: 2026-05-19 14:32:15
```

**When full SMS is revealed:**
- Dispute requires manual arbitration
- Seller contests fraud claim
- Both parties must verify bank statements

---

## Attack Vectors & Mitigations

### Attack 1: Sender Fakes Fraud Proof

**Attempt:**
```
1. Create bounty
2. DON'T pay Bizum (save €100)
3. Submit fake fraud proof
4. Claim seller's stake
```

**Mitigation:**
```
Bot only creates proof when SMS received:
├─ No SMS (didn't pay) = No proof to submit
└─ Can't fake SMS hash without full SMS

If sender has reputation:
└─ Still can't submit without valid SMS

If sender has NO reputation:
├─ Dispute triggered
├─ Must provide bank statement
├─ Can't produce valid statement (payment never happened)
└─ Seller wins, sender banned
```

### Attack 2: Sender's Bot Fails, Loses Protection

**Scenario:**
```
1. Sender pays €100 Bizum
2. Sender's bot crashes (no proof created)
3. Seller doesn't sign (dishonest)
4. Covenant times out
5. Sender has NO proof
6. Sender loses €100
```

**This is NOT a security flaw, it's user responsibility:**
```
Warning shown to sender:
┌─────────────────────────────────────────────┐
│ ⚠️ NO PAYMENT PROOF DETECTED                │
├─────────────────────────────────────────────┤
│ You paid but your bot didn't capture SMS.  │
│                                             │
│ Without proof, you CANNOT claim fraud if   │
│ seller doesn't sign.                        │
│                                             │
│ Your €100 deposit is AT RISK.              │
│                                             │
│ Keep bot running to protect yourself!      │
└─────────────────────────────────────────────┘

Probability: ~5% (if sender bot has 95% uptime)
User education: Bot is YOUR insurance policy
```

### Attack 3: Seller Provides Fake Bank Statement in Dispute

**Scenario:**
```
1. Seller receives €100 Bizum (real)
2. Seller doesn't sign (fraud attempt)
3. Sender submits proof
4. Dispute opened (sender has no reputation)
5. Seller submits FAKE bank statement: "No payment received"
```

**Mitigation:**
```
Arbitration process:
├─ Both parties submit signed documents
├─ Sender: "Bizum sent" statement + Transaction ID
├─ Seller: "No payment" statement
├─ Transaction IDs cross-checked with bank APIs (if available)
├─ Mismatch detected (seller lying)
└─ Seller banned, legal consequences

Legal recourse:
├─ Dispute channel is logged (traceable by authorities)
├─ Fake statements = Criminal fraud
├─ Sender can file police report
└─ Court order can force bank records disclosure
```

---

## Implementation Notes

### Bot Code Structure

```python
class SenderBot:
    """Sender's notification listener bot"""
    
    def __init__(self):
        self.payment_proofs = {}  # Covenant ID → proof
        
    def on_sms_received(self, sms):
        """Android NotificationListenerService callback"""
        
        # Parse for sent payments
        if "Bizum enviado" in sms.content or "Bizum sent" in sms.content:
            self.create_payment_proof(sms)
    
    def create_payment_proof(self, sms):
        """Create cryptographic fraud proof"""
        
        # Extract data from SMS
        amount = self.extract_amount(sms.content)
        recipient = self.extract_recipient(sms.content)
        concept = self.extract_concept(sms.content)
        covenant_id = self.extract_covenant_id(concept)
        
        # Create proof object
        proof = {
            "covenant_id": covenant_id,
            "amount_eur": amount,
            "recipient": recipient,
            "timestamp": sms.timestamp.isoformat(),
            "sms_hash": hashlib.sha256(sms.content.encode()).hexdigest(),
            "sender_pubkey": self.pubkey
        }
        
        # Sign with sender's private key
        signature = self.sign(json.dumps(proof, sort_keys=True))
        
        # Store proof
        self.payment_proofs[covenant_id] = {
            "proof": proof,
            "signature": signature,
            "sms_content": sms.content  # Keep for disputes
        }
        
        log(f"✓ Fraud proof created for covenant #{covenant_id}")
```

### Bulletin Board Verification

```python
def submit_fraud_proof(covenant_id, proof, signature):
    """Verify and process fraud proof submission"""
    
    covenant = get_covenant(covenant_id)
    sender = get_user(covenant.sender_id)
    seller = get_user(covenant.seller_id)
    
    # Basic validation
    assert covenant.status == "TIMEOUT", "Covenant must be timed out"
    assert verify_signature(proof, signature, sender.pubkey), "Invalid signature"
    assert covenant.created_at < proof["timestamp"] < covenant.timeout_at, \
        "Timestamp outside covenant window"
    assert proof["amount_eur"] == covenant.amount_eur, "Amount mismatch"
    
    # Reputation-based resolution
    if sender.reputation >= 50:
        # AUTO-RESOLVE (trusted sender)
        auto_resolve_fraud(covenant, sender, seller, proof)
    else:
        # MANUAL DISPUTE (untrusted sender)
        open_dispute(covenant, sender, seller, proof)

def auto_resolve_fraud(covenant, sender, seller, proof):
    """Fast-track resolution for trusted senders"""
    
    # Mature covenant based on proof
    covenant.status = "MATURE_FRAUD_PROOF"
    covenant.mature_timestamp = now()
    
    # Slash seller's stake
    seller.stake -= 0.1  # BCH
    sender.compensation_balance += 0.1
    
    # Ban seller
    seller.status = "BANNED"
    seller.ban_reason = f"Failed to sign covenant #{covenant.id} despite payment proof"
    seller.banned_at = now()
    
    # Update reputations
    sender.reputation += 1  # Successful fraud claim
    seller.reputation = -1000  # Permanent negative
    
    # Notify parties
    notify(sender, f"Seller banned. You've been compensated 0.1 BCH.")
    notify(seller, f"You've been banned for fraud. Stake slashed.")
    
    # Public event
    emit_event("SellerBannedForFraud", {
        "seller_id": seller.id,
        "covenant_id": covenant.id,
        "proof_hash": hash(proof),
        "stake_slashed": 0.1
    })
```

---

## Success Metrics

| Metric | Target | Indicates |
|--------|--------|-----------|
| **Fraud proof creation rate** | > 95% | Sender bots working reliably |
| **False fraud claims** | 0 | No sender gaming |
| **Auto-resolved disputes (trusted senders)** | > 95% | Reputation system effective |
| **Manual disputes (new senders)** | < 5% | Low fraud attempt rate |
| **Seller fraud attempts** | < 1% | Economic deterrence working |

---

## Related Concepts

- **[Universal Bot Fraud Prevention](universal-bot-fraud-prevention.md)** - Complete system overview
- **[Seller Heartbeat Mechanism](seller-heartbeat-mechanism.md)** - Liveness proof
- **[Unclaimed Transaction Expiry](../decisions/unclaimed-transaction-expiry.md)** - Timeout handling
- **[Two-Step Settlement](../decisions/two-step-settlement-timing.md)** - Covenant mechanics

---

## The Bottom Line

**Fraud proof mechanism protects senders through:**

✅ **Automatic proof creation** (sender's bot does it)  
✅ **Economic deterrence** (seller can't predict targets, -€90 EV)  
✅ **Reputation system** (trusted users get fast resolution)  
✅ **Privacy preservation** (hashed proofs, identity disclosure only when needed)  
✅ **Legal compliance** (traceable disputes for authorities)

**Warning to senders:** Keep your bot running. It's your insurance policy. No bot = No proof = No protection. 🎯
