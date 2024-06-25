package com.lairofpixies.whatmovienext.test

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.lairofpixies.whatmovienext.MainActivity
import io.cucumber.junit.WithJunitRule
import org.junit.Rule

@WithJunitRule
class ComposeRuleHolder {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()
}
