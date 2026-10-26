# PH-ResQ Vanguard 2026: Official Operational Rules & Work Protocol (`WORK_RULES.md`)

This document serves as the absolute, non-negotiable operational charter governing all interactions, code generation, architectural planning, and repository management for **PH-ResQ Vanguard 2026**.

---

## 🛑 1. Rule of Zero Unsolicited Modifications (Strict Repository Lock)
- **Absolute Restriction:** The AI assistant is **strictly prohibited** from creating, updating, modifying, committing, or pushing any files or code changes to the GitHub repository without an explicit, direct, and unambiguous command from the repository owner (Alinor). 

## 🔍 2. The "Double-Audit" & Zero-Crash Policy
- **Emergency-Grade Stability:** Zero-crash, zero-bug, and zero-failure standards are mandatory. All logic must undergo rigorous double-checking to eliminate null pointers, memory leaks, and UI overflows.
- **Graceful Degradation:** All sensor, GPS, and storage operations must include airtight fallback mechanisms to prevent app freezing during crises.

## 📡 3. Advanced P2P Proximity & Mesh Architecture Rules
- **BLE RSSI & Trilateration:** Must use low-power Bluetooth beaconing and RSSI distance estimation with multi-peer trilateration.
- **Wi-Fi Direct & Geohash:** High-speed peer handshakes and local IndexedDB Geohash spatial indexes must operate entirely offline without server dependencies.
- **Rolling Cryptographic Tokens:** Device IDs must rotate periodically using AES-GCM encryption to maintain absolute privacy under **RA 10173**.
- **Battery Protection Guard:** BLE and Wi-Fi Direct scanning loops must enforce an automatic **15-second timeout and background throttling** to conserve device battery during prolonged zero-peer states.

## 🔒 4. Security, Consent, Duress & Privacy Protocols
- **Non-Bypassable Persistent Consent Loop:** First launch presents mandatory policy acceptance. Declining triggers a warning, forces an exit to the device home screen, sets a `DECLINED` flag, and blocks dashboard access indefinitely until accepted, without blocking active reading.
- **Duress PIN Silent Recovery Protocol:** Entering a secondary panic passcode silently opens a clean dummy vault during coercion. The app **never** displays errors or warnings confirming a fake PIN was entered, guaranteeing absolute physical protection.
- **Stealth Audio Mute Mode:** Supports instant silencing of audible sirens and alarms for high-risk security threats (hostage/break-in) while transmitting silent GPS coordinates.
- **Offline Family QR Code Card:** Dynamically generates secure offline QR codes from encrypted local Medical ID vaults for instant scanning by emergency responders.

## 📦 5. Storage, Persistence & Legal Framework
- **Strict Storage Budget:** ~100MB to ~150MB total installed footprint (APK ~30MB, Directory JSON ~15MB, MBTiles vector maps ~100MB).
- **6-Layer Persistence:** Active Mode, Contact Vault, GPS Fallback, Crisis SOS State, Search History, and Map Tiles.
- **Legal Compliance:** Apache License 2.0, RA 10173 (Data Privacy Act), and RA 10639 (Free Mobile Disaster Alerts Act).
