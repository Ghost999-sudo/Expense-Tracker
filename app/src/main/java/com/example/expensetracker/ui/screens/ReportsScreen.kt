package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.ui.components.CategorySpendingBar
import com.example.expensetracker.util.DateUtils
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.ReportPeriod
import com.example.expensetracker.viewmodel.ReportUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ExpenseViewModel
) {

    val state by viewModel.reportUiState
        .collectAsStateWithLifecycle()

    var showStartDatePicker by remember {
        mutableStateOf(false)
    }

    var showEndDatePicker by remember {
        mutableStateOf(false)
    }

    Scaffold(

        topBar = {

            TopAppBar(
                title = {
                    Text("Reports")
                }
            )
        }

    ) { paddingValues ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(16.dp),

            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            item {

                PeriodSelector(
                    selectedPeriod = state.period,

                    onPeriodSelected = {
                        viewModel.selectPeriod(it)
                    }
                )
            }

            if (state.period == ReportPeriod.CUSTOM) {

                item {

                    CustomRangeFields(
                        startDate = state.startDate,
                        endDate = state.endDate,
                        onStartClick = {
                            showStartDatePicker = true
                        },
                        onEndClick = {
                            showEndDatePicker = true
                        }
                    )
                }
            }

            when {

                state.isLoading -> item {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }

                state.error != null -> item {

                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                state.transactionCount == 0 -> item {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "No expenses for this period.",
                            style =
                                MaterialTheme.typography.titleMedium
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                "Start adding expenses to see " +
                                    "your spending report.",
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {

                    item {

                        ReportSummary(
                            state = state
                        )
                    }

                    item {

                        Text(
                            text = "By Category",
                            style =
                                MaterialTheme.typography
                                    .titleLarge
                        )
                    }

                    items(
                        state.categories,
                        key = { it.category }
                    ) { categoryTotal ->

                        CategorySpendingBar(
                            category =
                                categoryTotal.category,

                            amount =
                                categoryTotal.total,

                            total =
                                state.total
                        )
                    }
                }
            }
        }
    }

    if (showStartDatePicker) {

        CustomDatePicker(
            initialDate = state.startDate,
            onConfirm = { picked ->
                viewModel.setCustomRange(
                    DateUtils.startOfDay(picked),
                    state.endDate
                )
            },
            onDismiss = {
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {

        CustomDatePicker(
            initialDate = state.endDate,
            onConfirm = { picked ->
                viewModel.setCustomRange(
                    state.startDate,
                    DateUtils.endOfDay(picked)
                )
            },
            onDismiss = {
                showEndDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDatePicker(
    initialDate: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {

    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = initialDate
        )

    DatePickerDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            Button(
                onClick = {

                    datePickerState
                        .selectedDateMillis
                        ?.let(onConfirm)

                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },

        dismissButton = {

            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }

    ) {

        DatePicker(
            state = datePickerState
        )
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: ReportPeriod,
    onPeriodSelected:
        (ReportPeriod) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        ReportPeriod.entries
            .forEach { period ->

                FilterChip(

                    selected =
                        selectedPeriod == period,

                    onClick = {
                        onPeriodSelected(period)
                    },

                    label = {
                        Text(period.label)
                    }
                )
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRangeFields(
    startDate: Long,
    endDate: Long,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit
) {

    Column(
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        OutlinedTextField(

            value = formatDate(startDate),

            onValueChange = {},

            readOnly = true,

            modifier = Modifier.fillMaxWidth(),

            label = {
                Text("Start Date")
            },

            trailingIcon = {

                Button(
                    onClick = onStartClick
                ) {
                    Text("Change")
                }
            }
        )

        OutlinedTextField(

            value = formatDate(endDate),

            onValueChange = {},

            readOnly = true,

            modifier = Modifier.fillMaxWidth(),

            label = {
                Text("End Date")
            },

            trailingIcon = {

                Button(
                    onClick = onEndClick
                ) {
                    Text("Change")
                }
            }
        )
    }
}

@Composable
private fun ReportSummary(
    state: ReportUiState
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "Total Spending",
                style =
                    MaterialTheme.typography
                        .titleMedium
            )

            Text(
                text =
                    formatAmount(state.total),

                style =
                    MaterialTheme.typography
                        .headlineLarge
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        text = "Transactions"
                    )

                    Text(
                        text =
                            state.transactionCount.toString(),

                        style =
                            MaterialTheme.typography
                                .titleMedium
                    )
                }

                Column {

                    Text(
                        text = "Average"
                    )

                    Text(
                        text =
                            formatAmount(
                                state.averageExpense
                                    .toLong()
                            ),

                        style =
                            MaterialTheme.typography
                                .titleMedium
                    )
                }
            }
        }
    }
}

private fun formatAmount(
    amount: Long
): String {

    return String.format(
        Locale.US,
        "KSh %,.2f",
        amount / 100.0
    )
}

private fun formatDate(
    timestamp: Long
): String {

    val formatter = SimpleDateFormat(
        "dd MMM yyyy",
        Locale.getDefault()
    )

    return formatter.format(
        Date(timestamp)
    )
}
