# Covenant Simplicity Principle: Protocol Capability vs Application Policy

**The Constraint:** Covenant complexity vs real-world failure modes

**The Question:** What belongs in the covenant (on-chain), and what belongs in the client (off-chain)?

---

## What Constrains Us

- **Immutability** - Once deployed, code cannot be changed
- **Auditability** - Complex logic = harder to verify
- **Failure modes** - Devices crash, users go offline, plans change
- **User sovereignty** - It's their money, not ours to control

---

## The Decision: Simple Covenants, Smart Clients

**The principle:**

> **Covenant = technical capability** ("CAN this action happen?")  
> **Client = business logic** ("SHOULD this action happen now?")

---

### **The Tagline**

> *"The app enforces fairness. The covenant enforces ownership."*

---

**In practice:**

```
┌─────────────────────────────────────────────────┐
│ COVENANT (on-chain, immutable)                  │
│                                                  │
│ function refund(sig senderSig) {                │
│     require(checkSig(senderSig, sender));       │
│     // Sender CAN refund ANYTIME               │
│ }                                               │
└─────────────────────────────────────────────────┘
                      ▲
                      │
                      │ enforces when appropriate
                      │
┌─────────────────────────────────────────────────┐
│ CLIENT (off-chain, updatable)                   │
│                                                  │
│ if (timeExpired || priceDropped) {              │
│     await covenant.refund();                    │
│     // Client SHOULD refund when conditions met │
│ }                                               │
└─────────────────────────────────────────────────┘
```

**What the covenant provides:**
- ✅ Technical escape hatch (sender can always refund)
- ✅ Emergency recovery (works even if client fails)
- ✅ Minimal attack surface (less code = easier to audit)

**What the client provides:**
- ✅ Business rules ("refund after expiry OR price drop >7%")
- ✅ User experience (automatic monitoring, notifications)
- ✅ Flexibility (update rules without redeploying covenant)

---

## The Trade-off

| Gain | Loss |
|------|------|
| **Simple covenant code** (easier to audit) | Relies on client behavior (off-chain enforcement) |
| **Emergency escape hatch** (sender can always exit) | Recipient trusts sender won't abuse early refund |
| **Updatable business logic** (client can improve) | Social layer needed (reputation, Nostr monitoring) |
| **No stuck funds** (works even if client crashes) | UX must hide refund button (prevent user confusion) |
| **Flexible without redeployment** | Two-layer mental model (covenant + client) |

---

## The Deeper Principle: User Sovereignty Over Safety Theater

**Just because a covenant CAN enforce a rule doesn't mean it SHOULD.**

When we lock user funds "for their protection," we're trading their sovereignty for our safety preferences. Covenants are powerful, but that power creates the temptation to over-constrain.

**Example of over-constraint:**

We COULD enforce a 24-hour waiting period before refund (prevents "impulsive" refunds). But:
- Whose money is it? **The sender's.**
- Who should decide when to refund? **The sender.**
- What should the app do? **Warn, suggest, delay the button.**
- What should the covenant do? **Not trap funds.**

**The principle:**
- Covenants enforce the **MINIMUM necessary constraint** (recipient must claim before expiry, price must be above floor)
- App logic handles **everything else** (should sender refund now? probably not, but it's their call)

**It's the user's money. Covenants should enable, not imprison.**

---

## What Complex Enforcement Looks Like (And Why We Reject It)

**We could build covenants that enforce every safety rule on-chain, but we choose not to.**

**Complex covenant approach (what we could do):**

```cash
function refund(...) {
    // "Safety" rules that trap user funds:
    require(oracleTimestamp >= expiryTime);  // Can't refund early
    require(currentPrice < priceFloor);      // Can't refund unless price dropped
    require(tx.time >= expiryMTP);          // Belt AND suspenders
    // User funds locked until ALL conditions met
}
```

**This "protects" the sender by trapping their money.** If sender changes mind, device fails, or oracle goes offline → funds stuck.

---

**Our v2.2 approach:**

```cash
// Sender's money, sender's decision
function refund(sig senderSig) {
    require(checkSig(senderSig, sender));  // Just verify ownership
    // Covenant enables. Client decides when.
}
```

**The app enforces fairness:**
```javascript
// Client auto-refunds when appropriate
if (timeExpired || priceDropped) {
    await covenant.refund();  // But sender CAN refund anytime if needed
}
```

---

## The Bitcoin Analogy

**Bitcoin does this already:**

| Layer | Bitcoin | Asgaya |
|-------|---------|--------|
| **Protocol** | Any valid transaction allowed | Covenant allows sender refund anytime |
| **Wallet** | "Are you sure?" warnings, fee estimation | Client hides refund button until appropriate |
| **Result** | Permissionless at base, sensible UX on top | Same pattern |

**Bitcoin example:**
- Protocol: CAN send all your BTC to a typo'd address
- Wallet: "⚠️ This address looks invalid. Proceed?"
- User: Protected by UX, not by protocol restriction

**Asgaya example:**
- Covenant: CAN refund immediately after funding
- Client: "Payment active - auto-refund if timeout or price drop"
- User: Protected by UX, not by covenant restriction

**Why this works:** Users run wallets they trust. Covenant permissionlessness means users can switch wallets if one misbehaves.

---

## How It Emerged (v2.0 → v2.2 Evolution)

**The progression from complex to simple:**

| Version | What changed | Problem it solved | Problem it created |
|---------|-------------|-------------------|--------------------|
| **Phase 1** | MTP-only refund | Basic refund path | Too slow for testing (hours) |
| **v2.0** | Added oracle fast path | Fast refund (5 min) | Oracle dependency for refund |
| **v2.1** | Added price drop protection | Claim rejects below floor | Can't refund on price drop before expiry |
| **v2.2** | Removed conditions from refund; client enforces | Simple covenant, emergency escape, no oracle needed for refund | Two-layer mental model |

**The realization:** We kept adding complexity to handle edge cases. Moving logic to the client solved all issues at once.

**Client enforcement pattern:**
```javascript
// Client decides when auto-refund is appropriate
async function shouldAutoRefund() {
    const timeExpired = currentTime >= expiryTime;
    const priceDropped = currentPrice < priceFloor;
    return timeExpired || priceDropped;
}
```

**Result:** 
- Simple covenant (easy to audit)
- Oracle only needed for claim (not refund)
- Immediate price-drop refund possible
- Emergency manual refund always available

---

## The Sender-Centric Design Choice

**Why sender gets flexibility:**

1. **Sender funds the infrastructure** (pays seller, enables ecosystem)
2. **Sender takes price risk** (BCH volatility exposure)
3. **Sender owns the covenant** (created it, provided capital)

**Recipient/merchant have deadlines:**
```cash
function claim(...) {
    require(oracleTimestamp < expiryTime);  // Must claim before deadline
}
```

**Seller is passive:**
- Gets buffer back in ALL scenarios (claim, refund, seller split)
- No time constraints
- No action required

**Asymmetry is intentional:** Protect the party funding the system (sender), give them maximum flexibility.

---

## What This Enables

**For senders:**
- Zero-friction UX (set conditions once, system auto-refunds)
- Emergency escape (manual refund if client fails)
- Price protection + timeout protection (automatic)

**For developers:**
- Iterate on UX without redeploying covenant
- Add new conditions (e.g., "refund if recipient offline 24hr")
- Fix bugs in client without touching covenant

**For auditors:**
- Simple covenant (3 functions, <100 lines)
- Clear security model (less code = smaller attack surface)
- Easy to verify (no complex time/price logic)

---

## The Limitations (And Why We Accept Them)

| Limitation | Impact | Mitigation | Why We Accept |
|------------|--------|------------|---------------|
| **Sender can refund early** | Recipient might not get payment | Client hides refund button, Nostr monitoring, reputation | Sender funded the covenant - they own it |
| **Client enforces fairness** | Malicious client could auto-refund immediately | Open-source client, users choose wallet, social reputation | Same as Bitcoin wallets - users trust what they run |
| **Two-layer mental model** | Harder to explain (covenant vs client) | UX hides complexity ("auto-refund protection"), docs explain "why" | Simpler covenant = less risk, worth the explanation cost |
| **Oracle only needed for claim** | Can't trustlessly verify refund conditions | MTP still available as fallback, refund is sender's right anyway | Permissionless escape hatch more important than perfect verification |

---

## History: Credit Where Due

**Discovered:** July 2026 during Phase 1 covenant testing (v2.0 → v2.2 evolution)

**Inspiration:**
- **Bitcoin Core** (protocol vs wallet separation since 2009)
- **CashScript examples** (simple unlock conditions, complex logic off-chain)
- **AnyHedge** (covenant oracle pattern, trustless fallback)

**The realization came from chipnet testing:**
- v2.0: "Oracle refund is faster!"
- v2.1: "But we can't refund on price drops before expiry..."
- v2.2: "Wait, why are we constraining the covenant? Just let sender refund, client decides when!"

**Key insight:** We were designing for the success case (time expiry, oracle available). Real-world testing revealed failure modes (oracle offline, price drops, sender emergency exit). Simplifying the covenant solved all of them.

---

## Implementation Status

**Current version: v2.2** (simplified refund)  
**Planned: v2.3** (adds seller buffer recovery for sender-offline edge case)

**v2.2 Covenant code:**
```cash
function refund(sig senderSig) {
    require(checkSig(senderSig, sender));
    
    // Output 0: Payment → sender
    // Output 1: Buffer → seller
}
```

**v2.3 Addition: Seller Buffer Recovery**

Handles edge case where sender device offline after expiry:

```cash
function sellerRecoverBuffer(sig sellerSig, datasig oracleSig, bytes oracleMessage) {
    require(checkSig(sellerSig, seller));
    require(checkDataSig(oracleSig, oracleMessage, oraclePubkey));
    
    int oracleTimestamp = int(oracleMessage.split(8)[0]);
    require(oracleTimestamp >= expiryOracleTime);  // Only after expiry
    
    // Output 0: Payment → sender (fair, even if sender offline)
    // Output 1: Buffer → seller (recovers own capital)
}
```

*Same principle, different actor:* Seller gets independent recovery path. Simplicity doesn't mean fewer functions—it means each function does one clear thing.

**Client implementation:** Auto-refund monitoring (20-second checks, distributed across sender/recipient/seller devices)

**Testing status:** 
- ✅ v2.0 oracle refund tested (chipnet)
- ✅ v2.1 price drop protection tested (chipnet)
- ⏳ v2.2 simplified refund deployed, testing in progress
- 📋 v2.3 seller recovery planned

---

## Success Criteria

**This principle succeeds when:**

1. ✅ **Covenant stays simple**
   - 3 functions (claim, refund, sellerRecoverBuffer)
   - <100 lines of CashScript
   - Auditable in 30 minutes

2. ✅ **Client handles complexity**
   - Business logic (when to refund)
   - User experience (auto-monitoring)
   - Flexibility (updatable without covenant changes)

3. ✅ **Emergency escape works**
   - Sender can manually refund even if client fails
   - No permanently stuck funds
   - Permissionless at protocol layer

**Phase 0 validates:** Does separation of concerns translate to real-world robustness?

---

## Related Documents

- [Requirements](../requirements/README.md) - Why capital efficiency matters (Requirement #6)
- [Time Oracle Decision](./time-oracle-mtp-fallback-trustless-ux.md) - Oracle vs MTP trade-offs
- [Auto-Refund UX](../../user-journeys/sender/auto-refund-ux.md) - How senders experience this principle
- [Covenant Version History](../../implementation/covenants/version-history.md) - v2.0 → v2.2 evolution

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Why This Design?](../README.md)** | **[📖 Glossary](../../glossary.md)**

**Related:** [Constraints](./README.md) | [Time Oracle](./time-oracle-mtp-fallback-trustless-ux.md)
