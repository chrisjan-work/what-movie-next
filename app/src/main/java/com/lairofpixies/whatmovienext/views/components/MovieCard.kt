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
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Movie
import com.lairofpixies.whatmovienext.models.data.Staff
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
                    modifier = Modifier.align(Alignment.CenterHorizontally),
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

                Row {
                    if (movie.detailData.mcRating != null) {
                        RatingRow(
                            logo = R.drawable.metacritic,
                            text =
                                movie.detailData.mcRating.displayValue,
                            modifier = Modifier.alpha(0.8f),
                        )
                    }
                    if (movie.detailData.rtRating != null) {
                        RatingRow(
                            logo = R.drawable.rotten_tomatoes,
                            text = movie.detailData.rtRating.displayValue,
                            modifier = Modifier.alpha(0.8f),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TaglineDisplay(movie.detailData.tagline)

                Spacer(modifier = Modifier.height(4.dp))

                PlotDisplay(movie.detailData.plot)

                Spacer(modifier = Modifier.height(22.dp))

                if (movie.staffData.crew.isNotEmpty()) {
                    DirectorsRooster(movie.staffData.crew)
                }
                if (movie.staffData.cast.isNotEmpty()) {
                    ActorsRooster(movie.staffData.cast)
                }

                MovieLinks(
                    imdbId = movie.detailData.imdbId,
                    tmdbId = movie.searchData.tmdbId.toString(),
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
fun CoverImage(
    coverUrl: String,
    modifier: Modifier = Modifier,
) {
    val imageState =
        remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }

    Box(
        modifier =
            modifier
                .padding(8.dp)
                .size(width = 320.dp, height = 480.dp)
                .clip(RoundedCornerShape(12.dp)),
    ) {
        if (imageState.value !is AsyncImagePainter.State.Success) {
            CoverPlaceholder(
                isLoading = imageState.value is AsyncImagePainter.State.Loading,
                modifier = Modifier,
            )
        }

        if (coverUrl.isNotBlank()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = "",
                onState = { state -> imageState.value = state },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun RatingRow(
    @DrawableRes logo: Int,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = logo),
            contentDescription = "",
            modifier =
                modifier
                    .padding(4.dp)
                    .size(20.dp),
        )

        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontStyle = FontStyle.Italic,
            modifier =
                modifier
                    .padding(top = 12.dp, start = 4.dp, end = 4.dp)
                    .height(height = 30.dp),
        )
    }
}

@Composable
fun ClickableLogo(
    @DrawableRes logo: Int,
    modifier: Modifier = Modifier,
    url: String? = null,
) {
    val context = LocalContext.current

    val borderColor =
        MaterialTheme.colorScheme.onBackground.copy(
            alpha = if (url != null) 0.2f else 0f,
        )

    Image(
        painter = painterResource(id = logo),
        contentDescription = "",
        modifier =
            modifier
                .padding(4.dp)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                ).padding(10.dp)
                .size(36.dp)
                .clickable {
                    url?.let {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                },
    )
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
    val dot = stringResource(id = R.string.middle_dot)
    Text(
        modifier = modifier,
        text =
            printableRuntime(runtimeMinutes = runtime, pos = "  $dot  ") +
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
fun DirectorsRooster(
    crew: List<Staff>,
    modifier: Modifier = Modifier,
) {
    val combinedCrew = joinRoles(crew)
    Column(modifier = modifier.height(180.dp)) {
        Text(
            text = stringResource(R.string.direction_and_writing),
            style = MaterialTheme.typography.titleSmall,
        )
        LazyRow {
            items(combinedCrew) {
                MiniProfile(it)
            }
        }
    }
}

@Composable
fun ActorsRooster(
    cast: List<Staff>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.height(180.dp)) {
        Text(
            text = stringResource(R.string.cast),
            style = MaterialTheme.typography.titleSmall,
        )
        LazyRow {
            items(cast) {
                MiniProfile(it)
            }
        }
    }
}

// sometimes a movie is directed and written by the same person
fun joinRoles(crew: List<Staff>): List<Staff> =
    crew
        .groupBy { it.personId }
        .values
        .mapNotNull { appearances ->
            if (appearances.size > 1) {
                appearances.first().copy(
                    order = appearances.minOf { it.order },
                    credit = appearances.joinToString(" / ") { it.credit },
                )
            } else {
                appearances.firstOrNull()
            }
        }.sortedBy { it.order }

@Composable
fun MiniProfile(
    person: Staff,
    modifier: Modifier = Modifier,
) {
    val imageState =
        remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    Column(
        modifier =
            modifier
                .padding(2.dp)
                .width(100.dp)
                .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(64.dp, 80.dp)) {
            if (imageState.value !is AsyncImagePainter.State.Success) {
                FacePlaceholder(
                    isLoading = imageState.value is AsyncImagePainter.State.Loading,
                    modifier = Modifier,
                )
            }

            if (person.faceUrl.isNotBlank()) {
                AsyncImage(
                    model = person.faceUrl,
                    onState = { state -> imageState.value = state },
                    contentDescription = "",
                    modifier =
                        modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = person.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        Text(
            text = person.credit,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun MovieLinks(
    imdbId: String?,
    tmdbId: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.external_links),
        style = MaterialTheme.typography.titleSmall,
    )
    Row {
        if (imdbId != null) {
            ClickableLogo(
                logo = R.drawable.imdb,
                url = stringResource(R.string.imdb_url) + imdbId,
            )
        }
        ClickableLogo(
            logo = R.drawable.tmdb,
            url = stringResource(R.string.tmdb_url) + tmdbId,
        )
    }
}

@Composable
fun CreditsRow(modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
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
            text = stringResource(R.string.iconduck_credits),
        )
        CreditsLink(
            text = stringResource(R.string.icon8_credits),
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
