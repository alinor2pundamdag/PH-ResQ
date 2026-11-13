package ph.resq.app.core

enum class FeatureAvailability { READY, DEGRADED, BLOCKED }

data class EffectiveFeaturePolicy(
    val featureId: String,
    val availability: FeatureAvailability,
    val effectiveMode: String,
    val reason: String,
    val requiredPermission: String? = null
)

/**
 * Derives safe feature behavior from device facts. It never changes a user's settings,
 * requests permissions, or reports an unimplemented transport as ready.
 */
object FeaturePolicyCoordinator {
    fun evaluate(profile: DeviceCapabilityProfile): List<EffectiveFeaturePolicy> {
        fun state(id: String) = profile.capability(id)
        val location = state("location")
        val ble = state("ble")
        val microphone = state("microphone")
        val notifications = state("notifications")
        val keystore = state("keystore")
        val telephony = state("telephony")
        val wifi = state("wifi_direct")

        return listOf(
            EffectiveFeaturePolicy(
                "sos_location",
                if (location.state == CapabilityState.AVAILABLE) FeatureAvailability.READY else FeatureAvailability.DEGRADED,
                if (location.state == CapabilityState.AVAILABLE) "fresh_or_last_known_location" else "last_known_or_manual_location",
                if (location.state == CapabilityState.AVAILABLE) "Location permission is granted; freshness and accuracy still need to be checked." else "Location is unavailable or permission is denied; show last-known age/accuracy or allow manual location.",
                location.requiredPermission
            ),
            EffectiveFeaturePolicy(
                "ble_discovery",
                if (ble.state == CapabilityState.AVAILABLE) FeatureAvailability.READY else FeatureAvailability.BLOCKED,
                if (ble.state == CapabilityState.AVAILABLE) "user_started_low_power_discovery_only" else "discovery_unavailable",
                if (ble.state == CapabilityState.AVAILABLE) "BLE discovery only; no packet transfer or mesh delivery is implemented." else "BLE hardware, radio state or runtime permission is unavailable.",
                ble.requiredPermission
            ),
            EffectiveFeaturePolicy(
                "voice_note",
                if (microphone.state == CapabilityState.AVAILABLE) FeatureAvailability.READY else FeatureAvailability.BLOCKED,
                if (microphone.state == CapabilityState.AVAILABLE) "encrypted_local_voice_note" else "text_only_sos",
                if (microphone.state == CapabilityState.AVAILABLE) "Microphone available; recording remains user-initiated." else "Voice note unavailable; the SOS text workflow remains available.",
                microphone.requiredPermission
            ),
            EffectiveFeaturePolicy(
                "sos_status",
                if (notifications.state == CapabilityState.AVAILABLE) FeatureAvailability.READY else FeatureAvailability.DEGRADED,
                if (notifications.state == CapabilityState.AVAILABLE) "persistent_notification_when_started_in_allowed_state" else "in_app_status_only",
                if (notifications.state == CapabilityState.AVAILABLE) "Notification permission/channel available; Android may still apply foreground-service restrictions." else "Notification permission or channel unavailable; do not block SOS preparation and show in-app status.",
                notifications.requiredPermission
            ),
            EffectiveFeaturePolicy(
                "protected_data",
                if (keystore.state == CapabilityState.AVAILABLE) FeatureAvailability.READY else FeatureAvailability.BLOCKED,
                if (keystore.state == CapabilityState.AVAILABLE) "keystore_backed_encryption" else "fail_closed_no_plaintext_fallback",
                if (keystore.state == CapabilityState.AVAILABLE) "Android Keystore provider is available; key lifecycle still requires device validation." else "Protected data operations must fail closed; never fall back to plaintext."
            ),
            EffectiveFeaturePolicy(
                "messaging_handoff",
                FeatureAvailability.DEGRADED,
                if (telephony.state == CapabilityState.AVAILABLE) "user_confirmed_resolver_check" else "share_or_copy_fallback",
                if (telephony.state == CapabilityState.AVAILABLE) "Telephony hardware is only a hint; verify that a messaging app resolves and let the user confirm. SIM, carrier service and delivery are not guaranteed." else "No telephony hardware detected; retain the SOS draft and offer copy/share fallback."
            ),
            EffectiveFeaturePolicy(
                "wifi_direct_transfer",
                FeatureAvailability.BLOCKED,
                "not_implemented_local_queue_only",
                if (wifi.state == CapabilityState.AVAILABLE) "Hardware feature detected, but Wi-Fi Direct transfer is not implemented." else "Wi-Fi Direct is unavailable on this device and transfer is not implemented."
            ),
            EffectiveFeaturePolicy(
                "background_execution",
                if (state("background_execution").state == CapabilityState.RESTRICTED_BY_SYSTEM) FeatureAvailability.DEGRADED else FeatureAvailability.READY,
                if (state("background_execution").state == CapabilityState.RESTRICTED_BY_SYSTEM) "user_visible_foreground_work_only" else "platform_managed_background_limits",
                if (state("background_execution").state == CapabilityState.RESTRICTED_BY_SYSTEM) "Android reports background execution restrictions; deferred work may be delayed or stopped." else "Background execution remains subject to Android and OEM battery policies; no delivery guarantee is implied."
            )
        )
    }
}
