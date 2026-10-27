# PH-ResQ Data Sources, Licensing & Risk Register

**Status:** Source verification pending. This register does not assert that a dataset has been acquired, licensed, or validated.

## Required data inventory

For each dataset, add a row only after the source has been checked and record the official source URL, license/terms, access date, version, geographic coverage, attribution, update cadence, expected offline size, schema, and validation result.

| Dataset | Intended use | Required source/evidence | Key risks |
|---|---|---|---|
| Administrative boundaries and barangay list | Nationwide directory/search and map labels | Philippine Statistics Authority (PSA) Philippine Standard Geographic Code (PSGC), https://psa.gov.ph/classification/psgc/; latest release checked 2026-10-27 reports 18 regions, 82 provinces, 1493 municipalities, and 42,010 barangays as of 2026-09-30. PSA site states CC BY 4.0 unless otherwise stated. Source file still must be acquired, parsed, and audited. | Outdated counts, changed boundaries/names, duplicates, missing coordinates; PSGC alone does not supply all service contacts or precise geospatial points |
| Emergency service contacts and hotlines | Emergency fallback | National Emergency Hotline 911 checked against the Philippine Government Emergency Hotlines portal on 2026-10-27: https://ehotlines.e.gov.ph/; additional/local numbers require their own authoritative source and review date | Wrong or stale number, coverage ambiguity |
| Evacuation centres | Disaster planning | LGU/official disaster-management source, status timestamp | Temporary closure, capacity/status becoming stale |
| Offline base maps/tiles | Offline navigation | OpenStreetMap data is ODbL 1.0 with attribution/share-alike obligations: https://www.openstreetmap.org/copyright. Tile-provider terms and offline redistribution rights must be reviewed separately; OSM data licensing does not automatically authorize copying any tile server's rendered tiles. | Licensing restrictions, package size, stale roads/boundaries |
| PAGASA advisories | Weather/disaster awareness | Official PAGASA source, retrieval/publication timestamps | Network failure, stale alerts, terms/API changes |
| Public safety/disaster advisories | Cached offline information | Issuing authority and usage terms | Misinformation, stale or superseded notice |
| Medical ID fields | User-generated local data | User entry and explicit sharing choices | Unintended disclosure through QR, logs, backup, or screenshots |

## Verified source entries (initial)

- **National Emergency Hotline:** 911.
- **Source:** Philippine Government Emergency Hotlines portal, https://ehotlines.e.gov.ph/.
- **Access/review date:** 2026-10-27.
- **Use in app:** opens the Android dialer with 911 prefilled; the user must confirm the call. This does not guarantee connectivity, answer, or response.
- **Scope limitation:** this single national number is not a completed nationwide barangay directory or verified local emergency-contact database.\n\n- **PSGC administrative reference:** PSA's official PSGC page was checked on 2026-10-27; latest release is dated 2026-10-12 and reports 42,010 barangays as of 2026-09-30. Official page: https://psa.gov.ph/classification/psgc/. The page states website content is CC BY 4.0 unless otherwise stated. The spreadsheet itself still needs to be downloaded, its file hash recorded, and coverage/schema validated before bundling.\n- **OpenStreetMap licensing:** https://www.openstreetmap.org/copyright describes ODbL 1.0 for data and attribution/share-alike requirements. No map tiles are included; tile provider rights remain unverified.

## Data pipeline requirements
1. Download/import only from sources whose terms allow the intended use.
2. Preserve source attribution and retrieval date in the package metadata.
3. Normalize names and administrative hierarchy without discarding the original source identifiers.
4. Validate required fields, coordinates, duplicates, malformed records, and hierarchy consistency.
5. Compare coverage against an authoritative current list; report missing/extra records.
6. Build a versioned package with a manifest and trusted expected digest/signature.
7. Estimate compressed and installed size on target devices; do not promise a fixed APK/offline package size before measuring.
8. Test partial download, interrupted update, rollback, expired cache, and no-network startup.
9. Do not overwrite a verified package with a partial or unauthenticated update.
10. Show users when data was last updated and identify unavailable or stale content.

## Risk register

| Risk | Severity | Mitigation / acceptance condition |
|---|---|---|
| Incomplete or stale nationwide directory | Critical | Authoritative coverage audit, versioned source, missing-data report |
| Unlicensed map redistribution | High | Legal/terms review before packaging |
| SMS not delivered due to carrier/SIM/network | Critical | Durable state, explicit status, fallback instructions; no delivery guarantee |
| Android background execution restrictions | High | Target-SDK-compliant foreground service, lifecycle tests, documented limits |
| BLE/Wi-Fi Direct incompatibility across devices | High | Physical-device matrix; disclose unsupported combinations |
| RSSI distance misleading users | High | Approximation labels and uncertainty; no exact-distance claims |
| Vault key lost on reinstall/device change | High | Explicit key lifecycle and recovery policy; test backup/reinstall |
| Duress PIN activity detected by adversary | Critical | Threat-model review; no absolute secrecy/physical safety promise |
| QR reveals sensitive medical data | High | Minimal user-selected payload, explicit reveal action and warning |
| Hash treated as self-healing | High | Trusted manifest + verified recovery source; integrity mismatch must not silently “repair” |
| Cell Broadcast assumed accessible to app | High | Explain OS/carrier control; no claim of intercepting every alert |
| Process death before low-battery callback | Medium | Persist critical state at each transition; low-battery snapshot remains best-effort |
| Data source/API changes | Medium/High | Scheduled source review, schema validation, stale-data labeling, graceful fallback |

## Verification rule
No source, license, coverage, size, or freshness claim is accepted until the evidence and review date are recorded. Administrative counts in planning documents are targets to verify, not a guarantee of completeness.
