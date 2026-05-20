# Concept: RFID Card Recipients

**Status:** ⚠️ **FUTURE ENHANCEMENT (Phase 1+)** - Not implemented in Phase 0  
**Related Requirement:** [Minimal Hardware](../core-architecture/why-minimal-hardware.md)

---

## What It Is

An alternative recipient identity system using RFID cards instead of smartphones, enabling remittance recipients to cash out without owning any personal device.

**Core idea:** Recipient carries an RFID card (like a payment card) that stores their cryptographic signature. When cashing out, they tap the card on the merchant's NFC-enabled device to authorize the transaction.

---

## Why This Exists

### Problem: Smartphone Ownership Barrier

**Phase 0 assumption:** Recipients have Android smartphones with Asgaya app installed.

**Reality in target markets:**
- Not everyone can afford smartphones (Venezuela, rural areas)
- Elderly recipients may not use smartphones
- Some recipients share family devices (not always available)
- Smartphone ownership = barrier to remittance access

**Result:** Excluding potential users who need remittances most.

---

### Solution: RFID Card as Cryptographic Identity

**RFID card provides:**
- ✅ **True minimal hardware** (card costs $0.50-2)
- ✅ **No internet required** for recipient
- ✅ **No smartphone ownership needed**
- ✅ **Cryptographically secure** (merchant can't fake)
- ✅ **Accessible** (tap is simpler than app)
- ✅ **Merchant-assisted onboarding** (lower barrier)

**Makes Asgaya accessible to ANYONE, regardless of device ownership.**

---

## How It Works

### Recipient Flow (RFID Card)

```
1. Sender sends €100 via Bizum to escrow
2. Escrow notifies recipient via SMS: "Code: 847293"
3. Recipient goes to merchant with RFID card
4. Recipient tells merchant: "Code 847293"
5. Merchant enters code → sees €100 ready
6. Merchant accepts, hands cash
7. Merchant device generates completion code: 625104
8. Recipient taps RFID card on merchant device (NFC)
9. Card cryptographically signs transaction with completion code
10. Merchant device verifies signature → Transaction complete ✅
```

**Key difference from smartphone flow:**
- No app needed
- No completion code entry
- Just **tap card** → done

---

### Merchant Flow (With RFID Recipient)

```
MERCHANT DEVICE:
1. Recipient: "I have transfer, code 847293"
2. Merchant enters 847293
3. App shows: "€100 cash-out, €0.247 earnings"
4. Merchant taps [Accept]
5. App generates completion code: 625104
6. Merchant hands cash to recipient
7. App shows: "Ask recipient to TAP their Asgaya card"
8. Recipient taps RFID card on device (NFC)
9. Device reads card signature
10. Device verifies signature matches completion code
11. Transaction completes → BCH sent to merchant ✅
```

**Merchant experience:**
- Almost identical to smartphone flow
- Just "tap card" instead of "enter code"
- NFC read happens automatically

---

## RFID Card Provisioning (Onboarding)

### How Recipients Get RFID Cards

**Option 1: Merchant-Assisted Onboarding (Preferred)**

```
1. Recipient goes to merchant with blank RFID card
2. Merchant opens Asgaya app → "Onboard New Recipient"
3. Merchant hands phone to recipient
4. Recipient enters phone number (for SMS notifications)
5. Recipient taps blank RFID card on merchant device
6. App generates cryptographic key pair
7. App writes private key to RFID card
8. App sends public key to escrow (registers recipient)
9. Recipient takes card home → ready to receive remittances ✅
```

**Why merchant-assisted:**
- Recipient doesn't need to own smartphone
- Merchant already has NFC device
- Merchant earns small fee (€0.50 onboarding reward)
- Builds merchant-recipient trust relationship
- Lower barrier than "download app yourself"

---

**Option 2: Self-Service Kiosk (Future)**

```
1. Recipient goes to kiosk location (community center, shop)
2. Enters phone number
3. Taps blank RFID card
4. Kiosk writes key, registers recipient
5. Pays small fee (€1) or free for first-time users
```

**Why later:**
- Requires kiosk hardware deployment
- More infrastructure than merchant-assisted
- Good for scaling, not MVP

---

**Option 3: Family Member Assistance**

```
1. Sender (in Spain) has Asgaya app
2. Sender provisions RFID card for recipient (in Venezuela)
3. Sender mails card to recipient
4. Recipient receives card → ready to use
```

**Why useful:**
- Sender already has app
- Can provision remotely
- Recipient just receives card in mail

---

## Security Model

### Cryptographic Key Storage

**RFID card contains:**
- Private key (Ed25519 or secp256k1)
- Public key derived from private key
- Recipient phone number (optional, for verification)

**Card types:**
- NTAG216 (most common, ~$0.50)
- MIFARE Classic (older, less secure)
- MIFARE DESFire (more secure, ~$2)

**Security properties:**
- Private key never leaves card
- Card signs completion code using private key
- Merchant device verifies signature with public key (on escrow)
- Merchant cannot extract private key (read-protected)

---

### What If Card Is Lost or Stolen?

**Scenario:** Recipient loses RFID card

**Current design (Phase 1):**
- Card is bearer instrument (whoever has it can use it)
- Similar to cash or prepaid card
- Sender notified via SMS before each cash-out
- Recipient can report card lost → escrow disables public key

**Future enhancement (Phase 2):**
- PIN protection (4-6 digit PIN required before tap)
- Biometric card (fingerprint sensor on card)
- Multi-card (recipient has backup card with same key)

**Risk mitigation:**
- Transactions require SMS notification (recipient alerted)
- Merchant reputation (known merchants less likely to collude)
- Small amounts typical (€50-200, not life savings)

---

### Anti-Fraud: Merchant Can't Clone Card

**Attack scenario:** Merchant tries to clone recipient's card

**Why it fails:**
1. Private key stored in read-protected memory
2. NFC tap only allows signing, not key extraction
3. Even if merchant clones card → escrow knows recipient phone number
4. Recipient gets SMS for every transaction
5. Cloned card use → recipient reports fraud → merchant Strike 3

**Result:** Cloning not economically viable (too risky, recipient alerted immediately).

---

## Comparison: RFID Card vs Smartphone App

| Aspect | RFID Card | Smartphone App |
|--------|-----------|----------------|
| **Cost** | $0.50-2 (one-time) | $50-200 (smartphone) |
| **Internet needed** | ❌ No | ✅ Yes (at cash-out) |
| **Onboarding** | Merchant helps | Self-service download |
| **UX complexity** | Tap (simple) | Enter code (slightly more steps) |
| **Lost device** | Card lost = risk | Phone lost = bigger risk |
| **Accessibility** | Elderly-friendly | Requires tech literacy |
| **Provisioning** | Needs NFC writer | Just download app |
| **Security** | Cryptographic signature | Cryptographic signature |
| **Phase 0 support** | ❌ No | ✅ Yes |

**Trade-off:** RFID simpler/cheaper but requires merchant NFC support and onboarding infrastructure.

---

## Implementation Requirements

### Hardware Requirements

**Merchant device:**
- ✅ NFC-enabled Android smartphone (most modern phones)
- ✅ Asgaya app with NFC read/write support

**Recipient:**
- ✅ RFID card (NTAG216 or compatible)
- ❌ No smartphone needed
- ❌ No internet needed

**Escrow:**
- ✅ Database of recipient public keys
- ✅ SMS notification system (alert recipient on each tx)

---

### Software Requirements

**Asgaya Android app additions:**
- NFC card read/write library (Android NFC API)
- Key generation (Ed25519 or secp256k1)
- Card provisioning flow
- Signature verification during cash-out
- "Tap card" UI instead of "Enter code" for RFID recipients

**Escrow backend additions:**
- Store recipient public keys
- Verify RFID card signatures
- Track which recipients use RFID vs app
- Card deactivation API (if lost/stolen)

---

### RFID Card Availability (Venezuela)

**Questions to validate:**
- Are NTAG216 cards available locally?
- What's local cost? ($0.50-2 expected)
- Can merchants source cards easily?
- Alternative: Ship cards from Spain? (adds logistics)

**Phase 1 approach:**
- Order 100 cards ($50-200 total)
- Distribute to 2-3 pilot merchants
- Test provisioning flow
- Validate demand (do recipients prefer card vs app?)

---

## Trade-offs

### ✅ What We Gain

**Accessibility:**
- Reaches recipients without smartphones
- Elderly/non-tech-savvy users included
- True "minimal hardware" (not just "minimal smartphone")

**Lower barrier:**
- Recipient doesn't need to download app
- Merchant helps onboard (assisted vs self-service)
- One-time provisioning (card lasts years)

**Cost savings:**
- $1 card vs $100 smartphone
- No data plan needed
- No battery/charging concerns

---

### ❌ What We Give Up

**Infrastructure dependency:**
- Requires NFC-enabled merchant devices
- Requires card provisioning flow
- More complex onboarding than "download app"

**Lost card risk:**
- Bearer instrument (Phase 1)
- Requires PIN/biometric for better security (Phase 2)
- Recipient must protect card like cash

**Logistics:**
- Sourcing RFID cards in Venezuela
- Distributing cards to merchants
- Replacing lost/damaged cards

---

## Adoption Strategy

### Phase 0 (Not Supported)
- Smartphone app only
- 1-2 trusted recipients (likely have phones)
- Focus on core flow validation

### Phase 1 (Pilot RFID Cards)
- Order 100 NTAG216 cards
- Distribute to 2-3 merchants
- Test merchant-assisted onboarding
- Measure: Do recipients prefer card vs app?
- Metrics: Onboarding time, lost card rate, usage vs app

### Phase 2 (Scale if Validated)
- Local card sourcing (Venezuela suppliers)
- Self-service kiosks for provisioning
- PIN protection (4-digit PIN before tap)
- Multi-card support (backup cards)

### Success Criteria
- ✅ Onboarding takes <5 minutes (merchant-assisted)
- ✅ Lost card rate <5% per year
- ✅ Recipient satisfaction higher than app (elderly users)
- ✅ Merchants willing to help onboard (€0.50 reward sufficient)

---

## Future Enhancements

### PIN Protection (Phase 2)
- Recipient sets 4-6 digit PIN during provisioning
- Merchant device prompts for PIN before tap
- Prevents stolen card use
- Trade-off: Adds friction (PIN entry)

### Biometric Cards (Phase 3+)
- RFID card with fingerprint sensor
- Recipient registers fingerprint during provisioning
- Tap requires fingerprint match
- Cost: $10-20 per card (vs $1 basic card)

### Multi-Card Support
- Recipient has 2-3 cards with same cryptographic key
- If one lost → still has backup
- Provisioning writes key to multiple cards simultaneously

### Family Cards
- One sender provisions cards for multiple family members
- Each card has unique key but linked to same sender
- Sender can send to "family pool", any member can cash out

---

## Open Questions

**To validate before implementation:**

1. **Card availability:**
   - Are NTAG216 cards available in Venezuela?
   - Local cost? Shipping cost from Spain?
   - Reliable suppliers?

2. **NFC support:**
   - What % of Venezuelan Android phones have NFC?
   - Are merchant devices NFC-enabled?
   - Fallback if no NFC? (External NFC reader dongle?)

3. **Demand validation:**
   - Do recipients actually want cards vs apps?
   - Is smartphone ownership the barrier we think it is?
   - Would elderly recipients use cards?

4. **Security vs accessibility:**
   - Is PIN required (Phase 1) or optional (Phase 2)?
   - How much friction is acceptable?
   - Lost card protocol (freeze immediately vs grace period?)

---

## Related Concepts

- [Pull System](pull-system.md) — Works with RFID cards (recipient taps to confirm)
- [Minimal Hardware](../core-architecture/why-minimal-hardware.md) — RFID cards make this claim TRUE

---

## Related Decisions

- [Dispute Resolution](../decisions/dispute-resolution.md) — RFID tap = cryptographic proof of presence
- [Merchant Flows](../android-app/flows/merchant-flows.md) — Alternative flow for RFID recipients

---

## References

**Technical:**
- NTAG216 datasheet (NXP Semiconductors)
- Android NFC API documentation
- Ed25519 signature scheme

**Similar implementations:**
- Public transit cards (cryptographic tap)
- Contactless payment cards (Visa/Mastercard)
- Access control cards (building entry)

---

## Status

**Current:** Concept documented, not implemented  
**Next steps:**
1. Validate card availability in Venezuela (research)
2. Prototype NFC provisioning flow (development)
3. Order 100 test cards (procurement)
4. Pilot with 2-3 merchants (validation)
5. Measure adoption vs smartphone app (metrics)

**Decision gate:** Only implement if demand validated + cards available + merchant NFC support confirmed.

---

*Concept proposed: May 2026*  
*Status: Future Enhancement (Phase 1+)*  
*Blocker: None (cards available globally, Android NFC common)*
