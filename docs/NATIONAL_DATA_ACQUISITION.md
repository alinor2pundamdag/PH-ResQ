# PH-ResQ national reference data acquisition
Last reviewed: 2026-11-13

## Official sources
- PSA PSGC masterlist: https://psa.gov.ph/classification/psgc — Q3 2026 count 42,010 barangays; geographic reference only, not contacts or polygons.
- PSA PSGC API: https://psa.gov.ph/classifications-api/psgc — versioned API; token required.
- DILG LGU Local Officials directory: https://lgu201.dilg.gov.ph/
- DILG Barangay Officials Directory: https://dilg.gov.ph/barangay-officials-directory
- DILG SubayBayan: https://subaybayan.dilg.gov.ph — evacuation-center projects; DILG says no consolidated national file with coordinates and PSGC.
- RA 12076: https://lawphil.net/statutes/repacts/ra2024/ra_12076_2024.html
- NAMRIA / Geoportal: https://www.namria.gov.ph/ and https://geoportal.gov.ph/
- PHIVOLCS hazard maps: https://www.phivolcs.dost.gov.ph/gisweb-download-hazard-maps-instruction/
- Pasig City emergency contacts: https://pasigcity.gov.ph/news-and-releases/emergency-contact-numbers-ng-lungsod-ng-pasig-393 — official 24 July 2024 list, including named barangay numbers; seeded as review-due because it is not recently call-verified.
- Marinduque emergency contacts: https://marinduque.gov.ph/emergency-contacts/ — official provincial page with province and municipal DRRMO, police and fire numbers; seeded as review-due.
- Lapuyan emergency contacts: https://www.lapuyan.gov.ph/contact-us/ — official municipal page with police, RHU, fire, DRRMO and Coast Guard numbers; seeded as review-due.
- Bay evacuation inventory: https://bay.gov.ph/wp-content/uploads/2025/12/Ecological-Profile-2025-CDP-BAY.pdf — Table 130, 28 listed facilities, capacity in families, as of December 2024; coordinates are not supplied and operating status is not current-verified.
- Geofabrik Philippines OSM extract: https://download.geofabrik.de/asia/philippines.html

## Verification policy
A PSGC row proves administrative identity only. It does not prove a public phone number or evacuation facility exists. Contact entries require public office number, source URL/title and explicit verification state. Public social posts are discovery leads only; keep UNVERIFIED_SOCIAL until corroborated by an official government source or completed call. Never scrape private groups, personal profiles or personal mobile numbers.
Evacuation records retain facility name/type, PSGC, address, coordinates/capacity when supplied, operational status, source URL/title, verification state/date. Missing values remain null. A completed project is not automatically an open, usable center.

## Barangay CSV
UTF-8 columns: psgc_code,region,province,city_municipality,barangay,urban_rural,source_url,source_version. Every row must identify the official PSA PSGC URL and Q3 2026 release. Importer accepts only a complete 42,010-row Q3-2026 snapshot with unique 10-digit PSGC codes; partial imports are rejected.

## Offline maps
PH-ResQ uses MapLibre Native and OpenFreeMap Liberty. User-triggered downloads obey current limits: <=3,000 tiles or 50 MB per download, <=3 parallel requests; PH-ResQ stops conservatively at 40 MB, unique app User-Agent. Attribution stays visible. Do not bulk-download the Philippines from the public tile service. Nationwide basemap requires separately licensed and built extract/package.

## Acceptance
1. PSGC count and unique codes match PSA.
2. Contact and evacuation coverage are separate from barangay coverage.
3. Every phone has provenance and verification status.
4. Evacuation status/coordinates/capacity are never inferred.
5. Test map download, restart, airplane-mode rendering and deletion on physical devices.


## FOI evidence and source conflict (reviewed 2026-11-13)
- DILG FOI #DILG-902947380736 (12 Feb 2026): DILG says it does not maintain a consolidated master file in the requested format; points users to SubayBayan and states coordinates and PSGC are not available at DILG.
- DILG FOI #DILG-274823337061 (2 Sep 2025): DILG supplied an NCR-only list and said nationwide detailed data were managed by OCD.
- OCD FOI #OCD-397929143969 (18 Feb 2026): OCD says the requested nationwide list was not available at its office and refers requesters to DILG/LGUs.
- DILG FOI #DILG-186294218093 (15 Apr 2019): points to DSWD DROMIC evacuation-center page but warns that its data were from 2016.
These replies conflict on the custodian and demonstrate that there is no single reliable public nationwide file currently confirmed by these FOI responses. The app must therefore show per-LGU coverage and must not advertise complete national evacuation coverage.

## Starter records committed with this branch
- 80 public office numbers from Pasig City (2024), Marinduque provincial/LGU page, and Lapuyan municipal page. All carry source links and are review-due; none is represented as verified by a phone call.
- 28 evacuation-center records from Bay, Laguna Table 130 (as of December 2024). Capacity unit is explicitly FAMILIES; coordinates are null and operational status is UNKNOWN.


## Bundled national PSGC identity reference (implemented)
The app now ships 12 CSV shards under `app/src/main/assets/data/psgc_q3_part_01.csv` through `psgc_q3_part_12.csv`, plus `psgc_metadata.json`.
- 42,010 rows, 42,010 unique 10-digit PSGC codes, zero duplicates, all 18 regions.
- Lineage: pinned third-party PSA-derived snapshot `Tenasia/ph-psgc@2026-Q2` (commit `3d7528b6625e4e7990ac1b96d89505a6bab157ec`), with PSA's sole Q3 2026 name correction applied: PSGC `1001322005`, Cabanglasan, Bukidnon, `Lambangan` → `Lambagan` (PSA ref. 2026-331).
- Official release: https://psa.gov.ph/classification/psgc ; official Q3 workbook: https://psa.gov.ph/system/files/scd/PSGC-3Q-2026-Publication-Datafile.xlsx
- The bundle is for administrative identity and hierarchy. The Q3 workbook also updates urban/rural classifications using the 2024 Census; that field is intentionally null in this bundle because the exact Q3 classification column has not yet been imported. Do not treat the urban/rural field as complete.
- The source is a third-party PSA-derived distributor, not an official PSA API. PSA remains the authority; attribution is retained in the asset metadata.
- The app installs the full reference into SQLite in a background coroutine and links existing barangay office contacts to PSGC codes using normalized name + city matching.

This is **100% barangay identity/code coverage** for the published count, not 100% emergency-contact coverage, evacuation-center coverage, or polygon-boundary coverage.
