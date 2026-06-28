# Volatility Buffer Rate

**Status:** Not Started  
**Priority:** Critical  
**Last Updated:** 2026-05-29  
**Contributors Welcome:** Yes

## What We Don't Know

Is the 7% volatility buffer rate sufficient to protect BCH sellers from price volatility during the covenant settlement window?

## Why It Matters

If the buffer is too small, sellers lose money when BCH price drops. If it's too large, capital efficiency decreases and the system becomes less competitive.

## Current Hypothesis

7% buffer provides adequate protection for a 24-hour settlement window based on historical BCH volatility analysis.

## Investigation Method

1. Analyze BCH/USD price volatility over 24-hour windows (last 2 years)
2. Calculate 95th percentile price movement
3. Model seller losses at various buffer rates
4. Compare 7% vs. 5%, 10%, 15% scenarios

## Success Criterion

Statistical analysis showing 7% buffer covers 95%+ of 24-hour price movements with acceptable capital efficiency tradeoff.

## Phase 0 Trial Integration

Track actual BCH price movements during trial covenants and measure buffer adequacy.

## Contributor Guidance

**Skills needed:** Data analysis, statistical modeling  
**Estimated effort:** 4-6 hours  
**How to start:** Pull BCH/USD historical data from major exchanges

## Related Documents

- [Risk Allocation Principle](../../../why-this-design/README.md)
- [Bounty Contracts with Volatility Buffer](../../../glossary.md#payment-first-covenant)
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
