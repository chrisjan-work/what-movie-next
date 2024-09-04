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

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.lairofpixies.whatmovienext.models.network.ConfigSynchronizer
import com.lairofpixies.whatmovienext.models.network.data.TmdbGenres
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.test.onNodeWithTextUnderTag
import com.lairofpixies.whatmovienext.views.screens.UiTags
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidTest
class FilteringStepDefs(
    private val testContext: CucumberTestContext,
) {
    private val composeRule
        get() = testContext.composeRuleHolder.composeRule

    @Inject
    lateinit var configSynchronizer: ConfigSynchronizer

    private val movieListStepDefs = MovieListStepDefs(testContext)

    @Given("the tmdb api offers the genres {string}")
    fun theTmdbApiOffersTheGenres(genreList: String) {
        testContext.movieApi.fakeGenres = {
            genreList.split(",").mapIndexed { index, genre ->
                TmdbGenres.TmdbGenre(index + 1L, genre)
            }
        }
        runBlocking {
            configSynchronizer.checkNow()
        }
    }

    @Given("the user clicks on Arrange and Filter")
    fun theUserClicksOnArrangeAndFilter() {
        movieListStepDefs.navigateBackIfInQueryEditor()
        composeRule.composeStep {
            onNodeWithTag(UiTags.Buttons.ARRANGE_MENU)
                .performClick()
        }
        composeRule.composeStep {
            onNodeWithTag(UiTags.Buttons.FILTER_TAB)
                .performClick()
        }
    }

    @And("the user enters {string} in the input {string}")
    fun theUserEntersInTheInput(
        value: String,
        label: String,
    ) = composeRule.composeStep {
        onNodeWithText(label).performTextInput(value)
    }

    @And("the user clicks on genre {string}")
    fun theUserClicksOnGenre(buttonName: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(buttonName, UiTags.Popups.WORD_SELECT)
                .performClick()
        }

    @And("the user clicks on {string} in word popup")
    fun theUserClicksOnInWordPopup(buttonName: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(buttonName, UiTags.Popups.WORD_SELECT)
                .performClick()
        }

    @And("the user clicks on {string} in filter tab")
    fun theUserClicksOnInFilterTab(buttonName: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(buttonName, UiTags.Menus.FILTERING)
                .performClick()
        }

    @And("the user clicks on {string} in number popup")
    fun theUserClicksOnInNumberPopup(buttonName: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(buttonName, UiTags.Popups.NUMBER_SELECT)
                .performClick()
        }
}
