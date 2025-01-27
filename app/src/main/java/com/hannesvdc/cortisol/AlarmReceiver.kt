package com.hannesvdc.cortisol

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.core.app.NotificationCompat
import android.util.Log
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AlarmReceiver : BroadcastReceiver() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    private var isVibrating = true
    private var vibrateThread: Thread? = null

    override fun onReceive(context: Context, intent: Intent?) {
        val alarmType = intent?.getStringExtra("ALARM_TYPE") ?: "Unknown alarm"
        val message : String = if (alarmType == "4-hour alarm") {
            "Please take 5 mg Hydrocortisol"
        } else {
            "Please take 2.5 mg Hydrocortisol"
        }
        Log.i("Alarm", "System alarm has passed: $alarmType")

        // Show Notification
        showNotification(context, message)

        // Show Floating View (if permission granted)
        if (Settings.canDrawOverlays(context)) {
            showFloatingView(context, message)
        }

        if ( alarmType == "8-hour alarm") {
            Log.i("alarmreceiver", "broadcast sent")
            val resetIntent = Intent("com.hannesvdc.cortisol.RESET_VIEWS")
            LocalBroadcastManager.getInstance(context).sendBroadcast(resetIntent)
        }
    }

    @SuppressLint("InflateParams")
    private fun showFloatingView(context: Context, message : String) {
        // Initialize WindowManager
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create Floating View
        floatingView = LayoutInflater.from(context).inflate(R.layout.floating_view, null)
        val floatingTextbox : TextView = floatingView!!.findViewById(R.id.floatingText)
        floatingTextbox.text = message

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
            isVibrating = false
            stopVibration(context)
            windowManager?.removeView(floatingView)
        }

        // Start playing sound and vibration continuously
        isVibrating = true
        startContinuousVibration(context)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun startContinuousVibration(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrateThread = Thread {
            while (isVibrating) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // For devices running Android Oreo and above
                    val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(vibrationEffect)
                } else {
                    // For older devices
                    vibrator.vibrate(500)
                }
                try {
                    Thread.sleep(1000) // Vibrate every 1 second (adjustable)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        vibrateThread?.start()
    }

    private fun stopVibration(context : Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel() // Stop vibration
    }

    private fun showNotification(context: Context, message : String) {
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
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // Use a built-in Android icon for now
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Show notification
        notificationManager.notify(1, notification)
    }
}