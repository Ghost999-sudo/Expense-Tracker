package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.CategoryBudgetDao
import com.example.expensetracker.data.local.CategoryBudgetEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val dao: CategoryBudgetDao
) {

    val allBudgets: Flow<List<CategoryBudgetEntity>> =
        dao.getAllBudgets()

    suspend fun setBudget(
        category: String,
        monthlyLimitCents: Long
    ) {

        dao.upsert(
            CategoryBudgetEntity(
                category = category,
                monthlyLimit = monthlyLimitCents
            )
        )
    }

    suspend fun clearBudget(
        category: String
    ) {

        dao.delete(
            CategoryBudgetEntity(
                category = category,
                monthlyLimit = 0L
            )
        )
    }
}
