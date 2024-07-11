package com.lairofpixies.whatmovienext.test

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import com.lairofpixies.whatmovienext.MainActivity
import org.junit.After

class ActivityScenarioHolder {
    private var scenario: ActivityScenario<*>? = null

    fun launch(intent: Intent) {
        assert(scenario == null)
        scenario = ActivityScenario.launch<MainActivity>(intent)
    }

    fun launch() {
        assert(scenario == null)
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun close() {
        scenario?.close()
        scenario = null
    }
}
