← [Back to Android App](android-app/README.md)

# Android App Flows

This section documents all user flows in the Asgaya Android app, organized by complexity and use case.

---

## Payment Flows (Priority Order)

### [BCH Payment Flows](android-app/flows/bch-payment-flows.md) — Build FIRST ✅

**Simplest use case:** Send BCH to any address using fiat (EUR).

**Use cases:**
- Tourist payments (pay BCH merchants with Bizum)
- Self-purchase (buy BCH for yourself)
- Fiat on-ramp (any BCH project can integrate)
- Merchant payments (if local BCH network exists)

**Why build first:**
- Already tested/working (`knowledge/code/smsbridge_loop.py`)
- Fewest screens (5 total)
- Fastest settlement (~30 seconds with BCH float)
- Proves core concept

**Participants:** Sender + Escrow + Merchant/Recipient (2-3 actors)

---

### [Remittance with Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md) — Build SECOND

**Core Asgaya innovation:** Cross-border remittance with merchant claim window.

**Use case:** María (Spain) sends €100 → Elena (Venezuela) claims cash at local merchant

**Why build second:**
- More complex (recipient notification, merchant selection, 24h claim window)
- More screens (7 sender + 6 recipient + 3 merchant = 16 total)
- Slower settlement (30-60 min for recipient to claim)
- Kickstarts merchant network (this is what drives adoption!)

**Participants:** Sender + Escrow + Recipient + Merchant + LP (4-5 actors)

---

## Participant-Specific Flows

### [Recipient Flows](android-app/flows/recipient-flows.md)

How recipients claim remittances at merchants.

**Screens:** 6 (notification → merchant map → claim → success)
**Key feature:** 24-hour claim window, merchant discovery, confirmation (simple or 2-of-2 signature)

---

### [Merchant Flows](android-app/flows/merchant-flows.md)

How merchants provide cash-out services.

**Screens:** 3 (check code → give cash → confirm)
**Key feature:** Earn from claims (1/3 or 1/2 of fee)

---

### [LP Flows](android-app/flows/lp-flows.md)

How liquidity providers enable instant settlements.

**Screens:** 4 (dashboard → earnings → leaderboard → settings)
**Key feature:** Earn from providing BCH liquidity
