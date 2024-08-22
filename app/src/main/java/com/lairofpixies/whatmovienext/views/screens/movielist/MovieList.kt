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
package com.lairofpixies.whatmovienext.views.screens.movielist

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.Rating
import com.lairofpixies.whatmovienext.models.data.isNotNegative
import com.lairofpixies.whatmovienext.util.printableRuntime
import com.lairofpixies.whatmovienext.util.printableYear
import com.lairofpixies.whatmovienext.views.components.AsyncPic
import com.lairofpixies.whatmovienext.views.components.ScrollableLazyColumn
import com.lairofpixies.whatmovienext.views.screens.UiTags

@Composable
fun MovieList(
    filteredMovies: List<Movie.ForList>,
    onMovieClicked: (Long) -> Unit,
    onScrollEvent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScrollableLazyColumn(
        modifier = modifier.testTag(UiTags.Screens.MOVIE_LIST),
        contentPadding = PaddingValues(bottom = 120.dp),
        onScrollEvent = onScrollEvent,
    ) {
        itemsIndexed(filteredMovies) { movieIndex, movie ->
            MovieListItem(
                movie,
                modifier = modifier.testTag("${UiTags.Items.MOVIE_LIST_ITEM}_$movieIndex"),
            ) { onMovieClicked(movie.appData.movieId) }
        }
    }
}

@Composable
fun MovieListItem(
    movie: Movie.ForList,
    modifier: Modifier = Modifier,
    onItemClicked: () -> Unit = {},
) {
    val bgColor =
        if (movie.appData.watchDates.isNotEmpty()) {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.background
        }
    Row(
        modifier =
            modifier
                .clickable(onClick = onItemClicked)
                .padding(2.dp)
                .clip(RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .background(bgColor)
                .border(
                    border =
                        BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        ),
                    shape = RoundedCornerShape(8.dp),
                ).padding(6.dp),
    ) {
        val maxWidth = if (movie.appData.watchDates.isNotEmpty()) 260.dp else 320.dp
        ThumbnailPic(
            thumbnailUrl = movie.searchData.thumbnailUrl,
            modifier =
                Modifier
                    .align(Alignment.CenterVertically),
        )
        Spacer(modifier = Modifier.size(16.dp))
        Column(
            modifier =
                Modifier
                    .heightIn(min = 120.dp)
                    .widthIn(max = maxWidth),
        ) {
            Text(
                text = movie.searchData.title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
            )
            YearAndRuntimeDisplay(
                movie.searchData.year,
                movie.detailData.runtimeMinutes,
            )
            RatingsDisplay(
                movie.detailData.mcRating,
                movie.detailData.rtRating,
                modifier = Modifier.alpha(0.8f),
            )
            Spacer(modifier = Modifier.weight(1f))
            if (movie.searchData.genres.isNotEmpty()) {
                Text(
                    text = movie.searchData.genres.joinToString(" / "),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                )
            }
            if (movie.detailData.directorNames.isNotEmpty()) {
                val names = movie.detailData.directorNames.joinToString(", ")
                val intro = stringResource(R.string.directed_by_short)
                Text(
                    text = "$intro: $names",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
        if (movie.appData.watchDates.isNotEmpty()) {
            Spacer(modifier = Modifier.size(8.dp))
            Spacer(modifier = Modifier.weight(1f))
            SeenDisplay(
                movie.appData.watchDates,
            )
        }
    }
}

@Composable
fun ThumbnailPic(
    thumbnailUrl: String,
    modifier: Modifier = Modifier,
) {
    AsyncPic(
        url = thumbnailUrl,
        placeholderIcon = Icons.Outlined.Theaters,
        width = 76.dp,
        height = 116.dp,
        cornerRadius = 5.dp,
        modifier = modifier,
    )
}

@Composable
fun YearAndRuntimeDisplay(
    year: Int?,
    runtimeMinutes: Int,
    modifier: Modifier = Modifier,
) {
    val dot = stringResource(id = R.string.middle_dot)
    Text(
        text = printableYear(year, pos = "  $dot  ") + printableRuntime(runtimeMinutes),
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
    )
}

@Composable
fun SeenDisplay(
    watchDates: List<Long>,
    modifier: Modifier = Modifier,
) {
    if (watchDates.isNotEmpty()) {
        val seenIcon = Icons.Outlined.RemoveRedEye
        Icon(
            imageVector = seenIcon,
            contentDescription = "",
            modifier =
                modifier
                    .padding(2.dp)
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    .padding(2.dp),
            tint = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
        )
    } else {
        Spacer(
            modifier
                .padding(2.dp)
                .size(24.dp),
        )
    }
}

@Composable
fun RatingsDisplay(
    mcRating: Rating?,
    rtRating: Rating?,
    modifier: Modifier = Modifier,
) {
    Row {
        if (mcRating.isNotNegative()) {
            RatingIcon(
                R.drawable.metacritic,
                modifier = modifier,
            )
            RatingDisplay(
                mcRating?.displayValue ?: "?",
                modifier = modifier,
            )
        }
        if (rtRating.isNotNegative()) {
            RatingIcon(
                R.drawable.rotten_tomatoes,
                modifier = modifier,
            )
            RatingDisplay(
                rtRating?.displayValue ?: "?",
                modifier = modifier,
            )
        }
    }
}

@Composable
fun RatingIcon(
    @DrawableRes resource: Int,
    modifier: Modifier = Modifier,
) {
    Image(
        painterResource(resource),
        contentDescription = "",
        modifier =
            modifier
                .padding(2.dp)
                .size(16.dp),
    )
}

@Composable
fun RatingDisplay(
    rating: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = rating,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier.padding(2.dp),
        color = MaterialTheme.colorScheme.onBackground,
    )
}
