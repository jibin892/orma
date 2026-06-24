package org.orma.project_90.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.io.File
import java.io.FilenameFilter
import javax.swing.SwingUtilities
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberOrmaCsvFilePicker(
    onResult: (OrmaCsvFilePickerResult) -> Unit,
): OrmaCsvFilePicker {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    return remember {
        OrmaCsvFilePicker {
            scope.launch {
                currentOnResult(openDesktopCsvPicker())
            }
        }
    }
}

private suspend fun openDesktopCsvPicker(): OrmaCsvFilePickerResult =
    if (GraphicsEnvironment.isHeadless()) {
        OrmaCsvFilePickerResult.Failure(
            title = "CSV picker unavailable",
            message = "This desktop session cannot open a file picker.",
            code = "DESKTOP_PICKER_HEADLESS",
        )
    } else {
        val selectedFile = when (val result = openDesktopCsvDialog()) {
            is DesktopCsvDialogResult.Selected -> result.file
            DesktopCsvDialogResult.Cancelled -> return OrmaCsvFilePickerResult.Cancelled
            is DesktopCsvDialogResult.Failed -> return OrmaCsvFilePickerResult.Failure(
                title = "CSV picker unavailable",
                message = "ORMA could not open the desktop file picker. Restart the app and try again.",
                code = "DESKTOP_PICKER_OPEN_FAILED",
            )
        }
        withContext(Dispatchers.IO) {
            selectedFile.toPickedCsvFile()
        }
    }

private sealed interface DesktopCsvDialogResult {
    data class Selected(val file: File) : DesktopCsvDialogResult
    data object Cancelled : DesktopCsvDialogResult
    data class Failed(val error: Throwable) : DesktopCsvDialogResult
}

private suspend fun openDesktopCsvDialog(): DesktopCsvDialogResult {
    val result = CompletableDeferred<DesktopCsvDialogResult>()
    val showDialog = Runnable {
        runCatching {
            val dialog = FileDialog(activeDesktopFrame(), "Choose product CSV", FileDialog.LOAD).apply {
                filenameFilter = FilenameFilter { _, name -> name.endsWith(".csv", ignoreCase = true) }
                file = "*.csv"
                isMultipleMode = false
            }
            dialog.isVisible = true
            val directory = dialog.directory
            val fileName = dialog.file
            dialog.dispose()
            if (directory.isNullOrBlank() || fileName.isNullOrBlank()) {
                DesktopCsvDialogResult.Cancelled
            } else {
                DesktopCsvDialogResult.Selected(File(directory, fileName))
            }
        }.fold(
            onSuccess = result::complete,
            onFailure = { result.complete(DesktopCsvDialogResult.Failed(it)) },
        )
    }

    if (SwingUtilities.isEventDispatchThread()) {
        showDialog.run()
    } else {
        SwingUtilities.invokeLater(showDialog)
    }
    return result.await()
}

private fun activeDesktopFrame(): Frame? =
    Frame.getFrames().firstOrNull { it.isActive }
        ?: Frame.getFrames().firstOrNull { it.isVisible }

private fun File.toPickedCsvFile(): OrmaCsvFilePickerResult {
    if (!name.endsWith(".csv", ignoreCase = true)) {
        return OrmaCsvFilePickerResult.Failure(
            title = "Unsupported file",
            message = "Choose a CSV file exported from ORMA or your spreadsheet app.",
            code = "CSV_FILE_TYPE_REQUIRED",
        )
    }
    if (length() > MaxCsvImportBytes) {
        return OrmaCsvFilePickerResult.Failure(
            title = "CSV too large",
            message = "Import up to 500 product rows at a time.",
            code = "CSV_TOO_LARGE",
        )
    }
    val text = runCatching { readText().trimStart('\uFEFF') }.getOrElse {
        return OrmaCsvFilePickerResult.Failure(
            title = "CSV not readable",
            message = "ORMA could not read the selected CSV file.",
            code = "CSV_READ_FAILED",
        )
    }
    if (text.isBlank()) {
        return OrmaCsvFilePickerResult.Failure(
            title = "CSV is empty",
            message = "Choose a CSV file with at least one product row.",
            code = "CSV_EMPTY",
        )
    }
    return OrmaCsvFilePickerResult.Success(
        OrmaPickedCsvFile(
            fileName = name,
            text = text,
        ),
    )
}

private const val MaxCsvImportBytes = 2L * 1024L * 1024L
