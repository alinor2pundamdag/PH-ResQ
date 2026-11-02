package ph.resq.app.core

import android.content.Context
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ConsentStoreTest {
    private fun context(): Context = RuntimeEnvironment.getApplication()

    @Test fun acceptanceIsVersionedAndPersistsAcrossStoreInstances() {
        val context = context()
        context.getSharedPreferences("ph_resq_consent", Context.MODE_PRIVATE).edit().clear().commit()
        val store = ConsentStore(context)
        assertFalse(store.isAccepted())
        store.setAccepted(true)
        assertTrue(ConsentStore(context).isAccepted())
        assertTrue(store.acceptedAtMillis() > 0L)
    }

    @Test fun decliningOrRevokingConsentClearsStoredAcceptance() {
        val context = context()
        context.getSharedPreferences("ph_resq_consent", Context.MODE_PRIVATE).edit().clear().commit()
        val store = ConsentStore(context)
        store.setAccepted(true)
        store.setAccepted(false)
        assertFalse(store.isAccepted())
        assertFalse(ConsentStore(context).isAccepted())
    }

    @Test fun acceptanceForAnOlderPolicyVersionDoesNotGrantAccess() {
        val context = context()
        context.getSharedPreferences("ph_resq_consent", Context.MODE_PRIVATE).edit()
            .putString("policy_version", "2026-10-27-v1").putLong("accepted_at", 1L).commit()
        assertFalse(ConsentStore(context).isAccepted())
    }
}