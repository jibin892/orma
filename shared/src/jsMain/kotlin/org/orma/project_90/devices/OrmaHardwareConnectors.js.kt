package org.orma.project_90.devices

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberOrmaHardwareConnectorSnapshot(): OrmaHardwareConnectorSnapshot =
    remember {
        OrmaHardwareConnectorSnapshot(
            platformName = "Web",
            printDevices = listOf(
                OrmaHardwareConnectorDevice(
                    name = "Browser system print",
                    connectorType = "Browser print",
                    status = "available",
                ),
                OrmaHardwareConnectorDevice(
                    name = "ORMA desktop local print agent",
                    connectorType = "Local print agent",
                    address = "http://127.0.0.1:39201/print",
                    status = "available when desktop app is open",
                ),
            ),
            barcodeDevices = listOf(
                OrmaHardwareConnectorDevice(
                    name = "USB / Bluetooth keyboard scanner",
                    connectorType = "Keyboard wedge",
                    status = "listening",
                ),
            ),
            printFallback = "Browsers do not expose installed printer names. Use browser print, or open ORMA desktop on this computer and select the local print agent.",
            barcodeFallback = "Connect a scanner in keyboard mode and keep ORMA focused while scanning.",
        )
    }
