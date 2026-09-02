package com.example.expensetracker.data.remote.api

import com.example.expensetracker.data.remote.dto.AuthRequest
import com.example.expensetracker.data.remote.dto.AuthResponse
import com.example.expensetracker.data.remote.dto.BudgetDto
import com.example.expensetracker.data.remote.dto.ExpenseDto
import com.example.expensetracker.data.remote.dto.SyncBudgetsRequest
import com.example.expensetracker.data.remote.dto.SyncBudgetsResponse
import com.example.expensetracker.data.remote.dto.SyncExpensesRequest
import com.example.expensetracker.data.remote.dto.SyncExpensesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ExpenseTrackerApi {

    @POST("api/auth/register")
    suspend fun register(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @GET("api/expenses")
    suspend fun getExpenses(
        @Header("Authorization") token: String
    ): Response<List<ExpenseDto>>

    @POST("api/expenses/sync")
    suspend fun syncExpenses(
        @Header("Authorization") token: String,
        @Body request: SyncExpensesRequest
    ): Response<SyncExpensesResponse>

    @GET("api/budgets")
    suspend fun getBudgets(
        @Header("Authorization") token: String
    ): Response<List<BudgetDto>>

    @POST("api/budgets/sync")
    suspend fun syncBudgets(
        @Header("Authorization") token: String,
        @Body request: SyncBudgetsRequest
    ): Response<SyncBudgetsResponse>
}
