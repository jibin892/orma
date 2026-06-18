package org.orma.project_90.auth

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.orma.project_90.firebase.OrmaFirebaseClientConfig
import org.orma.project_90.firebase.OrmaFirebaseConfig
import java.awt.Desktop
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

private val ormaJvmHttpClient: HttpClient =
    HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build()

private const val DesktopGoogleAuthPreferredPort = 8754

actual fun currentFirebaseClientConfig(): OrmaFirebaseClientConfig =
    OrmaFirebaseConfig.desktop

actual suspend fun ormaPostJson(
    url: String,
    body: String,
): OrmaHttpResponse = postJson(url = url, body = body, bearerToken = null)

actual suspend fun ormaPostJsonAuthorized(
    url: String,
    body: String,
    bearerToken: String,
): OrmaHttpResponse = postJson(url = url, body = body, bearerToken = bearerToken)

private suspend fun postJson(
    url: String,
    body: String,
    bearerToken: String?,
): OrmaHttpResponse = withContext(Dispatchers.IO) {
    val requestBuilder = HttpRequest.newBuilder(URI.create(url))
        .timeout(Duration.ofSeconds(15))
        .header("Content-Type", "application/json")
    if (!bearerToken.isNullOrBlank()) {
        requestBuilder.header("Authorization", "Bearer $bearerToken")
    }
    val request = requestBuilder
        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
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
    val server = createDesktopGoogleAuthServer()
    val authUrl = "http://localhost:${server.address.port}/"

    server.createContext("/") { exchange ->
        when {
            exchange.requestMethod == "GET" && exchange.requestURI.path == "/" -> {
                exchange.writeHtml(desktopGoogleAuthPage(config))
            }
            exchange.requestMethod == "POST" && exchange.requestURI.path == "/complete" -> {
                val body = exchange.requestBody.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
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

private fun desktopGoogleAuthPage(config: OrmaFirebaseClientConfig): String =
    """
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>ORMA Google sign-in</title>
      <style>
        :root { color-scheme: light; }
        body {
          min-height: 100vh;
          margin: 0;
          display: grid;
          place-items: center;
          background: #fffaf0;
          color: #103d40;
          font-family: -apple-system, BlinkMacSystemFont, "Google Sans", "Segoe UI", sans-serif;
        }
        main {
          width: min(420px, calc(100vw - 40px));
          border: 1px solid #dfe4df;
          border-radius: 28px;
          background: #fffefa;
          padding: 32px;
          box-shadow: 0 24px 70px rgba(16, 61, 64, 0.12);
        }
        h1 {
          margin: 0 0 10px;
          font-size: 34px;
          font-weight: 650;
          letter-spacing: 0;
        }
        p {
          margin: 0 0 24px;
          color: #78918c;
          font-size: 16px;
          line-height: 1.55;
        }
        button {
          width: 100%;
          height: 56px;
          border: 0;
          border-radius: 18px;
          background: #113f42;
          color: #fffefa;
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
          color: #b04436;
        }
      </style>
    </head>
    <body>
      <main id="fallback" hidden>
        <h1>Google sign-in</h1>
        <p id="message">Opening Google account chooser...</p>
        <button id="googleButton" type="button">Try again</button>
        <p id="status" role="status"></p>
      </main>
      <script type="module">
        import { initializeApp, getApps } from "https://www.gstatic.com/firebasejs/11.10.0/firebase-app.js";
        import { getAuth, GoogleAuthProvider, getRedirectResult, onAuthStateChanged, signInWithRedirect } from "https://www.gstatic.com/firebasejs/11.10.0/firebase-auth.js";

        const firebaseConfig = {
          apiKey: "${config.apiKey}",
          authDomain: "${config.authDomain}",
          projectId: "${config.projectId}",
          storageBucket: "${config.storageBucket}",
          messagingSenderId: "${config.projectNumber}",
          appId: "${config.appId}"
        };
        const app = getApps().length ? getApps()[0] : initializeApp(firebaseConfig);
        const auth = getAuth(app);
        const provider = new GoogleAuthProvider();
        provider.setCustomParameters({ prompt: "select_account" });

        const redirectKey = "orma-google-redirect-${config.appId}";
        const fallback = document.getElementById("fallback");
        const button = document.getElementById("googleButton");
        const message = document.getElementById("message");
        const status = document.getElementById("status");

        async function complete(payload) {
          await fetch("/complete", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
          });
        }

        function currentUserWhenReady() {
          if (auth.currentUser) {
            return Promise.resolve(auth.currentUser);
          }
          return new Promise((resolve) => {
            let settled = false;
            let unsubscribe = () => {};
            const timeout = setTimeout(() => {
              if (!settled) {
                settled = true;
                unsubscribe();
                resolve(null);
              }
            }, 900);
            unsubscribe = onAuthStateChanged(auth, (user) => {
              if (!settled) {
                settled = true;
                clearTimeout(timeout);
                unsubscribe();
                resolve(user || null);
              }
            });
          });
        }

        async function completeWithUser(user) {
          const firebaseIdToken = await user.getIdToken(true);
          if (!firebaseIdToken) {
            throw new Error("Firebase did not return an ID token for the signed-in user.");
          }
          const providerId = user.providerData && user.providerData.length
            ? user.providerData[0].providerId
            : "google.com";
          await complete({
            firebaseIdToken,
            refreshToken: user.refreshToken || "",
            uid: user.uid || "",
            email: user.email || "",
            phoneNumber: user.phoneNumber || "",
            displayName: user.displayName || "",
            providerId
          });
          document.body.innerHTML = "<main><h1>Signed in</h1><p>You can return to ORMA now.</p></main>";
          window.close();
        }

        async function openGoogle() {
          button.disabled = true;
          status.textContent = "";
          try {
            sessionStorage.setItem(redirectKey, String(Date.now()));
            await signInWithRedirect(auth, provider);
          } catch (error) {
            showFallback(error && error.message ? error.message : "Could not open Google account chooser.");
          }
        }

        function showFallback(text) {
          fallback.hidden = false;
          message.textContent = "Google account chooser did not open automatically.";
          status.textContent = text;
          button.disabled = false;
        }

        async function finishWithError(error) {
          const message = error && error.message ? error.message : "Google sign-in did not complete.";
          await complete({
            errorTitle: "Google sign-in failed",
            errorMessage: message,
            errorCode: error && error.code ? error.code : "GOOGLE_DESKTOP_FAILED"
          });
          document.body.innerHTML = "<main><h1>Sign-in failed</h1><p>You can return to ORMA and try again.</p></main>";
        }

        async function start() {
          fallback.hidden = true;
          try {
            const result = await getRedirectResult(auth);
            if (result) {
              sessionStorage.removeItem(redirectKey);
              const credential = GoogleAuthProvider.credentialFromResult(result);
              if (!credential || !credential.idToken) {
                throw new Error("Google did not return an ID token.");
              }
              await complete({ idToken: credential.idToken });
              document.body.innerHTML = "<main><h1>Signed in</h1><p>You can return to ORMA now.</p></main>";
              window.close();
              return;
            }

            const existingUser = await currentUserWhenReady();
            if (existingUser) {
              sessionStorage.removeItem(redirectKey);
              await completeWithUser(existingUser);
              return;
            }

            const startedAt = Number(sessionStorage.getItem(redirectKey) || "0");
            const redirectIsRecent = startedAt > 0 && Date.now() - startedAt < 180000;
            if (redirectIsRecent) {
              sessionStorage.removeItem(redirectKey);
              throw new Error("Google returned without an account token.");
            }

            await openGoogle();
          } catch (error) {
            sessionStorage.removeItem(redirectKey);
            await finishWithError(error);
          }
        }

        button.addEventListener("click", openGoogle);
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
