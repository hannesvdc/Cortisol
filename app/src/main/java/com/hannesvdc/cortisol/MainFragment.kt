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
import java.util.Locale

class MainFragment : Fragment() {

    private lateinit var wakeButton : Button
    private lateinit var fourHourTextview : TextView
    private lateinit var eightHourTextview : TextView
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var uiHandler : Handler

    @Volatile
    private var runUpdateThread : Boolean = false
    private val fourHoursInMillis : Long = 4 * 60 * 60 * 1000
    private val eightHoursInMillis : Long = 8 * 60 * 60 * 1000
    private val alarmStartTimeKey = "alarm_start_time"
    private val locale = Locale.US

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        return inflater.inflate(R.layout.treatment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ask view permissions
        if ( !Settings.canDrawOverlays(context) ) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:${context?.packageName}")
            startActivityForResult(intent, 123)
        }

        // Set up the UI Components
        wakeButton = view.findViewById(R.id.wake_button)
        wakeButton.setOnClickListener {
            setSystemAlarms()
            startAlarmReadingThreads()
        }
        fourHourTextview = view.findViewById(R.id.countdownTextView4)
        eightHourTextview = view.findViewById(R.id.countdownTextView8)

        // Logic of the Fragment
        val fromSetupFragment = arguments?.getBoolean("from_setup_fragment")
        val sharedPreferencesKey = arguments?.getString("shared_arguments_key")
        sharedPreferences = requireContext().getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
        uiHandler = Handler(Looper.getMainLooper())
        if ( fromSetupFragment == true ) {
            resetFragment()
        } else if ( sharedPreferences.contains(alarmStartTimeKey) ) {
            val timeSinceStartAlarms = System.currentTimeMillis() - sharedPreferences.getLong(alarmStartTimeKey, 0L)
            if ( timeSinceStartAlarms > eightHoursInMillis ) {
                resetFragment()
            } else {
                startAlarmReadingThreads()
            }
        }
    }

    /**
     * Function that is called when both alarms have finished. This function cleans up the
     * fragment and makes it ready for a new day.
     */
    fun resetFragment() {
        runUpdateThread = false
        wakeButton.isEnabled = true
        sharedPreferences.edit().clear().apply()
    }

    /**
     * This function creates the four-hour and eight-hour alarms using an alarmManager.
     */
    private fun setSystemAlarms() {
        val currentTime = System.currentTimeMillis()
        val triggerTime4Hour = currentTime + fourHoursInMillis
        val triggerTime8Hour = currentTime + eightHoursInMillis
        sharedPreferences.edit().putLong(alarmStartTimeKey, currentTime).apply()

        // Set the Alarms using alarmManager and pendingIntents
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent4Hour = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("ALARM_TYPE", "4-hour alarm")
        }
        val intent8Hour = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("ALARM_TYPE", "8-hour alarm")
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
        wakeButton.isEnabled = false
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

                    // Display the remaining time on the textview
                    var fourHourText = "Alarm has Passed"
                    var eightHourText = "Alarm has Passed"
                    if ( millisUntilFinished < eightHoursInMillis ) {
                        eightHourText = String.format(locale, "%02d:%02d:%02d", hours, minutes, seconds)
                        if ( millisUntilFinished >= fourHoursInMillis ) {
                            fourHourText = String.format(locale, "%02d:%02d:%02d", hours - 4, minutes, seconds)
                        }
                    }

                    uiHandler.post {
                        fourHourTextview.text = fourHourText
                        eightHourTextview.text = eightHourText
                    }
                } catch (e: InterruptedException ) {
                    e.printStackTrace()
                }
            }

            // Post a fixed message when the thread stops running
            uiHandler.post {
                fourHourTextview.text = String.format(locale, "%02d:%02d:%02d", 4, 0, 0)
                eightHourTextview.text = String.format(locale, "%02d:%02d:%02d", 8, 0, 0)
            }
        }
        updateViewsThread.start()
    }
}