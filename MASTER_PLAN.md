# PH-ResQ Vanguard 2026: proposed architecture

**Planning document, not a functional product.** An unbuilt native Android source now exists in `app/` with a privacy screen, dashboard, offline general guidance, local incident note, manual dialer/SMS composer, one local contact and last-known location display. A debug APK was built by CI from commit `65f89b8` (run `41445922041`), but it has not been installed or device-tested. No verified nationwide dataset, automated test suite or deployed service is present. The items below are requirements and research questions, not completed features.

## 1. Scope and emergency baseline
Build an offline-first Android aid with stored emergency guidance, user-managed emergency contacts, a clearly labeled SOS action and a vetted local hotline directory. Offline content must not imply that cellular SMS/calls or nearby-device delivery work during a network outage. Show delivery status as *attempted*, *sent by device*, or *unknown*; never conflate these with receipt or rescue. Always show a manual fallback and location age/accuracy. Support users without GPS or with denied permissions.

Prioritize readable emergency controls, large tap targets, screen reader labels, high contrast and reduced motion. Animated flag/header designs are optional and must not delay first useful paint, obscure SOS or consume critical battery.

## 2. Platform and consent
Choose and document a single Android architecture before implementation (native Android or a specified hybrid stack). Browser `localStorage`, IndexedDB, Web Crypto, BLE, Wi-Fi Direct and Android Keystore are not interchangeable; each integration needs its own supported platform adapter and tests.

Request granular permissions just in time. Explain purpose, retention, deletion, and whether any data leaves the device. Record versioned consent only for optional processing that requires it; do not treat open-source license acceptance as a privacy gate. Declining optional processing must not automatically terminate the app or block access to general emergency guidance. A consent save failure must not mark the session accepted. Consult Philippine privacy counsel before claiming RA 10173 compliance.

## 3. SOS path (first release)
Require deliberate activation and an accessible alternative to long-press; provide cancellation and clear feedback. Ask for recipient enrollment and validate numbers. When GPS is absent, use a timestamped last-known location with explicit warning, or send a message without coordinates if the user approves. Treat device SMS/call errors, missing SIM, denied permissions, zero balance and no signal as normal states. Provide direct dial/manual copy fallback; never silently reroute to a potentially wrong local number. Test on real low-end devices and under airplane mode, low power and interrupted process conditions.

## 4. Offline data and freshness
Acquire official, licensed, versioned directory/hotline/map sources; do not invent counts or claim nationwide coverage before checking completeness. Define update cadence, integrity manifests, signed releases, rollback and recovery from corrupt data. SHA-256 alone detects mismatch against a trusted expected digest; it cannot self-heal missing/corrupt records. Benchmark spatial search on realistic data and hardware; geohash indexing is not an R-tree. Label cached weather/evacuation data with timestamps and do not present it as real time while offline. Cell broadcast reception depends on platform/carrier support; investigate platform APIs before advertising integration.

## 5. Security and privacy
Use Android Keystore-backed key management where supported; plan backups and recovery explicitly. Never recreate encryption keys when reopening saved vault data. Use authenticated encryption with fresh nonces, versioned envelopes and tested migration. PINs require a rate-limited, salted password KDF; compare derived values safely. Do not present client-side PIN checks or browser crypto as hardware-backed. QR display of sensitive medical information needs a threat model and responder-compatible access design; an encrypted QR cannot be read by an ordinary camera without a key. Duress and stealth flows are deferred until independently reviewed for coercion and false-trigger risk.

## 6. Research track: proximity messaging
BLE signal strength is noisy and cannot provide exact distance or reliable trilateration without calibrated anchors. Wi-Fi Direct and BLE need native permissions, device compatibility, discovery throttling and power measurements. A timer/log statement is not a working mesh. Prototype authenticated, encrypted, replay-resistant packets and test multi-hop delivery in field trials before making availability claims. Never rely on mesh as the only SOS path.

## 7. Release gates
Follow the six phase documents in order. Require reproducible builds, automated tests, privacy/security review, offline/low-memory device tests, permissions and Play policy review, dataset provenance checks, accessibility checks and an independent emergency workflow exercise. Publish measured device/OS coverage, footprint, latency and failure rates instead of absolute guarantees. No production-release claim until these gates pass.
