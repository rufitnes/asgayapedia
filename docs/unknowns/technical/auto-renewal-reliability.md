# Auto-Renewal Reliability

**Status:** Not Started  
**Priority:** Medium  
**Last Updated:** 2026-06-18  
**Contributors Welcome:** Yes

---

## What We Don't Know

**Can the auto-renewal bot reliably renew AnyHedge contracts without failures?**

Specifically:
- What's the renewal success rate target? (99%? 99.9%? 100%?)
- What causes renewal failures? (network congestion, bot downtime, oracle issues?)
- What happens to user tokens when renewal fails?
- Can we guarantee renewals if contract period is short (1 week = 52/year)?

---

## Why It Matters

**Renewal reliability determines user trust and system robustness.**

### If renewal success = 100%:
- Users trust auto-renew (set and forget)
- Tokens maintain peg indefinitely
- Contract period can be short (1 week works)

### If renewal success = 95%:
- 1 in 20 renewals fail
- With 100 active tokens: ~5 fail per renewal cycle
- Users experience: "My H€ stopped working"
- Trust eroded, users avoid H€

### If renewal success = 80%:
- System fundamentally broken
- Users can't rely on stability
- Must abandon auto-renew (manual renewal instead)

**Wrong assumption = Broken peg, user losses, system failure.**

---

## Current Hypothesis

**Auto-renewal can achieve >99% success rate with proper infrastructure.**

**Reasoning:**

**Similar systems exist:**
- AnyHedge likely already handles renewals
- Bitcoin transaction bots are reliable (>99.9% uptime possible)
- This is a solved problem in DeFi

**Failure modes are manageable:**
- Bot downtime → deploy redundant bots
- Network congestion → queue renewals with fee bumping
- Oracle failure → fallback oracle sources

**But:** Haven't tested. Don't know AnyHedge renewal specifics. Could be harder than expected.

---

## Investigation Method

### Step 1: Research AnyHedge Renewal Mechanism

**Questions:**
- Does AnyHedge support auto-renewal natively?
- Or must we build renewal logic ourselves?
- What's the renewal transaction structure?
- Does renewal require both parties to sign? (merchant + bull)
- Or can one party renew unilaterally?

**Read:**
- [AnyHedge documentation](https://anyhedge.com)
- AnyHedge contract source code
- Existing AnyHedge implementations (MUSD renewal logic?)

**Deliverable:** Technical specification of renewal process

### Step 2: Identify Failure Modes

**Possible failures:**

**A. Bot Downtime**
- Server crashes
- Network outage
- Power failure
- Mitigation: Redundant bots on different servers/regions

**B. Network Congestion**
- BCH mempool full (rare but possible)
- Renewal transaction stuck
- Mitigation: Fee bumping (RBF), queue management

**C. Oracle Unavailable**
- Oracle feed offline during renewal window
- Can't get price for new contract
- Mitigation: Multiple oracle sources, cached prices

**D. Insufficient BCH for Fees**
- Bot wallet empty
- Can't pay transaction fee
- Mitigation: Automated wallet top-up, alerts

**E. Bull Pool Exhausted**
- No long capital available for renewal
- Contract can't be recreated
- Mitigation: Pool reserve for renewals, warning system

**F. Smart Contract Bug**
- Renewal logic has bug
- Renewal transaction invalid
- Mitigation: Thorough testing, gradual rollout

**Deliverable:** Failure mode analysis with mitigations

### Step 3: Design Redundancy Architecture

**Single bot (risky):**
```
One bot → one server → single point of failure
Uptime: ~99% (with restarts, updates)
```

**Redundant bots:**
```
Bot A (primary) on VPS 1 (Europe)
Bot B (standby) on VPS 2 (US)
Bot C (backup) on VPS 3 (Asia)

If A fails to renew within 2 hours:
→ B attempts renewal
If B fails within 1 hour:
→ C attempts renewal

Uptime: ~99.99% (all three fail simultaneously unlikely)
```

**Cost:**
- Single VPS: €5/month
- Triple redundancy: €15/month
- Worth it for 99.99% vs 99% reliability

**Deliverable:** Redundancy architecture diagram and cost estimate

### Step 4: Calculate Renewal Transaction Costs

**For 1-week contracts:**
- 52 renewals per year per token
- Transaction fee: ~€0.002 per renewal
- Annual cost per token: 52 × €0.002 = €0.10

**For 100 active tokens:**
- 5,200 renewal transactions per year
- Total fees: €10.40/year

**Affordable.** Transaction costs not a blocker.

**For 30-day contracts:**
- 12 renewals per year per token
- Annual cost per token: €0.024
- For 100 tokens: €2.40/year

**Even cheaper, but less flexible for users.**

**Deliverable:** Cost analysis of renewal frequencies

### Step 5: Test on BCH Testnet

**Phase 0 preparation:**

1. Deploy AnyHedge contract on testnet
2. Configure auto-renewal bot
3. Create test H€ tokens
4. Let them auto-renew through multiple cycles
5. Intentionally fail components (kill bot, delay oracle, etc.)
6. Measure success rate and failure recovery time

**Success criteria:**
- 100 renewal attempts
- >99 successes
- Failures recover within 1 hour (standby bot takes over)

**Deliverable:** Testnet results proving reliability

---

## Success Criterion

**This unknown is answered when:**

1. ✅ **We understand:**
   - How AnyHedge renewal works technically
   - What failure modes exist
   - How to build redundancy

2. ✅ **We have tested:**
   - Testnet deployment with multiple renewal cycles
   - Success rate measured (>99% target)
   - Failure recovery tested (redundant bots work)

3. ✅ **We can commit to SLA:**
   - "Auto-renewal will succeed >99% of the time"
   - "Redundant bots ensure recovery within 1 hour if primary fails"
   - "Users can trust set-and-forget auto-renew"

**Answered = "Renewal reliability is X%, here's our architecture, here's testnet proof."**

---

## Contributor Guidance

**Skills needed:**
- BCH development (CashScript, transaction building)
- AnyHedge protocol knowledge
- DevOps (bot deployment, monitoring, redundancy)
- Testing (testnet deployment)

**Estimated effort:** 8-12 hours (includes testnet testing)

**How to start:**
1. Read AnyHedge documentation on contract renewal
2. Find AnyHedge GitHub repo, review renewal logic
3. Set up BCH testnet wallet
4. Deploy simple AnyHedge contract on testnet
5. Attempt manual renewal (understand process before automating)
6. Document findings in GitHub issue or email rufitnes@proton.me

**This is advanced contribution** - requires technical skills. But even partial research (Step 1-2) helps!

---

## Related Documents

- [Stability Layer Overview](../../the-mechanism/stability-layer/README.md)
- [Contract Period Duration Unknown](../economic/contract-period-duration.md)
- [AnyHedge Claim Compatibility Unknown](anyhedge-claim-compatibility.md)

---

## User Impact of Renewal Failure

**What user sees when renewal fails:**

**Scenario: Alice holds 100 H€, renewal fails at maturity**

**Option A: Graceful degradation**
```
Contract matures → settlement executes → Alice gets BCH
Notification: "Your H€ contract ended. Received BCH equivalent. 
              Convert back to H€ or keep BCH?"
```
- Alice not stuck with broken token
- Can re-mint H€ manually if desired
- Annoying but not catastrophic

**Option B: Token becomes worthless**
```
Contract matures → renewal fails → no settlement → Alice stuck
Token shows: "ERROR: Contract expired, no value"
```
- Alice loses funds
- Unacceptable user experience
- Must prevent this

**Our design must ensure Option A (graceful degradation), never Option B.**

---

## Monitoring & Alerts

**Phase 0 dashboard should track:**
- Upcoming renewals (next 24 hours)
- Renewal success rate (last 7 days, last 30 days)
- Failed renewals (investigation needed)
- Bot uptime (primary, secondary, tertiary)
- Oracle feed status (online/offline)
- Pool capacity for renewals

**Alerts:**
- Email/SMS when renewal fails
- Slack notification when bot goes offline
- Warning when pool approaching exhaustion

**Continuous monitoring ensures problems caught early.**
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
