package com.hannesvdc.cortisol

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat

class VibrationService : Service() {

    private lateinit var vibrator: Vibrator
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_VIBRATION") {
            stopVibration()
            return START_NOT_STICKY
        }

        val message = intent?.getStringExtra("ALARM_MESSAGE") ?: "Default alarm message"
        startVibration()
        showFloatingView(message)
        startForeground(1, createNotification(message))

        return START_STICKY
    }

    private fun startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 500, 1000)
            val vibrationEffect = VibrationEffect.createWaveform(pattern, 0)
            vibrator.vibrate(vibrationEffect)
        } else {
            val pattern = longArrayOf(0, 500, 1000)
            vibrator.vibrate(pattern, 0)
        }
    }

    private fun stopVibration() {
        vibrator.cancel()

        if (floatingView != null && windowManager != null) {
            windowManager?.removeView(floatingView)
            floatingView = null
        }

        stopForeground(true)
        stopSelf()
    }

    private fun showFloatingView(message: String) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null)
        val floatingTextbox: TextView = floatingView!!.findViewById(R.id.floatingText)
        floatingTextbox.text = message

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        windowManager?.addView(floatingView, params)

        val closeButton: Button = floatingView!!.findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            stopVibration()
        }
    }

    private fun createNotification(message : String) : Notification {
        val channelId = "alarm_channel"
        val channelName = "Vibration Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, VibrationService::class.java).apply {
            action = "STOP_VIBRATION"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Cortisol Alarm")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_pill)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPendingIntent)
            .setAutoCancel(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVibration()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}