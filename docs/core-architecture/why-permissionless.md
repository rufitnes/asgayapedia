# Why: Permissionless

**Core Requirement:** Anyone can participate without KYC, using minimal hardware and knowledge. Asgaya isn't an entity—it's an open-source protocol anyone can participate in, build on, and develop.

**Key principle:** Maximum freedom, minimum risk. No KYC; secure key management education designed to avoid errors.

---

## The Problem

**Traditional remittances are gatekept systems.**

To send money via Western Union, you need:
- Government-issued ID (excludes refugees, undocumented migrants)
- Physical access to an office (excludes rural areas)
- Bank account (excludes 1.7 billion unbanked people)
- Internet access during business hours (excludes poor connectivity areas)
- Literacy to fill out forms (excludes ~773 million adults globally)

Even cryptocurrency "solutions" often require:
- KYC verification on exchanges (same exclusions as above)
- Expensive smartphones (iPhone, high-end Android)
- Deep understanding of wallets, keys, addresses
- Reliable internet (excludes intermittent connectivity areas)

**Asgaya removes these barriers entirely.**

---

## Sub-Requirements

To be truly permissionless, Asgaya must satisfy these requirements:

### 1. No KYC Requirement

**Requirement:** No participant (sender, recipient, merchant, LP, escrow) needs to provide government ID or personal information to use Asgaya.

**Why this matters:** KYC excludes the people who need remittances most—refugees without ID, undocumented workers who fear deportation, the 1.7 billion unbanked people trapped in catch-22s, dissidents under surveillance.

**The leverage model:** 10 escrow operators with KYC → 10,000 recipients without KYC. The burden is concentrated where it's voluntary, while end users have zero barriers.

**Success metric:** Verified usage by users who cannot access legacy remittances due to lack of ID.

**Deep dive:** [Why: No KYC](core-architecture/why-no-kyc.md) — Who KYC excludes, why financial access is a human right, the 1:1000 leverage ratio

---

### 2. Works Offline / Low Connectivity

**Requirement:** Users in areas with intermittent internet access must be able to send and receive remittances.

**Why this matters:**
- In some parts of the world power outages are common
- Even if power is reliable cell coverage might be intermitent
- Emergency situations: Natural disasters, infrastructure failures, conflicts

**How it works:**
- App syncs once daily when internet is available
- Transactions can be initiated offline and completed when connectivity returns
- Escrow operators handle coordination during offline periods

**Success metric:** Verified usage in low-connectivity areas

**How we achieve this:** [Permissionless Access](core-architecture/permissionless-access.md)

---

### 3. Minimal Hardware Required

**Requirement:** Support the widest possible range of devices, from cardboard QR codes to RFID stickers to basic smartphones.

**Why this matters:** Hardware requirements exclude the poorest users—those who need remittances most. If we require €100 smartphones, we've built "better Western Union for people with smartphones," not financial access for everyone.

**The tiered model:**
- Cardboard QR (€0): Can receive money
- RFID sticker (€5): More durable, tap-to-pay
- Smartphone (€100): Full functionality

**The principle:** If it works for someone with nothing, it works for everyone. Design for the edge case, not the average.

**Success metric:** Verified usage with cardboard QR codes and RFID stickers in at least one corridor.

**Deep dive:** [Why: Minimal Hardware](core-architecture/why-minimal-hardware.md) — The cardboard QR code philosophy, progressive enhancement, why edge cases matter most

---

### 4. Minimal Knowledge Required

**Requirement:** Users should not need to understand cryptocurrency, blockchain, or technical concepts to use Asgaya safely.

**Why this matters:**
- **Low literacy users:** Can't read technical documentation
- **Non-technical users:** Don't care about "blockchain" "decentralization" or "cryptography"
- **Elderly users:** May struggle with complex interfaces

**How it works:**
- Simple UI with familiar currency terminology (EUR, VES, ARS)
- No crypto jargon ("blockchain", "private key", "address")
- Guided flows with auto-calculation and auto-complete
- Visual indicators (checkmarks, progress bars) instead of text

**Example:** Instead of "Enter BCH address," show "Scan QR code" with camera icon.

**Success metric:** Non-crypto users can explain the system to others (word-of-mouth adoption).

**How we achieve this:** [Minimal Knowledge](core-architecture/minimal-knowledge.md)

---

### 5. Self-Custody (Users Control Keys)

**Requirement:** Users must hold their own BCH keys. Asgaya has zero access to user funds.

**Why this matters:** Custodial = permissioned by definition. If Asgaya held funds, we'd need licenses (payment institution, money transmitter), which require KYC, which excludes the unbanked. Self-custody is the only way to stay truly permissionless.

**The trade-off:** Users gain sovereignty (no one can freeze their accounts) but lose safety net (lost keys = lost funds forever).

**User choice preserved:** Don't want responsibility? Cash out immediately. Want sovereignty? Hold BCH and accept the risk.

**Success metric:** <1% of users losing funds due to lost keys (balanced with security).

**Deep dive:** [Why: Self-Custody](core-architecture/why-self-custody.md) — Why custodial is worse, "not your keys not your coins," harsh reality as feature not bug

---

### 6. Mandatory Key Backup Education

**Requirement:** Users must prove they've backed up their keys before making their first transaction.

**Why this matters:** Self-custody is worthless if users lose their keys. The #1 cause of crypto fund loss is lost/forgotten keys, not hacks.

**How it works:**
- On first launch, app generates BCH wallet
- User must write down 12-word recovery phrase
- User must re-enter 3 random words to prove they wrote it down
- Only then can they receive/send BCH

**Success metric:** >80% of users with verified backups.

---

### 7. Multiple Key Storage Options

**Requirement:** Support different backup methods for different risk profiles.

**Why this matters:**
- **Low-risk users** (small amounts): Encrypted phone storage is fine
- **Medium-risk users** (moderate amounts): Paper backup at home
- **High-risk users** (large amounts): Metal backup in safe or split between locations

**How it works:**
- Encrypted phone storage (default)
- Paper backup (recommended)
- Metal backup (advanced users)
- Multisig support (V1.1)

**Success metric:** Users can choose backup method based on amount stored.

**How we achieve this:** [Key Safekeeping](core-architecture/key-safekeeping.md)

---

### 8. Error Prevention

**Requirement:** Design the system to prevent common user errors (wrong amount, wrong recipient, wrong currency).

**Why this matters:** Financial transactions are high-stakes. A single typo can lose someone's rent money.

**How it works:**
- QR codes eliminate manual address entry
- Guided flows show each step clearly
- Auto-calculation of exchange rates (no manual math)
- Auto-complete for common amounts (€50, €100, €200)
- Confirmation screens before irreversible actions

**Success metric:** <1% error rate on transactions.

**How we achieve this:** [Error Mitigation](core-architecture/error-mitigation.md)

---

## Why "Permissionless" Matters

**It's not just about access—it's about freedom.**

When systems require permission (KYC, bank accounts, expensive hardware), they create:
- **Exclusion:** Billions of people are locked out
- **Surveillance:** Governments track every transaction
- **Censorship:** Accounts can be frozen arbitrarily
- **Control:** Central authorities decide who can participate

Asgaya inverts this:
- **Inclusion:** Anyone with a phone (or even a QR code) can participate
- **Privacy:** Peer-to-peer coordination, no central database
- **Censorship-resistance:** No central authority to freeze accounts
- **Freedom:** Open-source protocol, anyone can build on it

**We're not asking permission to build a better financial system. We're just building it.**

---

## Trade-offs

Permissionless design requires accepting some trade-offs:

1. **Security vs. Usability:** Mandatory key backups add friction, but prevent fund loss
2. **Privacy vs. Compliance:** No KYC means users are responsible for their own compliance
3. **Simplicity vs. Flexibility:** Limited features (by design) to avoid overwhelming users
4. **Accessibility vs. Perfection:** Support low-tech edge cases even if not "ideal"

These trade-offs are documented in detail in the **Decisions** section.

---

## Low-Tech Edge Cases Are Given Serious Consideration

Unlike most crypto projects that assume "everyone has a smartphone," Asgaya designs for the worst-case scenario:

- What if the user has no phone? → **Cardboard QR code**
- What if there's no internet? → **Offline sync mode**
- What if they can't read? → **Visual-first UI**
- What if they don't understand crypto? → **No crypto jargon**

**If it works for the lowest-tech user, it works for everyone.**
The protocol is unnecesary complicated because of the conditions it has to meet to work but for the casual user it has to feel like sending money from A to B not more than that.

---

## Related Requirements

- [Why: Cheaper Than Legacy](core-architecture/why-cheaper-than-legacy.md) — Permissionless design reduces compliance costs
- [Why: Promote Adoption](core-architecture/why-promote-adoption.md) — Low barriers enable rapid network growth

---

## Trade-offs and Decisions

See the **Decisions** section for detailed documentation of the trade-offs made to achieve this requirement:

- **[Bizum Concept Field](decisions/bizum-concept-field.md)** — Why phone numbers instead of semantic IDs (bank constraints)
- **[Payment Timeout Window](decisions/payment-timeout-window.md)** — Why 10 minutes accommodates SMS delays and encoding issues
- Decision: QR codes vs NFC (coming soon)
- Decision: No universal merchant API (coming soon)

---

*Last updated: April 30, 2026*
