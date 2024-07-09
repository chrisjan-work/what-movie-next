package com.lairofpixies.whatmovienext.test

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.lairofpixies.whatmovienext.MainActivity
import io.cucumber.junit.WithJunitRule
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Rule

typealias ComposeRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

@WithJunitRule
class ComposeRuleHolder {
    @get:Rule
    val composeRule: ComposeRule = createAndroidComposeRule<MainActivity>()

    fun composeStep(
        timeoutMillis: Long = 5000,
        intervalMillis: Long = 1000,
        stepCode: ComposeRule.() -> Unit,
    ) {
        var problem: Throwable? = null
        try {
            composeRule.waitUntil(timeoutMillis) {
                try {
                    runTest {
                        withTimeout(intervalMillis) {
                            composeRule.stepCode()
                            composeRule.waitForIdle()
                        }
                    }
                    true
                } catch (throwable: Throwable) {
                    problem = throwable
                    false
                }
            }
        } catch (timeoutException: ComposeTimeoutException) {
            throw problem ?: timeoutException
        }
    }
}
