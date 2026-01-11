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
import com.sujalkumar.knockme.data.mapper.toAuthError
import com.sujalkumar.knockme.data.mapper.toUser
import com.sujalkumar.knockme.domain.model.AuthError
import com.sujalkumar.knockme.domain.model.AuthResult
import com.sujalkumar.knockme.domain.model.User
import com.sujalkumar.knockme.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val applicationContext: Context,
    private val auth: FirebaseAuth,
    private val credentialManager: CredentialManager
) : AuthRepository {

    override val currentUser: Flow<User?> =
        callbackFlow {
            trySend(auth.currentUser?.toUser())

            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                trySend(firebaseAuth.currentUser?.toUser())
            }

            auth.addAuthStateListener(listener)

            awaitClose {
                auth.removeAuthStateListener(listener)
            }
        }.distinctUntilChanged()

    override suspend fun signInWithGoogle(): AuthResult {
        return try {
            val result = credentialManager.getCredential(
                context = applicationContext,
                request = GetCredentialRequest.Builder()
                    .addCredentialOption(
                        GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(
                                applicationContext.getString(R.string.default_web_client_id)
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
                    ?: return AuthResult.Failure(AuthError.Unknown)

            AuthResult.Success(firebaseUser.toUser())

        } catch (_: GetCredentialCancellationException) {
            AuthResult.Failure(AuthError.UserCancelled)
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialException) {
            AuthResult.Failure(e.toAuthError())
        } catch (e: Exception) {
            AuthResult.Failure(e.toAuthError())
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}