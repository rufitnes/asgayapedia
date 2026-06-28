# Implementation: Technical Details
**📖 Unfamiliar terms?** See the [glossary](../glossary.md) for definitions.

**Purpose:** This section contains implementation details, API documentation, and technical specifications.

---

## Overview

After understanding [The Mechanism](/the-mechanism/), [User Journeys](/user-journeys/), and [Why This Design](/why-this-design/), you might want to:

- **Build:** Implement your own Asgaya client
- **Integrate:** Connect existing app to Asgaya protocol
- **Extend:** Add new features or payment rails
- **Debug:** Understand error codes and edge cases

This section provides the technical details you need.

---

## Navigation

### [Android App](/implementation/android-app/)
**What:** Reference implementation of Asgaya client

Contains:
- App architecture
- Component implementations (wallet, bulletin board, nostr, notification bot)
- Code examples
- Electrum integration (blockchain queries)
- Error handling

**Read this if:** You're building or extending the Android client

---

### [Glossary](/glossary.md)
**What:** Definitions of technical terms (site-wide reference)

Contains:
- Asgaya-specific terms (Cash Account, covenant, volatility buffer, H€/HAu)
- BCH concepts (CashTokens, OP_RETURN, Nostr)
- Payment systems (Bizum, PagoMóvil, SEPA)
- Economic concepts (money velocity, capital recycling)

**Note:** Glossary is at root level (serves all documentation sections)

**Read this if:** You're confused by terminology

---

## Content Status

### ✅ Already Exists
- Android app implementation (in `/android-app/`)
- Glossary (at `/glossary.md`)

### 📝 To Be Created (Phase 1+)
- Protocol specifications (formal spec)
- Integration guides (how to add new payment rails)
- iOS/web client implementations

### 🔄 Needs Review (Phase 0)
- Android app docs - verify accuracy during trials

---

## How to Use This Section

### If You're Building
Start with [Android App](/implementation/android-app/) → see reference implementation and blockchain query patterns

### If You're Confused
Read [Glossary](/glossary.md) → define terms

---

## What's NOT in This Section

### Conceptual Explanations
**See:** [The Mechanism](/the-mechanism/) - what components do, how they work

### Rationale
**See:** [Why This Design](/why-this-design/) - why these choices were made

### User Guides
**See:** [User Journeys](/user-journeys/) - step-by-step flows for end users

**This section is purely technical** - code, blockchain queries, specs, debugging.

**Phase 0 focus:** Minimalistic implementation. Less stuff to break. No optional backend services.

---

## Contributing to This Section

**We welcome contributions:**
- Code improvements (Android app)
- New client implementations (iOS, web, desktop)
- API documentation (backend services)
- Integration guides (new payment rails)

**How to contribute:**
- Fork GitHub repository
- Submit pull request with implementation
- Document new features in this section

---

## Navigation

**[🏠 Home](../index.md)** | **[📖 Glossary](../glossary.md)**

**In this section:**
- [Android App](android-app/README.md) - 7 technical components

**Related sections:** [The Mechanism](../the-mechanism/README.md) · [User Journeys](../user-journeys/README.md) · [Why This Design?](../why-this-design/README.md)
