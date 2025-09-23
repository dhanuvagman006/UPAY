package com.example.upay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("SmsReceiver", "onReceive CALLED. Action: ${intent?.action}")

        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent?.action) {
            Log.d("SmsReceiver", "SMS_RECEIVED_ACTION matched.")
            val messages: Array<SmsMessage>? = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.forEach { smsMessage ->
                val sender = smsMessage.displayOriginatingAddress ?: "Unknown Sender"
                val messageBody = smsMessage.messageBody ?: "No Content"
                Log.d("SmsReceiver", "SMS from: $sender - Body: $messageBody")

                val newSms = SmsMessageData(sender, messageBody)
                MailViewModel.newSmsReceived(newSms)
            }
        } else {
            Log.d("SmsReceiver", "Intent action did NOT match SMS_RECEIVED_ACTION. Current action: ${intent?.action}")
        }
    }
}