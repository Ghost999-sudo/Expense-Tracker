package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expensetracker.data.local.ExpenseDatabase
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.ui.components.ExpenseBottomBar
import com.example.expensetracker.ui.screens.AddExpenseScreen
import com.example.expensetracker.ui.screens.ExpenseDetailsScreen
import com.example.expensetracker.ui.screens.HomeScreen
import com.example.expensetracker.ui.screens.ReportsScreen
import com.example.expensetracker.ui.screens.SettingsScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy {
        ExpenseDatabase.getDatabase(applicationContext)
    }

    private val repository by lazy {
        ExpenseRepository(
            database.expenseDao()
        )
    }

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                ExpenseTrackerApp(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ExpenseTrackerApp(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {

    val navController = rememberNavController()

    val backStackEntry by navController
        .currentBackStackEntryAsState()

    val currentRoute =
        backStackEntry?.destination?.route

    val showBottomBar = currentRoute in setOf(
        "home",
        "reports",
        "settings"
    )

    Scaffold(
        modifier = modifier,

        bottomBar = {

            if (showBottomBar) {

                ExpenseBottomBar(
                    currentRoute = currentRoute,

                    onHomeClick = {

                        navController.navigate("home") {

                            popUpTo("home") {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    },

                    onReportsClick = {

                        navController.navigate("reports") {
                            launchSingleTop = true
                        }
                    },

                    onSettingsClick = {

                        navController.navigate("settings") {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("home") {

                HomeScreen(
                    viewModel = viewModel,
                    onAddExpense = {
                        navController.navigate(
                            "add_expense"
                        )
                    },
                    onExpenseClick = { expenseId ->
                        navController.navigate(
                            "expense/$expenseId"
                        )
                    }
                )
            }

            composable(
                route = "expense/{expenseId}",
                arguments = listOf(
                    navArgument("expenseId") {
                        type = NavType.IntType
                    }
                )
            ) { entry ->

                val expenseId =
                    entry.arguments
                        ?.getInt("expenseId")
                        ?: return@composable

                ExpenseDetailsScreen(
                    expenseId = expenseId,
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("add_expense") {

                AddExpenseScreen(
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("reports") {

                ReportsScreen(
                    viewModel = viewModel
                )
            }

            composable("settings") {

                SettingsScreen()
            }
        }
    }
}
