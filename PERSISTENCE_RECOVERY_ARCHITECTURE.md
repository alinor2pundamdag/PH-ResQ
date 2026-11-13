# PH-ResQ Persistence, Recovery & Data Lifecycle Architecture

**Status:** Normative target; current SQLite schema v8 and selected encrypted stores exist, but complete repository wiring, migrations, retention, backup/recovery, and device acceptance are open.
**Related:** `DATABASE_SPEC.md`, `SOS_STATE_MACHINE.md`, `SECURITY_MODEL.md`, `ARCHITECTURE_DECISIONS.md`, `TEST_ACCEPTANCE_MATRIX.md`.

## 1. Persistence invariants

1. Persist user-approved critical state before starting external work.
2. Never rely on Activity memory, a coroutine, a broadcast callback, or a foreground service as the sole copy of a critical record.
3. Every write that spans multiple tables is transactional and idempotent.
4. A failed/corrupt migration never silently drops or resets user data.
5. Ciphertext without its required key is reported as unrecoverable; it is never interpreted as an empty value.
6. A transport attempt is not a delivery receipt; a local queue row is not a peer transmission.
7. Dataset replacement is atomic; failed validation leaves the prior usable version intact.
8. Every persisted dataset/action has a version, timestamp, owner/source, retention rule, and recovery behavior.
9. Low-battery callbacks are best effort; critical writes happen at transition boundaries.
10. Feature preservation is mandatory: migration/refactoring must retain existing records and behavior.

## 2. Six persistence areas and acceptance

| Area | Data | Current evidence | Required completion |
|---|---|---|---|
| P1 Active mode/settings | Consent version, UI settings, active mode, capability choices | ConsentStore + selected SharedPreferences/app_state values | Inventory every setting; typed repository; versioned migration; restore and consent revocation tests |
| P2 Contact vault | Emergency contacts, PIN verifier/derived key metadata | Keystore field cipher + PBKDF2/AES-GCM PIN vault integrated into contact UI | Key lifecycle, key invalidation/reinstall behavior, PIN change, recovery policy, deletion, no plaintext backup/log |
| P3 Last GPS | Last fix, accuracy, capture time | SQLite last_location + bounded one-shot location helper | Freshness policy, permission revocation, manual fallback, encrypted-at-rest review, retention, reboot/process-death tests |
| P4 SOS event/outbox | Event, state transitions, draft, recipient snapshot, location, retry and handoff evidence | Schema v8 `sos_events` stores a stable event UUID; encrypted draft/recipient/location snapshot is committed with QUEUED before handoff; SENDING and HANDED_OFF are persisted transitions; generic DELIVERED/ACKNOWLEDGED transitions are blocked without receipt evidence | Event-scoped recovery worker, attempt idempotency/reconciliation, process-death tests, and trusted receipt/ack pipeline if a real transport is added |
| P5 History/bookmarks/check-ins | Search, bookmarks, family check-ins and sync state | SQLite tables and Robolectric persistence tests | Repository/UI coverage, clear/delete retention, remote sync contract, no false synced state, migration fixtures |
| P6 Offline data/maps | Directory, advisory, evacuation, map tile metadata/files, mesh queue/replay | SQLite schema v8, import/search/cache primitives, encrypted mesh outbox | Licensed packages, signed manifest, version/rollback, tile file lifecycle, stale handling, queue retention and disk pressure |

The six areas are logical domains, not six separate databases. They must share one transaction and encryption policy where cross-domain invariants require it.

## 3. Data ownership and repositories

Each persisted aggregate must have one repository boundary:
- `SettingsRepository`
- `EmergencyContactRepository` / `VaultRepository`
- `LocationRepository`
- `SosEventRepository`
- `HistoryBookmarkCheckinRepository`
- `OfflineDatasetRepository` / `MapTileRepository`
- `MeshOutboxRepository`

UI code reads observable domain state and issues commands; it must not issue arbitrary SQL or bypass domain validation. Each repository exposes typed outcomes and transaction boundaries. A repository is not complete until its UI, migration, and recovery tests exist.

## 4. Transaction and state ledger

For SOS:
- Transaction A: create stable event ID, persist immutable user intent/draft/recipient snapshot and `QUEUED` transition.
- Transaction B: persist attempt ID and `ATTEMPTING` before opening a platform transport.
- Transaction C: record only the result actually returned by Android/transport: `HANDED_OFF`, `FAILED`, `CANCELLED`, or receipt-backed delivery state.
- On process restart, inspect the last committed transition and reconcile only idempotent/recoverable work. Never blindly repeat a potentially user-sent SMS handoff.

For mesh:
- Store encrypted envelope, expiry, attempts and route metadata before radio enqueue.
- Record radio write result separately from peer ACK.
- ACK must bind message ID, peer identity, protocol version and authenticated session.
- Retry is bounded and idempotent; expired work becomes `EXPIRED`, not `SENT`.

## 5. Key lifecycle, backup and recovery

Before production, explicitly choose:
- Which data is device-bound and intentionally unrecoverable after app data loss.
- Whether any user-authorized encrypted export/restore is supported.
- How Keystore key invalidation, biometric/device-lock changes, app reinstall, backup/restore, OS upgrade and device migration are handled.
- How key rotation re-encrypts records transactionally and can resume after process death.
- What happens when a key is unavailable: fail closed, show a recoverable error, preserve ciphertext for support/forensic export only with explicit consent.

Do not copy Android Keystore private key material into cloud backup. Do not silently create a replacement key and then report old ciphertext as decrypted. Do not introduce plaintext fallback.

## 6. Retention and deletion

Define per-domain retention periods and user controls for SOS history, precise location, voice notes, search history, check-ins, pending actions, imported data and mesh replay records. Pending SOS events require a clear warning before deletion. Deleting a dataset must remove both DB metadata and its owned files; use an atomic staging/commit process so interrupted deletion is recoverable.

## 7. Migration policy

- Maintain explicit sequential migrations from every supported released schema version.
- Test empty install and upgrades from each prior version, including schema v8 forward migration.
- Test interruption, low disk, duplicate migration, corrupt row, encrypted-column migration and rollback.
- Never use destructive fallback migration in production.
- Keep a recoverable pre-migration snapshot where storage/security policy permits; validate snapshot integrity before use.
- Migration version and result must be observable in a redacted diagnostic record.

## 8. Recovery scenarios

| Scenario | Required behavior |
|---|---|
| Activity recreation | UI reconstructs from repositories |
| Process death | Durable records recover; no duplicate external dispatch |
| Device reboot | Local records remain; only eligible scheduled work resumes under Android rules |
| App upgrade | Sequential migration preserves all retained records |
| Key invalidation | Protected values remain ciphertext; show explicit recovery/unavailable state |
| Low storage | Reject optional import/cache; do not corrupt or silently delete pending SOS |
| DB corruption | Detect, preserve evidence, recover only from validated backup; never silently reset |
| Interrupted dataset import | Previous dataset remains active |
| Interrupted map tile update | Previous manifest/version remains active |
| User clears history | Clear only selected history; do not delete pending SOS or vault data |
| Uninstall/device change | Explain app-private data and device-bound key loss; no false cloud recovery promise |

## 9. Persistence acceptance gate

Pass only with: repository inventory, schema/data dictionary, migration fixture for every supported version, transaction tests, process-death/reboot tests, key lifecycle decision and tests, retention/deletion tests, corrupt-data/low-storage tests, and physical-device evidence. The existence of a SQLite table or encrypted helper alone is not acceptance.
