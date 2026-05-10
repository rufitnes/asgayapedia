# Asgaya Implementation Roadmap

**Last Updated:** May 10, 2026  
**Status:** Pre-Launch Planning (Covenant Architecture)  
**Target Corridor:** EUR → VES (Spain → Venezuela)

**Architecture:** Covenant-based (no custody, no intermediation)

---

## Philosophy: Incremental Validation

**Approach:**
1. Start with **Asgaya Husk** (absolute minimum)
2. Test end-to-end with real money
3. Add **one feature** at a time
4. Test again after each addition
5. Iterate until fully automated

**Why incremental:**
- ✅ Prove core concept works before adding complexity
- ✅ Identify real pain points (not imagined ones)
- ✅ Build confidence with each successful transaction
- ✅ Safer with real money on the line

---

## Phase 0: Asgaya Husk

**Goal:** Complete ONE end-to-end remittance with covenant manually

### What We Build

**Sender App (Spain):**
- Create covenant screen
- Shows Bizum payment instructions (to BCH seller)
- Amount input (EUR)
- Recipient phone number input
- Generate bounty code (last 4 digits)

**BCH Seller Backend (Suso's computer):**
- Manually check Sabadell app for Bizum payment (from sender)
- Manually post BCH collateral to covenant (~107% of EUR value)
- Manual covenant creation on BCH blockchain
- Generate bounty code (covenant ID)

**Recipient App (Venezuela):**
- View covenant notification (via WhatsApp link)
- Show bounty code: 8923 (4 digits)
- **List** of merchants (no map)
  - Name, phone number, address
  - "Call Merchant" button

**Merchant App (Venezuela):**
- Enter 4-digit bounty code screen
- Shows VES amount to sell + BCH to receive
- Shows "Hand VES & Co-Sign" button
- Manual co-signing (both merchant and recipient tap button)

**Covenant Settlement:**
- Manual verification both signatures present
- Covenant matures → BCH distributed to merchant and seller
- Seller keeps surplus after merchant paid (~0.5% fee + hedge profit)

### What We Skip (For Now)

- ❌ No auto-parsing of Bizum notifications
- ❌ No auto-covenant creation (manual BCH collateral posting)
- ❌ No bulletin board (send bounty code via WhatsApp)
- ❌ No maps
- ❌ No timeout cascade automation (manual refund if needed)
- ❌ No BCH signature auth (use JWT)
- ❌ No merchant hours/limits

### Success Criteria

- [ ] Sender sends €100 via Bizum to BCH seller
- [ ] BCH seller posts ~0.107 BCH collateral to covenant
- [ ] Recipient receives WhatsApp notification with bounty code
- [ ] Recipient calls merchant, visits location
- [ ] Merchant enters code, sees VES 500,000 and 0.0995 BCH
- [ ] Merchant gives VES cash, both co-sign covenant
- [ ] Covenant matures → Merchant receives ~0.0995 BCH
- [ ] BCH seller keeps surplus (~0.007 BCH) + €100 fiat
- [ ] **Total time:** <2 hours end-to-end

**If this works 3 times → Move to Phase 1**

---

## Phase 1: Automation Layer 1

**Goal:** Reduce manual BCH seller intervention

### Features to Add

**Priority 1: Bizum Auto-Detection**
- Deploy `smsbridge_loop.py` on seller device
- Auto-parse Bizum notifications from Sabadell
- Auto-mark covenant as "funded by sender"
- **Why first:** Proven reliable, eliminates manual checking

**Priority 2: Automatic Covenant Creation**
- Script to post BCH collateral to covenant
- Auto-calculate overcollateralization (7% buffer)
- Auto-generate bounty code (covenant ID)
- **Why second:** Eliminates manual BCH posting

**Priority 3: Co-Signing Verification**
- Verify both merchant and recipient signatures
- Auto-detect covenant maturity
- Track covenant state (pending → partially_signed → mature)
- **Why third:** Eliminates manual signature checking

### What We Still Skip

- ❌ No BCH buyer bulletin yet (merchant holds BCH or sells P2P)
- ❌ No bulletin board (still send bounty codes via WhatsApp)
- ❌ No maps (still just list)
- ❌ No automated timeout refunds (manual split refund)

### Success Criteria

- [ ] 10 successful transactions with auto-Bizum detection
- [ ] Zero false positives on Bizum parsing
- [ ] Auto-covenant creation working reliably
- [ ] Both co-signing verified automatically
- [ ] **Total BCH seller manual intervention:** <5 minutes per transaction

**If 10/10 succeed → Move to Phase 2**

---

## Phase 2: Bulletin Board & Optional BCH Buyer

**Goal:** Add bulletin board for merchant discovery, optional BCH buyer market

### Features to Add

**Priority 1: Public Bulletin Board**
- All active covenants visible on bulletin board
- Merchants see: "Wants: 500,000 VES | You get: 0.0995 BCH"
- Recipient provides bounty code to claim specific covenant
- First merchant to enter valid code wins

**Priority 2: Merchant Decides to Hold or Sell**
- After receiving BCH from covenant, merchant chooses:
  - Option A: Hold BCH (recommended, earn full spread)
  - Option B: Sell to BCH buyer (instant fiat, lose some spread)
- Setting stored in merchant profile (default: hold BCH)

**Priority 3: BCH Buyer Bulletin (Optional)**
- Separate bulletin board for BCH buyers
- Shows: "Seller offers: 0.0995 BCH | Wants: 500,000 VES"
- **Uses same covenant mechanism** (merchant = seller, BCH buyer = recipient)
- Merchant posts BCH → BCH buyer sends Pagomóvil → Both co-sign
- Circular economy enabled (BCH ↔ VES in both directions)

**Priority 4: Notification System**
- Push notifications for new bounties (Firebase/APNs for MVP)
- OP_RETURN notifications (Phase 3)
- Recipient notified when covenant created

### What We Still Skip

- ❌ No Pagomóvil auto-parsing (manual buyer confirmation)
- ❌ No OP_RETURN notifications yet (using push notifications)
- ❌ No maps (bulletin board sorted by distance, but no visual map)

### Success Criteria

- [ ] 10 successful remittances with bulletin board
- [ ] Merchants successfully find and claim bounties
- [ ] 5 merchants hold BCH (full spread earned)
- [ ] 5 merchants sell to BCH buyer (instant fiat received)
- [ ] BCH buyer bulletin working (circular economy)
- [ ] **Zero failed covenant settlements**

**If 15/15 succeed → Move to Phase 3**

---

## Phase 3: Full Automation

**Goal:** Minimize manual confirmations, add resilience

### Features to Add

**Priority 1: Pagomóvil Auto-Parsing (for BCH Buyers)**
- Deploy NotificationListener on BCH buyer device
- Parse Pagomóvil SMS from Venezuelan banks
- Auto-confirm fiat received from merchant (seller)
- **Reuse proven `smsbridge_loop.py` approach**

**Priority 2: OP_RETURN Notifications**
- Replace push notifications with OP_RETURN
- Merchants monitor BCH address for bounties
- More censorship-resistant
- Keep push notifications as backup

**Priority 3: Automated Timeout Cascade**
- 5-minute Bizum timeout (auto-cancel if sender doesn't pay)
- 24-hour claim timeout (auto-split refund if unclaimed)
  - Merchant portion → Sender's refund address
  - Seller fee → BCH seller (earned for service)
- Auto-handle covenant expiration

**Priority 4: Map-Based Merchant Discovery**
- Add map view to recipient app
- Location-based bounty search
- Distance calculations
- Only add when 5+ merchants available

### What We Still Skip (Post-Beta)

- ❌ BCH signature authentication (JWT works fine)
- ❌ Merchant ratings/reviews (trust-based initially)
- ❌ Multiple payment methods (Bizum/Pagomóvil enough)
- ❌ Multiple corridors (EUR→VES only)

### Success Criteria

- [ ] 20 successful fully-automated settlements
- [ ] 95%+ auto-parsing success rate (Pagomóvil for BCH buyers)
- [ ] OP_RETURN notifications working reliably
- [ ] Timeout cascade working (3 test cases: Bizum timeout, claim timeout, normal)
- [ ] Map view tested with 5+ merchants
- [ ] **Total manual intervention:** <1 minute per transaction (only edge cases)

**If 20/20 succeed → Public Beta Launch**

---

## Phase 4: Public Beta

**Goal:** Scale to 10-20 testers, gather feedback

### Features to Add Based on Feedback

**Potential additions (priority TBD):**
- Merchant ratings/reviews (if merchant quality varies)
- BCH signature authentication (if JWT proves problematic)
- Multiple payment methods (if users request specific methods)
- Second corridor EUR→ARS (if Venezuela corridor stable)
- Merchant reliability tiers (if covenant failures occur)
- Enhanced error handling (based on real failure modes)
- Advanced covenant features (multi-sig, longer timeouts, etc.)

### Public Documentation Review

**Before public beta:**
1. Publish all documentation publicly
2. Open for external review and feedback
3. Accountability period (2-3 weeks)
4. Incorporate outside input
5. Refine roadmap based on real-world feedback

### Success Criteria

- [ ] 50+ successful transactions
- [ ] 10+ active users (senders + recipients)
- [ ] 5+ active merchants
- [ ] 2+ active BCH sellers
- [ ] 2+ active BCH buyers (optional)
- [ ] External feedback incorporated
- [ ] Clear feature priorities based on actual usage

**If metrics hit → Scale & Expand**

---

## Phase 5: Scale & Multi-Corridor

**Goal:** Expand beyond EUR-VES, optimize for growth

### Potential Expansions

**Geographic:**
- EUR → ARS (Argentina)
- EUR → COP (Colombia)  
- EUR → HNL (Honduras)
- USD → VES (USA → Venezuela)

**Feature:**
- Mobile app improvements based on feedback
- Advanced BCH seller features (multi-covenant, batch posting)
- Merchant onboarding improvements
- Sender experience polish
- Analytics dashboard

**Technical:**
- Performance optimizations
- Security hardening (covenant review, timeout testing)
- Decentralization improvements
- Multi-sig covenants (if needed for trust)

---

## Risk Mitigation

### Known Risks & Mitigations

**Risk 1: Bizum auto-parsing fails**
- **Mitigation:** Manual fallback (admin endpoint)
- **Monitoring:** Alert if no auto-confirmation in 5 min

**Risk 2: BCH price drops >7% during covenant wait**
- **Mitigation:** Overcollateralization buffer (seller absorbs)
- **Monitoring:** Track volatility, adjust buffer if needed
- **Seller hedge:** Seller receives €100 fiat before price moves (94-97% exposure reduction)

**Risk 3: Merchant doesn't co-sign (Elena's theft risk)**
- **Mitigation:** Clear warnings, 5-minute timeout
- **Monitoring:** Track merchant refusal rate, flag bad actors

**Risk 4: Elena doesn't co-sign (Merchant's fraud risk)**
- **Mitigation:** Elena won't sign without cash (self-enforcing)
- **Monitoring:** Dispute resolution if both claim opposite

**Risk 5: Covenant expires (24h timeout)**
- **Mitigation:** Split refund (merchant → sender, seller keeps fee)
- **Monitoring:** Track expiration rate, remind recipients

**Risk 6: BCH buyer doesn't send fiat (for circular economy)**
- **Mitigation:** Same covenant timeout mechanism
- **Monitoring:** Track BCH buyer reliability

---

## Success Metrics

### Phase 0 (Husk)
- **Target:** 3 successful covenant settlements

### Phase 1 (Automation L1)
- **Target:** 10 successful auto-confirmed covenants

### Phase 2 (Bulletin Board)
- **Target:** 15 successful transactions (10 hold BCH, 5 sell to buyer)

### Phase 3 (Full Automation)
- **Target:** 20 successful fully-automated covenants

### Phase 4 (Public Beta)
- **Target:** 50+ transactions, 10+ users

### Long-term
- **Target:** 500+ transactions across 2-3 corridors
- **Users:** 100+ senders, 50+ recipients, 20+ merchants, 5+ BCH sellers, 5+ BCH buyers

---

## Technical Debt Decisions

**Things we're intentionally punting:**

### Defer to Post-Beta
- **BCH signature auth:** JWT is simpler, proven
- **OP_RETURN notifications:** Push notifications work fine initially
- **Merchant ratings:** Not needed with 1-2 trusted merchants
- **Multiple payment methods:** Bizum/Pagomóvil enough
- **Merchant hours/limits:** Keep it simple, trust network

### Defer to Post-Launch
- **Multi-sig covenants:** Simple 2-of-2 co-signing works for beta
- **Advanced timeout logic:** 24h is enough for MVP
- **Covenant pool optimization:** Individual covenants simpler
- **Multi-currency support:** BCH-only is fine

### Never Build (Unless Proven Necessary)
- **Complex matching algorithms:** First-come-first-served works
- **Partial settlements:** One covenant per remittance simpler
- **In-app chat:** Users can use WhatsApp/Telegram
- **KYC/compliance:** Start permissionless, add if legally required

---

## Open Questions for External Review

**We'd love feedback on:**

1. **Covenant security:** Is 7% overcollateralization enough? Or should we increase buffer?

2. **BCH seller trust:** Is permissionless posting safe? Or should we whitelist sellers initially?

3. **Merchant reliability:** Should we track individual merchants from day one? Or wait for issues?

4. **Privacy vs convenience:** JWT auth vs BCH signatures? Push notifications vs OP_RETURN?

5. **Timeout cascade:** Is 24-hour claim window too long/short? Should split refund ratio change?

6. **BCH buyer bulletin:** Should this be MVP or post-beta? Is circular economy important early?

7. **Corridor priority:** EUR→VES first, or EUR→ARS? Why?

8. **Feature priorities:** What did we miss? What should be higher/lower priority?

---

## Phase Summary

| Phase | Key Features | Success Metric |
|-------|--------------|----------------|
| **Phase 0: Husk** | Manual covenant, Bizum to seller | 3 successful covenants |
| **Phase 1: Auto L1** | Auto Bizum, auto covenant creation | 10 successful covenants |
| **Phase 2: Bulletin** | Public bulletin, BCH buyer market | 15 transactions |
| **Phase 3: Full Auto** | Timeout cascade, OP_RETURN | 20 automated covenants |
| **Phase 4: Public Beta** | External feedback | 50+ txns, 10+ users |
| **Phase 5: Scale** | Multi-corridor expansion | 500+ txns |

---

## How to Contribute Feedback

**We're seeking input on:**
- 💡 **Architecture:** Is the covenant-based approach sound?
- 🔒 **Security:** What covenant risks are we missing?
- 🎯 **Priorities:** Are we building the right things in the right order?
- 🌍 **Corridors:** Which remittance corridors matter most?
- ⚡ **Features:** What's critical vs nice-to-have?

**Public review period:** 2-3 weeks before Phase 0 launch

**Where to provide feedback:** [TBD - GitHub issues, forum, etc.]

---

## Related Documents

- **[Android App Flows](android-app/flows/README.md)** - Screen-by-screen user flows
- **[Overcollateralized Bounty Contracts](concepts/overcollateralized-bounty-contracts.md)** - Covenant specification
- **[BCH Sellers](concepts/bch-sellers.md)** - Seller role and hedge mechanism
- **[Merchant Flows](android-app/flows/merchant-flows.md)** - Merchant experience
- **[How Exchange Rates Work](decisions/how-exchange-rates-work.md)** - EUR-denominated covenants

---

**Next Step:** Public documentation review → External feedback → Refine roadmap → Build Asgaya Husk (Covenant Edition)

*"Start simple, validate, iterate. Ship the husk, grow the tree."*
