package org.orma.project_90.files

actual suspend fun saveOrmaCsvFile(
    fileName: String,
    csv: String,
): OrmaCsvFileSaveResult {
    val safeName = fileName.safeCsvFileName()
    return runCatching {
        downloadCsvFile(safeName, csv)
        OrmaCsvFileSaveResult(
            saved = true,
            message = "Download started: $safeName",
        )
    }.getOrElse { error ->
        OrmaCsvFileSaveResult(
            saved = false,
            message = error.message ?: "Could not start the CSV download.",
        )
    }
}

@Suppress("UNUSED_PARAMETER")
private fun downloadCsvFile(fileName: String, csv: String): Unit = js(
    """
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = fileName;
    anchor.style.display = 'none';
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
    """,
)

private fun String.safeCsvFileName(): String {
    val base = trim()
        .ifBlank { "orma-products.csv" }
        .replace(Regex("[^A-Za-z0-9._-]"), "-")
        .trim('-')
        .ifBlank { "orma-products.csv" }
    return if (base.endsWith(".csv", ignoreCase = true)) base else "$base.csv"
}
