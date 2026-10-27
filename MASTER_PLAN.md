# PH-ResQ Vanguard 2026 — Master Architecture & Implementation Plan

**Document status:** Planning baseline; not proof of an implemented or release-ready Android application.  
**Target platform:** Android-first, offline-first emergency assistance for the Philippines.  
**Repository scope:** Planning documents, native Android source, automated tests, CI, and release evidence. Implementation status must be demonstrated by passing builds, device tests, and data-source evidence—not by this plan alone.

## 1. Purpose, scope, and non-negotiable requirements

PH-ResQ is intended to help people access emergency information and coordinate distress messages during disasters, including periods with weak or unavailable connectivity. The plan covers Luzon, Visayas, and Mindanao and targets residents, commuters, families, students, motorists, older adults, persons with disabilities, workers, and other people who may need emergency assistance.

All requirements in this document remain in scope unless an explicit architecture decision records a reason, impact, replacement, and user approval. Implementation must not silently remove or weaken a feature to make a build pass.

### Core design principles

- **Android-first:** Native Android APIs own device capabilities and lifecycle-sensitive work. Any shared UI layer must be justified by a documented architecture decision and must not substitute browser APIs for Android capabilities.
- **Offline-first:** Core emergency contacts, cached directories, saved locations, queued SOS events, and downloaded map data remain available without internet, subject to device storage and permissions.
- **Truthful capability claims:** No promise of zero bugs, exact radio-derived distance, universal delivery, absolute secrecy, or guaranteed nationwide data completeness. State measurable acceptance criteria and known limits.
- **Least privilege and informed consent:** Explain permissions in Filipino and English where appropriate, request them when needed, and degrade safely when denied.
- **Recoverable state:** Persist critical transitions, use bounded retries, deduplicate messages, and handle process death, reboot, storage pressure, and partial network availability.
- **Accessibility and low-end-device support:** High contrast, readable text, large touch targets, TalkBack labels, reduced-motion support, efficient background work, and battery-aware polling.
- **Traceability:** Every feature must map to requirements, implementation components, tests, evidence, and residual risks.

## 2. Visual identity and primary experience

- Product name: **PH-ResQ Vanguard 2026**; primary in-app title: **PH-ResQ**.
- The intended visual identity includes a clean emergency-first interface, neon/glass styling used without compromising legibility, and a Philippine-flag color-looping title (yellow, blue, red, white) with an optional flag-waving background.
- Animation must respect Android reduced-motion/accessibility settings, avoid obscuring emergency controls, and stop or reduce work when the app is backgrounded or battery constrained.
- Emergency actions must remain immediately discoverable. Decorative effects must never delay SOS initiation or block interaction.

## 3. Android architecture decision

**Initial implementation baseline recorded in ADR-001:** Kotlin + Jetpack Compose; minSdk 26; compileSdk/targetSdk 35; Java 17. The initial Gradle build and CI are now in the implementation branch, but the build result is pending. This is not evidence that all features are implemented.

The application is an Android app, not a browser app. Native Android APIs are required for capabilities such as location, SMS handoff, notifications, foreground services, Bluetooth Low Energy, Wi-Fi Direct, Android Keystore, lifecycle handling, and platform permission flows.

- Do not use `window`, `localStorage`, IndexedDB, or browser-only lifecycle assumptions as the implementation for native Android features.
- Build baseline: Kotlin + Jetpack Compose, minSdk 26, compileSdk/targetSdk 35, Java 17, AGP 8.7.3, Kotlin 2.0.21. This baseline remains provisional until CI passes on the latest commit. Native Android APIs own device capabilities and lifecycle work.
- Use a local database/storage layer appropriate for Android. Use a genuine spatial index only if the selected database supports it; otherwise document the chosen query strategy and benchmark it with realistic data.
- Use Android Keystore for encryption keys where feasible, with a documented compatibility fallback. StrongBox is optional and device-dependent.
- Background execution must follow current Android restrictions. Foreground services require the appropriate service type, permissions, and visible notification. Do not promise unrestricted continuous execution.

## 4. First launch, privacy policy, consent, and onboarding

### Required flow

1. Launch to a short initialization screen.
2. Run a bounded integrity check against a trusted expected digest or signed manifest, and load the six persistence areas described in Section 8.
3. On first launch, show the privacy notice, data-use explanation, applicable license information, and explicit **Accept** and **Decline** actions before protected features are available.
4. If **Accept** is selected, persist a versioned consent record and continue to onboarding/dashboard.
5. If **Decline** is selected, do not persist acceptance; explain that protected app features remain unavailable and allow the user to leave normally. A declined user must see the consent gate again on a later launch.

### Platform limitation and enforcement

An ordinary Android app cannot reliably force-stop itself or force the operating system to return to the home screen. Do not claim that it can. Enforce consent inside the app: on every launch and protected navigation, check the persisted consent state and show the consent screen until accepted. Do not abruptly close the app while the user is reading the policy.

Record the policy version, consent state, and timestamp without collecting unnecessary personal data. Re-consent is required when material changes to the policy or data use warrant it. Provide access to the current policy and a way to review consent status.

## 5. SOS workflow and emergency communications

### SOS interaction

- Require a deliberate **2-second press-and-hold** to initiate SOS, with haptic feedback where supported and a clear visual countdown.
- Provide an accessible alternative interaction that preserves protection against accidental activation.
- Support a voice-note attachment when available and explicitly selected/configured by the user.
- Provide a cancellation window where safe and appropriate, with a clear state transition and audit event.
- Support stealth/silent mode for situations where audible alarms may increase risk. Muting must not silently disable essential visual status or the user's ability to cancel.

### Durable SOS state machine

Persist state transitions so process death does not erase a pending emergency. At minimum, distinguish:

`IDLE → CONFIRMING → QUEUED → SENDING → SENT_TO_HANDOFF → DELIVERED (if confirmed) → ACKNOWLEDGED (if recipient/app confirms)`

Also represent `CANCELLED`, `RETRY_PENDING`, and `FAILED`. Do not label a message “delivered” merely because Android accepted an SMS request or a network API call returned success.

- Send a Tagalog SMS to up to **10 configured emergency contacts**, subject to SIM availability, Android permissions, carrier behavior, and the user's SMS configuration. The message should include the most recent location, timestamp, and accuracy estimate when available.
- If the app cannot send SMS directly under platform/permission rules, offer an explicit SMS-app handoff where supported; clearly show whether the message was sent by PH-ResQ or handed off to another app.
- Keep retry count and backoff bounded. Prevent duplicate sends using a durable event ID and idempotency/deduplication rules where the transport allows.
- Handle unavailable SIM, denied permissions, no signal, no GPS fix, stale location, app process death, reboot, and interrupted send operations.
- Make the current status and last error visible without exposing sensitive contact/location data unnecessarily.
- Never guarantee that an SMS or voice note will reach a recipient.

## 6. Location, proximity, and spatial search

### GPS and location fallback

- Use adaptive location polling based on active mode, movement, battery state, and Android background limits.
- Persist the last known location with capture timestamp, accuracy, and source. Mark stale or low-accuracy values; never present them as current without qualification.
- Explain why location is requested and support manual location selection if GPS is unavailable.

### BLE and Wi-Fi Direct mesh

- Preserve the requirement for nearby peer discovery and local device-to-device emergency data transfer using BLE and Wi-Fi Direct where supported.
- Specify a versioned packet protocol before implementation: message ID, sender pseudonym, creation time, TTL, hop limit, payload type/size, integrity/authentication tag, and protocol version.
- Define acknowledgement, retry/backoff, duplicate suppression, forwarding policy, TTL expiry, replay resistance, queue limits, rate limits, and malformed-packet rejection.
- Use cryptographically rotating pseudonyms/tokens where feasible. Do not broadcast precise GPS coordinates or sensitive medical data in BLE advertisements; exchange sensitive payloads only after an authenticated, consent-aware connection.
- Use Android permission checks and real platform scan/connection operations. Placeholder methods that always return success or only log are not implementations.
- Include the planned **15-second battery-saver scan window** as a configurable policy, not as a guarantee that Android will schedule every scan exactly on time. Measure battery use and document the behavior when no peers are found.

### RSSI and geospatial accuracy

- RSSI supports only rough proximity estimates; walls, device orientation, antenna differences, interference, and environment can make distance estimates unreliable. Do not display RSSI-derived distance as exact.
- Trilateration requires sufficient known reference positions and acceptable measurement geometry. If those conditions are not met, show an uncertainty range or “distance unavailable.”
- Use geohash or another documented grid for coarse spatial grouping. Store/query it with a real index where supported; benchmark actual query latency on target devices and realistic datasets.
- A local spatial query is not a substitute for an authoritative location fix.

## 7. Security, encrypted vault, privacy, and duress mode

### Encrypted vault

- Encrypt emergency contacts and sensitive Medical ID fields at rest using an authenticated encryption scheme such as AES-GCM.
- Define key creation, Android Keystore storage, nonce uniqueness, backup/restore behavior, rotation, revocation, reinstall behavior, and recovery before implementation. Never reuse an AES-GCM nonce with the same key.
- If a PIN-derived key is required, use a suitable password-based key derivation function with a per-vault salt and calibrated cost; enforce rate limits and secure failure handling. A PIN is not itself an encryption key.
- Handle devices without StrongBox by using the best supported Keystore-backed option and documenting the reduced guarantee.
- Keep secrets out of logs, crash reports, analytics, clipboard, and unencrypted backups.

### Duress PIN and dummy vault

- Preserve the optional secondary duress PIN, dummy vault, and discreet distress-signal requirements, but document the threat model and safety risks.
- Do not claim “zero indication” or absolute physical protection: timing, system UI, network activity, notifications, forensic access, and device behavior may reveal activity.
- Make the feature opt-in, explain the risks, and test behavior under network loss, permission denial, and failed dispatch. Never claim that a distress signal was sent unless the transport reports a supported status.
- Preserve stealth audio mute and ensure it does not override user-configured critical accessibility needs.

### Rolling tokens and integrity

- Rotate privacy-preserving identifiers using a defined cryptographic design and test for linkability, replay, clock drift, and collision behavior.
- SHA-256 detects a mismatch only when compared with a trusted expected digest or authenticated manifest. A checksum alone does not self-heal data. Recovery requires a verified backup or a trusted re-download source.
- Protect integrity metadata and avoid treating a hash stored beside mutable data as proof of authenticity.

## 8. Six required persistence layers and recovery

The implementation must preserve these six areas:

1. **Active mode** — current app/emergency mode and safe restoration rules.
2. **Emergency contact vault** — encrypted contacts and relevant consent/configuration.
3. **Last known GPS fallback** — coordinate, timestamp, accuracy, and provenance.
4. **Crisis SOS state** — durable queue, state transitions, attempts, and outcomes.
5. **Search and bookmark history** — local history/bookmarks with privacy controls.
6. **Offline map tiles** — downloaded MBTiles/vector or other licensed cache with coverage/version metadata.

Additional requirements:
- Trigger a best-effort low-battery snapshot when battery falls below **5%**, while recognizing Android may terminate the process before the callback runs. Critical state must be persisted continuously at transition boundaries, not only at low battery.
- Use atomic writes/transactions and schema migrations. Test interrupted writes, corrupt records, low storage, database upgrade, and recovery from verified backup.
- Keep the family check-in and sync queue durable and idempotent. Sync only when connectivity is available and respect user consent and data minimization.
- Cache PAGASA-related alerts/advisories with source, retrieval time, publication time, and expiry/staleness information. Do not present stale cached alerts as current.

## 9. Nationwide emergency directory, maps, and data governance

The intended scope includes national, regional, provincial, city/municipal, barangay, evacuation-centre, and emergency-service information. The target counts previously cited in planning materials (17 regions, 82 provinces, 149 cities, 1,493 municipalities, and 42,046 barangays) are planning figures, not verified completeness claims; validate current administrative counts against authoritative Philippine government sources before packaging.

For each dataset, record:
- authoritative publisher and source URL;
- license/terms and attribution requirements;
- retrieval date, data version, and update cadence;
- geographic and administrative coverage;
- offline package size and device storage requirements;
- validation rules, duplicates, missing fields, and stale-record policy;
- update mechanism and behavior when an update cannot be downloaded.

Do not label the directory “complete nationwide” until automated coverage checks and source verification demonstrate it. Offline maps must have a documented source, licensing, region coverage, update strategy, and package-size estimate. Missing or stale information must be clearly identified, with available official phone numbers or other fallback paths where verified.

### Emergency alerts and RA 10639

Cell Broadcast / Wireless Emergency Alerts behavior is controlled by Android, the device manufacturer, carrier, and government alert system. A normal third-party app cannot promise to intercept every cell broadcast. PH-ResQ may provide alert-awareness guidance and cache authorized public advisories, but must not claim system-level reception or privileged access it does not possess.

## 10. Medical ID QR, family check-in, and accessibility

- Provide an **offline Medical ID QR** containing only the fields the user explicitly chooses to share. Explain that a QR displayed on screen is not inherently encrypted or access-controlled; use a minimal payload and an explicit reveal/share action. Do not expose the encrypted vault blob as though any camera could decrypt it.
- Preserve family check-in and a durable sync queue with clear pending/synced/failed states.
- Include high-contrast mode, scalable text, screen-reader labels, large touch targets, haptic feedback controls, reduced motion, and non-color-only status indicators.
- Provide emergency helpline fallback only for verified numbers and identify the jurisdiction/source. Never fabricate a helpline or imply it works without cellular service.
- Apply safe-volume policy: no automatic loud siren when stealth mode is active; respect user choice, Android audio behavior, and accessibility settings.

## 11. Permissions, foreground service, and lifecycle

- Request only the permissions required for the feature being activated, with a plain-language explanation and a denial path.
- Test location, SMS, Bluetooth/nearby devices, Wi-Fi, notifications, microphone/voice note, and any foreground-service permissions against the selected target SDK and supported Android versions.
- A foreground service must display its required persistent notification and use the correct service type. Provide notification actions where appropriate and ensure stopping the service leaves SOS state recoverable.
- Respect Android background execution limits, battery optimizations, and manufacturer-specific restrictions. Explain settings the user may optionally change; do not use deceptive prompts.
- Test process death, task removal, reboot, permission revocation, app upgrade, and device restart.

## 12. Testing, reliability, and acceptance criteria

The project must not use “zero crash,” “zero bugs,” “100% guaranteed,” or “un-bypassable” as verified outcomes. Replace them with repeatable tests, measured results, and disclosed residual risks.

Required test groups:
- Unit tests for consent, SOS state transitions, retry/backoff, deduplication, expiry, encryption/decryption, token rotation, and data validation.
- Instrumentation tests for permission flows, lifecycle, foreground service notification/actions, SMS handoff/direct-send paths, QR sharing, and accessibility.
- Integration tests for BLE discovery, authenticated peer connection, Wi-Fi Direct transfer, message acknowledgement, TTL, duplicate/out-of-order packets, malformed payloads, and interrupted connections on supported physical devices.
- Resilience tests for no internet, no mobile signal, no SIM, denied permissions, no GPS fix, stale GPS, Bluetooth/Wi-Fi disabled, process kill, reboot, low storage, corrupted database, interrupted migration, battery below 5%, and clock changes.
- Data tests for administrative coverage, duplicate/invalid coordinates, source attribution, map coverage, update failure, and stale alerts.
- Security tests for nonce uniqueness, vault lockout/rate limiting, log redaction, backup leakage, replay attempts, tampered packets, and dummy-vault threat-model limitations.
- Performance tests using realistic nationwide data and low-end supported hardware; report cold start, search latency, memory, battery, APK/download size, and offline package size.

A feature is **accepted** only when its requirement has an implementation reference, a passing test/evidence record, and documented platform/data limitations. A passing documentation review or successful old build is not proof that the current source revision is buildable or that every feature works.

## 13. Release gate

Do not declare PH-ResQ ready for public release until all of the following are true:

1. Architecture and supported Android/API range are recorded.
2. Feature traceability and acceptance matrix cover every retained requirement.
3. Current source revision builds reproducibly and automated checks pass.
4. Critical SOS, consent, persistence, encryption, permissions, and recovery flows pass on physical devices.
5. BLE/Wi-Fi mesh behavior is validated on supported device combinations, or unsupported combinations are explicitly disclosed.
6. Nationwide directory/map source coverage, licensing, package size, and freshness are evidenced.
7. Privacy policy, consent, data minimization, and data-sharing behavior are reviewed.
8. Known limitations, emergency-service fallback, and residual risks are documented.
9. Release artifact, version, checksums, test reports, and rollback/recovery plan are recorded.

## 14. Required companion documents

Maintain these documents alongside this master plan and keep them mutually consistent:

- `README.md` — project status, scope, and truthful setup/validation instructions.
- `WORK_RULES.md` — implementation safeguards, branch/backup/diff discipline, and verification rules.
- `features_update.md` — complete feature inventory; no silent removals.
- `PHASE_1_INIT_CONSENT.md` through `PHASE_6_CONCURRENCY_CHAOS.md` — phase specifications revised to match the Android-first architecture and acceptance criteria.
- `ARCHITECTURE_DECISIONS.md` — recorded framework, storage, security, data, and platform decisions.
- `FEATURE_TRACEABILITY.md` — requirement → component → test → evidence mapping.
- `TEST_ACCEPTANCE_MATRIX.md` — test cases, preconditions, expected results, execution status, and evidence.
- `DATA_SOURCES_AND_RISKS.md` — authoritative datasets, licensing, freshness, coverage, and limitations.

**Status rule:** Planning, implementation, tested, and release-ready are separate states. Mark each honestly and link evidence for every claimed completion.
