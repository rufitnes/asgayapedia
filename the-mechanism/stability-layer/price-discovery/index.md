# Price Discovery: Oracle Strategy for H€/HAu Tokens

**Status:** Research Needed  
**Priority:** Medium (needed before Phase 0 launch)  
**Owner:** TBD

---

## Purpose

Document the oracle strategy for H€ and HAu token price discovery, including:
- Primary oracle sources
- Fallback oracle sources  
- Oracle chain complexity (direct vs multi-hop)
- Reliability requirements
- Failure modes and recovery

---

## Research Questions

### H€ (Euro-Pegged) Oracles

**Primary path:**
- [ ] Which BCH/EUR oracles are available? (GeneralProtocol, CoinGecko, others?)
- [ ] What is their update frequency?
- [ ] Historical uptime/reliability data?
- [ ] API availability and rate limits?

**Fallback path:**
- [ ] BCH/USD oracles (which sources?)
- [ ] USD/EUR oracles (which sources?)
- [ ] How to detect primary failure and switch to fallback?

### HAu (Gold-Pegged) Oracles

**EUR-denominated gold (preferred):**
- [ ] Do EUR/XAU oracles exist? (LBMA publishes in USD, but conversions available?)
- [ ] Which data providers offer EUR-denominated gold prices?
- [ ] Update frequency and reliability?

**USD-denominated gold (fallback):**
- [ ] LBMA AM/PM London Fix (twice daily, reliable?)
- [ ] CME gold futures (continuous, more volatile?)
- [ ] COMEX spot prices (exchange hours only?)
- [ ] Which provides best reliability for AnyHedge contracts?

**Oracle chain complexity:**
- [ ] If using USD-denominated gold: BCH/EUR + EUR/USD + USD/XAU = 3 oracles
- [ ] Can we simplify to BCH/USD + USD/XAU = 2 oracles?
- [ ] What's the error propagation through multiple oracle hops?

---

## Oracle Failure Scenarios

**What happens if:**
- Primary oracle goes offline during contract creation?
- Primary oracle diverges significantly from fallback (>1% difference)?
- Both primary and fallback fail simultaneously?
- Oracle is manipulated or reports incorrect prices?

**Mitigation strategies:**
- [ ] Multiple oracle sources (median of 3?)
- [ ] Circuit breakers (halt minting if deviation >X%)
- [ ] Manual override capability (governance)
- [ ] Graceful degradation (refuse to mint, don't break existing tokens)

---

## Integration with AnyHedge

**Questions for GeneralProtocols (AnyHedge developers):**
- [ ] Which oracle feeds does AnyHedge currently support?
- [ ] Can we add custom oracle sources?
- [ ] How does AnyHedge handle oracle failures during settlement?
- [ ] What's the oracle update frequency requirement?
- [ ] Can contracts use multiple oracles with fallback logic?

---

## Phase 0 Minimal Viable Strategy

**For launch, we need:**

**H€:**
- Primary: BCH/EUR (CoinGecko API)
- Fallback: BCH/USD (CoinGecko) + USD/EUR (ECB reference rate)
- Update frequency: 1 minute (CoinGecko API limit)

**HAu:**
- Primary: BCH/USD (CoinGecko) + USD/XAU (LBMA AM/PM Fix)
- Fallback: Same sources, different timing windows
- Update frequency: BCH/USD (1 min), USD/XAU (twice daily)
- **Note:** Twice-daily XAU updates may be insufficient for short-duration contracts (1-3 days)

**Research needed:** Can we find intraday gold price feeds (CME futures?) for better HAu granularity?

---

## Next Steps

1. [ ] Survey available BCH/EUR oracle providers
2. [ ] Survey available EUR/XAU and USD/XAU oracle providers
3. [ ] Contact GeneralProtocols about AnyHedge oracle integration
4. [ ] Test CoinGecko API reliability and rate limits
5. [ ] Design oracle fallback logic (when to switch, how to detect failure)
6. [ ] Document final oracle strategy in this file
7. [ ] Update stability-layer/README.md with confirmed oracle sources

---

## References

- [AnyHedge Documentation](https://anyhedge.com)
- [CoinGecko API](https://www.coingecko.com/en/api)
- [LBMA Gold Price](https://www.lbma.org.uk/gold-price)
- [CME Gold Futures](https://www.cmegroup.com/markets/metals/precious/gold.html)
- [ECB Reference Rates](https://www.ecb.europa.eu/stats/policy_and_exchange_rates/)

---

**This is a placeholder.** Research needed before Phase 0 launch to validate oracle strategy and fallback mechanisms.
