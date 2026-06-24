package org.orma.project_90.documents

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberOrmaOrderDocumentExporter(): OrmaOrderDocumentExporter {
    val context = LocalContext.current
    return remember(context) {
        object : OrmaOrderDocumentExporter {
            override fun downloadHtml(fileName: String, html: String): Boolean = false
            override fun downloadPdf(fileName: String, pdfBase64: String): Boolean = false
            override fun printHtml(title: String, html: String): Boolean =
                printHtmlWithAndroidPrintFramework(context = context, title = title, html = html)
        }
    }
}

private fun printHtmlWithAndroidPrintFramework(
    context: Context,
    title: String,
    html: String,
): Boolean =
    runCatching {
        val jobName = title.ifBlank { "ORMA receipt" }
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = view.createPrintDocumentAdapter(jobName)
                val attributes = PrintAttributes.Builder()
                    .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                    .setMediaSize(PrintAttributes.MediaSize.UNKNOWN_PORTRAIT)
                    .build()
                printManager.print(jobName, printAdapter, attributes)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        true
    }.getOrDefault(false)
