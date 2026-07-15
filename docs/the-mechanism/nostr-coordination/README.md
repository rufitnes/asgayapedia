# Nostr: How Buyers and Sellers Coordinate Privately
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**Encrypted. Peer-to-peer. No server.** Payment details delivered in under a second.

---

## What It Is

**Nostr** is a decentralized messaging protocol that Asgaya uses for **private coordination** between buyers and sellers after they find each other on the bulletin board.

Not WhatsApp. Not Telegram. Not email. Just **encrypted direct messages sent over a network of relays** that no single entity controls.

When María selects a BCH seller from the bulletin board, she sends an encrypted message: "I need payment details for covenant xyz789." The seller's bot responds instantly with bank account info. **Total latency: under 1 second.** María never sees a phone number, email, or centralized chat interface—just payment instructions delivered directly to her app.

---

## The Coordination Problem

**After the bulletin board, before the payment, there's a gap:**

```
1. María finds seller on bulletin board ✅
2. María creates covenant on-chain ✅
3. María needs seller's payment details ❓
   - Bank account number
   - Payment reference code
   - Exact amount to send
4. María pays via Bizum ✅
5. Seller's bot locks BCH ✅
```

**Step 3 is the coordination problem.** The bulletin board listing can't include bank account numbers (privacy risk, changes frequently). The blockchain can't store sensitive data (public forever). Email/SMS requires phone numbers (KYC, surveillance, censorship).

**Nostr solves this:** Encrypted, peer-to-peer, instant delivery of payment details. No phone number needed. No central server to block. No permanent record on the blockchain.

---

## How Nostr Works (The Basics)

### Keys Instead of Accounts

Unlike traditional messaging apps (WhatsApp, Signal), Nostr has **no accounts, no phone numbers, no registration.**

**You have two keys:**
- **Private key:** Like your password. Never share it. Stored encrypted on your phone.
- **Public key:** Like your username. Share it freely. This is how people message you.

**Your public key is your Nostr identity.** It looks like this:
```
npub1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t1u2v3w4x5y6z
```

No company controls it. No telecom can block it. No government can seize it.

---

### Relays Deliver Messages

Nostr messages are sent to **relays**—simple servers that receive and forward messages. Think of them like post offices, but:

- **No single relay is required:** Messages are sent to multiple relays simultaneously
- **Relays can't read your messages:** End-to-end encryption (only sender and recipient have keys)
- **Anyone can run a relay:** No permission needed, no company required
- **Relays are cheap:** A VPS costs €5/month, handles thousands of users

**Asgaya doesn't run relays.** We use public Nostr relays (Damus, Primal, etc.) that already exist. If one relay goes down, messages route through others. If all public relays are blocked, users can run their own.

---

### Encrypted Direct Messages (NIP-04)

When María sends a message to the seller:

```
1. María's app encrypts message with seller's public key
2. App sends encrypted message to 3-5 Nostr relays
3. Relays forward to anyone subscribed to seller's public key
4. Seller's bot decrypts message with seller's private key
5. Seller's bot encrypts response with María's public key
6. Response delivered to María's app via relays
7. María's app decrypts response

Total time: <1 second
```

**The relays never see the content.** They only see:
- Sender's public key (npub1...)
- Recipient's public key (npub1...)
- Encrypted blob of data
- Timestamp

They can't read the payment details, the bank account, or any sensitive information.

---

## How Asgaya Uses Nostr

### Step 1: Bulletin Board Discovery

María queries the bulletin board and finds a seller:

```
Seller listing:
├─ Payment methods: Bizum, SEPA
├─ Fee: 0.5%
├─ Buffer: 7%
└─ Contact: npub1seller... (Nostr public key)
```

The seller's **Nostr public key** is their contact info. No phone number, no email, no company-mediated chat.

---

### Step 2: Payment Info Request

María's app sends an encrypted Nostr message:

```json
{
  "type": "payment_info_request",
  "covenant_id": "covenant_xyz789",
  "amount_eur": 100,
  "payment_method": "Bizum",
  "sender_pubkey": "npub1maria..."
}
```

**Encrypted with seller's public key.** Only the seller's bot can decrypt it.

---

### Step 3: Seller Bot Response

The seller's bot (running on their phone or VPS) receives the request and responds:

```json
{
  "type": "payment_info_response",
  "covenant_id": "covenant_xyz789",
  "payment_method": "Bizum",
  "account_number": "+34-612-345-678",
  "reference": "ASGAYA-XYZ789",
  "amount_exact": "€100.00",
  "expires_at": "2026-06-10T15:30:00Z"
}
```

**Encrypted with María's public key.** Only María's app can decrypt it.

**Delivered in under 1 second.** María sees payment instructions immediately.

---

### Step 4: María Pays

María's app displays:

```
Pay €100.00 to:
  Phone: +34-612-345-678
  Reference: recipient cash account
  
Tap to open Bizum app →
```

María completes the payment. The seller's bot detects it (bank notification), locks BCH into the covenant. Elena can now claim her remittance.

---

## Why Nostr Instead of Alternatives

### Why Not Email?

- ❌ Requires email registration (KYC in some countries)
- ❌ Slow (minutes, not seconds)
- ❌ Centralized (Gmail, Outlook can censor or monitor)
- ❌ Not designed for automated bots

**Nostr:**
- ✅ No registration (just generate keys)
- ✅ Instant (sub-second)
- ✅ Decentralized (no single company)
- ✅ Bot-friendly (relays designed for programmatic access)

---

### Why Not Telegram/WhatsApp?

- ❌ Requires phone number (KYC, SIM card, telecom surveillance)
- ❌ Centralized (company can ban users, block messages)
- ❌ Not designed for automated payment info exchange
- ❌ Limited API access (bots restricted)

**Nostr:**
- ✅ No phone number (just public key)
- ✅ Decentralized (no company to ban you)
- ✅ Designed for automation (relays are APIs)
- ✅ Bot-native (no restrictions)

---

### Why Not On-Chain (OP_RETURN)?

**OP_RETURN approach:** Seller posts encrypted payment info as a blockchain transaction.

**Pros:**
- ✅ Maximum censorship resistance (on BCH blockchain)
- ✅ No external dependencies

**Cons:**
- ❌ Slower (~2 minutes for one block confirmation)
- ❌ Costs the seller €0.002 per covenant
- ❌ Permanent on-chain storage (privacy risk if encryption breaks in 20 years)
- ❌ More complex implementation

**Nostr:**
- ✅ Faster (<1 second)
- ✅ Free (relays don't charge)
- ✅ Ephemeral (messages not stored permanently)
- ✅ Simpler (standard protocol, existing libraries)

**Trade-off:** Nostr has *good* censorship resistance (multiple relays, anyone can run one) vs OP_RETURN's *maximum* censorship resistance (BCH blockchain). For Phase 0, good is enough. We can add OP_RETURN fallback in Phase 1+ if needed.

---

## Nostr Key Management in Asgaya

**Your wallet already has BCH keys.** Nostr adds a separate key pair:

```
Your Asgaya wallet contains:
├─ BCH private key (for signing transactions)
├─ BCH public key (your address)
├─ Cash Account (Elena#142)
├─ Nostr private key (for decrypting messages)
└─ Nostr public key (your messaging identity)
```

**Why separate keys?**
- **Security:** If Nostr key is compromised, your BCH funds are safe
- **Privacy:** Nostr messages don't link directly to BCH transactions
- **Flexibility:** Can change Nostr key without affecting BCH wallet

**Generated together:** When you create your Asgaya wallet, both keys are generated from the same 12-word recovery phrase. Restore your wallet, restore both.

---

## Privacy: What Nostr Reveals

**Visible to relays (public):**
- ✅ Your Nostr public key
- ✅ Recipient's Nostr public key
- ✅ Timestamp
- ✅ Encrypted message (unreadable blob)

**Not visible to relays:**
- ❌ Message content (encrypted)
- ❌ Payment details (encrypted)
- ❌ Your real identity (unless you dox yourself)
- ❌ Your BCH address (separate key)

**Visible to recipient (seller's bot):**
- ✅ Message content (they decrypt it)
- ✅ Covenant ID (links message to on-chain covenant)

**Metadata analysis risk:** A relay operator who logs all messages could see that npub1maria sent a message to npub1seller, guess it's payment coordination, and infer a transaction happened. But they can't read the content or link it to BCH addresses.

**Phase 0 acceptable.** Phase 1+ we can add onion routing or mix networks if metadata analysis becomes a concern.

---

## Liveness Check: Proof the Seller is Online

**A critical side benefit:** When the seller's bot responds with payment info, it proves the seller is online and ready to lock BCH.

**Without liveness check:**
```
1. María creates covenant
2. María pays seller via Bizum
3. Seller's bot is offline (phone died, server crashed)
4. María waits... seller never locks BCH
5. María stuck (paid fiat, covenant expires, funds returned after 48h)
```

**With Nostr liveness check:**
```
1. María creates covenant
2. María sends Nostr message
3. No response within 2 minutes → Warning: "Seller may be offline"
4. María cancels covenant, picks different seller
5. No fiat payment wasted
```

**Result:** María never pays fiat unless the seller proves they're online and ready.

---

## How Nostr Connects to the Other Gears

### Bulletin Board → Nostr

**The bulletin board is discovery.** It tells you *who* is selling BCH (their Nostr public key).

**Nostr is coordination.** It tells you *how* to pay them (bank account, reference code).

**Separation of concerns:**
- Bulletin board: Public, permanent, on-chain (listings visible forever)
- Nostr: Private, ephemeral, off-chain (payment details disappear after use)

---

### Wallet → Nostr

**Your wallet generates Nostr keys.** When you create an Asgaya wallet, you get:
- BCH keys (for transactions)
- Cash Account (for identity)
- Nostr keys (for messaging)

**All linked, all restored from 12 words.**

---

### Nostr → Notification Bot

**The seller's notification bot listens on Nostr.** When a payment info request arrives:
1. Bot decrypts message
2. Bot generates payment details (unique reference code per covenant)
3. Bot sends encrypted response
4. Bot watches for Bizum payment with that reference code
5. Bot locks BCH when payment detected

**Nostr is the bot's communication channel.** Without Nostr, the bot can't respond to senders.

---

## User Experience: What María Sees

María doesn't know Nostr exists. The app hides all complexity:

```
María's screen:
┌─────────────────────────────┐
│  Select Seller:             │
│                              │
│  ✅ seller$asgaya.org        │
│     Fee: 0.5%                │
│     Response time: 12 sec    │
│     [Select] ←              │
│                              │
│  Fetching payment details... │← Nostr happening here
│                              │
│  Pay €100.00 to:             │
│    Phone: +34-612-345-678    │
│    Reference: ASGAYA-XYZ789  │
│                              │
│  [Open Bizum App]            │
└─────────────────────────────┘
```

**"Fetching payment details..."** = Nostr message sent, response received, decrypted. Happens in <1 second. María thinks it's just the app loading data.

**She never sees:**
- Public keys (npub1...)
- Relay selection
- Encryption/decryption
- Message routing

**Just results:** Payment instructions, ready to use.

---

## Seller Experience: What the Bot Does

The seller doesn't manually respond to messages. **The bot automates everything:**

**Setup (one time):**
1. Seller installs Asgaya app or runs bot on VPS
2. App generates Nostr keys
3. Seller posts bulletin board listing with Nostr public key
4. Bot connects to 3-5 Nostr relays, subscribes to messages

**Runtime (automated):**
1. Bot receives encrypted message
2. Bot decrypts with private key
3. Bot validates covenant exists on-chain
4. Bot generates unique payment reference code
5. Bot sends encrypted response
6. Bot watches for bank notification matching reference
7. Bot locks BCH when payment detected

**Seller's job:** Keep phone/server online. Bot handles everything else.

---

## Nostr Features in Phase 0

**What's ready:**
- ✅ Nostr key generation and management
- ✅ Encrypted direct messages (NIP-04)
- ✅ Payment info request/response flow
- ✅ Multi-relay support (automatic failover)
- ✅ Liveness check (2-minute timeout)

**What's coming (Phase 1+):**
- User-to-user messaging (ask seller questions before committing)
- Group chat (community coordination)
- Dispute resolution chat (mediation if payment fails)
- Reputation comments (public reviews linked to Nostr identity)
- Nostr-native reputation (web of trust)

**Phase 0 keeps it simple:** Automated payment info exchange only. No manual messaging. Bot-to-bot coordination.

---

## What Happens If Nostr Relays Go Down

**Scenario 1: One relay is down**

**Impact:** None. Messages automatically route through other relays.

**Asgaya default:** Connect to 5 relays (Damus, Primal, Nostr.wine, relay.snort.social, custom relay). If 1-2 fail, 3-4 still work.

---

**Scenario 2: All public relays are blocked (censorship attack)**

**Mitigation:**
1. Seller runs own relay on VPS (€5/month)
2. Bulletin board listing includes custom relay URL
3. Sender's app connects to seller's relay
4. Messages delivered peer-to-peer

**Still works.** Just requires sellers to run infrastructure (Phase 1+ feature).

---

**Scenario 3: Complete Nostr network failure (catastrophic)**

**Fallback (Phase 1+):**
- Use OP_RETURN on BCH blockchain for payment info
- Slower (~2 minutes), costs €0.002, but still works
- Maintains censorship resistance

**Phase 0:** If Nostr completely fails, Asgaya pauses until relays recover. Acceptable risk—Nostr has been running reliably since 2022.

---

## Security Model

**What an attacker CAN'T do:**
- ❌ Read your messages (end-to-end encrypted)
- ❌ Impersonate the seller (needs seller's private key)
- ❌ Tamper with payment details (detected by signature verification)
- ❌ Block all messages (would need to control all relays)

**What an attacker CAN do:**
- ⚠️ Log metadata (npub1maria messaged npub1seller at timestamp X)
- ⚠️ Correlate messages with on-chain covenants (timing analysis)
- ⚠️ Run malicious relays that don't forward messages (sender switches to different relay)

**Phase 0 mitigation:**
- Use multiple relays (redundancy)
- Rotate Nostr keys periodically (reduces long-term metadata correlation)
- No real names in Nostr identity (pseudonymous by default)

**Phase 1+ mitigation:**
- Onion routing (hide metadata from relays)
- Mix networks (decorrelate timing)
- Tor integration (hide IP address)

---

## Why Nostr is a Big Deal Beyond Asgaya

**Nostr isn't just for Asgaya.** It's a general-purpose decentralized messaging protocol used by:
- Social media (Damus, Primal, Amethyst apps)
- Bitcoin Lightning wallets (payment coordination)
- Marketplaces (OpenBazaar-style P2P commerce)
- Identity systems (web of trust, reputation)

**By using Nostr, Asgaya plugs into an existing ecosystem:**
- Relays already running (no infrastructure cost)
- Libraries already built (faster development)
- Users already familiar (if they use Damus/Primal)
- Network effects (more relays = more resilience)

**Asgaya improves Nostr too:**
- New use case (payment coordination)
- More relay traffic (justifies relay operators' costs)
- More developers building Nostr tools (benefits everyone)

**Mutual benefit.** We're not building in isolation.

---

## Comparison to Other Coordination Methods

| Feature | Nostr DMs | OP_RETURN | Email | Telegram |
|---------|-----------|-----------|-------|----------|
| **Speed** | <1 second | ~2 minutes | Minutes-hours | <1 second |
| **Cost** | Free | €0.002/message | Free | Free |
| **Privacy** | End-to-end encrypted | End-to-end encrypted | Not encrypted (unless PGP) | Encrypted (server can read) |
| **Censorship resistance** | Good (multiple relays) | Maximum (blockchain) | Poor (centralized) | Poor (centralized) |
| **Requires** | Nostr keys | BCH transaction | Email account | Phone number |
| **Automation** | Bot-native | Bot-native | Limited | Restricted |
| **Permanence** | Ephemeral | Permanent on-chain | Depends on server | Depends on server |

**Nostr wins on speed, cost, and automation.** OP_RETURN wins on maximum censorship resistance. Email and Telegram lose on privacy and decentralization.

**Phase 0 uses Nostr.** Phase 1+ adds OP_RETURN fallback if needed.

---

## Common Questions

**Q: What if the seller's Nostr key is compromised?**

**A:** Attacker can read incoming payment info requests and send fake payment details. But:
- María's fiat payment goes to seller's real bank account (not attacker's)
- Seller's bot still detects the payment and locks BCH
- María gets her remittance, attacker gains nothing
- Seller should rotate Nostr keys periodically (Phase 1+ feature)

**Q: Can I use Nostr for regular messaging (not just payment info)?**

**A:** Phase 1+, yes. Phase 0, no—Nostr is automated for bot coordination only. But if you have a Nostr client app (Damus, Amethyst), you can message other users using your Asgaya Nostr keys.

**Q: What if relays censor me?**

**A:** Switch to different relays. If all public relays censor you, run your own (€5/month VPS). Can't be stopped.

**Q: Does Nostr leak my BCH address?**

**A:** No. Nostr keys are separate from BCH keys. Even if someone knows your Nostr public key, they can't derive your BCH address from it.

**Q: What's the difference between Nostr and Signal/WhatsApp?**

**A:** Signal/WhatsApp require phone numbers, run on centralized servers (company can block you), and aren't designed for automated bots. Nostr has no phone numbers, no central server, and is bot-native.

---

## Key Takeaways

1. **Encrypted messaging layer** — Private coordination between buyers and sellers
2. **Nostr protocol** — Decentralized, relay-based, end-to-end encrypted
3. **Payment info exchange** — Delivers bank account details in under 1 second
4. **Liveness check** — Proves seller is online before sender pays fiat
5. **No phone number required** — Just public keys (pseudonymous by default)
6. **Bot-native** — Automated responses, no manual intervention
7. **Ephemeral** — Messages don't live on blockchain permanently (privacy)

**Nostr is the invisible glue.** The bulletin board discovers sellers. Nostr coordinates the payment. The covenant executes the trade. Users never see Nostr—just instant payment instructions.

When regulators ask "where's the chat server we can subpoena?" there isn't one. It's just relays forwarding encrypted blobs. No company, no database, no records to seize.

That's the point.
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ The Mechanism](../README.md)** | **[📖 Glossary](../../glossary.md)**

**In this section:**
- [Device Health Checks](device-health.md) - Proactive fraud prevention
- [Dispute Resolution](dispute-resolution.md) - What happens when trades fail

**Related:** [Wallet](../wallet/README.md) · [Bulletin Board](../bulletin-board/README.md) · [Notification Bot](../notification-bot/README.md) · [Stability Layer](../stability-layer/README.md)
