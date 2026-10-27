package ph.resq.app.core

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PinProtectedVaultTest {
    @Test fun configuredVaultDecryptsOnlyWithCorrectPin() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("ph_resq_pin_vault_v1", Context.MODE_PRIVATE).edit().clear().commit()
        val vault = PinProtectedVault(context)
        vault.configure("482951".toCharArray(), "emergency contacts")
        assertTrue(vault.isConfigured())
        assertNull(vault.unlock("111111".toCharArray()))
        assertEquals("emergency contacts", vault.unlock("482951".toCharArray()))
    }

    @Test fun fiveWrongPinsTemporarilyLockVault() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("ph_resq_pin_vault_v1", Context.MODE_PRIVATE).edit().clear().commit()
        val vault = PinProtectedVault(context)
        vault.configure("482951".toCharArray(), "private")
        repeat(5) { assertNull(vault.unlock("111111".toCharArray())) }
        try {
            vault.unlock("482951".toCharArray())
            fail("Expected lockout")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message!!.contains("locked"))
        }
    }
}
