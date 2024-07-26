package com.lairofpixies.whatmovienext.stepdefs

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithTag
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.RemoteMovie
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.test.onNodeWithTextUnderTag
import com.lairofpixies.whatmovienext.views.screens.UiTags
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

@HiltAndroidTest
class SearchStepDefs(
    private val testContext: CucumberTestContext,
) {
    private val composeRule
        get() = testContext.composeRuleHolder.composeRule

    private val editCardStepDefs = EditCardStepDefs(testContext)

    @Given("the online repo is empty")
    fun theOnlineRepoIsEmpty() {
        testContext.movieApi.clearFakeResponse()
    }

    @Given("the online repo returns an entry with title {string}")
    fun theOnlineRepoReturnsAnEntryWithTitle(title: String) {
        testContext.movieApi.appendToFakeResponse(
            RemoteMovie(title = title),
        )
    }

    @Given("the online repo throws an error")
    fun theOnlineRepoThrowsAnError() {
        testContext.movieApi.replaceFakeResponse {
            throw Exception("Fake error")
        }
    }

    @When("the user searches for the title {string}")
    fun theUserSearchesForTheTitle(title: String) {
        with(editCardStepDefs) {
            theUserInitiatesANewEntry()
            theUserEntersTheTitle(title)
            theUserClicksOnTheFindButton()
        }
    }

    @Then("the edit card title is filled with {string}")
    fun theEditCardTitleIsFilledWith(title: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(activity.getString(R.string.title), UiTags.Screens.EDIT_CARD)
                .assertTextContains(title)
        }

    @Then("a pop-up is shown informing that no results were found")
    fun aPopUpIsShownInformingThatNoResultsWereFound() =
        composeRule.composeStep {
            onNodeWithTag(UiTags.Popups.SEARCH_EMPTY)
                .isDisplayed()
        }

    @Then("a pop-up is shown informing that an error occurred")
    fun aPopUpIsShownInformingThatAnErrorOccurred() =
        composeRule.composeStep {
            onNodeWithTag(UiTags.Popups.SEARCH_FAILED)
                .isDisplayed()
        }
}
