package org.orma.project_90.files

data class OrmaCsvFileSaveResult(
    val saved: Boolean,
    val message: String,
)

expect suspend fun saveOrmaCsvFile(
    fileName: String,
    csv: String,
): OrmaCsvFileSaveResult
