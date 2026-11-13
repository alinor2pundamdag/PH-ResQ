package ph.resq.app.core

import android.content.Context
import javax.crypto.spec.SecretKeySpec
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MedicalIdStoreTest {
    private lateinit var context: Context
    private lateinit var store: MedicalIdStore
    private val preferencesName = "ph_resq_medical_id_v1"
    private val key = SecretKeySpec(ByteArray(32) { 0x33 }, "AES")

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit().clear().commit()
        store = MedicalIdStore(context, EncryptedFieldCipher(testKeyProvider = { key }))
    }

    @After
    fun tearDown() {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun encryptedProfileSurvivesStoreRecreationWithoutPlaintextPreferences() {
        val profile = MedicalIdProfile("Test Patient", "O+", "Peanuts", "Medication A", "+639171234567")
        store.save(profile)

        val stored = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
            .getString("profile_ciphertext", null)
        assertTrue(store.hasStoredProfile())
        assertTrue(stored != null && !stored.contains("Test Patient") && !stored.contains("Peanuts"))

        val restored = MedicalIdStore(context, EncryptedFieldCipher(testKeyProvider = { key })).load()
        assertEquals(profile, restored)
    }

    @Test
    fun unreadableExistingCiphertextCannotBeSilentlyOverwritten() {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
            .edit().putString("profile_ciphertext", "not-valid-ciphertext").commit()
        assertNull(store.load())
        assertTrue(store.hasStoredProfile())
        assertTrue(runCatching { store.save(MedicalIdProfile("New", "", "", "", "")) }.isFailure)
        assertTrue(store.hasStoredProfile())
        store.clear()
        assertFalse(store.hasStoredProfile())
        store.save(MedicalIdProfile("New", "", "", "", ""))
        assertEquals("New", store.load()?.name)
    }

    @Test
    fun invalidEmergencyContactIsRejected() {
        assertTrue(runCatching { store.save(MedicalIdProfile("Test", "", "", "", "not a phone")) }.isFailure)
        assertFalse(store.hasStoredProfile())
    }
}
