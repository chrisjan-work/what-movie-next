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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lairofpixies.whatmovienext.models.database.data.DbPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbPreset: DbPreset): Long

    @Delete
    suspend fun deletePreset(dbPreset: DbPreset)

    @Query("SELECT * FROM dbpreset")
    fun getAllPresets(): Flow<List<DbPreset>>

    @Query("SELECT * FROM dbpreset WHERE presetId = :presetId")
    fun getPreset(presetId: Long): Flow<DbPreset?>
}
