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
import android.os.Parcel
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var mainFragment : MainFragment
    private lateinit var treatmentPlanFile : File
    private val sharedPreferencesKey = "TreatmentAlarms"

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

        // Load existing treatment plan from file if they exist, otherwise run the SetupFragment
        treatmentPlanFile = File(applicationContext.filesDir, "treatmentplan.json")
        if ( treatmentPlanFile.exists() ) {
            val treatments = loadTreatmentPlan()
            navigateToMainFragment(treatments)
        } else if (savedInstanceState == null ) {
            loadFragment(SetupFragment())
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
    fun navigateToMainFragment(treatmentPlan: Bundle) {
        if ( !treatmentPlanFile.exists() ) {
            storeTreatmentPlan(treatmentPlan)
        }

        val argumentsBundle = Bundle()
        argumentsBundle.putBundle("treatment_plan", treatmentPlan)
        argumentsBundle.putString("shared_preferences_key", sharedPreferencesKey)

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
     * Function to load stored user-preferences such as a compilation of their treatment plan.
     */
    private fun loadTreatmentPlan() : Bundle {
        val parcel = Parcel.obtain()
        return try {
            val data = treatmentPlanFile.readBytes()
            parcel.unmarshall(data, 0, data.size)
            parcel.setDataPosition(0)
            Bundle.CREATOR.createFromParcel(parcel)
        } catch (e: Exception) {
            Bundle()
        } finally {
            parcel.recycle()
        }
    }

    /**
     * Function to store user treatment preferences to file.
     **/
    private fun storeTreatmentPlan(treatmentPlan : Bundle) {
        val parcel = Parcel.obtain()
        try {
            treatmentPlan.writeToParcel(parcel, 0)
            val data = parcel.marshall()
            treatmentPlanFile.writeBytes(data)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            parcel.recycle()
        }
    }

}
