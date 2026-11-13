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

## Verification policy
A PSGC row proves administrative identity only. It does not prove a public phone number or evacuation facility exists. Contact entries require public office number, source URL/title and explicit verification state. Public social posts are discovery leads only; keep UNVERIFIED_SOCIAL until corroborated by an official government source or completed call. Never scrape private groups, personal profiles or personal mobile numbers.
Evacuation records retain facility name/type, PSGC, address, coordinates/capacity when supplied, operational status, source URL/title, verification state/date. Missing values remain null. A completed project is not automatically an open, usable center.

## Barangay CSV
UTF-8 columns: psgc_code,region,province,city_municipality,barangay,urban_rural,source_url,source_version. Every row must identify the official PSA PSGC URL and Q3 2026 release. Importer accepts only a complete 42,010-row Q3-2026 snapshot with unique 10-digit PSGC codes; partial imports are rejected.

## Offline maps
PH-ResQ uses MapLibre Native and OpenFreeMap Liberty for user-selected regional offline packs. User-triggered downloads obey the provider's current limits: <=3,000 tiles or 50 MB per download, <=3 parallel requests; PH-ResQ stops conservatively at 40 MB and uses a unique app User-Agent. Attribution stays visible. Do not bulk-download the Philippines from the public tile service.

The nationwide vector basemap uses the official Mapsforge v5 prebuilt Philippines map: https://download.mapsforge.org/maps/v5/asia/philippines.map (the public index listed 563 MB and a 2026-10-06 modification date when audited). GitHub Actions downloads it over HTTPS, computes a PH-ResQ SHA-256 sidecar, and publishes a replaceable `offline-map-latest` release. The Android app downloads the release asset, verifies SHA-256 before installation, and renders it with Mapsforge 0.25.0. The map is OSM basemap data under ODbL; it is not an authoritative barangay-boundary or emergency-facility dataset. Refresh workflow is monthly.

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
- 192 public office numbers are bundled: the original 102 records plus 43 national agency hotlines from the official eHotlines government portal, 46 Quezon Province DRRMO/police/fire records from the provincial government page, and one Laguna PDRRMO number published by Siniloan LGU. All new records retain source URLs, are marked official-source (not phone-verified), and remain review-due. The Quezon page's Mulanay number is shown as a 9-digit value and was excluded rather than guessed.
- 28 evacuation-center records from Bay, Laguna Table 130 (as of December 2024). Capacity unit is explicitly FAMILIES; coordinates are null and operational status is UNKNOWN.


## Bundled national PSGC identity reference (implemented)
The app now ships 12 CSV shards under `app/src/main/assets/data/psgc_q3_part_01.csv` through `psgc_q3_part_12.csv`, plus `psgc_metadata.json`.
- 42,010 rows, 42,010 unique 10-digit PSGC codes, zero duplicates, all 18 regions.
- Lineage: pinned third-party PSA-derived snapshot `Tenasia/ph-psgc@2026-Q2` (commit `3d7528b6625e4e7990ac1b96d89505a6bab157ec`), with PSA's sole Q3 2026 name correction applied: PSGC `1001322005`, Cabanglasan, Bukidnon, `Lambangan` → `Lambagan` (PSA ref. 2026-331).
- Official PSGC landing page: https://psa.gov.ph/classification/psgc ; official Q3 correction notice: https://psa.gov.ph/content/third-quarter-2026-psgc-updates-correction-name-one-barangay
- The bundle is for administrative identity and hierarchy. The Q3 workbook also updates urban/rural classifications using the 2024 Census; that field is intentionally null in this bundle because the exact Q3 classification column has not yet been imported. Do not treat the urban/rural field as complete.
- The source is a third-party PSA-derived distributor, not an official PSA API. PSA remains the authority; attribution is retained in the asset metadata.
- The app installs the full reference into SQLite in a background coroutine and links existing barangay office contacts to PSGC codes using normalized name + city matching.

This is **100% barangay identity/code coverage** for the published count, not 100% emergency-contact coverage, evacuation-center coverage, or polygon-boundary coverage.


## Additional official emergency hotlines (implemented)
The starter contact pack was expanded from 80 to 102 records. Added:
- Quezon City Helpline 122, QCDRRMO and Emergency Operations Center numbers from the Quezon City Government.
- Davao City DRRMO, Disaster Radio and BFP numbers from Davao City Online Services.
- Makati MDRRMO number from the Makati City Government directory.
- National Unified 911 reference from the DILG press release carried by PIA.

All added records remain review-due until independently call-tested. The Quezon City 122 short code is explicitly accepted by the phone validator.


## Quezon City evacuation facilities (implemented)
The bundled evacuation seed now combines:
- Bay, Laguna: 28 official-listed facilities from the municipal source table.
- Quezon City: 401 distinct barangay/facility pairs from the Quezon City Government's "QC Evacuation Centers for Flood and Fire" page, covering 140 barangay labels across districts 1–6. Exact duplicate barangay/facility pairs were removed.

The Quezon City page states it was last updated on 2026-02-19. It supplies facility names grouped by barangay, but does not publish capacities, coordinates, or current operational status. The app therefore stores the barangay as a locator, leaves coordinates and capacity null, sets operational status to UNKNOWN, and leaves verifiedAt null. These entries are official-source-listed, not call-verified or confirmed open.

Combined bundled evacuation seed: 429 records across two LGUs. This is not nationwide coverage. Repeated identical facility names in a barangay may represent separate sites that the source does not distinguish; the deduplication rule may therefore undercount those ambiguous duplicates.

## Official-source contact expansion (2026-11-13)
- Official national portal: https://ehotlines.e.gov.ph/ — added NDRRMC, DSWD, Red Cross, PNP, BFP, Coast Guard, MMDA, DOTr, LTO, LTFRB, PAGASA, PHIVOLCS, utilities, VAWC and NCMH numbers. The existing 911 record remains the single national emergency hotline entry.
- Quezon Provincial Government: https://quezon.gov.ph/quezon-province-emergency-hotlines/ — added the provincial DRRMO, listed municipal DRRMOs, provincial police and fire contacts. The Mulanay line is malformed (9 digits) and intentionally excluded; no digits were inferred.
- Siniloan LGU: https://siniloanlaguna.gov.ph/emergency-hotlines/ — added the published Laguna PDRRMO contact.
- These are official-source records, not call-verified numbers. All carry `expires_at=0` and must remain visibly review-due until a documented verification call or cross-check is recorded.
- Municipality/province contacts are not linked to barangay PSGC codes. Barangay contact coverage counts only records with `scope=BARANGAY`, not municipality/province contacts.
- National contact import and evacuation-center import now validate/insert each full batch inside a SQLite transaction; a rejected row rolls back the whole batch. Insert failures are checked rather than silently counted as success.
