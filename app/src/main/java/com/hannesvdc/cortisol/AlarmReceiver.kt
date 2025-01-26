package com.hannesvdc.cortisol

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onReceive(context: Context, intent: Intent?) {
        // Show Floating View (if permission granted)
        if (Settings.canDrawOverlays(context)) {
            showFloatingView(context)
        }

        // Show Notification
        showNotification(context)
    }

    private fun showFloatingView(context: Context) {
        // Initialize WindowManager
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create Floating View
        floatingView = LayoutInflater.from(context).inflate(R.layout.floating_view, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        // Add floating view to window
        windowManager?.addView(floatingView, params)

        // Handle close button in floating view
        val closeButton: Button = floatingView!!.findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            // Remove floating view when close button is pressed
            windowManager?.removeView(floatingView)
        }
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0 and above
        val channel = NotificationChannel(
            "alarm_channel", "Alarm Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        // Build notification
        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setContentTitle("Alarm Triggered")
            .setContentText("Your 4-hour alarm has gone off!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Show notification
        notificationManager.notify(0, notification)
    }
}