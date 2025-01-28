package com.hannesvdc.cortisol

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.TextView
import androidx.core.content.edit

class MainFragment : Fragment() {

    private lateinit var wakeButton : Button
    private lateinit var fourHourTextview : TextView
    private lateinit var eightHourTextview : TextView
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var handler : Handler

    @Volatile
    private var runUpdateThread : Boolean = false
    private val fourHoursInMillis : Long = 4 * 60 * 60 * 1000
    private val eightHoursInMillis : Long = 8 * 60 * 60 * 1000
    private val alarmStartTimeKey = "alarm_start_time"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        val sharedPreferencesKey = arguments?.getString("shared_arguments_key")
        sharedPreferences = requireContext().getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
        handler = Handler(Looper.getMainLooper())

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
            startAlarmReadingThreads()
        }

        fourHourTextview = view.findViewById(R.id.countdownTextView4)
        eightHourTextview = view.findViewById(R.id.countdownTextView8)

        // This statement is invoked whenever the timers are already running.
        // This event typically happens when the OS cancels, stops or pauses the app.
        if ( sharedPreferences.contains(alarmStartTimeKey) ) {
            startAlarmReadingThreads()
        }
    }

    /**
     * Function that is called when both alarms have finished. This function cleans up the
     * fragment and makes it ready for a new day.
     */
    fun resetView() {
        runUpdateThread = false
        wakeButton.isEnabled = true

        sharedPreferences.edit {
            remove("alarm_start_time")
            apply()
        }
    }

    /**
     * This function creates the four-hour and eight-hour alarms using an alarmManager.
     */
    private fun setSystemAlarms() {
        val currentTime = System.currentTimeMillis()
        sharedPreferences.edit {
            putLong(alarmStartTimeKey, currentTime)
            apply()
        }
        val triggerTime4Hour = currentTime + fourHoursInMillis
        val triggerTime8Hour = currentTime + eightHoursInMillis

        // Set the Alarms using alarmManager and pendingIntents
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent4Hour = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("ALARM_TYPE", "4-hour alarm") // Add an extra to distinguish the alarm
        }
        val intent8Hour = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("ALARM_TYPE", "8-hour alarm") // Add an extra to distinguish the alarm
        }
        val pendingIntent4Hour = PendingIntent.getBroadcast(context, 0, intent4Hour, PendingIntent.FLAG_IMMUTABLE)
        val pendingIntent8Hour = PendingIntent.getBroadcast(context, 1, intent8Hour, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime4Hour, pendingIntent4Hour)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime8Hour, pendingIntent8Hour)
    }

    /**
     * This function starts a thread that reads sharedPreferences and updates the
     * textviews with the time left in the threads.
     */
    private fun startAlarmReadingThreads() {
        runUpdateThread = true
        val updateViewsThread = Thread {
            while ( runUpdateThread ) {
                try {
                    Thread.sleep(900)

                    // Get the time the alarms were started
                    val alarmStartTime = sharedPreferences.getLong(alarmStartTimeKey, -1L)
                    val millisUntilFinished = eightHoursInMillis - (System.currentTimeMillis() - alarmStartTime)
                    val hours = millisUntilFinished / (1000 * 60 * 60)
                    val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = (millisUntilFinished % (1000 * 60)) / 1000

                    var fourHourText : String
                    var eightHourText : String
                    if ( millisUntilFinished < 0L ) {
                        fourHourText = "4-Hour Alarm has Passed"
                        eightHourText = "8-Hour Alarm has Passed"
                    } else if ( millisUntilFinished < fourHoursInMillis ) {
                        fourHourText = "4-Hour Alarm has Passed"
                        eightHourText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        fourHourText = String.format("%02d:%02d:%02d", hours - 4, minutes, seconds)
                        eightHourText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    }

                    handler.post {
                        fourHourTextview.text = fourHourText
                        eightHourTextview.text = eightHourText
                    }
                } catch (e: InterruptedException ) {
                    e.printStackTrace()
                }
            }

            // Post a fixed message when the thread stops running
            handler.post {
                fourHourTextview.text = "04:00:00"
                eightHourTextview.text = "08:00:00"
            }
        }
        updateViewsThread.start()
    }
}