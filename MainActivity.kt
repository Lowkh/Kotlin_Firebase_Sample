package com.example.loginapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/**
 * MainActivity.kt
 * 
 * This is the main entry point of the app.
 * It contains all the UI screens using Jetpack Compose.
 * 
 * Screens:
 * 1. LoginApp() - Decides which screen to show (Login or Welcome)
 * 2. LoginScreenWithSignUp() - Login/Sign Up form
 * 3. WelcomeScreen() - Shown after successful login
 */

// ========== MAIN ACTIVITY CLASS ==========

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LoginApp()
            }
        }
    }
}

// ========== MAIN SCREEN (DECIDES WHICH SCREEN TO SHOW) ==========

/**
 * LoginApp()
 * 
 * This is the main screen that decides what to show.
 * - If user is logged in → show WelcomeScreen
 * - If user is not logged in → show LoginScreenWithSignUp
 * 
 * It's like a "router" that controls navigation.
 */
@Composable
fun LoginApp() {
    // Create Firebase helper (remembers it between recomposes)
    val firebaseHelper = remember { FirebaseHelper() }
    
    // Track if user is logged in
    // When this changes, Compose automatically recomposes and shows new screen
    var isLoggedIn by remember { mutableStateOf(firebaseHelper.isLoggedIn()) }

    // Show different screen based on login status
    if (isLoggedIn) {
        // User is logged in → Show welcome screen
        WelcomeScreen(
            email = firebaseHelper.getCurrentUserEmail() ?: "Unknown",
            onSignOut = {
                firebaseHelper.signOut()
                isLoggedIn = false
            }
        )
    } else {
        // User is not logged in → Show login/signup screen
        LoginScreenWithSignUp(
            firebaseHelper = firebaseHelper,
            onLoginSuccess = {
                isLoggedIn = true
            }
        )
    }
}

// ========== LOGIN & SIGN UP SCREEN ==========

/**
 * LoginScreenWithSignUp()
 * 
 * This screen shows both Login and Sign Up forms.
 * User can toggle between them.
 * 
 * Features:
 * - Email input field
 * - Password input field
 * - Confirm password field (only in Sign Up mode)
 * - Login/Sign Up button (changes based on mode)
 * - Toggle link to switch between modes
 * - Error messages
 * - Loading spinner
 */
@Composable
fun LoginScreenWithSignUp(
    firebaseHelper: FirebaseHelper,
    onLoginSuccess: () -> Unit
) {
    // State variables for form fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ===== TITLE =====
        Text(
            text = if (isSignUp) "Create Account" else "Welcome Back",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ===== EMAIL FIELD =====
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ===== PASSWORD FIELD =====
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            enabled = !isLoading
        )

        // ===== CONFIRM PASSWORD FIELD (ONLY IN SIGN UP MODE) =====
        if (isSignUp) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !isLoading
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== LOGIN/SIGN UP BUTTON =====
        Button(
            onClick = {
                errorMessage = ""

                if (isSignUp) {
                    // ===== SIGN UP LOGIC =====
                    // Check if passwords match
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }

                    // Call Firebase to sign up
                    isLoading = true
                    firebaseHelper.signUp(
                        email = email,
                        password = password,
                        onSuccess = {
                            isLoading = false
                            onLoginSuccess()
                        },
                        onFailure = { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                } else {
                    // ===== SIGN IN LOGIC =====
                    // Call Firebase to sign in
                    isLoading = true
                    firebaseHelper.signIn(
                        email = email,
                        password = password,
                        onSuccess = {
                            isLoading = false
                            onLoginSuccess()
                        },
                        onFailure = { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                // Show loading spinner
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                // Show button text
                Text(if (isSignUp) "Sign Up" else "Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== TOGGLE BUTTON (SWITCH BETWEEN LOGIN AND SIGN UP) =====
        TextButton(
            onClick = {
                isSignUp = !isSignUp
                errorMessage = ""
                password = ""
                confirmPassword = ""
            }
        ) {
            Text(
                if (isSignUp)
                    "Already have an account? Log in"
                else
                    "Don't have an account? Sign up"
            )
        }

        // ===== ERROR MESSAGE =====
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

// ========== WELCOME SCREEN ==========

/**
 * WelcomeScreen()
 * 
 * This screen is shown after user successfully logs in.
 * It displays:
 * - Welcome message
 * - User's email in a card
 * - Sign Out button
 */
@Composable
fun WelcomeScreen(
    email: String,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ===== WELCOME MESSAGE =====
        Text(
            text = "Welcome! 🎉",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ===== USER INFO CARD =====
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "You are logged in as:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ===== SIGN OUT BUTTON =====
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out")
        }
    }
}
