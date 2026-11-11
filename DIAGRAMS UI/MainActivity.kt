package com.example.busstopinformationsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val manager = remember { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()

    var signup by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BUS STOP",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1769AA)
        )

        Text(
            text = "Information System",
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (signup) "Create Account" else "Welcome Back!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email address") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation =
                        PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (email.isBlank() ||
                            password.length < 6
                        ) {
                            message =
                                "Enter a valid email and a password with at least 6 characters."
                        } else {
                            busy = true
                            message = ""

                            val task = if (signup) {
                                auth.createUserWithEmailAndPassword(
                                    email.trim(), password
                                )
                            } else {
                                auth.signInWithEmailAndPassword(
                                    email.trim(), password
                                )
                            }

                            task.addOnCompleteListener { result ->
                                busy = false
                                message = if (result.isSuccessful) {
                                    "Success! Welcome, ${
                                        auth.currentUser?.email
                                    }"
                                } else {
                                    result.exception?.localizedMessage
                                        ?: "Authentication failed."
                                }
                            }
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        if (busy) "Please wait..."
                        else if (signup) "Create Account"
                        else "Sign In"
                    )
                }

                TextButton(
                    onClick = {
                        signup = !signup
                        message = ""
                    }
                ) {
                    Text(
                        if (signup)
                            "Already have an account? Sign in"
                        else
                            "New here? Create an account"
                    )
                }

                HorizontalDivider()

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            try {
                                val option =
                                    GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(
                                            false
                                        )
                                        .setServerClientId(
                                            context.getString(
                                                R.string.default_web_client_id
                                            )
                                        )
                                        .build()

                                val request =
                                    GetCredentialRequest.Builder()
                                        .addCredentialOption(option)
                                        .build()

                                val result =
                                    manager.getCredential(
                                        context, request
                                    )

                                val credential = result.credential

                                if (
                                    credential is CustomCredential &&
                                    credential.type ==
                                    GoogleIdTokenCredential
                                        .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                ) {
                                    val google =
                                        GoogleIdTokenCredential
                                            .createFrom(
                                                credential.data
                                            )

                                    val firebaseCredential =
                                        GoogleAuthProvider
                                            .getCredential(
                                                google.idToken, null
                                            )

                                    busy = true

                                    auth.signInWithCredential(
                                        firebaseCredential
                                    ).addOnCompleteListener { task ->
                                        busy = false
                                        message =
                                            if (task.isSuccessful) {
                                                "Google sign-in successful!"
                                            } else {
                                                task.exception
                                                    ?.localizedMessage
                                                    ?: "Google sign-in failed."
                                            }
                                    }
                                } else {
                                    message =
                                        "Please select a Google account."
                                }
                            } catch (e: GetCredentialException) {
                                message =
                                    e.localizedMessage
                                        ?: "Google sign-in was cancelled or failed."
                            } catch (e: Exception) {
                                message =
                                    e.localizedMessage
                                        ?: "Unable to sign in with Google."
                            }
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("G   Continue with Google")
                }

                if (message.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = message,
                        color = if (
                            message.startsWith("Success!") ||
                            message.startsWith("Google sign-in successful")
                        ) Color(0xFF168447)
                        else Color(0xFFB3261E),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}