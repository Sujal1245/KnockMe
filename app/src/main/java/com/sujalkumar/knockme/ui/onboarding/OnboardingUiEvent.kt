package com.sujalkumar.knockme.ui.onboarding

sealed interface OnboardingUiEvent {
    data class ShowSnackbar(val message: String) : OnboardingUiEvent
    object SignedIn : OnboardingUiEvent
}
