package com.example.expensetracker.data.remote.dto

data class ExpenseDto(
    val id: String,
    val amount: Long,
    val category: String,
    val description: String,
    val date: Long,
    val updatedAt: Long
)

data class SyncExpensesRequest(
    val expenses: List<ExpenseDto>
)

data class SyncExpensesResponse(
    val syncedIds: List<String>,
    val serverExpenses: List<ExpenseDto>
)
