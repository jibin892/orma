package org.orma.project_90.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import java.awt.GraphicsEnvironment
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
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
    withContext(Dispatchers.IO) {
        if (GraphicsEnvironment.isHeadless()) {
            return@withContext OrmaLogoPickerResult.Failure(
                title = "Logo picker unavailable",
                message = "This desktop session cannot open a file picker.",
                code = "DESKTOP_PICKER_HEADLESS",
            )
        }
        val chooser = JFileChooser().apply {
            dialogTitle = "Choose business logo"
            fileSelectionMode = JFileChooser.FILES_ONLY
            isAcceptAllFileFilterUsed = false
            fileFilter = FileNameExtensionFilter("Logo images", "png", "jpg", "jpeg", "webp")
        }
        val result = chooser.showOpenDialog(null)
        if (result != JFileChooser.APPROVE_OPTION) {
            return@withContext OrmaLogoPickerResult.Cancelled
        }
        chooser.selectedFile.toPickedImage()
    }

private fun File.toPickedImage(): OrmaLogoPickerResult {
    val contentType = name.contentTypeFromName()
    if (!contentType.isSupportedLogoContentType()) {
        return OrmaLogoPickerResult.Failure(
            title = "Unsupported logo",
            message = "Choose a PNG, JPG, or WebP image.",
            code = "UNSUPPORTED_LOGO_TYPE",
        )
    }
    val bytes = readBytes()
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
