package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.UserEntity
import com.example.expensetracker.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isInitializing: Boolean = true,
    val currentUser: UserEntity? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authUiState =
        MutableStateFlow(AuthUiState())

    val authUiState: StateFlow<AuthUiState> =
        _authUiState.asStateFlow()

    init {

        viewModelScope.launch {

            val user = repository.getCurrentUser()

            _authUiState.value = AuthUiState(
                isInitializing = false,
                currentUser = user
            )
        }
    }

    fun register(
        email: String,
        password: String
    ) {

        val validationError = validate(
            email = email,
            password = password
        )

        if (validationError != null) {

            _authUiState.value =
                _authUiState.value.copy(
                    error = validationError
                )

            return
        }

        _authUiState.value =
            _authUiState.value.copy(
                isSubmitting = true,
                error = null
            )

        viewModelScope.launch {

            val result =
                repository.register(email, password)

            _authUiState.value =
                handleResult(result)
        }
    }

    fun login(
        email: String,
        password: String
    ) {

        val validationError = validate(
            email = email,
            password = password
        )

        if (validationError != null) {

            _authUiState.value =
                _authUiState.value.copy(
                    error = validationError
                )

            return
        }

        _authUiState.value =
            _authUiState.value.copy(
                isSubmitting = true,
                error = null
            )

        viewModelScope.launch {

            val result = repository.login(email, password)

            _authUiState.value =
                handleResult(result)
        }
    }

    fun logout() {

        repository.logout()

        _authUiState.value = AuthUiState(
            isInitializing = false,
            currentUser = null
        )
    }

    private fun handleResult(
        result: Result<UserEntity>
    ): AuthUiState {

        return result.fold(
            onSuccess = { user ->

                AuthUiState(
                    isInitializing = false,
                    currentUser = user
                )
            },
            onFailure = { exception ->

                _authUiState.value.copy(
                    isSubmitting = false,
                    error =
                        exception.message
                            ?: "Something went wrong."
                )
            }
        )
    }

    private fun validate(
        email: String,
        password: String
    ): String? {

        if (email.isBlank()) {
            return "Enter your email address."
        }

        if (!android.util.Patterns
                .EMAIL_ADDRESS
                .matcher(email)
                .matches()
        ) {
            return "Enter a valid email address."
        }

        if (password.length < 6) {
            return "Password must be at least 6 characters."
        }

        return null
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(
                AuthViewModel::class.java
            )
        ) {
            return AuthViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}