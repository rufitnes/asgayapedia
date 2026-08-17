# Asgaya Oracle Husk: Trusted Bootstrap, Progressive Decentralization

**Purpose:** Document the Asgaya-owned price/time oracle — its role as development infrastructure, its graduation to a production bootstrap oracle, and its eventual retirement under progressive decentralization.

**Status:** Phase 0 — running on the Raspberry Pi (Pi-chan), integrated with the app (Aug 16, 2026)

---

## The Honest Framing

Asgaya's long-term vision is **blockchain-as-oracle** — the network discovers price from real covenant fundings, not from a trusted signer (see [Distributed Monitoring](distributed-monitoring.md)). But that vision is **aspirational, years away, and possibly never achieved**. It requires a covenant revision that removes the trusted oracle entirely, and a network mature enough that funding signals + reputation-filtered VWAP are reliable.

**Until then, Asgaya runs its own oracle.** This is a deliberate, long-lived part of the system — not a temporary hack.

> **Progressive decentralization:** The trusted oracle is the launch state, not a compromise. Decentralization is the eventual exit, achieved only when the network genuinely replaces it. We are explicit about what is trusted and when.

---

## The Three Phases

| Phase | What | Who runs it | Oracle role |
|-------|------|-------------|-------------|
| **Phase 0 (now)** | Development infrastructure | Asgaya (Pi) | Test tool: full control over price + time |
| **Mainnet graduation** | Production bootstrap oracle | Asgaya (Pi, hardened) | Trusted signer while network matures |
| **Aspirational** | Blockchain-as-oracle | No trusted signer | Oracle retired; covenant no longer needs it |

---

## Phase 0: Development Infrastructure

**Purpose:** Test infrastructure, not production.

**Why we run our own oracle for testing:**
- **Full control over price and time** — we can simulate arbitrary price drops (6.5%, 7%, 10%) and expiry timestamps to test abort()/overlap-zone thresholds precisely. No external feed can do this.
- **Deterministic testing** — the same result as hardcoded prices, but through the real signing path (checkdatasig), so the full covenant validation is exercised.
- **No external dependency** — tests don't fail because Kraken is down or rate-limited.

**Running service (Pi-chan):** `http://192.168.1.100:3001`

| Endpoint | Purpose | Phase |
|----------|---------|-------|
| `GET /oracle/info` | Serve the oracle public key (app fetches at covenant creation) | Phase 0 + production |
| `GET /oracle/price` | Serve a signed price (timestamp + price in cents) | Phase 0 + production |
| `POST /oracle/set-price` | Override price for testing (simulate price drops) | **Phase 0 only** |

**Zero keys in the app (architecture decision, Aug 16):**
- No private keys or static oracle pubkeys live in app code or the APK
- The app holds a single `ORACLE_URL` constant; everything else is dynamic
- `fetchOraclePubkey()` (5-second timeout) queries `/oracle/info` at covenant creation time
- Keys live only on the Pi-chan server; the private key never leaves it
- Oracle can rotate keys without an app update

**Signature pattern (critical):** uses `bitcoincashjs-lib` `crypto.sha256()` + `.toDER()` — never Node's built-in `crypto` (see [Covenant README](../../implementation/covenants/README.md))

**Message format:** 16 bytes, little-endian (timestamp + price in cents)

**Key rotation & recovery (validated Aug 16):**
- The covenant freezes `oraclePubkey` at creation, so a rotated key makes existing covenants unclaimable (refund/abort still work with the old key)
- **Two-oracle strategy:** the new oracle key serves production; the old oracle key is kept on the Pi (not in the app) purely for recovering old covenants via `covenant-tests` refund scripts
- Recovery validated end-to-end: 2 successful recoveries of covenants created with the old oracle key

---

## Mainnet Graduation: Production Bootstrap Oracle

The same husk graduates to production when Asgaya starts testing on mainnet.

**What changes:**
- **Reliability, not architecture** — the signing mechanism is unchanged; what changes is uptime guarantees, monitoring, and the price source (real market data instead of test-controlled values)
- **Still self-hosted** — Asgaya operates it. It's a trusted signer by design during the bootstrap phase
- **Still a bootstrap** — it feeds the network until user trades provide sufficient VWAP weight (see the hybrid weighting in [Distributed Monitoring](distributed-monitoring.md))

**Why this is consistent with the philosophy:**
- The covenant validates a signature from *whichever* oracle signs — the husk is one valid source among the eventual many
- The bootstrap-then-exit model is documented: Asgaya provides liquidity + price feed first, then network VWAP gains weight (60% → 95%+) as user volume grows
- We are explicit that the trusted signer is a **long-lived** part of the system, not a launch-day shortcut

---

## Aspirational: Blockchain-as-Oracle (No Trusted Signer)

**The end state — years away, optimistic, possibly never:**

- A **covenant revision** where the trusted oracle isn't needed (price discovered from on-chain funding signals + reputation-filtered VWAP)
- The husk retires (or becomes a 5% sanity check per the distributed-monitoring design)
- Fully permissionless, user-driven price discovery

**Flag:** This is aspirational, not a commitment. Treat it as a direction, not a roadmap item. The current covenant (v2.6/v2.6.1) requires the trusted oracle, and that's acceptable.

---

## Relationship to Other Docs

- **[Distributed Monitoring](distributed-monitoring.md)** — the blockchain-as-oracle destination and the bootstrap-then-exit strategy this husk serves
- **[Time Oracle + MTP Fallback](../../why-this-design/constraints/time-oracle-mtp-fallback-trustless-ux.md)** — why the oracle doesn't need to be trusted for security (MTP is the safety net; oracle failure only degrades UX)
- **[RS078 Oracle-over-Nostr Prior Art](../../research/RS078_oracle_over_nostr_prior_art.md)** — how Asgaya's oracle-for-UX approach differs from on-chain-enforcement oracles
- **[Covenant README](../../implementation/covenants/README.md)** — oracle signature requirements

---

**Status:** Phase 0 (running on Pi-chan, integrated with app)  
**Last Updated:** 2026-08-16  
**Location:** Raspberry Pi (Pi-chan) — `http://192.168.1.100:3001`

---

## Navigation

**[🏠 Home](../../../index.md)** | **[↑ Nostr Coordination](README.md)** | **[📖 Glossary](../../../glossary.md)**
