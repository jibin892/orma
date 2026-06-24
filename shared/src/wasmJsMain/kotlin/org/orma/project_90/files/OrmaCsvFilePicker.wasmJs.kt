package org.orma.project_90.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberOrmaCsvFilePicker(
    onResult: (OrmaCsvFilePickerResult) -> Unit,
): OrmaCsvFilePicker =
    remember(onResult) {
        OrmaCsvFilePicker {
            onResult(
                OrmaCsvFilePickerResult.Failure(
                    title = "CSV picker unavailable",
                    message = "Paste the CSV content into the import field on this web build.",
                    code = "WASM_CSV_PICKER_REQUIRED",
                ),
            )
        }
    }
