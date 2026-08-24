package com.example.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(
        expense: ExpenseEntity
    )

    @Update
    suspend fun updateExpense(
        expense: ExpenseEntity
    )

    @Delete
    suspend fun deleteExpense(
        expense: ExpenseEntity
    )

    @Query(
        "SELECT * FROM expenses ORDER BY date DESC"
    )
    fun getAllExpenses():
            Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses WHERE id = :id"
    )
    suspend fun getExpenseById(
        id: Int
    ): ExpenseEntity?

    @Query(
        "SELECT SUM(amount) FROM expenses"
    )
    fun getTotalExpenses():
            Flow<Long?>

    @Query(
        """
        SELECT SUM(amount)
        FROM expenses
        WHERE date >= :startDate
        AND date < :endDate
        """
    )
    fun getExpensesBetweenDates(
        startDate: Long,
        endDate: Long
    ): Flow<Long?>

    @Query(
        """
        SELECT category, SUM(amount) AS total
        FROM expenses
        WHERE date >= :startDate
        AND date < :endDate
        GROUP BY category
        ORDER BY total DESC
        """
    )
    fun getCategoryTotals(
        startDate: Long,
        endDate: Long
    ): Flow<List<CategoryTotal>>

    @Query(
        """
        SELECT COUNT(*)
        FROM expenses
        WHERE date >= :startDate
        AND date < :endDate
        """
    )
    fun getExpenseCount(
        startDate: Long,
        endDate: Long
    ): Flow<Int>

    @Query(
        """
        SELECT AVG(amount)
        FROM expenses
        WHERE date >= :startDate
        AND date < :endDate
        """
    )
    fun getAverageExpense(
        startDate: Long,
        endDate: Long
    ): Flow<Double?>
}
