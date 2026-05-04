# Asgaya Implementation Roadmap

**Last Updated:** May 3, 2026  
**Status:** Pre-Launch Planning  
**Target Corridor:** EUR → VES (Spain → Venezuela)

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

**Goal:** Complete ONE end-to-end remittance manually

### What We Build

**Sender App (Spain):**
- Create transaction screen
- Shows Bizum payment instructions
- Amount input (EUR)
- Recipient phone number input
- Generate claim code

**Recipient App (Venezuela):**
- View remittance notification (via WhatsApp link)
- Show claim code: REM-89234
- **List** of merchants (no map)
  - Name, phone number, address
  - "Call Merchant" button

**Merchant App (Venezuela):**
- Enter 4-digit code screen
- Shows amount in VES
- "Confirm Cash Given" button

**Escrow Backend (Suso's computer):**
- Manually check Sabadell app for Bizum payment
- Mark transaction as paid (admin endpoint)
- Buy BCH on Kraken (manual or script)
- Send BCH to merchant wallet
- Generate claim codes

### What We Skip (For Now)

- ❌ No auto-parsing of Bizum notifications
- ❌ No auto-parsing of PagoMóvil notifications
- ❌ No LP bounty system (direct settlement only)
- ❌ No maps
- ❌ No ratings/reviews
- ❌ No OP_RETURN notifications
- ❌ No BCH signature auth (use JWT)
- ❌ No merchant hours/limits
- ❌ No recipient confirmation (merchant confirmation enough)

### Success Criteria

- [ ] Sender sends €100 via Bizum
- [ ] Recipient receives WhatsApp notification with code
- [ ] Recipient calls merchant, visits location
- [ ] Merchant enters code, sees VES 113,850
- [ ] Merchant gives cash, taps confirm
- [ ] Merchant receives BCH reward
- [ ] **Total time:** <2 hours end-to-end

**If this works 3 times → Move to Phase 1**

---

## Phase 1: Automation Layer 1

**Goal:** Reduce manual escrow intervention

### Features to Add

**Priority 1: Bizum Auto-Detection**
- Deploy `smsbridge_loop.py` on escrow device
- Auto-parse Bizum notifications from Sabadell
- Auto-mark transactions as paid
- **Why first:** Proven reliable, eliminates manual checking

**Priority 2: Two-Sided Confirmation**
- Add recipient confirmation screen
- Require both merchant + recipient to confirm
- **Why second:** Prevents merchant fraud, adds security

**Priority 3: Automatic BCH Purchase**
- Kraken API integration
- Auto-buy BCH when payment confirmed
- Auto-send to merchant wallet
- **Why third:** Eliminates manual Kraken trading

### What We Still Skip

- ❌ No LP bounty system yet (direct settlement only)
- ❌ No merchant auto-confirmation of LP payments (no LP yet)
- ❌ No maps (still just list)
- ❌ No OP_RETURN notifications

### Success Criteria

- [ ] 10 successful transactions with auto-Bizum detection
- [ ] Zero false positives on Bizum parsing
- [ ] Both confirmations working reliably
- [ ] Kraken API purchases working smoothly
- [ ] **Total escrow manual intervention:** <5 minutes per transaction

**If 10/10 succeed → Move to Phase 2**

---

## Phase 2: Instant Settlement

**Goal:** Add LP bounty system for merchants who want fiat

### Features to Add

**Priority 1: Merchant Instant Settlement Toggle**
- Add setting to merchant profile
- `instant_settlement_enabled: true/false`
- Default: false (direct BCH settlement)

**Priority 2: LP Dashboard & Bounty System**
- LP dashboard showing available liquidity
- Bounty notifications (push notifications via Firebase/APNs for MVP)
- LP accepts bounty → sends PagoMóvil to merchant
- First-come-first-served competition
- Automatic liquidity deduction

**Priority 3: Merchant Manual Confirmation**
- Merchant checks bank account
- Merchant taps "Confirm Fiat Received from LP"
- (No auto-SMS parsing yet)

**Priority 4: Dual Settlement Paths**
- Path A: instant_settlement=false → BCH to merchant
- Path B: instant_settlement=true → LP bounty → BCH to LP

### What We Still Skip

- ❌ No PagoMóvil auto-parsing (manual merchant confirmation)
- ❌ No OP_RETURN notifications (using push notifications)
- ❌ No maps

### Success Criteria

- [ ] 5 successful direct settlements (instant_settlement=false)
- [ ] 5 successful LP settlements (instant_settlement=true)
- [ ] LP liquidity tracking works correctly
- [ ] LP earns BCH reward as expected
- [ ] Merchant receives fiat within 2 minutes of claim
- [ ] **Zero failed settlements**

**If 10/10 succeed → Move to Phase 3**

---

## Phase 3: Full Automation

**Goal:** Minimize manual confirmations, add resilience

### Features to Add

**Priority 1: PagoMóvil Auto-Parsing**
- Deploy NotificationListener on merchant device
- Parse PagoMóvil SMS from Venezuelan banks
- Auto-confirm fiat received from LP
- **Reuse proven `smsbridge_loop.py` approach**

**Priority 2: OP_RETURN Notifications**
- Replace push notifications with OP_RETURN
- LP monitors BCH address for bounties
- More censorship-resistant
- Keep push notifications as backup

**Priority 3: Map-Based Merchant Discovery**
- Add map view to recipient app
- Location-based merchant search
- Distance calculations
- Only add when 5+ merchants available

### What We Still Skip (Post-Beta)

- ❌ BCH signature authentication
- ❌ Merchant ratings/reviews
- ❌ Multiple payment methods
- ❌ Multiple corridors
- ❌ Leaderboard

### Success Criteria

- [ ] 20 successful fully-automated settlements
- [ ] 95%+ auto-parsing success rate (PagoMóvil)
- [ ] OP_RETURN notifications working reliably
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
- Merchant reliability tiers (if instant settlement failures occur)
- Enhanced error handling (based on real failure modes)

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
- [ ] 2+ active LPs
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
- Advanced LP features (pools, reputation)
- Merchant onboarding improvements
- Sender experience polish
- Analytics dashboard

**Technical:**
- Performance optimizations
- Security hardening
- Decentralization improvements
- Smart contract exploration (if needed)

---

## Risk Mitigation

### Known Risks & Mitigations

**Risk 1: Bizum auto-parsing fails**
- **Mitigation:** Manual fallback (admin endpoint)
- **Monitoring:** Alert if no auto-confirmation in 5 min

**Risk 2: PagoMóvil format changes**
- **Mitigation:** Manual merchant confirmation always available
- **Monitoring:** Track parse failure rate, update regex

**Risk 3: LP doesn't send fiat**
- **Mitigation:** Timeout + flag LP, restore liquidity
- **Monitoring:** Track LP timeout rate

**Risk 4: Merchant/recipient collusion**
- **Mitigation:** Two-sided confirmation required
- **Monitoring:** Flag suspicious patterns

**Risk 5: Exchange rate volatility**
- **Mitigation:** Escrow absorbs variance (for MVP)
- **Monitoring:** Track margin variance

**Risk 6: Kraken API downtime**
- **Mitigation:** Queue purchases, retry with backoff
- **Monitoring:** Alert on failed purchases

---

## Success Metrics

### Phase 0 (Husk)
- **Target:** 3 successful transactions

### Phase 1 (Automation L1)
- **Target:** 10 successful auto-confirmed transactions

### Phase 2 (Instant Settlement)
- **Target:** 10 successful LP settlements

### Phase 3 (Full Automation)
- **Target:** 20 successful fully-automated transactions

### Phase 4 (Public Beta)
- **Target:** 50+ transactions, 10+ users

### Long-term
- **Target:** 500+ transactions across 2-3 corridors
- **Users:** 100+ senders, 50+ recipients, 20+ merchants, 5+ LPs

---

## Technical Debt Decisions

**Things we're intentionally punting:**

### Defer to Post-Beta
- **BCH signature auth:** JWT is simpler, proven
- **OP_RETURN notifications:** Push notifications work fine initially
- **Merchant ratings:** Not needed with 1-2 trusted merchants
- **Multiple payment methods:** One per corridor is enough
- **Merchant hours/limits:** Keep it simple, trust network

### Defer to Post-Launch
- **Smart contracts:** Not needed for MVP architecture
- **Decentralized escrow:** Centralized works for beta
- **Advanced LP features:** Basic bounty system enough
- **Multi-currency wallets:** BCH-only is fine

### Never Build (Unless Proven Necessary)
- **Complex LP selection algorithms:** First-come-first-served works
- **Partial settlements:** One LP per settlement is simpler
- **In-app chat:** Users can use WhatsApp/Telegram
- **KYC/compliance:** Start permissionless, add if legally required

---

## Open Questions for External Review

**We'd love feedback on:**

1. **Security model:** Is two-sided confirmation enough? Or do we need BCH signatures from day one?

2. **LP trust model:** Is first-come-first-served fair? Or should we add reputation/priority?

3. **Merchant reliability:** Should we track individual merchants from day one? Or wait for issues?

4. **Privacy vs convenience:** JWT auth vs BCH signatures? Push notifications vs OP_RETURN?

5. **Escrow centralization:** Is centralized escrow acceptable for beta? When should we decentralize?

6. **Payment method expansion:** Which payment methods should we prioritize after PagoMóvil?

7. **Corridor priority:** EUR→VES first, or EUR→ARS? Why?

8. **Feature priorities:** What did we miss? What should be higher/lower priority?

---

## Phase Summary

| Phase | Key Features | Success Metric |
|-------|--------------|----------------|
| **Phase 0: Husk** | Manual everything | 3 successful txns |
| **Phase 1: Auto L1** | Bizum auto, 2-sided confirm | 10 successful txns |
| **Phase 2: Instant** | LP bounty system | 10 LP settlements |
| **Phase 3: Full Auto** | PagoMóvil auto, OP_RETURN | 20 automated txns |
| **Phase 4: Public Beta** | External feedback | 50+ txns, 10+ users |
| **Phase 5: Scale** | Multi-corridor expansion | 500+ txns |

---

## How to Contribute Feedback

**We're seeking input on:**
- 💡 **Architecture:** Is the phased approach sound?
- 🔒 **Security:** What are we missing?
- 🎯 **Priorities:** Are we building the right things in the right order?
- 🌍 **Corridors:** Which remittance corridors matter most?
- ⚡ **Features:** What's critical vs nice-to-have?

**Public review period:** 2-3 weeks before Phase 0 launch

**Where to provide feedback:** [TBD - GitHub issues, forum, etc.]

---

## Related Documents

- **[Android App Flows](android-app/flows/README.md)** - Screen-by-screen user flows
- **[Backend APIs](android-app/backend-apis/README.md)** - API specifications
- **[Notification Listener](android-app/notification-listener/README.md)** - Auto-parsing architecture
- **[LP Flows](android-app/flows/lp-flows.md)** - Liquidity provider experience
- **[Merchant Flows](android-app/flows/merchant-flows.md)** - Merchant experience

---

**Next Step:** Public documentation review → External feedback → Refine roadmap → Build Asgaya Husk

*"Start simple, validate, iterate. Ship the husk, grow the tree."*
