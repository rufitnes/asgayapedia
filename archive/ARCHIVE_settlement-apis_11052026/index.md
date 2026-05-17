⚠️ **ARCHIVED:** This document describes the escrow-era LP (Liquidity Provider) instant settlement system where LPs would accept bounties and send fiat to merchants before receiving BCH. This approach was made obsolete on May 10, 2026 with the covenant architecture pivot.

**Why superseded:**
- Old model: LP accepts bounty → sends fiat to merchant → receives BCH from escrow
- New model: Merchant receives BCH from covenant first → optionally sells to BCH buyer via bulletin board
- Same covenant mechanism used for both directions (circular economy)
- No special "instant settlement" feature needed - just show BCH buyer bulletin in UI

**Replaced by:**
- BCH buyer bulletin (shown in merchant-flows.md Screen 4a)
- Same covenant infrastructure, merchant = seller, BCH buyer = recipient
- Keep it simple: P2P bulletin board for both remittance and BCH sales

**For future automation:**
- Could add automated "instant settlement" as nice-to-have post-MVP
- But V0.2 keeps it simple: manual bulletin board selection

**Date archived:** May 10, 2026

---

