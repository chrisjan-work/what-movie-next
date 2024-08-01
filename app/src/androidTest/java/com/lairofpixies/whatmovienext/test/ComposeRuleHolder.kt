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
package com.lairofpixies.whatmovienext.test

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.lairofpixies.whatmovienext.MainActivity
import io.cucumber.junit.WithJunitRule
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Rule

typealias ComposeRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

@WithJunitRule
class ComposeRuleHolder {
    @get:Rule
    val composeRule: ComposeRule = createAndroidComposeRule<MainActivity>()
}

fun ComposeRule.composeStep(
    timeoutMillis: Long = 5000,
    intervalMillis: Long = 1000,
    stepCode: ComposeRule.() -> Unit,
) {
    var problem: Throwable? = null
    try {
        waitUntil(timeoutMillis) {
            try {
                runTest {
                    withTimeout(intervalMillis) {
                        stepCode()
                        waitForIdle()
                    }
                }
                true
            } catch (throwable: Throwable) {
                problem = throwable
                false
            }
        }
    } catch (timeoutException: ComposeTimeoutException) {
        throw problem ?: timeoutException
    }
}

fun ComposeRule.onNodeWithTextUnderTag(
    text: String,
    tag: String,
    acceptSubstring: Boolean = false,
    caseSensitive: Boolean = false,
) = onAllNodes(hasText(text, substring = acceptSubstring, ignoreCase = !caseSensitive))
    .filterToOne(hasAnyAncestor(hasTestTag(tag)))
