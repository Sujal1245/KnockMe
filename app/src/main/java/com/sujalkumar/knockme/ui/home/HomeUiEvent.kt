package com.sujalkumar.knockme.ui.home

sealed interface HomeUiEvent {
    data class ShowSnackbar(val message: String) : HomeUiEvent
}
