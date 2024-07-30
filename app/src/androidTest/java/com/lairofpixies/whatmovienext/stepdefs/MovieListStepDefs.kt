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
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.data.DbMovie
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.test.onNodeWithTextUnderTag
import com.lairofpixies.whatmovienext.views.screens.UiTags
import com.lairofpixies.whatmovienext.views.state.ListMode
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
                .insertMovie(DbMovie(title = movieTitle, watchState = WatchState.PENDING))
        }
        composeRule.waitForIdle()
    }

    @Given("a list with an entry {string} that is marked as watched")
    fun aListWithAnEntryThatIsMarkedAsWatched(movieTitle: String) {
        runBlocking {
            testContext.appDatabase
                .movieDao()
                .insertMovie(DbMovie(title = movieTitle, watchState = WatchState.WATCHED))
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
            onNodeWithTextUnderTag(movieTitle, UiTags.Screens.MOVIE_LIST)
                .assertIsDisplayed()
        }

    @When("the user opens the entry {string}")
    fun theUserOpensTheEntry(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, UiTags.Screens.MOVIE_LIST)
                .performClick()
        }

    @Then("the card containing the information of {string} should be visible")
    fun theCardContainingTheInformationOfShouldBeVisible(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, UiTags.Screens.MOVIE_CARD)
                .assertIsDisplayed()
        }

    @Then("the entry {string} is not available")
    fun theEntryIsNotVisible(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, UiTags.Screens.MOVIE_LIST)
                .assertDoesNotExist()
        }

    @Then("the entry in the card view is marked as pending")
    fun theEntryInTheCardViewIsMarkedAsPending() =
        composeRule.composeStep {
            val label = activity.getString(R.string.to_watch)
            onNodeWithText(label).assertIsDisplayed()
        }

    @And("the list is in mode {string}")
    fun theListIsInMode(expectedMode: String) =
        composeRule.composeStep {
            var attempts = ListMode.entries.size

            // click the button until it displays the desired mode
            val buttonNode = onNodeWithTag(UiTags.Buttons.LIST_MODE)
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
                val label = activity.getString(R.string.to_watch)
                onNodeWithText(label).performClick()
            } catch (_: Throwable) {
                // it was already on
            }
        }

    @And("the user navigates to the list")
    fun theUserNavigatesToTheList() =
        composeRule.composeStep {
            val home = activity.getString(R.string.movies)
            onNodeWithText(home)
                .performClick()
        }

    @When("the user marks the entry as pending")
    fun theUserMarksTheEntryAsPending() =
        composeRule.composeStep {
            try {
                val label = activity.getString(R.string.seen)
                onNodeWithText(label).performClick()
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
                    DbMovie(title = it)
                }

        runBlocking {
            testContext.appDatabase.movieDao().insertMovies(movieList)
        }
        composeRule.waitForIdle()
    }

    @When("the user scrolls down to {string}")
    fun theUserScrollsDownTo(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTag(UiTags.Screens.MOVIE_LIST)
                .performScrollToNode(hasText(movieTitle))
        }

    @Then("the list view is visible")
    fun theListViewIsVisible() =
        composeRule.composeStep {
            onNodeWithTag(UiTags.Screens.MOVIE_LIST)
                .assertIsDisplayed()
        }

    @When("the user starts editing the entry")
    fun theUserStartsEditingTheEntry() =
        composeRule.composeStep {
            val editLabel = activity.getString(com.lairofpixies.whatmovienext.R.string.edit)
            onNodeWithTextUnderTag(editLabel, UiTags.Screens.MOVIE_CARD)
                .performClick()
        }

    @Given("a card opened with the entry {string}")
    fun aCardOpenedWithTheEntry(movieTitle: String) {
        aListWithAnEntry(movieTitle)
        theUserOpensTheEntry(movieTitle)
        theCardContainingTheInformationOfShouldBeVisible(movieTitle)
    }

    @Then("the card {string} is visible and the list contains this entry")
    fun theCardIsVisibleAndTheListContainsThisEntry(movieTitle: String) {
        theCardContainingTheInformationOfShouldBeVisible(movieTitle)
        theUserNavigatesToTheList()
        theEntryIsVisible(movieTitle)
    }

    @And("the user navigates to the archive")
    fun theUserNavigatesToTheArchive() =
        composeRule.composeStep {
            val archiveLabel = activity.getString(R.string.archive)
            onNodeWithText(archiveLabel)
                .performClick()
        }
}
