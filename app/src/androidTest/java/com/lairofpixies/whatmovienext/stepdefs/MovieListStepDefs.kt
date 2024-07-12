package com.lairofpixies.whatmovienext.stepdefs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.WatchState
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.test.onNodeWithTextUnderTag
import com.lairofpixies.whatmovienext.viewmodel.ListMode
import com.lairofpixies.whatmovienext.views.screens.DetailScreenTags
import com.lairofpixies.whatmovienext.views.screens.MovieListTags
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking

@HiltAndroidTest
class MovieListStepDefs(
    private val testContext: CucumberTestContext,
) {
    private val composeRule
        get() = testContext.composeRuleHolder.composeRule

    @Given("a list with an entry {string}")
    fun aListWithAnEntry(movieTitle: String) {
        runBlocking {
            testContext.appDatabase
                .movieDao()
                .insertMovie(Movie(title = movieTitle, watchState = WatchState.PENDING))
        }
        composeRule.waitForIdle()
    }

    @Given("a list with an entry {string} that is marked as watched")
    fun aListWithAnEntryThatIsMarkedAsWatched(movieTitle: String) {
        runBlocking {
            testContext.appDatabase
                .movieDao()
                .insertMovie(Movie(title = movieTitle, watchState = WatchState.WATCHED))
        }
        composeRule.waitForIdle()
    }

    @Given("an empty list of films")
    fun anEmptyListOfFilms() {
        testContext.appDatabase.clearAllTables()
        composeRule.waitForIdle()
    }

    @Then("the entry {string} is visible")
    fun theEntryIsVisible(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, MovieListTags.TAG_MOVIE_LIST)
                .assertIsDisplayed()
        }

    @When("the user opens the entry {string}")
    fun theUserOpensTheEntry(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, MovieListTags.TAG_MOVIE_LIST)
                .performClick()
        }

    @Then("the card containing the information of {string} should be visible")
    fun theCardContainingTheInformationOfShouldBeVisible(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, DetailScreenTags.TAG_MOVIE_CARD)
                .assertIsDisplayed()
        }

    @When("the user archives the current entry")
    fun theUserArchivesTheCurrentEntry() =
        composeRule.composeStep {
            val archiveLabel = activity.getString(com.lairofpixies.whatmovienext.R.string.archive)
            onNodeWithTextUnderTag(archiveLabel, DetailScreenTags.TAG_MOVIE_CARD)
                .performClick()
        }

    @Then("the entry {string} is not available")
    fun theEntryIsNotVisible(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, MovieListTags.TAG_MOVIE_LIST)
                .assertDoesNotExist()
        }

    @Then("the entry in the details view is marked as pending")
    fun theEntryInTheDetailsViewIsMarkedAsPending() =
        composeRule.composeStep {
            onNodeWithTag(DetailScreenTags.TAG_WATCH_STATE_SWITCH)
                .assertIsOff()
        }

    @And("the list is in mode {string}")
    fun theListIsInMode(expectedMode: String) =
        composeRule.composeStep {
            var attempts = ListMode.entries.size

            // click the button until it displays the desired mode
            val buttonNode = onNodeWithTag(MovieListTags.TAG_MODE_BUTTON)
            while (attempts > 0) {
                attempts--
                try {
                    buttonNode.assertTextContains(expectedMode)
                    return@composeStep
                } catch (_: Throwable) {
                    buttonNode.performClick()
                    waitForIdle()
                }
            }

            buttonNode.assertTextContains(expectedMode)
        }

    @And("the user marks the entry as watched")
    fun theUserMarksTheEntryAsWatched() =
        composeRule.composeStep {
            try {
                onNodeWithTag(DetailScreenTags.TAG_WATCH_STATE_SWITCH).assertIsOff().performClick()
            } catch (_: Throwable) {
                // it was already on
            }
        }

    @And("the user navigates back to the list")
    fun theUserNavigatesBackToTheList() =
        composeRule.composeStep {
            val closeLabel = activity.getString(com.lairofpixies.whatmovienext.R.string.close)
            onNodeWithTextUnderTag(closeLabel, DetailScreenTags.TAG_MOVIE_CARD)
                .performClick()
        }

    @When("the user marks the entry as pending")
    fun theUserMarksTheEntryAsPending() =
        composeRule.composeStep {
            try {
                onNodeWithTag(DetailScreenTags.TAG_WATCH_STATE_SWITCH)
                    .assertIsOn()
                    .performClick()
            } catch (_: Throwable) {
                // it was already off
            }
        }

    @Given("a list with {int} entries, titled {string} where {string} is the index")
    fun aListWithEntriesTitledWhereIsTheIndex(
        amount: Int,
        titlePattern: String,
        indexPattern: String,
    ) {
        val movieList =
            (1..amount)
                .map { index ->
                    titlePattern.replace(indexPattern, index.toString())
                }.map {
                    Movie(title = it)
                }

        runBlocking {
            testContext.appDatabase.movieDao().insertMovies(movieList)
        }
        composeRule.waitForIdle()
    }

    @When("the user scrolls down to {string}")
    fun theUserScrollsDownTo(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTag(MovieListTags.TAG_MOVIE_LIST)
                .performScrollToNode(hasText(movieTitle))
        }

    @Then("the list view is visible")
    fun theListViewIsVisible() =
        composeRule.composeStep {
            onNodeWithTag(MovieListTags.TAG_MOVIE_LIST)
                .assertIsDisplayed()
        }

    @When("the user starts editing the entry")
    fun theUserStartsEditingTheEntry() =
        composeRule.composeStep {
            val editLabel = activity.getString(com.lairofpixies.whatmovienext.R.string.edit)
            onNodeWithTextUnderTag(editLabel, DetailScreenTags.TAG_MOVIE_CARD)
                .performClick()
        }

    @Given("a card opened with the entry {string}")
    fun aCardOpenedWithTheEntry(movieTitle: String) {
        aListWithAnEntry(movieTitle)
        theUserOpensTheEntry(movieTitle)
        theCardContainingTheInformationOfShouldBeVisible(movieTitle)
    }
}
