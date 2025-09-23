package com.example.upay

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat

class IncomingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            Log.d("IncomingCallReceiver", "Phone state changed: $state")

            // Ensure READ_PHONE_STATE permission is granted before proceeding
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Log.w("IncomingCallReceiver", "READ_PHONE_STATE permission not granted. Cannot show overlay.")
                return
            }

            val serviceIntent = Intent(context, CallOverlayService::class.java)

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    Log.d("IncomingCallReceiver", "Call ringing, starting CallOverlayService with ACTION_SHOW")
                    serviceIntent.action = CallOverlayService.ACTION_SHOW
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    Log.d("IncomingCallReceiver", "Call offhook.")
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    Log.d("IncomingCallReceiver", "Call idle, starting CallOverlayService with ACTION_HIDE")
                    serviceIntent.action = CallOverlayService.ACTION_HIDE
                    // No need for startForegroundService for hiding, but startService is fine.
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
