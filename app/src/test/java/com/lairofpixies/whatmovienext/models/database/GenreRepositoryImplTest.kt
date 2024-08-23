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
package com.lairofpixies.whatmovienext.models.database

import com.lairofpixies.whatmovienext.models.database.data.DbGenre
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GenreRepositoryImplTest {
    private lateinit var genreRepository: GenreRepository
    private lateinit var genreDao: GenreDao

    @Before
    fun setUp() {
        genreDao = mockk(relaxed = true)
    }

    private fun TestScope.initializeSut() {
        genreRepository =
            GenreRepositoryImpl(
                genreDao = genreDao,
                ioDispatcher = UnconfinedTestDispatcher(testScheduler),
            )
        advanceUntilIdle()
    }

    private fun genreFlow() =
        flowOf(
            listOf(
                DbGenre(name = "Horror", tmdbId = 1127),
                DbGenre(name = "Mockumentary", tmdbId = 1079),
            ),
        )

    @Test
    fun `when it is empty`() =
        runTest {
            // Given
            every { genreDao.getAllGenres() } returns emptyFlow()
            initializeSut()

            // When
            val result = genreRepository.isEmpty()

            // Then
            assertTrue(result)
        }

    @Test
    fun `when it is not empty`() =
        runTest {
            // Given
            every { genreDao.getAllGenres() } returns genreFlow()
            initializeSut()

            // When
            val result = genreRepository.isEmpty()

            // Then
            assertFalse(result)
        }

    @Test
    fun `synchronize existing genres with new ones`() =
        runTest {
            // Given
            val virtualGenreList =
                mutableListOf(
                    DbGenre(name = "Comedy", tmdbId = 1111),
                    DbGenre(name = "Dramedy", tmdbId = 1112),
                    DbGenre(name = "Drama", tmdbId = 1113),
                    DbGenre(name = "RomCom", tmdbId = 1114),
                )
            coEvery { genreDao.insert(any()) } answers {
                val newGenres = firstArg<List<DbGenre>>()
                virtualGenreList.addAll(newGenres)
            }
            coEvery { genreDao.update(any()) } answers {
                val updated = firstArg<List<DbGenre>>()
                val names = updated.map { it.name }
                virtualGenreList.removeIf { candidate ->
                    candidate.name in names
                }
                virtualGenreList.addAll(updated)
            }
            coEvery { genreDao.delete(any()) } answers {
                val deletable = firstArg<List<DbGenre>>()
                virtualGenreList.removeAll(deletable)
            }

            // Load existing genres
            coEvery { genreDao.getAllGenres() } returns flowOf(virtualGenreList)
            initializeSut()
            advanceUntilIdle()

            // When
            genreRepository.appendGenres(
                listOf(
                    // new addition
                    DbGenre(name = "Mockumentary", tmdbId = 1132),
                    // update id
                    DbGenre(name = "Dramedy", tmdbId = 1118),
                    // rename
                    DbGenre(name = "Romantic Comedy", tmdbId = 1114),
                    // keep untouched
                    DbGenre(name = "Drama", tmdbId = 1113),
                ),
            )

            // Then
            val expectedSet =
                setOf(
                    // unmentioned stays untouched
                    DbGenre(name = "Comedy", tmdbId = 1111),
                    // repeated stays untouched
                    DbGenre(name = "Drama", tmdbId = 1113),
                    // id updated for existing genre
                    DbGenre(name = "Dramedy", tmdbId = 1118),
                    // name updated for existing genre
                    DbGenre(name = "Romantic Comedy", tmdbId = 1114),
                    // new genre introduced
                    DbGenre(name = "Mockumentary", tmdbId = 1132),
                )
            // turn to set because order is not important
            assertEquals(expectedSet, virtualGenreList.toSet())
        }

    @Test
    fun `find genres by ids`() =
        runTest {
            // Given
            every { genreDao.getAllGenres() } returns genreFlow()
            initializeSut()

            // When
            val result = genreRepository.genreNamesByTmdbIds(listOf(1127))

            // Then
            assertEquals(listOf("Horror"), result)
        }

    @Test
    fun `export all genres`() =
        runTest {
            // Given
            every { genreDao.getAllGenres() } returns genreFlow()
            initializeSut()

            // When
            val result = genreRepository.allGenreNames().first()

            // Then
            assertEquals(listOf("Horror", "Mockumentary"), result)
        }
}
