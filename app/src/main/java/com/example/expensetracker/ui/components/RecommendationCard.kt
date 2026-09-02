package com.example.expensetracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.viewmodel.BudgetRecommendation
import java.util.Locale

@Composable
fun RecommendationCard(
    recommendation: BudgetRecommendation,
    modifier: Modifier = Modifier
) {

    val (icon, containerColor, title, body) = when (recommendation) {

        is BudgetRecommendation.OverBudget -> RecommendationContent(
            icon = Icons.Default.Warning,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            title = "${recommendation.category} — Over Budget",
            body = "You've spent ${formatAmount(recommendation.spentCents)} " +
                "of your ${formatAmount(recommendation.limitCents)} budget " +
                "(${recommendation.overByPercent + 100}%)."
        )

        is BudgetRecommendation.ApproachingBudget -> RecommendationContent(
            icon = Icons.Default.Warning,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            title = "${recommendation.category} — Approaching Budget",
            body = "You've used ${recommendation.percentUsed}% of your " +
                "${formatAmount(recommendation.limitCents)} budget " +
                "(${formatAmount(recommendation.spentCents)} spent)."
        )

        is BudgetRecommendation.UnusualSpend -> RecommendationContent(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            title = "${recommendation.category} — Spending Up",
            body = "You've spent ${formatAmount(recommendation.thisMonthCents)} " +
                "this month vs ${formatAmount(recommendation.lastMonthCents)} " +
                "last month (↑${recommendation.increasePercent}%)."
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private data class RecommendationContent(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val containerColor: androidx.compose.ui.graphics.Color,
    val title: String,
    val body: String
)

private fun formatAmount(cents: Long): String =
    String.format(Locale.US, "KSh %,.2f", cents / 100.0)
