package com.example.upay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
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

    private val lifecycleRegistry = LifecycleRegistry(this)
    private lateinit var savedStateRegistryController: SavedStateRegistryController

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    companion object {
        const val ACTION_SHOW = "com.example.upay.ACTION_SHOW"
        const val ACTION_HIDE = "com.example.upay.ACTION_HIDE"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        savedStateRegistryController = SavedStateRegistryController.create(this)
        savedStateRegistryController.performRestore(null) // Restore state if any
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        when (intent?.action) {
            ACTION_SHOW -> showOverlay()
            ACTION_HIDE -> hideOverlay()
        }
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.e("CallOverlayService", "SYSTEM_ALERT_WINDOW permission not granted.")
            Toast.makeText(this, "Overlay permission not granted", Toast.LENGTH_SHORT).show()
            // Optionally, you could try to send the user to settings here,
            // but it's better handled from an Activity context.
            return
        }

        if (overlayView == null) {
            overlayView = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@CallOverlayService)
                setViewTreeSavedStateRegistryOwner(this@CallOverlayService)
                setContent {
                    UPAYTheme { // Apply your app's theme
                        CallOverlayView(
                            onForwardClick = {
                                Log.d("CallOverlayService", "Forward clicked")
                                Toast.makeText(applicationContext, "Forward Clicked", Toast.LENGTH_SHORT).show()
                                // Potentially stopSelf() or hideOverlay()
                            },
                            onReportClick = {
                                Log.d("CallOverlayService", "Report clicked")
                                Toast.makeText(applicationContext, "Report Clicked", Toast.LENGTH_SHORT).show()
                                // Potentially stopSelf() or hideOverlay()
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
            } catch (e: Exception) {
                Log.e("CallOverlayService", "Error adding overlay view", e)
            }
        }
    }

    private fun hideOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
                overlayView = null
            } catch (e: Exception) {
                Log.e("CallOverlayService", "Error removing overlay view", e)
            }
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        hideOverlay()
    }
}
