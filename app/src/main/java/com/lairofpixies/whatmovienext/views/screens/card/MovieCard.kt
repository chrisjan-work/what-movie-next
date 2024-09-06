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
package com.lairofpixies.whatmovienext.views.screens.card

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.util.printableRuntime
import com.lairofpixies.whatmovienext.util.readableRuntime
import com.lairofpixies.whatmovienext.util.toAnnotatedString
import com.lairofpixies.whatmovienext.views.components.AsyncPic
import com.lairofpixies.whatmovienext.views.components.CopyableText
import com.lairofpixies.whatmovienext.views.components.CustomScaffold
import com.lairofpixies.whatmovienext.views.components.ScrollableColumn
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar

@Composable
fun MovieCard(
    movie: Movie.ForCard,
    bottomItems: List<CustomBarItem>,
    modifier: Modifier = Modifier,
    topBar: @Composable (State<Boolean>) -> Unit = {},
) {
    CustomScaffold(
        modifier = modifier,
        bottomBar = {
            CustomBottomBar(
                items = bottomItems,
            )
        },
        topBar = topBar,
    ) { innerPadding, onScrollEvent ->
        BoxWithConstraints(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                    ),
        ) {
            val parentHeight = maxHeight

            ScrollableColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = parentHeight),
                verticalArrangement = Arrangement.SpaceBetween,
                onScrollEvent = onScrollEvent,
            ) {
                CoverPic(
                    coverUrl = movie.searchData.coverUrl,
                    contentDescription = stringResource(R.string.poster_for_movie, movie.searchData.title),
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(14.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                TitlesDisplay(
                    title = movie.searchData.title,
                    originalTitle = movie.searchData.originalTitle,
                )
                YearDisplay(movie.searchData.year)

                Spacer(modifier = Modifier.height(8.dp))

                RuntimeAndGenresDisplay(
                    runtime = movie.detailData.runtimeMinutes,
                    genres = movie.searchData.genres,
                )

                RatingDisplay(
                    mcRating = movie.detailData.mcRating,
                    rtRating = movie.detailData.rtRating,
                )

                Spacer(modifier = Modifier.height(20.dp))

                TaglineDisplay(movie.detailData.tagline)

                Spacer(modifier = Modifier.height(4.dp))

                PlotDisplay(movie.detailData.plot)

                Spacer(modifier = Modifier.height(22.dp))

                if (movie.staffData.crew.isNotEmpty()) {
                    DirectorsRoster(movie.staffData.crew)
                }
                if (movie.staffData.cast.isNotEmpty()) {
                    ActorsRoster(movie.staffData.cast)
                }

                MovieLinks(
                    title = movie.searchData.title,
                    tmdbId = movie.searchData.tmdbId.toString(),
                    imdbId = movie.detailData.imdbId,
                    rtId = movie.detailData.rtRating.sourceId,
                    mcId = movie.detailData.mcRating.sourceId,
                )

                Spacer(modifier = Modifier.padding(top = 36.dp))

                Spacer(
                    modifier = Modifier.weight(1f),
                )

                CreditsRow(
                    modifier =
                        Modifier
                            .align(Alignment.End)
                            .alpha(0.4f),
                )
            }
        }
    }
}

@Composable
fun CoverPic(
    coverUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    AsyncPic(
        url = coverUrl,
        contentDescription = contentDescription,
        placeholderIcon = Icons.Outlined.Theaters,
        width = 320.dp,
        height = 480.dp,
        cornerRadius = 16.dp,
        modifier = modifier,
    )
}

@Composable
fun TitlesDisplay(
    title: String,
    originalTitle: String,
    modifier: Modifier = Modifier,
) {
    CopyableText(
        text = title,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.padding(start = 8.dp, end = 8.dp),
    )
    if (originalTitle.isNotBlank() && originalTitle != title) {
        CopyableText(
            text = originalTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = modifier.padding(start = 8.dp, end = 8.dp),
        )
    }
}

@Composable
fun YearDisplay(
    year: Int?,
    modifier: Modifier = Modifier,
) {
    if (year != null) {
        val readYear = stringResource(R.string.year_value, year)
        CopyableText(
            text = year.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier =
                modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .semantics { contentDescription = readYear },
        )
    }
}

@Composable
fun RuntimeAndGenresDisplay(
    runtime: Int,
    genres: List<String>,
    modifier: Modifier = Modifier,
) {
    val dot = stringResource(id = R.string.middle_dot)
    val readText =
        stringResource(
            R.string.read_runtime_and_genres,
            readableRuntime(runtime),
            genres.joinToString(", "),
        )
    CopyableText(
        text =
            printableRuntime(runtimeMinutes = runtime, pos = "  $dot  ") +
                genres.joinToString(" / "),
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.bodySmall,
        modifier =
            modifier
                .padding(start = 8.dp, end = 8.dp)
                .semantics { contentDescription = readText },
    )
}

@Composable
fun TaglineDisplay(
    tagline: String,
    modifier: Modifier = Modifier,
) {
    if (tagline.isNotBlank()) {
        CopyableText(
            text = tagline,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier.padding(start = 8.dp, end = 8.dp),
        )
    }
}

@Composable
fun PlotDisplay(
    plot: String,
    modifier: Modifier = Modifier,
) {
    CopyableText(
        text = plot,
        textAlign = TextAlign.Start,
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = modifier.padding(start = 8.dp, end = 8.dp),
    )
}

@Composable
fun CreditsRow(modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(start = 8.dp, end = 8.dp)) {
        Text(
            text = stringResource(id = R.string.sources),
            style = MaterialTheme.typography.labelSmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(4.dp),
        )
        CreditsLink(
            text = stringResource(R.string.tmdb_credits),
        )
        CreditsLink(
            text = stringResource(R.string.omdb_credits),
        )
        CreditsLink(
            text = stringResource(R.string.wikidata_credits),
        )
    }
}

@Composable
fun CreditsLink(
    text: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val annotatedString = text.toAnnotatedString(colorResource(id = R.color.link))

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.labelSmall,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onBackground,
        modifier =
            modifier
                .padding(4.dp)
                .clickable {
                    annotatedString
                        .getStringAnnotations(tag = "URL", start = 0, end = text.length)
                        .firstOrNull()
                        ?.let { annotation ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                            context.startActivity(intent)
                        }
                },
    )
}
