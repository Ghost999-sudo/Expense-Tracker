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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.ui.components.expenseCategories
import com.example.expensetracker.ui.theme.ThemeMode
import com.example.expensetracker.viewmodel.AuthViewModel
import com.example.expensetracker.viewmodel.BudgetViewModel
import com.example.expensetracker.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    budgetViewModel: BudgetViewModel
) {

    val settingsUiState by settingsViewModel
        .settingsUiState
        .collectAsStateWithLifecycle()

    val authUiState by authViewModel
        .authUiState
        .collectAsStateWithLifecycle()

    val budgetUiState by budgetViewModel
        .budgetUiState
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Monthly Budgets",
                style =
                    MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Set a limit per category. You'll get a warning when you reach 80%.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column {

                    expenseCategories.forEachIndexed { index, category ->

                        BudgetRow(
                            category = category,
                            currentLimitCents =
                                budgetUiState.budgets[category]
                                    ?.monthlyLimit,
                            onSave = { limitCents ->
                                budgetViewModel.setBudget(
                                    category,
                                    limitCents
                                )
                            },
                            onClear = {
                                budgetViewModel.clearBudget(
                                    category
                                )
                            }
                        )

                        if (index < expenseCategories.lastIndex) {
                            HorizontalDivider()
                        }
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

@Composable
private fun BudgetRow(
    category: String,
    currentLimitCents: Long?,
    onSave: (Long) -> Unit,
    onClear: () -> Unit
) {

    var inputText by rememberSaveable(category, currentLimitCents) {
        mutableStateOf(
            if (currentLimitCents != null && currentLimitCents > 0)
                (currentLimitCents / 100.0).toBigDecimal()
                    .stripTrailingZeros().toPlainString()
            else ""
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = category,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier.weight(1.2f),
            label = { Text("KSh") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        if (currentLimitCents != null && currentLimitCents > 0) {

            TextButton(
                onClick = onClear
            ) {
                Text("Clear")
            }

        } else {

            Button(
                onClick = {
                    val amount = inputText
                        .replace(",", "")
                        .trim()
                        .toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onSave((amount * 100).toLong())
                    }
                }
            ) {
                Text("Set")
            }
        }
    }
}