package ph.resq.app.core

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object RollingPrivacyToken {
    fun create(secret: ByteArray, timestampMillis: Long, windowMillis: Long = 900_000L): String {
        require(secret.size >= 32) { "Secret must be at least 256 bits" }
        require(windowMillis in 60_000L..86_400_000L) { "Unsupported token window" }
        val window = timestampMillis / windowMillis
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret, "HmacSHA256"))
        val digest = mac.doFinal(("PH-ResQ-token-v1:" + window).toByteArray(Charsets.UTF_8))
        return digest.take(12).joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }
}
