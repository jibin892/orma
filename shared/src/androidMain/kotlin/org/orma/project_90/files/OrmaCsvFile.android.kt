package org.orma.project_90.files

actual suspend fun saveOrmaCsvFile(
    fileName: String,
    csv: String,
): OrmaCsvFileSaveResult =
    OrmaCsvFileSaveResult(
        saved = true,
        message = "CSV is ready below. Copy it into a .csv file from this device.",
    )
