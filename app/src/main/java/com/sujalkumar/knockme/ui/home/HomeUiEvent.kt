package com.sujalkumar.knockme.ui.home

import com.sujalkumar.knockme.ui.common.UiText

sealed interface HomeUiEvent {
    data class ShowSnackbar(val message: UiText) : HomeUiEvent
}
