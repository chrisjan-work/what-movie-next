package com.lairofpixies.whatmovienext.test

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.lairofpixies.whatmovienext.MainActivity
import io.cucumber.junit.WithJunitRule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Rule

@WithJunitRule
class ComposeRuleHolder {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    fun waitUntilPassing(
        timeoutMillis: Long = 5000,
        intervalMillis: Long = 100,
        assertion: () -> Unit,
    ) {
        composeRule.waitUntil(timeoutMillis) {
            try {
                runBlocking {
                    withTimeout(intervalMillis) {
                        assertion()
                    }
                }
                true
            } catch (e: Throwable) {
                false
            }
        }
    }
}
