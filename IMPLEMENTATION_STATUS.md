# PH-ResQ Implementation Status

Snapshot: 2026-10-27. Status: early Android implementation baseline; **not a release candidate**.

## Automated evidence

- **Validated candidate SHA:** `408e2aaaea09df70400c92038b405c5d5a47d337`
- **GitHub Actions:** passed — [run 40451629467](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/40451629467)
- **Executed tasks:** `testDebugUnitTest`, `assembleDebug`, and `assembleRelease`; artifact upload succeeded.
- **Unit-test coverage:** contact parsing and SOS message/state behavior (including retry-pending transitions); geohash encoding; SHA-256 digest verification; authenticated mesh-envelope encode/decode, tamper/expiry/hop checks and bounded deduplication; rolling-token rotation; adaptive-location interval policy; Robolectric tests for search-history deduplication/clearing, bookmark save/update/removal, family check-in persistence, and database reopen persistence.
- **Artifact status:** `ph-resq-apk-builds` is available from the CI run until 2026-11-10. The release APK is unsigned; this is not a signed production artifact.
- **Not evidenced by CI:** physical-device behavior, SMS recipient handling, actual GPS fixes, BLE interoperability/battery use, voice recording/playback, Android Keystore migration on a real device, nationwide data coverage, accessibility, or end-to-end emergency delivery.

## Feature status

| Area | Status | Evidence / limitation |
|---|---|---|
| Android project and CI | Unit tests and debug/release assemble passed on the validated SHA | Every later candidate must be revalidated; no Gradle wrapper is committed |
| Consent/privacy notice | Versioned consent record with acceptance timestamp and current-build notice | Legal/privacy review and full data-deletion/export behavior remain incomplete |
| SOS press-and-hold | 2-second hold-to-prepare flow plus accessible confirmation alternative | UI timing, cancellation, haptics and accidental-activation tests remain |
| Emergency contacts | Contact list stored encrypted with Android Keystore AES-GCM | Not a PIN vault; key-loss, reinstall/recovery, corruption and device tests remain |
| SMS | User-confirmed messaging-app handoff | Recipient URI behavior varies; no delivery confirmation; no unattended/direct SMS dispatch |
| SOS persistence | SQLite SOS event history with encrypted draft/recipient/location fields | Retry-pending state is represented, but automatic retry/backoff and full recovery remain incomplete |
| Location | Last-known location helper and adaptive interval policy utility | No fresh GPS acquisition, lifecycle-aware polling, stale-location threshold enforcement or foreground location service |
| Voice note | Encrypted local recording, capped at 10 seconds/2 MB | No playback, deletion UI or attachment to SOS; device/lifecycle tests remain |
| Hotline subset | Searchable offline national-agency contacts and dialer/SMS handoff | 911 portal source checked 2026-10-27; remaining numbers need independent current verification and local jurisdiction data |
| BLE | 15-second low-power advertisement discovery | No authenticated data exchange, SOS transfer or multi-hop transport |
| Mesh protocol utilities | HMAC-authenticated versioned envelope, payload/time/hop bounds and in-memory dedupe cache | No peer key establishment, BLE/Wi-Fi transport, durable replay cache, ACK/retry transport or end-to-end delivery |
| Geohash/integrity/token utilities | Implemented and unit-tested in CI | Not connected to a nationwide data importer, trusted manifest or recovery pipeline |
| Local history/bookmarks/check-in | SQLite-backed search history, hotline bookmarks, and local check-in; persistence tests passed in CI | No remote family sync; device migration/resilience tests remain |
| Other SQLite tables | SOS events plus schema foundations for location, directory, map metadata and map tiles | Directory/map/location data importers, repositories, and complete UI are not implemented |
| Nationwide directory/maps | Not implemented | No audited nationwide service-contact dataset or licensed offline map package |
| PIN vault/duress mode | Not implemented | Contact-field encryption alone is not a PIN-protected vault |
| PAGASA/evacuation/cell-broadcast awareness | Not implemented | Data ingestion/freshness not validated; third-party app cannot promise to intercept all cell broadcasts |
| Physical-device tests | Not run | Requires target devices, SIM/network, permissions and multi-device mesh testing |
| Release signing | Not ready | Unsigned build only; no signed release or go/no-go evidence |

A manifest permission, UI card, schema table, utility, or successful build is not proof that a complete feature works. Do not distribute this early build as a dependable emergency service.
