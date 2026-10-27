package ph.resq.app.core

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Optional PIN-protected local vault. PIN is never persisted. AES-GCM authenticates content.
 * This is not a duress vault and cannot protect against a compromised/unlocked device.
 */
class PinProtectedVault(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ph_resq_pin_vault_v1", Context.MODE_PRIVATE)
    private val random = SecureRandom()
    private val iterations = 210_000
    private val minPinLength = 6
    private val lockoutMs = 30_000L

    fun isConfigured(): Boolean = prefs.contains("salt") && prefs.contains("ciphertext")

    @Synchronized
    fun configure(pin: CharArray, plaintext: String) {
        require(pin.size >= minPinLength && pin.all { it.isDigit() }) { "PIN must contain at least 6 digits." }
        require(!isConfigured()) { "Vault is already configured." }
        val salt = ByteArray(16).also(random::nextBytes)
        val key = derive(pin, salt)
        val iv = ByteArray(12).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        check(prefs.edit()
            .putString("salt", encode(salt))
            .putString("iv", encode(iv))
            .putString("ciphertext", encode(encrypted))
            .putInt("failed_attempts", 0)
            .putLong("locked_until", 0L)
            .commit()) { "Unable to save vault." }
        pin.fill('\u0000')
        key.encoded?.fill(0)
    }

    @Synchronized
    fun unlock(pin: CharArray): String? {
        val now = System.currentTimeMillis()
        if (now < prefs.getLong("locked_until", 0L)) {
            pin.fill('\u0000')
            throw IllegalStateException("Vault temporarily locked. Try again later.")
        }
        val salt = decode(prefs.getString("salt", null) ?: return null)
        val iv = decode(prefs.getString("iv", null) ?: return null)
        val encrypted = decode(prefs.getString("ciphertext", null) ?: return null)
        return try {
            val key = derive(pin, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            val result = cipher.doFinal(encrypted).toString(Charsets.UTF_8)
            prefs.edit().putInt("failed_attempts", 0).putLong("locked_until", 0L).commit()
            result
        } catch (_: AEADBadTagException) {
            val attempts = prefs.getInt("failed_attempts", 0) + 1
            prefs.edit().putInt("failed_attempts", attempts)
                .putLong("locked_until", if (attempts >= 5) now + lockoutMs else 0L).commit()
            null
        } finally {
            pin.fill('\u0000')
        }
    }

    @Synchronized
    fun replace(pin: CharArray, plaintext: String): Boolean {
        // Preserve a temporary copy because unlock deliberately wipes its input array.
        val storedPin = pin.copyOf()
        return try {
            val old = unlock(pin)
            if (old == null) return false
            check(prefs.edit().remove("salt").remove("iv").remove("ciphertext").commit())
            configure(storedPin, plaintext)
            true
        } finally {
            pin.fill('\\u0000')
            storedPin.fill('\\u0000')
        }
    }

    private fun derive(pin: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(pin, salt, iterations, 256)
        return try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            SecretKeySpec(bytes, "AES").also { bytes.fill(0) }
        } finally { spec.clearPassword() }
    }

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)
}
