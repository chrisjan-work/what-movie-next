package com.lairofpixies.whatmovienext.stepdefs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.test.onNodeWithTextUnderTag
import com.lairofpixies.whatmovienext.views.screens.ArchiveTags
import cucumber.api.PendingException
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

@HiltAndroidTest
class ArchiveStepDefs(
    private val testContext: CucumberTestContext,
) {
    private val composeRule
        get() = testContext.composeRuleHolder.composeRule

    private val movieListStepDefs = MovieListStepDefs(testContext)
    private val editCardStepDefs = EditCardStepDefs(testContext)

    @Then("the entry {string} is visible in the archive")
    fun theEntryIsVisibleInTheArchive(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, ArchiveTags.TAG_ARCHIVE_LIST)
                .assertIsDisplayed()
        }

    @When("the user archives the entry {string}")
    fun theUserArchivesTheEntry(movieTitle: String) {
        with(movieListStepDefs) {
            theUserOpensTheEntry(movieTitle)
            theUserStartsEditingTheEntry()
            editCardStepDefs.theUserArchivesTheCurrentEntry()
            theEntryIsNotVisible(movieTitle)
            theUserNavigatesToTheArchive()
        }
    }

    @And("the user selects the entry {string} in the archive")
    fun theUserSelectsTheEntryInTheArchive(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, ArchiveTags.TAG_ARCHIVE_LIST)
                .performClick()
        }

    @And("the user clicks on the archive action {string}")
    fun theUserClicksOnTheArchiveAction(action: String) =
        composeRule.composeStep {
            val label =
                when (action) {
                    "Delete forever" -> activity.getString(R.string.delete_forever)
                    "Restore" -> activity.getString(R.string.restore)
                    else -> throw PendingException("Action $action is not supported")
                }

            onNodeWithTextUnderTag(label, ArchiveTags.TAG_ARCHIVE_LIST)
                .performClick()
        }

    @Then("the entry {string} is not available in the archive")
    fun theEntryIsNotAvailableInTheArchive(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, ArchiveTags.TAG_ARCHIVE_LIST)
                .assertDoesNotExist()
        }
}
