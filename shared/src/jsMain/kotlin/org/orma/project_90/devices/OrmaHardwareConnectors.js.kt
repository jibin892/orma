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
                    connectorType = "System dialog",
                    status = "available",
                ),
            ),
            barcodeDevices = listOf(
                OrmaHardwareConnectorDevice(
                    name = "USB / Bluetooth keyboard scanner",
                    connectorType = "Keyboard wedge",
                    status = "listening",
                ),
            ),
            printFallback = "Browsers do not expose installed printer names. Use system print or a saved tcp://IP:9100 network printer.",
            barcodeFallback = "Connect a scanner in keyboard mode and keep ORMA focused while scanning.",
        )
    }
