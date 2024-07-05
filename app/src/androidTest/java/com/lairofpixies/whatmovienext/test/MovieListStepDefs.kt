package com.lairofpixies.whatmovienext.test

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lairofpixies.whatmovienext.database.InternalDatabase
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.MovieRepository
import com.lairofpixies.whatmovienext.database.WatchState
import com.lairofpixies.whatmovienext.viewmodel.ListMode
import com.lairofpixies.whatmovienext.views.DetailScreenTags
import com.lairofpixies.whatmovienext.views.MovieListTags
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidTest
class MovieListStepDefs(
    private val composeRuleHolder: ComposeRuleHolder,
    private val scenarioHolder: ActivityScenarioHolder,
) : SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {
    @Inject
    lateinit var appDatabase: InternalDatabase

    @Inject
    lateinit var movieRepository: MovieRepository

    @Before
    fun setUp() {
        scenarioHolder.launch()
    }

    @After
    fun tearDown() {
        appDatabase.close()
    }

    @Given("a list with an entry {string}")
    fun aListWithAnEntry(movieTitle: String) {
        runBlocking {
            appDatabase
                .movieDao()
                .insertMovies(listOf(Movie(title = movieTitle, watchState = WatchState.PENDING)))
        }
        composeRuleHolder.composeRule.waitForIdle()
    }

    @Given("a list with an entry {string} that is marked as watched")
    fun aListWithAnEntryThatIsMarkedAsWatched(movieTitle: String) {
        runBlocking {
            appDatabase
                .movieDao()
                .insertMovies(listOf(Movie(title = movieTitle, watchState = WatchState.WATCHED)))
        }
        composeRuleHolder.composeRule.waitForIdle()
    }

    @Given("an empty list of films")
    fun anEmptyListOfFilms() {
        appDatabase.clearAllTables()
        composeRuleHolder.composeRule.waitForIdle()
    }

    @When("the user creates a new entry with the title {string}")
    fun theUserCreatesANewEntryWithTheTitle(movieTitle: String) {
        // TODO: add the proper ui interaction
        movieRepository.addMovie(movieTitle)
    }

    @Then("the list should contain an entry with the title {string}")
    fun theListShouldContainAnEntryWithTheTitle(movieTitle: String) =
        composeRuleHolder.composeStep {
            onNodeWithTag(MovieListTags.TAG_MOVIE_LIST)
                .onChildren()
                .filterToOne(hasText(movieTitle))
                .assertIsDisplayed()
        }

    @When("the user opens the entry {string}")
    fun theUserOpensTheEntry(movieTitle: String) =
        composeRuleHolder.composeStep {
            onNodeWithTag(MovieListTags.TAG_MOVIE_LIST)
                .onChildren()
                .filterToOne(hasText(movieTitle))
                .assertIsDisplayed()
                .performClick()
        }

    @Then("the card containing the information of {string} should be visible")
    fun theCardContainingTheInformationOfShouldBeVisible(movieTitle: String) =
        composeRuleHolder.composeStep {
            onNodeWithTag(DetailScreenTags.TAG_MOVIE_CARD)
                .onChildren()
                .filterToOne(hasText(movieTitle))
                .assertIsDisplayed()
        }

    @When("the user archives the current entry")
    fun theUserArchivesTheCurrentEntry() =
        composeRuleHolder.composeStep {
            val archiveLabel = activity.getString(com.lairofpixies.whatmovienext.R.string.archive)
            onNodeWithTag(DetailScreenTags.TAG_MOVIE_CARD)
                .onChildren()
                .filterToOne(hasText(archiveLabel))
                .assertIsDisplayed()
                .performClick()
        }

    @Then("the list should not contain an entry with the title {string}")
    fun theListShouldNotContainAnEntryWithTheTitle(movieTitle: String) =
        composeRuleHolder.composeStep {
            onNodeWithTag(MovieListTags.TAG_MOVIE_LIST)
                .onChildren()
                .filterToOne(hasText(movieTitle))
                .assertDoesNotExist()
        }

    @Then("the entry in the details view is marked as pending")
    fun theEntryInTheDetailsViewIsMarkedAsPending() =
        composeRuleHolder.composeStep {
            onNodeWithTag(DetailScreenTags.TAG_WATCH_STATE_SWITCH)
                .assertIsOff()
        }

    @And("the list is in mode {string}")
    fun theListIsInMode(expectedMode: String) =
        composeRuleHolder.composeStep {
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
        composeRuleHolder.composeStep {
            try {
                onNodeWithTag(DetailScreenTags.TAG_WATCH_STATE_SWITCH).assertIsOff().performClick()
            } catch (_: Throwable) {
                // it was already on
            }
        }

    @And("the user navigates back to the list")
    fun theUserNavigatesBackToTheList() =
        composeRuleHolder.composeStep {
            onNodeWithTag(DetailScreenTags.TAG_MOVIE_CARD).assertIsDisplayed()
            onNodeWithText(activity.getString(com.lairofpixies.whatmovienext.R.string.close)).performClick()
        }

    @When("the user marks the entry as pending")
    fun theUserMarksTheEntryAsPending() =
        composeRuleHolder.composeStep {
            try {
                onNodeWithTag(DetailScreenTags.TAG_WATCH_STATE_SWITCH).assertIsOn().performClick()
            } catch (_: Throwable) {
                // it was already off
            }
        }
}
