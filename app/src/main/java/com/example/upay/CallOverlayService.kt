package com.example.upay

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.upay.ui.theme.UPAYTheme

class CallOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private lateinit var telecomManager: TelecomManager

    private val lifecycleRegistry = LifecycleRegistry(this)
    private lateinit var savedStateRegistryController: SavedStateRegistryController

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    companion object {
        const val ACTION_SHOW = "com.example.upay.ACTION_SHOW"
        const val ACTION_HIDE = "com.example.upay.ACTION_HIDE"
        private const val NOTIFICATION_ID = 12345
        private const val NOTIFICATION_CHANNEL_ID = "CallOverlayServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()

        startForegroundWithNotification()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        savedStateRegistryController = SavedStateRegistryController.create(this)
        savedStateRegistryController.performRestore(null) // Restore state if any
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    private fun startForegroundWithNotification() {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java) // Intent to launch when notification is tapped
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingIntentFlags)

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("UPAY Call Assistant Active")
            .setContentText("Managing incoming call overlay.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        try {
            startForeground(NOTIFICATION_ID, notification)
            Log.d("CallOverlayService", "Service started in foreground.")
        } catch (e: Exception) {
            Log.e("CallOverlayService", "Error starting foreground service", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Call Overlay Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
            Log.d("CallOverlayService", "Notification channel created.")
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        when (intent?.action) {
            ACTION_SHOW -> showOverlay()
            ACTION_HIDE -> hideOverlay()
            else -> {
                // If service is restarted or started without a specific action, ensure it's in foreground
                // and potentially show overlay if that's the desired default state
                Log.d("CallOverlayService", "onStartCommand with action: ${intent?.action}")
                // showOverlay() // Uncomment if you want the overlay to appear on any start command
            }
        }
        return START_NOT_STICKY
    }

    private fun answerCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
            try {
                telecomManager.acceptRingingCall()
                Log.d("CallOverlayService", "Call answered.")
                Toast.makeText(this, "Call Answered", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                Log.e("CallOverlayService", "SecurityException answering call: ", e)
                Toast.makeText(this, "Could not answer call: Security issue", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("CallOverlayService", "Exception answering call: ", e)
                Toast.makeText(this, "Could not answer call", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.e("CallOverlayService", "ANSWER_PHONE_CALLS permission not granted when trying to answer.")
            Toast.makeText(this, "Permission to answer calls not granted.", Toast.LENGTH_LONG).show()
        }
        // It's often good practice to hide the overlay after an action
         hideOverlay() // Hide overlay after answering
    }

    private fun declineCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
            try {
                // endCall should work for declining ringing calls and ending active ones on API 28+
                telecomManager.endCall()
                Log.d("CallOverlayService", "Call declined/ended.")
                Toast.makeText(this, "Call Declined", Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                Log.e("CallOverlayService", "SecurityException declining call: ", e)
                Toast.makeText(this, "Could not decline call: Security issue", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("CallOverlayService", "Exception declining call: ", e)
                Toast.makeText(this, "Could not decline call", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.e("CallOverlayService", "ANSWER_PHONE_CALLS permission not granted when trying to decline.")
            Toast.makeText(this, "Permission to decline calls not granted.", Toast.LENGTH_LONG).show()
        }
        hideOverlay() // Hide overlay after declining
    }

    private fun showOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.e("CallOverlayService", "SYSTEM_ALERT_WINDOW permission not granted.")
            Toast.makeText(this, "Overlay permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        if (overlayView == null) {
            overlayView = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@CallOverlayService)
                setViewTreeSavedStateRegistryOwner(this@CallOverlayService)
                setContent {
                    UPAYTheme { // Apply your app's theme
                        CallOverlayView(
                            onAnswerClick = {
                                Log.d("CallOverlayService", "Answer clicked")
                                answerCall()
                            },
                            onDeclineClick = {
                                Log.d("CallOverlayService", "Decline clicked")
                                declineCall()
                            }
                        )
                    }
                }
            }

            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER

            try {
                windowManager.addView(overlayView, params)
                 Log.d("CallOverlayService", "Overlay view added.")
            } catch (e: Exception) {
                Log.e("CallOverlayService", "Error adding overlay view", e)
            }
        } else {
            Log.d("CallOverlayService", "Overlay view already shown.")
        }
    }

    private fun hideOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
                overlayView = null
                Log.d("CallOverlayService", "Overlay view removed.")
            } catch (e: Exception) {
                Log.e("CallOverlayService", "Error removing overlay view", e)
            }
        }
        stopSelf() // Stop the service when the overlay is hidden
        Log.d("CallOverlayService", "Service stop requested.")
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        hideOverlay() // Ensure overlay is removed if service is destroyed
         Log.d("CallOverlayService", "Service destroyed.")
    }
}
