package com.lairofpixies.whatmovienext.stepdefs

import androidx.compose.ui.test.assertIsDisplayed
import com.lairofpixies.whatmovienext.test.CucumberTestContext
import com.lairofpixies.whatmovienext.test.composeStep
import com.lairofpixies.whatmovienext.test.onNodeWithTextUnderTag
import com.lairofpixies.whatmovienext.views.screens.ArchiveTags
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.Then

@HiltAndroidTest
class ArchiveStepDefs(
    private val testContext: CucumberTestContext,
) {
    private val composeRule
        get() = testContext.composeRuleHolder.composeRule

    @Then("the entry {string} is visible in the archive")
    fun theEntryIsVisibleInTheArchive(movieTitle: String) =
        composeRule.composeStep {
            onNodeWithTextUnderTag(movieTitle, ArchiveTags.TAG_ARCHIVE_LIST)
                .assertIsDisplayed()
        }
}
