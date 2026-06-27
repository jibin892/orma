package org.orma.project_90.documents

import androidx.compose.runtime.Composable

interface OrmaOrderDocumentExporter {
    fun downloadHtml(fileName: String, html: String): Boolean
    fun downloadPdf(fileName: String, pdfBase64: String): Boolean
    fun openPdf(fileName: String, pdfBase64: String): Boolean = false
    fun printHtml(title: String, html: String): Boolean
    fun printReceipt(
        title: String,
        html: String,
        text: String,
        target: OrmaPrintTarget? = null,
    ): Boolean = printHtml(title = title, html = html)
}

data class OrmaPrintTarget(
    val name: String,
    val connectionType: String,
    val address: String?,
    val paperWidthMm: Int,
    val dpi: Int,
)

@Composable
expect fun rememberOrmaOrderDocumentExporter(): OrmaOrderDocumentExporter
