package com.hannesvdc.cortisol

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
        if (intent?.action == "STOP_VIBRATION") {
            isVibrating = false
            windowManager?.removeView(floatingView)
            stopVibration(context) // Stop vibration when button is pressed
            return
        }

        val alarmType = intent?.getStringExtra("ALARM_TYPE") ?: "Unknown alarm"
        val message : String = if (alarmType == "4-hour alarm") {
            "Please take 5 mg Hydrocortisol"
        } else {
            "Please take 2.5 mg Hydrocortisol"
        }

        showNotification(context, message)
        if (Settings.canDrawOverlays(context)) {
            showFloatingView(context, message)
        }

        if ( alarmType == "8-hour alarm") {
            val resetIntent = Intent("com.hannesvdc.cortisol.RESET_VIEWS")
            LocalBroadcastManager.getInstance(context).sendBroadcast(resetIntent)
        }
    }

    @SuppressLint("InflateParams")
    private fun showFloatingView(context: Context, message : String) {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

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

        windowManager?.addView(floatingView, params)

        val closeButton: Button = floatingView!!.findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            isVibrating = false
            stopVibration(context)
            windowManager?.removeView(floatingView)
        }

        isVibrating = true
        startContinuousVibration(context)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun startContinuousVibration(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrateThread = Thread {
            while (isVibrating) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(vibrationEffect)
                } else {
                    vibrator.vibrate(500)
                }
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        vibrateThread?.start()
    }

    private fun stopVibration(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.cancel()
        vibrateThread?.interrupt()
        vibrateThread = null
    }

    private fun showNotification(context: Context, message : String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "alarm_channel", "Alarm Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        // Intent to stop vibration
        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "STOP_VIBRATION"
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setContentTitle("Cortisol Alarm")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(1, notification)
    }
}