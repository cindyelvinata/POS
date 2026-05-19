package com.example.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _authCheckState =
        MutableStateFlow<AuthCheckState>(AuthCheckState.Checking)
    val authCheckState: StateFlow<AuthCheckState> = _authCheckState

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    /*
     * Role user: "admin" atau "cashier".
     * Default "cashier" → prinsip least privilege:
     * jika fetch gagal, akses dibatasi ke minimum.
     */
    private val _userRole = MutableStateFlow("cashier")
    val userRole: StateFlow<String> = _userRole

    init {
        observeAuthStatus()
    }

    /*
     * Observe perubahan session secara realtime.
     * Saat Authenticated → langsung fetch role dari tabel profiles.
     * Saat NotAuthenticated → reset role ke default "cashier".
     */
    private fun observeAuthStatus() {
        viewModelScope.launch {
            repository.sessionStatus.collect { status ->
                _authCheckState.value = when (status) {

                    is SessionStatus.Authenticated -> {
                        fetchUserRole()
                        AuthCheckState.Authenticated
                    }

                    is SessionStatus.NotAuthenticated -> {
                        _userRole.value = "cashier"
                        AuthCheckState.NotAuthenticated
                    }

                    is SessionStatus.Initializing ->
                        AuthCheckState.Checking

                    is SessionStatus.RefreshFailure -> {
                        if (repository.isLoggedIn()) {
                            fetchUserRole()
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
     * Fetch role dari tabel profiles.
     * Dipanggil saat session aktif (login baru atau restore session).
     */
    private fun fetchUserRole() {
        viewModelScope.launch {
            try {
                val profile = repository.getProfile()
                _userRole.value = profile?.role ?: "cashier"
            } catch (e: Exception) {
                // Jika gagal, default ke cashier (aman)
                _userRole.value = "cashier"
            }
        }
    }

    fun onFullNameChange(value: String) { _fullName.value = value }
    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    fun login() {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                repository.login(
                    email = _email.value,
                    password = _password.value
                )
                // fetchUserRole() otomatis dipanggil lewat observeAuthStatus
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Login gagal")
            }
        }
    }

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
                _uiState.value = AuthUiState.Error(e.message ?: "Register gagal")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _userRole.value = "cashier"
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}