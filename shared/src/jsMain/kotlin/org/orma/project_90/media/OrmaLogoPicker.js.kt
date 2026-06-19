package org.orma.project_90.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlin.js.Promise
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.khronos.webgl.Int8Array

@Composable
actual fun rememberOrmaBusinessLogoPicker(
    onResult: (OrmaLogoPickerResult) -> Unit,
): OrmaBusinessLogoPicker {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    return remember {
        OrmaBusinessLogoPicker {
            scope.launch {
                currentOnResult(openWebLogoPicker())
            }
        }
    }
}

private suspend fun openWebLogoPicker(): OrmaLogoPickerResult {
    val picked = chooseWebImageFile().await() ?: return OrmaLogoPickerResult.Cancelled
    val fileName = picked.name ?: "business-logo.png"
    val contentType = picked.type.orEmpty().ifBlank { fileName.contentTypeFromName() }
    if (!contentType.isSupportedLogoContentType()) {
        return OrmaLogoPickerResult.Failure(
            title = "Unsupported logo",
            message = "Choose a PNG, JPG, or WebP image.",
            code = "UNSUPPORTED_LOGO_TYPE",
        )
    }
    val bytes = picked.bytes.toByteArray()
    if (bytes.isEmpty()) {
        return OrmaLogoPickerResult.Failure(
            title = "Logo not readable",
            message = "ORMA could not read the selected image.",
            code = "LOGO_READ_FAILED",
        )
    }
    return OrmaLogoPickerResult.Success(
        OrmaPickedImage(
            fileName = fileName,
            contentType = contentType,
            bytes = bytes,
        ),
    )
}

private external interface JsPickedImageFile {
    val name: String?
    val type: String?
    val bytes: Int8Array
}

private fun chooseWebImageFile(): Promise<JsPickedImageFile?> = js(
    """
    new Promise((resolve) => {
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = 'image/png,image/jpeg,image/webp';
      input.style.position = 'fixed';
      input.style.left = '-10000px';
      input.addEventListener('change', () => {
        const file = input.files && input.files[0];
        input.remove();
        if (!file) {
          resolve(null);
          return;
        }
        const reader = new FileReader();
        reader.onload = () => {
          resolve({
            name: file.name,
            type: file.type,
            bytes: new Int8Array(reader.result)
          });
        };
        reader.onerror = () => resolve(null);
        reader.readAsArrayBuffer(file);
      }, { once: true });
      document.body.appendChild(input);
      input.click();
    })
    """,
)

private fun Int8Array.toByteArray(): ByteArray {
    val output = ByteArray(length)
    copyInt8ArrayToByteArray(this, output)
    return output
}

@Suppress("UNUSED_PARAMETER")
private fun copyInt8ArrayToByteArray(
    source: Int8Array,
    target: ByteArray,
): Unit = js(
    """
    for (let index = 0; index < source.length; index += 1) {
      target[index] = source[index];
    }
    """,
)

private fun String.contentTypeFromName(): String = when (substringAfterLast('.', "").lowercase()) {
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "webp" -> "image/webp"
    else -> "application/octet-stream"
}

private fun String.isSupportedLogoContentType(): Boolean =
    this == "image/jpeg" || this == "image/png" || this == "image/webp"
