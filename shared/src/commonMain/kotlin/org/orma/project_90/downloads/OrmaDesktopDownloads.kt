package org.orma.project_90.downloads

object OrmaDesktopDownloads {
    const val MacUrl = "/downloads/orma-desktop-mac.dmg"
    const val WindowsUrl = "/downloads/orma-desktop-windows.msi"
}

enum class OrmaDesktopDownloadPlatform {
    Mac,
    Windows,
    Unknown,
}

expect fun isOrmaWebDownloadSurface(): Boolean

expect fun currentOrmaDesktopDownloadPlatform(): OrmaDesktopDownloadPlatform

expect fun openOrmaDownload(url: String)
