package com.example.upay

import android.annotation.SuppressLint
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.upay.ui.theme.UPAYTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow

// SmsMessageData from MailViewModel.kt is used

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllMessagesScreen(viewModel: MailViewModel = viewModel()) {
    val messages by viewModel.smsMessages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Messages") }, // Title reverted
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = Modifier.fillMaxSize()
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
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Send an SMS to this device or check your ViewModel data.", // Updated empty text
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages.reversed()) { message -> // Display latest messages first
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "From: ${message.sender}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper ViewModel for Previews
class PreviewMailViewModel(initialMessages: List<SmsMessageData>) : MailViewModel() {
    init {
        val privateSmsMessagesField = MailViewModel::class.java.getDeclaredField("_smsMessages")
        privateSmsMessagesField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val mutableStateFlow = privateSmsMessagesField.get(this) as MutableStateFlow<List<SmsMessageData>>
        mutableStateFlow.value = initialMessages
    }
}

@SuppressLint("ViewModelConstructorInComposable") // Added annotation here
@Preview(showBackground = true, name = "All Messages Screen Preview")
@Composable
fun AllMessagesScreenPreview() {
    val previewMessages = listOf(
        SmsMessageData("Sender A", "Hello! This is a test message.", System.currentTimeMillis() - 200000),
        SmsMessageData("Bank XYZ", "Your OTP is 123456.", System.currentTimeMillis() - 100000),
        SmsMessageData("Friend B", "Are you free later? This is a slightly longer message to see how it wraps and displays within the card element.", System.currentTimeMillis()),
        SmsMessageData("Service XYZ", "Your appointment is confirmed for tomorrow at 10 AM.", System.currentTimeMillis() - 300000),
        SmsMessageData("Mom", "Can you pick up groceries on your way home?", System.currentTimeMillis() - 400000),
        SmsMessageData("Boss", "Meeting rescheduled to 2 PM.", System.currentTimeMillis() - 500000),
        SmsMessageData("Online Store", "Your package has been shipped!", System.currentTimeMillis() - 600000),
        SmsMessageData("Newsletter", "Check out our latest offers.", System.currentTimeMillis() - 700000),
        SmsMessageData("Unknown", "This is a spam message. Please ignore.", System.currentTimeMillis() - 800000),
        SmsMessageData("Another Friend", "Long time no see! How are you doing?", System.currentTimeMillis() - 900000)
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
