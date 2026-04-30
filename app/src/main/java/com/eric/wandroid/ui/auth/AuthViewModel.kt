package com.eric.wandroid.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eric.wandroid.common.result.AppResult
import com.eric.wandroid.data.remote.NetworkModule
import com.eric.wandroid.data.repository.AuthRepository
import com.eric.wandroid.domain.model.UserSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState().withSession(repository.currentSession()))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _authSuccess = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val authSuccess: SharedFlow<Unit> = _authSuccess.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.sessionState.collect { session ->
                _uiState.update { currentState ->
                    currentState.withSession(session)
                }
            }
        }
    }

    fun switchMode() {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(
                mode = if (it.mode == AuthMode.Login) AuthMode.Register else AuthMode.Login,
                errorMessage = null
            )
        }
    }

    fun submit(username: String, password: String, repeatPassword: String) {
        val state = _uiState.value
        if (state.isLoading) return

        val normalizedUsername = username.trim()
        if (normalizedUsername.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username and password are required.") }
            return
        }
        if (state.mode == AuthMode.Register && password != repeatPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (state.mode == AuthMode.Login) {
                repository.login(normalizedUsername, password)
            } else {
                repository.register(normalizedUsername, password, repeatPassword)
            }

            when (result) {
                is AppResult.Success -> {
                    val session = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    _messages.tryEmit("Signed in as ${session.displayName}.")
                    _authSuccess.tryEmit(Unit)
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.logout()) {
                is AppResult.Success -> {
                    _uiState.update { currentState ->
                        currentState.withSession(null).copy(
                            mode = AuthMode.Login,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    _messages.tryEmit("Signed out.")
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.withSession(null).copy(
                            mode = AuthMode.Login,
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun AuthUiState.withSession(session: UserSession?): AuthUiState {
        return copy(
            isLoggedIn = session != null,
            displayName = session?.displayName.orEmpty(),
            username = session?.username.orEmpty()
        )
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository = AuthRepository(
        NetworkModule.apiService,
        NetworkModule.requireSessionStore()
    )
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
