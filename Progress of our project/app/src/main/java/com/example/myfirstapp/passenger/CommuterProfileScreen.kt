package com.example.myfirstapp.passenger

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myfirstapp.BusSchedule
import com.example.myfirstapp.DatabaseManager
import com.example.myfirstapp.SweetAlertDialog
import com.example.myfirstapp.SweetAlertType
import com.example.myfirstapp.TransitGreen
import com.example.myfirstapp.user

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommuterProfileScreen(
    user: user,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("TransitGoPrefs", Context.MODE_PRIVATE) }

    // State variables for profile details
    var fullName by remember { mutableStateOf(user.fullname) }
    var email by remember { mutableStateOf(user.email) }
    var password by remember { mutableStateOf(user.password) }
    var passwordVisible by remember { mutableStateOf(false) }

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageUrl by remember { mutableStateOf("") }
    var isSavingProfile by remember { mutableStateOf(false) }
    var profileErrorMessage by remember { mutableStateOf("") }

    // Schedules for Trip & Driver Rating
    var schedules by remember { mutableStateOf<List<BusSchedule>>(emptyList()) }
    var ratingScheduleTarget by remember { mutableStateOf<BusSchedule?>(null) }
    var showTripSelectDialog by remember { mutableStateOf(false) }

    // Help & Support dialog state
    var showHelpDialog by remember { mutableStateOf(false) }

    // SweetAlert message state
    var alertTitle by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf("") }
    var alertType by remember { mutableStateOf(SweetAlertType.SUCCESS) }
    var showAlert by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        DatabaseManager.listenToSchedules(
            onUpdate = { list -> schedules = list },
            onError = {}
        )
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            profileImageUri = uri
            Toast.makeText(context, "New profile photo selected!", Toast.LENGTH_SHORT).show()
        }
    }

    if (showAlert) {
        SweetAlertDialog(
            type = alertType,
            title = alertTitle,
            message = alertMessage,
            onConfirm = { showAlert = false },
            onDismiss = { showAlert = false }
        )
    }

    if (ratingScheduleTarget != null) {
        val targetSched = ratingScheduleTarget!!
        CommuterRateDialog(
            busNumber = targetSched.bus.busNumber,
            driverName = targetSched.bus.assignedDriver,
            routeName = targetSched.route.routeName,
            commuterName = user.fullname,
            onDismiss = { ratingScheduleTarget = null },
            onRatingSubmitted = {
                Toast.makeText(context, "Driver rating submitted successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Commuter Profile & Settings", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TransitGreen)
            )
        },
        containerColor = Color(0xFFF5F8F6)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Profile Avatar with Camera Button
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE8F5E9),
                    border = androidx.compose.foundation.BorderStroke(3.dp, TransitGreen),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (profileImageUri != null || profileImageUrl.isNotBlank()) {
                            AsyncImage(
                                model = profileImageUri ?: profileImageUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Profile",
                                tint = TransitGreen,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                }

                // Edit Camera Icon Button
                Surface(
                    shape = CircleShape,
                    color = TransitGreen,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Profile Picture",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = fullName.ifBlank { user.fullname },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = email.ifBlank { user.email },
                fontSize = 13.sp,
                color = Color.Gray
            )
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text(
                    text = "Role: ${user.role}",
                    fontSize = 12.sp,
                    color = TransitGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card 1: Account Settings (Email & Password)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Edit Account Credentials",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TransitGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Full Name Field
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            profileErrorMessage = ""
                        },
                        label = { Text("Full Name (Last Name, First Name, M.I.)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TransitGreen) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            profileErrorMessage = ""
                        },
                        label = { Text("Email Address (@g.batstate-u.edu.ph)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TransitGreen) },
                        supportingText = { Text("Must be @g.batstate-u.edu.ph email", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            profileErrorMessage = ""
                        },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TransitGreen) },
                        supportingText = { Text("Must be at least 8 characters", fontSize = 11.sp) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = TransitGreen
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (profileErrorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = profileErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Save Profile Button
                    Button(
                        onClick = {
                            when {
                                fullName.isBlank() -> profileErrorMessage = "Full name cannot be empty"
                                email.isBlank() -> profileErrorMessage = "Email address cannot be empty"
                                !email.trim().lowercase().endsWith("@g.batstate-u.edu.ph") -> profileErrorMessage = "Email must end with @g.batstate-u.edu.ph"
                                password.length < 8 -> profileErrorMessage = "Password must be at least 8 characters"
                                else -> {
                                    isSavingProfile = true
                                    profileErrorMessage = ""

                                    val performUserSave = {
                                        val updatedUser = user(
                                            userId = user.userId,
                                            fullname = fullName.trim(),
                                            email = email.trim(),
                                            password = password,
                                            role = user.role
                                        )

                                        DatabaseManager.saveUser(
                                            user = updatedUser,
                                            onSuccess = {
                                                isSavingProfile = false
                                                user.fullname = updatedUser.fullname
                                                user.email = updatedUser.email
                                                user.password = updatedUser.password

                                                // Update local SharedPreferences
                                                prefs.edit()
                                                    .putString("active_name", updatedUser.fullname)
                                                    .putString("active_email", updatedUser.email)
                                                    .apply()

                                                alertType = SweetAlertType.SUCCESS
                                                alertTitle = "Profile Updated!"
                                                alertMessage = "Your profile picture, email, and password have been saved successfully."
                                                showAlert = true
                                            },
                                            onFailure = { err ->
                                                isSavingProfile = false
                                                profileErrorMessage = err.localizedMessage ?: "Failed to update profile"
                                            }
                                        )
                                    }

                                    if (profileImageUri != null) {
                                        DatabaseManager.uploadBusImageToSupabaseStorage(
                                            context = context,
                                            imageUri = profileImageUri!!,
                                            onSuccess = { uploadedUrl ->
                                                profileImageUrl = uploadedUrl
                                                performUserSave()
                                            },
                                            onFailure = {
                                                performUserSave()
                                            }
                                        )
                                    } else {
                                        performUserSave()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TransitGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSavingProfile) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Save Account Changes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Trip & Driver Rating & Help Support Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("App Info & Support", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TransitGreen)
                    HorizontalDivider()

                    // Trip & Driver Rating Tile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTripSelectDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Trip & Driver Rating", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                            Text("Rate your bus drivers and completed trips", fontSize = 11.sp, color = Color.Gray)
                        }
                        Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = TransitGreen, modifier = Modifier.size(18.dp))
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    // Help & Support Tile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHelpDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = TransitGreen, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Help & Support Hotline", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                            Text("Customer service FAQs and contact details", fontSize = 11.sp, color = Color.Gray)
                        }
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Trip Selection Dialog for Rating Driver
    if (showTripSelectDialog) {
        AlertDialog(
            onDismissRequest = { showTripSelectDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Trip to Rate Driver", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (schedules.isEmpty()) {
                        Text("No recent trips found to rate.", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        schedules.forEach { sched ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showTripSelectDialog = false
                                        ratingScheduleTarget = sched
                                    },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Bus: ${sched.bus.busNumber} • Route: ${sched.route.routeName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                    Text("Driver: ${sched.bus.assignedDriver.ifBlank { "Unassigned" }}", fontSize = 12.sp, color = TransitGreen)
                                    Text("Time: ${sched.departureTime} - ${sched.arrivalTime}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTripSelectDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TransitGreen)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Help & Support Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = TransitGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TransitGo Help & Support", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("📞 Hotline: (043) 980-0385", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TransitGreen)
                            Text("✉️ Email: support@transitgo.ph", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TransitGreen)
                            Text("⏰ Service Hours: 6:00 AM - 10:00 PM Daily", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }

                    Text("Frequently Asked Questions (FAQs)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)

                    Text("Q: How do I track my bus in real time?", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("A: Tap 'Live Bus Tracker' on your home screen or select a route to view GPS locations.", fontSize = 11.sp, color = Color.DarkGray)

                    Text("Q: How do I pay using GCash / PayMaya?", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("A: Select your route, tap 'Book Ticket', and choose GCash/PayMaya to scan the official QR code.", fontSize = 11.sp, color = Color.DarkGray)

                    Text("Q: What if I lose something on the bus?", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("A: Call our support hotline immediately with your Bus Number and Schedule ID.", fontSize = 11.sp, color = Color.DarkGray)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TransitGreen)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
