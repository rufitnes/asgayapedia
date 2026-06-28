# DolarAPI Accuracy

**Status:** Not Started  
**Priority:** Medium  
**Last Updated:** 2026-05-29  
**Contributors Welcome:** Yes

## What We Don't Know

How accurate and reliable is DolarAPI for EUR→VES exchange rates? How often is it updated? What's the uptime?

## Why It Matters

Exchange rate accuracy affects:
- Sender expectations (how much bolívares recipient gets)
- Seller risk (rate changes during covenant window)
- System competitiveness (accurate rates = better UX)

## Current Hypothesis

DolarAPI provides accurate market rates updated hourly with 99%+ uptime, sufficient for Phase 0 needs.

## Investigation Method

1. Monitor DolarAPI rate updates and uptime
2. Compare rates to alternative sources
3. Measure rate staleness (time since last update)
4. Test API failure modes and fallback strategies
5. Evaluate alternative rate sources

## Success Criterion

Documented DolarAPI reliability metrics: uptime, update frequency, accuracy vs other sources.

## Phase 0 Trial Integration

Log every rate fetch: timestamp, rate, source, any errors or stale data.

## Contributor Guidance

**Skills needed:** API monitoring, data analysis  
**Estimated effort:** 2-3 hours  
**How to start:** Monitor DolarAPI for 48-72 hours, log all responses

- [Rate APIs](../../android-app/backend-apis/rate-apis.md)
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
