package com.lairofpixies.whatmovienext.stepdefs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.views.screens.EditableDetailScreenTags
import cucumber.api.PendingException
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

@HiltAndroidTest
class MovieCardStepDefs(
    private val testContext: CucumberTestContext,
) {
    private val composeRule
        get() = testContext.composeRuleHolder.composeRule

    @When("the user initiates a new entry")
    fun theUserInitiatesANewEntry() =
        composeRule.composeStep {
            onNodeWithContentDescription(activity.getString(R.string.add_new_movie))
                .performClick()
        }

    @Then("An editable movie details view is open")
    fun anEditableMovieDetailsViewIsOpen() =
        composeRule.composeStep {
            onNodeWithTag(EditableDetailScreenTags.TAG_EDITABLE_MOVIE_CARD)
                .isDisplayed()
        }

    @And("the title input is focused")
    fun theTitleInputIsFocused() =
        composeRule.composeStep {
            onNodeWithText(activity.getString(R.string.title))
                .assertIsFocused()
        }

    @When("the user enters the title {string}")
    fun theUserEntersTheTitle(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithText(activity.getString(R.string.title))
                .performTextClearance()

            onNodeWithText(activity.getString(R.string.title))
                .performTextInput(movieTitle)
        }

    @And("the user saves the entry")
    fun theUserSavesTheEntry() =
        composeRule.composeStep {
            onNodeWithText(activity.getString(R.string.save_and_close))
                .performClick()
        }

    @And("the edited title is empty")
    fun theEditedTitleIsEmpty() =
        composeRule.composeStep {
            onNodeWithText(activity.getString(R.string.title)).apply {
                performTextClearance()
                performImeAction()
            }
        }

    @Then("an error message is displayed indicating that the title is required")
    fun anErrorMessageIsDisplayedIndicatingThatTheTitleIsRequired() =
        composeRule.composeStep {
            onNodeWithText(activity.getString(R.string.error_title_is_required))
                .assertIsDisplayed()
        }

    @When("the user creates a new entry with the title {string}")
    fun theUserCreatesANewEntryWithTheTitle(movieTitle: String) {
        theUserInitiatesANewEntry()
        theUserEntersTheTitle(movieTitle)
        theUserSavesTheEntry()
        theUserNavigatesBack()
    }

    @Then("an alert message gives the user the option to save or discard the changes")
    fun anAlertMessageGivesTheUserTheOptionToSaveOrDiscardTheChanges() =
        composeRule.composeStep {
            onNodeWithText(activity.getString(R.string.warning_changes_not_saved))
                .assertIsDisplayed()
        }

    @And("the user navigates back")
    fun theUserNavigatesBack() =
        composeRule.composeStep {
            activity.runOnUiThread {
                activity.onBackPressedDispatcher.onBackPressed()
            }
        }

    @When("the user selects the option {string}")
    fun theUserSelectsTheOption(option: String) =
        composeRule.composeStep {
            val label =
                when (option) {
                    "Save" -> activity.getString(R.string.save)
                    "Discard" -> activity.getString(R.string.discard)
                    "Continue Editing" -> activity.getString(R.string.continue_editing)
                    else -> throw PendingException("Unknown option: $option")
                }

            onNodeWithText(label).performClick()
        }

    @And("an alert messages giving the user the option to save or discard the changes is gone")
    fun anAlertMessagesGivingTheUserTheOptionToSaveOrDiscardTheChangesIsGone() =
        composeRule.composeStep {
            onNodeWithText(activity.getString(R.string.warning_changes_not_saved))
                .assertDoesNotExist()
        }

    @Then("the edit view is visible")
    fun theEditViewIsVisible() =
        composeRule.composeStep {
            onNodeWithTag(EditableDetailScreenTags.TAG_EDITABLE_MOVIE_CARD)
                .assertIsDisplayed()
        }

    @Then("an error message is displayed indicating that the entry already exists")
    fun anErrorMessageIsDisplayedIndicatingThatTheEntryAlreadyExists() =
        composeRule.composeStep {
            onNodeWithText(activity.getString(R.string.error_title_already_exists))
                .assertIsDisplayed()
        }
}
