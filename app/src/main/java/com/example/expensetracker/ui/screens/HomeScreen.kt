package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.ui.components.EmptyExpenseState
import com.example.expensetracker.ui.components.ExpenseCard
import com.example.expensetracker.ui.components.InsightsSection
import com.example.expensetracker.viewmodel.BudgetViewModel
import com.example.expensetracker.viewmodel.ExpenseViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    budgetViewModel: BudgetViewModel,
    onAddExpense: () -> Unit,
    onExpenseClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val expenses by viewModel.filteredExpenses
        .collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery
        .collectAsStateWithLifecycle()

    val totalExpenses by viewModel.totalExpenses
        .collectAsStateWithLifecycle()

    val todayExpenses by viewModel.todayExpenses
        .collectAsStateWithLifecycle()

    val budgetUiState by budgetViewModel.budgetUiState
        .collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,

        floatingActionButton = {

            FloatingActionButton(
                onClick = onAddExpense
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add expense"
                )
            }
        }

    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),

            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 20.dp
            ),

            verticalArrangement = Arrangement.spacedBy(
                12.dp
            )
        ) {

            item {

                Text(
                    text = "Expense Tracker",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            item {

                SummaryCard(
                    totalExpenses = totalExpenses,
                    todayExpenses = todayExpenses
                )
            }

            item {

                InsightsSection(
                    recommendations =
                        budgetUiState.recommendations
                )
            }

            item {

                OutlinedTextField(

                    value = searchQuery,

                    onValueChange = {
                        viewModel.updateSearchQuery(it)
                    },

                    modifier = Modifier.fillMaxWidth(),

                    label = {
                        Text("Search expenses")
                    },

                    singleLine = true
                )
            }

            item {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (expenses.isEmpty()) {

                item {

                    if (searchQuery.isBlank()) {

                        EmptyExpenseState(
                            onAddExpense = onAddExpense
                        )

                    } else {

                        Text(
                            text =
                                "No expenses match \"$searchQuery\".",
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier =
                                Modifier.padding(vertical = 40.dp)
                        )
                    }
                }

            } else {

                items(
                    items = expenses,
                    key = { it.id }
                ) { expense ->

                    ExpenseCard(
                        category = expense.category,
                        description = expense.description,
                        amount = expense.amount,
                        date = expense.date,
                        onClick = {
                            onExpenseClick(expense.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totalExpenses: Long,
    todayExpenses: Long
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "Total Expenses",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = formatAmount(totalExpenses),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = formatAmount(todayExpenses),
                        style = MaterialTheme.typography.titleMedium
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
