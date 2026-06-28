# Payment Info Exchange Method

**Category:** Technical  
**Impact:** High (affects every covenant creation)  
**Phase:** Phase 0 critical  
**Status:** Open for collaboration

---

## The Question

**How should the sender obtain the seller's payment details (bank account, reference code, amount) in a way that is peer-to-peer, censorship-resistant, and privacy-preserving?**

---

## Why This Matters

Under Asgaya's payment-first, lock-second flow:

1. Sender creates covenant on-chain
2. Sender selects a BCH seller from bulletin board
3. **Sender needs seller's payment details to pay fiat via Bizum/SEPA**
4. Seller's bot locks BCH only after receiving fiat payment

The payment info delivery is a critical checkpoint that also serves as a **seller liveness check**: if the seller's bot doesn't respond with payment details, the sender knows not to proceed with the fiat payment.

---

## Design Requirements

The payment info exchange mechanism must satisfy:

### Must Have:
- ✅ **Peer-to-peer:** No central server dependency
- ✅ **Privacy:** Only the sender can read the payment details
- ✅ **Censorship-resistant:** No single point of failure
- ✅ **Liveness proof:** Proves seller's bot is online before sender pays
- ✅ **Fast:** Response within 2 minutes (sender won't wait longer)

### Nice to Have:
- ✅ **Minimal on-chain footprint:** Avoid bloating the blockchain unnecessarily
- ✅ **Standard protocols:** Use existing, well-tested solutions
- ✅ **Simple implementation:** Reduce development complexity
- ✅ **Cross-platform:** Works on Android, iOS, desktop

---

## Current Leading Options

### Option 1: Nostr Direct Messages (NIP-04)

**How it works:**
```
1. Seller includes Nostr pubkey in bulletin board listing
2. Sender's app sends encrypted DM: "Need payment info for covenant_xyz789"
3. Seller's bot responds via encrypted DM with payment details
4. Total latency: <1 second
```

**Pros:**
- ✅ Existing protocol (Nostr already used for bulletin board)
- ✅ End-to-end encrypted (NIP-04)
- ✅ Sub-second latency (very fast)
- ✅ Zero on-chain footprint
- ✅ Simple implementation (Nostr libraries well-supported)
- ✅ **Bonus:** Enables user-to-user messaging for coordination

**Cons:**
- ⚠️ Depends on Nostr relay availability
- ⚠️ Censorship resistance = good but not maximum (relays can be blocked)
- ⚠️ Requires separate Nostr integration (but already needed for bulletin board)

**Estimated effort:** Low (Nostr already integrated)

---

### Option 2: Encrypted OP_RETURN on BCH

**How it works:**
```
1. Sender posts request to blockchain (optional)
2. Seller's bot posts OP_RETURN transaction with payment info
3. Payment info encrypted with ECIES using sender's covenant pubkey
4. Sender's bot monitors blockchain, decrypts info
5. Total latency: ~2 minutes (one block confirmation)
```

**Pros:**
- ✅ Maximum censorship resistance (on BCH blockchain)
- ✅ End-to-end encrypted (ECIES)
- ✅ Verifiable (payment info cryptographically tied to covenant)
- ✅ No external dependencies (pure BCH)

**Cons:**
- ⚠️ Permanent on-chain storage (~200 bytes per covenant)
- ⚠️ Higher latency (~2 minutes vs <1 second)
- ⚠️ Small cost to seller (~€0.002 per covenant)
- ⚠️ More complex implementation (ECIES encryption, OP_RETURN monitoring)

**Estimated effort:** Moderate (requires encryption library and monitoring)

---

## Alternative Proposals Welcome

The Bitcoin Cash community may have better solutions we haven't considered:

**Possible alternatives:**
- WebRTC with Nostr signaling (P2P, fast, no on-chain data)
- Custom BCH-native messaging protocol
- Hybrid approach (Nostr primary, OP_RETURN fallback)
- Other decentralized messaging protocols

**What we're looking for:**
- Maintains privacy (encryption)
- Maintains decentralization (no central server)
- Works reliably in adversarial conditions
- Simple enough to implement in Phase 0

---

## Success Criteria

A solution is considered successful if:

1. **Reliability:** >99% success rate in delivering payment info within 2 minutes
2. **Privacy:** Payment details only readable by intended sender
3. **Censorship resistance:** Works even if some infrastructure is blocked/down
4. **User experience:** Feels instant to user (<5 second perceived latency)
5. **Implementation:** Can be built and tested within 2-4 weeks
6. **Maintenance:** Doesn't require constant manual intervention

---

## Investigation Methods

### 1. Protocol Analysis
- Compare Nostr NIP-04 encryption vs ECIES on BCH
- Evaluate relay network topology and censorship vectors
- Benchmark OP_RETURN monitoring performance
- Test cross-platform library support

### 2. Security Review
- Threat model for payment info interception
- Man-in-the-middle attack scenarios
- Relay operator malicious behavior
- Chain reorganization edge cases

### 3. Performance Testing
- Latency measurements: Nostr DM vs OP_RETURN
- Reliability under different network conditions
- Failover behavior when primary method unavailable
- Mobile device battery/data usage

### 4. Community Feedback
- Present both options to BCH Research Forum
- Gather input from BCH protocol developers
- Learn from similar projects (CashAccounts, AnyHedge, etc.)
- Identify edge cases we haven't considered

---

## How to Contribute

**If you have expertise in:**
- Nostr protocol and relay infrastructure
- BCH OP_RETURN best practices
- Decentralized messaging systems
- Cryptographic primitives (ECIES, NIP-04)

**We'd love your input on:**
1. Which option (Nostr or OP_RETURN) is better for Phase 0?
2. Are there hybrid approaches we should consider?
3. What edge cases or failure modes are we missing?
4. Are there existing BCH projects solving similar problems?

**How to help:**
- Comment on this unknown in BCH Research Forum
- Share relevant protocol documentation or examples
- Propose alternative approaches with rationale
- Review our technical assumptions

---

## Related Documents

### Technical Context:
- [Fraud Prevention: Payment-First Model](../collaborative_workspace/simplify-documentation-proposal/fraud-prevention-payment-first.md) - Why payment-first requires this
- [Encrypted Payment Info Technical Briefing](../collaborative_workspace/simplify-documentation-proposal/encrypted-payment-info-liveness-check.md) - Deep technical dive on OP_RETURN approach
- [Nostr Messaging Benefits](../collaborative_workspace/simplify-documentation-proposal/nostr-messaging-benefits.md) - User messaging bonus if Nostr chosen

### Architectural Context:
- [Covenant Architecture](../glossary.md#payment-first-covenant) - How covenants work
- [Bulletin Board](../the-mechanism/bulletin-board/README.md) - How sellers are discovered
- [Seller Bot](../android-app/backend-apis/seller-bot/README.md) - What needs to respond

---

## Current Status

**Decision:** Not yet made (Phase 0 design)  
**Recommendation:** Nostr DM (faster, simpler, enables messaging bonus)  
**Seeking:** BCH community technical review and feedback  
**Timeline:** Decision needed before Phase 0 implementation begins

---

## Questions for Discussion

1. **Nostr relay trust:** How much do we trust Nostr relay operators? Can they censor specific message types?

2. **On-chain bloat:** Is 200 bytes per covenant acceptable permanent on-chain storage? Or is this wasteful?

3. **Hybrid approach:** Should we implement both (Nostr primary, OP_RETURN fallback)? Or is this over-engineering?

4. **User messaging:** If Nostr is chosen, should we also enable user-to-user messaging for coordination? (sender→recipient, recipient→merchant)

5. **Future-proofing:** Which approach is more likely to still work in 5 years? 10 years?

---

**This unknown represents a critical Phase 0 design decision. Your expertise can help Asgaya choose the right path.**

*Last updated: 2026-05-31*  
*Status: Seeking community input*
---

## Navigation

**[🏠 Home](../index.md)** | **[↑ Unknowns](README.md)** | **[📖 Glossary](../glossary.md)**
