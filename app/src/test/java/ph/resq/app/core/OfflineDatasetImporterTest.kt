package ph.resq.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineDatasetImporterTest {
    @Test fun parsesDirectoryDatasetAndRecordMetadata() {
        val parsed = OfflineDatasetImporter.parseJson(
            """{"type":"directory","sourceUrl":"https://example.gov.ph/directory","license":"CC BY 4.0","version":"2026-10","verifiedAt":1793059200000,"records":[{"id":"hospital-1","name":"District Hospital","category":"hospital","phone":"911","latitude":7.1,"longitude":125.6}]}"""
        ) as OfflineDatasetImporter.ParsedDataset.Directory
        assertEquals("2026-10", parsed.metadata.version)
        assertEquals("District Hospital", parsed.records.single().name)
        assertEquals(7.1, parsed.records.single().latitude!!, 0.0001)
    }

    @Test fun parsesAdvisoriesAndEvacuationStatusEnums() {
        val advisory = OfflineDatasetImporter.parseJson(
            """{"type":"advisories","records":[{"id":"a1","title":"Heavy rain","severity":"warning","body":"Check local updates","sourceUrl":"https://example.gov.ph/alert","publishedAt":1793059200000,"fetchedAt":1793059200000,"expiresAt":1793062800000}]}"""
        ) as OfflineDatasetImporter.ParsedDataset.Advisories
        assertEquals(AdvisorySeverity.WARNING, advisory.records.single().severity)

        val evacuation = OfflineDatasetImporter.parseJson(
            """{"type":"evacuation_centers","records":[{"id":"e1","name":"Municipal Gym","address":"Main Road","latitude":7.1,"longitude":125.6,"status":"open","sourceUrl":"https://example.gov.ph/evac","verifiedAt":1793059200000}]}"""
        ) as OfflineDatasetImporter.ParsedDataset.EvacuationCenters
        assertEquals(EvacuationStatus.OPEN, evacuation.records.single().status)
    }

    @Test fun rejectsUnknownTypeMissingFieldsAndOversizedJson() {
        assertTrue(runCatching { OfflineDatasetImporter.parseJson("""{"type":"unknown","records":[]}""") }.isFailure)
        assertTrue(runCatching { OfflineDatasetImporter.parseJson("""{"type":"directory","records":[{"id":"x"}]}""") }.isFailure)
        assertTrue(runCatching { OfflineDatasetImporter.parseJson(" ".repeat(10 * 1024 * 1024 + 1)) }.isFailure)
    }
}
