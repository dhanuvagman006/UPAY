package com.example.upay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // TODO: Parse SMS messages and update MailScreen
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent?.action) {
            // Add your SMS parsing logic here
            Log.d("SmsReceiver", "SMS Received!")
        }
    }
}