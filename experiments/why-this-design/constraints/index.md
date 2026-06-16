# Constraints: The Design Trade-offs

**This section will contain content moved from `/decisions/`**

---

## Overview

After identifying requirements, we faced design constraints - trade-offs where no perfect solution exists:

1. **Regulatory:** No custody vs user support
2. **Technical:** BCH block time vs settlement speed
3. **Economic:** Buffer size vs capital efficiency
4. **Social:** Identity vs privacy
5. **Scaling:** Automation vs control

---

## Key Constraints

### 1. Payment-First Model (No Escrow)
**Trade-off:** Seller risk vs compliance  
**Decision:** Bank notification as notary, no BCH locked until payment confirmed  
**Why:** Avoids custody, enables legal recourse

### 2. 7% Volatility Buffer (Not 5% or 10%)
**Trade-off:** Seller capital efficiency vs risk coverage  
**Decision:** 7% covers 90% of daily swings  
**Why:** Balances protection with capital requirements

### 3. Cash Accounts (Not Phone Numbers)
**Trade-off:** Privacy vs usability  
**Decision:** BCH address + name as identity  
**Why:** No central database, blockchain-native lookup

### 4. Passive Mode (Bot Automation)
**Trade-off:** Control vs scaling  
**Decision:** Sellers post once, bot handles trades  
**Why:** Linear time investment doesn't scale

### 5. Reputation On-Chain (Not Central Database)
**Trade-off:** Privacy vs trust  
**Decision:** Ratings stored as blockchain transactions  
**Why:** Decentralized, censorship-resistant

---

## Content to be migrated from `/decisions/`

- Design decisions
- Trade-off analysis
- Alternative approaches rejected
- Rationale for choices

**Status:** Placeholder - content migration pending  
**Source:** `/decisions/` (OLD structure)
