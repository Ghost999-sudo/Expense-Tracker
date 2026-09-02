package com.example.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryBudgetDao {

    @Upsert
    suspend fun upsert(
        budget: CategoryBudgetEntity
    )

    @Delete
    suspend fun delete(
        budget: CategoryBudgetEntity
    )

    @Query("SELECT * FROM category_budgets ORDER BY category ASC")
    fun getAllBudgets(): Flow<List<CategoryBudgetEntity>>

    @Query(
        "SELECT * FROM category_budgets WHERE category = :category LIMIT 1"
    )
    suspend fun getBudgetForCategory(
        category: String
    ): CategoryBudgetEntity?

    @Query("SELECT * FROM category_budgets WHERE isSynced = 0")
    suspend fun getUnsyncedBudgets(): List<CategoryBudgetEntity>

    @Query("UPDATE category_budgets SET isSynced = 1 WHERE category = :category")
    suspend fun markBudgetSynced(category: String)
}
