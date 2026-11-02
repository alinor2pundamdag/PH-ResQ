package ph.resq.app.core

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MedicalIdQrTest {
    @Test fun payloadContainsOnlyExplicitFieldsAndVersion() {
        val payload = MedicalIdQr.payload(" Ana ", "O+", "Penicillin", "", "+639171234567")
        val json = JSONObject(payload)
        assertEquals("PH-ResQ-MED-ID-v1", json.getString("schema"))
        assertEquals("Ana", json.getString("name"))
        assertEquals("O+", json.getString("bloodType"))
        assertEquals("Penicillin", json.getString("allergies"))
        assertEquals("", json.getString("medications"))
        assertEquals("+639171234567", json.getString("emergencyContact"))
    }

    @Test fun nameIsRequired() {
        try {
            MedicalIdQr.payload("  ", "", "", "", "")
            fail("Expected validation error")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message!!.contains("pangalan"))
        }
    }
}
