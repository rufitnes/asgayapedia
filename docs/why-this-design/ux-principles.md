# UX Principles

**Purpose:** Document the user experience philosophy guiding Asgaya's design decisions.

**Status:** Production-proven (Phase 0 - August 2026)  
**Philosophy:** Manual first, automate strategically. User control over convenience.

---

## Core Principle: Manual > Automatic

**The guideline:** When choosing between manual user control and automatic background operations, default to manual.

**Why:**
1. **Predictability** - User knows exactly when network/blockchain operations happen
2. **Battery efficiency** - No background services draining power
3. **Privacy** - No unexpected network requests
4. **Simplicity** - Fewer moving parts, less to debug
5. **Trust** - User sees and controls every action

**This is not laziness.** Background automation is complex and can be built later. Manual control is the foundation that proves the core flow works.

---

## Principle 1: User Controls Timing

**Implementation:** All blockchain queries and transactions are user-initiated.

### Examples from Production

**Balance queries:**
```
User taps "🔄 Update Status" → App queries blockchain → Shows result
```

**NOT:**
```
App polls blockchain every 30s in background → Updates UI automatically
```

**Claim execution:**
```
User reviews parameters → Taps "💰 Claim" → Transaction executes
```

**NOT:**
```
App detects funded covenant → Auto-claims in background → User sees notification
```

**Why manual?**
- **Network usage** - User controls when app uses data (important on mobile/metered connections)
- **Battery drain** - Background polling consumes power continuously
- **Unexpected behavior** - Auto-claiming might happen at inconvenient time (low battery, poor signal)
- **User agency** - Recipient might want to wait (BCH price movement, timing preference)

### Trade-offs Accepted

**Convenience:** User must manually check covenant status

**Reliability:** No missed claims due to background service crashes, no battery drain, no permission issues

**For Phase 0:** Manual checking is acceptable. One covenant at a time. User expects to monitor.

**For scale:** If user testing shows manual checking is painful (merchants with 50 covenants/day), THEN consider background monitoring. Not before.

---

## Principle 2: Simplicity First (Phase 0 Philosophy)

**Implementation:** Minimalist feature set. Prove core flow before adding conveniences.

### What Phase 0 Includes

**Essential operations:**
- Create covenant (manual parameter entry)
- Fund covenant (self-funded flow)
- Check balance (manual button tap)
- Claim covenant (manual execution)
- Refund covenant (manual safety net)

**Essential infrastructure:**
- WebView bridge (Kotlin ↔ JavaScript)
- Wallet management (sender/recipient/seller matching)
- Parameter transport (copy-paste via Telegram)
- Connection management (TCP/WebSocket to Fulcrum)

**That's it.** No extras. No "nice to have" features.

### What Phase 0 Excludes

**Background services:**
- No automatic balance polling
- No covenant monitoring service
- No push notifications from blockchain events

**Convenience features:**
- No batch claiming (claim 10 covenants in one transaction)
- No auto-refund scheduling
- No QR code parameter sharing
- No contact/address book

**Advanced features:**
- No multi-wallet switching UI
- No covenant history search
- No analytics/charts
- No exchange rate alerts

**Why exclude these?**
1. **They're not required to prove the core mechanism works**
2. **Each feature adds complexity** (more code to debug, more surfaces for bugs)
3. **We don't know which will actually be useful yet** (assumptions ≠ validated pain points)
4. **Phase 0 is about proving viability, not polish**

### When to Add Features

**After Phase 0 proves:**
- ✅ Covenant creation works reliably
- ✅ Funding flow is stable
- ✅ Claim flow succeeds (August 10 - proven!)
- ✅ Refund flow works as safety net
- ✅ Connection management patterns documented

**Then observe:**
- What do users actually struggle with?
- What manual steps are genuinely painful?
- Where do errors cluster?

**Then automate strategically:**
- Proven pain points first
- Measure impact (did it actually help?)
- Keep simplicity where possible

---

## Principle 3: Automate Pain Points, Not Assumptions

**The trap:** "Users will find X annoying, let's automate it!"

**The method:** Ship manual version → Observe actual usage → Identify real friction → Automate proven pain points

### Example: Electrum Subscriptions

**The assumption:**
"Users won't want to manually tap 'Update Status' to check covenant balance. Let's implement Electrum subscriptions for real-time notifications!"

**The reality:**
- **Phase 0 use case:** One covenant at a time, user expects to monitor
- **Manual check frequency:** Maybe 2-3 times over covenant lifetime (after funding, before claim, after claim)
- **Subscription complexity:** Persistent WebSocket, background service, reconnection logic, battery impact
- **ROI:** High complexity for minimal UX improvement in Phase 0

**The decision:**
- ✅ Ship with manual "Update Status" button (Phase 0)
- ⏸️ Document Electrum subscriptions as future pattern
- 📊 Observe: Do users actually find manual checking painful?
- 🎯 If yes: Implement subscriptions for proven use case (e.g., seller funding notification)

**Result:** Simpler implementation, faster to production, can still add subscriptions later if needed.

### Example: 5-Second TCP Cooldown

**The assumption:**
"5 seconds is too slow! Users will hate waiting after checking balance!"

**The reality:**
- **Alternative:** Complex connection pooling, connection reuse, separate pools for TCP/WebSocket
- **Complexity cost:** Weeks of debugging, new failure modes, hard to troubleshoot
- **Reliability cost:** More surface area for bugs
- **Phase 0 context:** Single-device testing, one covenant at a time, user can wait 5 seconds

**The decision:**
- ✅ Accept 5-second cooldown (documented workaround)
- ✅ Prioritize reliability over speed
- ⏸️ Document better solutions for future
- 🎯 Optimize ONLY if user testing shows it's actually painful

**Result:** Shipped working implementation. Can optimize later if needed.

### Validation Framework

**Before automating, ask:**

1. **Have we observed this pain point in real usage?**
   - ❌ "I think users will find X annoying" → Not validated
   - ✅ "Three test users complained about X" → Validated

2. **What's the complexity cost?**
   - Background service + reconnection logic + battery optimization = High
   - Add a button = Low

3. **What's the failure mode?**
   - Background service crashes → User misses claim → Lost money
   - Manual button doesn't work → User sees error, retries

4. **Can we test the simple version first?**
   - Almost always yes!

5. **What's the actual usage pattern?**
   - Merchant with 50 covenants/day → Automation helps
   - Personal remittance 2x/month → Manual is fine

**Default:** Ship simple, observe real usage, automate proven pain points.

---

## Principle 4: Transparent Operations

**Implementation:** User sees what's happening. No hidden background work.

### Status Visibility

**Balance checks:**
```kotlin
// User sees:
"⏳ Checking balance..."
// Then:
"✅ Funded: 540000 sats"
// Or:
"✅ Claimed (balance: 0)"
```

**TCP cooldown (Phase 0):**
```kotlin
Log.d(TAG, "⏳ Waiting 5s for TCP connection cleanup...")
delay(5000)
Log.d(TAG, "✅ TCP connection cleanup complete")
```

User understands there's a delay and why.

### Transaction Details

**Claim covenant UI shows:**
- Covenant address
- Expected amount
- Recipient wallet address
- Seller wallet address (for buffer return)

**User confirms** before transaction executes.

**NOT hidden:** "Claiming..." → Done (user has no idea what just happened)

### Error Messages

**WebSocket connection fails:**
```
❌ Connection failed: ElectrumClient timeout
Try: Check Fulcrum node is running (192.168.1.100:60003)
```

**NOT:** "Error" (what error? what should I do?)

### Logging Philosophy

**In development:** Verbose logging to help debug

**In production:** Key events logged (connection, transaction broadcast, balance updates)

**User sees:** Status updates, not raw logs

**Principle:** User should never wonder "What is the app doing right now?"

---

## Principle 5: Accept UX Trade-offs for Reliability

**Core belief:** In early phases, reliability > convenience.

### Examples of Trade-offs

**5-second TCP cooldown:**
- ❌ UX: Visible pause between balance check and claim
- ✅ Reliability: Prevents WebSocket hangs (100% → 0%)
- **Verdict:** Accept UX pause, ship reliable version

**Manual balance updates:**
- ❌ UX: User must tap button to check status
- ✅ Reliability: No background service crashes, no battery drain, no permission issues
- **Verdict:** Manual is acceptable for Phase 0

**Copy-paste parameter transport (Telegram):**
- ❌ UX: Manual copy-paste, not seamless
- ✅ Reliability: Works immediately, no custom infrastructure needed
- **Verdict:** Ship with copy-paste, optimize later if needed

**Phase 0 priority ranking:**

1. **Correctness** - Does it work? (covenant math, buffer distribution)
2. **Reliability** - Does it work consistently? (connection management, error handling)
3. **Security** - Is it safe? (private key handling, transaction validation)
4. **Simplicity** - Can we understand and debug it? (minimal complexity)
5. **Performance** - Is it fast enough? (5-second delay acceptable)
6. **Convenience** - Is it pleasant to use? (nice to have, optimize later)

**This ranking is intentional.** Premature optimization of convenience before proving reliability is backwards.

---

## Principle 6: Progressive Enhancement

**Implementation:** Start with proven foundation, add features incrementally.

### Phase 0: Prove Core Flow

**Goal:** Demonstrate guaranteed-value BCH transfers work

**Features:**
- Self-funded sender flow (sender funds own covenant)
- Manual operations (user-initiated everything)
- Single covenant at a time
- Simple parameter transport (copy-paste)

**Success criteria:**
- ✅ Covenant created and funded
- ✅ Parameters shared to recipient
- ✅ Recipient claims successfully
- ✅ Buffer returns to seller (funder)
- ✅ Documented and reproducible

**Status:** ✅ Achieved August 10, 2026

### Phase 1: Merchant Cash-Out Flow (Next)

**Goal:** Enable seller-funded covenants (liquidity provision)

**New features:**
- Seller/funder liquidity provision
- Cosign path (merchant cash-out to fiat)
- Bulletin board for seller discovery
- Potentially: Seller funding notifications (Electrum subscriptions)

**Success criteria:**
- Merchant can request cash-out
- Seller provides liquidity (funds covenant)
- Merchant receives fiat equivalent
- Buyer claims BCH
- Documented and reproducible

**What NOT to add yet:**
- Batch processing
- Auto-refund scheduling
- Advanced analytics

**Why:** Prove seller-funded flow works before optimizing it.

### Phase 2+: Scale & Polish (Future)

**Goal:** Handle higher volume, improve UX based on real feedback

**Potential features (validated pain points only):**
- Batch claiming (if merchants have 50+ covenants/day)
- Background monitoring (if manual checks are proven painful)
- Advanced filtering/search (if covenant history gets unwieldy)
- QR code sharing (if copy-paste is proven problematic)

**Decision framework:**
1. What pain points did Phase 0/1 reveal?
2. What do actual users struggle with?
3. What automation provides measurable improvement?

**Not guessing. Observing.**

---

## Anti-Patterns to Avoid

**These violate Asgaya UX principles:**

### Anti-Pattern 1: Premature Automation

**Bad:**
```kotlin
// Phase 0 implementation
class CovenantMonitorService : Service() {
    // Polls blockchain every 30s
    // Auto-claims when funded
    // Sends push notifications
}
```

**Why bad:**
- Complex before core flow proven
- Battery drain before usage patterns known
- Auto-claiming removes user agency
- Failure modes not understood

**Good:**
```kotlin
// Phase 0 implementation
button.setOnClickListener {
    // User taps button
    checkBalance()
}
```

**Why good:**
- Simple, debuggable
- User controls timing
- No background complexity
- Can add automation later if needed

---

### Anti-Pattern 2: Hidden Operations

**Bad:**
```kotlin
// Silently queries blockchain every time fragment appears
override fun onResume() {
    lifecycleScope.launch {
        updateBalances() // No user feedback!
    }
}
```

**Why bad:**
- Unexpected network usage
- No user feedback
- Hard to debug (when did query happen?)
- User confused if slow

**Good:**
```kotlin
// User explicitly requests update
updateButton.setOnClickListener {
    statusText.text = "⏳ Checking balance..."
    lifecycleScope.launch {
        val balance = checkBalance()
        statusText.text = "✅ Balance: $balance sats"
    }
}
```

**Why good:**
- User initiated (knows network request happening)
- Clear feedback (loading → result)
- Easy to debug (click button → see result)
- Predictable behavior

---

### Anti-Pattern 3: Feature Creep Before Validation

**Bad:**
```
Phase 0 scope:
- Core covenant flow
- Batch claiming
- QR code sharing  
- Address book
- Analytics dashboard
- Auto-refund scheduling
- Exchange rate alerts
- Transaction history search
```

**Why bad:**
- 8 features before proving 1 works
- Each feature adds complexity
- Don't know which are actually useful
- Longer time to prove viability

**Good:**
```
Phase 0 scope:
- Core covenant flow (create, fund, claim, refund)

Phase 1 scope (after Phase 0 proven):
- Seller-funded flow

Phase 2 scope (after observing Phase 1 usage):
- Features that address observed pain points
```

**Why good:**
- Prove viability quickly
- Each phase builds on validated foundation
- Add features based on real need, not assumptions

---

### Anti-Pattern 4: Optimizing Before Measuring

**Bad:**
```kotlin
// Replace 5-second cooldown with connection pooling
class ConnectionPool {
    private val tcpConnections = mutableListOf<ElectrumClient>()
    private val wsConnections = mutableListOf<ElectrumWebSocket>()
    
    // 200 lines of pooling logic
    // Not tested in production yet!
}
```

**Why bad:**
- Optimizing before proving 5-second cooldown is actually painful
- Added complexity before measuring impact
- New failure modes (pool exhaustion, stale connections)
- Harder to debug

**Good:**
```kotlin
// Phase 0: Accept 5-second delay
delay(5000) // TCP cooldown - documented workaround

// After Phase 1: Measure actual pain
// - Do users complain about delay?
// - How often do they update balance?
// - Is delay noticeable in real usage?

// THEN decide: Is connection pooling worth complexity?
```

**Why good:**
- Simple version ships and works
- Optimization based on measured need
- Can A/B test if needed

---

## Design Process

**How to apply these principles when making UX decisions:**

### Step 1: What's the simplest version that works?

**Example:** Parameter sharing between devices

**Options:**
1. Manual copy-paste (simplest)
2. QR code scanning (medium)
3. NFC tap (complex)
4. Bluetooth pairing (most complex)

**Phase 0 choice:** Manual copy-paste via Telegram

**Why:** Works immediately, no custom UI needed, can test parameter format

### Step 2: What's the failure mode?

**Manual copy-paste:**
- User might paste wrong data → Validation catches it
- User might typo one character → Parsing fails with clear error

**QR code:**
- Camera permission required → Might be denied
- Poor lighting → Scan fails
- QR library adds dependency → More to debug

**For Phase 0:** Manual is safer (fewer failure modes)

### Step 3: What's the complexity cost?

**Manual:** Zero new code (use existing clipboard/Telegram)

**QR code:** QR generation library, camera integration, permission handling, scan UI

**For Phase 0:** Manual wins (lower cost)

### Step 4: Can we validate with simple version first?

**Yes!** Ship copy-paste, see if users struggle. If they do, THEN add QR codes.

**Result:** Ship simple, iterate based on feedback.

---

## Measuring UX Success (Phase 0)

**How do we know if UX principles are working?**

### Success Metrics

**1. Core flow completion rate**
- Can user complete create → fund → share → claim flow?
- Target: 100% (in assisted testing)

**2. Error recovery rate**
- When error occurs, can user understand and fix it?
- Target: User reads error message, takes corrective action

**3. Time to completion**
- Not optimizing for speed in Phase 0
- Tracking: How long does flow take? (for baseline)

**4. User confusion points**
- Where do users ask "What do I do now?"
- Track: Which steps need better UI feedback

**5. Reliability**
- Does operation succeed when user initiates it?
- Target: High reliability (connection patterns prevent hangs)

### What We're NOT Measuring (Phase 0)

**Convenience metrics:**
- "How many taps to claim?" - Don't care yet
- "How fast is auto-update?" - No auto-update
- "NPS score?" - Too early

**Why not:** These matter at scale, not for proving viability.

---

## Future UX Evolution

**Where this philosophy might change:**

### At Scale (Post Phase 0)

**If merchants process 50 covenants/day:**
- Manual checking becomes painful ✅ Valid pain point
- Background monitoring makes sense ✅ Automate proven pain
- Batch operations become useful ✅ Measured need

**If personal users send 2 remittances/month:**
- Manual checking is fine ✅ No change needed
- Background service is overkill ❌ Keep manual

**Different use cases may need different UX!**

### With Nostr Integration (Phase 1+)

**Nostr coordination layer enables:**
- Notifications when recipient claims (Nostr event, not blockchain polling)
- Parameter sharing via DM (still user-initiated, but smoother than copy-paste)
- Discovery (find sellers/recipients without bulletin board)

**Still manual-first:** User initiates Nostr DM, not automatic background syncing.

### Mobile-First Constraints

**Asgaya is mobile-first, which reinforces manual philosophy:**
- Battery life matters (background services drain power)
- Network costs matter (metered data, roaming)
- Permission fatigue (users deny background permissions)
- OS restrictions (Android kills background services aggressively)

**Mobile reality:** Manual, user-initiated operations are more reliable than background automation.

---

## Summary

**Asgaya UX principles in one sentence:**

**"Ship the simplest version that proves the mechanism works, observe real usage, then automate proven pain points strategically."**

**Core principles:**

1. **Manual > Automatic** - User controls timing, no background surprises
2. **Simplicity First** - Phase 0 is minimalist, add features incrementally  
3. **Automate Pain Points, Not Assumptions** - Observe before automating
4. **Transparent Operations** - User sees what's happening
5. **Accept UX Trade-offs for Reliability** - Correctness > Convenience in early phases
6. **Progressive Enhancement** - Build on validated foundation

**This is intentional.** These principles got us to production (August 10, 2026) with a working, reliable covenant flow.

**What's next:** Apply same principles to Phase 1 (merchant flow). Prove it works before optimizing it.

---

**Status:** Production-validated  
**Last Updated:** 2026-08-13  
**Evidence:** Phase 0 core flow working, documented, reproducible

---

## Related Documentation

**Implementation:**
- [Connection Management Patterns](../implementation/android-app/connection-management-patterns.md) - Manual updates vs subscriptions
- [End-to-End Claim Flow](../implementation/android-app/claim-flow-end-to-end.md) - User-initiated operations

**Design Constraints:**
- [Funder Principle](constraints/funder-principle.md) - Why seller address correctness matters
- [Time Oracle MTP Fallback](constraints/time-oracle-mtp-fallback-trustless-ux.md) - UX implications of time sources

**User Journeys:**
- [Sender Journey](../user-journeys/remittance/sender/README.md) - Manual flow from user perspective
- [Recipient Journey](../user-journeys/remittance/recipient/README.md) - Claim flow UX

---
