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
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Rating
import com.lairofpixies.whatmovienext.models.data.isNotNegative

@Composable
fun RatingDisplay(
    mcRating: Rating,
    rtRating: Rating,
    modifier: Modifier = Modifier,
) {
    Row {
        if (mcRating.isNotNegative()) {
            RatingRow(
                logo = R.drawable.metacritic,
                text =
                    mcRating.displayValue,
                modifier = modifier.alpha(0.8f),
            )
        }
        if (rtRating.isNotNegative()) {
            RatingRow(
                logo = R.drawable.rotten_tomatoes,
                text = rtRating.displayValue,
                modifier = modifier.alpha(0.8f),
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
        modifier = modifier.padding(start = 8.dp, end = 8.dp),
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
fun MovieLinks(
    tmdbId: String,
    imdbId: String?,
    rtId: String,
    mcId: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.external_links),
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier.padding(start = 8.dp, end = 8.dp),
    )
    Row(modifier = modifier.padding(start = 8.dp, end = 8.dp)) {
        ClickableLogo(
            logo = R.drawable.tmdb,
            url = stringResource(R.string.tmdb_url) + tmdbId,
            modifier = modifier,
        )
        if (!imdbId.isNullOrBlank()) {
            ClickableLogo(
                logo = R.drawable.imdb,
                url = stringResource(R.string.imdb_url) + imdbId,
                modifier = modifier,
            )
        }
        if (rtId.isNotBlank()) {
            ClickableLogo(
                logo = R.drawable.rotten_tomatoes,
                url = stringResource(R.string.rotten_tomatoes_url) + rtId,
                modifier = modifier,
            )
        }
        if (mcId.isNotBlank()) {
            ClickableLogo(
                logo = R.drawable.metacritic,
                url = stringResource(R.string.metacritic_url) + mcId,
                modifier = modifier,
            )
        }
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
                .alpha(0.7f)
                .clickable {
                    url?.let {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                },
    )
}
