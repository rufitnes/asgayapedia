# Nostr Component

**Purpose:** Encrypted peer-to-peer messaging for payment instructions between buyer and seller

**Complexity:** Low - WebSocket connections + NIP-04 encryption

---

## Overview

Nostr enables María and Isabel to coordinate payment details privately:
- **María creates covenant** → Sends payment instructions to Isabel via Nostr DM
- **Isabel receives fiat** → Notifies María via Nostr (optional, Electrum is primary)
- **No phone numbers exchanged** → Privacy preserved

**Key features:**
- End-to-end encrypted (NIP-04)
- No central server (connect to public relays)
- Censorship-resistant (multiple relay redundancy)

---

## What Is Nostr?

### Protocol Overview

**Definition:** Notes and Other Stuff Transmitted by Relays

**Architecture:**
- **Clients:** Asgaya apps (María, Isabel)
- **Relays:** WebSocket servers (wss://relay.damus.io, etc.)
- **Messages:** JSON events signed with user's private key

**No blockchain:** Just WebSocket servers passing messages

**Why Nostr?**
- Free (public relays available)
- Fast (~100-500ms latency)
- No phone number required (unlike Telegram/WhatsApp)
- Decentralized (no single point of failure)

---

## Nostr Identity

### Key Pair

**Each user has:**
- **Private key (nsec):** Signs messages, proves identity
- **Public key (npub):** Receives messages, shown to others

**Derivation from wallet:**
```
function getNostrKeys():
  // Derive from wallet seed (same entropy as BCH keys)
  wallet_seed = getWalletSeed()
  nostr_private_key = deriveNostrKey(wallet_seed, path="m/44'/1237'/0'/0/0")
  nostr_public_key = getPublicKey(nostr_private_key)
  
  return {
    private_key: nostr_private_key,  // Keep secret
    public_key: nostr_public_key     // Share in listings
  }
```

**Why derive from wallet?** One seed phrase backs up everything (BCH + Nostr)

**Encoding:**
- Private key: `nsec1...` (bech32 format)
- Public key: `npub1...` (bech32 format)

---

## Relay Management

### Connecting to Relays

**Strategy:** Connect to 3-5 public relays for redundancy

**Pseudocode:**
```
RELAYS = [
  "wss://relay.damus.io",
  "wss://relay.snort.social",
  "wss://nos.lol"
]

function connectToRelays():
  connections = []
  
  for relay_url in RELAYS:
    try:
      ws = WebSocket(relay_url)
      ws.onopen = () => {
        log("Connected to " + relay_url)
        subscribeToMessages(ws)
      }
      connections.push(ws)
    catch ConnectionError:
      log_warning("Failed to connect to " + relay_url)
      // Continue with other relays
  
  if connections.length == 0:
    return error("No relays available")
  
  return connections
```

**Redundancy:** If 1-2 relays fail, others still work

**Fallback:** If all relays fail, queue messages for retry when connectivity returns

---

### Subscribing to Messages

**What:** Tell relay to send me messages addressed to my public key

**Pseudocode:**
```
function subscribeToMessages(relay_connection):
  my_public_key = getNostrKeys().public_key
  
  subscribe_message = {
    type: "REQ",
    subscription_id: "asgaya_messages",
    filters: [
      {
        kinds: [4],  // Kind 4 = encrypted DM
        "#p": [my_public_key]  // Recipient is me
      }
    ]
  }
  
  relay_connection.send(json_encode(subscribe_message))
  
  relay_connection.onmessage = (event) => {
    handleIncomingMessage(event)
  }
```

**WebSocket message format:**
```json
["REQ", "asgaya_messages", {"kinds": [4], "#p": ["npub1..."]}]
```

**Relay response:** Sends all past encrypted DMs + streams new ones

---

## Encrypted Messaging (NIP-04)

### Sending a Message

**What:** Encrypt payment instructions, send to Isabel

**Pseudocode:**
```
function sendNostrDM(recipient_pubkey, message_text):
  my_keys = getNostrKeys()
  
  // Encrypt with NIP-04 (ECDH + AES-256-CBC)
  encrypted_content = nip04_encrypt(
    sender_private_key: my_keys.private_key,
    recipient_public_key: recipient_pubkey,
    plaintext: message_text
  )
  
  // Build Nostr event
  event = {
    kind: 4,  // Encrypted DM
    pubkey: my_keys.public_key,
    created_at: now(),
    tags: [
      ["p", recipient_pubkey]  // Recipient
    ],
    content: encrypted_content
  }
  
  // Sign event
  event.id = hash(event)
  event.sig = sign(event.id, my_keys.private_key)
  
  // Send to all connected relays
  for relay in active_relays:
    relay.send(json_encode(["EVENT", event]))
  
  return event.id
```

**NIP-04 encryption (simplified):**
```
shared_secret = ECDH(my_private_key, recipient_public_key)
encrypted = AES256_CBC_encrypt(plaintext, key=shared_secret)
content = base64(encrypted)
```

**Actual implementation:** Use Nostr library (handles NIP-04 complexity)

---

### Receiving a Message

**What:** Decrypt incoming DM from relay

**Pseudocode:**
```
function handleIncomingMessage(nostr_event):
  // Verify signature
  if not verify_signature(nostr_event):
    log_warning("Invalid signature, ignoring")
    return
  
  // Decrypt content
  my_keys = getNostrKeys()
  sender_pubkey = nostr_event.pubkey
  
  plaintext = nip04_decrypt(
    recipient_private_key: my_keys.private_key,
    sender_public_key: sender_pubkey,
    ciphertext: nostr_event.content
  )
  
  // Parse message (JSON payload)
  message = json_decode(plaintext)
  
  // Handle based on type
  if message.type == "payment_request":
    handlePaymentRequest(message)  // Isabel receives this, responds with payment_instruction
  else if message.type == "payment_instruction":
    handlePaymentInstruction(message)  // María receives this, uses it to pay Isabel
  else if message.type == "covenant_funded":
    handleCovenantFunded(message)  // María receives this (optional notification)
  else:
    log_warning("Unknown message type: " + message.type)
```

---

## Payment Instruction Format

### María → Isabel (Payment Request)

**When:** After María creates covenant and finds Isabel's listing

**Message payload:**
```json
{
  "version": 1,
  "type": "payment_request",
  "covenant_id": "abc123...",
  "recipient_cash_account": "Elena#142",
  "amount_eur": 100
}
```

**Fields:**
- `covenant_id`: BCH transaction ID (Isabel uses to verify covenant exists)
- `recipient_cash_account`: Who gets the BCH (Elena, or Carlos if cashing out)
- `amount_eur`: How much fiat María will send

**Purpose:** Request Isabel's payment details so María can pay her

---

### Isabel → María (Payment Instruction)

**When:** After Isabel receives payment request and verifies covenant

**Message payload:**
```json
{
  "version": 1,
  "type": "payment_instruction",
  "covenant_id": "abc123...",
  "payment_method": "bizum",
  "payment_details": {
    "phone": "+34654321098",
    "full_name": "Isabel Rodríguez García",
    "reference": "Elena#142"
  },
  "expires_at": 1735689600
}
```

**Fields:**
- `covenant_id`: Which covenant this payment is for
- `payment_method`: How María should pay (bizum, sepa, pagoMovil)
- `payment_details.phone`: Isabel's phone number for Bizum
- `payment_details.full_name`: Isabel's full name (fraud detection - María's bot verifies this matches bank notification)
- `payment_details.reference`: Payment reference María must include (so Isabel's notification bot can match payment to covenant)
- `expires_at`: Covenant expiry (8 hours from creation)

**Purpose:** Give María everything she needs to complete the Bizum payment

**Size:** ~200 bytes (no Nostr message limit)

---

### Isabel → María (Covenant Funded Notification)

**When:** After Isabel locks BCH in covenant (OPTIONAL - Electrum is primary detection)

**Message payload:**
```json
{
  "version": 1,
  "type": "covenant_funded",
  "covenant_id": "abc123...",
  "funded_at": 1735689000,
  "tx_id": "def456..."
}
```

**Purpose:** Prompt María to query Electrum immediately (faster than polling)

**Not critical:** María will detect funding via Electrum monitoring regardless

---

## Error Handling

### Relay Failures

```
try:
  relay.send(message)
catch WebSocketError:
  // Mark relay as failed
  mark_relay_failed(relay)
  
  // Try remaining relays
  if other_relays_available:
    log("Relay failed, message sent via other relays")
    return success
  else:
    // Queue for retry
    queue_message_for_retry(message)
    show_message("Offline. Message queued.")
```

### Message Not Delivered

**Problem:** No delivery confirmation in Nostr (fire-and-forget)

**Solution:** Timeout + fallback

```
function sendWithRetry(recipient, message):
  sent_at = now()
  
  sendNostrDM(recipient, message)
  
  // Wait for acknowledgment (custom Asgaya convention)
  wait_for_ack(timeout=30_SECONDS)
  
  if not received_ack:
    // Retry once
    log("No ack, retrying")
    sendNostrDM(recipient, message)
    
    wait_for_ack(timeout=30_SECONDS)
    
    if not received_ack:
      // Fallback: On-chain message (OP_RETURN)
      log_warning("Nostr failed, falling back to on-chain")
      sendOnChainMessage(recipient, message)
```

**On-chain fallback (last resort):**
- Create OP_RETURN transaction with message
- Costs ~€0.001 (vs free Nostr)
- Guaranteed delivery (on blockchain)

---

### Decryption Failures

```
try:
  plaintext = nip04_decrypt(ciphertext)
catch DecryptionError:
  // Message not for me, or corrupted
  log_warning("Failed to decrypt message from " + sender)
  // Silently ignore (don't crash)
```

### Invalid Message Format

```
try:
  message = json_decode(plaintext)
  validate_schema(message)
catch ParseError:
  log_warning("Invalid message format from " + sender)
  show_notification("Received malformed message (ignored)")
```

---

## Platform-Specific Notes

### Android
- **WebSocket library:** OkHttp WebSocket client
- **Background connections:** Keep WebSocket alive while app in foreground only
- **Offline queue:** Store unsent messages in SQLite, retry when online

### iOS
- **WebSocket library:** URLSessionWebSocketTask (native)
- **Background limitations:** iOS kills WebSocket when app backgrounded
- **Solution:** Reconnect when app returns to foreground

### Web/Desktop
- **WebSocket:** Native browser WebSocket API
- **Persistence:** IndexedDB for message history (optional)
- **Always-on:** Desktop apps can maintain persistent connections

---

## Nostr Libraries (Recommendations)

### Android
- **nostr-kt** (Kotlin): Full NIP-04 support, relay management
- **Alternative:** Raw WebSocket + manual NIP-04 (simpler, fewer dependencies)

### iOS
- **nostr-swift**: Full NIP-04 support
- **Alternative:** Raw WebSocket + manual NIP-04

### Web
- **nostr-tools** (JavaScript): Most popular, full NIP support
- **Alternative:** Raw WebSocket (NIP-04 crypto via SubtleCrypto API)

**Recommendation:** Use library for NIP-04 encryption (complex crypto). Raw WebSocket is fine for relay management.

---

## Privacy Considerations

### What's Private
- ✅ Message content (encrypted, only sender/recipient can read)
- ✅ Payment details (only in encrypted payload)
- ✅ Covenant amounts (only in encrypted payload)

### What's Public (Relay Can See)
- ❌ María's public key (npub)
- ❌ Isabel's public key (npub)
- ❌ Timestamp of message
- ❌ Message length (rough size)

**Metadata privacy:** Relays see who talks to whom, but not what's said

**Mitigation (Phase 1+):** Use Tor or VPN when connecting to relays

---

## Testing Strategy

### Unit Tests
- NIP-04 encryption/decryption (test vectors)
- Message serialization (JSON encoding)
- Signature verification
- Schema validation

### Integration Tests
- Connect to public relay (testnet)
- Send encrypted DM to self
- Receive and decrypt message
- Multi-relay redundancy (disconnect 1, message still delivered)

### Edge Cases
- All relays offline (queue for retry)
- Message too large (>10KB, should still work but slow)
- Relay returns error (handle gracefully)
- Recipient offline (message stored at relay, delivered when they connect)

---

## Related Components

**Uses:**
- Wallet (derive Nostr keys from seed)

**Used by:**
- [notification-bot.md](notification-bot.md) - Covenant funded notifications
- [bulletin-board.md](bulletin-board.md) - Contact seller from listing

**Interacts with:**
- [state-management.md](state-management.md) - Track sent/received messages

---

**Status:** Phase 0 - Implementation pending  
**Updated:** 2026-06-25  
**Complexity:** Low (WebSocket + library for NIP-04)  
**Research:** See RS070 (implementation documentation strategy)
---

## Navigation

**[🏠 Home](../../../index.md)** | **[↑ Android App](README.md)** | **[📖 Glossary](../../../glossary.md)**
