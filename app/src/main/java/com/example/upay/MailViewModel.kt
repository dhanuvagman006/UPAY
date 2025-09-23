package com.example.upay

import android.app.Application
import android.provider.Telephony
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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
    val fake: Boolean = false // 'fake' is false for actual SMS from inbox
)

open class MailViewModel(application: Application) : AndroidViewModel(application) {

    private val _smsMessages = MutableStateFlow<List<SmsMessageData>>(emptyList())
    val smsMessages: StateFlow<List<SmsMessageData>> = _smsMessages

    init {
        Log.d("MailViewModel", "ViewModel initialized. Instance: $this.")
        fetchSmsFromInbox() // Load initial SMS from device inbox

        // Collect new messages from the shared flow (e.g., for real-time updates from SmsReceiver)
        viewModelScope.launch {
            Log.d("MailViewModel", "Starting to collect from smsEventFlow. Instance: $this")
            smsEventFlow.collect { newSms ->
                Log.d("MailViewModel", "Collected new live SMS in ViewModel: ${newSms.body}. Instance: $this")
                // Prepend new messages to the existing list
                _smsMessages.value = listOf(newSms) + _smsMessages.value
                Log.d("MailViewModel", "Updated _smsMessages with new live SMS: ${_smsMessages.value.size} messages. Instance: $this")
            }
        }
    }

    private fun fetchSmsFromInbox() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val fetchedMessages = mutableListOf<SmsMessageData>()
            Log.d("MailViewModel", "Attempting to fetch SMS from inbox...")

            try {
                // Ensure READ_SMS permission is granted by the app before this is called.
                // The user has stated permission is handled.
                val cursor = context.contentResolver.query(
                    Telephony.Sms.Inbox.CONTENT_URI,
                    arrayOf(
                        Telephony.Sms.ADDRESS, // Sender's address
                        Telephony.Sms.BODY,    // Message body
                        Telephony.Sms.DATE     // Timestamp (date received/sent)
                    ),
                    null, // No selection criteria (get all from inbox)
                    null, // No selection arguments
                    Telephony.Sms.DATE + " DESC" // Sort by date descending (newest first)
                )

                cursor?.use { // Automatically closes the cursor
                    if (it.moveToFirst()) {
                        val senderColumnIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                        val bodyColumnIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                        val timestampColumnIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                        do {
                            val sender = it.getString(senderColumnIndex)
                            val body = it.getString(bodyColumnIndex)
                            val timestamp = it.getLong(timestampColumnIndex)
                            // Real SMS messages are not 'fake'
                            fetchedMessages.add(SmsMessageData(sender ?: "Unknown", body ?: "", timestamp, fake = false))
                        } while (it.moveToNext())
                        Log.d("MailViewModel", "Successfully fetched ${fetchedMessages.size} SMS from inbox.")
                    } else {
                        Log.d("MailViewModel", "Inbox is empty or no messages found.")
                    }
                } ?: run {
                    Log.w("MailViewModel", "Cursor was null when querying SMS inbox.")
                }
                _smsMessages.value = fetchedMessages
            } catch (e: SecurityException) {
                Log.e("MailViewModel", "SecurityException while fetching SMS: ${e.message}. READ_SMS permission might be missing or denied at runtime.", e)
                // _smsMessages.value will remain as it was (e.g., emptyList or previous state)
            } catch (e: Exception) {
                Log.e("MailViewModel", "Error fetching SMS from inbox: ${e.message}", e)
                // _smsMessages.value will remain as it was
            }
        }
    }

    companion object {
        private const val TAG = "MailViewModelCompanion"
        // SharedFlow to emit SMS messages from BroadcastReceiver to ViewModel
        private val _smsEventFlow = MutableSharedFlow<SmsMessageData>(replay = 1) // Replay for late collectors (e.g. screen rotation)
        val smsEventFlow = _smsEventFlow.asSharedFlow()

        // Function for SmsReceiver to call
        fun newSmsReceived(sms: SmsMessageData) {
            Log.d(TAG, "newSmsReceived called with SMS from: ${sms.sender}, Body: ${sms.body}")
            // Assuming SmsReceiver correctly sets 'fake = false' for real SMS
            val emitted = _smsEventFlow.tryEmit(sms)
            Log.d(TAG, "SMS event emitted to SharedFlow: $emitted")
        }
    }
}
