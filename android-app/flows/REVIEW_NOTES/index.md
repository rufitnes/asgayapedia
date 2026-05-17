# Flow Review Notes - 2026-05-16

## Stale Files Identified

### 1. `bch-payment-flows.md` - STALE (Escrow Architecture) → MERGED
**Issues:**
- Line 23: References "escrow + merchant/recipient"
- Line 213: "Escrow phone" for Bizum payment
- Lines 436-470: "BCH Float Settlement" section (escrow maintains float, Kraken purchases)
- Uses old escrow/LP architecture throughout

**Decision:** 
- **MERGED into sender flow Screen 1**
- Screen 1 now has two buttons:
  - "Send to Asgaya User" (covenant-based) ← Main remittance flow
  - "Pay with Bitcoin Cash" (standard BCH) ← Replaces bch-payment-flows.md
- This file can be **ARCHIVED**

**Resolution:**
- Standard BCH payments handled by "Pay with Bitcoin Cash" button (wallet functionality)
- Not an Asgaya flow (just standard BCH wallet-to-wallet transfer)
- No covenant, no seller, no fee - pure BCH transaction
- Merchant doesn't need Asgaya app (just accepts BCH)

---

### 2. `README.md` - STALE (Participant Names)
**Issues:**
- Line 27: "Sender + Escrow + Merchant/Recipient (2-3 actors)"
- Line 43: "Sender + Escrow + Recipient + Merchant + LP (4-5 actors)"
- References "Escrow" and "LP" (liquidity providers)

**Decision:**
- Update to covenant architecture terminology:
  - "Escrow" → "BCH Seller"
  - Remove "LP" (no longer exists in covenant model)

---

### 3. `lp-flows.md` - STALE (LP Architecture)
**Issues:**
- Entire flow based on LP (Liquidity Provider) role
- Uses escrow architecture (line 13: "LPs send fiat to merchant, receive BCH + reward from escrow")
- LPs don't exist in covenant model

**Valid concepts to salvage:**
- **Gamification**: Leaderboard rankings, first-to-accept bounties, competitive rewards
- **BCH Buyers**: People who buy BCH from merchants (merchants accumulate BCH → sell for fiat)
- **Circular economy**: Merchants cash out BCH → BCH buyers purchase it → buyers become senders
- **Bounty system**: Push notifications, countdown timers, "first come first served"

**Decision:**
- Rewrite as "BCH Buyer Flows" with gamification elements
- BCH Buyers = people who give fiat to merchants, receive BCH
- Enables merchant circular economy (merchants don't need to exchange BCH, just sell locally)
- Gamified experience: leaderboards, bounties, rewards for fast response

---

## Architectural Clarification

### Asgaya Flows (Covenant-Based)
**All Asgaya transactions use covenants:**
- Sender creates covenant (24-hour claim window)
- Recipient chooses: BCH (free) or Cash (0.5% merchant fee)
- If cash chosen: Merchant co-signs, gets BCH
- If BCH chosen: Recipient claims directly (no merchant needed)

**Payment method choice (Screen 4):**
- Send from My BCH Wallet (FREE)
- Buy BCH from Seller (0.5% fee via Bizum)

### Non-Asgaya BCH Payments (Standard Wallet)
**Standard BCH transactions (no covenant, no Asgaya infrastructure):**
- User has BCH in wallet
- Scans merchant's BCH address (non-Asgaya merchant)
- Sends BCH directly (standard transaction)
- No fee (just network fee ~$0.01)
- Instant settlement
- Merchant doesn't need Asgaya app

**This is wallet functionality, not an Asgaya flow.**

---

## Flows Reviewed ✓

1. **remittance-merchant-cash-out.md** (Sender Flow) - ✅ COVENANT-BASED (UPDATED)
   - 8 screens (added Screen 3.5: Pending Covenant Error)
   - **Screen 1:** UPDATED - Two distinct buttons:
     - "Send to Asgaya User" (covenant-based, recipient chooses BCH/cash)
     - "Pay with Bitcoin Cash" (standard BCH payment, direct to merchant)
   - **Screen 3:** Added validation check for pending covenants (one per sender→recipient pair)
   - **Screen 3.5:** NEW - Error screen if pending covenant exists
   - **Screen 4:** Payment method choice (My BCH vs Buy from Seller)
   - **Screen 5:** Changed Bizum concept field from "REM-89234" to "Elena#142" (Cash Account)
   - Creates covenant for all transactions
   - Recipient chooses claim method later
   
   **Key improvements:**
   - **Screen 1 clarity:** Two buttons clearly separate Asgaya network (covenant) vs non-Asgaya (standard BCH)
   - **Merges bch-payment-flows.md:** "Pay with Bitcoin Cash" button handles non-Asgaya merchants
   - **Cash Account in Bizum concept field:** Human-readable, clean 1:1 matching, prevents duplicates
   - **Fail-safe design:** Even if user picks wrong button, BCH ends up at recipient

2. **recipient-flows.md** (Recipient Flow) - ✅ COVENANT-BASED
   - 6 screens
   - Screen 1.5: Claim method choice (BCH free vs Cash 0.5%)
   - Positioned BCH as default/recommended

3. **merchant-flows.md** (Merchant Flow) - ✅ COVENANT-BASED
   - 4 screens
   - Direct Cash Account lookup (Elena#142)
   - No bulletin browsing (simplified)

---

## Pending Review

- [ ] **bch-payment-flows.md** - Archive/delete (escrow-based)
- [ ] **README.md** - Update participant names (Escrow → BCH Seller, remove LP)
- [ ] **lp-flows.md** - Check if still relevant (LPs removed in covenant model?)
- [ ] Any other flows referencing escrow/LP architecture

---

## Next Steps

1. Continue systematic screen review
2. Update flows/README.md with covenant terminology
3. Archive bch-payment-flows.md to archive/ folder
4. Check all other flow files for escrow/LP references
5. Ensure wallet functionality includes "standard BCH payments" (non-Asgaya)

---

*Review started: 2026-05-16*
*Reviewers: Coordination + User (suso)*
