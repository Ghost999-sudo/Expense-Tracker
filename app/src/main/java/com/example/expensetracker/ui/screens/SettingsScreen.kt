package com.example.expensetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.ui.theme.ThemeMode
import com.example.expensetracker.viewmodel.AuthViewModel
import com.example.expensetracker.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel
) {

    val settingsUiState by settingsViewModel
        .settingsUiState
        .collectAsStateWithLifecycle()

    val authUiState by authViewModel
        .authUiState
        .collectAsStateWithLifecycle()

    Scaffold(

        topBar = {

            TopAppBar(
                title = {
                    Text("Settings")
                }
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Appearance",
                style =
                    MaterialTheme.typography.titleLarge
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column {

                    ThemeMode.entries
                        .forEachIndexed { index, mode ->

                            ThemeOptionRow(
                                mode = mode,
                                selected =
                                    settingsUiState.themeMode ==
                                        mode,
                                onClick = {
                                    settingsViewModel
                                        .setThemeMode(mode)
                                }
                            )

                            if (
                                index <
                                ThemeMode.entries.lastIndex
                            ) {

                                HorizontalDivider()
                            }
                        }
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Account",
                style =
                    MaterialTheme.typography.titleLarge
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Signed in as",
                        style =
                            MaterialTheme
                                .typography.bodyMedium
                    )

                    Text(
                        text =
                            authUiState.currentUser
                                ?.email
                                ?: "Unknown",
                        style =
                            MaterialTheme
                                .typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            authViewModel.logout()
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text("Log Out")
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Spacer(
            modifier = Modifier
                .padding(horizontal = 8.dp)
        )

        Text(
            text = mode.label,
            style =
                MaterialTheme.typography.bodyLarge
        )
    }
}