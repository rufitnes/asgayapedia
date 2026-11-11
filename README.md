# Asgayapedia

**The public documentation of Asgaya — a research project on how to bootstrap Bitcoin Cash merchant adoption.**

**Docs:** [docs.asgaya.org](https://docs.asgaya.org/) · **For AI:** [raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/index.md](https://raw.githubusercontent.com/rufitnes/asgayapedia/main/docs/index.md)

---

> **Asgaya is a research project, not a company.** It investigates one question: **what makes a merchant accept Bitcoin Cash, and how do you bootstrap that adoption?** The thesis: **remittances first, and the universal payment gateway after** — those are the two incentives that give a merchant a reason to say yes. Everything else in this repo is the testbed for that thesis.

## The question

For years the pitch to merchants has been *please accept Bitcoin Cash*. Because it is better money. Because it is decentralized and censorship-resistant. The merchant nods politely and goes back to accepting whatever the customer can pay.

The question is not *why is BCH good*. The question is **what does the shopkeeper need to say yes?** The answer is not a cause. It is a margin, paid immediately, and a customer with money in their pocket.

## The thesis — in two steps

### Step 1. Remittances (Phase 0)

A migrant worker sends money home. Today a large part of it is lost in fees, delays and spreads. In Asgaya:

- the sender pays a **BCH seller** in their own fiat;
- the seller **funds a covenant** with BCH, which guarantees the value arrives;
- the recipient **claims** that value, or **cashes out at a merchant**;
- the merchant gets the BCH, and usually a customer who then buys something.

The merchant does not need to believe anything. They take a fee for something they already do, and a new customer walks in. **The remittance is the door opener.**

### The byproduct: the universal payment gateway

This is not a second project. It is what falls out of the base we build for remittances: the seller network, the covenant, the board.

A merchant that accepts BCH can accept **every payment method that has a seller on the board**. The customer pays the seller with the method they already use; the seller provides the BCH; the merchant is paid in BCH. The cost of the method travels with the customer who chose it, not with the merchant.

That is the second incentive: the merchant does not lose the sale at the counter, and does not have to integrate every payment app to do it. **The remittance rail is the base; the universal gateway is the byproduct.**

### The tripod

Organic, sustainable adoption needs three legs, not one.

- **Remittances — the first leg.** They put BCH in the customers' pockets **consistently**, which makes the recipients our **first organic ambassadors**: they want to get as much as possible out of every remittance, so they push for a better rate and a shop that accepts it.
- **The universal gateway — the second leg.** It turns merchants into **secondary ambassadors**: when a customer wants to pay with a method the shop does not have, the merchant can recommend Asgaya. We are **not blocking anyone; we are giving options**, and moving value from the fiat layer to the BCH layer.
- **B2B wholesale — the third leg.** A merchant discovers the list of merchants on Mercar Asgaya and sells to them **wholesale**. That is what makes BCH stop being swapped for fiat to pay employees and suppliers, and start being used **to pay for merchandise** — and, later, for more general payments.

The first ignites, the second widens, the third sustains.

### What supports them

The **discovery board** makes the sellers and merchants findable. The **stability layer** removes the volatility for the people who do not want it. The **notification bot** makes the seller's side automatic. None of it is the point; all of it serves the two incentives.

## What we are building

A Bitcoin Cash wallet and a peer-to-peer network, with no company, no custodian and no gatekeeper.

- **No custody** — the payment-first covenant holds the value; the app never does.
- **No KYC** — permissionless participation; the identity is a key, not an account.
- **Under 1% total fees, as a target** — the seller's and the merchant's spreads, not a company margin.
- **Self-custody** — the merchant keeps the key, the identity and the reputation.
- **Volatility protection** *(planned, Phase 1+)* — H€ and HAu stability tokens.

## Mercar Asgaya — the discovery layer

**Mercar is the board**: the place where a merchant posts what they offer and a buyer finds them. It is an **index, not a marketplace**. It does not take payments, match orders or hold anything.

- Merchants publish signed listings, and an on-chain **identity anchor** makes the merchant's claim verifiable;
- buyers find them, compare, and settle peer-to-peer;
- the ranking is computed locally, with no paid placement and no central recommendation.

For **BCH BLAZE 2026** we are building the slice that makes it visible: the **web board**, the **identity anchor**, and a **direct checkout**. The demo is deliberately small and labelled honestly — the product flow runs on testnet3, the anchor is the real artefact on mainnet, and the demo payment runs on chipnet.

## What to inspect (for judges and newcomers)

1. **[docs/index.md](docs/index.md)** — the five gears in one page.
2. **[the-mechanism/bulletin-board/](docs/the-mechanism/bulletin-board/README.md)** — Mercar and how discovery works.
3. **[the-mechanism/wallet/](docs/the-mechanism/wallet/README.md)** — the identity: one key, the npub, the anchor.
4. **[implementation/covenants/](docs/implementation/covenants/)** — the payment engine: the five paths, the oracle, the outputs.
5. **[user-journeys/remittance/](docs/user-journeys/remittance/README.md)** — the flow end to end, and **the universal gateway** in [customer/](docs/user-journeys/customer/README.md).

## Current status

**Phase 0 — active implementation and testnet3 validation.** Last update: **November 11, 2026.**

- ✅ **Covenant v2.7.1** — five spending paths; atomic single-input spends with exact-output checks, and `abort` also opens when the input cannot cover the face at the current price (offline 6/6 and e2e on testnet3).
- ✅ **Identity** — the same key for Nostr and BCH; the merchant keeps the key.
- ✅ **Discovery over Nostr** — signed listings (NIP-99); the anchor adds censorship resistance.
- 🧩 **BLAZE slice** — the web board, the identity anchor and a direct checkout (in preparation).
- ✅ **Signed oracle** — a 16-byte price and time message, verified on-chain.
- ✅ **Notification bot** — the seller's bank app is parsed locally; no credentials or sensitive data leaves the device.
- 📋 **Stability layer** — *planned, Phase 1+*.

The honest limits: Phase 0 is a small, trusted test; the stability layer is not built; some of the research questions are still open. We publish the questions as **36 structured unknowns**.

## The documentation

- **[implementation/](docs/implementation/README.md)** — the Android reference implementation and the covenants.
- **[the-mechanism/](docs/the-mechanism/README.md)** — the five gears and how they interact.
- **[user-journeys/](docs/user-journeys/README.md)** — sender, recipient, merchant, trader, customer.
- **[why-this-design/](docs/why-this-design/)** — the constraints and the rationale.
- **[unknowns/](docs/unknowns/README.md)** — the open questions; the research entry point.
- **[research/](docs/research/README.md)** — the formal sessions and summaries.
- **[glossary.md](docs/glossary.md)** — the terminology.
- **[risks-and-disclaimers.md](docs/risks-and-disclaimers.md)** — the risks, stated plainly.

## Feedback and license

- **GitHub Issues:** [github.com/rufitnes/asgayapedia/issues](https://github.com/rufitnes/asgayapedia/issues)
- **Email:** rufitnes@proton.me
- **Documentation:** [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). **Code:** MIT, when published.

---

**Created by Rufitnes (Suso), in collaboration with AI.** Coordination (⚙️) writes and implements; TightDS (🔍) reviews, verifies and curates the public documentation.

*Asgaya: the app that succeeds when it is no longer needed.*
