package com.lairofpixies.whatmovienext.test

import android.app.Application
import android.content.Context
import dagger.hilt.android.testing.HiltTestApplication
import io.cucumber.android.runner.CucumberAndroidJUnitRunner
import io.cucumber.junit.CucumberOptions

@CucumberOptions(
    glue = ["com.lairofpixies.whatmovienext.test", "com.lairofpixies.whatmovienext.stepdefs"],
    features = ["features"],
    plugin = ["pretty"],
    tags = [BuildConfig.CUCUMBER_TAG_EXPRESSION],
)
class CucumberRunner : CucumberAndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        name: String?,
        context: Context?,
    ): Application = super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
