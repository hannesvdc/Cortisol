package com.hannesvdc.cortisol

import android.os.Bundle
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
    private var timer4: CortisolTimer? = null
    private var timer8: CortisolTimer? = null

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

            if (timer4 === null || timer4!!.hasFinished()) {
                startTimers()
            } else {
                Log.i("Timer", "Doing Nothing, the timer is still running.")
            }
        }
    }

    private fun startTimers() {
        val durationInMillis4 = TimeUnit.HOURS.toMillis(4) // 4 hours in milliseconds
        val durationInMillis8 = TimeUnit.HOURS.toMillis(8) // 4 hours in milliseconds
        val reportTime : Long = 500

        timer4 = CortisolTimer(applicationContext, textView4Hour, durationInMillis4, reportTime)
        timer8 = CortisolTimer(applicationContext, textView8Hour, durationInMillis8, reportTime)

        timer4?.start()
        timer8?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer4?.cancel()
        timer8?.cancel()
    }
}