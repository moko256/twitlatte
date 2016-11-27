package com.moko256.twitterviewer256;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * Created by moko256 on 2016/11/14.
 *
 * @author moko256
 */

public class LicensesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.licenses);
    }
}
