package org.orma.project_90.documents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberOrmaOrderDocumentExporter(): OrmaOrderDocumentExporter =
    remember {
        object : OrmaOrderDocumentExporter {
            override fun downloadHtml(fileName: String, html: String): Boolean = false
            override fun downloadPdf(fileName: String, pdfBase64: String): Boolean = false
            override fun printHtml(title: String, html: String): Boolean = false
        }
    }
