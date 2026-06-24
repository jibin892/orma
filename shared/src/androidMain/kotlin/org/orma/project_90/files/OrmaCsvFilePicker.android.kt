package org.orma.project_90.files

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberOrmaCsvFilePicker(
    onResult: (OrmaCsvFilePickerResult) -> Unit,
): OrmaCsvFilePicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            currentOnResult(OrmaCsvFilePickerResult.Cancelled)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            currentOnResult(context.readPickedCsvFile(uri))
        }
    }
    return remember(launcher) {
        OrmaCsvFilePicker {
            launcher.launch(arrayOf("text/*", "text/csv", "application/csv", "application/vnd.ms-excel"))
        }
    }
}

private suspend fun Context.readPickedCsvFile(uri: Uri): OrmaCsvFilePickerResult =
    withContext(Dispatchers.IO) {
        try {
            val fileName = displayNameForUri(uri) ?: "products.csv"
            if (!fileName.endsWith(".csv", ignoreCase = true)) {
                return@withContext OrmaCsvFilePickerResult.Failure(
                    title = "Unsupported file",
                    message = "Choose a CSV file exported from ORMA or your spreadsheet app.",
                    code = "CSV_FILE_TYPE_REQUIRED",
                )
            }
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null || bytes.isEmpty()) {
                return@withContext OrmaCsvFilePickerResult.Failure(
                    title = "CSV not readable",
                    message = "ORMA could not read the selected CSV file.",
                    code = "CSV_READ_FAILED",
                )
            }
            if (bytes.size > MaxCsvImportBytes) {
                return@withContext OrmaCsvFilePickerResult.Failure(
                    title = "CSV too large",
                    message = "Import up to 500 product rows at a time.",
                    code = "CSV_TOO_LARGE",
                )
            }
            OrmaCsvFilePickerResult.Success(
                OrmaPickedCsvFile(
                    fileName = fileName,
                    text = bytes.decodeToString().trimStart('\uFEFF'),
                ),
            )
        } catch (error: Throwable) {
            OrmaCsvFilePickerResult.Failure(
                title = "CSV not readable",
                message = error.message ?: "ORMA could not read the selected CSV file.",
                code = "CSV_READ_FAILED",
            )
        }
    }

private fun Context.displayNameForUri(uri: Uri): String? =
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        } else {
            null
        }
    }

private const val MaxCsvImportBytes = 2 * 1024 * 1024
