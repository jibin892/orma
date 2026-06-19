package org.orma.project_90.media

import androidx.compose.runtime.Composable

data class OrmaPickedImage(
    val fileName: String,
    val contentType: String,
    val bytes: ByteArray,
) {
    val sizeBytes: Int
        get() = bytes.size
}

sealed interface OrmaLogoPickerResult {
    data class Success(
        val image: OrmaPickedImage,
    ) : OrmaLogoPickerResult

    data object Cancelled : OrmaLogoPickerResult

    data class Failure(
        val title: String,
        val message: String,
        val code: String? = null,
    ) : OrmaLogoPickerResult
}

class OrmaBusinessLogoPicker internal constructor(
    private val launchPicker: () -> Unit,
) {
    fun launch() {
        launchPicker()
    }
}

@Composable
expect fun rememberOrmaBusinessLogoPicker(
    onResult: (OrmaLogoPickerResult) -> Unit,
): OrmaBusinessLogoPicker
