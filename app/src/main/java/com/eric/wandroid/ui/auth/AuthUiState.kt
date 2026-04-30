package com.eric.wandroid.ui.auth

enum class AuthMode {
    Login,
    Register
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.Login,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val displayName: String = "",
    val username: String = "",
    val errorMessage: String? = null
)
