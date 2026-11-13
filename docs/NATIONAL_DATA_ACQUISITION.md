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
- Geofabrik Philippines OSM extract: https://download.geofabrik.de/asia/philippines.html

## Verification policy
A PSGC row proves administrative identity only. It does not prove a public phone number or evacuation facility exists. Contact entries require public office number, source URL/title and explicit verification state. Public social posts are discovery leads only; keep UNVERIFIED_SOCIAL until corroborated by an official government source or completed call. Never scrape private groups, personal profiles or personal mobile numbers.
Evacuation records retain facility name/type, PSGC, address, coordinates/capacity when supplied, operational status, source URL/title, verification state/date. Missing values remain null. A completed project is not automatically an open, usable center.

## Barangay CSV
UTF-8 columns: psgc_code,region,province,city_municipality,barangay,urban_rural. Importer accepts only a complete 42,010-row Q3-2026 snapshot with unique 10-digit PSGC codes; partial imports are rejected.

## Offline maps
PH-ResQ uses MapLibre Native and OpenFreeMap Liberty. User-triggered downloads obey current limits: <=3,000 tiles or 50 MB per download, <=4 parallel requests, unique app User-Agent. Attribution stays visible. Do not bulk-download the Philippines from the public tile service. Nationwide basemap requires separately licensed and built extract/package.

## Acceptance
1. PSGC count and unique codes match PSA.
2. Contact and evacuation coverage are separate from barangay coverage.
3. Every phone has provenance and verification status.
4. Evacuation status/coordinates/capacity are never inferred.
5. Test map download, restart, airplane-mode rendering and deletion on physical devices.
