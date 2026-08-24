package com.example.expensetracker.viewmodel

enum class ReportPeriod(
    val label: String
) {

    TODAY("Today"),

    WEEK("This Week"),

    MONTH("This Month"),

    YEAR("This Year"),

    CUSTOM("Custom Range");

    companion object {

        val default =
            MONTH
    }
}
