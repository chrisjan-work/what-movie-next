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

import com.lairofpixies.whatmovienext.models.data.Preset
import com.lairofpixies.whatmovienext.models.mappers.PresetMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class PresetRepositoryImpl(
    private val presetDao: PresetDao,
    private val presetMapper: PresetMapper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PresetRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override fun getPreset(presetId: Long): Flow<Preset?> =
        presetDao
            .getPreset(presetId)
            .map { dbPreset ->
                dbPreset?.let { presetMapper.toPreset(dbPreset) }
            }.flowOn(ioDispatcher)

    override suspend fun updatePreset(preset: Preset): Long =
        repositoryScope
            .async {
                val dbPreset = presetMapper.toDbPreset(preset)
                presetDao.insertOrUpdate(dbPreset)
            }.await()

    override suspend fun saveNewPreset(preset: Preset): Long =
        repositoryScope
            .async {
                val dbPreset = presetMapper.toDbPreset(preset).copy(presetId = Preset.NEW_ID)
                presetDao.insertOrUpdate(dbPreset)
            }.await()
}
