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

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.models.mappers.PresetMapper
import com.lairofpixies.whatmovienext.util.dpf
import com.lairofpixies.whatmovienext.util.readableRuntime
import com.lairofpixies.whatmovienext.viewmodels.MovieListViewModel
import com.lairofpixies.whatmovienext.views.navigation.ButtonSpec
import com.lairofpixies.whatmovienext.views.screens.UiTags
import com.lairofpixies.whatmovienext.views.state.BottomMenuOption
import com.lairofpixies.whatmovienext.views.state.ListFilters
import com.lairofpixies.whatmovienext.views.state.ListMode
import com.lairofpixies.whatmovienext.views.state.MinMaxFilter
import com.lairofpixies.whatmovienext.views.state.PopupInfo
import com.lairofpixies.whatmovienext.views.state.SortingCriteria
import com.lairofpixies.whatmovienext.views.state.SortingDirection
import com.lairofpixies.whatmovienext.views.state.SortingSetup
import com.lairofpixies.whatmovienext.views.state.WordFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListBottomSheet(
    listViewModel: MovieListViewModel,
    modifier: Modifier = Modifier,
) {
    val scaffoldState =
        rememberBottomSheetScaffoldState(
            bottomSheetState =
                rememberStandardBottomSheetState(
                    initialValue = SheetValue.Hidden,
                    skipHiddenState = false,
                ),
        )

    // react to viewmodel menu requests
    LaunchedEffect(listViewModel.bottomMenuState) {
        listViewModel.bottomMenuState.collect { (_, isOpen) ->
            if (!isOpen && scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                scaffoldState.bottomSheetState.hide()
            } else if (isOpen && scaffoldState.bottomSheetState.currentValue != SheetValue.Expanded) {
                scaffoldState.bottomSheetState.expand()
            }
        }
    }

    // notify viewmodel if sheet has been closed
    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }.collect {
            if (it != SheetValue.Expanded && listViewModel.bottomMenuState.value.isOpen) {
                listViewModel.closeBottomMenu()
            }
        }
    }

    // content
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            BottomSheetTabs(
                bottomMenuOption =
                    listViewModel.bottomMenuState
                        .collectAsState()
                        .value.bottomMenuOption,
                selectMenu = { listViewModel.onOpenBottomMenu(it) },
            )
            BottomSheetMenu(
                listViewModel,
            )
        },
        sheetPeekHeight = 0.dp,
        content = {},
        modifier = modifier,
    )
}

@Composable
fun BottomSheetTabs(
    bottomMenuOption: BottomMenuOption,
    selectMenu: (BottomMenuOption) -> Unit,
) {
    val tabHeight = 24.dpf
    TabRow(
        selectedTabIndex = bottomMenuOption.ordinal,
        modifier = Modifier.height(tabHeight),
    ) {
        BottomMenuTab(
            currentOption = bottomMenuOption,
            tabOption = BottomMenuOption.Sorting,
            select = { selectMenu(BottomMenuOption.Sorting) },
            buttonSpec = ButtonSpec.SortingMenu,
            height = tabHeight,
            modifier = Modifier.testTag(UiTags.Buttons.SORT_TAB),
        )
        BottomMenuTab(
            currentOption = bottomMenuOption,
            tabOption = BottomMenuOption.Filtering,
            select = { selectMenu(BottomMenuOption.Filtering) },
            buttonSpec = ButtonSpec.FilterMenu,
            height = tabHeight,
            modifier = Modifier.testTag(UiTags.Buttons.FILTER_TAB),
        )
    }
}

@Composable
fun BottomMenuTab(
    currentOption: BottomMenuOption,
    tabOption: BottomMenuOption,
    select: () -> Unit,
    buttonSpec: ButtonSpec,
    modifier: Modifier = Modifier,
    height: Dp = 24.dpf,
) {
    val label = stringResource(buttonSpec.labelRes)
    LeadingIconTab(
        selected = currentOption == tabOption,
        onClick = select,
        icon = {
            Icon(
                buttonSpec.icon,
                contentDescription = label,
                modifier = Modifier.size(height / 2),
            )
        },
        text = { Text(text = label, style = MaterialTheme.typography.labelSmall) },
        modifier = modifier.height(height),
    )
}

@Composable
fun BottomSheetMenu(listViewModel: MovieListViewModel) {
    val bottomMenuOption =
        listViewModel.bottomMenuState
            .collectAsState()
            .value.bottomMenuOption
    val currentPreset = listViewModel.currentPreset.collectAsState().value

    when (bottomMenuOption) {
        BottomMenuOption.Sorting ->
            SortingMenu(
                sortingSetup = currentPreset.sortingSetup,
                onSelectAction = { criteria, direction ->
                    listViewModel.updateSortingSetup(SortingSetup(criteria, direction))
                },
            )

        BottomMenuOption.Filtering -> {
            val allGenres = listViewModel.allGenres.collectAsState().value
            val allDirectors = listViewModel.allDirectors.collectAsState().value
            FilteringMenu(
                listFilters = currentPreset.listFilters,
                onListFiltersChanged = { listViewModel.setListFilters(it) },
                presetMapper = listViewModel.presetMapper(),
                allGenres = allGenres,
                allDirectors = allDirectors,
                showPopup = { listViewModel.showPopup(it) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SortingMenu(
    sortingSetup: SortingSetup,
    onSelectAction: (SortingCriteria, SortingDirection) -> Unit,
) {
    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 12.dp, end = 12.dp, top = 8.dp)
                .testTag(UiTags.Menus.SORTING),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        SortingCriteria.entries
            .map { criteria ->
                SortingButton(
                    criteria = criteria,
                    direction = sortingSetup.direction,
                    isSelected = criteria == sortingSetup.criteria,
                    onSelectAction = onSelectAction,
                )
            }
    }
}

@Composable
fun SortingButton(
    criteria: SortingCriteria,
    direction: SortingDirection,
    isSelected: Boolean,
    onSelectAction: (SortingCriteria, SortingDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        }

    val readState =
        when {
            !isSelected -> stringResource(R.string.is_unselected)
            direction == SortingDirection.Ascending -> stringResource(R.string.ascending)
            else -> stringResource(R.string.descending)
        }
    val readableText =
        when {
            criteria == SortingCriteria.Random ->
                stringResource(R.string.shuffle) + ". " +
                    if (isSelected) stringResource(R.string.is_selected) else stringResource(R.string.is_unselected)

            criteria.readable != null ->
                stringResource(criteria.readable) + readState

            else ->
                stringResource(R.string.sort_by, stringResource(criteria.display)) + readState
        }
    Box(
        modifier =
            modifier
                .padding(3.dp)
                .size(width = 110.dpf, height = 28.dpf)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                ).clickable {
                    if (isSelected) {
                        onSelectAction(criteria, direction.opposite())
                    } else {
                        onSelectAction(criteria, SortingDirection.Default)
                    }
                }.semantics { contentDescription = readableText },
    ) {
        Text(
            text = stringResource(id = criteria.display),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Start,
            maxLines = 1,
            modifier =
                Modifier
                    .padding(5.dpf)
                    .padding(start = 2.dpf, end = 2.dpf)
                    .size(100.dpf)
                    .clearAndSetSemantics {},
        )
        // do not show arrow for random
        if (isSelected && criteria != SortingCriteria.Random) {
            Icon(
                if (direction == SortingDirection.Ascending) {
                    Icons.Outlined.ArrowUpward
                } else {
                    Icons.Outlined.ArrowDownward
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier =
                    Modifier
                        .padding(8.dp)
                        .size(12.dp)
                        .align(Alignment.CenterEnd),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilteringMenu(
    listFilters: ListFilters,
    onListFiltersChanged: (ListFilters) -> Unit,
    presetMapper: PresetMapper,
    allGenres: List<String>,
    allDirectors: List<String>,
    showPopup: (PopupInfo) -> Unit,
) {
    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 6.dp, end = 6.dp, top = 8.dp)
                .testTag(UiTags.Menus.FILTERING),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ListModeButton(
            listFilters.listMode,
            onListModeChanged = { listMode ->
                onListFiltersChanged(listFilters.copy(listMode = listMode))
            },
        )
        WordSelectButton(
            label = stringResource(R.string.by_genre),
            filterValues = listFilters.genres,
            candidates = allGenres,
            onFilterValuesChanged = { wordFilter ->
                onListFiltersChanged(listFilters.copy(genres = wordFilter))
            },
            showPopup = showPopup,
            modifier = Modifier.testTag(tag = UiTags.Buttons.GENRES_FILTER),
        )
        WordSelectButton(
            label = stringResource(R.string.by_director),
            filterValues = listFilters.directors,
            candidates = allDirectors,
            onFilterValuesChanged = { wordFilter ->
                onListFiltersChanged(listFilters.copy(directors = wordFilter))
            },
            showPopup = showPopup,
            modifier = Modifier.testTag(tag = UiTags.Buttons.DIRECTORS_FILTER),
        )
        MinMaxButton(
            label = stringResource(R.string.by_year),
            filterValues = listFilters.year,
            localContentDescription =
                generateContentDescriptionForFilter(
                    stringResource(R.string.year),
                    listFilters.year.min?.toString(),
                    listFilters.year.max?.toString(),
                ),
            onFilterValuesChanged = { minMaxFilter ->
                onListFiltersChanged(listFilters.copy(year = minMaxFilter))
            },
            valueToTextInput = { presetMapper.numberToInput(it) },
            valueToTextButton = { presetMapper.numberToButton(it) },
            textToValue = { presetMapper.inputToYear(it) },
            showPopup = showPopup,
            modifier = Modifier.testTag(tag = UiTags.Buttons.YEAR_FILTER),
        )
        MinMaxButton(
            label = stringResource(R.string.by_metacritic_score),
            labelContentDescription = stringResource(R.string.metacritic_score),
            localContentDescription =
                generateContentDescriptionForFilter(
                    stringResource(R.string.metacritic_score),
                    listFilters.mcScore.min?.toString(),
                    listFilters.mcScore.max?.toString(),
                ),
            filterValues = listFilters.mcScore,
            onFilterValuesChanged = { minMaxFilter ->
                onListFiltersChanged(listFilters.copy(mcScore = minMaxFilter))
            },
            valueToTextInput = { presetMapper.numberToInput(it) },
            valueToTextButton = { presetMapper.numberToButton(it) },
            textToValue = { presetMapper.inputToScore(it) },
            showPopup = showPopup,
            modifier = Modifier.testTag(tag = UiTags.Buttons.MC_SCORE_FILTER),
        )
        MinMaxButton(
            label = stringResource(R.string.by_rotten_tomatoes_score),
            labelContentDescription = stringResource(R.string.rotten_tomatoes_score),
            localContentDescription =
                generateContentDescriptionForFilter(
                    stringResource(R.string.rotten_tomatoes_score),
                    listFilters.rtScore.min?.toString(),
                    listFilters.rtScore.max?.toString(),
                ),
            filterValues = listFilters.rtScore,
            onFilterValuesChanged = { minMaxFilter ->
                onListFiltersChanged(listFilters.copy(rtScore = minMaxFilter))
            },
            valueToTextInput = { presetMapper.numberToInput(it) },
            valueToTextButton = { presetMapper.numberToButton(it) },
            textToValue = { presetMapper.inputToScore(it) },
            showPopup = showPopup,
            modifier = Modifier.testTag(tag = UiTags.Buttons.RT_SCORE_FILTER),
        )
        MinMaxButton(
            label = stringResource(R.string.by_runtime),
            filterValues = listFilters.runtime,
            localContentDescription =
                generateContentDescriptionForFilter(
                    stringResource(R.string.runtime),
                    listFilters.runtime.min?.let { readableRuntime(it) },
                    listFilters.runtime.max?.let { readableRuntime(it) },
                ),
            onFilterValuesChanged = { minMaxFilter ->
                onListFiltersChanged(listFilters.copy(runtime = minMaxFilter))
            },
            valueToTextInput = { presetMapper.runtimeToInput(it) },
            valueToTextButton = { presetMapper.runtimeToButton(it) },
            textToValue = { presetMapper.inputToRuntime(it) },
            showPopup = showPopup,
            modifier = Modifier.testTag(tag = UiTags.Buttons.RUNTIME_FILTER),
        )
    }
}

@Composable
fun generateContentDescriptionForFilter(
    criteriaSt: String,
    minSt: String?,
    maxSt: String?,
): String =
    when {
        minSt == null && maxSt == null -> stringResource(R.string.is_undefined)
        minSt == null -> stringResource(R.string.filter_upto, criteriaSt, maxSt ?: "")
        maxSt == null -> stringResource(R.string.filter_from, criteriaSt, minSt)
        else -> stringResource(R.string.filter_between, criteriaSt, minSt, maxSt)
    }

@Composable
fun ListModeButton(
    listMode: ListMode,
    onListModeChanged: (ListMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonSpec =
        when (listMode) {
            ListMode.ALL -> ButtonSpec.AllMoviesFilter
            ListMode.PENDING -> ButtonSpec.PendingFilter
            ListMode.WATCHED -> ButtonSpec.WatchedFilter
        }
    val isSelected = listMode != ListMode.ALL
    val borderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        }
    Row(
        modifier =
            modifier
                .padding(3.dp)
                .sizeIn(minWidth = 126.dp, minHeight = 28.dpf)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                ).padding(6.dp)
                .clickable {
                    onListModeChanged(listMode.next())
                }.testTag(tag = UiTags.Buttons.LIST_MODE),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(id = buttonSpec.labelRes),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            maxLines = 1,
            textAlign = TextAlign.Start,
        )
        Spacer(modifier = Modifier.sizeIn(minWidth = 8.dp))
        Icon(
            buttonSpec.icon,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.size(12.dp),
        )
    }
}

@Composable
fun MinMaxButton(
    label: String,
    localContentDescription: String,
    filterValues: MinMaxFilter,
    onFilterValuesChanged: (MinMaxFilter) -> Unit,
    valueToTextInput: (Int?) -> String,
    valueToTextButton: (Int?) -> String,
    textToValue: (String) -> Int?,
    showPopup: (PopupInfo) -> Unit,
    modifier: Modifier = Modifier,
    labelContentDescription: String? = null,
) {
    val isSelected = filterValues.isActive
    val borderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        }

    val launchPopup: () -> Unit = {
        showPopup(
            PopupInfo.NumberChooser(
                label = labelContentDescription ?: label,
                filterValues = filterValues,
                valueToText = valueToTextInput,
                textToValue = textToValue,
                onConfirm = onFilterValuesChanged,
            ),
        )
    }

    Row(
        modifier =
            modifier
                .padding(3.dp)
                .sizeIn(minWidth = 126.dp, minHeight = 28.dpf)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                ).padding(6.dp)
                .clickable {
                    launchPopup()
                },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Start,
            maxLines = 1,
            modifier =
                Modifier
                    .clickable {
                        if (filterValues.isNotEmpty) {
                            onFilterValuesChanged(filterValues.copy(isEnabled = !filterValues.isEnabled))
                        } else {
                            launchPopup()
                        }
                    }.semantics {
                        contentDescription = labelContentDescription ?: label
                    },
        )
        Spacer(modifier = Modifier.sizeIn(minWidth = 8.dp))
        Text(
            text = "${valueToTextButton(filterValues.min)} - ${valueToTextButton(filterValues.max)} ",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier =
                Modifier.semantics {
                    contentDescription = localContentDescription
                },
        )
    }
}

@Composable
fun WordSelectButton(
    label: String,
    filterValues: WordFilter,
    onFilterValuesChanged: (WordFilter) -> Unit,
    candidates: List<String>,
    showPopup: (PopupInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelected = filterValues.isActive
    val borderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        }

    val launchPopup: () -> Unit = {
        showPopup(
            PopupInfo.WordChooser(
                label = label,
                filterValues = filterValues,
                candidates = candidates,
                onConfirm = onFilterValuesChanged,
            ),
        )
    }

    Row(
        modifier =
            modifier
                .padding(3.dp)
                .size(width = 126.dpf, height = 28.dpf)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp),
                ).padding(6.dp)
                .clickable {
                    launchPopup()
                },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Start,
            maxLines = 1,
            modifier =
                Modifier.clickable {
                    if (filterValues.isNotEmpty) {
                        onFilterValuesChanged(filterValues.copy(isEnabled = !filterValues.isEnabled))
                    } else {
                        launchPopup()
                    }
                },
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = filterValues.words.joinToString(","),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(64.dpf),
        )
    }
}
