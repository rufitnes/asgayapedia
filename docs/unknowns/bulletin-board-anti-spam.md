# Unknown: Bulletin Board Anti-Spam Strategies

**Status:** Not Started  
**Priority:** High (Phase 0 critical)  
**Last Updated:** 2026-06-05  
**Contributors Welcome:** Yes  
**Related:** [Bulletin Board](../the-mechanism/bulletin-board/README.md), [Notification Listener Bot](../the-mechanism/notification-bot/README.md)

---

## What We Don't Know

What is the optimal anti-spam strategy for the bulletin board to prevent fake listings without creating barriers for legitimate users?

**The challenge:** The bulletin board must be permissionless (anyone can list) but protected from spam attacks (thousands of fake listings).

---

## Why It Matters

### 1. User Experience

**Without anti-spam:**
- Bulletin board flooded with fake listings
- Users waste time contacting non-responsive sellers/buyers
- Trust in the system erodes
- Real listings buried under spam

### 2. Network Costs

**Spam impacts:**
- Blockchain bloat (every listing is a UTXO with NFT commitment)
- Electrum query performance (more UTXOs to scan)
- Mobile app performance (parsing thousands of fake listings)

### 3. Reputation Risk

**First impression matters:**
- Phase 0 users see spam → assume system is broken
- External reviewers see spam → assume poor design
- Competitors point to spam as "proof Asgaya doesn't work"

---

## Current Approach (Baseline)

**From bulletin-board.md:**

```
Minimum BCH value required in listing UTXO:
├─ NFT commitment (listing data)
└─ ≥ 0.001 BCH (~€0.50 at current prices)

To spam with 1000 fake listings:
Cost = 1000 × €0.50 = €500
```

**Pros:**
- Simple to implement
- Economic deterrent (spamming has a cost)
- No centralized authority needed

**Cons:**
- 0.001 BCH might be too low (€500 is affordable for determined spammer)
- BCH price volatility affects spam cost
- Doesn't prevent Sybil attacks (one person, many listings with different Cash Accounts)

---

## Alternative Strategies

### Strategy 1: Increase Commitment Amount

**Approach:** Raise minimum BCH from 0.001 to 0.01 or 0.1 BCH

**Economics:**
- 0.01 BCH = ~€5 per listing → €5,000 for 1000 fakes
- 0.1 BCH = ~€50 per listing → €50,000 for 1000 fakes

**Pros:**
- Higher cost makes spam economically unfeasible
- Adjustable based on BCH price

**Cons:**
- Higher barrier for legitimate users
- Locks up more capital (especially for merchants with multiple listings)
- Might discourage Phase 0 participation

**Phase 0 test:** Start with 0.001 BCH, monitor spam levels, raise if needed.

---

### Strategy 2: Limit Listings Per Cash Account

**Approach:** One buyer listing + one seller listing per Cash Account maximum

**Rationale:**
- Legitimate users rarely need multiple listings in same role
- Merchants need both (buy from recipients, sell to senders)
- Forces spammer to create many Cash Accounts (more friction)

**Pros:**
- Prevents single Cash Account from flooding board
- Still permissionless (anyone can create 2 listings)
- No additional capital lockup

**Cons:**
- Doesn't prevent determined spammer with many Cash Accounts
- Might limit legitimate use cases (e.g., merchant with multiple locations)

**Implementation:** App enforces limit client-side, blockchain enforces via covenant rules.

---

### Strategy 3: Device Fingerprinting via Notification Bot

**Approach:** Notification listener bot flags multiple Cash Accounts on same device

**Mechanism:**
- Bot runs on user's Android device
- Device has unique identifier (hashed for privacy)
- Backend tracks: Device X has Cash Accounts A, B, C
- Flag if same device posts 10+ listings

**Pros:**
- Spammer needs smartphone farm (high friction)
- Doesn't affect legitimate single-device users
- Privacy-preserving (hashed device ID, no personal data)

**Cons:**
- Users might legitimately run multiple accounts (e.g., personal + business)
- Rooted devices can spoof IDs
- Requires backend tracking (centralization concern)

**Phase 0 feasibility:** High (we already have notification bot infrastructure)

---

### Strategy 4: Payment Method Identity Verification

**Approach:** One Cash Account per payment method (e.g., one per Bizum account, one per bank account)

**Rationale:**
- Payment methods are identity-linked (bank account, phone number)
- Spammer would need many bank accounts (high friction)
- Natural Sybil resistance

**Mechanism:**
- When creating listing, user proves control of payment method
- Example: "Send €0.01 to this reference → we verify → listing approved"
- Hash payment details to prevent duplicates

**Pros:**
- Strong Sybil resistance (payment methods are scarce)
- Increases trust (payment method verified before listing appears)

**Cons:**
- Adds friction to listing creation
- Privacy concern (payment method linkable to Cash Account)
- Might require centralized verification service (conflicts with permissionless goal)

**Phase 0 feasibility:** Low (privacy/complexity concerns)

---

### Strategy 5: Hybrid Approach (Recommended)

**Combine multiple strategies:**

1. **Minimum commitment:** 0.001 BCH (low barrier, basic spam deterrent)
2. **Listing limit:** 2 per Cash Account (1 buyer, 1 seller)
3. **Device fingerprinting:** Flag if same device has 10+ Cash Accounts
4. **Dynamic adjustment:** Raise commitment if spam detected

**Phase 0 implementation:**
- Start with 1+2 (easy to implement)
- Add 3 once notification bot is stable
- Monitor spam levels, adjust commitment if needed

**Success metrics:**
- <5% of listings are spam (measured by user reports or non-response rate)
- Legitimate users don't complain about barriers
- No single device/Cash Account dominates bulletin board

---

## Testing Plan

### Phase 0 (Venezuela Launch)

**Baseline configuration:**
- 0.001 BCH minimum commitment
- 2 listings max per Cash Account
- No device fingerprinting (Phase 1 feature)

**Monitor:**
1. **Spam rate:** % of listings that never respond to messages
2. **User complaints:** "Too many fake listings" feedback
3. **Listing turnover:** How often listings are updated/replaced
4. **Capital locked:** Total BCH locked in listing UTXOs

**Adjustment triggers:**
- If spam rate >10%: Raise commitment to 0.01 BCH
- If user complaints >20%: Add device fingerprinting
- If single Cash Account has >5 listings: Enforce 2-listing limit client-side

### Phase 1+ (Post-Venezuela)

**Add device fingerprinting:**
- Notification bot reports device hash with Cash Account
- Backend flags devices with >10 Cash Accounts
- Manual review or auto-hide flagged listings

**Advanced features:**
- Reputation scores (successful trades increase trust)
- Time-based limits (new Cash Accounts can create 1 listing, aged accounts can create 2)
- Community flagging (users report spam, high-flag listings hidden)

---

## Open Questions

### 1. What is the optimal commitment amount?

**Trade-off:** Higher = less spam, but also less participation

**Phase 0 hypothesis:** 0.001 BCH is sufficient for Venezuela (€0.50 is non-trivial)

**Adjustment mechanism:** Monitor spam rate, raise to 0.01 BCH if >10% spam

### 2. Should we enforce listing limits on-chain or client-side?

**On-chain:** Covenant prevents same Cash Account from creating >2 listings
- **Pro:** Enforced by blockchain, can't be bypassed
- **Con:** More complex covenant logic

**Client-side:** App prevents user from creating >2 listings
- **Pro:** Simple, flexible (can adjust limit easily)
- **Con:** Malicious users can bypass by using custom app

**Recommendation:** Start client-side (Phase 0), move to on-chain if abuse detected (Phase 1+)

### 3. Is device fingerprinting too centralized?

**Concern:** Backend tracking device hashes conflicts with permissionless philosophy

**Counter:** 
- Optional (users can opt out by not running notification bot)
- Privacy-preserving (hashed IDs, no personal data)
- Only flags, doesn't block (users can still create listings)

**Alternative:** Fully on-chain solution (listing limit enforced by covenant)

### 4. What about merchant reputation?

**Suso's insight (June 5):** Merchants with physical locations have reputation at stake. Should they be highlighted/prioritized?

**Implications for anti-spam:**
- Merchants less likely to spam (reputation risk)
- Highlighting merchants might reduce spam visibility (real listings appear first)
- Could create "verified merchant" tier (requires proof of location)

**See:** [Issue #4 below](#issue-4-merchant-highlighting)

---

## Success Criteria

**Anti-spam strategy is successful if:**

1. **Spam rate <5%:** Most listings are from real users who respond to messages
2. **No user complaints:** <10% of Phase 0 users report spam as a problem
3. **No single actor dominates:** No Cash Account or device represents >10% of listings
4. **Low barrier:** Legitimate users don't complain about cost or complexity

**Anti-spam strategy fails if:**

- Spam rate >20%: Bulletin board unusable, users abandon app
- High user complaints: "Too many fake listings, can't find real sellers"
- Single actor gaming: One spammer creates 100s of fake listings
- High barrier: Legitimate users complain "Too expensive to create listing"

---

## Related Documents

- [Bulletin Board Concept](../the-mechanism/bulletin-board/README.md) - Current anti-spam implementation
- [Notification Listener Bot](../the-mechanism/notification-bot/README.md) - Device fingerprinting mechanism
- [Phase 0 Validation](../glossary.md#progressive-decentralisation) - Testing approach

---

## Contributor Guidance

**Skills needed:**
- Game theory (economic incentives, Sybil resistance)
- Android development (device fingerprinting)
- Blockchain economics (commitment mechanisms)

**How to contribute:**
1. **Test spam resistance:** Create multiple fake listings, measure cost/friction
2. **Propose mechanisms:** Alternative anti-spam strategies we haven't considered
3. **Implement device fingerprinting:** Android code to hash device ID securely
4. **Monitor Phase 0:** Track spam rates, user complaints, listing quality

---

**Status:** Hypothesis formed. Baseline strategy documented. Awaiting Phase 0 data.

**Next steps:**
1. Implement 0.001 BCH minimum commitment in Phase 0
2. Enforce 2-listing limit client-side
3. Monitor spam rate for 30 days
4. Adjust commitment or add device fingerprinting based on results

---

*This unknown asks: How do we keep the bulletin board permissionless while preventing spam? The answer determines user experience quality and system resilience.*
---

## Navigation

**[🏠 Home](../index.md)** | **[↑ Unknowns](README.md)** | **[📖 Glossary](../glossary.md)**
