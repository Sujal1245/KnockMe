package com.sujalkumar.knockme.ui.addalert

import com.sujalkumar.knockme.ui.common.UiText

sealed interface AddKnockAlertUiEvent {
    data class ShowSnackbar(val message: UiText) : AddKnockAlertUiEvent
    object AlertAdded : AddKnockAlertUiEvent
}
