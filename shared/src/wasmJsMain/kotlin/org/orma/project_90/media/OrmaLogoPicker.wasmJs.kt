package org.orma.project_90.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberOrmaBusinessLogoPicker(
    onResult: (OrmaLogoPickerResult) -> Unit,
): OrmaBusinessLogoPicker =
    remember {
        OrmaBusinessLogoPicker {
            onResult(
                OrmaLogoPickerResult.Failure(
                    title = "Logo picker unavailable",
                    message = "Logo picking is not wired for the Kotlin/Wasm target yet.",
                    code = "WASM_LOGO_PICKER_REQUIRED",
                ),
            )
        }
    }
