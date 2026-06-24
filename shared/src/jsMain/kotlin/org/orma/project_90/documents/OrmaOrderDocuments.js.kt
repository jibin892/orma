package org.orma.project_90.documents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberOrmaOrderDocumentExporter(): OrmaOrderDocumentExporter =
    remember {
        object : OrmaOrderDocumentExporter {
            override fun downloadHtml(fileName: String, html: String): Boolean =
                downloadHtmlFromBrowser(fileName = fileName, html = html)

            override fun downloadPdf(fileName: String, pdfBase64: String): Boolean =
                downloadPdfFromBrowser(fileName = fileName, pdfBase64 = pdfBase64)

            override fun printHtml(title: String, html: String): Boolean =
                printHtmlFromBrowser(title = title, html = html)
        }
    }

private fun downloadHtmlFromBrowser(fileName: String, html: String): Boolean =
    js(
        """
        (() => {
          try {
            if (typeof document === 'undefined' || typeof Blob === 'undefined' || typeof URL === 'undefined') {
              return false;
            }
            const blob = new Blob([html], { type: 'text/html;charset=utf-8' });
            const url = URL.createObjectURL(blob);
            const anchor = document.createElement('a');
            anchor.href = url;
            anchor.download = fileName || 'orma-document.html';
            anchor.style.display = 'none';
            document.body.appendChild(anchor);
            anchor.click();
            window.setTimeout(function () {
              URL.revokeObjectURL(url);
              if (anchor.parentNode) anchor.parentNode.removeChild(anchor);
            }, 0);
            return true;
          } catch (error) {
            return false;
          }
        })()
        """,
    ).unsafeCast<Boolean>()

private fun downloadPdfFromBrowser(fileName: String, pdfBase64: String): Boolean =
    js(
        """
        (() => {
          try {
            if (
              typeof document === 'undefined' ||
              typeof Blob === 'undefined' ||
              typeof URL === 'undefined' ||
              typeof atob === 'undefined'
            ) {
              return false;
            }
            const binary = atob(pdfBase64 || '');
            const bytes = new Uint8Array(binary.length);
            for (let index = 0; index < binary.length; index += 1) {
              bytes[index] = binary.charCodeAt(index);
            }
            const safeName = (fileName && String(fileName).trim()) || 'orma-invoice.pdf';
            const blob = new Blob([bytes], { type: 'application/pdf' });
            const url = URL.createObjectURL(blob);
            const anchor = document.createElement('a');
            anchor.href = url;
            anchor.download = safeName;
            anchor.style.display = 'none';
            document.body.appendChild(anchor);
            anchor.click();
            window.setTimeout(function () {
              URL.revokeObjectURL(url);
              if (anchor.parentNode) anchor.parentNode.removeChild(anchor);
            }, 0);
            return true;
          } catch (error) {
            return false;
          }
        })()
        """,
    ).unsafeCast<Boolean>()

private fun printHtmlFromBrowser(title: String, html: String): Boolean =
    js(
        """
        (() => {
          try {
            if (typeof window === 'undefined') return false;
            const printWindow = window.open('', '_blank', 'width=420,height=720');
            if (!printWindow || !printWindow.document) return false;
            printWindow.document.open();
            printWindow.document.write(html);
            printWindow.document.close();
            printWindow.document.title = title || 'ORMA receipt';
            printWindow.focus();
            printWindow.setTimeout(function () {
              printWindow.print();
            }, 180);
            return true;
          } catch (error) {
            return false;
          }
        })()
        """,
    ).unsafeCast<Boolean>()
