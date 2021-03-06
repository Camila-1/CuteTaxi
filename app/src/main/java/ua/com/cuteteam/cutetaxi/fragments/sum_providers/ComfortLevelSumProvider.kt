package ua.com.cuteteam.cutetaxi.fragments.sum_providers

import androidx.preference.ListPreference
import androidx.preference.Preference
import ua.com.cuteteam.cutetaxi.R

class ComfortLevelSumProvider : Preference.SummaryProvider<ListPreference> {
    override fun provideSummary(preference: ListPreference?): CharSequence {
        return preference?.value
            ?.let { preference.summary }
            ?: preference?.entry
            ?: preference?.context?.getString(R.string.not_set)!!
    }
}