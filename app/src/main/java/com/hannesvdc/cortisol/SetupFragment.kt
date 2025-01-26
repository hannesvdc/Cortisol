package com.hannesvdc.cortisol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip

class SetupFragment : Fragment() {

    private lateinit var button : Button
    private lateinit var addisonCheck : Chip

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        return inflater.inflate(R.layout.intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addisonCheck = view.findViewById(R.id.addison_chip)
        addisonCheck.setOnClickListener {
            button.isEnabled = addisonCheck.isChecked
        }

        button = view.findViewById(R.id.continue_button)
        button.setOnClickListener {
            // Gather all Preferences
            val preferences = Bundle()
            preferences.putBoolean("Addison", addisonCheck.isChecked)

            // Get a reference to the MainActivity and trigger navigation
            (activity as? MainActivity)?.navigateToMainFragment(preferences)
        }
    }
}