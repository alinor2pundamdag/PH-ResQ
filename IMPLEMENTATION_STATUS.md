# PH-ResQ Implementation Status

Snapshot: 2026-10-27. Status: expanded Android implementation pass; **not a release candidate**. Latest verified PR-head commit: `3d1e60101e1778ddd2df301ae81ca2d1d476c110`; CI run [40495534150](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/40495534150) passed `lintDebug`, `testDebugUnitTest`, `assembleDebug`, and `assembleRelease`, verified the checked-out SHA against the PR-head SHA, and uploaded APKs plus SHA-256/build metadata. Release APK is unsigned. Latest code includes bounded fresh-location requests, a best-effort low-battery snapshot, an encrypted durable local check-in outbox, a PBKDF2/AES-GCM PIN-vault utility, and a user-visible SOS handoff foreground-service notification with a stop action. These additions do not complete all 34 requirements. Voice-note sharing now opens the Android chooser with an audio attachment and SOS draft text; an offline directory importer and integrity-checked map-tile cache are present as data primitives, not as a complete nationwide product. A locally generated Medical ID QR with explicit field selection/privacy warning and a persistent high-contrast theme toggle are also present; both remain partial pending accessibility/privacy acceptance tests.

## Automated evidence

- **Current PR-head SHA:** `3d1e60101e1778ddd2df301ae81ca2d1d476c110`.
- **GitHub Actions:** passed — [run 40495534150](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/40495534150).
- **Executed tasks on current head:** `lintDebug`, `testDebugUnitTest`, `assembleDebug`, and `assembleRelease`; checkout SHA was compared to the PR-head SHA; APKs and build evidence were uploaded.
- **Unit-test coverage:** contact parsing and SOS message/state behavior (including retry-pending transitions); geohash encoding; SHA-256 digest verification; authenticated mesh-envelope encode/decode, tamper/expiry/hop checks and bounded deduplication; rolling-token rotation; adaptive-location interval policy; Robolectric tests for search-history deduplication/clearing, bookmark save/update/removal, family check-in persistence, and database reopen persistence.
- **Artifact status:** `ph-resq-apk-builds` from [run 40495534150](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/40495534150) is available until 2026-11-10. It contains debug/release APKs and build evidence including SHA-256 sums. The release APK is unsigned; this is not a signed production artifact.
- **Not evidenced by CI:** physical-device behavior, SMS recipient handling, actual GPS fixes, BLE interoperability/battery use, voice recording/playback, Android Keystore migration on a real device, nationwide data coverage, accessibility, or end-to-end emergency delivery.

## Feature status

| Area | Status | Evidence / limitation |
|---|---|---|
| Android project and CI | Current PR head passed lint, unit tests, debug/release assemble and SHA-verified artifact upload on CI run 40495534150 | No Gradle wrapper is committed; current APK is testable but not a signed production release |
| Consent/privacy notice | Versioned consent record with acceptance timestamp and current-build notice | Legal/privacy review and full data-deletion/export behavior remain incomplete |
| SOS press-and-hold | 2-second hold-to-prepare flow plus accessible confirmation alternative | UI timing, cancellation, haptics and accidental-activation tests remain |
| Emergency contacts | Contact list stored encrypted with Android Keystore AES-GCM | Not a PIN vault; key-loss, reinstall/recovery, corruption and device tests remain |
| SMS | User-confirmed messaging-app handoff | Recipient URI behavior varies; no delivery confirmation; no unattended/direct SMS dispatch |
| SOS persistence | SQLite SOS event history with encrypted draft/recipient/location fields | Retry-pending state is represented, but automatic retry/backoff and full recovery remain incomplete |
| Location | Last-known helper plus bounded one-shot fresh location request (15-second timeout) and saved fix metadata | No lifecycle-aware continuous polling/foreground service; freshness and accuracy policy still need acceptance tests |
| Voice note | Encrypted local recording capped at 10 seconds/2 MB; list/playback/delete and explicit system share chooser with temporary plaintext cleanup added | User chooses app/recipient; messaging app support and actual delivery are unverified; device/lifecycle tests remain |
| Hotline subset | Searchable offline national-agency contacts and dialer/SMS handoff | 911 portal source checked 2026-10-27; remaining numbers need independent current verification and local jurisdiction data |
| BLE | 15-second low-power advertisement discovery | No authenticated data exchange, SOS transfer or multi-hop transport |
| Mesh protocol utilities | HMAC-authenticated envelope, payload/time/hop bounds, durable encrypted outbox, bounded retry state, HMAC ACK verification and persistent replay cache | No peer key establishment, BLE/Wi-Fi transport integration, multi-hop relay service or end-to-end delivery |
| Geohash/integrity/token utilities | Implemented and unit-tested in CI | Not connected to a nationwide data importer, trusted manifest or recovery pipeline |
| Local history/bookmarks/check-in | SQLite-backed history/bookmarks/check-in; check-in creates encrypted durable outbox item; best-effort <=5% battery snapshot persists in app_state | No remote family sync/acknowledgement; low-battery snapshot is not guaranteed after process death |
| Other SQLite tables | SOS events plus schema foundations for location, directory, map metadata and map tiles | Directory/map/location data importers, repositories, and complete UI are not implemented |
| Nationwide directory/maps | UI-connected user-selected JSON import, transactional directory search with source/license/version metadata, and SHA-256-verified private tile cache | No audited nationwide service-contact dataset, licensed tile package, map renderer, signed manifest, or download/rollback pipeline |
| PIN vault/duress mode | PIN-derived AES-GCM vault is integrated into contact UI; locked state blocks SOS preparation; five-failure temporary lockout and tests added | No recovery/backup lifecycle or duress mode; loss of PIN can permanently lose contacts; physical-device review remains |
| Medical ID QR | On-device QR generation with explicit user-entered fields and clear action | QR contents are not encrypted; user must control what is exposed; privacy/scan interoperability tests pending |
| Accessibility/theme | Persistent high-contrast toggle, animated flag ribbon/title with persistent reduce-motion control, and cell-broadcast settings guidance | Formal contrast/TalkBack/touch-target/reduced-motion validation pending; device acceptance still required |
| PAGASA/evacuation/cell-broadcast awareness | UI-connected JSON import/search for source-attributed advisories and evacuation-centre cache with expiry/stale labels; UI explains Android/carrier cell-broadcast ownership | No live PAGASA/LGU feed, signed data manifest, verified local coverage, or cell-broadcast interception; Android/carrier system handles cell broadcasts |
| SOS handoff foreground service | Persistent notification with explicit Stop status action; Robolectric channel test added | Android 13+ notification permission and real-device service lifecycle behavior remain unverified |
| Physical-device tests | Not run | Requires target devices, SIM/network, permissions and multi-device mesh testing |
| Release signing | Not ready | Unsigned build only; no signed release or go/no-go evidence |

A manifest permission, UI card, schema table, utility, or successful build is not proof that a complete feature works. Do not distribute this early build as a dependable emergency service.
