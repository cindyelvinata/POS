package com.example.pos.viewmodel

sealed class AuthCheckState {
    object Checking : AuthCheckState()
    object Authenticated : AuthCheckState()
    object NotAuthenticated : AuthCheckState()
}