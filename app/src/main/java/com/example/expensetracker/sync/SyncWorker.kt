package com.example.expensetracker.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expensetracker.data.local.ExpenseDatabase
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.data.remote.TokenManager
import com.example.expensetracker.data.remote.api.ExpenseTrackerApi
import com.example.expensetracker.data.remote.dto.BudgetDto
import com.example.expensetracker.data.remote.dto.ExpenseDto
import com.example.expensetracker.data.remote.dto.SyncBudgetsRequest
import com.example.expensetracker.data.remote.dto.SyncExpensesRequest
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val tokenManager = TokenManager(applicationContext)

        val rawToken = tokenManager.getToken() ?: return Result.success()

        val token = if (rawToken.startsWith("Bearer ")) rawToken else "Bearer $rawToken"

        val db = ExpenseDatabase.getDatabase(applicationContext)

        val expenseDao = db.expenseDao()

        val budgetDao = db.categoryBudgetDao()

        val baseUrl = inputData.getString("BASE_URL") ?: "http://10.0.2.2:8080/"

        val api = try {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ExpenseTrackerApi::class.java)
        } catch (e: Exception) {
            Log.e("SyncWorker", "Failed to create API client", e)
            return Result.retry()
        }

        try {
            // 1. Sync expenses
            val unsyncedExpenses = expenseDao.getUnsyncedExpenses()

            if (unsyncedExpenses.isNotEmpty()) {

                val dtos = unsyncedExpenses.map { expense ->
                    ExpenseDto(
                        id = expense.serverId ?: UUID.randomUUID().toString(),
                        amount = expense.amount,
                        category = expense.category,
                        description = expense.description,
                        date = expense.date,
                        updatedAt = expense.updatedAt
                    )
                }

                val response = api.syncExpenses(token, SyncExpensesRequest(dtos))

                if (response.isSuccessful && response.body() != null) {

                    val body = response.body()!!

                    unsyncedExpenses.forEachIndexed { index, expense ->

                        val serverId = if (index < body.syncedIds.size)
                            body.syncedIds[index]
                        else
                            dtos[index].id

                        expenseDao.markExpenseSynced(expense.id, serverId)
                    }

                    // Pull remote expenses (conflict resolution: last-write-wins based on updatedAt)
                    body.serverExpenses.forEach { remote ->

                        val localMatch = unsyncedExpenses.find { it.serverId == remote.id }

                        if (localMatch == null || remote.updatedAt > localMatch.updatedAt) {

                            expenseDao.insertExpense(
                                ExpenseEntity(
                                    amount = remote.amount,
                                    category = remote.category,
                                    description = remote.description,
                                    date = remote.date,
                                    serverId = remote.id,
                                    updatedAt = remote.updatedAt,
                                    isSynced = true
                                )
                            )
                        }
                    }
                }
            }

            // 2. Sync budgets
            val unsyncedBudgets = budgetDao.getUnsyncedBudgets()

            if (unsyncedBudgets.isNotEmpty()) {

                val budgetDtos = unsyncedBudgets.map { budget ->
                    BudgetDto(
                        category = budget.category,
                        monthlyLimit = budget.monthlyLimit,
                        updatedAt = budget.updatedAt
                    )
                }

                val budgetResponse = api.syncBudgets(token, SyncBudgetsRequest(budgetDtos))

                if (budgetResponse.isSuccessful && budgetResponse.body() != null) {

                    val body = budgetResponse.body()!!

                    unsyncedBudgets.forEach { budget ->
                        budgetDao.markBudgetSynced(budget.category)
                    }
                }
            }

            return Result.success()

        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed", e)
            return Result.retry()
        }
    }
}
