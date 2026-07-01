package org.orma.project_90.documents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Base64
import java.util.concurrent.TimeUnit
import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.print.SimpleDoc
import javax.print.attribute.HashPrintRequestAttributeSet

@Composable
actual fun rememberOrmaOrderDocumentExporter(): OrmaOrderDocumentExporter =
    remember {
        val downloadedPdfs = mutableMapOf<String, Path>()
        object : OrmaOrderDocumentExporter {
            override fun downloadHtml(fileName: String, html: String): Boolean =
                runCatching {
                    writeDownloadHtml(fileName = fileName, html = html)
                }.isSuccess

            override fun downloadPdf(fileName: String, pdfBase64: String): Boolean =
                runCatching {
                    downloadedPdfs[fileName] = writeDownloadPdf(fileName = fileName, pdfBase64 = pdfBase64)
                }.isSuccess

            override fun openPdf(fileName: String, pdfBase64: String): Boolean =
                runCatching {
                    if (!Desktop.isDesktopSupported()) return@runCatching false
                    val desktop = Desktop.getDesktop()
                    if (!desktop.isSupported(Desktop.Action.OPEN)) return@runCatching false
                    val file = downloadedPdfs[fileName] ?: writeDownloadPdf(fileName = fileName, pdfBase64 = pdfBase64).also {
                        downloadedPdfs[fileName] = it
                    }
                    desktop.open(file.toFile())
                    true
                }.getOrDefault(false)

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

            override fun printReceipt(
                title: String,
                html: String,
                text: String,
                target: OrmaPrintTarget?,
            ): Boolean {
                if (target != null && printReceiptDirect(target = target, text = text)) {
                    return true
                }
                return printHtml(title = title, html = html)
            }
        }
    }

private fun printReceiptDirect(
    target: OrmaPrintTarget,
    text: String,
): Boolean {
    val type = target.connectionType.trim().lowercase()
    val address = target.address.orEmpty().trim()
    val bytes = receiptPrintBytes(text)
    return when {
        type == "bluetooth" -> {
            printRawBluetoothSerialReceipt(target = target, bytes = bytes) ||
                printWithSystemPrinter(target = target, bytes = bytes) ||
                printWithLp(target = target, bytes = bytes)
        }
        type == "network" || address.startsWith("tcp://", ignoreCase = true) -> {
            printRawNetworkReceipt(address = address, bytes = bytes) ||
                printWithSystemPrinter(target = target, bytes = bytes) ||
                printWithLp(target = target, bytes = bytes)
        }
        address.startsWith("/dev/") -> {
            printRawDeviceReceipt(path = address, bytes = bytes) ||
                printWithSystemPrinter(target = target, bytes = bytes) ||
                printWithLp(target = target, bytes = bytes)
        }
        else -> {
            printWithSystemPrinter(target = target, bytes = bytes) ||
                printWithLp(target = target, bytes = bytes)
        }
    }
}

private const val ReceiptQrMarkerPrefix = "[[ORMA_QR:"
private const val ReceiptQrMarkerSuffix = "]]"

private fun receiptPrintBytes(text: String): ByteArray =
    ByteArrayOutputStream().use { output ->
        output.write(byteArrayOf(0x1B, 0x40))
        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            val qrValue = line.takeIf {
                it.startsWith(ReceiptQrMarkerPrefix) && it.endsWith(ReceiptQrMarkerSuffix)
            }?.removePrefix(ReceiptQrMarkerPrefix)?.removeSuffix(ReceiptQrMarkerSuffix)?.trim()
            if (!qrValue.isNullOrBlank()) {
                output.writeEscPosQr(qrValue)
            } else {
                output.write(rawLine.toByteArray(StandardCharsets.UTF_8))
                output.write(0x0A)
            }
        }
        output.write(byteArrayOf(0x0A, 0x0A, 0x1D, 0x56, 0x42, 0x00))
        output.toByteArray()
    }

private fun ByteArrayOutputStream.writeEscPosQr(value: String) {
    val bytes = value.toByteArray(StandardCharsets.UTF_8)
    val storeLength = bytes.size + 3
    write(byteArrayOf(0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00))
    write(byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x06))
    write(byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31))
    write(
        byteArrayOf(
            0x1D,
            0x28,
            0x6B,
            (storeLength % 256).toByte(),
            (storeLength / 256).toByte(),
            0x31,
            0x50,
            0x30,
        ),
    )
    write(bytes)
    write(byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30, 0x0A))
}

private fun printRawNetworkReceipt(
    address: String,
    bytes: ByteArray,
): Boolean =
    runCatching {
        val endpoint = address.toPrinterNetworkEndpoint() ?: return@runCatching false
        Socket().use { socket ->
            socket.connect(InetSocketAddress(endpoint.host, endpoint.port), 3_000)
            socket.getOutputStream().use { output ->
                output.write(bytes)
                output.flush()
            }
        }
        true
    }.getOrDefault(false)

private fun printRawDeviceReceipt(
    path: String,
    bytes: ByteArray,
): Boolean =
    runCatching {
        Files.write(Paths.get(path), bytes)
        true
    }.getOrDefault(false)

private fun printRawBluetoothSerialReceipt(
    target: OrmaPrintTarget,
    bytes: ByteArray,
): Boolean {
    val osName = System.getProperty("os.name").orEmpty().lowercase()
    if (!osName.contains("mac")) return false
    val serialPort = findMacBluetoothSerialPort(target) ?: return false
    return printRawDeviceReceipt(path = serialPort.toString(), bytes = bytes)
}

private fun findMacBluetoothSerialPort(target: OrmaPrintTarget): Path? =
    runCatching {
        val candidates = listOf(target.address.orEmpty(), target.name)
            .map { it.bluetoothPortToken() }
            .filter { it.isNotBlank() }
        if (candidates.isEmpty()) return@runCatching null
        val devDir = Paths.get("/dev")
        if (!Files.isDirectory(devDir)) return@runCatching null
        Files.list(devDir).use { stream ->
            stream
                .filter { path ->
                    val name = path.fileName.toString()
                    name.startsWith("cu.", ignoreCase = true) || name.startsWith("tty.", ignoreCase = true)
                }
                .sorted { first, second ->
                    val firstName = first.fileName.toString()
                    val secondName = second.fileName.toString()
                    when {
                        firstName.startsWith("cu.", ignoreCase = true) && secondName.startsWith("tty.", ignoreCase = true) -> -1
                        firstName.startsWith("tty.", ignoreCase = true) && secondName.startsWith("cu.", ignoreCase = true) -> 1
                        else -> firstName.compareTo(secondName, ignoreCase = true)
                    }
                }
                .filter { path ->
                    val portToken = path.fileName.toString().bluetoothPortToken()
                    candidates.any { candidate ->
                        portToken.contains(candidate) || candidate.contains(portToken)
                    }
                }
                .findFirst()
                .orElse(null)
        }
    }.getOrNull()

private fun String.bluetoothPortToken(): String =
    lowercase().filter { it.isLetterOrDigit() }

private fun printWithSystemPrinter(
    target: OrmaPrintTarget,
    bytes: ByteArray,
): Boolean =
    runCatching {
        val service = findJvmPrintService(target) ?: return@runCatching false
        val flavor = listOf(
            DocFlavor.BYTE_ARRAY.AUTOSENSE,
            DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8,
        ).firstOrNull(service::isDocFlavorSupported) ?: DocFlavor.BYTE_ARRAY.AUTOSENSE
        val job = service.createPrintJob()
        job.print(SimpleDoc(bytes, flavor, null), HashPrintRequestAttributeSet())
        true
    }.getOrDefault(false)

private fun findJvmPrintService(target: OrmaPrintTarget): PrintService? {
    val candidates = listOf(target.address.orEmpty(), target.name)
        .map { it.trim() }
        .filter { it.isNotBlank() }
    val services = PrintServiceLookup.lookupPrintServices(null, null).orEmpty().toList()
    return services.firstOrNull { service ->
        candidates.any { candidate -> service.name.equals(candidate, ignoreCase = true) }
    } ?: services.firstOrNull { service ->
        candidates.any { candidate ->
            service.name.contains(candidate, ignoreCase = true) || candidate.contains(service.name, ignoreCase = true)
        }
    } ?: if (candidates.isEmpty()) PrintServiceLookup.lookupDefaultPrintService() else null
}

private fun printWithLp(
    target: OrmaPrintTarget,
    bytes: ByteArray,
): Boolean =
    runCatching {
        val file = Files.createTempFile("ORMA-printer-raw", ".bin")
        Files.write(file, bytes)
        file.toFile().deleteOnExit()
        val printerName = target.address.orEmpty().trim().ifBlank { target.name.trim() }
        val command = buildList {
            add("lp")
            if (printerName.isNotBlank()) {
                add("-d")
                add(printerName)
            }
            add("-o")
            add("raw")
            add(file.toString())
        }
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        val finished = process.waitFor(5, TimeUnit.SECONDS)
        if (!finished) {
            process.destroy()
            return@runCatching false
        }
        process.exitValue() == 0
    }.getOrDefault(false)

private data class PrinterNetworkEndpoint(
    val host: String,
    val port: Int,
)

private fun String.toPrinterNetworkEndpoint(): PrinterNetworkEndpoint? {
    val raw = trim()
    if (raw.isBlank()) return null
    if (raw.startsWith("tcp://", ignoreCase = true)) {
        val uri = runCatching { URI(raw) }.getOrNull() ?: return null
        val host = uri.host?.takeIf { it.isNotBlank() } ?: return null
        return PrinterNetworkEndpoint(host = host, port = uri.port.takeIf { it > 0 } ?: 9100)
    }
    val portSeparator = raw.lastIndexOf(':')
    if (portSeparator > 0 && raw.indexOf(':') == portSeparator) {
        val host = raw.substring(0, portSeparator).trim().takeIf { it.isNotBlank() } ?: return null
        val port = raw.substring(portSeparator + 1).trim().toIntOrNull() ?: return null
        return PrinterNetworkEndpoint(host = host, port = port)
    }
    return PrinterNetworkEndpoint(host = raw, port = 9100)
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
