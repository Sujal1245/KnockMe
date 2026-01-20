package com.sujalkumar.knockme.ui.onboarding

import com.sujalkumar.knockme.ui.common.UiText

sealed interface OnboardingUiEvent {
    data class ShowSnackbar(val message: UiText) : OnboardingUiEvent
    object SignedIn : OnboardingUiEvent
}
