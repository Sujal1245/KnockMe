package com.sujalkumar.knockme.ui.auth

import android.app.Application
import android.app.Activity.RESULT_OK
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.sujalkumar.knockme.data.repository.UserDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application,
    private val userDetailsRepository: UserDetailsRepository
) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            _authState.value = AuthState.AuthenticationSuccess
        }
    }

    fun createSignInIntent() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        _authState.value = AuthState.SignInIntentReady(signInIntent)
    }

    fun processSignInResult(result: FirebaseAuthUIAuthenticationResult, activityResultCode: Int) {
        _authState.value = AuthState.SigningIn
        if (activityResultCode == RESULT_OK) {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                viewModelScope.launch {
                    userDetailsRepository.setUserDetails(firebaseUser)
                    _authState.value = AuthState.AuthenticationSuccess
                }
            } else {
                _authState.value = AuthState.SignInError(result)
            }
        } else {
            _authState.value = AuthState.SignInError(result)
        }
    }

    fun signOut() {
        _authState.value = AuthState.SigningOut
        AuthUI.getInstance()
            .signOut(getApplication<Application>().applicationContext)
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    if (task.isSuccessful) {
                        userDetailsRepository.clearUserDetails()
                        _authState.value = AuthState.SignOutSuccess
                    } else {
                        _authState.value = AuthState.SignOutError
                    }
                }
            }
            .addOnFailureListener {
                 _authState.value = AuthState.SignOutError
            }
    }
    
    fun resetAuthState() {
        if (_authState.value !is AuthState.AuthenticationSuccess && _authState.value !is AuthState.SignOutSuccess) {
             _authState.value = AuthState.Idle
        } else if (_authState.value is AuthState.SignInError || _authState.value is AuthState.SignOutError) {
            _authState.value = AuthState.Idle
        }
    }
}