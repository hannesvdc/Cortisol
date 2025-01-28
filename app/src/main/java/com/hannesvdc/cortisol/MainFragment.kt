package com.hannesvdc.cortisol

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.util.Log
import android.widget.Button
import android.widget.TextView

class MainFragment : Fragment() {

    private lateinit var wakeButton : Button
    private lateinit var fourHourTextview : TextView
    private lateinit var eightHourTextview : TextView
    private val fourHoursInMillis : Long = 4 * 60 * 60 * 1000
    private val eightHoursInMillis : Long = 8 * 60 * 60 * 1000

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        return inflater.inflate(R.layout.treatment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ask view permissions
        if (!Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:${context?.packageName}")
            startActivityForResult(intent, 123) // 123 is an arbitrary request code
        }

        wakeButton = view.findViewById(R.id.wake_button)
        wakeButton.setOnClickListener {
            wakeButton.isEnabled = false
            setSystemAlarms()
            setCountdownTimers()
        }

        fourHourTextview = view.findViewById(R.id.countdownTextView4)
        eightHourTextview = view.findViewById(R.id.countdownTextView8)
    }

    private fun setSystemAlarms() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent4Hour = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("ALARM_TYPE", "4-hour alarm") // Add an extra to distinguish the alarm
        }
        val intent8Hour = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("ALARM_TYPE", "8-hour alarm") // Add an extra to distinguish the alarm
        }

        // Make the pending intents
        val pendingIntent4Hour = PendingIntent.getBroadcast(context, 0, intent4Hour, PendingIntent.FLAG_IMMUTABLE)
        val pendingIntent8Hour = PendingIntent.getBroadcast(context, 1, intent8Hour, PendingIntent.FLAG_IMMUTABLE)

        // Set the alarm for 4 hours later
        val triggerTime4Hour = System.currentTimeMillis() + fourHoursInMillis
        val triggerTime8Hour = System.currentTimeMillis() + eightHoursInMillis
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime4Hour,
            pendingIntent4Hour
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime8Hour,
            pendingIntent8Hour
        )
    }

    private fun setCountdownTimers() {
        val timer4Hour = object: CountDownTimer(fourHoursInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / (1000 * 60 * 60)
                val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                val seconds = (millisUntilFinished % (1000 * 60)) / 1000

                // Update the TextView with the formatted time
                fourHourTextview.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }

            override fun onFinish() {
                fourHourTextview.text = "4-Hour Alarm has Passed"
            }
        }
        val timer8Hour = object: CountDownTimer(eightHoursInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / (1000 * 60 * 60)
                val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                val seconds = (millisUntilFinished % (1000 * 60)) / 1000

                // Update the TextView with the formatted time
                eightHourTextview.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }

            override fun onFinish() {}
        }

        timer4Hour.start()
        timer8Hour.start()
    }

    fun resetView() {
        wakeButton.isEnabled = true
        fourHourTextview.text = "04:00:00"
        eightHourTextview.text = "08:00:00"
    }
}