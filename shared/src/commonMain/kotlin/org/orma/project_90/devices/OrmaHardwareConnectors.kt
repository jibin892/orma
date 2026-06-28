package org.orma.project_90.devices

import androidx.compose.runtime.Composable

data class OrmaHardwareConnectorDevice(
    val name: String,
    val connectorType: String,
    val address: String? = null,
    val status: String = "detected",
)

data class OrmaHardwareConnectorSnapshot(
    val platformName: String,
    val printDevices: List<OrmaHardwareConnectorDevice> = emptyList(),
    val barcodeDevices: List<OrmaHardwareConnectorDevice> = emptyList(),
    val printFallback: String = "",
    val barcodeFallback: String = "",
)

@Composable
expect fun rememberOrmaHardwareConnectorSnapshot(): OrmaHardwareConnectorSnapshot
