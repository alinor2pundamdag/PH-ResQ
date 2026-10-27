# PH-ResQ Privacy and Safety Notice

**Notice version:** 2026-10-27-v1  
**Status:** Early-build notice; requires legal/privacy review before public release.

## What this build stores

- Emergency contact numbers are stored locally in app preferences as AES-GCM ciphertext encrypted with an Android Keystore key.
- SOS event state is stored in the app's local SQLite database. SOS draft, recipient-list and location text fields are encrypted before insertion.
- Last-known location text is stored encrypted locally after the user grants location permission and requests a lookup. The location may be stale and is not live tracking.
- Search history, hotline bookmarks and family check-ins are stored locally in SQLite. They are not remotely synchronized in this build.
- A voice note is recorded only after the user chooses the recording control and grants microphone permission. The temporary recording is encrypted at rest and the raw temporary file is deleted after encryption or on a later app startup if left behind.
- BLE discovery runs only after the user starts a scan and grants the required permission. The current scan detects advertisements only; it does not transmit SOS data.

## Communications

- SOS opens the installed messaging app with a prepared message and recipient URI. The user must review and send it.
- Opening the messaging app is not proof that a message was sent or delivered. This build does not provide confirmed recipient delivery or remote family sync.
- Hotline actions open the system dialer or SMS app. They do not guarantee network availability, answer, or response.

## Permissions and limitations

- Location permission is used for last-known location lookup.
- Microphone permission is used for a user-initiated local voice-note recording.
- Bluetooth/nearby-device permissions are used for user-initiated BLE discovery.
- Contacts are entered manually; the app does not need to read the Android address book in this build.
- No verified nationwide barangay emergency directory or licensed offline map package is bundled.
- This is an early build. It has not passed physical-device, legal/privacy, or release acceptance and must not be relied on as a validated emergency service.

## User controls and retention

Users can edit their saved emergency contact list, clear search history, remove saved hotline bookmarks, and choose whether to record a voice note. The app currently has no complete export/recovery system or dedicated voice-note deletion UI. App data can be removed through Android system app settings; removing app data or uninstalling may make encrypted data unrecoverable because the encryption key is device/app scoped.

## Before public release

Complete legal/privacy review, document retention and deletion behavior, test Android backup/restore behavior, provide a full data-deletion/export path, and verify all data-source licenses and emergency-contact details. This notice is an implementation baseline, not a substitute for legal advice or a final privacy policy.
