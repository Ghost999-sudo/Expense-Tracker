package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.CategoryTotal
import com.example.expensetracker.data.local.ExpenseDao
import com.example.expensetracker.data.local.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao
) {

    val allExpenses: Flow<List<ExpenseEntity>> =
        expenseDao.getAllExpenses()

    val totalExpenses: Flow<Long?> =
        expenseDao.getTotalExpenses()

    suspend fun insertExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun getExpenseById(id: Int): ExpenseEntity? {
        return expenseDao.getExpenseById(id)
    }

    fun getExpensesBetweenDates(
        startDate: Long,
        endDate: Long
    ): Flow<Long?> {
        return expenseDao.getExpensesBetweenDates(
            startDate,
            endDate
        )
    }

    fun getCategoryTotals(
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryTotal>> {

        return expenseDao.getCategoryTotals(
            startDate,
            endDate
        )
    }

    fun getExpenseCount(
        startDate: Long,
        endDate: Long
    ): Flow<Int> {

        return expenseDao.getExpenseCount(
            startDate,
            endDate
        )
    }

    fun getAverageExpense(
        startDate: Long,
        endDate: Long
    ): Flow<Double?> {

        return expenseDao.getAverageExpense(
            startDate,
            endDate
        )
    }
}
