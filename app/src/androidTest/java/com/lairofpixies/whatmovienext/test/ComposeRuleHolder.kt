package com.lairofpixies.whatmovienext.test

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.lairofpixies.whatmovienext.MainActivity
import io.cucumber.junit.WithJunitRule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Rule

typealias ComposeRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

@WithJunitRule
class ComposeRuleHolder {
    @get:Rule
    val composeRule: ComposeRule = createAndroidComposeRule<MainActivity>()

    fun composeStep(
        timeoutMillis: Long = 5000,
        intervalMillis: Long = 100,
        stepCode: ComposeRule.() -> Unit,
    ) {
        composeRule.waitUntil(timeoutMillis) {
            try {
                runBlocking {
                    withTimeout(intervalMillis) {
                        composeRule.stepCode()
                        composeRule.waitForIdle()
                    }
                }
                true
            } catch (e: Throwable) {
                false
            }
        }
    }
}
