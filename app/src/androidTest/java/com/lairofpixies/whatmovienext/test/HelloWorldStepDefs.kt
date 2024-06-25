package com.lairofpixies.whatmovienext.test

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then

@HiltAndroidTest
class HelloWorldStepDefs(
    private val composeRuleHolder: ComposeRuleHolder,
    private val scenarioHolder: ActivityScenarioHolder,
) : SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {
    @Given("the app starts")
    fun theAppStarts() {
        scenarioHolder.launch()
    }

    @Then("the text {string} is displayed")
    fun theTextIsDisplayed(text: String) {
        onNodeWithText(text).assertIsDisplayed()
    }
}
