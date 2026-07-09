# Universal Bot Reliability

**Status:** In Progress  
**Priority:** High  
**Last Updated:** 2026-07-09  
**Contributors Welcome:** Yes

## Phase -1 Progress (July 2026)

**Partial validation completed:**
- ✅ 24-hour continuous operation test (Pixel 6a, Android 14)
- ✅ Five Spanish banks tested (BBVA, Santander, CaixaBank, Sabadell, Caja Rural)
- ✅ Production hardening: boot receiver, battery exemption, foreground service
- ✅ Bizum notification parsing validated
- ✅ Research published: [RS072: Bizum Notification Patterns](../../research/RS072_notification_listener/), [RS073: NotifyFlow Comparison](../../research/RS072_notification_listener/RS073_notifyflow_decompilation.md)

**Still unknown:**
- ❌ Multi-device testing (only one device tested)
- ❌ Various Android OS versions (only Android 14)
- ❌ Poor network conditions
- ❌ Formal 99%+ reliability metric

**Next steps:** Phase 0 trials with multiple users/devices to validate across device range.

## What We Don't Know

How reliably does the notification listener bot perform on real Android devices with varying network conditions, battery states, and OS versions?

## Why It Matters

Bot reliability is the technical foundation. If bots fail to detect payments or BCH transactions, the entire system breaks.

## Current Hypothesis

Bot achieves 99%+ reliability on Android 10+ devices with stable network connectivity.

## Investigation Method

1. Test bot on various Android devices and OS versions
2. Measure notification detection rates
3. Test under poor network conditions
4. Measure battery impact and background task survival
5. Simulate real-world failure scenarios

## Success Criterion

Demonstrated 99%+ notification detection reliability across target device range in Phase 0 trials.

## Phase 0 Trial Integration

Log all bot performance metrics: notification delays, missed notifications, battery usage, crashes.

## Contributor Guidance

**Skills needed:** Android development, QA testing  
**Estimated effort:** 6-10 hours  
**How to start:** Set up test devices and notification monitoring

## Related Documents

- [Notification Listener](../../android-app/notification-listener/README.md)
- [Multi-Device Test Plan](../../android-app/backend-apis/multi-device-test-plan.md)
---

## Navigation

**[🏠 Home](../../index.md)** | **[↑ Unknowns](../README.md)** | **[📖 Glossary](../../glossary.md)**
