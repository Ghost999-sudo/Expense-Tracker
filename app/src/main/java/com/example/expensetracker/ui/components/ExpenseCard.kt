package com.example.expensetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseCard(
    category: String,
    description: String,
    amount: Long,
    date: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val icon = getCategoryIcon(category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = category,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Spacer(
                modifier = Modifier.size(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium
                )

                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = formatDate(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatAmount(amount),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private fun getCategoryIcon(
    category: String
): ImageVector {

    return when (category.lowercase()) {

        "food" -> Icons.Default.Fastfood

        "transport" -> Icons.Default.DirectionsCar

        "bills" -> Icons.Default.AccountBalance

        "shopping" -> Icons.Default.ShoppingCart

        "entertainment" -> Icons.Default.TheaterComedy

        "health" -> Icons.Default.HealthAndSafety

        "education" -> Icons.Default.School

        else -> Icons.Default.MoreHoriz
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

private fun formatDate(
    timestamp: Long
): String {

    val formatter = SimpleDateFormat(
        "dd MMM yyyy",
        Locale.getDefault()
    )

    return formatter.format(Date(timestamp))
}
