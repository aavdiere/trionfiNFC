package co.vandierendonck.trionfinfc.views

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import co.vandierendonck.trionfinfc.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(bundle: Bundle, s: String) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preferences)
    }
}