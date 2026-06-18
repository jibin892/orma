package org.orma.project_90

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.orma.project_90.auth.OrmaAndroidGoogleAuthRegistry
import org.orma.project_90.auth.OrmaGoogleSignInResult

class MainActivity : ComponentActivity() {
    private var pendingGoogleContinuation: Continuation<OrmaGoogleSignInResult>? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        installGoogleAuthProvider()

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            OrmaAndroidGoogleAuthRegistry.requestIdToken = null
            pendingGoogleContinuation = null
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
                googleSignInLauncher.launch(googleClient.signInIntent)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
