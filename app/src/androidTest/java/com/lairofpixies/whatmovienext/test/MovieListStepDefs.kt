package com.lairofpixies.whatmovienext.test

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.Given

@HiltAndroidTest
class MovieListStepDefs(
    private val composeRuleHolder: ComposeRuleHolder,
    private val scenarioHolder: ActivityScenarioHolder,
) : SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {
    @Given("the app starts")
    fun theAppStarts() {
        scenarioHolder.launch()
    }
}
