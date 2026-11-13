# PH-ResQ Implementation Status

Snapshot: 2026-11-13 engineering remediation. PR #1 remains open and unmerged. The current test candidate is always the current PR #1 head; accept an APK only when the latest successful Actions run has an identical `head_sha`, `BUILD_METADATA.txt` source/checkout SHAs match it, and `SHA256SUMS` verifies both APKs. Historical artifacts are not valid for later commits. This is a controlled test build, not a production release; the release variant is unsigned until a separate signing and release process is approved.

## Current consent UI correction (implemented; exact-head CI and device acceptance tracked separately)

The original consent layout placed the entire notice and both actions in one vertically unscrollable centered Column. On a screen shorter than the notice, the action buttons were laid out below the viewport. The correction separates the weighted vertically scrollable notice from a fixed action card, applies safe-drawing insets, bumps the notice version to `2026-11-02-v2`, and adds a home-screen control to review the notice or withdraw consent. Declining or withdrawing clears the accepted policy record before the activity exits. Robolectric tests cover initial state, version mismatch, persistence, and revocation. ConsentStore and compact-screen Compose regression tests are included in CI; large-font, landscape, TalkBack, and physical-device checks are not yet passed.

## Architecture audit update (2026-11-13)

Audit target at initial architecture review: PR #1 head `0ef2401d0cdfb71ed34c6dbe4e3b3f746aee6f5d`. Later exact-SHA build artifacts are recorded in the snapshot and automated-evidence sections. Added four normative documents: `CRITICAL_ARCHITECTURE_AUDIT.md`, `ANDROID_COMPATIBILITY_AND_ADAPTIVE_RUNTIME.md`, `SETTINGS_DEVICE_INTEGRATION_CONTRACTS.md`, and `PERSISTENCE_RECOVERY_ARCHITECTURE.md`. `DeviceCapabilityProfile` is connected to a lifecycle-refreshed Compose device-status card; home content is centered with a tablet/large-window max width. Typed `SettingsRepository` now persists high-contrast, reduce-motion and safe voice playback gain; Robolectric tests cover preference persistence and volume clamping. API 26/30/31 capability-boundary tests were added. FeaturePolicyCoordinator now derives and displays explicit READY/FALLBACK/UNAVAILABLE behavior for location, BLE discovery, voice notes, notifications, protected data, messaging handoff and Wi-Fi Direct; policy unit tests were added. Full adaptive layout variants, accessibility instrumentation and physical-device tests remain pending. The audit identifies P0 gaps in device support policy, operational mesh, SOS handoff semantics, persistence/recovery, and key lifecycle. No feature IDs were removed. No source runtime behavior was changed by this architecture-document pass.

## Automated evidence

### Exact-head CI evidence — 2026-11-13
- PR #1 head: `0f36250beffb00dda7296bc2f92b20c39c0775da`.
- Successful workflow: [run 43411656696](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/43411656696), with identical `head_sha`.
- Verified job steps: `lintDebug`, `testDebugUnitTest`, `assembleDebug`, and `assembleRelease` all completed; Gradle reported `BUILD SUCCESSFUL`.
- CI wrote `BUILD_METADATA.txt` with source and checkout commit fields, generated `SHA256SUMS` for the APKs, and uploaded both APKs plus evidence.
- Artifact: `ph-resq-apk-builds`, ID `14030410499`, 15,985,130 bytes; archive SHA-256 `12fc2b32f1debc5f69256b2efa05a70248dd28bb136925004e216510729f6bb9`; expires 2026-11-27.
- Download through the [workflow artifact page](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/43411656696/artifacts/14030410499). Verify the included metadata and APK checksums after download. Release APK is unsigned.
- This proves build/test pipeline success for this exact commit only. It does not prove physical-device behavior, operational mesh, nationwide data coverage, emergency delivery, or production readiness.

- **Validation rule:** check the current PR head and latest successful workflow run together; use no artifact whose metadata source commit differs from the desired SHA.
- **Current-head validation:** use the latest successful [PR #1 Actions run](https://github.com/alinor2pundamdag/PH-ResQ/actions) and compare its `head_sha` with the open PR head. Do not use historical parent artifacts for a changed source SHA.
- **Artifact contents:** canonical debug/release APK filenames, `BUILD_METADATA.txt`, and `SHA256SUMS`; the release APK is unsigned. Verify the archive and both APK checksums before installation.
- **Workflow tasks:** `lintDebug`, `testDebugUnitTest`, `assembleDebug`, and `assembleRelease`; checkout SHA verification; APK and build-evidence upload.
- **Unit-test coverage:** contact parsing and SOS message/state behavior (including retry-pending transitions); geohash encoding; SHA-256 digest verification; authenticated mesh-envelope encode/decode, tamper/expiry/hop checks and bounded deduplication; rolling-token rotation; adaptive-location interval policy; Robolectric tests for search-history deduplication/clearing, bookmark save/update/removal, family check-in persistence, and database reopen persistence.
- **Not evidenced by CI:** physical-device behavior, SMS recipient handling, actual GPS fixes, BLE interoperability/battery use, voice recording/playback, Android Keystore migration on a real device, nationwide data coverage, accessibility, or end-to-end emergency delivery.

## Feature status

| Area | Status | Evidence / limitation |
|---|---|---|
| Android project and CI | Workflow runs lint, unit tests, debug/release assembly, exact-SHA verification and APK/evidence upload | A candidate is validated only by a successful run matching its exact source SHA; no Gradle wrapper is committed; release APK is unsigned |
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
| Geohash/integrity/token/spatial utilities | Geohash, SHA-256 and rolling-token utilities; indexed latitude/longitude nearby search with Haversine filtering and unit tests | Nationwide data, trusted manifest, recovery pipeline and realistic performance benchmark remain incomplete |
| Local history/bookmarks/check-in | SQLite-backed history/bookmarks/check-in; check-in creates encrypted durable outbox item; best-effort <=5% battery snapshot persists in app_state | No remote family sync/acknowledgement; low-battery snapshot is not guaranteed after process death |
| Other SQLite tables | SOS events, location metadata, directory/map metadata, advisories, evacuation centers and mesh queue; local directory/advisory/evacuation UI and JSON import are present | No live feed, nationwide coverage, signed data manifest, map renderer or automatic rollback/update pipeline |
| Nationwide directory/maps | UI-connected user-selected JSON import, transactional directory search with source/license/version metadata, and SHA-256-verified private tile cache | No audited nationwide service-contact dataset, licensed tile package, map renderer, signed manifest, or download/rollback pipeline |
| PIN vault/duress mode | PIN-derived AES-GCM vault is integrated into contact UI; locked state blocks SOS preparation; five-failure temporary lockout and tests added | No recovery/backup lifecycle or duress mode; loss of PIN can permanently lose contacts; physical-device review remains |
| Medical ID QR | On-device QR generation with explicit user-entered fields and clear action | QR contents are not encrypted; user must control what is exposed; privacy/scan interoperability tests pending |
| Accessibility/theme | Persistent high-contrast toggle, animated flag ribbon/title with persistent reduce-motion control, and cell-broadcast settings guidance | Formal contrast/TalkBack/touch-target/reduced-motion validation pending; device acceptance still required |
| PAGASA/evacuation/cell-broadcast awareness | UI-connected JSON import/search for source-attributed advisories and evacuation-centre cache with expiry/stale labels; UI explains Android/carrier cell-broadcast ownership | No live PAGASA/LGU feed, signed data manifest, verified local coverage, or cell-broadcast interception; Android/carrier system handles cell broadcasts |
| SOS handoff foreground service | Persistent notification with explicit Stop status action; Robolectric channel test added | Android 13+ notification permission and real-device service lifecycle behavior remain unverified |
| Safe voice playback gain | User-configurable 10–70% player gain; unit tests cover bounds and non-finite input | Acoustic output varies by device/headphones and Android system volume; physical-device check remains |
| Physical-device tests | Not run | Requires target devices, SIM/network, permissions and multi-device mesh testing |
| Release signing | Not ready | Unsigned build only; no signed release or go/no-go evidence |

A manifest permission, UI card, schema table, utility, or successful build is not proof that a complete feature works. Do not distribute this early build as a dependable emergency service.


## P0 SOS durability correction — event-scoped transition implementation

- `prepareSos()` now validates recipients and builds the exact draft before queueing. It persists `QUEUED` with the encrypted draft, recipient snapshot and location before attempting any external activity.
- The event is moved to `SENDING` in a separate persisted transition before the messaging-app launch. `HANDED_OFF` is recorded only after Android successfully launches the target activity; this still does not mean SMS was sent or delivered.
- `sos_events` now stores a stable UUID `event_id` across transitions for one SOS event. Schema version 8 adds the field and assigns independent IDs to legacy transition rows without deleting them.
- The database enforces valid per-event transitions. Generic code cannot mark an event `DELIVERED` or `ACKNOWLEDGED` because no trusted delivery-receipt/recipient-ack transport exists.
- Added Robolectric tests for encrypted SOS snapshot readback, stable event IDs, interleaved attempts, missing event-ID rejection, unverified delivery/acknowledgement rejection, v7 migration and empty-v6 schema repair.
- Each non-QUEUED transition now requires the originating event UUID; the database no longer guesses the target from the globally latest row.


## Compile and location correctness follow-up

A previous compile attempt found a missing explicit return in the block-bodied `recordSosEvent` database helper; it now returns the committed event UUID only after the transaction succeeds. Location timestamp policy rejects stale and implausibly future-dated fixes, and the one-shot provider reports failure if no enabled provider can be registered. A visible progress indicator and cancellation guidance were added to the two-second SOS hold control, with a short-tap regression test.


## Current remaining engineering boundary

The code includes event-scoped SOS transitions, schema v8 migration repair, stale/future GPS timestamp rejection, visible SOS hold progress, a short-tap UI test, and a non-deprecated notification action. These remain partial feature implementations. Wi-Fi Direct, authenticated BLE payload transport, peer-key establishment, multi-hop relay, remote family sync, signed dataset update/rollback, licensed offline map renderer/package, nationwide authoritative datasets, duress vault, full key recovery, and physical-device/accessibility/legal acceptance remain open. Do not claim emergency-service readiness.


## SOS interrupted-handoff recovery — source remediation added

Commit `bc32f658ae366e3a4a7300c290aa26007f4b0b24` adds a terminal `HANDOFF_UNCONFIRMED` state for the ambiguous case where the app process dies after persisting `SENDING` but before it can persist `HANDED_OFF`. Startup reconciliation appends this state transactionally for the latest interrupted event, preserves the original encrypted draft/recipient/location snapshot, and never reopens or retries the SMS composer. Robolectric tests cover encrypted snapshot preservation, idempotent reconciliation, and no change to an already `HANDED_OFF` event. This source change is awaiting exact-head CI; real process-death, messaging-app and SIM acceptance remain NOT RUN.
