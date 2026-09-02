package com.example.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val amount: Long,

    val category: String,

    val description: String = "",

    val date: Long,

    val serverId: String? = null,

    val updatedAt: Long = System.currentTimeMillis(),

    val isSynced: Boolean = true
)
