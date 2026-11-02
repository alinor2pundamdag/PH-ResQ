# Phase 3: offline directory and spatial data

**Specification only; no directory or database is checked into this repository.**

1. Obtain licensed, authoritative, timestamped Philippine administrative units, emergency hotline and map datasets. Check record completeness and ownership; display source and freshness. Never assume historic counts are current.
2. Choose SQLite with spatial index or a documented hybrid alternative. A geohash prefix index is not an R-tree; cover neighboring cells and use a verified distance calculation with invalid-coordinate handling. Benchmark on actual low-end devices rather than promising a 10 ms query.
3. Ship signed/versioned releases with trusted digests, migration and rollback. A digest cannot restore corrupt data by itself; retain a verified fallback snapshot and surface failures.
4. Mark weather, hotline and evacuation entries stale when updates cannot be fetched. Never assert offline data is live capacity.

**Acceptance gate:** provenance audit, measured coverage, migration/corruption tests, neighbor-boundary spatial tests, offline search tests and published device benchmarks.
