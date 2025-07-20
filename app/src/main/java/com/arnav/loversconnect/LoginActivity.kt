// LoginActivity.kt
package com.arnav.loversconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    // Inside LoginActivity class
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // ADD THIS BLOCK TO APPLY THE THEME
        val sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE)
        val themeId = sharedPreferences.getInt("theme_id", 0) // Default to 0 (Pink)
        when (themeId) {
            0 -> setTheme(R.style.Theme_LoversConnect)
            1 -> setTheme(R.style.Theme_LoversConnect_Blue)
            2 -> setTheme(R.style.Theme_LoversConnect_Purple)
        }
        super.onCreate(savedInstanceState)
        // This connects the Kotlin file to the XML layout file.
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        // Inside LoginActivity.onCreate method

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Authentication successful, go to MainActivity
                    Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Authentication error, user can't proceed.
                    // You might want to close the app or just stay on the login screen.
                    Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    // In a real app, you might want to finish() the activity here if security is critical.
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Authentication failed (e.g., wrong fingerprint)
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Lock")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use Account Password") // Allows user to cancel and login normally
            .build()

// Check if user is already logged in
        if (auth.currentUser != null) {
            // If logged in, show biometric prompt instead of the login form
            biometricPrompt.authenticate(promptInfo)
        }
// The rest of your onCreate method (finding views, setting click listener) remains the same

        // If the user is already logged in, skip the login screen.
        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Find the views from our layout file.
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Set a listener to do something when the login button is clicked.
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // If login is successful, go to the MainActivity.
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Close the login screen.
                    } else {
                        // If login fails, show an error message.
                        Toast.makeText(this, "Login Failed. Check your credentials.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}