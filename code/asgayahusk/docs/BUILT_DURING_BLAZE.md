# Built during BCH BLAZE 2026

**Baseline tag:** `v0-prehackathon-baseline` (2026-11-11)
**Build window:** BCH BLAZE 2026, kickoff 2026-11-14 14:00 UTC, submissions 2026-11-22 14:00 UTC.

This repository is the **pre-BLAZE baseline** of the AsgayaHusk Android reference implementation: the native-network + WebView-compute hybrid (covenant v2.7.1, Nostr coordination, signed oracle, notification bot). It is validated on testnet3 with real devices; the unit tests pass.

The BLAZE slice (Mercar Asgaya: the web board, the identity anchor, the direct checkout) is built in the competition repository from 14 Nov. This baseline stays frozen for comparison; the daily build log lives in the competition repository (`docs/build-log/`).

Build and test (offline):

```bash
./gradlew --offline :app:assembleDebug :app:testDebugUnitTest
```

Public documentation: [asgayapedia](https://github.com/rufitnes/asgayapedia).
