package com.example.upay

import android.annotation.SuppressLint
import android.app.Application
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.upay.ui.theme.UPAYTheme
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllMessagesScreen(viewModel: MailViewModel = viewModel(factory = MailViewModelFactory(LocalContext.current.applicationContext as Application))) {
    val messages by viewModel.smsMessages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Messages", color = MaterialTheme.colorScheme.onPrimaryContainer) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    text = "You currently have no messages or permission to read SMS might be denied.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                items(messages.reversed()) { message -> // Reversed to show newest first if not already sorted
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "From: ${message.sender}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

// Helper ViewModel for Previews, now takes Application
class PreviewMailViewModel(application: Application, initialMessages: List<SmsMessageData>) : MailViewModel(application) {
    init {
        // The SMS fetching in the main MailViewModel's init block will run.
        // We override the _smsMessages directly after to ensure previews show controlled data.
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
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val previewMessages = listOf(
        SmsMessageData("Sender A", "Hello! This is a test message.", System.currentTimeMillis() - 200000),
        SmsMessageData("Bank XYZ", "Your OTP is 123456. Please do not share this with anyone.", System.currentTimeMillis() - 100000),
        SmsMessageData("Friend B", "Are you free later? This is a slightly longer message.", System.currentTimeMillis()),
    )
    UPAYTheme {
        AllMessagesScreen(viewModel = PreviewMailViewModel(application, previewMessages))
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "All Messages Screen Empty Preview")
@Composable
fun AllMessagesScreenEmptyPreview() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    UPAYTheme {
        AllMessagesScreen(viewModel = PreviewMailViewModel(application, emptyList()))
    }
}
