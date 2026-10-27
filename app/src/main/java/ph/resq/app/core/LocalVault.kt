package ph.resq.app.core

import android.content.Context

/**
 * Compatibility storage adapter for emergency contacts.
 * Encryption is centralized in EncryptedFieldCipher so the app has one AES-GCM implementation.
 * This remains an encrypted local field store, not a PIN vault or recovery system.
 */
class LocalVault(context: Context) {
    private val prefs = context.getSharedPreferences("ph_resq_encrypted_local", Context.MODE_PRIVATE)
    private val cipher = EncryptedFieldCipher("ph_resq_contacts_aes_gcm_v1")

    fun saveContacts(plainText: String) {
        check(prefs.edit().putString("contacts_ciphertext", cipher.encrypt(plainText)).commit()) {
            "Unable to persist encrypted emergency contacts"
        }
    }

    fun loadContacts(): String {
        val encrypted = prefs.getString("contacts_ciphertext", null) ?: return ""
        return cipher.decrypt(encrypted) ?: ""
    }
}
