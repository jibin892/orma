package org.orma.project_90.calendar

@Suppress("UnsafeCastFromDynamic")
internal actual fun ormaCurrentIsoDate(): String =
    js("new Date().toISOString().slice(0, 10)") as String
