# PH-ResQ Implementation Status

Snapshot: 2026-10-27. Status: expanded Android implementation pass; **not a release candidate**. Latest code includes bounded fresh-location requests, a best-effort low-battery snapshot, an encrypted durable local check-in outbox, and a PBKDF2/AES-GCM PIN-vault utility. These additions do not complete all 34 requirements. Voice-note sharing now opens the Android chooser with an audio attachment and SOS draft text; an offline directory importer and integrity-checked map-tile cache are present as data primitives, not as a complete nationwide product. A locally generated Medical ID QR with explicit field selection/privacy warning and a persistent high-contrast theme toggle are also present; both remain partial pending accessibility/privacy acceptance tests.

## Automated evidence

- **Previously validated candidate SHA:** `408e2aaaea09df70400c92038b405c5d5a47d337`; the expanded implementation commit must pass its own CI before being considered build-validated.
- **GitHub Actions:** passed — [run 40451629467](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/40451629467)
- **Executed tasks on baseline only:** `testDebugUnitTest`, `assembleDebug`, and `assembleRelease`; artifact upload succeeded. Expanded candidate validation is pending.
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
| Location | Last-known helper plus bounded one-shot fresh location request (15-second timeout) and saved fix metadata | No lifecycle-aware continuous polling/foreground service; freshness and accuracy policy still need acceptance tests |
| Voice note | Encrypted local recording capped at 10 seconds/2 MB; list/playback/delete and explicit system share chooser with temporary plaintext cleanup added | User chooses app/recipient; messaging app support and actual delivery are unverified; device/lifecycle tests remain |
| Hotline subset | Searchable offline national-agency contacts and dialer/SMS handoff | 911 portal source checked 2026-10-27; remaining numbers need independent current verification and local jurisdiction data |
| BLE | 15-second low-power advertisement discovery | No authenticated data exchange, SOS transfer or multi-hop transport |
| Mesh protocol utilities | HMAC-authenticated versioned envelope, payload/time/hop bounds and in-memory dedupe cache | No peer key establishment, BLE/Wi-Fi transport, durable replay cache, ACK/retry transport or end-to-end delivery |
| Geohash/integrity/token utilities | Implemented and unit-tested in CI | Not connected to a nationwide data importer, trusted manifest or recovery pipeline |
| Local history/bookmarks/check-in | SQLite-backed history/bookmarks/check-in; check-in creates encrypted durable outbox item; best-effort <=5% battery snapshot persists in app_state | No remote family sync/acknowledgement; low-battery snapshot is not guaranteed after process death |
| Other SQLite tables | SOS events plus schema foundations for location, directory, map metadata and map tiles | Directory/map/location data importers, repositories, and complete UI are not implemented |
| Nationwide directory/maps | Transactional directory importer/search with source/license/version metadata and SHA-256-verified private tile cache | No audited nationwide service-contact dataset, licensed tile package, map renderer, signed manifest, or download/rollback pipeline |
| PIN vault/duress mode | PIN-derived AES-GCM vault utility with five-failure temporary lockout and Robolectric tests added | Utility is not integrated into contact access/UI; no recovery/backup lifecycle or duress mode; not accepted as complete vault |
| Medical ID QR | On-device QR generation with explicit user-entered fields and clear action | QR contents are not encrypted; user must control what is exposed; privacy/scan interoperability tests pending |
| Accessibility/theme | Persistent high-contrast theme toggle, animated flag ribbon/title and cell-broadcast settings guidance | Formal contrast/TalkBack/touch-target/reduced-motion validation pending; animation currently has no reduced-motion preference gate |
| PAGASA/evacuation/cell-broadcast awareness | Local source-attributed advisory and evacuation-centre cache/search with expiry/stale labels added | No live PAGASA/LGU feed, signed data manifest, verified local coverage, or cell-broadcast interception; Android/carrier system handles cell broadcasts |
| Physical-device tests | Not run | Requires target devices, SIM/network, permissions and multi-device mesh testing |
| Release signing | Not ready | Unsigned build only; no signed release or go/no-go evidence |

A manifest permission, UI card, schema table, utility, or successful build is not proof that a complete feature works. Do not distribute this early build as a dependable emergency service.
