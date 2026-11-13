package ph.resq.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w320dp-h480dp")
class ConsentScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun actionButtonsRemainVisibleWhilePrivacyNoticeScrollsOnCompactScreen() {
        var accepted = false
        var declined = false
        composeRule.setContent {
            MaterialTheme {
                ConsentScreen(
                    onAccept = { accepted = true },
                    onDecline = { declined = true }
                )
            }
        }

        val accept = composeRule.onNodeWithText("ACCEPT — Nauunawaan ko at magpatuloy")
        val decline = composeRule.onNodeWithText("DECLINE — Withdraw consent and exit")
        accept.assertIsDisplayed()
        decline.assertIsDisplayed()

        val coverageHeading = composeRule.onNodeWithText("DATA COVERAGE AT LIMITASYON")
        coverageHeading.performScrollTo()
        coverageHeading.assertIsDisplayed()
        accept.assertIsDisplayed()
        decline.assertIsDisplayed()
        assertFalse(accepted)
        assertFalse(declined)
    }

    @Test
    fun acceptOnlyRunsWhenAcceptButtonIsPressed() {
        var accepted = false
        var declined = false
        composeRule.setContent {
            MaterialTheme {
                ConsentScreen(
                    onAccept = { accepted = true },
                    onDecline = { declined = true }
                )
            }
        }

        composeRule.onNodeWithText("ACCEPT — Nauunawaan ko at magpatuloy").performClick()
        assertTrue(accepted)
        assertFalse(declined)
    }

    @Test
    fun declineOnlyRunsWhenDeclineButtonIsPressed() {
        var accepted = false
        var declined = false
        composeRule.setContent {
            MaterialTheme {
                ConsentScreen(
                    onAccept = { accepted = true },
                    onDecline = { declined = true }
                )
            }
        }

        composeRule.onNodeWithText("DECLINE — Withdraw consent and exit").performClick()
        assertFalse(accepted)
        assertTrue(declined)
    }
}
