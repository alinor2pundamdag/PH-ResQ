# PH-ResQ Android Device Compatibility & Adaptive Runtime Architecture

**Status:** Normative target architecture; partial capability checks exist, but the adaptive runtime, device matrix, and physical-device validation are not yet implemented.
**Applies to:** Android OS/API, hardware features, OEM behavior, app lifecycle, UI configuration, storage, and power policy.
**Related:** `MASTER_PLAN.md`, `SYSTEM_ARCHITECTURE.md`, `ARCHITECTURE_DECISIONS.md`, `DATABASE_SPEC.md`, `SECURITY_MODEL.md`, `TEST_ACCEPTANCE_MATRIX.md`.

## 1. Compatibility promise and honest boundary

PH-ResQ targets Android API 26 (Android 8.0) and later under the current `minSdk=26` build configuration. Android API levels below 26 are unsupported by this APK unless a separately maintained legacy product flavor is approved and tested. A future Android API level is a compatibility candidate, not automatically certified because it can install the APK.

No Android application can guarantee identical behavior on every device worldwide. Device vendors modify power management, background execution, Bluetooth stacks, notification behavior, location providers, storage, and system UI. PH-ResQ therefore uses capability detection and tested support tiers rather than claiming universal compatibility.

**Product requirement preserved:** every retained feature remains in scope. When a device lacks a capability, the feature must offer a safe alternative or a clear unavailable state; it must not silently pretend to work or be removed from the feature registry.

## 2. Supported-device tiers

| Tier | Definition | Product behavior |
|---|---|---|
| A — Validated | Device/API/OEM combination passed the physical-device acceptance suite | Feature behavior may be described as device-verified for that exact combination |
| B — Compatible by API/capability | API and required hardware exist; automated and targeted tests pass, but exact OEM model has not completed the full suite | Feature is supported with normal caveats; do not claim model-specific validation |
| C — Degraded | Core app runs, but one or more optional capabilities are missing, restricted, or denied | Use documented fallback; show capability state and preserve local data |
| D — Unsupported | API below minSdk, ABI/runtime incompatibility, storage/security prerequisite failure, or critical platform behavior cannot be safely met | Block installation/use where technically possible or show a clear unsupported-device explanation; never silently downgrade security |

Maintain a versioned device matrix containing manufacturer/model, Android release/API, build fingerprint (test evidence only), ABI, RAM/storage class, radio hardware, test date, app SHA, and test outcomes. Avoid a static OEM blacklist; use runtime capability checks and only add model-specific workarounds when a reproducible defect is documented.

## 3. Runtime capability profile

Create one immutable `DeviceCapabilityProfile` at process start and refresh dynamic fields when configuration, permission, radio, battery, or app lifecycle changes.

### Static/device properties
- Android API level, release, target SDK, supported ABI.
- Telephony feature/SIM state; do not assume a phone has a SIM or SMS app.
- BLE feature, adapter availability, scanner availability.
- Wi-Fi Direct feature/service availability (when implemented).
- GPS/provider availability and coarse/fine permission state.
- Microphone availability/permission.
- Notification manager/channel state and Android 13+ notification permission.
- Keystore availability, key algorithm/provider support, hardware-backed/StrongBox capability when queryable.
- Available memory/storage, screen dimensions/density, font scale, display cutouts/insets.
- Battery saver, power-save mode, background restriction state where available.

### Dynamic capability states
Each capability is one of: `AVAILABLE`, `AVAILABLE_WITH_PERMISSION`, `DENIED`, `DISABLED`, `UNSUPPORTED`, `RESTRICTED_BY_SYSTEM`, `TEMPORARILY_UNAVAILABLE`, or `ERROR`. Include a reason code, last-checked time, and safe fallback. Do not reduce these to a single boolean.

## 4. Capability adapters and fallbacks

| Capability | Primary Android integration | Detect/handle | Fallback and truth rule |
|---|---|---|---|
| Location | Fused/platform location provider | API, provider, coarse/fine permission, timeout, last-fix age/accuracy | Show last-known timestamp/accuracy or manual location; never label stale as fresh |
| SMS | Explicit messaging-app handoff under current ADR-010 | SIM, resolver, URI/recipient behavior, app availability | Keep durable draft and allow user copy/share/dialer; handoff is not sent/delivered |
| Notifications | Notification channels + permission-aware posting | API 26 channel rules; API 33+ runtime permission; channel disabled | In-app status remains authoritative; never block SOS preparation on optional notification denial |
| Foreground service | Target-SDK-specific FGS type and start restrictions | API level, service type permission, app state, notification state | Do not start prohibited background work; preserve state and explain status limits |
| BLE | BLE feature, adapter, runtime permissions, scan/connect state | API 26–30 location permission behavior; API 31+ BLUETOOTH_SCAN/CONNECT; adapter and OEM errors | Discovery unavailable; no mesh success; retain queued packet locally |
| Wi-Fi Direct | WifiP2pManager (not implemented) | API permissions, location/services, hardware, group/connection state | No transfer claim; queue locally |
| Microphone | AudioRecord/MediaRecorder | hardware, runtime permission, audio focus, interruption | Voice note unavailable; SOS text flow remains |
| Keystore | AndroidKeyStore | provider, algorithm, hardware backing, key invalidation | Fail closed for protected fields or use only an explicitly reviewed compatibility mode; never silently write plaintext |
| Storage | App-private files + SQLite | low storage, I/O errors, scoped storage | Stop optional cache writes; preserve/communicate critical-write failure |
| Haptics/audio | Vibrator/audio APIs | vibrator/audio device and system settings | Visual countdown/status remains; do not claim haptic feedback occurred |
| UI/accessibility | Compose, WindowInsets, font scale, semantics | display size, font scale, TalkBack, contrast, reduce motion | Responsive layout; emergency actions remain reachable and semantic |

Every adapter must return a typed result: success with evidence, recoverable failure, unsupported, permission denied, or cancelled. Do not return a boolean that conflates “request launched” with “operation completed.”

## 5. Adaptive UX and performance profiles

The runtime selects a policy profile from actual constraints, not a hard-coded phone model:

- **Normal:** standard UI motion, user-requested scans, normal cache behavior.
- **Battery saver:** reduce nonessential refresh/animation, defer optional imports/index work, do not disable critical SOS local persistence.
- **Critical battery (<=5%):** stop optional scanning and expensive refresh; persist important state at each transition (not only in a low-battery callback); keep the SOS action and last-known data accessible.
- **Low memory:** release image/QR/preview buffers, page database queries, avoid large in-memory datasets, handle process recreation.
- **Low storage:** refuse oversized import before allocation, avoid evicting pending SOS data, show storage warning, preserve transaction rollback.
- **Large text / small screen:** scroll content independently, keep primary actions visible, use responsive layout and no clipped controls.
- **Accessibility:** TalkBack labels/order, semantic state descriptions, contrast, non-color-only status, reduced motion and adequate touch targets.

The profile may adapt optional work, but must never silently weaken encryption, remove user consent, bypass permission requirements, or mark unavailable transport as successful.

## 6. Android lifecycle contract

- Activities/Compose UI render state; they do not own durable emergency work.
- Persist a critical user action before starting an external handoff or asynchronous task.
- Use lifecycle-aware collection; cancel UI-scoped work when leaving the screen.
- Use WorkManager for deferrable, persistent background work when its execution semantics fit; do not use it for real-time radio guarantees or emergency delivery guarantees.
- Use a foreground service only for a user-visible, policy-eligible ongoing task with the required service type and notification.
- Handle process death, task removal, reboot, permission revocation, adapter off/on, configuration change, and package upgrade.
- Android force-stop, OEM task killers, battery restrictions, disabled radios, and revoked permissions remain outside app control. State must be durable before these events, and the UI must explain what cannot continue.

## 7. Minimum validation matrix

Run API-level emulator tests at API 26, 28, 29, 30, 31, 32, 33, 34, and 35; add the newest stable API available at test time. Emulator tests do not prove radio/OEM behavior. Physical devices must include at least:
- one low-memory/entry-level device,
- one mid-range device,
- one current high-end device,
- two different OEM families,
- two devices for BLE/Wi-Fi Direct interoperability,
- one device with no SIM/telephony,
- one device with Bluetooth unavailable/disabled where possible.

Record each exact model/API/ABI, permission state, battery mode, network, expected/observed behavior, test ID, candidate SHA, and evidence. Future API versions remain provisional until tested.

## 8. Definition of device compatibility

A device/API combination is compatible only when:
1. The APK installs and launches on the declared API/ABI.
2. Consent, navigation, core local data, and SOS draft workflows remain usable.
3. Unsupported permissions/hardware are detected and safely degraded.
4. No crash, ANR, data corruption, duplicate SOS dispatch, or false transport success occurs in the defined test suite.
5. Database upgrades and protected-data behavior are correct.
6. Accessibility and screen adaptation meet the matrix.
7. The result is linked to a specific app SHA and device evidence.

This is a measurable support policy, not a guarantee of zero crashes on every Android device.
