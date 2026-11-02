# PH-ResQ Vanguard 2026 🇵🇭

> **Status: concept and implementation plan only. NOT an operational emergency app.**
> This repository now has an experimental Android source project, but a CI-built debug APK (not device-tested), no emergency directory, maps, automated tests, or verified hotline data. **Do not rely on it to request help.** For a real emergency, use available official local emergency channels.

PH-ResQ is a proposed offline-first mobile emergency aid for the Philippines. The Android source implements only a small **unverified prototype**: a consent choice, a blue/red dashboard inspired by the owner-provided infographic, offline general guidance, a locally saved incident note, manual 911 dialer launcher, one locally saved contact, optional last-known location display, and a review-before-send SMS composer. Unsupported tiles explicitly say they are unavailable rather than showing fabricated live alerts. It does not itself place calls or send SMS. The plan documents describe *intended* additional behavior, not shipped functionality. Offline maps and saved guidance may work without internet **once implemented and downloaded**; SMS/calls require a working cellular network, and device-to-device messaging requires compatible nearby devices, permissions, and working radios. No delivery is guaranteed.

## Build the Android project locally (requires Android SDK and Gradle)

Install JDK 17, Android SDK platform 35 and Gradle 8.9+ with access to Google Maven. From this directory run `gradle :app:assembleDebug`. If it succeeds, the local artifact is `app/build/outputs/apk/debug/PH-ResQ-debug.apk`; install it on a test Android device and exercise permission denial, no SIM, GPS unavailable and SMS cancellation. **GitHub Actions built a debug APK from commit `65f89b8` (run `41445922041`); it has not been device-tested.** Download the `PH-ResQ-debug-apk` artifact from https://github.com/alinor2pundamdag/PH-ResQ/actions/runs/41445922041 and extract the APK from the ZIP. The source image for the launcher icon was supplied by the owner; the installed launcher name is **PH-ResQ**. The build output is named **PH-ResQ-debug.apk** (browsers or file managers can rename downloads independently). Never distribute the prototype as an emergency service.

## Current repository

- `app/`: experimental Android source (privacy screen, dashboard, guidance, incident note and manual SOS composer).
- `TEST_PLAN.md`: device test checklist; not yet executed.
- `MASTER_PLAN.md`: proposed architecture, risks and milestones.
- `PHASE_1_INIT_CONSENT.md` through `PHASE_6_CONCURRENCY_CHAOS.md`: implementation and verification gates (not runnable code).
- `features_update.md`: proposed feature backlog; only the limited prototype functions above have been started.
- `WORK_RULES.md`: contribution and safety rules.
- `LICENSE`: Apache License 2.0 for repository contents; third-party data, maps and feeds need separate rights checks.

## Before claiming readiness

1. Select one supported Android architecture and build a minimal installable app; document supported OS versions and devices.
2. Verify consent, location permissions, contact enrollment, SOS cancellation, SMS/call failure handling and accessible manual fallback on physical devices.
3. Source, license, date, and validate hotline/directory/map datasets; display freshness and uncertainty, including on offline screens.
4. Add repeatable unit, integration, device and offline/low-battery tests, plus privacy/security review. Do not advertise or deploy as a safety tool until independently tested.

See the phase documents for specific acceptance criteria. Proposed budgets, administrative counts, response times and accuracy targets must be measured against dated sources before publication.
