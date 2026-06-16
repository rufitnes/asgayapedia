# Reference: Technical Implementation

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

### [Android App](/reference/android-app/)
**What:** Reference implementation of Asgaya client

Contains:
- App architecture
- Component implementations (wallet, bulletin board, nostr, notification bot)
- Code examples
- API integration
- Error handling

**Read this if:** You're building or extending the Android client

---

### [Backend APIs](/reference/backend-apis/)
**What:** Optional backend services for enhanced features

Contains:
- Push notification service
- Price oracle API
- Reputation aggregator
- Analytics (optional)

**Note:** Asgaya core protocol works WITHOUT these services. They're optional enhancements.

**Read this if:** You're running backend infrastructure

---

### [Glossary](/reference/glossary.md)
**What:** Definitions of technical terms

Contains:
- Asgaya-specific terms (Cash Account, bounty contract, volatility buffer)
- BCH concepts (covenant, CashTokens, OP_RETURN)
- General crypto terms (AnyHedge, Nostr, oracle)

**Read this if:** You're confused by terminology

---

## Content Status

### ✅ Already Exists
- Android app implementation (in `/android-app/`)
- Glossary (in `/glossary.md`)

### 📝 To Be Created
- Backend APIs documentation (optional services)
- Protocol specifications (formal spec)
- Integration guides (how to add new payment rails)

---

## How to Use This Section

### If You're Building
Start with [Android App](/reference/android-app/) → see reference implementation

### If You're Integrating
Check [Backend APIs](/reference/backend-apis/) → understand optional services

### If You're Confused
Read [Glossary](/reference/glossary.md) → define terms

---

## What's NOT in This Section

### Conceptual Explanations
**See:** [The Mechanism](/the-mechanism/) - what components do, how they work

### Rationale
**See:** [Why This Design](/why-this-design/) - why these choices were made

### User Guides
**See:** [User Journeys](/user-journeys/) - step-by-step flows for end users

**This section is purely technical** - code, APIs, specs, debugging.

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

**Status:** Phase 0 (Pre-Launch) - Android app reference implementation complete  
**Updated:** 2026-06-16  
**Next:** Explore [Android App](/reference/android-app/) for implementation details
