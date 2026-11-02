# Phase 1: platform, permissions and consent

**Specification only; no application code exists here.**

1. Select native Android or a specified hybrid framework, supported Android/API levels, storage adapter and test devices. Set up a reproducible build, CI and a minimal installable prototype.
2. Make general emergency guidance accessible without a forced privacy/license acceptance loop. Request optional location, contacts, SMS, microphone and nearby-device access only as needed; verify current Android/Play permission policies before implementation.
3. Version the privacy notice; record a specific choice only after durable storage succeeds. Failed writes must not grant access to protected processing. Allow withdrawal and deletion; test upgrade, reinstall, corrupt storage and denied permission states.
4. Do not use `window.close()` as an Android exit or treat Apache 2.0 as an end-user agreement. Do not silently transmit coordinates.

**Acceptance gate:** repeatable tests cover first launch, decline, re-entry, consent withdrawal, storage failure and screen reader navigation. No dashboard lockout of non-sensitive emergency guidance.
