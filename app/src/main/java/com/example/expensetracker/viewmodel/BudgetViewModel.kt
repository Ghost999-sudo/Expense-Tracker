package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.CategoryBudgetEntity
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    // ── Date ranges ───────────────────────────────────────────────────────

    private val thisMonthStart = DateUtils.startOfMonth()
    private val thisMonthEnd = DateUtils.endOfMonth()

    private val prevMonthStart = DateUtils.startOfPrevMonth()
    private val prevMonthEnd = DateUtils.endOfPrevMonth()

    // ── Reactive state ────────────────────────────────────────────────────

    val budgetUiState: StateFlow<BudgetUiState> = combine(
        budgetRepository.allBudgets,
        expenseRepository.getCategoryTotals(thisMonthStart, thisMonthEnd),
        expenseRepository.getCategoryTotals(prevMonthStart, prevMonthEnd)
    ) { budgets, thisMonth, prevMonth ->

        val budgetMap = budgets.associateBy { it.category }

        val prevMonthMap = prevMonth.associate { it.category to it.total }

        val recommendations = mutableListOf<BudgetRecommendation>()

        thisMonth.forEach { categoryTotal ->

            val budget = budgetMap[categoryTotal.category]

            if (budget != null) {

                val pct = if (budget.monthlyLimit > 0) {
                    (categoryTotal.total * 100L / budget.monthlyLimit).toInt()
                } else 0

                when {

                    pct >= 100 -> recommendations.add(
                        BudgetRecommendation.OverBudget(
                            category = categoryTotal.category,
                            spentCents = categoryTotal.total,
                            limitCents = budget.monthlyLimit,
                            overByPercent = pct - 100
                        )
                    )

                    pct >= 80 -> recommendations.add(
                        BudgetRecommendation.ApproachingBudget(
                            category = categoryTotal.category,
                            spentCents = categoryTotal.total,
                            limitCents = budget.monthlyLimit,
                            percentUsed = pct
                        )
                    )
                }

            } else {

                val prev = prevMonthMap[categoryTotal.category] ?: 0L

                if (prev > 0) {

                    val pct = ((categoryTotal.total - prev) * 100L / prev).toInt()

                    if (pct > 30) {

                        recommendations.add(
                            BudgetRecommendation.UnusualSpend(
                                category = categoryTotal.category,
                                thisMonthCents = categoryTotal.total,
                                lastMonthCents = prev,
                                increasePercent = pct
                            )
                        )
                    }
                }
            }
        }

        BudgetUiState(
            budgets = budgetMap,
            recommendations = recommendations,
            isLoading = false
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BudgetUiState(isLoading = true)
    )

    // ── Actions ───────────────────────────────────────────────────────────

    fun setBudget(
        category: String,
        monthlyLimitCents: Long
    ) {

        viewModelScope.launch {
            budgetRepository.setBudget(category, monthlyLimitCents)
        }
    }

    fun clearBudget(
        category: String
    ) {

        viewModelScope.launch {
            budgetRepository.clearBudget(category)
        }
    }
}

class BudgetViewModelFactory(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(
                BudgetViewModel::class.java
            )
        ) {
            return BudgetViewModel(
                budgetRepository,
                expenseRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}
