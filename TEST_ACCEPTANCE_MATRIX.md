# PH-ResQ Test & Acceptance Matrix

**CI evidence rule:** accept a run only when its `head_sha` exactly matches the intended PR #1 head and its artifact includes `BUILD_METADATA.txt` and `SHA256SUMS`. Do not use an older parent artifact for a changed PR head. CI success is not feature acceptance. Physical-device, accessibility, and data/security tests remain open.
**Rule:** Record repository commit SHA, build variant, device/API, test result, logs/screenshots, and tester for each executed case.

| Test ID | Scenario | Expected result | Evidence required | Gate |
|---|---|---|---|---|
| T-001 | First launch, no consent stored | Consent policy appears before protected features | UI test + persisted state | P0 |
| T-001A | Compact-screen consent layout | Accept/Decline remain visible while the notice body scrolls independently | Robolectric Compose UI test at 320×480 dp | P0 |
| T-001B | Consent action isolation | Only the tapped Accept or Decline callback fires | Robolectric Compose UI tests | P0 |
| T-002 | User declines consent and relaunches | Protected navigation remains blocked; app does not claim OS force-stop | UI/lifecycle test | P0 |
| T-003 | User accepts consent then process is killed | Versioned consent remains stored; valid navigation resumes | Instrumented lifecycle test | P0 |
| T-004 | Hold SOS for less than 2 seconds | No dispatch; clear countdown/cancel feedback | UI timing test | P0 |
| T-005 | Hold SOS for required 2 seconds | Exactly one durable SOS event is queued | UI + database test | P0 |
| T-006 | SMS permission denied or no SIM | Clear failure/handoff option; event remains recoverable | Device test | P0 |
| T-007 | SMS accepted by platform but no delivery receipt | Status does not claim recipient delivery | Dispatcher/state-machine test | P0 |
| T-008 | Process death during SOS send | State resumes without silently losing event or creating duplicate send | Fault-injection test | P0 |
| T-009 | GPS unavailable/stale | Last fix includes timestamp/accuracy and is marked stale; manual fallback offered | Device test | P0 |
| T-010 | Storage nearly full or write interrupted | Atomic persistence/recovery behavior; visible error if unable to save | Fault-injection test | P0 |
| T-011 | Tamper with directory database | Trusted digest mismatch is detected; no unverified “self-heal” claim | Integrity test | P0 |
| T-012 | Vault encryption/decryption and wrong PIN | Correct plaintext round-trip; failure throttled; secrets absent from logs | Security test | P0 |
| T-013 | Reinstall/backup/restore vault | Behavior matches documented key recovery policy; no false promise of recovery | Device/security test | P0 |
| T-014 | BLE permission denied / Bluetooth disabled | Scan fails safely and reports unavailable; no fake peer success | Physical-device test | P1 |
| T-015 | BLE peer packet duplicated/replayed/malformed | Duplicate/replay rejected or safely deduplicated; malformed data rejected | Protocol test | P1 |
| T-016 | Wi-Fi Direct connection drops mid-transfer | Bounded retry or recoverable failure; partial payload not accepted as complete | Physical-device test | P1 |
| T-017 | Mesh packet forwarded through peers | TTL/hop limit honored; message ID deduplicated; acknowledgement state accurate | Multi-device test | P1 |
| T-018 | RSSI changes due to wall/device orientation | UI communicates approximate/uncertain proximity, not exact distance | Controlled test | P1 |
| T-019 | Nationwide directory search | Results validate against sourced test dataset; latency measured on low-end target | Benchmark + source audit | P1 |
| T-020 | Offline map coverage | User sees coverage/version and missing-region behavior; licensed tiles render offline | Device + license audit | P1 |
| T-021 | PAGASA cache is stale | Source and timestamps shown; stale content is not presented as current | Unit/integration test | P1 |
| T-022 | Medical ID QR is displayed | Only user-selected fields are included and disclosure is explained | Payload inspection + UI test | P1 |
| T-023 | TalkBack, high contrast, large text, reduced motion | Emergency actions remain operable and readable | Accessibility audit | P1 |
| T-024 | Battery drops below 5% | Best-effort snapshot occurs if process is alive; prior critical transitions already persisted | Device/fault test | P1 |
| T-025 | Reboot, permission revocation, app upgrade | State recovers safely; revoked capability is handled; migrations are validated | Device matrix | P1 |
| T-026 | Family check-in offline then reconnect | Queue is durable, deduplicated and reports pending/synced/failed honestly | Integration test | P1 |
| T-027 | Helpline fallback | Displayed number matches authoritative source, jurisdiction, and review date | Data audit | P0 |
| T-028 | Full test suite on release candidate | No unexplained critical/high failures; residual risks signed off | CI reports + device reports | Release |
| T-029 | Core flows in airplane mode after first-run setup | Cached directory, saved contacts, queued SOS draft, local history and imported data remain usable; unavailable live features are labeled clearly | Automated offline tests + physical-device test | P0 |
| T-030 | Encrypted voice-note lifecycle and SOS share chooser | Record/list/play/delete works within limits; share explicitly includes the selected audio and draft text; app never claims delivery from opening a chooser | Unit/Robolectric + physical-device share test | P0 |
| T-031 | Duress PIN and dummy-vault threat model | When implemented, duress behavior does not expose the real vault through ordinary UI paths; limitations and recovery behavior are explicit. Until implementation exists, status remains NOT IMPLEMENTED, not PASS | Security review + adversarial device test | P1 |
| T-032 | SOS foreground-service notification lifecycle | Notification starts/stops correctly, remains truthful, handles Android 13+ notification permission denial and service restrictions without blocking SOS preparation | Instrumented test + supported-device matrix | P0 |
| T-033 | Safe-volume and audio privacy policy | Playback gain stays within configured limits; mute policy does not imply secret recording; system volume is not changed without user action | Unit tests + device loudness/route tests | P1 |
| T-034 | Evacuation-centre import, freshness and stale-state display | Invalid records are rejected, source/time metadata is visible, stale/unknown status is not represented as confirmed-open | Parser tests + UI/device test + source audit | P1 |
| T-035 | Cell-broadcast guidance | UI points users to Android/carrier emergency-alert settings and never claims app-level interception of all broadcasts | UI assertion + copy review | P1 |
| T-036 | Search/bookmark/check-in persistence | History, bookmarks and check-ins survive database reopen; delete/clear and pending-sync states remain truthful | Robolectric database tests + UI/device test | P1 |
| T-037 | Rolling privacy-token rotation and clock drift | Tokens rotate in the configured window; invalid/expired tokens are rejected; clock drift behavior is bounded and documented | Unit/property tests + integration test | P1 |
| T-038 | BLE scan duty cycle and battery behavior | Discovery respects the 15-second scan window, stops after timeout/permission revocation, and reports measured battery impact without claiming unsupported transport | Instrumented lifecycle test + physical-device battery report | P1 |

| T-043 | Consent notice on compact viewport | At 320x480 dp, notice body scrolls independently and Accept/Decline remain visible and tappable | Compose UI test + screenshot | P0 |
| T-044 | Consent large font, landscape and TalkBack | Actions remain reachable, focus order is logical, labels are announced and no content is clipped | Instrumented accessibility/UI test | P0 |
| T-045 | Runtime capability profile on API boundaries | API 26, 30, 31, 32, 33, 34 and 35 produce correct permission/hardware states without crash | Robolectric/API emulator tests | P0 |
| T-046 | Permission revocation and radio state changes | BLE/location/notification capability refreshes to denied/disabled; active work stops safely; no duplicate work | Instrumented lifecycle test | P0 |
| T-047 | Unsupported optional hardware | No telephony, BLE, microphone, GPS or notification permission does not crash core local app; each affected feature shows fallback | Emulator + physical-device tests | P0 |
| T-048 | Settings/capability authority separation | User preference persists; missing hardware/permission constrains effective mode without overwriting preference | Repository/policy unit tests | P1 |
| T-049 | Migration from each supported database version | Sequential migration through schema v8 preserves records and encrypted fields; interrupted migration never resets DB | Migration fixtures + Robolectric | P0 |
| T-050 | Key invalidation, reinstall and backup restore | Unavailable device-bound key is surfaced; ciphertext is not replaced by empty/plaintext data | Security/device test | P0 |
| T-051 | Process death during SOS transition | Event and transition ledger recover; ambiguous external handoff is not automatically repeated | Fault-injection + repository test | P0 |
| T-052 | Storage exhaustion during import/cache write | Prior dataset remains active; pending SOS data is retained; clear recoverable error shown | Fault-injection/Robolectric | P0 |
| T-053 | Settings restore after process death/reboot | User preferences restore consistently; effective capability is re-evaluated from current OS state | Persistence + device test | P1 |
| T-054 | Device/API/OEM acceptance record | Every PASS is linked to exact APK SHA, model, API, ABI, permissions, battery mode and evidence | Signed acceptance report | Release |

## Feature-to-test dependency map

This map ensures every retained requirement has at least one planned acceptance route. A listed test ID is a required test dependency, not evidence that the test has passed.

| Feature ID | Required test IDs | Acceptance note |
|---|---|---|
| F-001 | T-029, T-047 | Offline behavior is tested with preloaded data; first-time downloads are not expected offline. |
| F-002 | T-019, T-034 | Nationwide coverage and source licensing require a separate data audit. |
| F-003 | T-020, T-011 | Tile integrity is not equivalent to a licensed renderer or nationwide coverage. |
| F-004 | T-004, T-005, T-057 | Hold duration, cancellation and haptics require device/UI evidence. |
| F-005 | T-006, T-007, T-055 | Durable intent precedes handoff; messaging handoff is not delivery confirmation. |
| F-006 | T-030 | User-selected app/recipient and delivery limitations must be explicit. |
| F-007 | T-010, T-013, T-025, T-036, T-039–T-042, T-049–T-053 | All six persistence areas need recovery/migration coverage. |
| F-008 | T-024 | Low-battery snapshot is best effort; critical state must already be persisted. |
| F-009 | T-011 | Digest must come from a trusted expected value or authenticated manifest. |
| F-010 | T-026, T-036, T-041–T-042 | Remote sync/ack remains open until an actual transport exists. |
| F-011 | T-009, T-024, T-025, T-056 | Freshness, permission, lifecycle and battery behavior are required. |
| F-012 | T-014, T-038 | Discovery alone is not packet transfer. |
| F-013 | T-016 | Requires actual Wi-Fi Direct transport on supported devices. |
| F-014 | T-015, T-017, T-038 | Envelope/queue tests do not substitute for radio interoperability. |
| F-015 | T-019 | Benchmark realistic dataset size and target hardware. |
| F-016 | T-037 | Key lifecycle and clock-drift integration remain acceptance dependencies. |
| F-017 | T-012, T-013 | Include lockout, key loss, reinstall and recovery behavior. |
| F-018 | T-031 | Remains NOT IMPLEMENTED until the feature and threat-model tests exist. |
| F-019 | T-033 | No secret recording or false “zero indication” claims. |
| F-020 | T-022 | QR fields are user-selected and not encrypted by default. |
| F-021 | T-023, T-043–T-044, T-047 | Include TalkBack, scaling, contrast, touch targets and reduced motion. |
| F-022 | T-032 | Foreground-service and notification permission behavior need device tests. |
| F-023 | T-033 | Device/system volume differs; app policy is not a hardware loudness guarantee. |
| F-024 | T-027 | Number, jurisdiction, source and review date must match. |
| F-025 | T-021 | Source and freshness metadata must be visible. |
| F-026 | T-034 | No live LGU feed is implied by local JSON import. |
| F-027 | T-035 | Cell Broadcast remains an OS/carrier/government capability. |
| F-028 | T-001, T-002, T-003, T-043, T-044, T-046–T-047 | Consent/privacy review is separate from unit-test success; scrolling and fixed actions require UI/device evidence. |
| F-029 | T-008, T-010, T-025, T-028, T-046, T-049–T-052 | Chaos testing must include interrupted operations and recovery. |
| F-030 | T-023 | Animation must respect reduce-motion and never block emergency controls. |
| F-031 | T-014, T-038, T-045–T-047, T-054 | Scan-window compliance and battery measurement are separate assertions. |
| F-032 | T-036, T-039–T-040 | Search history/bookmarks require persistence, clear/delete and UI tests. |
| F-033 | T-007, T-015, T-017, T-055 | Distinguish queued, handed-off, received, acknowledged and delivered states; receipt-less delivery writes are rejected. |
| F-034 | T-011, T-020, T-021, T-034 | Signed manifests/update/rollback and nationwide coverage remain open. |
| T-039 | Search history repeated with different casing, then cleared | One distinct query remains; clear action removes all saved queries | Robolectric database test | P2 |
| T-040 | Save, update and remove a hotline bookmark | One row per item ID; list order stable; removal persists | Robolectric database test | P2 |
| T-041 | Save family check-in then reopen database | Latest local check-in is restored; no remote-delivery claim | Robolectric database test | P1 |
| T-042 | Close and reopen local database | Search, bookmarks and check-in records persist without duplication | Robolectric database test | P1 |

## Release gates
- **P0:** consent, SOS integrity, privacy/security baseline, persistence/recovery, honest delivery state, verified emergency fallback.
- **P1:** mesh, nationwide data/maps, accessibility, cache freshness, and broader resilience.
- **Release candidate:** all P0 passed; all P1 passed or explicitly accepted as documented limitations; reproducible build and complete evidence bundle available.

A failed, skipped, unexecuted, or unreported test is not a pass. Older successful builds do not validate a newer revision.


| T-055 | SOS event intent is committed before external messaging handoff; event UUID remains stable and each transition is scoped to its own event across interleaved QUEUED → SENDING → HANDED_OFF/FAILED attempts; schema v7 rows and empty v6 legacy schemas migrate without deletion; unverified DELIVERED/ACKNOWLEDGED writes are rejected | Robolectric database regression tests + exact-SHA CI; instrumented process-death test remains required | P0 |
| T-056 | Fresh GPS timestamp is within the accepted age/future-skew window; stale/future fixes are rejected and provider registration failure is reported | LocationFreshnessPolicy unit tests + physical-device provider/permission test | P0 |
| T-057 | SOS hold progress is visible; release before two seconds or gesture cancellation never prepares SOS; full hold triggers exactly one confirmation | Compose UI regression test + physical-device timing/haptic test | P0 |


| T-058 | App process is killed after SENDING but before HANDED_OFF is committed | On next startup, same event UUID becomes HANDOFF_UNCONFIRMED, encrypted draft/recipients/location remain intact, reconciliation is idempotent, and no SMS composer is relaunched | Robolectric database regression + exact-head CI; instrumentation/process-death test remains required | P0 |
