package org.orma.project_90.downloads

actual fun isOrmaWebDownloadSurface(): Boolean = false

actual fun currentOrmaDesktopDownloadPlatform(): OrmaDesktopDownloadPlatform = OrmaDesktopDownloadPlatform.Unknown

actual fun openOrmaDownload(url: String) = Unit
