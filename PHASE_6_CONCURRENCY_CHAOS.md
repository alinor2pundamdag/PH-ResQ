# Phase 6: reliability and release verification

**Specification only; no test suite or concurrency code exists here.**

1. Specify state transitions for SOS, queueing, cancellation and retries. Use transactional storage operations; a UI mutex alone does not make database writes atomic. Test re-entrancy, cancellation, timeouts and process death.
2. Dispose timers, listeners, location updates and radio sessions on lifecycle transitions. Measure memory/battery on low-end devices rather than claiming zero leaks.
3. Inject failures: absent GPS, no SIM/signal, stale location, revoked permissions, broken storage, corrupted dataset, low battery, background termination, concurrent SOS taps and clock drift. Provide visible failure and manual fallback.
4. Automate unit/integration/device checks and document reproducible builds. Run hands-on accessibility, security/privacy and emergency workflow reviews before release.

**Acceptance gate:** recorded test matrix, measured crash/failure rates, outstanding risks and an explicit go/no-go review. No 'zero-crash' guarantee.
