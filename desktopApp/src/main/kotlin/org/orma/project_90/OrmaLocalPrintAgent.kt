package org.orma.project_90

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.orma.project_90.documents.OrmaPrintTarget
import org.orma.project_90.documents.ormaPrintReceiptOnJvm
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

object OrmaLocalPrintAgent {
    private const val AgentHost = "127.0.0.1"
    private const val AgentPort = 39201
    private var server: HttpServer? = null

    fun start() {
        if (server != null) return
        val created = runCatching {
            HttpServer.create(InetSocketAddress(AgentHost, AgentPort), 0).also { httpServer ->
                httpServer.executor = Executors.newSingleThreadExecutor { runnable ->
                    Thread(runnable, "orma-local-print-agent").apply { isDaemon = true }
                }
                httpServer.createContext("/health") { exchange ->
                    exchange.withCors {
                        exchange.respond(
                            status = 200,
                            body = """{"status":"ok","service":"orma-local-print-agent"}""",
                            contentType = "application/json; charset=utf-8",
                        )
                    }
                }
                httpServer.createContext("/print") { exchange ->
                    exchange.withCors {
                        handlePrint(exchange)
                    }
                }
                httpServer.start()
            }
        }.getOrNull()
        server = created
        if (created != null) {
            Runtime.getRuntime().addShutdownHook(Thread { stop() })
        }
    }

    fun stop() {
        server?.stop(0)
        server = null
    }

    private fun handlePrint(exchange: HttpExchange) {
        if (!exchange.requestMethod.equals("POST", ignoreCase = true)) {
            exchange.respond(status = 405, body = "Method not allowed")
            return
        }
        val form = exchange.requestBody.use { input ->
            input.readBytes().toString(StandardCharsets.UTF_8).parseFormBody()
        }
        val requestedType = form["connectionType"].orEmpty().trim().lowercase()
        val systemPrinterName = form["systemPrinterName"].orEmpty().trim()
        val target = OrmaPrintTarget(
            name = if (requestedType == "local_agent") systemPrinterName else form["printerName"].orEmpty().trim(),
            connectionType = if (requestedType == "local_agent") "system" else requestedType.ifBlank { "system" },
            address = if (requestedType == "local_agent") null else form["address"].orEmpty().trim().takeIf { it.isNotBlank() },
            paperWidthMm = form["paperWidthMm"].orEmpty().toIntOrNull()?.coerceIn(48, 120) ?: 80,
            dpi = form["dpi"].orEmpty().toIntOrNull()?.coerceIn(120, 600) ?: 203,
        )
        val printed = ormaPrintReceiptOnJvm(
            title = form["title"].orEmpty().ifBlank { "ORMA receipt" },
            html = form["html"].orEmpty(),
            text = form["text"].orEmpty(),
            target = target,
        )
        if (printed) {
            exchange.respond(
                status = 200,
                body = """{"status":"printed"}""",
                contentType = "application/json; charset=utf-8",
            )
        } else {
            exchange.respond(
                status = 502,
                body = """{"status":"failed","message":"No OS printer accepted the receipt."}""",
                contentType = "application/json; charset=utf-8",
            )
        }
    }

    private fun HttpExchange.withCors(block: () -> Unit) {
        responseHeaders.set("Access-Control-Allow-Origin", "*")
        responseHeaders.set("Access-Control-Allow-Methods", "GET,POST,OPTIONS")
        responseHeaders.set("Access-Control-Allow-Headers", "Content-Type")
        if (requestMethod.equals("OPTIONS", ignoreCase = true)) {
            respond(status = 204, body = "")
            return
        }
        block()
    }

    private fun HttpExchange.respond(
        status: Int,
        body: String,
        contentType: String = "text/plain; charset=utf-8",
    ) {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        responseHeaders.set("Content-Type", contentType)
        sendResponseHeaders(status, bytes.size.toLong())
        responseBody.use { output -> output.write(bytes) }
    }

    private fun String.parseFormBody(): Map<String, String> =
        split("&")
            .filter { it.isNotBlank() }
            .mapNotNull { pair ->
                val separator = pair.indexOf("=")
                if (separator < 0) return@mapNotNull null
                val key = pair.substring(0, separator).formDecoded()
                val value = pair.substring(separator + 1).formDecoded()
                key to value
            }
            .toMap()

    private fun String.formDecoded(): String =
        URLDecoder.decode(this, StandardCharsets.UTF_8.name())
}
