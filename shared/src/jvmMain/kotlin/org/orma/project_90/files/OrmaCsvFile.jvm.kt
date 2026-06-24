package org.orma.project_90.files

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun saveOrmaCsvFile(
    fileName: String,
    csv: String,
): OrmaCsvFileSaveResult = withContext(Dispatchers.IO) {
    val safeName = fileName.safeCsvFileName()
    val home = System.getProperty("user.home").orEmpty()
    val downloads = Path.of(home, "Downloads")
    val directory = if (downloads.exists()) downloads else Path.of(home)
    val target = directory.resolve(safeName)
    runCatching {
        Files.createDirectories(directory)
        Files.writeString(target, csv)
        OrmaCsvFileSaveResult(
            saved = true,
            message = "Saved to ${target.toAbsolutePath()}",
        )
    }.getOrElse { error ->
        OrmaCsvFileSaveResult(
            saved = false,
            message = error.message ?: "Could not save the CSV file.",
        )
    }
}

private fun String.safeCsvFileName(): String {
    val base = trim()
        .ifBlank { "orma-products.csv" }
        .replace(Regex("[^A-Za-z0-9._-]"), "-")
        .trim('-')
        .ifBlank { "orma-products.csv" }
    return if (base.endsWith(".csv", ignoreCase = true)) base else "$base.csv"
}
