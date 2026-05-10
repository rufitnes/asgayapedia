# Why: Minimal Hardware Requirements

**Sub-requirement of:** [Why: Permissionless](core-architecture/why-permissionless.md)

**Core Requirement:** Support the widest possible range of devices, minimizing hardware barriers to remittance access.

> **Note on "Minimal":** In Phase 0, a basic Android smartphone is required. This is still "minimal" compared to traditional crypto (no BCH wallet, no private keys, no exchange account—just install a free app). Future phases aim for true zero-smartphone options (RFID cards, feature phones) as documented below.

**⚠️ Implementation Status:**
- **Phase 0 (Current):** Android smartphone with app required
- **Phase 1 (Planned):** RFID card alternative (no smartphone needed)
- **Future Vision:** Cardboard QR codes / feature phones (aspirational)

**This document describes the VISION.** See [RFID Card Recipients](concepts/rfid-card-recipients.md) for Phase 1 implementation plan.

---

## The Problem

### Hardware Requirements Exclude the Poorest Users

Most "crypto solutions" assume everyone has a smartphone. Many assume a recent smartphone with NFC, reliable internet, and modern operating systems.

**Who this excludes:**

**The extremely poor:**
- Can't afford €100+ smartphone
- Maybe have €10 feature phone
- Maybe have nothing at all
- **Need remittances most, excluded first**

**Elderly users:**
- Don't want to learn smartphone
- Prefer simple solutions
- May only have basic phone
- **Excluded by complexity**

**Children receiving money for family:**
- Too young for own phone
- Family shares one phone
- Need to receive money independently
- **Excluded by age**

**Rural populations:**
- Limited phone access
- Shared community devices
- No reliable electricity for charging
- **Excluded by infrastructure**

**The question:** How minimal can we go while still working?

**The answer:** A piece of cardboard with a QR code printed on it.

---

## Why Cardboard QR Codes Matter

### The Philosophy: If It Works for the Lowest-Tech User, It Works for Everyone

**Scenario:**
- Recipient in rural area
- No smartphone
- No electricity
- No money to buy device
- **Has: Piece of cardboard**

**Solution:**
1. Merchant (who has phone) generates BCH address for recipient
2. Prints QR code on cardboard or writes address on paper
3. Gives to recipient
4. Recipient can now receive remittances
5. **Cost: €0**

**When money arrives:**
1. Sender in Spain sends €100 via Bizum to escrow
2. Escrow notifies merchant: "€100 ready for [recipient ID]"
3. Recipient shows cardboard QR to merchant
4. Merchant scans, sees BCH address matches
5. Merchant hands €100 equivalent in cash
6. **Remittance complete**

**Hardware requirement for recipient: €0**

### Why This Approach Is Not "Nice to Have"—It's Essential

**If we require smartphones:**
- We exclude people earning <€200/month (can't afford €100 phone)
- We exclude rural areas with no electricity
- We exclude elderly who won't adopt new tech
- **We've built "better Western Union for people with smartphones"**

**With cardboard QR support:**
- Anyone can receive money
- No hardware barrier
- No learning curve (merchant does the tech part)
- **We've built financial access for everyone**

---

## The Tiered Hardware Model

### Progressive Enhancement: Pay for Better Experience, Not for Access

**Tier 1: Cardboard QR Code (€0)**
- **What you get:** Can receive money
- **What you can't do:** Verify transactions yourself, receive notifications
- **Trade-off:** Must trust merchant, need merchant with phone nearby

**Tier 2: RFID Sticker/Card (€2-5)**
- **What you get:** More durable, harder to counterfeit, can tap merchant's phone
- **What you can't do:** Still can't verify independently
- **Trade-off:** Small cost, but lasts longer than paper

**Tier 3: Feature Phone (€10-20)**
- **What you get:** Receive SMS notifications, communicate with escrow
- **What you can't do:** Can't verify transactions visually, can't scan codes
- **Trade-off:** More independence, still limited functionality

**Tier 4: Smartphone (€50-100 used)**
- **What you get:** Full functionality, complete autonomy, best experience
- **What you can't do:** Nothing, full control
- **Trade-off:** Higher cost, but maximum capability

**The key:** Everyone can participate. Hardware determines quality of experience, not access to the system.

---

## Why This Philosophy Matters

### 1. Hardware Should Not Be a Barrier to Financial Access

**Current system:**
- Want to receive Western Union? → Need government ID
- Want to receive crypto? → Need €300 smartphone
- **Either way, poorest are excluded**

**Asgaya's approach:**
- Want to receive remittance? → Need cardboard with QR code
- Have €5? → Get RFID sticker (better experience)
- Have €100? → Get smartphone (best experience)
- **No one excluded, everyone can upgrade when they can afford it**

### 2. Design for the Edge Case, Not the Average Case

Most tech projects ask: "What does our average user have?"
- Average user has smartphone
- Design for smartphone
- Edge cases (no phone) are ignored

**Asgaya inverts this:**
- What does our poorest user have? → Nothing
- Design so cardboard works
- **If cardboard works, smartphones work even better**

**Result:** System that serves everyone, from poorest to richest.

### 3. The Merchant Has the Tech, Not the Recipient

**Key insight:** Merchants are businesses. They can afford smartphones. Recipients might not.

**Division of labor:**
- **Merchant:** Has smartphone, runs Asgaya app, scans codes, co-signs covenant
- **Recipient:** Has cardboard (with QR code or 4-digit code), shows it to merchant, receives cash
- **Sender:** Has bank account, sends Bizum to BCH seller
- **BCH Seller:** Has BCH inventory, posts overcollateralized BCH to covenant, automated via bot

**Each participant contributes what they can afford. No one is required to have what they can't afford.**

---

## The RFID Sticker Vision

### Why RFID Stickers Are the Perfect Middle Ground

**Better than cardboard:**
- More durable (lasts years, not months)
- Harder to counterfeit (contains chip)
- Can store multiple addresses (rotate for privacy)
- Feels more "legitimate" (tap vs show paper)

**Cheaper than smartphones:**
- Cost: €2-5 per sticker
- No battery (passive NFC)
- No charging needed
- Can embed in ID card, keychain, wristband

**Use case:**
- Recipient gets RFID sticker embedded in ID card
- Goes to merchant
- Taps phone to merchant's phone
- Merchant's app reads BCH address
- Hands cash
- **Works even if recipient's phone is dead or stolen**

**Why this matters:** In areas with unreliable electricity, phones die. RFID stickers never die. They're the ultimate resilience layer.

---

## Low-Tech Edge Cases Are Given Serious Consideration

Unlike most crypto projects that say "edge cases don't matter," Asgaya designs for them:

**What if recipient has no phone?**
- → Cardboard QR code

**What if recipient's phone is dead?**
- → RFID sticker backup

**What if there's no electricity to charge phones?**
- → Cardboard or RFID (no power needed)

**What if recipient is illiterate?**
- → Visual QR code (universal, no reading needed)

**What if there's no internet in the area?**
- → Offline sync mode (merchant caches data, syncs later)

**The principle:** If we solve for the worst case, we solve for everyone.

---

## Why "Nice to Have" Becomes "Must Have"

**When you're designing for Silicon Valley:**
- "Cardboard QR support is nice to have, maybe V3"
- Average user has iPhone
- Edge cases ignored

**When you're designing for migrants and recipients:**
- "Cardboard QR support is V1.1, right after MVP"
- Average user has feature phone or nothing
- **Edge cases are the majority**

**Asgaya serves the majority that other systems call "edge cases."**

---

## The Trade-Offs Are Acceptable

**Cardboard users give up:**
- Independence (can't verify alone)
- Privacy (merchant sees their address)
- Convenience (must find merchant with phone)

**Cardboard users gain:**
- Financial access (€0 barrier)
- Ability to receive remittances
- Path to upgrade (save money, buy phone later)

**For someone with nothing, getting 70% of the experience at €0 cost is far better than getting 0% of the experience because they can't afford €100.**

---

## Related Requirements

- [Why: Permissionless](core-architecture/why-permissionless.md) — Minimal hardware is one aspect of truly permissionless access
- [Why: No KYC](core-architecture/why-no-kyc.md) — Low barriers across all dimensions (ID, hardware, knowledge)

---

## Trade-offs and Decisions

See the **Decisions** section for implementation approaches:

- Decision: QR codes vs NFC priority (coming soon)
- Decision: Offline sync capabilities (coming soon)

---

## Phase 0 Reality Check

**The vision above is aspirational.** Here's what we're actually building:

### Current Implementation (Phase 0)

**Minimum hardware requirement:** Android smartphone with Asgaya app

**Why start here:**
- Cryptographic confirmation requires recipient device (security)
- Numeric code entry prevents merchant unilateral completion
- SMS-only flow has security vulnerabilities (merchant could fake)
- Need to validate core flow before expanding hardware support

**Still "minimal" because:**
- No BCH wallet needed (vs traditional crypto)
- No private key management (vs holding crypto)
- No exchange account (vs buying/selling crypto)
- Just install free app, enter codes

**Who this serves:**
- Recipients with basic Android phones (common in Venezuela)
- Smartphone penetration ~70% in target markets
- **Excludes:** ~30% without smartphones (acknowledged limitation)

---

### Phase 1 Enhancement: RFID Cards

**Next step:** Support recipients without smartphones via RFID cards

**How it works:**
- Recipient gets RFID card ($0.50-2 cost)
- Merchant helps provision card (merchant-assisted onboarding)
- Recipient taps card on merchant device (NFC) to confirm
- **No smartphone needed**

**See:** [RFID Card Recipients](concepts/rfid-card-recipients.md) for full specification

**Status:** Concept documented, not yet implemented

---

### Future Vision: Cardboard QR / Feature Phones

**The tiered model described above (Tiers 1-4) is the long-term vision.**

**Challenges to solve:**
- How to do cryptographic confirmation with no recipient device
- How to prevent merchant from completing without recipient present
- SMS-based solutions have security trade-offs

**Research needed:** Can cardboard QR work with strong security model?

**Timeline:** Post-Phase 1, requires security design work

---

## The Bottom Line

**Most crypto projects assume "everyone has a smartphone."**

**This assumption excludes billions of people.**

Asgaya assumes nothing. If you can borrow a smartphone from a merchant for 30 seconds to scan a piece of cardboard, you can receive money.

**After that, it's your choice:**
- Stay with cardboard (€0, works)
- Upgrade to RFID (€5, better)
- Upgrade to phone (€100, best)

**But access is never tied to what you own. Only to what you can borrow for 30 seconds.**

---

*Last updated: May 1, 2026*
*Core principle: "If it works for someone with nothing, it works for everyone. Design for the edge case, not the average."*
