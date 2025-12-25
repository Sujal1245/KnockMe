package com.sujalkumar.knockme.data.repository.impl

import android.content.Context
import android.content.Intent
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sujalkumar.knockme.R
import com.sujalkumar.knockme.data.model.SignInResult
import com.sujalkumar.knockme.data.model.UserData
import com.sujalkumar.knockme.data.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepositoryImpl(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager
) : AuthRepository {

    override fun getSignedInUser(): UserData? {
        return auth.currentUser?.let {
            UserData(
                userId = it.uid,
                username = it.displayName,
                profilePictureUrl = it.photoUrl?.toString()
            )
        }
    }

    override suspend fun googleSignIn(intent: Intent): SignInResult {
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

        return if (googleIdTokenCredential.idToken != null) {
            try {
                val googleIdToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val user = auth.signInWithCredential(firebaseCredential).await().user

                SignInResult(
                    data = UserData(
                        userId = user?.uid ?: UUID.randomUUID().toString(),
                        username = user?.displayName,
                        profilePictureUrl = user?.photoUrl?.toString()
                    ),
                    errorMessage = null
                )
            } catch (e: Exception) {
                e.printStackTrace()
                SignInResult(
                    data = null,
                    errorMessage = e.message
                )
            }
        } else {
            SignInResult(
                data = null,
                errorMessage = "Google sign in failed"
            )
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
