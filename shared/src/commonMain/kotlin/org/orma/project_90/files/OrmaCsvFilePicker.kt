package org.orma.project_90.files

import androidx.compose.runtime.Composable

data class OrmaPickedCsvFile(
    val fileName: String,
    val text: String,
)

sealed interface OrmaCsvFilePickerResult {
    data class Success(
        val file: OrmaPickedCsvFile,
    ) : OrmaCsvFilePickerResult

    data object Cancelled : OrmaCsvFilePickerResult

    data class Failure(
        val title: String,
        val message: String,
        val code: String? = null,
    ) : OrmaCsvFilePickerResult
}

class OrmaCsvFilePicker internal constructor(
    private val launchPicker: () -> Unit,
) {
    fun launch() {
        launchPicker()
    }
}

@Composable
expect fun rememberOrmaCsvFilePicker(
    onResult: (OrmaCsvFilePickerResult) -> Unit,
): OrmaCsvFilePicker
