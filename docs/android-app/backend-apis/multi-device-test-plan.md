# Multi-Device Systematic Test Plan - Asgaya Husk v0.1

**Purpose:** Systematic testing of Asgaya Husk across sender, recipient, and merchant roles using multiple devices.  
**Hardware:** Pixel 6a (sender), Motorola G06 (recipient/merchant), pichan (Electrum server)  
**Network:** All devices on same WiFi (192.168.1.x)  
**Blockchain:** Regtest on pichan (instant blocks, controlled test data)  

---

## Test Environment

### Devices

| Device | Role(s) | SIM | IP Address |
|--------|---------|-----|------------|
| **Pixel 6a** | Sender (Iris) | Yes | 192.168.1.100 |
| **Motorola G06** | Recipient (Elena), Merchant (Carlos) | Yes | 192.168.1.101 |
| **pichan** | Electrum server (BCH Node + Fulcrum) | SIM hat | 192.168.1.42 |
| **susopc** | Development, monitoring | No | 192.168.1.50 |

### Test Accounts

| Cash Account | Role | Device | Address |
|--------------|------|--------|---------|
| **Iris#100** | Sender | Pixel 6a | bchreg:qz... |
| **Elena#142** | Recipient | Moto G06 | bchreg:qr... |
| **Carlos#891** | Merchant | Moto G06 | bchreg:qq... |

---

## UI Mockups Reference

**All screen designs documented in:** [User Flows](../flows/)

**Sender Screens (8 total):**
1. Home (Entry Point)
2. Enter Recipient Cash Account
3. Enter Amount (with currency selector)
4. Confirm Order (payment method choice)
4.5. Select BCH Seller (bulletin board)
5. Payment Instructions (if buying from seller)
6. Tracking (waiting for claim)
7. Completion & Savings

**Recipient Screens (4 total):**
1. Notification (incoming covenant)
1.5. Claim Method Choice (BCH or cash)
2. Remittance Details
3. Show Bounty Code (if cash selected)
4. Success / Receipt

**Merchant Screens (4 total):**
1. Customer Lookup (dashboard)
2. Verify Bounty Code
3. Co-Sign Covenant
4. Success / Receipt

---

## Test Scenarios

### Scenario 1: Empty Bulletin Board (Edge Case)

**Purpose:** Test UI when no sellers are available.

**Setup:**
```bash
# On pichan
cd ~/asgaya-regtest/scripts
./regtest-reset.sh
# Don't seed any sellers
```

**Test Steps:**

| Step | Device | Action | Expected Result |
|------|--------|--------|-----------------|
| 1 | Pixel | Open Asgaya Husk | See home screen with balance |
| 2 | Pixel | Tap "Send BCH" | Go to Screen 2 |
| 3 | Pixel | Enter "Elena#142" | Resolve Cash Account successfully |
| 4 | Pixel | Tap Continue | Go to Screen 3 |
| 5 | Pixel | Enter amount €10 | See BCH equivalent |
| 6 | Pixel | Tap Continue | Go to Screen 4 |
| 7 | Pixel | Select "Buy BCH from Seller" | Go to Screen 4.5 |
| 8 | Pixel | See bulletin board | **Show: "No sellers available. Try again later."** |

**Pass Criteria:**
- ✅ Graceful empty state message
- ✅ No crash or blank screen
- ✅ "Back" button returns to Screen 4

---

### Scenario 2: Single Seller (Auto-Select)

**Purpose:** Test auto-selection when only one seller available.

**Setup:**
```bash
cd ~/asgaya-regtest/scripts
./regtest-reset.sh
./scenarios/02-one-seller.sh
```

**Test Steps:**

| Step | Device | Action | Expected Result |
|------|--------|--------|-----------------|
| 1 | Pixel | Navigate to Screen 4.5 (bulletin board) | See 1 seller |
| 2 | Pixel | (Auto-select should trigger) | **Skip to Screen 5 automatically** |
| 3 | Pixel | See payment instructions | Show Bizum details for the only seller |

**Pass Criteria:**
- ✅ Auto-select when `sellers.length === 1`
- ✅ No manual selection needed
- ✅ Clear indication: "Auto-selected: Seller#3421 (only available)"

---

### Scenario 3: Competitive Market (Selection UI)

**Purpose:** Test seller selection with multiple options.

**Setup:**
```bash
cd ~/asgaya-regtest/scripts
./regtest-reset.sh
./scenarios/03-competitive.sh
# Seeds 5 sellers with different fees, limits, payment methods
```

**Test Steps:**

| Step | Device | Action | Expected Result |
|------|--------|--------|-----------------|
| 1 | Pixel | Navigate to Screen 4.5 | See 5 sellers listed |
| 2 | Pixel | Verify seller cards | Each shows: limits, payment, fee, response time |
| 3 | Pixel | Check liveness indicators | All show 🟢 (online) |
| 4 | Pixel | Tap "Sort by: Fee" | Sellers reorder (lowest fee first) |
| 5 | Pixel | Tap "Filter: Bizum only" | Show only sellers accepting Bizum |
| 6 | Pixel | Tap a seller card | Expand to show details |
| 7 | Pixel | Tap "Select" | Go to Screen 5 with selected seller |

**Pass Criteria:**
- ✅ All 5 sellers display correctly
- ✅ Sorting works (fee, speed, limits)
- ✅ Filtering works (payment method)
- ✅ Selection navigates to Screen 5
- ✅ Selected seller details shown in Screen 5

---

### Scenario 4: Full Sender Flow (Send Own BCH)

**Purpose:** Test complete sender flow when user has BCH already.

**Setup:**
```bash
./regtest-reset.sh
./scenarios/04-full-flow.sh
# Pre-fund Iris#100 with 1 BCH
```

**Test Steps:**

| Step | Device | Screen | Action | Expected Result |
|------|--------|--------|--------|-----------------|
| 1 | Pixel | 1 | Open Husk | See balance: 1.0 BCH |
| 2 | Pixel | 1 | Tap "Send BCH" | → Screen 2 |
| 3 | Pixel | 2 | Enter "Elena#142" | Resolve successfully, show recipient name |
| 4 | Pixel | 2 | Tap Continue | → Screen 3 |
| 5 | Pixel | 3 | Select currency: VES | Show VES input field |
| 6 | Pixel | 3 | Enter 500,000 VES | Show BCH equivalent: ~0.0198 BCH |
| 7 | Pixel | 3 | Tap Continue | → Screen 4 |
| 8 | Pixel | 4 | Review details | Confirm recipient, amount correct |
| 9 | Pixel | 4 | Select "Send from My BCH" | **Skip Screens 4.5 & 5** → Screen 6 |
| 10 | Pixel | 6 | See tracking screen | "Waiting for Elena to claim..." |
| 11 | Pixel | 6 | (Wait for notification) | Auto-update when Elena claims |

**Pass Criteria:**
- ✅ Currency selector works (VES, EUR)
- ✅ BCH conversion displays correctly
- ✅ "Send from My BCH" skips seller selection
- ✅ Covenant created on blockchain (verify via RPC)
- ✅ Tracking screen shows pending state

---

### Scenario 5: Full Recipient Flow (BCH Claim)

**Purpose:** Test recipient claiming BCH (not cash).

**Prerequisites:** Scenario 4 completed (covenant exists).

**Test Steps:**

| Step | Device | Screen | Action | Expected Result |
|------|--------|--------|--------|-----------------|
| 1 | Moto | 1 | See notification | "Elena, you received 0.0198 BCH from Iris" |
| 2 | Moto | 1 | Tap notification | Open Husk → Screen 1.5 |
| 3 | Moto | 1.5 | See claim options | **BCH (FREE)** vs Cash (0.5% fee) |
| 4 | Moto | 1.5 | Tap "Claim as BCH" | → Screen 2 (Remittance Details) |
| 5 | Moto | 2 | Review details | Sender: Iris#100, Amount: 0.0198 BCH |
| 6 | Moto | 2 | Tap "Claim BCH" | Sign transaction |
| 7 | Moto | 4 | See success | "BCH claimed! Added to your balance." |
| 8 | Moto | Home | Check balance | Balance increased by 0.0198 BCH |

**Cross-Device Verification:**

| Step | Device | Action | Expected Result |
|------|--------|--------|-----------------|
| 9 | Pixel | Screen 6 (tracking) | Auto-update | "Complete! Elena claimed as BCH." |
| 10 | Pixel | Tap "View Details" | → Screen 7 |
| 11 | Pixel | Screen 7 | See completion | Total fee: 0% (no seller, no merchant) |

**Pass Criteria:**
- ✅ Notification arrives on Moto (via OP_RETURN or push)
- ✅ Claim method choice shown (BCH prioritized)
- ✅ BCH claim succeeds (covenant matures with recipient sig only)
- ✅ Balance updates on Moto
- ✅ Tracking screen updates on Pixel automatically

---

### Scenario 6: Full Recipient Flow (Cash Claim)

**Purpose:** Test recipient claiming cash via merchant.

**Prerequisites:** Scenario 4 completed (covenant exists).

**Test Steps:**

#### Part A: Recipient Selects Cash

| Step | Device | Screen | Action | Expected Result |
|------|--------|--------|--------|-----------------|
| 1 | Moto | 1.5 | See claim options | BCH (FREE) vs Cash (0.5% fee) |
| 2 | Moto | 1.5 | Tap "Cash Pickup" | → Screen 2 (Remittance Details) |
| 3 | Moto | 2 | Review details | Amount: €9.85 cash (0.5% fee deducted) |
| 4 | Moto | 2 | Tap "Find Merchant" | Show map with nearby merchants |
| 5 | Moto | 2 | Select merchant | Carlos#891, 500m away |
| 6 | Moto | 2 | Tap "Generate Code" | → Screen 3 (Show Bounty Code) |
| 7 | Moto | 3 | See 4-digit code | Display: **7284** (large font) |
| 8 | Moto | 3 | (Wait for merchant) | "Show this code to merchant" |

#### Part B: Merchant Co-Signs

| Step | Device | Screen | Action | Expected Result |
|------|--------|--------|--------|-----------------|
| 9 | Moto | Switch to merchant mode | Open merchant dashboard | → Merchant Screen 1 |
| 10 | Moto | M1 | See incoming covenant | "Pending: Elena, €9.85" |
| 11 | Moto | M1 | Tap covenant | → M2 (Verify Bounty Code) |
| 12 | Moto | M2 | Enter code: 7284 | Verify correct |
| 13 | Moto | M2 | (Hand cash to Elena in person) | Physical exchange |
| 14 | Moto | M2 | Tap "I gave cash, release BCH" | → M3 (Co-Sign Covenant) |
| 15 | Moto | M3 | Sign transaction | Covenant matures (recipient + merchant sigs) |
| 16 | Moto | M4 | See success | "BCH received: 0.0198 BCH, Fee earned: 0.0001 BCH" |

#### Part C: Recipient Confirms

| Step | Device | Screen | Action | Expected Result |
|------|--------|--------|--------|-----------------|
| 17 | Moto | Switch back to recipient | Return to Elena's view | → Screen 4 |
| 18 | Moto | R4 | See success | "Cash pickup complete! €9.85 received." |

**Cross-Device Verification:**

| Step | Device | Action | Expected Result |
|------|--------|--------|-----------------|
| 19 | Pixel | Screen 6 (tracking) | Auto-update | "Complete! Elena claimed as cash via Carlos#891." |
| 20 | Pixel | Screen 7 | See completion | Total fee: 1% (0.5% seller + 0.5% merchant) |

**Pass Criteria:**
- ✅ Recipient can switch between BCH and cash claims
- ✅ 4-digit bounty code displays correctly
- ✅ Merchant can verify code
- ✅ Covenant requires both recipient + merchant signatures
- ✅ Merchant receives fee (0.5%)
- ✅ Tracking updates on sender device

---

### Scenario 7: Network Failure Recovery

**Purpose:** Test graceful handling of network issues.

**Test Steps:**

| Step | Device | Action | Simulate | Expected Result |
|------|--------|--------|----------|-----------------|
| 1 | Pixel | Navigate to Screen 4.5 | Turn off WiFi | Show: "Connecting to Electrum..." |
| 2 | Pixel | (Wait 10 seconds) | Still no WiFi | Show: "Connection failed. Retry?" |
| 3 | Pixel | Tap "Retry" | Still no WiFi | Show error again |
| 4 | Pixel | (Turn WiFi back on) | Restore connection | Auto-connect, load sellers |
| 5 | Pixel | Continue flow | Normal | Proceed to Screen 5 |

**Pass Criteria:**
- ✅ No crash on network failure
- ✅ Clear error message ("Connection failed")
- ✅ Retry button works
- ✅ Auto-reconnect when network restored

---

### Scenario 8: Concurrent Transactions

**Purpose:** Test multiple senders/recipients simultaneously.

**Setup:**
```bash
# Create second recipient: Maria#555
./register-cashaccount.sh Maria 555
```

**Test Steps:**

| Time | Device | User | Action |
|------|--------|------|--------|
| T+0s | Pixel | Iris | Send 0.01 BCH to Elena#142 |
| T+5s | susopc | Bob | Send 0.02 BCH to Maria#555 (via CLI) |
| T+10s | Moto | Elena | Claim Iris's covenant (BCH claim) |
| T+15s | susopc | Maria | Claim Bob's covenant (cash claim) |

**Verify:**
- ✅ Both covenants exist independently
- ✅ Electrum returns correct UTXOs for each recipient
- ✅ No transaction conflicts
- ✅ Both tracking screens update correctly

---

## Test Checklist

### Pre-Test Setup
- [ ] Pichan running bitcoind (regtest)
- [ ] Pichan running Fulcrum (port 50002 accessible)
- [ ] Both phones connected to same WiFi
- [ ] Both phones have Husk APK installed
- [ ] susopc has monitoring tools (bitcoin-cli, electrum-cli)

### Per-Scenario Checklist
- [ ] Reset regtest (clean blockchain state)
- [ ] Seed appropriate test data (sellers, merchants, accounts)
- [ ] Verify seeding via RPC: `bitcoin-cli --regtest listunspent`
- [ ] Test all steps in sequence
- [ ] Verify blockchain state after each action
- [ ] Check for crashes, errors, UI glitches
- [ ] Document any issues in test log

### Post-Test Verification
- [ ] Check pichan logs for errors
- [ ] Verify no memory leaks (check RAM usage)
- [ ] Confirm all transactions confirmed on blockchain
- [ ] Export test results (screenshots, logs)

---

## Test Log Template

**Date:** ___________  
**Tester:** ___________  
**Scenario:** ___________  

| Step | Pass/Fail | Notes |
|------|-----------|-------|
| 1 | ☐ | |
| 2 | ☐ | |
| ... | ☐ | |

**Issues Found:**
1. 
2. 

**Screenshots:**
- 

**Blockchain State:**
```bash
bitcoin-cli --regtest listunspent
# Paste output
```

---

## Automated Testing (Future)

**Phase 1:** Manual testing (this document)  
**Phase 2:** Espresso UI tests (Android automation)  
**Phase 3:** Full E2E automation (Appium + bitcoin-cli scripting)  

**Example automated test:**
```javascript
// espresso/SenderFlowTest.kt
@Test
fun testFullSenderFlow_OwnBCH() {
  // Setup: Seed regtest with Elena#142
  
  onView(withId(R.id.btn_send_bch)).perform(click())
  onView(withId(R.id.input_recipient)).perform(typeText("Elena#142"))
  onView(withId(R.id.btn_continue)).perform(click())
  
  // ... rest of flow
  
  // Verify: Covenant exists on blockchain
  val covenants = electrumClient.getCovenants("Elena#142")
  assertEquals(1, covenants.size)
}
```

---

## Related Documents

- [Pichan Regtest Setup](./pichan-regtest-setup.md) - Server configuration
- [Phase 0 Progressive Decentralization](../../decisions/phase-0-progressive-decentralization.md) - Overall strategy
- [User Flows](../flows/) - UI mockups and flow documentation
- [With volatility buffer Bounty Contracts](../../concepts/bounty-contracts-with-volatility-buffer.md) - Covenant logic

---

*Test plan version: 1.0*  
*Hardware: Pixel 6a + Motorola G06 + Raspberry Pi 5*  
*Network: Local WiFi (192.168.1.x)*  
*Blockchain: BCH regtest with instant blocks*
