package ph.resq.app.core

import android.Manifest
import android.bluetooth.BluetoothManager
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.os.StatFs
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.security.KeyStore

enum class CapabilityState {
    AVAILABLE,
    DENIED,
    DISABLED,
    UNSUPPORTED,
    RESTRICTED_BY_SYSTEM,
    TEMPORARILY_UNAVAILABLE,
    ERROR
}

data class DeviceCapability(
    val id: String,
    val state: CapabilityState,
    val reasonCode: String,
    val requiredPermission: String? = null
)

data class DeviceCapabilityProfile(
    val sdkInt: Int,
    val androidRelease: String,
    val manufacturer: String,
    val model: String,
    val supportedAbis: List<String>,
    val availableStorageBytes: Long,
    val batterySaverEnabled: Boolean,
    val capabilities: Map<String, DeviceCapability>
) {
    fun capability(id: String): DeviceCapability =
        capabilities[id] ?: DeviceCapability(id, CapabilityState.ERROR, "CAPABILITY_NOT_REGISTERED")

    companion object {
        fun detect(context: Context): DeviceCapabilityProfile {
            val app = context.applicationContext
            val pm = app.packageManager
            fun granted(permission: String): Boolean =
                ContextCompat.checkSelfPermission(app, permission) == PackageManager.PERMISSION_GRANTED
            fun feature(name: String): Boolean = runCatching { pm.hasSystemFeature(name) }.getOrDefault(false)
            fun cap(id: String, state: CapabilityState, reason: String, permission: String? = null) =
                DeviceCapability(id, state, reason, permission)

            val telephony = if (feature(PackageManager.FEATURE_TELEPHONY)) {
                cap("telephony", CapabilityState.AVAILABLE, "TELEPHONY_HARDWARE_PRESENT")
            } else cap("telephony", CapabilityState.UNSUPPORTED, "NO_TELEPHONY_HARDWARE")

            val ble = when {
                !feature(PackageManager.FEATURE_BLUETOOTH_LE) ->
                    cap("ble", CapabilityState.UNSUPPORTED, "NO_BLE_HARDWARE")
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !granted(Manifest.permission.BLUETOOTH_SCAN) ->
                    cap("ble", CapabilityState.DENIED, "BLUETOOTH_SCAN_PERMISSION_REQUIRED", Manifest.permission.BLUETOOTH_SCAN)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !granted(Manifest.permission.BLUETOOTH_CONNECT) ->
                    cap("ble", CapabilityState.DENIED, "BLUETOOTH_CONNECT_PERMISSION_REQUIRED", Manifest.permission.BLUETOOTH_CONNECT)
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !granted(Manifest.permission.ACCESS_FINE_LOCATION) ->
                    cap("ble", CapabilityState.DENIED, "LOCATION_PERMISSION_REQUIRED_FOR_BLE_SCAN", Manifest.permission.ACCESS_FINE_LOCATION)
                else -> {
                    val adapter = runCatching {
                        (app.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
                    }.getOrNull()
                    when {
                        adapter == null -> cap("ble", CapabilityState.UNSUPPORTED, "BLE_ADAPTER_UNAVAILABLE")
                        else -> try {
                            if (adapter.isEnabled) cap("ble", CapabilityState.AVAILABLE, "BLE_READY")
                            else cap("ble", CapabilityState.DISABLED, "BLUETOOTH_DISABLED")
                        } catch (_: SecurityException) {
                            cap("ble", CapabilityState.DENIED, "BLUETOOTH_CONNECT_PERMISSION_REVOKED", Manifest.permission.BLUETOOTH_CONNECT)
                        }
                    }
                }
            }

            val wifiDirect = if (feature(PackageManager.FEATURE_WIFI_DIRECT)) {
                cap("wifi_direct", CapabilityState.AVAILABLE, "WIFI_DIRECT_FEATURE_DECLARED")
            } else cap("wifi_direct", CapabilityState.UNSUPPORTED, "NO_WIFI_DIRECT_FEATURE")

            val locationManager = app.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            val locationEnabled = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    locationManager?.isLocationEnabled == true
                } else {
                    locationManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == true ||
                        locationManager?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) == true
                }
            }.getOrDefault(false)
            val location = when {
                !granted(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    !granted(Manifest.permission.ACCESS_FINE_LOCATION) ->
                    cap("location", CapabilityState.DENIED, "LOCATION_PERMISSION_REQUIRED", Manifest.permission.ACCESS_COARSE_LOCATION)
                !locationEnabled ->
                    cap("location", CapabilityState.DISABLED, "DEVICE_LOCATION_SERVICES_DISABLED")
                else -> cap("location", CapabilityState.AVAILABLE, "LOCATION_PERMISSION_AND_PROVIDER_READY")
            }

            val microphone = if (!feature(PackageManager.FEATURE_MICROPHONE)) {
                cap("microphone", CapabilityState.UNSUPPORTED, "NO_MICROPHONE_HARDWARE")
            } else if (!granted(Manifest.permission.RECORD_AUDIO)) {
                cap("microphone", CapabilityState.DENIED, "MICROPHONE_PERMISSION_REQUIRED", Manifest.permission.RECORD_AUDIO)
            } else cap("microphone", CapabilityState.AVAILABLE, "MICROPHONE_READY")

            val notifications = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !granted(Manifest.permission.POST_NOTIFICATIONS) ->
                    cap("notifications", CapabilityState.DENIED, "POST_NOTIFICATIONS_PERMISSION_REQUIRED", Manifest.permission.POST_NOTIFICATIONS)
                !runCatching { NotificationManagerCompat.from(app).areNotificationsEnabled() }.getOrDefault(false) ->
                    cap("notifications", CapabilityState.DISABLED, "APP_NOTIFICATIONS_DISABLED_IN_SYSTEM_SETTINGS")
                else -> cap("notifications", CapabilityState.AVAILABLE, "NOTIFICATIONS_ENABLED")
            }

            val keystore = if (runCatching {
                    KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                }.isSuccess) {
                cap("keystore", CapabilityState.AVAILABLE, "ANDROID_KEYSTORE_PROVIDER_AVAILABLE")
            } else cap("keystore", CapabilityState.ERROR, "ANDROID_KEYSTORE_PROVIDER_UNAVAILABLE")

            val backgroundRestricted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && runCatching {
                (app.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.isBackgroundRestricted == true
            }.getOrDefault(false)
            val backgroundExecution = if (backgroundRestricted) {
                cap("background_execution", CapabilityState.RESTRICTED_BY_SYSTEM, "BACKGROUND_EXECUTION_RESTRICTED_BY_SYSTEM")
            } else {
                cap("background_execution", CapabilityState.AVAILABLE, "NO_BACKGROUND_RESTRICTION_REPORTED")
            }

            val storage = runCatching { StatFs(app.filesDir.absolutePath).availableBytes }.getOrDefault(0L)
            val powerSave = runCatching {
                (app.getSystemService(Context.POWER_SERVICE) as? PowerManager)?.isPowerSaveMode ?: false
            }.getOrDefault(false)

            return DeviceCapabilityProfile(
                sdkInt = Build.VERSION.SDK_INT,
                androidRelease = Build.VERSION.RELEASE ?: "unknown",
                manufacturer = Build.MANUFACTURER ?: "unknown",
                model = Build.MODEL ?: "unknown",
                supportedAbis = Build.SUPPORTED_ABIS.toList(),
                availableStorageBytes = storage,
                batterySaverEnabled = powerSave,
                capabilities = listOf(telephony, ble, wifiDirect, location, microphone, notifications, keystore, backgroundExecution)
                    .associateBy { it.id }
            )
        }
    }
}
