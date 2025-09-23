package com.example.upay

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


// Enhanced Color Palette
val PrimaryVariant = Color(0xFF004BA0) // A deeper blue
val SurfaceLight = Color(0xFFF8F9FA) // A very light gray for backgrounds
val TextPrimary = Color(0xFF212121) // Dark gray for primary text
val TextSecondary = Color(0xFF757575) // Medium gray for secondary text
val AccentColor = Color(0xFFFFD700) // A gold-like accent
val IncomeGreen = Color(0xFF4CAF50)
val ExpenseDark = TextPrimary

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Mail : Screen("mail", "Mail", Icons.Default.Email)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Mail,
    Screen.Settings
)

@Composable
fun MainAppScreen(darkModeEnabled: Boolean, onDarkModeChange: (Boolean) -> Unit) { // Added parameters
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues = innerPadding)
        ) {
            composable(Screen.Home.route) {
                ActualHomeScreenContent()
            }
            composable(Screen.Search.route) {
                SearchScreen()
            }
            composable(Screen.Mail.route) {
                MailScreen(mailViewModel = viewModel())
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    navController = navController,
                    darkModeEnabled = darkModeEnabled,
                    onDarkModeChange = onDarkModeChange
                )
            }
        }
    }
}


@Composable
fun SearchScreen() {
    var inputText by remember { mutableStateOf("") }
    var isFakeNewsResult by remember { mutableStateOf<Boolean?>(null) }
    var resultText by remember { mutableStateOf("") }
    var resultColor by remember { mutableStateOf(Color.Black) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Verify Message",
            style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = {
                inputText = it
                isFakeNewsResult = null 
                resultText = ""
            },
            label = { Text("Paste message here", color = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 5,
            textStyle = TextStyle(color = TextPrimary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (inputText.isBlank()) {
                    resultText = "Please enter a message to verify."
                    resultColor = TextSecondary
                    isFakeNewsResult = null
                    return@Button
                }
                Log.d("SearchScreen", "Verifying message: $inputText")
                val isLikelyFake = inputText.contains("lottery", ignoreCase = true) ||
                                 inputText.contains("urgent prize", ignoreCase = true) ||
                                 inputText.contains("free money", ignoreCase = true)

                isFakeNewsResult = isLikelyFake

                if (isLikelyFake) {
                    resultText = "This message is likely FAKE."
                    resultColor = Color.Red
                } else {
                    resultText = "This message seems legitimate."
                    resultColor = IncomeGreen
                }
                Log.d("SearchScreen", "Verification result: $resultText")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Verify with Gemini", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (resultText.isNotEmpty()) {
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = resultColor
            )
        }
    }
}

@Composable
fun MailScreen(mailViewModel: MailViewModel = viewModel()) {
    val smsMessages by mailViewModel.smsMessages.collectAsState()
    Log.d("MailScreen", "Recomposing MailScreen. ViewModel: $mailViewModel, SMS Count: ${smsMessages.size}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .padding(16.dp)
    ) {
        Text("SMS Messages", style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary), modifier = Modifier.padding(bottom = 16.dp))
        if (smsMessages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Text("No messages yet.", style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(smsMessages) { sms ->
                    SmsListItem(smsMessage = sms)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SmsListItem(smsMessage: SmsMessageData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), // Slightly more rounded
        elevation = CardDefaults.cardElevation(4.dp), // A bit more shadow
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "From: ${smsMessage.sender}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = smsMessage.body,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp)) // Increased spacing
            Text(
                text = "Received: ${java.text.SimpleDateFormat("dd/MM/yy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(smsMessage.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = 0.7f) // Lighter for less emphasis
            )
        }
    }
}


@Composable
fun SettingsScreen(
    navController: NavHostController,
    darkModeEnabled: Boolean, 
    onDarkModeChange: (Boolean) -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .padding(16.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary), modifier = Modifier.padding(bottom = 24.dp))
        }

        item {
            SettingsGroupTitle("Account")
            SettingsItem(icon = Icons.Default.AccountCircle, title = "Profile", onClick = { /* TODO */ })
            SettingsItem(icon = Icons.Default.Shield, title = "Security", onClick = { /* TODO */ })
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = TextSecondary.copy(alpha = 0.2f))
        }

        item {
            SettingsGroupTitle("General")
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Enable Notifications",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
            SettingsToggleItem(
                icon = Icons.Default.Brightness6,
                title = "Dark Mode",
                checked = darkModeEnabled, 
                onCheckedChange = onDarkModeChange 
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = TextSecondary.copy(alpha = 0.2f))
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    Log.d("SettingsScreen", "Logout clicked")
                    // TODO: Implement logout logic
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsGroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium, // Made it slightly larger
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, color = TextPrimary) },
        leadingContent = {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = {
            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Navigate", tint = TextSecondary.copy(alpha = 0.7f))
        },
        modifier = Modifier.clickable(onClick = onClick)
            .padding(vertical = 4.dp) // Add some vertical padding to list items
    )
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, color = TextPrimary) },
        leadingContent = {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) }.padding(vertical = 4.dp)
    )
}

data class Transaction(
    val id: Int,
    val description: String,
    val amount: String,
    val date: String,
    val type: String // "Income" or "Expense"
)

@Composable
fun ActualHomeScreenContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight) // Use the new SurfaceLight color
            .padding(horizontal = 20.dp) // Adjusted padding
    ) {
        item { Spacer(modifier = Modifier.height(24.dp)) } // Reduced top space slightly
        item { HeaderSection() }
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { BalanceCard() }
        item { Spacer(modifier = Modifier.height(28.dp)) } // Increased space
        item { ActionButtons() }
        item { Spacer(modifier = Modifier.height(28.dp)) } // Increased space
        item { RecentTransactions() }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun RecentTransactions() {
    val transactions = listOf(
        Transaction(1, "Spotify Subscription", "-₹799", "10 June", "Expense"),
        Transaction(2, "Freelance Payment", "+₹40,000", "08 June", "Income"),
        Transaction(3, "Grocery Shopping", "-₹6,000", "07 June", "Expense"),
        Transaction(4, "Transfer from Sudharma", "+₹8,000", "05 June", "Income")
    )

    Column {
        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), // Made it larger
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        transactions.forEach { transaction ->
            TransactionItem(transaction = transaction)
            Spacer(modifier = Modifier.height(12.dp)) // Space between transaction items
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = transaction.amount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == "Income") IncomeGreen else ExpenseDark,
            )
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hi Dhanush!",
                style = MaterialTheme.typography.headlineMedium, // Adjusted style
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Thursday, 12 June 2025",
                style = MaterialTheme.typography.bodyMedium, // Adjusted style
                color = TextSecondary
            )
        }
        // Potentially add an avatar/icon here if desired
        // Icon(Icons.Default.AccountCircle, contentDescription = "Profile", modifier = Modifier.size(40.dp))
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    var selectedItem by remember { mutableIntStateOf(0) }

    NavigationBar(
        containerColor = Color.White, // Kept white for clean look
        contentColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 8.dp // Add some elevation
    ) {
        bottomNavItems.forEachIndexed { index, screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) }, // Added label
                selected = selectedItem == index,
                onClick = {
                    if (selectedItem != index) { // Avoid re-navigating to the same screen
                        selectedItem = index
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary, // Color for selected text
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary, // Color for unselected text
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) // Softer indicator
                )
            )
        }
    }
}

@Composable
fun BalanceCard() {
    Card(
        shape = RoundedCornerShape(20.dp), // Slightly adjusted shape
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // More prominent shadow
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) // Slightly taller
                .background( // Gradient background
                    brush = Brush.linearGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, PrimaryVariant)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Available Balance",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.labelLarge // Using theme typography
                    )
                    Text(
                        text = "₹2,84,750",
                        color = Color.White,
                        fontSize = 38.sp, // Slightly larger
                        fontWeight = FontWeight.ExtraBold, // Bolder
                        style = MaterialTheme.typography.displaySmall // Using theme typography
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Adjusted spacing
                ) {
                    BalanceActionButton(
                        icon = Icons.Default.KeyboardArrowUp,
                        text = "Send",
                        modifier = Modifier.weight(1f)
                    )
                    BalanceActionButton(
                        icon = Icons.Default.KeyboardArrowDown,
                        text = "Request",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceActionButton(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Button(
        onClick = { /* TODO: Action */ },
        modifier = modifier.height(48.dp), // Ensure a good touch target size
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.25f), // More subtle background
            contentColor = Color.White // Ensure content (icon and text) is white
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = text, tint = Color.White) // Explicitly tint icon
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White, // Explicitly color text
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ActionButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround // Keeps them spaced out
    ) {
        // Updated icons to be more distinct if possible, or use your existing ones.
        // These are examples.
        ActionButton(icon = Icons.Default.AccountBox, text = "Accounts")
        ActionButton(icon = Icons.Filled.Face, text = "Pay Bill") // Changed to Filled for consistency if others are
        ActionButton(icon = Icons.Default.FavoriteBorder, text = "Recharge")
        ActionButton(icon = Icons.Default.Settings, text = "More") // Settings icon is fine for "More"
    }
}

@Composable
fun ActionButton(icon: ImageVector, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = { /* TODO: Action for $text */ }) // Make the whole item clickable
    ) {
        Box(
            modifier = Modifier
                .size(68.dp) // Slightly larger
                .clip(CircleShape) // Ensure circle shape
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)) // Softer background
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary, // Use primary color for icon tint
                modifier = Modifier.size(28.dp) // Slightly larger icon
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            fontSize = 13.sp, // Slightly smaller text for balance
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
