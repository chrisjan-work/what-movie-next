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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonPin
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.data.Staff
import com.lairofpixies.whatmovienext.util.dpf
import com.lairofpixies.whatmovienext.views.components.AsyncPic
import com.lairofpixies.whatmovienext.views.components.CopyableText

@Composable
fun DirectorsRoster(
    crew: List<Staff>,
    modifier: Modifier = Modifier,
) {
    StaffRoster(
        sectionTitle = stringResource(R.string.direction_and_writing),
        staff = joinRoles(crew),
        modifier = modifier,
    )
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
fun ActorsRoster(
    cast: List<Staff>,
    modifier: Modifier = Modifier,
) {
    StaffRoster(
        sectionTitle = stringResource(R.string.cast),
        staff = cast,
        modifier = modifier,
    )
}

@Composable
fun StaffRoster(
    sectionTitle: String,
    staff: List<Staff>,
    modifier: Modifier = Modifier,
) {
    val scrollState = remember { LazyListState() }
    val startOverlayOpacity = remember { mutableFloatStateOf(0f) }
    val endOverlayOpacity = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(scrollState, staff.size) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            startOverlayOpacity.floatValue =
                if (index == 0) {
                    val item = scrollState.layoutInfo.visibleItemsInfo.first()
                    offset.toFloat() / item.size
                } else {
                    1f
                }
        }
    }

    LaunchedEffect(scrollState, staff.size) {
        snapshotFlow { scrollState.firstVisibleItemScrollOffset }
            .collect { _ ->
                endOverlayOpacity.floatValue =
                    scrollState.layoutInfo.visibleItemsInfo
                        .find { it.index == staff.size - 1 }
                        ?.let { lastItem ->
                            val visibleFraction =
                                (scrollState.layoutInfo.viewportEndOffset - lastItem.offset)
                                    .coerceAtMost(lastItem.size)
                            1f - visibleFraction.toFloat() / lastItem.size
                        } ?: 1f
            }
    }

    Column(modifier = modifier.height(186.dpf)) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleSmall,
            modifier = modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        )
        Box(modifier = modifier.fillMaxWidth()) {
            LazyRow(
                state = scrollState,
                modifier = modifier.fillMaxWidth(),
            ) {
                items(staff) {
                    MiniProfile(it)
                }
            }
            Box(
                modifier =
                    modifier
                        .size(48.dpf, 186.dpf)
                        .alpha(startOverlayOpacity.floatValue)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.background,
                                    Color.Transparent,
                                ),
                            ),
                        ),
            )
            Box(
                modifier =
                    modifier
                        .size(48.dpf, 186.dpf)
                        .alpha(endOverlayOpacity.floatValue)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background,
                                ),
                            ),
                        ).align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
fun MiniProfile(
    person: Staff,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .padding(2.dpf)
                .width(104.dpf)
                .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FacePic(
            person.faceUrl,
            modifier,
        )
        Spacer(modifier = Modifier.height(6.dp))
        CopyableText(
            text = person.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
        CopyableText(
            text = person.credit,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun FacePic(
    faceUrl: String,
    modifier: Modifier = Modifier,
) {
    AsyncPic(
        url = faceUrl,
        placeholderIcon = Icons.Outlined.PersonPin,
        width = 66.dpf,
        height = 85.dpf,
        cornerRadius = 5.dpf,
        modifier = modifier,
    )
}
