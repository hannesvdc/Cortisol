package com.hannesvdc.cortisol

import android.annotation.SuppressLint
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import android.widget.Button
import android.widget.TextView
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var textView4Hour: TextView
    private lateinit var textView8Hour: TextView
    private lateinit var wakeButton: Button
    private var timer4: CountDownTimer? = null
    private var timer8: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize UI components
        setContentView(R.layout.activity_main)
        textView4Hour = findViewById(R.id.text_view_4_hour)
        textView8Hour = findViewById(R.id.text_view_8_hour)
        wakeButton = findViewById(R.id.wake_button)

        wakeButton.setOnClickListener {
            Log.println(Log.INFO, "Button", "The user pressed the button")
            startTimers()
        }
    }

    private fun startTimers() {
        val durationInMillis4 = TimeUnit.HOURS.toMillis(4) // 4 hours in milliseconds
        val durationInMillis8 = TimeUnit.HOURS.toMillis(8) // 4 hours in milliseconds

        timer4 = object : CountDownTimer(durationInMillis4, 500) {
            @SuppressLint("SetTextI18n", "DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                // Format time as HH:mm:ss
                val timeRemaining = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                )
                textView4Hour.text = "Time remaining: $timeRemaining"
            }

            override fun onFinish() {
                triggerAlarm()
            }
        }

        timer8 = object : CountDownTimer(durationInMillis8, 500) {
            @SuppressLint("SetTextI18n", "DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                // Format time as HH:mm:ss
                val timeRemaining = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                )
                textView8Hour.text = "Time remaining: $timeRemaining"
            }

            override fun onFinish() {
                triggerAlarm()
            }
        }

        timer4?.start()
        timer8?.start()
    }

    private fun triggerAlarm() {
        // Play the default alarm sound
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        ringtone.play()

        // Log the alarm event
        Log.i("MainActivity", "Alarm triggered!")
    }

    override fun onDestroy() {
        super.onDestroy()
        timer4?.cancel()
        timer8?.cancel()
    }
}