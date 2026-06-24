package org.orma.project_90.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.memcpy

private var activeIosCsvPickerDelegate: IosCsvPickerDelegate? = null
private var activeIosCsvPicker: UIDocumentPickerViewController? = null

@Composable
actual fun rememberOrmaCsvFilePicker(
    onResult: (OrmaCsvFilePickerResult) -> Unit,
): OrmaCsvFilePicker =
    remember(onResult) {
        OrmaCsvFilePicker {
            val presenter = UIApplication.sharedApplication.keyWindow?.rootViewController
            if (presenter == null) {
                onResult(
                    OrmaCsvFilePickerResult.Failure(
                        title = "CSV picker unavailable",
                        message = "ORMA could not find a visible iOS screen to open the file picker.",
                        code = "IOS_CSV_PICKER_PRESENTER_MISSING",
                    ),
                )
                return@OrmaCsvFilePicker
            }
            openIosCsvPicker(
                presenter = presenter,
                onResult = onResult,
            )
        }
    }

private fun openIosCsvPicker(
    presenter: UIViewController,
    onResult: (OrmaCsvFilePickerResult) -> Unit,
) {
    val picker = UIDocumentPickerViewController(
        documentTypes = listOf(
            "public.comma-separated-values-text",
            "public.plain-text",
            "public.text",
        ),
        inMode = UIDocumentPickerMode.UIDocumentPickerModeImport,
    ).apply {
        allowsMultipleSelection = false
        shouldShowFileExtensions = true
    }
    val delegate = IosCsvPickerDelegate(
        picker = picker,
        onResult = onResult,
    )
    picker.delegate = delegate
    activeIosCsvPicker = picker
    activeIosCsvPickerDelegate = delegate
    presenter.presentViewController(
        viewControllerToPresent = picker,
        animated = true,
        completion = null,
    )
}

private class IosCsvPickerDelegate(
    private val picker: UIDocumentPickerViewController,
    private val onResult: (OrmaCsvFilePickerResult) -> Unit,
) : NSObject(),
    UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        finish(url.toPickedCsvFile())
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        finish(OrmaCsvFilePickerResult.Cancelled)
    }

    private fun finish(result: OrmaCsvFilePickerResult) {
        picker.dismissViewControllerAnimated(true) {
            onResult(result)
            activeIosCsvPicker = null
            activeIosCsvPickerDelegate = null
        }
    }
}

private fun NSURL?.toPickedCsvFile(): OrmaCsvFilePickerResult {
    val url = this ?: return OrmaCsvFilePickerResult.Cancelled
    val fileName = url.lastPathComponent ?: "products.csv"
    if (!fileName.endsWith(".csv", ignoreCase = true)) {
        return OrmaCsvFilePickerResult.Failure(
            title = "Unsupported file",
            message = "Choose a CSV file exported from ORMA or your spreadsheet app.",
            code = "CSV_FILE_TYPE_REQUIRED",
        )
    }
    val accessed = url.startAccessingSecurityScopedResource()
    return try {
        val data = NSData.dataWithContentsOfURL(url)
        if (data == null || data.length.toInt() == 0) {
            OrmaCsvFilePickerResult.Failure(
                title = "CSV is empty",
                message = "Choose a CSV file with at least one product row.",
                code = "CSV_EMPTY",
            )
        } else if (data.length.toLong() > MaxCsvImportBytes) {
            OrmaCsvFilePickerResult.Failure(
                title = "CSV too large",
                message = "Import up to 500 product rows at a time.",
                code = "CSV_TOO_LARGE",
            )
        } else {
            val text = data.toByteArray().decodeToString().trimStart('\uFEFF')
            OrmaCsvFilePickerResult.Success(
                OrmaPickedCsvFile(
                    fileName = fileName,
                    text = text,
                ),
            )
        }
    } catch (error: Throwable) {
        OrmaCsvFilePickerResult.Failure(
            title = "CSV not readable",
            message = error.message ?: "ORMA could not read the selected CSV file.",
            code = "CSV_READ_FAILED",
        )
    } finally {
        if (accessed) url.stopAccessingSecurityScopedResource()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size <= 0) return ByteArray(0)
    val output = ByteArray(size)
    output.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, size.convert())
    }
    return output
}

private const val MaxCsvImportBytes = 2L * 1024L * 1024L
