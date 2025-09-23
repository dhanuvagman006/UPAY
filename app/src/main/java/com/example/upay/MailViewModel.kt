package com.example.upay

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SmsMessageData(
    val sender: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis() // Automatically add a timestamp
)

class MailViewModel : ViewModel() {

    // Private MutableStateFlow that can be updated from within the ViewModel
    private val _smsMessages = MutableStateFlow<List<SmsMessageData>>(emptyList())

    // Publicly exposed StateFlow for UI to observe
    val smsMessages: StateFlow<List<SmsMessageData>> = _smsMessages

    // Function to add a new SMS message
    fun addSms(sender: String, body: String) {
        val newMessage = SmsMessageData(sender, body)
        // Add to the beginning of the list to show newest messages first
        _smsMessages.value = listOf(newMessage) + _smsMessages.value
    }

    // In a real app, you might load existing SMS messages here
    // init {
    //     loadInitialSmsMessages()
    // }

    // private fun loadInitialSmsMessages() {
    //     // TODO: Implement logic to load SMS from device storage if needed
    //     // This would require READ_SMS permission and querying the SMS content provider.
    //     // For now, we'll start with an empty list.
    // }
}