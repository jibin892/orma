@file:Suppress("UNUSED_PARAMETER")

package org.orma.project_90.downloads

actual fun isOrmaWebDownloadSurface(): Boolean = true

actual fun currentOrmaDesktopDownloadPlatform(): OrmaDesktopDownloadPlatform =
    when (readOrmaDesktopDownloadPlatform()) {
        "mac" -> OrmaDesktopDownloadPlatform.Mac
        "windows" -> OrmaDesktopDownloadPlatform.Windows
        else -> OrmaDesktopDownloadPlatform.Unknown
    }

actual fun openOrmaDownload(url: String) {
    openOrmaDownloadUrl(url)
}

private fun readOrmaDesktopDownloadPlatform(): String = js(
    """
    (function () {
      var nav = window.navigator || {};
      var platform = "";
      if (nav.userAgentData && nav.userAgentData.platform) {
        platform = String(nav.userAgentData.platform).toLowerCase();
      } else {
        platform = String(nav.platform || nav.userAgent || "").toLowerCase();
      }
      if (platform.indexOf("mac") >= 0 || platform.indexOf("darwin") >= 0) return "mac";
      if (platform.indexOf("win") >= 0) return "windows";
      return "unknown";
    })()
    """,
)

private fun openOrmaDownloadUrl(url: String): Unit = js(
    """
    const target = url;
    fetch(target, { method: 'HEAD', cache: 'no-store' })
      .then(function (response) {
        if (response.ok) {
          window.location.href = target;
        } else {
          window.alert('This ORMA desktop installer has not been published yet. Please try again after the next release.');
        }
      })
      .catch(function () {
        window.location.href = target;
      });
    """,
)
