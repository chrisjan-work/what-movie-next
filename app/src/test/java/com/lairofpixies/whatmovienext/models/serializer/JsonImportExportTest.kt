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
package com.lairofpixies.whatmovienext.models.serializer

import com.lairofpixies.whatmovienext.models.data.MovieDump
import com.lairofpixies.whatmovienext.models.database.MovieRepository
import com.lairofpixies.whatmovienext.models.mappers.testCardMovieExtended
import com.squareup.moshi.JsonAdapter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

class JsonImportExportTest {
    private lateinit var movieRepository: MovieRepository
    private lateinit var adapter: JsonAdapter<MovieDump>
    private lateinit var jsonImportExport: JsonImportExport

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        movieRepository = mockk(relaxed = true)
        adapter = mockk(relaxed = true)

        jsonImportExport =
            JsonImportExport(
                movieRepository,
                adapter,
                testDispatcher,
            )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `get all movies with details in json format`() =
        runTest {
            // Given
            val movieDump = listOf(testCardMovieExtended())
            coEvery { movieRepository.retrieveFullMovieDump() } returns movieDump
            every { adapter.toJson(movieDump) } returns "testJson"

            // When
            val result = jsonImportExport.fullMoviesJson()

            // Then
            val expected = "testJson"
            assertEquals(expected, result)
        }

    @Test
    fun `store movies successfully`() =
        runTest {
            // Given
            val movieDump = listOf(testCardMovieExtended())
            every { adapter.fromJson(any<String>()) } returns movieDump

            // When
            val result = jsonImportExport.storeMoviesFromJson("testJson")

            // Then
            assertEquals(true, result)
            coVerify { movieRepository.storeMovie(movieDump.first()) }
        }

    @Test
    fun `fail to store movies for wrong json`() =
        runTest {
            // Given
            every { adapter.fromJson(any<String>()) } throws IOException()

            // When
            val result = jsonImportExport.storeMoviesFromJson("testJson")

            // Then
            assertEquals(false, result)
        }
}
