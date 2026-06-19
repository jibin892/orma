package org.orma.project_90.media

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
actual fun rememberOrmaBusinessLogoPicker(
    onResult: (OrmaLogoPickerResult) -> Unit,
): OrmaBusinessLogoPicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            currentOnResult(OrmaLogoPickerResult.Cancelled)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            currentOnResult(context.readPickedImage(uri))
        }
    }
    return remember(launcher) {
        OrmaBusinessLogoPicker {
            launcher.launch("image/*")
        }
    }
}

private suspend fun Context.readPickedImage(uri: Uri): OrmaLogoPickerResult =
    withContext(Dispatchers.IO) {
        try {
            val contentType = contentResolver.getType(uri).orEmpty().ifBlank {
                uri.lastPathSegment.orEmpty().contentTypeFromName()
            }
            if (!contentType.isSupportedLogoContentType()) {
                return@withContext OrmaLogoPickerResult.Failure(
                    title = "Unsupported logo",
                    message = "Choose a PNG, JPG, or WebP image.",
                    code = "UNSUPPORTED_LOGO_TYPE",
                )
            }
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null || bytes.isEmpty()) {
                return@withContext OrmaLogoPickerResult.Failure(
                    title = "Logo not readable",
                    message = "ORMA could not read the selected image.",
                    code = "LOGO_READ_FAILED",
                )
            }
            OrmaLogoPickerResult.Success(
                OrmaPickedImage(
                    fileName = displayNameForUri(uri) ?: "business-logo.${contentType.logoExtension()}",
                    contentType = contentType,
                    bytes = bytes,
                ),
            )
        } catch (error: Throwable) {
            OrmaLogoPickerResult.Failure(
                title = "Logo not readable",
                message = error.message ?: "ORMA could not read the selected image.",
                code = "LOGO_READ_FAILED",
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

private fun String.contentTypeFromName(): String = when (substringAfterLast('.', "").lowercase()) {
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "webp" -> "image/webp"
    else -> "application/octet-stream"
}

private fun String.isSupportedLogoContentType(): Boolean =
    this == "image/jpeg" || this == "image/png" || this == "image/webp"

private fun String.logoExtension(): String = when (this) {
    "image/jpeg" -> "jpg"
    "image/webp" -> "webp"
    else -> "png"
}
