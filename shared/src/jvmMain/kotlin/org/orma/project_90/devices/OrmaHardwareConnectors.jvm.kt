package org.orma.project_90.devices

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javax.print.PrintServiceLookup

@Composable
actual fun rememberOrmaHardwareConnectorSnapshot(): OrmaHardwareConnectorSnapshot =
    remember {
        val printers = runCatching {
            PrintServiceLookup.lookupPrintServices(null, null)
                .mapNotNull { service ->
                    service.name
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { name ->
                            OrmaHardwareConnectorDevice(
                                name = name,
                                connectorType = "System printer",
                                status = "available",
                            )
                        }
                }
        }.getOrDefault(emptyList())
        OrmaHardwareConnectorSnapshot(
            platformName = "Desktop",
            printDevices = printers,
            barcodeDevices = listOf(
                OrmaHardwareConnectorDevice(
                    name = "USB / Bluetooth keyboard scanner",
                    connectorType = "Keyboard wedge",
                    status = "listening",
                ),
            ),
            printFallback = "No OS printer found. Add a system printer, raw device path, or tcp://IP:9100 thermal printer.",
            barcodeFallback = "Connect a USB/Bluetooth barcode scanner in keyboard mode, then scan from Sales or Products.",
        )
    }
