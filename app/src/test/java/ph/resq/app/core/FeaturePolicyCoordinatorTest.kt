package ph.resq.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeaturePolicyCoordinatorTest {
    private fun profile(states: Map<String, CapabilityState>) = DeviceCapabilityProfile(
        sdkInt = 35,
        androidRelease = "15",
        manufacturer = "Test",
        model = "Device",
        supportedAbis = listOf("arm64-v8a"),
        availableStorageBytes = 1024L,
        batterySaverEnabled = false,
        capabilities = states.mapValues { (id, state) ->
            DeviceCapability(id, state, "TEST_$id")
        }
    )

    @Test fun deniedLocationKeepsManualFallbackAndNeverClaimsFreshGps() {
        val policies = FeaturePolicyCoordinator.evaluate(profile(mapOf("location" to CapabilityState.DENIED)))
        val location = policies.first { it.featureId == "sos_location" }
        assertEquals(FeatureAvailability.DEGRADED, location.availability)
        assertEquals("last_known_or_manual_location", location.effectiveMode)
    }

    @Test fun bluetoothDiscoveryNeverClaimsMeshTransport() {
        val policies = FeaturePolicyCoordinator.evaluate(profile(mapOf("ble" to CapabilityState.AVAILABLE)))
        val ble = policies.first { it.featureId == "ble_discovery" }
        assertEquals(FeatureAvailability.READY, ble.availability)
        assertTrue(ble.reason.contains("no packet transfer"))
    }

    @Test fun unavailableKeystoreBlocksProtectedDataWithoutPlaintextFallback() {
        val policies = FeaturePolicyCoordinator.evaluate(profile(mapOf("keystore" to CapabilityState.ERROR)))
        val protectedData = policies.first { it.featureId == "protected_data" }
        assertEquals(FeatureAvailability.BLOCKED, protectedData.availability)
        assertEquals("fail_closed_no_plaintext_fallback", protectedData.effectiveMode)
    }

    @Test fun wifiDirectIsBlockedUntilTransportActuallyExists() {
        val policies = FeaturePolicyCoordinator.evaluate(profile(mapOf("wifi_direct" to CapabilityState.AVAILABLE)))
        assertEquals(FeatureAvailability.BLOCKED, policies.first { it.featureId == "wifi_direct_transfer" }.availability)
    }
}
