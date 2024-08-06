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

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.util.printableRuntime
import com.lairofpixies.whatmovienext.util.toAnnotatedString
import com.lairofpixies.whatmovienext.views.navigation.CustomBarItem
import com.lairofpixies.whatmovienext.views.navigation.CustomBottomBar

@Composable
fun MovieCard(
    movie: Movie.ForCard,
    bottomItems: List<CustomBarItem>,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            CustomBottomBar(
                items = bottomItems,
            )
        },
    ) { innerPadding ->
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

            Column(
                modifier =
                    Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                        .heightIn(min = parentHeight)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                CoverImage(
                    coverUrl = movie.searchData.coverUrl,
                    Modifier
                        .align(Alignment.CenterHorizontally),
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

                Spacer(modifier = Modifier.height(16.dp))

                TaglineDisplay(movie.detailData.tagline)

                Spacer(modifier = Modifier.height(4.dp))

                PlotDisplay(movie.detailData.plot)

                Spacer(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(top = 36.dp),
                )

                CreditsLink(
                    text = stringResource(R.string.tmdbCredits),
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
fun CoverImage(
    coverUrl: String,
    modifier: Modifier = Modifier,
) {
    if (coverUrl.isNotBlank()) {
        AsyncImage(
            model = coverUrl,
            contentDescription = null,
            modifier =
                modifier
                    .padding(2.dp)
                    .width(720.dp)
                    .clip(RoundedCornerShape(8.dp)),
        )
    } else {
        Box(
            modifier
                .padding(2.dp)
                .background(colorResource(id = R.color.missing_image))
                .width(720.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
    }
}

@Composable
fun TitlesDisplay(
    title: String,
    originalTitle: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.titleLarge,
    )
    if (originalTitle.isNotBlank() && originalTitle != title) {
        Text(
            text = originalTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = modifier,
        )
    }
}

@Composable
fun YearDisplay(
    year: Int?,
    modifier: Modifier = Modifier,
) {
    if (year != null) {
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier,
        )
    }
}

@Composable
fun RuntimeAndGenresDisplay(
    runtime: Int,
    genres: List<String>,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text =
            printableRuntime(runtimeMinutes = runtime, pos = "  •  ") +
                genres.joinToString(" / "),
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
fun TaglineDisplay(
    tagline: String,
    modifier: Modifier = Modifier,
) {
    if (tagline.isNotBlank()) {
        Text(
            text = tagline,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier,
        )
    }
}

@Composable
fun PlotDisplay(
    plot: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = plot,
        textAlign = TextAlign.Start,
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    )
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
            modifier.clickable {
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
