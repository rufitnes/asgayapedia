← [Back to Decisions](README.md)

# Dispute Resolution Framework

**Status:** Phase 0 (Simplified)  
**Category:** Trust & Safety  
**Related:** [Covenant Architecture](../concepts/bounty-contracts-with-volatility-buffer.md), [Merchant Flows](../android-app/flows/merchant-flows.md)

---

## The Problem

**Covenant settlement requires both parties to co-sign:**
- Merchant signs: "I gave cash to recipient"
- Recipient signs: "I received cash from merchant"

**What if they don't both sign?**

| Merchant | Recipient | Scenario | Resolution |
|----------|-----------|----------|------------|
| ✅ Signs | ✅ Signs | Happy path | Covenant executes ✅ |
| ✅ Signs | ❌ Doesn't sign | Disputed delivery | Timeout refund |
| ❌ Doesn't sign | ✅ Signs | Merchant backing out | Timeout refund |
| ❌ Doesn't sign | ❌ Doesn't sign | Coordination failure | Timeout refund |

**Most common dispute:** One party claims cash was exchanged, other party refuses to sign.

---

## The Decision: Autonomous Covenant + Trusted Parties (Phase 0)

### Phase 0 (MVP): No Formal Dispute System

**Strategy:** Start with trusted network only, let covenant handle edge cases autonomously

**Trust model:**
- **Trusted merchants only:** Sender's family/friends in recipient country
- **Trusted recipients only:** Sender's own family members
- **Personal vouching:** Sender knows both merchant and recipient personally
- **Expected dispute rate:** ~0% (trusted relationships)

**Covenant handles everything:**
1. **Both sign within 24h** → Covenant executes automatically → BCH distributed
2. **Timeout (24h no signatures)** → Covenant refunds automatically → Split refund:
   - Merchant portion (€99.50 BCH) → Sender (Iris gets BCH back)
   - Seller portion (€7.50 BCH) → BCH Seller (keeps fee for service)

**No mediation. No investigation. No judgment.**

**Why this works for Phase 0:**
- 1-2 personally vetted merchants per sender
- Low volume (<10 transactions/week total)
- Small amounts (€50-100 typical)
- Family network (fraud unlikely)
- Learn what disputes actually occur before building resolution system

---

## Dispute Scenarios & Mitigation

### Scenario 1: Recipient Takes Cash and Runs

**What happens:**
```
1. Merchant hands 500,000 VES to recipient
2. Recipient runs without signing covenant
3. Covenant times out (24h)
4. Split refund:
   - Merchant portion → Sender (€99.50 BCH back)
   - Seller portion → BCH Seller (€7.50 BCH)
5. Merchant lost: 500,000 VES (stolen)
```

**Mitigation:**
- ❌ **Protocol cannot solve this** (physical theft)
- ✅ **Merchant risk management:** Only accept trusted recipients
- ✅ **Phase 0 approach:** Sender's own family only
- ✅ **Sender accountability:** Sender vouches for recipient
- **Sender incentive:** If recipient steals, merchant won't serve that sender again

---

### Scenario 2: Merchant Tricks Recipient into Signing Early

**What happens:**
```
1. Merchant: "Tap here to confirm" (before giving cash)
2. Recipient signs covenant (doesn't realize)
3. Merchant signs too → Covenant executes
4. Merchant receives €99.50 BCH
5. Merchant never gives 500,000 VES (or gives less)
6. Recipient lost: Should have gotten cash, got scammed
```

**Mitigation:**
- ✅ **UX warnings:** "⚠️ NEVER SIGN UNTIL CASH IS IN YOUR HAND"
- ✅ **Education:** Video tutorials, in-app tips, sender briefs recipient
- ✅ **Trusted merchants:** Phase 0 = sender's family/friends only
- ✅ **Sender accountability:** Sender vouches for merchant
- ⚠️ **Residual risk acknowledged:** Social engineering possible, acceptable for Phase 0

---

### Scenario 3: Honest Disagreement

**What happens:**
```
Merchant claims: "I gave 500k VES, she won't sign!"
Recipient claims: "He only gave 450k VES!"
Both stuck, neither signs.
Covenant times out → Split refund.
```

**Mitigation:**
- ✅ **Covenant timeout:** Automatic resolution (split refund)
- ✅ **Trusted network:** Unlikely in Phase 0 (family relationships)
- ✅ **Sender arbitrates:** Sender knows both parties, can mediate offline
- **No protocol involvement needed**

---

## Safe Confirmation Sequence (Critical UX)

**Proper sequencing prevents most disputes:**

### Covenant Co-Signature Flow

```
1. Recipient arrives at merchant with bounty code
2. Merchant enters code → Reviews terms:
   - Amount: 500,000 VES
   - Merchant receives: €99.50 BCH (~0.0995 BCH)
3. Merchant accepts bounty (commits to terms)

4. ⚠️ MERCHANT HANDS CASH FIRST (before signing!)
   - Count 500,000 VES carefully
   - Hand ALL cash to recipient
   - Verify recipient has cash in hand

5. Merchant signs covenant: "Cash delivered"
   - Cryptographic signature on BCH blockchain
   - Immutable, non-repudiable

6. Recipient counts cash, verifies amount

7. ⚠️ RECIPIENT ONLY SIGNS AFTER CASH IN HAND
   - Recipient signs covenant: "Cash received"
   - Cryptographic signature on BCH blockchain

8. Covenant executes automatically
   - Both conditions met → BCH distributed on-chain
   - Merchant receives €99.50 BCH
   - Transaction complete (immutable)
```

**Why this works:**
- ✅ Covenant requires BOTH signatures (neither can complete alone)
- ✅ Recipient won't sign without cash in hand
- ✅ Signatures are blockchain-anchored (verifiable, immutable)
- ✅ Execution is autonomous code (no human discretion)
- ✅ Timeout protection (24h → auto-refund if incomplete)

**UI Enforcement:**
- **Merchant screen:** "⚠️ Hand cash BEFORE signing covenant"
- **Recipient screen:** "⚠️ ONLY sign AFTER cash is in your hand"
- **Both screens:** "Once both sign, covenant executes immediately"
- **Large warning:** Visual emphasis on sequence

**Security properties:**
- **Blockchain-anchored:** Signatures recorded on BCH blockchain
- **Non-repudiable:** Cannot claim "I didn't sign" after signing
- **Atomic execution:** Both conditions met → instant distribution
- **Timeout protection:** 24h → split refund if no signatures
- **No central control:** Smart contract executes autonomously

**Acknowledged risk:**
- ⚠️ Social engineering possible (merchant pressures recipient to sign early)
- **Acceptable for Phase 0:** Trusted merchants only, UX warnings, education
- **Future defense:** Video verification, reputation system (V1+)

---

## V1: Social Media Transparency (Future Enhancement)

**Phase 0 status:** Not implemented

**Concept:** When scaling beyond trusted network, disputes can be posted publicly on social media for transparency.

### How It Would Work:

**1. Party posts evidence on social media:**
- Platform: X (Twitter), Instagram, TikTok, etc.
- Content: Video/photo evidence, merchant name, covenant address, description
- Hashtag: `#AsgayaDispute` (searchable)

**2. Asgaya app searches and displays:**
- App searches: `#AsgayaDispute` + merchant name
- Displays: "2 disputes found on X.com"
- Links: Direct links to social media posts

**3. Users review evidence and decide:**
- Click link → View post on X/Instagram
- Watch videos, read comments, see community reactions
- Judge merchant trustworthiness themselves
- Accept or skip bounty based on own assessment

**4. Reputation emerges organically:**
- Merchants with credible disputes → Avoided naturally
- Merchants with clean history → Trusted
- False accusations without evidence → Ignored by community
- No Asgaya judgment, just information aggregation

### Why V1 (Not Phase 0):

- **Phase 0 = trusted parties:** Disputes should be rare (~0%)
- **No infrastructure needed yet:** Build only if disputes actually occur
- **Learn first:** Understand what disputes happen in practice
- **Scale trigger:** When expanding beyond trusted network (Phase 1+)

### Regulatory Status:

**This is information aggregation** (like Google Search):
- ✅ Asgaya doesn't host dispute content
- ✅ Asgaya doesn't judge or arbitrate
- ✅ Just links to publicly available information
- ✅ Social platforms handle hosting/moderation
- ✅ No dispute resolution service provided
- ✅ Pure bulletin board model preserved

**See:** Full social media dispute strategy documented in V1 roadmap (not detailed here).

---

## Alternative: RFID Card Signing (V1+)

For recipients without smartphones:

```
1-3. [Same as above: merchant accepts bounty]
4. Merchant hands cash to recipient
5. Merchant device shows: "Ask recipient to TAP their Asgaya card"
6. Recipient taps RFID card on merchant device (NFC)
7. Card cryptographically signs transaction → covenant executes
```

**See:** [RFID Card Recipients](../concepts/rfid-card-recipients.md) for full specification.

**Phase 0 status:** Not implemented (smartphone app only)

---

## What We're NOT Building (Phase 0)

❌ Mediator role  
❌ Investigation process  
❌ Strike system  
❌ Evidence collection  
❌ Dispute email  
❌ Manual arbitration  
❌ Appeal process  
❌ Community mediation  
❌ Social media integration  

✅ Just: Autonomous covenant + trusted parties + timeout refund

**Rationale:** Don't build dispute resolution until we know what disputes actually occur. Phase 0 trusted-only network might have ZERO disputes.

---

## Success Metrics (Phase 0)

**Target:**
- **<1% dispute rate** (trusted network should be nearly dispute-free)
- **100% resolved by timeout** (covenant handles automatically)
- **Zero merchant bans** (all trusted, personally vetted)

**Learning triggers:**
- **>5% dispute rate** → Investigate: Why are trusted parties disputing?
- **Repeat disputes from same merchant** → Remove from trusted network
- **Recipient fraud** → Sender accountability (vouching system)

**Decision point for V1:**
- If disputes are common (>5%) → Build social media integration
- If disputes are rare (<1%) → Keep simple, scale trusted network slowly

---

## Phase 0 Operational Approach

**If dispute occurs:**

1. **Covenant handles resolution automatically** (timeout refund)
2. **Sender mediates offline** (knows both parties personally)
3. **Sender decides future trust:**
   - Merchant at fault → Remove from trusted network
   - Recipient at fault → Don't send to that recipient again
   - Honest mistake → Give second chance

4. **No protocol involvement** (social layer resolution)

**Why this works:**
- Sender has all the information (family relationships)
- Sender has incentive to resolve fairly (future transactions)
- No regulatory risk (no Asgaya service provided)
- Learn what disputes look like before building systems

---

## Related Research

- **Covenant timeout mechanism:** See [Bounty Contracts with Volatility Buffer](../concepts/bounty-contracts-with-volatility-buffer.md)
- **Safe confirmation UX:** Prevents most disputes through proper sequencing
- **Trust networks:** Social reputation in local communities (V1+)
- **Social media transparency:** V1 enhancement for scaling beyond trusted network

---

## Contributors

- **Original framework:** Suso + Coordination (May 5, 2026)
- **Covenant architecture:** Suso + Coordination (May 10, 2026)
- **Phase 0 simplification:** Suso + Coordination (May 11, 2026)
- **Critical gap identified:** DeepSeek Review (May 4, May 10, 2026)

---

## Architecture Notes

**Evolution:**

**Old model (pre-May 10):**
- Escrow operator decides distribution
- Entity has discretionary control
- Triggered MiCA/PSD2 licensing requirements

**Covenant model (May 10):**
- Covenant executes autonomously
- Mediator investigates and recommends (no fund control)
- MiCA/PSD2 compliant

**Phase 0 simplification (May 11):**
- No mediator at all
- Trusted parties only
- Covenant timeout handles everything
- Social media transparency documented as V1 (not built yet)

**Preserved principles:**
- Safe confirmation sequencing (hand cash first, then sign)
- 24h resolution timeline (covenant timeout)
- Regulatory compliance (no intermediation)
- User protection (clear UX warnings)

---

**Last updated:** May 11, 2026  
**Status:** Phase 0 (Simplified, trusted-only network)  
**Next review:** After Phase 0 trials (first 50 transactions)
