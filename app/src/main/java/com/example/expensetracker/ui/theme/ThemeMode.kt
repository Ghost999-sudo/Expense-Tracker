package com.example.expensetracker.ui.theme

enum class ThemeMode(
    val storageValue: String,
    val label: String
) {
    SYSTEM("system", "System default"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark")
}