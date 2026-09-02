package com.example.expensetracker.data.remote.dto

data class AuthRequest(
    val email: String,
    val passwordHash: String
)

data class AuthResponse(
    val token: String,
    val email: String
)
