# PH-ResQ Privacy and Safety Notice

**Notice version:** 2026-11-02-v2  
**Status:** Early-build notice; requires legal/privacy review before public release.

This notice is shown on first launch and whenever the policy version changes. The user must explicitly Accept to enter the main screen. Declining exits the activity and clears acceptance. The notice content scrolls independently while Accept and Decline remain in a fixed action area. Acceptance is persisted with the exact policy version and timestamp. The main screen provides a review/withdraw control; withdrawing clears acceptance and returns to this gate. Withdrawing consent does not itself delete previously stored app data.

## What this build stores

- Emergency contact numbers are stored locally in app preferences as AES-GCM ciphertext encrypted with an Android Keystore key.
- SOS event state is stored in the local SQLite database. SOS draft, recipient-list and location text fields are encrypted before insertion.
- Last-known location text is stored encrypted locally after the user grants location permission and requests a lookup. The location may be stale and is not live tracking.
- Search history, hotline bookmarks and family check-ins are stored locally in SQLite. They are not remotely synchronized in this build.
- A voice note is recorded only after the user chooses the recording control and grants microphone permission. The temporary recording is encrypted at rest and the raw temporary file is deleted after encryption or on a later app startup if left behind.
- BLE discovery runs only after the user starts a scan and grants the required permission. The current scan detects advertisements only; it does not transmit SOS data.
- Consent acceptance stores the current policy version and acceptance timestamp in local app preferences.

## Communications and sharing

- SOS opens the installed messaging app with a prepared message and recipient URI. The user must review and send it.
- Opening the messaging app is not proof that a message was sent or delivered. This build does not provide confirmed recipient delivery or remote family sync.
- Sharing a voice note decrypts it to a temporary plaintext file so the selected external app can read it. The user chooses the target app and recipient. PH-ResQ cannot control that app's retention, cloud backup, or onward sharing. Temporary-file cleanup is attempted when the share flow returns and at a later app startup.
- Hotline actions open the system dialer or SMS app. They do not guarantee network availability, answer, or response.

## Permissions and limitations

- Location permission is used for user-initiated location lookup.
- Microphone permission is used for user-initiated local voice-note recording.
- Bluetooth/nearby-device permissions are used for user-initiated BLE discovery.
- Contacts are entered manually; the app does not need to read the Android address book in this build.
- No verified nationwide barangay emergency directory or licensed offline map package is bundled.
- This is an early build. It has not passed physical-device, legal/privacy, or release acceptance and must not be relied on as a validated emergency service.

## User controls and retention

Users can edit their saved emergency contact list, clear search history, remove saved hotline bookmarks, and choose whether to record or share a voice note. The app currently has no complete export/recovery system or dedicated voice-note deletion UI. App data can be removed through Android system app settings; removing app data or uninstalling may make encrypted data unrecoverable because the encryption key is device/app scoped.

## Before public release

Complete legal/privacy review, document retention and deletion behavior, test Android backup/restore behavior, provide a full data-deletion/export path, and verify all data-source licenses and emergency-contact details. This notice is an implementation baseline, not a substitute for legal advice or a final privacy policy.