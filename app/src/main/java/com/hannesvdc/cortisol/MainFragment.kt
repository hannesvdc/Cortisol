package com.hannesvdc.cortisol

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.util.Log
import android.widget.Button

class MainFragment : Fragment() {

    private lateinit var wakeButton : Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        return inflater.inflate(R.layout.treatment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ask view permissions
        if (!Settings.canDrawOverlays(context)) {
            Log.i("Permissions", "Asking permission to draw overlays")
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:${context?.packageName}")
            startActivityForResult(intent, 123) // 123 is an arbitrary request code
        } else {
            Log.i("Permissions", "Apparently we can draw overlays")
        }

        wakeButton = view.findViewById(R.id.wake_button)
        wakeButton.setOnClickListener {
            wakeButton.isEnabled = false
            setSystemAlarms()
        }
    }

    private fun setSystemAlarms() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)

        // Create a PendingIntent
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Set the alarm for 4 hours later
        val triggerTime4Hour = System.currentTimeMillis() + 4 * 60 * 60 * 1000 // 4 hours in milliseconds
        val triggerTime8Hour = System.currentTimeMillis() + 8 * 60 * 60 * 1000 // 4 hours in milliseconds
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime4Hour,
            pendingIntent
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime8Hour,
            pendingIntent
        )

        Log.i("Alarms", "System Alarms have been set")
    }
}