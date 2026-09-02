package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onSkip: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    val authUiState by authViewModel.authUiState
        .collectAsStateWithLifecycle()

    var isRegisterMode by remember {
        mutableStateOf(false)
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "Expense Tracker",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text =
                if (isRegisterMode)
                    "Create an account to get started."
                else
                    "Welcome back. Sign in to continue.",
            style =
                MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                OutlinedTextField(

                    value = email,

                    onValueChange = { email = it },

                    modifier = Modifier.fillMaxWidth(),

                    label = {
                        Text("Email")
                    },

                    singleLine = true,

                    keyboardOptions = KeyboardOptions(
                        keyboardType =
                            KeyboardType.Email
                    )
                )

                OutlinedTextField(

                    value = password,

                    onValueChange = { password = it },

                    modifier = Modifier.fillMaxWidth(),

                    label = {
                        Text("Password")
                    },

                    singleLine = true,

                    keyboardOptions = KeyboardOptions(
                        keyboardType =
                            KeyboardType.Password
                    ),

                    visualTransformation =
                        if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),

                    trailingIcon = {

                        IconButton(
                            onClick = {
                                passwordVisible =
                                    !passwordVisible
                            }
                        ) {

                            Icon(
                                imageVector =
                                    if (passwordVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                contentDescription =
                                    if (passwordVisible)
                                        "Hide password"
                                    else
                                        "Show password"
                            )
                        }
                    }
                )

                authUiState.error?.let { error ->

                    Text(
                        text = error,
                        color =
                            MaterialTheme.colorScheme.error,
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = {

                        if (isRegisterMode) {

                            authViewModel.register(
                                email,
                                password
                            )

                        } else {

                            authViewModel.login(
                                email,
                                password
                            )
                        }
                    },

                    enabled =
                        !authUiState.isSubmitting,

                    modifier = Modifier.fillMaxWidth()
                ) {

                    if (authUiState.isSubmitting) {

                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )

                        Spacer(
                            modifier =
                                Modifier.size(8.dp)
                        )

                    }

                    Text(
                        if (isRegisterMode)
                            "Create Account"
                        else
                            "Sign In"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.Center
                ) {

                    Text(
                        text =
                            if (isRegisterMode)
                                "Already have an account?"
                            else
                                "Don't have an account?",
                        style =
                            MaterialTheme.typography.bodyMedium
                    )

                    TextButton(
                        onClick = {

                            isRegisterMode =
                                !isRegisterMode
                        }
                    ) {

                        Text(
                            if (isRegisterMode)
                                "Sign In"
                            else
                                "Register"
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        TextButton(
            onClick = onSkip
        ) {
            Text("Continue without account")
        }
    }
}