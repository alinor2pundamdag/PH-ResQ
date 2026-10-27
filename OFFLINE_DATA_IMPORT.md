# Offline JSON Import Guide

The app's **Import Verified Offline JSON** button opens Android's document picker and imports one dataset into the local cache. It does not download data or verify a digital signature. Fields such as `sourceUrl`, `license`, `version`, and `verifiedAt` are supplied by the file and can be forged.

## Before importing

1. Obtain the file from an official, trusted publisher and check its HTTPS origin independently.
2. Confirm redistribution/use rights and the dataset license.
3. Check the publication, verification, and expiry dates, geographic coverage, and responsible agency.
4. Keep a trusted original copy and do not treat an import success message as proof that the data is authentic or complete.
5. Import only JSON files up to 10 MiB. The importer validates required fields, enum values, coordinates, record bounds, and repository-specific constraints before replacing the corresponding local dataset.

Imports replace the matching cache transactionally; they do not merge records. Directory metadata is stored with the dataset. Advisory and evacuation records retain their own source/timestamp fields. There is no signed manifest, automated rollback/download service, or live PAGASA/LGU feed in this build.

## Supported JSON schemas

### Emergency directory

Root fields: `type` = `directory`, `sourceUrl` (HTTPS), `license`, `version`, `verifiedAt` (Unix epoch milliseconds), and non-empty `records`.

```json
{
  "type": "directory",
  "sourceUrl": "https://example.gov.ph/directory",
  "license": "Publisher's stated license",
  "version": "2026-10-27",
  "verifiedAt": 1793059200000,
  "records": [
    {
      "id": "hospital-001",
      "name": "Example District Hospital",
      "category": "hospital",
      "phone": "911",
      "latitude": 7.1,
      "longitude": 125.6
    }
  ]
}
```

Each record requires unique `id`, `name`, and `category`. `phone`, `latitude`, and `longitude` are optional; coordinates must be supplied as a valid pair.

### Hazard advisories

Root fields: `type` = `advisories` and non-empty `records`. Every record requires `id`, `title`, `severity` (`INFO`, `WATCH`, `WARNING`, or `EMERGENCY`), `body`, HTTPS `sourceUrl`, and `publishedAt`, `fetchedAt`, `expiresAt` timestamps in Unix epoch milliseconds.

### Evacuation centres

Root fields: `type` = `evacuation_centers` and non-empty `records`. Every record requires `id`, `name`, `address`, `status` (`OPEN`, `LIMITED`, `FULL`, `CLOSED`, or `UNKNOWN`), HTTPS `sourceUrl`, and `verifiedAt` in Unix epoch milliseconds. Optional coordinates must be a valid latitude/longitude pair.

## Known limitations

- Source metadata is not a cryptographic signature and can be forged.
- No nationwide dataset is bundled or certified by this importer.
- Imported data can be stale, incomplete, duplicated across sources, or unsuitable for a user's current jurisdiction.
- Search is local-only; no live update, signed manifest, automatic rollback, or server sync is implemented.
- The current UI shows stale labels for advisories and evacuation-centre records; users should verify critical details with official local authorities.
