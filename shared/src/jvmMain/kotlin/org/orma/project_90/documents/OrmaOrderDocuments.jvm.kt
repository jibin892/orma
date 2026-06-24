package org.orma.project_90.documents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64

@Composable
actual fun rememberOrmaOrderDocumentExporter(): OrmaOrderDocumentExporter =
    remember {
        object : OrmaOrderDocumentExporter {
            override fun downloadHtml(fileName: String, html: String): Boolean =
                runCatching {
                    writeDownloadHtml(fileName = fileName, html = html)
                }.isSuccess

            override fun downloadPdf(fileName: String, pdfBase64: String): Boolean =
                runCatching {
                    writeDownloadPdf(fileName = fileName, pdfBase64 = pdfBase64)
                }.isSuccess

            override fun printHtml(title: String, html: String): Boolean =
                runCatching {
                    val file = writePrintableHtml(title = title, html = html)
                    if (!Desktop.isDesktopSupported()) return@runCatching false
                    val desktop = Desktop.getDesktop()
                    when {
                        desktop.isSupported(Desktop.Action.PRINT) -> desktop.print(file.toFile())
                        desktop.isSupported(Desktop.Action.OPEN) -> desktop.open(file.toFile())
                        else -> return@runCatching false
                    }
                    true
                }.getOrDefault(false)
        }
    }

private fun writeDownloadHtml(fileName: String, html: String): Path {
    val downloads = Paths.get(System.getProperty("user.home"), "Downloads")
    Files.createDirectories(downloads)
    val safeName = safeHtmlFileName(fileName)
    val dotIndex = safeName.lastIndexOf('.').takeIf { it > 0 } ?: safeName.length
    val stem = safeName.substring(0, dotIndex)
    val extension = safeName.substring(dotIndex).ifBlank { ".html" }
    var target = downloads.resolve(safeName)
    var index = 2
    while (Files.exists(target)) {
        target = downloads.resolve("$stem-$index$extension")
        index += 1
    }
    Files.write(target, html.toByteArray(StandardCharsets.UTF_8))
    return target
}

private fun writeDownloadPdf(fileName: String, pdfBase64: String): Path {
    val downloads = Paths.get(System.getProperty("user.home"), "Downloads")
    Files.createDirectories(downloads)
    val safeName = safeDocumentFileName(fileName, extension = ".pdf", fallback = "orma-invoice")
    val dotIndex = safeName.lastIndexOf('.').takeIf { it > 0 } ?: safeName.length
    val stem = safeName.substring(0, dotIndex)
    val extension = safeName.substring(dotIndex).ifBlank { ".pdf" }
    var target = downloads.resolve(safeName)
    var index = 2
    while (Files.exists(target)) {
        target = downloads.resolve("$stem-$index$extension")
        index += 1
    }
    Files.write(target, Base64.getDecoder().decode(pdfBase64))
    return target
}

private fun writePrintableHtml(title: String, html: String): Path {
    val stem = safeHtmlFileName(title)
        .removeSuffix(".html")
        .ifBlank { "orma-receipt" }
    val file = Files.createTempFile(stem, ".html")
    Files.write(file, html.toByteArray(StandardCharsets.UTF_8))
    file.toFile().deleteOnExit()
    return file
}

private fun safeHtmlFileName(value: String): String {
    return safeDocumentFileName(value = value, extension = ".html", fallback = "orma-document")
}

private fun safeDocumentFileName(
    value: String,
    extension: String,
    fallback: String,
): String {
    val normalized = value
        .trim()
        .replace(Regex("[\\\\/:*?\"<>|]+"), "-")
        .replace(Regex("\\s+"), "-")
        .trim('-')
        .ifBlank { fallback }
    return if (normalized.endsWith(extension, ignoreCase = true)) normalized else "$normalized$extension"
}
