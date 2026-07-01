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

            override fun openPdf(fileName: String, pdfBase64: String): Boolean =
                openPdfFromBrowser(fileName = fileName, pdfBase64 = pdfBase64)

            override fun printHtml(title: String, html: String): Boolean =
                printHtmlFromBrowser(title = title, html = html)

            override fun printReceipt(
                title: String,
                html: String,
                text: String,
                target: OrmaPrintTarget?,
            ): Boolean {
                if (target?.connectionType?.trim()?.lowercase() == "local_agent") {
                    return printReceiptWithLocalAgentOrBrowser(
                        title = title,
                        html = html,
                        text = text,
                        targetName = target.name,
                        connectionType = target.connectionType,
                        address = target.address.orEmpty(),
                        paperWidthMm = target.paperWidthMm,
                        dpi = target.dpi,
                    )
                }
                return printHtmlFromBrowser(title = title, html = html)
            }
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
            const userAgent = (typeof navigator !== 'undefined' && navigator.userAgent) || '';
            const isMobile = /Android|iPhone|iPad|iPod/i.test(userAgent);
            if (isMobile && typeof navigator !== 'undefined' && typeof File !== 'undefined') {
              const file = new File([blob], safeName, { type: 'application/pdf' });
              if (
                typeof navigator.canShare === 'function' &&
                typeof navigator.share === 'function' &&
                navigator.canShare({ files: [file] })
              ) {
                navigator.share({ files: [file], title: safeName }).catch(function () {});
                return true;
              }
            }
            const url = URL.createObjectURL(blob);
            const anchor = document.createElement('a');
            anchor.href = url;
            anchor.download = safeName;
            anchor.style.display = 'none';
            document.body.appendChild(anchor);
            anchor.click();
            if (/iPhone|iPad|iPod/i.test(userAgent) && typeof window !== 'undefined' && typeof window.open === 'function') {
              window.setTimeout(function () {
                window.open(url, '_blank');
              }, 80);
            }
            window.setTimeout(function () {
              URL.revokeObjectURL(url);
              if (anchor.parentNode) anchor.parentNode.removeChild(anchor);
            }, isMobile ? 60000 : 0);
            return true;
          } catch (error) {
            return false;
          }
        })()
        """,
    ).unsafeCast<Boolean>()

private fun openPdfFromBrowser(fileName: String, pdfBase64: String): Boolean =
    js(
        """
        (() => {
          try {
            if (
              typeof Blob === 'undefined' ||
              typeof URL === 'undefined' ||
              typeof atob === 'undefined' ||
              typeof window === 'undefined' ||
              typeof window.open !== 'function'
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
            const opened = window.open(url, '_blank', 'noopener,noreferrer');
            if (!opened) {
              URL.revokeObjectURL(url);
              return false;
            }
            try {
              opened.document.title = safeName;
            } catch (error) {}
            window.setTimeout(function () {
              URL.revokeObjectURL(url);
            }, 60000);
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
            const writeAndPrint = function (targetWindow, cleanup) {
              targetWindow.document.open();
              targetWindow.document.write(html);
              targetWindow.document.close();
              targetWindow.document.title = title || 'ORMA receipt';
              targetWindow.focus();
              targetWindow.setTimeout(function () {
                targetWindow.print();
                if (typeof cleanup === 'function') {
                  targetWindow.setTimeout(cleanup, 1000);
                }
              }, 180);
            };
            const printWindow = window.open('', '_blank', 'width=420,height=720');
            if (printWindow && printWindow.document) {
              writeAndPrint(printWindow);
              return true;
            }
            if (typeof document === 'undefined' || !document.body) return false;
            const iframe = document.createElement('iframe');
            iframe.style.position = 'fixed';
            iframe.style.right = '0';
            iframe.style.bottom = '0';
            iframe.style.width = '0';
            iframe.style.height = '0';
            iframe.style.border = '0';
            iframe.setAttribute('aria-hidden', 'true');
            document.body.appendChild(iframe);
            const frameWindow = iframe.contentWindow;
            if (!frameWindow || !frameWindow.document) {
              if (iframe.parentNode) iframe.parentNode.removeChild(iframe);
              return false;
            }
            writeAndPrint(frameWindow, function () {
              if (iframe.parentNode) iframe.parentNode.removeChild(iframe);
            });
            return true;
          } catch (error) {
            return false;
          }
        })()
        """,
    ).unsafeCast<Boolean>()

private fun printReceiptWithLocalAgentOrBrowser(
    title: String,
    html: String,
    text: String,
    targetName: String,
    connectionType: String,
    address: String,
    paperWidthMm: Int,
    dpi: Int,
): Boolean =
    js(
        """
        (() => {
          const fallback = function () {
            try {
              if (typeof window === 'undefined') return false;
              const writeAndPrint = function (targetWindow, cleanup) {
                targetWindow.document.open();
                targetWindow.document.write(html || '');
                targetWindow.document.close();
                targetWindow.document.title = title || 'ORMA receipt';
                targetWindow.focus();
                targetWindow.setTimeout(function () {
                  targetWindow.print();
                  if (typeof cleanup === 'function') {
                    targetWindow.setTimeout(cleanup, 1000);
                  }
                }, 180);
              };
              const printWindow = window.open('', '_blank', 'width=420,height=720');
              if (printWindow && printWindow.document) {
                writeAndPrint(printWindow);
                return true;
              }
              if (typeof document === 'undefined' || !document.body) return false;
              const iframe = document.createElement('iframe');
              iframe.style.position = 'fixed';
              iframe.style.right = '0';
              iframe.style.bottom = '0';
              iframe.style.width = '0';
              iframe.style.height = '0';
              iframe.style.border = '0';
              iframe.setAttribute('aria-hidden', 'true');
              document.body.appendChild(iframe);
              const frameWindow = iframe.contentWindow;
              if (!frameWindow || !frameWindow.document) {
                if (iframe.parentNode) iframe.parentNode.removeChild(iframe);
                return false;
              }
              writeAndPrint(frameWindow, function () {
                if (iframe.parentNode) iframe.parentNode.removeChild(iframe);
              });
              return true;
            } catch (error) {
              return false;
            }
          };
          try {
            if (typeof fetch !== 'function' || typeof URLSearchParams === 'undefined') {
              return fallback();
            }
            const cleanAddress = (address && String(address).trim()) || 'http://127.0.0.1:39201/print';
            const endpoint = cleanAddress.endsWith('/print') ? cleanAddress : cleanAddress.replace(/\/+$/, '') + '/print';
            const body = new URLSearchParams();
            body.set('title', title || 'ORMA receipt');
            body.set('html', html || '');
            body.set('text', text || '');
            body.set('printerName', targetName || 'Default receipt printer');
            body.set('connectionType', connectionType || 'system');
            body.set('address', address || '');
            body.set('paperWidthMm', String(paperWidthMm || 80));
            body.set('dpi', String(dpi || 203));
            fetch(endpoint, {
              method: 'POST',
              mode: 'cors',
              headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
              body
            }).then(function (response) {
              if (!response || !response.ok) fallback();
            }).catch(function () {
              fallback();
            });
            return true;
          } catch (error) {
            return fallback();
          }
        })()
        """,
    ).unsafeCast<Boolean>()
