package com.example.upay

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.Color // Removed direct color imports if not used elsewhere
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upay.ui.theme.UPAYTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow

// Removed local color definitions: SurfaceLight, TextPrimary, TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllMessagesScreen(viewModel: MailViewModel = viewModel()) {
    val messages by viewModel.smsMessages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Messages", color = MaterialTheme.colorScheme.onPrimaryContainer) }, // Title color from theme
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use theme background color
    ) { innerPadding ->
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No messages yet.",
                    style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "You currently have no messages.", // Updated empty text for clarity
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant) // Use onSurfaceVariant for secondary text
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages.reversed()) { message ->
                    MessageItem(message = message)
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: SmsMessageData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Slightly reduced elevation for a flatter design if preferred
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // Use theme surface color for card
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "From: ${message.sender}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface, // Text color on card's surface
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // Secondary text color on card's surface
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), // Lighter secondary text
                fontSize = 11.sp
            )
        }
    }
}

// Helper ViewModel for Previews (remains unchanged)
class PreviewMailViewModel(initialMessages: List<SmsMessageData>) : MailViewModel() {
    init {
        val privateSmsMessagesField = MailViewModel::class.java.getDeclaredField("_smsMessages")
        privateSmsMessagesField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val mutableStateFlow = privateSmsMessagesField.get(this) as MutableStateFlow<List<SmsMessageData>>
        mutableStateFlow.value = initialMessages
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "All Messages Screen Preview")
@Composable
fun AllMessagesScreenPreview() {
    val previewMessages = listOf(
        SmsMessageData("Sender A", "Hello! This is a test message.", System.currentTimeMillis() - 200000),
        SmsMessageData("Bank XYZ", "Your OTP is 123456. Please do not share this with anyone. This is an important security alert.", System.currentTimeMillis() - 100000),
        SmsMessageData("Friend B", "Are you free later? This is a slightly longer message to see how it wraps and displays within the card element. We could go for a coffee or something.", System.currentTimeMillis()),
        SmsMessageData("Service XYZ", "Your appointment is confirmed for tomorrow at 10 AM.", System.currentTimeMillis() - 300000),
        SmsMessageData("Mom", "Can you pick up groceries on your way home? Milk, eggs, bread, and cheese.", System.currentTimeMillis() - 400000)
    )
    UPAYTheme {
        AllMessagesScreen(viewModel = PreviewMailViewModel(previewMessages))
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "All Messages Screen Empty Preview")
@Composable
fun AllMessagesScreenEmptyPreview() {
    UPAYTheme {
        AllMessagesScreen(viewModel = PreviewMailViewModel(emptyList()))
    }
}
