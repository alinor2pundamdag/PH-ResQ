package com.phresq.vanguard.data.mesh

import android.telephony.SmsManager
import android.util.Log

class SmsFallbackManager {

    companion object {
        private const val TAG = "SmsFallbackManager"
        private const val EMERGENCY_HOTLINE_PH = "911" // Philippine Emergency Hotline / MDRRMO
    }

    fun sendSosSms(phoneNumber: String = EMERGENCY_HOTLINE_PH, message: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            Log.i(TAG, "Emergency SOS SMS dispatched successfully to $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send emergency SMS: ${e.localizedMessage}")
        }
    }
}
