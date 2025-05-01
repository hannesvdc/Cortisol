package com.hannesvdc.cortisol

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var mainFragment : MainFragment
    private val sharedPreferencesKey = "TreatmentAlarms"
    private val treatmentPlanKey = "TreatmentPlan"

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPostNotification()

        // Instantiate the main fragment and setup a BroadcastManager for our AlarmReceiver
        mainFragment = MainFragment()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                mainFragment.resetFragment()
            }
        }
        val filter = IntentFilter("com.hannesvdc.cortisol.RESET_VIEWS")
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver, filter)

        // Check if a treatment sharedPreferences exists. If so, proceed to the main fragment, otherwise do setup
        if ( File(applicationContext.filesDir.parent,  "shared_prefs/$treatmentPlanKey.xml").exists() ) {
            Log.i("Activity", "File exists")
            val treatmentPlan = applicationContext.getSharedPreferences(treatmentPlanKey, Context.MODE_PRIVATE)
            val diseases = stringToBundle(treatmentPlan.getString("Diseases", ""))
            navigateToMainFragment(diseases, false)
        } else if (savedInstanceState == null ) {
            loadFragment(SetupFragment(treatmentPlanKey))
        }
    }

    /**
     * Function to load a fragment into the container.
     * @param fragment The fragment to display.
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Function to handle navigation from SetupFragment to MainFragment.
     * Called from SetupFragment or onCreate.
     */
    fun navigateToMainFragment(treatmentPlan: Bundle, fromSetupFragment : Boolean) {
        val argumentsBundle = Bundle()
        argumentsBundle.putBundle("treatment_plan", treatmentPlan)
        argumentsBundle.putString("shared_preferences_key", sharedPreferencesKey)
        argumentsBundle.putBoolean("from_setup_fragment", fromSetupFragment)

        mainFragment.arguments = argumentsBundle
        loadFragment(mainFragment)
    }

    /**
     * Function to ask for Notification Permissions using a pop-up window
     * Called from the onCreate function.
     */
    private fun requestPostNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101 // Request code for POST_NOTIFICATIONS permission
                )
            }
        }
    }

    /**
     * Simple function that converts the treatmentPlan stored as a string to
     * a Bundle. The treatmentPlan comes from SharedPreferences, which does not
     * store Bundles natively.
     */
    private fun stringToBundle(plan : String?) : Bundle {
        val bundle = Bundle()
        val jsonObject = plan?.let { JSONObject(it) }

        if (jsonObject != null) {
            for (key in jsonObject.keys()) {
                when (val value = jsonObject.get(key)) {
                    is String -> bundle.putString(key, value)
                    is Int -> bundle.putInt(key, value)
                    is Boolean -> bundle.putBoolean(key, value)
                    is Double -> bundle.putDouble(key, value)
                    is Long -> bundle.putLong(key, value)
                }
            }
        }
        return bundle
    }
}
