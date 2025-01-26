package com.hannesvdc.cortisol

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
     * Function to handle navigation from WelcomeFragment to MainFragment.
     * Called from WelcomeFragment.
     */
    fun navigateToMainFragment(preferences: Bundle) {
        val mainFragment = MainFragment()
        mainFragment.arguments = preferences // Pass preferences as arguments
        loadFragment(mainFragment) // Load the MainFragment
    }
}
