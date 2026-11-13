package ph.resq.app.core

import android.content.Context
import org.json.JSONObject

data class MedicalIdProfile(
    val name: String,
    val bloodType: String,
    val allergies: String,
    val medications: String,
    val emergencyContact: String
)

/**
 * Stores the user's optional Medical ID fields encrypted with an Android Keystore AES-GCM key.
 * The QR generated from these fields is intentionally separate and remains readable plaintext.
 */
class MedicalIdStore(
    context: Context,
    private val cipher: EncryptedFieldCipher = EncryptedFieldCipher("ph_resq_medical_id_aes_gcm_v1")
) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun hasStoredProfile(): Boolean = preferences.contains(KEY_CIPHERTEXT)

    /** Returns null when no profile exists or when the Keystore key/ciphertext cannot be recovered. */
    fun load(): MedicalIdProfile? {
        val encrypted = preferences.getString(KEY_CIPHERTEXT, null) ?: return null
        val plaintext = runCatching { cipher.decrypt(encrypted) }.getOrNull() ?: return null
        return runCatching {
            val json = JSONObject(plaintext)
            require(json.optString("schema") == SCHEMA) { "Unsupported Medical ID profile format." }
            MedicalIdProfile(
                name = json.optString("name"),
                bloodType = json.optString("blood_type"),
                allergies = json.optString("allergies"),
                medications = json.optString("medications"),
                emergencyContact = json.optString("emergency_contact")
            ).also(::validate)
        }.getOrNull()
    }

    fun save(profile: MedicalIdProfile) {
        validate(profile)
        require(!hasStoredProfile() || load() != null) {
            "An existing encrypted Medical ID cannot be decrypted. Delete it explicitly before replacing it."
        }
        val payload = JSONObject()
            .put("schema", SCHEMA)
            .put("name", profile.name.trim())
            .put("blood_type", profile.bloodType.trim())
            .put("allergies", profile.allergies.trim())
            .put("medications", profile.medications.trim())
            .put("emergency_contact", profile.emergencyContact.trim())
        val encrypted = cipher.encrypt(payload.toString())
        check(preferences.edit().putString(KEY_CIPHERTEXT, encrypted).commit()) {
            "Could not persist the encrypted Medical ID on this device."
        }
    }

    /** Explicit deletion only; no automatic overwrite of unreadable ciphertext. */
    fun clear() {
        check(preferences.edit().remove(KEY_CIPHERTEXT).commit()) {
            "Could not delete the saved Medical ID."
        }
    }

    private fun validate(profile: MedicalIdProfile) {
        require(profile.name.isNotBlank() && profile.name.length <= 80) { "Name is required and must be 80 characters or fewer." }
        require(profile.bloodType.length <= 20) { "Blood type is too long." }
        require(profile.allergies.length <= 160) { "Allergies field is too long." }
        require(profile.medications.length <= 160) { "Medications field is too long." }
        require(profile.emergencyContact.length <= 40) { "Emergency contact is too long." }
        require(profile.emergencyContact.isBlank() || profile.emergencyContact.matches(Regex("[+0-9() .,-]{5,40}"))) {
            "Emergency contact must be a phone number."
        }
    }

    private companion object {
        const val PREFERENCES = "ph_resq_medical_id_v1"
        const val KEY_CIPHERTEXT = "profile_ciphertext"
        const val SCHEMA = "PH-ResQ-MED-ID-v1"
    }
}
