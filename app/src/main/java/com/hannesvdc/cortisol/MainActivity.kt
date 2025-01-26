package com.hannesvdc.cortisol

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity : AppCompatActivity() {

    private lateinit var mainFragment : MainFragment

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}
