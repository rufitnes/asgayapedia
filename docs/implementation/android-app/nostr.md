# Nostr Component

**Purpose:** Encrypted peer-to-peer messaging for payment instructions between buyer and seller

**Complexity:** Low-Medium - WebSocket connections + NIP-17 gift-wrapped DMs (NIP-44 encryption, ChaCha20-Poly1305)

> **⚠️ Phase 0 Priority - Required for Mainnet Launch**
> 
> Nostr is the **coordination layer** for payment instructions between sender and BCH seller:
> 
> **Why Phase 0 (High Priority):**
> 1. **Seamless UX** - Integrated directly into Asgaya client, no separate app needed
> 2. **One-time setup** - User installs Asgaya, permissions granted, coordination works
> 3. **Privacy by default** - End-to-end encrypted, censorship-resistant, no phone numbers
> 4. **No external dependencies** - Users don't need Telegram/WhatsApp accounts
> 
> **Current status:**
> - **Design:** ✅ **NIP-17 decided (Sep 9, 2026)** — see update note below; authoritative implementation design lives in `collaborative_workspace/nostr-transport/` (files 00, 03, 09, 10, 12)
> - **Implementation (September 9, 2026):** 🔨 Task 2 in progress — NostrKeyManager (random per-wallet keys, Option A approved)
> - **Testing:** Telegram bot serves this function (development only)
> - **Production:** Nostr required for mainnet (Telegram = fallback/emergency only)
> 
> **⚠️ UPDATE (2026-09-09) — NIP-17 replaces kind-4:**
> This doc originally specified kind-4 DMs. The design has moved to **NIP-17 gift-wrapped DMs** (kind-14 rumor → kind-13 seal → kind-1059 gift-wrap):
> - **Why:** NIP-04 (kind-4) is officially deprecated; kind-4+NIP-44 would be a custom format, not a standard. NIP-17 is what other BCH wallets (Paytaca, OPTN) already speak → zero-change interop, strengthens the "Asgaya is an open-protocol client, not an intermediary" compliance case.
> - **Subscription filter:** `kinds: [1059]` (gift-wraps addressed to me), not kind-4.
> - **Crypto:** NIP-44 unchanged (ChaCha20-Poly1305). The seal (kind-13) is **signed by the sender's real key**; the gift-wrap (kind-1059) is signed by a random ephemeral key; the rumor (kind-14) carries the payload and is unsigned.
> - **Payload format:** the JSON message examples further down (`payment_request`, etc.) predate the current tag-block format — the implemented design ships `[FUND_COVENANT]`-style blocks as the rumor content (see Receiving note).
> 
> **Telegram drawbacks:** Users may not have it, requires separate download/setup, not privacy-focused.
> **Nostr advantage:** Built into Asgaya = zero friction for users.

---

## Overview

Nostr enables María and Isabel to coordinate payment details privately:
- **María creates covenant** → Sends payment instructions to Isabel via Nostr DM
- **Isabel receives fiat** → Notifies María via Nostr (optional, Electrum is primary)
- **No phone numbers exchanged** → Privacy preserved

**Key features:**
- End-to-end encrypted (NIP-44 - ChaCha20-Poly1305, current standard)
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
        kinds: [1059],  // NIP-17 gift-wraps addressed to me (was kind-4 — see update note)
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
["REQ", "asgaya_messages", {"kinds": [1059], "#p": ["npub1..."]}]
```

**Relay response:** Sends all past encrypted DMs + streams new ones

---

## Encrypted Messaging (NIP-44)

**Why NIP-44?** Current standard (ChaCha20-Poly1305, authenticated). NIP-04 is legacy (AES-256-CBC, deprecated by ecosystem). Recommended by `nostr-tools`, `nostr-sdk`, and most clients. In our design NIP-44 is used **within NIP-17** (seal + gift-wrap encryption).

### Sending a Message

**What:** Encrypt payment instructions, send to Isabel

**NIP-17 three-layer structure (authoritative — see workspace files 00/03):**
```
1. rumor (kind-14):    plain-text payload (e.g. [FUND_COVENANT] block), has id, NO sig
2. seal (kind-13):     rumor NIP-44-encrypted to recipient, SIGNED by sender's real key, no tags
3. gift-wrap (kind-1059): seal NIP-44-encrypted to recipient, signed by a RANDOM ephemeral key,
                       single ["p", recipient] tag, obfuscated created_at (≤2 days past)
```

**Pseudocode:**
```
function sendNostrDM(recipient_pubkey, message_text):
  my_keys = getNostrKeys()
  
  // Layer 1: build rumor (unsigned)
  rumor = {
    kind: 14,
    pubkey: my_keys.public_key,
    created_at: now(),                    // real time (canonical)
    tags: [["p", recipient_pubkey]],
    content: message_text                 // plain text payload
  }
  rumor.id = sha256(serialize(rumor))     // has id, no sig
  
  // Layer 2: seal — rumor NIP-44-encrypted, signed by sender's real key
  seal = {
    kind: 13,
    pubkey: my_keys.public_key,
    created_at: randomNow(),              // obfuscated ≤2 days past
    tags: [],
    content: nip44_encrypt(rumor, my_keys.private_key, recipient_pubkey)
  }
  seal.id = hash(seal)
  seal.sig = schnorr_sign(seal.id, my_keys.private_key)   // BIP-340, sender auth
  
  // Layer 3: gift-wrap — seal NIP-44-encrypted, signed by ephemeral key
  ephemeral_keys = generate_keypair()     // random, disposable per message
  gift_wrap = {
    kind: 1059,
    pubkey: ephemeral_keys.public_key,
    created_at: randomNow(),              // obfuscated ≤2 days past
    tags: [["p", recipient_pubkey]],
    content: nip44_encrypt(seal, ephemeral_keys.private_key, recipient_pubkey)
  }
  gift_wrap.id = hash(gift_wrap)
  gift_wrap.sig = schnorr_sign(gift_wrap.id, ephemeral_keys.private_key)
  
  // Send to all connected relays
  for relay in active_relays:
    relay.send(json_encode(["EVENT", gift_wrap]))
  
  return gift_wrap.id
```

**NIP-44 encryption (simplified):**
```
shared_secret = ECDH(my_private_key, recipient_public_key)
// Derive conversation key using HKDF
conversation_key = HKDF_extract(shared_secret, salt="nip44-v2")
message_keys = HKDF_expand(conversation_key, info=nonce)   // per-message keys
// ChaCha20-Poly1305-style AEAD (ChaCha20 + HMAC-SHA256 per NIP-44 v2)
encrypted = ...                                             // per NIP-44 spec
content = base64(version + nonce + ciphertext + mac)
```

**Actual implementation:** Hand-rolled NIP-44 on BouncyCastle + NIP-17 gift-wrap (no nostr library — offline-build constraint). Authoritative spec: NIP-44 + NIP-59 + workspace file 03.

---

### Receiving a Message

**What:** Unwrap + decrypt incoming gift-wrap from relay (NIP-17)

**Pseudocode:**
```
function handleIncomingMessage(gift_wrap):       // kind-1059
  // 1. Verify gift-wrap signature (ephemeral key) — NIP-44: MUST validate before decrypt
  if not verify_signature(gift_wrap):
    log_warning("Invalid gift-wrap signature, ignoring")
    return

  // 2. Decrypt gift-wrap content → recover seal (kind-13)
  my_keys = getNostrKeys()
  seal = nip44_decrypt(
    recipient_private_key: my_keys.private_key,
    sender_public_key: gift_wrap.pubkey,          // ephemeral key
    ciphertext: gift_wrap.content
  )

  // 3. Verify seal signature — authenticates the REAL sender (anti-impersonation)
  if not verify_signature(seal):
    log_warning("Invalid seal signature, ignoring")
    return

  // 4. Decrypt seal content → recover rumor (kind-14)
  rumor = nip44_decrypt(
    recipient_private_key: my_keys.private_key,
    sender_public_key: seal.pubkey,               // real sender key
    ciphertext: seal.content
  )

  // 5. Verify rumor.pubkey == seal.pubkey (NIP-17 anti-impersonation)
  if rumor.pubkey != seal.pubkey:
    log_warning("Sender identity mismatch, ignoring")
    return

  // 6. Handle payload (see payment-format note below)
  if rumor.content contains "[FUND_COVENANT]":
    handleFundCovenant(rumor.content)   // existing parseFundCovenant — unchanged
  else:
    log_warning("Unknown message type: " + rumor.content)
```

> **⚠️ Payload note:** the JSON message types below (`payment_request`, `payment_instruction`, `covenant_funded`) predate the current **transport-agnostic tag-block format** (`[FUND_COVENANT]`, `[CASH_IN_PERSON]`, `[COVENANT_V25]`). The implemented design ships the tag-block text as the rumor content; `NotificationListener.parseFundCovenant()` parses it unchanged. Treat the JSON examples below as historical.

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
  plaintext = nip44_decrypt(ciphertext)
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
- **nostr-kt** (Kotlin): Full NIP-44 support, relay management
- **Alternative:** Raw WebSocket + manual NIP-44 (simpler, fewer dependencies but complex crypto)

### iOS
- **nostr-swift**: Full NIP-44 support
- **Alternative:** Raw WebSocket + manual NIP-44

### Web
- **nostr-tools** (JavaScript): Most popular, full NIP support including NIP-44
- **Alternative:** Raw WebSocket (NIP-44 crypto via SubtleCrypto API + HKDF implementation)

**Recommendation:** Use library for NIP-44 encryption (complex crypto: HKDF key derivation + ChaCha20-Poly1305 authenticated encryption). Raw WebSocket is fine for relay management.

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
- NIP-44 encryption/decryption (test vectors from NIP-44 spec)
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

**Status:** Phase 0 - Design decided (NIP-17), implementation in progress  
**Updated:** 2026-09-09 (kind-4 → NIP-17 migration)  
**Originally written:** 2026-08-04  
**Complexity:** Low-Medium (WebSocket + NIP-17 gift-wrap + NIP-44)  
**Priority:** Essential coordination layer (sender ↔ seller payment instructions)  
**Current:** Telegram bot (testing/fallback only) | **Production:** Nostr required (integrated UX)

**Note:** Updated to NIP-44 (current standard: ChaCha20-Poly1305). NIP-04 (kind-4) is legacy (AES-256-CBC, deprecated). DMs use **NIP-17 gift-wrap** (kind-14 → kind-13 seal → kind-1059). Authoritative implementation design: `collaborative_workspace/nostr-transport/` (files 00, 03, 09, 10, 12).
---

## Navigation

**[🏠 Home](../../../index.md)** | **[↑ Android App](README.md)** | **[📖 Glossary](../../../glossary.md)**
