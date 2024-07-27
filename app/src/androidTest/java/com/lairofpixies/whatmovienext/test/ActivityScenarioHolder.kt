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
