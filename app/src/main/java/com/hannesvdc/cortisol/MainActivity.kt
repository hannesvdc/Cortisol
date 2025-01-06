package com.hannesvdc.cortisol

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import android.widget.Button

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.wake_button).setOnClickListener {
            Log.println(Log.INFO, "Button", "The user pressed the button")
        }
    }
}