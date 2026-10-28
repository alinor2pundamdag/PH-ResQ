# PH-ResQ Data Sources, Licensing & Risk Register

**Status:** Initial source references reviewed; data acquisition, licensing review, and coverage validation are incomplete. A reference URL is not proof that a complete dataset has been bundled or accepted.

## Required data inventory

Record official source URL, license/terms, access date, version, geographic coverage, attribution, update cadence, expected offline size, schema, and validation result for every packaged dataset.

| Dataset | Intended use | Current source/evidence | Key risks and remaining work |
|---|---|---|---|
| Administrative boundaries and barangay list | Nationwide directory/search and map labels | Philippine Statistics Authority (PSA) Philippine Standard Geographic Code (PSGC): https://psa.gov.ph/classification/psgc/. Latest release checked 2026-10-27 reports 18 regions, 82 provinces, 149 cities, 1,493 municipalities, and 42,010 barangays as of 2026-09-30. PSA site states CC BY 4.0 unless otherwise stated. | Workbook has not been bundled, parsed or coverage-audited. PSGC does not provide a complete emergency-service contact list or all precise coordinates. |
| National emergency hotline | Emergency fallback | 911 listed on the Philippine Government Emergency Hotlines portal: https://ehotlines.e.gov.ph/; checked 2026-10-27. | Dialer handoff is not proof of network access, answer, or response. |
| Other national-agency contact subset | Searchable offline fallback | A subset is copied into the app from the same portal and marked reviewed 2026-10-27. | Each number, method, jurisdiction, availability, and freshness must be independently rechecked before release; not all entries are emergency dispatch lines. |
| Local LGU/barangay emergency services | Nationwide local fallback | No complete authoritative dataset acquired yet. | Critical coverage gap; require source owner, jurisdiction, verification date, duplicates/missing-field audit and update process. |
| Evacuation centres | Disaster planning | Official LGU/disaster-management sources still to be selected. | Closure/capacity may change rapidly; require timestamp and stale status. |
| Offline base maps/tiles | Offline navigation | OpenStreetMap data licensing: https://www.openstreetmap.org/copyright (ODbL 1.0, attribution/share-alike obligations). | OSM data license does not automatically grant permission to copy rendered tiles from any tile server. No offline map package is included; provider terms, coverage, size and attribution remain open. |
| PAGASA advisories | Weather/disaster awareness | Official PAGASA source and permitted access method still to be documented. | Network failure, stale alerts, terms/API changes; show source and publication/retrieval/expiry times. |
| Public safety/disaster advisories | Cached offline information | Issuing authority and usage terms must be recorded per source. | Misinformation, stale or superseded notices. |
| Medical ID fields | User-generated local data | User entry and explicit sharing choices. | Unintended disclosure through QR, logs, backup or screenshots. |

## Verified-source notes

- **911:** the app opens the Android dialer with 911 prefilled; the user confirms the call. This does not guarantee connectivity, answer, or response.
- **National-agency subset:** includes Red Cross 143, PNP 117, MMDA 136, LTFRB 1342, DOTr 7890, DSWD/PNP/PCG text hotlines, PAGASA, PHIVOLCS, BFP, and NDRRMC/PCG office lines. These entries have not all been independently revalidated for current service, jurisdiction, or emergency-dispatch suitability. Recheck before release.
- **PSGC:** official page checked 2026-10-27; latest release noted as dated 2026-10-12 and reports 42,010 barangays as of 2026-09-30. Download the source workbook, record its SHA-256, validate its schema/hierarchy and report missing/extra records before packaging.
- **OpenStreetMap:** ODbL data obligations apply; map tile provider rights are separate. No map tiles are included in this branch.

## Data pipeline requirements

1. Acquire only data whose terms permit the intended use.
2. Preserve attribution, source URL, retrieval date and source version in package metadata.
3. Normalize names/hierarchy without discarding source identifiers.
4. Validate required fields, coordinates, duplicates, malformed records and hierarchy consistency.
5. Compare coverage against a current authoritative list and report missing/extra records.
6. Build a versioned package with a manifest and trusted expected digest/signature.
7. Measure compressed and installed size on target devices.
8. Test partial download, interrupted update, rollback, expired cache and no-network startup.
9. Never overwrite a verified package with a partial or unauthenticated update.
10. Show users when data was last updated and identify unavailable/stale content.

## Risk register

| Risk | Severity | Mitigation / acceptance condition |
|---|---|---|
| Incomplete or stale nationwide directory | Critical | Authoritative coverage audit, versioned source and missing-data report |
| Unlicensed map redistribution | High | License/provider terms review before packaging |
| SMS not delivered due to carrier/SIM/network | Critical | Durable state, explicit status and fallback instructions; no delivery guarantee |
| Android background execution restrictions | High | Target-SDK-compliant foreground service, lifecycle tests and documented limits |
| BLE/Wi-Fi Direct incompatibility | High | Physical-device matrix and disclosed unsupported combinations |
| RSSI distance misleads users | High | Approximation labels and uncertainty; no exact-distance claims |
| Vault key lost on reinstall/device change | High | Defined key lifecycle/recovery policy and backup/reinstall tests |
| Duress PIN activity detected by adversary | Critical | Threat-model review; no absolute secrecy/physical-safety promise |
| QR reveals sensitive medical data | High | Minimal user-selected payload, explicit reveal action and warning |
| Hash treated as self-healing | High | Trusted manifest plus verified recovery source |
| Cell Broadcast assumed accessible to app | High | Explain OS/carrier control; no claim of intercepting every alert |
| Process dies before low-battery callback | Medium | Persist critical state at each transition; low-battery snapshot is best-effort |
| Data source/API changes | Medium/High | Scheduled review, schema validation, stale labeling and graceful fallback |

## Verification rule

No source, license, coverage, size or freshness claim is accepted until the evidence and review date are recorded. Administrative counts are reference counts, not a guarantee that the application contains complete emergency-service coverage.
