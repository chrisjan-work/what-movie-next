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
package com.lairofpixies.whatmovienext.views.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.views.screens.UiTags

@Composable
fun SearchResultsPicker(
    searchResults: List<Movie>,
    onResultSelected: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (searchResults.isEmpty()) {
        return
    }
    Column(
        modifier =
            modifier
                .testTag(UiTags.Screens.SEARCH_RESULTS)
                .background(MaterialTheme.colorScheme.background)
                .padding(4.dp)
                .fillMaxSize(),
    ) {
        LazyColumn(
            modifier = modifier,
        ) {
            items(searchResults) { movie ->
                SearchResultItem(
                    movie,
                    onClick = {
                        onResultSelected(movie)
                    },
                )
            }
        }
    }
}

@Composable
fun SearchResultItem(
    movie: Movie,
    onClick: () -> Unit,
) {
    val missingThumbnailColor = Color(LocalContext.current.getColor(R.color.missing_image))
    Row(
        modifier =
            Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(2.dp)
                .border(
                    border =
                        BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        ),
                    shape = RoundedCornerShape(8.dp),
                ).padding(6.dp),
    ) {
        if (movie.thumbnailUrl.isNotBlank()) {
            AsyncImage(
                model = movie.thumbnailUrl,
                contentDescription = null,
                modifier =
                    Modifier
                        .size(width = 68.dp, height = 102.dp)
                        .align(Alignment.CenterVertically),
            )
        } else {
            Spacer(
                modifier =
                    Modifier
                        .size(width = 68.dp, height = 102.dp)
                        .align(Alignment.CenterVertically)
                        .background(missingThumbnailColor),
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Column(
            modifier = Modifier.heightIn(min = 100.dp),
        ) {
            Text(
                text = movie.title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (movie.originalTitle.isNotBlank() && movie.originalTitle != movie.title) {
                Text(
                    text = movie.originalTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            movie.year?.let { year ->
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (movie.genres.isNotEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = movie.genres.joinToString(" / "),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}
