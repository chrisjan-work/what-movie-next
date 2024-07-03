package com.lairofpixies.whatmovienext.test

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onNodeWithText
import com.lairofpixies.whatmovienext.database.InternalDatabase
import com.lairofpixies.whatmovienext.database.Movie
import com.lairofpixies.whatmovienext.database.MovieRepository
import com.lairofpixies.whatmovienext.database.WatchState
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
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
        runBlocking {
            withTimeout(1000) {
                composeRuleHolder.composeRule.onNodeWithText(movieTitle).assertExists()
            }
        }
}
