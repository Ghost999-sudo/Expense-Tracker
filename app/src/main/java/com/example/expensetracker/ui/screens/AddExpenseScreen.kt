package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ui.components.CategoryDropdown
import com.example.expensetracker.util.DateUtils
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.ParsedReceipt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit,
    onScanReceipt: () -> Unit = {},
    prefill: ParsedReceipt? = null
) {

    var amountText by remember(prefill) {
        mutableStateOf(
            if (prefill?.amountCents != null)
                (prefill.amountCents / 100.0)
                    .toBigDecimal()
                    .stripTrailingZeros()
                    .toPlainString()
            else ""
        )
    }

    var description by remember(prefill) {
        mutableStateOf(prefill?.merchantName ?: "")
    }

    var selectedCategory by remember(prefill) {
        mutableStateOf(prefill?.suggestedCategory ?: "")
    }

    var selectedDate by remember(prefill) {
        mutableLongStateOf(
            prefill?.date ?: System.currentTimeMillis()
        )
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var amountError by remember {
        mutableStateOf(false)
    }

    var categoryError by remember {
        mutableStateOf(false)
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Add Expense")
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
                .padding(horizontal = 16.dp)
                .verticalScroll(
                    rememberScrollState()
                ),

            verticalArrangement = Arrangement.spacedBy(
                16.dp
            )
        ) {

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // ── Scan Receipt button ───────────────────────────────
            OutlinedButton(
                onClick = onScanReceipt,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Receipt")
                }
            }

            OutlinedTextField(

                value = amountText,

                onValueChange = {

                    amountText = it

                    amountError = false
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("Amount")
                },

                prefix = {
                    Text("KSh ")
                },

                singleLine = true,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),

                isError = amountError,

                supportingText = {

                    if (amountError) {
                        Text("Enter a valid amount")
                    }
                }
            )

            CategoryDropdown(
                selectedCategory = selectedCategory,
                onCategorySelected = {
                    selectedCategory = it
                    categoryError = false
                },
                isError = categoryError,
                supportingText =
                    if (categoryError) "Select a category"
                    else null
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

                placeholder = {
                    Text("Optional")
                },

                minLines = 2,

                maxLines = 4
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

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(

                onClick = {

                    val amount = parseAmount(
                        amountText
                    )

                    amountError =
                        amount == null || amount <= 0

                    categoryError =
                        selectedCategory.isBlank()

                    if (
                        amount != null &&
                        amount > 0 &&
                        selectedCategory.isNotBlank()
                    ) {

                        viewModel.addExpense(

                            amount = amount,

                            category = selectedCategory,

                            description = description.trim(),

                            date = DateUtils.startOfDay(
                                selectedDate
                            )
                        )

                        onBack()
                    }
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Save Expense")
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )
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

                Button(
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
