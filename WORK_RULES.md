# PH-ResQ Vanguard 2026: Official Operational Rules & Work Protocol (`WORK_RULES.md`)

This document serves as the absolute, non-negotiable operational charter governing all interactions, code generation, architectural planning, and repository management for **PH-ResQ Vanguard 2026**.

---

## 🛑 1. Rule of Zero Unsolicited Modifications (Strict Repository Lock)
- **Absolute Restriction:** The AI assistant is **strictly prohibited** from creating, updating, modifying, committing, or pushing any files or code changes to the GitHub repository without an explicit, direct, and unambiguous command from the repository owner (Alinor). 
- **Protocol:** Every single action involving repository changes must be preceded by explicit user authorization. In all other cases, the AI must provide answers and advice in the chat without touching the repository.

## 🔍 2. The "Double-Audit" & Zero-Crash Policy
- **Emergency-Grade Stability:** Because human safety depends on this application during critical disaster scenarios, zero-crash and zero-failure standards are mandatory.
- **Double-Check Rule:** Before any code or logic structure is presented or approved, it must undergo a rigorous double-check audit to eliminate memory leaks, null-pointer exceptions, UI overflows, and unhandled exceptions.
- **Graceful Degradation:** All system features (GPS reading, local storage access, map rendering) must feature robust fallback states so that temporary sensor or network failures never freeze or crash the application UI.

## 📦 3. 100% Offline-First & Strict Storage Budget (~100MB - ~150MB)
- **Local Autonomy:** Every feature must operate fully offline without external server dependency.
- **Strict Footprint Limits:** 
  - Core APK binary & UI assets: ~20 MB to ~30 MB.
  - Nationwide JSON directory database: ~10 MB to ~15 MB.
  - Compressed offline vector maps / MBTiles: ~50 MB to ~100 MB.
  - Total ceiling: ~150 MB maximum to ensure smooth performance on low-end Android hardware.

## 🔒 4. Privacy-First, RA 10173, Cybersecurity & Permissions Framework
- **Data Privacy Act of 2012 (RA 10173):** Complete adherence to National Privacy Commission (NPC) guidelines. All personal emergency contacts, voice note recordings, and device GPS coordinates remain strictly local (`localStorage` / IndexedDB).
- **Client-Side Encryption & User-Defined Master PIN Vault:** Sensitive user data and the 10-slot emergency contact vault must be secured via **AES-GCM client-side encryption**. 
  - *User-Defined PIN/Passcode:* Users must set a personal Master PIN to lock their emergency contacts and medical data against unauthorized physical snooping.
  - *SOS Bypass:* During emergencies, triggering the SOS broadcast securely uses an internal decryption token without requiring manual PIN entry.
  - *Duress PIN / Panic Wipe Option:* Supports a secondary panic passcode that opens a clean dummy vault during coercion while silently logging a silent distress flag.
  - *Input Sanitization & Integrity:* Input fields are strictly sanitized with Regex filters, and database files are guarded by startup SHA-256 checksum verification.
- **User-Consent & Intent-Driven Emergency Data Sharing:** 
  - *Zero Silent Tracking:* No background location harvesting or cloud telemetry.
  - *Explicit User-Initiated SOS:* Location coordinates and local voice notes are shared and broadcasted via SMS (`sms:`) or P2P mesh exclusively when the user actively triggers the emergency SOS action during a crisis.
  - *Vital Interest Exception:* Fully compliant with emergency data sharing exemptions under Philippine law and Google Play policies.
- **Transparent Google Play Permissions Policy:** Device permissions are requested strictly under core functionality exceptions with prominent in-app disclosures:
  - `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`: Foreground GPS proximity calculation only. *No background tracking.*
  - `CALL_PHONE` / Intent Dispatch (`tel:`): Safe 1-tap emergency calling via native device dialer (`ACTION_DIAL`).
  - `SEND_SMS`: User-triggered emergency SOS broadcast with GPS coordinates.
  - `RECORD_AUDIO`: Optional user-triggered 10-second offline voice note recording for emergency SOS logs.
  - `RECEIVE_BOOT_COMPLETED`: Local startup initialization for safety monitoring.
  - `MODIFY_AUDIO_SETTINGS`: Emergency beacon volume override support.
  - `FOREGROUND_SERVICE`: Persistent high-priority emergency notification channel support under Android 14+ public safety exemptions.
  - `INTERNET` / `ACCESS_NETWORK_STATE`: Wi-Fi maximizer bandwidth optimization and zero-rated disaster alerts.

## 🎛️ 5. Dashboard Controls, SOS Toggle, Persistence & Reliability Standards
- **`[🔄 Refresh GPS]` & `[🌐 Online Update]` Controls:** Main dashboard must feature rapid GPS recalibration with 10-second timeout fallbacks and smart online update fetching with data-saving/offline notices.
- **Home Screen SOS Toggle & Anti-Accidental Hold Guard:** 1-tap / widget SOS toggle requiring a continuous **2-second press-and-hold** with haptic vibration feedback and countdown guard against accidental pocket activation.
- **Tagalog Emergency SMS Broadcast:** Dispatches clear, natural Tagalog emergency messages to all 10 personal emergency contacts containing exact GPS coordinates.
- **Persistent Sticky State Memory (6 Layers):** Strict local storage persistence for Active Mode, Emergency Contact Vault, Last Known GPS, Crisis SOS State, Search History, and Offline Map Tiles.
- **Advanced Reliability Safeguards:** Low-battery auto-save snapshot (<5% battery or unexpected crash protection), startup SHA-256 database integrity verification with self-healing fallback, and offline voice note recorder for trapped disaster victims.
- **Critical Operational Policies:** Emergency Beacon Volume Override (ensuring maximum alarm volume even on silent mode), Android 14+ Foreground Service Notification Lock, Duress PIN Vault, and High-Contrast Accessibility Mode for PWDs and senior citizens.
- **Offline Emergency Utilities:** Must include Offline Flashlight/Strobe (SOS Morse Code), Emergency Siren/Alarm, "Ligtas Ako" (I am Safe) Check-In Broadcast, Battery Saver / Low-Power OLED Emergency Mode, and P2P Local Emergency Mesh Sync (Bluetooth LE / Wi-Fi Direct).
- **Educational Accessibility:** Built-in "About PH-ResQ" mission section and Interactive Quick Tutorial / Onboarding walkthrough for seniors and first-time smartphone users.

## 🔘 6. Context-Aware UI Filtering & Universal Priority Rule
- **Universal Top Priority:** Regardless of the active mode tab (Student, Motorist, Senior/PWD, Worker/OFW, Pet Owner), universal emergency hotlines (**911, PNP, NDRRMC, BFP, Philippine Red Cross**) must **permanently remain pinned at the top** of every active list.
- **Dynamic Proximity Sorting:** Remaining facilities and map pins automatically filter and sort by distance using hardware GPS coordinates according to the active mode tab.

## 🧭 7. Communication & Vision Protocol
- **Direct & Rigorous:** The AI must respond directly to user inquiries, maintaining deep engineering vision and practical foresight without overstepping repository boundaries.
- **Memory Retention:** The AI must always keep these rules active in memory before addressing any task or repository request.
