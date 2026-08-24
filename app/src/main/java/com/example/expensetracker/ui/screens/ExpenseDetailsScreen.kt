package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.ui.components.CategoryDropdown
import com.example.expensetracker.util.DateUtils
import com.example.expensetracker.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailsScreen(
    expenseId: Int,
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {

    var expense by remember {
        mutableStateOf<ExpenseEntity?>(null)
    }

    var isEditing by remember {
        mutableStateOf(false)
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(expenseId) {

        expense = viewModel.getExpenseById(
            expenseId
        )
    }

    val currentExpense = expense

    if (currentExpense == null) {
        return
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        if (isEditing)
                            "Edit Expense"
                        else
                            "Expense Details"
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }

    ) { paddingValues ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            if (isEditing) {

                EditExpenseForm(
                    expense = currentExpense,

                    onSave = { updatedExpense ->

                        viewModel.updateExpense(
                            updatedExpense
                        )

                        expense = updatedExpense

                        isEditing = false
                    }
                )

            } else {

                ExpenseDetails(
                    expense = currentExpense
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Button(
                        onClick = {
                            isEditing = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Edit,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                        )

                        Text("Edit")
                    }

                    OutlinedButton(
                        onClick = {
                            showDeleteDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Delete,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                        )

                        Text("Delete")
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text("Delete Expense?")
            },

            text = {
                Text(
                    "Are you sure you want to delete this expense? " +
                        "This action cannot be undone."
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        viewModel.deleteExpense(
                            currentExpense
                        )

                        showDeleteDialog = false

                        onBack()
                    }
                ) {

                    Text("Delete")
                }
            },

            dismissButton = {

                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ExpenseDetails(
    expense: ExpenseEntity
) {

    Column(
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = expense.category,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = formatAmount(expense.amount),
            style = MaterialTheme.typography.displaySmall
        )

        if (expense.description.isNotBlank()) {

            Text(
                text = expense.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Text(
            text = formatDate(expense.date),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditExpenseForm(
    expense: ExpenseEntity,
    onSave: (ExpenseEntity) -> Unit
) {

    var amount by remember {
        mutableStateOf(
            (expense.amount / 100.0).toString()
        )
    }

    var description by remember {
        mutableStateOf(
            expense.description
        )
    }

    var category by remember {
        mutableStateOf(
            expense.category
        )
    }

    var selectedDate by remember {
        mutableLongStateOf(expense.date)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        OutlinedTextField(

            value = amount,

            onValueChange = {
                amount = it
            },

            modifier = Modifier.fillMaxWidth(),

            label = {
                Text("Amount")
            },

            prefix = {
                Text("KSh ")
            },

            singleLine = true
        )

        CategoryDropdown(
            selectedCategory = category,
            onCategorySelected = {
                category = it
            }
        )

        OutlinedTextField(

            value = description,

            onValueChange = {
                description = it
            },

            modifier = Modifier.fillMaxWidth(),

            label = {
                Text("Description")
            },

            minLines = 2
        )

        OutlinedTextField(

            value = formatDate(selectedDate),

            onValueChange = {},

            readOnly = true,

            modifier = Modifier.fillMaxWidth(),

            label = {
                Text("Date")
            },

            trailingIcon = {

                Button(
                    onClick = {
                        showDatePicker = true
                    }
                ) {
                    Text("Change")
                }
            }
        )

        Button(

            onClick = {

                val newAmount =
                    parseAmount(amount)

                if (
                    newAmount != null &&
                    newAmount > 0 &&
                    category.isNotBlank()
                ) {

                    onSave(
                        expense.copy(
                            amount = newAmount,
                            category = category.trim(),
                            description =
                                description.trim(),
                            date = DateUtils.startOfDay(
                                selectedDate
                            )
                        )
                    )
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Save Changes")
        }
    }

    if (showDatePicker) {

        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = selectedDate
            )

        DatePickerDialog(

            onDismissRequest = {
                showDatePicker = false
            },

            confirmButton = {

                Button(
                    onClick = {

                        datePickerState
                            .selectedDateMillis
                            ?.let {
                                selectedDate = it
                            }

                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },

            dismissButton = {

                OutlinedButton(
                    onClick = {
                        showDatePicker = false
                    }
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
}

private fun parseAmount(
    text: String
): Long? {

    return try {

        val amount = text
            .replace(",", "")
            .trim()
            .toDouble()

        if (amount <= 0) {
            null
        } else {
            (amount * 100).toLong()
        }

    } catch (
        exception: NumberFormatException
    ) {

        null
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
