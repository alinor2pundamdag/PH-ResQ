package ph.resq.app.core

import java.security.MessageDigest

object IntegrityVerifier {
    fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(data)
        return digest.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }

    fun verify(data: ByteArray, expectedSha256: String): Boolean {
        if (!expectedSha256.matches(Regex("^[0-9a-fA-F]{64}$"))) return false
        val expected = expectedSha256.lowercase()
        val actual = sha256Hex(data)
        return MessageDigest.isEqual(expected.toByteArray(Charsets.US_ASCII), actual.toByteArray(Charsets.US_ASCII))
    }
}
