# PH-ResQ Full Cross-System Audit — 2026-11-13

**Audit target:** PR #1 head `927af2031bbe6a6b9846910872824859f07e34d5`  
**Repository:** `alinor2pundamdag/PH-ResQ`  
**Branch:** `docs/architecture-correction-2026-10-27`  
**Pull request:** [#1](https://github.com/alinor2pundamdag/PH-ResQ/pull/1) — open, unmerged  
**Audit type:** Requirements-to-code, architecture-to-integration, device/API, persistence/security, UI/UX, tests/CI, data governance and release-evidence review.  
**Important boundary:** This is a source/document/CI audit. No physical Android device, SIM/carrier, second peer device, TalkBack session, or nationwide licensed data package was available for this audit. Those checks are marked NOT RUN/BLOCKED, never PASS.

## 1. Executive result

**Decision: INCOMPLETE — NO-GO for production; controlled APK testing only after exact-head CI and artifact verification.**

The repository contains a native Kotlin/Jetpack Compose Android application and a substantial set of architecture documents. It is no longer a documentation-only archive. Several real partial implementations are connected to the UI: consent, SOS handoff, encrypted voice notes, PIN-protected contacts, location helpers, offline JSON import/search, advisories/evacuation cache, BLE discovery, capability/policy status, medical QR and visible SOS handoff notification.

However, **none of the 34 retained feature IDs is currently demonstrated as fully complete end-to-end**. Feature traceability marks 31 Partial and 3 Planned (F-013, F-018, F-019). Build success is not emergency-service, radio, data-coverage, accessibility or physical-device acceptance.

### Current-head CI mismatch — release blocker

- Current PR #1 head: `927af2031bbe6a6b9846910872824859f07e34d5`.
- Latest successful workflow inspected: [run 43411656696](https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/43411656696), head SHA `0f36250beffb00dda7296bc2f92b20c39c0775da`.
- These SHAs differ. The current PR head has no workflow run returned by the commit-specific workflow query.
- Therefore, the successful run and artifact `ph-resq-apk-builds` ID `14030410499` do **not** validate the current PR head. Do not install or describe that artifact as the current candidate.
- The CI workflow itself runs `lintDebug`, `testDebugUnitTest`, `assembleDebug`, and `assembleRelease`, verifies checkout SHA, and creates `BUILD_METADATA.txt` and `SHA256SUMS`. It must rerun after the next PR-head commit.

## 2. Requirements coverage — all 34 retained features

The repository's `FEATURE_TRACEABILITY.md` retains F-001 through F-034. Current statuses:

| Feature IDs | Current finding |
|---|---|
| F-001–F-003 | Offline local state/import/search exists; no complete airplane-mode acceptance, nationwide barangay dataset, licensed offline map package or renderer |
| F-004–F-006 | SOS hold and encrypted voice-note primitives/UI exist; timing/haptics/device tests and actual SMS/voice attachment delivery are not confirmed |
| F-007–F-011 | Local persistence, SOS event UUIDs, check-in queue and one-shot location exist; six-domain recovery, remote sync and lifecycle-aware GPS service remain incomplete |
| F-012–F-016 | BLE discovery, packet codec, queue, HMAC and geospatial query primitives exist; no authenticated peer key establishment, BLE payload exchange, Wi-Fi Direct, multi-hop router or measured nationwide spatial performance |
| F-017–F-020 | PIN vault and Medical ID QR exist; no recovery/backup lifecycle, duress vault or stealth-audio policy; QR is readable and not encrypted |
| F-021–F-024 | High contrast, reduce motion, volume cap, foreground status and hotline fallback exist partially; formal accessibility/device testing and verified nationwide/local hotline directory remain |
| F-025–F-027 | Local advisory/evacuation import/cache and cell-broadcast guidance exist; no live PAGASA/LGU ingestion or cell-broadcast interception |
| F-028–F-034 | Consent/privacy, local history/bookmarks, battery snapshot and state primitives exist; legal/privacy audit, full chaos suite, nationwide data update pipeline and true transport-backed delivery remain |

**No feature should be promoted to COMPLETE solely because a class, button, database table, protocol utility or Robolectric test exists.** Each needs the feature's full acceptance evidence.

## 3. P0 — emergency correctness and data safety

### P0-01 — Current candidate has no matching CI/APK evidence
**Evidence:** PR #1 head SHA is `927af203...`; successful run #43411656696 is for `0f36250...`.  
**Impact:** Current source may differ from the tested binary.  
**Required action:** Trigger a new PR CI run; verify all four Gradle tasks, exact head SHA, metadata SHA, APK filenames and checksums. Do not use the older artifact.

### P0-02 — SOS handoff is not SMS delivery, and crash reconciliation is incomplete
**Evidence:** `MainActivity.prepareSos()` persists QUEUED then SENDING, starts the foreground status service where notifications are allowed, and opens an `ACTION_SENDTO smsto:` external messaging composer. HANDED_OFF is recorded after `startActivity` returns. The user still has to review and tap Send.  
**Impact:** No carrier-send receipt, recipient delivery receipt, recipient acknowledgement, or emergency-service response is available. A process kill between SENDING and HANDED_OFF can leave an ambiguous event; there is no event-scoped recovery worker/reconciliation screen that safely explains it without duplicate sending.  
**Required action:** Keep wording at “opened messaging app / delivery unconfirmed”; add startup reconciliation for QUEUED/SENDING/HANDED_OFF/FAILED, preserve event UUID, never auto-resend an ambiguous handoff, and add no-SIM/no-app/permission/process-death device tests. Do not add direct SMS sending without a separately approved consent/legal/security design.

### P0-03 — Sensitive-data field coverage and key-loss behavior are not closed
**Evidence:** `EncryptedFieldCipher` uses Android Keystore AES-GCM for selected values; SOS draft/recipient/location snapshots are encrypted; PIN vault uses PBKDF2-derived AES-GCM. The SQLite schema also contains plaintext `last_location`, `search_history`, `bookmarks`, `family_checkins`, and generic `app_state` values. Current visible location text is encrypted in preferences, but schema and all call paths still need a field-by-field classification and proof of actual use.  
**Impact:** Sensitive search terms, saved labels/check-in status, location metadata or future writes could be exposed in local database/backup/forensic access. Keystore invalidation, reinstall, rotation, backup/restore and PIN recovery remain unresolved.  
**Required action:** Complete a field inventory and data-flow audit; encrypt or deliberately minimize sensitive fields; define retention/deletion; test key invalidation/reinstall; fail closed without plaintext fallback; define whether loss is intentionally unrecoverable or user-authorized encrypted export/restore is supported.

### P0-04 — Operational mesh is not implemented
**Evidence:** `BleDiscoveryManager` scans for 15 seconds and returns device names/RSSI only. `MeshEnvelopeCodec` and `DurableMeshQueue` provide codec/queue/HMAC/replay/ACK primitives. `FeaturePolicyCoordinator` explicitly reports Wi-Fi Direct as BLOCKED / not implemented. No authenticated peer identity/key agreement, BLE GATT payload path, Wi-Fi Direct socket transfer or multi-hop router exists in the source tree.  
**Impact:** No emergency packet can be claimed to travel between PH-ResQ devices. An HMAC utility is not a trust/key-establishment protocol.  
**Required action:** Design and implement peer identity/key agreement first; then BLE payload transport, Wi-Fi Direct transport, router/TTL/forwarding and peer-bound ACK; add packet fuzz/replay tests and two-/three-device acceptance. Until then label mesh as NOT IMPLEMENTED, not available.

## 4. P1 — major integration, platform and product gaps

### P1-01 — Device capability profile is incomplete
**Evidence:** `DeviceCapabilityProfile` detects API/model/ABI/storage/battery saver, telephony feature, BLE permissions/adapter, Wi-Fi Direct feature declaration, location, microphone, notifications, Keystore provider and background restriction.  
**Missing:** Actual SIM state and messaging-app availability are not capability facts; Wi-Fi Direct manager/service/permissions are not checked because transport is absent; StrongBox/key algorithm/invalidation details, memory pressure, display size/density/font scale, notification-channel state and broader radio/network events are not represented.  
**Required action:** Complete the runtime profile contract and event-specific refresh; keep capability facts separate from user preferences and feature policy; add API 26/30/31/33/34/35 and OEM/hardware tests.

### P1-02 — UI is highly concentrated in one Compose activity
**Evidence:** `MainActivity.kt` is approximately 64 KB and contains SOS orchestration, permissions, DB/repository construction, audio lifecycle, BLE scan, QR, directory/advisory UI, settings and device-policy display inside one activity/composable. The main screen is one long vertically scrolling surface.  
**Impact:** High coupling, harder state restoration, testing, accessibility and regression isolation; UI can become a single failure surface during an emergency.  
**Required action:** Refactor incrementally (without feature removal) into screen components, ViewModels/use-cases and repository interfaces. Separate Emergency/SOS, Contacts/Vault, Offline Directory/Alerts, Check-in, Medical ID, Settings/Device Status. Preserve a single-tap path to SOS and avoid decorative work delaying it.

### P1-03 — Accessibility and adaptive UI are not device-verified
**Evidence:** Consent has independently scrollable text and fixed actions; high-contrast and reduce-motion settings exist; content width is constrained for larger windows.  
**Missing:** TalkBack traversal/actions, large font/font scaling, landscape, display cutouts, touch target and contrast audits, tablet/foldable layouts, keyboard/switch access, real haptics and animation/background lifecycle tests. Default non-high-contrast theme currently uses `lightColorScheme()`, which should be checked against the intended emergency-first visual specification and user preference.  
**Required action:** Run T-023 and screen-by-screen acceptance on compact phone, large-font, landscape and tablet; document results and fix issues.

### P1-04 — Offline data is a user-import cache, not a verified nationwide service
**Evidence:** `OfflineDatasetImporter` accepts user-selected JSON; source/license/version fields are supplied by the file and are not signed. `DATA_SOURCES_AND_RISKS.md` states no PSGC workbook was bundled/coverage-audited, no full LGU emergency-contact dataset, no live PAGASA/LGU feed, and no offline map tiles/renderer package.  
**Impact:** Imported data can be forged, incomplete, stale or mislicensed. A record count is not geographic completeness.  
**Required action:** Acquire source data and redistribution rights; retain source identifiers/hierarchy; validate all 42,010 barangays against the relevant current PSA PSGC release; separately audit actual emergency contacts, evacuation centres and hotlines; implement signed manifests, publisher key rotation, package integrity and rollback. For maps, license both data and rendered tiles and implement a renderer before advertising offline maps.

### P1-05 — Family check-in has no remote transport
**Evidence:** SQLite writes a local check-in and encrypted PENDING outbox row with `QUEUED_LOCAL_ONLY`; there is no connected authenticated endpoint or ACK.  
**Required action:** Keep UI as LOCAL/PENDING; define consent, server trust/authentication, encryption, idempotency, retry, deletion and acknowledgement before remote sync. Do not display SYNCED until a real receipt is stored.

### P1-06 — Location is one-shot, not adaptive/background GPS
**Evidence:** `FreshLocationProvider` requests a bounded fresh fix for up to 15 seconds and cancels listeners; `AdaptiveLocationPolicy` exists as a utility. There is no lifecycle-aware scheduled/foreground location service.  
**Required action:** Decide whether background location is truly needed; if yes, implement opt-in policy-compliant foreground tracking with visible notification and explicit stop. Otherwise keep one-shot/manual fallback and correct the feature wording. Test stale/future fix, no provider, revoked permission, battery saver and process death.

### P1-07 — Foreground notification service is only a handoff status aid
**Evidence:** `SosStatusForegroundService` displays a generic notification with a Stop status action. Start is gated on notification permission/channel availability; if unavailable, handoff proceeds with in-app status.  
**Impact:** It is not a durable SOS sender, location tracker, mesh relay or delivery monitor. Physical Android 13–15 lifecycle/notification behavior is not verified.  
**Required action:** Keep its narrow stated purpose; test denied notifications, background start restrictions, stop action, activity return, process death and API/OEM behavior.

## 5. P2 — maintainability, integrity and governance

- **P2-01 Database architecture:** custom `SQLiteOpenHelper` schema v8 exists with sequential migrations and selected Robolectric fixtures. Repository boundaries are incomplete; UI constructs and uses database/repository objects directly. No Room dependency is present. Introduce repository/DAO seams and migration fixtures for every supported historical schema without destructive resets.
- **P2-02 Migration/recovery:** add upgrade fixtures from every supported version, corrupted DB, interrupted migration, low-storage, backup restore, app reinstall and key-loss tests. Verify that undecryptable ciphertext is reported as unrecoverable, not treated as empty.
- **P2-03 Integrity:** SHA-256 verifier and tile cache hashes do not authenticate the publisher if the expected digest is stored beside mutable content. Add signed manifest and pinned publisher trust/rotation before calling data verified.
- **P2-04 Spatial index:** current index is a SQLite B-tree on latitude/longitude plus bounding-box/Haversine filtering, not an R-tree. Benchmark on official nationwide data and low-end devices before making latency claims.
- **P2-05 Reproducible build:** no Gradle wrapper is committed. CI installs Gradle 8.9 externally; local build reproducibility and dependency verification are not closed.
- **P2-06 Privacy/legal:** consent and privacy notice exist, but retention, export/deletion, privacy impact, RA 10173/RA 10639 operational mapping, data-controller/contact process and legal review are not evidenced.
- **P2-07 Diagnostics/observability:** no comprehensive redacted diagnostic export, event correlation or failure analytics policy was found; any diagnostics must exclude phone numbers, exact location, PIN, medical data, tokens and voice content.
- **P2-08 Documentation drift:** some audit/status sections describe earlier schema and source SHAs. Update all evidence pointers only after current-head validation; keep architecture collection as the index and detailed domain docs authoritative.

## 6. Cross-system dependency and interaction map

| User/system action | UI | Domain/orchestration | Persistence | Android/device/external dependency | Current state |
|---|---|---|---|---|---|
| First launch/consent | ConsentScreen | ConsentStore gate | SharedPreferences version/timestamp | App lifecycle | Partial; process/withdrawal/accessibility device acceptance pending |
| SOS hold | Compose hold button | SosState + MainActivity prepare flow | Encrypted event snapshot, event UUID, transition rows | Location permission, vibrator, notification service, messaging app, SIM/carrier, recipient | Handoff only; no delivery receipt or restart reconciliation |
| Contacts/vault | Contacts UI | PinProtectedVault / LocalVault / EncryptedFieldCipher | SharedPreferences + Keystore AES-GCM | Android Keystore/device lock state | Partial; recovery/key-loss/reinstall unresolved |
| Fresh location | UI action | FreshLocationProvider + freshness policy | Encrypted location text preference; metadata in app_state | GPS/network provider and runtime permission | One-shot only; no background/adaptive service |
| BLE discovery | UI button | BleDiscoveryManager | In-memory peer list | BLE hardware/adapter/runtime permission | Discovery only; no packet exchange |
| Wi-Fi Direct | Device status card | FeaturePolicyCoordinator | None | P2P hardware/service/permissions | Explicitly blocked/not implemented |
| Voice note | UI recorder/player/share | EncryptedVoiceNoteRecorder + safe gain | Encrypted app-private file; temporary decrypted cache/share file | Microphone, MediaRecorder, MediaPlayer, FileProvider, external share app | Local record/play/delete/share; delivery unconfirmed; temp-file cleanup/device tests needed |
| Offline directory/alerts | Import/search cards | OfflineDatasetImporter + repositories | SQLite cache + metadata | Android document picker and user-provided source file | Import/search only; not publisher-authenticated or nationwide |
| Medical ID QR | Form/QR | QR encoder | Currently session UI state | Screen/camera of recipient | Local display only; readable by any scanner; no encrypted QR |
| Family check-in | Check-in button | Local DB outbox | family_checkins + pending_actions | No remote transport | Local-only |
| Low-battery snapshot | Home status | Battery broadcast receiver | app_state | App process must be alive/receiver active | Best effort only |
| Emergency hotlines | Directory list | Static NationalHotlineDirectory | Search/bookmarks local DB | Dialer/SMS app, SIM/network, external agency | Dialer/SMS handoff only; each number/source must be revalidated |
| Cell broadcast | Help text | None | None | Android/carrier system alert settings | Guidance only; app does not intercept |

## 7. Verification matrix summary

### Automated/CI
- Current source has Robolectric/JUnit coverage for selected consent, SOS model/DB transitions, importer, vault, mesh codec/queue, advisories, location freshness, capability/policy, volume and persistence utilities.
- Test matrix defines T-001 through T-057. A test row is not evidence it ran.
- Current PR head `927af203...` has no matching workflow run in the commit-specific query. Exact-head CI is therefore NOT VERIFIED.

### Device/integration NOT RUN
- No physical Android device evidence supplied for API 26/30/31/33/34/35 or OEM-specific behavior.
- No SIM/carrier/messaging-app test, no location-provider test, no denied/revoked permission device test.
- No two-/three-device BLE/Wi-Fi mesh test; Wi-Fi Direct transport does not exist.
- No TalkBack/large font/landscape/tablet/foldable accessibility run.
- No offline nationwide data/map package, no source/license/coverage acceptance.

### Release
- PR #1 remains open and unmerged.
- CI release variant is unsigned. No production signing or release authorization evidence.
- Production release decision remains NO-GO.

## 8. Remediation order — do not skip gates

1. **Exact-head CI gate:** create a new CI run for current head and retain its APK + metadata + checksums only if all tasks pass.
2. **P0 SOS/recovery:** add safe startup reconciliation and tests for ambiguous SENDING state; maintain no-auto-resend rule; run real messaging-app/SIM device matrix.
3. **P0 security/data:** complete sensitive-field inventory, encryption/retention/key lifecycle and recovery policy; test key invalidation and reinstall.
4. **P0 mesh architecture:** define peer authentication/key agreement; implement BLE payload transport; Wi-Fi Direct transport; routing/ACK and multi-device verification. Until completed, mark F-012/F-014 partial and F-013 planned.
5. **P1 data:** acquire and audit official PSGC and emergency datasets; signed manifest/update/rollback; licensed map renderer/package.
6. **P1 family/location:** define and implement authenticated remote check-in only if authorized; decide and implement or explicitly scope out background GPS.
7. **P1 UI/UX:** refactor monolithic UI incrementally; complete accessibility/responsive tests without reducing SOS reachability.
8. **P2 platform/reproducibility:** add pinned Gradle wrapper, dependency verification, migration/DAO architecture, performance benchmarks and legal/privacy review.
9. **Final acceptance:** execute all applicable T-001–T-057 on the target physical devices, record PASS/FAIL/BLOCKED/NOT RUN, fix defects and re-run exact current-head CI.
10. **Release gate:** signed artifact only after owner authorization; do not merge or publish from this audit.

## 9. Audit evidence sources

All source references below are at audit target `927af2031bbe6a6b9846910872824859f07e34d5` unless otherwise stated:
- `ARCHITECTURE_COLLECTION.md`, `MASTER_PLAN.md`, `SYSTEM_ARCHITECTURE.md`
- `CRITICAL_ARCHITECTURE_AUDIT.md`, `SETTINGS_DEVICE_INTEGRATION_CONTRACTS.md`, `ANDROID_COMPATIBILITY_AND_ADAPTIVE_RUNTIME.md`
- `FEATURE_TRACEABILITY.md`, `IMPLEMENTATION_STATUS.md`, `IMPLEMENTATION_ROADMAP.md`
- `SOS_STATE_MACHINE.md`, `DATABASE_SPEC.md`, `PERSISTENCE_RECOVERY_ARCHITECTURE.md`
- `SECURITY_MODEL.md`, `MESH_PROTOCOL.md`, `DATA_SOURCES_AND_RISKS.md`
- `TEST_ACCEPTANCE_MATRIX.md`, `SINGLE_PASS_DEVICE_ACCEPTANCE.md`, `RELEASE_CANDIDATE_CHECKLIST.md`, `.github/workflows/android-ci.yml`
- Source: `MainActivity.kt`, `ResQDatabase.kt`, `DeviceCapabilityProfile.kt`, `FeaturePolicyCoordinator.kt`, `BleDiscoveryManager.kt`, `DurableMeshQueue.kt`, `MeshEnvelopeCodec.kt`, `FreshLocationProvider.kt`, `EncryptedFieldCipher.kt`, `PinProtectedVault.kt`, `OfflineDatasetImporter.kt`, `HazardDataRepository.kt`, `SosStatusForegroundService.kt`, `ConsentScreen.kt`.

## 10. Status and authorization

This report is a verified source/document/CI review, not a physical-device certification. No PR merge, production signing, deployment, or release was performed. Keep PR #1 open and unmerged.
