# Bank Notification Parsing Validation (Real-World Proof-of-Concept)

**Status:** Not Started  
**Priority:** 🔴 CRITICAL — Phase 0 Launch Blocker  
**Last Updated:** 2026-06-28  
**Contributors Welcome:** Yes

---

## What We Don't Know

**Can we reliably parse real bank notifications from actual Bizum/SEPA transactions in production conditions?**

We have theoretical validation (RS026 proves the API exists and banks can't block it), regex patterns from example notifications, but **zero real-world testing with actual bank apps.**

---

## Why It Matters

**80% of Asgaya's automation relies on notification parsing.** Without it:
- Passive sellers can't work (manual confirmation only)
- Bot can't detect payments automatically
- User experience degrades to SMS-level complexity

**This is the highest technical risk in Phase 0.**

---

## Current Hypothesis

NotificationListenerService can reliably extract payment details (amount, sender name, timestamp) from Spanish bank notifications with >99% accuracy.

**What we have:**
- ✅ RS026 research: API exists, technically feasible
- ✅ Regex patterns for 5 Spanish banks (Caja Rural, BBVA, Santander, Sabadell, ING)
- ✅ Example notification formats

**What we DON'T have:**
- ❌ Actual Android app that parses notifications
- ❌ Real Bizum transactions tested
- ❌ Proof regex works on production bank apps
- ❌ Validation that bank app updates don't break parsing

---

## Investigation Method

### Phase 1: Build Minimal Proof-of-Concept (Week 1)
1. Build minimal Android app with NotificationListenerService
2. Test with ONE bank (Caja Rural - primary test bank)
3. Send 10 real €1 Bizum transactions
4. Verify app correctly parses: amount, sender name, timestamp
5. Document actual notification format vs. expected

### Phase 2: Multi-Bank Validation (Week 2)
6. Test with 3 additional banks (BBVA, Santander, Sabadell)
7. 5 transactions per bank (20 total)
8. Update regex patterns based on real formats
9. Document edge cases (special characters in names, decimal formats)

### Phase 3: Failure Scenarios (Week 3)
10. Test bank app updates (if notification format changes)
11. Test with airplane mode → back online (delayed notifications)
12. Test with app killed by Android (notification arrives while stopped)
13. Test with low battery mode
14. Document failure modes and fallback strategies

---

## Success Criterion

**Minimum viable validation:**
- ✅ Android app successfully parses 50+ real Bizum notifications
- ✅ 3+ banks tested (Caja Rural required, 2 others)
- ✅ >95% parsing success rate
- ✅ Documented edge cases and fallback plan
- ✅ Known failure modes documented

**Phase 0 launch criterion:**
- App exists, tested with real money
- Proven to work with at least one bank
- Clear documentation for pioneers to test other banks

---

## Phase 0 Trial Integration

**Critical metrics to log:**
- Notification arrival time vs. actual payment time (delay measurement)
- Parsing success rate (how many notifications successfully extracted all fields)
- Format changes detected (bank app updates that break parsing)
- Fallback to manual confirmation rate

**Real-world validation approach:**
- Phase 0 sellers run the app on their personal phones
- Real Bizum transactions from Phase 0 senders
- Weekly parsing success reports
- Immediate notification if parsing fails

---

## Contributor Guidance

**Skills needed:** Android development (Kotlin/Java), regex, basic QA testing  
**Estimated effort:** 
- PoC app: 4-6 hours
- Testing with real bank: 2-3 hours
- Multi-bank validation: 4-8 hours

**How to start:**
1. Read [RS026: Android Notifications](../../research/summaries/RS026-android-notifications-summary.md)
2. Set up Android Studio + test device (Android 10+)
3. Implement NotificationListenerService (code examples in RS026)
4. Send yourself €1 via Bizum (Caja Rural if possible)
5. Verify parsed output matches expected format
6. Document findings in GitHub issue or email rufitnes@proton.me

**Blocker status:** No other Phase 0 work should proceed until this is validated. This is the foundation.

---

## Related Documents

- [RS026: Android Notifications Architecture](../../research/summaries/RS026-android-notifications-summary.md) — Theoretical validation
- [Notification Bot](../../the-mechanism/notification-bot/README.md) — Architecture relying on this
- [Implementation: Android App](../../implementation/android-app/notification-bot.md) — Technical specs

---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
