package com.lairofpixies.whatmovienext.stepdefs

import com.lairofpixies.whatmovienext.test.CucumberTestContext
import cucumber.api.PendingException
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.Given
import io.cucumber.java.en.When

@HiltAndroidTest
class SearchStepDefs(
    private val testContext: CucumberTestContext,
) {
    private val composeRule
        get() = testContext.composeRuleHolder.composeRule

    private val editCardStepDefs = EditCardStepDefs(testContext)

    @Given("the online repo contains an entry with title {string}")
    fun theOnlineRepoContainsAnEntryWithTitle(title: String) {
        // Write code here that turns the phrase above into concrete actions
        throw PendingException()
    }

    @When("the user searches for the title {string}")
    fun theUserSearchesForTheTitle(title: String) {
        with(editCardStepDefs) {
            theUserInitiatesANewEntry()
            theUserEntersTheTitle(title)
            theUserClicksOnTheFindButton()
        }
    }
}
