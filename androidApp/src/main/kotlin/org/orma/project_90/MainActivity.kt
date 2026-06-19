package org.orma.project_90

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.orma.project_90.auth.OrmaAndroidAuthSessionStore
import org.orma.project_90.auth.OrmaAndroidGoogleAuthRegistry
import org.orma.project_90.auth.OrmaGoogleSignInResult
import org.orma.project_90.notifications.OrmaAndroidNotificationPermissionRegistry

class MainActivity : ComponentActivity() {
    private var pendingGoogleContinuation: Continuation<OrmaGoogleSignInResult>? = null
    private var pendingNotificationContinuation: Continuation<Boolean>? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val continuation = pendingGoogleContinuation ?: return@registerForActivityResult
        pendingGoogleContinuation = null

        val signInResult = try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                OrmaGoogleSignInResult.Failure(
                    title = "Google token missing",
                    message = "Google returned an account without an ID token. Check the Android web client ID in Firebase.",
                    code = "GOOGLE_ANDROID_ID_TOKEN_MISSING",
                )
            } else {
                OrmaGoogleSignInResult.Success(idToken = idToken)
            }
        } catch (error: ApiException) {
            OrmaGoogleSignInResult.Failure(
                title = "Google sign-in cancelled",
                message = error.localizedMessage ?: "Google sign-in did not complete.",
                code = "GOOGLE_ANDROID_${error.statusCode}",
            )
        } catch (error: Throwable) {
            OrmaGoogleSignInResult.Failure(
                title = "Google sign-in failed",
                message = error.message ?: "Google sign-in did not complete.",
                code = "GOOGLE_ANDROID_FAILED",
            )
        }

        continuation.resume(signInResult)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val continuation = pendingNotificationContinuation ?: return@registerForActivityResult
        pendingNotificationContinuation = null
        continuation.resume(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        OrmaAndroidAuthSessionStore.install(applicationContext)
        installGoogleAuthProvider()
        installNotificationPermissionProvider()

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            OrmaAndroidGoogleAuthRegistry.requestIdToken = null
            pendingGoogleContinuation = null
            OrmaAndroidGoogleAuthRegistry.clearSession = null
            OrmaAndroidNotificationPermissionRegistry.requestPermission = null
            pendingNotificationContinuation = null
        }
        super.onDestroy()
    }

    private fun installGoogleAuthProvider() {
        val googleClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build(),
        )
        OrmaAndroidGoogleAuthRegistry.requestIdToken = {
            suspendCoroutine { continuation ->
                if (pendingGoogleContinuation != null) {
                    continuation.resume(
                        OrmaGoogleSignInResult.Failure(
                            title = "Google sign-in already open",
                            message = "Complete the current Google sign-in request first.",
                            code = "GOOGLE_ANDROID_ALREADY_RUNNING",
                        ),
                    )
                    return@suspendCoroutine
                }
                pendingGoogleContinuation = continuation
                googleClient.signOut()
                    .addOnCompleteListener {
                        googleSignInLauncher.launch(googleClient.signInIntent)
                    }
            }
        }
        OrmaAndroidGoogleAuthRegistry.clearSession = {
            suspendCoroutine { continuation ->
                googleClient.signOut()
                    .addOnCompleteListener {
                        continuation.resume(Unit)
                    }
            }
        }
    }

    private fun installNotificationPermissionProvider() {
        OrmaAndroidNotificationPermissionRegistry.requestPermission = {
            suspendCoroutine { continuation ->
                if (pendingNotificationContinuation != null) {
                    continuation.resume(false)
                    return@suspendCoroutine
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    continuation.resume(true)
                    return@suspendCoroutine
                }
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    continuation.resume(true)
                    return@suspendCoroutine
                }
                pendingNotificationContinuation = continuation
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
