package com.example.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(

    @PrimaryKey
    val category: String,

    val monthlyLimit: Long,

    val updatedAt: Long = System.currentTimeMillis(),

    val isSynced: Boolean = true
)
