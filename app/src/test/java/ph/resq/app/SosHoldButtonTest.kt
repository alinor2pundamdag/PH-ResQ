package ph.resq.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SosHoldButtonTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shortTapCancelsWithoutPreparingSos() {
        var confirmed = false
        composeRule.setContent {
            MaterialTheme {
                SosHoldButton { confirmed = true }
            }
        }
        composeRule.onNodeWithText("HOLD FOR 2 SECONDS — PREPARE SOS")
            .performTouchInput {
                down(center)
                up()
            }
        composeRule.waitForIdle()
        assertFalse("A short tap must never prepare an SOS.", confirmed)
    }
}
