package com.hannesvdc.cortisol

import android.annotation.SuppressLint
import android.content.Context
import android.media.RingtoneManager
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import java.util.concurrent.TimeUnit

class CortisolTimer(private var applicationContext : Context,
                    private var textview: TextView,
                    millisInFuture: Long,
                    countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {

    private var isFinished : Boolean = false

    fun hasFinished() : Boolean {
        return isFinished
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onTick(millisUntilFinished: Long) {
        isFinished = false

        // Format time as HH:mm:ss
        val timeRemaining = String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
        )
        textview.text = "Time remaining: $timeRemaining"
    }

    override fun onFinish() {
        isFinished = true

        // Play the default alarm sound
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        ringtone.play()

        // Log the alarm event
        Log.i("MainActivity", "Alarm triggered!")
    }
}