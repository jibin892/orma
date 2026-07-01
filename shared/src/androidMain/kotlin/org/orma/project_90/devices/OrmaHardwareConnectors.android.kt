package org.orma.project_90.devices

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberOrmaHardwareConnectorSnapshot(): OrmaHardwareConnectorSnapshot {
    val context = LocalContext.current
    val usbDevices = context.detectUsbConnectorDevices()
    val bluetoothDevices = context.detectBluetoothConnectorDevices()
    val printDevices = (usbDevices.printDevices + bluetoothDevices.printDevices)
        .distinctBy { "${it.connectorType}:${it.address}:${it.name}" }
    val barcodeDevices = (usbDevices.barcodeDevices + bluetoothDevices.barcodeDevices)
        .ifEmpty {
            listOf(
                OrmaHardwareConnectorDevice(
                    name = "USB / Bluetooth keyboard scanner",
                    connectorType = "Keyboard wedge",
                    status = "listening",
                ),
            )
        }
        .distinctBy { "${it.connectorType}:${it.address}:${it.name}" }
    return OrmaHardwareConnectorSnapshot(
        platformName = "Android",
        printDevices = printDevices,
        barcodeDevices = barcodeDevices,
        printFallback = "No USB/Bluetooth printer detected. Connect an MTP/USB thermal printer, pair Bluetooth, or use network/system print.",
        barcodeFallback = "Connect or pair a barcode scanner in keyboard mode, then scan from Sales or Products.",
    )
}

private data class AndroidConnectorDevices(
    val printDevices: List<OrmaHardwareConnectorDevice> = emptyList(),
    val barcodeDevices: List<OrmaHardwareConnectorDevice> = emptyList(),
)

private fun Context.detectUsbConnectorDevices(): AndroidConnectorDevices =
    runCatching {
        val manager = getSystemService(Context.USB_SERVICE) as? UsbManager ?: return@runCatching AndroidConnectorDevices()
        val printDevices = mutableListOf<OrmaHardwareConnectorDevice>()
        val barcodeDevices = mutableListOf<OrmaHardwareConnectorDevice>()
        manager.deviceList.values.forEach { device ->
            val displayName = device.productNameOrFallback()
            val address = "${device.vendorId}:${device.productId}"
            when {
                device.looksLikeUsbPrinter() -> {
                    printDevices += OrmaHardwareConnectorDevice(
                        name = displayName,
                        connectorType = "MTP / USB thermal",
                        address = address,
                        status = if (manager.hasPermission(device)) "connected" else "permission needed",
                    )
                }
                device.looksLikeBarcodeScanner() -> {
                    barcodeDevices += OrmaHardwareConnectorDevice(
                        name = displayName,
                        connectorType = "USB scanner",
                        address = address,
                        status = "connected",
                    )
                }
            }
        }
        AndroidConnectorDevices(printDevices = printDevices, barcodeDevices = barcodeDevices)
    }.getOrDefault(AndroidConnectorDevices())

@Suppress("MissingPermission")
private fun Context.detectBluetoothConnectorDevices(): AndroidConnectorDevices =
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            return@runCatching AndroidConnectorDevices()
        }
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return@runCatching AndroidConnectorDevices()
        val printDevices = mutableListOf<OrmaHardwareConnectorDevice>()
        val barcodeDevices = mutableListOf<OrmaHardwareConnectorDevice>()
        adapter.bondedDevices.orEmpty().forEach { device ->
            val name = device.name?.trim().orEmpty().ifBlank { device.address ?: "Bluetooth device" }
            val normalized = name.lowercase()
            val connector = OrmaHardwareConnectorDevice(
                name = name,
                connectorType = "Bluetooth",
                address = device.address,
                status = "paired",
            )
            if (normalized.contains("scan") || normalized.contains("barcode") || normalized.contains("hid")) {
                barcodeDevices += connector.copy(connectorType = "Bluetooth scanner")
            }
            if (device.looksLikeBluetoothPrinter(normalized)) {
                printDevices += connector.copy(connectorType = "Bluetooth printer")
            }
        }
        AndroidConnectorDevices(printDevices = printDevices, barcodeDevices = barcodeDevices)
    }.getOrDefault(AndroidConnectorDevices())

private fun BluetoothDevice.looksLikeBluetoothPrinter(normalizedName: String): Boolean {
    val bluetoothClass = bluetoothClass
    val classLooksPrintable =
        bluetoothClass?.majorDeviceClass == BluetoothClass.Device.Major.IMAGING
    val nameLooksPrintable =
        normalizedName.contains("print") ||
            normalizedName.contains("printer") ||
            normalizedName.contains("pos") ||
            normalizedName.contains("thermal") ||
            normalizedName.contains("receipt") ||
            normalizedName.contains("mpt") ||
            normalizedName.startsWith("pt-") ||
            normalizedName.startsWith("pt_") ||
            normalizedName.startsWith("pt ")
    return classLooksPrintable || nameLooksPrintable
}

private fun UsbDevice.productNameOrFallback(): String =
    productName?.trim()?.takeIf { it.isNotBlank() }
        ?: manufacturerName?.trim()?.takeIf { it.isNotBlank() }
        ?: deviceName

private fun UsbDevice.looksLikeUsbPrinter(): Boolean =
    deviceClass == UsbConstants.USB_CLASS_PRINTER ||
        interfacesAny { it.interfaceClass == UsbConstants.USB_CLASS_PRINTER } ||
        interfacesAny {
            it.interfaceClass == UsbConstants.USB_CLASS_VENDOR_SPEC &&
                (it.interfaceSubclass == 1 || it.endpointCount > 0)
        }

private fun UsbDevice.looksLikeBarcodeScanner(): Boolean =
    deviceClass == UsbConstants.USB_CLASS_HID ||
        interfacesAny { it.interfaceClass == UsbConstants.USB_CLASS_HID }

private fun UsbDevice.interfacesAny(predicate: (android.hardware.usb.UsbInterface) -> Boolean): Boolean {
    for (index in 0 until interfaceCount) {
        if (predicate(getInterface(index))) return true
    }
    return false
}
