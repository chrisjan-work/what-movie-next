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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GenreRepositoryImpl(
    private val genreDao: GenreDao,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : GenreRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // Using a map in memory to avoid having to query the database for every movie
    private val inMemDb: StateFlow<Map<Long, String>> =
        genreDao
            .getAllGenres()
            .map { genreList ->
                genreList.associate { it.tmdbId to it.name }
            }.stateIn(
                scope = repositoryScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyMap(),
            )

    override suspend fun isEmpty(): Boolean = genreDao.getAllGenres().firstOrNull()?.isEmpty() ?: true

    override suspend fun appendGenres(genres: List<DbGenre>) {
        repositoryScope.launch {
            // if both are different, add
            val insertions = mutableListOf<DbGenre>()
            val removals = mutableListOf<DbGenre>()
            val updates = mutableListOf<DbGenre>()

            val currentMap = inMemDb.value

            genres.forEach { candidate ->
                val (name, tmdbId) = candidate
                val oldName = currentMap[tmdbId]
                when {
                    // name and tmdbId are the same -> ignore
                    name == oldName -> {}

                    // Id is known, but name is different because we skipped above
                    oldName != null -> {
                        removals.add(DbGenre(name = oldName, tmdbId = tmdbId))
                        insertions.add(candidate)
                    }

                    // name is known, but tmdbId is different because we skipped above
                    name in currentMap.values -> updates.add(candidate)

                    // name and tmdbId are different, insert
                    else -> insertions.add(candidate)
                }
            }

            genreDao.delete(removals)
            genreDao.update(updates)
            genreDao.insert(insertions)
        }
    }

    override fun genreNamesByTmdbIds(tmdbIds: List<Long>): List<String> =
        inMemDb.value
            .filterKeys { it in tmdbIds }
            .values
            .toList()
}
