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
package com.lairofpixies.whatmovienext.views.screens.popups

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.lairofpixies.whatmovienext.R
import com.lairofpixies.whatmovienext.views.screens.UiTags
import com.lairofpixies.whatmovienext.views.state.PopupInfo

@Composable
fun PopupDialogs(
    modifier: Modifier = Modifier,
    popupInfo: PopupInfo,
    onDismiss: () -> Unit,
) {
    when (popupInfo) {
        PopupInfo.None -> {}
        PopupInfo.EmptyTitle ->
            SingleButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.EMPTY_TITLE),
                titleRes = R.string.missing_title_title,
                contentRes = R.string.error_title_is_required,
                onDismiss = onDismiss,
            )

        is PopupInfo.ConfirmDeletion ->
            TwoButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.CONFIRM_DELETION),
                titleRes = R.string.delete_forever,
                contentRes = R.string.please_confirm_deletion,
                confirmRes = R.string.confirm_deletion,
                dismissRes = R.string.cancel,
                onConfirm = popupInfo.onConfirm,
                onDismiss = onDismiss,
            )

        is PopupInfo.Searching ->
            ProgressDialog(
                modifier = modifier.testTag(UiTags.Popups.SEARCHING),
                contentRes = R.string.looking_up_database,
                onDismiss = {
                    popupInfo.onCancel()
                    onDismiss()
                },
            )

        is PopupInfo.SearchEmpty ->
            SingleButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.SEARCH_EMPTY),
                titleRes = R.string.search_empty_title,
                contentRes = R.string.search_empty_explanation,
                onDismiss = onDismiss,
            )

        is PopupInfo.MovieNotFound ->
            SingleButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.MOVIE_NOT_FOUND),
                titleRes = R.string.movie_not_found,
                contentRes = R.string.wrong_link,
                onDismiss = onDismiss,
            )

        is PopupInfo.ConnectionFailed ->
            SingleButtonDialog(
                modifier = modifier.testTag(UiTags.Popups.CONNECTION_FAILED),
                titleRes = R.string.connection_failed_title,
                contentRes = R.string.connection_failed_explanation,
                onDismiss = onDismiss,
            )

        is PopupInfo.NumberChooser ->
            NumberChooserDialog(
                modifier = modifier.testTag(UiTags.Popups.NUMBER_SELECT),
                label = popupInfo.label,
                filterValues = popupInfo.filterValues,
                valueToText = popupInfo.valueToText,
                textToValue = popupInfo.textToValue,
                onConfirm = popupInfo.onConfirm,
                onDismiss = onDismiss,
            )

        is PopupInfo.WordChooser ->
            WordChooserDialog(
                modifier = modifier.testTag(UiTags.Popups.WORD_SELECT),
                label = popupInfo.label,
                filterValues = popupInfo.filterValues,
                candidates = popupInfo.candidates,
                onConfirm = popupInfo.onConfirm,
                onDismiss = onDismiss,
            )

        is PopupInfo.Loading ->
            ProgressDialog(
                modifier = modifier.testTag(UiTags.Popups.SEARCHING),
                contentRes = R.string.processing_movie_data,
                onDismiss = {
                    popupInfo.onCancel()
                    onDismiss()
                },
            )

        is PopupInfo.ExportSaved ->
            SingleButtonDialog(
                contentRes = R.string.list_saved,
                onDismiss = onDismiss,
                titleRes = R.string.export_saved_title,
            )

        is PopupInfo.ExportSaveFailed ->
            SingleButtonDialog(
                contentRes = R.string.export_failed,
                onDismiss = onDismiss,
            )

        is PopupInfo.ImportSuccessful ->
            SingleButtonDialog(
                contentRes = R.string.list_loaded,
                onDismiss = onDismiss,
                titleRes = R.string.import_finished_title,
            )

        is PopupInfo.ImportFailed ->
            SingleButtonDialog(
                contentRes = R.string.import_failed,
                onDismiss = onDismiss,
            )
    }
}
