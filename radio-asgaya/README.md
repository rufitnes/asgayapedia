# Radio Asgaya - Episode Scripts

**37 episodes explaining the complete Asgaya remittance system**

This directory contains the text scripts for Radio Asgaya episodes. These scripts are used to generate audio versions with Azure Neural TTS voices (EN and ES).

**For audio files:** See `/home/suso/Documents/asgaya/knowledge/meta/radio_asgaya/audio/`

**Why scripts instead of audio here:**
- Text files can be reviewed by AI tools (DeepSeek, Claude, etc.)
- Version control tracks changes meaningfully (git diff works)
- Easier to suggest improvements and iterate
- Much smaller repository size

---

## Episode List

Episodes are numbered in increments of 100 to allow for future insertions:

### Foundation (100-500)
- **100** - The Mission: What Asgaya is and why it exists
- **200** - Merchant Business Case: The escape hatch from VES depreciation
- **300** - Freelance Payments: Same protocol, Pro Seller role
- **400** - Risk Allocation: Who bears which risk and why
- **500** - Pull System: Recipient timing controls everything

### Core Mechanics (600-1000)
- **600** - BCH Sellers: Who provides liquidity and why
- **700** - 3% Early Warning System: Protecting against volatility
- **800** - Bounty Contracts: The covenant architecture
- **900** - Two-Step Settlement Timing: 5 minutes and 24 hours
- **1000** - Cash Accounts: Human-readable names for BCH

### Economics & Strategy (1100-1700)
- **1100** - Fee Splitting: 0.5% seller + 0.5% merchant
- **1200** - Why Cheaper: 1% vs 6.49% legacy fees
- **1300** - Cold Start Strategy: First 150 senders, then scale
- **1400** - BCH Usage Incentive: Aligning everyone's interests
- **1500** - Regulatory Constraints: MiCA compliance without licensing
- **1600** - Dual Citizen Arbitrage: BCH buyers with BCH surplus
- **1650** - The Onboarder: Building networks one handshake at a time
- **1700** - Progressive Decentralization: Training wheels to permissionless

### Implementation (1800-2400)
- **1800** - Multi-Payment Methods: Bizum, SEPA, ATMs, Revolut
- **1900** - Phase 0 Validation: Learning before scaling
- **2000** - Exchange Rates: How pricing works
- **2100** - UI Language Regulatory: Words matter for compliance
- **2200** - MUSD Integration: Phase 1 stability layer
- **2300** - Fraud Proofs: Cryptographic sender protection
- **2350** - Covenant Flows: Complete technical walkthrough
- **2400** - Unknown: Overcollateralization Rate

### Unknowns & Vision (2500-3400)
- **2500** - Unknown: Claim Timing Patterns
- **2600** - BCH Capabilities: Why BCH and not another chain
- **2700** - Unknowns Overview: 14 questions Phase 0 will answer
- **2800** - Contributing Guide: How to help build Asgaya
- **2900** - BCH Adoption Flywheel: Self-sustaining growth
- **3000** - Venezuela Opportunity: Hardest market, biggest prize
- **3100** - Permissionless Accelerant: Why this spreads
- **3200** - The Complete Picture: Everything together
- **3300** - **NEW:** The Melting Currency Problem

---

## Script Format

Each script includes:
- Episode number and title
- Estimated duration
- Target audience
- Tone guidance
- Section markers for TTS optimization
- Complete narrative with strategic punctuation

**TTS "Secret Sauce" Applied:**
- Semicolons connect related thoughts for natural flow
- Strategic em-dash placement (after context, not after pause)
- Layered punctuation hierarchy: comma < semicolon < colon < em-dash < period < blank line
- Consolidated paragraphs with blank lines only between major sections
- Natural contractions and conversational phrasing

---

## Key Episodes for Review

**Recent rewrites (June 2026):**
- **100-the-mission.md** - Now includes two-problem framing (expensive remittances + VES depreciation)
- **200-merchant-business-case.md** - Leads with escape hatch, addresses scam history
- **3400-melting-currency-problem.md** - NEW episode explaining hyperinflation psychology

**Critical for BCH community:**
- **2600-bch-capabilities.md** - Why BCH specifically
- **2900-bch-adoption-flywheel.md** - Self-sustaining growth mechanism
- **3000-venezuela-opportunity.md** - Strategic rationale

**Technical deep-dives:**
- **800-bounty-contracts.md** - Covenant architecture
- **2300-fraud-proofs.md** - Cryptographic protection
- **2350-covenant-flows.md** - Complete walkthrough

---

## Contributing Improvements

**To suggest improvements:**
1. Read the script
2. Propose changes via GitHub issue/PR
3. Reference specific line numbers
4. Explain rationale (clarity, accuracy, persuasiveness)

**Criteria for good suggestions:**
- Improves clarity without adding length
- Fixes technical inaccuracies
- Strengthens narrative flow
- Better addresses objections

---

## Related Documentation

- [Phase 0 Validation Checklist](../docs/decisions/phase-0-validation-checklist.md)
- [Unknowns Directory](../docs/unknowns/README.md)
- [Contributing Guide](../docs/meta/contributing.md)
- [RS064: BCH as Store of Value vs VES](../docs/research/RS064_bch_sov_in_venezuela.md)
- [RS065: BCH Volatility Declining](../docs/research/RS065_BCH_price_volatility_decline.md)

---

**Last Updated:** June 2, 2026  
**Hosted by:** Claudia Sonnet 4.5 (AI collaboration persona)  
**Voice (EN):** `en-US-JennyNeural` at -10% speed  
**Voice (ES):** `es-MX-DaliaNeural` at standard speed
