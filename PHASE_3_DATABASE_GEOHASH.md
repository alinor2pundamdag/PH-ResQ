# Phase 3 — Local Database, Six Persistence Areas & Geospatial Search

**Status:** Partial SQLite implementation exists (`ResQDatabase`, schema version 7), with directory/advisory/evacuation JSON import and local query primitives. Complete nationwide data, all persistence flows, migration/recovery acceptance, spatial performance, and offline map rendering remain unverified.

## Objective
Provide reliable offline storage for emergency data and local spatial search, with atomic updates, trusted integrity metadata, and recovery behavior.

## 1. Select and document storage
Choose an Android-compatible database and record the decision. Use a real spatial index only when supported by the selected database and configured correctly. Otherwise document the query strategy and benchmark it. IndexedDB is a browser API and is not the native Android database choice.

Suggested logical entities:
- Administrative area/barangay records with stable source IDs, hierarchy, coordinates, source/version, and validation status.
- Emergency services/hotlines with source, jurisdiction, last-verified date, and contact details.
- Evacuation centres with source and status timestamp.
- Map package metadata and coverage/version.
- Emergency contacts and Medical ID fields, stored encrypted where sensitive.
- SOS events and durable delivery-state transitions.
- Family check-in/sync queue.
- Search/bookmarks and user preferences.
- Integrity manifest and migration metadata.

## 2. Six required persistence areas
1. Active mode.
2. Emergency-contact vault.
3. Last known GPS including timestamp, accuracy, and source.
4. Crisis SOS state/queue.
5. Search/bookmark history.
6. Offline map tiles/package cache.

Use atomic transactions and schema migrations. Persist critical state at transition boundaries. The below-5% battery snapshot is an additional best-effort safeguard, not the only save mechanism.

## 3. Integrity and recovery
- Verify downloaded/packaged data against a trusted expected digest or authenticated manifest.
- On mismatch, quarantine or mark the affected dataset unavailable; do not silently trust it.
- Recover only from a verified backup or trusted source.
- Test interrupted writes, partial downloads, low storage, corrupt records, migration interruption, and rollback.
- Keep integrity and error logs free of personal secrets and precise location.

## 4. Geohash and query behavior
- Geohash supports coarse spatial grouping; choose precision based on the query use case and validate boundary behavior.
- Use a documented spatial index/query plan; do not claim an R-tree if implementation uses brute-force filtering.
- Benchmark realistic national-scale records on low-end supported hardware. Record cold/warm search latency, memory, and package size.
- RSSI proximity estimates are separate from database geospatial queries and must not be presented as exact location.

## 5. Data provenance and maps
For every dataset, record authoritative source URL, license, attribution, access date, version, update cadence, coverage, validation results, and compressed/installed size. Preserve partial-update rollback and label stale or missing data. Administrative counts in the plan are targets to verify, not evidence of completeness.

## Current implementation boundary
The database currently contains app state, SOS events, last location, search history, bookmarks, directory entries, map metadata, dataset metadata, advisories, evacuation centres, mesh queue/replay cache, family check-ins, and pending actions. Table existence does not prove every table is fully wired to UI/repositories or that data is complete. See `DATABASE_SPEC.md`.

## Acceptance criteria
- Six persistence areas survive process death and expected restart flows.
- Database migrations and interrupted writes are tested.
- Trusted-manifest mismatch is detected; recovery source is verified.
- Search results are validated against a sourced fixture and measured on a named device.
- Offline maps render only for downloaded/licensed coverage and clearly show unavailable regions.

Related: `TEST_ACCEPTANCE_MATRIX.md` T-009 through T-011, T-019, T-020, T-024, T-025.
