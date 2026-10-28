package ph.resq.app.core

import android.content.Context

/** Stores an explicit acceptance for the exact notice version currently shown by the app. */
class ConsentStore(context: Context) {
    private val preferences = context.getSharedPreferences("ph_resq_consent", Context.MODE_PRIVATE)

    companion object {
        const val CURRENT_POLICY_VERSION = "2026-10-27-v1"
    }

    fun isAccepted(): Boolean =
        preferences.getString("policy_version", null) == CURRENT_POLICY_VERSION &&
            preferences.getLong("accepted_at", 0L) > 0L

    fun acceptedAtMillis(): Long = preferences.getLong("accepted_at", 0L)

    fun setAccepted(accepted: Boolean) {
        val editor = preferences.edit()
        if (accepted) {
            editor.putString("policy_version", CURRENT_POLICY_VERSION)
                .putLong("accepted_at", System.currentTimeMillis())
                .putBoolean("consent_v1_accepted", true)
        } else {
            editor.remove("policy_version")
                .remove("accepted_at")
                .remove("consent_v1_accepted")
        }
        check(editor.commit()) { "Unable to persist consent choice" }
    }
}
