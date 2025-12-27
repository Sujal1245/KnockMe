package com.sujalkumar.knockme.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sujalkumar.knockme.R
import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.data.mapper.toAuthError
import com.sujalkumar.knockme.data.mapper.toUser
import com.sujalkumar.knockme.domain.model.AuthError
import com.sujalkumar.knockme.domain.model.SignInResult
import com.sujalkumar.knockme.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager
) : AuthRepository {

    override fun getSignedInUser(): User? {
        return auth.currentUser?.toUser()
    }

    override suspend fun googleSignIn(): SignInResult {
        return try {
            val result = credentialManager.getCredential(
                context = context,
                request = GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(
                                context.getString(R.string.default_web_client_id)
                            )
                            .setAutoSelectEnabled(true)
                            .build()
                    )
                    .build()
            )

            val googleIdToken = GoogleIdTokenCredential
                .createFrom(result.credential.data)
                .idToken

            val firebaseCredential =
                GoogleAuthProvider.getCredential(googleIdToken, null)

            val firebaseUser =
                auth.signInWithCredential(firebaseCredential)
                    .await()
                    .user
                    ?: return SignInResult.Failure(AuthError.Unknown)

            SignInResult.Success(firebaseUser.toUser())

        } catch (_: GetCredentialCancellationException) {
            SignInResult.Failure(AuthError.UserCancelled)
        } catch (e: GetCredentialException) {
            SignInResult.Failure(e.toAuthError())
        } catch (e: Exception) {
            SignInResult.Failure(e.toAuthError())
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}