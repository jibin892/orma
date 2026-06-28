package org.orma.project_90.devices

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberOrmaHardwareConnectorSnapshot(): OrmaHardwareConnectorSnapshot =
    remember {
        OrmaHardwareConnectorSnapshot(
            platformName = "iOS",
            printDevices = listOf(
                OrmaHardwareConnectorDevice(
                    name = "AirPrint / iOS print services",
                    connectorType = "System dialog",
                    status = "available",
                ),
            ),
            barcodeDevices = listOf(
                OrmaHardwareConnectorDevice(
                    name = "Bluetooth keyboard scanner",
                    connectorType = "Keyboard wedge",
                    status = "listening",
                ),
            ),
            printFallback = "iOS printer names are selected inside the AirPrint sheet.",
            barcodeFallback = "Pair a Bluetooth scanner in keyboard mode, then scan while ORMA is focused.",
        )
    }
