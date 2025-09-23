package com.example.upay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
//  You'll need a ViewModel and data class for messages later
// import kotlinx.coroutines.flow.StateFlow
// import kotlinx.coroutines.flow.flowOf

// Placeholder data class for an SMS message
data class SmsMessage(
    val id: Long,
    val sender: String,
    val body: String,
    val timestamp: Long
)

// Placeholder ViewModel - In a real app, this would interact with a repository/database
// class AllMessagesViewModel {
//     // Placeholder for messages - replace with actual data source
//     val messages: StateFlow<List<SmsMessage>> = flowOf(
//         listOf(
//             SmsMessage(1, "Sender A", "Hello! This is a test message.", System.currentTimeMillis()),
//             SmsMessage(2, "Bank XYZ", "Your OTP is 123456.", System.currentTimeMillis() - 100000),
//             SmsMessage(3, "Friend B", "Are you free later?", System.currentTimeMillis() - 200000)
//         )
//     )
// }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllMessagesScreen(/*viewModel: AllMessagesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()*/) {
    // val messages by viewModel.messages.collectAsState()
    // Using placeholder data directly for now, until ViewModel and data persistence are set up
    val placeholderMessages = listOf(
        SmsMessage(1, "Sender A", "Hello! This is a test message.", System.currentTimeMillis()),
        SmsMessage(2, "Bank XYZ", "Your OTP is 123456.", System.currentTimeMillis() - 100000),
        SmsMessage(3, "Friend B", "Are you free later? This is a slightly longer message to see how it wraps and displays within the card element that we are using for each message item.", System.currentTimeMillis() - 200000)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Messages") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (placeholderMessages.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No messages yet.",
                    style = MaterialTheme.typography.bodyLarge
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
                items(placeholderMessages) { message ->
                    MessageItem(message = message)
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: SmsMessage) {
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
                text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, name = "All Messages Screen Preview")
@Composable
fun AllMessagesScreenPreview() {
    UPAYTheme {
        AllMessagesScreen()
    }
}

@Preview(showBackground = true, name = "All Messages Screen Empty Preview")
@Composable
fun AllMessagesScreenEmptyPreview() {
    UPAYTheme {
        // To preview the empty state, we'd ideally pass an empty list
        // For now, the placeholder data is hardcoded, so this preview will show the same
        // as the one above until ViewModel and real data flow is implemented.
        AllMessagesScreen()
    }
}
