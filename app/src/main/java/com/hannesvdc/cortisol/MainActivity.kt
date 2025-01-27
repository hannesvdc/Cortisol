package com.hannesvdc.cortisol

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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

class MainActivity : AppCompatActivity() {

    private lateinit var mainFragment : MainFragment

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPostNotification()

        mainFragment = MainFragment()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                Log.i("broadcast", "broadcast received")
                mainFragment.resetView()
            }
        }
        val filter = IntentFilter("com.hannesvdc.cortisol.RESET_VIEWS")
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver, filter)


        // Load the WelcomeFragment as the initial screen
        if (savedInstanceState == null) {
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
        mainFragment.arguments = preferences // Pass preferences as arguments
        loadFragment(mainFragment) // Load the MainFragment
    }

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
}
