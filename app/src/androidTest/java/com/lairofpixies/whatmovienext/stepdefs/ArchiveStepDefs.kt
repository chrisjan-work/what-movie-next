package com.lairofpixies.whatmovienext.stepdefs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.test.onNodeWithTextUnderTag
import com.lairofpixies.whatmovienext.views.screens.UiTags
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
            onNodeWithTextUnderTag(movieTitle, UiTags.Screens.ARCHIVE)
                .assertIsDisplayed()
        }

    @When("the user archives the entry {string}")
    fun theUserArchivesTheEntry(movieTitle: String) {
        with(movieListStepDefs) {
            theUserOpensTheEntry(movieTitle)
            theUserStartsEditingTheEntry()
            editCardStepDefs.theUserArchivesTheCurrentEntry()
            theEntryIsNotVisible(movieTitle)
        }
    }

    @And("the user selects the entry {string} in the archive")
    fun theUserSelectsTheEntryInTheArchive(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, UiTags.Screens.ARCHIVE)
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

            onNodeWithTextUnderTag(label, UiTags.Screens.ARCHIVE)
                .performClick()
        }

    @Then("the entry {string} is not available in the archive")
    fun theEntryIsNotAvailableInTheArchive(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, UiTags.Screens.ARCHIVE)
                .assertDoesNotExist()
        }

    @Then("a pop-up asks for confirmation for deleting the entry")
    fun aPopUpAsksForConfirmationForDeletingTheEntry() =
        composeRule.composeStep {
            onNodeWithTag(UiTags.Popups.CONFIRM_DELETION)
                .performClick()
        }

    @When("the user selects {string} in the deletion pop-up")
    fun theUserSelectsInTheDeletionPopUp(deletionConfirmation: String) {
        composeRule.composeStep {
            val label =
                when (deletionConfirmation) {
                    "Confirm" -> activity.getString(R.string.confirm_deletion)
                    "Cancel" -> activity.getString(R.string.cancel)
                    else -> throw PendingException("Unknown option: $deletionConfirmation")
                }

            onNodeWithTextUnderTag(label, UiTags.Popups.CONFIRM_DELETION)
                .performClick()
        }
    }

    @Then("the archive shortcut is not available")
    fun theArchiveShortcutIsNotAvailable() =
        composeRule.composeStep {
            onNodeWithText(activity.getString(R.string.archive))
                .assertDoesNotExist()
        }
}
