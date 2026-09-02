package com.example.expensetracker.viewmodel

import com.example.expensetracker.data.local.CategoryBudgetEntity

data class BudgetUiState(

    val budgets: Map<String, CategoryBudgetEntity> = emptyMap(),

    val recommendations: List<BudgetRecommendation> = emptyList(),

    val isLoading: Boolean = true
)
