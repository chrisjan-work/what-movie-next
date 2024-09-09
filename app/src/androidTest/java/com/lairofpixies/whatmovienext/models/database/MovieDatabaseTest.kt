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

import com.lairofpixies.whatmovienext.models.data.Departments
import com.lairofpixies.whatmovienext.models.data.MovieData
import com.lairofpixies.whatmovienext.models.database.data.DbGenre
import com.lairofpixies.whatmovienext.models.database.data.DbMovie
import com.lairofpixies.whatmovienext.models.database.data.DbPerson
import com.lairofpixies.whatmovienext.models.database.data.DbRole
import com.lairofpixies.whatmovienext.models.database.data.DbStaff
import com.lairofpixies.whatmovienext.models.database.data.DbStaffedMovie
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
                    movieId = MovieData.NEW_ID,
                    tmdbId = 22,
                    title = "Casino",
                    dbWatchDates = "",
                )
            val movieId = movieDao.insertMovie(dbMovie)
            val result = movieDao.getStaffedMovie(movieId).first()

            // Then
            val expected =
                DbStaffedMovie(
                    movie = dbMovie.copy(movieId = movieId),
                    staff = emptyList(),
                )
            assertNotEquals(MovieData.NEW_ID, movieId)
            assertEquals(expected, result)
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
                        dbWatchDates = "",
                    ),
                    DbMovie(
                        movieId = 2,
                        title = "Watchmen",
                        dbWatchDates = "",
                    ),
                    DbMovie(
                        movieId = 3,
                        title = "A Beautiful Mind",
                        dbWatchDates = "",
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
            val movie =
                DbMovie(movieId = 1, title = "The Wizard of Oz", dbWatchDates = "")
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
            val movie = DbMovie(movieId = 9, title = "Stargate", dbWatchDates = "")
            movieDao.insertMovie(movie)

            // When updating the movie details
            movieDao.updateMovie(movie.copy(title = "Stargate: Atlantis"))

            // Then the movie details are updated
            assertEquals(
                "Stargate: Atlantis",
                movieDao
                    .getStaffedMovie(9)
                    .first()
                    ?.movie
                    ?.title,
            )
        }

    @Test
    fun `set movie watch state`() =
        runTest {
            // Given a database with a single movie
            assert(movieDao.getAllMovies().first().isEmpty())
            val movie =
                DbMovie(movieId = 1, title = "The Wizard of Oz", dbWatchDates = "")
            movieDao.insertMovie(movie)

            // When setting the movie to watched
            movieDao.replaceWatchDates(movie.movieId, "10000,200")

            // Then the movie is watched
            assertEquals(
                "10000,200",
                movieDao
                    .getStaffedMovie(1)
                    .first()
                    ?.movie
                    ?.dbWatchDates,
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
                    dbWatchDates = "",
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
    fun `get any movie`() =
        runTest {
            // None
            assertEquals(null, movieDao.getOneMovie().first())

            // One
            val movie = DbMovie(movieId = 99, title = "Home Alone")
            movieDao.insertMovie(movie)

            assertEquals(movie, movieDao.getOneMovie().first())
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
            val expectedMovie =
                DbStaffedMovie(
                    movie = movie,
                    staff = emptyList(),
                )
            assertEquals(expectedMovie, shouldBeMovie)
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

    @Test
    fun `people create and read`() =
        runTest {
            // Given
            val examplePeople =
                listOf(
                    DbPerson(personId = 1, name = "Nic Cage", originalName = "Nicolas Coppola"),
                    DbPerson(personId = 2, name = "Solsonegene", originalName = "Arnol Sol"),
                )

            // When
            movieDao.insertPeople(examplePeople)
            val result = movieDao.fetchAllPeople()

            // Then
            assertEquals(examplePeople, result)
        }

    @Test
    fun `roles create and read`() =
        runTest {
            // Given
            val movies =
                listOf(
                    DbMovie(movieId = 111, title = "hundred eleven"),
                    DbMovie(movieId = 222, title = "two hundred twenty two"),
                )
            val insertedMovieIds = movieDao.insertMovies(movies)
            val people =
                listOf(
                    DbPerson(personId = 11, name = "eleven"),
                    DbPerson(personId = 22, name = "twentytwo"),
                )
            val insertedPeopleIds = movieDao.insertPeople(people)
            val roles =
                listOf(
                    DbRole(
                        roleId = 1,
                        personId = insertedPeopleIds[1],
                        movieId = insertedMovieIds[0],
                        order = 1111,
                        credit = "ones",
                        dept = "acting",
                    ),
                    DbRole(
                        roleId = 2,
                        personId = insertedPeopleIds[0],
                        movieId = insertedMovieIds[1],
                        order = 2222,
                        credit = "twos",
                        dept = "directing",
                    ),
                )

            // When
            movieDao.insertRoles(roles)
            val result = movieDao.fetchAllRoles()

            // Then
            assertEquals(roles, result)
        }

    @Test
    fun `get staffed movie`() =
        runTest {
            // Given
            val dbMovie =
                DbMovie(
                    movieId = 10,
                    title = "Casino",
                    dbWatchDates = "",
                )
            val dbStaff =
                DbStaff(
                    role =
                        DbRole(
                            roleId = 100,
                            personId = 1000,
                            movieId = 10,
                            credit = "director",
                            dept = "directing",
                            order = 1,
                        ),
                    person =
                        DbPerson(
                            personId = 1000,
                            name = "John Woo",
                        ),
                )
            movieDao.insertMovie(dbMovie)
            movieDao.insertPeople(listOf(dbStaff.person))
            movieDao.insertRoles(listOf(dbStaff.role))

            // When
            val result = movieDao.getStaffedMovie(10).first()

            // Then
            val expected =
                DbStaffedMovie(
                    movie = dbMovie,
                    staff = listOf(dbStaff),
                )
            assertEquals(expected, result)
        }

    @Test
    fun `get staff by department`() =
        runTest {
            // Given
            val dbMovie = DbMovie(movieId = 1, title = "production")
            val staffList =
                listOf(
                    DbStaff(
                        role =
                            DbRole(
                                roleId = 10,
                                personId = 10,
                                movieId = 1,
                                credit = "Actor",
                                dept = Departments.Actors.department,
                            ),
                        person =
                            DbPerson(
                                personId = 10,
                                name = "Aaron",
                            ),
                    ),
                    DbStaff(
                        role =
                            DbRole(
                                roleId = 11,
                                personId = 11,
                                movieId = 1,
                                credit = "Director",
                                dept = Departments.Directors.department,
                            ),
                        person =
                            DbPerson(
                                personId = 11,
                                name = "Betty",
                            ),
                    ),
                    DbStaff(
                        role =
                            DbRole(
                                roleId = 12,
                                personId = 12,
                                movieId = 1,
                                credit = "Writer",
                                dept = Departments.Writers.department,
                            ),
                        person =
                            DbPerson(
                                personId = 12,
                                name = "Carol",
                            ),
                    ),
                )
            movieDao.insertMovie(dbMovie)
            movieDao.insertPeople(staffList.map { it.person })
            movieDao.insertRoles(staffList.map { it.role })

            // When
            val actors = movieDao.getStaffByDepartment(Departments.Actors.department).first()
            val directors = movieDao.getStaffByDepartment(Departments.Directors.department).first()
            val writers = movieDao.getStaffByDepartment(Departments.Writers.department).first()

            // Then
            assertEquals(listOf(staffList[0]), actors)
            assertEquals(listOf(staffList[1]), directors)
            assertEquals(listOf(staffList[2]), writers)
        }

    @Test
    fun `dump movie table`() =
        runTest {
            // Given
            val dbMovie =
                DbMovie(
                    movieId = 10,
                    title = "Casino",
                    dbWatchDates = "",
                )
            val dbStaff =
                DbStaff(
                    role =
                        DbRole(
                            roleId = 100,
                            personId = 1000,
                            movieId = 10,
                            credit = "director",
                            dept = "directing",
                            order = 1,
                        ),
                    person =
                        DbPerson(
                            personId = 1000,
                            name = "John Woo",
                        ),
                )
            movieDao.insertMovie(dbMovie)
            movieDao.insertPeople(listOf(dbStaff.person))
            movieDao.insertRoles(listOf(dbStaff.role))

            // When
            val result = movieDao.allStaffedMovies()

            // Then
            val expected =
                DbStaffedMovie(
                    movie = dbMovie,
                    staff = listOf(dbStaff),
                )
            assertEquals(listOf(expected), result)
        }
}
