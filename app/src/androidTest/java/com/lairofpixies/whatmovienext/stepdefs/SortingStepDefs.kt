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
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.views.screens.UiTags
import cucumber.api.PendingException
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then

@HiltAndroidTest
class SortingStepDefs(
    private val testContext: CucumberTestContext,
) {
    private val composeRule
        get() = testContext.composeRuleHolder.composeRule

    private val movieListStepDefs = MovieListStepDefs(testContext)

    @Given("the user clicks on Arrange")
    fun theUserClicksOnArrange() {
        movieListStepDefs.navigateBackIfInQueryEditor()
        composeRule.composeStep {
            onNodeWithTag(UiTags.Buttons.ARRANGE_MENU)
                .performClick()
        }
    }

    @Then("the sorting menu is visible")
    fun theSortingMenuIsVisible() =
        composeRule.composeStep {
            onNodeWithTag(UiTags.Menus.SORTING)
                .assertIsDisplayed()
        }

    @Then("the sorting menu is not visible")
    fun theSortingMenuIsNotVisible() =
        composeRule.composeStep {
            onNodeWithTag(UiTags.Menus.SORTING)
                .assertIsNotDisplayed()
        }

    @And("the user sorts by {string}")
    fun theUserSortsBy(label: String) =
        composeRule.composeStep {
            val filterTag =
                when (label) {
                    "title" -> UiTags.Buttons.SORT_BY_TITLE
                    "date added" -> UiTags.Buttons.SORT_BY_CREATION_TIME
                    "year" -> UiTags.Buttons.SORT_BY_YEAR
                    "genre" -> UiTags.Buttons.SORT_BY_GENRE
                    "runtime" -> UiTags.Buttons.SORT_BY_RUNTIME
                    "director" -> UiTags.Buttons.SORT_BY_DIRECTOR
                    "avg. score" -> UiTags.Buttons.SORT_BY_SCORE
                    else -> throw PendingException("Unknown filter button \"$label\"")
                }
            onNodeWithTag(filterTag).performClick()
        }

    @And("the user taps on sort by {string} {string} times")
    fun theUserTapsOnSortByTimes(
        label: String,
        number: String,
    ) = number.toIntOrNull()?.let { count ->
        repeat(count) {
            theUserSortsBy(label)
        }
    }

    @Then("the list contains an entry {string} in position {string}")
    fun theListContainsAnEntryInPosition(
        title: String,
        position: String,
    ) = composeRule.composeStep {
        onNodeWithTag("${UiTags.Items.MOVIE_LIST_ITEM}_$position")
            .assertIsDisplayed()
            .assertTextContains(title)
    }
}
