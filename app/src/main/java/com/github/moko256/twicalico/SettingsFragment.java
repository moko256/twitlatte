package com.github.moko256.twicalico;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by moko256 on 2016/03/28.
 *
 * @author moko256
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

        TokenSQLiteOpenHelper helper=new TokenSQLiteOpenHelper(getContext());
        SQLiteDatabase database=helper.getReadableDatabase();
        Cursor c=database.query("AccountTokenList",new String[]{"userName","userId","token","tokenSecret"},null,null,null,null,null);

        CharSequence[] entries=new CharSequence[(int) DatabaseUtils.queryNumEntries(database,"AccountTokenList")+1];
        CharSequence[] entryValues=new CharSequence[(int) DatabaseUtils.queryNumEntries(database,"AccountTokenList")+1];

        while (c.moveToNext()){
            entries[c.getPosition()]=c.getString(0);
            entryValues[c.getPosition()]=String.valueOf(c.getPosition());
        }

        entries[entries.length-1]=getString(R.string.add_account);
        entryValues[entryValues.length-1]="-1";

        c.close();
        database.close();

        ListPreference nowAccountList=(ListPreference) findPreference("AccountPoint");
        nowAccountList.setEntries(entries);
        nowAccountList.setEntryValues(entryValues);
        nowAccountList.setDefaultValue(defaultSharedPreferences.getString("AccountPoint","-1"));
        nowAccountList.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    if (newValue.equals("-1")){
                        getActivity().finish();
                        startActivity(new Intent(getContext(),OAuthActivity.class));
                    }
                    return true;
                }
        );

        findPreference("logout").setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.logout)
                    .setMessage("Logout?")
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok,
                            (dialog, i) ->
                                    nowAccountList.getOnPreferenceChangeListener().onPreferenceChange(preference,String.valueOf(helper.deleteAccessToken(GlobalApplication.user.getId())-1))
                    )
                    .setNegativeButton(android.R.string.cancel,(dialog, i) -> dialog.cancel())
                    .show();
            return false;
        });

        ListPreference nowThemeMode=(ListPreference) findPreference("nightModeType");
        nowThemeMode.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    switch(String.valueOf(newValue)){
                        case "mode_night_no":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);break;
                        case "mode_night_auto":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);break;
                        case "mode_night_follow_system":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);break;
                        case "mode_night_yes":AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);break;
                        default:AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                    return true;
                }
        );

        findPreference("license").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getContext(),LicensesActivity.class));
            return false;
        });

        Preference version=findPreference("app_version");
        version.setSummary("Version "+ BuildConfig.VERSION_NAME);
        version.setOnPreferenceClickListener(preference -> {
            Date birthday=new Date(1446956982000L);
            Toast.makeText(getContext(), "This application was born on "+birthday.toString(), Toast.LENGTH_SHORT).show();
            return false;
        });

    }
}
