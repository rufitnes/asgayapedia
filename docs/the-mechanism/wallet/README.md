# The Wallet: Your BCH, Your Identity
**📖 Unfamiliar terms?** See the [glossary](../../glossary.md) for definitions.

**Non-custodial. Pseudonymous. Open source.** Your keys, your Cash Account, your control.

---

## What It Is

A standard Bitcoin Cash wallet with one critical addition: **Cash Accounts as your identity layer.** Not a bank account. Not a company holding your funds. Just a regular BCH wallet where you control the keys, paired with a human-readable name system that replaces phone numbers and long addresses.

When María sends money to Elena, she types `Elena#142` instead of a 42‑character address. The wallet resolves it on‑chain and completes the payment.

---

## Two Core Functions

**1. Hold Your Bitcoin Cash** – Send, receive, check balance, view history. The app generates a 12‑word recovery phrase on first launch. Write it down; that phrase restores everything. No company can freeze or block your funds.

**2. Establish Your Identity with Cash Accounts** – Register a name like `Elena#142` on the blockchain. It’s human-readable, easy to share, and stored on‑chain forever. No phone number, email, or KYC required.

---

## Cash Accounts: How They Work

**Registration (one‑time):** Pick a name, the app creates an on‑chain registration transaction (~€0.002 fee). You get a unique number (e.g., `Elena#142`). The name is yours permanently.

**Using them:** Share your Cash Account to receive. To send, type the recipient’s Cash Account; the wallet looks up their BCH address from the blockchain and completes the transaction. No phone number, no SMS, no identity verification.

**Privacy:** Your Cash Account name and linked address are public. Your real identity, transaction history, phone number, and location are not—unless you choose to reveal them.

---

## Why Cash Accounts: Bridge between legacy payment systems and BCH

Traditional remittance apps require phone numbers, KYC, and often unreliable SMS delivery. In Venezuela, SIM cards can be blocked, phone service is unstable, and telecom surveillance is real. Cash Accounts need none of that: they run on the BCH blockchain, are permissionless, and are pseudonymous by default. Even if a recipient’s phone service is cut off, their Cash Account still works.

Cash Accounts allow us to use the concept field of the transfers to match a fiat payment to a BCH address, this approach eliminates the need of a data base, random codes and a mapping layer.

Cash Account serves four roles simultaneously:

1. User identifier (María types Elena#142)
2. Covenant field (Elena's BCH address as recipient)
3. Payment reference (in Bizum description)
4. Bot lookup key (match bank payment → on-chain covenant)

All from **ONE on-chain** identifier. This is why it works without a central coordinator.

---

## How the Wallet Connects to the Other Gears

The wallet is the foundation for the other three Asgaya components.

- **Bulletin Board:** Your Cash Account is your contact info on listings. Sellers and buyers are found and contacted via Cash Accounts, all on‑chain.
- **Nostr:** Your wallet generates a Nostr key pair. When you select a counterparty, you message them over Nostr—encrypted, peer‑to‑peer, no server.
- **Notification Bot:** You grant the bot limited wallet access (read‑only balance, sign‑only for specific covenants). It watches for incoming payments and auto‑signs covenants when conditions are met.

---

## Wallet Features

**Phase 0 (ready):**
- Standard BCH wallet (send, receive, balance)
- Cash Account registration and resolution
- QR code generation (with embedded Cash Account)
- Fiat balance display (BCH → EUR, VES, USD)
- Transaction history with covenant status
- Recovery phrase backup and restore

**Phase 1+ (planned):**
- Multiple Cash Accounts per wallet
- Cash Account transfer / selling
- Reputation score display
- Multi‑sig support

---

## Recovery

**You have your 12‑word recovery phrase:** Restore on any device; your BCH and Cash Account reappear (the blockchain remembers the name).

**You lost your recovery phrase:** The BCH is gone. The Cash Account name remains on‑chain but can’t be accessed. Write down the 12 words; that’s the only backup.

---

## Security Model

The app knows your public keys, transaction history, and Cash Account. It never knows your recovery phrase, private keys, or PINs. All sensitive data stays encrypted on your phone. The code is open source; anyone can audit it to verify there’s no key leakage.

---

## Why Bitcoin Cash

| Feature | BCH | BTC |
|---------|-----|-----|
| **Transaction fees** | ~€0.002 | €5–50 |
| **Covenant support** | Native | None (requires centralized escrow) |
| **Cash Account cost** | ~€0.002 one‑time | Not natively supported |
| **Design philosophy** | Peer‑to‑peer electronic cash | Digital gold |

BCH is the only Bitcoin that can scale for €100 remittances with sub‑cent fees and covenant‑enforced trustlessness.

---

## Comparison to Other Wallets

| Feature | Traditional BCH Wallet | Asgaya Wallet |
|---------|------------------------|---------------|
| Send/Receive BCH | ✅ | ✅ |
| Non‑custodial | ✅ | ✅ |
| Cash Accounts | Some support | Native, required |
| Covenant creation | Manual | One‑tap |
| Bulletin board discovery | No | Built‑in |
| Nostr messaging | No | Integrated |
| Notification bot | No | Automated |

Asgaya is a specialised wallet for remittances. Your keys work in any BCH wallet—no lock‑in.

---

## Key Takeaways

1. **Non‑custodial** — Your keys, your coins. No intermediary.
2. **Cash Accounts as identity** — Human‑readable, blockchain‑registered, censorship‑resistant.
3. **Pseudonymous by default** — Use nicknames or real names, your choice.
4. **Foundation for the other gears** — Bulletin board, Nostr, and notification bot all depend on the wallet.
5. **Interoperable** — Keys and Cash Accounts work anywhere on BCH.
6. **Open source** — Audit the code; no key leakage possible.

---

**Next:** [Nostr](nostr.md) — Private coordination between buyers and sellers  
**Related:** [Bulletin Board](bulletin-board.md) — Where listings are discovered  
**Related:** [Notification Bot](notification-bot.md) — Automation that makes the system run
