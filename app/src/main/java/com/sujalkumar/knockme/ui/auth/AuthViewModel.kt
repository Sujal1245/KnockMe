package com.sujalkumar.knockme.ui.auth

import android.app.Application
import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.sujalkumar.knockme.R // Ensure this is your app's R file
import com.sujalkumar.knockme.data.repository.UserDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    application: Application,
    private val userDetailsRepository: UserDetailsRepository
) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getApplication<Application>().getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(getApplication(), gso)

        if (firebaseAuth.currentUser != null) {
            _authState.value = AuthState.AuthenticationSuccess
        }
    }

    fun createEmailSignInIntent() {
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.Theme_FirebaseUI_Auth) // Using the theme we defined
            .build()
        _authState.value = AuthState.EmailSignInIntentReady(signInIntent)
    }

    fun createGoogleSignInIntent() {
        val signInIntent = googleSignInClient.signInIntent
        _authState.value = AuthState.GoogleSignInIntentReady(signInIntent)
    }

    fun processEmailSignInResult(result: FirebaseAuthUIAuthenticationResult, activityResultCode: Int) {
        _authState.value = AuthState.ProcessingSignIn
        if (activityResultCode == RESULT_OK) {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                viewModelScope.launch {
                    try {
                        userDetailsRepository.setUserDetails(firebaseUser)
                        _authState.value = AuthState.AuthenticationSuccess
                    } catch (e: Exception) {
                        // Handle error during user details saving if necessary
                        _authState.value = AuthState.FirebaseAuthenticationError(e)
                    }
                }
            } else {
                // This case should ideally not happen if RESULT_OK, but good to handle
                _authState.value = AuthState.EmailSignInError(result)
            }
        } else {
            _authState.value = AuthState.EmailSignInError(result)
        }
    }

    fun processGoogleSignInResult(data: Intent?) {
        _authState.value = AuthState.ProcessingSignIn
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account)
            } else {
                _authState.value = AuthState.GoogleSignInError(null) // Should not happen if no ApiException
            }
        } catch (e: ApiException) {
            _authState.value = AuthState.GoogleSignInError(e)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    userDetailsRepository.setUserDetails(firebaseUser)
                    _authState.value = AuthState.AuthenticationSuccess
                } else {
                    _authState.value = AuthState.FirebaseAuthenticationError(Exception("Firebase user is null after Google sign-in"))
                }
            } catch (e: Exception) {
                _authState.value = AuthState.FirebaseAuthenticationError(e)
            }
        }
    }

    fun signOut() {
        _authState.value = AuthState.SigningOut
        viewModelScope.launch {
            try {
                googleSignInClient.signOut().await() // Sign out from Google
                AuthUI.getInstance().signOut(getApplication()).await() // Sign out from FirebaseUI/FirebaseAuth session
                userDetailsRepository.clearUserDetails()
                _authState.value = AuthState.SignOutSuccess
            } catch (e: Exception) {
                // Attempt to clear local Firebase session even if Google sign-out fails for some reason
                try {
                    firebaseAuth.signOut()
                    userDetailsRepository.clearUserDetails() // Ensure local details are cleared
                    _authState.value = AuthState.SignOutSuccess // Or a more specific error
                } catch (firebaseError: Exception) {
                     _authState.value = AuthState.SignOutError
                }
                _authState.value = AuthState.SignOutError // General error if Google sign out failed initially
            }
        }
    }

    fun resetAuthState() {
        // Only reset if not in a success state that leads to navigation,
        // or a pending state that will auto-transition.
        val currentState = _authState.value
        if (currentState !is AuthState.AuthenticationSuccess &&
            currentState !is AuthState.SignOutSuccess &&
            currentState !is AuthState.ProcessingSignIn &&
            currentState !is AuthState.EmailSignInIntentReady && // these are reset after launch
            currentState !is AuthState.GoogleSignInIntentReady // these are reset after launch
        ) {
            _authState.value = AuthState.Idle
        }
    }
}
