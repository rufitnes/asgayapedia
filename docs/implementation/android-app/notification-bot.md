# Notification Bot Component

**Purpose:** Detect bank payment notifications, extract Cash Account from reference, match to covenant, auto-fund with BCH

**Complexity:** High - Android notification interception, regex parsing, payment matching, auto-funding

---

## Overview

The Notification Bot is what makes passive selling possible:
- **Isabel (passive seller):** Has active listing, goes about her day
- **María pays Isabel:** Bizum to +34612345678, reference "Elena#142"
- **Isabel's phone:** Detects bank notification, extracts "Elena#142"
- **Auto-match:** Finds María's covenant for Elena#142
- **Auto-fund:** Locks BCH from Isabel's wallet

**Phase 0:** Auto-fund only if wallet has sufficient BCH (show notification if insufficient)
**Phase 1+ extension:** Auto-buy from exchange if wallet balance low (see Optional Extensions section)

**Phase 0 approach:** Android Notification Listener Service (intercepts bank app notifications)

**Why push notifications, not SMS?** Banks are migrating from SMS to push notifications because:
- SMS costs banks ~€0.01-0.05 per message (telecom fees)
- Push notifications are free (internet-based)
- Result: Most modern banking apps use push notifications only

**Legacy SMS support:** Some users (grandfathered accounts) still receive SMS for Bizum payments. This is increasingly rare as banks phase out SMS to reduce costs. The `smsbridge_loop.py` prototype demonstrated SMS parsing works, but Notification Listener Service is the primary approach for Phase 0.

---

## How It Works (End-to-End)

### Lifecycle

```
1. María creates covenant for Elena#142 (€100 BCH)
   ↓ [Covenant broadcast to bulletin board]

2. Isabel has active listing ("Sell €100-500 EUR")
   ↓ [Notification Bot running in background]

3. María sends Bizum to Isabel: +34612345678, ref "Elena#142"
   ↓ [Bizum app sends notification: "Received €100.50 from María"]

4. Isabel's Notification Bot intercepts notification
   ↓ [Parse: amount=€100.50, reference="Elena#142"]

5. Bot queries Electrum: "Find covenant for Elena#142"
   ↓ [Finds María's unfunded covenant]

6. Bot checks Isabel's wallet: Has 0.0234 BCH? (€107 at current rate)
   ↓ [Yes: Fund covenant. No: Notify Isabel to top up wallet]

7. Bot funds covenant with €107 BCH (if wallet has sufficient balance)
   ↓ [Broadcast funding transaction]

8. Elena gets notified (Electrum detects funded covenant)
   ↓ [Elena claims €100 BCH, €7 buffer returns to Isabel later]
```

**All automatic for Isabel:** She just receives fiat, BCH locked without touching phone.

---

## Notification Detection

### Android Notification Listener Service

**What:** Android API that intercepts app notifications

**Setup:**
```
AndroidManifest.xml:
<service android:name=".NotificationListener"
         android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
  <intent-filter>
    <action android:name="android.notification.action.NOTIFICATION_LISTENER_SERVICE" />
  </intent-filter>
</service>
```

**Pseudocode:**
```
class NotificationListener extends NotificationListenerService {
  function onNotificationPosted(notification):
    // Check if notification is from bank app
    app_package = notification.packageName
    
    if app_package not in SUPPORTED_BANK_APPS:
      return  // Ignore non-bank notifications
    
    // Extract notification text
    title = notification.extras.get("android.title")
    text = notification.extras.get("android.text")
    
    full_text = title + " " + text
    
    // Parse payment details
    payment = parsePaymentNotification(full_text, app_package)
    
    if payment:
      handlePaymentReceived(payment)
}
```

**Supported bank apps (Phase 0):**
- `com.bbva.bbvacontigo` (BBVA - Bizum)
- `es.lacaixa.mobile.android.newwapicon` (CaixaBank - Bizum)
- `es.openbank.mobile` (Openbank - Bizum)
- **Add more as needed**

**User permission required:** User must enable notification access in Android settings (one-time setup)

---

### Parsing Bizum Notifications (Spain)

**Example notification:**
```
Title: "Bizum recibido"
Text: "Has recibido 100,50 € de María García. Concepto: Elena#142"
```

**Note:** This example is based on typical Bizum SMS format. Actual push notification format needs validation with real banking apps. Test data available in `knowledge/intelligence/raw_intelligence/` (historical SMS from PoC testing on Pichan/Raspberry Pi). Push notification format may differ slightly from SMS format.

**Regex pattern:**
```
function parseBizumNotification(text):
  // Pattern: "recibido <amount> € de <name>. Concepto: <reference>"
  pattern = /recibido\s+([\d,]+)\s*€\s+de\s+([^.]+)\.\s*Concepto:\s*(.+)/i
  
  match = regex_match(pattern, text)
  
  if not match:
    return null
  
  return {
    amount: parse_decimal(match[1]),  // "100,50" → 100.50
    sender_name: match[2].trim(),     // "María García"
    reference: match[3].trim()        // "Elena#142"
  }
```

**Variations by bank:**
- BBVA: "Has recibido..."
- CaixaBank: "Bizum de..."
- Openbank: "Recepción Bizum..."

**Hypothesis:** Bizum is a collaboration between Spanish banks (Red de Servicios de Bizum), so notification format is likely standardized across all participating banks. The variations above may be SMS-era differences that have been unified in push notifications.

**Solution (defensive):** Multiple regex patterns per bank, try all until match

**Pseudocode:**
```
BIZUM_PATTERNS = [
  /recibido\s+([\d,]+)\s*€\s+de\s+([^.]+)\.\s*Concepto:\s*(.+)/i,
  /Bizum de\s+([^:]+):\s+([\d,]+)\s*€\.\s*Concepto:\s*(.+)/i,
  /Recepción Bizum\s+([\d,]+)\s*€\.\s*De:\s+([^.]+)\.\s*Concepto:\s*(.+)/i
]

function parseBizumNotification(text):
  for pattern in BIZUM_PATTERNS:
    match = regex_match(pattern, text)
    if match:
      return extract_payment_details(match)
  
  return null  // No pattern matched
```

**Testing needed:** Validate actual push notification format from BBVA, CaixaBank, Openbank apps. May discover single pattern works for all.

---

### Parsing SEPA Notifications (Europe)

**Example notification:**
```
Title: "Transferencia recibida"
Text: "100,50 € de ES12 3456 7890 1234 5678 9012. Concepto: Elena#142"
```

**Regex pattern:**
```
function parseSEPANotification(text):
  // Pattern: "<amount> € de <IBAN>. Concepto: <reference>"
  pattern = /([\d,]+)\s*€\s+de\s+([A-Z]{2}\d{2}\s*(?:\d{4}\s*){4}\d{4})\.\s*Concepto:\s*(.+)/i
  
  match = regex_match(pattern, text)
  
  if not match:
    return null
  
  return {
    amount: parse_decimal(match[1]),
    sender_iban: match[2].replace(/\s/g, ""),  // Remove spaces
    reference: match[3].trim()
  }
```

**Note:** SEPA Instant (SCT Inst) transfers complete in seconds, similar to Bizum. Standard SEPA (SCT) takes 1-3 business days. Most Spanish banks now support SEPA Instant. Parsing approach is identical for both.

**Reference:** https://www.sepaesp.es/sepa/es/secciones/instrumentos/Transferencia-Inmediata/transferencia-inmediata.html

**Fraud detection (SEPA Instant feature "g"):** Recipient name verification. If the name in the bank account doesn't match the payment instruction, sender's bank shows warning. This is a built-in scam protection feature that complements Asgaya's fraud detection (María's bot verifies `payment.recipient_name == seller.expected_name`).

---

### Parsing PagoMóvil Notifications (Venezuela)

**Example notification:**
```
Title: "Pago recibido"
Text: "2.500.000,00 Bs de 0412-1234567. Ref: Elena#142"
```

**Regex pattern:**
```
function parsePagoMovilNotification(text):
  // Pattern: "<amount> Bs de <phone>. Ref: <reference>"
  pattern = /([\d.,]+)\s*Bs\s+de\s+([\d-]+)\.\s*Ref:\s*(.+)/i
  
  match = regex_match(pattern, text)
  
  if not match:
    return null
  
  return {
    amount: parse_decimal(match[1]),  // "2.500.000,00" → 2500000.00
    sender_phone: match[2],
    reference: match[3].trim()
  }
```

**Currency conversion needed:** Bs → EUR (use exchange rate oracle, Phase 1+)

**Market validation:** Traditional remittance services charge ~10% fees on EUR→VES corridor. Asgaya can offer 0.5-2% fees (sender fee + seller profit margin), capturing 7-9% savings for users. See `knowledge/intelligence/2026-06-26-remittance-quote.md` for real market data.

---

## Cash Account Extraction

### What to Extract

**Goal:** Get "Elena#142" from payment reference

**Challenges:**
- User might type "elena#142" (lowercase)
- User might type "Elena 142" (missing #)
- User might type "Elena#142 gracias" (extra text)

### Normalization

**Pseudocode:**
```
function extractCashAccount(reference):
  // Remove extra whitespace
  clean = reference.trim()
  
  // Pattern: <name>#<number> (supports multi-word names with spaces/hyphens)
  // Examples: "Elena#142", "Bodega Rosa#487", "Farmacia-Caracas#1200"
  pattern = /([a-zA-Z][a-zA-Z0-9\-\s]*?)\s*#\s*(\d+)/i
  
  match = regex_match(pattern, clean)
  
  if not match:
    return null
  
  // Capitalize first letter of each word
  name = capitalize_words(match[1].trim())  // "bodega rosa" → "Bodega Rosa"
  number = parseInt(match[2])
  
  return name + "#" + number  // "Bodega Rosa#487"
```

**Pattern breakdown:**
- `[a-zA-Z]` - Must start with letter
- `[a-zA-Z0-9\-\s]*?` - Followed by letters, numbers, hyphens, or spaces (non-greedy)
- `\s*#\s*` - Hash separator (required in Phase 0 for safety)
- `(\d+)` - Number

**Edge cases:**
- "elena142" (no # separator) → Rejected (Phase 0 strict mode)
- "Elena" (no number) → Rejected (need both name and number)
- "Bodega Rosa#487" → Accepted (multi-word name)
- "Farmacia-Caracas#1200" → Accepted (hyphenated name)

**Phase 0 approach:** Require # separator (strict), reject ambiguous references. Phase 1+ can relax to support legacy formats.

---

## Covenant Matching

### Query Electrum for Matching Covenant

**What:** Find unfunded covenant where recipient = Elena#142

**Pseudocode:**
```
function findCovenantByRecipient(cash_account):
  // Resolve Cash Account to BCH address
  recipient_address = resolveCashAccount(cash_account)
  
  if not recipient_address:
    return null  // Cash Account not registered
  
  // Query Electrum for covenants with this recipient
  covenants = electrumQuery("blockchain.covenant.list_by_recipient", {
    recipient_address: recipient_address,
    status: "unfunded"  // Only unfunded covenants
  })
  
  if covenants.length == 0:
    return null  // No matching covenant
  
  if covenants.length > 1:
    // Multiple matches - use most recent
    covenants.sort(by: created_at, desc)
  
  return covenants[0]
```

**Electrum query:**
```json
{
  "method": "blockchain.covenant.list_by_recipient",
  "params": {
    "recipient_address": "bitcoincash:qp...",
    "status": "unfunded"
  }
}
```

**Response:**
```json
{
  "result": [
    {
      "covenant_id": "abc123...",
      "recipient": "bitcoincash:qp...",
      "amount": 100.00,
      "buffer": 7.00,
      "total_bch": 0.0234,
      "expires_at": 1735689600,
      "created_at": 1735686000
    }
  ]
}
```

---

### Matching Logic

**Decision tree:**
```
function matchPaymentToCovenant(payment):
  // 1. Extract Cash Account
  cash_account = extractCashAccount(payment.reference)
  
  if not cash_account:
    log("No Cash Account in reference: " + payment.reference)
    return null
  
  // 2. Find covenant
  covenant = findCovenantByRecipient(cash_account)
  
  if not covenant:
    log("No covenant for " + cash_account)
    return null
  
  // 3. Verify amount matches
  expected_amount = covenant.amount + (covenant.amount * 0.005)  // 0.5% sender fee
  tolerance_percent = 0.01  // 1% tolerance (2× the sender fee)
  tolerance_amount = covenant.amount * tolerance_percent
  
  if abs(payment.amount - expected_amount) > tolerance_amount:
    if payment.amount < expected_amount:
      // Underpayment: REJECT - seller would lose money if we fund
      log_warning("Underpayment detected. Expected at least " + (expected_amount - tolerance_amount) + ", got " + payment.amount)
      show_notification("Payment too low (€" + payment.amount + " < €" + expected_amount + "). Contact sender.")
      return null  // Don't fund covenant
    else:
      // Overpayment: ACCEPT - buyer tipped or rounded up
      log("Overpayment accepted: €" + payment.amount + " (expected ~€" + expected_amount + "). Proceeding with funding.")
      // Continue to fund covenant (buyer gets less BCH per EUR, their choice)
  
  // 4. Check expiry
  if now() > covenant.expires_at:
    log("Covenant expired: " + covenant.covenant_id)
    return null
  
  return covenant
```

**Amount tolerance:** 1% of covenant amount (percentage-based, not fixed EUR)
- **Rationale:** Asgaya's 0.5% sender fee means tolerance must scale with amount
- **Example:** €100 covenant → €1 tolerance, €20 covenant → €0.20 tolerance
- **Allows for:** Buyer rounding up (€100.50 instead of €100), bank decimal precision, tips

**Expiry check:** Don't fund expired covenants (BCH would be locked forever)

---

## Auto-Funding

### Check Wallet Balance

**What:** Does Isabel have enough BCH to fund covenant?

**Pseudocode:**
```
function hasEnoughBCH(required_bch):
  wallet_balance = getWalletBalance()  // Total BCH in wallet
  
  if wallet_balance >= required_bch:
    return true
  else:
    return false
```

**If insufficient:** Buy more from Kraken (see below)

💬 I think this is a great use case for nostr in the bulletin board there is a range of transactions but the nstr message can say if the seller has enough bch to lock in the covenant. Why I think this is important if we allow sellers that don't hold bch to participate we are asking for problems and bad UX when something inevitably happens. If insuficient funds notify the sender and fail gracefully this give the oportunity to other sellers to do business and increase their reputation, if not a few early adopters can potentially concentrate most of the transactions per corridor and the new commers can only compeet in price.

---

### Fund Covenant

**What:** Create transaction that spends BCH into covenant script

**Pseudocode:**
```
function fundCovenant(covenant):
  required_bch = covenant.total_bch
  
  // Select UTXOs
  coins = selectCoins(required_bch)
  
  if not coins:
    return error("Insufficient BCH")
  
  // Build funding transaction
  tx = createTransaction({
    inputs: coins.inputs,
    outputs: [
      {
        value: required_bch,
        script: covenant.covenant_script  // Lock in covenant
      },
      {
        value: coins.change,
        script: p2pkh(my_change_address)  // Change back to Isabel
      }
    ]
  })
  
  // Sign and broadcast
  signed_tx = signTransaction(tx)
  tx_id = broadcast(signed_tx)
  
  // Notify sender (optional)
  sendNostrDM(covenant.sender_pubkey, {
    type: "covenant_funded",
    covenant_id: covenant.covenant_id,
    tx_id: tx_id
  })
  
  return tx_id
```

**Cost:** ~€0.0002 (BCH transaction fee)

**Timing:** 10-30 seconds (sign + broadcast + 1 confirmation)

---

## Exchange Integration (Phase 1+ Extension)

**Status:** Optional extension, not required for Phase 0

**Phase 0 approach:** If wallet balance insufficient, show notification to seller ("Received payment but wallet has only X BCH, need Y BCH. Top up wallet to fund covenant.")

**Phase 1+ enhancement:** Auto-replenish from exchange (Kraken example below)

**Two replenishment strategies:**

1. **Just-in-time (use received fiat):** When Isabel receives €100 fiat payment and wallet BCH is insufficient, automatically send €107 fiat to exchange, buy BCH, fund covenant
2. **Bulk replenishment (when balance low):** When wallet BCH drops below threshold (e.g., €500 worth), trigger automated bank payment to exchange for bulk purchase (e.g., send €1,000, buy BCH in bulk, lower per-transaction costs)

**Which strategy?**
- Just-in-time: Better for low-volume sellers (don't tie up capital)
- Bulk: Better for high-volume sellers (lower exchange fees, faster covenant funding)

---

### Strategy 1: Just-in-Time Replenishment

**Trigger:** Wallet balance < required amount for covenant

**Flow:**
1. Isabel receives €100.50 Bizum payment (covenant needs €107 BCH)
2. Wallet has only €50 worth of BCH
3. Send €57 (shortage + buffer) from Isabel's bank to exchange
4. Buy BCH with received fiat
5. Withdraw to wallet
6. Fund covenant

**Pseudocode:**
```
function ensureSufficientBCH(required_bch, payment_received_eur):
  wallet_balance = getWalletBalance()
  
  if wallet_balance >= required_bch:
    return  // Already have enough
  
  shortage_bch = required_bch - wallet_balance
  shortage_eur = shortage_bch * getCurrentBCHPrice()
  
  // Send fiat to exchange (automated bank payment)
  sendFiatToExchange(shortage_eur, source="received_payment")
  
  // Buy BCH on exchange
  buyBCHFromExchange(shortage_bch)
  
  // Withdraw to wallet
  withdrawToWallet(shortage_bch)
  
  // Wait for BCH to arrive (10-30 minutes)
  wait_for_wallet_balance_increase(timeout=30_MINUTES)
```

**Limitation:** Requires exchange account with EUR balance or automated bank integration

---

### Strategy 2: Bulk Replenishment (Threshold-Based)

**Trigger:** Wallet BCH drops below seller's maximum transaction size

**Flow:**
1. Isabel's wallet has €400 worth of BCH
2. Threshold: €500 (her max listing size)
3. Trigger automated bank payment: €1,000 → exchange
4. Buy €1,000 BCH in bulk (better exchange rate)
5. Withdraw to wallet
6. Resume covenant funding

**Pseudocode:**
```
function checkReplenishmentThreshold():
  wallet_balance_eur = getWalletBalance() * getCurrentBCHPrice()
  replenishment_threshold = seller_max_listing_amount  // e.g., €500
  
  if wallet_balance_eur < replenishment_threshold:
    // Trigger bulk buy
    bulk_amount = replenishment_threshold * 2  // Buy 2× threshold for buffer
    
    // Automated bank payment to exchange
    sendBankPaymentToExchange(bulk_amount)
    
    // Buy BCH in bulk
    buyBCHFromExchange(bulk_amount)
    
    log("Bulk replenishment triggered: buying €" + bulk_amount + " BCH")
```

**Advantage:** Lower per-transaction fees (bulk buying), faster covenant funding (wallet always topped up)

---

### Exchange Selection

**Kraken (example below):** Low fees (0.16-0.26%), reliable API, supports BCH/EUR pairs, SEPA deposits

**Alternatives:**
- **Coinbase:** Higher fees (~0.5%), but easier bank integration in some regions
- **Binance:** Low fees, high liquidity, but regulatory uncertainty in some countries
- **Local exchanges:** May offer better bank integration (e.g., Bit2Me in Spain)

**Selection criteria:**
- EUR → BCH direct pair (avoid USD conversion)
- SEPA Instant deposit support (fast fiat transfers)
- Low trading fees (<0.3%)
- API access for automation
- Withdrawal fees (some exchanges charge high BCH withdrawal fees)

**Pluggable design:** App should support multiple exchanges (user configures their preferred exchange + API keys)

---

### Kraken API Integration (Example)

**Step 1: Get market price**
```
function getKrakenBCHPrice():
  response = http_get("https://api.kraken.com/0/public/Ticker?pair=BCHEUR")
  
  price_eur = parse_float(response.result.BCHEUR.c[0])  // Current price
  
  return price_eur
```

**Step 2: Calculate EUR needed**
```
function calculateEURForBCH(bch_amount):
  price = getKrakenBCHPrice()
  
  eur_needed = bch_amount * price
  
  // Add 1% for slippage + fees
  eur_with_buffer = eur_needed * 1.01
  
  return eur_with_buffer
```

**Step 3: Place market order**
```
function buyBCHFromKraken(bch_amount):
  // Authenticate with API key
  api_key = getKrakenAPIKey()
  api_secret = getKrakenAPISecret()
  
  // Build order request
  order = {
    pair: "BCHEUR",
    type: "buy",
    ordertype: "market",
    volume: bch_amount
  }
  
  // Sign request (Kraken-specific signature)
  signature = sign_kraken_request(order, api_secret)
  
  // Submit order
  response = http_post("https://api.kraken.com/0/private/AddOrder", 
    body: order,
    headers: {
      "API-Key": api_key,
      "API-Sign": signature
    }
  )
  
  if response.error:
    return error("Kraken order failed: " + response.error)
  
  order_id = response.result.txid[0]
  
  return order_id
```

**Step 4: Withdraw BCH to wallet**
```
function withdrawBCHFromKraken(amount, my_address):
  api_key = getKrakenAPIKey()
  api_secret = getKrakenAPISecret()
  
  withdrawal = {
    asset: "BCH",
    key: my_address,  // Pre-registered withdrawal address
    amount: amount
  }
  
  signature = sign_kraken_request(withdrawal, api_secret)
  
  response = http_post("https://api.kraken.com/0/private/Withdraw",
    body: withdrawal,
    headers: {
      "API-Key": api_key,
      "API-Sign": signature
    }
  )
  
  if response.error:
    return error("Withdrawal failed: " + response.error)
  
  // Wait for BCH to arrive (usually 10-30 minutes)
  wait_for_wallet_balance_increase(timeout=30_MINUTES)
  
  return success
```

**Security:** API keys stored encrypted, withdrawal addresses pre-registered on Kraken

---

## Error Handling

### Notification Parsing Failures

```
function parsePaymentNotification(text, app_package):
  payment = null
  
  if app_package in BIZUM_APPS:
    payment = parseBizumNotification(text)
  else if app_package in SEPA_APPS:
    payment = parseSEPANotification(text)
  else if app_package in PAGOMOVIL_APPS:
    payment = parsePagoMovilNotification(text)
  
  if not payment:
    log_warning("Failed to parse notification: " + text)
    // Store for manual review
    save_unparsed_notification(text, app_package)
    show_notification("Received payment but couldn't parse. Check manually.")
    return null
  
  return payment
```

**Manual review UI:** Show unparsed notifications, let user manually enter Cash Account

---

### Cash Account Not Found

```
if not resolveCashAccount(cash_account):
  log("Cash Account not registered: " + cash_account)
  show_notification("Payment received for " + cash_account + " but not registered. Manual action needed.")
  // Don't auto-fund (could be wrong recipient)
```

---

### Covenant Not Found

```
if not findCovenantByRecipient(cash_account):
  log("No covenant for " + cash_account)
  show_notification("Payment received for " + cash_account + " but no matching covenant. Contact sender.")
  // Refund fiat? Or wait for sender to create covenant?
```

**Phase 0 approach:** Show notification, wait for user to investigate

---

### Insufficient BCH

**Phase 0 behavior:**
```
wallet_balance = getWalletBalance()
required_bch = covenant.total_bch

if wallet_balance < required_bch:
  shortage = required_bch - wallet_balance
  log("Insufficient BCH: have " + wallet_balance + ", need " + required_bch)
  
  // Notify seller (Isabel)
  show_notification("Payment received but wallet has only " + wallet_balance + " BCH (need " + required_bch + "). Top up wallet to fund covenant.")
  
  // Save pending funding for manual completion
  save_pending_funding(covenant, shortage)
```

**Phase 0+ enhancement: Notify sender via Nostr**
```
// Also notify sender (María) so she can pick a different seller if needed
sendNostrDM(covenant.sender_pubkey, {
  type: "insufficient_capacity",
  covenant_id: covenant.covenant_id,
  seller_cash_account: my_cash_account,
  message: "Received your payment but temporarily low on BCH. Will fund within 1 hour or you can select a different seller from bulletin board."
})
```

**Benefit:** Sender can make real-time decision (wait for current seller to top up, or pick different seller with capacity). Improves UX without adding protocol complexity.

**Phase 1+ with exchange integration:**
```
try:
  buyBCHFromExchange(shortage)  // Auto-replenish (see Exchange Integration section)
catch ExchangeAPIError:
  // Fallback to Phase 0 behavior (notify user)
  show_notification("Received payment but can't auto-buy BCH. Manual top-up needed.")
```

---

### Double-Funding Prevention

**Problem:** Two notifications for same payment (bank app + SMS)

**Solution:** Track funded covenants

```
function handlePaymentReceived(payment):
  covenant = matchPaymentToCovenant(payment)
  
  if not covenant:
    return
  
  // Check if already funded
  if isCovenantAlreadyFunded(covenant.covenant_id):
    log("Covenant already funded, ignoring duplicate notification")
    return
  
  // Mark as funded (before actually funding, prevent race)
  markCovenantAsFunded(covenant.covenant_id)
  
  fundCovenant(covenant)
```

**Storage:** Local database (SQLite) tracks funded covenant IDs

---

## Platform-Specific Notes

### Android
- **Notification Listener:** Requires user to enable in Settings > Notification Access
- **Background service:** Keep notification listener running (foreground service, show persistent notification)
- **Battery optimization:** Request exemption (Settings > Battery > Unrestricted)
- **Permissions:** BIND_NOTIFICATION_LISTENER_SERVICE (user grants via system settings)

### iOS
- **Critical limitation:** iOS doesn't allow apps to read other apps' notifications (privacy/security sandbox)
- **Impact:** Passive selling (auto-funding) is **not possible** on iOS Phase 0
- **Phase 0 workaround:** Manual workflow (Isabel checks bank app, manually opens Asgaya, pastes covenant ID, clicks "Fund")
  - **UX degradation:** No longer passive - Isabel must take action for every payment
  - **Result:** iOS becomes "just another P2P market wallet" without the notification bot magic
- **Phase 1+ solution:** Bank API integration
  - **PSD2 (Europe):** Open banking APIs allow apps to query payment notifications with user consent
  - **Business accounts:** Some banks offer API access to business customers (e.g., BBVA API Market)
  - **Potential strategy:** iOS version for "pro sellers" (business bank accounts) who can afford API integration

**Recommendation for Phase 0:** Focus on Android (where passive selling works). iOS can launch with manual workflow, upgrade to bank API in Phase 1+ when volume justifies API integration costs.

### Web/Desktop
- **No notification access:** Can't intercept mobile bank notifications
- **Alternative:** Manual covenant funding (paste covenant ID, click "Fund")

---

## Testing Strategy

### Unit Tests
- Regex patterns (test all bank notification formats)
- Cash Account extraction (various reference formats)
- Amount parsing (decimal separators: "100,50" vs "100.50")
- Covenant matching logic

### Integration Tests (Testnet)
- Send test notification (simulated bank notification)
- Parse and extract Cash Account
- Query testnet Electrum for covenant
- Fund covenant with testnet BCH

### Edge Cases
- Unparseable notification (save for manual review)
- Multiple covenants for same recipient (use most recent)
- Expired covenant (don't fund)
- Insufficient BCH (Phase 0: notify user, Phase 1+: auto-buy from exchange)

### Real-World Testing
**Critical:** Test with actual bank apps (BBVA, CaixaBank, etc.) on real phones
- Real Bizum payment with "Elena#142" reference
- Verify notification appears
- Verify parsing extracts correct Cash Account
- Verify covenant matched correctly

---

## Research References

**smsbridge_loop.py:**
- Early prototype (SMS parsing via Termux + Python)
- Replaced by Notification Listener Service (better UX)
- Still useful reference for regex patterns
- Located: `/knowledge/poc/smsbridge_loop.py`

**RS026: Android Notifications Architecture**
- Validates Notification Listener Service approach
- Alternative strategies (SMS, bank APIs)
- Privacy/security considerations
- Located: `/knowledge/research/RS026_android_notifications.md`

---

## Related Components

**Uses:**
- [wallet.md](wallet.md) - Cash Account resolution, UTXO selection, transaction signing
- [state-management.md](state-management.md) - Track funded covenants (prevent double-funding)
- Electrum servers (covenant queries)
- Kraken API (BCH replenishment)

**Used by:**
- [bulletin-board.md](bulletin-board.md) - Passive sellers have active listings

**Interacts with:**
- [nostr.md](nostr.md) - Send "covenant funded" notification to sender

---

**Status:** Phase 0 - Core implementation priority  
**Updated:** 2026-06-25  
**Complexity:** High (most complex component)  
**Research:** See RS026 (Android notifications), smsbridge_loop.py (early prototype)
