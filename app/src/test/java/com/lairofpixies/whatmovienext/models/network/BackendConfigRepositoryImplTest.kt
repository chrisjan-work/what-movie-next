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
package com.lairofpixies.whatmovienext.models.network

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BackendConfigRepositoryImplTest {
    private lateinit var backendConfigRepository: BackendConfigRepository

    @Before
    fun setUp() {
        backendConfigRepository = BackendConfigRepositoryImpl()
    }

    @Test
    fun getThumbnailUrl() {
        val url = backendConfigRepository.getThumbnailUrl("/test")
        assertEquals("https://image.tmdb.org/t/p/w154/test", url)
    }

    @Test
    fun getCoverUrl() {
        val url = backendConfigRepository.getCoverUrl("/test")
        assertEquals("https://image.tmdb.org/t/p/w500/test", url)
    }
}
