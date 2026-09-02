package com.example.expensetracker.data.remote.dto

data class BudgetDto(
    val category: String,
    val monthlyLimit: Long,
    val updatedAt: Long
)

data class SyncBudgetsRequest(
    val budgets: List<BudgetDto>
)

data class SyncBudgetsResponse(
    val syncedCategories: List<String>,
    val serverBudgets: List<BudgetDto>
)
