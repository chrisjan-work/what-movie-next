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
import androidx.compose.ui.test.performClick
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.views.screens.UiTags
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.Given

@HiltAndroidTest
class FilteringStepDefs(
    private val testContext: CucumberTestContext,
) {
    private val composeRule
        get() = testContext.composeRuleHolder.composeRule

    @Given("the user clicks on Arrange and Filter")
    fun theUserClicksOnArrangeAndFilter() {
        composeRule.composeStep {
            onNodeWithTag(UiTags.Buttons.ARRANGE_MENU)
                .performClick()
        }
        composeRule.composeStep {
            onNodeWithTag(UiTags.Buttons.FILTER_TAB)
                .performClick()
        }
    }
}
