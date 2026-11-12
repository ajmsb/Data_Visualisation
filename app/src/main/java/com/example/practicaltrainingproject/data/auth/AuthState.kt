package com.example.practicaltrainingproject.data.auth

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object Unauthenticated : AuthState()
    object SignupInProgress : AuthState()  // Add this line
    data class Error(val message: String) : AuthState()
}