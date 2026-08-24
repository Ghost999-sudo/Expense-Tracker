package com.example.expensetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

private fun categoryPercentage(
    categoryAmount: Long,
    total: Long
): Float {

    if (total <= 0) {
        return 0f
    }

    return (
        categoryAmount.toFloat() /
        total.toFloat()
    )
}

@Composable
fun CategorySpendingBar(
    category: String,
    amount: Long,
    total: Long
) {

    val percentage =
        categoryPercentage(
            amount,
            total
        )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text = category,
                style =
                    MaterialTheme.typography
                        .titleMedium
            )

            Text(
                text = formatAmount(amount),
                style =
                    MaterialTheme.typography
                        .titleMedium
            )
        }

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        LinearProgressIndicator(

            progress = {
                percentage
            },

            modifier =
                Modifier.fillMaxWidth()
        )
    }
}

private fun formatAmount(
    amount: Long
): String {

    return String.format(
        Locale.US,
        "KSh %,.2f",
        amount / 100.0
    )
}
