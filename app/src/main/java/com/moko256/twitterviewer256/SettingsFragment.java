package com.moko256.twitterviewer256;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import java.util.Set;

/**
 * Created by moko256 on GitHub on 2016/03/28.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences defaultSharedPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> accountsIdLongStrSet=defaultSharedPreferences.getStringSet("AccountsList",null);

        ListPreference nowAccountList=(ListPreference) findPreference("nowUserDataFileInt");
        nowAccountList.setDefaultValue(defaultSharedPreferences.getString("AccountPoint","-1"));
        nowAccountList.setEntries((CharSequence[])accountsIdLongStrSet.toArray());
        nowAccountList.setEntryValues((CharSequence[])accountsIdLongStrSet.toArray());
    }
}
