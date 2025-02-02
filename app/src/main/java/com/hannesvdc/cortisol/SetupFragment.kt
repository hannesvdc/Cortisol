package com.hannesvdc.cortisol

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.json.JSONObject

class SetupFragment(private val treatmentPlanKey: String) : Fragment() {

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
            proceedToMainActivity()
        }
    }

    private fun proceedToMainActivity() {
        val diseases = Bundle()
        diseases.putBoolean("Addison", (diseaseChipGroup.getChildAt(0) as Chip).isChecked)

        // Store the diseases first in shared preferences
        val diseasesString = bundleToJsonString(diseases)
        val sharedPreferences = requireContext().getSharedPreferences(treatmentPlanKey, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("Diseases", diseasesString).apply()

        // Then proceed to the Main Fragment
        (activity as? MainActivity)?.navigateToMainFragment(diseases, true)
    }

    private fun bundleToJsonString(bundle: Bundle): String {
        val jsonObject = JSONObject()
        for (key in bundle.keySet()) {
            when (val value = bundle.get(key)) {
                is String -> jsonObject.put(key, value)
                is Int -> jsonObject.put(key, value)
                is Boolean -> jsonObject.put(key, value)
                is Double -> jsonObject.put(key, value)
                is Long -> jsonObject.put(key, value)
                is Float -> jsonObject.put(key, value)
                is Bundle -> jsonObject.put(key, bundleToJsonString(value))
                else -> jsonObject.put(key, value.toString())
            }
        }
        return jsonObject.toString()
    }
}