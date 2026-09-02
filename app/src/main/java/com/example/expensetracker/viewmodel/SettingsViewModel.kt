package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _settingsUiState = MutableStateFlow(
        SettingsUiState(
            themeMode = repository.getThemeMode()
        )
    )

    val settingsUiState: StateFlow<SettingsUiState> =
        _settingsUiState.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {

        repository.setThemeMode(mode)

        _settingsUiState.value =
            _settingsUiState.value.copy(
                themeMode = mode
            )
    }
}

class SettingsViewModelFactory(
    private val repository: SettingsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(
                SettingsViewModel::class.java
            )
        ) {
            return SettingsViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}