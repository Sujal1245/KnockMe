package com.sujalkumar.knockme.data.repository.impl

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.sujalkumar.knockme.R
import com.sujalkumar.knockme.data.model.SignInResult
import com.sujalkumar.knockme.data.model.UserData
import com.sujalkumar.knockme.data.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager
) : AuthRepository {

    override fun getSignedInUser(): FirebaseUser? {
        return auth.currentUser
    }

    override suspend fun googleSignIn(): SignInResult {
        return try {
            val result = credentialManager.getCredential(
                context = context,
                request = GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(context.getString(R.string.default_web_client_id))
                            .setAutoSelectEnabled(true)
                            .build()
                    )
                    .build()
            )

            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val user =
                auth.signInWithCredential(firebaseCredential).await().user ?: return SignInResult(
                    data = null,
                    errorMessage = "Sign in failed: user is null after credential exchange."
                )

            SignInResult(
                data = user,
                errorMessage = null
            )
        } catch (e: GetCredentialCancellationException) {
            e.printStackTrace()
            // User cancelled the sign-in flow, return a result with a message.
            // You might want to return 'null' for errorMessage to avoid showing a Toast.
            SignInResult(data = null, errorMessage = "Sign-in was cancelled.")
        } catch (e: GetCredentialException) {
            e.printStackTrace()
            SignInResult(data = null, errorMessage = e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            SignInResult(data = null, errorMessage = e.message)
        }
    }

    override suspend fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
