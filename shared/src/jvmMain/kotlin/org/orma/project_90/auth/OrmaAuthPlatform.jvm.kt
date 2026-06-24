package org.orma.project_90.auth

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.orma.project_90.firebase.OrmaFirebaseClientConfig
import org.orma.project_90.firebase.OrmaFirebaseConfig
import java.io.ByteArrayOutputStream
import java.awt.Desktop
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.UUID

private val ormaJvmHttpClient: HttpClient =
    HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build()

private const val DesktopGoogleAuthPreferredPort = 8754
private const val DesktopGoogleAuthWebUrl = "https://orma-web-dist-dev-api.vercel.app/"
private const val DesktopGoogleAuthCallbackPath = "/desktop-auth-complete"

actual fun currentFirebaseClientConfig(): OrmaFirebaseClientConfig =
    OrmaFirebaseConfig.desktop

actual suspend fun ormaPostJson(
    url: String,
    body: String,
): OrmaHttpResponse = postJson(url = url, body = body, bearerToken = null)

actual suspend fun ormaPostFormUrlEncoded(
    url: String,
    body: String,
): OrmaHttpResponse = postBody(
    url = url,
    body = body,
    contentType = "application/x-www-form-urlencoded",
    bearerToken = null,
)

actual suspend fun ormaPostJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse = postJson(url = url, body = body, bearerToken = bearerToken)

actual suspend fun ormaPutJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse = putJson(url = url, body = body, bearerToken = bearerToken)

actual suspend fun ormaGetAuthorized(
    url: String,
    bearerToken: String,
): OrmaHttpResponse = withContext(Dispatchers.IO) {
    val request = HttpRequest.newBuilder(URI.create(url))
        .timeout(Duration.ofSeconds(15))
        .header("Authorization", "Bearer $bearerToken")
        .GET()
        .build()
    val response = ormaJvmHttpClient.send(
        request,
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8),
    )
    OrmaHttpResponse(
        statusCode = response.statusCode(),
        body = response.body(),
    )
}

actual suspend fun ormaPostMultipartAuthorized(
    url: String,
    bearerToken: String,
    fileFieldName: String,
    fileName: String,
    contentType: String,
    bytes: ByteArray,
    fields: Map<String, String>,
): OrmaHttpResponse = withContext(Dispatchers.IO) {
    val boundary = "----OrmaBoundary${System.currentTimeMillis()}"
    val request = HttpRequest.newBuilder(URI.create(url))
        .timeout(Duration.ofSeconds(30))
        .header("Authorization", "Bearer $bearerToken")
        .header("Content-Type", "multipart/form-data; boundary=$boundary")
        .POST(HttpRequest.BodyPublishers.ofByteArray(buildMultipartBody(boundary, fields, fileFieldName, fileName, contentType, bytes)))
        .build()
    val response = ormaJvmHttpClient.send(
        request,
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8),
    )
    OrmaHttpResponse(
        statusCode = response.statusCode(),
        body = response.body(),
    )
}

private suspend fun postJson(
    url: String,
    body: String,
    bearerToken: String?,
): OrmaHttpResponse = postBody(
    url = url,
    body = body,
    contentType = "application/json",
    bearerToken = bearerToken,
    method = "POST",
)

private suspend fun putJson(
    url: String,
    body: String,
    bearerToken: String?,
): OrmaHttpResponse = postBody(
    url = url,
    body = body,
    contentType = "application/json",
    bearerToken = bearerToken,
    method = "PUT",
)

private suspend fun postBody(
    url: String,
    body: String,
    contentType: String,
    bearerToken: String?,
    method: String = "POST",
): OrmaHttpResponse = withContext(Dispatchers.IO) {
    val requestBuilder = HttpRequest.newBuilder(URI.create(url))
        .timeout(Duration.ofSeconds(15))
        .header("Content-Type", contentType)
    if (!bearerToken.isNullOrBlank()) {
        requestBuilder.header("Authorization", "Bearer $bearerToken")
    }
    val bodyPublisher = HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)
    val request = if (method == "PUT") {
        requestBuilder.PUT(bodyPublisher).build()
    } else {
        requestBuilder.POST(bodyPublisher).build()
    }
    val response = ormaJvmHttpClient.send(
        request,
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8),
    )
    OrmaHttpResponse(
        statusCode = response.statusCode(),
        body = response.body(),
    )
}

private fun buildMultipartBody(
    boundary: String,
    fields: Map<String, String>,
    fileFieldName: String,
    fileName: String,
    contentType: String,
    bytes: ByteArray,
): ByteArray {
    val output = ByteArrayOutputStream()
    fields.forEach { (name, value) ->
        output.writeUtf8("--$boundary\r\n")
        output.writeUtf8("Content-Disposition: form-data; name=\"${name.multipartEscaped()}\"\r\n\r\n")
        output.writeUtf8(value)
        output.writeUtf8("\r\n")
    }
    output.writeUtf8("--$boundary\r\n")
    output.writeUtf8(
        "Content-Disposition: form-data; name=\"${fileFieldName.multipartEscaped()}\"; " +
            "filename=\"${fileName.multipartEscaped()}\"\r\n",
    )
    output.writeUtf8("Content-Type: $contentType\r\n\r\n")
    output.write(bytes)
    output.writeUtf8("\r\n--$boundary--\r\n")
    return output.toByteArray()
}

private fun ByteArrayOutputStream.writeUtf8(value: String) {
    write(value.toByteArray(StandardCharsets.UTF_8))
}

private fun String.multipartEscaped(): String =
    replace("\\", "\\\\").replace("\"", "\\\"")

actual suspend fun requestGoogleIdToken(
    config: OrmaFirebaseClientConfig,
): OrmaGoogleSignInResult = withContext(Dispatchers.IO) {
    if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        return@withContext OrmaGoogleSignInResult.Failure(
            title = "Google browser unavailable",
            message = "Desktop Google sign-in needs a system browser to open the Firebase Google auth page.",
            code = "GOOGLE_DESKTOP_BROWSER_UNAVAILABLE",
        )
    }

    val deferred = CompletableDeferred<OrmaGoogleSignInResult>()
    val server = try {
        createDesktopGoogleAuthServer()
    } catch (error: Throwable) {
        return@withContext OrmaGoogleSignInResult.Failure(
            title = "Google sign-in cannot open",
            message = "ORMA could not start the local desktop sign-in callback. Close any other ORMA sign-in window, restart the desktop app, and try again.",
            code = "GOOGLE_DESKTOP_LOCALHOST_UNAVAILABLE",
        )
    }
    val state = UUID.randomUUID().toString()
    val authUrl = desktopGoogleAuthUrl(
        port = server.address.port,
        state = state,
    )

    server.createContext("/") { exchange ->
        when {
            exchange.requestMethod == "GET" && exchange.requestURI.path == "/" -> {
                exchange.writeHtml(desktopGoogleRedirectPage(authUrl))
            }
            exchange.requestMethod == "GET" && exchange.requestURI.path == DesktopGoogleAuthCallbackPath -> {
                exchange.writeHtml(desktopGoogleCallbackPage(expectedState = state))
            }
            exchange.requestMethod == "POST" && exchange.requestURI.path == "/complete" -> {
                val body = exchange.requestBody.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                if (body.jsonStringValue("state") != state) {
                    exchange.sendResponseHeaders(403, -1)
                    exchange.close()
                    return@createContext
                }
                val idToken = body.jsonStringValue("idToken")
                val firebaseIdToken = body.jsonStringValue("firebaseIdToken")
                val errorMessage = body.jsonStringValue("errorMessage")
                val errorCode = body.jsonStringValue("errorCode")
                val result = when {
                    !idToken.isNullOrBlank() -> OrmaGoogleSignInResult.Success(idToken = idToken)
                    !firebaseIdToken.isNullOrBlank() -> OrmaGoogleSignInResult.ExistingFirebaseSession(
                        session = OrmaAuthSession(
                            uid = body.jsonStringValue("uid").orEmpty(),
                            provider = body.jsonStringValue("providerId").toOrmaAuthProvider(),
                            idToken = firebaseIdToken,
                            refreshToken = body.jsonStringValue("refreshToken").orEmpty(),
                            email = body.jsonStringValue("email"),
                            phoneNumber = body.jsonStringValue("phoneNumber"),
                            displayName = body.jsonStringValue("displayName"),
                        ),
                    )
                    else -> OrmaGoogleSignInResult.Failure(
                        title = body.jsonStringValue("errorTitle") ?: "Google sign-in failed",
                        message = errorMessage ?: "Google sign-in did not return an ID token.",
                        code = errorCode ?: "GOOGLE_DESKTOP_ID_TOKEN_MISSING",
                    )
                }
                deferred.complete(result)
                exchange.writeHtml(desktopGoogleCompletePage())
            }
            exchange.requestURI.path == "/favicon.ico" -> {
                exchange.sendResponseHeaders(204, -1)
                exchange.close()
            }
            else -> {
                exchange.sendResponseHeaders(404, -1)
                exchange.close()
            }
        }
    }

    try {
        server.start()
        Desktop.getDesktop().browse(URI.create(authUrl))
        withTimeout(180_000) { deferred.await() }
    } catch (error: Throwable) {
        OrmaGoogleSignInResult.Failure(
            title = "Google sign-in failed",
            message = error.message ?: "Desktop Google sign-in did not complete.",
            code = "GOOGLE_DESKTOP_FAILED",
        )
    } finally {
        server.stop(0)
    }
}

private fun HttpExchange.writeHtml(html: String) {
    val bytes = html.toByteArray(StandardCharsets.UTF_8)
    responseHeaders.add("Content-Type", "text/html; charset=utf-8")
    sendResponseHeaders(200, bytes.size.toLong())
    responseBody.use { output: OutputStream ->
        output.write(bytes)
    }
}

private fun createDesktopGoogleAuthServer(): HttpServer =
    try {
        HttpServer.create(InetSocketAddress("localhost", DesktopGoogleAuthPreferredPort), 0)
    } catch (_: IOException) {
        HttpServer.create(InetSocketAddress("localhost", 0), 0)
    }

private fun desktopGoogleAuthUrl(
    port: Int,
    state: String,
): String {
    val callbackUrl = "http://localhost:$port$DesktopGoogleAuthCallbackPath"
    val query = listOf(
        "desktopAuthCallback" to callbackUrl,
        "desktopAuthState" to state,
    ).joinToString("&") { (name, value) ->
        "${name.urlEncoded()}=${value.urlEncoded()}"
    }
    return "$DesktopGoogleAuthWebUrl?$query"
}

private fun String.urlEncoded(): String =
    URLEncoder.encode(this, StandardCharsets.UTF_8)

private fun desktopGoogleRedirectPage(authUrl: String): String =
    """
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="utf-8">
      <title>ORMA Google sign-in</title>
      <meta http-equiv="refresh" content="0; url=$authUrl">
    </head>
    <body>
      <p>Opening ORMA Google sign-in...</p>
      <script>window.location.replace("$authUrl");</script>
    </body>
    </html>
    """.trimIndent()

private fun desktopGoogleCallbackPage(expectedState: String): String =
    """
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>ORMA Google sign-in complete</title>
      <style>
        :root { color-scheme: light; }
        body {
          min-height: 100vh;
          margin: 0;
          display: grid;
          place-items: center;
          background: #FCFDFE;
          color: #173B3D;
          font-family: -apple-system, BlinkMacSystemFont, "Google Sans", "Segoe UI", sans-serif;
        }
        main {
          width: min(420px, calc(100vw - 40px));
          border: 1px solid #EEF2FF;
          border-radius: 28px;
          background: #FFFFFF;
          padding: 32px;
          box-shadow: 0 24px 70px rgba(79, 70, 229, 0.14);
        }
        h1 {
          margin: 0 0 10px;
          font-size: 34px;
          font-weight: 650;
          letter-spacing: 0;
        }
        p {
          margin: 0 0 24px;
          color: rgba(23, 59, 61, 0.56);
          font-size: 16px;
          line-height: 1.55;
        }
        button {
          width: 100%;
          height: 56px;
          border: 0;
          border-radius: 18px;
          background: #4F46E5;
          color: #FFFFFF;
          font: inherit;
          font-size: 16px;
          font-weight: 650;
          cursor: pointer;
        }
        button:disabled {
          cursor: wait;
          opacity: 0.68;
        }
        #status {
          margin-top: 18px;
          min-height: 22px;
          color: #EF4444;
        }
      </style>
    </head>
    <body>
      <main>
        <h1>Signed in</h1>
        <p id="message">Finishing ORMA desktop sign-in...</p>
      </main>
      <script>
        const expectedState = "$expectedState";
        const message = document.getElementById("message");

        function decodePayload(encoded) {
          const base64 = encoded.replace(/-/g, "+").replace(/_/g, "/");
          const padded = base64 + "=".repeat((4 - base64.length % 4) % 4);
          const binary = atob(padded);
          const bytes = new Uint8Array(binary.length);
          for (let index = 0; index < binary.length; index += 1) {
            bytes[index] = binary.charCodeAt(index);
          }
          return JSON.parse(new TextDecoder().decode(bytes));
        }

        async function complete(payload) {
          payload.state = payload.state || expectedState;
          await fetch("/complete", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
          });
        }

        async function finishWithError(error) {
          await complete({
            errorTitle: "Google sign-in failed",
            errorMessage: error && error.message ? error.message : "ORMA could not complete desktop sign-in.",
            errorCode: error && error.code ? error.code : "GOOGLE_DESKTOP_FAILED"
          });
        }

        async function start() {
          try {
            const fragment = new URLSearchParams(window.location.hash.slice(1));
            const encodedPayload = fragment.get("payload");
            if (!encodedPayload) throw new Error("Desktop sign-in returned without an account token.");
            const payload = decodePayload(encodedPayload);
            if (payload.state !== expectedState) throw new Error("Desktop sign-in state did not match.");
            await complete(payload);
            message.textContent = "You can return to ORMA now.";
            window.close();
          } catch (error) {
            await finishWithError(error);
            message.textContent = "Sign-in failed. You can return to ORMA and try again.";
          }
        }

        start();
      </script>
    </body>
    </html>
    """.trimIndent()
private fun desktopGoogleCompletePage(): String =
    """
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="utf-8">
      <title>ORMA Google sign-in complete</title>
    </head>
    <body>
      <p>You can return to ORMA now.</p>
      <script>window.close();</script>
    </body>
    </html>
    """.trimIndent()

private fun String.jsonStringValue(key: String): String? {
    val pattern = Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
    return pattern.find(this)?.groupValues?.get(1)?.jsonUnescapedValue()
}

private fun String.jsonUnescapedValue(): String =
    replace("\\\"", "\"")
        .replace("\\\\", "\\")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")

private fun String?.toOrmaAuthProvider(): OrmaAuthProvider = when (this) {
    "phone" -> OrmaAuthProvider.PhoneOtp
    "password" -> OrmaAuthProvider.EmailPassword
    "google.com" -> OrmaAuthProvider.Google
    else -> OrmaAuthProvider.Google
}
