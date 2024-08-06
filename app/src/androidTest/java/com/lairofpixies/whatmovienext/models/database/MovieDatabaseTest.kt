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

import com.lairofpixies.whatmovienext.models.data.WatchState
import com.lairofpixies.whatmovienext.models.database.data.DbGenre
import com.lairofpixies.whatmovienext.models.database.data.DbMovie
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class MovieDatabaseTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: MovieDatabase

    @Inject
    lateinit var movieDao: MovieDao

    @Inject
    lateinit var genreDao: GenreDao

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `create and read entry`() =
        runTest {
            // Given an empty database
            assert(movieDao.getAllMovies().first().isEmpty())

            // When we insert a movie
            val dbMovie =
                DbMovie(
                    movieId = 10,
                    title = "Casino",
                    watchState = WatchState.PENDING,
                )
            movieDao.insertMovie(dbMovie)

            // Then the movie is in the database
            assertEquals(dbMovie, movieDao.getMovie(10).first())
        }

    @Test
    fun `create and read several entries at a time`() =
        runTest {
            // Given an empty database
            assert(movieDao.getAllMovies().first().isEmpty())

            // When we insert some movies
            val movies =
                listOf(
                    DbMovie(
                        movieId = 1,
                        title = "Someone flew over the cuckoo's nest",
                        watchState = WatchState.PENDING,
                    ),
                    DbMovie(
                        movieId = 2,
                        title = "Watchmen",
                        watchState = WatchState.PENDING,
                    ),
                    DbMovie(
                        movieId = 3,
                        title = "A Beautiful Mind",
                        watchState = WatchState.PENDING,
                    ),
                )

            movieDao.insertMovies(movies)

            // Then the movie is in the database
            assertEquals(movies, movieDao.getAllMovies().first())
        }

    @Test
    fun `create and delete entry`() =
        runTest {
            // Given a database with a single movie
            assert(movieDao.getAllMovies().first().isEmpty())
            val movie = DbMovie(movieId = 1, title = "The Wizard of Oz", watchState = WatchState.PENDING)
            movieDao.insertMovie(movie)

            // When we remove it
            movieDao.deleteMovie(movie)

            // Then the movie is in the database
            assertEquals(emptyList<DbMovie>(), movieDao.getAllMovies().first())
        }

    @Test
    fun `update movie details`() =
        runTest {
            // Given a database with a single movie
            val movie = DbMovie(movieId = 9, title = "Stargate", watchState = WatchState.PENDING)
            movieDao.insertMovie(movie)

            // When updating the movie details
            movieDao.updateMovie(movie.copy(title = "Stargate: Atlantis"))

            // Then the movie details are updated
            assertEquals(
                "Stargate: Atlantis",
                movieDao
                    .getMovie(9)
                    .first()
                    ?.title,
            )
        }

    @Test
    fun `set movie watch state`() =
        runTest {
            // Given a database with a single movie
            assert(movieDao.getAllMovies().first().isEmpty())
            val movie = DbMovie(movieId = 1, title = "The Wizard of Oz", watchState = WatchState.PENDING)
            movieDao.insertMovie(movie)

            // When setting the movie to watched
            movieDao.updateWatchState(movie.movieId, WatchState.WATCHED)

            // Then the movie is watched
            assertEquals(
                WatchState.WATCHED,
                movieDao
                    .getMovie(1)
                    .first()
                    ?.watchState,
            )
        }

    @Test
    fun `archive movie`() =
        runTest {
            // Given a database with a single movie
            assert(movieDao.getAllMovies().first().isEmpty())
            val movie =
                DbMovie(
                    movieId = 1,
                    title = "The Wizard of Oz",
                    watchState = WatchState.PENDING,
                    isArchived = false,
                )
            movieDao.insertMovie(movie)

            // When setting the movie to archived
            movieDao.archive(movie.movieId)

            // Then the movie is removed from the view list and moved to the archive
            assert(movieDao.getAllMovies().first().isEmpty())
            assertEquals(
                listOf(movie.copy(isArchived = true)),
                movieDao.getArchivedMovies().first(),
            )
        }

    @Test
    fun `fetch single movie by id`() =
        runTest {
            // Given a database with a single movie
            val movie = DbMovie(movieId = 11, title = "The Searchers")
            movieDao.insertMovie(movie)

            // When fetching the movie by id
            val shouldBeMovie = movieDao.fetchMovieById(11)
            val shouldBeNull = movieDao.fetchMovieById(12)

            // Then the movie is returned or not
            assertEquals(movie, shouldBeMovie)
            assertEquals(null, shouldBeNull)
        }

    @Test
    fun `fetch movies by title`() =
        runTest {
            // Given a database with three movies, but one has a duplicated title
            val movies =
                listOf(
                    DbMovie(movieId = 1, title = "The Godfather"),
                    DbMovie(movieId = 2, title = "The Godfather II"),
                    DbMovie(movieId = 3, title = "The Godfather II"),
                )
            movieDao.insertMovies(movies)

            // When fetching the movies by title
            val duplicated = movieDao.fetchMoviesByTitle("The Godfather II")
            val single = movieDao.fetchMoviesByTitle("The Godfather")
            val none = movieDao.fetchMoviesByTitle("The Godfather III")

            // Then the movies are returned
            assertEquals(2, duplicated.size)
            assertEquals(1, single.size)
            assertEquals(0, none.size)
        }

    @Test
    fun `fetch movies by title is case insensitive`() =
        runTest {
            // Given a database with a single movie
            val movie = DbMovie(movieId = 1, title = "AbCd")
            movieDao.insertMovie(movie)

            // When fetching the movies by title with different case
            val lowerCase = movieDao.fetchMoviesByTitle("abcd")
            val upperCase = movieDao.fetchMoviesByTitle("ABCD")
            val none = movieDao.fetchMoviesByTitle("abcd123")

            // Then the movies are returned
            assertEquals(movie, lowerCase.first())
            assertEquals(movie, upperCase.first())
            assertEquals(emptyList<DbMovie>(), none)
        }

    @Test
    fun `fetch movie by tmdbid`() =
        runTest {
            // Given a database with a single movie
            val movie = DbMovie(movieId = 11, tmdbId = 121, title = "The Searchers")
            movieDao.insertMovie(movie)

            // When fetching the movie by id
            val shouldBeMovie = movieDao.fetchMovieByTmdbId(121)
            val shouldBeNull = movieDao.fetchMovieByTmdbId(212)

            // Then the movie is returned or not
            assertEquals(movie, shouldBeMovie)
            assertEquals(null, shouldBeNull)
        }

    @Test
    fun `restore archived movies`() =
        runTest {
            // Given a database with an archived movie
            val movie = DbMovie(movieId = 1, title = "The Rum Diary")
            movieDao.insertMovie(movie)
            movieDao.archive(movie.movieId)

            // When restored
            movieDao.restore(movie.movieId)

            // Then
            assertEquals(emptyList<DbMovie>(), movieDao.getArchivedMovies().first())
            assertEquals(listOf(movie), movieDao.getAllMovies().first())
        }

    @Test
    fun `delete archived movies`() =
        runTest {
            // Given a database with an archived movie
            val movie = DbMovie(movieId = 1, title = "The Rum Diary")
            movieDao.insertMovie(movie)
            movieDao.archive(movie.movieId)

            // When restored
            movieDao.deleteMovie(movie.copy(isArchived = true))

            // Then
            assertEquals(emptyList<DbMovie>(), movieDao.getArchivedMovies().first())
            assertEquals(emptyList<DbMovie>(), movieDao.getAllMovies().first())
        }

    @Test
    fun `genres CRUD`() =
        runTest {
            // Given
            val exampleGenres =
                listOf(
                    DbGenre(name = "Action", tmdbId = 100),
                    DbGenre(name = "Adventure", tmdbId = 101),
                    DbGenre(name = "Comedy", tmdbId = 105),
                )

            // When
            genreDao.insert(exampleGenres)
            genreDao.update(listOf(DbGenre(tmdbId = 88, name = "Action")))
            genreDao.delete(listOf(exampleGenres[1]))
            val result = genreDao.getAllGenres().first()

            // Then
            assertEquals(
                listOf(
                    DbGenre(name = "Action", tmdbId = 88),
                    DbGenre(name = "Comedy", tmdbId = 105),
                ),
                result,
            )
        }
}
