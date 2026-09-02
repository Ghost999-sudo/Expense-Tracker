package com.example.expensetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.viewmodel.BudgetRecommendation

/**
 * Shows a titled list of [RecommendationCard]s.
 * Renders nothing if [recommendations] is empty.
 */
@Composable
fun InsightsSection(
    recommendations: List<BudgetRecommendation>,
    modifier: Modifier = Modifier
) {

    if (recommendations.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "Insights",
            style = MaterialTheme.typography.titleLarge
        )

        recommendations.forEach { recommendation ->

            RecommendationCard(
                recommendation = recommendation
            )
        }
    }
}
