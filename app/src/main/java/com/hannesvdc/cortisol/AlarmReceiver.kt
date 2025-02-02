package com.hannesvdc.cortisol

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        when (action) {
            "STOP_VIBRATION" -> {
                stopVibration(context)
            }
            else -> {
                val alarmType = intent?.getStringExtra("ALARM_TYPE") ?: "Unknown alarm"
                val message: String = if (alarmType == "4-hour alarm") {
                    "Please take 5 mg Hydrocortisol"
                } else {
                    "Please take 2.5 mg Hydrocortisol"
                }

                // Start vibration service
                val vibrateServiceIntent = Intent(context, VibrationService::class.java) .apply {
                    putExtra("ALARM_MESSAGE", message)
                }
                ContextCompat.startForegroundService(context, vibrateServiceIntent)

                if (alarmType == "8-hour alarm") {
                    val resetIntent = Intent("com.hannesvdc.cortisol.RESET_VIEWS")
                    LocalBroadcastManager.getInstance(context).sendBroadcast(resetIntent)
                }
            }
        }
    }

    private fun stopVibration(context: Context) {
        val stopVibrateServiceIntent = Intent(context, VibrationService::class.java)
        stopVibrateServiceIntent.action = "STOP_VIBRATION"
        context.stopService(stopVibrateServiceIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }
}