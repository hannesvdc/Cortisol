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
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var mainFragment : MainFragment
    private lateinit var preferencesFile : File

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request all necessary permissions using pop-up windows
        requestPostNotification()

        // Instantiate the main fragment, we will use it anyway, either now or through the SetupFragment
        mainFragment = MainFragment()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                Log.i("broadcast", "broadcast received")
                mainFragment.resetView()
            }
        }
        val filter = IntentFilter("com.hannesvdc.cortisol.RESET_VIEWS")
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver, filter)

        // Load existing preferences from file if they exist, otherwise start the SetupFragment
        preferencesFile = File(applicationContext.filesDir, "preferences.json")
        if ( preferencesFile.exists() ) {
            Log.i("Preferences", "Preferences file exists, moving to the main fragment")
            val preferences = loadPreferences()
            navigateToMainFragment(preferences)
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
            .replace(R.id.fragment_container, fragment) // Replace current fragment
            .addToBackStack(null) // Add transaction to the back stack (optional for navigation)
            .commit()
    }

    /**
     * Function to handle navigation from SetupFragment to MainFragment.
     * Called from SetupFragment.
     */
    fun navigateToMainFragment(preferences: Bundle) {
        // Store the new preferences first
        storePreferences(preferences)

        // Then display the main fragment
        mainFragment.arguments = preferences
        loadFragment(mainFragment)
    }

    /**
     * Function to ask for Notification Permissions using a pop-up window
     * Called from the onCreate function.
     */
    private fun requestPostNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.i("Permissions", "Asking Post-notifications permissions")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101 // Request code for POST_NOTIFICATIONS permission
                )
            }
        }
    }

    /**
     * Fynction to load stored user-preferences such as a compilation of their treatment plan.
     *
     * TODO: These are not as much 'preferences' as they are the foundation of this app
     */
    private fun loadPreferences() : Bundle {
        val parcel = Parcel.obtain()
        return try {
            val data = preferencesFile.readBytes()
            parcel.unmarshall(data, 0, data.size)
            parcel.setDataPosition(0)
            Bundle.CREATOR.createFromParcel(parcel)
        } catch (e: Exception) {
            Log.i("Loading", "Preferences file could not be loaded")
            Bundle()
        } finally {
            parcel.recycle() // Clean up the Parcel
        }
    }

    /**
     * Fynction to store user treatment preferences to file.
     *
     * TODO: These are not as much 'preferences' as they are the foundation of this app
     */
    private fun storePreferences(preferences : Bundle) {
        val parcel = Parcel.obtain()
        try {
            preferences.writeToParcel(parcel, 0) // Write the Bundle to the Parcel
            val data = parcel.marshall() // Convert the Parcel to a byte array
            preferencesFile.writeBytes(data) // Write the byte array to the file
            println("Bundle saved successfully!")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to save Bundle.")
        } finally {
            parcel.recycle() // Clean up the Parcel
        }
    }

}
