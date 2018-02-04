/*
 * Copyright 2018 The twicalico authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.moko256.twicalico;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.github.moko256.twicalico.database.TokenSQLiteOpenHelper;
import com.github.moko256.twicalico.entity.AccessToken;
import com.github.moko256.twicalico.intent.AppCustomTabsKt;
import com.github.moko256.twicalico.text.TwitterStringUtils;

import java.util.Date;

/**
 * Created by moko256 on 2016/03/28.
 *
 * @author moko256
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.settings, s);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getArguments()==null||getArguments().getString(ARG_PREFERENCE_ROOT)==null){
            SharedPreferences defaultSharedPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());

            TokenSQLiteOpenHelper helper=new TokenSQLiteOpenHelper(getContext());

            AccessToken[] accessTokens = helper.getAccessTokens();

            CharSequence[] entries=new CharSequence[accessTokens.length + 1];
            CharSequence[] entryValues=new CharSequence[accessTokens.length + 1];

            for (int i = 0; i < accessTokens.length; i++) {
                AccessToken accessToken = accessTokens[i];

                entries[i] = TwitterStringUtils.plusAtMark(accessToken.getScreenName(), accessToken.getUrl());
                entryValues[i] = accessToken.getKeyString();
            }

            entries[entries.length-1]=getString(R.string.add_account);
            entryValues[entryValues.length-1]="-1";

            ListPreference nowAccountList=(ListPreference) findPreference("AccountKey");
            nowAccountList.setEntries(entries);
            nowAccountList.setEntryValues(entryValues);
            nowAccountList.setDefaultValue(defaultSharedPreferences.getString("AccountKey","-1"));
            nowAccountList.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        if (newValue.equals("-1")){
                            GlobalApplication.twitter = null;
                            startActivity(
                                    new Intent(getContext(),OAuthActivity.class)
                            );
                        } else {
                            TokenSQLiteOpenHelper tokenOpenHelper = new TokenSQLiteOpenHelper(this.getContext());
                            AccessToken accessToken=tokenOpenHelper.getAccessToken((String) newValue);
                            tokenOpenHelper.close();

                            ((GlobalApplication) getActivity().getApplication()).initTwitter(accessToken);
                            startActivity(
                                    new Intent(getContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                            );
                        }
                        return true;
                    }
            );

            findPreference("logout").setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.confirm_logout)
                        .setCancelable(true)
                        .setPositiveButton(R.string.do_logout,
                                (dialog, i) -> {
                                    helper.deleteAccessToken(
                                            helper.getAccessToken(
                                                    defaultSharedPreferences.getString("AccountKey","-1")
                                            )
                                    );

                                    int point = helper.getSize() - 1;
                                    if (point != -1) {
                                        AccessToken accessToken = helper.getAccessTokens()[point];

                                        defaultSharedPreferences
                                                .edit()
                                                .putString("AccountKey", accessToken.getKeyString())
                                                .apply();

                                        ((GlobalApplication) getActivity().getApplication()).initTwitter(accessToken);
                                        startActivity(
                                                new Intent(getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                                        );
                                    } else {
                                        defaultSharedPreferences
                                                .edit()
                                                .putString("AccountKey", "-1")
                                                .apply();
                                        GlobalApplication.twitter = null;
                                        startActivity(
                                                new Intent(getContext(), OAuthActivity.class)
                                        );
                                    }
                                }
                        )
                        .setNeutralButton(R.string.back,(dialog, i) -> dialog.cancel())
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

            Preference causeError = findPreference("cause_error");
            causeError.setOnPreferenceClickListener(preference -> {
                getActivity().finish();
                throw new NullPointerException();
            });

            Preference licenseThisApp=findPreference("license_at_this_app");
            licenseThisApp.setOnPreferenceClickListener(preference -> {
                getContext().startActivity(
                        new Intent(getContext(), LicensesActivity.class)
                                .putExtra("title", getResources().getText(R.string.app_name))
                                .putExtra("library_name", "twicalico")
                );
                return true;
            });

            Preference sourceCodeLink=findPreference("source_code_link");
            sourceCodeLink.setOnPreferenceClickListener(preference -> {
                AppCustomTabsKt.launchChromeCustomTabs(getContext(), "https://github.com/moko256/twicalico");
                return false;
            });

            Preference version=findPreference("app_version");
            version.setSummary(BuildConfig.VERSION_NAME);
            version.setOnPreferenceClickListener(preference -> {
                Date birthday=new Date(1446956982000L);
                Toast.makeText(
                        getContext(),
                        getString(
                                R.string.birthday_of_this_app_is,
                                birthday
                        ) + "\n\n" + getString(
                                R.string.age_of_this_app_is,
                                (int) Math.floor((new Date().getTime()-birthday.getTime())/(31557600000L))
                        ),
                        Toast.LENGTH_LONG
                ).show();
                return false;
            });
        } else if(getArguments().getString(ARG_PREFERENCE_ROOT).equals("license")){
            final String license_keys[]=new String[]{
                    "support_v4",
                    "support_v7",
                    "support_v13",
                    "support_v14",
                    "support_design",
                    "support_custom_tabs",
                    "exo_player",
                    "glide",
                    "gson",
                    "photo_view",
                    "okhttp",
                    "mastodon4j",
                    "twitter4j",
                    "twitter_text",
                    "rx_java",
                    "rx_android"
            };

            for (String name : license_keys) {
                findPreference("license_lib_" + name).setOnPreferenceClickListener(preference -> {
                    getContext().startActivity(
                            new Intent(getContext(), LicensesActivity.class)
                                    .putExtra("title", preference.getTitle())
                                    .putExtra("library_name", name)
                    );
                    return true;
                });
            }
        }

    }

}
