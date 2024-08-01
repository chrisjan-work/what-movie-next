/*
 * This file is part of What Movie Next.
 *
 * Copyright (C) 2024 Christiaan Janssen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.lairofpixies.whatmovienext.stepdefs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.database.data.DbGenre
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieBasic
import com.lairofpixies.whatmovienext.models.network.data.TmdbMovieExtended
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.test.onNodeWithTextUnderTag
import com.lairofpixies.whatmovienext.views.screens.UiTags
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking

@HiltAndroidTest
class SearchStepDefs(
    private val testContext: CucumberTestContext,
) {
    private val composeRule
        get() = testContext.composeRuleHolder.composeRule

    private val editCardStepDefs = EditCardStepDefs(testContext)

    @Given("the online repo is empty")
    fun theOnlineRepoIsEmpty() {
        testContext.movieApi.clearFakeResponses()
    }

    @Given("the online repo returns an entry with title {string}")
    fun theOnlineRepoReturnsAnEntryWithTitle(title: String) {
        testContext.movieApi.appendToFakeMovies(
            TmdbMovieBasic(tmdbId = 1, title = title),
        )
        testContext.movieApi.fakeMovieExtended = {
            TmdbMovieExtended(tmdbId = 1, title = title)
        }
    }

    @Given("the online repo returns an entry with title {string} from {string} and poster {string}")
    fun theOnlineRepoReturnsAnEntryWithTitleFromAndPoster(
        title: String,
        year: String,
        poster: String,
    ) {
        testContext.movieApi.appendToFakeMovies(
            TmdbMovieBasic(
                tmdbId = 1,
                title = title,
                originalTitle = title,
                releaseDate = "$year-01-01", // month and day don't matter
                posterPath = poster,
            ),
        )
    }

    @Given("the configuration contains the genre {string} with id {string}")
    fun theConfigurationContainsTheGenreWithId(
        name: String,
        id: String,
    ) = runBlocking {
        testContext.appDatabase.genreDao().insert(
            listOf(
                DbGenre(
                    tmdbId = id.toLong(),
                    name = name,
                ),
            ),
        )
    }

    @Given("the online repo returns an entry with title {string} and genre id {string}")
    fun theOnlineRepoReturnsAnEntryWithTitleAndGenreId(
        title: String,
        genreId: String,
    ) {
        testContext.movieApi.appendToFakeMovies(
            TmdbMovieBasic(
                tmdbId = 10,
                title = title,
                genreIds = listOf(genreId.toLong()),
            ),
        )
    }

    @Given("the online repo throws an error")
    fun theOnlineRepoThrowsAnError() {
        val exception = Exception("Fake error")
        testContext.movieApi.fakeMoviesBasic = {
            throw exception
        }
        testContext.movieApi.fakeMovieExtended = {
            throw exception
        }
        testContext.movieApi.fakeGenres = {
            throw exception
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
            onNodeWithTag(UiTags.Popups.CONNECTION_FAILED)
                .isDisplayed()
        }

    @Then("the search results contains an entry with title {string}")
    fun theSearchResultsContainsAnEntryWithTitle(title: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(title, UiTags.Screens.SEARCH_RESULTS)
                .isDisplayed()
        }

    @When("the user selects the search result {string}")
    fun theUserSelectsTheSearchResult(title: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(title, UiTags.Screens.SEARCH_RESULTS)
                .performClick()
        }

    @And("the search results are not visible")
    fun theSearchResultsAreNotVisible() =
        composeRule.composeStep {
            onNodeWithTag(UiTags.Screens.SEARCH_RESULTS)
                .assertDoesNotExist()
        }

    @And("the search results contains an entry with title {string} and year {string}")
    fun theSearchResultsContainsAnEntryWithTitleAndYear(
        title: String,
        year: String,
    ) = composeRule.composeStep {
        onNodeWithTextUnderTag(title, UiTags.Screens.SEARCH_RESULTS)
            .onParent()
            .onChildren()
            .filterToOne(hasText(year))
            .assertIsDisplayed()
    }

    @Then("the search results contains an entry with title {string} and genre {string}")
    fun theSearchResultsContainsAnEntryWithTitleAndGenre(
        title: String,
        genre: String,
    ) = composeRule.composeStep {
        onNodeWithTextUnderTag(title, UiTags.Screens.SEARCH_RESULTS)
            .onParent()
            .onChildren()
            .filterToOne(hasText(genre))
            .assertIsDisplayed()
    }

    @And("the online repo returns details for an entry with title {string}")
    fun theOnlineRepoReturnsDetailsForAnEntryWithTitle(title: String) {
        testContext.movieApi.fakeMovieExtended = {
            TmdbMovieExtended(tmdbId = 1, title = title)
        }
    }
}
