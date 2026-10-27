# PH-ResQ Implementation Status

Snapshot: 2026-10-27. Status: early Android implementation baseline; **not a release candidate**.

## Automated evidence

- **Validated commit:** `4a611c8f085cc20ac5ec9558b07074ba2c1b26df`
- **GitHub Actions:** passed — [run 40450465511](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/40450465511)
- **Executed tasks:** `testDebugUnitTest`, `assembleDebug`, and `assembleRelease`; workflow artifact upload succeeded.
- **Unit coverage in this revision:** contact parsing and SOS message/state behavior (including retry-pending transitions); geohash encoding; SHA-256 digest verification; authenticated mesh-envelope encode/decode, tamper/expiry/hop checks and bounded deduplication; rolling-token rotation; adaptive-location interval policy.
- **Artifact status:** CI uploaded APK build outputs for 14 days. The release APK is unsigned; this is not a signed production artifact.
- **Not evidenced by CI:** physical-device behavior, SMS recipient handling, actual GPS fixes, BLE interoperability/battery use, voice recording/playback, key migration on a real device, offline data coverage, accessibility, or end-to-end emergency delivery.

## Feature status

| Area | Status | Evidence / limitation |
|---|---|---|
| Android project and CI | Build and unit tests passed on the validated SHA | Every later candidate must be revalidated; no Gradle wrapper is currently committed |
| Consent gate | Basic local preference implemented | Consent version/timestamp and full policy/lifecycle testing remain incomplete |
| SOS press-and-hold | Implemented as a 2-second hold-to-prepare flow with a separate accessible confirmation alternative | UI timing, cancellation, haptics and accidental-activation tests remain |
| Emergency contacts | Contact list stored encrypted with Android Keystore AES-GCM | Not a PIN vault; key-loss, reinstall/recovery, corruption and device tests remain |
| SMS | User-confirmed messaging-app handoff | Recipient URI behavior varies; no delivery confirmation; no unattended/direct SMS dispatch |
| SOS persistence | SQLite SOS event history with encrypted draft/recipient/location fields; `RETRY_PENDING` state is represented in the state machine | UI does not yet implement automatic retry/backoff; recovery and database migration tests remain |
| Location | Last-known location helper and adaptive interval policy utility | No fresh GPS acquisition, lifecycle-aware polling, stale/accuracy enforcement or foreground location service |
| Voice note | Encrypted local recording, capped at 10 seconds/2 MB | No playback, deletion UI, or attachment to SOS; device and lifecycle tests remain |
| Hotline subset | Searchable offline national-agency contacts and dialer/SMS handoff | 911 portal source checked 2026-10-27; remaining numbers need independent current verification and local jurisdiction data |
| BLE | 15-second low-power advertisement discovery | No authenticated data exchange, SOS transfer or multi-hop transport |
| Mesh protocol utilities | HMAC-authenticated versioned envelope, payload/time/hop bounds and in-memory dedupe cache | No peer key establishment, BLE/Wi-Fi transport, durable replay cache, ACK/retry or end-to-end delivery |
| Geohash/integrity/token utilities | Implemented and unit-tested in CI | Not connected to a nationwide data importer, trusted manifest or recovery pipeline |
| SQLite schema | SOS events plus schema foundations for location, directory, map metadata, search, bookmarks and family check-ins | Most tables have no complete repositories/importers, UI or migration/acceptance coverage |
| Family check-in | Local-only status | No network relay or family delivery; not yet a durable sync queue |
| Nationwide directory/maps | Not implemented | No audited nationwide service-contact dataset or licensed offline map package |
| PIN vault/duress mode | Not implemented | Contact-field encryption alone is not a PIN-protected vault |
| PAGASA/evacuation/cell-broadcast awareness | Not implemented | Data ingestion/freshness not validated; third-party app cannot promise to intercept all cell broadcasts |
| Physical-device tests | Not run | Requires target devices, SIM/network, permissions and multi-device mesh testing |
| Release signing | Not ready | Unsigned build only; no signed release or go/no-go evidence |

A manifest permission, UI card, schema table, utility, or successful build is not proof that a complete feature works. Do not distribute this early build as a dependable emergency service.
