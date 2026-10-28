package ph.resq.app.core

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONObject

/** Creates a local QR payload only from fields the user explicitly chooses to enter. */
object MedicalIdQr {
    fun payload(name: String, bloodType: String, allergies: String, medications: String, emergencyContact: String): String {
        val cleanName = name.trim().take(80)
        require(cleanName.isNotEmpty()) { "Maglagay ng pangalan bago gumawa ng Medical ID QR." }
        val json = JSONObject()
            .put("schema", "PH-ResQ-MED-ID-v1")
            .put("name", cleanName)
            .put("bloodType", bloodType.trim().take(20))
            .put("allergies", allergies.trim().take(160))
            .put("medications", medications.trim().take(160))
            .put("emergencyContact", emergencyContact.trim().take(40))
        val value = json.toString()
        require(value.length <= 700) { "Masyadong mahaba ang Medical ID QR payload." }
        return value
    }

    fun createBitmap(name: String, bloodType: String, allergies: String, medications: String, emergencyContact: String, size: Int = 512): Bitmap {
        require(size in 256..1024) { "QR size must be between 256 and 1024 pixels." }
        val value = payload(name, bloodType, allergies, medications, emergencyContact)
        val matrix = MultiFormatWriter().encode(
            value, BarcodeFormat.QR_CODE, size, size,
            mapOf(EncodeHintType.MARGIN to 1, EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M)
        )
        val pixels = IntArray(size * size)
        for (y in 0 until size) for (x in 0 until size) {
            pixels[y * size + x] = if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
        }
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also {
            it.setPixels(pixels, 0, size, 0, 0, size, size)
        }
    }
}
