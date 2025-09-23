package com.example.upay

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class SmsMessageData(
    val sender: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fake: Boolean = false
)

open class MailViewModel : ViewModel() { 

    // Initialize with dummy messages
    private val initialDummyMessages = listOf(
        SmsMessageData("DummySender 1", "This is the first dummy message.", System.currentTimeMillis() - 500000, fake = true),
        SmsMessageData("DummySender 2", "Another dummy message here for testing purposes.", System.currentTimeMillis() - 400000, fake = true),
        SmsMessageData("Bank Alert", "Your dummy OTP is 654321. Do not share.", System.currentTimeMillis() - 300000, fake = false),
        SmsMessageData("Friend", "Hey, how are you doing? This is a dummy chat message.", System.currentTimeMillis() - 200000, fake = true),
        SmsMessageData("Service Update", "Your dummy subscription has been renewed.", System.currentTimeMillis() - 100000, fake = false)
    )

    private val _smsMessages = MutableStateFlow<List<SmsMessageData>>(initialDummyMessages)
    val smsMessages: StateFlow<List<SmsMessageData>> = _smsMessages

    init {
        Log.d("MailViewModel", "ViewModel initialized. Instance: $this. Initial messages count: ${_smsMessages.value.size}")
        // Collect messages from the shared flow
        viewModelScope.launch {
            Log.d("MailViewModel", "Starting to collect from smsEventFlow. Instance: $this")
            smsEventFlow.collect {
                Log.d("MailViewModel", "Collected SMS in ViewModel: ${it.body}. Instance: $this")
                // Prepend new messages to the existing list (which includes dummies initially)
                _smsMessages.value = listOf(it) + _smsMessages.value
                Log.d("MailViewModel", "Updated _smsMessages: ${_smsMessages.value.size} messages. Instance: $this")
            }
        }
    }

    companion object {
        private const val TAG = "MailViewModelCompanion"
        // SharedFlow to emit SMS messages from BroadcastReceiver to ViewModel
        // Configure it to replay the last emitted item for new collectors
        private val _smsEventFlow = MutableSharedFlow<SmsMessageData>(replay = 1)
        val smsEventFlow = _smsEventFlow.asSharedFlow()

        // Function for SmsReceiver to call
        fun newSmsReceived(sms: SmsMessageData) {
            Log.d(TAG, "newSmsReceived called with SMS from: ${sms.sender}")
            val emitted = _smsEventFlow.tryEmit(sms)
            Log.d(TAG, "SMS event emitted to SharedFlow: $emitted. Body: ${sms.body}")
        }
    }
}