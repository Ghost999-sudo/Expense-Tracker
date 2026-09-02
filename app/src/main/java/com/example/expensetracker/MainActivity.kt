package com.example.expensetracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expensetracker.data.local.ExpenseDatabase
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.data.repository.BudgetRepository
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.ui.components.ExpenseBottomBar
import com.example.expensetracker.ui.screens.AddExpenseScreen
import com.example.expensetracker.ui.screens.AuthScreen
import com.example.expensetracker.ui.screens.CameraScreen
import com.example.expensetracker.ui.screens.ExpenseDetailsScreen
import com.example.expensetracker.ui.screens.HomeScreen
import com.example.expensetracker.ui.screens.ReportsScreen
import com.example.expensetracker.ui.screens.SettingsScreen
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewmodel.AuthViewModel
import com.example.expensetracker.viewmodel.AuthViewModelFactory
import com.example.expensetracker.viewmodel.BudgetViewModel
import com.example.expensetracker.viewmodel.BudgetViewModelFactory
import com.example.expensetracker.viewmodel.ExpenseViewModel
import com.example.expensetracker.viewmodel.ExpenseViewModelFactory
import com.example.expensetracker.viewmodel.ParsedReceipt
import com.example.expensetracker.viewmodel.SettingsViewModel
import com.example.expensetracker.viewmodel.SettingsViewModelFactory
import java.io.File

class MainActivity : ComponentActivity() {

    private val database by lazy {
        ExpenseDatabase.getDatabase(applicationContext)
    }

    private val repository by lazy {
        ExpenseRepository(
            database.expenseDao()
        )
    }

    private val budgetRepository by lazy {
        BudgetRepository(
            database.categoryBudgetDao()
        )
    }

    private val authRepository by lazy {
        AuthRepository(
            database.userDao(),
            applicationContext
        )
    }

    private val settingsRepository by lazy {
        SettingsRepository(applicationContext)
    }

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory(repository)
    }

    private val budgetViewModel: BudgetViewModel by viewModels {
        BudgetViewModelFactory(budgetRepository, repository)
    }

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(authRepository)
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(settingsRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val settingsUiState by settingsViewModel
                .settingsUiState
                .collectAsStateWithLifecycle()

            ExpenseTrackerTheme(
                themeMode = settingsUiState.themeMode
            ) {

                ExpenseTrackerRoot(
                    expenseViewModel = viewModel,
                    budgetViewModel = budgetViewModel,
                    authViewModel = authViewModel,
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ExpenseTrackerRoot(
    expenseViewModel: ExpenseViewModel,
    budgetViewModel: BudgetViewModel,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {

    val authUiState by authViewModel.authUiState
        .collectAsStateWithLifecycle()

    // Allow the user to skip login and use the app offline
    var skipAuth by remember { mutableStateOf(false) }

    when {

        authUiState.isInitializing -> {

            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {

                CircularProgressIndicator()
            }
        }

        authUiState.currentUser == null && !skipAuth -> {

            AuthScreen(
                authViewModel = authViewModel,
                onSkip = { skipAuth = true },
                modifier = modifier
            )
        }

        else -> {

            ExpenseTrackerApp(
                viewModel = expenseViewModel,
                budgetViewModel = budgetViewModel,
                settingsViewModel = settingsViewModel,
                authViewModel = authViewModel,
                modifier = modifier
            )
        }
    }
}

@Composable
fun ExpenseTrackerApp(
    viewModel: ExpenseViewModel,
    budgetViewModel: BudgetViewModel,
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {

    val navController = rememberNavController()

    val backStackEntry by navController
        .currentBackStackEntryAsState()

    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in setOf(
        "home",
        "reports",
        "settings"
    )

    var pendingPdf by remember {
        mutableStateOf<File?>(null)
    }

    val context = LocalContext.current

    val savePdfLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/pdf")
        ) { uri ->

            val file = pendingPdf

            if (uri != null && file != null) {

                savePdfTo(context, uri, file)
            }

            pendingPdf = null
        }

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
                    budgetViewModel = budgetViewModel,
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

            composable("add_expense") { backEntry ->

                val prefill = backEntry
                    .savedStateHandle
                    .get<ParsedReceipt>("parsed_receipt")

                AddExpenseScreen(
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onScanReceipt = {
                        navController.navigate("camera_scan")
                    },
                    prefill = prefill
                )
            }

            composable("camera_scan") {

                CameraScreen(
                    onScanComplete = { parsed ->

                        navController
                            .previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("parsed_receipt", parsed)

                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("reports") {

                ReportsScreen(
                    viewModel = viewModel,
                    onPdfGenerated = { file ->
                        pendingPdf = file
                    }
                )
            }

            composable("settings") {

                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    authViewModel = authViewModel,
                    budgetViewModel = budgetViewModel
                )
            }
        }
    }

    pendingPdf?.let { file ->

        PdfActionDialog(
            fileName = file.name,
            onOpen = {
                openPdf(context, file)
                pendingPdf = null
            },
            onShare = {
                sharePdf(context, file)
                pendingPdf = null
            },
            onSave = {
                savePdfLauncher.launch(file.name)
            },
            onDismiss = {
                pendingPdf = null
            }
        )
    }
}

@Composable
private fun PdfActionDialog(
    fileName: String,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("PDF Generated")
        },
        text = {
            Column {
                Text("Expense report ready.")

                TextButton(
                    onClick = onOpen
                ) {
                    Text("Open PDF")
                }

                TextButton(
                    onClick = onShare
                ) {
                    Text("Share PDF")
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save to Downloads")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun pdfUri(
    context: android.content.Context,
    file: File
): Uri {

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

private fun openPdf(
    context: android.content.Context,
    file: File
) {

    val uri = pdfUri(context, file)

    val intent = Intent(
        Intent.ACTION_VIEW
    ).apply {

        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(intent, "Open PDF")
    )
}

private fun sharePdf(
    context: android.content.Context,
    file: File
) {

    val uri = pdfUri(context, file)

    val intent = Intent(
        Intent.ACTION_SEND
    ).apply {

        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(intent, "Share PDF")
    )
}

private fun savePdfTo(
    context: android.content.Context,
    uri: Uri,
    file: File
) {

    try {

        context.contentResolver
            .openOutputStream(uri)
            ?.use { output ->

                file.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

    } catch (exception: Exception) {

        exception.printStackTrace()
    }
}