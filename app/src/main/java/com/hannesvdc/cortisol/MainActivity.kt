package com.hannesvdc.cortisol

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import android.widget.Button
import android.widget.TextView

class MainActivity : ComponentActivity() {

    private lateinit var textView4Hour: TextView
    private lateinit var wakeButton: Button
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize UI components
        setContentView(R.layout.activity_main)
        textView4Hour = findViewById(R.id.text_view_4_hour)
        wakeButton = findViewById(R.id.wake_button)

        wakeButton.setOnClickListener {
            Log.println(Log.INFO, "Button", "The user pressed the button")
        }
    }
}