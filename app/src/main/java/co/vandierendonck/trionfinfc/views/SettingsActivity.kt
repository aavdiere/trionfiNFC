package co.vandierendonck.trionfinfc.views

import android.preference.PreferenceActivity
import co.vandierendonck.trionfinfc.R

class SettingsActivity : PreferenceActivity() {
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.headers_preference, target)
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return SettingsFragment::class.java.name == fragmentName
    }
}