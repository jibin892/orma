package org.orma.project_90.documents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIMarkupTextPrintFormatter
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UIPrintInfo
import platform.UIKit.UIPrintInteractionController
import platform.UIKit.UIViewController
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

@Composable
actual fun rememberOrmaOrderDocumentExporter(): OrmaOrderDocumentExporter =
    remember {
        val downloadedPdfs = mutableMapOf<String, NSURL>()
        object : OrmaOrderDocumentExporter {
            override fun downloadHtml(fileName: String, html: String): Boolean = false
            override fun downloadPdf(fileName: String, pdfBase64: String): Boolean =
                savePdfToIosTemporaryFile(fileName = fileName, pdfBase64 = pdfBase64)
                    ?.also { downloadedPdfs[fileName] = it } != null

            override fun openPdf(fileName: String, pdfBase64: String): Boolean {
                val url = downloadedPdfs[fileName] ?: savePdfToIosTemporaryFile(
                    fileName = fileName,
                    pdfBase64 = pdfBase64,
                )?.also { downloadedPdfs[fileName] = it } ?: return false
                val presenter = UIApplication.sharedApplication.keyWindow?.rootViewController
                    ?: return false
                return openPdfWithIosActivityController(presenter = presenter, url = url)
            }

            override fun printHtml(title: String, html: String): Boolean =
                printHtmlWithIosPrintController(title = title, html = html)
        }
    }

private fun savePdfToIosTemporaryFile(
    fileName: String,
    pdfBase64: String,
): NSURL? =
    runCatching {
        val bytes = pdfBase64.decodeBase64Bytes() ?: return@runCatching null
        val path = "${NSTemporaryDirectory().trimEnd('/')}/${safePdfFileName(fileName)}"
        if (!writeBytesToPath(path = path, bytes = bytes)) return@runCatching null
        NSURL.fileURLWithPath(path)
    }.getOrNull()

@OptIn(ExperimentalForeignApi::class)
private fun writeBytesToPath(
    path: String,
    bytes: ByteArray,
): Boolean =
    runCatching {
        val file = fopen(path, "wb") ?: return@runCatching false
        try {
            bytes.usePinned { pinned ->
                val written = fwrite(
                    pinned.addressOf(0),
                    1.convert(),
                    bytes.size.convert(),
                    file,
                )
                written.toLong() == bytes.size.toLong()
            }
        } finally {
            fclose(file)
        }
    }
        .getOrDefault(false)

private fun String.decodeBase64Bytes(): ByteArray? {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val table = IntArray(128) { -1 }
    alphabet.forEachIndexed { index, character ->
        table[character.code] = index
    }
    val output = mutableListOf<Byte>()
    var buffer = 0
    var bits = 0
    for (character in this) {
        if (character.isWhitespace()) continue
        if (character == '=') break
        if (character.code >= table.size) return null
        val value = table[character.code]
        if (value < 0) return null
        buffer = (buffer shl 6) or value
        bits += 6
        if (bits >= 8) {
            bits -= 8
            output += ((buffer shr bits) and 0xff).toByte()
        }
    }
    return output.toByteArray()
}

private fun openPdfWithIosActivityController(
    presenter: UIViewController,
    url: NSURL,
): Boolean =
    runCatching {
        val controller = UIActivityViewController(
            activityItems = listOf(url),
            applicationActivities = null,
        )
        controller.modalPresentationStyle = UIModalPresentationFullScreen
        presenter.presentViewController(
            viewControllerToPresent = controller,
            animated = true,
            completion = null,
        )
        true
    }.getOrDefault(false)

private fun safePdfFileName(value: String): String {
    val normalized = value
        .trim()
        .replace(Regex("[\\\\/:*?\"<>|]+"), "-")
        .replace(Regex("\\s+"), "-")
        .trim('-')
        .ifBlank { "orma-invoice" }
    return if (normalized.endsWith(".pdf", ignoreCase = true)) normalized else "$normalized.pdf"
}

private fun printHtmlWithIosPrintController(
    title: String,
    html: String,
): Boolean =
    runCatching {
        val printController = UIPrintInteractionController.sharedPrintController()
        val printInfo = UIPrintInfo.printInfo()
        printInfo.jobName = title.ifBlank { "ORMA receipt" }
        printController.printInfo = printInfo
        printController.printFormatter = UIMarkupTextPrintFormatter(markupText = html)
        UIApplication.sharedApplication.keyWindow?.rootViewController ?: return@runCatching false
        printController.presentAnimated(true, completionHandler = null)
        true
    }.getOrDefault(false)
