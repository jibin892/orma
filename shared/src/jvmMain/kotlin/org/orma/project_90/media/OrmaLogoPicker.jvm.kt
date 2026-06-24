package org.orma.project_90.media

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
actual fun rememberOrmaBusinessLogoPicker(
    onResult: (OrmaLogoPickerResult) -> Unit,
): OrmaBusinessLogoPicker {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    return remember {
        OrmaBusinessLogoPicker {
            scope.launch {
                currentOnResult(openDesktopLogoPicker())
            }
        }
    }
}

private suspend fun openDesktopLogoPicker(): OrmaLogoPickerResult =
    if (GraphicsEnvironment.isHeadless()) {
        OrmaLogoPickerResult.Failure(
            title = "Logo picker unavailable",
            message = "This desktop session cannot open a file picker.",
            code = "DESKTOP_PICKER_HEADLESS",
        )
    } else {
        val selectedFile = when (val result = openDesktopImageDialog()) {
            is DesktopImageDialogResult.Selected -> result.file
            DesktopImageDialogResult.Cancelled -> return OrmaLogoPickerResult.Cancelled
            is DesktopImageDialogResult.Failed -> return OrmaLogoPickerResult.Failure(
                title = "Logo picker unavailable",
                message = "ORMA could not open the desktop file picker. Restart the app and try again.",
                code = "DESKTOP_PICKER_OPEN_FAILED",
            )
        }
        withContext(Dispatchers.IO) {
            selectedFile.toPickedImage()
        }
    }

private sealed interface DesktopImageDialogResult {
    data class Selected(val file: File) : DesktopImageDialogResult
    data object Cancelled : DesktopImageDialogResult
    data class Failed(val error: Throwable) : DesktopImageDialogResult
}

private suspend fun openDesktopImageDialog(): DesktopImageDialogResult {
    val result = CompletableDeferred<DesktopImageDialogResult>()
    val showDialog = Runnable {
        runCatching {
            val dialog = FileDialog(activeDesktopFrame(), "Choose image", FileDialog.LOAD).apply {
                filenameFilter = FilenameFilter { _, name ->
                    name.contentTypeFromName().isSupportedLogoContentType()
                }
                file = "*.png;*.jpg;*.jpeg;*.webp"
                isMultipleMode = false
            }
            dialog.isVisible = true
            val directory = dialog.directory
            val fileName = dialog.file
            dialog.dispose()
            if (directory.isNullOrBlank() || fileName.isNullOrBlank()) {
                DesktopImageDialogResult.Cancelled
            } else {
                DesktopImageDialogResult.Selected(File(directory, fileName))
            }
        }.fold(
            onSuccess = result::complete,
            onFailure = { result.complete(DesktopImageDialogResult.Failed(it)) },
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

private fun File.toPickedImage(): OrmaLogoPickerResult {
    val contentType = name.contentTypeFromName()
    if (!contentType.isSupportedLogoContentType()) {
        return OrmaLogoPickerResult.Failure(
            title = "Unsupported logo",
            message = "Choose a PNG, JPG, or WebP image.",
            code = "UNSUPPORTED_LOGO_TYPE",
        )
    }
    val bytes = runCatching { readBytes() }.getOrElse {
        return OrmaLogoPickerResult.Failure(
            title = "Logo not readable",
            message = "ORMA could not read the selected image.",
            code = "LOGO_READ_FAILED",
        )
    }
    if (bytes.isEmpty()) {
        return OrmaLogoPickerResult.Failure(
            title = "Logo not readable",
            message = "The selected image file is empty.",
            code = "LOGO_READ_FAILED",
        )
    }
    return OrmaLogoPickerResult.Success(
        OrmaPickedImage(
            fileName = name,
            contentType = contentType,
            bytes = bytes,
        ),
    )
}

private fun String.contentTypeFromName(): String = when (substringAfterLast('.', "").lowercase()) {
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "webp" -> "image/webp"
    else -> "application/octet-stream"
}

private fun String.isSupportedLogoContentType(): Boolean =
    this == "image/jpeg" || this == "image/png" || this == "image/webp"
