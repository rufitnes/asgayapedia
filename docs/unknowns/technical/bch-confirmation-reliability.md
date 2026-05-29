# BCH Confirmation Reliability

**Status:** Not Started  
**Priority:** Low  
**Last Updated:** 2026-05-29  
**Contributors Welcome:** Yes

## What We Don't Know

What percentage of BCH transactions confirm successfully in the first block? How often do transactions get stuck or require rebroadcast?

## Why It Matters

Transaction reliability affects user experience and system timing assumptions. Frequent failures would require additional error handling.

## Current Hypothesis

BCH transactions with appropriate fees confirm in the next block 99%+ of the time, with minimal need for rebroadcast logic.

## Investigation Method

1. Analyze BCH mainnet transaction confirmation rates
2. Test various fee levels and confirmation times
3. Identify failure modes (low fees, network issues)
4. Model system performance under various reliability scenarios

## Success Criterion

Documented BCH transaction reliability with measured confirmation rates in Phase 0 trials.

## Phase 0 Trial Integration

Track every BCH transaction: time to first confirmation, any rebroadcast needs, failures.

## Contributor Guidance

**Skills needed:** Blockchain analysis, BCH infrastructure  
**Estimated effort:** 3-4 hours  
**How to start:** Query BCH block explorers for transaction confirmation data

## Related Documents

- [BCH Native Architecture](../../android-app/backend-apis/bch-native-architecture.md)
