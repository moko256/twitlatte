/*
 * Copyright 2015-2018 The twitlatte authors
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

package com.github.moko256.twitlatte;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

import com.github.moko256.twitlatte.entity.AccessToken;
import com.github.moko256.twitlatte.intent.AppCustomTabsKt;
import com.github.moko256.twitlatte.model.AccountsModel;
import com.github.moko256.twitlatte.text.TwitterStringUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by moko256 on 2016/03/28.
 *
 * @author moko256
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private int eggCount = 3;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.settings, s);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String preferenceRoot = getArguments() != null? getArguments().getString(ARG_PREFERENCE_ROOT, null): null;
        if (preferenceRoot == null){
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            AccountsModel accountsModel = GlobalApplication.accountsModel;
            List<AccessToken> accessTokens = accountsModel.getAccessTokens();

            CharSequence[] entries = new CharSequence[accessTokens.size() + 1];
            CharSequence[] entryValues = new CharSequence[accessTokens.size() + 1];

            for (int i = 0; i < accessTokens.size(); i++) {
                AccessToken accessToken = accessTokens.get(i);

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
                            AccessToken accessToken = GlobalApplication.accountsModel.get((String) newValue);

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
                                    accountsModel.delete(accountsModel.get(
                                            defaultSharedPreferences.getString("AccountKey","-1")
                                    ));

                                    int point = accountsModel.size() - 1;
                                    if (point != -1) {
                                        AccessToken accessToken = accountsModel.getAccessTokens().get(point);

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
                        .setNegativeButton(android.R.string.cancel, null)
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

            Preference licenseThisApp=findPreference("license_at_this_app");
            licenseThisApp.setOnPreferenceClickListener(preference -> {
                getContext().startActivity(
                        new Intent(getContext(), LicensesActivity.class)
                                .putExtra("title", getResources().getText(R.string.app_name))
                                .putExtra("library_name", "twitlatte")
                );
                return true;
            });

            Preference sourceCodeLink=findPreference("source_code_link");
            sourceCodeLink.setOnPreferenceClickListener(preference -> {
                AppCustomTabsKt.launchChromeCustomTabs(getContext(), "https://github.com/moko256/twitlatte");
                return false;
            });

            Preference version=findPreference("app_version");
            version.setSummary(BuildConfig.VERSION_NAME);
            version.setOnPreferenceClickListener(preference -> {
                eggCount--;
                if (eggCount <= 0) {
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    eggCount = 3;
                }

                return false;
            });
        } else if (preferenceRoot.equals("license")) {
            PreferenceScreen license = getPreferenceScreen();
            for (int i = 0, length = license.getPreferenceCount(); i < length; i++) {
                license.getPreference(i).setOnPreferenceClickListener(preference -> {
                    getContext().startActivity(
                            new Intent(getContext(), LicensesActivity.class)
                                    .putExtra("title", preference.getTitle())
                                    .putExtra("library_name", preference.getKey().substring(12)) // "license_lib_".length
                    );
                    return true;
                });
            }
        }

    }

}
