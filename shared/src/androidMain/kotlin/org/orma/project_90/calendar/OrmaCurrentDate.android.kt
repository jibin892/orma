package org.orma.project_90.calendar

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal actual fun ormaCurrentIsoDate(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
