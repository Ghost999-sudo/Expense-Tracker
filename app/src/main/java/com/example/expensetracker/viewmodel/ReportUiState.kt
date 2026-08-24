package com.example.expensetracker.viewmodel

import com.example.expensetracker.data.local.CategoryTotal

data class ReportUiState(

    val period: ReportPeriod = ReportPeriod.MONTH,

    val startDate: Long = 0L,

    val endDate: Long = 0L,

    val total: Long = 0L,

    val transactionCount: Int = 0,

    val averageExpense: Double = 0.0,

    val categories: List<CategoryTotal> = emptyList(),

    val isLoading: Boolean = false,

    val error: String? = null
)
