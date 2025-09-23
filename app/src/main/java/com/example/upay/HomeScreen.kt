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
                // Pass dark mode state and updater to SettingsScreen
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
    var isFakeNewsResult by remember { mutableStateOf<Boolean?>(null) } // null: not checked, true: fake, false: not fake
    var resultText by remember { mutableStateOf("") }
    var resultColor by remember { mutableStateOf(Color.Black) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Verify Message",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = {
                inputText = it
                isFakeNewsResult = null // Reset result when text changes
                resultText = ""
            },
            label = { Text("Paste message here") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (inputText.isBlank()) {
                    resultText = "Please enter a message to verify."
                    resultColor = Color.DarkGray
                    isFakeNewsResult = null
                    return@Button
                }
                // TODO: Replace with actual Gemini API call
                Log.d("SearchScreen", "Verifying message: $inputText")
                // Simulate API response based on keywords
                val isLikelyFake = inputText.contains("lottery", ignoreCase = true) ||
                                 inputText.contains("urgent prize", ignoreCase = true) ||
                                 inputText.contains("free money", ignoreCase = true)

                isFakeNewsResult = isLikelyFake

                if (isLikelyFake) {
                    resultText = "This message is likely FAKE."
                    resultColor = Color.Red
                } else {
                    resultText = "This message seems legitimate."
                    resultColor = Color.Green
                }
                Log.d("SearchScreen", "Verification result: $resultText")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify with Gemini")
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
    if (smsMessages.isNotEmpty()) {
        Log.d("MailScreen", "First message sender: ${smsMessages.first().sender}, body: '${smsMessages.first().body}'")
        smsMessages.forEachIndexed { index, sms ->
            Log.d("MailScreen", "Message at index $index: '${sms.body}' from ${sms.sender}")
        }
    } else {
        Log.d("MailScreen", "smsMessages list is empty.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Mail Screen - SMS Messages", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        if (smsMessages.isEmpty()) {
            Log.d("MailScreen", "Displaying 'No messages yet.' because smsMessages is empty.")
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Text("No messages yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Log.d("MailScreen", "Displaying LazyColumn for ${smsMessages.size} messages.")
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(smsMessages) { sms ->
                    Log.d("MailScreen_LazyColumn", "Rendering item for sender: ${sms.sender}, body: '${sms.body}'")
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
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "From: ${smsMessage.sender}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = smsMessage.body,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Received: ${java.text.SimpleDateFormat("dd/MM/yy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(smsMessage.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}


@Composable
fun SettingsScreen(
    navController: NavHostController,
    darkModeEnabled: Boolean, // Added parameter
    onDarkModeChange: (Boolean) -> Unit // Added parameter
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    // No longer need local darkModeEnabled state, use the passed one

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
        }

        // Account Section
        item {
            SettingsGroupTitle("Account")
            SettingsItem(icon = Icons.Default.AccountCircle, title = "Profile", onClick = { /* TODO: Navigate to Profile Screen */ })
            SettingsItem(icon = Icons.Default.Shield, title = "Security", onClick = { /* TODO: Navigate to Security Screen */ })
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // General Settings Section
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
                checked = darkModeEnabled, // Use passed state
                onCheckedChange = onDarkModeChange // Use passed updater
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // Logout
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    Log.d("SettingsScreen", "Logout clicked")
                    // TODO: Implement logout logic (clear session, navigate to login)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Logout", color = Color.White)
            }
        }
    }
}

@Composable
fun SettingsGroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = {
            Icon(icon, contentDescription = title)
        },
        trailingContent = {
            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Navigate")
        },
        modifier = Modifier.clickable(onClick = onClick)
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
        headlineContent = { Text(title) },
        leadingContent = {
            Icon(icon, contentDescription = title)
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) } // Allow clicking the whole row
    )
}


val LightBlue = Color(0xFFF0F5FF)
val TextGray = Color(0xFF7D8DAA)

data class Transaction(
    val id: Int,
    val description: String,
    val amount: String,
    val date: String,
    val type: String
)

@Composable
fun ActualHomeScreenContent() {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F9))
                .padding(horizontal = 24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { HeaderSection() }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { BalanceCard() }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { ActionButtons() }
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item { RecentTransactions() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        transactions.forEach {
            TransactionItem(transaction = it)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = transaction.description,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                fontSize = 16.sp
            )
            Text(
                text = transaction.date,
                color = TextGray,
                fontSize = 14.sp
            )
        }
        Text(
            text = transaction.amount,
            fontWeight = FontWeight.Bold,
            color = if (transaction.type == "Income") Color(0xFF22C55E) else Color.Black,
            fontSize = 16.sp
        )
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
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Thursday, 12 June 2025",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    var selectedItem by remember { mutableIntStateOf(0) }

    NavigationBar(
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        bottomNavItems.forEachIndexed { index, screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = TextGray,
                    indicatorColor = LightBlue
                )
            )
        }
    }
}

@Composable
fun BalanceCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Card Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Available Balance",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "₹2,84,750",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
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
        onClick = {  },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.8f) // Fixed: Added closing parenthesis
        )
    ) { 
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ActionButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ActionButton(icon = Icons.Default.AccountBox, text = "Accounts") // Example, replace with actual icons/text
        ActionButton(icon = Icons.Default.Face, text = "Pay Bill")
        ActionButton(icon = Icons.Default.FavoriteBorder, text = "Recharge")
        ActionButton(icon = Icons.Default.Settings, text = "More")
    }
}

@Composable
fun ActionButton(icon: ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(LightBlue, CircleShape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, fontSize = 14.sp, color = TextGray)
    }
}
