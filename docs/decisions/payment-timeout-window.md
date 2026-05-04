# Decision: Payment Timeout Window - 10 Minutes

**Decision Date:** April 2026
**Status:** Implemented
**Related Requirement:** [Permissionless](core-architecture/why-permissionless.md) ([Error Mitigation](core-architecture/error-mitigation.md)), [Cheaper Than Legacy](core-architecture/why-cheaper-than-legacy.md) (Volatility Protection)

---

## The Goal (Architectural Ideal)

**Automated payment from Asgaya app** - sender initiates payment without leaving the app.

**Why we wanted this:**
- Seamless user experience (no app switching)
- Instant payment confirmation (no waiting for sender to complete payment manually)
- Zero uncertainty (payment guaranteed once initiated)

**Ideal flow:**
1. Sender taps "Send €100" in Asgaya app (t=0)
2. **Asgaya app automatically initiates Bizum payment** via banking API (t=1s)
3. Escrow receives notification (t=2s)
4. Escrow confirms funds → notifies recipient (t=3s)
5. **Total time: <5 seconds**

**Why this is the ideal:**
- Eliminates sender errors (wrong amount, wrong recipient)
- Eliminates timeout issues (payment happens instantly)
- Perfect user experience (one-tap payment)

---

## The Constraint (Reality Check)

### Constraint 0: Cannot Automate Payment (Terms of Service)

**The fundamental constraint:** We cannot automate Bizum payments from the Asgaya app.

**Why not:**
- **Violates banking terms of service** - Taking control of a user's banking app to initiate payments would breach ToS for virtually all banks
- **Security risk** - Automating banking app interactions requires access to banking credentials (unacceptable security model)
- **Legal liability** - Unauthorized automation could expose users and Asgaya to legal consequences
- **Should not, not cannot** - Technically possible in some edge cases, but ethically/legally wrong

**Implication:**
- **Sender must manually initiate payment** in their banking app (outside Asgaya)
- This introduces uncertainty: Did sender actually send it? When?
- This is where the timeout becomes necessary

**Future consideration:**
- Some banks may offer official APIs for payment initiation (Open Banking, PSD2 in EU)
- When/if available in supported corridors, we can reduce timeout significantly
- **For now:** Assume 99%+ of payments require manual initiation

---

### Constraint 1: Notification Delays

**Real-world observation:**
- Banking app notifications arrive **anywhere from seconds to 10 minutes after payment**
- Delay varies by:
  - Time of day (peak hours slower)
  - Mobile carrier (network congestion)
  - Bank processing time (not consistent)
  - App vs push notification delivery

**Testing data (from hundreds of Bizum transfers):**
- Fastest observed: A few seconds
- Slowest observed: ~8 minutes
- Median: ~3 minutes
- 95th percentile: ~7 minutes

**Note:** Most banking apps use push notifications, not SMS. This simplifies implementation but doesn't eliminate delays.

---

### Constraint 2: Sender Behavior Variability

**Discovery:** Senders take different amounts of time to complete the payment.

**Real-world scenario:**
- Some senders have Bizum ready: 30 seconds
- Others need to set up Bizum first: 5 minutes
- Some get distracted mid-flow: Could take longer

**Challenge:**
- Can't rely on sender confirming "I sent it" (what if they forget?)
- Timer must start when instructions are shown
- Must accommodate both fast and slow senders

**Result:** Need generous timeout to avoid false failures.

---

### Constraint 3: Multiple Payment Sources

**Discovery:** Escrows receive payments from many sources, not just Asgaya.

**Real-world scenario:**
- Escrow might receive 10 Bizum payments/day
- Only some are Asgaya-related
- Others are: personal transfers, refunds, business payments

**Challenge:**
- Must match notification against pending transfers
- Requires fuzzy matching (encoding can vary)
- Need time buffer for matching algorithm

---

## Alternatives Considered

### Option 1: 30-Second Timeout (Optimistic)

**Assumption:** Sender completes payment and notification arrives within 30 seconds.

**Pros:**
- Best user experience
- Fastest possible flow

**Cons:**
- Fails for real-world sender behavior (many take >30s just to open bank app)
- Notification delays mean most transfers would timeout
- Forces constant manual intervention

**Verdict:** ❌ Unrealistic based on testing

---

### Option 2: 5-Minute Timeout (Moderate)

**Assumption:** Most senders complete payment and notification arrives within 5 minutes.

**Pros:**
- Covers median case (~3 min notification + reasonable sender time)
- Acceptable user experience
- Reduces failures significantly

**Cons:**
- Still fails for slower senders or delayed notifications (~20% of transfers)
- Forces fallback handling too often
- Not enough buffer for edge cases

**Verdict:** ⚠️ Close, but insufficient buffer

---

### Option 3: 10-Minute Timeout (Pragmatic)

**Assumption:** Nearly all senders complete payment and notifications arrive within 10 minutes.

**Pros:**
- Covers 95%+ of real-world scenarios
- Accommodates slow senders + notification delays
- Rare timeouts (only extreme outliers)
- Gives buffer for fuzzy matching algorithms

**Cons:**
- Slightly slower user experience for fast senders (but they still complete in ~3 min)
- Longer uncertainty window

**Verdict:** ✅ Best balance of reliability vs. speed

---

### Option 4: 20-Minute Timeout (Over-Engineered)

**Assumption:** Cover 99.9% of cases.

**Pros:**
- Almost never times out
- Maximum reliability

**Cons:**
- Unnecessarily long for 95% of transfers
- Poor user experience (perceived as slow)
- Doesn't add meaningful value over 10 minutes

**Verdict:** ❌ Diminishing returns

---

## The Decision

**Use 10-minute timeout window for escrow to receive and confirm payment.**

**Rationale:**
1. **Covers 95%+ of real-world scenarios** (fast + slow senders, typical notification delays)
2. **Balances reliability vs. speed** (rare timeouts, acceptable wait time)
3. **No volatility risk** (escrow holds EUR during this window, BCH purchased AFTER recipient cashes out)
4. **Simple user experience** (no "confirm I sent it" button - system handles automatically)

**Implementation:**
- **Timer starts:** When sender receives payment instructions
- **Timer ends:** When escrow parses notification from sender OR when 10 minutes elapses
- **If notification received:** Escrow confirms funds → notifies recipient
- **If timeout (10 min):** System asks sender "Did you send the payment?"
  - If yes → Manual verification
  - If no → "Do you still want to proceed?"

---

## Implementation Details

### Sender Experience

1. Sender receives payment instructions in Asgaya app (includes escrow Bizum details, amount €101, concept field with recipient phone)
2. **Timer starts automatically: "Waiting for payment... (0:00 / 10:00)"**
3. Sender leaves app, opens bank app, sends Bizum
4. Sender can close bank app and wait (no need to return to Asgaya app)
5. Typical wait: 2-5 minutes
6. Notification: "Payment received! Notifying recipient..."

**If timeout:**
- "We haven't detected your payment yet. Did you send it?"
- Options: "Yes, I sent it" (manual review) / "No, give me more time" / "Cancel"

---

### Escrow Experience

1. Generates payment instructions for sender
2. **Starts monitoring for notifications** (checks every 15-30 seconds)
3. Receives Bizum notification from bank app
4. **Fuzzy matches** notification against pending Asgaya transfers:
   - Strips non-numeric characters from concept field
   - Matches phone number
   - Validates amount (€101)
5. If match found → Confirms funds received
6. **Holds EUR** (does NOT buy BCH yet)
7. Notifies recipient: "Funds ready for cash-out"

---

### Recipient Experience

1. Receives notification: "€100 transfer ready - go to merchant to cash out"
2. Goes to participating merchant
3. Shows transaction ID to merchant
4. Merchant enters code in Asgaya merchant app
5. Merchant hands cash (VES) to recipient
6. Both confirm cash-out in app

---

### Settlement (After Cash-Out Confirmed)

**If merchant selected instant settlement (3 participants):**
1. LP sends €100 fiat to merchant
2. Merchant app parses fiat settlement notification from LP
3. **Escrow buys €101 worth of BCH** (e.g., 0.1007374 BCH lands in hot wallet)
4. Calculate fee: 0.1007374 - 0.1000000 = 0.0007374 BCH
5. Split 3 ways: 0.0007374 / 3 = 0.0002458 BCH each
6. Escrow sends to LP: 0.1002458 BCH (€100 principal + 0.0002458 BCH fee)
7. Escrow sends to merchant: 0.0002458 BCH (fee only, merchant already has fiat from LP)
8. Escrow keeps: 0.0002458 BCH

**Volatility window:** Seconds (from BCH purchase to LP receipt)

**If merchant holds BCH (2 participants):**
1. **Escrow buys €101 worth of BCH** (e.g., 0.1007374 BCH)
2. Calculate fee: 0.0007374 BCH
3. Split 2 ways: 0.0007374 / 2 = 0.0003687 BCH each
4. Escrow sends to merchant: 0.1003687 BCH
5. Escrow keeps: 0.0003687 BCH

**Volatility window:** Seconds

---

## Trade-offs Accepted

### Lost: Instant Confirmation
- Cannot achieve <1 minute sender-to-escrow confirmation
- Must accommodate real-world sender behavior + notification delays
- 2-10 minute typical wait

### Gained: Reliability
- 95%+ automatic confirmation (no manual intervention)
- Rare timeouts (<5%)
- Simple user experience (no "confirm I sent it" button needed)

### Volatility Risk: Near Zero
- **10-minute timeout is NOT a volatility window**
- Escrow holds EUR during timeout (zero volatility)
- BCH only purchased AFTER recipient cashes out (settlement in seconds)
- **Total volatility exposure: ~5-30 seconds** (from BCH purchase to LP/merchant receipt)

---

## Fuzzy Matching Strategy

**Challenge:** Banking app notifications can have minor encoding variations.

**Solution:** Fuzzy matching on concept field (phone number).

**Algorithm:**
```
1. Strip all non-numeric characters from concept field
2. Compare numeric-only phone numbers
3. If exact match → Accept
4. If no match → Try Levenshtein distance ≤2
5. If still no match → Flag for manual review
```

**Example:**
```
Expected: 584121234567
Received: +58 412 123 4567 (with spaces and +)
→ Normalize both: 584121234567 vs 584121234567
→ Exact match → Accept
```

**Trade-off:** Minimal false positive risk (mitigated by amount validation - must match €101)

---

## Validation

**How we verify this decision:**
- ✅ 95%+ of transfers confirmed within 10 minutes (hundreds of Bizum tested)
- ✅ Timeout rate <5%
- ✅ No false positives observed (fuzzy matching + amount validation works)
- ⏳ Pending: Full corridor testing (EUR→VES with real cash-outs)

**Metrics to track:**
- Average confirmation time (target: 3-5 minutes)
- Timeout rate (target: <5%)
- Manual intervention rate (target: <5%)

---

## Future Considerations

### If Notification Delays Improve
- Monitor median confirmation time over 3+ months
- If consistently <2 minutes → consider reducing to 5-minute timeout
- **Do NOT reduce prematurely** (reliability > speed)

### If Notification Delays Worsen
- Monitor timeout rate
- If >10% timeouts for 1+ month → extend to 15 minutes
- Communicate with user community

### Alternative Notification Methods
- **Bank APIs:** If banks offer webhook APIs, could reduce to 1-2 minute timeout
- **Direct integrations:** If bank allows direct Bizum API access, eliminate notification dependency
- **For now:** Banking app notifications are most reliable method

---

## Related Decisions

- [Bizum Concept Field](decisions/bizum-concept-field.md) — Why phone numbers (enables simple fuzzy matching)
- [Two-Step Settlement Timing](decisions/two-step-settlement-timing.md) — Why escrow holds EUR during timeout (zero volatility)
- [Fee Splitting Model](decisions/fee-splitting-model.md) — How fees are calculated after BCH lands in wallet

---

## Lessons Learned

### 1. Real-World Latency Always Wins
- Paper specs say "instant" but reality says "minutes"
- Design for 95th percentile, not median
- 10-minute buffer is prudent, not wasteful

### 2. Don't Ask Users to Confirm
- "Did you send it?" button adds friction
- Better to auto-detect and only ask on timeout
- Users appreciate "it just works" experience

### 3. Volatility Mitigation Enables Longer Timeouts
- Because escrow holds EUR (not BCH) during timeout, 10 minutes is completely acceptable
- Actual volatility window is seconds (settlement phase), not minutes
- **Two-step settlement architecture enables this pragmatic decision**

### 4. Simple > Perfect
- Could implement complex retry logic, exponential backoffs, etc.
- 10-minute timeout with simple fallback is more reliable
- Users prefer predictable behavior over optimization

---

## References

- **Implementation:** `/docs/android-app/notification-listener/bizum-android.md`
- **User Flow:** `/docs/android-app/flows/sender-flows.md`
- **Testing Data:** Hundreds of real Bizum transfers (internally tested)

---

*Decision made: April 2026*
*Validated: 95%+ success rate over hundreds of transfers*
*Status: Active, proven design*
