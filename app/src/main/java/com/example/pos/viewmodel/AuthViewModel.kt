package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    /*
     * Repository untuk akses Supabase.
     */
    private val repository = AuthRepository()

    /*
     * State UI.
     */
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    /*
     * State pengecekan login.
     */
    private val _authCheckState =
        MutableStateFlow<AuthCheckState>(AuthCheckState.Checking)

    val authCheckState: StateFlow<AuthCheckState> = _authCheckState

    /*
     * State Full Name.
     */
    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName

    /*
     * State Email.
     */
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    /*
     * State Password.
     */
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    init {
        observeAuthStatus()
    }

    /*
     * Observe auth status realtime.
     */
    private fun observeAuthStatus() {

        viewModelScope.launch {

            repository.sessionStatus.collect { status ->

                _authCheckState.value = when (status) {

                    is SessionStatus.Authenticated ->
                        AuthCheckState.Authenticated

                    is SessionStatus.NotAuthenticated ->
                        AuthCheckState.NotAuthenticated

                    is SessionStatus.Initializing ->
                        AuthCheckState.Checking

                    is SessionStatus.RefreshFailure -> {

                        if (repository.isLoggedIn()) {
                            AuthCheckState.Authenticated
                        } else {
                            AuthCheckState.NotAuthenticated
                        }
                    }
                }
            }
        }
    }

    /*
     * Update full name.
     */
    fun onFullNameChange(value: String) {
        _fullName.value = value
    }

    /*
     * Update email.
     */
    fun onEmailChange(value: String) {
        _email.value = value
    }

    /*
     * Update password.
     */
    fun onPasswordChange(value: String) {
        _password.value = value
    }

    /*
     * Login.
     */
    fun login() {

        viewModelScope.launch {

            try {

                _uiState.value = AuthUiState.Loading

                repository.login(
                    email = _email.value,
                    password = _password.value
                )

                _uiState.value = AuthUiState.Success

            } catch (e: Exception) {

                _uiState.value = AuthUiState.Error(
                    message = e.message ?: "Login gagal"
                )
            }
        }
    }

    /*
     * Register.
     */
    fun register() {

        viewModelScope.launch {

            try {

                _uiState.value = AuthUiState.Loading

                repository.register(
                    fullName = _fullName.value,
                    email = _email.value,
                    password = _password.value
                )

                _uiState.value = AuthUiState.Success

            } catch (e: Exception) {

                _uiState.value = AuthUiState.Error(
                    message = e.message ?: "Register gagal"
                )
            }
        }
    }

    /*
     * Logout.
     */
    fun logout() {

        viewModelScope.launch {

            repository.logout()

            _uiState.value = AuthUiState.Idle
        }
    }

    /*
     * Reset UI State.
     */
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}