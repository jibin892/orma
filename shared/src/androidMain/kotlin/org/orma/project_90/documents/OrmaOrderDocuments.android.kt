package org.orma.project_90.documents

import android.Manifest
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID

@Composable
actual fun rememberOrmaOrderDocumentExporter(): OrmaOrderDocumentExporter {
    val context = LocalContext.current
    val bluetoothPermissionLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    } else {
        null
    }
    return remember(context, bluetoothPermissionLauncher) {
        val downloadedPdfs = mutableMapOf<String, Uri>()
        object : OrmaOrderDocumentExporter {
            override fun downloadHtml(fileName: String, html: String): Boolean = false
            override fun downloadPdf(fileName: String, pdfBase64: String): Boolean =
                savePdfToDownloads(context = context, fileName = fileName, pdfBase64 = pdfBase64)
                    ?.also { downloadedPdfs[fileName] = it } != null

            override fun openPdf(fileName: String, pdfBase64: String): Boolean {
                val uri = downloadedPdfs[fileName] ?: savePdfToDownloads(
                    context = context,
                    fileName = fileName,
                    pdfBase64 = pdfBase64,
                )?.also { downloadedPdfs[fileName] = it } ?: return false
                return openPdfUri(context = context, uri = uri)
            }

            override fun printHtml(title: String, html: String): Boolean =
                printHtmlWithAndroidPrintFramework(context = context, title = title, html = html)

            override fun printReceipt(
                title: String,
                html: String,
                text: String,
                target: OrmaPrintTarget?,
            ): Boolean {
                if (target?.canUseUsbDirect() == true) {
                    val directStarted = printEscPosUsb(
                        context = context,
                        target = target,
                        text = text,
                    )
                    if (directStarted) return true
                }
                if (target?.canUseBluetoothDirect() == true) {
                    val directStarted = printEscPosBluetooth(
                        context = context,
                        target = target,
                        text = text,
                        requestBluetoothPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                bluetoothPermissionLauncher?.launch(Manifest.permission.BLUETOOTH_CONNECT)
                            }
                        },
                    )
                    if (directStarted) return true
                }
                if (target?.canUseNetworkDirect() == true) {
                    val directStarted = printEscPosNetwork(target = target, text = text)
                    if (directStarted) return true
                }
                return printHtmlWithAndroidPrintFramework(context = context, title = title, html = html)
            }
        }
    }
}

private fun savePdfToDownloads(
    context: Context,
    fileName: String,
    pdfBase64: String,
): Uri? =
    runCatching {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@runCatching null
        val safeName = safePdfFileName(fileName)
        val bytes = Base64.decode(pdfBase64, Base64.DEFAULT)
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, safeName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: return@runCatching null
        val written = runCatching {
            resolver.openOutputStream(uri)?.use { output ->
                output.write(bytes)
                output.flush()
                true
            } ?: false
        }.getOrElse {
            resolver.delete(uri, null, null)
            false
        }
        if (!written) return@runCatching null
        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        uri
    }.getOrNull()

private fun openPdfUri(
    context: Context,
    uri: Uri,
): Boolean =
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Open PDF").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
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

private fun OrmaPrintTarget.canUseBluetoothDirect(): Boolean {
    val type = connectionType.trim().lowercase()
    return type == "bluetooth"
}

private fun OrmaPrintTarget.canUseUsbDirect(): Boolean {
    val type = connectionType.trim().lowercase()
    val printerAddress = address.orEmpty().trim().lowercase()
    return type == "mtp_usb" || type == "usb" || (type == "esc_pos" && !printerAddress.startsWith("tcp://"))
}

private fun OrmaPrintTarget.canUseNetworkDirect(): Boolean {
    val type = connectionType.trim().lowercase()
    val printerAddress = address.orEmpty().trim().lowercase()
    return type == "network" || printerAddress.startsWith("tcp://")
}

private fun printEscPosBluetooth(
    context: Context,
    target: OrmaPrintTarget,
    text: String,
    requestBluetoothPermission: () -> Unit,
): Boolean =
    runCatching {
        if (!context.hasBluetoothConnectPermission()) {
            requestBluetoothPermission()
            return@runCatching false
        }
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return@runCatching false
        if (!adapter.isEnabled) return@runCatching false
        val device = adapter.bondedDevices.orEmpty().firstOrNull { it.matchesPrintTarget(target) }
            ?: return@runCatching false
        Thread {
            runCatching {
                device.createRfcommSocketToServiceRecord(SerialPortProfileUuid).use { socket ->
                    socket.connect()
                    socket.outputStream.use { output ->
                        output.write(byteArrayOf(0x1B, 0x40))
                        output.write(text.toByteArray(Charsets.UTF_8))
                        output.write(byteArrayOf(0x0A, 0x0A, 0x0A, 0x1D, 0x56, 0x42, 0x00))
                        output.flush()
                    }
                }
            }
        }.start()
        true
    }.getOrDefault(false)

private fun printEscPosUsb(
    context: Context,
    target: OrmaPrintTarget,
    text: String,
): Boolean =
    runCatching {
        val manager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return@runCatching false
        val outputTarget = manager.findUsbOutputTarget(target) ?: return@runCatching false
        if (!manager.hasPermission(outputTarget.device)) {
            manager.requestPermission(outputTarget.device, context.usbPermissionIntent(outputTarget.device))
            return@runCatching false
        }
        val bytes = receiptPrintBytes(text)
        Thread {
            runCatching {
                val connection = manager.openDevice(outputTarget.device) ?: return@runCatching
                try {
                    if (!connection.claimInterface(outputTarget.usbInterface, true)) return@runCatching
                    try {
                        connection.bulkTransfer(outputTarget.endpoint, bytes, bytes.size, 5_000)
                    } finally {
                        connection.releaseInterface(outputTarget.usbInterface)
                    }
                } finally {
                    connection.close()
                }
            }
        }.start()
        true
    }.getOrDefault(false)

private fun printEscPosNetwork(
    target: OrmaPrintTarget,
    text: String,
): Boolean =
    runCatching {
        val endpoint = target.address.orEmpty().toPrinterNetworkEndpoint() ?: return@runCatching false
        val bytes = receiptPrintBytes(text)
        Thread {
            runCatching {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(endpoint.host, endpoint.port), 3_000)
                    socket.getOutputStream().use { output ->
                        output.write(bytes)
                        output.flush()
                    }
                }
            }
        }.start()
        true
    }.getOrDefault(false)

private fun receiptPrintBytes(text: String): ByteArray =
    byteArrayOf(0x1B, 0x40) +
        text.toByteArray(Charsets.UTF_8) +
        byteArrayOf(0x0A, 0x0A, 0x0A, 0x1D, 0x56, 0x42, 0x00)

private fun Context.hasBluetoothConnectPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

private fun BluetoothDevice.matchesPrintTarget(target: OrmaPrintTarget): Boolean {
    val addressOrName = target.address.orEmpty().trim()
    val targetName = target.name.trim()
    return when {
        addressOrName.isBlank() -> name.equals(targetName, ignoreCase = true)
        address.equals(addressOrName, ignoreCase = true) -> true
        name.equals(addressOrName, ignoreCase = true) -> true
        name.equals(targetName, ignoreCase = true) -> true
        name.contains(addressOrName, ignoreCase = true) -> true
        else -> false
    }
}

private data class UsbOutputTarget(
    val device: UsbDevice,
    val usbInterface: UsbInterface,
    val endpoint: UsbEndpoint,
)

private fun UsbManager.findUsbOutputTarget(target: OrmaPrintTarget): UsbOutputTarget? {
    val outputTargets = deviceList.values
        .mapNotNull { device -> device.usbBulkOutTarget() }
    if (outputTargets.isEmpty()) return null
    return outputTargets.firstOrNull { it.device.matchesUsbPrintTarget(this, target) }
        ?: outputTargets.singleOrNull()
}

private fun UsbDevice.usbBulkOutTarget(): UsbOutputTarget? {
    for (interfaceIndex in 0 until interfaceCount) {
        val usbInterface = getInterface(interfaceIndex)
        for (endpointIndex in 0 until usbInterface.endpointCount) {
            val endpoint = usbInterface.getEndpoint(endpointIndex)
            if (
                endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                endpoint.direction == UsbConstants.USB_DIR_OUT
            ) {
                return UsbOutputTarget(
                    device = this,
                    usbInterface = usbInterface,
                    endpoint = endpoint,
                )
            }
        }
    }
    return null
}

private fun UsbDevice.matchesUsbPrintTarget(
    manager: UsbManager,
    target: OrmaPrintTarget,
): Boolean {
    val query = target.address.orEmpty().trim().ifBlank { target.name.trim() }
    if (query.isBlank()) return false
    val candidates = buildList {
        add(deviceName)
        add(deviceId.toString())
        add("$vendorId:$productId")
        add("${vendorId.toString(16)}:${productId.toString(16)}")
        if (manager.hasPermission(this@matchesUsbPrintTarget)) {
            runCatching { manufacturerName }.getOrNull()?.takeIf { it.isNotBlank() }?.let(::add)
            runCatching { productName }.getOrNull()?.takeIf { it.isNotBlank() }?.let(::add)
            runCatching { serialNumber }.getOrNull()?.takeIf { it.isNotBlank() }?.let(::add)
        }
    }
    return candidates.any { candidate ->
        candidate.equals(query, ignoreCase = true) || candidate.contains(query, ignoreCase = true)
    }
}

private fun Context.usbPermissionIntent(device: UsbDevice): PendingIntent {
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    return PendingIntent.getBroadcast(
        this,
        device.deviceId,
        Intent(OrmaUsbPermissionAction).setPackage(packageName),
        flags,
    )
}

private data class PrinterNetworkEndpoint(
    val host: String,
    val port: Int,
)

private fun String.toPrinterNetworkEndpoint(): PrinterNetworkEndpoint? {
    val raw = trim()
    if (raw.isBlank()) return null
    if (raw.startsWith("tcp://", ignoreCase = true)) {
        val withoutScheme = raw.substringAfter("://")
        val separator = withoutScheme.lastIndexOf(':')
        val host = if (separator > 0) withoutScheme.substring(0, separator) else withoutScheme
        val port = if (separator > 0) withoutScheme.substring(separator + 1).toIntOrNull() ?: 9100 else 9100
        return host.trim().takeIf { it.isNotBlank() }?.let { PrinterNetworkEndpoint(host = it, port = port) }
    }
    val separator = raw.lastIndexOf(':')
    if (separator > 0 && raw.indexOf(':') == separator) {
        val host = raw.substring(0, separator).trim().takeIf { it.isNotBlank() } ?: return null
        val port = raw.substring(separator + 1).trim().toIntOrNull() ?: return null
        return PrinterNetworkEndpoint(host = host, port = port)
    }
    return PrinterNetworkEndpoint(host = raw, port = 9100)
}

private val SerialPortProfileUuid: UUID =
    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

private const val OrmaUsbPermissionAction = "org.orma.project_90.USB_PRINTER_PERMISSION"

private fun printHtmlWithAndroidPrintFramework(
    context: Context,
    title: String,
    html: String,
): Boolean =
    runCatching {
        val jobName = title.ifBlank { "ORMA receipt" }
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = view.createPrintDocumentAdapter(jobName)
                val attributes = PrintAttributes.Builder()
                    .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                    .setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT)
                    .build()
                printManager.print(jobName, printAdapter, attributes)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        true
    }.getOrDefault(false)
