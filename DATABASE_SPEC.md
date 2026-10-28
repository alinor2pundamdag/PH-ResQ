# PH-ResQ Local Database Specification

**Status:** Current schema inventory plus target invariants. The SQLite helper is implemented, but full repository wiring, migration coverage, retention, backup/recovery, and acceptance are not complete.
**Current database:** `ph_resq_local.db`, schema version 7 in `ResQDatabase.kt`.
**Related:** `MASTER_PLAN.md`, `SECURITY_MODEL.md`, `TEST_ACCEPTANCE_MATRIX.md`.

## 1. Storage principles

- Use SQLite transactions for related state changes and atomic dataset replacement.
- Keep sensitive SOS drafts, recipients, precise location, queued mesh packets, and pending action payloads encrypted before persistence.
- Never treat encryption as a substitute for access control, data minimization, retention, or verified backups.
- Use schema migrations with explicit version steps; never drop user data as a shortcut to resolve migration failures.
- Treat imported source/license/version metadata as untrusted claims unless validated independently.

## 2. Current tables

| Table | Purpose / key fields | Data sensitivity / notes |
|---|---|---|
| `app_state` | key/value persisted app mode and settings | Review each value; do not store secrets |
| `sos_events` | state, created_at, encrypted draft/recipients/location | High sensitivity; ciphertext columns must not be logged |
| `last_location` | one row: latitude, longitude, accuracy, capture time | Sensitive location; define retention and access path |
| `search_history` | query and timestamp | Potentially sensitive; provide clear/delete control |
| `bookmarks` | item ID, label, creation time | Labels may reveal sensitive locations |
| `directory_entries` | ID, name, category, optional phone/coordinates, source URL, verification time | Public contact data still requires licensing/freshness validation |
| `map_tiles` | tile key, local path, SHA-256, dataset version, update time | Path/hash are metadata; hash is useful only with trusted expected digest |
| `dataset_meta` | dataset name, source URL, license, version, verified/imported times, count | Metadata is not a digital signature |
| `cached_advisories` | title, severity, body, source, published/fetched/expiry times | Stale advisory must be visibly marked |
| `evacuation_centers` | name, address, coordinates, status, source, verified time | Status freshness is safety-critical |
| `mesh_queue` | unique message ID, encrypted packet, created/expiry, attempts, status, ACK fields | Transport is not currently integrated |
| `mesh_seen` | message ID and seen time | Persistent replay suppression; retention/capacity policy required |
| `family_checkins` | status, creation time, sync state | Local record does not imply remote delivery |
| `pending_actions` | action type, encrypted payload, creation time, attempts, status, error | Define per-action idempotency and expiry |

The exact SQL and migration steps in `ResQDatabase.kt` are the source of truth for the deployed schema. If this document and code disagree, update this document in the same change and add a migration test.

## 3. Transactional invariants

1. An SOS event and its initial state are committed before a send/handoff attempt.
2. Import validates the entire file before replacing the corresponding dataset. Replacement must be transactional; invalid input must leave the previous cache intact.
3. A duplicate mesh `message_id` must not create a second queue row.
4. ACK state changes only after authenticating the ACK against the key established by a future authenticated transport.
5. Expired queue/advisory records are not presented as current.
6. Migration failure must not silently reset or delete the database.
7. Search/history retention and clear actions must operate transactionally and must not erase active SOS state.

## 4. Dataset import contract

The current importer supports bounded user-selected JSON for directory, advisories, and evacuation centres. It validates schema fields and constraints and records source metadata. It does **not** cryptographically authenticate a publisher, automatically download data, or certify nationwide coverage. Do not call a file "verified" solely because it passes structural validation.

- Enforce file size and record-count limits before materializing unbounded input.
- Validate HTTPS source URL syntax, required fields, IDs, enums, coordinates, timestamps, duplicate IDs, and cross-field constraints.
- Reject malformed or oversized files without partial replacement.
- Keep dataset type, version, import time, record count, source, and license for audit.
- Include fixture tests for valid, malformed, duplicate, stale, and maximum-size cases.

## 5. Retention, backup, and recovery

A release policy must define retention for SOS events, precise locations, search history, check-ins, voice notes, pending actions, and mesh replay records. Default to minimum necessary retention; provide user-facing deletion where safe. Never delete a still-pending SOS without explicit disclosure.

Backup policy must account for Keystore keys not being portable across devices. A restored ciphertext whose key is missing must be reported as unrecoverable, not silently interpreted as empty data. Recovery requires a separately designed, user-authorized mechanism.

## 6. Required indexes and performance

Current indexes include directory coordinate columns; this is not equivalent to a full spatial index. Before claiming nationwide nearby search performance, benchmark with realistic directory volumes and low-end supported hardware. Add indexes based on query plans for directory category/name, dataset freshness, queue status/expiry, and search history as justified by measured query patterns.

## 7. Migration and acceptance

Test upgrades from every released schema version through version 7, interrupted migrations, corrupted rows, low storage, concurrent readers/writers, encryption key loss, and import rollback. Record schema version, test result, device/API, and evidence in the acceptance matrix. Do not raise the database version without a forward migration and test fixture.
