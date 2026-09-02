package com.example.expensetracker.viewmodel

import com.example.expensetracker.data.local.CategoryBudgetEntity
import com.example.expensetracker.data.local.CategoryTotal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationEngineTest {

    private fun computeRecommendations(
        thisMonthTotals: List<CategoryTotal>,
        prevMonthTotals: List<CategoryTotal>,
        budgets: Map<String, CategoryBudgetEntity>,
        categories: List<String> = listOf("Food", "Transport", "Bills")
    ): List<BudgetRecommendation> {

        val list = mutableListOf<BudgetRecommendation>()

        categories.forEach { category ->

            val thisMonthSpend =
                thisMonthTotals.find { it.category == category }?.total ?: 0L

            val prevMonthSpend =
                prevMonthTotals.find { it.category == category }?.total ?: 0L

            val budget = budgets[category]

            if (budget != null && budget.monthlyLimit > 0) {

                val pct = thisMonthSpend.toDouble() / budget.monthlyLimit.toDouble()

                when {
                    pct >= 1.0 -> {
                        list.add(
                            BudgetRecommendation.OverBudget(
                                category = category,
                                spentCents = thisMonthSpend,
                                limitCents = budget.monthlyLimit,
                                overByPercent = (pct * 100).toInt() - 100
                            )
                        )
                    }

                    pct >= 0.80 -> {
                        list.add(
                            BudgetRecommendation.ApproachingBudget(
                                category = category,
                                spentCents = thisMonthSpend,
                                limitCents = budget.monthlyLimit,
                                percentUsed = (pct * 100).toInt()
                            )
                        )
                    }
                }
            } else if (prevMonthSpend > 0) {

                val increase =
                    (thisMonthSpend - prevMonthSpend).toDouble() / prevMonthSpend.toDouble()

                if (increase >= 0.30) {
                    list.add(
                        BudgetRecommendation.UnusualSpend(
                            category = category,
                            thisMonthCents = thisMonthSpend,
                            lastMonthCents = prevMonthSpend,
                            increasePercent = (increase * 100).toInt()
                        )
                    )
                }
            }
        }

        return list
    }

    @Test
    fun `flags over budget when spend exceeds limit`() {

        val budgets = mapOf(
            "Food" to CategoryBudgetEntity("Food", 10_000L) // limit: 100
        )
        val thisMonth = listOf(
            CategoryTotal("Food", 12_000L) // spend: 120
        )

        val recs = computeRecommendations(thisMonth, emptyList(), budgets)

        assertEquals(1, recs.size)
        assertTrue(recs.first() is BudgetRecommendation.OverBudget)

        val over = recs.first() as BudgetRecommendation.OverBudget
        assertEquals("Food", over.category)
        assertEquals(20, over.overByPercent)
    }

    @Test
    fun `flags approaching budget when spend is between 80% and 99% of limit`() {

        val budgets = mapOf(
            "Transport" to CategoryBudgetEntity("Transport", 10_000L)
        )
        val thisMonth = listOf(
            CategoryTotal("Transport", 8_500L) // 85%
        )

        val recs = computeRecommendations(thisMonth, emptyList(), budgets)

        assertEquals(1, recs.size)
        assertTrue(recs.first() is BudgetRecommendation.ApproachingBudget)

        val app = recs.first() as BudgetRecommendation.ApproachingBudget
        assertEquals(85, app.percentUsed)
    }

    @Test
    fun `flags unusual spend when spending increases by 30% or more over prev month`() {

        val thisMonth = listOf(
            CategoryTotal("Bills", 15_000L)
        )
        val prevMonth = listOf(
            CategoryTotal("Bills", 10_000L) // +50% increase
        )

        val recs = computeRecommendations(thisMonth, prevMonth, emptyMap())

        assertEquals(1, recs.size)
        assertTrue(recs.first() is BudgetRecommendation.UnusualSpend)

        val unusual = recs.first() as BudgetRecommendation.UnusualSpend
        assertEquals(50, unusual.increasePercent)
    }

    @Test
    fun `returns empty list when spend is normal and within budget`() {

        val budgets = mapOf(
            "Food" to CategoryBudgetEntity("Food", 10_000L)
        )
        val thisMonth = listOf(
            CategoryTotal("Food", 5_000L) // 50%
        )

        val recs = computeRecommendations(thisMonth, emptyList(), budgets)

        assertTrue(recs.isEmpty())
    }
}
