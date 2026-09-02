package com.example.expensetracker.viewmodel

sealed class BudgetRecommendation {

    abstract val category: String

    /** Budget set and spend is between 80–99 % of limit. */
    data class ApproachingBudget(
        override val category: String,
        val spentCents: Long,
        val limitCents: Long,
        val percentUsed: Int
    ) : BudgetRecommendation()

    /** Budget set and spend has hit or exceeded 100 % of limit. */
    data class OverBudget(
        override val category: String,
        val spentCents: Long,
        val limitCents: Long,
        val overByPercent: Int
    ) : BudgetRecommendation()

    /**
     * No budget set but this month's spend is > 30 % more than last
     * month's spend in the same category.
     */
    data class UnusualSpend(
        override val category: String,
        val thisMonthCents: Long,
        val lastMonthCents: Long,
        val increasePercent: Int
    ) : BudgetRecommendation()
}
