package com.sujalkumar.knockme.ui.addalert

sealed interface AddKnockAlertUiEvent {
    data class ShowSnackbar(val message: String) : AddKnockAlertUiEvent
    object AlertAdded : AddKnockAlertUiEvent
}
