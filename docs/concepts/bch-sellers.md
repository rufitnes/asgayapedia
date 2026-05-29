# BCH Sellers — BCH Inventory Providers in Asgaya

**Core Concept:** BCH sellers provide BCH inventory to the Asgaya network by posting BCH + volatility buffer to covenant contracts, earning fees while benefiting from a built-in hedge against BCH price volatility.

**Related:** [Bulletin Board](./bulletin-board.md), [Pull System](./pull-system.md), [Bounty Contracts with Volatility Buffer](./bounty-contracts-with-volatility-buffer.md), [Fee Splitting Model](../decisions/fee-splitting-model.md)

---

## What Is a BCH Seller?

A BCH seller is anyone who:
1. **Posts a seller listing** on the [Asgaya Bulletin Board](./bulletin-board.md) (NFT category: `ASGAYA_SELLER_V1`)
2. **Posts BCH + volatility buffer** to covenant contracts when sender pays (typically 107% of requested amount)
3. **Receives EUR (or other fiat)** from senders via Bizum/bank transfer
4. **Earns BCH fees** (0.5% of transaction in BCH) + gets back volatility buffer
5. **Can voluntarily time extension** to earn uptime incentives if BCH price drops significantly

**On the bulletin board:**
```
Type: ASGAYA_SELLER_V1
Payment Methods: ["Bizum", "SEPA", "bank_transfer"]
Corridors: ["EUR-VES", "USD-VES"]
Fee: 0.5%
Buffer: 7%
```

**See:** [Bulletin Board](./bulletin-board.md#1-bch-sellers-provide-bch--receive-fiat) for complete listing mechanics.

**BCH sellers are NOT:**
- ❌ Custodians (don't hold client funds—post their own BCH as collateral)
- ❌ Escrows (don't intermediate—covenant contract executes automatically)
- ❌ Payment processors (don't provide payment services—just sell BCH)

**BCH sellers ARE:**
- ✅ BCH inventory providers (supply BCH capital to enable settlement)
- ✅ Private individuals or entities (selling their own BCH for fiat)
- ✅ Participants in a permissionless protocol (no KYC, no licensing required)

> **How sellers actually make money:** The 0.5% fee per transaction is only part of the story. Sellers recycle the same capital many times per day, and the hedge mechanism means they profit in both rising and falling BCH markets. Read the [Capital Recycling Strategy](bounty-contracts-with-volatility-buffer.md#capital-recycling-strategy-the-sellers-business-model) for a complete walk-through.

---

## 🎯 CRITICAL RISK ALLOCATION FOR SELLERS

**What risk do sellers bear?**

* ✅ **Sellers DO bear:**
  * Opportunity cost (capital locked for up to 24h)
  * Reliability reputation risk (if they don't time extension during volatility)
  * Brief exposure during Bizum payment window (~5 minutes)
* ❌ **Sellers DO NOT bear:**
  * Merchant's volatility risk (merchant completely protected)
  * Sender's tail risk (refund goes to sender if >7% drop)
  * Recipient's claim timing risk (seller gets capital back either way)

**If BCH drops >7% and seller doesn't time extension:**
* Covenant expires early
* Remaining BCH refunds to **SENDER** (not seller)
* Seller receives their 0.5% fee portion only
* **This is fair exchange, not a penalty** - seller traded BCH for EUR at market rate

**If BCH rises >7%:**
* Seller gets LARGER refund (excess BCH returned)
* Seller participated at favorable rate (sold BCH before rise)
* **No "trapped gains" problem** - covenant holds cash buy order for EUR-worth, not fixed BCH amount

**See:** [With volatility buffer Bounty Contracts](./bounty-contracts-with-volatility-buffer.md) for complete covenant mechanics.

---

## Who Can Be a BCH Seller?

**Requirements:**
1. **BCH inventory** — Must own BCH to post as collateral (minimum ~€100-1000 depending on corridor volume)
2. **Automation capability** — Bot to monitor bulletin board and parse payment notifications (smsbridge_loop.py or similar)
3. **Capital tolerance** — Can lock BCH for up to 24 hours (volatility buffer requirement)
4. **Risk tolerance** — Accept margin call risk if BCH drops >7% during wait period
5. **Payment rail access** — Can receive Bizum, SEPA, or other fiat payment methods

**Anyone meeting these requirements can be a BCH seller:**
- ✅ Bitcoin Cash miners
- ✅ Cryptocurrency traders
- ✅ BCH HODLers (want yield on holdings)
- ✅ Small exchanges or OTC desks
- ✅ BCH enthusiasts with inventory
- ✅ Automated market makers

**Permissionless:** No approval needed, no registration, no KYC. Just accept a bounty and post collateral.

---

## Reliability Rewards

Sellers who voluntarily time extension their covenants when BCH drops >7% earn progressive rewards that increase their earning potential:

**Reward tiers** (based on number of time extensions completed):
- **🛡️ Reliable** (1 time extension): Higher transaction limits (€220), badge visible to senders
- **📊 Trusted** (3 time extensions): Even higher limits (€260), highlighted in sender searches
- **⭐ Priority** (5 time extensions): Premium limits (€300), auto-selected by senders
- **🎯 Premium** (10 time extensions): Featured placement (€360), priority routing
- **💎 Elite** (20+ time extensions): Maximum limits (€400), top-tier visibility

**Economic value:** Higher limits enable more volume per transaction, better visibility drives more selections, and auto-select eligibility creates passive income opportunities. Sellers who demonstrate reliability earn significantly more than those who rely only on automatic fair exchange.

**Philosophy:** Time extensions are voluntary, not required. Covenants that mature automatically result in fair exchange at market rate (no penalty). See [Top-Up Opportunity technical details](./bounty-contracts-with-volatility-buffer.md#time extension-opportunity-how-sellers-earn-reliability-rewards) for implementation.

---

## Why Miners Are Especially Ideal ⭐

While anyone can be a BCH seller, **Bitcoin Cash miners have unique advantages** that make them the most natural participants.

### 1. Already Have BCH Inventory

**The miner advantage:**
- Miners earn BCH block rewards (currently 3.125 BCH per block)
- Miners receive BCH transaction fees
- **Miners own BCH natively**—don't need to buy it

**For other seller types:**
- Must buy BCH on exchanges to participate
- Pay exchange fees (0.26-0.50%) to acquire inventory
- Capital efficiency lower (buy BCH, then lock for volatility buffer)

**Result:** Miners can participate with **zero acquisition cost**.

---

### 2. Already Converting BCH → Fiat

**Miners' natural economic flow:**
```
Mine BCH → Need fiat for operational expenses → Sell BCH to exchange
```

**Traditional miner economics:**
1. Mine BCH (block rewards + transaction fees)
2. Pay exchange fees (0.26-0.50%) to convert BCH → EUR/USD
3. Use fiat for electricity, hardware, facilities, staff
4. Hope the margin covers costs

**Miner as Asgaya BCH seller:**
1. Mine BCH (block rewards + transaction fees)
2. **Post BCH to Asgaya covenants** instead of selling to exchange
3. **Receive EUR directly from senders** via Bizum
4. **Earn 0.5% fee** + volatility buffer surplus (if BCH rises)
5. Use EUR for operational expenses (same as before)

**Result:** Miners earn MORE while avoiding exchange fees.

---

### 3. Dual Revenue Stream

Miners operating as BCH sellers benefit from **two revenue sources:**

**Revenue Stream 1: Mining (Traditional)**
- Block rewards (currently 3.125 BCH per block, halving every 4 years)
- Transaction fees from BCH network usage

**Revenue Stream 2: BCH Selling (New)**
- 0.5% fee per bounty accepted
- Example: 0.0005 BCH per 0.1 BCH transaction (€100 at 1 BCH = €1,000)
- Plus: Avoid exchange fees (0.26% savings vs. traditional selling)
- Plus: Returned volatility buffer (0.007 BCH) maintains BCH exposure

**Example scenario (assuming 1 BCH = €1,000):**
- Miner accepts 100 bounties/day (each 0.1 BCH transaction)
- BCH fees earned: 100 × 0.0005 BCH = 0.05 BCH/day
- EUR equivalent: 0.05 × €1,000 = €50/day = €1,500/month = €18,000/year
- Plus: Exchange fee savings (0.26% on volume sold via Asgaya)

**Combined revenue makes mining more profitable even as block rewards decrease.**

---

### 4. Long-Term BCH Sustainability (Post-Block-Reward Era)

BCH's block reward halves every 4 years. Eventually, miners will rely entirely on transaction fees. This is the "BCH sustainability question"—will transaction fees alone support network security?

**Asgaya provides part of the answer:**

**Current BCH Economics:**
- Block reward: 3.125 BCH (~€937 at €300/BCH)
- Avg transaction fee: ~$0.01 per transaction
- To match block reward from fees alone: Need ~93,700 transactions per block (10 min)

**BCH + Asgaya Economics:**
- **BCH transaction fees:** ~$0.01 per transaction (from blockchain)
- **Asgaya seller fees:** €0.50 per remittance (paid in fiat by sender)
- Miners earn from BOTH sources

**Example scenario (post-block-reward, year 2140):**
- Miner accepts 100 Asgaya bounties/day
- Earns €1,500/month (€18,000/year) in **fiat** seller fees
- PLUS earns BCH transaction fees from increased network usage (merchants, recipients using BCH)

**This makes BCH mining sustainable even as block rewards approach zero.**

---

### 5. Already Have Infrastructure

**Technical advantages miners bring:**
- ✅ 24/7 server uptime (required for mining)
- ✅ Technical expertise (running nodes, monitoring systems)
- ✅ Capital reserves (invested in mining equipment)
- ✅ Understanding of BCH blockchain (transaction monitoring, wallet management)

**Transition cost to become BCH seller:** Minimal
- Install notification listener bot (smsbridge_loop.py)
- Configure covenant monitoring
- Set margin call thresholds
- **Reuse existing mining infrastructure**

**For non-miners:**
- Must set up dedicated infrastructure
- Learn BCH wallet management
- Higher technical barrier to entry

---

### 6. Aligned Incentives with BCH Ecosystem

**Miners benefit from:**
1. **BCH price appreciation** (mining rewards worth more)
2. **BCH network growth** (more transactions = higher fees)
3. **BCH adoption** (stronger network = more mining profitability)

**Asgaya drives all three:**
- More remittances → More BCH transactions → Higher network fees
- More merchants accepting BCH → Broader BCH adoption
- More people holding BCH → Price support

**Virtuous cycle:**
```
More Asgaya remittances → More seller fees for miners
More seller fees → Mining more profitable
More profitable → More hash power on BCH
More hash power → More secure BCH network
More secure → More trust in remittances
More trust → More Asgaya usage
→ Repeat
```

---

### 7. Geographic Distribution

**Miners exist wherever electricity is cheap:**
- Hydroelectric regions (Norway, Canada, Iceland)
- Solar-rich areas (Spain, California, Australia)
- Geothermal zones (Iceland, New Zealand)

**This creates natural corridor coverage:**
- Miners in Spain → EUR corridor (Bizum payments)
- Miners in USA → USD corridor (Zelle, ACH)
- Miners in Canada → CAD corridor
- **Geographic distribution = corridor resilience**

---

### 8. Built-In Hedge Against BCH Volatility 🎯

**Miners face a constant challenge:** BCH price volatility affects revenue predictability.

**The covenant structure provides a partial hedge:**

**Traditional mining economics:**
- Mine 0.107 BCH → Hold → Price drops 5% → Lose €5.35
- Full exposure to BCH price swings

**Covenant seller economics:**
- Post 0.107 BCH → Get €100 fiat immediately → Price drops 5% → Lose only €4.85
- **94-97% of value converted to stable fiat BEFORE price moves**
- Only small surplus (0.00226 BCH) exposed to volatility

**The key advantage:**
- **Get paid in fiat immediately** (before price changes)
- **Reduce BCH exposure** by converting to EUR at creation-time rate
- **Win in both directions:**
  - Price drops → Better off than holding (€0.50 less loss)
  - Price rises → Better off than holding (€0.50 more gain)
  - Price stable → Earn 0.5% fee (pure profit)

**Why this matters for miners:**
- Miners need predictable fiat income (electricity, hardware, operational costs)
- Traditional approach: Sell BCH on exchange (full price exposure until sold)
- Covenant approach: Lock in fiat payment immediately (minimal price exposure)

**Result:** Miners can manage cash flow more predictably while still maintaining BCH exposure via the surplus.

**See detailed comparison:** [Profit Model - The Hedge Mechanism](#the-hedge-mechanism-why-sellers-always-win-🎯)

---

## Other BCH Seller Types

While miners are ideal, other participants can also succeed as BCH sellers:

### Cryptocurrency Traders
**Advantages:**
- Already buy/sell BCH regularly
- Understand price volatility and risk management
- Have exchange accounts and fiat rails
- Can hedge margin call risk with derivatives

**Challenges:**
- Must buy BCH inventory (acquisition cost)
- Profit margins thinner than miners (bought BCH, not mined)

**Best fit:** High-volume corridors where trading spreads justify participation

---

### BCH HODLers
**Advantages:**
- Already own BCH (no acquisition cost)
- Want yield on holdings (0.5% fee per bounty)
- Philosophically aligned (want BCH adoption)

**Challenges:**
- May not want to sell BCH (prefer long-term holding)
- Less likely to automate (smaller scale)
- Capital locked during bounty period (24h)

**Best fit:** Low-volume corridors, casual participation

---

### Small Exchanges / OTC Desks
**Advantages:**
- Already have BCH liquidity
- Already converting BCH ↔ fiat
- Existing automation infrastructure
- High capital capacity

**Challenges:**
- May require KYC compliance (depending on jurisdiction)
- Higher operational overhead
- Regulated entities (less permissionless)

**Best fit:** High-volume corridors, institutional sellers

---

### BCH Enthusiasts
**Advantages:**
- Mission-driven (want to support BCH ecosystem)
- Technical capability (understand blockchain, covenants)
- Community-oriented (help onboard new users)

**Challenges:**
- Limited capital (smaller BCH holdings)
- Manual operations (no automation)
- Opportunity cost (time investment)

**Best fit:** Niche corridors, community-driven routes (e.g., Venezuela support groups)

---

## Capital Requirements

**Per bounty (€100 example):**
- Covenant requires: €100 worth of BCH (at settlement rate)
- Volatility buffer: 7% → **€107 worth of BCH locked**
- Lock duration: Up to 24 hours (until recipient claims or timeout)

**Volume scenarios:**

**Low volume (10 concurrent bounties):**
- Capital locked: 10 × €107 = **€1,070**
- Daily volume: €1,000
- Monthly fees: €1,000 × 30 × 0.5% = **€150/month**
- **ROI on locked capital:** 14% monthly (if all bounties execute)

**Medium volume (50 concurrent bounties):**
- Capital locked: 50 × €107 = **€5,350**
- Daily volume: €5,000
- Monthly fees: €5,000 × 30 × 0.5% = **€750/month**
- **ROI on locked capital:** 14% monthly

**High volume (200 concurrent bounties):**
- Capital locked: 200 × €107 = **€21,400**
- Daily volume: €20,000
- Monthly fees: €20,000 × 30 × 0.5% = **€3,000/month**
- **ROI on locked capital:** 14% monthly

**Capital efficiency:**
- Higher volume = same ROI (14% monthly on locked capital)
- But absolute profit scales with volume
- Miners with large BCH holdings can participate at any scale

**Important caveats:**
- ROI assumes 100% capital utilization (no idle BCH waiting for bounties)
- ROI assumes 100% execution rate (no timeouts or failed transactions)
- ROI excludes operational costs (server, bot maintenance, monitoring)
- **Real-world ROI will be lower** due to idle capital, occasional timeouts, and operational overhead
- Consider these as **best-case scenarios**, not guaranteed returns

---

## Profit Model — BCH-Native Accounting

**The protocol operates in BCH. EUR only appears in the Bizum payment.**

### Example: €100 transaction (assuming 1 BCH = €1,000)

**Fiat on-ramp (only EUR touchpoint):**
```
Sender pays: €100 Bizum to seller
Seller receives: €100 in bank account
```

**BCH covenant (everything else in BCH):**
```
Transaction amount: 0.1 BCH (€100 worth at current rate)
Seller posts: 0.107 BCH (0.1 + 7% volatility buffer)

At settlement, covenant distributes:
├─ Merchant receives: 0.0995 BCH (€99.50 worth at settlement)
└─ Seller receives: 0.0075 BCH (includes fee + volatility buffer)
```

**Seller's position at settlement:**
```
€100 fiat (in bank account)
+ 0.0075 BCH (returned from covenant)
```

**Fee breakdown (in BCH):**
- Total fee: 0.001 BCH (1% of 0.1 BCH transaction)
  - Seller's reward: **0.0005 BCH** (0.5%)
  - Merchant's reward: 0.0005 BCH (0.5%, included in their 0.0995 BCH)
- Volatility buffer returned: 0.007 BCH (7% buffer)
- **Total to seller: 0.0075 BCH**

---

### Scenario 1: BCH price unchanged (1 BCH = €1,000)

**Seller's accounting:**
- Posted: 0.107 BCH (was €107)
- Received: €100 fiat
- Got back: 0.0075 BCH (worth €7.50)
- **Net: €100 + €7.50 = €107.50**
- **Profit: €0.50** (the 0.5% fee in EUR terms)

**In BCH terms:**
- Sold 0.0995 BCH for €100 fiat (merchant's portion)
- Kept 0.0075 BCH (fee + volatility buffer)
- **Fee earned: 0.0005 BCH**

---

### Scenario 2: BCH rises 5% (1 BCH = €1,050)

**Seller's accounting:**
- Posted: 0.107 BCH (was €107 when posted)
- Received: €100 fiat
- Merchant takes: 0.09476 BCH (€99.50 worth at new price)
- Got back: 0.01224 BCH (worth €12.85 at new price)
- **Net: €100 + €12.85 = €112.85**
- **Profit in EUR terms: €5.85**
  - Fee component: €0.50
  - BCH appreciation: €5.35 (the 0.01224 BCH appreciated)

**In BCH terms:**
- Sold 0.09476 BCH for €100 fiat (less BCH sold because price is higher)
- Kept 0.01224 BCH (more BCH returned)
- **Fee earned: 0.0005 BCH** (same as always)
- **Note:** The €5.35 "profit" is just the returned BCH appreciating in value

---

### Scenario 3: BCH drops 5% (1 BCH = €950)

**Seller's accounting:**
- Posted: 0.107 BCH (was €107 when posted)
- Received: €100 fiat
- Merchant takes: 0.10474 BCH (€99.50 worth at new price)
- Got back: 0.00226 BCH (worth €2.15 at new price)
- **Net: €100 + €2.15 = €102.15**
- **Loss in EUR terms: -€4.85**
  - Fee component: +€0.50
  - BCH depreciation: -€5.35 (the 0.00226 BCH lost value)

**In BCH terms:**
- Sold 0.10474 BCH for €100 fiat (more BCH sold because price is lower)
- Kept 0.00226 BCH (less BCH returned)
- **Fee earned: 0.0005 BCH** (same as always)
- **Note:** The -€4.85 "loss" is just the returned BCH depreciating in value

**Volatility buffer still protects merchant** (they got full €99.50), but seller bears price risk.

---

### **The Hedge Mechanism: Why Sellers Always Win** 🎯

**The covenant structure creates a powerful partial hedge for sellers.** Here's the key insight:

**Seller gets €100 fiat within 5 minutes (before significant price moves)**  
**Seller only has BCH price exposure on the SURPLUS (0.00226-0.01224 BCH)**

**The 5-minute Bizum window:**
- Seller posts BCH at T+0
- Iris sends Bizum within 5 minutes
- Typical Bizum arrival: 2-3 minutes
- **During this window:** Seller has full BCH exposure (0.107 BCH)
- **Volatility in 5 min:** Typically 0.5-1% (well within 7% buffer)
- **After Bizum received:** Hedge activates (converted most BCH to EUR)

This means the seller has **converted most of their BCH to EUR shortly after posting**, with only a brief window of full exposure that's negligible for BCH holders.

#### Comparison: Covenant vs. Just Holding BCH

**Scenario: BCH drops 5% (€1,000 → €950)**

| Strategy | Starting Position | Ending Position | Result |
|----------|------------------|-----------------|--------|
| **Just holding BCH** | 0.107 BCH (€107) | 0.107 BCH (€101.65) | **Loss: -€5.35** |
| **Covenant seller** | 0.107 BCH (€107) | €100 + 0.00226 BCH (€102.15) | **Loss: -€4.85** |
| **Hedge benefit** | — | — | **€0.50 less loss!** |

**The seller is €0.50 better off!** They:
- Got paid €100 in fiat BEFORE the price drop (locked in at higher rate)
- Only the tiny surplus (0.00226 BCH) is exposed to the drop
- Reduced BCH exposure by **97.9%** (from 0.107 to 0.00226)

---

**Scenario: BCH rises 5% (€1,000 → €1,050)**

| Strategy | Starting Position | Ending Position | Result |
|----------|------------------|-----------------|--------|
| **Just holding BCH** | 0.107 BCH (€107) | 0.107 BCH (€112.35) | **Gain: +€5.35** |
| **Covenant seller** | 0.107 BCH (€107) | €100 + 0.01224 BCH (€112.85) | **Gain: +€5.85** |
| **Hedge benefit** | — | — | **€0.50 MORE gain!** |

**The seller is €0.50 better off!** They:
- Got paid €100 in fiat immediately
- Kept a BIGGER surplus (0.01224 BCH) because merchant needed less BCH
- The larger surplus appreciated more

---

#### Why This Hedge Works

**Key mechanism:**
1. **Immediate payment:** Seller receives €100 Bizum at creation-time price
2. **Reduced exposure:** Only surplus BCH exposed to future price movement
3. **Asymmetric outcomes:**
   - Price drops → Only small surplus loses value (most value locked in fiat)
   - Price rises → Larger surplus gains value (keep more BCH)

**Seller's BCH exposure:**
- **Before covenant:** 0.107 BCH full exposure
- **After receiving Bizum:** Only 0.003-0.012 BCH exposed (depending on price movement)
- **Exposure reduction:** ~94-97%!

**This is why sellers always win:**
- ✅ **Price drops:** Hedge protects them (better than holding)
- ✅ **Price rises:** Keep bigger surplus (better than holding)
- ✅ **Price stable:** Earn 0.5% fee (pure profit)

**No matter what happens, the seller is better off with the covenant than just holding BCH!**

---

### Key Insight: Partial Hedge + Fee Earnings

**What seller earns:** 0.0005 BCH per transaction (fixed fee)

**What seller gets:** Partial hedge against BCH volatility (always better than just holding)

**The value proposition:**
- ✅ Reduce BCH price exposure by ~94-97% (convert most to fiat immediately)
- ✅ Earn 0.5% fee (guaranteed)
- ✅ Keep upside if BCH rises (bigger surplus = more gain)
- ✅ Protected if BCH drops (only small surplus exposed)
- ✅ Better than holding BCH in BOTH directions

**If seller wants to stay BCH-denominated:**
- Just keep accumulating BCH from fees (0.0005 BCH per bounty)
- Price movement is irrelevant to operations
- Accumulation compounds over time

**If seller needs EUR (like miners paying electricity):**
- Covenant provides partial BCH→EUR conversion at favorable terms
- Get paid immediately (before price moves)
- Only small surplus exposed to price volatility
- Always better than selling full amount on exchange

---

### Additional profit source (miners only): Avoid exchange fees

**Traditional flow:**
```
Mine BCH → Sell on exchange (pay 0.26% fee) → Get EUR
```

**Asgaya flow:**
```
Mine BCH → Post to covenant → Receive Bizum (no fee) → Get EUR
```

**Savings:** 0.26% on every BCH sold via Asgaya (vs. exchange)

---

## Risks & Mitigation

### Risk 1: Price Volatility (BCH drops >7%)

**Scenario:**
- Seller posts €107 worth of BCH
- 24 hours later, BCH drops 8%
- €107 × 0.92 = €98.44 worth (not enough to cover €99.50 needed)
- **Margin call triggered**

**Mitigation:**
- **Higher volatility buffer** (10% instead of 7%) → Covers larger drops
- **Automated time extension** (bot adds more BCH when price drops >5%)
- **Diversification** (accept bounties in multiple corridors to spread risk)
- **Hedging** (use derivatives to hedge BCH price exposure during lock period)

---

### Risk 2: Capital Lockup (Opportunity Cost)

**Scenario:**
- Seller locks €1,070 for 10 concurrent bounties
- Recipients slow to claim (average 18 hours instead of 2 hours)
- Capital sitting idle instead of earning elsewhere

**Mitigation:**
- **Dynamic pricing** (charge higher fee for slower corridors)
- **Accept only high-volume corridors** (faster turnover = better capital efficiency)
- **Stagger acceptance** (don't accept all bounties at once, spread over time)

---

### Risk 3: Automation Failures

**Scenario:**
- Seller bot crashes
- Fails to detect Bizum payment
- Doesn't sign covenant condition #1
- Bounty expires, sender's payment refunded, reputation penalty

**Mitigation:**
- **Redundant monitoring** (multiple notification parsers)
- **Alerting system** (SMS/email when bot offline)
- **Manual fallback** (can manually sign covenant if bot fails)
- **Monitoring dashboard** (real-time status of all active bounties)

---

### Risk 4: Regulatory Uncertainty

**Scenario:**
- Jurisdiction classifies BCH selling as money transmission
- Requires licensing or KYC compliance
- Seller must cease operations or obtain license

**Mitigation:**
- **Operate in favorable jurisdictions** (research local laws before participating)
- **Stay informed** (monitor regulatory developments in MiCA, PSD2, etc.)
- **Private individual status** (selling own BCH, not providing services to clients)
- **Permissionless protocol** (can exit/enter without permission if laws change)

---

## Why This Matters for Asgaya

**Decentralization:**
- Multiple BCH sellers = no single point of failure
- Anyone can participate = permissionless liquidity
- Geographic distribution = corridor resilience

**Sustainability:**
- Miners have long-term commitment (capital invested in equipment)
- Seller fees make mining more sustainable (post-block-reward)
- Aligned incentives (BCH growth = seller profitability)

**Scalability:**
- More volume = more sellers join (profit opportunity)
- More sellers = more corridors covered (geographic expansion)
- More corridors = more adoption (network effects)

**BCH Ecosystem Growth:**
- Asgaya success directly benefits BCH sellers (especially miners)
- Sellers promote BCH adoption (economic incentive to grow network)
- Stronger BCH network = more secure remittances = more trust = more volume

---

## Real-World Example: Solar Miner in Spain

**Scenario:** Small-scale BCH miner using excess solar power

**Current setup (mining only):**
- 5 kW solar array
- 2 kW household use, 3 kW excess during peak sun (5 hours/day)
- Mines ~0.00045 BCH/day (~€0.135 at €300/BCH)
- Annual mining revenue: **~€49**
- Exchange fees to sell BCH: €0.13/year (0.26% of €49)
- **Net: €48.87/year**

**With Asgaya BCH seller role (assuming 1 BCH = €300):**
- Same mining revenue: €49/year
- Accept 3 bounties/day (low-volume corridor, €100 each):
  - Each €100 transaction = 0.333 BCH at current rate
  - Seller fee: 0.5% = 0.001665 BCH per transaction
  - Annual BCH fees: 3 × 0.001665 × 365 = **1.825 BCH/year**
  - EUR equivalent: 1.825 × €300 = **€547.50/year**
- Avoid exchange fees on Asgaya-sold BCH: **€1.42/year**
- **Total revenue: €597.92/year** (1,123% increase vs. mining alone!)

**Key insight:** Seller accumulates **1.825 BCH/year** in fees (can hold as BCH or convert to EUR as needed)

**Plus:**
- Support BCH adoption
- Help remittance users save money (vs. 6.49% Western Union)
- Contribute to permissionless financial infrastructure

**Investment required:** Near zero (reuse existing mining infrastructure + install seller bot)

---

## Implementation Path

### Phase 0 (May-June 2026): Recruit Pioneer Sellers
**Target:** Small BCH miners + HODLers with technical capability
- Spain-Venezuela corridor (test route)
- €50-100 transaction sizes
- 1-10 bounties/day volume
- **Goal:** Prove concept, gather data

### Phase 1 (July-Dec 2026): Scale to Medium Sellers
**Target:** 100-500 kW mining operations + active traders
- Add Honduras, Argentina corridors
- €100-500 transaction sizes
- 50-200 bounties/day volume
- **Goal:** Demonstrate profitability, build reputation system

### Phase 2 (2027): Attract Institutional Sellers
**Target:** Multi-MW miners + small exchanges
- Global corridor coverage
- €500-5,000 transaction sizes
- 1,000+ bounties/day volume
- **Goal:** Achieve critical mass, network effects kick in

---

## Related Concepts

- [Pull System](./pull-system.md) — How recipient-triggered execution works
- [With volatility buffer Bounty Contracts](./bounty-contracts-with-volatility-buffer.md) — Technical covenant implementation
- [Fee Splitting Model](../decisions/fee-splitting-model.md) — How fees are distributed (0.5% to seller)
- [Core Regulatory Constraints](./core-regulatory-constraints.md) — Why this model is MiCA/PSD2 compliant

---

## Conclusion

BCH sellers are the **BCH inventory backbone** of Asgaya. While anyone with BCH holdings can participate, **miners are the ideal sellers** due to:
- ✅ Native BCH ownership (no acquisition cost)
- ✅ Existing conversion needs (BCH → fiat for operations)
- ✅ Technical infrastructure (24/7 uptime, automation capability)
- ✅ Aligned incentives (BCH growth = mining profitability)
- ✅ Dual revenue streams (mining + selling)

By making BCH mining more sustainable—especially in the post-block-reward era—Asgaya creates a **virtuous cycle** that benefits miners, recipients, merchants, and the entire BCH ecosystem.

**Permissionless, profitable, and protocol-aligned.**

---

**Last updated:** May 10, 2026  
**Status:** Core concept, in active development (Chipnet testing phase)  
**Next:** Recruit pioneer BCH sellers for Phase 0 validation
