package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.example.data.LoginResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AuditDatabase
import com.example.data.AuditQuestion
import com.example.data.AuditRepository
import com.example.data.AuditSection
import com.example.data.AuditTab
import com.example.data.AuditViewModel
import com.example.data.AuditViewModelFactory
import com.example.data.ProfileEntity
import com.example.data.auditSections
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    val db = remember { AuditDatabase.getDatabase(context) }
    val repository = remember { AuditRepository(db.auditDao()) }
    val factory = remember { AuditViewModelFactory(repository) }
    val viewModel: AuditViewModel = viewModel(factory = factory)

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val answers by viewModel.answers.collectAsState()
    val showValidationErrors by viewModel.showValidationErrors.collectAsState()

    // Collect export events and open file share dialog
    LaunchedEffect(Unit) {
        viewModel.exportEvents.collectLatest { result ->
            when (result) {
                is AuditViewModel.ExportResult.Success -> {
                    Toast.makeText(context, "Export Successful! Report saved.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/msword"
                        putExtra(Intent.EXTRA_STREAM, result.fileUri)
                        putExtra(Intent.EXTRA_SUBJECT, "Marketing Environment Audit Report")
                        putExtra(Intent.EXTRA_TEXT, "Here is the completed Marketing Environment Audit Report for ${profile.organizationName}.")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Export to Word Document"))
                }
                is AuditViewModel.ExportResult.Failure -> {
                    Toast.makeText(context, "Export Failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (!isLoggedIn) {
        SignInScreen(viewModel = viewModel)
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(72.dp)
                        .border(width = 1.dp, color = Color(0xFF49454F)), // Sophisticated Dark top border
                    containerColor = Color(0xFF2B2930), // Sophisticated Dark nav bg
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == AuditTab.DASHBOARD,
                        onClick = { viewModel.setTab(AuditTab.DASHBOARD) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Audit", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE6E1E9),
                            selectedTextColor = Color(0xFFE6E1E9),
                            indicatorColor = Color(0xFF4A4458), // Active bottom tab indicator pill
                            unselectedIconColor = Color(0xFFCAC4D0),
                            unselectedTextColor = Color(0xFFCAC4D0)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == AuditTab.PROFILE,
                        onClick = { viewModel.setTab(AuditTab.PROFILE) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE6E1E9),
                            selectedTextColor = Color(0xFFE6E1E9),
                            indicatorColor = Color(0xFF4A4458),
                            unselectedIconColor = Color(0xFFCAC4D0),
                            unselectedTextColor = Color(0xFFCAC4D0)
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == AuditTab.CONSULTANT,
                        onClick = { viewModel.setTab(AuditTab.CONSULTANT) },
                        icon = { Icon(Icons.Default.AccountBox, contentDescription = "Consultant Info") },
                        label = { Text("Consultant", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE6E1E9),
                            selectedTextColor = Color(0xFFE6E1E9),
                            indicatorColor = Color(0xFF4A4458),
                            unselectedIconColor = Color(0xFFCAC4D0),
                            unselectedTextColor = Color(0xFFCAC4D0)
                        )
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1C1B1F)) // Sophisticated Dark body background
                    .padding(innerPadding)
            ) {
                // High-end Executive Header
                HeaderView(onSignOut = { viewModel.signOut() })

                // Main Content Area based on currentTab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (currentTab) {
                        AuditTab.DASHBOARD -> DashboardScreen(
                            profile = profile,
                            answers = answers,
                            viewModel = viewModel
                        )
                        AuditTab.PROFILE -> ProfileScreen(
                            profile = profile,
                            showValidationErrors = showValidationErrors,
                            viewModel = viewModel
                        )
                        AuditTab.CONSULTANT -> ConsultantScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SignInScreen(viewModel: AuditViewModel) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var showAboutModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initDeviceBinding(context)
    }

    if (showAboutModal) {
        AboutSupportDialog(onDismiss = { showAboutModal = false })
    }

    val deviceBindingInfo by viewModel.deviceBindingInfo.collectAsState()
    val currentDeviceModel by viewModel.currentDeviceModel.collectAsState()
    val currentDeviceFingerprintDisplay by viewModel.currentDeviceFingerprintDisplay.collectAsState()
    val loginResultState by viewModel.loginResultState.collectAsState()

    // Handle Anti-Circumvention Device Mismatch Alert Dialog
    if (loginResultState is LoginResult.DeviceMismatch) {
        val mismatch = loginResultState as LoginResult.DeviceMismatch
        AlertDialog(
            onDismissRequest = { viewModel.clearLoginResultState() },
            containerColor = Color(0xFF2B2930),
            titleContentColor = Color(0xFFF2B8B5),
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Anti-Circumvention Alert",
                    tint = Color(0xFFF2B8B5),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Anti-Circumvention Alert",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF2B8B5)
                    ),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Access blocked! Account sharing across multiple devices or unauthorized APK sharing is strictly prohibited under Device Session Binding protection.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFE6E1E9),
                            lineHeight = 18.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                        border = BorderStroke(1.dp, Color(0xFF601410)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "REGISTERED HARDWARE:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF2B8B5)
                                )
                            )
                            Text(
                                text = "${mismatch.boundDeviceModel} (${mismatch.boundFingerprint})",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE6E1E9))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ATTEMPTED HARDWARE:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD0BCFF)
                                )
                            )
                            Text(
                                text = "$currentDeviceModel (${mismatch.currentFingerprint})",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE6E1E9))
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearLoginResultState() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF), contentColor = Color(0xFF381E72))
                ) {
                    Text("Acknowledge", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.resetDeviceBinding(context)
                    }
                ) {
                    Text("Reset Binding (Test)", color = Color(0xFFD0BCFF), fontSize = 12.sp)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2B2930),
                        Color(0xFF1C1B1F)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Title Header - all in one font
            Text(
                text = "Marketing Audit Tool™",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF),
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Powered by Marketing Diagnostics℠",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD0BCFF).copy(alpha = 0.9f),
                    fontSize = 13.sp
                ),
                textAlign = TextAlign.Center
            )


            Spacer(modifier = Modifier.height(20.dp))

            // Visual Security Badge Indicator Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2838)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF3B485E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("security_badge_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Session Protection",
                            tint = Color(0xFF70EFDE),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Anti-Circumvention Session Protection Active",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF70EFDE)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val deviceVal = if (deviceBindingInfo.isBound) {
                        deviceBindingInfo.boundDeviceModel ?: currentDeviceModel
                    } else {
                        currentDeviceModel
                    }

                    val idVal = if (deviceBindingInfo.isBound) {
                        deviceBindingInfo.boundFormattedFingerprint ?: currentDeviceFingerprintDisplay
                    } else {
                        currentDeviceFingerprintDisplay
                    }

                    val statusVal = if (deviceBindingInfo.isBound) {
                        "Active & Bound to Account"
                    } else {
                        "Ready for Initial Binding on Sign In"
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Device: ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD0BCFF),
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = deviceVal,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFE6E1E9),
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ID: ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD0BCFF),
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = idVal,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFE6E1E9),
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Status: ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD0BCFF),
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = statusVal,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (deviceBindingInfo.isBound) Color(0xFF70EFDE) else Color(0xFFCAC4D0),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card-Based Interface with rounded corners and subtle gradient backdrop
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2B2930)
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Lock icon instead of saying "Secure access portal"
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Icon",
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    Text(
                        text = "User Access Sign In",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1E9),
                            fontSize = 20.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Subtitle description text
                    Text(
                        text = "Please enter your assigned username and password to access the app.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFCAC4D0),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            localError = null
                        },
                        label = { Text("Username") },
                        placeholder = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Icon",
                                tint = Color(0xFFD0BCFF)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F),
                            focusedLabelColor = Color(0xFFD0BCFF),
                            unfocusedLabelColor = Color(0xFFCAC4D0),
                            focusedTextColor = Color(0xFFE6E1E9),
                            unfocusedTextColor = Color(0xFFE6E1E9)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Input with Visibility Toggle
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localError = null
                        },
                        label = { Text("Password") },
                        placeholder = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Icon",
                                tint = Color(0xFFD0BCFF)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.testTag("visibility_toggle")
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide Password" else "Show Password",
                                    tint = Color(0xFFCAC4D0)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color(0xFF49454F),
                            focusedLabelColor = Color(0xFFD0BCFF),
                            unfocusedLabelColor = Color(0xFFCAC4D0),
                            focusedTextColor = Color(0xFFE6E1E9),
                            unfocusedTextColor = Color(0xFFE6E1E9)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    // Error Feedback
                    if (localError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = localError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Start Marketing Audit Survey Button
                    Button(
                        onClick = {
                            val loginResult = viewModel.loginWithDeviceBinding(context, username, password)
                            if (loginResult is LoginResult.InvalidCredentials) {
                                localError = "Invalid username or password. Please try again."
                            } else {
                                localError = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("sign_in_button")
                    ) {
                        Text(
                            text = "Start Marketing Audit Survey",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Watermark
            Text(
                text = "© 2026 Marketing RAAY Inc. All Rights Reserved",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFCAC4D0).copy(alpha = 0.7f),
                    fontSize = 12.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Login Screen Footer Quick-action
            OutlinedButton(
                onClick = { showAboutModal = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFD0BCFF)
                ),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("about_support_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About & Support",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "About & Contact Support",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AboutSupportDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2B2930),
        titleContentColor = Color(0xFFE6E1E9),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6750A4), Color(0xFFD0BCFF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Corporate Logo Badge",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Marketing RAAY Inc.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE6E1E9),
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "Powering Marketing Diagnostics℠",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFD0BCFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Marketing Audit Tool User Edition",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD0BCFF),
                                fontSize = 14.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "© 2026 Marketing RAAY Inc. All Rights Reserved",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCAC4D0),
                                fontSize = 11.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "This app combines a total 100 years of Academic Research and Industry Experience.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFE6E1E9),
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 16.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Direct Interactive Support Channels",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCAC4D0),
                        fontSize = 12.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 8.dp)
                )

                // Support Email Card
                Card(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:AnOutsourcedCMO@gmail.com"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_email_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Support Email",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Support Email",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFCAC4D0),
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = "AnOutsourcedCMO@gmail.com",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF70EFDE),
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Helpdesk Hotline Card
                Card(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919453441385"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Unable to open phone dialer", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_hotline_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Helpdesk Hotline",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Helpdesk Hotline",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFCAC4D0),
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = "+91 9453441385",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF70EFDE),
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Website & Documentation Card
                Card(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://marketingraay.com"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
                    border = BorderStroke(1.dp, Color(0xFF49454F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_website_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Website & Documentation",
                            tint = Color(0xFFD0BCFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Website & Documentation",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFCAC4D0),
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = "marketingraay.com",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF70EFDE),
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0BCFF),
                    contentColor = Color(0xFF381E72)
                )
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun HeaderView(onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color(0xFF1C1B1F)) // Solid Sophisticated Dark background
            .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Marketing Audit Tool™",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD0BCFF), // Sophisticated light purple
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Powered by Marketing Diagnostics℠",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD0BCFF).copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                )

            }
            IconButton(
                onClick = onSignOut,
                modifier = Modifier.testTag("sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = Color(0xFFD0BCFF)
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF49454F)) // Sophisticated Dark border bottom
        )
    }
}

@Composable
fun DashboardScreen(
    profile: ProfileEntity,
    answers: Map<String, String>,
    viewModel: AuditViewModel
) {
    val context = LocalContext.current
    val totalCount = auditSections.flatMap { it.questions }.size
    val currentFilled = auditSections.flatMap { it.questions }.count { answers[it.id]?.isNotBlank() == true }
    val completenessFraction = currentFilled.toFloat() / totalCount

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Text Block
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color(0xFF381E72) // bg-[#381E72] Sophisticated Dark banner
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Assess your Marketing Environment, Marketing Systems & Organization, your 4 P's, along with your Marketing Objectives & Productivity by filling out the answers to all the below questions",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFEADDFF), // text-[#EADDFF]
                            lineHeight = 22.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Completeness Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Audit Completeness ($currentFilled/$totalCount)",
                            color = Color(0xFFD0BCFF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${(completenessFraction * 100).toInt()}%",
                            color = Color(0xFFE6E1E9),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = completenessFraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color(0xFFD0BCFF),
                        trackColor = Color(0xFF49454F) // border-b-[#49454F]
                    )
                }
            }
        }

        // Demo Quick Fill Action & Export Actions Header row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.performExport(context) },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(54.dp)
                        .testTag("export_to_word_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF) // bg-[#D0BCFF]
                    ),
                    shape = RoundedCornerShape(27.dp) // rounded-full
                ) {
                    Icon(
                        Icons.Default.Share, 
                        contentDescription = null, 
                        tint = Color(0xFF381E72), // text-[#381E72]
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Export to Word", 
                        fontWeight = FontWeight.Bold, 
                        color = Color(0xFF381E72), // text-[#381E72]
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = { fillSampleData(viewModel) },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD0BCFF)
                    ),
                    shape = RoundedCornerShape(27.dp),
                    border = BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Icon(
                        Icons.Default.CheckCircle, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFD0BCFF)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto Demo", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFFE6E1E9))
                }
            }
        }

        // Checklist Sections Titles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUDIT SECTIONS",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD0BCFF),
                        letterSpacing = 1.sp
                    )
                )

                // Instruction tip
                Text(
                    text = "Tap to expand queries",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFCAC4D0).copy(alpha = 0.5f),
                        fontStyle = FontStyle.Italic
                    )
                )
            }
        }

        // Auditing sections accordion grouped by environment
        item {
            Text(
                text = "Marketing Environment",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF)
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }
        items(auditSections.filter { it.isMacro && !it.isStrategy }) { section ->
            AuditSectionCard(
                section = section,
                answers = answers,
                onAnswerChanged = { qId, text -> viewModel.updateAnswer(qId, text) }
            )
        }

        items(auditSections.filter { !it.isMacro && !it.isStrategy && !it.isOrganization && !it.isSystems && !it.isProductivity && !it.isFunction }) { section ->
            AuditSectionCard(
                section = section,
                answers = answers,
                onAnswerChanged = { qId, text -> viewModel.updateAnswer(qId, text) }
            )
        }

        item {
            Text(
                text = "Marketing Strategy",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF)
                ),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }
        items(auditSections.filter { it.isStrategy }) { section ->
            AuditSectionCard(
                section = section,
                answers = answers,
                onAnswerChanged = { qId, text -> viewModel.updateAnswer(qId, text) }
            )
        }

        item {
            Text(
                text = "Marketing Organization",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF)
                ),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }
        items(auditSections.filter { it.isOrganization }) { section ->
            AuditSectionCard(
                section = section,
                answers = answers,
                onAnswerChanged = { qId, text -> viewModel.updateAnswer(qId, text) }
            )
        }

        item {
            Text(
                text = "Marketing Systems",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF)
                ),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }
        items(auditSections.filter { it.isSystems }) { section ->
            AuditSectionCard(
                section = section,
                answers = answers,
                onAnswerChanged = { qId, text -> viewModel.updateAnswer(qId, text) }
            )
        }

        item {
            Text(
                text = "Marketing Productivity",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF)
                ),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }
        items(auditSections.filter { it.isProductivity }) { section ->
            AuditSectionCard(
                section = section,
                answers = answers,
                onAnswerChanged = { qId, text -> viewModel.updateAnswer(qId, text) }
            )
        }

        item {
            Text(
                text = "Marketing Function",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD0BCFF)
                ),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }
        items(auditSections.filter { it.isFunction }) { section ->
            AuditSectionCard(
                section = section,
                answers = answers,
                onAnswerChanged = { qId, text -> viewModel.updateAnswer(qId, text) }
            )
        }
    }
}

@Composable
fun AuditSectionCard(
    section: AuditSection,
    answers: Map<String, String>,
    onAnswerChanged: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sectionCompletedCount = section.questions.count { answers[it.id]?.isNotBlank() == true }
    val isFullyCompleted = sectionCompletedCount == section.questions.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                border = BorderStroke(
                    width = if (expanded) 1.5.dp else 1.dp,
                    color = if (expanded) Color(0xFFD0BCFF) else Color(0xFF49454F)
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2B2930) // bg-[#2B2930] Sophisticated Dark card bg
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE6E1E9) // Theme default light text
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$sectionCompletedCount of ${section.questions.size} completed",
                        fontSize = 12.sp,
                        color = if (isFullyCompleted) Color(0xFF34D399) else Color(0xFFCAC4D0).copy(alpha = 0.7f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFullyCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Section",
                        tint = Color(0xFFCAC4D0)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1B1F)) // nested content has deep page bg [#1C1B1F]
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    section.questions.forEach { question ->
                        val currentText = answers[question.id] ?: ""

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${question.questionNumber}. ${question.label}",
                                color = Color(0xFFD0BCFF), // Sub-labels styled in Sophisticated light lavender
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = currentText,
                                onValueChange = { onAnswerChanged(question.id, it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_${question.id}"),
                                placeholder = {
                                    Text(
                                        "Enter your assessment text here...",
                                        fontSize = 12.sp,
                                        color = Color(0xFFCAC4D0).copy(alpha = 0.5f)
                                    )
                                },
                                maxLines = 8,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD0BCFF),
                                    unfocusedBorderColor = Color(0xFF49454F),
                                    focusedTextColor = Color(0xFFE6E1E9),
                                    unfocusedTextColor = Color(0xFFE6E1E9),
                                    focusedContainerColor = Color(0xFF2B2930),
                                    unfocusedContainerColor = Color(0xFF2B2930)
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    profile: ProfileEntity,
    showValidationErrors: Boolean,
    viewModel: AuditViewModel
) {
    val context = LocalContext.current
    val phoneValid = viewModel.isPhoneValid(profile.representativePhone)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF381E72)), // Solid Sophisticated Dark banner background
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Your Organization Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please answer all the below questions. These answers will be included in the Marketing Audit report.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFEADDFF),
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        item {
            ProfileFieldInput(
                label = "Client Organization Name *",
                value = profile.organizationName,
                tag = "organization_name_input",
                onValueChange = { text -> viewModel.updateProfile { it.copy(organizationName = text) } },
                isError = showValidationErrors && profile.organizationName.isBlank(),
                errorText = "Client Organization Name is required"
            )
        }

        item {
            ProfileFieldInput(
                label = "Target Industry / Domain *",
                value = profile.targetIndustry,
                tag = "target_industry_input",
                onValueChange = { text -> viewModel.updateProfile { it.copy(targetIndustry = text) } },
                isError = showValidationErrors && profile.targetIndustry.isBlank(),
                errorText = "Target Industry is required"
            )
        }

        item {
            ProfileFieldInput(
                label = "Client Representative Name *",
                value = profile.representativeName,
                tag = "representative_name_input",
                onValueChange = { text -> viewModel.updateProfile { it.copy(representativeName = text) } },
                isError = showValidationErrors && profile.representativeName.isBlank(),
                errorText = "Client Representative Name is required"
            )
        }

        item {
            ProfileFieldInput(
                label = "Representative Designation *",
                value = profile.representativeDesignation,
                tag = "representative_designation_input",
                onValueChange = { text -> viewModel.updateProfile { it.copy(representativeDesignation = text) } },
                isError = showValidationErrors && profile.representativeDesignation.isBlank(),
                errorText = "Representative designation is required"
            )
        }

        item {
            ProfileFieldInput(
                label = "Representative Location *",
                value = profile.representativeLocation,
                tag = "representative_location_input",
                onValueChange = { text -> viewModel.updateProfile { it.copy(representativeLocation = text) } },
                isError = showValidationErrors && profile.representativeLocation.isBlank(),
                errorText = "Location of representative is required"
            )
        }

        item {
            // Phone with custom error country validation
            val isError = showValidationErrors && (!phoneValid || profile.representativePhone.isBlank())
            
            ProfileFieldInput(
                label = "Representative Phone Number (Starts with +) *",
                value = profile.representativePhone,
                tag = "representative_phone_input",
                onValueChange = { text -> viewModel.updateProfile { it.copy(representativePhone = text) } },
                isError = isError,
                errorText = if (profile.representativePhone.isBlank()) {
                    "Phone number is required"
                } else {
                    "Invalid Format. Guarantee starting with '+' followed by Country Code and valid digits (e.g., +1 555-019-2834)"
                }
            )
        }

        // Action controls
        item {
            OutlinedButton(
                onClick = {
                    viewModel.clearAllAnswers(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(top = 10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFF2B8B5)
                ),
                shape = RoundedCornerShape(27.dp),
                border = BorderStroke(1.25.dp, Color(0xFFF2B8B5))
            ) {
                Text(
                    "Clear All Data", 
                    fontWeight = FontWeight.Bold, 
                    color = Color(0xFFF2B8B5),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ProfileFieldInput(
    label: String,
    value: String,
    tag: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorText: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp, // text-[11px] in Sophisticated Dark spec
            fontWeight = FontWeight.Medium, // font-medium in Sophisticated Dark spec
            color = if (isError) Color(0xFFF2B8B5) else Color(0xFFD0BCFF) // Sophisticated error/label tones
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag),
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(8.dp), // lg in Tailwind is 8dp
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFFCAC4D0),
                unfocusedTextColor = Color(0xFFCAC4D0),
                focusedBorderColor = Color(0xFFD0BCFF),
                unfocusedBorderColor = Color(0xFF49454F),
                focusedContainerColor = Color(0xFF2B2930),
                unfocusedContainerColor = Color(0xFF2B2930),
                errorTextColor = Color(0xFFF2B8B5),
                errorContainerColor = Color(0xFF2B2930),
                errorBorderColor = Color(0xFFF2B8B5)
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
        )
        if (isError) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error icon",
                    tint = Color(0xFFF2B8B5),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorText,
                    color = Color(0xFFF2B8B5),
                    fontSize = 10.sp, // text-[10px]
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ConsultantScreen(
    viewModel: AuditViewModel
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Anuj Khanna",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6E1E9)
                )
            )
            Text(
                text = "Marketing Strategy Consultant",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD0BCFF)
                )
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About Me",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Using my three decades of Domestic and International Marketing & Sales experience, I provide Marketing Consulting services to Startups and SME's in the Software and IT industry. The various elements of a Marketing Audit serve as the input data towards understanding of the current marketing situation of the organization. These data points work as enablers toward building an effective Marketing Strategy.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFE6E1E9),
                            lineHeight = 22.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    Text(
                        text = "LinkedIn: https://www.linkedin.com/in/marketingraayfounder/",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFD0BCFF),
                            fontWeight = FontWeight.Bold,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        ),
                        modifier = Modifier
                            .clickable {
                                uriHandler.openUri("https://www.linkedin.com/in/marketingraayfounder/")
                            }
                            .padding(vertical = 4.dp)
                            .testTag("linkedin_link")
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About The Marketing Audit Platform",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Determine Problem areas and opportunities and Recommend a plan of action to improve the Marketing Performance of your Organization",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFE6E1E9),
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2B2930), RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, Color(0xFF49454F)), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "For questions regarding this Audit and Marketing Strategy Consulting for your Organization, please email",
                        color = Color(0xFFCAC4D0),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "AnOutsourcedCMO@gmail.com",
                        color = Color(0xFFD0BCFF),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:AnOutsourcedCMO@gmail.com")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF49454F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Methodology Credit",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD0BCFF)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All credit goes to Dr. Philip Kotler of Northwestern University for teaching us this Methodology",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFE6E1E9),
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }
    }
}

// Helper to draw clean custom outlined border brush
@Composable
fun borderBrush(color: Color = Color(0xFF334155)) = remember {
    androidx.compose.foundation.BorderStroke(1.dp, color)
}

// Function to automatically pre-populate realistic sample responses
fun fillSampleData(viewModel: AuditViewModel) {
    viewModel.updateProfile {
        it.copy(
            organizationName = "Cortex Global Solutions",
            targetIndustry = "SaaS Cybersecurity & Cloud Monitoring",
            representativeName = "Priya Khanna",
            representativeDesignation = "Chief Marketing Officer",
            representativeLocation = "New Delhi, Delhi",
            representativePhone = "+91 98765-43210"
        )
    }

    // Populate standard responses
    val samples = mapOf(
        "dem_1" to "The rise of remote workforce has accelerated the demand for SaaS enterprise security tools. However, baby-boomer retirements deplete direct legacy knowledge.",
        "dem_2" to "Our organization launched a remote-first zero-trust framework module tailored specifically to address secure access requirements for scattered populations.",
        "eco_1" to "Currency fluctuations, high interest rates, and tech-budget consolidations are reducing arbitrary enterprise software spending by 15-20% globally.",
        "eco_2" to "We have shifted our core pricing scheme to modular, results-oriented, consumption-based subscriptions instead of large flat licensing models.",
        "ecl_1" to "High energy footprint of global distributed server warehouses has driven client pressure to source exclusively carbon-neutral clouds.",
        "ecl_2" to "We signed a clean ecological hosting pact to transition of 100% of backups to green-certified AWS data nodes by end of Q4.",
        "tec_1" to "Generative AI code validation is altering process tech. Direct competitors are embedding LLM audits natively inside network code scans.",
        "tec_2" to "Automated AI firewalls could potentially bypass manual consulting evaluations, creating a replacement substitute in lightweight domains.",
        "pol_1" to "The newly proposed Data Sovereignty & Protection Acts in Asia and updated EU regulations enforce absolute local physical database custody rules.",
        "pol_2" to "We are actively monitoring national physical cluster laws and developing specialized regional hosting units to avert costly compliance issues.",
        "cul_1" to "Increasing awareness regarding data theft has led to heightened end-user skepticism concerning enterprise data usage practices.",
        "cul_2" to "The modern consumer values absolute end-user transparency. Organizations that hide cookie collection lose up to 40% user trust.",
        "mkt_1" to "Market size for cloud security is expanding at double-digit 12% CAGR, with geographical skew leaning heavily toward APAC enterprise clients.",
        "mkt_2" to "Our major segments are Mid-Market FinTech operations (40%), Healthcare portals (35%), and Government educational bodies (25%).",
        "cst_1" to "Customers rate us exceptional on data security, reliability, and salesforce assistance, but occasionally criticize our high-entry price tiers.",
        "cst_2" to "Healthcare portals base choosing decisions on absolute compliance (HIPAA), while FinTech decides entirely on live transaction processing speed.",
        "cmp_1" to "Major competitor is SecureGate (34% share) with aggressive pricing strategy, and NetShield (20% share) emphasizing extreme developer-friendliness.",
        "cmp_2" to "The integration of unified AI engines into standard browsers represents a crucial competition trend targeting standalone enterprise security.",
        "dst_1" to "The main trade channels are Direct B2B platform signups, cloud alliance partner catalogs (AWS Marketplace), and specialized enterprise resellers.",
        "dst_2" to "AWS Marketplace exhibits tremendous 45% annual velocity growth while classic direct cold email outreach channel efficiency is dropping.",
        "sup_1" to "Critical server clusters depend fully on cloud server providers. Sourcing of graphic chip elements is experiencing severe global supply gaps.",
        "sup_2" to "Cloud infrastructure providers are rapidly consolidating, resulting in high contractual pressure and reduced capacity to barter rates.",
        "fac_1" to "We operate fully in digital cloud delivery. Our transportation is instant virtual content delivery network systems, which have high availability.",
        "fac_2" to "Virtual regional data cloud nodes are fully accessible, with costs decreasing marginally due to competitive multi-region storage bidding.",
        "fac_3" to "Financial seed backing and access to Venture Debt remain moderate, with strategic partnership investments acting as prime expansion funding.",
        "fac_4" to "Our digital research agencies are highly effective, but our main ad agency lacks deep technical security insight, restricting general content conversions.",
        "pub_1" to "Open-source developer groups act as crucial publics that could turn into major adversaries if privacy vulnerabilities are mismanaged.",
        "pub_2" to "We initiated a public safety bug-bounty initiative with rewards to actively collaborate with white-hat dev crowds.",
        "str_mis_1" to "Yes, our business mission is clearly stated to provide robust, developer-centric SaaS cloud security and monitoring. It is highly feasible and aligned with the industry's transition to cloud infrastructure.",
        "str_obj_1" to "Yes, corporate goals are set around achieving 30% YoY growth and maintaining 99.99% system uptime, which directly guides our engineering and marketing milestones.",
        "str_obj_2" to "Our marketing objectives are appropriate given our competitive positioning, focusing on high-growth FinTech and Healthcare sectors where our high reliability is valued.",
        "str_str_1" to "Management has articulated a clear 'land and expand' strategy, focusing on high-touch initial security audits that lead to broad platform adoption. This is highly appropriate for our current stage.",
        "str_str_2" to "We segment primarily by industry compliance requirements (FinTech vs Healthcare vs Education). This is the best basis as regulatory pressure is the primary driver for cloud security spend.",
        "str_str_3" to "Yes, we position as the most compliant, high-performance security tool. Marketing resources are optimized primarily toward technical product content (40%) and direct enterprise sales (35%).",
        "str_str_4" to "Our current marketing budget is adequate but lean; we allocate 12% of projected revenue to marketing, which is standard for mid-stage SaaS but requires high efficiency.",
        "org_str_1" to "The CMO has full authority over core SaaS product messaging and standard sales support content, but lacks direct influence on post-sale user support queues, causing a gap in long-term customer experience optimization.",
        "org_str_2" to "Marketing is divided into specialized inbound content, outbound sales-enablement, and target regional growth units. This functional organization is highly suited for SaaS operations, though geographical coordination is sometimes sluggish.",
        "org_eff_1" to "Working relationships with direct sales are positive, facilitated by weekly sync meetings and shared pipeline targets, though the handover process of marketing-qualified leads to sales needs automated refinement.",
        "org_eff_2" to "The product management setup is functional. Product managers are currently tasked both with roadmap planning and basic revenue goals, enabling a healthy balance of engineering priorities and profit delivery.",
        "org_eff_3" to "Yes, the product-led growth team needs deep analytical training on advanced attribution systems, and our design agency partners require direct instruction on security compliance criteria.",
        "org_int_1" to "Some coordination friction exists with our engineering team regarding documentation speed and feature release timelines, which slightly delays our public go-to-market communication.",
        "sys_inf_1" to "Our marketing intelligence system effectively captures customer sentiment through NPS, but lacks automated tracking of distributor/dealer feedback channels.",
        "sys_inf_2" to "Yes, decision makers request bi-monthly market research on cloud-compliance and use the insights to tailor our regional ad copy.",
        "sys_inf_3" to "We use a combination of linear regression and historic cohort analysis for sales forecasting, though we are evaluating machine learning predictive models.",
        "sys_pla_1" to "The marketing planning system is well-conceived, following a structured quarterly review cycle, though it can be slow to adapt to sudden competitor price changes.",
        "sys_pla_2" to "Sales forecasting is soundly carried out using bottom-up pipeline estimations, but long-term market potential measurement requires more external advisory inputs.",
        "sys_pla_3" to "Yes, sales quotas are set based on a combination of historic territory performance and projected regional compliance pressures.",
        "sys_con_1" to "Control procedures are adequate, featuring weekly pipeline health checks and monthly budget reconciliations against target customer acquisition costs.",
        "sys_con_2" to "Management analyzes profitability quarterly across healthcare and fintech segments, but granular territory-level reporting is still developing.",
        "sys_con_3" to "Yes, marketing expenditures are examined monthly to optimize ad spend across high-performing search and social channels.",
        "sys_dev_1" to "The company has an open product feedback loop to gather ideas from sales and clients, though the screening process lacks a formal scoring system.",
        "sys_dev_2" to "Yes, we perform extensive concept interviews and total addressable market analysis before investing resources in developing major new modules.",
        "sys_dev_3" to "We carry out alpha/beta testing with trusted clients before public launches, though broader pre-release market testing is occasionally expedited to hit deadlines.",
        "prod_prf_1" to "FinTech and Healthcare segments yield the highest profitability (gross margin ~82%), while the Education sector is a lower-margin segment (gross margin ~55%) due to heavy customization requirements.",
        "prod_prf_2" to "We should aggressively expand our mid-market FinTech offerings to maximize short-term profit. Conversely, we must contract or standardize our Education segment engagements as custom feature requests significantly dilute development resources.",
        "prod_cst_1" to "Paid search campaigns for high-intent keywords currently have a high acquisition cost. We are shifting focus towards organic content marketing and technical SEO to bring down customer acquisition costs by 15%.",
        "fun_prd_1" to "Our product-line objective is to provide comprehensive API security. This is highly sound and is currently meeting our core metrics with a 94% retention rate.",
        "fun_prd_2" to "We should stretch the product line upward into high-enterprise dedicated security hardware integrations to capture higher-end financial sector clients.",
        "fun_prd_3" to "We are phasing out legacy standalone server modules and adding an automated AI-driven compliance risk-assessment dashboard.",
        "fun_prd_4" to "Buyers appreciate our deep compliance reporting features but feel the UI styling could be modernized. We need to focus on streamlining our onboarding flow.",
        "fun_prc_1" to "Prices are set based on value and competitive criteria. Enterprise tiers are structured around the volume of monitored API endpoints.",
        "fun_prc_2" to "Yes, clients generally perceive our prices as highly fair given our high reliability and the legal compliance risks mitigated by our platform.",
        "fun_prc_3" to "Demand is relatively inelastic due to high switching costs in enterprise security. Competitors offer lower pricing but lack our security certifications.",
        "fun_prc_4" to "Our pricing policies are highly compatible with distributors and comply fully with SOC2 and GDPR auditing standards.",
        "fun_dst_1" to "We distribute primarily via direct cloud marketplace integrations (AWS/GCP) and a dedicated enterprise sales team.",
        "fun_dst_2" to "Market coverage is adequate in North America and Europe, but we need deeper distribution coverage in the Asia-Pacific fintech hubs.",
        "fun_dst_3" to "AWS and GCP Marketplace distribution channel partners are highly effective, contributing to 40% of our new customer acquisition pipeline.",
        "fun_dst_4" to "We should consider expanding into indirect consulting partner channels, leveraging security integration firms to resell our licenses.",
        "fun_adv_1" to "Our advertising objectives are to establish thought leadership in cloud security. They are sound and measured by inbound organic demo requests.",
        "fun_adv_2" to "We spend approximately $45k monthly on advertising. The budget is determined as a percentage of our forecasted next-quarter sales pipeline.",
        "fun_adv_3" to "The ad themes focused on 'Zero-Trust SaaS Compliance' are highly effective, driving a 3.4% click-through rate on LinkedIn.",
        "fun_adv_4" to "LinkedIn and developer-centric newsletter sponsorships are extremely well-chosen, yielding high-quality enterprise leads.",
        "fun_adv_5" to "Our internal advertising team consists of two specialized growth marketers, which is adequate for our current stage but needs expansion next year.",
        "fun_adv_6" to "The promotion budget is adequate, focusing on hosting technical security webinars and providing free trial sandbox instances to developers.",
        "fun_adv_7" to "The publicity budget is lean but adequate, managed by an external PR agency that has successfully landed features in TechCrunch and VentureBeat.",
        "fun_slf_1" to "Salesforce objectives are to close 15 mid-market and 3 enterprise contracts per representative annually, keeping customer acquisition cost below $12k.",
        "fun_slf_2" to "Our team of 8 account executives is currently sufficient, though we will need to double the head count to meet next year's expansion goals.",
        "fun_slf_3" to "The salesforce is organized by territory (North America, EMEA, APAC). This is effective, and we have a flat hierarchy of one sales director guiding the team.",
        "fun_slf_4" to "The base-plus-commission (60/40 split) structure is highly competitive and provides strong incentives for account executives to exceed quotas.",
        "fun_slf_5" to "The salesforce shows very high morale, driven by strong product market fit and regular sales incentive trips.",
        "fun_slf_6" to "Quotas are set based on historical territory performance and adjusted for regional market potential. Performance is evaluated weekly.",
        "fun_slf_7" to "Our salesforce is highly technical compared to competitors, allowing us to build deeper trust during the initial security discovery calls."
    )

    samples.forEach { (qId, text) ->
        viewModel.updateAnswer(qId, text)
    }
}
