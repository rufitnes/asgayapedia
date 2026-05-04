← [Back to Android App](android-app/README.md)

# RS046-5: Backend APIs - INDEX

**Research Type:** Technical Specification
**Status:** In Progress
**Created:** 2026-04-27
**Related:** [RS046 Main Index](android-app/README.md)

---

## Documents in This Section

### Core APIs (MVP Required)
- [rate-apis.md](android-app/backend-apis/rate-apis.md) - Exchange rates and transaction cost estimation
- [transaction-apis.md](android-app/backend-apis/transaction-apis.md) - Remittance transactions and status tracking
- [settlement-apis.md](android-app/backend-apis/settlement-apis.md) - Liquidity provider settlement opportunities

### User Management (MVP - Minimal)
- [user-apis.md](android-app/backend-apis/user-apis.md) - User registration and preferences
- [notification-apis.md](android-app/backend-apis/notification-apis.md) - Push notification registration and delivery

### Discovery & Network (Post-MVP)
- [merchant-apis.md](android-app/backend-apis/merchant-apis.md) - Merchant discovery and availability
- [leaderboard-apis.md](android-app/backend-apis/leaderboard-apis.md) - LP rankings and gamification

### Infrastructure
- [common-patterns.md](android-app/backend-apis/common-patterns.md) - Authentication, error handling, rate limiting
- [bch-native-architecture.md](android-app/backend-apis/bch-native-architecture.md) - BCH-native design philosophy

---

## 💡 Documentation-First Philosophy

> **This is a documentation-first project.** We're building the API specifications collaboratively before implementation. Nothing is set in stone. Every endpoint, every design decision, and every architectural choice is open for discussion and improvement.
>
> **We're seeking the best ideas from the community.** If you see a better way to structure an API, a smarter design pattern, or an improvement to any specification—please contribute! Your feedback shapes the foundation of Asgaya.
>
> **The status "💡 Open for Design" means:** This API is ready for discussion, feedback, and collaborative refinement. We want your input to make it better.

---

## Overview

This folder contains the REST API specifications for Asgaya backend services. Each file focuses on one domain area and serves as a collaborative design document.

**Architecture principle:** Keep backend minimal. Phone = identity, local storage only.

**API design goal:** Simple enough for humans to understand, structured enough for AI agents to consume.

---

## API Categories

### Core APIs (MVP Required)

1. **[Rate APIs](android-app/backend-apis/rate-apis.md)** 💡 Open for Design
   - Get current exchange rates
   - Calculate transaction estimates
   - Dynamic reward split (BCH volatility)
   - **Why needed:** Show users accurate rates before sending money
   - **How to contribute:** Review the design, suggest simplifications, propose alternative endpoints

2. **[Transaction APIs](android-app/backend-apis/transaction-apis.md)** 💡 Open for Design
   - Create remittance transaction
   - Track transaction status
   - Recipient claim (select merchant)
   - Two-sided confirmation
   - **Why needed:** Core flow from sender → recipient → merchant
   - **How to contribute:** Discuss confirmation flows, error handling, edge cases

3. **[Settlement APIs](android-app/backend-apis/settlement-apis.md)** 💡 Open for Design
   - LP settlement opportunities
   - Accept settlement
   - Settlement history
   - **Why needed:** LPs provide instant liquidity to merchants
   - **How to contribute:** Review settlement guarantees, timing constraints, LP incentives

### User Management (MVP - Minimal)

4. **[User APIs](android-app/backend-apis/user-apis.md)** 💡 Open for Design
   - Register user (phone verification)
   - User preferences
   - Transaction history
   - **Why needed:** Basic identity and settings (keep minimal!)
   - **How to contribute:** Suggest ways to keep this lightweight while meeting needs

5. **[Notification APIs](android-app/backend-apis/notification-apis.md)** 💡 Open for Design
   - Register device for push notifications
   - Send notifications (transaction updates, settlement alerts)
   - **Why needed:** Real-time updates for all participants
   - **How to contribute:** Propose notification types, delivery guarantees, retry logic

### Discovery & Network (Post-MVP?)

6. **[Merchant APIs](android-app/backend-apis/merchant-apis.md)** 💡 Open for Design
   - Find nearby merchants
   - Register merchant location
   - Merchant availability
   - **Why needed:** Recipients need to find merchants to pick up cash
   - **How to contribute:** Design the merchant discovery UX, location privacy, search filters

7. **[Leaderboard APIs](android-app/backend-apis/leaderboard-apis.md)** 💡 Open for Design
   - LP rankings (speed, volume, activity)
   - Gamification mechanics
   - **Why needed:** Incentivize LP participation (nice-to-have, not MVP)
   - **How to contribute:** Suggest fair ranking algorithms, discuss gaming resistance

### Infrastructure

8. **[Common Patterns](android-app/backend-apis/common-patterns.md)** 💡 Open for Design
   - Authentication (JWT)
   - Error handling
   - Rate limiting
   - API versioning
   - Security best practices
   - **Why needed:** Consistent patterns across all endpoints
   - **How to contribute:** Propose security best practices, discuss API versioning strategy

---

## How These Connect to the App

### Sender Flow (RS046-2)
```
User opens app → Calls Rate APIs → Shows estimate
User sends Bizum → Transaction APIs create txn
User tracks → Transaction APIs poll status
```

### Merchant Flow (RS046-3)
```
Recipient selects merchant → Merchant APIs (nearby search)
Merchant gets notification → Notification APIs
Merchant confirms cash → Transaction APIs (confirm)
LP settles → Settlement APIs
```

### LP Flow (RS046-4)
```
LP deposits BCH → (handled in wallet, not backend)
LP sees opportunities → Settlement APIs (poll or push)
LP accepts → Settlement APIs (accept)
LP tracks earnings → Settlement APIs (history)
LP sees leaderboard → Leaderboard APIs
```

---

## MVP Scope Decision (Collaborative & Flexible)

**What we LIKELY need for MVP** (open for discussion):
- 💡 Rate APIs (users need accurate estimates)
- 💡 Transaction APIs (core remittance flow)
- 💡 Settlement APIs (LP liquidity provision)
- 💡 Notification APIs (real-time updates critical)
- 💡 Common Patterns (auth, errors, etc.)

**What we CAN probably defer** (post-MVP, but let's discuss):
- 💡 User APIs (can start with local-only storage, add backend sync later)
- 💡 Merchant APIs (can manually add test merchants initially)
- 💡 Leaderboard APIs (nice-to-have, but maybe we add gamification earlier?)

**Current thinking:** Focus on APIs 1-3, 5, 8 first. Add 4, 6, 7 after MVP proven.

**But:** This is a proposal, not a mandate. Do you see a better approach? Should we prioritize differently?

---

## Architecture Context

### What Lives Where

**On mobile device (Android app):**
- User identity (phone number)
- BCH wallet (seed phrase, private keys)
- Transaction history (local cache)
- Preferences and settings

**On escrow backend (Python service):**
- Bizum notification parsing (NotificationListener → Backend)
- Kraken API integration (buy/sell BCH)
- Transaction coordination (matching sender → recipient → merchant → LP)
- Rate aggregation (DolarAPI + Kraken)
- Settlement verification

**External services:**
- Kraken: BCH exchange
- DolarAPI: VES/ARS exchange rates
- BCH network: Blockchain verification
- Bizum: Payment rail (via Sabadell SMS)

---

## Communication Flow

```
┌─────────────────────────────────────────────────┐
│                 Mobile Apps                     │
│  (Sender, Merchant, LP, Recipient)              │
└──────────────────┬──────────────────────────────┘
                   │
                   │ HTTPS REST APIs (this folder)
                   │
┌──────────────────▼──────────────────────────────┐
│            Escrow Backend (Python)              │
│  • Transaction coordination                     │
│  • Notification parsing                         │
│  • Rate aggregation                             │
│  • BCH operations (via Kraken)                  │
└──────┬────────────────┬────────────┬────────────┘
       │                │            │
       ▼                ▼            ▼
   ┌────────┐     ┌──────────┐  ┌─────────┐
   │ Kraken │     │ DolarAPI │  │   BCH   │
   │  API   │     │          │  │ Network │
   └────────┘     └──────────┘  └─────────┘
```

---

## Design Status & Contribution Opportunities

| Document | MVP Priority | Status | Key Open Questions | How to Help |
|----------|-------------|--------|-------------------|------------|
| 1. Rate APIs | 🔴 Critical | 💡 Open for Design | How often to refresh rates? Which sources to trust? | Review rate aggregation logic, suggest data sources |
| 2. Transaction APIs | 🔴 Critical | 💡 Open for Design | How to handle failures mid-flow? Timeout strategies? | Discuss confirmation flows, error scenarios |
| 3. Settlement APIs | 🔴 Critical | 💡 Open for Design | What are fair LP incentives? How to prevent gaming? | Design settlement economics, discuss fairness |
| 4. User APIs | 🟡 Important | 💡 Open for Design | How minimal can we go? Local-first sync strategy? | Suggest lean architecture, privacy considerations |
| 5. Notification APIs | 🔴 Critical | 💡 Open for Design | Delivery guarantees? Retry strategy? | Design notification semantics, discuss reliability |
| 6. Merchant APIs | 🟡 Important | 💡 Open for Design | How to ensure merchant verification? Privacy approach? | Design discovery UX, location privacy model |
| 7. Leaderboard APIs | 🟢 Nice-to-have | 💡 Open for Design | Fair ranking metrics? Smurf-proof algorithm? | Propose ranking algorithms, gaming resistance |
| 8. Common Patterns | 🔴 Critical | 💡 Open for Design | Which auth mechanism? Rate limit strategy? | Contribute security expertise, design patterns |

**Legend:**
- 🔴 Critical: Must have for MVP
- 🟡 Important: Needed soon after MVP
- 🟢 Nice-to-have: Can defer to V2
- 💡 Open for Design: Ready for collaborative input and improvement

---

## How to Use This Index

**For understanding:**
1. Start here (index) to see big picture
2. Read one API file at a time
3. Ask questions when concepts unclear
4. Connect APIs to user flows (RS046-2, 3, 4)

**For implementation:**
1. Pick highest priority API (Critical first)
2. Read the spec
3. Implement endpoints one at a time
4. Test with mobile app
5. Move to next API

**For review:**
1. Open specific API file
2. Update based on learnings
3. Keep this index updated (status, dates)

---

## Current Focus

**Today (2026-04-27):**
- [x] Create folder structure
- [x] Write index.md
- [ ] Review and explain rate-apis.md concept
- [ ] Create rate-apis.md together

**This Week:**
- [ ] Complete critical APIs (1, 2, 3, 5, 8)
- [ ] Review with Suso for understanding
- [ ] Connect to user flows

---

## Target API Complexity

**Each file should be:**
- ✅ 150-300 lines max (readable in one sitting)
- ✅ Focus on 2-5 related endpoints
- ✅ Clear request/response examples
- ✅ Explain WHY endpoint exists (connect to user need)
- ✅ Note MVP vs post-MVP

**Avoid:**
- ❌ 1000+ line files (too overwhelming)
- ❌ Mixing unrelated endpoints
- ❌ Over-engineering (keep it simple!)

---

## Next Steps

**Immediate:**
1. Explain Rate APIs concept to Suso
2. Get alignment on what's needed
3. Create rate-apis.md together
4. Repeat for each API category

**Goal:** Suso understands each API and how it connects to the app before we write the spec.

---

## Related Documents

- **Parent:** [RS046 Main Index](android-app/README.md)
- **User Flows:** [RS046-2 Remittance & Merchant Cash-Out](android-app/flows/remittance-merchant-cash-out.md), [RS046-3 Merchant](android-app/flows/merchant-flows.md), [RS046-4 LP](android-app/flows/lp-flows.md)
- **Technical:** [RS046-6 NotificationListener](android-app/notification-listener/README.md)

---

*Created: April 27, 2026*
*Philosophy: Atomic documents, one domain at a time, explain before writing*
*Status: Index complete, ready to build APIs incrementally*
