package com.lairofpixies.whatmovienext.test

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.After
import io.cucumber.java.Before

@HiltAndroidTest
class CucumberHooks(
    private val testContext: CucumberTestContext,
) : SemanticsNodeInteractionsProvider by testContext.composeRuleHolder.composeRule {
    @Before
    fun setUp() {
        testContext.scenarioHolder.launch()
    }

    @After
    fun tearDown() {
        testContext.appDatabase.close()
    }
}
