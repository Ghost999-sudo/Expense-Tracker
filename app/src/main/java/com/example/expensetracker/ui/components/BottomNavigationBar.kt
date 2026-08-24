package com.example.expensetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ExpenseBottomBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onReportsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {

    NavigationBar {

        NavigationBarItem(

            selected =
                currentRoute == "home",

            onClick =
                onHomeClick,

            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home"
                )
            },

            label = {
                Text("Home")
            }
        )

        NavigationBarItem(

            selected =
                currentRoute == "reports",

            onClick =
                onReportsClick,

            icon = {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = "Reports"
                )
            },

            label = {
                Text("Reports")
            }
        )

        NavigationBarItem(

            selected =
                currentRoute == "settings",

            onClick =
                onSettingsClick,

            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            },

            label = {
                Text("Settings")
            }
        )
    }
}
