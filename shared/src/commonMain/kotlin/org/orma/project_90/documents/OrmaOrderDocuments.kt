package org.orma.project_90.documents

import androidx.compose.runtime.Composable

interface OrmaOrderDocumentExporter {
    fun downloadHtml(fileName: String, html: String): Boolean
    fun downloadPdf(fileName: String, pdfBase64: String): Boolean
    fun printHtml(title: String, html: String): Boolean
}

@Composable
expect fun rememberOrmaOrderDocumentExporter(): OrmaOrderDocumentExporter
