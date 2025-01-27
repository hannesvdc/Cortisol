package com.hannesvdc.cortisol

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class SetupFragment : Fragment() {

    private lateinit var button : Button
    private lateinit var diseaseChipGroup : ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        return inflater.inflate(R.layout.intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        diseaseChipGroup = view.findViewById(R.id.disease_chipgroup)
        diseaseChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            Log.i("ChipGroup", "A checkbox has changed")
            val anyChecked = checkedIds.isNotEmpty()

            button.isEnabled = anyChecked
        }

        button = view.findViewById(R.id.continue_button)
        button.setOnClickListener {
            // Gather all Preferences
            val preferences = Bundle()
            preferences.putBoolean("Addison", (diseaseChipGroup.getChildAt(0) as Chip).isChecked)

            // Get a reference to the MainActivity and trigger navigation
            (activity as? MainActivity)?.navigateToMainFragment(preferences)
        }
    }
}