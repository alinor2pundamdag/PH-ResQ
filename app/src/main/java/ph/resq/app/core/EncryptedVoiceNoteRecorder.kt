package ph.resq.app.core

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Records a short local note and encrypts it at rest with an Android Keystore AES-GCM key.
 * Saved recordings can be listed, decrypted only to a temporary cache file for playback, and deleted.
 * Decrypted temporary files must be deleted by the caller after playback.
 */
class EncryptedVoiceNoteRecorder {
    private var recorder: MediaRecorder? = null
    private var rawFile: File? = null
    private var appContext: Context? = null
    private var completion: ((File?) -> Unit)? = null
    private var finishing = false

    companion object {
        fun cleanupStaleRawFiles(context: Context) {
            context.cacheDir.listFiles()
                ?.filter { it.name.startsWith("phresq-voice-") && it.name.endsWith(".m4a") }
                ?.forEach { it.delete() }
            File(context.cacheDir, "voice-share").listFiles()
                ?.filter { it.name.startsWith("phresq-playback-") && it.name.endsWith(".m4a") }
                ?.forEach { it.delete() }
        }
    }

    fun start(context: Context, onFinished: (File?) -> Unit) {
        check(recorder == null) { "Recording already active" }
        val raw = File.createTempFile("phresq-voice-", ".m4a", context.cacheDir)
        val active = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        try {
            active.setAudioSource(MediaRecorder.AudioSource.MIC)
            active.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            active.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            active.setMaxDuration(10_000)
            active.setMaxFileSize(2_000_000L)
            active.setOutputFile(raw.absolutePath)
            active.setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
                    what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) finish()
            }
            active.prepare()
            rawFile = raw
            appContext = context.applicationContext
            completion = onFinished
            finishing = false
            recorder = active
            active.start()
        } catch (error: Exception) {
            runCatching { active.reset() }
            active.release()
            raw.delete()
            recorder = null
            rawFile = null
            appContext = null
            completion = null
            throw error
        }
    }

    fun stop() = finish()

    @Synchronized
    private fun finish() {
        if (finishing) return
        val active = recorder ?: return
        finishing = true
        recorder = null
        var recorded = false
        try {
            active.stop()
            recorded = true
        } catch (_: RuntimeException) {
            recorded = false
        } finally {
            runCatching { active.reset() }
            active.release()
        }
        val raw = rawFile
        val context = appContext
        val callback = completion
        val encrypted = if (recorded && raw != null && context != null) {
            runCatching { encryptFile(context, raw) }.getOrNull()
        } else null
        raw?.delete()
        rawFile = null
        appContext = null
        completion = null
        finishing = false
        callback?.invoke(encrypted)
    }

    private fun encryptFile(context: Context, source: File): File {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(source.readBytes())
        val destination = File(context.filesDir, "voice-note-" + System.currentTimeMillis() + ".enc")
        destination.outputStream().use { output ->
            output.write(cipher.iv)
            output.write(ciphertext)
        }
        return destination
    }

    /** Returns saved encrypted notes newest first. The filename contains no user-provided data. */
    fun listSaved(context: Context): List<File> =
        context.filesDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("voice-note-") && it.name.endsWith(".enc") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

    /** Decrypts a note into cache for MediaPlayer. Caller MUST delete the returned file. */
    fun decryptToTemporaryFile(context: Context, encryptedFile: File): File {
        require(encryptedFile.canonicalFile.parentFile == context.filesDir.canonicalFile) {
            "Voice note must be in the app's private files directory"
        }
        require(encryptedFile.isFile && encryptedFile.name.startsWith("voice-note-") && encryptedFile.name.endsWith(".enc")) {
            "Invalid voice-note file"
        }
        val bytes = encryptedFile.readBytes()
        require(bytes.size > 12) { "Encrypted voice note is truncated" }
        val iv = bytes.copyOfRange(0, 12)
        val ciphertext = bytes.copyOfRange(12, bytes.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), javax.crypto.spec.GCMParameterSpec(128, iv))
        val plaintext = cipher.doFinal(ciphertext)
        val shareCache = File(context.cacheDir, "voice-share").apply { mkdirs() }
        val temp = File.createTempFile("phresq-playback-", ".m4a", shareCache)
        try {
            temp.outputStream().use { it.write(plaintext) }
        } catch (error: Exception) {
            temp.delete()
            throw error
        } finally {
            plaintext.fill(0)
        }
        return temp
    }

    fun deleteSaved(context: Context, encryptedFile: File): Boolean {
        require(encryptedFile.canonicalFile.parentFile == context.filesDir.canonicalFile) {
            "Voice note must be in the app's private files directory"
        }
        require(encryptedFile.name.startsWith("voice-note-") && encryptedFile.name.endsWith(".enc")) {
            "Invalid voice-note file"
        }
        return !encryptedFile.exists() || encryptedFile.delete()
    }

    private fun getOrCreateKey(): SecretKey {
        val alias = "ph_resq_voice_note_aes_gcm_v1"
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }
}
