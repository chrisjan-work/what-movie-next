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
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lairofpixies.whatmovienext.models.database.data.DbMovie
import com.lairofpixies.whatmovienext.models.network.data.TmdbGenres
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.test.onNodeWithTextUnderTag
import com.lairofpixies.whatmovienext.views.screens.UiTags
import cucumber.api.PendingException
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.When
import kotlinx.coroutines.test.runTest

@HiltAndroidTest
class MovieCardStepDefs(
    private val testContext: CucumberTestContext,
) {
    private val composeRule
        get() = testContext.composeRuleHolder.composeRule

    private fun updateDbEntry(
        title: String,
        updateFunction: DbMovie.() -> DbMovie,
    ) {
        runTest {
            val entry =
                testContext.appDatabase
                    .movieDao()
                    .fetchMoviesByTitle(title)
                    .firstOrNull()
                    ?: return@runTest

            val updated = updateFunction(entry)
            testContext.appDatabase.movieDao().updateMovie(updated)
        }
    }

    @Given("the db entry {string} has {string} set as {string}")
    fun theDbEntryHasSetAs(
        title: String,
        field: String,
        value: String,
    ) {
        updateDbEntry(title) {
            when (field) {
                "original title" -> copy(originalTitle = value)
                "year" -> copy(year = value.toInt())
                "runtime" -> copy(runtimeMinutes = value.toInt())
                "genres" -> copy(genres = value)
                "tagline" -> copy(tagline = value)
                "plot" -> copy(plot = value)
                else -> throw PendingException("Unknown field \"$field\"")
            }
        }
    }

    @And("the card contains the text {string}")
    fun theCardContainsTheText(text: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(text, acceptSubstring = true, tag = UiTags.Screens.MOVIE_CARD)
                .assertIsDisplayed()
        }

    @And("the extended entry {string} has {string} set as {string}")
    fun theExtendedEntryHasSetAs(
        title: String,
        field: String,
        value: String,
    ) {
        val entry = testContext.movieApi.fakeMovieExtended()
        val updated =
            when (field) {
                "original title" -> entry.copy(originalTitle = value)
                "year" -> entry.copy(releaseDate = "$value-01-01")
                "runtime" -> entry.copy(runtime = value.toInt())
                "genres" -> entry.copy(genres = listOf(TmdbGenres.TmdbGenre(1, value)))
                "tagline" -> entry.copy(tagline = value)
                "plot" -> entry.copy(summary = value)
                else -> throw PendingException("Unknown field \"$field\"")
            }
        testContext.movieApi.fakeMovieExtended = { updated }
    }

    @When("the user archives the current entry")
    fun theUserArchivesTheCurrentEntry() =
        composeRule.composeStep {
            val archiveLabel = activity.getString(com.lairofpixies.whatmovienext.R.string.archive)
            onNodeWithText(archiveLabel)
                .performClick()
        }
}
