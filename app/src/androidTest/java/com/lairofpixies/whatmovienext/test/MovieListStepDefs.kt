package com.lairofpixies.whatmovienext.test

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.lairofpixies.whatmovienext.database.InternalDatabase
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.MovieRepository
import com.lairofpixies.whatmovienext.database.WatchState
import com.lairofpixies.whatmovienext.views.DetailScreenTags
import com.lairofpixies.whatmovienext.views.MovieListTags
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
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

    private val composeRule
        get() = composeRuleHolder.composeRule

    @Before
    fun setUp() {
        scenarioHolder.launch()
    }

    @After
    fun tearDown() {
        appDatabase.close()
    }

    @Given("a list with an entry {string}")
    fun aListWithAnEntry(movieTitle: String): Unit =
        runBlocking {
            appDatabase
                .movieDao()
                .insertMovies(listOf(Movie(title = movieTitle, watchState = WatchState.PENDING)))
        }

    @Given("an empty list of films")
    fun anEmptyListOfFilms() {
        appDatabase.clearAllTables()
    }

    @When("the user creates a new entry with the title {string}")
    fun theUserCreatesANewEntryWithTheTitle(movieTitle: String) {
        // TODO: add the proper ui interaction
        movieRepository.addMovie(movieTitle)
    }

    @Then("the list should contain an entry with the title {string}")
    fun theListShouldContainAnEntryWithTheTitle(movieTitle: String) =
        runTest {
            withTimeout(TIMEOUT) {
                composeRule
                    .onNodeWithTag(MovieListTags.TAG_MOVIE_LIST)
                    .onChildren()
                    .filterToOne(hasText(movieTitle))
                    .assertIsDisplayed()
            }
        }

    @When("the user opens the entry {string}")
    fun theUserOpensTheEntry(movieTitle: String) =
        runTest {
            withTimeout(TIMEOUT) {
                composeRule
                    .onNodeWithTag(MovieListTags.TAG_MOVIE_LIST)
                    .onChildren()
                    .filterToOne(hasText(movieTitle))
                    .assertIsDisplayed()
                    .performClick()
            }
        }

    @Then("the card containing the information of {string} should be visible")
    fun theCardContainingTheInformationOfShouldBeVisible(movieTitle: String) {
        composeRule
            .onNodeWithTag(DetailScreenTags.TAG_MOVIE_CARD)
            .onChildren()
            .filterToOne(hasText(movieTitle))
            .isDisplayed()
    }

    companion object {
        const val TIMEOUT = 1000L
    }
}
