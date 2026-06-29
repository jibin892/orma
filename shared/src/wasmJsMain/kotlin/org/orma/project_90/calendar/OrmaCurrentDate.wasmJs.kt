@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.orma.project_90.calendar

internal actual fun ormaCurrentIsoDate(): String =
    currentIsoDateJs().toString().takeIf { it.length >= 10 }?.take(10) ?: "1970-01-01"

private fun currentIsoDateJs(): JsAny? = js("new Date().toISOString()")
