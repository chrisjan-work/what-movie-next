package com.lairofpixies.whatmovienext.stepdefs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.views.screens.EditableDetailScreenTags
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
            onNodeWithText(activity.getString(R.string.title))
                .performTextClearance()
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
    }
}
