package org.orma.project_90.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlin.js.Promise
import kotlinx.coroutines.await
import kotlinx.coroutines.launch

@Composable
actual fun rememberOrmaCsvFilePicker(
    onResult: (OrmaCsvFilePickerResult) -> Unit,
): OrmaCsvFilePicker {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    return remember {
        OrmaCsvFilePicker {
            scope.launch {
                currentOnResult(openWebCsvPicker())
            }
        }
    }
}

private suspend fun openWebCsvPicker(): OrmaCsvFilePickerResult {
    val picked = chooseWebCsvFile().await() ?: return OrmaCsvFilePickerResult.Cancelled
    val fileName = picked.name ?: "products.csv"
    if (!fileName.endsWith(".csv", ignoreCase = true)) {
        return OrmaCsvFilePickerResult.Failure(
            title = "Unsupported file",
            message = "Choose a CSV file exported from ORMA or your spreadsheet app.",
            code = "CSV_FILE_TYPE_REQUIRED",
        )
    }
    val text = picked.text.orEmpty().trimStart('\uFEFF')
    if (text.isBlank()) {
        return OrmaCsvFilePickerResult.Failure(
            title = "CSV is empty",
            message = "Choose a CSV file with at least one product row.",
            code = "CSV_EMPTY",
        )
    }
    return OrmaCsvFilePickerResult.Success(
        OrmaPickedCsvFile(
            fileName = fileName,
            text = text,
        ),
    )
}

private external interface JsPickedCsvFile {
    val name: String?
    val text: String?
}

private fun chooseWebCsvFile(): Promise<JsPickedCsvFile?> = js(
    """
    new Promise((resolve) => {
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = '.csv,text/csv';
      input.style.position = 'fixed';
      input.style.left = '-10000px';
      input.addEventListener('change', () => {
        const file = input.files && input.files[0];
        input.remove();
        if (!file) {
          resolve(null);
          return;
        }
        if (file.size > 2 * 1024 * 1024) {
          resolve({ name: file.name, text: '' });
          return;
        }
        const reader = new FileReader();
        reader.onload = () => resolve({ name: file.name, text: String(reader.result || '') });
        reader.onerror = () => resolve(null);
        reader.readAsText(file);
      }, { once: true });
      document.body.appendChild(input);
      input.click();
    })
    """,
)
