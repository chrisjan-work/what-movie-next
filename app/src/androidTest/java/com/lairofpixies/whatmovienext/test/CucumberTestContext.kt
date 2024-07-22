package com.lairofpixies.whatmovienext.test

import com.lairofpixies.whatmovienext.models.database.MovieDatabase
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@Singleton
class CucumberTestContext
    @Inject
    constructor(
        val composeRuleHolder: ComposeRuleHolder,
        val scenarioHolder: ActivityScenarioHolder,
    ) {
        @Inject
        lateinit var appDatabase: MovieDatabase
    }
