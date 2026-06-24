package org.orma.project_90.documents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIApplication
import platform.UIKit.UIMarkupTextPrintFormatter
import platform.UIKit.UIPrintInfo
import platform.UIKit.UIPrintInteractionController

@Composable
actual fun rememberOrmaOrderDocumentExporter(): OrmaOrderDocumentExporter =
    remember {
        object : OrmaOrderDocumentExporter {
            override fun downloadHtml(fileName: String, html: String): Boolean = false
            override fun downloadPdf(fileName: String, pdfBase64: String): Boolean = false
            override fun printHtml(title: String, html: String): Boolean =
                printHtmlWithIosPrintController(title = title, html = html)
        }
    }

private fun printHtmlWithIosPrintController(
    title: String,
    html: String,
): Boolean =
    runCatching {
        val printController = UIPrintInteractionController.sharedPrintController()
        val printInfo = UIPrintInfo.printInfo()
        printInfo.jobName = title.ifBlank { "ORMA receipt" }
        printController.printInfo = printInfo
        printController.printFormatter = UIMarkupTextPrintFormatter(markupText = html)
        UIApplication.sharedApplication.keyWindow?.rootViewController ?: return@runCatching false
        printController.presentAnimated(true, completionHandler = null)
        true
    }.getOrDefault(false)
