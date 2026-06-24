package org.orma.project_90.calendar

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

internal actual fun ormaCurrentIsoDate(): String =
    NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd"
    }.stringFromDate(NSDate())
