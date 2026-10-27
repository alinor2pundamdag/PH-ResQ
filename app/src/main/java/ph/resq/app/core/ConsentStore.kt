package ph.resq.app.core

import android.content.Context

class ConsentStore(context: Context) {
    private val preferences = context.getSharedPreferences("ph_resq_consent", Context.MODE_PRIVATE)
    fun isAccepted(): Boolean = preferences.getBoolean("consent_v1_accepted", false)
    fun setAccepted(accepted: Boolean) {
        preferences.edit().putBoolean("consent_v1_accepted", accepted).apply()
    }
}
