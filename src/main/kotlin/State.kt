package com.github.kotlintelegrambot

sealed interface State {
    data object DefaultState: State
    data object WaitingNameState: State
    data class WaitingPhoneState(val name: String): State
    data object WaitingSearthNameState: State
    data object WaitingDeleteNameState: State
}