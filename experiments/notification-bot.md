# The Notification Bot: Automation That Makes Asgaya Scale

**Set it and forget it.** Post your listing once. The bot handles everything else—24/7.

---

## What It Is

The notification bot is a background process running on the seller's device. It continuously monitors three data streams:

1. **Nostr messages** — incoming payment requests from senders
2. **Bank notifications** — incoming fiat payments via Bizum, SEPA, etc.
3. **Blockchain state** — covenant status and expiration times

When a sender pays, the bot automatically funds the covenant, signs it, and broadcasts it to the BCH network. The seller never touches the app.

**Without the bot:** Manual checking, 5–10 transactions/day max, seller must be awake and attentive.  
**With the bot:** Sub‑second response, 100+ transactions/day, zero cognitive load.

---

## Three Core Functions

### 1. Nostr Message Handler

Listens on 3–5 Nostr relays for messages addressed to the seller's public key. Decrypts with the seller's private key. Validates the covenant exists on‑chain and the amount is within the seller's limits. Generates a unique payment reference using the recipient's Cash Account. Replies with encrypted bank details in under one second.

### 2. Bank Notification Listener

Uses the Android Notification Listener Service to intercept banking app notifications. Extracts payment amount, sender info, and the reference code. Matches the reference to a pending covenant. Verifies the amount (within a small tolerance). Triggers covenant funding.

**Key insight:** The bank notification is the payment proof. No API scraping, no login credentials stored, no polling required. The bot only reads notification summaries, which contain non‑sensitive data like "You received €100 from María, ref: Elena#142."

**Compatibility:** Works with any online banking app that notifies the customer within minutes.

### 3. Covenant Manager

Queries Electrum for covenant state, creates funding transactions (locking the seller's BCH per the covenant terms), signs with pre‑authorised keys limited to covenant UTXOs only, and broadcasts to the BCH network. Monitors for confirmation and reclaims if the covenant expires unfunded.

---

## Security Model

**What the bot can access:**
- Read‑only: wallet balance, covenant state, bank notification summaries, Nostr messages.
- Write (limited): sign covenant funding transactions, send Nostr payment responses, write local logs.

**What the bot CANNOT access:**
- The recovery phrase (never stored).
- Full wallet private keys (only covenant‑specific derived keys).
- Bank login credentials (never requested or stored).
- Arbitrary transaction signing (limited to covenant UTXOs).

**If the bot is compromised:** The attacker can read recent notification summaries and attempt to fund unpaid covenants (losing BCH), but cannot drain the wallet, redirect covenants, fake bank notifications, or access the bank account. Loss is limited to covenants in progress—typically 1–3 at a time, maximum ~€300 exposure.

**Key storage:** The Nostr private key is encrypted via Android Keystore. Covenant signing keys are derived from the wallet but segregated (HD path `m/44'/145'/0'/2/`). The main wallet keys remain separate.

---

## Seller Experience

**One‑time setup (2–3 minutes):** Enable the bot, grant notification access and background execution, select the banking app to monitor, test with a €2 self‑payment.

**Daily operation (zero effort):** The seller checks earnings and buys BCH again to fund more covenants when convenient. The bot processes transactions while the seller works, sleeps, or ignores the phone. Optional push notifications ("Covenant funded: +€0.50 earned") can be silenced.

**When something goes wrong:**

| Scenario | Bot Response | Seller Impact |
|----------|-------------|---------------|
| Bot offline (phone dead) | No Nostr response within 2 min | Sender picks different seller; no loss |
| Payment amount mismatch | Bot detects, does NOT fund | Manual resolution needed (rare) |
| Bank notification delayed | Bot funds when notification arrives | 5‑minute delay for sender; acceptable |
| False positive (phantom notification) | Bot attempts fund, finds no covenant | Logged error; no financial loss |

---

## Why the Bot Enables Scale

Without automation, sellers are constrained by attention: 8–12 hours of availability, 5–10 transactions per day, high fees needed to earn a decent hourly rate, limited to one city. With the bot, a single seller can serve an entire country, 24/7, processing 100+ transactions daily. The required seller count drops by 10×, and response times improve by 300×.

**A sender at 2:00 AM finds three bots awake and responsive.** She picks the cheapest, pays, and the covenant is funded in under a minute. Without bots, she'd wait until morning or pay Western Union €5.

---

## How It Connects

- **Wallet:** The bot has limited signing authority (covenant keys only).
- **Bulletin Board:** The bot posts and updates the seller's listing.
- **Nostr:** The bot listens for requests and sends payment details.
- **Bank notifications:** The bot detects payments and triggers covenant funding.

**Complete automated flow:**

1. **Bulletin board:** Listing posted (one‑time, manual)
2. **Nostr:** Bot listens, responds with payment details (automated, <1 s)
3. **Bank:** Sender pays via Bizum (manual, sender's side)
4. **Bot:** Detects notification, creates and signs funding tx (automated)
5. **Blockchain:** Transaction confirmed (automated)
6. **Seller:** Checks earnings when convenient (manual)

Steps 2–5 are fully automated. The seller only touches steps 1 and 6.

---

## Technical Essentials

**Android:** Foreground service with a persistent notification, partial wake lock, WiFi‑preferred with mobile data fallback. Battery impact: ~5–10 % per day.

**Nostr:** rust‑nostr via JNI, 3–5 public relays with exponential backoff reconnection. Messages use NIP‑04 encryption. A 30‑second keepalive detects dead connections.

**Bank parsing:** Pattern matching on notification text (e.g., "Has recibido 100,00 € de María García. Concepto: Elena#142"). Extracts amount and reference. Multiple regex patterns per bank handle format variations. If all patterns fail, the bot alerts the seller for manual verification.

**Covenant matching:** Resolves the Cash Account on‑chain, queries Electrum for unfunded covenants where recipient matches and amount is within tolerance, creates the funding transaction (107 % of face value), signs with the covenant‑specific key, and broadcasts.

---

## Phase 0 Scope

**Ready:** Nostr message handler, bank notification parser (Spanish banks), covenant manager, multi‑relay failover, reconciliation logs, limited key permissions, error handling.

**Phase 1+:** VPS deployment option, multi‑currency support, dispute mediation interface, advanced accounting exports, concurrent multi‑corridor support.

---

## Key Takeaways

1. **Automation is the killer feature.** Without it, Asgaya doesn't scale.
2. **Set and forget.** Post a listing once; the bot earns passive income 24/7.
3. **Secure by design.** Limited key permissions; separate from the main wallet.
4. **Bank notification = payment proof.** No API scraping, no credentials stored.
5. **10× reduction in required sellers.** The bot makes liquidity accessible to anyone with capital.

The bot transforms Asgaya from a manual P2P trading platform into a passive income generator backed by real remittance demand. That's the point.

---

**Next:** [Buyers and Sellers](buyers-and-sellers.md) — The user perspective  
**Related:** [Bulletin Board](bulletin-board.md) — Where listings are posted  
**Related:** [Nostr](nostr.md) — How coordination happens  
**Related:** [Wallet](wallet.md) — Where keys are stored
