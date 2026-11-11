# Research code

**These are research artifacts, not a production release.**

The reference implementation and the proof-of-concept spikes that back the public docs. The snapshot is the **pre-BLAZE baseline**, tagged `v0-prehackathon-baseline` (2026-11-11). The BCH BLAZE 2026 slice (Mercar Asgaya: the web board, the identity anchor, the direct checkout) is built from 14 Nov.

## Contents

- [`asgayahusk/`](./asgayahusk/) — the Android reference implementation: the native-network + WebView-compute hybrid (covenant v2.7.1, Nostr coordination, signed oracle, notification bot). Validated on testnet3 with real devices; the unit tests pass.
- [`anchor-mint-spike/`](./anchor-mint-spike/) — the proof of concept for the identity anchor: a browser dapp connects through WizardConnect, the wallet signs a genesis anchor NFT, and the result is verified on chipnet.

## Running the code

The snapshot points the local services at **`localhost`**:

- oracle API: `http://localhost:3001`
- Fulcrum (testnet3): `localhost:60001`
- Nostr relay: `ws://localhost:7777`

Point them at your own setup. The app's local testnet keys are **not** included; copy `asgayahusk/app/src/main/assets/secrets/testnet-keys.example.json` to `testnet-keys.json` and fill your own testnet WIFs.

```bash
# AsgayaHusk
cd asgayahusk && ./gradlew --offline :app:assembleDebug :app:testDebugUnitTest

# anchor-mint spike
cd anchor-mint-spike && npm install && npm test
```

## License

- **Code:** MIT, see [`LICENSE`](./LICENSE).
- **Documentation:** CC BY 4.0, see the repository root `LICENSE`.

Testnet only. Do not use production funds or keys.
